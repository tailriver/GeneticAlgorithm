package net.tailriver.science.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class Chromosome {
	public final int bitLength;
	public final Object[] phenoType;

	private final int[] offsetArray;
	private final BitSet genoType;
	private ChromosomeWatcher watcher;

	/**
	 * Create {@link Chromosome} from {@link Creator#inflate()}.
	 * 
	 * @param nbitList
	 * @throws NullPointerException
	 *             if {@code nbitList} is null.
	 * @throws IllegalArgumentException
	 *             if {@code nbitList} is empty or contains non-positive value.
	 */
	protected Chromosome(List<Integer> nbitList) {
		if (nbitList.isEmpty())
			throw new IllegalArgumentException("list is empty");

		int size = nbitList.size();
		offsetArray = new int[size + 1];
		offsetArray[0] = 0;
		for (int i = 0; i < size; i++) {
			int nbit = nbitList.get(i);
			offsetArray[i + 1] = offsetArray[i] + nbit;
			if (nbit < 1)
				throw new IllegalArgumentException("list contains <1 value");
		}
		bitLength = offsetArray[size];
		genoType = new BitSet(bitLength);
		phenoType = new Object[size];
	}

	/**
	 * Copy {@link Chromosome} from another {@link Chromosome} object.
	 * 
	 * <ul>
	 * <li>{@code original == copied} is <code>false</code>.
	 * <li>{@code original.equals(copied)} is <code>true</code>.</li>
	 * <li>Also {@code original.equalsSchema(copied)} is <code>true</code>.</li>
	 * <li>Geno-type is deep-copied.</li>
	 * <li>{@link Chromosome#phenoType} is shallow-copied.</li>
	 * <li> {@link Chromosome#setOnChromosomeChanged(ChromosomeWatcher)} is
	 * reseted (set to null).</li>
	 * </ul>
	 * 
	 * @param original
	 *            copy source of {@link Chromosome}.
	 */
	public Chromosome(Chromosome original) {
		// shared address
		bitLength = original.bitLength;
		offsetArray = original.offsetArray;

		// deep copy
		genoType = (BitSet) original.genoType.clone();

		// shallow copy
		phenoType = original.phenoType.clone();

		// watcher is null
	}

	/**
	 * 
	 * @param i
	 *            index of geno-type.
	 * @return BitSet value of specified index of geno-type.
	 * @throws IndexOutOfBoundsException
	 * @see Chromosome#getLong(int)
	 */
	public BitSet getBitSet(int i) {
		return genoType.get(offsetArray[i], offsetArray[i + 1]);
	}

	/**
	 * 
	 * @param i
	 *            index of geno-type.
	 * @return long value of specified index of geno-type.
	 * @throws IndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 *             if the bit size of the specified index is more than 64 bit.
	 * @see Chromosome#getBitSet(int)
	 * @see Chromosome#getDouble(int)
	 * @see Chromosome#getScaled(int, double, double)
	 */
	public long getLong(int i) {
		int nbit = offsetArray[i + 1] - offsetArray[i];
		if (nbit > 64)
			throw new IllegalArgumentException("geno-type is <= 64 bit: "
					+ nbit);

		long[] v = getBitSet(i).toLongArray();
		return v.length == 1 ? v[0] : 0;
	}

	/**
	 * 
	 * @param i
	 *            index of geno-type.
	 * @return {@code Double.longBitsToDouble(getLong(i))}.
	 * @throws IndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 *             if the bit size of the specified index is not 64 bit.
	 * @see Chromosome#getLong(int)
	 */
	public double getDouble(int i) {
		int nbit = offsetArray[i + 1] - offsetArray[i];
		if (nbit != 64)
			throw new IllegalArgumentException("geno-type is not 64 bit: "
					+ nbit);

		return Double.longBitsToDouble(getLong(i));
	}

	/**
	 * 
	 * @param i
	 *            index of geno-type.
	 * @param min
	 *            minimum value.
	 * @param max
	 *            maximum value.
	 * @return {@code min + getLong(i) / resolution * (max - min)}<br>
	 *         where {@code resolution} is 2<sup>nbit</sup> - 1.
	 * @throws IndexOutOfBoundsException
	 * @see Chromosome#getLong(int)
	 */
	public double getScaled(int i, double min, double max) {
		int nbit = offsetArray[i + 1] - offsetArray[i];
		double resolution = Math.pow(2, nbit) - 1;
		return min + getLong(i) / resolution * (max - min);
	}

	public void setOnChromosomeChanged(ChromosomeWatcher watcher) {
		this.watcher = watcher;
	}

	protected void notifyChromosomeChanged() {
		for (int i = 0; i < phenoType.length; i++) {
			phenoType[i] = null;
		}
		if (watcher != null) {
			watcher.onChromosomeChanged();
		}
	}

	/**
	 * 
	 * @param random
	 *            random seed.
	 * @throws NullPointerException
	 *             if {@code random} is null.
	 */
	public void randomize(Random random) {
		for (int i = 0; i < bitLength; i++) {
			genoType.set(i, random.nextBoolean());
		}
	}

	/**
	 * 
	 * @param random
	 *            random seed.
	 * @param probability
	 *            probability of mutation happens.
	 * @throws NullPointerException
	 *             if {@code random} is null.
	 * @throws IllegalArgumentException
	 *             if {@code probability} is {@code NaN}.
	 * @throws IndexOutOfBoundsException
	 *             if {@code (probability < 0 || probability > 1)}.
	 */
	public void mutate(Random random, double probability) {
		if (Double.isNaN(probability))
			throw new IllegalArgumentException("probability is NaN");
		if (probability < 0)
			throw new IndexOutOfBoundsException("probability < 0: "
					+ probability);
		if (probability > 1)
			throw new IndexOutOfBoundsException("probability > 1: "
					+ probability);

		boolean changed = false;
		for (int i = 0; i < bitLength; i++) {
			if (random.nextDouble() < probability) {
				genoType.flip(i);
				changed = true;
			}
		}
		if (changed) {
			notifyChromosomeChanged();
		}
	}

	/**
	 * 
	 * @param a
	 *            object to swap.
	 * @param b
	 *            object to swap.
	 * @param mask
	 *            A {@link BitSet} object. The {@link Chromosome}s swap where
	 *            the bit is <code>true</code>.
	 * @throws NullPointerException
	 *             if arguments contain null.
	 * @throws IllegalArgumentException
	 *             if {@link Chromosome}s point same address, or they are
	 *             incompatible ({@code a.equalsSchema(b) == false}).
	 */
	public static void swap(Chromosome a, Chromosome b, BitSet mask) {
		if (a == b)
			throw new IllegalArgumentException("chromosomes have same address");
		if (!a.equalsSchema(b))
			throw new IllegalArgumentException("incompatible chromosomes");

		for (int i = 0, max = a.bitLength; i < max; i++) {
			if (mask.get(i)) {
				boolean temp = a.genoType.get(i);
				a.genoType.set(i, b.genoType.get(i));
				b.genoType.set(i, temp);
			}
		}
		a.notifyChromosomeChanged();
		b.notifyChromosomeChanged();
	}

	/**
	 * Returns hash code of geno-type object.
	 */
	@Override
	public int hashCode() {
		return genoType.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Chromosome) {
			Chromosome c = (Chromosome) obj;
			return super.equals(obj) || genoType.equals(c.genoType)
					&& Arrays.equals(phenoType, c.phenoType) && equalsSchema(c);
		}
		return false;
	}

	/**
	 * 
	 * @param c
	 *            the reference object with which to compare.
	 * @return {@code true} if {@code c == this} or they are created same
	 *         condition in {@link Chromosome.Creator}; {@code false} otherwise.
	 */
	public boolean equalsSchema(Chromosome c) {
		return c != null
				&& (c == this || Arrays.equals(offsetArray, c.offsetArray));
	}

	@Override
	public String toString() {
		final char delimiter = ' ';
		StringBuilder sb = new StringBuilder();
		for (int i = 0, imax = offsetArray.length - 1; i < imax; i++) {
			int nbit = offsetArray[i + 1] - offsetArray[i];
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

	public static class Creator {
		private List<Integer> nbitList = new ArrayList<>();

		/**
		 * 
		 * @param nbit
		 *            bit size (resolution).
		 * @return {@link Creator} itself.
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
		 * @return {@link Creator} itself.
		 * @throws IllegalArgumentException
		 *             if {@code nbit} < 1 or {@code times} < 1.
		 */
		public Creator append(int nbit, int times) {
			if (nbit < 1)
				throw new IllegalArgumentException("nbit < 1: " + nbit);
			if (times < 1)
				throw new IllegalArgumentException("times < 1: " + times);

			for (int i = 0; i < times; i++) {
				nbitList.add(nbit);
			}
			return this;
		}

		/**
		 * Create {@link Chromosome} from this {@link Creator}.
		 * 
		 * @return newly created {@link Chromosome} object.
		 * @throws IllegalArgumentException
		 *             if nothing appended.
		 */
		public Chromosome inflate() {
			return new Chromosome(nbitList);
		}
	}
}
