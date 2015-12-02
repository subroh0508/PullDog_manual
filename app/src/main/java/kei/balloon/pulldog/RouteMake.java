package kei.balloon.pulldog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class RouteMake extends FragmentActivity {

    //Google Map
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private BootstrapEditText fileId;
    private BootstrapEditText rootName;
    private DataAccess da;
    private String name;
    private String id;
    private File file;
    boolean flag = true;
    private Context context;
    private AlertDialog.Builder alertDialog;
    private TextView disTxt;
    public static TextView txtView;
    private RouteMake rm;
    private List<LatLng> latlngList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_make);
        setUpMapIfNeeded();

        txtView = new TextView(this);
        rm = this;
        StringBuffer strBuf = new StringBuffer();
        BootstrapButton start = (BootstrapButton)findViewById(R.id.start);
        BootstrapButton end = (BootstrapButton)findViewById(R.id.end);
        rootName = (BootstrapEditText)findViewById(R.id.rootName);
        fileId = (BootstrapEditText)findViewById(R.id.id);
        disTxt = (TextView) findViewById(R.id.total_distance);
        context = getApplicationContext();
        da = new DataAccess(rm);
        file = MainActivity.uploadFile;
        alertDialog=new AlertDialog.Builder(this);
        String[] strList;
        latlngList = new ArrayList<>();

        try {
            FileInputStream is = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String data = br.readLine();

            while(data.indexOf("<coordinates>") == -1){
                data = br.readLine();
                if  (data == null) break;
            }
            data = data.replaceAll("\t", "");
            data = data.substring(13, data.length()-15);
            strList = data.split(" ");
            for(String s : strList){
                String[] tmp = s.split(",");
                latlngList.add(new LatLng(Double.parseDouble(tmp[1]),Double.parseDouble(tmp[0])));
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        drawPolyline(latlngList);

        RouteInfoGoogle rig = new RouteInfoGoogle();
        if((int)rig.getTotalDistance(latlngList) < 1000){
            disTxt.setText("総距離:" + Integer.toString((int) rig.getTotalDistance(latlngList)) + "m");
        }else disTxt.setText(String.format("総距離:%.3fm",Double.toString(rig.getTotalDistance(latlngList)/1000.0)));


        updateMap(latlngList.get(latlngList.size() / 2));

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ダイアログの設定
                alertDialog.setTitle("終了");      //タイトル設定
                alertDialog.setMessage("アップロードせずに終了しますか？\n作成したルートは削除されます。");  //内容(メッセージ)設定

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // OKボタン押下時の処理
                        finish();
                    }
                });
                // NG(否定的な)ボタンの設定
                alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // NGボタン押下時の処理
                    }
                });
                // ダイアログの作成と描画
                alertDialog.show();

            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!flag){
                    //Toast.makeText(rm,"記録開始のボタンを押してください",Toast.LENGTH_SHORT).show();
                    return;
                }
                id = String.valueOf(fileId.getText());
                name = String.valueOf(rootName.getText());
                if(id.isEmpty()){
                    Toast.makeText(rm,"IDを入力してください",Toast.LENGTH_SHORT).show();
                    return;
                }else if(name.isEmpty()){
                    Toast.makeText(rm,"ルートの名前が入力されていません",Toast.LENGTH_SHORT).show();
                    return;
                }
                RouteInfoGoogle rig = new RouteInfoGoogle();
                end((int)rig.getTotalDistance(latlngList));
            }
        });

        txtView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                switch (txtView.getText().toString()) {
                    case "fail_01":
                        Toast.makeText(rm,getString(R.string.fail_01),Toast.LENGTH_SHORT).show();
                        break;
                    case "fail_02":
                        Toast.makeText(rm,getString(R.string.fail_02),Toast.LENGTH_SHORT).show();
                        break;
                    case "fail_03":
                        Toast.makeText(rm,getString(R.string.fail_03),Toast.LENGTH_SHORT).show();
                        break;
                    case "fail_04":
                        Toast.makeText(rm,getString(R.string.fail_04),Toast.LENGTH_SHORT).show();
                        break;
                    case "fail_05":
                        Toast.makeText(rm,getString(R.string.fail_05),Toast.LENGTH_SHORT).show();
                        break;
                    case "fail_06":
                        Toast.makeText(rm,getString(R.string.fail_06),Toast.LENGTH_SHORT).show();
                        break;
                    case "fail_07":
                        Toast.makeText(rm,getString(R.string.fail_07),Toast.LENGTH_SHORT).show();
                        break;
                    case "success":
                        Toast.makeText(rm,getString(R.string.success),Toast.LENGTH_SHORT).show();
                        rm.finish();
                        break;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public void start(){
        OutputStream os;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));

            String str = "test";
            os.write(str.getBytes());

            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        flag = true;
    }

    public void end(int dis){
        StringBuffer sb = new StringBuffer();
        try {
            FileInputStream is = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String str;
            while((str = br.readLine()) != null){
                sb.append(str+"\n");
            }
            Log.d("debug", sb.toString());
            br.close();
            da.fileSave(sb.toString(),id,name,Integer.toString(dis));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***** Google Mapセットアップ *****/
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.kenjosya_map))
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
