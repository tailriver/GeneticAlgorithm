package net.tailriver.science.ga;

import java.util.Arrays;

public class Individual implements ChromosomeWatcher, Comparable<Individual> {
	public final GenoType genoType;
	private final Object[] phenoType;
	private double fitness;

	public Individual(GenoType genoType) {
		this.genoType = genoType;
		phenoType = new Object[genoType.length];
		fitness = Double.NaN;
	}

	public Individual(Individual original) {
		genoType = new GenoType(original.genoType);
		phenoType = original.phenoType.clone();
		fitness = original.fitness;
	}

	public Object getPhenoType(int i) {
		return phenoType[i];
	}

	public void setPhenoType(int i, Object phenoType) {
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

	public void activateChromosomeWatcher() {
		genoType.setOnChromosomeChanged(this);
	}

	public void deactivateChromosomeWatcher() {
		genoType.setOnChromosomeChanged(null);
	}

	@Override
	public void onChromosomeChanged() {
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
	public String toString() {
		return new StringBuilder()
				.append(Integer.toHexString(genoType.hashCode())).append("#")
				.append(fitness).toString();
	}

	public void print() {
		System.out.println(this);
		System.out.println(genoType);
		System.out.println(Arrays.deepToString(phenoType));
	}
}
