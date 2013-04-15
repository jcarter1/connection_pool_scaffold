package com.opower.connectionpool;
//hm
import static org.junit.Assert.*;
import org.junit.*;

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

	//@BeforeClass
	//create in memory db here and fill with some data
	
	//@Before
	// initialize OPConnection_Pool
	
	@Test
	public void testOPConnectionPoolExists() {
		// does connection pool exist
		fail("Not yet implemented");
	}

	@Test
	public void testOPConnectionPoolMinConns() {
		// did constructor create min_conns connections
		fail("Not yet implemented");
	}
	
	@Test
	public void testOPConnectionPoolMaxConns() {
		// will number of connections not exceed max_conns
		// possibly do this test later on
		fail("Not yet implemented");
	}
	
	@Test
	public void testOPConnectionPoolGetWorkingConnection() {
		// does this return a working connection
		// execute SQL statement and test result for correctness
		fail("Not yet implemented");
	}

	@Test
	public void testOPConnectionPoolGetConnection_used_conns() {
		// is the returned connection added to used_conns?
		fail("Not yet implemented");
	}
	
	@Test
	public void testReleaseConnection_used_conns() {
		// is used_conns -1 when calling this function?
		fail("Not yet implemented");
	}
	
	@Test
	public void testReleaseConnectionTo_open_conns() {
		// if (min_conns > used_conns.size() + open_conns.size())
		// is open_conns increased by 1 when calling this function?
		fail("Not yet implemented");
	}
	
	@Test
	public void testReleaseConnectionClose() {
		// if NOT (min_conns > used_conns.size() + open_conns.size())
		// is open_conns staying the same when calling this function?
		fail("Not yet implemented");
	}
}
