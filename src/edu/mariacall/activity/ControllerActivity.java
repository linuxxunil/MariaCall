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
	final protected String dbPath = "/sdcard/data/mariacall/location.sqlite";
	final protected double kX = -59;
	final protected double kP = 0.1;
	final protected double kQ = 0.1;
	final protected double kR = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	private void disableWindowTitle() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	protected void userMacSet(String[] macSet) {
		macSet[0] = new String("78:A5:04:60:02:26");
		macSet[1] = new String("D0:39:72:D9:FA:65");
		macSet[2] = new String("D0:39:72:D9:FE:9F");
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
