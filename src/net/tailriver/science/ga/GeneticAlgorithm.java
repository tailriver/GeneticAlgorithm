package net.tailriver.science.ga;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public interface GeneticAlgorithm {
	Chromosome inflateChromosome();

	Random getRandom();

	void calculateFitness(Collection<Individual> collection);

	List<Individual> selectRoulette(int n, List<Individual> candidates);
}
