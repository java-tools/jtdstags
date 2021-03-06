package net.sourceforge.jtds.test;

import java.sql.*;
import junit.framework.TestCase;

/**
 * @version 1.0
 */
public class CallableStatementTest extends TestBase {
    public CallableStatementTest(String name) {
        super(name);
    }

    public void testCallableStatement() throws Exception {
        CallableStatement stmt = con.prepareCall("{call sp_who}");

        stmt.close();
    }

    public void testCallableStatement1() throws Exception {
        CallableStatement stmt = con.prepareCall("sp_who");

        makeTestTables(stmt);
        makeObjects(stmt, 8);

        ResultSet rs = stmt.executeQuery();

        dump(rs);

        stmt.close();
        rs.close();
    }

    public void testCallableStatementCall1() throws Exception {
        CallableStatement stmt = con.prepareCall("{call sp_who}");

        makeTestTables(stmt);
        makeObjects(stmt, 8);

        ResultSet rs = stmt.executeQuery();

        dump(rs);

        stmt.close();
        rs.close();
    }

    public void testCallableStatementCall2() throws Exception {
        CallableStatement stmt = con.prepareCall("{CALL sp_who}");

        makeTestTables(stmt);
        makeObjects(stmt, 8);

        ResultSet rs = stmt.executeQuery();

        dump(rs);

        stmt.close();
        rs.close();
    }

    public void testCallableStatementCall3() throws Exception {
        CallableStatement stmt = con.prepareCall("{cAlL sp_who}");

        makeTestTables(stmt);
        makeObjects(stmt, 8);

        ResultSet rs = stmt.executeQuery();

        dump(rs);

        stmt.close();
        rs.close();
    }

    public void testCallableStatementExec1() throws Exception {
        CallableStatement stmt = con.prepareCall("exec sp_who");

        makeTestTables(stmt);
        makeObjects(stmt, 8);

        ResultSet rs = stmt.executeQuery();

        dump(rs);

        stmt.close();
        rs.close();
    }

    public void testCallableStatementExec2() throws Exception {
        CallableStatement stmt = con.prepareCall("EXEC sp_who");

        makeTestTables(stmt);
        makeObjects(stmt, 8);

        ResultSet rs = stmt.executeQuery();

        dump(rs);

        stmt.close();
        rs.close();
    }

    public void testCallableStatementExec3() throws Exception {
        CallableStatement stmt = con.prepareCall("execute sp_who");

        makeTestTables(stmt);
        makeObjects(stmt, 8);

        ResultSet rs = stmt.executeQuery();

        dump(rs);

        stmt.close();
        rs.close();
    }

    public void testCallableStatementExec4() throws Exception {
        CallableStatement stmt = con.prepareCall("EXECUTE sp_who");

        makeTestTables(stmt);
        makeObjects(stmt, 8);

        ResultSet rs = stmt.executeQuery();

        dump(rs);

        stmt.close();
        rs.close();
    }

    public void testCallableStatementExec5() throws Exception {
        CallableStatement stmt = con.prepareCall("eXeC sp_who");

        makeTestTables(stmt);
        makeObjects(stmt, 8);

        ResultSet rs = stmt.executeQuery();

        dump(rs);

        stmt.close();
        rs.close();
    }

    public void testCallableStatementExec6() throws Exception {
        CallableStatement stmt = con.prepareCall("ExEcUtE sp_who");

        makeTestTables(stmt);
        makeObjects(stmt, 8);

        ResultSet rs = stmt.executeQuery();

        dump(rs);

        stmt.close();
        rs.close();
    }

    public void testCallableStatementExec7() throws Exception {
        CallableStatement stmt = con.prepareCall("execute \"master\"..sp_who");

        makeTestTables(stmt);
        makeObjects(stmt, 8);

        ResultSet rs = stmt.executeQuery();

        dump(rs);

        stmt.close();
        rs.close();
    }

    public void testCallableStatementExec8() throws Exception {
        Statement stmt;

        try {
            stmt = con.createStatement();
            stmt.execute("create procedure _test as SELECT COUNT(*) FROM sysobjects");
            stmt.close();

            CallableStatement cstmt = con.prepareCall("execute _test");

            makeTestTables(cstmt);
            makeObjects(cstmt, 8);

            ResultSet rs = cstmt.executeQuery();

            dump(rs);

            cstmt.close();
            rs.close();
        } finally {
            stmt = con.createStatement();
            stmt.execute("drop procedure _test");
            stmt.close();
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(CallableStatementTest.class);
    }
}