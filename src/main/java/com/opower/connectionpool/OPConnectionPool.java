package com.opower.connectionpool;

import java.util.*;
//import java.util.concurrent.*;
//import java.util.Collections.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

/**
 * @assignment OPower Homework
 * @name OPConnectionPool.java
 * @purpose SQL connection pool implementation
 * @author Dennis Backhaus
 * @created 04/14/2013
 * @last_modified 04/14/2013
 */
public class OPConnectionPool implements ConnectionPool
{
	// server connection attributes
	private String serverURL;
	private String user;
	private String password;
	// minimum number of existing (in use or not) connections for the pool
	private Integer min_conns = 10;
	// maximum number of existing (in use or not) connections for the pool
	private Integer max_conns = 200;
	// minimum number of always open connections (up to max_conns)
	private Integer min_open_conns = 5;
	// wait time for getConnection() when the maximum number of connections has been reached and no
	// open connections are available.
	// If timeout = 0, wait time is indefinite and getConnection() waits until a connection opens up
	private Integer timeout = 0;
	// list of all currently open connections
	private List<Connection> open_conns = Collections.synchronizedList(new ArrayList<Connection>());
	// list of all currently used connections
	private List<Connection> used_conns = Collections.synchronizedList(new ArrayList<Connection>());


	//constructor
	public OPConnectionPool(String serverURL, String user, String password, Integer min_conns, Integer max_conns, Integer min_open_conns, Integer timeout) throws SQLException
	{
		this.serverURL = serverURL;
		this.user = user;
		this.password = password;
		this.min_conns = min_conns;
		this.max_conns = max_conns;
		this.min_open_conns = min_open_conns;
		this.timeout = timeout;

		createInitConns();
	}
	
	
	@Override
	public synchronized Connection getConnection() throws SQLException
	{
		// thread to create new connection while continuing with the program
		Thread create_new_conn = new Thread()
	    { 
	        public void run()
	        {
	        	try
	        	{
					open_conns.add(createConn());
				}   catch (SQLException e)
				{
					e.printStackTrace();
				}
		    }
		};
		
		// return an open connection if available and create a new one as long as the maximum number of connections is not reached
		// the second part is done in a thread, so the user does not have to wait for it
		if (!open_conns.isEmpty())
		{
			if (open_conns.size() < min_open_conns     &&     open_conns.size() + used_conns.size() <= max_conns)
			{
				create_new_conn.start();
			}
			
			return getOpenConn();
		}
		else
		{
			// maximum number of connections has been reached. If timeout = 0, wait here indefinitely, otherwise wait for timeout amount
			// of time to get a connection. If still no connection after timeout has passed: return NULL/throw Exception.
			if (timeout == 0)
			{
				Connection conn = createConn();
				used_conns.add(conn);
				return conn;
			}
			else
			{
				// dummy for now
				Connection conn = getOpenConn();
				return conn;
			}
		}
		
		
	}

	@Override
	public synchronized void releaseConnection(Connection conn) throws SQLException
	{
		// take conn out of used_conns
		used_conns.remove(conn);

		//release conn to open_conns to meet minimum amount of connections, else close connection entirely
		if (min_conns > used_conns.size() + open_conns.size())
		{
			open_conns.add(conn);
		}
		else
		{
			conn.close();
		}
				
	}
	
	// creates initial # of connections defined by min_conns and adds them to open_conns Connection List 
	private void createInitConns() throws SQLException
	{
		for(int i = 0; i < min_conns; i++)
		{
			open_conns.add(createConn());
		}
	}
	
	// returns a new connection
	private Connection createConn() throws SQLException
	{
		return DriverManager.getConnection(serverURL, user, password);
	}

	// returns an open connection from open_conns list
	private Connection getOpenConn() throws SQLException
	{
		Connection conn = open_conns.remove(0);
		used_conns.add(conn);
		return conn;
	}

	public Integer getMinConns()
	{
		return min_conns;
	}
	
	public Integer getOpenConnsCount()
	{
		return open_conns.size();
	}
	
	public Integer getUsedConnsCount()
	{
		return used_conns.size();
	}
}
