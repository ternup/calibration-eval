/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import org.akvo.caddisfly.model.HsvColor;
import org.akvo.caddisfly.model.LabColor;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.XyzColor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

/**
 * Set of utility functions for color calculations and analysis
 */
public final class ColorUtil {
    /**
     * The default color model used for analysis
     */
    public static final ColorModel DEFAULT_COLOR_MODEL = ColorModel.RGB;
    /**
     * The maximum color distance before the color is considered out of range
     */
    public static final int MAX_COLOR_DISTANCE_RGB = 30;
    private static final double Xn = 0.950470;
    private static final double Yn = 1.0;
    private static final double Zn = 1.088830;
    private static final double t0 = 0.137931034;  // 4 / 29;
    private static final double t1 = 0.206896552;  // 6 / 29;
    private static final double t2 = 0.12841855;   // 3 * t1 * t1;
    private static final double t3 = 0.008856452; // t1 * t1 * t1;
    private static final int MAX_COLOR_DISTANCE_LAB = 4;

    /**
     * The minimum color distance at which the colors are considered equivalent
     */
    private static final double MIN_COLOR_DISTANCE_RGB = 6;
    private static final double MIN_COLOR_DISTANCE_LAB = 1.2;

    /**
     * The color distance within which the sampled colors should be for a valid test
     */
    private static final double MAX_SAMPLING_COLOR_DISTANCE_RGB = 11;
    private static final double MAX_SAMPLING_COLOR_DISTANCE_LAB = 1.5;

    private ColorUtil() {
    }

    public static double getMinDistance() {
        switch (DEFAULT_COLOR_MODEL) {
            case RGB:
                return MIN_COLOR_DISTANCE_RGB;
            case LAB:
                return MIN_COLOR_DISTANCE_LAB;
            default:
                return MIN_COLOR_DISTANCE_RGB;
        }
    }

    public static double getMaxDistance() {
        switch (DEFAULT_COLOR_MODEL) {
            case RGB:
                return MAX_COLOR_DISTANCE_RGB;
            case LAB:
                return MAX_COLOR_DISTANCE_LAB;
            default:
                return MAX_COLOR_DISTANCE_RGB;
        }
    }

    private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

    static int minBrightness = 75;
    public static int getRowCenter(int row, int col, int r, int[][] imageArray) {

        int topLength = 0;
        int bottomLength = 0;

        for (int i = row - 1; i >= row - r; i--) {
            int pixel = imageArray[i][col];
            int brightness = getBrightness(pixel);
            if (brightness > minBrightness) {
                topLength++;
            } else {
                break;
            }
        }

        for (int i = row; i < row + r; i++) {
            int pixel = imageArray[i][col];
            int brightness = getBrightness(pixel);
            if (brightness > minBrightness) {
                bottomLength++;
            } else {
                break;
            }
        }

        int center = (topLength + bottomLength) / 2;

        if (topLength + bottomLength == 0){
            return -1;
        }else {
            if (center > topLength) {
                row = row + (center - topLength);
            } else {
                row = row - (topLength - center);
            }
        }

        return row;
    }

    private static int getColumnCenter(int row, int col, int r, int[][] imageArray) {
        int leftLength = 0;
        int rightLength = 0;

        for (int i = col - 1; i >= col - r; i--) {
            int pixel = imageArray[row][i];
            int brightness = getBrightness(pixel);
            if (brightness > minBrightness) {
                leftLength++;
            } else {
                break;
            }
        }

        for (int i = col; i < col + r; i++) {
            int pixel = imageArray[row][i];
            int brightness = getBrightness(pixel);
            if (brightness > minBrightness) {
                rightLength++;
            } else {
                break;
            }
        }

        int center = (leftLength + rightLength) / 2;
        if (center > leftLength) {
            col = col + (center - leftLength);
        } else {
            col = col - (leftLength - center);
        }
        return col;
    }


    public static boolean isCenterOfCircle(int row, int col, int r, BufferedImage image, StringBuilder sb) {
        int[][] imageArray = convertTo2DWithoutUsingGetRGB(image);

        for (int i = 0; i < 5; i++) {
            row = getRowCenter(row, col, r, imageArray);
            if (row == -1){
                return false;
            }
            col = getColumnCenter(row, col, r, imageArray);
        }

        StringBuilder sb1 = new StringBuilder();
        for (int i = row - 1; i >= row - r; i--) {
            int pixel = imageArray[i][col];
            int brightness = getBrightness(pixel);
            if (brightness > 100) {
                sb1.insert(0, String.format("<li style=\"background-color:rgb(%s)\">%s  &nbsp;&nbsp;&nbsp;  B: %s</li>",
                        getColorHexString(pixel),
                        getColorHexString(pixel),
                        getBrightness(pixel)));
            } else {
                break;
            }
        }

        sb1.append("<li>" + "1" + "</li>");

        for (int i = row; i < row + r; i++) {
            int pixel = imageArray[i][col];
            int brightness = getBrightness(pixel);
            if (brightness > 100) {
                sb1.append(String.format("<li style=\"background-color:rgb(%s)\">%s  &nbsp;&nbsp;&nbsp;  B: %s</li>",
                        getColorHexString(pixel),
                        getColorHexString(pixel),
                        getBrightness(pixel)));
            } else {
                break;
            }
        }

        int color = imageArray[row][col];

        sb1.append(String.format("<li style=\"background-color:rgb(%s)\">Result: %s  &nbsp;&nbsp;&nbsp;  B: %s</li>",
                getColorHexString(color),
                getColorHexString(color),
                getBrightness(color)));

        sb.append(sb1);

        return true;
    }


    public static boolean isCenterOfCircle1(int row, int col, int r, BufferedImage image, StringBuilder sb) {

        //byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        int[][] imageArray = convertTo2DWithoutUsingGetRGB(image);
        int topLength = 0;
        int bottomLength = 0;

        StringBuilder sb1 = new StringBuilder();
        for (int i = row - 1; i >= row - r; i--) {
            int pixel = imageArray[i][col];
            int brightness = getBrightness(pixel);
            if (brightness > 100) {
                topLength++;
                sb1.insert(0, String.format("<li style=\"background-color:rgb(%s)\">%s  &nbsp;&nbsp;&nbsp;  B: %s</li>",
                        getColorHexString(pixel),
                        getColorHexString(pixel),
                        getBrightness(pixel)));
            } else {
                break;
            }
        }

        sb1.append("<li>" + topLength + "</li>");

        for (int i = row; i < row + r; i++) {
            int pixel = imageArray[i][col];
            int brightness = getBrightness(pixel);
            if (brightness > 100) {
                bottomLength++;
                sb1.append(String.format("<li style=\"background-color:rgb(%s)\">%s  &nbsp;&nbsp;&nbsp;  B: %s</li>",
                        getColorHexString(pixel),
                        getColorHexString(pixel),
                        getBrightness(pixel)));
            } else {
                break;
            }
        }

        sb1.append("<li>" + bottomLength + "</li>");
        sb1.append("<li>&nbsp;</li>");

        int color = 0;
        int center = (topLength + bottomLength) / 2;

        if (center > topLength) {
            row = row + (center - topLength);
            color = imageArray[row + (center - topLength)][col];
        } else {
            row = row - (topLength - center);
            color = imageArray[row - (topLength - center)][col];
        }

        sb1.append(String.format("<li style=\"background-color:rgb(%s)\">%s  &nbsp;&nbsp;&nbsp;  B: %s</li>",
                getColorHexString(color),
                getColorHexString(color),
                getBrightness(color)));

        sb.append(sb1);

        int leftLength = 0;
        int rightLength = 0;
        StringBuilder sb2 = new StringBuilder();

        for (int i = col - 1; i >= col - r; i--) {
            int pixel = imageArray[row][i];
            int brightness = getBrightness(pixel);
            if (brightness > 100) {
                leftLength++;
                sb2.insert(0, String.format("<li style=\"background-color:rgb(%s)\">%s  &nbsp;&nbsp;&nbsp;  B: %s</li>",
                        getColorHexString(pixel),
                        getColorHexString(pixel),
                        getBrightness(pixel)));
            } else {
                break;
            }
        }

        sb2.append("<li>" + leftLength + "</li>");

        for (int i = col; i < col + r; i++) {
            int pixel = imageArray[row][i];
            int brightness = getBrightness(pixel);
            if (brightness > 100) {
                rightLength++;
                sb2.append(String.format("<li style=\"background-color:rgb(%s)\">%s  &nbsp;&nbsp;&nbsp;  B: %s</li>",
                        getColorHexString(pixel),
                        getColorHexString(pixel),
                        getBrightness(pixel)));
            } else {
                break;
            }
        }

        sb2.append("<li>" + rightLength + "</li>");
        sb2.append("<li>&nbsp;</li>");

        center = (leftLength + rightLength) / 2;
        if (center > leftLength) {
            color = imageArray[row + (center - leftLength)][col];
        } else {
            color = imageArray[row - (leftLength - center)][col];
        }

        sb2.append(String.format("<li style=\"background-color:rgb(%s)\">%s  &nbsp;&nbsp;&nbsp;  B: %s</li>",
                getColorHexString(color),
                getColorHexString(color),
                getBrightness(color)));

        sb.append(sb2);

        //getPixels gets the color of the current pixel.
//        if (imageArray[row][col] == imageArray[row + r][col]
//                || imageArray[row][col] == imageArray[row - r][col]
//                || imageArray[row][col] == imageArray[row][col + r]
//                || imageArray[row][col] == imageArray[row][col - r]) {
//            return true;
//        } else {
//            return false;
//        }
        return true;
    }

    public static Object getColorHexString(int color) {
        return String.format("%d,  %d,  %d", Color.red(color), Color.green(color), Color.blue(color));
    }

//    /**
//     * Get the most common color from the bitmap
//     *
//     * @param bitmap       The bitmap from which to extract the color
//     * @param sampleLength The max length of the image to traverse
//     * @return The extracted color information
//     */
//    public static ColorInfo getColorFromBitmap(BitMap bitmap,
//                                               @SuppressWarnings("SameParameterValue") int sampleLength) {
//        int highestCount = 0;
//        int commonColor = -1;
//        int counter;
//
//        int goodPixelCount = 0;
//        int totalPixels = 0;
//        double quality = 0;
//        int colorsFound;
//
//        try {
//
//            SparseIntArray m = new SparseIntArray();
//
//            for (int i = 0; i < Math.min(bitmap.getWidth(), sampleLength); i++) {
//
//                for (int j = 0; j < Math.min(bitmap.getHeight(), sampleLength); j++) {
//
//                    int color = bitmap.getPixel(i, j);
//
//                    if (color != Color.TRANSPARENT) {
//                        totalPixels++;
//
//                        counter = m.get(color);
//                        counter++;
//                        m.put(color, counter);
//
//                        if (counter > highestCount) {
//                            commonColor = color;
//                            highestCount = counter;
//                        }
//                    }
//                }
//            }
//
//            // check the quality of the photo
//            colorsFound = m.size();
//            int goodColors = 0;
//
//            for (int i = 0; i < colorsFound; i++) {
//                if (areColorsSimilar(commonColor, m.keyAt(i))) {
//                    goodColors++;
//                    goodPixelCount += m.valueAt(i);
//                }
//            }
//
//            double quality1 = ((double) goodPixelCount / totalPixels) * 100d;
//            double quality2 = ((double) (colorsFound - goodColors) / colorsFound) * 100d;
//            quality = Math.min(quality1, (100 - quality2));
//
//            m.clear();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return new ColorInfo(commonColor, quality);
//    }

    /**
     * Get the brightness of a given color
     *
     * @param color The color
     * @return The brightness value
     */
    public static int getBrightness(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return (int) Math.sqrt(r * r * .241 +
                        g * g * .691 +
                        b * b * .068
        );
    }

    /**
     * Validate the color by looking for missing color, duplicate colors, color out of sequence etc...
     *
     * @param swatches the range of colors
     * @return True if calibration is complete
     */
    public static boolean isCalibrationComplete(ArrayList<Swatch> swatches) {
        for (Swatch swatch : swatches) {
            if (swatch.getColor() == 0 || swatch.getColor() == Color.BLACK) {
                //Calibration is incomplete
                return false;
            }
        }
        return true;
    }

    /**
     * Computes the Euclidean distance between the two colors
     *
     * @param color1 the first color
     * @param color2 the color to compare with
     * @return the distance between the two colors
     */
    public static double getColorDistance(int color1, int color2) {
        switch (DEFAULT_COLOR_MODEL) {
            case RGB:
                return getColorDistanceRgb(color1, color2);

            case LAB:
                return getColorDistanceLab(colorToLab(color1), colorToLab(color2));

            default:
                //todo: create a hsv distance. currently using rgb
                return getColorDistanceRgb(color1, color2);
        }
    }

    /**
     * Computes the Euclidean distance between the two colors
     *
     * @param color1 the first color
     * @param color2 the color to compare with
     * @return the distance between the two colors
     */
    private static double getColorDistanceRgb(int color1, int color2) {
        double r, g, b;

        r = Math.pow(Color.red(color2) - Color.red(color1), 2.0);
        g = Math.pow(Color.green(color2) - Color.green(color1), 2.0);
        b = Math.pow(Color.blue(color2) - Color.blue(color1), 2.0);

        return Math.sqrt(b + g + r);
    }

    public static boolean areColorsTooDissimilar(int color1, int color2) {
        switch (DEFAULT_COLOR_MODEL) {
            case RGB:
                return getColorDistanceRgb(color1, color2) > MAX_SAMPLING_COLOR_DISTANCE_RGB;

            case LAB:
                return getColorDistanceLab(colorToLab(color1), colorToLab(color2))
                        > MAX_SAMPLING_COLOR_DISTANCE_LAB;

            default:
                //todo: create a hsv distance. currently using rgb
                return getColorDistanceRgb(color1, color2) > MIN_COLOR_DISTANCE_RGB;
        }
    }


    public static boolean areColorsSimilar(int color1, int color2) {
        switch (DEFAULT_COLOR_MODEL) {
            case RGB:
                return getColorDistanceRgb(color1, color2) < MIN_COLOR_DISTANCE_RGB;

            case LAB:
                return getColorDistanceLab(colorToLab(color1), colorToLab(color2))
                        < MIN_COLOR_DISTANCE_LAB;

            default:
                //todo: create a hsv distance. currently using rgb
                return getColorDistanceRgb(color1, color2) < MIN_COLOR_DISTANCE_RGB;
        }
    }

    /**
     * Auto generate the color swatches for the given test type
     *
     * @param swatches The test object
     * @return The list of generated color swatches
     */
    @SuppressWarnings("SameParameterValue")
    public static ArrayList<Swatch> generateGradient(
            ArrayList<Swatch> swatches, ColorModel colorModel, double increment) {

        if (colorModel == ColorModel.HSV) {
            return getGradientHsvColor(swatches, 200);
        }

        ArrayList<Swatch> list = new ArrayList<>();

        for (int i = 0; i < swatches.size() - 1; i++) {

            int startColor = swatches.get(i).getColor();
            int endColor = swatches.get(i + 1).getColor();
            double startValue = swatches.get(i).getValue();
            int steps = (int) ((swatches.get(i + 1).getValue() - startValue) / increment);

            for (int j = 0; j < steps; j++) {
                int color = 0;
                switch (colorModel) {
                    case RGB:
                        color = ColorUtil.getGradientColor(startColor, endColor, steps, j);
                        break;
                    case LAB:
                        color = ColorUtil.labToColor(ColorUtil.getGradientLabColor(colorToLab(startColor),
                                colorToLab(endColor), steps, j));
                }

                list.add(new Swatch(startValue + (j * increment), color, color));
            }
        }
        list.add(new Swatch(swatches.get(swatches.size() - 1).getValue(),
                swatches.get(swatches.size() - 1).getColor(),
                swatches.get(swatches.size() - 1).getColor()));

        return list;
    }

    /**
     * Get the color that lies in between two colors
     *
     * @param startColor The first color
     * @param endColor   The last color
     * @param n          Number of steps between the two colors
     * @param i          The index at which the color is to be calculated
     * @return The newly generated color
     */
    private static int getGradientColor(int startColor, int endColor, int n, int i) {
        return Color.rgb(interpolate(Color.red(startColor), Color.red(endColor), n, i),
                interpolate(Color.green(startColor), Color.green(endColor), n, i),
                interpolate(Color.blue(startColor), Color.blue(endColor), n, i));
    }

    /**
     * Get the color component that lies between the two color component points
     *
     * @param start The first color component value
     * @param end   The last color component value
     * @param n     Number of steps between the two colors
     * @param i     The index at which the color is to be calculated
     * @return The calculated color component
     */
    private static int interpolate(int start, int end, int n, int i) {
        return (int) ((float) start + ((((float) end - (float) start) / n) * i));
    }

    /**
     * Convert color value to RGB string
     *
     * @param color The color to convert
     * @return The rgb value as string
     */
    public static String getColorRgbString(int color) {
        return String.format("%d  %d  %d", Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Convert rgb string color to color
     *
     * @param rgb The rgb string representation of the color
     * @return An Integer color value
     */
    public static Integer getColorFromRgb(String rgb) {
        String[] rgbArray = rgb.split("\\s+");
        return Color.rgb(Integer.valueOf(rgbArray[0]), Integer.valueOf(rgbArray[1]), Integer.valueOf(rgbArray[2]));
    }

    /**
     * Convert int color to Lab color
     *
     * @param color The color to convert
     * @return The lab color
     */
    public static LabColor colorToLab(int color) {
        return rgbToLab(Color.red(color), Color.green(color), Color.blue(color));
    }

    //http://stackoverflow.com/questions/27090107/color-gradient-algorithm-in-lab-color-space
    private static LabColor getGradientLabColor(LabColor c1, LabColor c2, int n, int index) {
        double alpha = (double) index / (n - 1);  // 0.0 <= alpha <= 1.0
        double L = (1 - alpha) * c1.L + alpha * c2.L;
        double a = (1 - alpha) * c1.a + alpha * c2.a;
        double b = (1 - alpha) * c1.b + alpha * c2.b;
        return new LabColor(L, a, b);
    }

    /**
     * Convert LAB color to int Color
     *
     * @param color the LAB color
     * @return int color value
     */
    private static int labToColor(LabColor color) {
        double a, b, g, l, r, x, y, z;
        l = color.L;
        a = color.a;
        b = color.b;
        y = (l + 16) / 116;
        x = y + a / 500;
        z = y - b / 200;
        y = Yn * lab_xyz(y);
        x = Xn * lab_xyz(x);
        z = Zn * lab_xyz(z);
        r = xyz_rgb(3.2404542 * x - 1.5371385 * y - 0.4985314 * z);
        g = xyz_rgb(-0.9692660 * x + 1.8760108 * y + 0.0415560 * z);
        b = xyz_rgb(0.0556434 * x - 0.2040259 * y + 1.0572252 * z);
        r = Math.max(0, Math.min(r, 255));
        g = Math.max(0, Math.min(g, 255));
        b = Math.max(0, Math.min(b, 255));
        return Color.rgb((int) r, (int) g, (int) b);
    }

    private static double lab_xyz(double t) {
        if (t > t1) {
            return t * t * t;
        } else {
            return t2 * (t - t0);
        }
    }

    private static double xyz_rgb(double r) {
        return Math.round(255 * (r <= 0.00304 ? 12.92 * r : 1.055 * Math.pow(r, 1 / 2.4) - 0.055));
    }

    private static LabColor rgbToLab(double r, double g, double b) {
        XyzColor xyzColor = rgbToXyz(r, g, b);
        return new LabColor(116 * xyzColor.y - 16, 500 * (xyzColor.x - xyzColor.y), 200 * (xyzColor.y - xyzColor.z));
    }

    private static double rgb_xyz(double r) {
        if ((r /= 255) <= 0.04045) {
            return (r / 12.92);
        } else {
            return (Math.pow((r + 0.055) / 1.055, 2.4));
        }
    }

    private static double xyz_lab(double t) {
        if (t > t3) {
            return Math.pow(t, 1.0 / 3.0);
        } else {
            return t / t2 + t0;
        }
    }

    private static XyzColor rgbToXyz(double r, double g, double b) {
        double x, y, z;
        r = rgb_xyz(r);
        g = rgb_xyz(g);
        b = rgb_xyz(b);
        x = xyz_lab((0.4124564 * r + 0.3575761 * g + 0.1804375 * b) / Xn);
        y = xyz_lab((0.2126729 * r + 0.7151522 * g + 0.0721750 * b) / Yn);
        z = xyz_lab((0.0193339 * r + 0.1191920 * g + 0.9503041 * b) / Zn);
        return new XyzColor(x, y, z);
    }

    // create gradient from yellow to red to black with 100 steps
    //var gradient = hsvGradient(100, [{h:0.14, s:0.5, b:1}, {h:0, s:1, b:1}, {h:0, s:1, b:0}]);
    // http://stackoverflow.com/questions/2593832/how-to-interpolate-hue-values-in-hsv-colour-space
    @SuppressWarnings("SameParameterValue")
    private static ArrayList<Swatch> getGradientHsvColor(ArrayList<Swatch> colors, int steps) {
        int parts = colors.size() - 1;
        ArrayList<Swatch> gradient = new ArrayList<>();
        int gradientIndex = 0;
        double increment = 0.01;
        double partSteps = Math.floor(steps / parts);
        double remainder = steps - (partSteps * parts);
        for (int col = 0; col < parts; col++) {

            double startValue = colors.get(col).getValue();

            float[] hsvColor = new float[3];

            Color.RGBToHSV(Color.red(colors.get(col).getColor()),
                    Color.green(colors.get(col).getColor()),
                    Color.blue(colors.get(col).getColor()), hsvColor);
            HsvColor c1 = new HsvColor(hsvColor[0], hsvColor[1], hsvColor[2]);

            Color.RGBToHSV(Color.red(colors.get(col + 1).getColor()),
                    Color.green(colors.get(col + 1).getColor()),
                    Color.blue(colors.get(col + 1).getColor()), hsvColor);
            HsvColor c2 = new HsvColor(hsvColor[0], hsvColor[1], hsvColor[2]);

            // determine clockwise and counter-clockwise distance between hues
            double distCCW = (c1.h >= c2.h) ? c1.h - c2.h : 1 + c1.h - c2.h;
            double distCW = (c1.h >= c2.h) ? 1 + c2.h - c1.h : c2.h - c1.h;

            // ensure we get the right number of steps by adding remainder to final part
            if (col == parts - 1) partSteps += remainder;

            // make gradient for this part
            for (int step = 0; step < partSteps; step++) {
                double p = step / partSteps;
                // interpolate h, s, b
                float h = (float) ((distCW <= distCCW) ? c1.h + (distCW * p) : c1.h - (distCCW * p));
                if (h < 0) h = 1 + h;
                if (h > 1) h = h - 1;
                float s = (float) ((1 - p) * c1.s + p * c2.s);
                float v = (float) ((1 - p) * c1.v + p * c2.v);

                hsvColor[0] = h;
                hsvColor[1] = s;
                hsvColor[2] = v;
                // add to gradient array
                gradient.add(gradientIndex, new Swatch(startValue + (step * increment),
                        Color.HSVToColor(hsvColor), Color.HSVToColor(hsvColor)));

                gradientIndex++;
            }
        }
        return gradient;
    }

    //https://github.com/StanfordHCI/c3/blob/master/java/src/edu/stanford/vis/color/LAB.java
    public static double getColorDistanceLab(LabColor x, LabColor y) {
        // adapted from Sharma et al's MATLAB implementation at
        //  http://www.ece.rochester.edu/~gsharma/ciede2000/

        // parametric factors, use defaults
        double kl = 1, kc = 1, kh = 1;

        // compute terms
        double pi = Math.PI,
                L1 = x.L, a1 = x.a, b1 = x.b, Cab1 = Math.sqrt(a1 * a1 + b1 * b1),
                L2 = y.L, a2 = y.a, b2 = y.b, Cab2 = Math.sqrt(a2 * a2 + b2 * b2),
                Cab = 0.5 * (Cab1 + Cab2),
                G = 0.5 * (1 - Math.sqrt(Math.pow(Cab, 7) / (Math.pow(Cab, 7) + Math.pow(25, 7)))),
                ap1 = (1 + G) * a1,
                ap2 = (1 + G) * a2,
                Cp1 = Math.sqrt(ap1 * ap1 + b1 * b1),
                Cp2 = Math.sqrt(ap2 * ap2 + b2 * b2),
                Cpp = Cp1 * Cp2;

        // ensure hue is between 0 and 2pi
        double hp1 = Math.atan2(b1, ap1);
        if (hp1 < 0) hp1 += 2 * pi;
        double hp2 = Math.atan2(b2, ap2);
        if (hp2 < 0) hp2 += 2 * pi;

        double dL = L2 - L1,
                dC = Cp2 - Cp1,
                dhp = hp2 - hp1;

        if (dhp > +pi) dhp -= 2 * pi;
        if (dhp < -pi) dhp += 2 * pi;
        if (Cpp == 0) dhp = 0;

        // Note that the defining equations actually need
        // signed Hue and chroma differences which is different
        // from prior color difference formulae
        double dH = 2 * Math.sqrt(Cpp) * Math.sin(dhp / 2);

        // Weighting functions
        double Lp = 0.5 * (L1 + L2),
                Cp = 0.5 * (Cp1 + Cp2);

        // Average Hue Computation
        // This is equivalent to that in the paper but simpler programmatically.
        // Average hue is computed in radians and converted to degrees where needed
        double hp = 0.5 * (hp1 + hp2);
        // Identify positions for which abs hue diff exceeds 180 degrees
        if (Math.abs(hp1 - hp2) > pi) hp -= pi;
        if (hp < 0) hp += 2 * pi;

        // Check if one of the chroma values is zero, in which case set
        // mean hue to the sum which is equivalent to other value
        if (Cpp == 0) hp = hp1 + hp2;

        double Lpm502 = (Lp - 50) * (Lp - 50),
                Sl = 1 + 0.015 * Lpm502 / Math.sqrt(20 + Lpm502),
                Sc = 1 + 0.045 * Cp,
                T = 1 - 0.17 * Math.cos(hp - pi / 6)
                        + 0.24 * Math.cos(2 * hp)
                        + 0.32 * Math.cos(3 * hp + pi / 30)
                        - 0.20 * Math.cos(4 * hp - 63 * pi / 180),
                Sh = 1 + 0.015 * Cp * T,
                ex = (180 / pi * hp - 275) / 25,
                deltaThetaRad = (30 * pi / 180) * Math.exp(-1 * (ex * ex)),
                Rc = 2 * Math.sqrt(Math.pow(Cp, 7) / (Math.pow(Cp, 7) + Math.pow(25, 7))),
                RT = -1 * Math.sin(2 * deltaThetaRad) * Rc;

        dL = dL / (kl * Sl);
        dC = dC / (kc * Sc);
        dH = dH / (kh * Sh);

        // The CIE 00 color difference
        return Math.sqrt(dL * dL + dC * dC + dH * dH + RT * dC * dH);
    }

    /**
     * The different types of color models
     */
    public enum ColorModel {
        RGB, LAB, HSV
    }
}