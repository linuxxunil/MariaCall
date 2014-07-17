package edu.mariacall.activity;

import edu.mariacall.R;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class MenuActivity extends ControllerActivity {

	private ImageButton iBtnCall = null;
	private final String[] operating = {"©w¦ì","©I¥sº¿ÄR¨È"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initLayout();
		
		initListeners();
	}
	
	private void initLayout() {
		setContentView(R.layout.activity_menu);
	}
	
	private void initListeners() {
		iBtnCall = (ImageButton) findViewById(R.id.imgBtn_call);

		iBtnCall.setOnClickListener(oclCall);
	}
	
	private ImageButton.OnClickListener oclCall = new ImageButton.OnClickListener() {
		@Override
		public void onClick(View v) {
			changeActivity(MenuActivity.this, Map1FActivity.class);
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
