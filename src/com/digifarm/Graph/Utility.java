package com.digifarm.Graph;

import com.digifarm.DBConnection.ConnectionDB;

import java.sql.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public static HashMap<Integer, Node> createInterestSet(ConnectionDB dbConn, HashMap<Integer, Node> set, String match) {
        long before1 = System.currentTimeMillis();
        HashMap<Integer, Node> interestSet = new HashMap<Integer, Node>();
        Connection conn = dbConn.getDBConnection();
        try {
            Statement stmn = conn.createStatement();
            ResultSet columns = null;
            ResultSetMetaData meta = null;
            Node n;
            //check if a Node contains the match string
            System.out.println("\nCreating Interest Set for \"" + match + "\". Please Wait...\n-----------------" );
            for(Map.Entry<Integer, Node> e : set.entrySet()) {
                n = e.getValue();
                //extract all columns from this tuple
                columns = stmn.executeQuery("SELECT * FROM " + n.getTableName() + " WHERE __search_id = " + n.getSearchID());
                //check if a match occurs
                while(columns.next()) {
                    meta = columns.getMetaData();
                    int max = meta.getColumnCount();
                    for (int i = 1; i < max; i++) {
                        String str = columns.getString(i);
                        if (str != null && isContained(str.toLowerCase(), match.toLowerCase())) {
                            System.out.println("Matched: " + str);
                            interestSet.put(n.getSearchID(), n);
                            break;
                        }
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        long after1 = System.currentTimeMillis();
        long time1 = (after1 - before1)/1000;
        System.out.println("Interest Set Built in: " + time1 + " seconds");
        return interestSet;
    }

    /**
     *
     * @param s1    The main string on which s2 is searched
     * @param s2    The string to be searched inside s1
     * @return      If s2 is found inside s1 the return value is true
     */

    private static boolean isContained(String s1, String s2) {
        for (String word : s1.split("\\s+")) {
            if (word.equals(s2))
                return true;
        }
        return false;
    }

    /**
     *  This method connect the nodes of the set provided by input
     *
     * @param   dBconn          An instance of ConnectionDB
     * @see     ConnectionDB
     * @param nodeList          A set of Nodes that needs to be connected
     */

    public static ArrayList<ArrayList<Edge>> connectNodes(ConnectionDB dBconn, HashMap<Integer, Node> interestSet, HashMap<Integer, Node> nodeList) {

        long before = System.currentTimeMillis();

        ArrayList<String> queries;
        ArrayList<Edge> edges = new ArrayList<>();
        ArrayList<Edge> backedges = new ArrayList<>();


        HashMap <String, ArrayList<String>> sqlKey = foreignKeyTable(dBconn);
        Node n;

        Connection conn = dBconn.getDBConnection();
        Statement stm;
        ResultSet rs;

        for (Map.Entry<Integer, Node> e : interestSet.entrySet()) {
            n = e.getValue();
            queries = sqlKey.get(n.getTableName());
            if(queries != null && !queries.isEmpty()) {
                for (String q : queries) {
                    try {
                        q += " WHERE t1.__search_id = " + n.getSearchID();

                        stm = conn.createStatement();
                        rs = stm.executeQuery(q);

                        //extract the list of adjacent nodes
                        Node connected;
                        Edge edge;
                        Edge backedge;
                        while(rs.next()) {
                            connected = nodeList.get(new Integer(rs.getString(2)));
                            System.out.println(n.getTableName() + "->" + n.getSearchID() + " : " + connected.getTableName() + "->" + connected.getSearchID());
                            n.addAdjacentNode(connected);
                            edge = new Edge(n,connected,1);
                            edges.add(edge);
                            backedge = new Edge(connected,n,0);
                            backedges.add(backedge);

                        }
                    } catch (SQLException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }

        long after = System.currentTimeMillis();

        System.out.println("Nodes connected in: " + (after - before) / 1000 + " seconds");

        ArrayList<ArrayList<Edge>> list = new ArrayList<>();
        list.add(edges);
        list.add(backedges);
        return list;


    }

    /**
     *
     * @param connDB
     * @return
     */
    private static HashMap<String, ArrayList<String>> foreignKeyTable (ConnectionDB connDB) {
        HashMap<String, ArrayList<String>> keyTable = new HashMap<>();
        Connection conn = connDB.getDBConnection();
        Statement stm, stm1;
        ResultSet rs, rs1;
        try {
            stm = conn.createStatement();
            //this query retrieves the source table, the destination table, the foreign key, the primary key
            //and conrelid
            rs = stm.executeQuery("SELECT c1.relname AS src_table, c3.conrelid, c3.conkey AS foreign_key, c2.relname AS dst_table, c3.confrelid, c3.confkey AS primary_key FROM pg_constraint AS c3 INNER JOIN pg_class AS c1 ON c3.conrelid = c1.oid INNER JOIN pg_class AS c2 ON c3.confrelid = c2.oid WHERE confrelid <> 0;\n");
            String fromTable, toTable;
            Integer[] fk,pk;
            Array a;

            while (rs.next()) {
                fromTable = rs.getString(1);
                toTable = rs.getString(4);
                a = rs.getArray(3);
                fk = (Integer[]) a.getArray();
                a = rs.getArray(6);
                pk = (Integer[]) a.getArray();

                ArrayList<String> colName1  = new ArrayList<>();
                ArrayList<String> colName2 = new ArrayList<>();

                stm1 = conn.createStatement();
                for (Integer i : fk) {
                    rs1 = stm1.executeQuery("SELECT a1.attname FROM pg_attribute AS a1  WHERE a1.attnum = " + i + " AND a1.attrelid = " + rs.getString(2));
                    while(rs1.next()) {
                        colName1.add(rs1.getString(1));
                    }
                }

                stm1 = conn.createStatement();
                for (Integer i : pk) {
                    rs1 = stm1.executeQuery("SELECT a1.attname FROM pg_attribute AS a1  WHERE a1.attnum = " + i + " AND a1.attrelid = " + rs.getString(5));
                    while(rs1.next()) {
                        colName2.add(rs1.getString(1));
                    }
                }

                String joinCondition = "t1." + colName1.remove(0) + " = t2." + colName2.remove(0);
                while(!colName1.isEmpty()) {
                    joinCondition += " AND t1." + colName1.remove(0) + " = t2." + colName2.remove(0);
                }

                String query = "SELECT " + "t1.__search_id," +  "t2.__search_id FROM " + fromTable + " AS t1 INNER JOIN " + toTable + " AS t2 ON " + joinCondition;
                //System.out.println(query+ ";");

                if (keyTable.containsKey(fromTable)) {
                    ArrayList<String> list = keyTable.get(fromTable);
                    list.add(query);
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(query);
                    keyTable.put(fromTable, list);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return keyTable;
    }

    public static void backEdgePoint(ArrayList<Edge> bedge, ArrayList<Edge> edge) {
        //il punteggio di un backedge (v,u) è proporzionale al numero di link da v ad u
        Node v,u;
        int weight = 0;
        for (Edge be : bedge ) {

            v = be.getFrom();
            u = be.getTo();

            for (Edge e : edge) {

                if (u == e.getFrom()) {
                    System.out.println("u: " + u.getSearchID() + " e: " + e.getFrom().getSearchID());

                    if (v == e.getTo()) {
                        System.out.println("v: " + v.getSearchID() + " e: " + e.getTo().getSearchID());
                        System.out.println("Old weight " + be.getWeight());
                        be.setWeight(be.getWeight() + 1);
                        System.out.println("New weight: " + be.getWeight());
                    }
                }


            }
            System.out.println("Backedge point: " + be.toString());

        }

    }
}
