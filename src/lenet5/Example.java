package lenet5;

import java.awt.image.BufferedImage;

class Example {
	public String letter;
	public double[] input;
	public double[] target;
	public BufferedImage original;

	public Example(String letter, double[] input, double[] target) {
		this.letter = letter;
		this.input = input;
		this.target = target;
	}	
}
