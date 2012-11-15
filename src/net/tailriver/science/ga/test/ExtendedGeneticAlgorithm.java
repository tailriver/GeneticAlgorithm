package net.tailriver.science.ga.test;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.tailriver.science.ga.GeneticAlgorithm;
import net.tailriver.science.ga.GeneticAlgorithmPlan;

/** extend test */
public class ExtendedGeneticAlgorithm extends
		GeneticAlgorithm<ExtendedIndividual> implements
		GeneticAlgorithmPlan<ExtendedIndividual> {

	public ExtendedGeneticAlgorithm(
			GeneticAlgorithmPlan<ExtendedIndividual> plan, int size) {
		super(plan, size);
	}

	@Override
	public ExtendedIndividual inflateIndividual() {
		ExtendedGenoType egt = new ExtendedGenoType.Creator().inflate();
		ExtendedIndividual ei = new ExtendedIndividual(egt);
		return ei;
	}

	@Override
	public Random getRandom() {
		return null;
	}

	@Override
	public void calculateFitness(Collection<ExtendedIndividual> population) {
	}

	@Override
	public void applyCrossOver(ExtendedIndividual x, ExtendedIndividual y) {
	}

	@Override
	public List<ExtendedIndividual> applySelection(
			List<ExtendedIndividual> population) {
		return null;
	}
}
