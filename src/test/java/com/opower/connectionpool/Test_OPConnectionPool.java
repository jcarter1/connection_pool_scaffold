package com.opower.connectionpool;

import static org.junit.Assert.*;
import org.junit.*;

import org.h2.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @assignment OPower Homework
 * @name Test_OPConnectionPool.java
 * @purpose Test file for OpConnectionPool.java
 * @author Dennis Backhaus
 * @created 04/14/2013
 * @last_modified 04/15/2013
 */

public class Test_OPConnectionPool {
	
	private OPConnectionPool OPCP;
	static Connection db_conn;
	// minimum number of existing (in use or not) connections for the pool
	private Integer min_conns = 10;
	private Integer before_release;

	@BeforeClass
	//create in memory h2 db here and fill with some data
	public static void createDB() throws SQLException, ClassNotFoundException
	{
		// register driver
		Class.forName("org.h2.Driver");
		// create h2 DB in memory
		db_conn = DriverManager.getConnection("jdbc:h2:mem:OPclassesDB;INIT=runscript from 'C:/Users/Dennis/git/connection_pool_scaffold/src/test/java/com/opower/connectionpool/createTable.sql'\\;runscript from 'C:/Users/Dennis/git/connection_pool_scaffold/src/test/java/com/opower/connectionpool/popTable.sql'");
	}
	
	@Before
	// initialize OPConnection_Pool
	public void createOPCP() throws SQLException, NullPointerException
	{
		OPCP = new OPConnectionPool("jdbc:h2:mem:OPclassesDB", "", "", min_conns, 200, 5, 0);
	}
	
	@Test
	public void testOPConnectionPoolExists() throws SQLException
	{
		// does connection pool exist
		assertEquals(OPCP.getMinConns(), min_conns);
	}

	@Test
	public void testOPConnectionPoolMinConns() throws SQLException
	{
		// did constructor create min_conns connections
		assertEquals(OPCP.getOpenConnsCount(), min_conns);
	}
	
	@Test
	public void testOPConnectionPoolMaxConns() throws SQLException
	{
		// will number of connections not exceed max_conns
		// do this test later on
		fail("Not yet implemented");
	}
	
	@Test
	public void testOPConnectionPoolGetWorkingConnection() throws SQLException
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
	public void testOPConnectionPoolGetConnection_used_conns() throws SQLException
	{
		// is the returned connection added to used_conns?
		OPCP.getConnection();
		assertEquals(OPCP.getUsedConnsCount(), new Integer(1)); 
	}
	
	@Test
	public void testReleaseConnection_used_conns() throws SQLException
	{
		// is used_conns -1 when calling this function?
		Connection conn = OPCP.getConnection();
		OPCP.releaseConnection(conn);
		assertEquals(OPCP.getUsedConnsCount(), new Integer(0));
	}
	
	@Test
	public void testReleaseConnectionTo_open_conns() throws SQLException
	{
		// if (min_conns > used_conns.size() + open_conns.size())
		// aka: if less than the minimum amount of connections are
		// either open or in use:
		// is open_conns increased by 1 when calling this function?
		Connection conn = OPCP.getConnection();
		before_release = OPCP.getOpenConnsCount();
		before_release++;
		OPCP.releaseConnection(conn);
		assertEquals(before_release , OPCP.getOpenConnsCount());
	}
	
	@Test
	public void testReleaseConnectionClose() throws SQLException
	{
		// if NOT (min_conns > used_conns.size() + open_conns.size())
		// aka: opposite of previous test:
		// is open_conns staying the same when calling this function?
		for(int i = 0; i < 6; i++)
		{
			OPCP.getConnection();
		}
		Connection c6 = OPCP.getConnection();
		Integer before_release = OPCP.getOpenConnsCount();
		OPCP.releaseConnection(c6);
		assertEquals(before_release , OPCP.getOpenConnsCount());
	}
}
