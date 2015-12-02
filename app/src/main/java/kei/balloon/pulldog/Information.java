package kei.balloon.pulldog;

import com.google.android.gms.maps.model.LatLng;

public class Information
{
	private LatLng location;
	private String content;
	private int category;
	private int tagId;
    private boolean isInDoor, notified = false;
    private int id;

	public final static int TWEET        = 1;
	public final static int SYSTEM_INFO  = 2;
	public final static int SYSTEM_ALERT = 3;
	public final static int USER_INFO    = 4;
	public final static int USER_ALERT   = 5;
    public final static int USER_FORMAT  = 6;

    public final static String TWEET_MSG = "があります。";
    public final static String SYSTEM_INFO_MSG = "があります。";
    public final static String SYSTEM_ALERT_MSG = "があります、注意してください。";
    public final static String USER_INFO_MSG = "があります。";
    public final static String USER_ALERT_MSG = "があります、注意してください。";
    public final static String USER_FORMAT_MSG = "";

    public final static double REGIST_DISTANCE = 5.0;
    public final static double NOTIFY_DISTANCE = 2.0;
    public final static int DATABASE_OFFSET = 10000;

	public Information(int idNum, double latitude, double longitude, String str, int categoryNum)
	{
		this(idNum, latitude, longitude, -1, str, categoryNum);
	}

	public Information(int idNum, int tagIdNum, String str, int categoryNum){
        id = idNum;
		location = null;
		content = str;
		category = categoryNum;
		tagId = tagIdNum;
        isInDoor = true;

	}

	public Information(int idNum, double latitude, double longitude, int tagIdNum, String str, int categoryNum)
	{
        id = idNum;
		location = new LatLng(latitude, longitude);
		content = str;
		category = categoryNum;
		tagId = tagIdNum;
        isInDoor = false;
	}

    public boolean isInDoor(){
        return isInDoor;
    }

    public double getLat(){
        return location.latitude;
    }

    public double getLng(){
        return location.longitude;
    }

    public LatLng getLatLng(){
        return location;
    }

    public String getContent(){
        String assistMsg;
        switch (category){
            case TWEET:
                assistMsg = TWEET_MSG;
                break;

            case SYSTEM_INFO:
                assistMsg = SYSTEM_INFO_MSG;
                break;

            case SYSTEM_ALERT:
                assistMsg = SYSTEM_ALERT_MSG;
                break;

            case USER_INFO:
                assistMsg = USER_INFO_MSG;
                break;

            case USER_ALERT:
                assistMsg = USER_ALERT_MSG;
                break;

            case USER_FORMAT:
                assistMsg = USER_FORMAT_MSG;
                break;

            default:
                assistMsg = "";
                break;
        }
        return content + assistMsg;
    }

    public void notified(){
        notified = true;
    }

    public void clearNotified(){
        notified = false;
    }

    public boolean isNotified(){
        return notified;
    }

    public int getId(){
        return id;
    }

    public int getTagId(){
        return tagId;
    }

	public static void main(String[] args){
		
	}
}
