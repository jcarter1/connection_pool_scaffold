package com.opower.connectionpool;

import static org.junit.Assert.*;
import org.junit.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * assignment OPower Homework
 * name Test_OPConnectionPool.java
 * purpose Test file for OpConnectionPool.java
 * author Dennis Backhaus
 * created 04/14/2013
 * last_modified 04/18/2013
 * by Dennis Backhaus
 */

public class Test_OPConnectionPool {
	
	/**
	 *  ConnectionPool OPCP for testing without timeout setting
	 */
	private OPConnectionPool OPCP;
	/**
	 *  ConnectionPool OPCP_timeout for testing with timeout setting
	 */
	private OPConnectionPool OPCP_timeout;
	/**
	 * Database connection of type Connection
	 */
	static Connection db_conn;
	/**
	 *  minimum number of existing (in use or not) connections for the pool
	 */
	private static Integer min_conns = 10;
	/**
	 *  maximum number of existing (in use or not) connections for the pool
	 */
	private static Integer max_conns = 200;

	
	/**
	 * create in memory h2 db here and fill with some data
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	@BeforeClass
	public static void createDB() throws SQLException, ClassNotFoundException
	{
		/**
		 *  register driver
		 */
		Class.forName("org.h2.Driver");
		/**
		 *  create h2 DB in memory
		 */
		db_conn = DriverManager.getConnection("jdbc:h2:mem:OPclassesDB;INIT=runscript from 'classpath:scripts/createTable.sql'\\;runscript from 'classpath:scripts/popTable.sql'");
	}
	
	/**
	 *  initialize OPConnection_Pool
	 * @throws SQLException
	 */
	@Before
	public void createOPCP() throws SQLException
	{
		OPCP = new OPConnectionPool("jdbc:h2:mem:OPclassesDB", "", "", min_conns, max_conns, 0);
		OPCP_timeout = new OPConnectionPool("jdbc:h2:mem:OPclassesDB", "", "", min_conns, max_conns, 4000);
	}
	
	/**
	 *  does connection pool exist
	 */
	@Test
	public void testOPConnectionPoolExists() throws SQLException
	{
		assertEquals(OPCP.getMinConns(), min_conns);
	}

	/**
	 *  did constructor create min_conns connections
	 */
	@Test
	public void testOPConnectionPoolMinConns() throws SQLException
	{
		assertEquals(OPCP.getOpenConnsCount(), min_conns);
	}
	
	/**
	 *  does this return a working connection
	 *  execute SQL statement and test result for correctness
	 */
	@Test
	public void testOPConnectionPoolGetWorkingConnection() throws SQLException
	{
		Connection conn = OPCP.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery("SELECT * FROM BestClasses");
		result.next();

		assertEquals(result.getString("Class"), "Hunters");
	}

	/**
	 *  is the returned connection added to used_conns?
	 */
	@Test
	public void testOPConnectionPoolGetConnection_used_conns() throws SQLException
	{
		OPCP.getConnection();

		assertEquals(OPCP.getUsedConnsCount(), new Integer(1)); 
	}
	
	/**
	 *  is used_conns -1 when calling this function?
	 */
	@Test
	public void testReleaseConnection_used_conns() throws SQLException
	{
		Connection conn = OPCP.getConnection();
		OPCP.releaseConnection(conn);

		assertEquals(OPCP.getUsedConnsCount(), new Integer(0));
	}
	
	/**
	 *  if (min_conns > used_conns.size() + open_conns.size())
	 *  aka: if less than the minimum amount of connections are
	 *  either open or in use:
	 *  is open_conns increased by 1 when calling this function?
	 */
	@Test
	public void testReleaseConnectionTo_open_conns() throws SQLException
	{
		Connection conn = OPCP.getConnection();
		Integer before_release = OPCP.getOpenConnsCount();
		OPCP.releaseConnection(conn);
		
		assertEquals(++before_release , OPCP.getOpenConnsCount());
	}
	
	/**
	 *  if NOT (min_conns > used_conns.size() + open_conns.size())
	 *  aka: opposite of previous test:
	 *  is open_conns staying the same when calling this function?
	 */
	@Test
	public void testReleaseConnectionClose() throws SQLException
	{
		for(int i = 0; i < 15; i++)
		{
			OPCP.getConnection();
		}
		Connection c6 = OPCP.getConnection();
		Integer before_release = OPCP.getOpenConnsCount();
		OPCP.releaseConnection(c6);

		assertEquals(before_release , OPCP.getOpenConnsCount());
	}
	
	/**
	 *  will number of connections not exceed max_conns?
	 */
	@Test
	public void testOPConnectionPoolMaxConnsDelayed() throws Throwable
	{
		class Delayed_release implements Runnable
		{
			private Connection conn;

			   public Delayed_release(Connection conn)
			   {
			       this.conn = conn;
			   }

			   public void run()
			   {
				   try
				   {
					   Thread.sleep(3000);
					   try
					   {
						   OPCP.releaseConnection(conn);
					   }   catch (SQLException e)
					   {
						   e.printStackTrace();
					   }
				   }   catch (InterruptedException e)
				   {
					   e.printStackTrace();
				   }
			   }
		}
		
		try
		{
			for(int i=0; i < max_conns-1; i++)
			{
				OPCP.getConnection();
			}
			
					
			Connection c200 = OPCP.getConnection();
			Thread t = new Thread(new Delayed_release(c200));
			t.start();
			/**
			 *  this is connection max_conns + 1 and will wait until c200 was released - ~3 seconds
			 */
			OPCP.getConnection();			
		}   catch (SQLException e)
		{
			throw e.getCause();
		}

		assertEquals(max_conns, new Integer(OPCP.getOpenConnsCount() + OPCP.getUsedConnsCount()));
	}
	
	/**
	 *  fill DB up to max_conns + 1
	 *  this will timeout and throw an exception after ~4 seconds
	 */
	@Test(expected=SQLException.class)
	public void testOPConnectionPoolMaxConnsTimeout() throws SQLException
	{
		for(int i=0; i < max_conns+1; i++)
		{
			OPCP_timeout.getConnection();
		}
	}
}
