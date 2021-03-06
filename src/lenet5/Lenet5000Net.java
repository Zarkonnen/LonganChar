package lenet5;

import java.util.ArrayList;
import java.util.Random;

import static lenet5.Util.*;

public class Lenet5000Net {
	Network nw;
	Random r = new Random();
	
	public Lenet5000Net() {
		Layer input = new Layer("Input");
		for (int y = 0; y < 32; y++) { for (int x = 0; x < 32; x++) {
			input.nodes.add(new Node("input " + y + "/" + x));
		}}
		// Bias node!
		Node biasN = new Node("input bias");
		biasN.activation = 1.0;
		
		Layer h1 = new Layer("H1 (conv)");
		for (int m = 0; m < 6; m++) {
			for (int y = 0; y < 28; y++) { for (int x = 0; x < 28; x++) {
				h1.nodes.add(new Node("H1." + m + " " + y + "/" + x));
			}}
		}
		
		Layer h2 = new Layer("H2 (subsampling)");
		for (int m = 0; m < 6; m++) {
			for (int y = 0; y < 14; y++) { for (int x = 0; x < 14; x++) {
				h2.nodes.add(new Node("H2." + m + " " + y + "/" + x));
			}}
		}
		
		Layer h3 = new Layer("H3 (conv)");
		for (int m = 0; m < 16; m++) {
			for (int y = 0; y < 10; y++) { for (int x = 0; x < 10; x++) {
				h3.nodes.add(new Node("H3." + m + " " + y + "/" + x));
			}}
		}
		
		Layer h4 = new Layer("H4 (subsampling)");
		for (int m = 0; m < 16; m++) {
			for (int y = 0; y < 5; y++) { for (int x = 0; x < 5; x++) {
				h4.nodes.add(new Node("H4." + m + " " + y + "/" + x));
			}}
		}
		
		Layer h5 = new Layer("H5 (convolution)");
		for (int m = 0; m < 120; m++) {
			h5.nodes.add(new Node("H5." + m));
		}
		
		Layer output = new Layer("Output");
		for (int i = 0; i < Lenet4b.OUTPUT_SIZE; i++) {
			output.nodes.add(new Node("Output " + i));
		}
		
		// Connect input to h1
		for (int m = 0; m < 6; m++) {
			for (int wY = 0; wY < 5; wY++) { for (int wX = 0; wX < 5; wX++) {
				Weight w = new Weight(rnd(-2.0 / 26, 2.0 / 26));
				input.weights.add(w);
				for (int y = 0; y < 28; y++) { for (int x = 0; x < 28; x++) {
					new Connection(
						input.nodes.get(
							(y + wY) * 32 +
							(x + wX)
						),
						h1.nodes.get(
							m * 28 * 28 +
							y * 28 +
							x
						),
						w
					);
				}}
			}}
			
			// Bias
			Weight w = new Weight(rnd(-2.0 / 26, 2.0 / 26));
			input.weights.add(w);
			for (int y = 0; y < 28; y++) { for (int x = 0; x < 28; x++) {
				new Connection(
					biasN,
					h1.nodes.get(
						m * 28 * 28 +
						y * 28 +
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
			for (int y = 0; y < 14; y++) { for (int x = 0; x < 14; x++) {
				for (int dy = 0; dy < 2; dy++) { for (int dx = 0; dx < 2; dx++) {
					new Connection(
						h1.nodes.get(
							m * 28 * 28 +
							(y * 2 + dy) * 28 +
							(x * 2 + dx)
						),
						h2.nodes.get(
							m * 14 * 14 +
							y * 14 +
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
				for (int wY = 0; wY < 5; wY++) { for (int wX = 0; wX < 5; wX++) {
					Weight w = new Weight(rnd(-2.0 / 26, 2.0 / 26));
					h2.weights.add(w);
					for (int y = 0; y < 10; y++) { for (int x = 0; x < 10; x++) {
						new Connection(
							h2.nodes.get(
								m1 * 14 * 14 +
								(y + wY) * 14 +
								(x + wX)
							),
							h3.nodes.get(
								m3 * 10 * 10 +
								y * 10 +
								x
							),
							w
						);
					}}
				}}
			}
		}
		
		// Add bias
		for (int m3 = 0; m3 < 16; m3++) {
			Weight w = new Weight(rnd(-2.0 / 26, 2.0 / 26));
			h2.weights.add(w);
			for (int y = 0; y < 10; y++) { for (int x = 0; x < 10; x++) {
				new Connection(
					biasN,
					h3.nodes.get(
						m3 * 10 * 10 +
						y * 10 +
						x
					),
					w
				);
			}}
		}
		
		// Connect h3 to h4
		for (int m3 = 0; m3 < 16; m3++) {
			Weight w = new Weight(0.25);
			h3.weights.add(w);
			for (int y = 0; y < 5; y++) { for (int x = 0; x < 5; x++) {
				for (int dy = 0; dy < 2; dy++) { for (int dx = 0; dx < 2; dx++) {
					new Connection(
						h3.nodes.get(
							m3 * 10 * 10 +
							(y * 2 + dy) * 10 +
							(x * 2 + dx)
						),
						h4.nodes.get(
							m3 * 5 * 5 +
							y * 5 +
							x
						),
						w
					);
				}}
			}}
		}
		
		// H4 to H5
		for (int m3 = 0; m3 < 16; m3++) {
			for (int m5 = 0; m5 < 120; m5++) {
				if (r.nextInt(4) != 0) { continue; }
				for (int wY = 0; wY < 5; wY++) { for (int wX = 0; wX < 5; wX++) {
					Weight w = new Weight(rnd(-2.0 / 101, 2.0 / 101));
					h4.weights.add(w);
					new Connection(
						h4.nodes.get(
							m3 * 5 * 5 +
							(wY) * 5 +
							(wX)
						),
						h5.nodes.get(
							m5
						),
						w
					);
				}}
			}
		}
		
		for (int m5 = 0; m5 < 120; m5++) {
			// Add bias
			Weight w = new Weight(rnd(-2.0 / 101, 2.0 / 101));
			h4.weights.add(w);
			new Connection(
				biasN,
				h3.nodes.get(
					m5
				),
				w
			);
		}
		
		// Connect h5 to output (full connection)
		for (Node h5N : h5.nodes) {
			for (Node oN : output.nodes) {
				Weight w = new Weight(rnd(-2.0 / 121, 2.0 / 121));
				h5.weights.add(w);
				new Connection(h5N, oN, w);
			}
		}
		// Add biases to output.
		for (Node oN : output.nodes) {
			Weight w = new Weight(rnd(-2.0 / 121, 2.0 / 121));
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
	}
	
	public double[] run(double[] input) {
		return nw.run(input);
	}
}
