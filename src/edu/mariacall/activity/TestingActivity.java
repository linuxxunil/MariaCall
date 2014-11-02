package edu.mariacall.activity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.Context;
import android.graphics.Color;
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

public class TestingActivity extends ControllerActivity {
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
	private String nnWeightFile00 = "/sdcard/data/Mariacall/wei/NN00.wei";
	private String nnWeightFile01 = "/sdcard/data/Mariacall/wei/NN01.wei";
	private String nnWeightFile02 = "/sdcard/data/Mariacall/wei/NN02.wei";
	private String nnWeightFile03 = "/sdcard/data/Mariacall/wei/NN03.wei";
	private double[] weight = null;

	/* Chart */
	private int maxTimes = 0;
	private String[] macSet;
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
		setContentView(R.layout.layout_testing);
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
		btnStart = (Button) findViewById(R.id.tst_btnStart);
		eTxtID = (EditText) findViewById(R.id.tst_eTxtID);
		eTxtQuantity = (EditText) findViewById(R.id.tst_eTxtQuantity);
		eTxtDetectTimes = (EditText) findViewById(R.id.tst_eTxtDetectTimes);
		cBoxDB = (CheckBox) findViewById(R.id.tst_cBoxDB);
		cBoxKalman = (CheckBox) findViewById(R.id.tst_cBoxKalman);
		cBoxWinAvg = (CheckBox) findViewById(R.id.tst_cBoxWinAvg);
		cBoxAuto = (CheckBox) findViewById(R.id.tst_cBoxAuto);
		cBoxKalmanWeight = (CheckBox) findViewById(R.id.tst_cBoxKalmanWeight);
		cBoxWinAvgWeight = (CheckBox) findViewById(R.id.tst_cBoxWinAvgWeight);
		tViwAreaID = (TextView) findViewById(R.id.tst_txtAreaID);
		lLayChart = (LinearLayout) findViewById(R.id.tst_lLayChart);

		btnStart.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (flgStart == false) {
					btnStart.setText("����");
					reset();
					db.onConnect();
					locationStart();
				} else {
					btnStart.setText("�Ұ�");
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
		db.createTable(DatabaseTable.Testing.create());
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
		// for (int i = 0; i < dataSetLen; i++)
		// macSet[i] = new String();

		dataSetLists = new LinkedList[dataSetLen];
		for (int i = 0; i < dataSetLen; i++)
			dataSetLists[i] = new LinkedList<Double>();

		series = new XYSeries[dataSetLen];
		for (int i = 0; i < dataSetLen; i++)
			series[i] = new XYSeries(dataSetName[i]);

		// �Ыؤ@�Ӽƾڶ�����ҡA�o�Ӽƾڶ��N�Q�ΨӳЫعϪ�
		mDataset = new XYMultipleSeriesDataset();

		// �N�I���K�[��o�Ӽƾڶ���
		for (int i = 0; i < dataSetLen; i++)
			mDataset.addSeries(series[i]);

		// �H�U���O���u���˦��M�ݩʵ������]�m�Arenderer�۷��@�ӥΨӵ��Ϫ���V���y�`
		renderer = buildRenderer(dataSetColors, dataSetStyles, true);

		// �]�m�n�Ϫ��˦�
		setChartSettings(renderer);

		// �ͦ��Ϫ�
		chart = ChartFactory.getLineChartView(context, mDataset, renderer);

		// �N�Ϫ�K�[�쥬�����h
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

		macSet = new String[dataSetLen];
		macSet[0] = new String("78:A5:04:60:02:26");
		macSet[1] = new String("D0:39:72:D9:FA:65");
		//macSet[2] = new String("D0:39:72:D9:FE:D6");
		macSet[2] = new String("D0:39:72:D9:FA:2A");
	}

	private void setWinAvg() {
		winAvg = new double[dataSetLen][maxWinAvg];
		for (int i = 0; i < kalman.length; i++)
			winAvg[i] = new double[maxWinAvg];
	}

	private void setKalman() {
		kalman = new Kalman[dataSetLen];
		for (int i = 0; i < kalman.length; i++)
			kalman[i] = new Kalman(-59, 0.1, 0.1, 0.01);
	}

	private void setNN() {

		String nnWeightFile;
		if (cBoxWinAvgWeight.isChecked() && cBoxKalmanWeight.isChecked()) {
			nnWeightFile = nnWeightFile03;
			nnet = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 3,
					20, 11);
		} else if (cBoxWinAvgWeight.isChecked()) {
			nnWeightFile = nnWeightFile02;
			nnet = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 3,
					20, 11);
		} else if (cBoxKalmanWeight.isChecked()) {
			nnWeightFile = nnWeightFile01;
			nnet = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 3,
					20, 11);
		} else {
			nnWeightFile = nnWeightFile00;
			nnet = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 3,
					24, 11);

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
		String sql = "INSERT INTO " + DatabaseTable.Testing.name + "(\""
				+ DatabaseTable.Testing.colDeviceID + "\"," + "	\""
				+ DatabaseTable.Testing.colPredict + "\"," + "	\""
				+ DatabaseTable.Testing.colDeviceMAC + "\"," + " \""
				+ DatabaseTable.Testing.colRSSI + "\"," + " \""
				+ DatabaseTable.Testing.colWinAvg + "\"," + " \""
				+ DatabaseTable.Testing.colKalman + "\")" 
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
		} catch (IOException e1) {
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
		int id = Integer.valueOf(eTxtID.getText().toString());
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
			insertTestingInfo(id, predict,macSet, rssiSet,
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
		for (int i = 0; i < macSet.length; i++) {
			if (macSet[i].equals(mac)) {
				match = i;
				break;
			} else if (macSet[i].equals("")) {
				macSet[i] = mac;
				match = i;
				break;
			}
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
					switch (testCase++) {
					case 1:
						for (int i = 0; i < list.length; i++)
							list[i].clear();
						cBoxWinAvg.setChecked(false);
						cBoxKalman.setChecked(true);
						cBoxWinAvgWeight.setChecked(false);
						cBoxKalmanWeight.setChecked(true);
						break;
					case 2:
						for (int i = 0; i < list.length; i++)
							list[i].clear();
						cBoxWinAvg.setChecked(true);
						cBoxKalman.setChecked(false);
						cBoxWinAvgWeight.setChecked(true);
						cBoxKalmanWeight.setChecked(false);
						break;
					case 3:
						for (int i = 0; i < list.length; i++)
							list[i].clear();
						cBoxWinAvg.setChecked(true);
						cBoxKalman.setChecked(true);
						cBoxWinAvgWeight.setChecked(true);
						cBoxKalmanWeight.setChecked(true);
						break;
					case 4:
						cBoxWinAvg.setChecked(false);
						cBoxKalman.setChecked(false);
						cBoxWinAvgWeight.setChecked(false);
						cBoxKalmanWeight.setChecked(false);
						testCase = 0;
						btnStart.callOnClick();
						PlaySound(context);
						Builder alertDialog = new AlertDialog.Builder(
								TestingActivity.this);
						alertDialog.setTitle("����");
						alertDialog.setMessage("���է���");
						alertDialog.setPositiveButton("�T�w", null);
						alertDialog.show();
						break;
					}
					setNN();
				} else {
					btnStart.callOnClick();
					PlaySound(context);
					Builder alertDialog = new AlertDialog.Builder(
							TestingActivity.this);
					alertDialog.setTitle("����");
					alertDialog.setMessage("���է���");
					alertDialog.setPositiveButton("�T�w", null);
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
			renderer.addSeriesRenderer(r); // �N�y���ܦ��u�[�J�Ϥ����
		}
		return renderer;
	}

	private void setChartSettings(XYMultipleSeriesRenderer renderer) {
		// ������Ϫ���V�i�Ѭ�api����

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
		// �����ƾڶ����ª��I��
		for (int i = 0; i < dataSetLen; i++)
			mDataset.removeSeries(series[i]);

		// �I�����M�šA���F�����s���I���ӭ��
		for (int i = 0; i < dataSetLen; i++)
			series[i].clear();

		for (int i = 0; i < dataSetLen; i++) {
			for (int k = 0; k < list[i].size(); k++) {
				series[i].add(k, (double) list[i].get(k));
			}
		}

		// �b�ƾڶ����K�[�s���I��
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
			changeActivity(TestingActivity.this, MenuActivity.class);
		}
		return true;
	}
}