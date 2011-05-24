package lenet5;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

import static lenet5.Util.*;

public class UniNetwork3 {
	Network nw;
	Random r = new Random();
	
	public UniNetwork3(int variant) {
		Layer input = new Layer("Input");
		for (int i = 0; i < 12 * 12 * UniNet.kernels.length + 3; i++) {
			input.nodes.add(new Node("input " + i));
		}
		// Bias node!
		Node biasN = new Node("input bias");
		biasN.activation = 1.0;
		//input.nodes.add(biasN);*/
		
		Layer hidden = new Layer("Hidden");
		for (int i = 0; i < 12 * 12; i++) {
			hidden.nodes.add(new Node("hidden " + i));
		}
		
		// 2nd hidden layer
		Layer h2 = new Layer("Hidden 2");
		for (int i = 0; i < UniNet3.OUTPUT_SIZE * 3; i++) {
			h2.nodes.add(new Node("h2 " + i));
		}
		
		Layer output = new Layer("Output");
		for (int i = 0; i < UniNet3.OUTPUT_SIZE; i++) {
			output.nodes.add(new Node("output " + i));
		}
		
		// Connect input to h1
		HashMap<String, Weight> offsetToWeight = new HashMap<String, Weight>();
		int iNum = 0;
		for (Node iN : input.nodes) {
			int inY = iNum / 12;
			int inX = iNum % 12;
			iNum++;
			int hNum = 0;
			for (Node hN : hidden.nodes) {
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
		for (Node hN : hidden.nodes) {
			Weight w = new Weight(rnd(-0.2, 0.2));
			input.weights.add(w);
			ArrayList<Node> inputs = new ArrayList<Node>();
			inputs.add(biasN);
			new Connection(inputs, hN, w);
		}
		
		// Connect h1 to h2
		int h1Num = 0;
		for (Node hN : hidden.nodes) {
			h1Num++;
			int h2Num = 0;
			for (Node h2N : h2.nodes) {
				h2Num++;
				if (iNum / 25 != h1Num / (3 + variant) && (h1Num + h2Num * (variant + 1)) % 2 == 0) {
					Weight w = new Weight(rnd(-1.0, 1.0));
					hidden.weights.add(w);
					ArrayList<Node> inputs = new ArrayList<Node>();
					inputs.add(hN);
					new Connection(inputs, h2N, w);
				}
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
