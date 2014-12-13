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
	private Button btnCollection;
	private Button btnNNTraing;
	private Button btnNNTesting;
	private Button btnSVMTesting;
	private Button btnSVMMap;
	
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
		btnCollection 		= (Button) findViewById(R.id.meu_collection);
		btnNNTraing 		= (Button) findViewById(R.id.meu_nn_training);
		btnNNTesting 		= (Button) findViewById(R.id.meu_nn_testing);
		btnSVMTesting 		= (Button) findViewById(R.id.meu_svm_testing);
		btnSVMMap			= (Button) findViewById(R.id.meu_svm_map);
		
	
		btnCollection.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeActivity(MenuActivity.this, CollectionActivity.class);
			}
		});
		
		btnNNTraing.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeActivity(MenuActivity.this, Map1FActivity.class);
			}
		});
		
		btnNNTesting.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeActivity(MenuActivity.this, AnnRecallingActivity.class);
			}
		});
		
		btnSVMTesting.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeActivity(MenuActivity.this, SvmRecallingActivity.class);
			}
		});
		
		btnSVMMap.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeActivity(MenuActivity.this, SvmMapActivity.class);
			}
		});

	}
		
}
