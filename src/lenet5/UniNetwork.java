package lenet5;

import java.util.ArrayList;
import java.util.Random;

import static lenet5.Util.*;

public class UniNetwork {
	Network nw;
	Random r = new Random();
	
	public UniNetwork() {
		Layer input = new Layer("Input");
		for (int i = 0; i < 12 * 12 * UniNet.kernels.length + 3; i++) {
			input.nodes.add(new Node("input " + i));
		}
		// Bias node!
		Node biasN = new Node("input bias");
		biasN.activation = 1.0;
		//input.nodes.add(biasN);*/
		
		Layer hidden = new Layer("Hidden");
		for (int i = 0; i < 177; i++) {
			hidden.nodes.add(new Node("hidden " + i));
		}
		
		// 2nd hidden layer
		Layer h2 = new Layer("Hidden 2");
		for (int i = 0; i < 9 * 5 * 7; i++) {
			h2.nodes.add(new Node("h2 " + i));
		}
		
		Layer output = new Layer("Output");
		for (int y = 0; y < 9; y++) {
			for (int x = 0; x < 5; x++) {
				output.nodes.add(new Node("output " + y + "/" + x));
			}
		}
		
		// Connect input to h1
		int iNum = 0;
		for (Node iN : input.nodes) {
			iNum++;
			int hNum = 0;
			for (Node hN : hidden.nodes) {
				hNum++;
				Weight w = new Weight(rnd(-0.2, 0.2));
				input.weights.add(w);
				ArrayList<Node> inputs = new ArrayList<Node>();
				inputs.add(iN);
				if (iNum / 144 != hNum / 3 && (iNum + hNum) % 4 == 0) {
					new Connection(inputs, hN, w);
				}
			}
		}
		// Attach bias to h1
		for (Node hN : hidden.nodes) {
			Weight w = new Weight(rnd(-0.2, 0.2));
			input.weights.add(w);
			ArrayList<Node> inputs = new ArrayList<Node>();
			inputs.add(biasN);
			new Connection(inputs, hN, w);
		}
		
		// Connect h1 to h2
		for (Node hN : hidden.nodes) {
			for (Node h2N : h2.nodes) {
				Weight w = new Weight(rnd(-1.0, 1.0));
				hidden.weights.add(w);
				ArrayList<Node> inputs = new ArrayList<Node>();
				inputs.add(hN);
				new Connection(inputs, h2N, w);
			}
		}
		
		// Add bias to h2
		for (Node h2N : h2.nodes) {
			Weight w = new Weight(rnd(-1.0, 1.0));
			hidden.weights.add(w);
			ArrayList<Node> inputs = new ArrayList<Node>();
			inputs.add(biasN);
			new Connection(inputs, h2N, w);
		}
		
		// Connect h2 to output
		int h2NodeID = 0;
		for (Node h2N : h2.nodes) {
			int outNodeID = 0;
			for (Node oN : output.nodes) {
				if (outNodeID++ != h2NodeID / 7) { continue; }
				Weight w = new Weight(rnd(-1.0, 1.0));
				h2.weights.add(w);
				ArrayList<Node> inputs = new ArrayList<Node>();
				inputs.add(h2N);
				new Connection(inputs, oN, w);
			}
			
			h2NodeID++;
		}
		
		// Add bias to output
		for (Node oN : output.nodes) {
			Weight w = new Weight(rnd(-1.0, 1.0));
			h2.weights.add(w);
			ArrayList<Node> inputs = new ArrayList<Node>();
			inputs.add(biasN);
			new Connection(inputs, oN, w);
		}
		
		ArrayList<Layer> layers = new ArrayList<Layer>();
		layers.add(input);
		layers.add(hidden);
		layers.add(h2);
		layers.add(output);
		
		nw = new Network(layers);
	}
	
	public void train(UniNet.Example ex, double n, double m) {
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
