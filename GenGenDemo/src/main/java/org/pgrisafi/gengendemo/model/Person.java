package org.pgrisafi.gengendemo.model;

import java.util.Date;

import org.pgrisafi.gengen.annotations.GenGenBeanBuilder;

@GenGenBeanBuilder
public class Person {
	private String firstName;
	private String lastName;
	private int age;
	private Date birthDate;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

}
