package org.pgrisafi.gengendemo.model;

import org.pgrisafi.gengen.annotations.GenGenBuilder;

@GenGenBuilder
public class BigDog extends Dog {

	private int height;
	private int height2;

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight2() {
		return height2;
	}

	public void setHeight2(int height2) {
		this.height2 = height2;
	}

}
