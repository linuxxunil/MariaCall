package edu.mariacall.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.LinkedList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import alg.common.Log;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import edu.mariacall.R;
import edu.mariacall.algorithm.Kalman;
import edu.mariacall.algorithm.BPNN;
import edu.mariacall.database.DatabaseDriver;
import edu.mariacall.database.DatabaseTable;
import edu.mariacall.database.MsResultSet;
import edu.mariacall.database.SqliteDriver;
import edu.mariacall.location.Beacon;

class LocationBPNN extends BPNN {
	DatabaseDriver db = null;
	ResultSet[] rs;
	double yMax, yMin;
	double dMax = 0.5, dMin = -0.5;

	String[] macSet = { "D0:39:72:D9:FA:2A", "D0:39:72:D9:FA:65",
			"D0:39:72:D9:FE:D6" };

	public LocationBPNN() {
		super();
	}

	public LocationBPNN(int nCount, int nLearnDSet, int nInput, int nHidden,
			int nOutput, float eta, float alpha) {
		super(nCount, nLearnDSet, nInput, nHidden, nOutput, eta, alpha);
	}

	public LocationBPNN(int nCount, int nLearnDSet,
			LinkedList<float[][]> weight, LinkedList<float[]> theta, float eta,
			float aplpha) {
		super(nCount, nLearnDSet, weight, theta, eta, aplpha);
	}

	public float convert(float y) {
		return (float) ((((y - yMin) * (dMax - dMin) / (yMax - yMin))) + dMin);
	}
	
	public void initYminYmax(int type) {
		String path = "";
		switch(type) {
		case 0: path = "/sdcard/data/mariacall/location.sqlite_00";
		case 1:	path = "/sdcard/data/mariacall/location.sqlite_01"; //kalman
		case 2: path = "/sdcard/data/mariacall/location.sqlite_02";
		case 3: path = "/sdcard/data/mariacall/location.sqlite_03";
		}
		
		DatabaseDriver db = new SqliteDriver(path);
		db.onConnect();
		rs = new ResultSet[nInput];
		
		try {
			MsResultSet ms = db.select("SELECT MIN(rssi) FROM Ibeacon");
			if ( ms.rs.next() )  {
				yMax = ms.rs.getDouble(1);
			}
			ms = db.select("SELECT MAX(rssi) FROM Ibeacon");
			if ( ms.rs.next() )  {
				yMin = ms.rs.getDouble(1);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		db.close();
	}
}

public class BPNNLocationActivity extends ControllerActivity {
	/* UI */
	private Button btnStart = null;
	private CheckBox cBoxWinAvg = null;
	private CheckBox cBoxKalman = null;
	private CheckBox cBoxWinAvgParm = null;
	private CheckBox cBoxKalmanParm = null;
	private TextView tViwAreaID = null;
	private DatabaseDriver db = null;
	private boolean flgStart = false;
	private Context context;
	/* Definition Handler tag */
	private final static int TAG_LOCATION_START = 1;

	/* bluetooth */
	private Beacon beacon = null;

	/* algorithm */
	private final int maxWinAvg = 10;
	private double[] winAvg;
	private Kalman kalman = null;

	private LocationBPNN bpnn = null;
	private int nInput = 3;
	private int nHidden = 8;
	private int nOutput = 11;
	private float[][] wXH, wHY;
	private float[] thetaH, thetaY;
	private float[] X = null;
	private float[] Y = null;
	private LinkedList<float[][]> wList = null;
	private LinkedList<float[]> tList = null;
	private Hashtable<String,Float> hash = null;
	private String[] macSet;

	/* Initial */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();

		initLayout();

		initListeners();

		initHandler();

		// initDatabase();

		initAlgorithm();

		if (beacon == null)
			beacon = new Beacon(context);
	}

	private void initAlgorithm() {
		// initial windows average
		winAvg = new double[maxWinAvg];

		// initial kalman
		kalman = new Kalman(-59, 0.1, 0.1, 0.1);
		bpnn = new LocationBPNN();
		X = new float[nInput];
		macSet = new String[nInput];
		wXH = new float[nInput][nHidden];
		wHY = new float[nHidden][nOutput];
		thetaH = new float[nHidden];
		thetaY = new float[nOutput];
		wList = new LinkedList<float[][]>();
		tList = new	LinkedList<float[]>();
		
		for (int i=0; i<macSet.length; i++) 
        	macSet[i] = new String("");
	}

	private void setParmToBPNN(String line) {

		String[] item = line.split(";");
		
		int p = 0;
		for (int i=0; i<wXH.length; i++) {
			for (int j=0; j<wXH[i].length; j++) {
				wXH[i][j] = Float.valueOf(item[p++].split("=")[1]);
			}
		}
		
		for (int i=0; i<wHY.length; i++) {
			for (int j=0; j<wHY[i].length; j++) {
				wHY[i][j] = Float.valueOf(item[p++].split("=")[1]);
			}
		}
		
		for (int i=0; i<thetaH.length; i++)
			thetaH[i] = Float.valueOf(item[p++].split("=")[1]);
		
		for (int i=0; i<thetaY.length; i++)
			thetaY[i] = Float.valueOf(item[p++].split("=")[1]);
		
		wList.clear();
		tList.clear();
		wList.add(wXH);
		wList.add(wHY);
		tList.add(thetaH);
		tList.add(thetaY);
		bpnn.reSetParmeter(1000, 50, wList, tList, 0.5f, 0.2f);
		
	}
	
	private int matchMacSet(String mac) {
		int match = -1;
		for (int i=0; i<macSet.length; i++) {
    		if ( macSet[i].equals(mac)) {    			
    			match = i;
    			break;
    		} else if ( macSet[i].equals("") ) {
    			macSet[i] = mac;
    			match = i;
    			break;
    		}
    	}
		return match;
	}

	private void setBpnnParm(int type) {
		String path = "/sdcard/data/mariacall/none_wei.txt" ;
		FileReader fr;
		BufferedReader br;
		String line;
		bpnn.initYminYmax(type);
		try {
			switch (type) {
			case 0: path = "/sdcard/data/mariacall/none_wei.txt"; break;
			case 1: path = "/sdcard/data/mariacall/kalman_wei.txt";break;// kalman
			case 2: path = "/sdcard/data/mariacall/winAvg_wei.txt";break;
			case 3: path = "/sdcard/data/mariacall/hybrid_wei.txt";break;
			}
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			line = br.readLine(); //讀第一行
			setParmToBPNN(line);
			br.close();
			fr.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initLayout() {
		setContentView(R.layout.layout_bpnn_location);
	}

	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case TAG_LOCATION_START:
					locationStartResult(msg);
					break;
				}
			}
		};
	}

	private void initListeners() {
		btnStart = (Button) findViewById(R.id.bpn_btnStart);
		cBoxKalman = (CheckBox) findViewById(R.id.bpn_cBoxKalman);
		cBoxWinAvg = (CheckBox) findViewById(R.id.bpn_cBoxWinAvg);
		cBoxKalmanParm = (CheckBox) findViewById(R.id.bpn_cBoxKalmanParm);
		cBoxWinAvgParm = (CheckBox) findViewById(R.id.bpn_cBoxWinAvgParm);
		tViwAreaID = (TextView) findViewById(R.id.bpn_txtAreaID);
		

		btnStart.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (flgStart == false) {
					btnStart.setText("關閉");
					
					if (cBoxWinAvgParm.isChecked() )
						setBpnnParm(2);
					else if (cBoxKalmanParm.isChecked() )
						setBpnnParm(1);
					else if (cBoxWinAvgParm.isChecked() 
								&& cBoxKalmanParm.isChecked())
						setBpnnParm(3);
					else
						setBpnnParm(0);
					
					locationStart();
				} else {
					btnStart.setText("啟動");
					// location start
					locationStop();
					// close db
					// db.close();
				}
				flgStart ^= true;
			}
		});
	}

	private void initDatabase() {
		db = new SqliteDriver(dbPath);
		db.onConnect();
		db.createTable(DatabaseTable.Ibeacon.create());
		db.close();
	}

	private void sendMessage(int what, int statusCode, String mac) {
		if (handler != null) {
			Message msg = handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putInt("rssi", statusCode);
			bundle.putString("mac", mac);
			msg.what = what;
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}

	private void insertBeaconInfo(int deviceID, String deviceMAC, double rssi,
			double distance, boolean winAvg, boolean kalman) {
		String sql = "INSERT INTO " + DatabaseTable.Ibeacon.name + "(\""
				+ DatabaseTable.Ibeacon.colDeviceID + "\"," + "	\""
				+ DatabaseTable.Ibeacon.colDeviceMAC + "\"," + " \""
				+ DatabaseTable.Ibeacon.colRSSI + "\"," + " \""
				+ DatabaseTable.Ibeacon.colDistance + "\"," + " \""
				+ DatabaseTable.Ibeacon.colWinAvg + "\"," + " \""
				+ DatabaseTable.Ibeacon.colKalman + "\")" + " VALUES " + "(\""
				+ deviceID + "\"," + "	\"" + deviceMAC + "\"," + " \"" + rssi
				+ "\"," + " \"" + distance + "\"," + " \"" + (winAvg ? 1 : 0)
				+ "\"," + " \"" + (kalman ? 1 : 0) + "\")";
		db.insert(sql);
	}

	/* Location */
	private void locationStart() {

		beacon.startLeScan(new LeScanCallback() {
			@Override
			public void onLeScan(BluetoothDevice arg0, int rssi,
					byte[] scanRecord) {
				sendMessage(TAG_LOCATION_START, rssi, arg0.toString());
			}
		});
	}

	private void locationStop() {

		beacon.stopLeScan(new LeScanCallback() {
			@Override
			public void onLeScan(BluetoothDevice arg0, int rssi,
					byte[] scanRecord) {
			}
		});
	}

	private void locationStartResult(Message msg) {
		double rssi = (double) msg.getData().getInt("rssi");
		String mac = msg.getData().getString("mac");
		int match = 0;
		float max = 0.0f;
		int id = 0;
		
		
		if (cBoxWinAvg.isChecked())
			rssi = exeWinAvg(rssi, 1);
		if (cBoxKalman.isChecked())
			rssi = exeKalman(rssi);

		
		match = matchMacSet(mac);
		System.out.println(rssi);
		X[match] = bpnn.convert((float) rssi);
		System.out.println(X[match]);
		Y = bpnn.predict(X);
		for ( int i=0; i<Y.length; i++) {
			if ( max < Y[i]) {
				max = Y[i];
				id = i;
			}
		}
		tViwAreaID.setText("AreaID="+id);
	}

	private double exeKalman(double rssi) {
		kalman.correct(rssi);
		kalman.predict();
		kalman.update();
		return kalman.getStateEstimation();
	}

	private double exeWinAvg(double rssi, int exeCount) {
		double sum = 0;

		if (winAvg[maxWinAvg - 1] == 0) {
			for (int i = 0; i < maxWinAvg; i++)
				winAvg[i] = rssi;
		}

		winAvg[exeCount % maxWinAvg] = rssi;

		for (int i = 0; i < maxWinAvg; i++) {
			sum += winAvg[i];
		}
		return (sum / (double) maxWinAvg);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			changeActivity(BPNNLocationActivity.this, MenuActivity.class);
		}
		return true;
	}
}