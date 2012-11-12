package net.tailriver.science.ga;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Solves Michalewicz sample.
 * 
 * <h3>Bibliography</h3>
 * <ul>
 * <li>M. Sakawa and M. Tanaka: <i>"Genetic Algorithms" (in Japanese)</i>,
 * Asakura Publishing, ISBN 978-4-254-20990-7 (1995)</li>
 * <li>Z. Michalewicz:
 * <i>"Genetic Algorithms + Data Structures + Evolution Programs"</i>,
 * Springer-Verlag (1992). (above book was cited from this)</li>
 * </ul>
 * 
 * @author tailriver
 */
public class MichalewiczSample implements GeneticAlgorithmPlan {
	private Random random = new Random();

	@Override
	public Individual inflateIndividual() {
		Chromosome chromosome = new Chromosome.Creator().append(22).inflate();
		chromosome.randomize(random);
		return new Individual(chromosome);
	}

	@Override
	public Random getRandom() {
		return random;
	}

	@Override
	public void calculateFitness(Collection<Individual> population) {
		for (Individual individual : population) {
			final Chromosome c = individual.chromosome;
			double x = c.getScaled(0, -1, 2);
			c.phenoType[0] = x;
			double fitness = x * Math.sin(10d * Math.PI * x) + 2;
			individual.setFitness(fitness);
		}
	}

	@Override
	public void applyCrossOver(Individual x, Individual y) {
		GeneticAlgorithm.crossOverTwoPoint(x, y, random);
	}

	@Override
	public List<Individual> applySelection(List<Individual> candidates) {
		List<Individual> winner = new ArrayList<>();
		winner.addAll(GeneticAlgorithm.selectElite(candidates, 5));
		winner.addAll(GeneticAlgorithm.selectTournament(candidates, random, 45,
				2));
		return winner;
	}

	public static void main(String... args) {
		Individual best = null;
		GeneticAlgorithm ga = new GeneticAlgorithm(new MichalewiczSample(), 50);
		ga.setReverseOrder(true);
		for (int generation = 0; generation < 10000; generation++) {
			ga.cross(0.25, 1);
			ga.mutate(0.1);

			Individual generationTop = ga.getRankAt(1);
			if (best == null || generationTop.compareTo(best) > 0) {
				best = generationTop;
				System.out.println(">> " + generation);
				best.print();
				System.out.println();
			}

			ga.select();
		}
	}
}
