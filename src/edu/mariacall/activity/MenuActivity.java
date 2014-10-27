package edu.mariacall.activity;

import edu.mariacall.R;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuActivity extends ControllerActivity {
	private Button btnSignalDetection;
	private Button btnBpnnLocation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initLayout();
		initListeners();
	}
	
	private void initLayout() {
		setContentView(R.layout.layout_menu);
	
	}
	
	private void initListeners() {
		btnSignalDetection 		= (Button) findViewById(R.id.meu_signal_detection);
		btnBpnnLocation 		= (Button) findViewById(R.id.meu_bpnn_location);
		
	
		btnSignalDetection.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeActivity(MenuActivity.this, SignalDetectionActivity.class);
			}
		});
		
		btnBpnnLocation.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeActivity(MenuActivity.this, BPNNLocationActivity.class);
			}
		});
	}
		
}
