package edu.mariacall.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class Logger {
	
	private static int getClassNumber(String className) {
		int i;
		String patternStr;//= "^Account\\z|^Account\\$[1-9]";
		Pattern pattern;
		Matcher matcher;
		boolean matchFound;
		
		for ( i=0; i<StatusCode.ClassInfo.length; i++ ) {
			patternStr = String.format("^%s\\z|^%s\\$[1-9]", 
					StatusCode.ClassInfo[i], StatusCode.ClassInfo[i]);
			pattern = Pattern.compile(patternStr);
			matcher = pattern.matcher(className);
			matchFound  = matcher.find();
			if ( matchFound ) 
				return Integer.valueOf(StatusCode.ClassInfo[i+1]);
		}
		return e(Logger.class,StatusCode.ERR_LOGER_NUMBER_NOT_FOUND);
	}
	
	public static synchronized Integer e(Class cls, String errMsg, String extMsg) {
		String[] split = errMsg.split(",", 2);
		int code = Integer.valueOf(split[0]);
		String msg = split[1];
		String className = cls.getName();		
		int classNumber = getClassNumber(className);
		int rtValue = 0;
		if ( code < 0 ) {
			rtValue = classNumber - code;
			Log.e(className,String.valueOf(String.format("ErrCode: -%08d, ErrMsg: %s(%s)",
														rtValue,msg,extMsg )));
			return -rtValue;
		} else {
			rtValue = classNumber + code;
			Log.e(className,String.valueOf(String.format("ErrCode: %08d, ErrMsg: %s(%s)",
														rtValue,msg,extMsg )));
			return rtValue;
		}
	}
	public static Integer e(Object obj, String errMsg, String extMsg) {
		return e(obj.getClass(), errMsg, "");
	}
	
	public static Integer e(Object obj, String errMsg) {
		return e(obj.getClass(), errMsg, "");
	}
	
	public static Integer e(Class cls, String errMsg) {
		return e(cls, errMsg, "");
	}
}
