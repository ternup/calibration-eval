package org.akvo.caddisfly.util;

public class Color extends java.awt.Color {

    public static int TRANSPARENT = 0;

    public static int BLACK = java.awt.Color.black.getRGB();

    public Color(int r, int g, int b) {
        super(r, g, b);
    }

    public Color(int r, int g, int b, int a) {
        super(r, g, b, a);
    }

    public Color(int rgb) {
        super(rgb);
    }

    public Color(int rgba, boolean hasalpha) {
        super(rgba, hasalpha);
    }

    public Color(float r, float g, float b) {
        super(r, g, b);
    }

    public Color(float r, float g, float b, float a) {
        super(r, g, b, a);
    }

    public static int red(int color) {
        return new Color(color).getRed();
    }

    public static int blue(int color) {
        return new Color(color).getBlue();
    }

    public static int green(int color) {
        return new Color(color).getGreen();
    }

    public static int rgb(int r, int g, int b) {
        return new Color(r, g, b).getRGB();
    }

    public static void RGBToHSV(int red, int green, int blue, float[] hsvColor) {
        RGBtoHSB(red, green, blue, hsvColor);
    }

    public static int HSVToColor(float[] hsvColor) {
        return HSBtoRGB(hsvColor[0], hsvColor[1], hsvColor[2]);
    }

    public static void colorToHSV(int color, float[] colorHSV) {
        RGBtoHSB(Color.red(color), Color.green(color), Color.blue(color), colorHSV);
    }
}
