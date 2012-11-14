package net.tailriver.science.ga;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public interface GeneticAlgorithmPlan<T extends Individual> {
	/**
	 * Called from constructor of {@link GeneticAlgorithm}.
	 * 
	 * @return inflated {@link Individual}.
	 * @see GenoType.Creator
	 * @see Individual#Individual(GenoType)
	 * @see Individual#randomize(Random)
	 */
	T inflateIndividual();

	/**
	 * Called from some methods in {@link GeneticAlgorithm}.
	 * 
	 * Supplied random seed are used selection, crossover and mutation
	 * operations. You do not return new {@link Random} instance for each
	 * calling because it is called very frequently. You should return the
	 * reference of Random object stored in field variable.
	 * 
	 * @return a random object.
	 */
	Random getRandom();

	/**
	 * Called from some methods in {@link GeneticAlgorithm}.
	 * 
	 * <ol>
	 * <li>Get the values of {@link GenoType}:
	 * <ul>
	 * <li>{@link Individual#getGenoTypeBitSet(int)}</li>
	 * <li>{@link Individual#getGenoTypeBoolean(int)}</li>
	 * <li>{@link Individual#getGenoTypeDouble(int)}</li>
	 * <li>{@link Individual#getGenoTypeDouble(int, double, double)}</li>
	 * <li>{@link Individual#getGenoTypeLong(int)}</li>
	 * </ul>
	 * </li>
	 * <li>Calculate pheno type value from above.</li>
	 * <li>(optional) Set pheno type by
	 * {@link Individual#setPhenoType(int, Object)}.</li>
	 * <li>Calculate fitness.</li>
	 * <li>Save fitness value by {@link Individual#setFitness(double)}.</li>
	 * </ol>
	 * 
	 * If you don't save fitness for some of Individuals,
	 * {@link IllegalStateException} will throw later.
	 * 
	 * @param population
	 *            Collection of {@link Individual} to calculate fitness.
	 */
	void calculateFitness(Collection<T> population);

	/**
	 * Called from {@link GeneticAlgorithm#cross(double, double)}.
	 * 
	 * <p>
	 * You select one of method to specify a crossover strategy of the plan and
	 * describe, that's all. If you want to use other strategy, you may need to
	 * use {@link GenoType#swap(GenoType, GenoType, Mask)} directly or extend
	 * {@link GenoType} to modify geno-type.
	 * </p>
	 * 
	 * <p>
	 * Parents are saved in above method. So, argument {@code x} and {@code y}
	 * are mutable objects. If you need to check the correctness of these
	 * strategy (or yet another one), the easiest way is to use
	 * {@link Individual#print()} at before and after crossover method.
	 * </p>
	 * 
	 * @param x
	 *            parent and child (mutable).
	 * @param y
	 *            parent and child (mutable).
	 * @see Individual#crossOverSinglePoint(Individual, Individual, Random)
	 * @see Individual#crossOverTwoPoint(Individual, Individual, Random)
	 * @see Individual#crossOverUniform(Individual, Individual, Random)
	 */
	void applyCrossOver(T x, T y);

	/**
	 * Called from {@link GeneticAlgorithm#select()}.
	 * 
	 * If you return different size of Individuals,
	 * {@link IllegalStateException} will throw later.
	 * 
	 * @param population
	 * @return list of winners.
	 * @see GeneticAlgorithm#selectElite(List, int)
	 * @see GeneticAlgorithm#selectTournament(List, Random, int, int)
	 */
	List<T> applySelection(List<T> population);
}
