package net.tailriver.science.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm<T extends Individual> {
	protected final GeneticAlgorithmPlan<T> plan;
	protected final T[] population;
	private Comparator<? super T> comparator;
	private boolean sorted;

	public GeneticAlgorithm(GeneticAlgorithmPlan<T> plan, int size) {
		this.plan = plan;
		population = makePopulationArray(size);

		for (int i = 0; i < size; i++) {
			T individual = plan.inflateIndividual();
			individual.activateWatcher();
			population[i] = individual;
		}
	}

	/**
	 * Sets a comparator to determine the order of the individuals (by value of
	 * fitness).
	 * 
	 * @param comparator
	 *            the comparator to determine the order of the individuals. A
	 *            {@code null} value indicates that the elements'
	 *            {@linkplain Comparable natural ordering} should be used.
	 */
	public void setComparator(Comparator<? super T> comparator) {
		this.comparator = comparator;
		sorted = false;
	}

	/**
	 * Sets a simple comparator to determine the order of the individuals. A
	 * <code>true</code> value indicates that the elements' reverse order of
	 * natural ordering should be used. A <code>false</code> values indicates
	 * that the elements' natural ordering should be used.
	 * 
	 * @param reverseOrder
	 * @see #setComparator(Comparator)
	 */
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
	 * @throws ArrayIndexOutOfBoundsException
	 *             if {@code rank} is less than 1 or greater than number of
	 *             population.
	 */
	public T getRankAt(int rank) {
		sort();
		return makeClone(population[rank - 1]);
	}

	/**
	 * 
	 * @param crossoverRate
	 * @param generationGap
	 * @throws IllegalArgumentException
	 *             if arguments are NaN, less than 0 or greater than 1.
	 * @see GeneticAlgorithmPlan#applyCrossOver(Individual, Individual)
	 */
	public void cross(double crossoverRate, double generationGap) {
		probabilityCheck("crossover rate", crossoverRate);
		probabilityCheck("generation gap", generationGap);

		Random random = plan.getRandom();
		int size = population.length;
		List<T> before = Arrays.asList(population);
		List<T> after = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			T x = before.get(i);
			T y = before.get(random.nextInt(size));
			if (random.nextDouble() < crossoverRate) {
				x = makeClone(x);
				y = makeClone(y);
				plan.applyCrossOver(x, y);
			}
			after.add(x);
			after.add(y);
		}

		Collections.shuffle(before, random);
		Collections.shuffle(after, random);
		int ng = (int) (size * generationGap);
		for (int i = 0; i < ng; i++) {
			population[i] = after.get(i);
		}
		for (int i = ng; i < size; i++) {
			population[i] = before.get(i);
		}
		sorted = false;
	}

	/**
	 * 
	 * @param mutationRate
	 * @throws NullPointerException
	 *             if {@link GeneticAlgorithmPlan#getRandom()} returns null.
	 * @throws IllegalArgumentException
	 *             if {@code mutationRate} is NaN, less than 0 or greater than
	 *             1.
	 */
	public void mutate(double mutationRate) {
		Random random = plan.getRandom();
		for (T i : population)
			i.mutate(random, mutationRate);
	}

	/**
	 * @see GeneticAlgorithmPlan#applySelection(List)
	 */
	public void select() {
		sort();
		List<T> next = new ArrayList<>();
		List<T> current = Arrays.asList(population);
		for (T w : plan.applySelection(current)) {
			T winner = next.contains(w) ? makeClone(w) : w;
			next.add(winner);
		}
		int size = population.length;
		if (next.size() != size)
			throw new IllegalStateException("incosistent size: expected "
					+ size + ", got " + next.size());

		for (int i = 0; i < size; i++) {
			population[i].deactivateWatcher();
			population[i] = next.get(i);
			population[i].activateWatcher();
		}
		sorted = false;
	}

	/**
	 * @throws IllegalStateException
	 *             if fitness of individual is still invalid after
	 *             {@link GeneticAlgorithmPlan#calculateFitness(Collection)}
	 *             called.
	 */
	protected void sort() {
		if (sorted)
			return;

		plan.calculateFitness(Arrays.asList(population));
		Arrays.sort(population, comparator);
		sorted = true;
	}

	protected boolean isSorted() {
		return sorted;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (T i : population) {
			sb.append(i).append('\n');
		}
		return sb.toString();
	}

	/**
	 * Applies elite selection.
	 * 
	 * @param candidates
	 *            it should be same as the argument of
	 *            {@link GeneticAlgorithmPlan#applySelection(List)}.
	 * @param n
	 *            the size of the result.
	 * @return a list of selected individuals. It returns empty list if
	 *         specified size is zero.
	 * @throws NullPointerException
	 *             if specified candidates is <code>null</code>.
	 * @throws IndexOutOfBoundsException
	 *             if specified number is negative or exceeds the size of
	 *             candidates.
	 */
	public static final <T extends Individual> List<T> selectElite(
			List<T> candidates, int n) {
		return candidates.subList(0, n);
	}

	/**
	 * Applies tournament selection.
	 * 
	 * @param candidates
	 *            it should be same as the argument of
	 *            {@link GeneticAlgorithmPlan#applySelection(List)}.
	 * @param random
	 *            a random seed using the method. You can use
	 *            {@link GeneticAlgorithmPlan#getRandom()}.
	 * @param n
	 *            the size of the result.
	 * @param k
	 *            tournament order (more than 0). if it is 1, the selection acts
	 *            as a completely random selection.
	 * @return a list of selected individuals. It returns empty list if
	 *         specified size is zero.
	 * @throws NullPointerException
	 *             if specified candidates or random seed is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if specified candidates is empty, specified size is negative,
	 *             or specified order is less than 1.
	 */
	public static final <T extends Individual> List<T> selectTournament(
			List<T> candidates, Random random, int n, int k) {
		if (n < 0)
			throw new IllegalArgumentException("n < 0: " + n);
		if (k < 1)
			throw new IllegalArgumentException("k < 1: " + k);

		T[] winner = makePopulationArray(n);
		for (int i = 0, size = candidates.size(); i < n; i++) {
			int m = random.nextInt(size);
			for (int j = 1; j < k; j++) {
				m = Math.min(m, random.nextInt(size));
			}
			winner[i] = candidates.get(m);
		}
		return Arrays.asList(winner);
	}

	protected static final void probabilityCheck(CharSequence name,
			double probability) {
		if (!(probability >= 0 && probability <= 1))
			throw new IllegalArgumentException(name + " must be [0,1]: "
					+ probability);
	}

	@SuppressWarnings("unchecked")
	private static final <T extends Individual> T makeClone(T o) {
		return (T) o.clone();
	}

	@SuppressWarnings("unchecked")
	private static final <T extends Individual> T[] makePopulationArray(int size) {
		return (T[]) new Individual[size];
	}
}
