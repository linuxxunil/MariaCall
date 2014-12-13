package edu.mariacall.activity;

import edu.mariacall.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;




public class Map1FActivity extends ControllerActivity {
	//private Map1FView view;
	private Marker view1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		initLayout();

		initListeners();
		
	}

	private void initLayout() {
		setContentView(R.layout.layout_map_1f);
		
		view1 = new Marker(this);
		FrameLayout frameLayout = (FrameLayout)findViewById(R.id.view1FrameLayout);
		frameLayout.addView(view1);
	
	}


	private void initListeners() {
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
		} else if (id == R.id.action_call) {
			return true;
		} else if (id == R.id.action_location) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}


