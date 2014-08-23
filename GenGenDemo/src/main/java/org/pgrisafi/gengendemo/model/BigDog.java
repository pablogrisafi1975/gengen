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

}
