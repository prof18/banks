package com.digifarm.Graph;

import com.digifarm.DBConnection.ConnectionDB;
import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;
import com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nihilus on 07/07/16.
 **/
public class Utility {

    /**
     *  This method will create the Graph to be used by the algorithm; it will query the database for
     *  all tables and it will extract all the known tuples (saving in the Node objects the search_id).
     *
     *  @param  dbConn          An instance of ConnectionDB.
     *  @see    ConnectionDB
     *  @return set             A set of Nodes
     *
     **/
    public static HashMap<Integer, Node> createGraph(ConnectionDB dbConn, String DBName) {
        HashMap<Integer, Node> set = new HashMap<Integer, Node>();
        //query the database to know tables names
        Connection conn = dbConn.getDBConnection();
        Statement stmn = null, stmn2 = null;
        try {
            stmn = conn.createStatement();
            stmn2 = conn.createStatement();
            ResultSet tuple;
            ResultSet table = stmn.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_CATALOG='" + DBName + "' ORDER BY TABLE_NAME;");
            String tableName;
            System.out.println("\nCreating graph from DB. Please Wait...\n-----------------" );
            int id;
            long before = System.currentTimeMillis();
            while(table.next()) {
                tableName = table.getString(1);
                if(!tableName.startsWith("pg_") && !tableName.startsWith("sql_")) {
                    //now we know the table name, we need to extract the search_id for each tuple
                    tuple = stmn2.executeQuery("SELECT __search_id FROM " + tableName);
                    while(tuple.next()) {
                        //creating and adding a new Node to the set
                        //set.add(new Node(tuple.getInt(1), tableName));
                        id = tuple.getInt(1);
                        set.put(id, new Node(id,tableName));
                    }
                }
            }
            long after = System.currentTimeMillis();
            long time = (after - before);
            System.out.println("Database Created in: " + time + " millis\n-----------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return set;
    }

    /**
     *  This method create a set of interest nodes filtered by keywords
     *
     * @param   dbConn          An instance of ConnectionDB
     * @see     ConnectionDB
     * @param   set             A set of Node
     * @param   match           Keyword
     * @return  interestSet     The interest set of node
     */
    //TODO FIX COINTAINS AND IGNORE CASE
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

    /**
     *  This method connect the nodes of the set provided by input
     *
     * @param   dBconn          An instance of ConnectionDB
     * @see     ConnectionDB
     * @param nodeList          A set of Nodes that needs to be connected
     */

    public static void connectNodes(ConnectionDB dBconn, ArrayList<Node> nodeList) {
        ArrayList<Edge> edges = new ArrayList<>();
        ArrayList<Edge> backedge = new ArrayList<>();
        ArrayList<Node> adjacent = new ArrayList<>();

        Connection conn = dBconn.getDBConnection();
        Statement stm;
        Statement stm2;

        try {

            stm = conn.createStatement();
            stm2 = conn.createStatement();
            ResultSet idTo;

            //table name of the foreign key
            String fromTable;
            //foreign key
            String fromKey;
            //table name of table referred by the foreign key
            String toTable;
            //primary key of "toTable"
            String toKey;
            //tuple's search_id of fromTable
            int nIdFrom;
            //tuple's search_id of toTable
            int nIdTo;

            System.out.println("\nCreating Edges. Please Wait...\n-----------------" );

            for (Node n : nodeList ) {

                //bad query

                ResultSet keys = stm.executeQuery("SELECT source_table::regclass, source_attr.attname AS source_column,\n" +
                        "\ttarget_table::regclass, target_attr.attname AS target_column\n" +
                        "FROM pg_attribute target_attr, pg_attribute source_attr,\n" +
                        "\t(SELECT source_table, target_table, source_constraints[i] source_constraints, target_constraints[i] AS target_constraints\n" +
                        "\tFROM\n" +
                        "\t(SELECT conrelid as source_table, confrelid AS target_table, conkey AS source_constraints, confkey AS target_constraints,\n" +
                        "\tgenerate_series(1, array_upper(conkey, 1)) AS i\n" +
                        "\tFROM pg_constraint\n" +
                        "\tWHERE contype = 'f'\n" +
                        "  ) query1\n" +
                        " ) query2\n" +
                        "WHERE target_attr.attnum = target_constraints AND target_attr.attrelid = \t target_table AND\n" +
                        "\tsource_attr.attnum = source_constraints AND source_attr.attrelid = source_table ORDER BY source_table;");

               /* ResultSet keys = stm.executeQuery("SELECT\n" +
                        "    tc.table_name, kcu.column_name, \n" +
                        "    ccu.table_name AS foreign_table_name,\n" +
                        "    ccu.column_name AS foreign_column_name \n" +
                        "FROM \n" +
                        "    information_schema.table_constraints AS tc \n" +
                        "    JOIN information_schema.key_column_usage AS kcu\n" +
                        "      ON tc.constraint_name = kcu.constraint_name\n" +
                        "    JOIN information_schema.constraint_column_usage AS ccu\n" +
                        "      ON ccu.constraint_name = tc.constraint_name\n" +
                        "WHERE constraint_type = 'FOREIGN KEY' AND tc.table_name='" +
                        n.getTableName() + "';");*/

               // System.out.println("Creating Edges from " + n.getTableName());

                while (keys.next()) {

                    fromTable  = keys.getString(1);
                    fromKey = keys.getString(2);
                    toTable = keys.getString(3);
                    toKey = keys.getString(4);
                    System.out.println("FromTable; " + fromTable+" | fromKey: " + fromKey + " | toTable: " + toTable + " | toKey: " + toKey);

                    idTo = stm2.executeQuery("SELECT " +  "t1.__search_id," +  "t2.__search_id FROM " + fromTable + " AS t1 INNER JOIN " + toTable +
                            " AS t2 ON " + "t1." + fromKey + " = " + "t2." + toKey) ;

                    while (idTo.next()) {

                        nIdFrom = idTo.getInt(1);
                        nIdTo = idTo.getInt(2);

                       // System.out.println("Reference from: " + nIdFrom + " to " + nIdTo);

                        /**
                         *  TODO:
                         *  - get a node from search id;
                         *  - create a new Edge from nIdFrom and nIdTo and add it to the Edge's list;
                         *  - do the same for the backedge
                         *  - add nIdTo to the list of adjcent Node of nIdFrom (a list is correct??)
                         */
                    }
                }
            }

        } catch (SQLException e ) {
            e.printStackTrace();
        }
    }
}
