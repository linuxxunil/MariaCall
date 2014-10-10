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
		"edu.mariacall.common.Logger"					,"00011000",
		"edu.mariacall.common.Network"					,"00012000",
		"edu.mariacall.database.MSSqlDriver"			,"00021000",
		"edu.mariacall.database.SqliteDriver"			,"00022000",
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
}


