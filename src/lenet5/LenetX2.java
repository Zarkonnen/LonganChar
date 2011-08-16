package lenet5;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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

public class LenetX2 {
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
	
	static final List<String> CASE_MERGED = Arrays.asList(new String[] {
		"c", "m", "o", "p", "s", "u", "v", "w", "x", "z"
	});
	
	static final int OUTPUT_SIZE = 128;
		
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
	
	static final int THRESHOLD = 210;
	
	static BufferedImage rotate(BufferedImage in, double rotation) {
		BufferedImage out = new BufferedImage(in.getWidth() * 2, in.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = out.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, in.getWidth() * 2, in.getHeight() * 2);
		g.translate(in.getWidth(), in.getHeight());
		g.rotate(rotation);
		g.translate(-in.getWidth(), -in.getHeight());
		g.drawImage(in, 0, 0, null);
		
		// Box in
		int minX = 0;
		loop: while (minX < out.getWidth()) {
			for (int y = 0; y < out.getHeight(); y++) {
				Color c = new Color(out.getRGB(minX, y));
				int intensity = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
				if (intensity < THRESHOLD) {
					break loop;
				}
			}
			minX++;
		}
		if (minX > 0) { minX--; }
		int maxX = out.getWidth() - 1;
		loop: while (maxX >= 0) {
			for (int y = 0; y < out.getHeight(); y++) {
				Color c = new Color(out.getRGB(maxX, y));
				int intensity = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
				if (intensity < THRESHOLD) {
					break loop;
				}
			}
			maxX--;
		}
		if (maxX < out.getWidth() - 1) { maxX++; }
		int minY = 0;
		loop: while (minY < out.getHeight()) {
			for (int x = 0; x < out.getWidth(); x++) {
				Color c = new Color(out.getRGB(x, minY));
				int intensity = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
				if (intensity < THRESHOLD) {
					break loop;
				}
			}
			minY++;
		}
		if (minY > 0) { minY--; }
		int maxY = out.getHeight() - 1;
		loop: while (maxY >= 0) {
			for (int x = 0; x < out.getWidth(); x++) {
				Color c = new Color(out.getRGB(x, maxY));
				int intensity = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
				if (intensity < THRESHOLD) {
					break loop;
				}
			}
			maxY--;
		}
		if (maxY < out.getHeight() - 1) { maxY++; }
		
		BufferedImage o2 = new BufferedImage(maxX - minX, maxY - minY, BufferedImage.TYPE_INT_RGB);
		Graphics g2 = o2.getGraphics();
		g2.drawImage(out, 0, 0, o2.getWidth(), o2.getHeight(), minX, minY, maxX, maxY, null);
		/*try {
			ImageIO.write(o2, "jpg", new File("/Users/zar/Desktop/imaje.jpg"));
			System.exit(0);
		} catch (Exception e) {}*/
		
		return o2;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		PrintStream ps = new PrintStream(new File("/Users/zar/Desktop/out.txt"));
		System.setOut(ps);
		ArrayList<File> bExFolders = new ArrayList<File>();
		HashMap<String, DoubleArray> targets = new HashMap<String, DoubleArray>();
		Random r = new Random();
		
		for (String s : LETTERS) {			
			MessageDigest md = MessageDigest.getInstance("MD5");
			double[] data = new double[OUTPUT_SIZE];
			byte[] digest;
			if (CASE_MERGED.contains(s.toLowerCase())) {
				 digest = md.digest(s.toLowerCase().getBytes("UTF-8"));
			} else {
				 digest = md.digest(s.getBytes("UTF-8"));
			}
			if (s.equals("0")) {
				digest = md.digest("o".getBytes("UTF-8"));
			}
			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 8; j++) {
					data[i * 8 + j] = ((digest[i] >>> j) & 1) * 2 - 1;
				}
			}
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
			ArrayList<Example> examplesForThisLetter = new ArrayList<Example>();
			for (File f : fol.listFiles()) {
				if (nForThisLetter++ >= 200) { break; }
				if (f.getName().endsWith(".png")) {
					try {
						BufferedImage img = ImageIO.read(f);						
						Example ex = new Example(fol.getName(),
								getInputForNN(img), targets.get(fol.getName()).data);
						ex.original = img;
						examples.add(ex);
						examplesForThisLetter.add(ex);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// Top up
			int exOffset = 0;
			while (examplesForThisLetter.size() < 200) {
				examples.add(examplesForThisLetter.get(exOffset));
				examplesForThisLetter.add(examplesForThisLetter.get(exOffset));
				exOffset = (exOffset + 1) % examplesForThisLetter.size();
			}
			
			// Top up with rotations
			for (int i = 0; i < 400; i++) {
				Example ox = examplesForThisLetter.get(i % 200);
				BufferedImage rotated = rotate(ox.original, (r.nextDouble() - 0.5) * 2 / Math.PI);
				Example ex = new Example(ox.letter, getInputForNN(rotated), ox.target);
				examples.add(ex);
			}
			
			// clear buffered images
			for (Example e : examplesForThisLetter) { e.original = null; }
		}
		Collections.shuffle(examples);

		System.out.println("Loaded images and convolved data.");
		
		Lenet5ishNet network = new Lenet5ishNet();
		System.out.println("Nodes: " + network.nw.numNodes());
		System.out.println("Weights: " + network.nw.numWeights());
		
		if (args[0].startsWith("train")) {
			for (int rep = 0; rep < 20; rep++) {
				int to = args[0].equals("trainAndTest") ? examples.size() / 2 : examples.size();
				Collections.shuffle(examples);
				for (int i = 0; i < to; i++) {
					//network.train(examples.get(i), 0.0001, 0.00002);
					//network.train(examples.get(i), 0.001, 0.0002);
					network.train(examples.get(i), 0.002, 0.0005);
					//network.train(examples.get(i), 0.5, 0.15);
					if (i % 100 == 0) {
						System.out.println(i + "/" + to + "/" + rep);
					}
				}
			}
			System.out.println("Network trained.");
		} else {
			FileInputStream fis = new FileInputStream(new File(args[2]));
			NetworkIO.input(network.nw, fis);
			fis.close();
			System.out.println("Loaded network.");
		}
		
		if (args[0].equals("train")) {
			FileOutputStream fos = new FileOutputStream(new File(args[2]));
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
				bestScoringLetter.equals(example.letter + "-uc") ||
				bestScoringLetter.equals("0") && example.letter.equals("o") ||
				bestScoringLetter.equals("o") && example.letter.equals("0"))
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
	
	static double[] getInputForNN(BufferedImage src) {
		BufferedImage scaledSrc = new BufferedImage(28, 28, BufferedImage.TYPE_INT_RGB);
		Graphics g = scaledSrc.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 28, 28);
		int width = 0;
		int xOffset = 0;
		int height = 0;
		int yOffset = 0;
		if (src.getWidth() > src.getHeight()) {
			width = 16;
			height = 16 * src.getHeight() / src.getWidth();
			yOffset = (16 - height) / 2;
		} else {
			height = 16;
			width = 16 * src.getWidth() / src.getHeight();
			xOffset = (16 - width) / 2;
		}
		g.drawImage(src, 6 + xOffset, 6 + yOffset, 6 + xOffset + width, 6 + yOffset + height, 0, 0, src.getWidth(), src.getHeight(), null);
		src = scaledSrc;
		double[] result = new double[28 * 28];
		for (int y = 0; y < 28; y++) { for (int x = 0; x < 28; x++) {
			Color c = new Color(src.getRGB(x, y));
			result[y * 28 + x] = (c.getRed() + c.getGreen() + c.getBlue()) / 255.0 / 1.5 - 1;
		} }
		return result;
	}
}
