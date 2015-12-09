package org.akvo.caddisfly;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.*;
import org.akvo.caddisfly.util.Color;
import org.akvo.caddisfly.util.ColorUtil;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;


public class Main {

    public static void main(String[] args) {

        //Color color = new Color(255, 0, 0);

        //LabColor labColor = ColorUtil.rgbToLab(255, 87, 181);
        //String hsvString = String.format("%.5f,%.5f,%.5f", labColor.L, labColor.a, labColor.b);

        //String hsvString = ColorUtil.getColorRgbString(Color.rgb(255, 87, 181));

        TestInfo testInfo = new TestInfo();


        File[] files = new File("C:\\Users\\Ishan\\Caddisfly\\GitHub\\calibration-eval\\calibrations").listFiles(
                (dir, name) -> {
                    return name.toLowerCase().endsWith(".txt");
                }
        );

        //testInfo.setRanges(getSwatchList(file, 2));
        //StringBuilder sb = new StringBuilder(hsvString);
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>ul{\n" +
                "     list-style:none;padding:0\n" +
                "}</style></head><body>");
//
//        try {
//            BufferedImage bufferedImage = ImageIO.read(new File("/Users/super/AndroidProjects/TestCircle.jpg"));
//            if(ColorUtil.isCenterOfCircle(147, 352, 120, bufferedImage, sb)){
//                sb.append("Found");
//            }else{
//                sb.append("Not Found");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        ArrayList<Swatch> defaultSwatches = new ArrayList<>();
        File defaultFile = new File("C:\\Users\\Ishan\\Caddisfly\\GitHub\\calibration-eval\\calibrations\\" + "Default.text");
        if (defaultFile.exists()) {
            defaultSwatches = getSwatchList(defaultFile, 2);
        }

        ArrayList<RgbColor> totals = new ArrayList<>(files.length);

        for (File file : files) {

            ArrayList<Swatch> swatches = null;
            try {
                swatches = SwatchHelper.loadCalibrationFromFile(file);
                RgbColor rgbColor;
                for (int i = 0; i < swatches.size(); i++) {
                    int color = swatches.get(i).getColor();
                    rgbColor = new RgbColor(Color.red(color), Color.green(color), Color.blue(color));

                    if (totals.size() > i) {
                        RgbColor current = totals.get(i);

                        totals.set(i, new RgbColor(current.r + rgbColor.r,
                                current.g + rgbColor.g,
                                current.b + rgbColor.b));
                    } else {
                        totals.add(i, rgbColor);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sb.append("<div style=\"font-size:15px;font-weight:bold\">Average Calibration</div>");
        sb.append("<ul style=\"width:200px\">");

        if (defaultSwatches.size() > 0) {

            for (int i = 0; i < defaultSwatches.size(); i++) {

                int swatchColor = defaultSwatches.get(i).getColor();
                RgbColor color = new RgbColor(Color.red(swatchColor), Color.green(swatchColor), Color.blue(swatchColor));

                sb.append(String.format("<li style=\"background-color:rgb(%s)\">%.2f=%s</li>",
                        String.format("%d,%d,%d", color.r, color.g, color.b),
                        i * 0.5,
                        String.format("%d  %d  %d", color.r, color.g, color.b)
                ));
            }

        }else {
            for (int i = 0; i < totals.size(); i++) {

                RgbColor current = totals.get(i);
                RgbColor color = new RgbColor(divide(current.r, files.length),
                        divide(current.g, files.length),
                        divide(current.b, files.length));

                totals.set(i, color);

                Swatch swatch = new Swatch(i * 0.5, Color.rgb(color.r, color.g, color.b), Color.TRANSPARENT);
                defaultSwatches.add(swatch);

                sb.append(String.format("<li style=\"background-color:rgb(%s)\">%.2f=%s</li>",
                        String.format("%d,%d,%d", color.r, color.g, color.b),
                        i * 0.5,
                        String.format("%d  %d  %d", color.r, color.g, color.b)
                ));

            }
        }
        sb.append("</ul>");

        sb.append("<br/>");

        for (File file : files) {

            sb.append("<div style=\"width:130px;float:left\">");

            sb.append("<div style=\"font-size:15px;font-weight:bold\">" + file.getName() + "</div>");

            sb.append("<ul>");
            testInfo.setRanges(getSwatchList(file, 2));
            ArrayList<Swatch> list = ColorUtil.generateGradient(testInfo.getSwatches(), ColorUtil.ColorModel.RGB, 0.01);
            for (Swatch swatch : list) {
                sb.append(String.format("<li style=\"background-color:rgb(%s)\">%s</li>",
                        getColorHexString(swatch.getColor()), getColorHexString(swatch.getColor())));
            }
            sb.append("</ul>");

            sb.append("<div style=\"font-size:15px;font-weight:bold\">Result (Error Diff)</div>");


            for (int i = 0; i < 201; i += 10) {
                ColorInfo colorInfo = new ColorInfo(list.get(i).getColor(), 0);

                //ColorInfo colorInfo = new ColorInfo(Color.rgb(246, 37, 95), 0);

                ResultDetail resultInfo1 = SwatchHelper.analyzeColor(colorInfo, defaultSwatches, ColorUtil.ColorModel.RGB);

                double variance = resultInfo1.getResult() - (i * 0.01);

                if (Math.abs(variance) > 0.2) {
                    sb.append(String.format(" <span style=\"background-color:orange\">%.2f (%.2f)</span><br/>", resultInfo1.getResult(), variance));
                } else {
                    sb.append(String.format("%.2f (%.2f)<br/>", resultInfo1.getResult(), variance));
                }
            }

//            testInfo.setRanges(getSwatchList(file, 3));
//            resultInfo1 = SwatchHelper.analyzeColor(colorInfo, testInfo.getSwatches(), ColorUtil.ColorModel.RGB);
//            sb.append(String.format("3 Step: %.2f<br/>", resultInfo1.getResult()));
//
//            testInfo.setRanges(getSwatchList(file, 5));
//            resultInfo1 = SwatchHelper.analyzeColor(colorInfo, testInfo.getSwatches(), ColorUtil.ColorModel.RGB);
//            sb.append(String.format("5 Step: %.2f<br/>", resultInfo1.getResult()));

            sb.append("</div>");

        }

//        sb.append("<div style=\"width:25%;float:left\">");
//
//        sb.append("<h2>LAB</h2>");
//
//        testInfo.setRanges(getSwatchList(2));
//        resultInfo1 = SwatchHelper.analyzeColor(colorInfo, testInfo.getSwatches(), ColorUtil.ColorModel.LAB);
//        sb.append(String.format("2 Step: %.2f<br/>", resultInfo1.getResult()));
//
//        testInfo.setRanges(getSwatchList(3));
//        resultInfo1 = SwatchHelper.analyzeColor(colorInfo, testInfo.getSwatches(), ColorUtil.ColorModel.LAB);
//        sb.append(String.format("3 Step: %.2f<br/>", resultInfo1.getResult()));
//
//        testInfo.setRanges(getSwatchList(5));
//        resultInfo1 = SwatchHelper.analyzeColor(colorInfo, testInfo.getSwatches(), ColorUtil.ColorModel.LAB);
//        sb.append(String.format("5 Step: %.2f<br/>", resultInfo1.getResult()));
//
//        sb.append("<ul>");
//        testInfo.setRanges(getSwatchList(2));
//        list = ColorUtil.generateGradient(testInfo.getSwatches(), ColorUtil.ColorModel.LAB, 0.01);
//        for (Swatch swatch : list) {
//            sb.append(String.format("<li style=\"background-color:rgb(%s)\">%s</li>",
//                    getColorHexString(swatch.getColor()), getColorHexString(swatch.getColor())));
//        }
//        sb.append("</ul>");
//        sb.append("</div>");
//        sb.append("<div style=\"width:25%;float:left\">");
//
//        sb.append("<h2>HSV</h2>");
//
//        testInfo.setRanges(getSwatchList(2));
//        resultInfo1 = SwatchHelper.analyzeColor(colorInfo, testInfo.getSwatches(), ColorUtil.ColorModel.HSV);
//        sb.append(String.format("2 Step: %.2f<br/>", resultInfo1.getResult()));
//
//        testInfo.setRanges(getSwatchList(3));
//        resultInfo1 = SwatchHelper.analyzeColor(colorInfo, testInfo.getSwatches(), ColorUtil.ColorModel.HSV);
//        sb.append(String.format("3 Step: %.2f<br/>", resultInfo1.getResult()));
//
//        testInfo.setRanges(getSwatchList(5));
//        resultInfo1 = SwatchHelper.analyzeColor(colorInfo, testInfo.getSwatches(), ColorUtil.ColorModel.HSV);
//        sb.append(String.format("5 Step: %.2f<br/>", resultInfo1.getResult()));
//
//        sb.append("<ul>");
//        testInfo.setRanges(getSwatchList(2));
//        list = ColorUtil.generateGradient(testInfo.getSwatches(), ColorUtil.ColorModel.HSV, 0.01);
//        for (Swatch swatch : list) {
//            sb.append(String.format("<li style=\"background-color:rgb(%s)\">&nbsp;</li>",
//                    getColorHexString(swatch.getColor())));
//        }
//        sb.append("</ul>");
//        sb.append("</div>");

        sb.append("<div style=\"clear:both\"></div><br/><br/><br/><br/><br/><br/></body></html>");

        HttpServer httpServer = Vertx.vertx().createHttpServer();
        httpServer.requestHandler(request -> {

            HttpServerResponse response = request.response();

            response.setStatusCode(200);
            response.headers()
                    .add("Content-Type", "text/html");
            response.end(sb.toString());
        });


//        Vertx.vertx().createHttpServer()
//                .websocketHandler(ws -> { ws.handler(ws::writeMessage);})
//                .requestHandler(req -> {
//                    if (req.uri().equals("/")) req.response().sendFile("ws.html");
//                }).listen(
//                Integer.getInteger("http.port"), System.getProperty("http.address"));

        Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end(sb.toString())).listen(8080);
    }

    private static ArrayList<Swatch> getSwatchList(File file, int count) {

        ArrayList<Swatch> swatches = null;
        try {
            swatches = SwatchHelper.loadCalibrationFromFile(file);

//            swatches.add(new Swatch(0, new Color(255, 87, 181).getRGB(), new Color(255, 87, 181).getRGB()));
//            swatches.add(new Swatch(2, new Color(245, 185, 122).getRGB(), new Color(245, 185, 122).getRGB()));
//
//            if (count > 2) {
//                swatches.add(new Swatch(1, new Color(255, 146, 139).getRGB(), new Color(245, 185, 122).getRGB()));
//            }
//            if (count > 4) {
//                swatches.add(new Swatch(0.5, new Color(255, 124, 157).getRGB(), new Color(245, 185, 122).getRGB()));
//                swatches.add(new Swatch(1.5, new Color(250, 171, 130).getRGB(), new Color(245, 185, 122).getRGB()));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return swatches;
    }

    public static Object getColorHexString(int color) {
        return String.format("%d,  %d,  %d", Color.red(color), Color.green(color), Color.blue(color));
    }


    public static int divide(int dividend, int divisor) {

        return new BigDecimal(dividend).divide(new BigDecimal(divisor), RoundingMode.HALF_UP).intValue();

//        return (int) Math.ceil((double)dividend / divisor);
    }

}
