package net.sourceforge.jtds.test;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Test for miscellaneous JDBC 3.0 features.
 *
 * @version $Id: JDBC3Test.java,v 1.1 2005-04-04 20:37:23 alin_sinpalean Exp $
 */
public class JDBC3Test extends TestBase {
    public JDBC3Test(String name) {
        super(name);
    }

    /**
     * Test return of multiple open result sets from one execute.
     */
    public void testMultipleResults() throws Exception {
        Statement stmt = con.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        //
        // Create 4 test tables
        //
        for (int rs = 1; rs <= 4; rs++) {
            stmt.execute("CREATE TABLE #TESTRS" + rs + " (id int, data varchar(255))");
            for (int row = 1; row <= 10; row++) {
                assertEquals(1, stmt.executeUpdate("INSERT INTO #TESTRS" + rs +
                        " VALUES(" + row + ", 'TABLE " + rs + " ROW " + row + "')"));
            }
        }

        assertTrue(stmt.execute(
                "SELECT * FROM #TESTRS1\r\n" +
                "SELECT * FROM #TESTRS2\r\n" +
                "SELECT * FROM #TESTRS3\r\n" +
                "SELECT * FROM #TESTRS4\r\n"));
        ResultSet rs = stmt.getResultSet();
        assertTrue(rs.next());
        assertEquals("TABLE 1 ROW 1", rs.getString(2));
        // Get RS 2 keeping RS 1 open
        assertTrue(stmt.getMoreResults(Statement.KEEP_CURRENT_RESULT));
        ResultSet rs2 = stmt.getResultSet();
        assertTrue(rs2.next());
        assertEquals("TABLE 2 ROW 1", rs2.getString(2));
        // Check RS 1 still open and on row 1
        assertEquals("TABLE 1 ROW 1", rs.getString(2));
        // Read a cached row from RS 1
        assertTrue(rs.next());
        assertEquals("TABLE 1 ROW 2", rs.getString(2));
        // Close RS 2 but keep RS 1 open and get RS 3
        assertTrue(stmt.getMoreResults(Statement.CLOSE_CURRENT_RESULT));
        ResultSet rs3 = stmt.getResultSet();
        assertTrue(rs3.next());
        assertEquals("TABLE 3 ROW 1", rs3.getString(2));
        // Check RS 2 is closed
        try {
            assertEquals("TABLE 2 ROW 1", rs2.getString(2));
            fail("Expected RS 2 to be closed!");
        } catch (SQLException e) {
            // Ignore
        }
        // Check RS 1 is still open
        assertEquals("TABLE 1 ROW 2", rs.getString(2));
        // Close all result sets and get RS 4
        assertTrue(stmt.getMoreResults(Statement.CLOSE_ALL_RESULTS));
        ResultSet rs4 = stmt.getResultSet();
        assertTrue(rs4.next());
        assertEquals("TABLE 4 ROW 1", rs4.getString(2));
        // check RS 1 is now closed as well
        try {
            assertEquals("TABLE 1 ROW 2", rs.getString(2));
            fail("Expected RS 1 to be closed!");
        } catch (SQLException e) {
            // Ignore
        }
        assertFalse(stmt.getMoreResults());
        stmt.close();
    }
}
