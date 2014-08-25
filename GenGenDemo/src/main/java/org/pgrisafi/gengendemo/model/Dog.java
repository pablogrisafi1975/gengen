package org.pgrisafi.gengendemo.model;

import java.math.BigDecimal;

import org.pgrisafi.gengen.annotations.GenGenBeanBuilder;

@GenGenBeanBuilder(builderPackage = ".builders")
public class Dog {
	private String petName;
	private int age;
	private Integer size;
	private BigDecimal fleas;
	private Long attitude;

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

	public BigDecimal getFleas() {
		return fleas;
	}

	public void setFleas(BigDecimal fleas) {
		this.fleas = fleas;
	}

	public Long getAttitude() {
		return attitude;
	}

	public void setAttitude(Long attitude) {
		this.attitude = attitude;
	}

}
