package net.tailriver.science.ga.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import net.tailriver.science.ga.GenoType;
import net.tailriver.science.ga.Mask;

import org.junit.Before;
import org.junit.Test;

public class GenoTypeTest {
	GenoType c;
	Random random;
	Mask mask;

	@Before
	public void setUp() {
		c = new GenoType.Creator().append(4, 8).inflate();
		mask = c.getMask();
	}

	@Test
	public void testGenoType() {
		random = new Random(42342352);
		c = new GenoType.Creator().append(8, 1).append(34, 1).inflate();
		randomize(c);

		assertEquals(2, c.length);
		assertEquals(8, c.getLength(0));
		assertEquals(34, c.getLength(1));
		assertEquals("10111001 0101001101111101000111010011101101",
				c.toString());
		assertEquals(185, c.getLong(0));
		assertArrayEquals(new long[] { 185 }, c.getBitSet(0).toLongArray());
		assertEquals(5602833645L, c.getLong(1));
	}

	@Test
	public void testRandomize() {
		random = new Random(343129087);
		GenoType p = new GenoType(c);
		randomize(p);
		assertEquals("0000 0000 0000 0000 0000 0000 0000 0000", c.toString());
		assertEquals("1110 0010 0000 1111 0001 1110 1010 1100", p.toString());
	}

	@Test
	public void testInvert() {

	}

	@Test
	public void testSwap() {
		random = new Random(4329);
		GenoType a = new GenoType(c);
		GenoType b = new GenoType(c);
		randomize(a);
		randomize(b);
		assertEquals("1110 0001 0101 1000 1011 1111 1101 1111", a.toString());
		assertEquals("1110 0101 1100 0101 0011 1011 0011 0010", b.toString());
		mask.set(6, 22);
		GenoType.swap(a, b, mask);
		assertEquals("1110 0101 1100 0101 0011 1111 1101 1111", a.toString());
		assertEquals("1110 0001 0101 1000 1011 1011 0011 0010", b.toString());
	}

	@Test
	public void testCopy() {
		GenoType copy = new GenoType(c);
		assertNotSame(c, copy);
		assertTrue(c.equals(copy));
		assertEquals(c.toString(), copy.toString());

		mask.set(0);
		c.invert(mask);
		assertFalse(c.toString().equals(copy.toString()));
	}

	@Test
	public void testCreator() {
		GenoType.Creator creator = new GenoType.Creator();
		assertNotNull(creator);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreatorNothingAppended() {
		new GenoType.Creator().inflate();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreatorAppendInvaild1() {
		new GenoType.Creator().append(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreatorAppendInvaild2() {
		new GenoType.Creator().append(1, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreatorAppendInvaild3() {
		new GenoType.Creator().append(0, 1);
	}

	@Test
	public void testGetBitSet() {
		c.getBitSet(0);
	}

	@Test
	public void testGetBoolean1() {
		c = new GenoType.Creator().append(2).inflate();
		c.getBoolean(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetBoolean2() {
		c = new GenoType.Creator().append(2).inflate();
		c.getBoolean(0);
	}

	@Test
	public void testGetLong64() {
		c = new GenoType.Creator().append(64).inflate();
		c.getLong(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetLong65() {
		c = new GenoType.Creator().append(65).inflate();
		c.getLong(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSwapIncompatibleChoromosome() {
		GenoType a = new GenoType.Creator().append(4, 8).inflate();
		GenoType b = new GenoType.Creator().append(4, 9).inflate();
		mask.set(0, 1);
		GenoType.swap(a, b, mask);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSwapSameGenoType() {
		mask.set(0, 1);
		GenoType.swap(c, c, mask);
	}

	@Test(expected = NullPointerException.class)
	public void testSwapInvalidMask() {
		GenoType a = new GenoType(c);
		GenoType b = new GenoType(c);
		GenoType.swap(a, b, null);
	}

	public void randomize(GenoType g) {
		for (int i = 0, max = mask.length; i < max; i++) {
			if (random.nextBoolean())
				mask.set(i);
		}
		g.invert(mask);
	}
}
