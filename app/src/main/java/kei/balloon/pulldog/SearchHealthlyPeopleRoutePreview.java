package kei.balloon.pulldog;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kei on 2015/09/16.
 */
public class SearchHealthlyPeopleRoutePreview extends FragmentActivity {

    //Google Map
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private List<LatLng> latlngList;
    private File file;
    private TextView txt;
    private BootstrapButton saveBtn;
    private BootstrapButton cancelBtn;
    private SearchHealthlyPeopleRoutePreview ac;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthly_route_preview);
        setUpMapIfNeeded();

        ac = this;

        txt = (TextView) findViewById(R.id.total_distance);

        saveBtn = (BootstrapButton) findViewById(R.id.preview_ok);
        cancelBtn = (BootstrapButton) findViewById(R.id.preview_cancel);

        String[] strList;
        latlngList = new ArrayList<>();

        String data = getIntent().getStringExtra("ROUTE");

        String[] d = data.split("coordinates");

        data = d[1].replaceAll(">", "");
        data = data.replaceAll("</", "");
        strList = data.split(" ");
        for(String s : strList){
            String[] tmp = s.split(",");
            latlngList.add(new LatLng(Double.parseDouble(tmp[1]),Double.parseDouble(tmp[0])));
        }

        drawPolyline(latlngList);

        RouteInfoGoogle rig = new RouteInfoGoogle();
        if((int)rig.getTotalDistance(latlngList) < 1000){
            txt.setText("総距離:" + Integer.toString((int) rig.getTotalDistance(latlngList)) + "m");
        }else txt.setText(String.format("総距離:%.3fm",Double.toString(rig.getTotalDistance(latlngList)/1000.0)));


        updateMap(latlngList.get(latlngList.size() / 2));

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputFile = new File(ac.getApplicationContext().getFilesDir(),"HealthlyPeopleRouteID.txt");
                // 出力ストリームの生成
                FileOutputStream fos = null;
                OutputStreamWriter osw = null;
                try {
                    fos = new FileOutputStream(outputFile);
                    osw = new OutputStreamWriter(fos,"UTF-8");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                PrintWriter pw = new PrintWriter(osw);
                pw.println(getIntent().getIntExtra("ID", 0));
                pw.close();
                Toast.makeText(ac,"ルートの保存に成功しました",Toast.LENGTH_SHORT).show();
                ac.finish();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ac.finish();
            }
        });

    }

    /***** Google Mapセットアップ *****/
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.preview_map))
                    .getMap();
            // Check if we were successful in obtaining the map.
        }
    }

    /***** GoogleMap ポリライン描画 *****/
    public void drawPolyline(List<LatLng> latlng){
        PolylineOptions options = new PolylineOptions();
        for(LatLng ll : latlng){
            options.add(ll);
        }
        options.color(Color.GREEN);
        options.width(5);
        options.geodesic(true); // 測地線で表示する場合、地図上で２点間を結ぶ最短曲線
        mMap.addPolyline(options);
    }

    /***** GoogleMap マーカー更新 *****/
    public void updateMap(LatLng latlng) {
        CameraUpdate cu =
                CameraUpdateFactory.newLatLngZoom(
                        latlng, 20f);
        mMap.moveCamera(cu);
    }
}