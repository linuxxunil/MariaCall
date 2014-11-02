package edu.mariacall.activity;

import edu.mariacall.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;


public class HomeActivity extends ControllerActivity {

	private boolean sleep = false;

	// TAG
	private final int INIT_TAG = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initHandler();
		
		initValueToView();
	}

	
	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case INIT_TAG:
					initValueToViewResult(msg);
					break;
				}
			}
		};
	}
	
	private void initValueToView() {
		setContentView(R.layout.layout_main);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}
	
	private void initValueToViewResult(Message msg) {
		changeActivity(HomeActivity.this, CollectionActivity.class);
	}
	
}
