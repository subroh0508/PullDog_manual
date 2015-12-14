package kei.balloon.pulldog;


import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends FragmentActivity implements Runnable, Serializable {

	private final static String FILE_PATH = "ExperimentForKitano/TagList.txt";
	private final static String FILE_PATH_TEST = "ExperimentForKitano/TagList_TNCT.txt";
	private MainActivity activity;
	private GoogleMapFragment mapFragment = new GoogleMapFragment();
	private InformationFragment infoFragment = new InformationFragment();

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

	//USB Thread
	private Thread usbThread;
	private boolean usbThreadFlag = true;

	// アプリ用変数
	private byte flag;  //USB関連
	private int result; //USB関連

	//みちびき(QZ1・FTDI)
	private static D2xxManager ftD2xx = null;
	private FT_Device ftDevice = null;
	static final int READBUF_SIZE = 256;
	byte[] rbuf = new byte[1]; //受信モジュールから生データを受ける
	char[] rchar = new char[READBUF_SIZE]; //生データを文字データ変換して受ける
	int mReadSize = 0; //受けたデータのサイズ

	public String[] rNMEA;
	public String data;
	public NowLocation mNowLocation; //
	public Qzss mQZSS;

	public double[] point = {0.0, 0.0, 0.0};

	boolean mThreadIsStopped = true;
	boolean mDataIsRead = false;

	//RFID
	private RfidManager tagList;
	private int tagId = -1;

	boolean RFIDReady = false, GPSReady = false;

	//RFID通信テスト用変数
	private byte[] RFIDTag = new byte[100];

	private Bundle infoBundle, mapBundle;
	private double testlat = 0.0, testlng = 0.0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_activity);
		//setUpMapIfNeeded();

		activity = this;

		//TagList読み込み
		tagList = new RfidManager(this, FILE_PATH);

		//みちびき FTDIのインスタンスをとってくる
		mNowLocation = new NowLocation(tagList);
		mQZSS = new Qzss();

		try {
			ftD2xx = D2xxManager.getInstance(this);
		} catch (D2xxManager.D2xxException e) {
			Log.e("TAG", e.toString());
		}

		// USBホストAPIインスタンス生成
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		FragmentTabHost tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		tabHost.setup(this, getSupportFragmentManager(), R.id.content);

		TabHost.TabSpec infoTab = tabHost.newTabSpec("Information").setIndicator("Info");
		infoBundle = new Bundle();
		infoBundle.putSerializable("QZSS", mQZSS);
		tabHost.addTab(infoTab, InformationFragment.class, infoBundle);

		TabHost.TabSpec mapTab = tabHost.newTabSpec("googleMap").setIndicator("Map");
		mapBundle = new Bundle();
		mapBundle.putSerializable("NowLocation", mNowLocation);
		tabHost.addTab(mapTab, GoogleMapFragment.class, mapBundle);
	}


	@Override
	protected void onResume() {
		super.onResume();

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
	@Override
	public void onPause() {
		super.onPause();
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
				new Thread(receiveLoop).start();
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
				new Thread(receiveLoop).start();
				Log.d("TAG", "スレッド起動");
			}
		}
	}

	/* データの逐次取得 */
	private Runnable receiveLoop = new Runnable() {
		@Override
		public void run() {
			int readSize, i = 0;

			GPSReady = true;
			mThreadIsStopped = false;

			if (RFIDReady)
				Log.d("main", "プルドッグは正常に起動しました");

			while (true) {
				if (mThreadIsStopped) break;

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
								mQZSS.setLog(data);
								//Log.d("Main", mQZSS.getLog());
								//Log.d("Main", "("+mNowLocation.getNowPoint().latitude+","
										//+mNowLocation.getNowPoint().longitude+")");

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

				//Log.d("Main", "GNSS Loop");
			}
		}
	};

	private void arrangeData(String[] rNMEA) {
		if (rNMEA.length > 0) {
			if(rNMEA[0].equals("GPGNS")) {
				mQZSS.setGPS(rNMEA);
				mNowLocation.setGnssPoint(rNMEA);
			} else if(rNMEA[0].equals("GLGNS")) {
				mQZSS.setGLONASS(rNMEA);
				mNowLocation.setGnssPoint(rNMEA);
			} else if(rNMEA[0].equals("GNGNS")) {
				mQZSS.setGNSS(rNMEA);
				mNowLocation.setGnssPoint(rNMEA);
			} else if(rNMEA[0].equals("GPGSA")) {
				mQZSS.setUsefulGNSS(rNMEA);
				mQZSS.checkQZSS();
			} else if(rNMEA[0].equals("GNGSA")) {
				mQZSS.setUsefulGNSS(rNMEA);
				mQZSS.checkQZSS();
			} else if(rNMEA[0].equals("GPGSV")) {
				mQZSS.setVisibleGNSS(rNMEA);
			} else if(rNMEA[0].equals("QZGSV")) {
				mQZSS.setVisibleQZSS(rNMEA);
				mQZSS.checkL1SAIF();
			} else if(rNMEA[0].equals("GNVTG")) {
				mNowLocation.setVelocity(rNMEA);
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
		if (!ftDevice.isOpen()) return;


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
					Log.d("TAG", "VenderID:" + dvc.getVendorId());

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

		if (GPSReady)
			Log.d("main", "プルドッグは正常に起動しました");

		while (true) {
			if (flag == 1) {
				sendStatusCommand();                // 状態，計測要求コマンド送信
				result = mConnection.bulkTransfer(mEndpointIn, buffer, 64, 200); //状態受信処理　常に64byte受信
				if (result > 0) {
					// 正常受信なら受信データ処理実行
					for(int i = 4; i < 10; i++) RFIDTag[i] = buffer[i];

					tagId = (((int) RFIDTag[6]) << 8) + (int) RFIDTag[7];

					//Log.d("main", "tagID:"+tagId);

					mNowLocation.setTagId(tagId);

					try {
						usbThread.sleep(USB_SLEEP);            //インターバル
					} catch (InterruptedException e) {

					}

				} else {
					mConnection.close();
					flag = 0;

					Log.d("TAG", "デバイスが切り離されました");
				}
			}

			//Log.d("Main", "RFID Loop");
		}
	}
}
