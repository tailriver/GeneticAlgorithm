package net.tailriver.science.ga;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class Individual implements Cloneable, Comparable<Individual> {
	private Chromosome genom;
	private double fitness;

	public Individual(Chromosome chromosome) {
		this.genom = chromosome;
		resetFitness();
	}

	public long getLongGenom(int index) {
		return genom.getLong(index);
	}

	public double getDoubleGenom(int index) {
		return Double.longBitsToDouble(genom.getLong(index));
	}

	public double getScaledGenom(int index, double min, double max) {
		double resolution = Math.pow(2, genom.bitSizeAt(index)) - 1;
		return min + genom.getLong(index) / resolution * (max - min);
	}

	public BitSet getBitSetGenom(int index) {
		return genom.getBitSet(index);
	}

	private Object[] getPhenoType() {
		return genom.getPhenoType();
	}

	public Object getPhenoType(int index) {
		return getPhenoType()[index];
	}

	public void setPhenoType(int index, Object phenoType) {
		getPhenoType()[index] = phenoType;
	}

	public String getGenomString() {
		return genom.toString();
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

	private final void resetFitness() {
		fitness = Double.NaN;
	}

	public static Collection<Individual> cross(Individual x, Individual y,
			Random random, CrossOver strategy) {
		Individual a = x.clone();
		Individual b = y.clone();

		int max = a.genom.bitSizeTotal();
		if (strategy != CrossOver.Uniform) {
			int p = random.nextInt(max);
			int q = strategy == CrossOver.SinglePoint ? max : random.nextInt(max);
			Chromosome.swap(a.genom, b.genom, Math.min(p, q), Math.max(p, q));
		} else {
			for (int i = 0; i < max; i++) {
				if (random.nextBoolean()) {
					Chromosome.swap(a.genom, b.genom, i, i);
				}
			}
		}
		a.resetFitness();
		b.resetFitness();

		Collection<Individual> collection = new HashSet<>();
		Collections.addAll(collection, a, b);
		return collection;
	}

	public void mutate(Random random, double mutationRate) {
		boolean changed = genom.mutate(random, mutationRate);
		if (changed) {
			resetFitness();
		}
	}

	@Override
	public int compareTo(Individual o) {
		if (hasFitness() && o.hasFitness()) {
			return Double.compare(fitness, o.fitness);
		}
		throw new IllegalStateException("invalid fitness value");
	}

	@Override
	public Individual clone() {
		try {
			Individual individual = (Individual) super.clone();
			individual.genom = genom.clone();
			return individual;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	@Override
	public int hashCode() {
		return genom.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append("genotype : ").append(genom.toString()).append("\n")
		.append("phenotype: ").append(Arrays.deepToString(getPhenoType())).append("\n")
		.append("fitness  : ").append(fitness)
		.toString();
	}
}
