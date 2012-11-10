package net.tailriver.science.ga;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Population {
	private GeneticAlgorithm ga;
	private List<Individual> population;
	private Collection<Individual> nextGeneration;
	private int size;
	private boolean reverseOrder;
	private boolean sorted;

	public Population(GeneticAlgorithm ga, int size) {
		this.ga        = ga;
		this.size      = size;
		population     = new ArrayList<>();
		nextGeneration = new HashSet<>();

		Random random       = ga.getRandom();
		Chromosome original = ga.inflateChromosome();
		while (population.size() < size) {
			Chromosome clone = original.clone();
			clone.randomize(random);
			Individual one = new Individual(clone);
			population.add(one);
		}
	}

	public void setReverseOrder(boolean reverseOrder) {
		if (this.reverseOrder != reverseOrder) {
			this.reverseOrder = reverseOrder;
			sorted = false;
		}
	}

	public Individual getRankAt(int rank) {
		sort();
		return population.get(rank).clone();
	}

	public void selectElite(int n) {
		if (n < 1 || n > size) {
			throw new IllegalArgumentException();
		}

		sort();
		for (Individual winner : population.subList(0, n)) {
			addNextGeneration(winner);
		}
	}

	public void selectRoulette(int n) {
		if (n < 1 || n > size) {
			throw new IllegalArgumentException();
		}

		sort();
		List<Individual> result = ga.selectRoulette(n, population);
		if (result.size() != n) {
			throw new IllegalStateException();
		}
		for (Individual winner : result) {
			addNextGeneration(winner);
		}
	}

	public void selectTournament(int n, int k) {
		if (n < 1 || k < 2 || n > size) {
			throw new IllegalArgumentException();
		}

		sort();
		Random random = ga.getRandom();
		int size = population.size();
		for (int i = 0; i < n; i++) {
			int m = random.nextInt(size);
			for (int j = 1; j < k; j++) {
				m = Math.min(m, random.nextInt(size));
			}
			addNextGeneration(population.get(m));
		}
	}

	private void addNextGeneration(Individual winner) {
		if (!nextGeneration.contains(winner)) {
			nextGeneration.add(winner);
		} else {
			nextGeneration.add(winner.clone());
		}
	}

	public void changeGeneration() {
		if (nextGeneration.size() != size) {
			throw new IllegalStateException(
					"wrong pop size: expect " + size + 
					", got " + nextGeneration.size());
		}
		population.clear();
		population.addAll(nextGeneration);
		nextGeneration.clear();
		sorted = false;
	}

	public void sort() {
		if (sorted) {
			return;
		}

		Collection<Individual> todo = new HashSet<>();
		for (Individual individual : population) {
			if (!individual.hasFitness()) {
				todo.add(individual);
			}
		}
		ga.calculateFitness(todo);

		if (reverseOrder) {
			Collections.sort(population, Collections.reverseOrder());
		} else {
			Collections.sort(population);
		}
		sorted = true;
	}

	public void cross(CrossOver strategy, double crossoverRate, double generationGap) {		
		if (Double.isNaN(crossoverRate) || crossoverRate < 0 || crossoverRate > 1) {
			throw new IllegalArgumentException("out of range: " + crossoverRate);
		}
		if (Double.isNaN(generationGap) || generationGap < 0 || generationGap > 1) {
			throw new IllegalArgumentException("out of range: " + generationGap);
		}

		Random random = ga.getRandom();
		List<Individual> stack = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			Individual x = population.get(i);
			Individual y = population.get(random.nextInt(size));
			if (random.nextDouble() < crossoverRate) {
				Collection<Individual> children = Individual.cross(x, y, random, strategy);
				stack.addAll(children);
			} else {
				stack.add(x);
				stack.add(y);
			}
		}

		int ng = (int) (size * generationGap);
		Collections.shuffle(population, random);
		Collections.shuffle(stack, random);
		population.subList(0, ng).clear();
		population.addAll(stack.subList(0, ng));
		sorted = false;
	}

	public void mutate(double mutationRate) {
		Random random = ga.getRandom();
		for (Individual i : population) {
			i.mutate(random, mutationRate);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb =  new StringBuilder();
		for (Individual i : population) {
			sb.append(i).append('\n');
		}
		return sb.toString();
	}
}
