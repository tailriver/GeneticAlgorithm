package net.tailriver.science.ga;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public interface GeneticAlgorithmPlan {
	Individual inflateIndividual();

	Random getRandom();

	void calculateFitness(Collection<Individual> population);

	void applyCrossOver(Individual x, Individual y);

	List<Individual> applySelection(List<Individual> population);
}
