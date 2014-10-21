package edu.mariacall.activity;

import java.util.LinkedList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.AlertDialog;
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

public class BPNNLocationActivity extends ControllerActivity {
	/* UI */
	private Button	btnStart = null;
	private EditText eTxtDetectTimes = null;
	private EditText eTxtID = null;
	private TextView tViwCurrentRSSI = null;
	private TextView tViwCurrentMAC = null;
	private CheckBox cBoxDB = null;
	private CheckBox cBoxWinAvg = null;
	private CheckBox cBoxKalman = null;
	private DatabaseDriver db = null;
	private boolean flgStart = false;
	
    /* Definition  Handler tag */
    private final static int TAG_LOCATION_START = 1;
    
    /* Using draw chart */
    private String title = "Signal Strength";  
    private XYSeries series;  
    private XYMultipleSeriesDataset mDataset;  
    private GraphicalView chart;  
    private XYMultipleSeriesRenderer renderer;  
    private Context context; 
    private LinkedList<Double> yList = new LinkedList<Double>();
   
    private int nowTimes = 0;  
    private int maxTimes = -1;	
    
    /* bluetooth */
    private Beacon beacon = null;
    
    /* algorithm */
    private final int maxWinAvg = 10; 
    private double[] winAvg ;
    private Kalman kalman = null;
    
    /* Initial */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initLayout();
		
		initListeners();
		
		initHandler();
		
		initDatabase();
		
		initAlgorithm();
		
		if ( beacon == null )
			beacon = new Beacon(context);
	}
	
	private void initAlgorithm() {
		// initial windows average
		winAvg = new double[maxWinAvg];
		
		// initial kalman
		kalman = new Kalman(-59, 0.1, 0.1, 0.1);
	}
	private void initLayout() {
		setContentView(R.layout.layout_signal_detection);

		context = getApplicationContext();  
		
		//這裏獲得main界面上的布局，下面會把圖表畫在這個布局裏面  
        LinearLayout layout = (LinearLayout)findViewById(R.id.sig_lLayChart);  
        
        //這個類用來放置曲線上的所有點，是一個點的集合，根據這些點畫出曲線  
        series = new XYSeries(title);  
          
        //創建一個數據集的實例，這個數據集將被用來創建圖表  
        mDataset = new XYMultipleSeriesDataset();  
          
        //將點集添加到這個數據集中  
        mDataset.addSeries(series);  
          
        //以下都是曲線的樣式和屬性等等的設置，renderer相當於一個用來給圖表做渲染的句柄  
        int color = Color.GREEN;  
        PointStyle style = PointStyle.CIRCLE;  
        renderer = buildRenderer(color, style, true);  
          
        //設置好圖表的樣式
        setChartSettings(renderer);  
          
        //生成圖表  
        chart = ChartFactory.getLineChartView(context, mDataset, renderer);  
          
        //將圖表添加到布局中去   
        layout.addView(chart, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
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
		eTxtDetectTimes = (EditText) findViewById(R.id.sig_eTxtDetectTimes);
		eTxtID			= (EditText) findViewById(R.id.sig_eTxtID);
		tViwCurrentRSSI = (TextView) findViewById(R.id.sig_tViwCurrentRSSI);
		tViwCurrentMAC 	= (TextView) findViewById(R.id.sig_tViwCurrentMAC);
		cBoxDB			= (CheckBox) findViewById(R.id.sig_cBoxDB);
		cBoxKalman		= (CheckBox) findViewById(R.id.sig_cBoxKalman);
		cBoxWinAvg		= (CheckBox) findViewById(R.id.sig_cBoxWinAvg);
		
		btnStart.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( flgStart == false ) {
					// set detect times
					maxTimes = Integer.valueOf(
								eTxtDetectTimes.getText().toString());
					
					btnStart.setText("關閉");
					// open db
					db.onConnect();
					// location start
					locationStart();
				} else {
					// reset detect times
					maxTimes = -1;
					nowTimes = 0;  

					btnStart.setText("啟動");
					// location start
					locationStop();
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

	private void locationStartResult(Message msg) {
		double rssi	= (double)msg.getData().getInt("rssi");
		String mac 	= msg.getData().getString("mac");
		int id 		= Integer.valueOf(eTxtID.getText().toString());
		
		if ( cBoxWinAvg.isChecked() )
			rssi = exeWinAvg(rssi);
		if ( cBoxKalman.isChecked() )
			rssi = exeKalman(rssi);
		
		if ( cBoxDB.isChecked() ) {
			insertBeaconInfo(id, mac, rssi, 0.0,
					cBoxWinAvg.isChecked(),cBoxKalman.isChecked() );
		}
		
		// update activity
		tViwCurrentMAC.setText("MAC = " + mac);
		tViwCurrentRSSI.setText("RSSI = " + rssi);
		updateChart(rssi);
		
		if ( maxTimes == 0 )
			return ;
		else if ( nowTimes >= maxTimes ) {
			btnStart.callOnClick();
			Builder alertDialog = new AlertDialog.Builder(
					BPNNLocationActivity.this);
			alertDialog.setTitle("提示");
			alertDialog.setMessage("測試完成");
			alertDialog.setPositiveButton("確定",null);
			alertDialog.show();
		}
	}
	
	private double exeKalman(double rssi) {
		kalman.correct(rssi);
		kalman.predict();
		kalman.update();
		return kalman.getStateEstimation();
	}

	private double exeWinAvg(double rssi) {
		double sum = 0;
		
		if ( winAvg[maxWinAvg-1] == 0 ) {
			for (int i=0; i<maxWinAvg; i++)
				winAvg[i] = rssi;
		}
		
		winAvg[nowTimes%maxWinAvg] = rssi;
	
		for(int i=0; i<maxWinAvg; i++) {
			sum += winAvg[i];
		}
		return (sum / (double)maxWinAvg);
	}

	
	/* Chart */
	private XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) {  
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();  
          
        //設置圖表中曲線本身的樣式，包括顏色、點的大小以及線的粗細等  
        XYSeriesRenderer r = new XYSeriesRenderer();  
        r.setColor(color);  
        r.setPointStyle(style);  
        r.setFillPoints(fill);  
        r.setLineWidth(3);  
        renderer.addSeriesRenderer(r);  
          
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
        renderer.setGridColor(Color.BLUE);  
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
      
    private void updateChart(double rssi) {  
             
        //移除數據集中舊的點集  
        mDataset.removeSeries(series);  
        
        yList.add(rssi);
         
        //點集先清空，為了做成新的點集而准備  
        series.clear();  
        
        for (int k = 0; k < nowTimes; k++) {  
            series.add(k, yList.get(k));  
        }  
       
        //在數據集中添加新的點集  
        mDataset.addSeries(series);  
         
        // update chart
        chart.invalidate();  
        
        nowTimes++;
    }  	
}
