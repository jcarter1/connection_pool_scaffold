package com.opower.connectionpool;


import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 */

/**
 * @assignment OPower Homework
 * @name OPConnectionPool.java
 * @purpose SQL connection pool implementation
 * @author Dennis Backhaus
 * @created 04/14/2013
 * @last_modified 04/14/2013
 */
public class OPConnectionPool implements ConnectionPool{

	/**
	 * @param args
	 */


	@Override
	public Connection getConnection() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void releaseConnection(Connection connection) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
