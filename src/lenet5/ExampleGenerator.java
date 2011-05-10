package lenet5;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ExampleGenerator {
	static final int LARGE_IMG_SIZE = 40;
	static final int IMG_SIZE = 20;
	
	public static void main(String[] args) {
		String l = args[0];
		File targetF = new File(args[1]);
		targetF.mkdirs();
		Font[] fs = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (int i = 0; i < fs.length; i++) {
			try {
				ImageIO.write(makeLetterImage(l, fs[i].getFontName()), "png", new File(targetF, i + ".png"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static BufferedImage makeLetterImage(String l, String fontName) {
		BufferedImage img = new BufferedImage(LARGE_IMG_SIZE, LARGE_IMG_SIZE, BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = img.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, LARGE_IMG_SIZE, LARGE_IMG_SIZE);
		g.setColor(Color.BLACK);
		g.setFont(new Font(fontName, Font.PLAIN, LARGE_IMG_SIZE / 2));
		g.drawString(l, LARGE_IMG_SIZE / 4, LARGE_IMG_SIZE / 2);

		int minY = 0;
		outer: while (minY < LARGE_IMG_SIZE) {
			for (int x = 0; x < LARGE_IMG_SIZE; x++) {
				if (!new Color(img.getRGB(x, minY)).equals(Color.WHITE)) {
					break outer;
				}
			}
			minY++;
		}

		int maxY = LARGE_IMG_SIZE - 1;
		outer: while (maxY >= 0) {
			for (int x = 0; x < LARGE_IMG_SIZE; x++) {
				if (!new Color(img.getRGB(x, maxY)).equals(Color.WHITE)) {
					break outer;
				}
			}
			maxY--;
		}

		int minX = 0;
		outer: while (minX < LARGE_IMG_SIZE) {
			for (int y = 0; y < LARGE_IMG_SIZE; y++) {
				if (!new Color(img.getRGB(minX, y)).equals(Color.WHITE)) {
					break outer;
				}
			}
			minX++;
		}

		int maxX = LARGE_IMG_SIZE - 1;
		outer: while (maxX >= 0) {
			for (int y = 0; y < LARGE_IMG_SIZE; y++) {
				if (!new Color(img.getRGB(maxX, y)).equals(Color.WHITE)) {
					break outer;
				}
			}
			maxX--;
		}

		BufferedImage img2 = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_BYTE_GRAY);
		img2.getGraphics().drawImage(img,
				0, 0, IMG_SIZE, IMG_SIZE, minX - 1, minY - 1, maxX + 2, maxY + 2, null);
		return img2;
	}
}
