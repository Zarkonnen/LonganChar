package lenet5;

import java.util.ArrayList;
import java.util.Random;

import static lenet5.Util.*;

public class ConvNetwork {
	static final double[][][] kernels = {
		// Identity
		{
			{ 0,  0,  0,  0,  0 },
			{ 0,  0,  0,  0,  0 },
			{ 0,  0,  1,  0,  0 },
			{ 0,  0,  0,  0,  0 },
			{ 0,  0,  0,  0,  0 },
		},
		// c/o detector (vgap)
			{
			{ 0,  0,  0,  0,  0 },
			{ 0, -1,  4, -1,  0 },
			{ 0, -1, -2, -1,  0 },
			{ 0, -1,  4, -1,  0 },
			{ 0,  0,  0,  0,  0 },
		},
		// weird-ass kernel
		{
			{ 0,  0,  0,  0,  0 },
			{ 0, -2,  4,  2,  0 },
			{ 0, -4,  0,  4,  0 },
			{ 0,  2, -4, -2,  0 },
			{ 0,  0,  0,  0,  0 },
		},
	};
	
	Network nw;
	Random r = new Random();
	
	static final int MAPS = 6;
	
	public ConvNetwork() {
		Layer input = new Layer("Input");
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				input.nodes.add(new Node("input " + y + "/" + x));
			}
		}
		
		// Bias node!
		Node biasN = new Node("input bias");
		biasN.activation = 1.0;
		
		Layer convolution = new Layer("Convolution");
		for (int m = 0; m < MAPS; m++) {
			for (int y = 0; y < 12; y++) {
				for (int x = 0; x < 12; x++) {
					convolution.nodes.add(new Node("convolution " + m + "/" + y + "/" + x));
				}
			}
		}
		
		Layer hidden = new Layer("Hidden");
		for (int i = 0; i < 44; i++) {
			hidden.nodes.add(new Node("hidden " + i));
		}
		
		// 2nd hidden layer
		Layer h2 = new Layer("Hidden 2");
		for (int i = 0; i < 9; i++) {
			h2.nodes.add(new Node("h2 " + i));
		}
		
		Layer output = new Layer("Output");
		output.nodes.add(new Node("output"));
		
		// Wire up input layer to convolution layer.
		for (int m = 0; m < MAPS; m++) {
			for (int mapY = 0; mapY < 5; mapY++) {
				for (int mapX = 0; mapX < 5; mapX++) {
					Weight w = null;
					if (m < kernels.length) {
						w = new Weight(kernels[m][mapY][mapX]);
					} else {
						w = new Weight(rnd(-1.0, 1.0));
					}
					input.weights.add(w);
					for (int convY = 0; convY < 12; convY++) {
						for (int convX = 0; convX < 12; convX++) {
							ArrayList<Node> inputs = new ArrayList<Node>();
							inputs.add(input.nodes.get(
								(convY + mapY) * 16 +
								(convX + mapX)
							));
							new Connection(inputs,
							convolution.nodes.get(
								m * 12 * 12 +
								convY * 12 +
								convX
							),
							w);
						}
					}
				}
			}
			Weight w = new Weight(rnd(-1.0, 1.0));
			input.weights.add(w);
			for (int convY = 0; convY < 12; convY++) {
				for (int convX = 0; convX < 12; convX++) {
					ArrayList<Node> inputs = new ArrayList<Node>();
					inputs.add(biasN);
					new Connection(inputs,
					convolution.nodes.get(
						m * 12 * 12 +
						convY * 12 +
						convX
					),
					w);
				}
			}
		}
		
		// Wire up convolution layer to h1.
		int iNum = 0;
		for (Node iN : convolution.nodes) {
			iNum++;
			int hNum = 0;
			for (Node hN : hidden.nodes) {
				hNum++;
				Weight w = new Weight(rnd(-0.2, 0.2));
				convolution.weights.add(w);
				ArrayList<Node> inputs = new ArrayList<Node>();
				inputs.add(iN);
				if (iNum / 144 != hNum / 3 && (iNum + hNum) % 4 == 0) {
					new Connection(inputs, hN, w);
				}
			}
		}
		for (Node hN : hidden.nodes) {
			Weight w = new Weight(rnd(-0.2, 0.2));
			convolution.weights.add(w);
			ArrayList<Node> inputs = new ArrayList<Node>();
			inputs.add(biasN);
			new Connection(inputs, hN, w);
		}
		
		// Wire up h1 to h2.
		for (Node hN : hidden.nodes) {
			for (Node h2N : h2.nodes) {
				Weight w = new Weight(rnd(-1.0, 1.0));
				hidden.weights.add(w);
				ArrayList<Node> inputs = new ArrayList<Node>();
				inputs.add(hN);
				new Connection(inputs, h2N, w);
			}
		}
		
		for (Node h2N : h2.nodes) {
			Weight w = new Weight(rnd(-1.0, 1.0));
			hidden.weights.add(w);
			ArrayList<Node> inputs = new ArrayList<Node>();
			inputs.add(biasN);
			new Connection(inputs, h2N, w);
		}
		
		// Wire up h2 to input.
		for (Node h2N : h2.nodes) {
			Weight w = new Weight(rnd(-2.0, 2.0));
			//hidden.weights.add(w);
			h2.weights.add(w);
			ArrayList<Node> inputs = new ArrayList<Node>();
			inputs.add(h2N);
			new Connection(inputs, output.nodes.get(0), w);
		}
		
		ArrayList<Layer> layers = new ArrayList<Layer>();
		layers.add(input);
		layers.add(convolution);
		layers.add(hidden);
		layers.add(h2);
		layers.add(output);
		
		nw = new Network(layers);
	}
	
	public void train(double[][] positives, double[][] negatives, double n, double m) {
		int to = Math.max(positives.length, negatives.length);
		for (int i = 0; i < to; i++) {
			nw.train(positives[i % positives.length], new double[] {1.0}, n, m);
			nw.train(negatives[i % negatives.length], new double[] {0.0}, n, m);
		}
	}
	
	public double run(double[] input) {
		return nw.run(input)[0];
	}
}
