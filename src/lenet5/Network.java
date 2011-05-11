package lenet5;

import java.util.ArrayList;

public class Network {
	public final ArrayList<Layer> layers;

	public Network(ArrayList<Layer> layers) {
		this.layers = layers;
	}
	
	public void train(double[] input, double[] target, double n, double m) {
		setInput(input);
		update();
		setTargets(target);
		calculateDelta();
		adjustWeights(n, m);
	}
	
	public double[] run(double[] input) {
		setInput(input);
		update();
		double[] output = new double[layers.get(layers.size() - 1).nodes.size()];
		for (int i = 0; i < output.length; i++) {
			output[i] = layers.get(layers.size() - 1).nodes.get(i).activation;
		}
		return output;
	}
	
	public void setInput(double[] inputs) {
		ArrayList<Node> iNodes = layers.get(0).nodes;
		assert(inputs.length == iNodes.size());
		for (int i = 0; i < inputs.length; i++) {
			iNodes.get(i).activation = inputs[i];
		}
	}
	
	public void setTargets(double[] targets) {
		ArrayList<Node> outputs = layers.get(layers.size() - 1).nodes;
		assert(targets.length == outputs.size());
		for (int i = 0; i < targets.length; i++) {
			outputs.get(i).delta = targets[i] - outputs.get(i).activation;
		}
	}
	
	public void calculateDelta() {
		// Don't calc delta for output layer.
		for (int i = layers.size() - 2; i >= 0; i--) {
			layers.get(i).calculateDelta();
		}
	}
	
	public void adjustWeights(double n, double m) {
		for (int i = layers.size() - 1; i >= 0; i--) {
			//System.out.println(layers.get(i).name + " adjusted by " + layers.get(i).adjustWeights(n, m));
			layers.get(i).adjustWeights(n, m);
		}
	}
	
	public void update() {
		for (Layer l : layers) { l.update(); }
	}
	
	public String getDetails() {
		String d = "";
		for (Layer l : layers) {
			d += l.getDetails() + "\n";
		}
		return d;
	}
}