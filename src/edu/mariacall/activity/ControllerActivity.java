package edu.mariacall.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class ControllerActivity extends Activity {
	protected Handler handler = null;
	final static protected String dbPath = "/sdcard/data/mariacall/location.sqlite";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	private void disableWindowTitle() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	
	protected void sendMessage(int what, int statusCode ) {
		if ( handler != null ) {
			Message msg = handler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putInt("status", statusCode);
			msg.what = what;
			msg.setData(bundle);
			handler.sendMessage(msg);
		}
	}
	
	
	protected void changeActivity(Context  from, Class to) {
		Intent intent = new Intent();
		intent.setClass(from, to);
		startActivity(intent);
		finish();
	}

}
