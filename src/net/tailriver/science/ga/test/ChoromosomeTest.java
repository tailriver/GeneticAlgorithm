package net.tailriver.science.ga.test;

import static org.junit.Assert.*;

import java.util.Random;

import net.tailriver.science.ga.Chromosome;

import org.junit.*;

public class ChoromosomeTest {
	Chromosome c;
	Random random;

	@Before
	public void setUp() {
		c = new Chromosome.Creator().append(4, 8).inflate();
		random = new Random();
	}

	@Test
	public void testChromosome() {
		random = new Random(42342352);
		c = new Chromosome.Creator()
		.append(8, 1)
		.append(34, 1)
		.inflate();

		c.randomize(random);
		assertEquals(42, c.bitSizeTotal());
		assertEquals("10111001 0101001101111101000111010011101101", c.toString());
		assertEquals(185, c.getLong(0));
		assertEquals(5602833645L, c.getLong(1));
	}

	@Test
	public void testRandomize() {
		random = new Random(343129087);
		Chromosome p = new Chromosome(c);
		p.randomize(random);
		assertEquals("0000 0000 0000 0000 0000 0000 0000 0000", c.toString());
		assertEquals("1110 0010 0000 1111 0001 1110 1010 1100", p.toString());
	}

	@Test
	public void testMutate() {
		random = new Random(59034);
		assertEquals("0000 0000 0000 0000 0000 0000 0000 0000", c.toString());
		boolean result;
	
		// should not change
		Chromosome p = new Chromosome(c);
		result = p.mutate(random, 0);
		assertEquals("0000 0000 0000 0000 0000 0000 0000 0000", p.toString());
		assertFalse(result);
	
		// should flip about 3 bits
		Chromosome q = new Chromosome(c);
		result = q.mutate(random, 0.1);
		assertEquals("0010 0001 0000 0000 0000 0100 0000 0000", q.toString());
		assertTrue(result);
	
		// should flip about half of all bits
		Chromosome r = new Chromosome(c);
		result = r.mutate(random, 0.5);
		assertEquals("1011 1100 1000 1111 1001 1110 1111 0100", r.toString());
		assertTrue(result);
	
		// should invert all bits
		Chromosome s = new Chromosome(c);
		result = s.mutate(random, 1);
		assertEquals("1111 1111 1111 1111 1111 1111 1111 1111", s.toString());
		assertTrue(result);
	}

	@Test
	public void testSwap() {
		random = new Random(4329);
		Chromosome a = new Chromosome(c);
		Chromosome b = new Chromosome(c);
		a.randomize(random);
		b.randomize(random);
		assertEquals("1110 0001 0101 1000 1011 1111 1101 1111", a.toString());
		assertEquals("1110 0101 1100 0101 0011 1011 0011 0010", b.toString());
		Chromosome.swap(a, b, 6, 22);
		assertEquals("1110 0101 1100 0101 0011 1111 1101 1111", a.toString());
		assertEquals("1110 0001 0101 1000 1011 1011 0011 0010", b.toString());
	}

	@Test
	public void testCopy() {
		Chromosome copy = new Chromosome(c);
		assertNotSame(c, copy);
		assertEquals(c.bitSizeTotal(),  copy.bitSizeTotal());
		assertEquals(c.toString(), copy.toString());
	
		c.randomize(random);
		assertFalse(c.toString().equals(copy.toString()));
	}

	@Test
	public void testCreator() {
		Chromosome.Creator creator = new Chromosome.Creator();
		assertNotNull(creator);
		assertNotNull(c);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreatorAppendInvaild1() {
		new Chromosome.Creator().append(0, 1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreatorAppendInvaild2() {
		new Chromosome.Creator().append(1, 0);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testRandomizeInvalidRandom() {
		c.randomize(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testMutateInvalidRandom() {
		c.mutate(null, 0.5);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testMutateInvalidRate() {
		c.mutate(random, Double.NaN);
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testMutateOutOfBounds1() {
		c.mutate(random, - Double.MIN_NORMAL);
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testMutateOutOfBounds2() {
		c.mutate(random, 1.000000001);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSwapIncompatibleChoromosome() {
		Chromosome a = new Chromosome.Creator().append(4, 8).inflate();
		Chromosome b = new Chromosome.Creator().append(4, 9).inflate();
		Chromosome.swap(a, b, 0, 1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSwapSameAddressChromosome() {
		Chromosome.swap(c, c, 0, 1);		
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testSwapOutOfBounds1() {
		Chromosome.swap(new Chromosome(c), new Chromosome(c), -1, 0);
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testSwapOutOfBounds2() {
		Chromosome.swap(new Chromosome(c), new Chromosome(c), 0, c.bitSizeTotal() + 1);
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testSwapOutOfBounds3() {
		Chromosome.swap(new Chromosome(c), new Chromosome(c), c.bitSizeTotal(), c.bitSizeTotal());
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testSwapOutOfBounds4() {
		Chromosome.swap(new Chromosome(c), new Chromosome(c), 1, 0);
	}
}
