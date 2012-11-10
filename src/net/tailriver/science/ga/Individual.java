package net.tailriver.science.ga;

public class Individual implements Comparable<Individual> {
	public final Chromosome chromosome;
	private double fitness;

	public Individual(Chromosome chromosome) {
		this.chromosome = chromosome;
		clearFitness();
	}

	public Individual(Individual original) {
		chromosome = new Chromosome(original.chromosome);
		fitness    = original.fitness;
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

	public final void clearFitness() {
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
		return chromosome.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append(Integer.toHexString(chromosome.hashCode())).append("#")
		.append(fitness)
		.toString();
	}
}
