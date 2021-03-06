package kei.balloon.pulldog;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by subroh0508 on 15/12/15.
 */
public class ConvertCsvToKml {
	private BufferedReader reader;
	private RecordingKML kml, gnssKml, rfidKml;
	private String fileName;

	private List<LatLng> point = new ArrayList<>();

	public ConvertCsvToKml(String s) {
		String path = new String("/storage/emulated/0/DCIM/"+s+".csv"), data;

		try {
			reader = new BufferedReader(new FileReader(new File(path)));
			data = reader.readLine();
			while(data != null) {
				String tmp[] = data.split(",");
				point.add(new LatLng(Double.valueOf(tmp[0]).doubleValue(), Double.valueOf(tmp[1]).doubleValue()));
				data = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		kml = new RecordingKML("/storage/emulated/0/DCIM/"+s+".kml");
	}

	public boolean convert() { return kml.closeFile(point); }
}
