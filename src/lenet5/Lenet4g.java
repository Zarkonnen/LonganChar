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
import java.util.Random;
import javax.imageio.ImageIO;

public class Lenet4g {
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
	
	static final int OUTPUT_SIZE = 64;
		
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
	
	public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		Random rnd = new Random();
		PrintStream ps = new PrintStream(new File("/Users/zar/Desktop/out.txt"));
		System.setOut(ps);
		ArrayList<File> bExFolders = new ArrayList<File>();
		HashMap<String, HashMap<String, Double>> offsets = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, ArrayList<Double>> offsetLists = new HashMap<String, ArrayList<Double>>();
		HashMap<String, HashMap<String, Double>> sizes = new HashMap<String, HashMap<String, Double>>();
		HashMap<String, ArrayList<Double>> sizeLists = new HashMap<String, ArrayList<Double>>();
		HashMap<String, DoubleArray> targets = new HashMap<String, DoubleArray>();
		
		int lIndex = 0;
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
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			double[] data = new double[OUTPUT_SIZE];
			byte[] digest = md.digest(s.getBytes("UTF-8"));
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					data[i * 8 + j] = (digest[i] >>> j) & 1;
				}
			}
			//System.out.println(s + Arrays.toString(data));
			targets.put(letterToFilename(s),
				new DoubleArray(data)
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
		
		Lenet4hNet network = new Lenet4hNet();
		System.out.println("Nodes: " + network.nw.numNodes());
		System.out.println("Weights: " + network.nw.numWeights());
		
		if (args[0].startsWith("train")) {
			for (int rep = 0; rep < 30; rep++) {
				int to = args[0].equals("trainAndTest") ? examples.size() / 2 : examples.size();
				for (int i = 0; i < to; i++) {
					//network.train(examples.get(i), 0.0001, 0.00002);
					//network.train(examples.get(i), 0.001, 0.0002);
					network.train(examples.get(i), 0.002, 0.0005);
					if (i % 100 == 0) {
						System.out.println(i + "/" + to + "/" + rep);
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
		}
		
		System.out.println("Testing");
		int hits = 0;
		int misses = 0;
		
		System.out.println(System.currentTimeMillis());
		for (int i = examples.size() / 2; i < examples.size(); i++) {
			Example example = examples.get(i);
			double[] result = network.run(example.input);
			String bestScoringLetter = null;
			double leastError = 0;
			double errorForCorrectLetter = -1;
			for (String letter : LETTERS) {
				double[] target = targets.get(letterToFilename(letter)).data;
				double error = 0.0;
				for (int j = 0; j < OUTPUT_SIZE; j++) {
					error += (result[j] - target[j]) * (result[j] - target[j]);
				}
				if (bestScoringLetter == null || leastError > error) {
					bestScoringLetter = letterToFilename(letter);
					leastError = error;
				}
				if (letterToFilename(letter).equals(example.letter)) {
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
		System.out.println(System.currentTimeMillis());
		
		System.out.println("Hits: " + hits);
		System.out.println("Misses: " + misses);
		
		ps.close();
	}
	
	static boolean done = false;
	
	static double[] getInputForNN(BufferedImage src, double size, double offset) {
		BufferedImage scaledSrc = new BufferedImage(14, 14, BufferedImage.TYPE_INT_RGB);
		Graphics g = scaledSrc.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 14, 14);
		int width = 10;
		int xOffset = 0;
		int height = 10;
		int yOffset = 0;
		/*if (src.getWidth() > src.getHeight()) {
			width = 10;
			height = 10 * src.getHeight() / src.getWidth();
			yOffset = (10 - height) / 2;
		} else {
			height = 10;
			width = 10 * src.getWidth() / src.getHeight();
			xOffset = (10 - width) / 2;
		}*/
		g.drawImage(src, 2 + xOffset, 2 + yOffset, 2 + xOffset + width, 2 + yOffset + height, 0, 0, src.getWidth(), src.getHeight(), null);
		if (!done) { done = true;
			try {
				ImageIO.write(scaledSrc, "png", new File("/Users/zar/Desktop/ex.png"));
			} catch (Exception e) {}
		}
		src = scaledSrc;
		double[] result = new double[14 * 14];
		for (int y = 0; y < 14; y++) { for (int x = 0; x < 14; x++) {
			Color c = new Color(src.getRGB(x, y));
			result[y * 14 + x] = (c.getRed() + c.getGreen() + c.getBlue()) / 255.0 / 1.5 - 1;
		} }
		return result;
	}
}
