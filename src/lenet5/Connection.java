package lenet5;

import java.util.ArrayList;

public class Connection {
	public final ArrayList<Node> inputs;
	public final Node output;
	public final Weight weight;

	public Connection(Node input, Node output, Weight weight) {
		inputs = new ArrayList<Node>();
		inputs.add(input);
		this.output = output;
		this.weight = weight;
		weight.connections.add(this);
		input.outgoing.add(this);
		output.incoming.add(this);
	}

	public Connection(ArrayList<Node> inputs, Node output, Weight weight) {
		this.inputs = inputs;
		this.output = output;
		this.weight = weight;
		weight.connections.add(this);
		for (Node input : inputs) { 
			input.outgoing.add(this);
		}
		output.incoming.add(this);
	}
}
