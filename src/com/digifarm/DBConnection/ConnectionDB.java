package com.digifarm.DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by marco on 7/7/16.
 **/
public class ConnectionDB {

    private String JDBC_DRV = "org.postgresql.Driver", DB_URL;
    private Connection conn = null;

    public ConnectionDB(String user, String pass, String addr, String port, String name) throws SQLException, ClassNotFoundException {

        //url for db connection
        DB_URL = "jdbc:postgresql://" + addr + ":" + port + "/" + name;
        //driver loading
        Class.forName(JDBC_DRV);
        //DB Connection
        conn = DriverManager.getConnection(DB_URL, user, pass);

    }

    public Connection getDBConnection() {

        return conn;

    }

    public void closeDBConnection() {
        try {

            if(conn != null) conn.close();

        } catch(SQLException se) {

            se.printStackTrace();

        } finally {

            conn = null;
        }
    }
}
