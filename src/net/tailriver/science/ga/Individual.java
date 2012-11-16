package net.tailriver.science.ga;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

public class Individual implements Cloneable, Comparable<Individual>,
		GenoTypeWatcher, Serializable {
	private static final long serialVersionUID = -35172888649712656L;

	protected GenoType genoType;
	protected transient Object[] phenoType;
	private transient double fitness;

	public Individual(GenoType genoType) {
		setGenoType(genoType);
	}

	public final boolean getGenoTypeBoolean(int i) {
		return genoType.getBoolean(i);
	}

	public final BitSet getGenoTypeBitSet(int i) {
		return genoType.getBitSet(i);
	}

	public final long getGenoTypeLong(int i) {
		return genoType.getLong(i);
	}

	/**
	 * 
	 * @param i
	 *            index of geno-type.
	 * @return {@code Double.longBitsToDouble(getLong(i))}.
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 *             if the bit size of the specified index is not 64 bit.
	 * @see GenoType#getLong(int)
	 */
	public final double getGenoTypeDouble(int i) {
		if (genoType.getLength(i) != 64)
			throw new IllegalArgumentException(
					"GenoType must be 64 bit for index: " + i);
		return Double.longBitsToDouble(genoType.getLong(i));
	}

	/**
	 * 
	 * @param i
	 *            index of geno-type.
	 * @param min
	 *            minimum value (inclusive).
	 * @param max
	 *            maximum value (inclusive).
	 * @return {@code min + getLong(i) / resolution * (max - min)}<br>
	 *         where {@code resolution} is 2<sup>nbit</sup> - 1.
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 */
	public final double getGenoTypeDouble(int i, double min, double max) {
		int nbit = genoType.getLength(i);
		double resolution = Math.pow(2, nbit) - 1;
		return min + genoType.getLong(i) / resolution * (max - min);
	}

	public final void setGenoType(GenoType genoType) {
		this.genoType = genoType;
		phenoType = new Object[genoType.length];
		fitness = Double.NaN;
	}

	public final Object getPhenoType(int i) {
		return phenoType[i];
	}

	public final void setPhenoType(int i, Object phenoType) {
		this.phenoType[i] = phenoType;
	}

	public final boolean hasFitness() {
		return !Double.isNaN(fitness);
	}

	public final double getFitness() {
		return fitness;
	}

	public final void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public void activateWatcher() {
		genoType.setGenoTypeWatcher(this);
	}

	public void deactivateWatcher() {
		genoType.setGenoTypeWatcher(null);
	}

	/**
	 * 
	 * @param random
	 *            random seed.
	 * @throws NullPointerException
	 *             if {@code random} is null.
	 */
	public void randomize(Random random) {
		mutate(random, 0.5);
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
	 *             if {@code probability} is NaN, less than 0 or greater than 1.
	 */
	public void mutate(Random random, double probability) {
		GeneticAlgorithm.probabilityCheck("mutation rate", probability);

		Mask mask = genoType.getMask();
		for (int i = 0, max = mask.length; i < max; i++) {
			if (random.nextDouble() < probability)
				mask.set(i);
		}
		genoType.invert(mask);
	}

	/**
	 * 
	 * @param o
	 * @return <code>true</code> if this is greater than {@code o};
	 *         <code>false</code> otherwise.
	 */
	public final boolean isGreaterThan(Individual o) {
		return o == null || compareTo(o) > 0;
	}

	/**
	 * This implementation calls {@code compareTo(o) < 0}.
	 * 
	 * @param o
	 * @return <code>true</code> if this is less than {@code o};
	 *         <code>false</code> otherwise.
	 */
	public final boolean isLessThan(Individual o) {
		return o == null || compareTo(o) < 0;
	}

	@Override
	public void onGenoTypeChanged() {
		for (int i = 0, max = phenoType.length; i < max; i++) {
			phenoType[i] = null;
		}
		fitness = Double.NaN;
	}

	@Override
	public int compareTo(Individual o) {
		if (hasFitness() && o.hasFitness()) {
			return Double.compare(fitness, o.fitness);
		}
		throw new IllegalStateException("invalid fitness value");
	}

	@Override
	public int hashCode() {
		return genoType.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public Individual clone() {
		try {
			Individual clone = (Individual) super.clone();
			clone.genoType = new GenoType(genoType);
			clone.phenoType = Arrays.copyOf(phenoType, phenoType.length);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append(Integer.toHexString(genoType.hashCode())).append("#")
				.append(fitness).toString();
	}

	public String toGenoTypeString() {
		return genoType.toString();
	}

	public String toPhenoTypeString() {
		return Arrays.deepToString(phenoType);
	}

	public void print() {
		System.out.println(this);
		System.out.println(toGenoTypeString());
		System.out.println(toPhenoTypeString());
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param random
	 * @throws NullPointerException
	 *             if arguments contain null.
	 * @throws IllegalArgumentException
	 *             if {@link GenoType}s of {@link Individual}s point same
	 *             address, or they are incompatible.
	 */
	public static void crossOverSinglePoint(Individual x, Individual y,
			Random random) {
		Mask mask = x.genoType.getMask();
		int p = random.nextInt(mask.length);
		mask.set(p, mask.length);
		GenoType.swap(x.genoType, y.genoType, mask);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param random
	 * @throws NullPointerException
	 *             if arguments contain null.
	 * @throws IllegalArgumentException
	 *             if {@link GenoType}s of {@link Individual}s point same
	 *             address, or they are incompatible.
	 */
	public static void crossOverTwoPoint(Individual x, Individual y,
			Random random) {
		Mask mask = x.genoType.getMask();
		int p = random.nextInt(mask.length);
		int q = random.nextInt(mask.length);
		mask.set(Math.min(p, q), Math.max(p, q));
		GenoType.swap(x.genoType, y.genoType, mask);
	}

	/**
	 * 
	 * @param x
	 *            a individual to cross.
	 * @param y
	 *            another individual to cross.
	 * @param random
	 *            random seed.
	 * @throws NullPointerException
	 *             if arguments contain null.
	 * @throws IllegalArgumentException
	 *             if {@link GenoType}s of {@link Individual}s point same
	 *             address, or they are incompatible.
	 */
	public static void crossOverUniform(Individual x, Individual y,
			Random random) {
		Mask mask = x.genoType.getMask();
		for (int i = 0, max = mask.length; i < max; i++) {
			if (random.nextBoolean())
				mask.set(i);
		}
		GenoType.swap(x.genoType, y.genoType, mask);
	}
}
