package net.tailriver.science.ga;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

/**
 * This class represents an individual. It has a raw {@link GenoType genetic
 * code object} (so called geno-type), an aggregation of objects (so called
 * pheno-type) and a real number value (so called fitness).
 * 
 * <p>
 * Pheno-type can store arbitrary type of object. The stored data is not used in
 * library (except methods which describe internal state of this object, such as
 * {@link #toString()}). However, it is invalidated (set to null) when geno-type
 * is changed by {@link #mutate(Random, double) mutation} or crossover
 * operations.
 * 
 * @author tailriver
 * 
 */
public class Individual implements Cloneable, Comparable<Individual>,
		GenoTypeWatcher, Serializable {
	private static final long serialVersionUID = -35172888649712656L;

	/**
	 * @serial
	 */
	protected GenoType genoType;

	/**
	 * aggregation of pheno-type objects.
	 */
	protected transient Object[] phenoType;

	/**
	 * fitness value. When it is {@link Double#NaN}, it represents this and
	 * contents of pheno-type are invalid and need to be recalculated.
	 */
	private transient double fitness;

	/**
	 * 
	 * @param genoType
	 */
	public Individual(GenoType genoType) {
		setGenoType(genoType);
	}

	/**
	 * Gets boolean value from specified index of chromosome.
	 * 
	 * @param i
	 *            index of chromosome.
	 * @return boolean value of specified index of chromosome.
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 * @see GenoType#getBoolean(int)
	 */
	public final boolean getGenoTypeBoolean(int i) {
		return genoType.getBoolean(i);
	}

	/**
	 * Gets {@link BitSet} value from specified index of chromosome.
	 * 
	 * @param i
	 *            index of chromosome.
	 * @return BitSet value of specified index of chromosome.
	 * @throws ArrayIndexOutOfBoundsException
	 * @see GenoType#getBitSet(int)
	 */
	public final BitSet getGenoTypeBitSet(int i) {
		return genoType.getBitSet(i);
	}

	/**
	 * Gets long value from specified index of chromosome.
	 * 
	 * @param i
	 *            index of chromosome.
	 * @return long value of specified index of chromosome.
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 * @see GenoType#getLong(int)
	 */
	public final long getGenoTypeLong(int i) {
		return genoType.getLong(i);
	}

	/**
	 * Gets full-ranged double value from specified index of chromosome.
	 * 
	 * @param i
	 *            index of chromosome.
	 * @return {@code Double.longBitsToDouble(getLong(i))}.
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 *             if the bit size of the specified index is not 64 bit.
	 * @see GenoType#getLong(int)
	 */
	public final double getGenoTypeDouble(int i) {
		if (genoType.getLength(i) != Double.SIZE)
			throw new IllegalArgumentException("GenoType must be "
					+ Double.SIZE + " bit for index: " + i);
		return Double.longBitsToDouble(genoType.getLong(i));
	}

	/**
	 * Gets linear-scaled real number value from specified index of chromosome.
	 * 
	 * @param i
	 *            index of chromosome.
	 * @param min
	 *            minimum value (inclusive).
	 * @param max
	 *            maximum value (inclusive).
	 * @return {@code min + getLong(i) / resolution * (max - min)}<br>
	 *         where {@code resolution} is 2<sup>nbit</sup> - 1.
	 * @throws ArrayIndexOutOfBoundsException
	 * @throws IllegalArgumentException
	 * @see GenoType#getLong(int)
	 */
	public final double getGenoTypeDouble(int i, double min, double max) {
		int nbit = genoType.getLength(i);
		double resolution = Math.pow(2, nbit) - 1;
		return min + genoType.getLong(i) / resolution * (max - min);
	}

	/**
	 * Sets a {@link GenoType} object to the instance.
	 * 
	 * @param genoType
	 *            a geno-type object.
	 * @throws NullPointerException
	 *             if {@code genoType} is <code>null</code>.
	 */
	public final void setGenoType(GenoType genoType) {
		this.genoType = genoType;
		phenoType = new Object[genoType.length];
		fitness = Double.NaN;
	}

	/**
	 * Gets a pheno-type object stored at the specific index of chromosome. You
	 * may need to cast it correctly because it always returns as a
	 * {@link Object}.
	 * 
	 * @param i
	 *            index of chromosome.
	 * @return stored object in specified index of chromosome. It may return
	 *         <code>null</code> if it has no stored object or invalidated due
	 *         to mutation and crossover operations.
	 */
	public final Object getPhenoType(int i) {
		return phenoType[i];
	}

	/**
	 * Sets an arbitrary object as a pheno-type object at the specific index of
	 * chromosome. Note stored objects are invalidated automatically and fill
	 * <code>null</code> when change of geno-type occurs. So you should not
	 * think that stored objects hold permanently.
	 * 
	 * @param i
	 *            index of chromosome.
	 * @param phenoType
	 *            object to store.
	 */
	public final void setPhenoType(int i, Object phenoType) {
		this.phenoType[i] = phenoType;
	}

	/**
	 * Checks the fitness value (by {@link #setFitness(double)}) is valid or
	 * not. If it returns <code>true</code>, fitness value and stored pheno-type
	 * objects are keep its previous state.
	 * 
	 * @return <code>true</code> if fitness is valid; <code>false</code>
	 *         otherwise.
	 */
	public final boolean hasFitness() {
		return !Double.isNaN(fitness);
	}

	/**
	 * Gets fitness value.
	 * 
	 * @return fitness value; {@link Double#NaN} if fitness is invalid.
	 */
	public final double getFitness() {
		return fitness;
	}

	/**
	 * Sets calculated fitness value.
	 * 
	 * @param fitness
	 *            fitness value. It should not be a {@link Double#NaN}.
	 * @throws IllegalArgumentException
	 *             if {@code fitness} is NaN.
	 */
	public final void setFitness(double fitness) {
		if (Double.isNaN(fitness))
			throw new IllegalArgumentException("fitness is NaN");
		this.fitness = fitness;
	}

	public void activateWatcher() {
		genoType.setGenoTypeWatcher(this);
	}

	public void deactivateWatcher() {
		genoType.setGenoTypeWatcher(null);
	}

	/**
	 * Calls {@code mutate(random, 0.5)}.
	 * 
	 * @param random
	 *            random seed.
	 * @throws NullPointerException
	 *             if {@code random} is <code>null</code>.
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
	 *             if {@code random} is <code>null</code>.
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
	 * Calls {@code compareTo(o) > 0}. It always returns <code>true</code> if
	 * {@code o} is <code>null</code>.
	 * 
	 * @param o
	 *            the object to be compared.
	 * @return <code>true</code> if this is greater than {@code o} or {@code o}
	 *         is <code>null</code>; <code>false</code> otherwise.
	 * @throws IllegalStateException
	 *             if fitness value of this and/or {@code o} is invalid.
	 * @see #isLessThan(Individual)
	 */
	public final boolean isGreaterThan(Individual o) {
		return o == null || compareTo(o) > 0;
	}

	/**
	 * Calls {@code compareTo(o) < 0}. It always returns <code>true</code> if
	 * {@code o} is <code>null</code>.
	 * 
	 * @param o
	 *            the object to be compared.
	 * @return <code>true</code> if this is less than {@code o} or {@code o} is
	 *         <code>null</code>; <code>false</code> otherwise.
	 * @throws IllegalStateException
	 *             if fitness value of this and/or {@code o} is invalid.
	 * @see #isGreaterThan(Individual)
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

	/**
	 * Note: this comparator imposes orderings that are inconsistent with
	 * equals.
	 * 
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 * @throws IllegalStateException
	 *             if fitness value of this and/or the specified object is
	 *             invalid.
	 * @see #isGreaterThan(Individual)
	 * @see #isLessThan(Individual)
	 */
	@Override
	public int compareTo(Individual o) {
		if (hasFitness() && o.hasFitness())
			return Double.compare(fitness, o.fitness);
		throw new IllegalStateException("invalid fitness: "
				+ (hasFitness() ? o : this));
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
		return Integer.toHexString(genoType.hashCode()) + '#' + fitness;
	}

	public String toGenoTypeString() {
		return genoType.toString();
	}

	public String toPhenoTypeString() {
		return Arrays.deepToString(phenoType);
	}

	/**
	 * @see #toString()
	 * @see #toGenoTypeString()
	 * @see #toPhenoTypeString()
	 */
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
