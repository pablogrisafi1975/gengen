package org.pgrisafi.gengendemo;

import org.pgrisafi.gengendemo.model.Person;
import org.pgrisafi.gengendemo.model.PersonBuilder;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Person person = PersonBuilder.start().age(33).lastName("perez").lastName("juan").build();

	}

}
