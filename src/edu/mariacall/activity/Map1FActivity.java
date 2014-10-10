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




class Marker extends View {

	private final int TAG_ADD_MARKER = 1;
	private int mode = TAG_ADD_MARKER;

	private Bitmap backgroud, marker;
	private int x,y;
	
	public Marker(Context context) {
		super(context);
		marker = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
	}
	
	@Override
	protected void onDraw (Canvas canvas) {
		super.onDraw(canvas);
	
		switch (mode) {
		case TAG_ADD_MARKER:
			canvas.drawBitmap(marker, x, y, null);
			break;
		}
	}
	
	public void addMarker(int x, int y) {
		this.x = x;
		this.y = y;
		mode = TAG_ADD_MARKER;
	}
	
	int a = 0;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		addMarker(x, y);
			
		invalidate();
		return false; 
	
	}
}


