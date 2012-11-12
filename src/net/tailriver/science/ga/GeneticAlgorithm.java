package net.tailriver.science.ga;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {
	private final GeneticAlgorithmPlan plan;
	private final List<Individual> population;
	private final int size;
	private Comparator<? super Individual> comparator;
	private boolean sorted;

	public GeneticAlgorithm(GeneticAlgorithmPlan plan, int size) {
		this.plan = plan;
		this.size = size;
		population = new ArrayList<>();

		while (population.size() < size) {
			Individual individual = plan.inflateIndividual();
			individual.activateChromosomeWatcher();
			population.add(individual);
		}
	}

	public void setComparator(Comparator<? super Individual> comparator) {
		this.comparator = comparator;
		sorted = false;
	}

	public void setReverseOrder(boolean reverseOrder) {
		setComparator(reverseOrder ? Collections.reverseOrder() : null);
	}

	/**
	 * Returns specified rank in population.
	 * 
	 * This method uses sort function internally. You may need to be careful
	 * with the position to call from performance point of view. The best
	 * position will be before {@link #select()}.
	 * 
	 * @param rank
	 *            BE CAREFUL. It starts from <em>ONE</em>.
	 * @return copy of {@link Individual} at specified {@code rank}.
	 * @throws IndexOutOfBoundsException
	 *             if {@code rank} is less than 1 or greater than number of
	 *             population.
	 */
	public Individual getRankAt(int rank) {
		sort();
		return new Individual(population.get(rank - 1));
	}

	/**
	 * 
	 * @param crossoverRate
	 * @param generationGap
	 * @throws IllegalArgumentException
	 *             if arguments contain NaN.
	 * @throws IndexOutOfBoundsException
	 *             if arguments are less than 0 or greater than 1.
	 */
	public void cross(double crossoverRate, double generationGap) {
		if (Double.isNaN(crossoverRate))
			throw new IllegalArgumentException("crossoverRate is NaN");
		if (crossoverRate < 0)
			throw new IndexOutOfBoundsException("crossoverRate < 0: "
					+ crossoverRate);
		if (crossoverRate > 1)
			throw new IndexOutOfBoundsException("crossoverRate > 1: "
					+ crossoverRate);
		if (Double.isNaN(generationGap))
			throw new IllegalArgumentException("generationGap is NaN");
		if (generationGap < 0)
			throw new IndexOutOfBoundsException("generationGap < 0: "
					+ generationGap);
		if (generationGap > 1)
			throw new IndexOutOfBoundsException("generationGap > 1: "
					+ generationGap);

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

	/**
	 * 
	 * @param mutationRate
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 * @throws IndexOutOfBoundsException
	 */
	public void mutate(double mutationRate) {
		Random random = plan.getRandom();
		for (Individual i : population) {
			i.chromosome.mutate(random, mutationRate);
		}
	}

	public void select() {
		sort();
		Collection<Individual> next = new HashSet<>();
		for (Individual w : plan.applySelection(population)) {
			next.add(next.contains(w) ? new Individual(w) : w);
		}
		if (next.size() != size) {
			throw new IllegalStateException();
		}

		for (Individual i : population) {
			i.deactivateChromosomeWatcher();
		}
		population.clear();

		for (Individual i : next) {
			i.activateChromosomeWatcher();
			population.add(i);
		}
		next.clear();
		sorted = false;
	}

	/**
	 * @throws IllegalStateException
	 *             if fitness of individual is still invalid after
	 *             {@link GeneticAlgorithmPlan#calculateFitness(Collection)}
	 *             called.
	 */
	private void sort() {
		if (sorted) {
			return;
		}

		Collection<Individual> todo = new HashSet<>();
		for (Individual individual : population) {
			if (!individual.hasFitness()) {
				todo.add(individual);
			}
		}
		plan.calculateFitness(todo);
		for (Individual individual : todo) {
			if (!individual.hasFitness())
				throw new IllegalStateException("fitness is NaN: "
						+ individual.toString());
		}

		Collections.sort(population, comparator);
		sorted = true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Individual i : population) {
			sb.append(i).append('\n');
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param random
	 * @throws NullPointerException
	 *             if arguments contain null.
	 * @throws IllegalArgumentException
	 *             if {@link Chromosome}s of {@link Individual}s point same
	 *             address, or they are incompatible.
	 */
	public final static void crossOverSinglePoint(Individual x, Individual y,
			Random random) {
		int max = x.chromosome.bitLength;
		int p = random.nextInt(max);
		BitSet mask = new BitSet(max);
		mask.set(p, max, true);
		Chromosome.swap(x.chromosome, y.chromosome, mask);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param random
	 * @throws NullPointerException
	 *             if arguments contain null.
	 * @throws IllegalArgumentException
	 *             if {@link Chromosome}s of {@link Individual}s point same
	 *             address, or they are incompatible.
	 */
	public final static void crossOverTwoPoint(Individual x, Individual y,
			Random random) {
		int max = x.chromosome.bitLength;
		int p = random.nextInt(max);
		int q = random.nextInt(max);
		BitSet mask = new BitSet(max);
		mask.set(Math.min(p, q), Math.max(p, q), true);
		Chromosome.swap(x.chromosome, y.chromosome, mask);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param random
	 * @throws NullPointerException
	 *             if arguments contain null.
	 * @throws IllegalArgumentException
	 *             if {@link Chromosome}s of {@link Individual}s point same
	 *             address, or they are incompatible.
	 */
	public static final void crossOverUniform(Individual x, Individual y,
			Random random) {
		int max = x.chromosome.bitLength;
		BitSet mask = new BitSet(max);
		for (int i = 0; i < max; i++) {
			mask.set(i, random.nextBoolean());
		}
		Chromosome.swap(x.chromosome, y.chromosome, mask);
	}

	public static final List<Individual> selectElite(
			List<Individual> candidates, int n) {
		if (n < 1 || n > candidates.size()) {
			throw new IllegalArgumentException();
		}
		return candidates.subList(0, n);
	}

	public static final List<Individual> selectTournament(
			List<Individual> candidates, Random random, int n, int k) {
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
}
