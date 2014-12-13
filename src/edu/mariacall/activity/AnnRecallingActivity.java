package edu.mariacall.activity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.mariacall.R;
import edu.mariacall.algorithm.Kalman;
import edu.mariacall.database.DatabaseDriver;
import edu.mariacall.database.DatabaseTable;
import edu.mariacall.database.SqliteDriver;
import edu.mariacall.location.Beacon;

public class AnnRecallingActivity extends ControllerActivity {
	/* UI */
	private Button btnStart = null;
	private CheckBox cBoxDB = null;
	private CheckBox cBoxWinAvg = null;
	private CheckBox cBoxKalman = null;
	private CheckBox cBoxAuto = null;
	private CheckBox cBoxWinAvgWeight = null;
	private CheckBox cBoxKalmanWeight = null;
	private TextView tViwAreaID = null;
	private EditText eTxtID = null;
	private EditText eTxtQuantity = null;
	private EditText eTxtDetectTimes = null;
	private DatabaseDriver db = null;
	private boolean flgStart = false;
	private LinearLayout lLayChart = null;
	private Context context;
	/* Definition Handler tag */
	private final static int TAG_LOCATION_START = 1;

	/* bluetooth */
	private Beacon beacon = null;

	/* algorithm */
	private int dataSetLen = -1;
	private final int maxWinAvg = 10;
	private double[][] winAvg;
	private Kalman[] kalman = null;
	private NeuralNetwork nnet = null;
	private LearningRule nnRule = null;
	private String nnWeightFile00 = "/sdcard/data/Mariacall/ANN/NN00.wei";
	private String nnWeightFile01 = "/sdcard/data/Mariacall/ANN/NN01.wei";
	private String nnWeightFile02 = "/sdcard/data/Mariacall/ANN/NN02.wei";
	private String nnWeightFile03 = "/sdcard/data/Mariacall/ANN/NN03.wei";


	/* Chart */  int maxTimes = 0;
	private String[] floorMac;
	private int[] sequence;
	private double[] rssiSet;
	private double[] normRssiSet;
	private LinkedList<Double>[] dataSetLists;
	private String[] dataSetName = { "Beacon1", "Beacon2", "Beacon3", "Beacon4" };
	private int[] dataSetColors = { Color.BLUE, Color.GREEN,
			Color.rgb(0xFF, 0xA5, 0x00), Color.RED };
	private PointStyle[] dataSetStyles = { PointStyle.CIRCLE,
			PointStyle.CIRCLE, PointStyle.CIRCLE, PointStyle.CIRCLE };

	// background definition
	private String title = "Signal Strength";
	private XYSeries[] series;
	private XYMultipleSeriesDataset mDataset;
	private GraphicalView chart;
	private XYMultipleSeriesRenderer renderer;
	private int id;

	/* Initial */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();

		initLayout();

		initListeners();

		initChart();

		initHandler();

		initAlgorithm();

		initDatabase();

		if (beacon == null)
			beacon = new Beacon(context);
	}

	private void initLayout() {
		setContentView(R.layout.layout_ann_recalling);
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
		btnStart = (Button) findViewById(R.id.anr_btnStart);
		eTxtID = (EditText) findViewById(R.id.anr_eTxtID);
		eTxtQuantity = (EditText) findViewById(R.id.anr_eTxtQuantity);
		eTxtDetectTimes = (EditText) findViewById(R.id.anr_eTxtDetectTimes);
		cBoxDB = (CheckBox) findViewById(R.id.anr_cBoxDB);
		cBoxKalman = (CheckBox) findViewById(R.id.anr_cBoxKalman);
		cBoxWinAvg = (CheckBox) findViewById(R.id.anr_cBoxWinAvg);
		cBoxAuto = (CheckBox) findViewById(R.id.anr_cBoxAuto);
		cBoxKalmanWeight = (CheckBox) findViewById(R.id.anr_cBoxKalmanWeight);
		cBoxWinAvgWeight = (CheckBox) findViewById(R.id.anr_cBoxWinAvgWeight);
		tViwAreaID = (TextView) findViewById(R.id.anr_txtAreaID);
		lLayChart = (LinearLayout) findViewById(R.id.anr_lLayChart);

		btnStart.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (flgStart == false) {
					btnStart.setText("關閉");
					reset();
					db.onConnect();
					locationStart();
				} else {
					btnStart.setText("啟動");
					// location start
					locationStop();
					maxTimes = 0;
					db.close();
				}
				flgStart ^= true;
			}
		});
	}

	private void initAlgorithm() {
		dataSetLen = Integer.valueOf(eTxtQuantity.getText().toString());
		setKalman();
		setWinAvg();
		setOther();
		// Multi Layer Perceptron
		setNN();

	}

	private void initDatabase() {
		db = new SqliteDriver(dbPath);
		db.onConnect();
		db.createTable(DatabaseTable.AnnTesting.create());
		db.close();
	}

	private void initChart() {
		maxTimes = Integer.valueOf(eTxtDetectTimes.getText().toString());
		dataSetLen = Integer.valueOf(eTxtQuantity.getText().toString());
		setChart();
	}

	private void reset() {
		int n = Integer.valueOf(eTxtQuantity.getText().toString());
		maxTimes = Integer.valueOf(eTxtDetectTimes.getText().toString());
		/* reset chart */
		if (dataSetLen != n) {
			
			dataSetLen = n;
			setChart();
			setKalman();
			setWinAvg();
			setOther();
		} else {
			for (int i = 0; i < dataSetLists.length; i++) {
				dataSetLists[i].clear();
			}
		}
		setNN();
	}

	private void setChart() {
		dataSetLists = new LinkedList[dataSetLen];
		for (int i = 0; i < dataSetLen; i++)
			dataSetLists[i] = new LinkedList<Double>();

		series = new XYSeries[dataSetLen];
		for (int i = 0; i < dataSetLen; i++)
			series[i] = new XYSeries(dataSetName[i]);

		// 創建一個數據集的實例，這個數據集將被用來創建圖表
		mDataset = new XYMultipleSeriesDataset();

		// 將點集添加到這個數據集中
		for (int i = 0; i < dataSetLen; i++)
			mDataset.addSeries(series[i]);

		// 以下都是曲線的樣式和屬性等等的設置，renderer相當於一個用來給圖表做渲染的句柄
		renderer = buildRenderer(dataSetColors, dataSetStyles, true);

		// 設置好圖表的樣式
		setChartSettings(renderer);

		// 生成圖表
		chart = ChartFactory.getLineChartView(context, mDataset, renderer);

		// 將圖表添加到布局中去
		lLayChart.removeAllViews();
		lLayChart.addView(chart, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	private void setOther() {
		sequence = new int[dataSetLen];
		rssiSet = new double[dataSetLen];
		normRssiSet = new double[dataSetLen];
		for (int i = 0; i < dataSetLen; i++) {
			sequence[i] = 0;
			rssiSet[i] = 0;
		}

		floorMac = getFloorMac(2);

	}

	private void setWinAvg() {
		winAvg = new double[dataSetLen][maxWinAvg];
		for (int i = 0; i < kalman.length; i++)
			winAvg[i] = new double[maxWinAvg];
	}

	private void setKalman() {
		kalman = new Kalman[dataSetLen];
		for (int i = 0; i < kalman.length; i++)
			kalman[i] = new Kalman(kX, kP, kQ, kR);
	}

	private void setNN() {
		double[] weight = null;
		String nnWeightFile;
		if (cBoxWinAvgWeight.isChecked() && cBoxKalmanWeight.isChecked()) {
			nnWeightFile = nnWeightFile03;
			nnet = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 4,
					20, 11);
		} else if (cBoxWinAvgWeight.isChecked()) {
			nnWeightFile = nnWeightFile02;
			nnet = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 4,
					20, 11);
		} else if (cBoxKalmanWeight.isChecked()) {
			nnWeightFile = nnWeightFile01;
			nnet = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 4,
					20, 11);
		} else {
			nnWeightFile = nnWeightFile00;
			nnet = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 4,
					20, 11);

		}
		weight = getWeight(nnWeightFile);
		nnet.setWeights(weight);
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

	private void insertTestingInfo(int deviceID,int predict, String[] macSet, double[] rssiSet,
			 boolean winAvg, boolean kalman) {
		String sql = "INSERT INTO " + DatabaseTable.AnnTesting.name + "(\""
				+ DatabaseTable.AnnTesting.colDeviceID + "\"," + "	\""
				+ DatabaseTable.AnnTesting.colPredict + "\"," + "	\""
				+ DatabaseTable.AnnTesting.colDeviceMAC + "\"," + " \""
				+ DatabaseTable.AnnTesting.colRSSI + "\"," + " \""
				+ DatabaseTable.AnnTesting.colWinAvg + "\"," + " \""
				+ DatabaseTable.AnnTesting.colKalman + "\")" 
				+ " VALUES (" 
				+ "\"" + deviceID 	+ "\"," 
				+ "\"" + predict 	+ "\"," ;
				for (int i=0; i<macSet.length; i++) {
					if ( i!=0) sql += ";" + macSet[i];
					else sql +=  "\"" + macSet[i] ;
				}
				for (int i=0; i<rssiSet.length; i++) {
					if ( i!=0) sql += ";" + rssiSet[i];
					else sql += "\",\"" + rssiSet[i] ;
				}
				sql +="\",\"" + (winAvg ? 1 : 0) + "\"," 
					+ "\"" + (kalman ? 1 : 0) + "\")";
				db.insert(sql);
	}

	private double[] getWeight(String nnName) {
		FileReader fr = null;
		BufferedReader br = null;
		double[] weight = null;
		LinkedList<String> list = new LinkedList<String>();
		try {
			fr = new FileReader(nnName);
			br = new BufferedReader(fr);

			String tmp = "";
			while ((tmp = br.readLine()) != null) {
				list.add(tmp);
			}

			weight = new double[list.size()];

			for (int i = 0; i < list.size(); i++) {
				weight[i] = Double.valueOf(list.get(i));
			}
		} catch (FileNotFoundException e) {
			weight = null;
		} catch (IOException e1) {
			weight = null;
		} finally {
			try {
				br.close();
				fr.close();
			} catch (Exception e) {
				// nothing
			}
		}
		return weight;
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

	public double normalize(double y) {

		double dMax = 0.8f;
		double dMin = -0.8f;
		double yMax = 100.0f;
		double yMin = 40.0f;
		y = -y;
		if (y > yMax)
			y = yMax;
		else if (y < yMin)
			y = yMin;
		return (double) ((((y - yMin) * (dMax - dMin) / (yMax - yMin))) + dMin);
	}

	private void locationStartResult(Message msg) {
		double rssi = (double) msg.getData().getInt("rssi");
		String mac = msg.getData().getString("mac");
		id = Integer.valueOf(eTxtID.getText().toString());
		int match = 0;

		match = matchMacSet(mac);

		if (match == -1) {
			return;
		} else {
			rssiSet[match] = rssi;

			if (cBoxWinAvg.isChecked())
				rssi = exeWinAvg(match, rssi, dataSetLists[match].size());
			if (cBoxKalman.isChecked())
				rssi = exeKalman(match, rssi);

			// add rssi to list
			if (dataSetLists[match].size() < maxTimes) {
				dataSetLists[match].add(rssi);
			}

			rssiSet[match] = rssi;
			normRssiSet[match] = normalize(rssiSet[match]);
		}

		int predict = execMLP();
		
		tViwAreaID.setText("AreaID=" + predict);
		if (cBoxDB.isChecked()) {
			insertTestingInfo(id, predict,floorMac, rssiSet,
					cBoxWinAvg.isChecked(), cBoxKalman.isChecked());
		}
	
		// update chart
		updateChart(dataSetLists);

		// check finish
		checkFinish(dataSetLists);
	}

	private int execMLP() {
		nnet.setInput(normRssiSet);
		nnet.calculate();
		double[] networkOutput = nnet.getOutput();
		double max = 2;
		int id = 0;

		max = networkOutput[0];
		id = 0;
		for (int i = 1; i < networkOutput.length; i++) {
			if (networkOutput[i] > max) {
				max = networkOutput[i];
				id = i;
			}
		}
		return id;
	}

	private int matchMacSet(String mac) {
		int match = -1;
		for (int i = 0; i < floorMac.length; i++) {
			if (floorMac[i].equals(mac)) {
				match = i;
				break;
			}// else if (macSet[i].equals("")) {
			//	macSet[i] = mac;
			//	match = i;
			//	break;
			//}
		}
		return match;
	}

	private double exeKalman(int p, double rssi) {
		kalman[p].correct(rssi);
		kalman[p].predict();
		kalman[p].update();
		return kalman[p].getStateEstimation();
	}

	private double exeWinAvg(int p, double rssi, int exeCount) {
		double sum = 0;

		if (winAvg[p][maxWinAvg - 1] == 0) {
			for (int i = 0; i < maxWinAvg; i++)
				winAvg[p][i] = rssi;
		}

		winAvg[p][exeCount % maxWinAvg] = rssi;

		for (int i = 0; i < maxWinAvg; i++) {
			sum += winAvg[p][i];
		}
		return (sum / (double) maxWinAvg);
	}

	int testCase = 0;
	private void checkFinish(LinkedList<Double>[] list) {
		boolean finish = true;

		if (maxTimes == 0) {
			return;
		} else {
			for (int i = 0; i < list.length; i++) {
				if (list[i].size() < maxTimes) {
					finish = false;
					break;
				}
			}

			if (finish) {
				if (cBoxAuto.isChecked()) {
					switch (++testCase) {
					case 1:
						for (int i = 0; i < list.length; i++)
							list[i].clear();
						shoot(this,"00");
						cBoxWinAvg.setChecked(false);
						cBoxKalman.setChecked(true);
						cBoxWinAvgWeight.setChecked(false);
						cBoxKalmanWeight.setChecked(true);
						break;
					case 2:
						for (int i = 0; i < list.length; i++)
							list[i].clear();
						shoot(this,"01");
						cBoxWinAvg.setChecked(true);
						cBoxKalman.setChecked(false);
						cBoxWinAvgWeight.setChecked(true);
						cBoxKalmanWeight.setChecked(false);
						break;
					case 3:
						for (int i = 0; i < list.length; i++)
							list[i].clear();
						shoot(this,"02");
						cBoxWinAvg.setChecked(true);
						cBoxKalman.setChecked(true);
						cBoxWinAvgWeight.setChecked(true);
						cBoxKalmanWeight.setChecked(true);
						break;
					case 4:
						shoot(this,"03");
						cBoxWinAvg.setChecked(false);
						cBoxKalman.setChecked(false);
						cBoxWinAvgWeight.setChecked(false);
						cBoxKalmanWeight.setChecked(false);
						testCase = 0;
						btnStart.callOnClick();
						PlaySound(context);
						Builder alertDialog = new AlertDialog.Builder(
								AnnRecallingActivity.this);
						alertDialog.setTitle("提示");
						alertDialog.setMessage("測試完成");
						alertDialog.setPositiveButton("確定", null);
						alertDialog.show();
						break;
					}
					setNN();
				} else {
					btnStart.callOnClick();
					PlaySound(context);
					if (cBoxKalman.isChecked() && cBoxWinAvg.isChecked())shoot(this,"03");
					else if(cBoxWinAvg.isChecked())shoot(this,"02");
					else if(cBoxKalman.isChecked())shoot(this,"01");
					else shoot(this,"00");
					Builder alertDialog = new AlertDialog.Builder(
							AnnRecallingActivity.this);
					alertDialog.setTitle("提示");
					alertDialog.setMessage("測試完成");
					alertDialog.setPositiveButton("確定", null);
					alertDialog.show();
				}
			}
		}
	}

	/* Chart */
	private XYMultipleSeriesRenderer buildRenderer(int[] colors,
			PointStyle[] styles, boolean fill) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		for (int i = 0; i < dataSetLen; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			r.setFillPoints(fill);
			renderer.addSeriesRenderer(r); // 將座標變成線加入圖中顯示
		}
		return renderer;
	}

	private void setChartSettings(XYMultipleSeriesRenderer renderer) {
		// 有關對圖表的渲染可參看api文檔

		renderer.setChartTitle(title);
		renderer.setXAxisMin(0);
		renderer.setXAxisMax(100);
		renderer.setYAxisMin(-20);
		renderer.setYAxisMax(-100);
		renderer.setAxesColor(Color.WHITE);
		renderer.setLabelsColor(Color.WHITE);
		renderer.setShowGrid(true);
		renderer.setGridColor(Color.LTGRAY);
		renderer.setXLabels(20);
		renderer.setYLabels(20);
		renderer.setXTitle("Time");
		renderer.setYTitle("dBm");
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setPointSize((float) 2);
		renderer.setShowLegend(false);
		// renderer.setPanEnabled(false);
		// renderer.setZoomEnabled(false);
	}

	private void updateChart(LinkedList<Double>[] list) {
		// 移除數據集中舊的點集
		for (int i = 0; i < dataSetLen; i++)
			mDataset.removeSeries(series[i]);

		// 點集先清空，為了做成新的點集而准備
		for (int i = 0; i < dataSetLen; i++)
			series[i].clear();

		for (int i = 0; i < dataSetLen; i++) {
			for (int k = 0; k < list[i].size(); k++) {
				series[i].add(k, (double) list[i].get(k));
			}
		}

		// 在數據集中添加新的點集
		for (int i = 0; i < dataSetLen; i++)
			mDataset.addSeries(series[i]);

		// update chart
		chart.invalidate();
	}

	public static int PlaySound(final Context context) {
		NotificationManager mgr = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification nt = new Notification();
		nt.defaults = Notification.DEFAULT_SOUND;
		int soundId = new Random(System.currentTimeMillis())
				.nextInt(Integer.MAX_VALUE);
		mgr.notify(soundId, nt);
		return soundId;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			changeActivity(AnnRecallingActivity.this, MenuActivity.class);
		}
		return true;
	}
	private Bitmap takeScreenShot(Activity activity) {
		// View是你需要截圖的View
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();

		// 得到狀態列高度
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		System.out.println(statusBarHeight);

		// 得到螢幕長和高　
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		int height = activity.getWindowManager().getDefaultDisplay()
				.getHeight();
		// 去掉標題列
		// Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);
		Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
				- statusBarHeight);
		view.destroyDrawingCache();
		return b;
	}

	// 儲存到sdcard
	private void savePic(Bitmap b, String strFileName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(strFileName);
			if (null != fos) {
				b.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shoot(Activity a,String mode) {
		String path = "/sdcard/data/Mariacall/AnnRecalling-"+mode+"_"+id+".png";
		savePic(takeScreenShot(a), path);
	}
}