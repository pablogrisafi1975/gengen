package org.pgrisafi.gengen.generator;

public class Field {
	private String type;
	private String name;
	private String setter;

	public Field(String type, String name, String setter) {
		this.type = type;
		this.name = name;
		this.setter = setter;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSetter() {
		return setter;
	}

	public void setSetter(String setter) {
		this.setter = setter;
	}
}