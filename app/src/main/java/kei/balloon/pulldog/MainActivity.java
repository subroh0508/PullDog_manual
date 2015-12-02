package kei.balloon.pulldog;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;



public class MainActivity extends FragmentActivity implements RecognitionListener, Runnable, TextToSpeech.OnInitListener, MiconButtonPushListener, SensorEventListener{

    static final private int REQUEST_CODE_DESTINATION = 1;
    static final private int REQUEST_CODE_TWITTER = 2;
    static final private int REQUEST_CODE_TWEET = 3;
    static final private int REQUEST_CODE_TWEET_RETURN = 5;
    static final private int REQUEST_CODE_UPLOAD = 6;
    static final private int REQUEST_CODE_HEALTHLY = 7;
    static final private int ROUTE_NAVI = 1;
    static final private int PULLDOG_PULLDOG = 1;
    static final private int GOOGLE_GOOGLE = 2;
    static final private int PULLDOG_GOOGLE = 3;
    static final private int GOOGLE_PULLDOG = 4;
    static final private int TWITTER = 2;
    static final private int DEPARTURE = 1;
    static final private int DESTINATION = 2;
    static final private int DIALOG_1 = 1;
    static final private int DIALOG_2 = 2;

    //出発地・目的地の検索範囲(八王子市周辺、適宜変更OK)
    private final static double LOWER_LEFT_LATITUDE = 35.61237;
    private final static double LOWER_LEFT_LONGTUDE = 139.22767;
    private final static double UPPER_RIGHT_LATITUDE = 35.64599; //old:  35.69759
    private final static double UPPER_RIGHT_LONGITUDE = 139.35495; //old: 139.37633

    //GoogleMapでの案内関連のパラメータ
    private final static int ROUTE_DIS = 10;        //ルートからの距離
    private static final int CORNER_IN = 10;        //コーナーに入ったことにする距離
    private static final int FIFTYMETER_NOTIFY_MARGIN = 55;     //何m手前からコーナーの注意喚起をするか

    //ソレノイド駆動周波数
    //private static final byte SOLENOID_HZ = 12;
    private static final byte SOLENOID_HZ = 1;


    //ソレノイド駆動時間
    private static final int SOLENOID_ONTIME = 4;

    public static MainActivity ma;

    //Google Map
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public Marker mLocation = null;
    private GroundOverlay pLine;
    private GroundOverlay rDot, noticeDot;

    //GoogleMap Navigation
    private Geocoder mGC;

    //private LatLng lLL;
    private List<LatLng> mPointList, mCorners;
    private String[] mIndicator;
    private int GoogleTotalDistance;
    private int PulldogTotalDistance;
    private int mDumpDistance;
    private Marker mMarker;
    private MarkerOptions mOptions;
    private MapInfo mMI;
    private List<List<HashMap<String, String>>> RouteList;
    private boolean gMapNaviFlag = false;       //案内中かどうかのフラグ
    private boolean gMapNaviReserve = false;    //案内が控えているかどうかのフラグ

    public TextView googleNaviStart;
    private RouteInfoGoogle rtInfoGoogle;

    private int depAndDisType = 0;  //出発地と目的地の種類  1:両方PULLDOG 2:両方Google 3:出PULLDOG目Google 4:目Google出PULLDOG

    //ナビゲーション
    public Navi navi;
    private Landmark departure = null;
    private Landmark destination = null;
    public int vId = -11;
    private Route route;
    private Polyline rtPolyLine;
    private final static int OUT = 0;
    private final static int IN  = 1;
    private double distanceNextTo;


    // USB1関係変数定義
    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private UsbEndpoint mEndpointOut;
    private UsbEndpoint mEndpointIn;
    private byte[] message = new byte[64];
    private byte[] buffer = new byte[64];
    private final static int USB_SLEEP = 50;
    private final static int USB_INTERRUPT_HZ = (int)(1000/(double)USB_SLEEP);
    private final static double USB_INTERRUPT_TOSEC_FACTOR = 1.0/USB_INTERRUPT_HZ;



    //コンパス
    float[] accelerometerValues = new float[3];
    float[] magneticValues = new float[3];
    private SensorManager sensor;
    private int MATRIX_SIZE = 16;
    private int DIMENSION = 3;

    //GUI関連クラス定義
    private TextView ltlgText;  //緯度・経度確認用
    private TextView srcText;   //みちびきかGPSかを表示
    private TextView pSWText;   //プッシュスイッチ確認用
    private TextView departureTV; //出発地点
    private TextView destinationTV; //目的地
    private TextView latTextV;
    private TextView lngTextV;
    private Button latupBt;
    private Button latdownBt;
    private Button lngupBt;
    private Button lngdownBt;
    private EditText tagEtxt;
    private TextView directionTxt;
    private TextView orientTxt;
    private TextView rtdisTxt;
    private Button idUp;
    private Button idDown;
    private ToggleButton positionSwtich;
    private Button tagBtn;

    private ImageButton pullDogIcon;  //PULLDOGのICON

    // コマンド用定数定義
    private static final byte COMMAND_CHECK = 0x30;		// マイコンとの接続チェック用コマンド（未使用）
    private static final byte COMMAND_LED = 0x31;		// LED制御用コマンド
    private static final byte COMMAND_LCD = 0x32;		// LCD制御用コマンド
    private static final byte COMMAND_SOL = 0x33;		// ソレノイド用コマンド
    private static final byte COMMAND_STATUS = 0x40;	// マイコンの状態取得用コマンド

    //マイコンのボタン関係
    private MiconButtonPushNotify mn = null;    //マイコンのButtonが押されたかどうかの通知クラス
    private static final int BTN_PUSH_LENGTH = 20;
    private static final int CHATTERING_RANGE = 1;
    private boolean btnEnable = true;

    // USB受信ハンドラのインスタンス生成
    private final Handler handler = new Handler();

    //USB Thread
    private Thread usbThread;
    private boolean usbThreadFlag = true;

    // アプリ用変数
    private byte flag;  //USB関連
    private int result; //USB関連
    private int buttonCounter1 = 0; //PushSW1押した時間判定用
    private int buttonCounter2 = 0; //PushSW2押した時間判定用
    private boolean onTOoff1 = false;
    private boolean onTOoff2 = false;
    private boolean offTOon1 = false;
    private boolean offTOon2 = false;
    private int Mode = 0;                 //アプリのモード切替　　1：目的地・出発地入力　2：不満・ピンポイントな情報アップロード
    private int depaOrdest = 0;           //出発地or目的地      1：出発地点　2：目的地点
    private int dialogMode = 0;           //dialogのMODE      1:音声入力確認　2：住所確認
    private double dummyLat = 35.64505f;
    private double dummyLng = 139.35259f;
    private boolean cameraFocus = true;            //現在地をフォーカス   true:有効 false:無効

    //音声読み上げ
    private TextToSpeech txtToSpeech;
    private List<String> speechList = new ArrayList<>();
    private int[] visitedTag = new int[1000];

    //音声認識
    private Intent intent;  //音声認識用 Intent
    private ArrayList<String> results;  //音声認識候補リスト
    private String departureName = null; //出発地の名前
    private String destinationName; //目的地の名前
    private TextView deTxtView; //出発地点・目的地点が入る
    private String tweetStr = null; //Tweetの内容
    private SpeechRecognizer mSpeechRecognizer;
    private static String YAHOO_ID = "dj0zaiZpPU5yQ01xV01GNnp5NyZzPWNvbnN1bWVyc2VjcmV0Jng9M2I-";
    //public  String furi;
    private String departureNameHira = null;
    private String destinationNameHira = null;

    //GeaoCoder
    private Geocoder geocoder;
    LinkedList<Landmark> addressList;  //GeoCoderによる検索結果のリスト
    Landmark address;    //一時的に候補の場所を格納


    //みちびき(QZ1・FTDI)
    private static D2xxManager ftD2xx = null;
    private FT_Device ftDevice = null;
    static final int READBUF_SIZE = 256;
    byte[] rbuf = new byte[1]; //受信モジュールから生データを受ける
    char[] rchar = new char[READBUF_SIZE]; //生データを文字データ変換して受ける
    int mReadSize = 0; //受けたデータのサイズ

    public String[] rNMEA;
    public String data;
    private NowLocation mNowLocation; //
    private QZSS mQZSS;


    public double[] point = {dummyLat, dummyLng, 0.0};

    boolean mThreadIsStopped = true;
    Handler mHandler= new Handler();
    boolean mDataIsRead = false;

    //RFID
    private int tagId = -1;
    private int pastId = -1;

    //ファイルアップロード用変数
    public static File uploadFile;

    //健常者ルート作成モード
    private LinearLayout infoLinearLayout;
    private BootstrapEditText infoEdTxt;
    private RadioGroup infoType;
    private BootstrapButton registBtn;
    private DataAccess da;
    public static TextView hRouteTxt;
    private Route hRoute = null;

    //危険情報
    public static List<PointParameter> alartList = new ArrayList<>();

    //スポット情報
    public static List<PointParameter> spotList = new ArrayList<>();

    //Tweet
    public static List<PointParameter> tweetList = new ArrayList<>();

    private int AlartSpotTweet = 0;


    //力覚デバイス
    private byte[] Lside = { 1, 1, 1, 1, 1,
                             0, 0, 0, 0, 0,
                             0, 0, 0, 0, 0,
                             0, 0, 0, 0, 0,};

    private byte[] Rside = { 0, 0, 0, 0, 0,
                             1, 1, 1, 1, 1,
                             1, 1, 1, 1, 1,
                             1, 1, 1, 1, 1,};
    private int onTime = -1;

    //時間用変数
    int usbInterruptCount = 0;
    int systemSec = 0;

    //起動読み上げ用変数
    int connectedDeviceCount = 0;
    boolean usbReady = false, gpsReady = false;
    boolean isSpeakedInit = false;

    //テスト用テキストボックス
    TextView tx1;
    TextView tx2;
    TextView tx3;
    TextView tx4;

    //RFID通信テスト用変数
    private byte[] rfidTag = new byte[100];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ma =this;
        setUpMapIfNeeded();

        //メイン画面のアニメーション
        View view = findViewById(R.id.main_bottom);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.translate_bottom);
        view.setAnimation(anim);

        // TextToSpeechオブジェクト生成
        txtToSpeech = new TextToSpeech(this, this);

        //Naviオブジェクト生成
        navi = new Navi(this);
        //navi.setCurrentLocation(new PointD(139.35259, 35.64505));     //現在地初期設定
        navi.setCurrentLocation(10010);

        String s = null;
        DataAccess daServer = new DataAccess(this);
        File inputFile = new File(this.getApplicationContext().getFilesDir(),"HealthlyPeopleRouteID.txt");
        BufferedReader br = null;
        try {
            FileInputStream fis = new FileInputStream(inputFile);
            InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
            br = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            if(br != null) {
                s = br.readLine();
                daServer.fileGet(Integer.parseInt(s));
            }

            daServer.get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        hRouteTxt = new TextView(this);



        hRouteTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String[] strList;
                List<LatLng> latlngList = new ArrayList<>();
                String data = hRouteTxt.getText().toString();

                String[] d = data.split("coordinates");

                data = d[1].replaceAll(">", "");
                data = data.replaceAll("</", "");
                strList = data.split(" ");
                for (String str : strList) {
                    String[] tmp = str.split(",");
                    latlngList.add(new LatLng(Double.parseDouble(tmp[1]), Double.parseDouble(tmp[0])));
                }

                //drawPolyline(latlngList);
                List<Vertex> vList = new ArrayList<Vertex>();
                int i = 0;
                Landmark sta = new Landmark(latlngList.get(0).longitude, latlngList.get(0).latitude, 0, "Start", true);
                Landmark end = new Landmark(latlngList.get(latlngList.size() - 1).longitude, latlngList.get(latlngList.size() - 1).latitude, latlngList.size(), "End", true);
                for (LatLng l : latlngList) {

                    vList.add(new Vertex(i, new PointD(l.longitude, l.latitude), Vertex.OUT));
                    i++;
                }
                List<Edge> eList = new ArrayList<Edge>();
                for (i = 0; i < latlngList.size() - 1; i++) {
                    eList.add(new Edge(latlngList.get(i).longitude, latlngList.get(i).latitude,
                            latlngList.get(i + 1).longitude, latlngList.get(i + 1).latitude, i, "test"));
                }

                hRoute = new Route(vList, sta, end, new RouteInfoGoogle().getTotalDistance(latlngList), eList);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        /**************/

        //テスト用テキストボックス
        tx1 = (TextView)findViewById(R.id.textView);
        tx2 = (TextView)findViewById(R.id.textView2);
        tx3 = (TextView)findViewById(R.id.textView3);
        tx4 = (TextView)findViewById(R.id.textView4);

        //音声認識
        mSpeechRecognizer = null;


        //コンパス
        sensor = (SensorManager)getSystemService(SENSOR_SERVICE);

        //GeoCoderオブジェクト生成
        geocoder = new Geocoder(this);

        //GUI関連変数
        ltlgText = (TextView)findViewById(R.id.latlng);
        srcText = (TextView)findViewById(R.id.data_source);

        destinationTV = (TextView) findViewById(R.id.destination_txt);
        departureTV = (TextView) findViewById(R.id.departure_txt);
        latTextV = (TextView) findViewById(R.id.lat);
        lngTextV = (TextView) findViewById(R.id.lng);
        latupBt = (Button) findViewById(R.id.lat_up);
        latdownBt = (Button) findViewById(R.id.lat_down);
        lngupBt = (Button) findViewById(R.id.lng_up);
        lngdownBt = (Button) findViewById(R.id.lng_down);
        tagEtxt = (EditText) findViewById(R.id.tag_num);
        tagBtn = (Button) findViewById(R.id.tag_btn);
        directionTxt = (TextView) findViewById(R.id.direction);
        orientTxt = (TextView) findViewById(R.id.orient_txt);
        rtdisTxt = (TextView) findViewById(R.id.rt_pos_txt);
        idUp = (Button) findViewById(R.id.id_up);
        idDown = (Button) findViewById(R.id.id_down);
        positionSwtich = (ToggleButton) findViewById(R.id.position_tgl);

        //健常者モード移行用ボタン
        pullDogIcon = (ImageButton) findViewById(R.id.pd_icon);

        //健常者モード(情報入力フォーム)
        infoLinearLayout = (LinearLayout) findViewById(R.id.information_layout);
        infoEdTxt = (BootstrapEditText) findViewById(R.id.info_edittext);
        infoType = (RadioGroup) findViewById(R.id.info_type);
        registBtn = (BootstrapButton) findViewById(R.id.regist_btn);
        da = new DataAccess(this);


        //Google案内開始用
        googleNaviStart = new TextView(this);

        //地点入力用
        deTxtView = (TextView)findViewById(R.id.route);


        pSWText = (TextView)findViewById(R.id.pushsw_text);
        pSWText.setVisibility(View.GONE);
        // MICONのBUTTONのリスナ通知用クラスのインスタンス化
        mn = new MiconButtonPushNotify(pSWText);
        // MICONのBUTTONのリスナ通知用クラスに通知先のインスタンスを付加
        mn.setListener(this);

        //みちびき FTDIのインスタンスをとってくる
        mNowLocation = new NowLocation();
        mQZSS = new QZSS();

        try {
            ftD2xx = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException e){
            Log.e("TAG", e.toString());
        }

        // USBホストAPIインスタンス生成
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);


        //navi初期化系
        for (int i=0; i<1000; i++) visitedTag[i] = -1;
        positionSwtich.setChecked(true);
        navi.enableRfid();

        //目的地入力用テキストボックスのリスナ
        deTxtView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /***** 検索結果確認 *****/
                String hira = deTxtView.getText().toString();
                if(depaOrdest == DEPARTURE){
                    departureNameHira = hira;
                }else if(depaOrdest == DESTINATION){
                    destinationNameHira = hira;
                }
                addressList = navi.getLandmarksByName(hira);
                address = addressList.poll();
                if (address != null) {
                    StringBuffer st = new StringBuffer("");
                    st.append(address.getName());
                    st.append("");
                    speechText(st.toString() + "ですか?");
                }else if (address == null){
                    if(depaOrdest == DEPARTURE){
                        depAndDisType = GOOGLE_GOOGLE;
                        departureTV.setText(departureName);
                        speechText("出発地は"+departureTV.getText()+"に設定されました。");
                        enableLongPushSW();
                    }else if(depaOrdest ==DESTINATION){
                        if(depAndDisType == PULLDOG_PULLDOG) {
                            //出発地と目的地がPULLDOGとGoogleで異なったとき
                            depAndDisType = PULLDOG_GOOGLE;
                            if (departureName != null) { //出発地点がnullじゃなかったら、出発地点から目的地点
                                if(nameToLatLng(destinationName) == null) return;
                                createRoutePulldogAndGoogle(departure, nameToLatLng(destinationName));
                            }else if (departureName == null) { //出発地点がnullだったら、現在地から目的地
                                if(nameToLatLng(destinationName) == null) return;
                                LatLng l = new LatLng(point[0], point[1]);
                                //createRoutePulldogAndGoogle(l, nameToLatLng(destinationName));
                            }
                            departureName = null;
                            destinationName = null;
                            departureNameHira =null;
                            destinationNameHira = null;
                        }else {
                            //出発地と目的地がGoogle

                            if (departureName != null)  //出発地点がnullじゃなかったら、出発地点から目的地点
                                if(!departureName.equals(destinationName)) {
                                    getDepartureAndDistinationByName(departureName, destinationName);
                                    departureName = null;
                                    departureNameHira =null;
                                }else speechText("出発地と目的地が同じです。もう一度入力を行ってください。");
                            else if (departureName == null) { //出発地点がnullだったら、現在地から目的地
                                if (nameToLatLng(destinationName) == null) {
                                } else {
                                    LatLng l = new LatLng(point[0], point[1]);
                                    createRoutePulldogAndGoogle(l, nameToLatLng(destinationName));
                                }
                            }
                            destinationName = null;
                            destinationNameHira = null;
                        }
                        enableLongPushSW();
                    }

                    Mode = 0;
                    dialogMode = 0;
                    depaOrdest = 0;
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        }); //目的地が入ったら検索を行うようにするため

        //Google案内開始用
        googleNaviStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getRouteFromGoogleMap();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        pullDogIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 指がタッチした時の処理を記述
                    /***** 音声入力(目的地点) *****/
                    Mode = 0;
                    depaOrdest = DEPARTURE;
                    //音声認識
                    mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(ma);
                    mSpeechRecognizer.setRecognitionListener(ma);
                    try {
                        Intent voiceIntent = RecognizerIntent.getVoiceDetailsIntent(getApplicationContext());
                        mSpeechRecognizer.startListening(voiceIntent);
                    } catch (ActivityNotFoundException e) {
                        // 端末が音声認識に対応していない場合
                        speechText(getString(R.string.Incompatible));
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // タッチした指が離れた時の処理を記述
                    if(mSpeechRecognizer != null) mSpeechRecognizer.cancel();
                }
                return false;
            }
        });

        //健常者ルート作成モード(Information)
        registBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int infoT = 0;
                String info = null;
                if(!infoEdTxt.getText().toString().isEmpty()) {
                    info = infoEdTxt.getText().toString();
                }else{
                    Toast.makeText(ma,"情報が入力されていません",Toast.LENGTH_SHORT).show();
                    return;
                }
                int checkedId = infoType.getCheckedRadioButtonId();
                if (-1 != checkedId) {
                    if(checkedId == R.id.radio_btn_kiken){
                        infoT = 5;
                    }else if(checkedId == R.id.radio_btn_spot){
                        infoT = 4;
                    }
                 } else {
                    Toast.makeText(ma,"情報の種類が入力されていません",Toast.LENGTH_SHORT).show();
                    return;
                }
                da.save(info,String.valueOf(point[0]),String.valueOf(point[1]),String.valueOf(infoT));
                infoEdTxt.setText("");
            }
        });


        //緯度経度を上げ下げするボタン
        latupBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //point[0] += GraphProcesser.METER_OF_LATITUDE;
                //drawDot(new LatLng(dummyLat,dummyLng),Color.BLUE);
                //processClickAction(OUT);
            }
        });
        latdownBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //point[0] -= GraphProcesser.METER_OF_LATITUDE;
                //drawDot(new LatLng(dummyLat,dummyLng),Color.BLUE);
                //processClickAction(OUT);
            }
        });
        lngupBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //point[1] += GraphProcesser.METER_OF_LONGITUDE;
                //drawDot(new LatLng(dummyLat,dummyLng),Color.BLUE);
                //processClickAction(OUT);
            }
        });
        lngdownBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //point[1] -= GraphProcesser.METER_OF_LONGITUDE;

                //drawDot(new LatLng(dummyLat,dummyLng),Color.BLUE);
                //processClickAction(OUT);
            }
        });

        //RFID ID を上げ下げするボタン
        idUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                try {
                    if(tagEtxt.getText().length() != 0) {
                        tagId = Integer.parseInt(tagEtxt.getText().toString());
                        tagId++;
                    }
                    tagEtxt.setText(Integer.toString(tagId));

                    processClickAction(IN);
                } catch (Exception e){
                    e.printStackTrace();
                }
                */

            }
        });
        idDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                try {
                    if(tagEtxt.getText().length() != 0) {
                        tagId = Integer.parseInt(tagEtxt.getText().toString());
                        tagId--;
                    }//現在地設定
                    tagEtxt.setText(Integer.toString(tagId));

                    processClickAction(IN);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                */
            }
        });

        tagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (navi.getNotifyMode() == navi.NEW_MODE){
                    navi.enableConventionalMode();
                    tagBtn.setText("CONV");
                }
                else{
                    navi.enableNewMode();
                    tagBtn.setText("NEW");
                }
            }
        });

        //測位モード
        positionSwtich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //positionSwtich.setChecked(!positionSwtich.isChecked());
                if  (isChecked == Navi.RFID){
                    navi.enableRfid();
                }
                else {
                    navi.enableGps();
                }
            }
        });
    }

    /***** Google Map による道案内(出発地点と目的地点入力 String ver) *****/
    private void getDepartureAndDistinationByName(String start,String end){
        LatLng s = new LatLng(0.0,0.0);
        LatLng e = new LatLng(0.0,0.0);
        List<Address> addresses1 = null, addresses2 = null;
        mGC = new Geocoder(MainActivity.this);

        try {
            //出発地点
            addresses1 = mGC.getFromLocationName(start, 3,
                    LOWER_LEFT_LATITUDE, LOWER_LEFT_LONGTUDE, UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE);
            //目的地点
            addresses2 = mGC.getFromLocationName(end, 3,
                    LOWER_LEFT_LATITUDE, LOWER_LEFT_LONGTUDE, UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE);
        } catch(IOException erro) {
            Toast.makeText(MainActivity.this, "IOException", Toast.LENGTH_SHORT).show();
            speechText("検索結果が見つかりません。");
            return;
        }
        if(addresses1.size() != 0 && addresses2.size() != 0) {
            s = new LatLng(addresses1.get(0).getLatitude(), addresses1.get(0).getLongitude());
            e = new LatLng(addresses2.get(0).getLatitude(), addresses2.get(0).getLongitude());
            getDepartureAndDistinationByLatLng(s,e);
        } else {
            //目的地or出発地が入ってなかった
            speechText(getString(R.string.search_fail));
            return;
        }

    }
    private LatLng nameToLatLng(String name) {
        Address a;
        mGC = new Geocoder(MainActivity.this);
        try {
            a = mGC.getFromLocationName(name, 3,
                    LOWER_LEFT_LATITUDE, LOWER_LEFT_LONGTUDE, UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE).get(0);
            LatLng ll = new LatLng(a.getLatitude(),a.getLongitude());
            return ll;
        }catch (Exception e){
            speechText("検索結果が見つかりません。");
            return null;
        }

    }

    /***** Google Map による道案内(出発地点と目的地点入力 LatLng Ver) *****/
    private void getDepartureAndDistinationByLatLng(LatLng s,LatLng e) {
        mMI = new MapInfo(this, s, e);            //Google Maps APIからルートをとってくる(別クラス)
    }

    /***** Google Map による道案内(ルート作成) *****/
    private void getRouteFromGoogleMap(){
        mCorners = new ArrayList<>();       //曲がり角
        RouteList = mMI.getRoute();         //ルート全体の情報を取得
        rtInfoGoogle = new RouteInfoGoogle(RouteList);

        if(RouteList != null){
            //ルートの表示
            drawPolyline(rtInfoGoogle.getPointList());

            GoogleTotalDistance = (int)rtInfoGoogle.getTotalDistance();   //総距離(初期値)
            mDumpDistance = GoogleTotalDistance;                     //残り(初期値)：総距離と同じ

            //現在地
            //dummyLat = Double.valueOf(RouteList.get(0).get(0).get("lat"));
            //dummyLng = Double.valueOf(RouteList.get(0).get(0).get("lng"));
            int type = depAndDisType;

            if(type == GOOGLE_GOOGLE)
                speechText("ルート案内を開始します。ルートの総距離は"+GoogleTotalDistance+"メートルです。");
            else
                speechText("ルート案内を開始します。ルートの総距離は"+(PulldogTotalDistance+GoogleTotalDistance)+"メートルです。");
        }

    }
    /***** Google Map による道案内(道案内中の動作) *****/
    private void operateGoogleMapNaviMode() {

        PointParameter nearest = rtInfoGoogle.getNearestCorner(point[0],point[1]);
        int r = (int) rtInfoGoogle.getRemainDistance(point[0], point[1]);

        if (nearest != null) {
            int d = (int) rtInfoGoogle.getDistance(point[0], point[1], nearest.latitude, nearest.longitude);

            if (d <= FIFTYMETER_NOTIFY_MARGIN && d % CORNER_IN == 0)
                //50m手前から注意
                if (!nearest.isVisited())
                    speechText(d+"メートル先、"+nearest.getInformation());

            if (d < CORNER_IN)
                //コーナーに10mまで近づいたら次から注意しない
                rtInfoGoogle.setFlag(nearest);

            if (r - mDumpDistance > ROUTE_DIS) {
                //ルートから外れている
            } else {
                //ルート上にいる
            }

            //残りの距離と総距離
            String str = "Remain:" + r + "[m] Total:" + GoogleTotalDistance + "[m]";

            if (r <= mDumpDistance) mDumpDistance = r;
        } else {
            if(CORNER_IN <= r && r <= FIFTYMETER_NOTIFY_MARGIN && r%CORNER_IN == 0)
                speechText(r+"メートル先、目的地です");
            else if(r < CORNER_IN-5 && gMapNaviFlag) {
                speechText(getString(R.string.finish));
                gMapNaviFlag = false;
                navi.startRouting();
            }
        }

        updateMap(point[0], point[1]);
    }

    /***** PULLDOGとGoogle Mapによる複合ルート作成 *****/
    private void createRoutePulldogAndGoogle(Landmark start, LatLng end){
        //PULLDOG TO GOOGLE

        depAndDisType = 0;
        Edge relay;
        List<Edge> edges = navi.getAllEdge();

        relay = GraphProcesser.getNearestEdge(edges, end);

        //PULLDOGの案内でLatLngからEdgeへの案内が必要

        Route rt = navi.generateRoute(start, relay.getNearerLatLng(end));
        getDepartureAndDistinationByLatLng(relay.getNearerLatLng(end), end);

        drawRoute(rt);
        PulldogTotalDistance = (int)rt.getTotalDistance();
        navi.startRouting();
        gMapNaviFlag = false;
        gMapNaviReserve = true;

        Log.e("ROUTE", "P to G");
    }
    private void createRoutePulldogAndGoogle(LatLng start, Landmark end){
        depAndDisType = 0;

        //GOOLGE TO PULLDOG
        Edge relay;
        List<Edge> edges = navi.getAllEdge();

        relay = GraphProcesser.getNearestEdge(edges, start);

        //PULLDOGの案内でLatLngからEdgeへの案内が必要

        getDepartureAndDistinationByLatLng(start, relay.getNearerLatLng(start));
        Route rt = navi.generateRoute(relay.getNearerLatLng(start), end);
        gMapNaviFlag = true;
        gMapNaviReserve = false;
        drawRoute(rt);
        PulldogTotalDistance = (int)rt.getTotalDistance();

        Log.e("ROUTE", "G to P");
    }
    private void createRoutePulldogAndGoogle(LatLng start, LatLng end){
        depAndDisType = 0;

        //PULLDOG TO GOOGLE
        Edge relay;
        List<Edge> edges = navi.getAllEdge();

        relay = GraphProcesser.getNearestEdge(edges, end);

        //PULLDOGの案内でLatLngからEdgeへの案内が必要

        getDepartureAndDistinationByLatLng(relay.getNearerLatLng(end) , end);
        Route rt = navi.generateRouteFromCurrentLocation(relay.getNearerLatLng(start));
        gMapNaviFlag = false;
        gMapNaviReserve = true;
        navi.startRouting();
        drawRoute(rt);

        Log.e("ROUTE", "G to P");
    }

    /***** デバック用？移動中の動作 *****/
    private void processClickAction(int state) {
        /*int status;

        Log.d("TAG","status:"+state);
        if  (!navi.isHealthlyRoute()) {

            switch (state) {
                case OUT:
                    if (gMapNaviFlag) {
                        operateGoogleMapNaviMode();
                        return;
                    }

                    status = navi.setCurrentLocation(point[0], point[1]);
                    updateMap(point[0], point[1]);

                    if (status == Navi.ROUTE_FINISH && gMapNaviReserve) {
                        gMapNaviFlag = true;
                        gMapNaviReserve = false;
                        navi.finishRouting();
                    }
                    else if (status == Navi.ROUTE_FINISH) {
                        speechText(getString(R.string.finish));
                        navi.finishRouting();
                    }

                    if (navi.isRecording()) navi.insertRoute(point[0], point[1]);

                    if (navi.isRouting()) {
                        //ルートへの最短距離を取得
                        double distanceToRoute = navi.getDistanceToNearestEdge();
                        rtdisTxt.setText("距離：" + Double.toString(distanceToRoute));

                        //最短距離をとる点を赤ドットで描画
                        if (rDot != null) rDot.remove();
                        //rDot = drawDot(navi.getLatLngToNearestEdge(), Color.RED); //todo 10/2
                        drawDot(navi.getLatLngToNearestEdge(), Color.RED);

                        // todo added by sakaue
                        Vertex nearestv;
                        nearestv = navi.getCurrentVertex();

                        if(nearestv != null) {
                            distanceNextTo = navi.getDistanceToNearestEdge()
                                    + GraphProcesser.toMeter(nearestv.getLatLng(), navi.getLatLngToNearestEdge());
                        } else
                            distanceNextTo = navi.getDistanceToNearestEdge();


                        if (navi.isInSafeArea(point[0], point[1]) && (distanceToRoute > 5 || distanceToRoute < 0) && !navi.isCurrentRouteEndInDoor()) {
                            Route route = navi.generateRoute(navi.getDestinationName());
                            if (route == null) {
                                //speechText("ルート NULUPO!");
                                List<Edge> tmp = navi.getAllEdge();
                                List<LatLng> latlngtmp = new ArrayList<>();
                                for (Edge t : tmp) {
                                    latlngtmp.add(t.getVertexsLatLng()[0]);
                                    latlngtmp.add(t.getVertexsLatLng()[1]);
                                    drawPolyline(latlngtmp);

                                    latlngtmp.clear();
                                }

                            } else drawRoute(route);
                        }


                        // todo 近いスポットよむとこ
                        //ルートに最も近い危険箇所orスポットを通知
                        int n = getNearestInfo();
                        //Toast.makeText(this, "alartList:"+alartList.size()+",spotList:"+spotList.size()+",tweetList:"+tweetList.size(), Toast.LENGTH_SHORT).show();

                        if(noticeDot != null) noticeDot.remove();
                        if(n != -1) {
                            double lat, lng;
                            switch (AlartSpotTweet) {
                                case 1:
                                    //speechText("3メートル以内に"+alartList.get(n).getInformation()+"があります。注意してください。");
                                    lat = alartList.get(n).latitude;
                                    lng = alartList.get(n).longitude;
                                    //noticeDot = drawDot(new LatLng(lat, lng), Color.RED); //todo 10/2
                                    drawDot(new LatLng(lat, lng), Color.RED);
                                    alartList.get(n).setVisitedFlag();
                                    break;
                                case 2:
                                    //speechText("3メートル以内に"+spotList.get(n).getInformation()+"があります。");
                                    lat = spotList.get(n).latitude;
                                    lng = spotList.get(n).longitude;
                                    drawDot(new LatLng(lat, lng), Color.BLUE);
                                    spotList.get(n).setVisitedFlag();
                                    break;
                                case 3:
                                    //speechText("3メートル以内に"+tweetList.get(n).getInformation()+"があります。");
                                    lat = tweetList.get(n).latitude;
                                    lng = tweetList.get(n).longitude;
                                    drawDot(new LatLng(lat, lng), Color.GREEN);
                                    tweetList.get(n).setVisitedFlag();
                                    break;
                            }
                        }

                        AlartSpotTweet = 0;
                        //Toast.makeText(this, "Routing.", Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(this, "No Routing.", Toast.LENGTH_SHORT).show();
                    }

                    if  (navi.getCurrentRoute() != null) {
                        //Toast.makeText(this, "getinformaition.size():" + navi.getCurrentRoute().getInformation().size(), Toast.LENGTH_SHORT).show();
                    }

                    for (Information i : navi.getNotify()) {
                        speechText(i.getContent());
                        rtdisTxt.setText(i.getContent());
                        i.notified(); //通知フラグを立てて置く。以降読みません。
                    }
                    break;

                case IN:
                    //現在地設定
                    status = navi.setCurrentLocation(tagId);


                    if (status == Navi.ROUTE_FINISH)
                        speechText(getString(R.string.finish));

                    Log.d("TAG", "navi.isRouting():"+navi.isRouting());
                    if (navi.isRouting()) {
                        Toast.makeText(this, navi.getCurrentDirection(), Toast.LENGTH_SHORT).show();
                        speechText(navi.getCurrentDirection());
                    }

                    for (Information i : navi.getNotify()) {
                        speechText(i.getContent());
                        rtdisTxt.setText(i.getContent());
                        i.notified(); //通知フラグを立てて置く。以降読みません。
                    }

                    if(status == Navi.DIRECTION_NOTIFY)
                        speechText(navi.getDirectionNotify());

                    break;
            }
        } else {
            status = navi.setCurrentLocation(point[0], point[1]);

            updateMap(point[0], point[1]);
            if (status == Navi.ROUTE_FINISH) {
                speechText(getString(R.string.finish));
                navi.finishRouting();
            }



            //最短距離をとる点を赤ドットで描画
            if (rDot != null) rDot.remove();
            //rDot = drawDot(navi.getLatLngToNearestEdge(), Color.RED);   //todo 10/2
            drawDot(navi.getLatLngToNearestEdge(), Color.RED);

            for (Information i : navi.getNotify()) {
                if (!txtToSpeech.isSpeaking()) {
                    speechText(i.getContent());
                    rtdisTxt.setText(i.getContent());
                    i.notified(); //通知フラグを立てて置く。以降読みません。
                }
            }
        }*/
    }

    private int getNearestInfo(){
        int n = -1;
        double mindistance = 999999999.9;

        for(int i = 0; i < tweetList.size() ; i++){
            LatLng tweet = new LatLng(tweetList.get(i).latitude, tweetList.get(i).longitude);
            LatLng p = new LatLng(point[0], point[1]);
            if(mindistance > GraphProcesser.toMeter(p, tweet) && !tweetList.get(i).isVisited()) {
                mindistance = GraphProcesser.toMeter(p, tweet);
                n = i;
                AlartSpotTweet = 3;
            }
        }

        for(int i = 0; i < alartList.size() ; i++){
            LatLng alart = new LatLng(alartList.get(i).latitude, alartList.get(i).longitude);
            LatLng p = new LatLng(point[0], point[1]);
            if(mindistance > GraphProcesser.toMeter(p, alart) && !alartList.get(i).isVisited()) {
                mindistance = GraphProcesser.toMeter(p, alart);
                n = i;
                AlartSpotTweet = 1;
            }
        }

        for(int i = 0; i < spotList.size() ; i++){
            LatLng spot = new LatLng(spotList.get(i).latitude, spotList.get(i).longitude);
            LatLng p = new LatLng(point[0], point[1]);
            if(mindistance > GraphProcesser.toMeter(p, spot) && !spotList.get(i).isVisited()) {
                mindistance = GraphProcesser.toMeter(p, spot);
                n = i;
                AlartSpotTweet = 2;
            }
        }

        if(mindistance < 4) return n;
        return -1;
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        // コンパスセンサーイベントの登録
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
        sensor.registerListener(this, sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        if (mMap == null) {
            // MapFragment から GoogleMap を取得する
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                // マップの種類を設定する
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

                // 現在地の取得
                mMap.setMyLocationEnabled(true);

                // UI の有効／無効設定を行う
                UiSettings ui = mMap.getUiSettings();

                ui.setCompassEnabled(true);                 // コンパスの有効化
                ui.setMyLocationButtonEnabled(true);        // 現在位置に移動するボタンの有効化
                ui.setRotateGesturesEnabled(true);          // 回転ジェスチャーの有効化
                ui.setScrollGesturesEnabled(true);          // スクロールジェスチャーの有効化
                ui.setTiltGesturesEnabled(true);            // Tlitジェスチャー(立体表示)の有効化
                ui.setZoomControlsEnabled(true);            // ズームイン・アウトボタンの有効化
                ui.setZoomGesturesEnabled(true);            // ズームジェスチャー(ピンチイン・アウト)の有効化
                ui.setAllGesturesEnabled(true);             // すべてのジェスチャーの有効化
            }
        }
        Intent nowIntent = getIntent();
        String action = nowIntent.getAction();
        UsbDevice device = (UsbDevice)nowIntent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)){
            Log.d("TAG","アプリケーション再起動");
            setDevice(device);
        }
        else if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(device)){
            setDevice(null);
            mConnection.close();
            Log.d("TAG","デバイス　デタッチで終了");
        }
    }

    /***** アプリ終了時クローズ処理 *****/
    public void onPause(){
        usbThreadFlag = false;
        if(mSpeechRecognizer!=null){
            mSpeechRecognizer.stopListening();
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();

        }
        mSpeechRecognizer = null;
        super.onPause();
        Log.d("TAG", "アプリケーション一旦停止");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThreadIsStopped = true;
        flag = 0;
        try{
            mSpeechRecognizer.destroy();
        }
        catch (Exception e)
        {
            Log.e("TAG","Exception:"+e.toString());
        }
        //unregisterReceiver(mUsbReceiver);
        if (null != txtToSpeech) {
            // TextToSpeechのリソースを解放する
            txtToSpeech.shutdown();
        }
        closeDevice();
        // センサーイベントを削除
        sensor.unregisterListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        openDevice();
    }

    /* 通信をオープンにする */
    private void openDevice() {
        if(ftDevice != null && ftDevice.isOpen()) {
            if(mThreadIsStopped) {
                setConfig(112500, (byte) 8, (byte) 1, (byte) 0, (byte) 0);
                ftDevice.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                ftDevice.restartInTask();
                new Thread(mLoop).start();
                Log.d("TAG", "スレッド起動");
            }

            return;
        }

        int devCount = ftD2xx.createDeviceInfoList(this);
        D2xxManager.FtDeviceInfoListNode[] deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
        ftD2xx.getDeviceInfoList(devCount, deviceList);

        Log.d("TAG", "devCount:"+devCount);

        if(devCount <= 0) {
            Log.d("TAG", "とてもつらい");
            return;
        }

        if(ftDevice == null){
            ftDevice = ftD2xx.openByIndex(this, 0);
        } else {
            synchronized (ftDevice) {
                ftDevice = ftD2xx.openByIndex(this, 0);
            }
        }

        if(ftDevice != null && ftDevice.isOpen()) {
            if (mThreadIsStopped) {
                setConfig(112500, (byte) 8, (byte) 1, (byte) 0, (byte) 0);
                ftDevice.purge((byte) (D2xxManager.FT_PURGE_TX | D2xxManager.FT_PURGE_RX));
                ftDevice.restartInTask();
                new Thread(mLoop).start();
            }
        }
    }



    /* データの逐次取得 */
    private Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            int readSize, i = 0;

            mThreadIsStopped = false;
            while(true) {
                if (mThreadIsStopped) {
                    break;
                }

                synchronized (ftDevice) {
                    readSize = ftDevice.getQueueStatus();
                    if (readSize > 0) {
                        mReadSize = readSize;
                        if (mReadSize > READBUF_SIZE) mReadSize = READBUF_SIZE;

                        ftDevice.read(rbuf, 1);
                        if (mDataIsRead == false) {
                            if ('$' == (char) rbuf[0]) {
                                mDataIsRead = true;
                                i = 0;
                            }
                        } else {
                            if ('*' == (char) rbuf[0]) {
                                data = String.valueOf(rchar, 0, i);
                                rNMEA = data.split(",", -1);

                                arrangeData(rNMEA);

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        gpsReady = true;

                                        try {
                                            if(mNowLocation.SURVEY_EN || mNowLocation.CORRECTION_ON) {
                                                //通常測位または補正有測位

                                                if (mQZSS.GPS_ON || mQZSS.GLONASS_ON) {
                                                    srcText.setTextColor(0xFFFFFF00);
                                                    //GPS衛星orGLONASS衛星の電波が取得できている
                                                } else if (mQZSS.QZSS_ON) {
                                                    srcText.setTextColor(0xFFFF0000);
                                                    //みちびきの電波が取得できている
                                                } else if (mQZSS.L1SAIF_ON) {
                                                    srcText.setTextColor(0xFF0000FF);
                                                    //L1SAIF補強信号が受信できている
                                                } else {
                                                    srcText.setTextColor(0xFFFFFFFF);
                                                }

                                                if (navi.getPositioningMode() == Navi.GPS) {
                                                    //本番はこっち
                                                    point[0] = mNowLocation.getPointLat();
                                                    point[1] = mNowLocation.getPointLng();

                                                    navi.setCurrentLocation(point[0], point[1]);


                                                    if (navi.isRouting()) {
                                                        String dir;
                                                        Vertex v;
                                                        if (navi.isHealthlyRoute()) {
                                                            dir = hRoute.getNextDirection(hRoute.getVertex().indexOf(navi.getNextDirectionVertex()));
                                                            v = navi.getNextDirectionVertex();
                                                        } else {
                                                            dir = navi.getCurrentDirection();
                                                            v = navi.getCurrentVertex();
                                                        }

                                                        if (v != null) {

                                                            if (vId != v.getId()) {
                                                                if (pLine != null) pLine.remove();
                                                                //pLine = drawDot(v.getLatLng(), Color.YELLOW); //todo　変えた10/2
                                                                drawDot(v.getLatLng(),Color.YELLOW);


                                                                if (1 < (int) distanceNextTo && (int) distanceNextTo <= 5 && !dir.equals("")) {
                                                                    vId = v.getId();
                                                                    directionTxt.setText(dir);
                                                                    speechText((int) distanceNextTo + "メートル先" + dir);
                                                                }
                                                            }

                                                            if (!v.isVisitedForNavi() && (int) distanceNextTo <= 1 && !dir.equals("")) {
                                                                directionTxt.setText(dir);
                                                                speechText(dir);
                                                                v.visitForNavi();
                                                            }
                                                        }
                                                    }

                                                    updateMap(point[0], point[1]);
                                                } else {
                                                    //RFID測位＋GPS補正
                                                    if  (navi.getReferenceUpdateFlag()){
                                                        navi.setReferencePoint(mNowLocation.getPointLat(), mNowLocation.getPointLng());
                                                    }
                                                    if (!navi.isInDoor()) {
                                                        LatLng nowLocation = navi.getLatLng();
                                                        LatLng referenceLocation = navi.getReferencePoint();
                                                        double latCorrection = mNowLocation.getPointLat() - referenceLocation.latitude;
                                                        double lngCorrection = mNowLocation.getPointLng() - referenceLocation.longitude;
                                                        point[0] = nowLocation.latitude + latCorrection;
                                                        point[1] = nowLocation.longitude + lngCorrection;
                                                        updateMap(nowLocation.latitude + latCorrection, nowLocation.longitude + lngCorrection);
                                                    }
                                                }


                                                latTextV.setText("Lat : " + point[0] + " ");
                                                lngTextV.setText("Lng : " + point[1]);
                                            } else {
                                                latTextV.setText("NotAvailable");
                                                lngTextV.setText("NotAvailable");
                                            }
                                        } catch (Exception e) {

                                        }
                                    }
                                });

                                i = 0;
                                mDataIsRead = false;
                            } else {
                                rchar[i] = (char) rbuf[0];
                                i++;
                                if (i >= mReadSize){
                                    mDataIsRead = false;
                                    i = 0;
                                }
                            }
                        }
                    }
                }

            }
        }
    };

    private void arrangeData(String[] rNMEA) {
        if(rNMEA.length > 0) {
            switch (rNMEA[0]) {
                case "GPGNS":
                    mQZSS.setGPS(rNMEA);
                    mNowLocation.setPoint(rNMEA);

                    break;
                case "GLGNS":
                    mQZSS.setGLONASS(rNMEA);
                    mNowLocation.setPoint(rNMEA);

                    break;
                case "GNGNS":
                    mQZSS.setGNSS(rNMEA);
                    mNowLocation.setPoint(rNMEA);

                    break;
                case "GPGSA":
                    mQZSS.setUsefulGNSS(rNMEA);
                    mQZSS.checkQZSS();

                    break;
                case "GNGSA":
                    mQZSS.setUsefulGNSS(rNMEA);
                    mQZSS.checkQZSS();

                    break;
                case "GPGSV":
                    mQZSS.setVisibleGNSS(rNMEA);

                    break;
                case "QZGSV":
                    mQZSS.setVisibleQZSS(rNMEA);
                    mQZSS.checkL1SAIF();

                    break;
                case "GNVTG":
                    mNowLocation.setVelocity(rNMEA);

                    //tmp = new String(data);

                    break;
                default:
                    break;
            }
        }
    }

    /* 通信の終了処理 */
    private void closeDevice() {
        mThreadIsStopped = true;
        if(ftDevice != null) ftDevice.close();
    }

    /* ボーレート[bps]・データ長[bit]・ストップBit[bit]・バリティを設定 */
    private void setConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        if (!ftDevice.isOpen()) {
            return;
        }

        ftDevice.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
        ftDevice.setBaudRate(baud);

        switch (dataBits) {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits) {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity) {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        ftDevice.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl) {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        ftDevice.setFlowControl(flowCtrlSetting, (byte) 0x0b, (byte) 0x0d);
    }

    /***** USBデバイス接続処理 *****/
    private boolean setDevice(UsbDevice device){
        // デバイスインタフェース検出
        if(device.getInterfaceCount() != 1){
            Log.d("TAG","インタフェース発見できませんでした");
            return false;
        }
        UsbInterface intf = device.getInterface(0);
        // エンドポイントの検出
        if(intf.getEndpointCount() < 2){
            Log.d("TAG","エンドポイントを検出できませんでした");
            return false;
        }
        Log.d("TAG","エンドポイント数 ="+intf.getEndpointCount());

        // OUTエンドポイントの確認
        UsbEndpoint epout = intf.getEndpoint(0);
        if(epout.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK){
            Log.d("TAG","OUTエンドポイントがバルクタイプではありません");
            return false;
        }

        // INエンドポイントの確認
        UsbEndpoint epin = intf.getEndpoint(1);
        if(epin.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK){
            Log.d("TAG","INエンドポイントがバルクタイプではありません");
            return false;
        }

        // デバイス定数代入
        mDevice = device;
        mEndpointOut = epout;
        mEndpointIn = epin;

        //　接続許可確認
        if(mDevice != null){
            UsbDeviceConnection connection = mUsbManager.openDevice(mDevice);
            if(connection != null && connection.claimInterface(intf, true)){
                UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                Log.d("TAG", "IteratorSize:"+deviceList.values().size());
                while(deviceIterator.hasNext()){
                    UsbDevice dvc = deviceIterator.next();
                    Log.d("TAG", "VenderID:"+dvc.getProductId());

                    if(dvc.getVendorId() == 1240) {
                        //マイコン
                        mConnection = connection;
                        Log.d("TAG", "USBデバイス接続正常完了");
                        flag = 1;

                        if (usbThreadFlag) {
                            usbThread = new Thread(this);
                            usbThread.start();
                        }
                    }
                    if(dvc.getVendorId() == 1027){
                        //みちびき
                        Log.d("TAG", "USBデバイス接続正常完了");
                        openDevice();
                    }
                }

                return true;
            }else{
                mConnection = null;
                Log.d("TAG","USBデバイス接続失敗");
                return false;
            }
        }

        return false;
    }

    /***** 状態取得コマンド送信メソッド *****/
    private void sendStatusCommand(){
        synchronized(this){				// 排他制御で実行
            if(mConnection != null){
                message[0] = COMMAND_STATUS;
                message[1] = 0x00;
                message[2] = 0x00;
                mConnection.bulkTransfer(mEndpointOut, message, 3, 200);
            }
        }
    }

    /***** ソレノイド駆動時間コマンド送信メソッド( ON ) *****/
    private void turnOnSolenoid(int direction){
        synchronized(this){				// 排他制御で実行
            if(mConnection != null){
                onTime = SOLENOID_ONTIME * USB_INTERRUPT_HZ;
                message[0] = COMMAND_SOL;
                switch (direction) {
                    case Route.LEFT:
                        System.arraycopy(Lside, 0, message, 1, 20);
                        System.arraycopy(Rside, 0, message, 21, 20);
                        break;
                    case Route.RIGHT:
                        System.arraycopy(Rside, 0, message, 1, 20);
                        System.arraycopy(Lside, 0, message, 21, 20);
                        break;

                    default:
                        break;
                }
                message[41] = SOLENOID_HZ;
                message[42] = 1;

                mConnection.bulkTransfer(mEndpointOut, message, 44, 200);
            }
        }
    }

    /***** ソレノイド駆動時間コマンド送信メソッド( OFF ) *****/
    private void turnOffSolenoid(){
        synchronized(this){				// 排他制御で実行
            if(mConnection != null){
                message[0] = COMMAND_SOL;
                message[41] = SOLENOID_HZ;
                message[42] = 1;
                Arrays.fill(message,1,41,(byte)0);
                mConnection.bulkTransfer(mEndpointOut, message, 44, 200);
            }
        }
    }


    /***** USBデータ状態受信スレッド *****/
    public void run(){
        while(true){
            if(flag == 1) {
                sendStatusCommand();                // 状態，計測要求コマンド送信
                result = mConnection.bulkTransfer(mEndpointIn, buffer, 64, 200); //状態受信処理　常に64byte受信
                if (result > 0) {                        // 正常受信なら受信データ処理実行
                    handler.post(new Runnable() {    // データ表示
                        public void run() {

                            usbReady = true;

                            if(gpsReady && usbReady && !isSpeakedInit){
                                speechText("プルドッグは正常に起動しました");
                                isSpeakedInit = true;
                            }
                            tx1.setText("  " + Integer.toString(buttonCounter1) + "  ");
                            tx2.setText("  " + Integer.toString(buttonCounter2) + "  ");
                            //tx3.setText("  " + Integer.toString(j++) + "  ");

                            //時間計算
                            systemSec = (int) ((double) (usbInterruptCount++) * USB_INTERRUPT_TOSEC_FACTOR);
                            String min = Integer.toString(systemSec / 60);
                            String sec;
                            if (systemSec % 60 < 10) sec = "0" + Integer.toString(systemSec % 60);
                            else sec = Integer.toString(systemSec % 60);
                            tx3.setText("  " + min + ":" + sec + "  ");
                            tx4.setText(" Mode: " + Integer.toString(Mode) + " Dialog: " + Integer.toString(dialogMode) + "deordes:" + depaOrdest + "btnEnable:" + btnEnable);

                            //ソレノイド駆動処理
                            if (onTime > 0) onTime--;
                            else if (onTime == 0) {
                                turnOffSolenoid();
                                onTime--;
                            }

                            rfidTag[4] = buffer[4];
                            rfidTag[5] = buffer[5];
                            rfidTag[6] = buffer[6];
                            rfidTag[7] = buffer[7];
                            rfidTag[8] = buffer[8];
                            rfidTag[9] = buffer[9];
                            //speechText(Integer.toString(rfidTag[4])+Integer.toString(rfidTag[5])+Integer.toString(rfidTag[6])+Integer.toString(rfidTag[7])+Integer.toString(rfidTag[8])+Integer.toString(rfidTag[9]));

                            //tagEtxt.setText(Integer.toHexString(rfidTag[4])+Integer.toHexString(rfidTag[5])+Integer.toHexString(rfidTag[6])+Integer.toHexString(rfidTag[7])+Integer.toHexString(rfidTag[8])+Integer.toHexString(rfidTag[9]));


                            //RFID測位部
                            if  (navi.getPositioningMode() == Navi.RFID) {
                                tagEtxt.setText(Integer.toString((((int) rfidTag[6]) << 8) + (int) rfidTag[7]));  //todo デバッグ時は消す

                                try {
                                    int tagId = Integer.parseInt(tagEtxt.getText().toString());
                                    boolean isVisited = navi.isVisited(tagId);
                                    if  (navi.getVisitedSize() == 0) pastId = -1;

                                    if ((rfidTag[4] != 0 || (rfidTag[5] == 1 || rfidTag[5] == 2) || rfidTag[8] != 0 || rfidTag[9] != 0) && navi.isExist(tagId) && !isVisited && pastId != tagId) {
                                        int status = navi.setCurrentLocation(tagId);
                                        navi.updateReferencePoint();

                                        if (status == Navi.ROUTE_FINISH) {
                                            speechText(getString(R.string.finish));
                                            navi.finishRouting();
                                        }

                                        if (navi.isInDoor() && navi.isRouting()) { //中更新

                                            for (Information i : navi.getNotify()) {
                                                speechText(i.getContent());
                                                rtdisTxt.setText(i.getContent());
                                            }

                                            if  (status == Navi.DESTINATION_NOTIFY){
                                                speechText("まもなく、目的地です。");
                                            }

                                            if (status == Navi.DIRECTION_NOTIFY) speechText(navi.getDirectionNotify());

                                        } else if (!navi.isInDoor() && navi.isRouting()){ //外更新
                                            for (Information i : navi.getNotify()) {
                                                speechText(i.getContent());
                                                rtdisTxt.setText(i.getContent());
                                            }

                                            if  (status == Navi.DESTINATION_NOTIFY){
                                                speechText("まもなく、目的地です。");
                                            }

                                            point[0] = navi.getLatLng().latitude;
                                            point[1] = navi.getLatLng().longitude;
                                            updateMap(point[0], point[1]);

                                            //ルートへの最短距離を取得
                                            double distanceToRoute = navi.getDistanceToNearestEdge();
                                            rtdisTxt.setText("距離：" + Double.toString(distanceToRoute));

                                            //最短距離をとる点を赤ドットで描画
                                            if (rDot != null) rDot.remove();
                                            rDot = drawDot(navi.getLatLngToNearestEdge(),Color.RED);

                                            //drawDot(navi.getLatLngToNearestEdge());   //todo　変えた10/2


                                            //TODO 注意。
                                            /********************************************今回リルートはオフ*********************************************
                                            if (navi.isInSafeArea(point[0], point[1]) && (distanceToRoute > 5 || distanceToRoute < 0) && !navi.isCurrentRouteEndInDoor()) {
                                                Route route = navi.generateRoute(navi.getDestinationName());
                                                if (route == null) {
                                                    //speechText("ルート NULUPO!");
                                                    List<Edge> tmp = navi.getAllEdge();
                                                    List<LatLng> latlngtmp = new ArrayList<>();
                                                    for (Edge t : tmp) {
                                                        latlngtmp.add(t.getVertexsLatLng()[0]);
                                                        latlngtmp.add(t.getVertexsLatLng()[1]);
                                                        drawPolyline(latlngtmp);

                                                        latlngtmp.clear();
                                                    }

                                                } else drawRoute(route);
                                            }
                                            **************************************************************************************************************/

                                            //メートル方向指示
                                            String dir = "";
                                            Vertex nextV = navi.getCurrentVertex();

                                            if (navi.isHealthlyRoute()) {
                                                dir = hRoute.getNextDirection(hRoute.getVertex().indexOf(navi.getNextDirectionVertex()));
                                                nextV = navi.getNextDirectionVertex();
                                            } else {
                                                //todo どうよ
                                                Edge e = GraphProcesser.getNearestEdge(navi.getAllEdge(), navi.getLatLng());
                                                //Edge e = GraphProcesser.getNearestEdge(navi.getCurrentRoute().getEdge(), navi.getLatLng());

                                                LatLng l1 = e.getVertexsLatLng()[0];
                                                LatLng l2 = e.getVertexsLatLng()[1];

                                                List<Vertex> vList = navi.getCurrentRoute().getVertex();
                                                Vertex ev1 = GraphProcesser.getNearestVertex(vList, l1);
                                                Vertex ev2 = GraphProcesser.getNearestVertex(vList, l2);

                                                //どちらが先かを探索している。
                                                if  (vList.indexOf(ev1) > vList.indexOf(ev2)) nextV = ev1;
                                                if  (vList.indexOf(ev2) > vList.indexOf(ev1)) nextV = ev2;

                                            }

                                            if (nextV != null) {
                                                if (navi.getCurrentRoute().getVertex().indexOf(nextV) == navi.getCurrentRoute().getVertex().indexOf(navi.getCurrentVertex())) {
                                                    dir = navi.getCurrentDirection();
                                                    distanceNextTo = navi.getDistanceToNearestEdge()
                                                            + GraphProcesser.toMeter(nextV.getLatLng(), navi.getLatLngToNearestEdge());
                                                } else if (GraphProcesser.toMeter(navi.getCurrentVertex().getLatLng(), navi.getLatLng()) < 0.1) {
                                                    dir = navi.getCurrentDirection();
                                                    distanceNextTo = 0.0;
                                                } else {
                                                    dir = navi.getNextDirection();
                                                    distanceNextTo = navi.getDistanceToNearestEdge()
                                                            + GraphProcesser.toMeter(nextV.getLatLng(), navi.getLatLngToNearestEdge());
                                                    //distanceNextTo = navi.getDistanceToNearestEdge();
                                                }

                                                if (pLine != null) pLine.remove();
                                                pLine = drawDot(nextV.getLatLng(), Color.YELLOW);

                                                //drawDot(nextV.getLatLng(),Color.YELLOW);       //todo 10/2 変えた

                                                if(!dir.equals("")) {
                                                    directionTxt.setText(dir);
                                                    if (1 <= (int) distanceNextTo && (int) distanceNextTo <= 5)
                                                        speechText("まもなく、" + dir);
                                                    else if(5 < distanceNextTo && distanceNextTo <= 15)
                                                        speechText((int) distanceNextTo + "メートル先、" + dir);
                                                    else if(15 < distanceNextTo)
                                                        speechText("直線が"+(int)distanceNextTo + "メートル続きます");
                                                    else if ((int) distanceNextTo < 1)
                                                        speechText(dir);

                                                }

                                                //　todo みちびき使用時には復活させないと読みまくってうるさくなるはず
                                                /*if (vId != nextV.getId()) {
                                                    if (pLine != null) pLine.remove();
                                                    pLine = drawDot(nextV.getLatLng(), Color.YELLOW);


                                                    if (1 < (int) distanceNextTo && (int) distanceNextTo <= 5 && !dir.equals("")) {
                                                        vId = nextV.getId();
                                                        directionTxt.setText(dir);
                                                        speechText((int) distanceNextTo + "メートル先" + dir);
                                                    }
                                                }*/


                                            }
                                        }
                                        pastId = tagId;
                                        navi.visit(tagId);

                                    } else {
                                        //tagEtxt.setText("NoTag NoLife");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e("ERROR", e.getMessage());
                                }
                            }

                            // PushSWの状態表示
                            if (buffer[3] == 0x01) {
                                buttonCounter1++;               //Button1が押されたらインクリメント
                                offTOon1 = true;                //button1がOFFからONになったフラグ
                            } else if (buffer[3] == 0x02) {
                                buttonCounter2++;               //Button2が押されたらインクリメント
                                offTOon2 = true;                //button2がOFFからONになったフラグ
                            } else if (buffer[3] == 0x04) {

                            } else if (buffer[3] == 0x08) {

                            } else if (buffer[3] == 0x00) {
                                if (offTOon1) {
                                    onTOoff1 = true;            //button1がONからOFFになったフラグ
                                }
                                if (offTOon2) {
                                    onTOoff2 = true;            //button2がONからOFFになったフラグ
                                }
                            }
                            if (onTOoff1 || onTOoff2) {               //button1 or button2がONからOFFになった
                                if (CHATTERING_RANGE < buttonCounter1 && buttonCounter1 < BTN_PUSH_LENGTH) {
                                    pSWText.setText("MICON_BUTTON_SHORT_PUSH_SW1");    //　PushSW1を短く押し　ON
                                }
                                if (CHATTERING_RANGE < buttonCounter2 && buttonCounter2 < BTN_PUSH_LENGTH ) {
                                    pSWText.setText("MICON_BUTTON_SHORT_PUSH_SW2");    //　PushSW2を短く押し　ON
                                }
                                //初期化
                                buttonCounter1 = 0;
                                buttonCounter2 = 0;
                                onTOoff1 = false;
                                onTOoff2 = false;
                                offTOon1 = false;
                                offTOon2 = false;
                                mn.checkText();       //TextViewチェック
                            }

                            if (BTN_PUSH_LENGTH == buttonCounter1 && btnEnable) {
                                pSWText.setText("MICON_BUTTON_LONG_PUSH_SW1");    //　PushSW1を長押し　ON
                                //初期化
                                onTOoff1 = false;
                                offTOon1 = false;
                                mn.checkText();       //TextViewチェック
                                disableLongPushSW();
                            }
                            if (BTN_PUSH_LENGTH == buttonCounter2 && btnEnable) {
                                pSWText.setText("MICON_BUTTON_LONG_PUSH_SW2");     //　PushSW2を長押し　ON
                                //初期化
                                onTOoff2 = false;
                                offTOon2 = false;
                                mn.checkText();       //TextViewチェック
                                disableLongPushSW();
                            }
                        }
                    });
                    try {
                        usbThread.sleep(USB_SLEEP);            //インターバル
                    } catch (InterruptedException e) {

                    }

                } else {
                    mConnection.close();
                    flag = 0;
                    handler.post(new Runnable() {    // メッセージ出力
                        public void run() {
                            Log.d("TAG", "デバイスが切り離されました");
                        }
                    });
                }
            }
        }
    }


    /***** Google Mapセットアップ *****/
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                updateMap(point[0], point[1]);
            }
        }
    }


    @Override
    public void onInit(int status) {
        if (TextToSpeech.SUCCESS == status) {
            Locale locale = Locale.ENGLISH;
            if (txtToSpeech.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                txtToSpeech.setLanguage(locale);
            } else {
                Log.d("TAG", "Error SetLocale");
            }
        } else {
            Log.d("TAG", "Error Init");
        }
    }

    /****** TextToSpeech 関連 ******/
    private void speechText(String string) {
        if (0 < string.length()) {
            if (txtToSpeech.isSpeaking()) {
               //txtToSpeech.stop();
            }
            setSpeechRate(1.0f);
            setSpeechPitch(1.0f);

            // tts.speak(text, TextToSpeech.QUEUE_FLUSH, null) に
            // KEY_PARAM_UTTERANCE_ID を HasMap で設定
            HashMap<String, String> hMap = new HashMap<String, String>();
            hMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"messageID");

            if(Build.VERSION.SDK_INT < 21){
                if(string.equals(getString(R.string.speech_recognize_fail)))
                    txtToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH,hMap);
                else txtToSpeech.speak(string, TextToSpeech.QUEUE_ADD, hMap);
            }else if(Build.VERSION.SDK_INT >= 21) {
                if (string.equals(getString(R.string.speech_recognize_fail)))
                    txtToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null, null);
                else txtToSpeech.speak(string, TextToSpeech.QUEUE_ADD, null, null);
            }

        }
    }
    //読み上げストップ
    private void speechStop(){
        txtToSpeech.stop();
        txtToSpeech.shutdown();
        txtToSpeech = new TextToSpeech(this,this);
    }
    // 読み上げのスピード
    private void setSpeechRate(float rate){
        if (null != txtToSpeech) {
            txtToSpeech.setSpeechRate(rate);
        }
    }
    // 読み上げのピッチ
    private void setSpeechPitch(float pitch){
        if (null != txtToSpeech) {
            txtToSpeech.setPitch(pitch);
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // requestCodeを確認して、自分が発行したIntentの結果であれば処理を行う
        Bundle bundle = data.getExtras();

        if(RESULT_OK == resultCode){
            flag = 1;
            if(REQUEST_CODE_UPLOAD == requestCode){     //ルート作成説明Activityから帰ってきたとき
                infoLinearLayout.setVisibility(View.VISIBLE);
                record();
            }else if (REQUEST_CODE_TWEET_RETURN == requestCode){
                if(bundle.getInt("res") == 1) {
                    speechText(tweetStr + "とつぶやきました");
                }else if(bundle.getInt("res") == 0) {
                    speechText("ツイートに失敗しました");
                }
            }else results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            /*if (REQUEST_CODE_DESTINATION == requestCode) {

                // 結果はArrayListで返ってくる
                destinationName = results.get(0);
                results.remove(0);
                speechText(destinationName);
                pSWText.setText("MICON_BUTTON_OFF");        //pSWTextにこのString("MICON_BUTON_OFF")を入れるとボタンが有効になる
                Mode = 1;   //道案内モード
                dialogMode = 1;

            }*/
            /*if (REQUEST_CODE_TWITTER == requestCode){

                if (!TwitterUtils.hasAccessToken(this)) {
                    //認証
                    Intent intent = new Intent(this, TwitterOAuthActivity.class);
                    startActivity(intent);
                } else {
                    //Tweetを一覧表示するアクティビティにとぶ
                    intent = new Intent(this, TweetList.class);
                    startActivity(intent);
                }
            }*/
            /*if (REQUEST_CODE_TWEET == requestCode){
                Mode = 2;   //アップロードモード
                tweetStr = results.get(0);
                speechText(tweetStr);
                pSWText.setText("MICON_BUTTON_OFF");        //pSWTextにこのString("MICON_BUTON_OFF")を入れるとボタンが有効になる
            } else if (REQUEST_CODE_TWEET_RETURN == requestCode){
                flag=1;
            }*/

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /***** みちびき *****/
    /*BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)){

            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)){

            }
        }
    };*/

    /***** GoogleMap マーカー更新 *****/
    public void updateMap(double lat, double lng) {
        LatLng p = new LatLng(lat, lng);

        if (mLocation != null) mLocation.remove();
        mLocation = mMap.addMarker(new MarkerOptions().position(p).title("げんざいち"));

        if  (cameraFocus) {
            CameraUpdate cu =
                    CameraUpdateFactory.newLatLngZoom(
                            new LatLng(lat,lng), 25f);
            mMap.moveCamera(cu);
            cameraFocus = false;
            /*
            CameraPosition cp = new CameraPosition.Builder()
                    .target(p).zoom(20.0F)
                    .bearing(0).tilt(25).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        */
        }
    }

    /***** GoogleMap ポリゴン描画 *****/
    public void drawPolygon(ArrayList<LatLng> latlng){
        PolygonOptions rectOptions = new PolygonOptions();
        for(LatLng ll : latlng){
            rectOptions.add(ll);
        }
        Polygon polygon_newsta  = mMap.addPolygon(rectOptions);
        //線の幅を指定（ピクセル単位）
        polygon_newsta.setStrokeWidth(2);
        //線の色を指定（ARGBフォーマット）
        polygon_newsta.setStrokeColor(Color.CYAN);
        //塗りつぶす場合、色を指定（ARGBフォーマット、半透明な青の場合）
        polygon_newsta.setFillColor(0x557777ff);

    }

    /***** GoogleMap ポリライン描画 *****/
    public void drawPolyline(List<LatLng> latlng){
        PolylineOptions options = new PolylineOptions();
        for(LatLng ll : latlng){
            options.add(ll);
        }
        options.color(0xff2FAEFC);
        options.width(20);
        options.geodesic(true); // 測地線で表示する場合、地図上で２点間を結ぶ最短曲線
        mMap.addPolyline(options);
    }

    /***** GoogleMap 点描画 *****/     //todo 10/2 変えた
    /*public void drawDot(LatLng latlng){
        return drawDot(latlng, Color.BLUE);
    }*/

    public GroundOverlay drawDot(LatLng latlng, int color){ //todo 10/2 変えた
        /*double len = 0.0000020f;
        PolylineOptions options = new PolylineOptions();
        options.add(new LatLng((latlng.latitude - len),(latlng.longitude - len)));
        options.add(new LatLng((latlng.latitude + len),(latlng.longitude + len)));
        options.color(color);
        options.width(15);
        options.zIndex(1f);
        if  (color == Color.YELLOW) options.zIndex(100f);
        if  (color == Color.RED) options.zIndex(200f);
        options.geodesic(true); // 測地線で表示する場合、地図上で２点間を結ぶ最短曲線
        */
        // 画像及び位置情報設定
        GroundOverlayOptions imageOptions = new GroundOverlayOptions();
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.dot_blu);
        if(color == Color.RED){
            imageOptions.zIndex(200f);
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.dot_red);
        }
        if(color == Color.YELLOW){
            imageOptions.zIndex(100f);
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.dot_yel);
        }
        if(color == Color.BLUE){
            imageOptions.zIndex(1f);
            bitmap = BitmapDescriptorFactory.fromResource(R.drawable.dot_blu);
        }
        imageOptions.image(bitmap);
        imageOptions.anchor(0.5f,0.5f);
        imageOptions.position(latlng, 1.0f, 1.0f);

        // マップに画像をオーバーレイ
        GroundOverlay overlay = mMap.addGroundOverlay(imageOptions);
        overlay.setTransparency(0.0f);
        return overlay;
    }

    public void drawDot(List<LatLng> latlng){   //todo 10/2 変えた
        for (LatLng l : latlng) drawDot(l,Color.BLUE);
    }

    private String downloadUrl(String strUrl) throws IOException{
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
            Log.d("Exception while", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }



    // 音声認識準備完了
    @Override
    public void onReadyForSpeech(Bundle params) {
        Toast.makeText(this, "音声認識準備完了", Toast.LENGTH_SHORT);
        if (txtToSpeech.isSpeaking()) {
            speechStop();
        }
    }

    // 音声入力開始
    @Override
    public void onBeginningOfSpeech() {
        Toast.makeText(this, "入力開始", Toast.LENGTH_SHORT);

    }

    // 録音データのフィードバック用
    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    // 入力音声のdBが変化した
    @Override
    public void onRmsChanged(float rmsdB) {

    }

    // 音声入力終了
    @Override
    public void onEndOfSpeech() {
        Toast.makeText(this, "入力終了", Toast.LENGTH_SHORT);

    }

    // ネットワークエラー又は、音声認識エラー
    @Override
    public void onError(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                // 音声データ保存失敗
                Toast.makeText(this, "音声データ保存失敗", Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                // Android端末内のエラー(その他)
                Toast.makeText(this, "Anndroid端末内エラー", Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                // 権限無し
                Toast.makeText(this, "権限無し", Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                // ネットワークエラー(その他)
                Toast.makeText(this, "ネットワークエラー", Toast.LENGTH_LONG).show();
                //Log.e(LOGTAG, "network error");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                // ネットワークタイムアウトエラー
                Toast.makeText(this, "ネットワークタイムアウトエラー", Toast.LENGTH_LONG).show();
                //Log.e(LOGTAG, "network timeout");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                // 音声認識結果無し
                Toast.makeText(this, "音声認識結果無し", Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                // RecognitionServiceへ要求出せず

                Toast.makeText(this, "RecognitionServiceへ要求出せず", Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_SERVER:
                // Server側からエラー通知
                Toast.makeText(this, "Server側からエラー通知", Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                // 音声入力無し
                Toast.makeText(this, "音声入力無し", Toast.LENGTH_LONG).show();
                break;
            default:

        }

        //todo　換えた
        if(mSpeechRecognizer!=null){
            mSpeechRecognizer.stopListening();
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();

        }
        mSpeechRecognizer = null;

        speechText(getString(R.string.speech_recognize_fail));
        Mode = 0;
        dialogMode = 0;
        enableLongPushSW();

    }

    // イベント発生時に呼び出される
    @Override
    public void onEvent(int eventType, Bundle params) {
        //Log.v(LOGTAG,"onEvent");
    }

    // 部分的な認識結果が得られる場合に呼び出される
    @Override
    public void onPartialResults(Bundle partialResults) {
        //Log.v(LOGTAG, "onPartialResults");
    }

    // 認識結果
    @Override
    public void onResults(Bundle result) {
        List<String> results = result.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        mSpeechRecognizer = null;
        if(Mode == ROUTE_NAVI){              //目的地・出発地 入力結果
            if(depaOrdest == DEPARTURE) {   //出発地点
                departureName = results.get(0);
                results.remove(0);
                speechText(departureName);
                Toast.makeText(this, departureName, Toast.LENGTH_SHORT).show();
                if(departureName.equals("テスト")){
                    test();
                    return;
                }else if(departureName.equals("案内開始")){
                    if(hRoute != null){
                        navi.setRouteAndStartRouting(hRoute); //hRouteで案内開始
                        drawRoute(hRoute);
                    }else{
                        speechText("保存されたルートはありません。");
                    }
                    enableLongPushSW();
                    return;
                }else if(departureName.equals("終了")){
                    speechText("プルドッグを終了します");
                    while(txtToSpeech.isSpeaking());
                    finish();
                }

                pSWText.setText("MICON_BUTTON_OFF");        //pSWTextにこのString("MICON_BUTON_OFF")を入れるとボタンが有効になる
                Mode = ROUTE_NAVI;   //道案内モード
                dialogMode = DIALOG_1;
            }else if(depaOrdest == DESTINATION){  //目的地点
                destinationName = results.get(0);
                results.remove(0);
                speechText(destinationName);
                pSWText.setText("MICON_BUTTON_OFF");        //pSWTextにこのString("MICON_BUTON_OFF")を入れるとボタンが有効になる
                Mode = ROUTE_NAVI;   //道案内モード
                dialogMode = DIALOG_1;
            }

        }else if(Mode == TWITTER){        //Twitter投稿
            tweetStr = results.get(0);
            speechText(tweetStr+"、とつぶやきますか?");
            pSWText.setText("MICON_BUTTON_OFF");        //pSWTextにこのString("MICON_BUTON_OFF")を入れるとボタンが有効になる
            dialogMode = DIALOG_1;
        }else if(Mode == 0){
            departureName = results.get(0);
            results.remove(0);
            speechText(departureName);
            if(departureName.equals(getString(R.string.kenjosya_route_create_finish))){
                record();
                infoLinearLayout.setVisibility(View.GONE);
                speechText("ルート作成を終了します。");
                return;
            }else if(departureName.equals(getString(R.string.kenjosya_route_create))){
                dialogMode = 0;
                depaOrdest = 0;
                Mode = 0; //モード初期化
                speechText("ルート作成モードに移ります。");
                Intent intent = new Intent(this, RouteMakeManual.class);
                startActivityForResult(intent,REQUEST_CODE_UPLOAD);
                return;
            }else if(departureName.equals(getString(R.string.kenjosya_route_save))){
                dialogMode = 0;
                depaOrdest = 0;
                Mode = 0; //モード初期化
                speechText("ルートのIDを入力してください。");
                Intent intent = new Intent(this, SearchHealthlyPeopleRoute.class);
                startActivityForResult(intent,REQUEST_CODE_HEALTHLY);
                return;
            }else if(departureName.equals("終了")){
                speechText("プルドッグを終了します");
                while(txtToSpeech.isSpeaking());
                finish();
            }

        }

    }

    private void record() {
        if(!navi.isRecording()){
            if(!navi.recordRoute("nurupo")) return;
            directionTxt.setText("記録中");
            directionTxt.setTextColor(Color.RED);
        }
        else {
            route = navi.finalizeRoute();
            directionTxt.setText("記録終了");
            directionTxt.setTextColor(Color.GRAY);
            drawRoute(route);
            uploadFile = route.outputForKml(ma);
            Intent intent = new Intent(this,RouteMake.class);
            startActivity(intent);
        }
    }

    private void test() {
        departure = navi.getLandmarkByName("ばすてい1");
        destination = navi.getLandmarkByName("ばすてい2");
        updateMap(departure.getLat(), departure.getLng());

        //出発地なしだったら現在地から検索
        //ルート検索後に出発地点と目的地点を初期化かする
        departureTV.setText(departure.getName());
        destinationTV.setText(destination.getName());
        Route rt;                //ルート
        if(departure != null) {           //出発地が入ってたら
            rt = navi.generateRoute(departure.getName(), destination.getName());     //ルート検索
            navi.setRouteLandmarkName("start", destination.getName());
        }else {                                //出発地が入ってなかったら
            rt = navi.generateRoute(destination.getName());                         //ルート検索
            navi.setRouteLandmarkName(departure.getName(),destination.getName());
        }
        navi.startRouting();
        drawRoute(rt);
        departure = null;       //出発地初期化
        destination = null;     //目的地初期化
        Mode = 0; //モード初期化
        depaOrdest = 0;
        dialogMode = 0;
    }

    /***** コンパス *****/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD: // 地磁気センサ
                magneticValues = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:  // 加速度センサ
                accelerometerValues = event.values.clone();
                break;
        }

        if (magneticValues != null && accelerometerValues != null) {
            float[] rotationMatrix = new float[MATRIX_SIZE];
            float[] inclinationMatrix = new float[MATRIX_SIZE];
            float[] remapedMatrix = new float[MATRIX_SIZE];

            float[] orientationValues = new float[DIMENSION];

            // 加速度センサと地磁気センサから回転行列を取得
            SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, accelerometerValues, magneticValues);
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapedMatrix);
            SensorManager.getOrientation(remapedMatrix, orientationValues);

            // 方位を取得する
            int orientDegrees = toOrientationDegrees(orientationValues[0]);
            String orientString = toOrientationString(orientationValues[0]);

            //Log.d("TAG", "orientDegrees = " + orientDegrees + " , orientString = " + orientString);
            orientTxt.setText(" " + orientString + "：" + orientDegrees);
        }
    }


    /**
     * 方位の角度に変換する
     * @param angrad
     * @return
     */
    private int toOrientationDegrees(double angrad) {
        return (int)Math.floor(angrad >= 0 ? Math.toDegrees(angrad) : 360 + Math.toDegrees(angrad));
    }

    /**
     * 方位の文字列に変換する
     * @param angrad
     * @return
     */
    private String toOrientationString(double angrad) {
        double[] orientation_range = {
                - (Math.PI * 3 / 4), // 南
                - (Math.PI * 1 / 4), // 西
                + (Math.PI * 1 / 4), // 北
                + (Math.PI * 3 / 4), // 東
        };

        String[] orientation_string = {
                "south",
                "west",
                "north",
                "east",
        };

        for (int i = 0; i < orientation_range.length; i++) {
            if (angrad < orientation_range[i]) {
                return orientation_string[i];
            }
        }

        return orientation_string[0];
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        //非同期で取得

        @Override
        protected String doInBackground(String... urlStr) {

            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(urlStr[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }

            return data;
        }


        // doInBackground
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docbuilder = null;
            try {
                docbuilder = dbfactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null; //<- エンコードはXML自身のエンコードにあわせる
            try {
                doc = docbuilder.parse(new ByteArrayInputStream(result.getBytes("UTF-8")));
                xmlDecoder(doc);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
    }


    /***** マイコンのボタンが押されたら立ち上がるリスナ *****/
    @Override
    public void shortPushSW1() {                                        //YES
        if( Mode == ROUTE_NAVI ) {                                   //道案内モード
            if(dialogMode == DIALOG_1) {                       //音声入力確認
                dialogMode = DIALOG_2;
                if(depaOrdest == DEPARTURE){
                    disableLongPushSW();
                    furigana(departureName);
                }else if(depaOrdest == DESTINATION){
                    disableLongPushSW();
                    furigana(destinationName);
                }

            }else if(dialogMode == DIALOG_2){                  //住所確認
                if(departure != null)
                    if(departure.equals(address)) {
                        speechText("出発地と目的地が同じです。もう一度入力を行ってください。");
                        enableLongPushSW();
                        destinationName = null;
                        destinationNameHira = null;
                        Mode = 0;
                        dialogMode = 0;
                        depaOrdest = 0;
                        return;
                    }
                if  (!address.isInDoor()) {
                    updateMap(address.getLat(), address.getLng());
                }

                //出発地なしだったら現在地から検索
                //ルート検索後に出発地点と目的地点を初期化する
                if(depAndDisType !=GOOGLE_GOOGLE && depAndDisType !=GOOGLE_PULLDOG) depAndDisType = PULLDOG_PULLDOG;
                if(depaOrdest == DEPARTURE){
                    depAndDisType = PULLDOG_PULLDOG;
                    departureTV.setText(address.getName());
                    speechText("出発地は"+departureTV.getText()+"に設定されました。");
                    departure = address;
                }else if(depaOrdest == DESTINATION){
                    if(depAndDisType == PULLDOG_PULLDOG) {
                        //出発地と目的地が両方PULLDOG
                        Route rt;                //ルート
                        destinationTV.setText(address.getName());
                        destination = address;
                        if (departure != null) {           //出発地が入ってたら
                            depAndDisType = 0;
                            rt = navi.generateRoute(departure.getName(), destination.getName());     //ルート検索
                            navi.setRouteLandmarkName(departure.getName(), destination.getName());
                        } else {                                //出発地が入ってなかったら
                            depAndDisType = 0;
                            rt = navi.generateRoute(destination.getName());                         //ルート検索
                            // todo depatureとdestinationを比較して同じなら弾くようにしたい
                            navi.setRouteLandmarkName("start", destination.getName());
                        }
                        navi.startRouting();
                        drawRoute(rt);
                        PulldogTotalDistance = (int)rt.getTotalDistance();

                        speechText("ルート案内を開始します。ルートの総距離は" + PulldogTotalDistance + "メートルです");

                        //TODO FOR_DEBUG!!!
                        navi.setCurrentLocation(10021);

                    }else if(depAndDisType == GOOGLE_GOOGLE){
                        destination = address;
                        destinationTV.setText(address.getName());
                        depAndDisType = 0;
                        //出発地と目的地がPULLDOGとGoogleで異なったとき
                        if (departureName != null) {  //出発地点がnullじゃなかったら、出発地点から目的地点
                            if(nameToLatLng(departureName) == null) return;
                            createRoutePulldogAndGoogle(nameToLatLng(departureName), destination);
                        }else if (departureName == null) { //出発地点がnullだったら、現在地から目的地
                            LatLng l = new LatLng(point[0], point[1]);
                            createRoutePulldogAndGoogle(l, destination);
                        }
                    }

                    departure = null;       //出発地初期化
                    destination = null;     //目的地初期化
                    departureName = null;
                    destinationName = null;
                    departureNameHira =null;
                    destinationNameHira = null;

                }
                //TODO 代用部？
                Toast.makeText(this,destinationName,Toast.LENGTH_SHORT).show();
                enableLongPushSW();
                Mode = 0; //モード初期化
                depaOrdest = 0;
                dialogMode = 0;

            }
        } else if( Mode == TWITTER){
            if(dialogMode == DIALOG_1) {
                Mode = 0; //モード初期化
                dialogMode = 0;
                flag=0;
                //Twitterでつぶやく
                /***** 音声入力結果をTweetする *****/
                if (tweetStr != null) {
                    //認証
                    if (!TwitterUtils.hasAccessToken(this)) {
                        Intent intent = new Intent(this, TwitterOAuthActivity.class);
                        startActivity(intent);
                    } else {
                        //Tweetするアクティビティに飛ぶ
                        Intent intent = new Intent(this, TweetActivity.class);
                        intent.putExtra(getString(R.string.twitter_putExtra_key), tweetStr);        //Tweet内容
                        intent.putExtra(getString(R.string.user_latlng), point);                    //現在地
                        startActivityForResult(intent,REQUEST_CODE_TWEET_RETURN);
                    }
                } else {
                    speechText("音声入力をやり直してください");
                }
                enableLongPushSW();
            }
        } else {
            //力覚
            if(navi.isRouting()){
                if  (navi.getCurrentVertex() != null) {
                    if (navi.isExistInRoute(navi.getCurrentVertex().getTagId())) speechText("残りの距離は" + navi.getRemainingDistanceMsg() + "です");
                    else speechText("現在、ルート外にいます。");
                }

                rtdisTxt.setText(Double.toString(navi.getRemainingDistance()));

                String dir = navi.getCurrentDirection();
                if  (dir.equals("左です")) turnOnSolenoid(Route.LEFT);
                else if (dir.equals("右です")) turnOnSolenoid(Route.RIGHT);
            }

        }
    }

    private void drawRoute(Route rt) {
        if  (rt == null){
            Toast.makeText(this, "Can't draw route.", Toast.LENGTH_LONG).show();
            return;
        }
        mMap.clear();
        updateMap(point[0], point[1]);
        drawPolyline(rt.getPoints()); //ルート描画
        drawDot(rt.getPoints()); //ルート頂点描画
        for (ArrayList<LatLng> v : navi.getAreaVertex()){
            drawPolygon(v);
        }
    }

    private void furigana(String destination) {
        String url = getYahooUrl(destination);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);

    }

    private String getYahooUrl(String str){
        String url = new String();
        url = "http://jlp.yahooapis.jp/FuriganaService/V1/furigana?appid=" + YAHOO_ID + "&grade=&sentence=" + str;
        return url;
    }


    private void xmlDecoder(Document doc) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("//Word/Furigana");

        // Object result = expr.evaluate(doc, XPathConstants.STRING);
        NodeList nodeList = (NodeList) expr.evaluate(doc,
                XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String furi = node.getTextContent();
            deTxtView.setText(furi);
            enableLongPushSW();
        }
    }



    @Override
    public void shortPushSW2() {                                        //NO
        if( Mode == ROUTE_NAVI ){
            if(dialogMode == DIALOG_1){
                try {
                    destinationName = results.get(0);
                    results.remove(0);
                    speechText(destinationName+"でいいですか?");
                } catch( Exception e ) {
                    speechText("音声入力をやり直してください");
                    enableLongPushSW();
                    dialogMode = 0;
                    Mode = 0; //モード初期化
                }

            } else if(dialogMode == DIALOG_2){
                address = addressList.poll();
                if (address != null) {
                    StringBuffer st = new StringBuffer("");
                    st.append(address.getName());
                    st.append("");
                    speechText(st.toString()+"でいいですか?");
                }else if (address == null){
                    speechText("検索結果が見つかりません");
                    dialogMode = 0;
                    Mode = 0; //モード初期化
                }
            }
            enableLongPushSW();
        } else if( Mode == TWITTER){
            if(dialogMode == DIALOG_1) {
                speechText("もう一度音声を入力してください");
                tweetStr = null;
                dialogMode = 0;
                Mode = 0; //モード初期化
                enableLongPushSW();
            }
        } else if( btnEnable ){
            /***** 音声入力(Twitter) *****/
            disableLongPushSW();
            Mode = 2;
            //todo 音声認識換えた
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mSpeechRecognizer.setRecognitionListener(this);
            try{
                Intent voiceIntent = RecognizerIntent.getVoiceDetailsIntent(getApplicationContext());
                mSpeechRecognizer.startListening(voiceIntent);
            }catch (ActivityNotFoundException e){
                // 端末が音声認識に対応していない場合
                speechText(getString(R.string.Incompatible));
            }
            Log.d("TAG",Integer.toString(buttonCounter2));

        }
    }

    @Override
    public void longPushSW1() {
        /***** 音声入力(目的地点) *****/
        if(mSpeechRecognizer == null) {
            Mode = ROUTE_NAVI;
            depaOrdest = DEPARTURE;

            //todo 音声認識換えた
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mSpeechRecognizer.setRecognitionListener(this);

            try {
                Intent voiceIntent = RecognizerIntent.getVoiceDetailsIntent(getApplicationContext());
                mSpeechRecognizer.startListening(voiceIntent);
            } catch (ActivityNotFoundException e) {
                // 端末が音声認識に対応していない場合
                speechText(getString(R.string.Incompatible));
            }
        }

    }

    @Override
    public void longPushSW2() {
        /***** 音声入力(出発地点) *****/
        if(mSpeechRecognizer == null) {
            Mode = ROUTE_NAVI;
            depaOrdest = DESTINATION;

            //todo 音声認識換えた
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mSpeechRecognizer.setRecognitionListener(this);

            try {
                Intent voiceIntent = RecognizerIntent.getVoiceDetailsIntent(getApplicationContext());
                mSpeechRecognizer.startListening(voiceIntent);
            } catch (ActivityNotFoundException e) {
                // 端末が音声認識に対応していない場合
                speechText(getString(R.string.Incompatible));
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route_make, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_route_make) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableLongPushSW(){
        btnEnable = true;
        buttonCounter1 = 0;
        buttonCounter2 = 0;
    }

    private void disableLongPushSW(){
        btnEnable = false;
        buttonCounter1 = 0;
        buttonCounter2 = 0;
    }



}
