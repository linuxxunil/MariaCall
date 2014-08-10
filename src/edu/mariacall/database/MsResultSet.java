package edu.mariacall.database;
import java.sql.ResultSet;
public class MsResultSet {
	public int status;
	public ResultSet rs = null;
	
	public MsResultSet() {}	
	public MsResultSet(int status) {
		this.status = status;
	}
	public MsResultSet(ResultSet rs) {
		status = 0;
		this.rs = rs;
	}
}
