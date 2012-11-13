package net.tailriver.science.ga.demo;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.tailriver.science.ga.Chromosome;
import net.tailriver.science.ga.GeneticAlgorithm;
import net.tailriver.science.ga.GeneticAlgorithmPlan;
import net.tailriver.science.ga.Individual;

/**
 * Solves knapsack problem.
 * 
 * When maximum weight is 200, the good results seems to be ...
 * <ol>
 * <li>856: <tt>10111100111010010110111111010010011111110110111110</tt></li>
 * <li>854: <tt>01111101100110010001001011110111101001101101111111</tt></li>
 * <li>841: <tt>01111101100110011011001011110101101001101101111101</tt></li>
 * <li>838: <tt>10111101100110011001001110010111111001111100111111</tt></li>
 * <li>833: <tt>00111101100111011101100011101111101001111100101100</tt></li>
 * </ol>
 * 
 * <h3>Reference</h3>
 * <ul>
 * <li><a
 * href="http://ipr20.cs.ehime-u.ac.jp/column/ga/chapter4.html">http://ipr20
 * .cs.ehime-u.ac.jp/column/ga/chapter4.html</a></li>
 * </ul>
 * 
 * @author tailriver
 */
public class Knapsack implements GeneticAlgorithmPlan {
	private Random random = new Random();

	private static final int[] weights = new int[] {
	/**/2, 10, 7, 2, 4, 9, 10, 7, 8, 5,
	/**/3, 10, 9, 8, 8, 5, 7, 3, 9, 7,
	/**/2, 10, 7, 9, 7, 2, 10, 4, 9, 10,
	/**/4, 7, 8, 5, 2, 3, 10, 9, 7, 8,
	/**/8, 5, 7, 5, 7, 3, 9, 7, 7, 9 };
	private static final int[] prices = new int[] {
	/**/21, 22, 28, 21, 12, 24, 15, 2, 25, 28,
	/**/4, 22, 36, 2, 7, 40, 14, 40, 33, 21,
	/**/28, 22, 14, 36, 28, 21, 18, 12, 4, 15,
	/**/21, 2, 5, 28, 28, 4, 22, 36, 31, 2,
	/**/7, 40, 14, 4, 28, 40, 33, 35, 21, 20 };
	private static final int weightMax = 200;

	@Override
	public Individual inflateIndividual() {
		Chromosome chromosome = new Chromosome.Creator().append(50, 1)
				.inflate();
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
			BitSet set = c.getBitSet(0);
			int weightTotal = 0;
			int priceTotal = 0;
			for (int i = 0; i < weights.length; i++) {
				if (set.get(i)) {
					weightTotal += weights[i];
					priceTotal += prices[i];
				}
			}
			double fitness = priceTotal;
			if (weightTotal > weightMax) {
				fitness -= 100 * (weightTotal - weightMax);
			}
			c.phenoType[0] = weightTotal;
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
		winner.addAll(GeneticAlgorithm.selectElite(candidates, 2));
		winner.addAll(GeneticAlgorithm.selectTournament(candidates, random, 78,
				2));
		return winner;
	}

	public static void main(String... args) {
		Individual best = null;
		GeneticAlgorithm ga = new GeneticAlgorithm(new Knapsack(), 80);
		ga.setReverseOrder(true);
		for (int generation = 0; generation < 100000; generation++) {
			ga.cross(0.7, 0.9);
			ga.mutate(0.01);

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
