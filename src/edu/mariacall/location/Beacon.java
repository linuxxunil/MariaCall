package edu.mariacall.location;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Beacon {
	private BluetoothAdapter mBluetoothAdapter;
	final private BluetoothManager bluetoothManager;


	public Beacon(Context context) {
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
		if ( mBluetoothAdapter.isEnabled() )
			mBluetoothAdapter.startLeScan(leStartCallBack);
	}
	
	public void stopLeScan(LeScanCallback leStartCallBack) {
		if ( mBluetoothAdapter.isEnabled() )
			mBluetoothAdapter.stopLeScan(leStopCallBack);
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
