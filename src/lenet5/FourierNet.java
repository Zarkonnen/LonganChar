package lenet5;

import java.util.ArrayList;
import java.util.Random;

import static lenet5.Util.*;

public class FourierNet {
	Network nw;
	Random r = new Random();
	
	public FourierNet() {
		Layer input = new Layer("Input");
		for (int y = 0; y < 28; y++) { for (int x = 0; x < 28; x++) {
			input.nodes.add(new Node("input " + y + "/" + x));
		}}
		// Bias node!
		Node biasN = new Node("input bias");
		biasN.activation = 1.0;
		
		Layer h1 = new Layer("H1 (fft)");
		for (int y = 0; y < 14; y++) { for (int x = 0; x < 14; x++) {
			h1.nodes.add(new Node("FFT " + y + "/" + x));
		}}
		
		Layer h2 = new Layer("H2 (random)");
		for (int i = 0; i < Lenet4b.OUTPUT_SIZE; i++) {
			h2.nodes.add(new Node("H2." + i));
		}
		
		Layer output = new Layer("Output");
		for (int i = 0; i < Lenet4b.OUTPUT_SIZE; i++) {
			output.nodes.add(new Node("Output " + i));
		}
		
		for (int y = 0; y < 14; y++) {
			for (int x = 0; x < 14; x++) {
				Weight w = new Weight(1.0 * 28 * 28 / (x + 1) / (y + 1));
				input.weights.add(w);
				for (int yy = 0; yy < 28; yy += (y + 1)) {
					for (int xx = 0; xx < 28; xx += (x + 1)) {
						new Connection(
								input.nodes.get(yy * 28 + xx),
								h1.nodes.get(y * 14 + x),
								w
						);
					}
				}
			}
		}
		
		// Connect h1 to h2
		for (Node h1N : h1.nodes) {
			for (Node h2N : h2.nodes) {
				if (r.nextInt(5) == 0) {
					Weight w = new Weight(rnd(-2.0 / Lenet4b.OUTPUT_SIZE * 5, 2.0 / Lenet4b.OUTPUT_SIZE * 5));
					h1.weights.add(w);
					new Connection(h1N, h2N, w);
				}
			}
		}
		
		for (Node h2N : h2.nodes) {
			Weight w = new Weight(rnd(-2.0 / Lenet4b.OUTPUT_SIZE * 5, 2.0 / Lenet4b.OUTPUT_SIZE * 5));
			h1.weights.add(w);
			new Connection(biasN, h2N, w);
		}
		
		// Connect h2 to output
		for (Node h2N : h2.nodes) {
			for (Node oN : output.nodes) {
				Weight w = new Weight(rnd(-2.0 / Lenet4b.OUTPUT_SIZE, 2.0 / Lenet4b.OUTPUT_SIZE));
				h2.weights.add(w);
				new Connection(h2N, oN, w);
			}
		}
		
		for (Node oN : output.nodes) {
			Weight w = new Weight(rnd(-2.0 / Lenet4b.OUTPUT_SIZE, 2.0 / Lenet4b.OUTPUT_SIZE));
			h2.weights.add(w);
			new Connection(biasN, oN, w);
		}
		
		ArrayList<Layer> layers = new ArrayList<Layer>();
		layers.add(input);
		layers.add(h1);
		layers.add(h2);
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
