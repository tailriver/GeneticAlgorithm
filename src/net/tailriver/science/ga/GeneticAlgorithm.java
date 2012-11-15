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

	@SuppressWarnings("unchecked")
	public GeneticAlgorithm(GeneticAlgorithmPlan<T> plan, int size) {
		this.plan = plan;
		population = (T[]) new Individual[size];

		for (int i = 0; i < size; i++) {
			T individual = plan.inflateIndividual();
			individual.activateWatcher();
			population[i] = individual;
		}
	}

	public void setComparator(Comparator<? super T> comparator) {
		this.comparator = comparator;
		sorted = false;
	}

	/**
	 * 
	 * @param reverseOrder
	 *            if <code>true</code>
	 * 
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
	@SuppressWarnings("unchecked")
	public T getRankAt(int rank) {
		sort();
		return (T) population[rank - 1].clone();
	}

	/**
	 * 
	 * @param crossoverRate
	 * @param generationGap
	 * @throws IllegalArgumentException
	 *             if arguments are NaN, less than 0 or greater than 1.
	 */
	@SuppressWarnings("unchecked")
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
				x = (T) x.clone();
				y = (T) y.clone();
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
		for (Individual i : population)
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
			@SuppressWarnings("unchecked")
			T winner = next.contains(w) ? (T) w.clone() : w;
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
		for (T individual : population) {
			if (!individual.hasFitness())
				throw new IllegalStateException("fitness is NaN: "
						+ individual.toString());
		}

		Arrays.sort(population, comparator);
		sorted = true;
	}

	protected boolean isSorted() {
		return sorted;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Individual i : population) {
			sb.append(i).append('\n');
		}
		return sb.toString();
	}

	public static final <T extends Individual> List<T> selectElite(
			List<T> candidates, int n) {
		if (n < 1 || n > candidates.size()) {
			throw new IllegalArgumentException();
		}
		return candidates.subList(0, n);
	}

	public static final <T extends Individual> List<T> selectTournament(
			List<T> candidates, Random random, int n, int k) {
		int size = candidates.size();
		if (n < 1 || k < 2 || n > size) {
			throw new IllegalArgumentException();
		}

		List<T> winner = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			int m = random.nextInt(size);
			for (int j = 1; j < k; j++) {
				m = Math.min(m, random.nextInt(size));
			}
			winner.add(candidates.get(m));
		}
		return winner;
	}

	/* package */static final void probabilityCheck(CharSequence name,
			double probability) {
		if (!(probability >= 0 && probability <= 1))
			throw new IllegalArgumentException(name + " must be [0,1]: "
					+ probability);
	}
}
