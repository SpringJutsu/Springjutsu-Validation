package org.springjutsu.validation.test.entities;

public enum Color {
	
	RED("FF0800"), // candy apple red
	ORANGE("FF7518"), // pumpkin
	YELLOW("FFEF00"), // process yellow
	GREEN("4F7930"), // fern green
	BLUE("333399"), // Pigment blue
	PURPLE("DF73FF"); // heliotrope
	
	Color(String hexTriplet) {
		this.hexTriplet = hexTriplet;
	}
	
	private String hexTriplet;
	
	public String getHexTriplet() {
		return hexTriplet;
	}

}