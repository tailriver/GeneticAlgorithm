package net.tailriver.science.ga;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * This class represents a chromosome.
 * 
 * @author tailriver
 * 
 */
public class GenoType implements Serializable {
	private static final long serialVersionUID = -6524107047593002784L;

	/**
	 * Length of the chromosome. It does not equals to the bit length
	 * (information length) of chromosome.
	 */
	public final int length;

	/**
	 * Offset information of chromosome. The length must be {@link #length} - 1.
	 * 
	 * @serial
	 */
	private final int[] offsetArray;

	/**
	 * Chromosome bits. To improve performance, it is implemented by a
	 * {@link BitSet} object rather than <code>boolean[]</code> or something
	 * like that.
	 * 
	 * @serial
	 */
	private final BitSet chromosome;

	/**
	 * Observer pattern.
	 */
	private transient GenoTypeWatcher watcher;

	/**
	 * Creates a new chromosome from {@link Creator#inflate()} or other factory
	 * method.
	 * 
	 * @param nbitList
	 * @throws NullPointerException
	 *             if {@code nbitList} is null.
	 * @throws IllegalArgumentException
	 *             if {@code nbitList} is empty or contains non-positive value.
	 */
	protected GenoType(List<Integer> nbitList) {
		if (nbitList.isEmpty())
			throw new IllegalArgumentException("list is empty");

		length = nbitList.size();
		offsetArray = new int[length + 1];
		offsetArray[0] = 0;
		for (int i = 0; i < length; i++) {
			int nbit = nbitList.get(i);
			offsetArray[i + 1] = offsetArray[i] + nbit;
			if (nbit < 1)
				throw new IllegalArgumentException("list contains <1 value");
		}
		chromosome = new BitSet(offsetArray[length]);
	}

	/**
	 * Creates {@link GenoType} from another object.
	 * 
	 * <ul>
	 * <li>{@code original == copied} is <code>false</code>.
	 * <li>{@code original.equals(copied)} is <code>true</code> (chromosome is
	 * deep-copied).</li>
	 * <li>{@code original.equalsSchema(copied)} is also <code>true</code>
	 * (index information is shared).</li>
	 * <li>Specified {@link GenoTypeWatcher} object is lost. Please reset by
	 * {@link #setGenoTypeWatcher(GenoTypeWatcher)} if need.</li>
	 * </ul>
	 * 
	 * @param original
	 *            copy source.
	 */
	public GenoType(GenoType original) {
		// shared address
		length = original.length;
		offsetArray = original.offsetArray;

		// deep copy
		chromosome = (BitSet) original.chromosome.clone();

		// watcher is null
	}

	/**
	 * 
	 * @param i
	 * @return bit size of specified index.
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public final int getLength(int i) {
		return offsetArray[i + 1] - offsetArray[i];
	}

	/**
	 * 
	 * @param i
	 *            index of geno-type
	 * @return boolean value of specified index of geno-type.
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 *             if the bit size of specified index is not 1 bit.
	 */
	public final boolean getBoolean(int i) {
		checkBooleanRange(i);
		return chromosome.get(offsetArray[i]);
	}

	/**
	 * 
	 * @param i
	 *            index of geno-type.
	 * @return BitSet value of specified index of geno-type.
	 * @throws ArrayIndexOutOfBoundsException
	 * @see GenoType#getLong(int)
	 */
	public final BitSet getBitSet(int i) {
		return chromosome.get(offsetArray[i], offsetArray[i + 1]);
	}

	/**
	 * 
	 * @param i
	 *            index of geno-type.
	 * @return long value of specified index of geno-type.
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 *             if the bit size of the specified index is more than 64 bit.
	 * @see GenoType#getBitSet(int)
	 */
	public final long getLong(int i) {
		checkLongRange(i);
		long[] v = getBitSet(i).toLongArray();
		return v.length == 1 ? v[0] : 0;
	}

	protected final void setBoolean(int i, boolean value) {
		checkBooleanRange(i);
		chromosome.set(offsetArray[i], value);
		notifyGenoTypeChanged();
	}

	protected final void setBitSet(int i, BitSet value) {
		int offset = offsetArray[i];
		int max = offsetArray[i + 1] - offset;
		for (int j = 0; j < max; j++) {
			chromosome.set(offset + j, value.get(j));
		}
		notifyGenoTypeChanged();
	}

	private final void checkBooleanRange(int i) {
		if (getLength(i) != 1)
			throw new IllegalArgumentException("index [" + i
					+ "] must be 1 bit");
	}

	private final void checkLongRange(int i) {
		if (getLength(i) > 64)
			throw new IllegalArgumentException("index [" + i
					+ "] must be 64 bit or less");
	}

	public void setGenoTypeWatcher(GenoTypeWatcher watcher) {
		this.watcher = watcher;
	}

	protected void notifyGenoTypeChanged() {
		if (watcher != null)
			watcher.onGenoTypeChanged();
	}

	public Mask getMask() {
		return new Mask(offsetArray[length]);
	}

	/**
	 * 
	 * @param mask
	 * @throws NullPointerException
	 *             if {@code mask} is null.
	 */
	public void invert(Mask mask) {
		if (mask.isEmpty())
			return;

		for (int i = mask.nextSetBit(0); i >= 0; i = mask.nextSetBit(i + 1))
			chromosome.flip(i);
		notifyGenoTypeChanged();
	}

	/**
	 * 
	 * @param a
	 *            object to swap.
	 * @param b
	 *            object to swap.
	 * @param mask
	 *            A {@link BitSet} object. They swap where the bit is
	 *            <code>true</code>.
	 * @throws NullPointerException
	 *             if arguments contain null.
	 * @throws IllegalArgumentException
	 *             if {@link GenoType}s point same address, or they are
	 *             incompatible ({@code a.equalsSchema(b) == false}).
	 */
	public static void swap(GenoType a, GenoType b, Mask mask) {
		if (a == b)
			throw new IllegalArgumentException("a and b point the same address");
		if (!a.equalsSchema(b))
			throw new IllegalArgumentException("incompatible chromosome type");
		if (mask.isEmpty())
			return;

		for (int i = mask.nextSetBit(0); i >= 0; i = mask.nextSetBit(i + 1)) {
			boolean temp = a.chromosome.get(i);
			a.chromosome.set(i, b.chromosome.get(i));
			b.chromosome.set(i, temp);
		}
		a.notifyGenoTypeChanged();
		b.notifyGenoTypeChanged();
	}

	/**
	 * Returns hash code of chromosome object.
	 */
	@Override
	public int hashCode() {
		return chromosome.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GenoType) {
			GenoType c = (GenoType) obj;
			return this == c || chromosome.equals(c.chromosome)
					&& equalsSchema(c);
		}
		return false;
	}

	/**
	 * 
	 * @param c
	 *            the reference object with which to compare.
	 * @return <code>true</code> if {@code c == this} or they are created same
	 *         condition in {@link GenoType.Creator}; <code>false</code>
	 *         otherwise.
	 * @throws NullPointerException
	 *             if {@code c} is null.
	 */
	protected boolean equalsSchema(GenoType c) {
		return Arrays.equals(offsetArray, c.offsetArray);
	}

	@Override
	public String toString() {
		final char delimiter = ' ';
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int nbit = getLength(i);
			long[] array = getBitSet(i).toLongArray();
			if (array.length == 0) {
				array = new long[] { 0 };
			}
			for (long l : array) {
				String binary = Long.toBinaryString(l);
				for (int j = 0, max = nbit - binary.length(); j < max; j++) {
					sb.append(0);
				}
				sb.append(binary);
			}
			sb.append(delimiter);
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	/**
	 * Factory class for {@link GenoType}.
	 * 
	 * @author tailriver
	 * 
	 */
	public static class Creator {
		protected List<Integer> nbitList = new ArrayList<>();

		/**
		 * 
		 * This implementation calls {@code append(nbit, 1)}.
		 * 
		 * @param nbit
		 *            bit size (resolution).
		 * @return this creator object.
		 * @throws IllegalArgumentException
		 *             if {@code nbit} < 1.
		 */
		public Creator append(int nbit) {
			return append(nbit, 1);
		}

		/**
		 * 
		 * @param nbit
		 *            bit size (resolution).
		 * @param times
		 *            times to repeat.
		 * @return this creator object.
		 * @throws IllegalArgumentException
		 *             if {@code nbit} < 1 or {@code times} < 1.
		 */
		public Creator append(int nbit, int times) {
			if (nbit < 1)
				throw new IllegalArgumentException("nbit < 1: " + nbit);
			if (times < 1)
				throw new IllegalArgumentException("times < 1: " + times);

			for (int i = 0; i < times; i++)
				nbitList.add(nbit);
			return this;
		}

		/**
		 * Create {@link GenoType} from this {@link Creator}.
		 * 
		 * @return newly created {@link GenoType} object.
		 * @throws IllegalArgumentException
		 *             if nothing appended.
		 */
		public GenoType inflate() {
			return new GenoType(nbitList);
		}
	}
}
