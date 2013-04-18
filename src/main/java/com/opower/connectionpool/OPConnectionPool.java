package com.opower.connectionpool;

import java.util.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

import org.apache.log4j.*;
import org.apache.log4j.PropertyConfigurator;

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
	// wait time for getConnection() when the maximum number of connections has been reached and no
	// open connections are available.
	// If timeout = 0, wait time is indefinite and getConnection() waits until a connection opens up
	private Integer timeout = 0;
	// list of all currently open connections
	private List<Connection> open_conns = Collections.synchronizedList(new ArrayList<Connection>());
	// list of all currently used connections
	private List<Connection> used_conns = Collections.synchronizedList(new ArrayList<Connection>());
	// log4j logger
	private static final Logger logger = Logger.getLogger(OPConnectionPool.class);
	


	//constructor
	public OPConnectionPool(String serverURL, String user, String password, Integer min_conns, Integer max_conns, Integer timeout) throws SQLException
	{
		this.serverURL = serverURL;
		this.user = user;
		this.password = password;
		this.min_conns = min_conns;
		this.max_conns = max_conns;
		this.timeout = timeout;
		
		PropertyConfigurator.configure("log4j.properties");
		
		createInitConns();
	}
	
	
	@Override
	public synchronized Connection getConnection() throws SQLException
	{
		// return an open connection if available and create a new one as long as the maximum number of connections is not reached
		if (!open_conns.isEmpty())
		{
			return getOpenConn();
		}
		else
		{
			return getConnectionHandler();
		}
		
		
	}

	@Override
	public void releaseConnection(Connection conn) throws SQLException
	{
		// take conn out of used_conns and call notify()
		// notify() is needed to let the getConnectionHandler() know that a Connection has freed up
		// which is needed if the max limit was previously reached
		synchronized(used_conns)
		{
			used_conns.remove(conn);
		    used_conns.notify();
		}
		
		//release conn to open_conns to meet minimum amount of connections, else close connection entirely
		if (min_conns > used_conns.size() + open_conns.size())
		{
			open_conns.add(conn);
			logger.debug("A formerly used connection has been released to open connections pool.");
		}
		else
		{
			logger.debug("Connection closed because minimum amount of connections has already been reached:");
			logger.debug("Connection: " + conn);
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
		logger.debug("Amount of connections created by createInitConns(): " + open_conns.size());
	}
	
	// returns a new connection
	private Connection createConn() throws SQLException
	{
		try
		{
			return DriverManager.getConnection(serverURL, user, password);
		}   catch (SQLException e)
		{
			logger.error("A Database connection could not be established.");
			logger.error(e);
		}
		return null;

	}

	// returns an open connection from open_conns list
	private Connection getOpenConn()
	{
		Connection conn = open_conns.remove(0);
		used_conns.add(conn);
		return conn;
	}
	
	private Connection getConnectionHandler() throws SQLException
	{
		// if max connections has not been reached, create a new connection
		if (used_conns.size() < max_conns)
		{
			Connection conn = createConn();
			used_conns.add(conn);
			return conn;
		}
		// if max connections has been reached
		else
		{
			synchronized(used_conns)
			{
			    try
			    {
			    	// if timeout=0: wait indefinitely until some connection has been closed
			    	if (timeout==0)
			    	{
			    		logger.debug("Started indefinite wait loop due to max connections and timeout = 0.");
			    		used_conns.wait();
			    		logger.debug("Some other connection was closed, attempting to create a new one now.");
				    	Connection conn = createConn();
						used_conns.add(conn);
						return conn;
				    }
			    	
			    	// timeout is NOT 0. Wait for timeout amount of time until rejecting the request
					else
					{
						logger.debug("Connection limit reached. Waiting " + timeout + "ms for one to open up.");
						used_conns.wait(timeout);
						
						// check if we timed out
						if (used_conns.size() == max_conns)
						{
							// return null because no connection opened up
							logger.debug("Unfortunately time has passed and we timed out - no connection made.");
							throw new SQLException("All connections in use. Timed out trying to get one.");
						}
						
						// we did not time out and can create a new connection now
						else
						{
							logger.debug("Connection became available before timeout reached.");
							Connection conn = createConn();
							used_conns.add(conn);
							return conn;
						}
					}
			    }   catch (InterruptedException e)
			        {
			    		logger.warn("The program has been interrupted.");
			    		logger.warn(e);
			    		logger.warn(e.getCause());
			    	}
			}
		}
		return null;
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
