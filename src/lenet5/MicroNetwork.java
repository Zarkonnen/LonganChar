package lenet5;

import java.util.ArrayList;
import java.util.Random;

import static lenet5.Util.*;

public class MicroNetwork {
	Network nw;
	Random r = new Random();
	
	public MicroNetwork() {
		Layer input = new Layer("Input");
		for (int i = 0; i < 5 * 5 * MicroNet.kernels.length; i++) {
			input.nodes.add(new Node("input " + i));
		}
		// Bias node!
		/*Node biasN = new Node("input bias");
		biasN.activation = 1.0;
		input.nodes.add(biasN);*/
		
		Layer hidden = new Layer("Hidden");
		for (int i = 0; i < 100; i++) {
			hidden.nodes.add(new Node("hidden " + i));
		}
		
		Layer output = new Layer("Output");
		output.nodes.add(new Node("output"));
		
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
				//if (r.nextInt(/*5*/5) == 0/*r.nextBoolean()*/) {
				//	new Connection(inputs, hN, w);
				//}
				if (iNum / 25 != hNum / 3 && (iNum + hNum) % 4 == 0) {
					new Connection(inputs, hN, w);
				}
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
		
		nw = new Network(layers);
	}
	
	public void train(double[][] positives, double[][] negatives, double n, double m) {
		for (int i = 0; i < positives.length; i++) {
			nw.train(positives[i], new double[] {1.0}, n, m);
			nw.train(negatives[i], new double[] {0.0}, n, m);
		}
	}
	
	public double run(double[] input) {
		return nw.run(input)[0];
	}
}
