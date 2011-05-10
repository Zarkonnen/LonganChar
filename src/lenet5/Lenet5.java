package lenet5;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import static lenet5.Util.*;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

public class Lenet5 {
	public static void main(String[] args) {
		Lenet5 l5 = new Lenet5();
		l5 = null;
	}
	
	final Network nw;
	
	public static double[][] rasterClasses(BufferedImage[] classes) {
		double[][] classRasters = new double[classes.length][7*12];
		for (int i = 0; i < classes.length; i++) {
			// Read in classes
			for (int y = 0; y < 12; y++) {
				for (int x = 0; x < 7; x++) {
					Color c = new Color(classes[i].getRGB(x, y));
					// Black is 1.0, white is -1.0.
					double value = 1.0 - (c.getRed() + c.getGreen() + c.getBlue()) / (255 * 3) * 2.0;
					classRasters[i][y * 7 + x] = value;
				}
			}
		}
		return classRasters;
	}
	
	public static double[][][] rasterData(BufferedImage[][] data) {
		BufferedImage scaled = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaled.createGraphics();
		double[][][] dataRasters = new double[data.length][][]; // Jagged array
		for (int i = 0; i < data.length; i++) {
			dataRasters[i] = new double[data[i].length][32 * 32];
			for (int j = 0; j < data[i].length; j++) {
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, 32, 32);
				g.drawImage(
					data[i][j],
					6,
					6,
					26,
					26,
					0,
					0,
					data[i][j].getWidth(),
					data[i][j].getHeight(),
					null
				);
				/*try {ImageIO.write(scaled, "png", new File("/Users/zar/Desktop/out/" + i + "-" + j + ".png"));
				} catch (Exception e) { e.printStackTrace(); }*/
				for (int y = 0; y < 32; y++) {
					for (int x = 0; x < 32; x++) {
						Color c = new Color(scaled.getRGB(x, y));
						double value = 1.175 - (c.getRed() + c.getGreen() + c.getBlue()) / (255 * 3) * 1.275;
						dataRasters[i][j][y * 32 + x] = value;
					}
				}
			}
		}
		return dataRasters;
	}
	
	public void train(double[][] classRasters, double[][][] trainingRasters, double n, double m, int passes) {
		for (int pass = 0; pass < passes; pass++) {
			/*
			for (int classId = 0; classId < classRasters.length; classId++) {
				for (double[] example : trainingRasters[classId]) {
					nw.train(example, classRasters[classId], n, m);
				}
			}
			 * 
			 */
			for (int exId = 0; exId < trainingRasters[0].length; exId++) {
				for (int classId = 0; classId < classRasters.length; classId++) {
					nw.train(trainingRasters[classId][exId], classRasters[classId], n, m);
				}
			}
			System.out.println("pass " + pass + " complete");
		}
	}
	
	public double[] classify(double[][] classRasters, double[] input) {
		double[] errors = new double[classRasters.length];
		for (int classId = 0; classId < classRasters.length; classId++) {
			double[] output = nw.run(input);
			for (int i = 0; i < output.length; i++) {
				errors[classId] += (output[i] - classRasters[classId][i]) * (output[i] - classRasters[classId][i]);
			}
			errors[classId] /= 84;
		}
		return errors;
	}

	public Lenet5() {		
		Layer inputL = new Layer("Input");
		Layer c1 = new Layer("C1");
		Layer s2 = new Layer("S2");
		Layer c3 = new Layer("C3");
		Layer s4 = new Layer("S4");
		Layer c5 = new Layer("C5");
		Layer f6 = new Layer("F6");
		
		ArrayList<Layer> layers = new ArrayList<Layer>();
		layers.add(inputL);
		layers.add(c1);
		layers.add(s2);
		layers.add(c3);
		layers.add(s4);
		layers.add(c5);
		layers.add(f6);
		
		nw = new Network(layers);
		
		Node bias = new Node("Bias");
		bias.activation = 1.0;
		
		// Nodes in the input layer are simply addressed by y * 32 + x.
		for (int y = 0; y < 32; y++) {
			for (int x = 0; x < 32; x++) {
				inputL.nodes.add(new Node("Input " + x + "/" + y));
			}
		}
		
		// Nodes in the first (convolutional) layer are addressed by f * 784 + y * 28 + x.
		for (int f = 0; f < 6; f++) {
			ArrayList<Weight> ws = new ArrayList<Weight>();
			for (int i = 0; i < 26; i++) {
				ws.add(new Weight(rnd(-2.4 / 26, 2.4 / 26)));
			}
			inputL.weights.addAll(ws);
			
			for (int y = 0; y < 28; y++) {
				for (int x = 0; x < 28; x++) {
					Node n = new Node("C1, map " + f + ", " + x + "/" + y);
					c1.nodes.add(n);
					// Hook 'er up:
					for (int dy = 0; dy < 5; dy++) {
						for (int dx = 0; dx < 5; dx++) {
							ArrayList<Node> inList = new ArrayList<Node>();
							inList.add(inputL.nodes.get((y + dy) * 32 + (x + dx)));
							new Connection(inList, n, ws.get(dy * 5 + dx));
						}
					}
					// ...and the bias
					ArrayList<Node> inList = new ArrayList<Node>();
					inList.add(bias);
					new Connection(inList, n, ws.get(25));
				}
			}
		}
		
		// Nodes in the second (scaling) layer are addressed by f * 196 + y * 14 + x.
		for (int f = 0; f < 6; f++) {
			ArrayList<Weight> ws = new ArrayList<Weight>();
			ws.add(new Weight(rnd(-2.4 / 2, 2.4 / 2))); // Scaling weight
			ws.add(new Weight(rnd(-2.4 / 2, 2.4 / 2))); // Bias weight
			c1.weights.addAll(ws);
			for (int y = 0; y < 14; y++) {
				for (int x = 0; x < 14; x++) {
					Node n = new Node("S2, map " + f + ", " + x + "/" + y);
					s2.nodes.add(n);
					ArrayList<Node> inList = new ArrayList<Node>();
					for (int dy = 0; dy < 2; dy++) {
						for (int dx = 0; dx < 2; dx++) {
							inList.add(c1.nodes.get(
									f * 784 +
									(y * 2 + dy) * 28 +
									(x * 2 + dx)
									));
						}
					}
					new Connection(inList, n, ws.get(0));
					inList = new ArrayList<Node>();
					inList.add(bias);
					new Connection(inList, n, ws.get(1));
				}
			}
		}
		
		// Nodes in the third (convolutional) layer are addressed by f * 100 + y * 10 + x.
		boolean X = true;
		boolean O = false;
		// Which feature maps in C1/S2 are connected to which feature maps in C3.
		boolean[][] wiringPattern = {
			{X, O, O, O, X, X, X, O, O, X, X, X, X, O, X, X},
			{X, X, O, O, O, X, X, X, O, O, X, X, X, X, O, X},
			{X, X, X, O, O, O, X, X, X, O, O, X, O, X, X, X},
			{O, X, X, X, O, O, X, X, X, X, O, O, X, O, X, X},
			{O, O, X, X, X, O, O, X, X, X, X, O, X, X, O, X},
			{O, O, O, X, X, X, O, O, X, X, X, X, O, X, X, X}
		};
		
		for (int f = 0; f < 16; f++) {
			// Calculate the number of weights we're going to have.
			int nWeights = 0;
			for (boolean[] pattern : wiringPattern) {
				if (pattern[f]) { nWeights += 25; }
			}
			nWeights++; // bias
			// Generate that many weights.
			ArrayList<Weight> ws = new ArrayList<Weight>(nWeights);
			for (int i = 0; i < nWeights; i++) {
				ws.add(new Weight(rnd(-2.4 / nWeights, 2.4 / nWeights)));
			}
			s2.weights.addAll(ws);
			// Weights are indexed by connection * 25 + dy * 5 + dx. The final weight is for the
			// bias.
			for (int y = 0; y < 10; y++) {
				for (int x = 0; x < 10; x++) {
					Node n = new Node("C3, map " + f + ", " + x + "/" + y);
					c3.nodes.add(n);
					int connection = 0;
					for (int s2f = 0; s2f < 6; s2f++) {
						if (wiringPattern[s2f][f]) {
							for (int dy = 0; dy < 5; dy++) {
								for (int dx = 0; dx < 5; dx++) {
									ArrayList<Node> inList = new ArrayList<Node>();
									inList.add(s2.nodes.get(
											196 * s2f +
											14 * (y + dy) +
											(x + dx)
									));
									new Connection(inList, n, ws.get(
											connection * 25 +
											dy * 5 +
											dx
									));
								}
							}
							connection++;
						}
					}
					// Finally, add the bias node.
					ArrayList<Node> inList = new ArrayList<Node>();
					inList.add(bias);
				}
			}
		}
		
		// Nodes in the fourth (scaling) layer are addressed by f * 25 + y * 5 + x.
		for (int f = 0; f < 16; f++) {
			ArrayList<Weight> ws = new ArrayList<Weight>();
			ws.add(new Weight(rnd(-2.4 / 2, 2.4 / 2))); // Scaling weight
			ws.add(new Weight(rnd(-2.4 / 2, 2.4 / 2))); // Bias weight
			c3.weights.addAll(ws);
			for (int y = 0; y < 5; y++) {
				for (int x = 0; x < 5; x++) {
					Node n = new Node("S4, map " + f + ", " + x + "/" + y);
					s4.nodes.add(n);
					ArrayList<Node> inList = new ArrayList<Node>();
					for (int dy = 0; dy < 2; dy++) {
						for (int dx = 0; dx < 2; dx++) {
							inList.add(c3.nodes.get(
									f * 100 +
									(y * 2 + dy) * 10 +
									(x * 2 + dx)
									));
						}
					}
					new Connection(inList, n, ws.get(0));
					inList = new ArrayList<Node>();
					inList.add(bias);
					new Connection(inList, n, ws.get(1));
				}
			}
		}
		
		// The fifth (convolutional) layer is essentially a fully connected layer of 120 nodes.
		for (int f = 0; f < 120; f++) {
			// Each node is connected to all the previous feature maps, yielding
			// 16 * 25 + 1 weights each.
			ArrayList<Weight> ws = new ArrayList<Weight>();
			for (int i = 0; i < 16 * 25 + 1; i++) {
				ws.add(new Weight(rnd(-2.4 / (16 * 25 + 1), 2.4 / (16 * 25 + 1))));
			}
			s4.weights.addAll(ws);
			
			for (int s4f = 0; s4f < 16; s4f++) {
				for (int y = 0; y < 1; y++) {
					for (int x = 0; x < 1; x++) {
						Node n = new Node("C5, map " + f + ", " + x + "/" + y);
						c5.nodes.add(n);
						for (int dy = 0; dy < 5; dy++) {
							for (int dx = 0; dx < 5; dx++) {
								ArrayList<Node> inList = new ArrayList<Node>();
								inList.add(s4.nodes.get(
										25 * s4f +
										5 * (y + dy) +
										(x + dx)
								));
								new Connection(inList, n, ws.get(
										s4f * 25 +
										dy * 5 +
										dx));
							}
						}
						// ...and the bias
						ArrayList<Node> inList = new ArrayList<Node>();
						inList.add(bias);
						new Connection(inList, n, ws.get(16 * 25));
					}
				}
			}
		}
		
		// The sixth layer is fully connected.
		for (int j = 0; j < 84; j++) {
			ArrayList<Weight> ws = new ArrayList<Weight>();
			for (int i = 0; i < 121; i++) {
				ws.add(new Weight(rnd(-2.4 / 121, 2.4 / 121)));
			}
			c5.weights.addAll(ws);
			
			Node n = new Node("F6, #" + j);
			f6.nodes.add(n);
			
			for (int c5f = 0; c5f < 120; c5f++) {
				ArrayList<Node> inList = new ArrayList<Node>();
				inList.add(c5.nodes.get(c5f));
				new Connection(inList, n, ws.get(c5f));
			}
			
			ArrayList<Node> inList = new ArrayList<Node>();
			inList.add(bias);
			new Connection(inList, n, ws.get(120));
		}
	}
}
