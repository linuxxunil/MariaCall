package edu.mariacall.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {
	static private Context _context = null;
	
	static public void setContext(Context context) {
		_context = context;
	}
	
	
	static public int isNetworkAvailable() {
		if ( _context == null ) 
			return Logger.e(Network.class, StatusCode.ERR_NETWORK_DONT_SET_CONTEXT);
		ConnectivityManager connectivityManager 
        		= (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    	    
  		return (activeNetworkInfo != null && activeNetworkInfo.isConnected())
  				?StatusCode.success:Logger.e(Network.class, StatusCode.ERR_NETWORK_ISNOT_AVAILABLE);
	}
}
