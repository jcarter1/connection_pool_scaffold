package com.opower.connectionpool;

import static org.junit.Assert.*;
import org.junit.*;

import java.sql.*;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;

/**
 * @assignment OPower Homework
 * @name Test_OPConnectionPool.java
 * @purpose Test file for OpConnectionPool.java
 * @author Dennis Backhaus
 * @created 04/14/2013
 * @last_modified 04/15/2013
 */

public class Test_OPConnectionPool {
	
	OPConnectionPool OPCP;
	// minimum number of existing (in use or not) connections for the pool
	private Integer min_conns = 10;

	//@BeforeClass
	//create in memory h2 db here and fill with some data
	public static void createDB() throws SQLException, ClassNotFoundException
	{
		// register driver
		Class.forName("org.h2.Driver");
		// create h2 DB in memory
		Connection db_conn = DriverManager.getConnection("jdbc:h2:mem:OPclassesDB;INIT=runscript from './createTable.sql'\\;runscript from './popTable.sql'");
	}
	
	//@Before
	// initialize OPConnection_Pool
	public void createOPCP() throws SQLException
	{
		OPCP = new OPConnectionPool("jdbc:h2:mem:OPclassesDB", "dennis", "test", min_conns, 200, 5, 0);
	}
	
	@Test
	public void testOPConnectionPoolExists()
	{
		// does connection pool exist
		assertEquals(OPCP.getMinConns(), min_conns);
	}

	@Test
	public void testOPConnectionPoolMinConns()
	{
		// did constructor create min_conns connections
		assertEquals(OPCP.getOpenConnsCount(), min_conns);
	}
	
	@Test
	public void testOPConnectionPoolMaxConns()
	{
		// will number of connections not exceed max_conns
		// do this test later on
		fail("Not yet implemented");
	}
	
	@Test
	public void testOPConnectionPoolGetWorkingConnection()
	{
		// does this return a working connection
		// execute SQL statement and test result for correctness
		Connection conn = OPCP.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet result = stmt.executeQuery("SELECT * FROM BestClasses");
		result.next();
		assertEquals(result.getString("Class"), "Hunters");
	}

	@Test
	public void testOPConnectionPoolGetConnection_used_conns()
	{
		// is the returned connection added to used_conns?
		Connection conn = OPCP.getConnection();
		assertEquals(OPCP.getUsedConnsCount(), new Integer(1)); 
	}
	
	@Test
	public void testReleaseConnection_used_conns()
	{
		// is used_conns -1 when calling this function?
		Connection conn = OPCP.getConnection();
		OPCP.releaseConnection(conn);
		assertEquals(OPCP.getUsedConnsCount(), new Integer(0));
	}
	
	@Test
	public void testReleaseConnectionTo_open_conns()
	{
		// if (min_conns > used_conns.size() + open_conns.size())
		// aka: if less than the minimum amount of connections are
		// either open or in use:
		// is open_conns increased by 1 when calling this function?
		Connection conn = OPCP.getConnection();
		private Integer before_release = OPCP.getOpenConnsCount();
		OPCP.releaseConnection(conn);
		assertEquals(before_release + 1 , OPCP.getOpenConnsCount());
	}
	
	@Test
	public void testReleaseConnectionClose()
	{
		// if NOT (min_conns > used_conns.size() + open_conns.size())
		// aka: opposite of previous test:
		// is open_conns staying the same when calling this function?
		Connection c1 = OPCP.getConnection();
		Connection c2 = OPCP.getConnection();
		Connection c3 = OPCP.getConnection();
		Connection c4 = OPCP.getConnection();
		Connection c5 = OPCP.getConnection();
		Connection c6 = OPCP.getConnection();
		private Integer before_release = OPCP.getOpenConnsCount();
		OPCP.releaseConnection(c6);
		assertEquals(before_release , OPCP.getOpenConnsCount());
	}
}
