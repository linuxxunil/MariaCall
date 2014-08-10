package edu.mariacall.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqldroid.SqldroidConnection;

import edu.mariacall.common.Logger;
import edu.mariacall.common.StatusCode;
import edu.mariacall.common.StringUtility;

public class SqliteDriver extends DatabaseDriver {

	private final String dbPath;
	private Connection conn = null;
	private Statement stmt = null;
	
	
	public SqliteDriver(final String dbPath) {
		this.dbPath = dbPath;
	}
	
	@Override
	public int onConnect() {
		try {
			Class.forName("org.sqldroid.SqldroidDriver");
			
			File dir = new File(StringUtility.getDirectory(dbPath));
			
			if ( !dir.exists() && !dir.mkdirs() )
				return Logger.e(this, StatusCode.ERR_OPEN_SQLITE_DIR,StringUtility.getDirectory(dbPath));
			
			conn =  (SqldroidConnection)DriverManager
					.getConnection(
					"jdbc:sqldroid:" + this.dbPath
						);
		}  catch (ClassNotFoundException e) {
			return  Logger.e(this, StatusCode.ERR_JDBC_CLASS_NOT_FOUND);
		} catch (SQLException e) {
			return  Logger.e(this, StatusCode.ERR_SQL_SYNTAX_IS_ILLEGAL, e.getMessage());
		}
		return StatusCode.success;
	}
	
	@Override
	public int createTable(String sql) {
		if (conn == null)
			return  Logger.e(this, StatusCode.ERR_INITIAL_DB_NOT_SUCCESS);
		else if (sql.isEmpty())
			return  Logger.e(this, StatusCode.PARM_SQL_IS_ERROR);

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			return Logger.e(this, StatusCode.ERR_SQL_SYNTAX_IS_ILLEGAL,e.getMessage());
		}
		return StatusCode.success;
	}
	
	@Override
	public int insert(String sql) {
		if (conn == null)
			return  Logger.e(this, StatusCode.ERR_INITIAL_DB_NOT_SUCCESS);
		else if (sql.isEmpty())
			return  Logger.e(this, StatusCode.PARM_SQL_IS_ERROR);

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			return Logger.e(this, StatusCode.ERR_SQL_SYNTAX_IS_ILLEGAL,e.getMessage());
		}
		
		return StatusCode.success;
	}
	
	@Override
	public int update(String sql) {
		if (conn == null)
			return  Logger.e(this, StatusCode.ERR_INITIAL_DB_NOT_SUCCESS);
		else if (sql.isEmpty())
			return  Logger.e(this, StatusCode.PARM_SQL_IS_ERROR);

		try {
			System.out.println(sql);
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			
		} catch (SQLException e) {
			return Logger.e(this, StatusCode.ERR_SQL_SYNTAX_IS_ILLEGAL,e.getMessage());
		}
		
		return StatusCode.success;
	}
	

	
	@Override
	public MsResultSet select(String sql) {
		if (conn == null)
			return new MsResultSet(Logger.e(this, StatusCode.ERR_INITIAL_DB_NOT_SUCCESS));
		else if (sql.isEmpty())
			return new MsResultSet(Logger.e(this, StatusCode.PARM_SQL_IS_ERROR));
		
		try {
			System.out.println(sql);
			stmt = conn.createStatement();
			return new MsResultSet(stmt.executeQuery(sql));
		} catch (SQLException e) {
			return new MsResultSet(Logger.e(
					this, StatusCode.ERR_SQL_SYNTAX_IS_ILLEGAL, e.getMessage()));
		}
	}
	
	@Override
	public int delete(String sql) {
		if (conn == null)
			return  Logger.e(this, StatusCode.ERR_INITIAL_DB_NOT_SUCCESS);
		else if (sql.isEmpty())
			return  Logger.e(this, StatusCode.PARM_SQL_IS_ERROR);
		
		try {
			System.out.println(sql);
			stmt = conn.createStatement();
			stmt.execute(sql);
		} catch (SQLException e) {
			return Logger.e(this, StatusCode.ERR_SQL_SYNTAX_IS_ILLEGAL,e.getMessage());
		}
		
		return StatusCode.success;
	}

	@Override
	protected int setAutoCommit(boolean autoCommit) {
		try {
			conn.setAutoCommit(autoCommit);
		} catch ( SQLException e ) {
			return Logger.e(this, StatusCode.ERR_SET_AUTOCOMMIT_FAIL, e.getMessage());
		}
		return StatusCode.success;
	}

	@Override
	protected int commit() {
		try {
			conn.commit();
		} catch ( SQLException e ) {
			return Logger.e(this, StatusCode.ERR_SET_AUTOCOMMIT_FAIL, e.getMessage());
		}
		return StatusCode.success;
	}
	
	@Override
	protected int rollback() {
		try {
			conn.rollback();
		} catch ( SQLException e ) {
			return Logger.e(this, StatusCode.ERR_SET_AUTOCOMMIT_FAIL, e.getMessage());
		}
		return StatusCode.success;
		
	}
		
	@Override
	protected int getAutoCommit() {
		int status = StatusCode.success;
		try {
			if ( !conn.getAutoCommit() )
				status = Logger.e(this, StatusCode.ERR_GET_AUTOCOMMIT_FAIL);
		} catch ( SQLException e ) {
			return Logger.e(this, StatusCode.ERR_SET_AUTOCOMMIT_FAIL, e.getMessage());
		}
		return status;
	}

	@Override
	public int getTables(String[] tables) {
		/*
		try {
			stmt = conn.createStatement();
			ResultSet rs = null;
			String sql = "SELECT * FROM sqlite_master WHERE type='table'";
			rs = stmt.executeQuery(sql);
			
			int len=0;
			while (rs.next() ) len++;
		
			if ( len <= 0 ) {
				rs.close();
				//return Logger.e(this, StatusCode.ERR_SQL_SYNTAX_IS_ILLEGAL,e.getMessage());
			}
			
			tables = new String[len];
			rs = stmt.executeQuery(sql);
		
			int i = 0;
			while ( rs.next() ) {
				tables[i++] = new String(rs.getString("tbl_name"));
			}
			
			rs.close();
		} catch (SQLException e) {
			return Logger.e(this, StatusCode.ERR_SQL_SYNTAX_IS_ILLEGAL,e.getMessage());
		} 
		*/
		return StatusCode.success;
	}
	

	@Override
	public int close() {
		if(stmt != null) {
			try {
				stmt.close();
			} catch(SQLException e) {
				// nothing
			}
		}
		if(conn != null) {
			try {
				conn.close();
			} catch(SQLException e) {
				return Logger.e(this, StatusCode.ERR_SQL_SYNTAX_IS_ILLEGAL,e.getMessage());       
			}
		}
		return StatusCode.success;
	}
	
	public Integer excuteTransation(final Transation tran,final Object retValue) {
		int status = StatusCode.success ; 
		try {
			status = setAutoCommit(false);
			if ( status != StatusCode.success )
				return status;
			status = tran.execute(retValue);
			if ( status != StatusCode.success ) {
				int status1 = rollback();	
				status = (status1 != StatusCode.success)?status1:status;
			} else {
				status = commit();
				if ( status != StatusCode.success ) {
					int status1 = rollback();	
					status = (status1 != StatusCode.success)?status1:status;
				}
			}
		} catch ( Exception e ) {
			int status1 = rollback();	
			status = (status1 != StatusCode.success)?status1:
				Logger.e(this, StatusCode.ERR_EXE_TRANSCATION_FAIL, e.getMessage());
		} 
		int status1 = setAutoCommit(true);
		return ( status1 != StatusCode.success )?status1:status;
	}
}
