package lenet5;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class MicroNet {
	static final double[][][] kernels = {
		// Hline
		{
			{-1, -1, -1 },
			{ 2,  2,  2 },
			{-1, -1, -1 }
		},
		// Vline
		{
			{-1,  2, -1 },
			{-1,  2, -1 },
			{-1,  2, -1 }
		},
		// / line
		{
			{-1, -1,  2 },
			{-1,  2, -1 },
			{ 2, -1, -1 }
		},
		// \ line
		{
			{ 2, -1, -1 },
			{-1,  2, -1 },
			{-1, -1,  2 }
		},
		// ^ end
		{
			{-1, -1, -1 },
			{-1,  2, -1 },
			{-1,  2, -1 }
		},
		// v end
		{
			{-1,  2, -1 },
			{-1,  2, -1 },
			{-1, -1, -1 }
		},
		// < end
		{
			{-1, -1, -1 },
			{-1,  2,  2 },
			{-1, -1, -1 }
		},
		// > end
		{
			{-1, -1, -1 },
			{ 2,  2, -1 },
			{-1, -1, -1 }
		},
		// ^ shape
		{
			{-1,  2, -1 },
			{ 2, -1,  2 },
			{ 0, -1,  0 }
		},
		// v shape
		{
			{ 0, -1,  0 },
			{ 2, -1,  2 },
			{-1,  2, -1 }
		},
		// < shape
		{
			{-1,  2,  0 },
			{ 2, -1, -1 },
			{-1,  2,  0 }
		},
		// > shape
		{
			{ 0,  2, -1 },
			{-1, -1,  2 },
			{ 0,  2, -1 }
		},
	};
	
	public static void main(String[] args) {
		File aExFolder = new File(args[0]);
		File bExFolder = new File(args[1]);
		
		ArrayList<BufferedImage> aExs = new ArrayList<BufferedImage>();
		for (File f : aExFolder.listFiles()) {
			if (f.getName().endsWith(".png")) {
				try {
					aExs.add(ImageIO.read(f));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (aExs.size() == 360) { break; } // qqDPS
		}
		
		ArrayList<BufferedImage> bExs = new ArrayList<BufferedImage>();
		for (File f : bExFolder.listFiles()) {
			if (f.getName().endsWith(".png")) {
				try {
					bExs.add(ImageIO.read(f));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (bExs.size() == 360) { break; } // qqDPS
		}
		
		System.out.println("Loaded images");
				
		double[][] as = new double[aExs.size()][0];
		for (int i = 0; i < as.length; i++) {
			as[i] = convolve(aExs.get(i));
		}
		double[][] bs = new double[bExs.size()][0];
		for (int i = 0; i < bs.length; i++) {
			bs[i] = convolve(bExs.get(i));
		}
		System.out.println("Convolved data");
		
		double[][] trainingAs = new double[as.length / 2][0];
		for (int i = 0; i < trainingAs.length; i++) {
			trainingAs[i] = as[i];
		}
		
		double[][] trainingBs = new double[bs.length / 2][0];
		for (int i = 0; i < trainingBs.length; i++) {
			trainingBs[i] = bs[i];
		}
		
		double[][] testAs = new double[as.length / 2][0];
		for (int i = 0; i < testAs.length; i++) {
			testAs[i] = as[as.length / 2 + i];
		}
		
		double[][] testBs = new double[bs.length / 2][0];
		for (int i = 0; i < testBs.length; i++) {
			testBs[i] = bs[bs.length / 2 + i];
		}
		
		MicroNetwork mn = new MicroNetwork();
		System.out.println("Created MN");
		for (int i = 0; i < 9; i++) {
			mn.train(trainingAs, trainingBs, 0.001, 0.0002);
			System.out.println("pass " + (i + 1) + " complete");
		}
		
		System.out.println("Trained MN");
		
		double err = 0.0;
		for (double[] a : testAs) {
			double result = mn.run(a);
			//System.out.println(result);
			err += Math.abs(1.0 - result);
		}
		System.out.println("Positives' average error: " + (err / testAs.length));
		
		err = 0.0;
		for (double[] b : testBs) {
			double result = mn.run(b);
			//System.out.println(result);
			err += Math.abs(result);
		}
		System.out.println("Negatives' average error: " + (err / testBs.length));
	}
	
	public static void main1(String[] args) throws IOException {
		BufferedImage src = ImageIO.read(new File(args[0]));
		BufferedImage scaledSrc = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB);
		Graphics g = scaledSrc.getGraphics();
		g.setColor(Color.WHITE);
		g.drawImage(src, 1, 1, 11, 11, 0, 0, src.getWidth(), src.getHeight(), null);
		/*for (int i = 0; i < kernels.length; i++) {
			ImageIO.write(convolve(scaledSrc, kernels[i]), "png", new File(new File(args[1]), i + ".png"));
		}*/
		double[] in = convolve(scaledSrc);
		BufferedImage out = new BufferedImage(5, 5 * kernels.length, BufferedImage.TYPE_INT_RGB);
		for (int k = 0; k < kernels.length; k++) {
			for (int y = 0; y < 5; y++) { for (int x = 0; x < 5; x++) {
				int rgbV = Math.min(255, Math.max(0, (int) (in[k * 25 + y * 5 + x])));
				Color c = new Color(rgbV, rgbV, rgbV);
				out.setRGB(x, k * 5 + y, c.getRGB());
			} }
		}
		ImageIO.write(out, "png", new File(args[1]));
	}
	
	static double[] convolve(BufferedImage src) {
		double[] result = new double[kernels.length * 5 * 5];
		for (int y = 0; y < 5; y++) { for (int x = 0; x < 5; x++) {
			double[][][] kernelResult = new double[kernels.length][2][2];
			for (int sdy = 0; sdy < 2; sdy++) { for (int sdx = 0; sdx < 2; sdx++) {
				for (int kdy = 0; kdy < 3; kdy++) { for (int kdx = 0; kdx < 3; kdx++) {
					Color c = new Color(src.getRGB(x + sdx + kdx, y + sdy + kdy));
					double intensity = (c.getRed() + c.getGreen() + c.getBlue()) / 255.0 / 3.0;
					for (int k = 0; k < kernels.length; k++) {
						kernelResult[k][sdy][sdx] += intensity * kernels[k][kdy][kdx];
					}
				} }
			} }
			for (int k = 0; k < kernels.length; k++) {
				double total = 0;
				for (int sdy = 0; sdy < 2; sdy++) { for (int sdx = 0; sdx < 2; sdx++) {
					total += kernelResult[k][sdy][sdx];// * kernelResult[k][sdy][sdx];
				} }
				result[k * 25 + y * 5 + x] = total;
			}
		} }
		return result;
	}
	
	static BufferedImage convolve(BufferedImage src, double[][] kernel) {
		BufferedImage dst = new BufferedImage(src.getWidth() - 2, src.getHeight() - 2,
				BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < src.getHeight() - 2; y++) {
			for (int x = 0; x < src.getWidth() - 2; x++) {
				double value = 0.0;
				for (int dy = 0; dy < 3; dy++) {
					for (int dx = 0; dx < 3; dx++) {
						Color c = new Color(src.getRGB(x + dx, y + dy));
						double intensity = (c.getRed() + c.getGreen() + c.getBlue()) / 255.0 / 3.0;
						value += intensity * kernel[dy][dx];
					}
				}
				int rgbV = Math.min(255, Math.max(0, (int) (value * value * 5)));
				Color c = new Color(rgbV, rgbV, rgbV);
				dst.setRGB(x, y, c.getRGB());
			}
		}
		return dst;
	}
}
