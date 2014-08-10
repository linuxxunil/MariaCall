package edu.mariacall.database;

public abstract class DatabaseDriver {
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	abstract public int createTable(String sql) ;
	
	
	abstract protected int setAutoCommit(boolean value);
	
	abstract protected int commit();

	abstract protected int rollback();
	
	abstract protected int getAutoCommit();
	
	abstract public Integer excuteTransation(Transation tran, Object retValue);

	
	/**
	 * 
	 * @return
	 * @return 
	 */
	abstract public int onConnect();
	
	/**
	 * 
	 * @return
	 */
	abstract public int close();
	
	
	/**
	 * 
	 * @param sql
	 * @return 
	 */
	abstract public int insert(String sql) ;
	
	/**
	 * 
	 * @param sql
	 * @return 
	 */
	abstract public MsResultSet select(String sql) ;

	
	/**
	 * 
	 * @param sql
	 * @return 
	 */
	abstract public int update(String sql) ;
	
	/**
	 * 
	 * @param table
	 * @return 
	 */
	abstract public int delete(String sql);

	abstract public int getTables(String[] tables);	
	protected void finalize()  {
		close();
	}
		
}
