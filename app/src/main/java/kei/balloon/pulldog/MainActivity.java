package kei.balloon.pulldog;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends FragmentActivity implements Runnable {

	public MainActivity ma;

	//Google Map
	private GoogleMap mMap; // Might be null if Google Play services APK is not available.
	public Marker mLocation = null;

	// USB1関係変数定義
	private UsbManager mUsbManager;
	private UsbDevice mDevice;
	private UsbDeviceConnection mConnection;
	private UsbEndpoint mEndpointOut;
	private UsbEndpoint mEndpointIn;
	private byte[] message = new byte[64];
	private byte[] buffer = new byte[64];
	private final static int USB_SLEEP = 50;
	private final static int USB_INTERRUPT_HZ = (int) (1000 / (double) USB_SLEEP);
	private final static double USB_INTERRUPT_TOSEC_FACTOR = 1.0 / USB_INTERRUPT_HZ;

	// コマンド用定数定義
	private static final byte COMMAND_CHECK = 0x30;        // マイコンとの接続チェック用コマンド（未使用）
	private static final byte COMMAND_LED = 0x31;        // LED制御用コマンド
	private static final byte COMMAND_LCD = 0x32;        // LCD制御用コマンド
	private static final byte COMMAND_SOL = 0x33;        // ソレノイド用コマンド
	private static final byte COMMAND_STATUS = 0x40;    // マイコンの状態取得用コマンド

	// USB受信ハンドラのインスタンス生成
	private final Handler handler = new Handler();

	//USB Thread
	private Thread usbThread;
	private boolean usbThreadFlag = true;

	// アプリ用変数
	private byte flag;  //USB関連
	private int result; //USB関連
	private boolean cameraFocus = true;            //現在地をフォーカス   true:有効 false:無効

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


	public double[] point = {0.0, 0.0, 0.0};

	boolean mThreadIsStopped = true;
	Handler mHandler = new Handler();
	boolean mDataIsRead = false;

	//RFID
	private int tagId = -1;
	private int pastId = -1;

	boolean usbReady = false, gpsReady = false;
	boolean isSpeakedInit = false;

	//RFID通信テスト用変数
	private byte[] rfidTag = new byte[100];

	private TextView RFIDOrQZSS;
	private TextView tagNumberText;
	private TextView latlngText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUpMapIfNeeded();

		ma = this;

		RFIDOrQZSS = (TextView) findViewById(R.id.QZSS_RFID);
		tagNumberText = (TextView) findViewById(R.id.tag_number);
		latlngText = (TextView) findViewById(R.id.latlang);

		//みちびき FTDIのインスタンスをとってくる
		mNowLocation = new NowLocation();
		mQZSS = new QZSS();

		try {
			ftD2xx = D2xxManager.getInstance(this);
		} catch (D2xxManager.D2xxException e) {
			Log.e("TAG", e.toString());
		}

		// USBホストAPIインスタンス生成
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
	}


	@Override
	protected void onResume() {
		super.onResume();

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
		UsbDevice device = (UsbDevice) nowIntent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
			Log.d("TAG", "アプリケーション再起動");
			setDevice(device);
		} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(device)) {
			setDevice(null);
			mConnection.close();
			Log.d("TAG", "デバイス　デタッチで終了");
		}
	}

	/*****
	 * アプリ終了時クローズ処理
	 *****/
	public void onPause() {
		usbThreadFlag = false;

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mThreadIsStopped = true;
		flag = 0;
		closeDevice();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		openDevice();
	}

	/* 通信をオープンにする */
	private void openDevice() {
		if (ftDevice != null && ftDevice.isOpen()) {
			if (mThreadIsStopped) {
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

		Log.d("TAG", "devCount:" + devCount);

		if (devCount <= 0) {
			Log.d("TAG", "とてもつらい");
			return;
		}

		if (ftDevice == null) {
			ftDevice = ftD2xx.openByIndex(this, 0);
		} else {
			synchronized (ftDevice) {
				ftDevice = ftD2xx.openByIndex(this, 0);
			}
		}

		if (ftDevice != null && ftDevice.isOpen()) {
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
			while (true) {
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
											if (mNowLocation.SURVEY_EN || mNowLocation.CORRECTION_ON) {
												//通常測位または補正有測位

												if (mQZSS.GPS_ON || mQZSS.GLONASS_ON) {
													//srcText.setTextColor(0xFFFFFF00);
													//GPS衛星orGLONASS衛星の電波が取得できている
												} else if (mQZSS.QZSS_ON) {
													//srcText.setTextColor(0xFFFF0000);
													//みちびきの電波が取得できている
												} else if (mQZSS.L1SAIF_ON) {
													//srcText.setTextColor(0xFF0000FF);
													//L1SAIF補強信号が受信できている
												} else {
													//srcText.setTextColor(0xFFFFFFFF);
												}

												if (navi.getPositioningMode() == Navi.GPS) {
													//本番はこっち
													point[0] = mNowLocation.getPointLat();
													point[1] = mNowLocation.getPointLng();

													navi.setCurrentLocation(point[0], point[1]);

													updateMap(point[0], point[1]);
												} else {
													//RFID測位＋GPS補正
													if (navi.getReferenceUpdateFlag()) {
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


												latlngText.setText("(" + point[0] + "," + point[1] + ")");
											} else {
												latlngText.setText("NotAvailable");
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
								if (i >= mReadSize) {
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
		if (rNMEA.length > 0) {
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
		if (ftDevice != null) ftDevice.close();
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

	/*****
	 * USBデバイス接続処理
	 *****/
	private boolean setDevice(UsbDevice device) {
		// デバイスインタフェース検出
		if (device.getInterfaceCount() != 1) {
			Log.d("TAG", "インタフェース発見できませんでした");
			return false;
		}
		UsbInterface intf = device.getInterface(0);
		// エンドポイントの検出
		if (intf.getEndpointCount() < 2) {
			Log.d("TAG", "エンドポイントを検出できませんでした");
			return false;
		}
		Log.d("TAG", "エンドポイント数 =" + intf.getEndpointCount());

		// OUTエンドポイントの確認
		UsbEndpoint epout = intf.getEndpoint(0);
		if (epout.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK) {
			Log.d("TAG", "OUTエンドポイントがバルクタイプではありません");
			return false;
		}

		// INエンドポイントの確認
		UsbEndpoint epin = intf.getEndpoint(1);
		if (epin.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK) {
			Log.d("TAG", "INエンドポイントがバルクタイプではありません");
			return false;
		}

		// デバイス定数代入
		mDevice = device;
		mEndpointOut = epout;
		mEndpointIn = epin;

		//　接続許可確認
		if (mDevice != null) {
			UsbDeviceConnection connection = mUsbManager.openDevice(mDevice);
			if (connection != null && connection.claimInterface(intf, true)) {
				UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
				HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
				Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
				Log.d("TAG", "IteratorSize:" + deviceList.values().size());
				while (deviceIterator.hasNext()) {
					UsbDevice dvc = deviceIterator.next();
					Log.d("TAG", "VenderID:" + dvc.getProductId());

					if (dvc.getVendorId() == 1240) {
						//マイコン
						mConnection = connection;
						Log.d("TAG", "USBデバイス接続正常完了");
						flag = 1;

						if (usbThreadFlag) {
							usbThread = new Thread(this);
							usbThread.start();
						}
					}
					if (dvc.getVendorId() == 1027) {
						//みちびき
						Log.d("TAG", "USBデバイス接続正常完了");
						openDevice();
					}
				}

				return true;
			} else {
				mConnection = null;
				Log.d("TAG", "USBデバイス接続失敗");
				return false;
			}
		}

		return false;
	}

	/*****
	 * 状態取得コマンド送信メソッド
	 *****/
	private void sendStatusCommand() {
		synchronized (this) {                // 排他制御で実行
			if (mConnection != null) {
				message[0] = COMMAND_STATUS;
				message[1] = 0x00;
				message[2] = 0x00;
				mConnection.bulkTransfer(mEndpointOut, message, 3, 200);
			}
		}
	}

	/*****
	 * USBデータ状態受信スレッド
	 *****/
	public void run() {
		while (true) {
			if (flag == 1) {
				sendStatusCommand();                // 状態，計測要求コマンド送信
				result = mConnection.bulkTransfer(mEndpointIn, buffer, 64, 200); //状態受信処理　常に64byte受信
				if (result > 0) {                        // 正常受信なら受信データ処理実行
					handler.post(new Runnable() {    // データ表示
						public void run() {

							usbReady = true;

							if (gpsReady && usbReady && !isSpeakedInit) {
								Toast.makeText(ma, "プルドッグは正常に起動しました", Toast.LENGTH_LONG).show();
							}

							rfidTag[4] = buffer[4];
							rfidTag[5] = buffer[5];
							rfidTag[6] = buffer[6];
							rfidTag[7] = buffer[7];
							rfidTag[8] = buffer[8];
							rfidTag[9] = buffer[9];

							//RFID測位部
							if (navi.getPositioningMode() == Navi.RFID) {
								tagNumberText.setText(Integer.toString((((int) rfidTag[6]) << 8) + (int) rfidTag[7]));  //todo デバッグ時は消す

								try {
									int tagId = Integer.parseInt(tagNumberText.getText().toString());

									if ((rfidTag[4] != 0 || (rfidTag[5] == 1 || rfidTag[5] == 2) || rfidTag[8] != 0 || rfidTag[9] != 0) && navi.isExist(tagId) && !isVisited && pastId != tagId) {
										int status = navi.setCurrentLocation(tagId);
										navi.updateReferencePoint();
									}

									point[0] = navi.getLatLng().latitude;
									point[1] = navi.getLatLng().longitude;
									updateMap(point[0], point[1]);

								} catch (Exception e) {
									e.printStackTrace();
									Log.e("ERROR", e.getMessage());
								}
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


	/*****
	 * Google Mapセットアップ
	 *****/
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

	/*****
	 * GoogleMap マーカー更新
	 *****/
	public void updateMap(double lat, double lng) {
		LatLng p = new LatLng(lat, lng);

		if (mLocation != null) mLocation.remove();
		mLocation = mMap.addMarker(new MarkerOptions().position(p).title("げんざいち"));

		if (cameraFocus) {
			CameraUpdate cu =
					CameraUpdateFactory.newLatLngZoom(
							new LatLng(lat, lng), 25f);
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

	/*****
	 * GoogleMap ポリゴン描画
	 *****/
	public void drawPolygon(ArrayList<LatLng> latlng) {
		PolygonOptions rectOptions = new PolygonOptions();
		for (LatLng ll : latlng) {
			rectOptions.add(ll);
		}
		Polygon polygon_newsta = mMap.addPolygon(rectOptions);
		//線の幅を指定（ピクセル単位）
		polygon_newsta.setStrokeWidth(2);
		//線の色を指定（ARGBフォーマット）
		polygon_newsta.setStrokeColor(Color.CYAN);
		//塗りつぶす場合、色を指定（ARGBフォーマット、半透明な青の場合）
		polygon_newsta.setFillColor(0x557777ff);

	}

	/*****
	 * GoogleMap ポリライン描画
	 *****/
	public void drawPolyline(List<LatLng> latlng) {
		PolylineOptions options = new PolylineOptions();
		for (LatLng ll : latlng) {
			options.add(ll);
		}
		options.color(0xff2FAEFC);
		options.width(20);
		options.geodesic(true); // 測地線で表示する場合、地図上で２点間を結ぶ最短曲線
		mMap.addPolyline(options);
	}
}
