package edu.mariacall.activity;

import java.util.Arrays;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.learning.error.ErrorFunction;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;

import edu.mariacall.R;
import edu.mariacall.R.id;
import edu.mariacall.R.layout;
import edu.mariacall.R.menu;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class TrainingActivity extends ControllerActivity {
	private final int TAG_TEST = 1;
	
	private Button btnTest = null;
	private TextView tViwView = null;
	private String nnetPath = "/sdcard/data/mariacall/myMlPerceptron.nnet";
	private String traingPath = "/sdcard/data/mariacall/training_03.txt";
	private int nInput = 3;
	private int nHidden = 20;
	private int nOutput = 11;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initLayout();
		initListeners();
		initHandler();
	}
	
	private void initLayout() {
		setContentView(R.layout.layout_training);
	}

	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case TAG_TEST:
					break;
				}
			}
		};
	}

	private void initListeners() {
		btnTest = (Button) findViewById(R.id.trn_btnTest);
		tViwView = (TextView) findViewById(R.id.trn_tViwView);
		

		btnTest.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {


			    // create training set (logical XOR function)
			    DataSet trainingSet = DataSet.createFromFile(
			    		traingPath, nInput, nOutput, ",", false);
			
			    MultiLayerPerceptron myMlPerceptron = 
			    		new MultiLayerPerceptron(TransferFunctionType.SIGMOID, nInput, nHidden, nOutput);
			    // learn the training set
			    BackPropagation bp = new BackPropagation();
			    
			    bp.setErrorFunction(new ErrorFunction(){

					@Override
					public double getTotalError() {
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public void addOutputError(double[] outputError) {
						
						System.out.println(outputError.length);
					}

					@Override
					public void reset() {
						// TODO Auto-generated method stub
						
					}
			    	
			    }); 
			    bp.setLearningRate(0.01);
			    
			    myMlPerceptron.learn(trainingSet,bp);
			  
			    
			    // test perceptron
			    //System.out.println("Testing trained neural network");
			    //testNeuralNetwork(myMlPerceptron, trainingSet);

			    // save trained neural network
			    //myMlPerceptron.save(nnetPath);

			    // load saved neural network
			   // NeuralNetwork loadedMlPerceptron = NeuralNetwork.createFromFile(nnetPath);

			    // test loaded neural network
			    //System.out.println("Testing loaded neural network");
			    //testNeuralNetwork(loadedMlPerceptron, trainingSet);
				
			}
		});
	}
	
	private void testNeuralNetwork(NeuralNetwork nnet, DataSet testSet) {

	    for(DataSetRow dataRow : testSet.getRows()) {

	        nnet.setInput(dataRow.getInput());
	        nnet.calculate();
	        double[ ] networkOutput = nnet.getOutput();
	        System.out.print("Input: " + Arrays.toString(dataRow.getInput()) );
	        System.out.println(" Output: " + Arrays.toString(networkOutput) ); 

	    }
	}

}
