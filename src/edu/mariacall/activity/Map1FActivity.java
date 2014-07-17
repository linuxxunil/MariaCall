package edu.mariacall.activity;

import edu.mariacall.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;



class Map1FView extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder hold;
	private Bitmap backgroud, marker;
	private Canvas canvas;
	
	public Map1FView(Context context) {
		super(context);
		hold = getHolder();
		hold.addCallback(this);
		
		backgroud = BitmapFactory.decodeResource(getResources(), R.drawable.map_1f);
		marker = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
	}
	

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		canvas = holder.lockCanvas();
		canvas.drawBitmap(backgroud, null, new Rect(0, 0, getWidth(), getHeight()), null); 
		holder.unlockCanvasAndPost(canvas);
	}
	
	public void addMarker(int x, int y) {
		 
		canvas = getHolder().lockCanvas();
		//canvas.drawBitmap(backgroud, null, new Rect(0, 0, getWidth(), getHeight()), null); 
		canvas.drawBitmap(marker, x, y, null);
		hold.unlockCanvasAndPost(canvas);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		System.out.println("surfaceChanged");
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		System.out.println("surfaceDestroyed");
	}
}

public class Map1FActivity extends ControllerActivity {
	private Map1FView view;
	private Marker view1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		initLayout();

		initListeners();
		
	}

	private void initLayout() {
		//setContentView(R.layout.activity_map_1f);
		//view = new Map1FView(this);
		view1 = new Marker(this);
		setContentView(view1);
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

	private final int TAG_INIT = 1;
	private final int TAG_ADD_MARKER = 2;
	private int mode = TAG_INIT;

	private Bitmap backgroud, marker;
	private int x,y;
	
	public Marker(Context context) {
		super(context);
		backgroud = BitmapFactory.decodeResource(getResources(), R.drawable.map_1f);
		marker = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
		mode = TAG_INIT;
	}
	
	@Override
	protected void onDraw (Canvas canvas) {
		super.onDraw(canvas);
		switch (mode) {
		case TAG_INIT:
			canvas.drawBitmap(backgroud, null, new Rect(0, 0, getWidth(), getHeight()), null); 
			
			break;
		case TAG_ADD_MARKER:
			canvas.drawBitmap(marker, x, y, null);
			break;
		}
	}
	
	public void addMarker(int x, int y) {
		this.x = x;
		this.y = y;
		mode = ADD_MARKER_TAG;
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


