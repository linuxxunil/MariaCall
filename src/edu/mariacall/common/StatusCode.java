package edu.mariacall.common;

/**
 * StatusCode Definition 
 *  Status Code :
 *  1. status code = 0 => Success
 *  2. status code > 0 => Information
 *  3. status code < 0 => Error 
 *  4. consist of 
 *     F E DD CC B AA (-0
 *     AA : Error Code
 *     B  : Type 
 *     		* 0: User define Error
 *     		* 1: Parameter Error
 *     		* 2: Database common Error
 *     		* 3: Network Error
 *     DD : Class Index
 *     DD : Class number
 *     E  : Reserved
 *     F  : +/-
 * @author jesse
 *
 */

public class StatusCode {
	final static public int success = 0;
		
	final static public String[] ClassInfo = {
		"edu.mcscheduling.common.Logger"				,"00011000",
		"edu.mcscheduling.common.Network"				,"00012000",
		"edu.mcscheduling.database.MSSqlDriver"			,"00023000",
		"edu.mcscheduling.database.SqliteDriver"		,"00024000",
		"edu.mcscheduling.model.Account"				,"00031000",
		"edu.mcscheduling.model.Hospital"				,"00032000",
		"edu.mcscheduling.model.Department"				,"00033000",
		"edu.mcscheduling.model.Doctor"					,"00034000",
		"edu.mcscheduling.model.DoctorSchedule"			,"00035000",
		"edu.mcscheduling.controller.AuthorizeActivty"	,"00041000"
	};
	
	/* Common Define */
	// Parameter
	final static public String PARM_SQL_IS_ERROR   			= "-101,PARM: SQL is error";
	final static public String PARM_USERID_ERROR   			= "-102,PARM: userid is error";
	final static public String PARM_USERNAME_ERROR			= "-103,PARM: username is error";
	final static public String PARM_USERPASSWD_ERROR 		= "-104,PARM: userpasswd is error";
	final static public String PARM_OLDPASSWD_ERROR 		= "-105,PARM: old userpasswd is error";
	final static public String PARM_NEWPASSWD_ERROR 		= "-106,PARM: new userpasswd is error";
	final static public String PARM_CONTENT_ARRAY_ERROR		= "-107,PARM: content is error";
	final static public String PARM_UPDATEID_ERROR			= "-108,PARM: update id is error";
	final static public String PARM_DEPART_NAME_ERROR		= "-109,PARM: depart name is error";
	final static public String PARM_DESC_ERROR				= "-110,PARM: desc is error";
	final static public String PARM_DOCTOR_NUM_ERROR		= "-111,PARM: dorNo is error";
	final static public String PARM_SCH_YEAR_ERROR			= "-112,PARM: SchYear is error";
	final static public String PARM_SCH_MONTH_ERROR			= "-113,PARM: SchMonth is error";

	// Logger
	final static public String ERR_LOGER_NUMBER_NOT_FOUND 	= "-201,Loger number not define";
	// Database
	final static public String ERR_JDBC_CLASS_NOT_FOUND 	= "-301,JDBC class not found";
	final static public String ERR_INITIAL_DB_NOT_SUCCESS 	= "-302,Inital DB don't success";
	final static public String ERR_SQL_SYNTAX_IS_ILLEGAL 	= "-303,SQL syntax is illegal";
	final static public String ERR_GET_RESULTSET_FAIL 		= "-304,ResultSet get error";
	final static public String ERR_SET_AUTOCOMMIT_FAIL		= "-305,AutoCommit set fail";
	final static public String ERR_GET_AUTOCOMMIT_FAIL		= "-306,AutoCommit get fail";
	final static public String ERR_EXE_ROLLBACK_FAIL		= "-307,Execute rollback fail";
	final static public String ERR_EXE_TRANSCATION_FAIL		= "-308,Execute transcation fail";
	final static public String ERR_CONNECT_MSSERVER_FAIL 	= "-309,Connect to SQL Server fail";
	// Network
	final static public String ERR_NETWORK_DONT_SET_CONTEXT	= "-401,Network Context variable is null";
	final static public String ERR_NETWORK_ISNOT_AVAILABLE 	= "-402,Network isn't avaiable";
	
	// Runtime : Value only is -999
	final static public String ERR_UNKOWN_ERROR				= "-999,unkown error";

	
	// database_SqliteDriver
	final static public String ERR_OPEN_SQLITE_DIR			= "-001,Open sqlite dir error";

	// database_MSSqlDriver
	final static public String ERR_MSSQL_CONNECT_ERROR		= "-001,Cannot connect to MSSQL";
	
	// model
	// Account
	final static public String ERR_LOGIN_FAIL				= "-001,Login fail (userid or password is error)";
	final static public String ERR_MEMBER_INFO_IS_NOT		= "-002,Member info is not found";
	final static public String ERR_GET_MEMBER_INFO_FAIL		= "-003,Get Member information fail";
	final static public String WAR_REGISTERED_USER			= "001,Registed user";
	// Hospital
	final static public String WAR_HOSPITAL_NOT_SETTING		= "001,Hospital is not setting";
	// Depatment
	final static public String WAR_DEPARTMENT_NOT_SETTING	= "001,Department is not setting";
	// Doctor
	final static public String WAR_DOCTOR_NOT_SETTING		= "001,Doctor is not setting";
	// DoctorSchedule
	final static public String WAR_SCHEDULE_NOT_SETTING		= "001,Doctor Schudule is not setting";
	final static public String WAR_NOT_SUPPORT_YEAR			= "002, value must gather than 2012 year";
	final static public String ERR_GET_DEAPART_DATA_ERROR	= "-001,Get depart data error";
	final static public String ERR_GET_DOCTOR_DATA_ERROR	= "-002,Get doctor data error";
	final static public String ERR_GET_SCHEDULE_DATA_ERROR	= "-003,Get schudule data error";
	
	// controller
	// AuthorizeActivty
	final static public String WAR_MACT_UNINSTALLED			= "001,Mact uninstalled";
	
	//final static public String ERR_DEPART_NOT_EXIST			= "-002,Depart is not found";
	//final static public String ERR_GET_MEMBER_INFO_FAIL		= "-003,Get Member information fail";
	/*
	final static public String WAR_REGISTER_FAIL			= "001,Register Fail";
	final static public String WAR_LOGIN_FAIL				= "002,Login Fail";
	final static public String WAR_PASSWD_NOT_CHANGE_FAIL	= "003,Password not change";
	final static public String ERR_PASSWD_CHANGE_ERROR		= "-001,Password change error";
	final static public String ERR_SET_MEMBER_INFO_ERROR	= "-002,Set member information error";
	*/
	/*
	// http
	public static int ERR_HTTP_PROTOCOL_ERR(int upperStatus, String evt) {
		return log("-4001","HTTP Protocol error("+evt+")");
	}
	
	public static int ERR_HTTP_CONNECT_ERR(int upperStatus) {
		return log("-4002","HTTP connect error");
	}
	
	public static int ERR_HTTP_RESPONSE_CODE_ERR(int upperStatus, int httCode) {
		return log("-4003","HTTP response error("+httCode+")");
	}
	
	public static int ERR_HTTP_URL_ILLEGAL(int upperStatus, String evt) {
		return log("-4004","HTTP URL is illegal("+evt+")");
	}
	
	public static int ERR_HTTP_IO_ERR(int upperStatus, String evt) {
		return log("-4005","HTTP IO error("+evt+")");
	}
	
	// Authorize Activty
	public static int WAR_MACT_UNINSTALLED(int upperStatus) {
		return log("10001","Mact uninstalled ");
	}
	*/
}


