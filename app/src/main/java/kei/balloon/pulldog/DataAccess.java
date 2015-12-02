package kei.balloon.pulldog;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 拓海 on 2015/08/25.
 */
public class DataAccess extends Activity{

    private Activity ac;
    private MainActivity ma;
    private Context co;
    static ArrayList<String> returnList;
    static String returnText;
    private ListView lv;
    private LinearLayout ll;
    //static TextView textView;
    String data;
    String lat;
    String lng;

    public static void setReturnList(ArrayList<String> text){returnList = text;}

    public static void setReturnText(String text){returnText = text;}


    public DataAccess(Activity activity, ListView listView, LinearLayout lLayout){
        lv = listView;
        ac = activity;
        ll = lLayout;
    }

    public DataAccess(Activity context){
        ac = context;
        co = context.getApplicationContext();
    }

    public DataAccess(MainActivity context){
        ma = context;
    }

    public void get(){
        AsyncTask<String,Void,JSONArray> task = new AsyncTask<String, Void, JSONArray>() {
            @Override
            protected JSONArray doInBackground(String... params) {
                String result;
                JSONArray ja = null;
                try {
                    URL url = new URL("http://54.64.216.22/tweetget.php");
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.connect();
                    InputStreamReader isr = new InputStreamReader(con.getInputStream(),"UTF-8");
                    BufferedReader br = new BufferedReader(isr);
                    StringBuffer tmp = new StringBuffer();
                    while(null != (result = br.readLine())){
                        tmp.append(result);
                    }
                    result = tmp.toString();
                    ja = new JSONArray(result);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return ja;
            }

            @Override
            protected void onPostExecute(JSONArray ja) {
                int registId = 0;
                StringBuffer sb = new StringBuffer();
                if (ja != null) {
                    for (int i = 0; i < ja.length(); i++) {
                        try {
                            JSONObject jo = ja.getJSONObject(i);
                            HashMap<String,String> hm = new HashMap<String,String>();
                            hm.put("data",jo.getString("data"));
                            hm.put("lat",jo.getString("lat"));
                            hm.put("lng",jo.getString("lng"));
                            hm.put("infoType", jo.getString("infoType"));

                            if(!jo.getString("infoType").equals("null")) {
                                double lat = Double.valueOf(jo.getString("lat"));
                                double lng = Double.valueOf(jo.getString("lng"));
                                String data = jo.getString("data");
                                int type = Integer.valueOf(jo.getString("infoType"));

                                ma.navi.addInformation(new Information(registId++, lat, lng, data, type));

                                /*switch (type) {
                                    case Information.TWEET:
                                        //ma.tweetList.add(p);
                                        ma.navi.addInformation(new Information(registId++, ));
                                        break;
                                    case Information.USER_INFO:
                                        ma.spotList.add(p);
                                        break;
                                    case Information.USER_ALERT:
                                        ma.alartList.add(p);
                                        break;
                                }*/


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        };
        task.execute();
    }


    public void save(String data, String lat, String lng, String infoType){
        AsyncTask<String,Void,String> task = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String result = null;
                try {
                    URL url = new URL("http://54.64.216.22/tweetsave.php?data=" + params[0] + "&lat=" + params[1] + "&lng="+params[2]+"&infoType="+params[3]);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.connect();
                    InputStreamReader isr = new InputStreamReader(con.getInputStream(),"UTF-8");
                    BufferedReader br = new BufferedReader(isr);
                    StringBuffer tmp = new StringBuffer();
                    while(null != (result = br.readLine())){
                        tmp.append(result);
                    }
                    result = tmp.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String text){
                switch(text){
                    case "fail":
                        Toast.makeText(ma,"アップロードに失敗しました",Toast.LENGTH_SHORT).show();
                        break;
                    case "success":
                        Toast.makeText(ma,"アップロードに成功しました",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        };
        task.execute(data,lat,lng,infoType);
    }

    public void fileSave(String data, String id, String name, String totalDistance){        //
        AsyncTask<String,Void,String> task = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String result = null;
                try {
                    URL url = new URL("http://54.64.216.22/routeupload.php");
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    String postData = "userfile=" + params[0] + "&name=" + params[1] + "&id=" + params[2] + "&totalDistance=" + params[3];
                    OutputStream os = con.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();
                    InputStreamReader isr = new InputStreamReader(con.getInputStream(),"UTF-8");
                    BufferedReader br = new BufferedReader(isr);
                    StringBuffer tmp = new StringBuffer();
                    while(null != (result = br.readLine())){
                        tmp.append(result);
                    }
                    result = tmp.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            protected void onPostExecute(String text){

                if(text != null) RouteMake.txtView.setText(text);      //todo 木岡氏に聞く
            }
        };
        task.execute(data,name,id,totalDistance);
    }

    public void fileGet(String id){
        AsyncTask<String,Void,String> task = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String result = null;
                try {
                    URL url = new URL("http://54.64.216.22/routedownload.php?id=" + params[0]);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.connect();
                    InputStreamReader isr = new InputStreamReader(con.getInputStream(),"UTF-8");
                    BufferedReader br = new BufferedReader(isr);
                    StringBuffer tmp = new StringBuffer();
                    while(null != (result = br.readLine())){
                        tmp.append(result);
                    }
                    result = tmp.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            protected void onPostExecute(String text){
                if(text != null) SearchHealthlyPeopleRoute.txt.setText(text);
            }
        };
        task.execute(id);
    }

    public void fileGet(int id){
        AsyncTask<String,Void,String> task = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String result = null;
                try {
                    URL url = new URL("http://54.64.216.22/routedownload.php?id=" + params[0]);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.connect();
                    InputStreamReader isr = new InputStreamReader(con.getInputStream(),"UTF-8");
                    BufferedReader br = new BufferedReader(isr);
                    StringBuffer tmp = new StringBuffer();
                    while(null != (result = br.readLine())){
                        tmp.append(result);
                    }
                    result = tmp.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            protected void onPostExecute(String text){
                if(text != null)  MainActivity.hRouteTxt.setText(text);
            }
        };
        task.execute(Integer.toString(id));
    }


    public void fileSearch(String searchText){
        AsyncTask<String,Void,JSONArray> task = new AsyncTask<String, Void, JSONArray>() {
            @Override
            protected JSONArray doInBackground(String... params) {
                String result;
                JSONArray ja = new JSONArray();
                try {
                    URL url = new URL("http://54.64.216.22/routesearch.php?searchText=" + params[0]);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.connect();
                    InputStreamReader isr = new InputStreamReader(con.getInputStream(),"UTF-8");
                    BufferedReader br = new BufferedReader(isr);
                    StringBuffer tmp = new StringBuffer();
                    while(null != (result = br.readLine())){
                        tmp.append(result);
                    }
                    result = tmp.toString();
                    ja = new JSONArray(result);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return ja;
            }

            protected void onPostExecute(JSONArray ja){
                List<RouteData> aa = new ArrayList<>();
                int idNum = 0;
                String name = "名前がありません";
                int totalDistance = 0;
                if(ja.length() == 0){
                    aa.clear();
                    RouteList rtList = new RouteList(aa,ac.getApplicationContext());
                    lv.setAdapter(rtList);
                    ll.setVisibility(View.VISIBLE);
                }else if(ja.length() != 0) {
                    for (int i = 0; i < ja.length(); i++) {
                        try {
                            idNum = 0;
                            name = "名前がありません";
                            totalDistance = 0;
                            JSONObject jo = ja.getJSONObject(i);
                            if (!jo.get("id").equals(null))
                                idNum = Integer.parseInt(jo.get("id").toString());
                            if (!jo.get("name").equals(null)) name = jo.get("name").toString();
                            if (!jo.get("totalDistance").equals(null))
                                totalDistance = Integer.parseInt(jo.get("totalDistance").toString());
                            RouteData list = new RouteData(idNum, name, totalDistance);
                            aa.add(list);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    RouteList rtList = new RouteList(aa, ac.getApplicationContext());
                    lv.setAdapter(rtList);
                    ll.setVisibility(View.INVISIBLE);
                }
            }
        };
        task.execute(searchText);
    }
}
