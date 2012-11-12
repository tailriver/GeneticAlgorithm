package net.tailriver.science.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class Chromosome {
	private final int[] offsetArray;
	private final int[] nbitArray;
	private final BitSet genoType;
	private final Object[] phenoType;
	private final int bitSize;
	private ChromosomeWatcher watcher;

	protected Chromosome(List<Integer> offsetList, List<Integer> nbitList) {
		int size = offsetList.size();
		bitSize = offsetList.get(size - 1) + nbitList.get(size - 1);
		offsetArray = new int[size];
		nbitArray = new int[size];
		genoType = new BitSet(bitSize);
		phenoType = new Object[size];
		for (int i = 0; i < size; i++) {
			offsetArray[i] = offsetList.get(i);
			nbitArray[i] = nbitList.get(i);
		}
	}

	// shares address: offsetAray, nbitArray
	// shallow copy: phenoType
	// deep copy: genoType
	public Chromosome(Chromosome original) {
		bitSize = original.bitSize;
		offsetArray = original.offsetArray;
		nbitArray = original.nbitArray;
		genoType = (BitSet) original.genoType.clone();
		phenoType = original.phenoType.clone();
	}

	public int bitSizeAt(int i) {
		return nbitArray[i];
	}

	public int bitSizeTotal() {
		return bitSize;
	}

	public BitSet getBitSet(int i) {
		int offset = offsetArray[i];
		return genoType.get(offset, offset + nbitArray[i]);
	}

	public long getLong(int i) {
		if (nbitArray[i] > 64) {
			throw new IllegalArgumentException();
		}
		long[] v = getBitSet(i).toLongArray();
		return v.length == 1 ? v[0] : 0;
	}

	public double getDouble(int i) {
		return Double.longBitsToDouble(getLong(i));
	}

	public double getScaled(int i, double min, double max) {
		double resolution = Math.pow(2, nbitArray[i]) - 1;
		return min + getLong(i) / resolution * (max - min);
	}

	public Object[] getPhenoType() {
		return phenoType.clone();
	}

	public Object getPhenoType(int i) {
		return phenoType[i];
	}

	public void setPhenoType(int i, Object phenoType) {
		this.phenoType[i] = phenoType;
	}

	public void setOnChromosomeChanged(ChromosomeWatcher watcher) {
		this.watcher = watcher;
	}

	protected void notifyChromosomeChanged() {
		for (int i = 0, max = phenoType.length; i < max; i++) {
			phenoType[i] = null;
		}
		if (watcher != null) {
			watcher.onChromosomeChanged();
		}
	}

	public void randomize(Random random) {
		if (random == null) {
			throw new IllegalArgumentException();
		}

		for (int i = 0; i < bitSize; i++) {
			genoType.set(i, random.nextBoolean());
		}
	}

	public void mutate(Random random, double mutationRate) {
		if (random == null || Double.isNaN(mutationRate)) {
			throw new IllegalArgumentException();
		}
		if (mutationRate < 0 || mutationRate > 1) {
			throw new IndexOutOfBoundsException("out of range: " + mutationRate);
		}

		boolean changed = false;
		for (int i = 0; i < bitSize; i++) {
			if (random.nextDouble() < mutationRate) {
				genoType.flip(i);
				changed = true;
			}
		}
		if (changed) {
			notifyChromosomeChanged();
		}
	}

	public static void swap(Chromosome a, Chromosome b, BitSet mask) {
		if (a == null || b == null || a == b || !a.equalsSchema(b)
				|| mask == null) {
			throw new IllegalArgumentException();
		}
		for (int i = 0, max = a.bitSize; i < max; i++) {
			if (mask.get(i)) {
				boolean temp = a.genoType.get(i);
				a.genoType.set(i, b.genoType.get(i));
				b.genoType.set(i, temp);
			}
		}
		a.notifyChromosomeChanged();
		b.notifyChromosomeChanged();
	}

	@Override
	public int hashCode() {
		return genoType.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof Chromosome) {
			Chromosome c = (Chromosome) obj;
			return hashCode() == c.hashCode() && genoType.equals(c.genoType)
					&& Arrays.equals(phenoType, c.phenoType) && equalsSchema(c);
		}
		return false;
	}

	public boolean equalsSchema(Chromosome c) {
		if (this == c)
			return true;
		return Arrays.equals(offsetArray, c.offsetArray)
				&& Arrays.equals(nbitArray, c.nbitArray);
	}

	@Override
	public String toString() {
		final String delimiter = " ";
		StringBuilder sb = new StringBuilder();
		for (int i = 0, imax = offsetArray.length; i < imax; i++) {
			int length = nbitArray[i];
			long[] array = getBitSet(i).toLongArray();
			if (array.length == 0) {
				array = new long[] { 0 };
			}
			for (long l : array) {
				String binary = Long.toBinaryString(l);
				for (int j = 0, max = length - binary.length(); j < max; j++) {
					sb.append(0);
				}
				sb.append(Long.toBinaryString(l));
			}
			sb.append(delimiter);
		}
		return sb.delete(sb.length() - delimiter.length(), sb.length())
				.toString();
	}

	public static class Creator {
		private List<Integer> indexList = new ArrayList<>();
		private List<Integer> nbitList = new ArrayList<>();
		private int head = 0;

		public Creator append(int nbit, int count) {
			if (nbit < 1 || count < 1) {
				throw new IllegalArgumentException();
			}

			for (int i = 0; i < count; i++) {
				indexList.add(head);
				nbitList.add(nbit);
				head += nbit;
			}
			return this;
		}

		public Chromosome inflate() {
			return new Chromosome(indexList, nbitList);
		}
	}
}
