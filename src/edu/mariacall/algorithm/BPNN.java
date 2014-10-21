package edu.mariacall.algorithm;


import java.util.LinkedList;
import java.util.Random;

abstract class BPNN {
	private int nCount	= 4000;
	private int nInput 	= 2;
	private int nHidden = 2;
	private int nOutput = 1;
	private float eta 	= 0.5f;		// 學習率
	private float alpha = 0.2f;		// 慣性因子
	
	private float[] 	X=null,H=null,Y=null,T=null;			
	private float[][] 	wXH=null,wHY=null;			// Weight
	private float[][]	dwXH=null,dwHY=null;			// ΔWeight
	private float[]	thetaH=null, thetaY=null;		// θ = theta
	private float[]	dThetaH=null,dThetaY=null;	// Δθ
	private float[]	deltaH=null,deltaY=null;		// ΔH、ΔY
	
	private float mse = 0.0f;
	
	public BPNN(int nCount,int nInput,int nHidden, int nOutput, float eta,float alpha) {
		this.nCount = nCount;
		this.nInput = nInput;
		this.nHidden = nHidden;
		this.nOutput = nOutput;
		this.eta = eta;
		this.alpha = alpha;
		init();
	}
	
	public BPNN(int nCount,LinkedList<float[][]> weight,LinkedList<float[]> theta,float eta, float aplpha) {
		this.nCount = nCount;
		wXH = weight.get(0);
		wHY = weight.get(1);
		thetaH = theta.get(0);
		thetaY = theta.get(1);
		
		nInput = wXH.length;
		nHidden = wHY.length;
		nOutput = wHY[0].length;
		init();
	}
	
	public BPNN() { 
		init();
	}
	
	abstract protected void cbSetInputData(float[] X);
	
	abstract protected void cbSetOutputData(float[] T);
	
	private void initRandomWeight(float[][] w) {
		Random ran = new Random();
		for (int i=0; i<w.length; i++) {
			for(int j=0; j<w[i].length; j++) {
				w[i][j] = ran.nextFloat()-0.5f;
			}
		}
	}
	
	private void init() {
		if( X 		== null ) X 		= new float[nInput];
		if( H 		== null ) H 		= new float[nHidden];
		if( Y 		== null ) Y 		= new float[nOutput];
		if( T 		== null ) T 		= new float[nOutput];
		if( dwXH 	== null ) dwXH 		= new float[nInput][nHidden];
		if( dwHY 	== null ) dwHY 		= new float[nHidden][nOutput];
		if( thetaH	== null ) thetaH	= new float[nHidden];
		if( thetaY 	== null ) thetaY	= new float[nOutput];
		if( dThetaH == null ) dThetaH	= new float[nHidden];
		if( dThetaY == null ) dThetaY	= new float[nOutput];
		if( deltaH 	== null ) deltaH	= new float[nHidden];
		if( deltaY 	== null ) deltaY	= new float[nOutput];
		if( wXH == null ) {
			wXH  = new float[nInput][nHidden];
			initRandomWeight(wXH);
		}
		if( wHY == null ) { 
			wHY 		= new float[nHidden][nOutput];
			initRandomWeight(wHY);
		}

	}	
	
	private boolean computeNet(final float[] input, float[] output,
							final float[] theta,final float[][] weight) {
		if ( theta.length != output.length || 
				input.length != weight.length )
			return false;
		
		float sum;
		for (int j=0; j<weight[0].length; j++) {
			sum = 0.0f;
			for (int i=0; i<weight.length; i++) {
				sum += input[i]*weight[i][j];
			}
			output[j] = (float) ((float)1.0 / (1.0 + Math.exp(-(sum-theta[j]))));	
		}
		return true;
	}

	private boolean computeDeltaWeight(float[][] dweight, final float[] delta, 
						final float[] net) {
		if ( dweight.length != net.length || 
				dweight[0].length != delta.length )
			return false;
		
		for(int j=0; j<dweight[0].length; j++)
			for(int i=0; i<dweight.length; i++) {
				dweight[i][j] = eta * delta[j] * net[i] 
						+ alpha * dweight[i][j];
		}
		return true;
	}
	
	private boolean computeDeltaTheta(float[] dTheta, final float[] delta) {
		if ( dTheta.length != delta.length )
			return false;
		
		for (int i=0; i< dTheta.length; i++)
			dTheta[i] = (-eta * delta[i]) + (alpha * dTheta[i]);
		return true;
	}
	
	private boolean updateWeight(float[][] weight, final float[][] dweight) {
		if ( weight.length != dweight.length ||
				weight[0].length != dweight[0].length )
			return false;
		
		for (int j=0; j<weight[0].length; j++) {
			for(int i=0; i<weight.length; i++) {
				weight[i][j] += dweight[i][j];
				//System.out.println("i="+i+";j=" +j+";w="+weight[i][j]+";dw="+ dweight[i][j]);
			}
		}
		return true;
	}
	
	private boolean updateTheta(float[] theta, final float[] dTheta) {
		if ( theta.length != dTheta.length )
			return false;
		for (int i=0; i<theta.length; i++) {
			theta[i] += dTheta[i];
		}
		return true;
	}
	
	public void printWeight(float[][] w) {
		for (int i=0; i<w.length; i++) {
			for (int j=0; j<w[0].length; j++)
				System.out.print("w["+i+"]["+j+"]="+w[i][j]+"\t");
			System.out.println("");
		}
	}
	
	public void printTheta(float[] theta) {
		for (int i=0; i<theta.length; i++)
				System.out.print("t["+i+"]="+theta[i]+"\t");
		System.out.println("");
	}
	
	public void learningStart() {
		mse = 0.0f;
		float sum;
		
		
		for (int cnt=0; cnt<nCount; cnt++) {
			cbSetInputData(X);
			cbSetOutputData(T);	
			
			// compute H,Y
			computeNet(X, H,thetaH, wXH);
			computeNet(H, Y,thetaY, wHY);
			
			// compute deltaY
			for(int y=0; y<deltaY.length; y++) 
				deltaY[y] = (T[y]-Y[y])*Y[y]*(1.0f-Y[y]);
			
			// compute deltaH
			for(int h=0; h<deltaH.length; h++) {
				sum = 0.0f;
				for(int y=0; y<deltaY.length; y++)
					sum += deltaY[y] * wHY[h][y];
				deltaH[h] = sum * H[h] * (1.0f - H[h]); 
			}
			
			// compute ΔWeight
			computeDeltaWeight(dwHY, deltaY, H);
			computeDeltaWeight(dwXH, deltaH, X);
			
			// compute Δθ
			computeDeltaTheta(dThetaY, deltaY);
			computeDeltaTheta(dThetaH, deltaH);
			
			// update weight
			updateWeight(wHY,dwHY);
			updateWeight(wXH,dwXH);

			// update theta
			updateTheta(thetaY, dThetaY);
			updateTheta(thetaH, dThetaH);
			
			 mse=0.0f;
			 for (int j=0;j<T.length;j++) {
				 mse+=(T[j]-Y[j])*(T[j]-Y[j]);
			 }
		}
	}
	
	public LinkedList<float[][]> getWeight() {
		LinkedList<float[][]> list = new LinkedList<float[][]>();
		list.add(wXH);
		list.add(wHY);
	
		return list;
	}
	
	public LinkedList<float[]> getTheta() {
		LinkedList<float[]> list = new LinkedList<float[]>();
		list.add(thetaH);
		list.add(thetaY);
	
		return list;
	}

	public float getMSE() {
		return mse;
	}
	public float[] predict(float[] X) {
		float[] Y = new float[nOutput];
		
		computeNet(X, H,thetaH, wXH);
		computeNet(H, Y,thetaY, wHY);
		
		return Y;
	}
}