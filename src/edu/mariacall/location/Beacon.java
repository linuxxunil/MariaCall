package edu.mariacall.location;

import edu.mariacall.algorithm.Kalman;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

public class Beacon {
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothManager bluetoothManager = null;
	private Context context = null;
	static private int benchmark = -64; 
	static private double n = 2.92;
			
	static public void setBenchmark(int dBmAtOneMeter) {
		benchmark = dBmAtOneMeter;
	}
	
	static public double convertToDistance(double rssi) {
		

		
		double tmp = -(rssi - benchmark) / (10 * n);
		
		return Math.pow(10, tmp);
		
	}


	public Beacon(Context context) {
		this.context = context;
		
		bluetoothManager =
		        (BluetoothManager) context.getSystemService(
		        					context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
	}
	
	public void startLeScan() {
		startLeScan(leStartCallBack);
	}
	
	public void stopLeScan() {
		stopLeScan(leStopCallBack);
	}
	
	public void startLeScan(LeScanCallback leStartCallBack) {
		try {
			mBluetoothAdapter.enable();
			Thread.sleep(3000);
			if ( mBluetoothAdapter.isEnabled())
				mBluetoothAdapter.startLeScan(leStartCallBack);
		} catch (InterruptedException e) {
		}
	}
	
	public void stopLeScan(LeScanCallback leStartCallBack) {
		
		if ( mBluetoothAdapter.isEnabled() ) 	
			mBluetoothAdapter.stopLeScan(leStopCallBack);
		mBluetoothAdapter.disable();
	}
	
	private LeScanCallback leStartCallBack = new LeScanCallback(){
		@Override
		public void onLeScan(BluetoothDevice arg0, int rssi, byte[] scanRecord) {
			
		}
	};
	
	private LeScanCallback leStopCallBack = new LeScanCallback(){
		@Override
		public void onLeScan(BluetoothDevice arg0, int rssi, byte[] scanRecord) {
			System.out.println(rssi);
		}
	};
	
	

}
