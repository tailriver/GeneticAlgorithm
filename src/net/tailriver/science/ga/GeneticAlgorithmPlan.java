package net.tailriver.science.ga;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public interface GeneticAlgorithmPlan {
	/**
	 * Called from constructor of {@link GeneticAlgorithm}.
	 * 
	 * @return inflated {@link Individual}.
	 * @see Chromosome.Creator
	 * @see Chromosome#randomize(Random)
	 * @see Individual#Individual(Chromosome)
	 */
	Individual inflateIndividual();

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
	 * <li>Get {@link Chromosome}: {@link Individual#genoType}.
	 * <li> {@link Chromosome#getLong(int)} or something like that to fetch
	 * geno-type value.</li>
	 * <li>(optional) Set pheno-type by
	 * {@link Individual#setPhenoType(int, Object)}.</li>
	 * <li>Calculate fitness from above geno-type or pheno-type.</li>
	 * <li>Save fitness value. Use {@link Individual#setFitness(double)}.</li>
	 * </ol>
	 * 
	 * If you don't save fitness for some of Individuals,
	 * {@link IllegalStateException} will throw later.
	 * 
	 * @param population
	 *            Collection of {@link Individual} to calculate fitness.
	 */
	void calculateFitness(Collection<Individual> population);

	/**
	 * Called from {@link GeneticAlgorithm#cross(double, double)}.
	 * 
	 * <p>
	 * You select one of method to specify a crossover strategy of the plan and
	 * describe, that's all. If you want to use other strategy, you may need to
	 * use {@link Chromosome#swap(Chromosome, Chromosome, java.util.BitSet)}
	 * directly or extend {@link Chromosome} to modify geno-type.
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
	 * @see GeneticAlgorithm#crossOverSinglePoint(Individual, Individual,
	 *      Random)
	 * @see GeneticAlgorithm#crossOverTwoPoint(Individual, Individual, Random)
	 * @see GeneticAlgorithm#crossOverUniform(Individual, Individual, Random)
	 */
	void applyCrossOver(Individual x, Individual y);

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
	List<Individual> applySelection(List<Individual> population);
}
