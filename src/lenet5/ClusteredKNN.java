package lenet5;

import com.metalbeetle.fruitbat.atrio.ATRReader;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import javax.imageio.ImageIO;

public class ClusteredKNN {
	static class DoubleArray {
		double[] data;

		public DoubleArray(double[] data) {
			this.data = data;
		}
	}
		
	static final String[] LETTERS = {
		"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
		"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
		"!", "@", "£", "$", "%", "&", "(", ")", "'", ".", ",", ":", ";", "/", "?", "+", "-",
		"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
	};
		
	static String letterToFilename(String l) {
		return
				l.equals(".")
				? "period"
				: l.equals(":")
				? "colon"
				: l.equals("/")
				? "slash"
				: l.toLowerCase().equals(l)
				? l
				: l.toLowerCase() + "-uc";
	}
	
	static final int K = 1;
	static final int SIZE = 9;
	static final double ACCEPTANCE_D = 2.5;
	static final int CLUSTERS = 25;
	static final int CLUSTER_LOOPS = 8;
	
	public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		Random rnd = new Random();
		HashMap<String, HashMap<String, Double>> offsets = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, ArrayList<Double>> offsetLists = new HashMap<String, ArrayList<Double>>();
		HashMap<String, HashMap<String, Double>> sizes = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, ArrayList<Double>> sizeLists = new HashMap<String, ArrayList<Double>>();
		
		for (String s : LETTERS) {
			HashMap<String, Double> os = new HashMap<String, Double>();
			ArrayList<Double> osL = new ArrayList<Double>();
			ATRReader r = new ATRReader(new BufferedInputStream(new FileInputStream(
					new File(new File(args[1]), letterToFilename(s) + "-offset.atr"))));
			List<String> rec = null;
			while ((rec = r.readRecord()) != null) {
				os.put(rec.get(0), Double.parseDouble(rec.get(1)));
				osL.add(Double.parseDouble(rec.get(1)));
			}
			offsets.put(letterToFilename(s), os);
			offsetLists.put(letterToFilename(s), osL);
			r.close();
			
			HashMap<String, Double> ss = new HashMap<String, Double>();
			ArrayList<Double> sL = new ArrayList<Double>();
			r = new ATRReader(new BufferedInputStream(new FileInputStream(
					new File(new File(args[1]), letterToFilename(s) + "-size.atr"))));
			rec = null;
			while ((rec = r.readRecord()) != null) {
				ss.put(rec.get(0), Double.parseDouble(rec.get(1)));
				sL.add(Double.parseDouble(rec.get(1)));
			}
			sizes.put(letterToFilename(s), ss);
			sizeLists.put(letterToFilename(s), sL);
			r.close();
		}
		System.out.println("Loaded offsets and sizes.");
		
		//PrintStream ps = new PrintStream(new File("/Users/zar/Desktop/out.txt"));
		//System.setOut(ps);
		ArrayList<File> bExFolders = new ArrayList<File>();
		
		for (String s : LETTERS) {
			bExFolders.add(new File(new File(args[1]), letterToFilename(s)));
		}
		
		ArrayList<Example> train = new ArrayList<Example>();
		ArrayList<Example> test = new ArrayList<Example>();
		
		for (File fol : bExFolders) {
			if (fol.listFiles() == null) {
				System.out.println(fol + " is not a folder");
				continue;
			}
			if (fol.listFiles().length < 10) {
				System.out.println(fol + " doesn't have enough data");
				continue;
			}
			int nForThisLetter = 0;
			for (File f : fol.listFiles()) {
				//if (nForThisLetter++ >= 200) { break; }
				nForThisLetter++;
				if (f.getName().endsWith(".png")) {
					try {
						double offset = 0.0;
						double size = 1.0;
						String num = f.getName().substring(0, f.getName().length() - 4);
						if (offsets.get(fol.getName()).containsKey(num)) {
							offset = offsets.get(fol.getName()).get(num);
						} else {
							offset = offsetLists.get(fol.getName()).get(
									rnd.nextInt(offsetLists.get(fol.getName()).size()));
						}
						if (sizes.get(fol.getName()).containsKey(num)) {
							size = sizes.get(fol.getName()).get(num);
						} else {
							size = sizeLists.get(fol.getName()).get(
									rnd.nextInt(sizeLists.get(fol.getName()).size()));
						}
						BufferedImage img = ImageIO.read(f);
						(nForThisLetter % 2 == 0 && nForThisLetter < 500 ? train : test).add(
								new Example(fol.getName(), getInputForNN(img, offset, size), null));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		System.out.println("Loaded images.");
		
		// Do self-similarity runs to determine weights;
		/*
		for (Example n : train) {
			double[] withinDistances = new double[SIZE*SIZE + 3];
			int count = 0;
			for (Example n2 : train) {
				if (n2.letter.equals(n.letter) && n != n2) {
					count++;
					for (int i = 0; i < SIZE*SIZE + 3; i++) {
						withinDistances[i] += (n.input[i] - n2.input[i]) * (n.input[i] - n2.input[i]);
					}
				}
			}
			for (int i = 0; i < SIZE*SIZE + 3; i++) {
				withinDistances[i] = 1 / (withinDistances[i] / count + 1);
			}
			n.target = new double[SIZE*SIZE + 3];
			double sum = 0;
			for (int i = 0; i < SIZE*SIZE + 3; i++) {
				sum += withinDistances[i];
			}
			for (int i = 0; i < SIZE*SIZE + 3; i++) {
				n.target[i] = withinDistances[i] * (SIZE*SIZE + 3) / sum;
			}
		}
		 * 
		 */
		
		// Run clustering.
		HashMap<String, ArrayList<Example>> letterToTrains = new HashMap<String, ArrayList<Example>>();
		for (Example e : train) {
			if (!letterToTrains.containsKey(e.letter)) {
				letterToTrains.put(e.letter, new ArrayList<Example>());
			}
			letterToTrains.get(e.letter).add(e);
		}
		double[][][] clusters = new double[LETTERS.length][CLUSTERS][SIZE*SIZE + 3];
		double[][][] clusterSums = new double[LETTERS.length][CLUSTERS][SIZE*SIZE + 3];
		int[][] clusterNums = new int[LETTERS.length][CLUSTERS];
		// Init clusters.
		for (int l = 0; l < LETTERS.length; l++) {
			for (int c = 0; c < CLUSTERS; c++) {
				for (int i = 0; i < SIZE*SIZE + 3; i++) {
					clusters[l][c][i] = rnd.nextDouble();
				}
			}
		}
		for (int iteration = 0; iteration < CLUSTER_LOOPS; iteration++) {
			for (int l = 0; l < LETTERS.length; l++) {
				ArrayList<Example> trains = letterToTrains.get(letterToFilename(LETTERS[l]));
				if (trains.size() <= CLUSTERS) {
					// Fewer elements than clusters: just copy them over.
					for (int c = 0; c < CLUSTERS; c++) {
						System.arraycopy(trains.get(c % trains.size()).input, 0, clusters[l][c], 0,
								SIZE*SIZE + 3);
					}
				} else {
					// More elements than clusters.
					// Clear cluster averaging info.
					for (int c = 0; c < CLUSTERS; c++) {
						clusterNums[l][c] = 0;
						for (int i = 0; i < SIZE*SIZE + 3; i++) {
							clusterSums[l][c][i] = 0;
						}
					}
					// Get data for new cluster position.
					for (Example ex : trains) {
						// Find closest cluster.
						int closest = 0;
						double closestDist = 1000000;
						for (int c = 0; c < CLUSTERS; c++) {
							double d = dist(ex, clusters[l][c]);
							if (d < closestDist) {
								closest = c;
								closestDist = d;
							}
						}
						
						// Add to that cluster.
						clusterNums[l][closest]++;
						for (int i = 0; i < SIZE*SIZE + 3; i++) {
							clusterSums[l][closest][i] += ex.input[i];
						}
					}
					// Shift clusters.
					for (int c = 0; c < CLUSTERS; c++) {
						for (int i = 0; i < SIZE*SIZE + 3; i++) {
							clusters[l][c][i] = clusterSums[l][c][i] / clusterNums[l][c];
						}
					}
				}
			}
		}
		
		
		int hits = 0;
		int misses = 0;
		int acceptedHits = 0;
		int rejectedHits = 0;
		int acceptedMisses = 0;
		int rejectedMisses = 0;
		for (Example t : test) {
			/*String result = null;
			double best = 10000000;
			for (int l = 0; l < LETTERS.length; l++) {
				for (int c = 0; c < CLUSTERS; c++) {
					double d = dist(t, clusters[l][c]);
					if (d < best) {
						result = letterToFilename(LETTERS[l]);
						best = d;
					}
				}
			}*/
			/*for (Example n : train) {
				double d = dist(n, t);
				if (d < best) {
					result = n.letter;
					best = d;
				}
			}*/
			
			ArrayList<DistCluster> dis = new ArrayList<DistCluster>();
			for (int l = 0; l < LETTERS.length; l++) {
				for (int c = 0; c < CLUSTERS; c++) {
					double d = dist(t, clusters[l][c]);
					if (!Double.isNaN(d)) {
						dis.add(new DistCluster(letterToFilename(LETTERS[l]), d));
					}
				}
			}
			
			//System.out.println("Dissize: " + dis.size());
			
			Collections.sort(dis);
			HashMap<String, Double> votes = new HashMap<String, Double>();
			for (int i = 0; i < Math.min(dis.size(), K); i++) {
				String v = dis.get(i).l;
				double weight = 1 / (dis.get(i).d + 1);
				if (!votes.containsKey(v)) {
					votes.put(v, weight);
				} else {
					votes.put(v, votes.get(v) + weight);
				}
			}
			
			//System.out.println(votes.values().iterator().next());
			
			String result = null;
			double highestVote = 0;
			for (Entry<String, Double> v : votes.entrySet()) {
				if (v.getValue() > highestVote) {
					result = v.getKey();
					highestVote = v.getValue();
				}
			}
						
			if (t.letter.equals(result) || (t.letter + "-uc").equals(result) || t.letter.equals(result + "-uc")) {
				hits++;
				/*if (best <= ACCEPTANCE_D) {
					acceptedHits++;
				} else {
					rejectedHits++;
				}*/
			} else {
				misses++;
				System.out.println(t.letter + " as " + result);
				/*if (best <= ACCEPTANCE_D) {
					acceptedMisses++;
					System.out.println(" A");
				} else {
					rejectedMisses++;
					System.out.println(" R");
				}*/
			}
		}
		
		System.out.println("Hits " + hits);
		System.out.println("Misses " + misses);
		/*System.out.println("Acceptance boundary " + ACCEPTANCE_D);
		System.out.println("Accepted Hits " + acceptedHits);
		System.out.println("Rejected Hits " + rejectedHits);
		System.out.println("Accepted Misses " + acceptedMisses);
		System.out.println("Rejected Misses " + rejectedMisses);(*/
		System.out.println((hits * 100 / (hits + misses)) + "%");
		
		//ps.close();
	}
	
	static class DistCluster implements Comparable<DistCluster> {
		final String l;
		final double d;

		public DistCluster(String l, double d) {
			this.l = l;
			this.d = d;
		}

		@Override
		public int compareTo(DistCluster t) {
			return
					  d < t.d
					? -1
					: d > t.d
					? 1
					: 0;
		}
	}
	
	static double dist(Example a, Example b) {
		double ds = 0.0;
		for (int i = 0; i < a.input.length; i++) {
			ds += (a.input[i] - b.input[i]) * (a.input[i] - b.input[i]);
		}
		return Math.sqrt(ds);
	}
	
	static double wdist(Example train, Example test) {
		double ds = 0.0;
		for (int i = 0; i < train.input.length; i++) {
			ds += (train.input[i] - test.input[i]) * (train.input[i] - test.input[i]) * train.target[i];
		}
		return Math.sqrt(ds);
	}
	
	static double dist(Example ex, double[] cluster) {
		double ds = 0.0;
		for (int i = 0; i < ex.input.length; i++) {
			ds += (ex.input[i] - cluster[i]) * (ex.input[i] - cluster[i]);
		}
		return Math.sqrt(ds);
	}
	
	static double mdist(Example a, Example b) {
		double ds = 0.0;
		for (int i = 0; i < a.input.length; i++) {
			ds += Math.abs(a.input[i] - b.input[i]);
		}
		return ds;
	}
	
	static boolean done = false;
	
	static double[] getInputForNN(BufferedImage src, double offset, double size) {
		BufferedImage scaledSrc = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
		Graphics g = scaledSrc.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, SIZE, SIZE);
		g.drawImage(src, 0, 0, SIZE, SIZE, 0, 0, src.getWidth(), src.getHeight(), null);
		double aspect = Math.log(((double) src.getWidth()) / src.getHeight());
		double[] result = new double[SIZE * SIZE + 3];
		result[SIZE*SIZE] = aspect * SIZE * SIZE / 20.0;
		result[SIZE*SIZE + 1] = offset * SIZE * SIZE / 20.0;
		result[SIZE*SIZE + 2] = size;
		src = scaledSrc;
		for (int y = 0; y < SIZE; y++) { for (int x = 0; x < SIZE; x++) {
			Color c = new Color(src.getRGB(x, y));
			result[y * SIZE + x] = (c.getRed() + c.getGreen() + c.getBlue()) / 255.0 / 1.5 - 1;
		} }
		return result;
	}
}
