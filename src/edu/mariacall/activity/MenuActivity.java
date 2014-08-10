package edu.mariacall.activity;

import edu.mariacall.R;
import edu.mariacall.database.DatabaseDriver;
import edu.mariacall.database.DatabaseTable;
import edu.mariacall.database.SqliteDriver;
import edu.mariacall.location.Beacon;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MenuActivity extends ControllerActivity {

	private ImageButton iBtnCall = null;
	private Button		btnDeleteDB = null;
	private TextView	tViwRSSI = null;
	private final int SHOW_TAG = 1;
	private DatabaseDriver db = null;
	private String[] operating = {"AAA","BBB"};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initLayout();
		
		initListeners();
		
		initHandler();
		
		initDatabase();
	}
	
	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SHOW_TAG:
					locationStartResult(msg);
					break;
				}
			}
		};
	}
	
	private void initLayout() {
		setContentView(R.layout.activity_menu);
	}
	
	private void initListeners() {
		iBtnCall = (ImageButton) findViewById(R.id.imgBtn_call);
		tViwRSSI = (TextView) findViewById(R.id.tViw_rssi);
		btnDeleteDB = (Button) findViewById(R.id.btn_delete_db);
		
		iBtnCall.setOnClickListener(oclCall);
		btnDeleteDB.setOnClickListener(oclDeleteDB);
	}
	
	private void initDatabase() {
		db = new SqliteDriver(dbPath);
		db.onConnect();
		db.createTable(DatabaseTable.Ibeacon.create());
	}

	private void insertBeaconInfo(String device, int rssi) {
		String sql = String.format(
				"INSERT INTO %s (\"%s\",\"%s\") VALUES (\"%s\",\"%d\")",
							DatabaseTable.Ibeacon.name,
							DatabaseTable.Ibeacon.colDevice, 
							DatabaseTable.Ibeacon.colRSSI,
							device, rssi);
		db.insert(sql);
		System.out.println(sql);
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
	
	private Beacon beacon; 
	private void locationStart() {
		beacon = new Beacon(MenuActivity.this);
		beacon.startLeScan(new LeScanCallback(){
			@Override
			public void onLeScan(BluetoothDevice arg0, int rssi, byte[] scanRecord) {
				sendMessage(SHOW_TAG, rssi, arg0.toString());
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
		insertBeaconInfo(mac,rssi);
		tViwRSSI.setText(mac + " " + String.valueOf(rssi));
	}
	static private boolean enable = true;
	private ImageButton.OnClickListener oclCall = new ImageButton.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if ( enable ) {
				locationStart();
				enable = false;
			} else {
				locationStop();
				enable = true;
			}
			
			//changeActivity(MenuActivity.this, Map1FActivity.class);
		}

	};
	
	private ImageButton.OnClickListener oclDeleteDB = new ImageButton.OnClickListener() {
		@Override
		public void onClick(View v) {
			String sql = String.format("DELETE FROM %s", DatabaseTable.Ibeacon.name);
			db.delete(sql);
		}

	};
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		for (int i=0; i<operating.length; i++) {
			menu.add(operating[i]);
		}
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
