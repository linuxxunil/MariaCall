package edu.mariacall.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtility {
	/**
	 * ㄧ计嘿 : getDirectory
	 * ㄧ计弧 : 耝郎隔畖竚
	 * ㄧ计絛ㄒ :  
	 * @param token
	 * @param path
	 * @return
	 */
	static public String getBasename(String token, String path) {
		String[] split = path.split(token);
		return split[split.length-1];
	}
	
	/**
	 * ㄧ计嘿 : getDirectory
	 * ㄧ计弧 : 耝郎隔畖竚
	 * ㄧ计絛ㄒ :  
	 *  input  : /sdcard/data/cscheduling/data.db
	 *  output : /sdcard/data/cscheduling
	 * 
	 * @param path
	 * @return directory path
	 */
	static public String getDirectory(String path) {
		String[] split = path.split("/");
		String dir = "";
		for (int i=0; i<split.length-2; i++) {
			dir += split[i] + "/";		
		}
		return dir + split[split.length-2];
	}
	
	
	/**
	 * ㄧ计嘿 : isMailAddress
	 * ㄧ计弧 : 耞琌琌MailAdress
	 * ㄧ计絛ㄒ :  
	 *  input  : jesse_lin@ismp.csie.ncku.edu.tw
	 *  output : true
	 * @param mail
	 * @return true, if input is mail address 
	 */
	static public boolean isMailAddress(String mail) {
		 String check = "^([a-z0-9A-Z]+[-|\\._]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		 Pattern regex = Pattern.compile(check);
		 Matcher matcher = regex.matcher(mail);
		 return matcher.matches();
	}
}
