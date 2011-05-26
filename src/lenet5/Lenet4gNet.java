package lenet5;

import java.util.ArrayList;
import java.util.Random;

import static lenet5.Util.*;

public class Lenet4gNet {
	Network nw;
	Random r = new Random();
	
	public Lenet4gNet() {
		Layer input = new Layer("Input");
		for (int y = 0; y < 14; y++) { for (int x = 0; x < 14; x++) {
			input.nodes.add(new Node("input " + y + "/" + x));
		}}
		// Bias node!
		Node biasN = new Node("input bias");
		biasN.activation = 1.0;
		
		Layer h1 = new Layer("H1 (conv)");
		for (int m = 0; m < 6; m++) {
			for (int y = 0; y < 12; y++) { for (int x = 0; x < 12; x++) {
				h1.nodes.add(new Node("H1." + m + " " + y + "/" + x));
			}}
		}
		
		Layer h2 = new Layer("H2 (subsampling)");
		for (int m = 0; m < 6; m++) {
			for (int y = 0; y < 6; y++) { for (int x = 0; x < 6; x++) {
				h2.nodes.add(new Node("H2." + m + " " + y + "/" + x));
			}}
		}
		
		Layer h3 = new Layer("H3 (conv)");
		for (int m = 0; m < 16; m++) {
			for (int y = 0; y < 4; y++) { for (int x = 0; x < 4; x++) {
				h3.nodes.add(new Node("H3." + m + " " + y + "/" + x));
			}}
		}
		
		Layer h4 = new Layer("H4 (subsampling)");
		for (int m = 0; m < 16; m++) {
			for (int y = 0; y < 2; y++) { for (int x = 0; x < 2; x++) {
				h4.nodes.add(new Node("H4." + m + " " + y + "/" + x));
			}}
		}
		
		Layer output = new Layer("Output");
		for (int i = 0; i < Lenet4g.OUTPUT_SIZE; i++) {
			output.nodes.add(new Node("Output " + i));
		}
		
		// Connect input to h1
		for (int m = 0; m < 6; m++) {
			for (int wY = 0; wY < 3; wY++) { for (int wX = 0; wX < 3; wX++) {
				Weight w = new Weight(rnd(-2.0 / 10, 2.0 / 10));
				input.weights.add(w);
				for (int y = 0; y < 12; y++) { for (int x = 0; x < 12; x++) {
					new Connection(
						input.nodes.get(
							(y + wY) * 14 +
							(x + wX)
						),
						h1.nodes.get(
							m * 12 * 12 +
							y * 12 +
							x
						),
						w
					);
				}}
			}}
			
			// Bias
			Weight w = new Weight(rnd(-2.0 / 10, 2.0 / 10));
			input.weights.add(w);
			for (int y = 0; y < 12; y++) { for (int x = 0; x < 12; x++) {
				new Connection(
					biasN,
					h1.nodes.get(
						m * 12 * 12 +
						y * 12 +
						x
					),
					w
				);
			}}
		}
		
		// Connect h1 to h2
		for (int m = 0; m < 6; m++) {
			Weight w = new Weight(0.25);
			h1.weights.add(w);
			for (int y = 0; y < 6; y++) { for (int x = 0; x < 6; x++) {
				for (int dy = 0; dy < 2; dy++) { for (int dx = 0; dx < 2; dx++) {
					new Connection(
						h1.nodes.get(
							m * 12 * 12 +
							(y * 2 + dy) * 12 +
							(x * 2 + dx)
						),
						h2.nodes.get(
							m * 6 * 6 +
							y * 6 +
							x
						),
						w
					);
				}}
			}}
		}
		
		// Connect h2 to h3
		boolean X = true;
		boolean O = false;
		boolean[][] table = {
			{X, O, O, O, X, X, X, O, O, X, X, X, X, O, X, X},
			{X, X, O, O, O, X, X, X, O, O, X, X, X, X, O, X},
			{X, X, X, O, O, O, X, X, X, O, O, X, O, X, X, X},
			{O, X, X, X, O, O, X, X, X, X, O, O, X, O, X, X},
			{O, O, X, X, X, O, O, X, X, X, X, O, X, X, O, X},
			{O, O, O, X, X, X, O, O, X, X, X, X, O, X, X, X}
		};
		
		for (int m1 = 0; m1 < 6; m1++) {
			for (int m3 = 0; m3 < 16; m3++) {
				if (!table[m1][m3]) { continue; }
				for (int wY = 0; wY < 3; wY++) { for (int wX = 0; wX < 3; wX++) {
					Weight w = new Weight(rnd(-2.0 / 10, 2.0 / 10));
					h2.weights.add(w);
					for (int y = 0; y < 4; y++) { for (int x = 0; x < 4; x++) {
						new Connection(
							h2.nodes.get(
								m1 * 6 * 6 +
								(y + wY) * 6 +
								(x + wX)
							),
							h3.nodes.get(
								m3 * 4 * 4 +
								y * 4 +
								x
							),
							w
						);
					}}
				}}
				
				// Add bias
				Weight w = new Weight(rnd(-2.0 / 10, 2.0 / 10));
				h2.weights.add(w);
				for (int y = 0; y < 4; y++) { for (int x = 0; x < 4; x++) {
					new Connection(
						biasN,
						h3.nodes.get(
							m3 * 4 * 4 +
							y * 4 +
							x
						),
						w
					);
				}}
			}
		}
		
		// Connect h3 to h4
		for (int m3 = 0; m3 < 16; m3++) {
			Weight w = new Weight(0.25);
			h3.weights.add(w);
			for (int y = 0; y < 2; y++) { for (int x = 0; x < 2; x++) {
				for (int dy = 0; dy < 2; dy++) { for (int dx = 0; dx < 2; dx++) {
					new Connection(
						h3.nodes.get(
							m3 * 4 * 4 +
							(y * 2 + dy) * 4 +
							(x * 2 + dx)
						),
						h4.nodes.get(
							m3 * 2 * 2 +
							y * 2 +
							x
						),
						w
					);
				}}
			}}
		}
		
		// Connect h4 to output (full connection)
		for (Node h4N : h4.nodes) {
			for (Node oN : output.nodes) {
				Weight w = new Weight(rnd(-2.0 / 65, 2.0 / 65));
				h4.weights.add(w);
				new Connection(h4N, oN, w);
			}
		}
		// Add biases to output.
		for (Node oN : output.nodes) {
			Weight w = new Weight(rnd(-2.0 / 65, 2.0 / 65));
			h4.weights.add(w);
			new Connection(biasN, oN, w);
		}
		
		/*
		for (int out = 0; out < Lenet4b.OUTPUT_SIZE; out++) {
			for (int i = 0; i < 7; i++) {
				Weight w = new Weight(rnd(-2.0 / 8, 2.0 / 8));
				h5.weights.add(w);
				new Connection(
					h5.nodes.get(
						out * 7 +
						i
					),
					output.nodes.get(out),
					w
				);
			}
		}
		
		// Add biases to output.
		for (Node oN : output.nodes) {
			Weight w = new Weight(rnd(-2.0 / 8, 2.0 / 8));
			h5.weights.add(w);
			new Connection(biasN, oN, w);
		}*/
		
		ArrayList<Layer> layers = new ArrayList<Layer>();
		layers.add(input);
		layers.add(h1);
		layers.add(h2);
		layers.add(h3);
		layers.add(h4);
		layers.add(output);
		
		nw = new Network(layers);
	}
	
	public void train(Example ex, double n, double m) {
		nw.train(ex.input, ex.target, n, m);
		/*
		int to = Math.max(positives.length, negatives.length);
		for (int i = 0; i < to; i++) {
			nw.train(positives[i % positives.length], new double[] {1.0}, n, m);
			nw.train(negatives[i % negatives.length], new double[] {0.0}, n, m);
		}*/
	}
	
	public double[] run(double[] input) {
		return nw.run(input);
	}
}
