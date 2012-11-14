package net.tailriver.science.ga.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.tailriver.science.ga.GeneticAlgorithm;
import net.tailriver.science.ga.GeneticAlgorithmPlan;
import net.tailriver.science.ga.GenoType;
import net.tailriver.science.ga.Individual;

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
public class Michalewicz implements GeneticAlgorithmPlan<Individual> {
	protected GeneticAlgorithm<Individual> ga;
	private Random random;

	{
		random = new Random();
		ga = new GeneticAlgorithm<>(this, 50);
		ga.setReverseOrder(true);
	}

	@Override
	public Individual inflateIndividual() {
		GenoType genoType = new GenoType.Creator().append(22).inflate();
		Individual individual = new Individual(genoType);
		individual.randomize(random);
		return individual;
	}

	@Override
	public Random getRandom() {
		return random;
	}

	@Override
	public void calculateFitness(Collection<Individual> population) {
		for (Individual individual : population) {
			double x = individual.getGenoTypeDouble(0, -1, 2);
			double fitness = x * Math.sin(10d * Math.PI * x) + 2;
			individual.setPhenoType(0, x);
			individual.setFitness(fitness);
		}
	}

	@Override
	public void applyCrossOver(Individual x, Individual y) {
		Individual.crossOverTwoPoint(x, y, random);
	}

	@Override
	public List<Individual> applySelection(List<Individual> candidates) {
		List<Individual> winner = new ArrayList<>();
		winner.addAll(GeneticAlgorithm.selectElite(candidates, 2));
		winner.addAll(GeneticAlgorithm.selectTournament(candidates, random, 48,
				2));
		return winner;
	}

	public static void main(String... args) {
		Michalewicz mi = new Michalewicz();
		Individual best = null;
		for (int generation = 0; generation < 10000; generation++) {
			mi.ga.cross(0.25, 1);
			mi.ga.mutate(0.1);

			Individual generationTop = mi.ga.getRankAt(1);
			if (generationTop.isGreaterThan(best)) {
				best = generationTop;
				System.out.println(">> " + generation);
				best.print();
				System.out.println();
			}

			mi.ga.select();
		}
	}
}
