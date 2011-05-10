package lenet5;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class ABTest {
	public static void main(String[] args) throws IOException {
		File aClass = new File(args[0]);
		File aExFolder = new File(args[1]);
		File aOutFolder = new File(args[2]);
		File bClass = new File(args[3]);
		File bExFolder = new File(args[4]);
		File bOutFolder = new File(args[5]);
		
		BufferedImage aClassImg = ImageIO.read(aClass);
		ArrayList<BufferedImage> aExs = new ArrayList<BufferedImage>();
		for (File f : aExFolder.listFiles()) {
			if (f.getName().endsWith(".png")) {
				try {
					aExs.add(ImageIO.read(f));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (aExs.size() == 10) { break; } // qqDPS
		}
		BufferedImage[] aExamples = aExs.toArray(new BufferedImage[0]);
		
		BufferedImage bClassImg = ImageIO.read(bClass);
		ArrayList<BufferedImage> bExs = new ArrayList<BufferedImage>();
		for (File f : bExFolder.listFiles()) {
			if (f.getName().endsWith(".png")) {
				try {
					bExs.add(ImageIO.read(f));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (bExs.size() == 10) { break; } // qqDPS
		}
		BufferedImage[] bExamples = bExs.toArray(new BufferedImage[0]);
		
		BufferedImage[] classes = {aClassImg, bClassImg};
		BufferedImage[][] examples = {aExamples, bExamples};
		System.out.println("Loaded images");
				
		double[][] rClasses = Lenet5.rasterClasses(classes);
		System.out.println("Rastered classes");
		double[][][] rExamples = Lenet5.rasterData(examples);
		System.out.println("Rastered data");
		
		Lenet5 lenet5 = new Lenet5();
		System.out.println("Created Lenet 5");
		
		lenet5.train(rClasses, rExamples, 0.001, 0.002, 20);
		System.out.println("Trained Lenet 5");
		
		aOutFolder.mkdirs();
		for (int i = 0; i < rExamples[0].length; i++) {
			double[] output = lenet5.nw.run(rExamples[0][i]);
			BufferedImage img = new BufferedImage(7, 12, BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < 12; y++) {
				for (int x = 0; x < 7; x++) {
					int normVal = Math.min(255, Math.max(0, (int) (127 - output[y * 7 + x] * 127)));
					img.setRGB(x, y, new Color(normVal, normVal, normVal).getRGB());
				}
			}
			ImageIO.write(img, "png", new File(aOutFolder, i + ".png"));
		}
		
		bOutFolder.mkdirs();
		for (int i = 0; i < rExamples[1].length; i++) {
			double[] output = lenet5.nw.run(rExamples[1][i]);
			BufferedImage img = new BufferedImage(7, 12, BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < 12; y++) {
				for (int x = 0; x < 7; x++) {
					int normVal = Math.min(255, Math.max(0, (int) (127 - output[y * 7 + x] * 127)));
					img.setRGB(x, y, new Color(normVal, normVal, normVal).getRGB());
				}
			}
			ImageIO.write(img, "png", new File(bOutFolder, i + ".png"));
		}
		
		/*
		System.out.println("Error for A examples (a vs b):");
		for (double[] aEx : rExamples[0]) {
			double[] result = lenet5.classify(rClasses, aEx);
			System.out.println(result[0] + " vs " + result[1]);
		}
		
		System.out.println("Error for B examples (b vs a):");
		for (double[] aEx : rExamples[1]) {
			double[] result = lenet5.classify(rClasses, aEx);
			System.out.println(result[1] + " vs " + result[0]);
		}*/
	}
}
