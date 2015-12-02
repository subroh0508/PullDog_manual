package kei.balloon.pulldog;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kei on 2015/08/19.
 */
public class RecordingKML {
    private OutputStreamWriter sw;
    private PrintWriter pw;
    private String fileName;

    private List<Double[]> route = new ArrayList<>();

    public RecordingKML(String s) {
        fileName = s;
        try {
            sw = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
            pw = new PrintWriter(sw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recordData(double[] point) {
        Double[] tmp = {point[1], point[0]};
        route.add(tmp);
    }

    public boolean closeFile() {
        double[] start = {(route.get(0))[0], (route.get(1))[1]};
        double[] end = {(route.get(route.size()-1))[0], (route.get(route.size()-1))[1]};

        try {
            pw.println("<?xml version='1.0' encoding='UTF-8'?>");
            pw.println("<kml xmlns='http://www.opengis.net/kml/2.2'>");
            pw.println("\t<Document>");
            pw.println("\t\t<name>Route</name>");

            //Marker(Start)
            pw.println("\t\t<Placemark>");
            pw.println("\t\t\t<name>Departure</name>");
            pw.println("\t\t\t<styleUrl>#icon-503-41F08C-nodesc</styleUrl>");
            pw.println("\t\t\t<Point>");
            pw.println("\t\t\t\t<tessellate>1</tessellate>");
            pw.print("\t\t\t\t<coordinates>" + start[0] + "," + start[1] + ",0");
            pw.println("</coordinates>");
            pw.println("\t\t\t</Point>");
            pw.println("\t\t</Placemark>");

            //Route
            pw.println("\t\t<Placemark>");
            pw.println("\t\t\t<name>Route</name>");
            pw.println("\t\t\t<styleUrl>#line-FFFF00-3-nodesc</styleUrl>");
            pw.println("\t\t\t<LineString>");
            pw.println("\t\t\t\t<tessellate>1</tessellate>");
            pw.print("\t\t\t\t<coordinates>");
            for (Double[] p : route) pw.println("\t\t\t\t\t"+p[0] + "," + p[1] + " ");
            pw.println("</coordinates>");
            pw.println("\t\t\t</LineString>");
            pw.println("\t\t</Placemark>");

            //Marker(End)
            pw.println("\t\t<Placemark>");
            pw.println("\t\t\t<name>Destination</name>");
            pw.println("\t\t\t<styleUrl>#icon-503-41F08C-nodesc</styleUrl>");
            pw.println("\t\t\t<Point>");
            pw.println("\t\t\t\t<tessellate>1</tessellate>");
            pw.print("\t\t\t\t<coordinates>" + end[0] + "," + end[1] + ",0");
            pw.println("</coordinates>");
            pw.println("\t\t\t</Point>");
            pw.println("\t\t</Placemark>");

            //DefineStyle
            pw.println("\t\t<Style id='line-FFFF00-3-nodesc-normal'>");
            pw.println("\t\t\t<LineStyle>");
            pw.println("\t\t\t\t<color>ff0000FF</color>");
            pw.println("\t\t\t\t<width>3</width>");
            pw.println("\t\t\t</LineStyle>");
            pw.println("\t\t\t<BalloonStyle>");
            pw.println("\t\t\t\t<text><![CDATA[<h3>$[name]</h3>]]></text>");
            pw.println("\t\t\t</BalloonStyle>");
            pw.println("\t\t</Style>");
            pw.println("\t\t<Style id='line-FFFF00-3-nodesc-highlight'>");
            pw.println("\t\t\t<LineStyle>");
            pw.println("\t\t\t\t<color>ff0000FF</color>");
            pw.println("\t\t\t\t<width>5.0</width>");
            pw.println("\t\t\t</LineStyle>");
            pw.println("\t\t\t<BalloonStyle>");
            pw.println("\t\t\t\t<text><![CDATA[<h3>$[name]</h3>]]></text>");
            pw.println("\t\t\t</BalloonStyle>");
            pw.println("\t\t</Style>");
            pw.println("\t\t<StyleMap id='line-FFFF00-3-nodesc'>");
            pw.println("\t\t\t<Pair>");
            pw.println("\t\t\t\t<key>normal</key>");
            pw.println("\t\t\t\t<styleUrl>#line-FFFF00-3-nodesc-normal</styleUrl>");
            pw.println("\t\t\t</Pair>");
            pw.println("\t\t\t<Pair>");
            pw.println("\t\t\t\t<key>highlight</key>");
            pw.println("\t\t\t\t<styleUrl>#line-FFFF00-3-nodesc-highlight</styleUrl>");
            pw.println("\t\t\t</Pair>");
            pw.println("\t\t</StyleMap>");

            pw.println("\t</Document>");
            pw.println("</kml>");

            pw.close();
        } catch(Exception e) {
            Log.d("TAG", e.getMessage());
            return false;
        }

        return true;
    }
}
