package net.tailriver.science.ga;

import java.util.BitSet;

public final class Mask implements Cloneable {
	public final int length;

	private final BitSet mask;

	public Mask(int length) {
		mask = new BitSet(length);
		this.length = length;
	}

	/** @see java.util.BitSet#flip(int) */
	public void flip(int bitIndex) {
		mask.flip(bitIndex);
	}

	/** @see java.util.BitSet#flip(int, int) */
	public void flip(int fromIndex, int toIndex) {
		mask.flip(fromIndex, toIndex);
	}

	/** @see java.util.BitSet#set(int) */
	public void set(int bitIndex) {
		mask.set(bitIndex);
	}

	/** @see java.util.BitSet#set(int, int) */
	public void set(int fromIndex, int toIndex) {
		mask.set(fromIndex, toIndex);
	}

	/** @see java.util.BitSet#clear(int) */
	public void clear(int bitIndex) {
		mask.clear(bitIndex);
	}

	/** @see java.util.BitSet#clear(int, int) */
	public void clear(int fromIndex, int toIndex) {
		mask.clear(fromIndex, toIndex);
	}

	/** @see java.util.BitSet#clear() */
	public void clear() {
		mask.clear();
	}

	/** @see java.util.BitSet#get(int) */
	public boolean get(int bitIndex) {
		return mask.get(bitIndex);
	}

	/** @see java.util.BitSet#get(int, int) */
	public BitSet get(int fromIndex, int toIndex) {
		return mask.get(fromIndex, toIndex);
	}

	/** @see java.util.BitSet#nextSetBit(int) */
	public int nextSetBit(int fromIndex) {
		return mask.nextSetBit(fromIndex);
	}

	/** @see java.util.BitSet#nextClearBit(int) */
	public int nextClearBit(int fromIndex) {
		return mask.nextClearBit(fromIndex);
	}

	/** @see java.util.BitSet#previousSetBit(int) */
	public int previousSetBit(int fromIndex) {
		return mask.previousSetBit(fromIndex);
	}

	/** @see java.util.BitSet#previousClearBit(int) */
	public int previousClearBit(int fromIndex) {
		return mask.previousClearBit(fromIndex);
	}

	/** @see java.util.BitSet#isEmpty() */
	public boolean isEmpty() {
		return mask.isEmpty();
	}

	/** @see java.util.BitSet#hashCode() */
	@Override
	public int hashCode() {
		return mask.hashCode();
	}

	/** @see java.util.BitSet#clone() */
	@Override
	public Object clone() {
		return mask.clone();
	}

	/** @see java.util.BitSet#toString() */
	@Override
	public String toString() {
		return mask.toString();
	}
}
