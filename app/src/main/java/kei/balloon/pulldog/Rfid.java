package kei.balloon.pulldog;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Kazuki on 2015/09/08.
 */
public class Rfid {
    private int id;
    private LatLng location;
    private boolean inDoor;

    public Rfid(int id, double lat, double lng){
        this.id = id;
        location = new LatLng(lat, lng);
        inDoor = false;
    }

    public Rfid(int id){
        this.id = id;
        location = null;
        inDoor = true;
    }

    public boolean isInDoor(){
        return inDoor;
    }

    public LatLng getLatLng(){
        return location;
    }

    public double getLat(){
        return location.latitude;
    }

    public double getLng(){
        return location.longitude;
    }

    public int getId(){
        return id;
    }
}
