/*
*   Copyright 2017 Marco Gomiero, Luca Rossi
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
*/

package com.digifarm.DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by digifarmer on 7/7/16.
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
