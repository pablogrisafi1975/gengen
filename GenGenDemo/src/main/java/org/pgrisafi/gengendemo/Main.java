package org.pgrisafi.gengendemo;

import org.pgrisafi.gengendemo.model.Person;
import org.pgrisafi.gengendemo.model.PersonBuilder;

public class Main {
	public static void main(String[] args) {
		Person person = PersonBuilder.start().age(33).lastName("perez").lastName("juan").build();
		Person person2 = new PersonBuilder().age(33).lastName("perez").lastName("juan").build();

	}

}
