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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;

public class UniNet {
	static final int NUM_NETWORKS = 3;
	
	static final double[][][] kernels = {
		// Identity
		{
			{ 0,  0,  0 },
			{ 0,  1,  0 },
			{ 0,  0,  0 }
		},
		// c/o detector (vgap)
		{
			{-1,  4, -1 },
			{-1, -2, -1 },
			{-1,  4, -1 }
		},
		// weird-ass kernel
		{
			{-2,  4,  2 },
			{-4,  0,  4 },
			{ 2, -4, -2 }
		},
	};
	
	static class Example {
		public String letter;
		public double[] input;
		public double[] target;

		public Example(String letter, double[] input, double[] target) {
			this.letter = letter;
			this.input = input;
			this.target = target;
		}
	}
	
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
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Random rnd = new Random();
		PrintStream ps = new PrintStream(new File("/Users/zar/Desktop/out.txt"));
		System.setOut(ps);
		ArrayList<File> bExFolders = new ArrayList<File>();
		HashMap<String, HashMap<String, Double>> offsets = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, ArrayList<Double>> offsetLists = new HashMap<String, ArrayList<Double>>();
		HashMap<String, HashMap<String, Double>> sizes = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, ArrayList<Double>> sizeLists = new HashMap<String, ArrayList<Double>>();
		HashMap<String, DoubleArray> targets = new HashMap<String, DoubleArray>();
		
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
			
			targets.put(letterToFilename(s),
				new DoubleArray(getOutputForNN(
					ImageIO.read(new File(new File(args[2]), letterToFilename(s) + ".png"))
				))
			);
		}
		System.out.println("Loaded offsets, sizes and targets.");
		
		for (String s : LETTERS) {
			bExFolders.add(new File(new File(args[1]), letterToFilename(s)));
		}
		
		ArrayList<Example> examples = new ArrayList<Example>();
		
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
				if (nForThisLetter++ >= 200) { break; }
				if (f.getName().endsWith(".png")) {
					try {
						BufferedImage img = ImageIO.read(f);
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
						
						examples.add(new Example(fol.getName(),
								getInputForNN(img, size, offset), targets.get(fol.getName()).data));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		Collections.shuffle(examples);

		
		System.out.println("Loaded images and convolved data.");
		
		ArrayList<UniNetwork> networks = new ArrayList<UniNetwork>();
		
		ArrayList<Example> training = new ArrayList<Example>(examples.size() / 2);
		training.addAll(examples.subList(0, examples.size() / 2));
	
		for (int n = 0; n < NUM_NETWORKS; n++) {
			UniNetwork network = new UniNetwork();
			networks.add(network);
			for (int rep = 0; rep < 10; rep++) {
				Collections.shuffle(training);
				for (Example e : training) {
					network.train(e, 0.001, 0.0002);
				}
			}
			System.out.println("Network trained.");
		}
		
		/*if (args[0].startsWith("train")) {
			for (int rep = 0; rep < 10; rep++) {
				int to = args[0].equals("trainAndTest") ? examples.size() / 2 : examples.size();
				for (int i = 0; i < to; i++) {
					network.train(examples.get(i), 0.001, 0.0002);
					if (i % 100 == 0) {
						System.out.println(i);
					}
				}
			}
			System.out.println("Network trained.");
		} else {
			FileInputStream fis = new FileInputStream(new File(args[3]));
			NetworkIO.input(network.nw, fis);
			fis.close();
			System.out.println("Loaded network.");
		}
		
		if (args[0].equals("train")) {
			FileOutputStream fos = new FileOutputStream(new File(args[3]));
			NetworkIO.output(network.nw, fos);
			fos.close();
			return;
		}*/
		
		System.out.println("Testing");
		int hits = 0;
		int misses = 0;
		
		for (int i = examples.size() / 2; i < examples.size(); i++) {
			Example example = examples.get(i);
			String bestScoringLetter = null;
			double leastError = 1000000;
			double errorForCorrectLetter = -1;
			//double[] result = network.run(example.input);
			double[][] result = new double[NUM_NETWORKS][0];
			for (int r = 0; r < NUM_NETWORKS; r++) {
				result[r] = networks.get(r).run(example.input);
			}
			for (Map.Entry<String, DoubleArray> t : targets.entrySet()) {
				double[] target = t.getValue().data;
				double error = 0.0;
				for (int r = 0; r < NUM_NETWORKS; r++) {
					for (int j = 0; j < result.length; j++) {
						error += (result[r][j] - target[j]) * (result[r][j] - target[j]);
					}
				}
				if (bestScoringLetter == null || error < leastError) {
					bestScoringLetter = t.getKey();
					leastError = error;
				}
				if (example.letter.equals(t.getKey())) {
					errorForCorrectLetter = error;
				}
			}
			
			if (bestScoringLetter.equals(example.letter) ||
				(bestScoringLetter + "-uc").equals(example.letter) ||
				bestScoringLetter.equals(example.letter + "-uc"))
			{
				hits++;
			} else {
				System.out.println("Mis-identified " + example.letter + " as " +
						bestScoringLetter + " with an error of " + leastError + " vs " +
						errorForCorrectLetter + ".");
				misses++;
			}
		}
		
		System.out.println("Hits: " + hits);
		System.out.println("Misses: " + misses);
		
		ps.close();
	}
	
	static double[] getInputForNN(BufferedImage src, double size, double offset) {
		BufferedImage scaledSrc = new BufferedImage(14, 14, BufferedImage.TYPE_INT_RGB);
		Graphics g = scaledSrc.getGraphics();
		g.setColor(Color.WHITE);
		g.drawImage(src, 2, 2, 12, 12, 0, 0, src.getWidth(), src.getHeight(), null);
		src = scaledSrc;
		double[] result = new double[kernels.length * 12 * 12 + 3];
		for (int y = 0; y < 12; y++) { for (int x = 0; x < 12; x++) {
			for (int kdy = 0; kdy < 3; kdy++) { for (int kdx = 0; kdx < 3; kdx++) {
				Color c = new Color(src.getRGB(x + kdx, y + kdy));
				double intensity = (c.getRed() + c.getGreen() + c.getBlue()) / 255.0 / 3.0;
				for (int k = 0; k < kernels.length; k++) {
					result[k * 144 + y * 12 + x] += intensity * kernels[k][kdy][kdx];
				}
			} }
		} }
		for (int i = 0; i < 144; i++) {
			result[i] = result[i] * 2 - 1;
		}
		result[result.length - 3] = Math.log(src.getWidth() / ((double) src.getHeight())) * 2;
		result[result.length - 2] = Math.log(size) * 2;
		result[result.length - 1] = offset * 5;
		return result;
	}
	
	static double[] getOutputForNN(BufferedImage img) {
		double[] result = new double[9 * 5];
		for (int y = 0; y < 9; y++) {
			for (int x = 0; x < 5; x++) {
				Color c = new Color(img.getRGB(x, y));
				result[y * 5 + x] = (c.getRed() + c.getGreen() + c.getBlue()) / 255.0 / 1.5 - 1;
			}
		}
		return result;
	}
}
