package lenet5;

import static lenet5.Util.*;
import java.util.ArrayList;
import java.util.Random;

public class XorTest {

	public static void main(String[] args) {
		Layer input = new Layer("Input");
		for (int i = 0; i < 2; i++) {
			input.nodes.add(new Node("input " + i));
		}
		// Bias node!
		Node biasN = new Node("input bias");
		biasN.activation = 1.0;
		input.nodes.add(biasN);
		
		Layer hidden = new Layer("Hidden");
		for (int i = 0; i < 2; i++) {
			hidden.nodes.add(new Node("hidden " + i));
		}
		
		Layer output = new Layer("Output");
		output.nodes.add(new Node("output"));
		
		for (Node iN : input.nodes) {
			for (Node hN : hidden.nodes) {
				Weight w = new Weight(rnd(-0.2, 0.2));
				input.weights.add(w);
				ArrayList<Node> inputs = new ArrayList<Node>();
				inputs.add(iN);
				new Connection(inputs, hN, w);
			}
		}
		
		for (Node hN : hidden.nodes) {
			Weight w = new Weight(rnd(-2.0, 2.0));
			hidden.weights.add(w);
			ArrayList<Node> inputs = new ArrayList<Node>();
			inputs.add(hN);
			new Connection(inputs, output.nodes.get(0), w);
		}
		
		ArrayList<Layer> layers = new ArrayList<Layer>();
		layers.add(input);
		layers.add(hidden);
		layers.add(output);
		
		Network nw = new Network(layers);
		
		/*
		n.inputToHiddenWeights[0][0] = 0.01;
		n.inputToHiddenWeights[0][1] = -0.004;
		
		n.inputToHiddenWeights[1][0] = -0.15;
		n.inputToHiddenWeights[1][1] = -0.11;
		
		n.inputToHiddenWeights[2][0] = -0.09;
		n.inputToHiddenWeights[2][1] = 0.07;
		
		n.hiddenToOutputWeights[0][0] = 1.6;
		n.hiddenToOutputWeights[1][0] = -0.84;
		 * 
		 */
		/*input.weights.get(0).value = 0.01;
		input.weights.get(1).value = -0.004;
		input.weights.get(2).value = -0.15;
		input.weights.get(3).value = -0.11;
		input.weights.get(4).value = -0.09;
		input.weights.get(5).value = 0.07;
		
		hidden.weights.get(0).value = 1.6;
		hidden.weights.get(1).value = -0.84;*/
		
		double[][][] pat = {
			{{0,0}, {0}},
			{{0,1}, {1}},
			{{1,0}, {1}},
			{{1,1}, {0}}
		};
		
		for (int rep = 0; rep < 1000; rep++) {
			for (double[][] p : pat) {
				for (int i = 0; i < p[0].length; i++) {
					input.nodes.get(i).activation = p[0][i];
				}
				nw.update();
				nw.setTargets(p[1]);
				nw.calculateDelta();
				/*if (rep % 100 == 0) {
					System.out.println(nw.getDetails());
				}*/
				nw.adjustWeights(0.5, 0.1);
				//System.out.println("meow");
			}
		}
		
		for (double[][] p : pat) {
			for (int i = 0; i < p[0].length; i++) {
				input.nodes.get(i).activation = p[0][i];
			}
			nw.update();
			System.out.println(p[0][0] + " xor " + p[0][1] + " = " + output.nodes.get(0).activation);
		}
	}
}
