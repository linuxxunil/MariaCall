package edu.mariacall.activity;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import edu.mariacall.R;
import edu.mariacall.algorithm.Kalman;
import edu.mariacall.database.DatabaseDriver;
import edu.mariacall.database.DatabaseTable;
import edu.mariacall.database.SqliteDriver;
import edu.mariacall.location.Beacon;
import android.app.AlertDialog;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
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

public class SignalDetectionActivity extends ControllerActivity {
	/* UI */
	private Button	btnStart = null;
	private EditText eTxtQuantity = null;
	private EditText eTxtDetectTimes = null;
	private EditText eTxtID = null;
	private TextView tViwCurrentRSSI = null;
	private TextView tViwCurrentMAC = null;
	private CheckBox cBoxDB = null;
	private CheckBox cBoxWinAvg = null;
	private CheckBox cBoxKalman = null;
	private DatabaseDriver db = null;
	LinearLayout lLayChart = null;  
	private boolean flgStart = false;
	
    /* Definition  Handler tag */
    private final static int TAG_LOCATION_START = 1;
   
    /* Using draw chart */
    // dataSet definition
    private int dataSetLen = -1;
    private int maxTimes = 0;	
    private String[] dataSetName = { "Beacon1", "Beacon2", "Beacon3","Beacon4"};
    private int[] dataSetColors = { Color.BLUE, Color.GREEN, Color.rgb(0xFF, 0xA5, 0x00), Color.RED};
    private PointStyle[] dataSetStyles = { PointStyle.CIRCLE, PointStyle.CIRCLE,
    											PointStyle.CIRCLE,PointStyle.CIRCLE };
    private LinkedList[] dataSetLists;
    private String[] macSet;
    
    // background definition
    private String title = "Signal Strength";  
    private XYSeries[] series;  
    private XYMultipleSeriesDataset mDataset;  
    private GraphicalView chart;  
    private XYMultipleSeriesRenderer renderer;  
    private Context context; 
   
   
    
    
    /* bluetooth */
    private Beacon beacon = null;
    
    /* algorithm */
    private final int maxWinAvg = 10; 
    private double[][] winAvg ;
    private Kalman[] kalman = null;
    
    /* Initial */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();  
		
		initLayout();
		
		initListeners();
		
		initChart();
		
		initHandler();
		
		initDatabase();
		
		initAlgorithm();
		
		if ( beacon == null )
			beacon = new Beacon(context);
	}
	
	private void initAlgorithm() {
		// initial windows average
		
		
		// initial kalman
		//kalman = new Kalman(-59, 0.1, 0.1, 0.01);
	}
	private void initLayout() {
		setContentView(R.layout.layout_signal_detection);
	}
	
	private void reset() {
		int tmp;
		maxTimes = Integer.valueOf(
						eTxtDetectTimes.getText().toString());
		
        tmp = Integer.valueOf(eTxtQuantity.getText().toString());
        /* reset chart */
        if ( dataSetLen != tmp ) {
        	dataSetLen = tmp;
        	System.out.println(dataSetLen);
        	setChart();
        	setKalman();
        	setWinAvg();
        } else {
        	for (int i=0; i<dataSetLists.length; i++) {
        			dataSetLists[i].clear();
        	}
        }
	}
	
	private void setWinAvg() {
		winAvg = new double[dataSetLen][maxWinAvg];
		for ( int i=0; i<kalman.length; i++)
			winAvg[i] =  new double[maxWinAvg];
	}
	
	private void setKalman() {
		kalman = new Kalman[dataSetLen];
		for ( int i=0; i<kalman.length; i++) 
			kalman[i] =  new Kalman(-59, 0.1, 0.1, 0.01);
	}
	
	private void initChart() {
		maxTimes = Integer.valueOf(
				eTxtDetectTimes.getText().toString());
        dataSetLen = Integer.valueOf(eTxtQuantity.getText().toString());
        setChart();
        setKalman();
        setWinAvg();
	}
	
	private void setChart() {
        macSet = new String[dataSetLen];
        for (int i=0; i<dataSetLen; i++) 
        	macSet[i] = new String();
        
        dataSetLists = new LinkedList[dataSetLen];
        for (int i=0; i<dataSetLen; i++) 
        	dataSetLists[i] = new LinkedList<Double>();
        
        series = new XYSeries[dataSetLen];
        for (int i=0; i<dataSetLen; i++)
        	series[i] = new XYSeries(dataSetName[i]);
        
        
        //創建一個數據集的實例，這個數據集將被用來創建圖表  
        mDataset = new XYMultipleSeriesDataset();

        //將點集添加到這個數據集中  
        for ( int i=0; i<dataSetLen; i++)
        	mDataset.addSeries(series[i]);
        
        //以下都是曲線的樣式和屬性等等的設置，renderer相當於一個用來給圖表做渲染的句柄
        renderer = buildRenderer(dataSetColors, dataSetStyles, true);
              
        //設置好圖表的樣式
        setChartSettings(renderer);  
          
        //生成圖表  
        chart = ChartFactory.getLineChartView(context, mDataset, renderer);  
          
        //將圖表添加到布局中去   
        lLayChart.removeAllViews();
        lLayChart.addView(chart, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
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
		btnStart 		= (Button) findViewById(R.id.sig_btnStart);
		eTxtQuantity	= (EditText) findViewById(R.id.sig_eTxtQuantity);
		eTxtDetectTimes = (EditText) findViewById(R.id.sig_eTxtDetectTimes);
		eTxtID			= (EditText) findViewById(R.id.sig_eTxtID);
		tViwCurrentRSSI = (TextView) findViewById(R.id.sig_tViwCurrentRSSI);
		tViwCurrentMAC 	= (TextView) findViewById(R.id.sig_tViwCurrentMAC);
		cBoxDB			= (CheckBox) findViewById(R.id.sig_cBoxDB);
		cBoxKalman		= (CheckBox) findViewById(R.id.sig_cBoxKalman);
		cBoxWinAvg		= (CheckBox) findViewById(R.id.sig_cBoxWinAvg);
		lLayChart		= (LinearLayout)findViewById(R.id.sig_lLayChart);
		
		btnStart.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( flgStart == false ) {
					// set detect times
					btnStart.setText("關閉");
					
					reset();
					// open db
					db.onConnect();
					// location start
					locationStart();
				} else {
					// reset detect times
					btnStart.setText("啟動");
					// location start
					locationStop();
					
					maxTimes = 0;
					// close db
					db.close();
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
	
	private void sendMessage(int what, int statusCode, String mac ) {
		if ( handler != null ) {
			Message msg = handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putInt("rssi", statusCode);
			bundle.putString("mac", mac);
			msg.what = what;
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}
	
	private void insertBeaconInfo(int deviceID, String deviceMAC, double rssi, double distance,
				boolean winAvg, boolean kalman) {
		String sql = "INSERT INTO " + DatabaseTable.Ibeacon.name
				+ "(\"" + DatabaseTable.Ibeacon.colDeviceID		+ "\","
				+ "	\"" + DatabaseTable.Ibeacon.colDeviceMAC	+ "\","
				+ " \"" + DatabaseTable.Ibeacon.colRSSI			+ "\","
				+ " \"" + DatabaseTable.Ibeacon.colDistance 	+ "\","
				+ " \"" + DatabaseTable.Ibeacon.colWinAvg 		+ "\","
				+ " \"" + DatabaseTable.Ibeacon.colKalman	 	+ "\")"
				+ " VALUES "
				+ "(\"" + deviceID		+ "\","
				+ "	\"" + deviceMAC		+ "\","
				+ " \"" + rssi 			+ "\","
				+ " \"" + distance 		+ "\","
				+ " \"" + (winAvg?1:0)	+ "\","
				+ " \"" + (kalman?1:0) 	+ "\")";
		db.insert(sql);
	}
	
	/* Location */
	private void locationStart() {
		
		beacon.startLeScan(new LeScanCallback(){
			@Override
			public void onLeScan(BluetoothDevice arg0, int rssi, byte[] scanRecord) {
				sendMessage(TAG_LOCATION_START, rssi, arg0.toString());
			}
		}); 
	}
	
	private void locationStop() {
		
		beacon.stopLeScan(new LeScanCallback(){
			@Override
			public void onLeScan(BluetoothDevice arg0, int rssi, byte[] scanRecord) {
			}
		}); 
	}
	
	private int matchMacSet(String mac) {
		int match = -1;
		for (int i=0; i<dataSetLen; i++) {
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
	
	int testCase=0;
	private void checkFinish(LinkedList[] list) {
		boolean finish = true;
		
		if ( maxTimes == 0 ) {
			return ;
		} else {
			for(int i=0; i<list.length; i++) {
				if ( list[i].size() < maxTimes ) {
					finish = false;
					break;
				} 
			}
			
			if ( finish ) {
				switch(testCase++) {
				case 1:
					for(int i=0; i<list.length; i++) list[i].clear();
					cBoxWinAvg.setChecked(false);
					cBoxKalman.setChecked(true);
				break;
				case 2:
					for(int i=0; i<list.length; i++) list[i].clear();
					cBoxWinAvg.setChecked(true);
					cBoxKalman.setChecked(false);
				break;
				case 3:
					for(int i=0; i<list.length; i++) list[i].clear();
					cBoxWinAvg.setChecked(true);
					cBoxKalman.setChecked(true);
				break;
				case 4:
					cBoxWinAvg.setChecked(false);
					cBoxKalman.setChecked(false);
					testCase = 0;
					btnStart.callOnClick();
					Builder alertDialog = new AlertDialog.Builder(
							SignalDetectionActivity.this);
					alertDialog.setTitle("提示");
					alertDialog.setMessage("測試完成");
					alertDialog.setPositiveButton("確定",null);
					alertDialog.show();
				break;
				}
			}
		}
	}
	
	private void locationStartResult(Message msg) {
		double rssi	= (double)msg.getData().getInt("rssi");
		String mac 	= msg.getData().getString("mac");
		int id 		= Integer.valueOf(eTxtID.getText().toString());

		int match = matchMacSet(mac);
		
    	if ( match == -1) {
    		return ;
    	} else {
			if ( cBoxWinAvg.isChecked() )
				rssi = exeWinAvg(match,rssi,dataSetLists[match].size());
			if ( cBoxKalman.isChecked() )
				rssi = exeKalman(match,rssi);
			
			
			// add rssi to list
			if ( dataSetLists[match].size() < maxTimes) {
				dataSetLists[match].add(rssi);
				// inset to database
				if ( cBoxDB.isChecked() ) {
					insertBeaconInfo(id, mac, rssi, 0.0,
							cBoxWinAvg.isChecked(),cBoxKalman.isChecked() );
				}
			}
		
			// update activity
			tViwCurrentMAC.setText("MAC = " + mac);
			tViwCurrentRSSI.setText("RSSI = " + rssi);
			
			
			
			// update chart
			updateChart(dataSetLists);
			
			// check finish
			checkFinish(dataSetLists);
		}
	}
	
	private double exeKalman(int p,double rssi) {
		kalman[p].correct(rssi);
		kalman[p].predict();
		kalman[p].update();
		return kalman[p].getStateEstimation();
	}

	private double exeWinAvg(int p,double rssi,int exeCount) {
		double sum = 0;
		
		if ( winAvg[p][maxWinAvg-1] == 0 ) {
			for (int i=0; i<maxWinAvg; i++)
				winAvg[p][i] = rssi;
		}
		
		winAvg[p][exeCount%maxWinAvg] = rssi;
	
		for(int i=0; i<maxWinAvg; i++) {
			sum += winAvg[p][i];
		}
		return (sum / (double)maxWinAvg);
	}

	/* Chart */
    private XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles, boolean fill) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        
        for (int i = 0; i < dataSetLen; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors[i]);
            r.setPointStyle(styles[i]);
            r.setFillPoints(fill);
            renderer.addSeriesRenderer(r); //將座標變成線加入圖中顯示
        }
        return renderer;
    }

    private void setChartSettings(XYMultipleSeriesRenderer renderer) {  
        //有關對圖表的渲染可參看api文檔  
    	
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
        //renderer.setPanEnabled(false);
        //renderer.setZoomEnabled(false);
    }  
    
    
    private void updateChart(LinkedList[] list) {
        //移除數據集中舊的點集  
    	for (int i=0; i<dataSetLen; i++)
    		mDataset.removeSeries(series[i]);  
    	
        //點集先清空，為了做成新的點集而准備  
        for (int i=0; i<dataSetLen; i++)
        	series[i].clear();
        
        for (int i=0; i<dataSetLen; i++) {
        	for (int k = 0; k < list[i].size(); k++) {  
        		series[i].add(k, (double)list[i].get(k));
        	}
        }
       
        //在數據集中添加新的點集  
        for (int i=0; i<dataSetLen; i++)
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
			changeActivity(SignalDetectionActivity.this, MenuActivity.class);
		}
		return true;
	}
}
