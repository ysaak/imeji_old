package ysaak.imeji.data;

import java.util.Objects;

public class Color {
	private long red;
	private long green;
	private long blue;

	private double xt2;
	private double xt;

	public Color() {
	}

	public Color(java.awt.Color color) {
		this(color.getRed(), color.getGreen(), color.getBlue());
	}

	public Color(long red, long green, long blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;

		xt2 = (Math.pow(red, 2) + Math.pow(green, 2) + Math.pow(blue, 2));
		xt = Math.sqrt(xt2);
	}

	public long getRed() {
		return red;
	}

	public void setRed(long red) {
		this.red = red;
	}

	public long getGreen() {
		return green;
	}

	public void setGreen(long green) {
		this.green = green;
	}

	public long getBlue() {
		return blue;
	}

	public void setBlue(long blue) {
		this.blue = blue;
	}

	public double getXt2() {
		return xt2;
	}

	public void setXt2(double xt2) {
		this.xt2 = xt2;
	}

	public double getXt() {
		return xt;
	}

	public void setXt(double xt) {
		this.xt = xt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Color color = (Color) o;
		return red == color.red &&
				green == color.green &&
				blue == color.blue &&
				Double.compare(color.xt2, xt2) == 0 &&
				Double.compare(color.xt, xt) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(red, green, blue, xt2, xt);
	}

	@Override
	public String toString() {
		return "Color{" +
				"red=" + red +
				", green=" + green +
				", blue=" + blue +
				", xt2=" + xt2 +
				", xt=" + xt +
				'}';
	}
}
