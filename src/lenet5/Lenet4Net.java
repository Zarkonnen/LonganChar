package lenet5;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

import static lenet5.Util.*;

public class Lenet4Net {
	Network nw;
	Random r = new Random();
	
	public Lenet4Net(int variant) {
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
		
		Layer output = new Layer("Output");
		for (int i = 0; i < Lenet4.LETTERS.length; i++) {
			output.nodes.add(new Node("Output " + i + ": " + Lenet4.LETTERS[i]));
		}
		
		// Connect input to h1
		HashMap<String, Weight> offsetToWeight = new HashMap<String, Weight>();
		int iNum = 0;
		for (Node iN : input.nodes) {
			int inY = iNum / 12;
			int inX = iNum % 12;
			iNum++;
			int hNum = 0;
			for (Node hN : h1.nodes) {
				int hY = hNum / 12;
				int hX = hNum % 12;
				hNum++;
				if (iNum / 144 != hNum / (2 + variant) && (iNum + hNum + variant) % 5 == 0) {
					String offset = null;
					if (iNum >= 12 * 12 * DemoNet.kernels.length) {
						offset = "special " + iNum;
					} else {
						offset = (inY - hY) + "/" + (inX - hX);
					}
					Weight w;
					if (offsetToWeight.containsKey(offset)) {
						w = offsetToWeight.get(offset);
					} else {
						w = new Weight(rnd(-0.2, 0.2));
						offsetToWeight.put(offset, w);
						input.weights.add(w);
					}
					ArrayList<Node> inputs = new ArrayList<Node>();
					inputs.add(iN);
					new Connection(inputs, hN, w);
				}
			}
		}
		// Attach bias to h1
		for (Node hN : h1.nodes) {
			Weight w = new Weight(rnd(-0.2, 0.2));
			input.weights.add(w);
			ArrayList<Node> inputs = new ArrayList<Node>();
			inputs.add(biasN);
			new Connection(inputs, hN, w);
		}
		
		// Connect h1 to h2
		int h1Num = 0;
		for (Node hN : h1.nodes) {
			h1Num++;
			int h2Num = 0;
			for (Node h2N : h2.nodes) {
				h2Num++;
				if (iNum / 25 != h1Num / (3 + variant) && (h1Num + h2Num * (variant + 1)) % 2 == 0) {
					Weight w = new Weight(rnd(-1.0, 1.0));
					h1.weights.add(w);
					ArrayList<Node> inputs = new ArrayList<Node>();
					inputs.add(hN);
					new Connection(inputs, h2N, w);
				}
			}
		}
		
		// Add bias to h2
		for (Node h2N : h2.nodes) {
			Weight w = new Weight(rnd(-1.0, 1.0));
			h1.weights.add(w);
			ArrayList<Node> inputs = new ArrayList<Node>();
			inputs.add(biasN);
			new Connection(inputs, h2N, w);
		}
		
		// Connect h2 to output
		int h2NodeID = 0;
		for (Node h2N : h2.nodes) {
			int outNodeID = 0;
			for (Node oN : h3.nodes) {
				if (outNodeID++ != h2NodeID / 3) { continue; }
				Weight w = new Weight(rnd(-1.0, 1.0));
				h2.weights.add(w);
				ArrayList<Node> inputs = new ArrayList<Node>();
				inputs.add(h2N);
				new Connection(inputs, oN, w);
			}
			
			h2NodeID++;
		}
		
		// Add bias to output
		for (Node oN : h3.nodes) {
			Weight w = new Weight(rnd(-1.0, 1.0));
			h2.weights.add(w);
			ArrayList<Node> inputs = new ArrayList<Node>();
			inputs.add(biasN);
			new Connection(inputs, oN, w);
		}
		
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
