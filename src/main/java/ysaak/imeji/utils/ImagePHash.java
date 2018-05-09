package ysaak.imeji.utils;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.math.BigInteger;

/**
 * pHash-like image hash.
 * Author: Elliot Shepherd (elliot@jarofworms.com
 * Based On: http://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html
 */
public class ImagePHash {

	public static long hash(BufferedImage image) {
		return new ImagePHash().getHash(image);

	}

	public static double calculateSimilarity(long hash1, long hash2) {
		final BigInteger bigHash1 = BigInteger.valueOf(hash1);
		final BigInteger bigHash2 = BigInteger.valueOf(hash2);
		return 1 - (bigHash1.xor(bigHash2).bitCount() / 64.0);
	}


	private static final int SIZE = 32;
	private static final int SMALLER_SIZE = 8;
	private double[] c;

	public ImagePHash() {
		initCoefficients();
	}

	// Returns a 'binary string' (like. 001010111011100010) which is easy to do a hamming distance on.
	private long getHash(BufferedImage image) {
		/* 1. Reduce SIZE.
		 * Like Average Hash, pHash starts with a small image.
		 * However, the image is larger than 8x8; 32x32 is a good SIZE.
		 * This is really done to simplify the DCT computation and not
		 * because it is needed to reduce the high frequencies.
		 */
		BufferedImage img = resize(image, SIZE, SIZE);

		/* 2. Reduce colorthief.
		 * The image is reduced to a greyscale just to further simplify
		 * the number of computations.
		 */
		img = grayscale(img);

		double[][] vals = new double[SIZE][SIZE];

		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				vals[x][y] = getBlue(img, x, y);
			}
		}

		/* 3. Compute the DCT.
		 * The DCT separates the image into a collection of frequencies
		 * and scalars. While JPEG uses an 8x8 DCT, this algorithm uses
		 * a 32x32 DCT.
		 */
		double[][] dctVals = applyDCT(vals);

		/* 4. Reduce the DCT.
		 * This is the magic step. While the DCT is 32x32, just keep the
		 * top-left 8x8. Those represent the lowest frequencies in the
		 * picture.
		 */
		/* 5. Compute the average value.
		 * Like the Average Hash, compute the mean DCT value (using only
		 * the 8x8 DCT low-frequency values and excluding the first term
		 * since the DC coefficient can be significantly different from
		 * the other values and will throw off the average).
		 */
		double total = 0;

		for (int x = 0; x < SMALLER_SIZE; x++) {
			for (int y = 0; y < SMALLER_SIZE; y++) {
				total += dctVals[x][y];
			}
		}
		total -= dctVals[0][0];

		double avg = total / (double) ((SMALLER_SIZE * SMALLER_SIZE) - 1);

		/* 6. Further reduce the DCT.
		 * This is the magic step. Set the 64 hash bits to 0 or 1
		 * depending on whether each of the 64 DCT values is above or
		 * below the average value. The result doesn't tell us the
		 * actual low frequencies; it just tells us the very-rough
		 * relative scale of the frequencies to the mean. The result
		 * will not vary as long as the overall structure of the image
		 * remains the same; this can survive gamma and colorthief histogram
		 * adjustments without a problem.
		 */
		StringBuilder hash = new StringBuilder();

		for (int x = 0; x < SMALLER_SIZE; x++) {
			for (int y = 0; y < SMALLER_SIZE; y++) {
				if (x != 0 && y != 0) {
					hash.append(dctVals[x][y] > avg ? "1" : "0");
				}
			}
		}

		return Long.valueOf(hash.toString(), 2);
	}

	private BufferedImage resize(BufferedImage image, int width,    int height) {
		BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}

	private ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

	private BufferedImage grayscale(BufferedImage img) {
		colorConvert.filter(img, img);
		return img;
	}

	private static int getBlue(BufferedImage img, int x, int y) {
		return (img.getRGB(x, y)) & 0xff;
	}

	private void initCoefficients() {
		c = new double[SIZE];

		for (int i = 1; i< SIZE; i++) {
			c[i]=1;
		}
		c[0]=1/Math.sqrt(2.0);
	}

	// DCT function stolen from http://stackoverflow.com/questions/4240490/problems-with-dct-and-idct-algorithm-in-java
	private double[][] applyDCT(double[][] f) {
		int N = SIZE;

		double[][] F = new double[N][N];
		for (int u=0;u<N;u++) {
			for (int v=0;v<N;v++) {
				double sum = 0.0;
				for (int i=0;i<N;i++) {
					for (int j=0;j<N;j++) {
						sum+=Math.cos(((2*i+1)/(2.0*N))*u*Math.PI)*Math.cos(((2*j+1)/(2.0*N))*v*Math.PI)*(f[i][j]);
					}
				}
				sum*=((c[u]*c[v])/4.0);
				F[u][v] = sum;
			}
		}
		return F;
	}
}
