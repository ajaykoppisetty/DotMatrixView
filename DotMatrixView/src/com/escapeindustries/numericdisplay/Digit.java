package com.escapeindustries.numericdisplay;

public class Digit extends Glyph {

	private int current = 10;

	public Digit(Grid grid, int column, int row) {
		this.width = 7;
		this.height = 13;
		this.leftMostColumn = column;
		this.topRow = row;
		this.grid = grid;
	}

	public int getNumber() {
		return current;
	}

	public void setNumber(int to) {
		DigitTransition trans = new DigitTransition(
				new DotChangeAnimationAction(this));
		trans.makeTransition(DigitDefinition.patterns[current],
				DigitDefinition.patterns[to]);
		current = to;
	}

	@Override
	void draw() {
		setNumber(current);
	}

}