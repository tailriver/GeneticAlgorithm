package net.tailriver.science.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {
	private final GeneticAlgorithmPlan plan;
	private final List<Individual> population;
	private final int size;
	private boolean reverseOrder;
	private boolean sorted;
	private boolean calculateFitnessOneByOne;

	public GeneticAlgorithm(GeneticAlgorithmPlan plan, int size) {
		this.plan  = plan;
		this.size  = size;
		population = new ArrayList<>();

		Chromosome original = plan.inflateChromosome();
		while (population.size() < size) {
			Chromosome clone      = new Chromosome(original);
			Individual individual = plan.inflateIndividual(clone);
			individual.activateChromosomeWatcher();
			population.add(individual);
		}
	}

	public void setCalculateFitnessOneByOne(boolean oneByOne) {
		calculateFitnessOneByOne = oneByOne;
	}

	public void setReverseOrder(boolean reverseOrder) {
		if (this.reverseOrder != reverseOrder) {
			this.reverseOrder = reverseOrder;
			sorted = false;
		}
	}

	public Individual getRankAt(int rank) {
		sort();
		return new Individual(population.get(rank));
	}

	public void cross(double crossoverRate, double generationGap) {
		if (Double.isNaN(crossoverRate) || crossoverRate < 0 || crossoverRate > 1) {
			throw new IllegalArgumentException("out of range: " + crossoverRate);
		}
		if (Double.isNaN(generationGap) || generationGap < 0 || generationGap > 1) {
			throw new IllegalArgumentException("out of range: " + generationGap);
		}
	
		Random random = plan.getRandom();
		List<Individual> stack = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			Individual x = population.get(i);
			Individual y = population.get(random.nextInt(size));
			if (random.nextDouble() < crossoverRate) {
				x = new Individual(x);
				y = new Individual(y);
				plan.applyCrossOver(x, y);
			}
			stack.add(x);
			stack.add(y);
		}

		int ng = (int) (size * generationGap);
		Collections.shuffle(population, random);
		Collections.shuffle(stack, random);
		population.subList(0, ng).clear();
		population.addAll(stack.subList(0, ng));
		sorted = false;
		
	}

	public void mutate(double mutationRate) {
		Random random = plan.getRandom();
		for (Individual i : population) {
			i.chromosome.mutate(random, mutationRate);
		}
	}

	public void select() {
		sort();
		Collection<Individual> nextGeneration = new HashSet<>();
		for (Individual w : plan.applySelection(population)) {
			nextGeneration.add( nextGeneration.contains(w) ? new Individual(w) : w);
		}
		if (nextGeneration.size() != size) {
			throw new IllegalStateException();
		}

		for (Individual i : population) {
			i.deactivateChromosomeWatcher();
		}
		population.clear();

		for (Individual i : nextGeneration) {
			i.activateChromosomeWatcher();
			population.add(i);
		}
		nextGeneration.clear();
		sorted = false;
	}

	private void sort() {
		if (sorted) {
			return;
		}

		if (calculateFitnessOneByOne) {
			for (Individual individual : population) {
				if (!individual.hasFitness()) {
					double fitness = plan.calculateFitness(individual.chromosome);
					individual.setFitness(fitness);
				}
			}
		} else {
			Collection<Individual> todo = new HashSet<>();
			for (Individual individual : population) {
				if (!individual.hasFitness()) {
					todo.add(individual);
				}
			}
			plan.calculateFitness(todo);
		}

		if (reverseOrder) {
			Collections.sort(population, Collections.reverseOrder());
		} else {
			Collections.sort(population);
		}
		sorted = true;
	}

	@Override
	public String toString() {
		StringBuilder sb =  new StringBuilder();
		for (Individual i : population) {
			sb.append(i).append('\n');
		}
		return sb.toString();
	}

	public final static void crossOverSinglePoint(Individual x, Individual y, Random random) {
		int max = x.chromosome.bitSizeTotal();
		int p = random.nextInt(max);
		Chromosome.swap(x.chromosome, y.chromosome, p, max);
	}

	public final static void crossOverTwoPoint(Individual x, Individual y, Random random) {
		int max = x.chromosome.bitSizeTotal();
		int p = random.nextInt(max);
		int q = random.nextInt(max);
		Chromosome.swap(x.chromosome, y.chromosome, Math.min(p, q), Math.max(p, q));
	}

	public static final void crossOverUniform(Individual x, Individual y, Random random) {
		int max = x.chromosome.bitSizeTotal();
		for (int i = 0; i < max; i++) {
			if (random.nextBoolean()) {
				Chromosome.swap(x.chromosome, y.chromosome, i, i);
			}
		}
	}

	public static final List<Individual> selectElite(List<Individual> candidates, int n) {
		if (n < 1 || n > candidates.size()) {
			throw new IllegalArgumentException();
		}
		return candidates.subList(0, n);
	}

	public static final List<Individual> selectTournament(List<Individual> candidates,
			Random random, int n, int k) {
		int size = candidates.size();
		if (n < 1 || k < 2 || n > size) {
			throw new IllegalArgumentException();
		}
	
		List<Individual> winner = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			int m = random.nextInt(size);
			for (int j = 1; j < k; j++) {
				m = Math.min(m, random.nextInt(size));
			}
			winner.add(candidates.get(m));
		}
		return winner;
	}

	public static void printIndividual(Individual individual) {
		System.out.println(individual);
		System.out.println(individual.chromosome);
		System.out.println(Arrays.deepToString(individual.chromosome.getPhenoType()));		
	}
}
