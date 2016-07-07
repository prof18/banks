package com.digifarm.Graph;

import com.digifarm.DBConnection.ConnectionDB;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by nihilus on 07/07/16.
 **/
public class Utility {

    /**
     *  This method will create the Graph to be used by the algorithm; it will query the database for
     *  all tables and it will extract all the known tuples (saving in the Node objects the search_id).
     *
     *  @param dbConn An instance of ConnectionDB.
     *  @see ConnectionDB
     *
     **/
    public static ArrayList<Node> createGraph(ConnectionDB dbConn) {
        ArrayList<Node> set = new ArrayList<Node>();
        //query the database to know tables names
        Connection conn = dbConn.getDBConnection();
        Statement stmn = null, stmn2 = null;
        try {
            stmn = conn.createStatement();
            stmn2 = conn.createStatement();
            ResultSet tuple;
            ResultSet table = stmn.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_CATALOG='mondial'");
            String tableName;
            System.out.println("\nCreating graph from DB. Please Wait...\n-----------------" );
            while(table.next()) {
                tableName = table.getString(1);
                if(!tableName.startsWith("pg_") && !tableName.startsWith("sql_")) {
                    //now we know the table name, we need to extract the search_id for each tuple
                    tuple = stmn2.executeQuery("SELECT __search_id FROM " + tableName);
                    while(tuple.next()) {
                        //creating and adding a new Node to the set
                        set.add(new Node(tuple.getInt(1), tableName));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return set;
    }

    public static ArrayList<Node> createInterestSet(ConnectionDB dbConn, ArrayList<Node> set, String match) {
        ArrayList<Node> interestSet = new ArrayList<Node>();
        Connection conn = dbConn.getDBConnection();
        try {
            Statement stmn = conn.createStatement();
            ResultSet columns = null;
            ResultSetMetaData meta = null;
            //check if a Node contains the match string
            System.out.println("\nCreating Interest Set from \"" + match + "\". Please Wait...\n-----------------" );
            for(Node n: set) {
                //extract all columns from this tuple
                columns = stmn.executeQuery("SELECT * FROM " + n.getTableName() + " WHERE __search_id = " + n.getSearchID());
                //check if a match occurs
                while(columns.next()) {
                    meta = columns.getMetaData();
                    int max = meta.getColumnCount();
                    for (int i = 1; i < max; i++) {
                        String str = columns.getString(i);
                        if (str != null && str.contains(match)) {
                            System.out.println("Matched: " + str);
                            interestSet.add(n);
                            break;
                        }
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return interestSet;
    }
}
