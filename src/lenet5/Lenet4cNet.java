package lenet5;

import java.util.ArrayList;
import java.util.Random;

import static lenet5.Util.*;

public class Lenet4cNet {
	Network nw;
	Random r = new Random();
	
	public Lenet4cNet() {
		Layer input = new Layer("Input");
		for (int y = 0; y < 28; y++) { for (int x = 0; x < 28; x++) {
			input.nodes.add(new Node("input " + y + "/" + x));
		}}
		// Bias node!
		Node biasN = new Node("input bias");
		biasN.activation = 1.0;
		
		Layer h1 = new Layer("H1 (conv)");
		for (int m = 0; m < 4; m++) {
			for (int y = 0; y < 24; y++) { for (int x = 0; x < 24; x++) {
				h1.nodes.add(new Node("H1." + m + " " + y + "/" + x));
			}}
		}
		
		Layer h2 = new Layer("H2 (subsampling)");
		for (int m = 0; m < 4; m++) {
			for (int y = 0; y < 12; y++) { for (int x = 0; x < 12; x++) {
				h2.nodes.add(new Node("H2." + m + " " + y + "/" + x));
			}}
		}
		
		Layer h3 = new Layer("H3 (conv)");
		for (int m = 0; m < 12; m++) {
			for (int y = 0; y < 8; y++) { for (int x = 0; x < 8; x++) {
				h3.nodes.add(new Node("H3." + m + " " + y + "/" + x));
			}}
		}
		
		Layer h4 = new Layer("H4 (subsampling)");
		for (int m = 0; m < 12; m++) {
			for (int y = 0; y < 4; y++) { for (int x = 0; x < 4; x++) {
				h4.nodes.add(new Node("H4." + m + " " + y + "/" + x));
			}}
		}
		
		Layer h5 = new Layer("H5 (pre-output)");
		for (int i = 0; i < Lenet4b.OUTPUT_SIZE; i++) {
			for (int j = 0; j < 7; j++) {
				h5.nodes.add(new Node("Pre-output " + i + ":" + j));
			}
		}
		
		Layer output = new Layer("Output");
		for (int i = 0; i < Lenet4b.OUTPUT_SIZE; i++) {
			output.nodes.add(new Node("Output " + i));
		}
		
		// Connect input to h1
		for (int m = 0; m < 4; m++) {
			for (int wY = 0; wY < 5; wY++) { for (int wX = 0; wX < 5; wX++) {
				Weight w = new Weight(rnd(-2.0 / 26, 2.0 / 26));
				input.weights.add(w);
				for (int y = 0; y < 24; y++) { for (int x = 0; x < 24; x++) {
					new Connection(
						input.nodes.get(
							(y + wY) * 28 +
							(x + wX)
						),
						h1.nodes.get(
							m * 24 * 24 +
							y * 24 +
							x
						),
						w
					);
				}}
			}}
			
			// Bias
			Weight w = new Weight(rnd(-2.0 / 26, 2.0 / 26));
			input.weights.add(w);
			for (int y = 0; y < 24; y++) { for (int x = 0; x < 24; x++) {
				new Connection(
					biasN,
					h1.nodes.get(
						m * 24 * 24 +
						y * 24 +
						x
					),
					w
				);
			}}
		}
		
		// Connect h1 to h2
		for (int m = 0; m < 4; m++) {
			Weight w = new Weight(0.25);
			h1.weights.add(w);
			for (int y = 0; y < 12; y++) { for (int x = 0; x < 12; x++) {
				for (int dy = 0; dy < 2; dy++) { for (int dx = 0; dx < 2; dx++) {
					new Connection(
						h1.nodes.get(
							m * 24 * 24 +
							(y * 2 + dy) * 24 +
							(x * 2 + dx)
						),
						h2.nodes.get(
							m * 12 * 12 +
							y * 12 +
							x
						),
						w
					);
				}}
			}}
		}
		
		// Connect h2 to h3
		boolean X = true;
		boolean _ = false;
		boolean[][] table = {
			{X, X, X, _, X, X, _, _, _, _, _, _},
			{_, X, X, X, X, X, _, _, _, _, _, _},
			{_, _, _, _, _, _, X, X, X, _, X, X},
			{_, _, _, _, _, _, _, X, X, X, X, X}
		};
		
		for (int m1 = 0; m1 < 4; m1++) {
			for (int m3 = 0; m3 < 12; m3++) {
				if (!table[m1][m3]) { continue; }
				for (int wY = 0; wY < 5; wY++) { for (int wX = 0; wX < 5; wX++) {
					Weight w = new Weight(rnd(-2.0 / 26, 2.0 / 26));
					h2.weights.add(w);
					for (int y = 0; y < 8; y++) { for (int x = 0; x < 8; x++) {
						new Connection(
							h2.nodes.get(
								m1 * 12 * 12 +
								(y + wY) * 12 +
								(x + wX)
							),
							h3.nodes.get(
								m3 * 8 * 8 +
								y * 8 +
								x
							),
							w
						);
					}}
				}}
				
				// Add bias
				Weight w = new Weight(rnd(-2.0 / 26, 2.0 / 26));
				h2.weights.add(w);
				for (int y = 0; y < 8; y++) { for (int x = 0; x < 8; x++) {
					new Connection(
						biasN,
						h3.nodes.get(
							m3 * 8 * 8 +
							y * 8 +
							x
						),
						w
					);
				}}
			}
		}
		
		// Connect h3 to h4
		for (int m3 = 0; m3 < 12; m3++) {
			Weight w = new Weight(0.25);
			h3.weights.add(w);
			for (int y = 0; y < 4; y++) { for (int x = 0; x < 4; x++) {
				for (int dy = 0; dy < 2; dy++) { for (int dx = 0; dx < 2; dx++) {
					new Connection(
						h3.nodes.get(
							m3 * 8 * 8 +
							(y * 2 + dy) * 8 +
							(x * 2 + dx)
						),
						h4.nodes.get(
							m3 * 4 * 4 +
							y * 4 +
							x
						),
						w
					);
				}}
			}}
		}
		
		// Connect h4 to h5 (full connection)
		for (Node h4N : h4.nodes) {
			for (Node h5N : h5.nodes) {
				Weight w = new Weight(rnd(-2.0 / 193, 2.0 / 193));
				h4.weights.add(w);
				new Connection(h4N, h5N, w);
			}
		}
		// Add biases to h5.
		for (Node h5N : h5.nodes) {
			Weight w = new Weight(rnd(-2.0 / 193, 2.0 / 193));
			h4.weights.add(w);
			new Connection(biasN, h5N, w);
		}
		
		// Connect h5 to output
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
		}
		
		ArrayList<Layer> layers = new ArrayList<Layer>();
		layers.add(input);
		layers.add(h1);
		layers.add(h2);
		layers.add(h3);
		layers.add(h4);
		layers.add(h5);
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