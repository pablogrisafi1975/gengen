package org.pgrisafi.gengendemo.model;

import org.pgrisafi.gengen.annotations.GenGenBuilder;

@GenGenBuilder
public class Dog {
	private String petName;
	private int age;
	private Integer size;

	public String getPetName() {
		return petName;
	}

	public void setPetName(String petName) {
		this.petName = petName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

}
