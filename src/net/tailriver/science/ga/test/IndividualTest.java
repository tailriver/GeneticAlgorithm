package net.tailriver.science.ga.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;
import java.util.Random;

import net.tailriver.science.ga.GenoType;
import net.tailriver.science.ga.Individual;

import org.junit.Before;
import org.junit.Test;

public class IndividualTest {
	GenoType g;
	Individual a;
	Random random;
	BitSet mask;

	@Before
	public void setUp() {
		g = new GenoType.Creator().append(4, 8).inflate();
		a = new Individual(g);
		random = new Random();
		mask = new BitSet();
	}

	@Test
	public void test() {
		assertNotNull(a);
	}

	// @Test
	public void testGenoType() {
		random = new Random(42342352);
		g = new GenoType.Creator().append(8, 1).append(34, 1).inflate();
		a = new Individual(g);

		a.randomize(random);
		assertEquals(185, a.getGenoTypeLong(0));
		assertEquals(5602833645L, a.getGenoTypeLong(1));
	}

	@Test
	public void testGetDouble() {
		random = new Random(245238909421L);
		g = new GenoType.Creator().append(64).inflate();
		a = new Individual(g);

		a.randomize(random);
		String binary = "1100101001001101011110010010010001101010011011110111001000001111";
		assertEquals(binary, a.toGenoTypeString());
		assertTrue(a.getGenoTypeDouble(0) < Double.MAX_VALUE);
	}

	@Test
	public void testRandomize() {
		random = new Random(343129087);
		Individual p = new Individual(new GenoType(g));
		p.randomize(random);
		assertEquals("0000 0000 0000 0000 0000 0000 0000 0000",
				a.toGenoTypeString());
		assertEquals("1101 0011 0110 0111 1000 1010 1111 0101",
				p.toGenoTypeString());
	}

	@Test
	public void testMutate() {
		random = new Random(59034);
		assertEquals("0000 0000 0000 0000 0000 0000 0000 0000",
				a.toGenoTypeString());

		// should not change
		Individual p = new Individual(new GenoType(g));
		p.mutate(random, 0);
		assertEquals("0000 0000 0000 0000 0000 0000 0000 0000",
				p.toGenoTypeString());

		// should flip about 3 bits
		Individual q = new Individual(new GenoType(g));
		q.mutate(random, 0.1);
		assertEquals("0010 0001 0000 0000 0000 0100 0000 0000",
				q.toGenoTypeString());

		// should flip about half of all bits
		Individual r = new Individual(new GenoType(g));
		r.mutate(random, 0.5);
		assertEquals("1011 1100 1000 1111 1001 1110 1111 0100",
				r.toGenoTypeString());

		// should invert all bits
		Individual s = new Individual(new GenoType(g));
		s.mutate(random, 1);
		assertEquals("1111 1111 1111 1111 1111 1111 1111 1111",
				s.toGenoTypeString());
	}

	@Test
	public void testClone() {
		Individual c = a.clone();
		assertNotSame(a, c);
		assertFalse(a.equals(c));
		assertEquals(a.toString(), c.toString());

		a.randomize(random);
		assertFalse(a.toString().equals(c.toString()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetDouble63() {
		g = new GenoType.Creator().append(63).inflate();
		a = new Individual(g);
		a.getGenoTypeDouble(0);
	}

	@Test
	public void testGetDouble64() {
		g = new GenoType.Creator().append(64).inflate();
		a = new Individual(g);
		a.getGenoTypeDouble(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetDouble65() {
		g = new GenoType.Creator().append(65).inflate();
		a = new Individual(g);
		a.getGenoTypeDouble(0);
	}

	@Test(expected = NullPointerException.class)
	public void testRandomizeInvalidRandom() {
		a.randomize(null);
	}

	@Test(expected = NullPointerException.class)
	public void testMutateInvalidRandom() {
		a.mutate(null, 0.5);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMutateInvalidRate() {
		a.mutate(random, Double.NaN);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMutateOutOfBounds1() {
		a.mutate(random, -Double.MIN_NORMAL);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMutateOutOfBounds2() {
		a.mutate(random, 1.000000001);
	}

	@Test
	public void testIsGreaterThan() {
		a.setFitness(0);
		Individual b = a.clone();

		b.setFitness(-1);
		assertTrue(a.isGreaterThan(b));

		b.setFitness(0);
		assertFalse(a.isGreaterThan(b));

		b.setFitness(1);
		assertFalse(a.isGreaterThan(b));
	}

	@Test
	public void testIsLessThan() {
		a.setFitness(0);
		Individual b = a.clone();

		b.setFitness(-1);
		assertFalse(a.isLessThan(b));

		b.setFitness(0);
		assertFalse(a.isLessThan(b));

		b.setFitness(1);
		assertTrue(a.isLessThan(b));
	}
}
