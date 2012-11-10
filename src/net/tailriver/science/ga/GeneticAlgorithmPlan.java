package net.tailriver.science.ga;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public interface GeneticAlgorithmPlan {
	Chromosome inflateChromosome();
	Individual inflateIndividual(Chromosome chromosome);

	Random getRandom();

	double calculateFitness(final Chromosome chromosomes);
	void calculateFitness(Collection<Individual> population);

	void applyCrossOver(Individual x, Individual y);

	List<Individual> applySelection(List<Individual> population);
}
