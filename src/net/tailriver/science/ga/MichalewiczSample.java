package net.tailriver.science.ga;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Solves Michalewicz sample.
 * <h3>Bibliography</h3>
 * <ul>
 *  <li>M. Sakawa and M. Tanaka: <i>"Genetic Algorithms" (in Japanese)</i>,
 *   Asakura Publishing, ISBN 978-4-254-20990-7 (1995)</li>
 *  <li>Z. Michalewicz: <i>"Genetic Algorithms + Data Structures + Evolution Programs"</i>,
 *   Springer-Verlag (1992). (above book was cited from this)</li>
 * </ul>
 * @author tailriver
 */
public class MichalewiczSample implements GeneticAlgorithm {
	private Random random = new Random();

	@Override
	public Chromosome inflateChromosome() {
		return new Chromosome.Creator()
		.append(22, 1)
		.inflate();
	}

	@Override
	public Random getRandom() {
		return random;
	}

	@Override
	public void calculateFitness(Collection<Individual> collection) {
		for (Individual individual : collection) {
			double x = individual.getScaledGenom(0, -1, 2);
			double fitness = x * Math.sin(10d * Math.PI * x) + 2;
			individual.setPhenoType(0, Double.valueOf(x));
			individual.setFitness(fitness);
		}
	}

	@Override
	public List<Individual> selectRoulette(int n, List<Individual> candidates) {
		throw new UnsupportedOperationException();
	}

	public static void main(String... args) {
		Individual best = null;
		Population pop = new Population(new MichalewiczSample(), 50);
		pop.setReverseOrder(true);
		for (int generation = 0; generation < 10000; generation++) {
			pop.cross(CrossOver.SinglePoint, 0.25, 1);
			pop.mutate(0.1);

			Individual generationTop = pop.getRankAt(0);
			if (best == null || generationTop.compareTo(best) > 0) {
				best = generationTop;
				System.out.println("[" + generation + "] " + Integer.toHexString(best.hashCode()));
				System.out.println(best);
				System.out.println();
			}

			pop.selectElite(5);
			pop.selectTournament(45, 2);
			pop.changeGeneration();
		}
	}
}
