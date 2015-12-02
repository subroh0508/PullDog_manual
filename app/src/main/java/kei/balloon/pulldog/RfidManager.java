package kei.balloon.pulldog;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazuki on 2015/09/09.
 */
public class RfidManager {
    private List<Rfid> tagList;
    private MainActivity context;

    public RfidManager(MainActivity ma,  String tagListName){
        context = ma;
        tagList = new ArrayList<Rfid>();
        importData(tagListName);
    }

    public boolean isExist(int tagId){
        for (Rfid r : tagList){
            if  (r.getId() == tagId) return true;
        }
        return false;
    }

    private void importData(String fileName){
        String line;
        String[] tmp;
        int uId;
        double lat, lng;
        try {
            // 入力ストリームの生成
            InputStream fis = context.getAssets().open(fileName);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            //読み込み
            while ((line = br.readLine()) != null){
                tmp = line.split(",");
                if  (tmp.length == 2) {
                    uId = Integer.parseInt(tmp[0]);
                    tagList.add(new Rfid(uId));
                } else {
                    uId = Integer.parseInt(tmp[0]);
                    lat = Double.parseDouble(tmp[1]);
                    lng = Double.parseDouble(tmp[2]);
                    tagList.add(new Rfid(uId, lat, lng));
                }
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    public Rfid getRfidById(int idNum){
        for (Rfid r : tagList){
            if  (r.getId() == idNum) return r;
        }
        return null;
    }
}
