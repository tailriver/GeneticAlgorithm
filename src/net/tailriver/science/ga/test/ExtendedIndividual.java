package net.tailriver.science.ga.test;

import net.tailriver.science.ga.GenoType;
import net.tailriver.science.ga.Individual;

public class ExtendedIndividual extends Individual {
	private static final long serialVersionUID = 1L;

	public ExtendedIndividual(GenoType genoType) {
		super(genoType);
	}

	public GenoType getGenoType() {
		return genoType;
	}

	public Object[] getPhenoType() {
		return phenoType;
	}
}
