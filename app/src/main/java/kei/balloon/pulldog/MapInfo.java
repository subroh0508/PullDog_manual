package kei.balloon.pulldog;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kei on 2015/09/10.
 */
public class MapInfo {
    LatLng start;
    LatLng end;

    List<List<HashMap<String, String>>> route;

    MainActivity ma;

    public MapInfo(MainActivity m,LatLng s,LatLng e){
        start = s;
        end = e;
        route = null;
        ma = m;
        routeSearch();
    }

    private void routeSearch(){
        String url = getDirectionsUrl(start, end);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);

    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){


        String str_origin = "origin="+origin.latitude+","+origin.longitude;


        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        String sensor = "sensor=false";

        //パラメータ
        String parameters = str_origin+"&"+str_dest+"&"+sensor + "&language=ja" + "&mode=" + "walking";

        //JSON指定
        String output = "json";


        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        Log.d("URL", url);

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);


            urlConnection = (HttpURLConnection) url.openConnection();


            urlConnection.connect();


            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {
        //非同期で取得

        @Override
        protected String doInBackground(String... url) {


            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }


        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(result);
                parseJSONofDirectionAPI parser = new parseJSONofDirectionAPI();


                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            route = routes;
            ma.googleNaviStart.setText("OK!");
        }
    }



    public List<List<HashMap<String, String>>> getRoute(){
        return route;
    }

}
