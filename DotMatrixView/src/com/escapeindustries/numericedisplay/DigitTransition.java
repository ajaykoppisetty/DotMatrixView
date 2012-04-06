package com.escapeindustries.numericedisplay;

import java.util.ArrayList;
import java.util.List;

public class DigitTransition {

	private int[] from;
	private int[] to;
	private int[] dim;
	private int[] light;

	public DigitTransition(int[] from, int[] to) {
		this.from = from;
		this.to = to;
		// calculateTransition();
		calculateTransitionOnePass();
	}

	public void calculateTransition() {
		dim = getTransitionToDimDots();
		light = getTransitionToLitDots();
	}

	public int[] getDotsToDim() {
		return dim;
	}

	public int[] getDotsToLight() {
		return light;
	}

	private int[] getTransitionToDimDots() {
		List<Integer> dimWorking = new ArrayList<Integer>();
		boolean found;
		for (int i = 0; i < from.length; i++) {
			found = false;
			for (int j = 0; j < to.length; j++) {
				if (to[j] == from[i]) {
					found = true;
					break;
				}
			}
			if (!found) {
				dimWorking.add(from[i]);
			}
		}
		return intListToArray(dimWorking);
	}

	private int[] getTransitionToLitDots() {
		ArrayList<Integer> litWorking = new ArrayList<Integer>();
		boolean found;
		for (int i = 0; i < to.length; i++) {
			found = false;
			for (int j = 0; j < from.length; j++) {
				if (from[j] == to[i]) {
					found = true;
					break;
				}
			}
			if (!found) {
				litWorking.add(to[i]);
			}
		}
		return intListToArray(litWorking);
	}

	public void calculateTransitionOnePass() {
		List<Integer> lightThese = new ArrayList<Integer>();
		List<Integer> dimThese = new ArrayList<Integer>();
		int f = 0;
		int t = 0;
		while (f < from.length && t < to.length) {
			if (from[f] == to[t]) {
				// Dot should stay on - no change
				f++;
				t++;
			} else if (from[f] > to[t]) {
				// Dot should be lit
				lightThese.add(to[t]);
				t++;
			} else {
				// Dot should be dimmed
				dimThese.add(from[f]);
				f++;
			}
		}
		if (f < from.length) {
			// Reached the end of to before the end of from - remaining from
			// must be dimmed
			for (; f < from.length; f++) {
				dimThese.add(from[f]);
			}
		} else if (t < to.length) {
			// Reached the end of from before the end of to - remaining to must
			// be lit
			for (; t < to.length; t++) {
				lightThese.add(to[t]);
			}
		}
		light = intListToArray(lightThese);
		dim = intListToArray(dimThese);
	}

	private int[] intListToArray(List<Integer> dimWorking) {
		int[] dim = new int[dimWorking.size()];
		for (int i = 0; i < dimWorking.size(); i++) {
			dim[i] = dimWorking.get(i);
		}
		return dim;
	}
}
