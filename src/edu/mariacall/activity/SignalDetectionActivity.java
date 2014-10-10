package edu.mariacall.activity;

import java.util.LinkedList;

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
import edu.mariacall.location.Beacon;
import android.app.ActionBar.LayoutParams;
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
import android.widget.EditText;
import android.widget.LinearLayout;

public class SignalDetectionActivity extends ControllerActivity {

	private Button	btnStart = null;
	private EditText eTxtDbName = null;
	private EditText eTxtDetectTimes = null;
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
    private LinkedList<Integer> yList = new LinkedList<Integer>();
   
    private int xCount = 0;  
    private int xCountMax = -1;	
    
    /* bluetooth */
    private Beacon beacon;
    
    /* Initial */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initLayout();
		
		initListeners();
		
		initHandler();
		
		initDatabase();
		
		locationStart();
	}
	
	private void initLayout() {
		setContentView(R.layout.layout_signal_detection);

		context = getApplicationContext();  
		
		//�o����omain�ɭ��W�������A�U���|��Ϫ�e�b�o�ӥ����ح�  
        LinearLayout layout = (LinearLayout)findViewById(R.id.linearLayout3);  
        
        //�o�����Ψө�m���u�W���Ҧ��I�A�O�@���I�����X�A�ھڳo���I�e�X���u  
        series = new XYSeries(title);  
          
        //�Ыؤ@�Ӽƾڶ�����ҡA�o�Ӽƾڶ��N�Q�ΨӳЫعϪ�  
        mDataset = new XYMultipleSeriesDataset();  
          
        //�N�I���K�[��o�Ӽƾڶ���  
        mDataset.addSeries(series);  
          
        //�H�U���O���u���˦��M�ݩʵ������]�m�Arenderer�۷��@�ӥΨӵ��Ϫ���V���y�`  
        int color = Color.GREEN;  
        PointStyle style = PointStyle.CIRCLE;  
        renderer = buildRenderer(color, style, true);  
          
        //�]�m�n�Ϫ��˦�
        setChartSettings(renderer);  
          
        //�ͦ��Ϫ�  
        chart = ChartFactory.getLineChartView(context, mDataset, renderer);  
          
        //�N�Ϫ�K�[�쥬�����h   
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
		btnStart = (Button) findViewById(R.id.sig_btnStart);
		eTxtDbName = (EditText) findViewById(R.id.sig_eTxtDbName);
		eTxtDetectTimes = (EditText) findViewById(R.id.sig_eTxtDetectTimes);
		
		btnStart.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( flgStart == false ) {
					// set detect times
					xCountMax = Integer.valueOf(
								eTxtDetectTimes.getText().toString());
					btnStart.setText("����");
					// location start
					locationStart();

				} else {
					// reset detect times
					xCountMax = -1;
					
					btnStart.setText("�Ұ�");
					// location start
					locationStop();
					
					
				}
				flgStart ^= true;
			}
		});
	}
	
	private void initDatabase() {
		//db = new SqliteDriver(dbPath);
		//db.onConnect();
		//db.createTable(DatabaseTable.Ibeacon.create());
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
	
	private void insertBeaconInfo(String device, int rssi, double distance) {
		String sql = "INSERT INTO " + DatabaseTable.Ibeacon.name 
				+ "(\"" +  DatabaseTable.Ibeacon.colDevice 		+ "\","
				+ " \"" +  DatabaseTable.Ibeacon.colRSSI		+ "\","
				+ " \"" +  DatabaseTable.Ibeacon.colDistance 	+ "\")"
				+ " VALUES "
				+ "(\"" +  device 		+ "\","
				+ " \"" +  rssi 		+ "\","
				+ " \"" +  distance 	+ "\")";
		db.insert(sql);
	}
	
	/* Location */
	private void locationStart() {
		beacon = new Beacon(context);
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
				//sendMessage(SHOW_TAG, rssi, arg0.toString());

			}
		}); 
	}

	private void locationStartResult(Message msg) {
		int rssi = msg.getData().getInt("rssi");
		String mac = msg.getData().getString("mac");
		updateChart(rssi);
		
		/*		
		kalman.correct(rssi);
		kalman.predict();
		kalman.update();
		
		//float distance = 10
		System.out.println(Beacon.convertToDistance(kalman.getStateEstimation()));
		
		//insertBeaconInfo(mac,rssi,Beacon.convertToDistance(rssi));
		tViwRSSI.setText(mac + " "+  Beacon.convertToDistance(kalman.getStateEstimation()) + " " + " " + i++);
		*/
	}
	
	/* Chart */
	private XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) {  
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();  
          
        //�]�m�Ϫ����u�������˦��A�]�A�C��B�I���j�p�H�νu���ʲӵ�  
        XYSeriesRenderer r = new XYSeriesRenderer();  
        r.setColor(color);  
        r.setPointStyle(style);  
        r.setFillPoints(fill);  
        r.setLineWidth(3);  
        renderer.addSeriesRenderer(r);  
          
        return renderer;  
    }  
      
    private void setChartSettings(XYMultipleSeriesRenderer renderer) {  
        //������Ϫ���V�i�Ѭ�api����  
    	
        renderer.setChartTitle(title);
        renderer.setXAxisMin(0);  
        renderer.setXAxisMax(100);  
        renderer.setYAxisMin(0);  
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
        renderer.setPanEnabled(false);
        renderer.setZoomEnabled(false);
    }  
      
    private void updateChart(int rssi) {  
             
        //�����ƾڶ����ª��I��  
        mDataset.removeSeries(series);  
        
        yList.add(rssi);
         
        //�I�����M�šA���F�����s���I���ӭ��  
        series.clear();  
        
        for (int k = 0; k < xCount; k++) {  
            series.add(k, yList.get(k));  
        }  
       
        //�b�ƾڶ����K�[�s���I��  
        mDataset.addSeries(series);  
         
        // update chart
        chart.invalidate();  
        
        xCount++;
    }  
	
}
