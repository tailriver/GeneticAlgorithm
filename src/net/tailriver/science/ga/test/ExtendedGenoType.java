package net.tailriver.science.ga.test;

import java.util.List;

import net.tailriver.science.ga.GenoType;

public class ExtendedGenoType extends GenoType {
	private static final long serialVersionUID = 1L;

	public ExtendedGenoType(List<Integer> nbitList) {
		super(nbitList);
	}

	public ExtendedGenoType(ExtendedGenoType original) {
		super(original);
	}

	public static class Creator extends GenoType.Creator {

		@Override
		public ExtendedGenoType inflate() {
			return new ExtendedGenoType(nbitList);
		}
	}
}
