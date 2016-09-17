package com.digifarm.Graph;

import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Tree.Tree;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by digifarmer on 07/07/16.
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
        HashMap<Integer, Node> interestSet = new HashMap<>();
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
                            n.setKeywordNode(true);
                            n.addKeyword(match);
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
        System.out.println("Interest Set Built in: " + time1 + " seconds\n");
        return interestSet;
    }

    /**
     *
     * @param s1    The main string on which s2 is searched
     * @param s2    The string to be searched inside s1
     * @return      If s2 is found inside s1 the return value is true
     */

    private static boolean isContained(String s1, String s2) {
        //split string by whitespace
        for (String word : s1.split("\\s+")) {
            if (word.equals(s2))
                return true;
        }
        return false;
    }


    /**
     *  This method connect the nodes of the set provided by input
     *
     * @param dBconn             An instance of ConnectionDB
     * @see   ConnectionDB
     * @param interestSet       The interest set that has to be connected
     * @param nodeList          The "global" node list
     * @return                  An ArrayList of edge and backedge
     */

    public static ArrayList<ArrayList<Edge>> connectNodes(ConnectionDB dBconn, HashMap<Integer, Node> interestSet, HashMap<Integer, Node> nodeList) {

        long before = System.currentTimeMillis();

        ArrayList<String> queries;
        ArrayList<Edge> edges = new ArrayList<>();
        ArrayList<Edge> backedges = new ArrayList<>();
        ArrayList<Node> toAdd = new ArrayList<>();

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

                            connected = nodeList.get(rs.getInt(2));
                            System.out.println(n.getTableName() + "->" + n.getSearchID() + " : " + connected.getTableName() + "->" + connected.getSearchID());
                            n.addAdjacentNode(connected);
                            edge = new Edge(n,connected,1);
                            edges.add(edge);
                            backedge = new Edge(connected,n,0);
                            backedges.add(backedge);
                            //assign score to the node --> indegree of the node
                            connected.incrementScore();
                            //the destination node could not be in the interest set. It isn't a keyword node
                            if (!interestSet.containsKey(rs.getInt(2))) {
                                toAdd.add(connected);
                            }
                            connected.addAdjacentNode(n);
                        }
                    } catch (SQLException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }

        for (Node node : toAdd) {
            interestSet.put(node.getSearchID(), node);
            System.out.println("Not keyword node: " + node);
        }

        long after = System.currentTimeMillis();

        System.out.println("Nodes connected in: " + (after - before) / 1000 + " seconds\n");

        ArrayList<ArrayList<Edge>> list = new ArrayList<>();
        list.add(edges);
        list.add(backedges);
        return list;
    }

    /**
     *
     *
     * @param connDB            An instance of ConnectionDB
     * @see   ConnectionDB
     * @return                  An Hash map with key the fromTable and the value the query that retrieves node connections
     */
    private static HashMap<String, ArrayList<String>> foreignKeyTable (ConnectionDB connDB) {
        HashMap<String, ArrayList<String>> keyTable = new HashMap<>();
        Connection conn = connDB.getDBConnection();
        Statement stm, stm1;
        ResultSet rs, rs1;
        try {
            stm = conn.createStatement();
            //this query retrieves the source table, the destination table, the foreign key, the primary key
            //and conrelid (The table this constraint is on; 0 if not a table constraint)
            rs = stm.executeQuery("SELECT c1.relname AS src_table, c3.conrelid, c3.conkey AS foreign_key, c2.relname " +
                    "AS dst_table, c3.confrelid, c3.confkey AS primary_key FROM pg_constraint AS c3 INNER JOIN pg_class AS c1 " +
                    "ON c3.conrelid = c1.oid INNER JOIN pg_class AS c2 ON c3.confrelid = c2.oid WHERE confrelid <> 0;\n");
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
                    rs1 = stm1.executeQuery("SELECT a1.attname FROM pg_attribute AS a1  WHERE a1.attnum = " + i +
                            " AND a1.attrelid = " + rs.getString(2));
                    while(rs1.next()) {
                        colName1.add(rs1.getString(1));
                    }
                }

                stm1 = conn.createStatement();
                for (Integer i : pk) {
                    rs1 = stm1.executeQuery("SELECT a1.attname FROM pg_attribute AS a1  WHERE a1.attnum = " + i +
                            " AND a1.attrelid = " + rs.getString(5));
                    while(rs1.next()) {
                        colName2.add(rs1.getString(1));
                    }
                }

                String joinCondition = "t1." + colName1.remove(0) + " = t2." + colName2.remove(0);
                while(!colName1.isEmpty()) {
                    joinCondition += " AND t1." + colName1.remove(0) + " = t2." + colName2.remove(0);
                }

                String query = "SELECT " + "t1.__search_id," +  "t2.__search_id FROM " + fromTable + " AS t1 INNER JOIN " +
                        toTable + " AS t2 ON " + joinCondition;

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

    /**
     * This method calculates the weight of the backedges
     *
     */

    public static void backEdgePoint(ArrayList<Edge> bedge) {
        //the point of an backedge is proportional to number of link to v from nodes of the same type as u
        Node to, from;
        ArrayList<Node> adjacent = new ArrayList<>();
        String table;

        for (Edge be : bedge) {

            to = be.getFrom();   //finish       --> v
            from = be.getTo();   //start     --> u

            table = from.getTableName();
            adjacent = to.getAdjacentNodes();

            for (Node n : adjacent) {
                //Node n is of the same type of u, i.e. belongs to the same table
                if ((n.getTableName()).compareTo(table) == 0) {
                    be.setScore(be.getScore() + 1);
                }
            }
        }
    }

    /**
     *
     *
     * @param nodes
     * @return
     */

    /**
     * This method calculate the maximus value of the node score
     *
     * @param nodes     Hash Map of nodes
     * @param max       The actual max value
     * @return          The max value
     */
    public static double maxNodeScore(HashMap<Integer,Node> nodes, double max) {
        Node n;
        double score;

        for (Map.Entry<Integer, Node> e : nodes.entrySet()) {
            n = e.getValue();
            score = n.getScore();
            if (score > max)
                max = score;
        }
        return max;
    }

    /**
     * This method calculate the minimum value of the edge weight
     *
     * @param edges     List of nodes
     * @param min       The actual min value
     * @return          The min value
     */
    public static double minEdgeWeight(ArrayList<Edge> edges, double min) {

        double weight;
        for (Edge e : edges) {

            weight = e.getScore();
            if (weight < min)
                min = weight;
        }

        return min;
    }

    /**
     * This method normalize the score of the nodes
     *
     * @param nodes         Hash Map of nodes
     * @param type          "fraction" for linear scale or "logarithm" for logarithm scale
     * @param maxScore      The max score for normalization
     */
    public static void nScoreNorm(HashMap<Integer, Node> nodes, String type, double maxScore) {

        double score;
        double normScore;
        Node n;
        for (Map.Entry<Integer, Node> e : nodes.entrySet()) {
            n = e.getValue();
            score = n.getScore();

            switch(type) {

                case "fraction" :
                    normScore = score / maxScore;
                    n.setScore(normScore);
                    break;

                case "logarithm" :
                    normScore = (Math.log(1 + score/maxScore)) / Math.log(2);
                    n.setScore(normScore);
                    break;
            }
        }
    }

    /**
     * This method normalize the weight of the edge
     *
     * @param edges         List of edges
     * @param minWeight     The minimum score for normalization
     */
    public static void eWeightNorm(ArrayList<Edge> edges, double minWeight) {
        double weight;
        double normWeight;
        for (Edge e : edges) {
            weight = e.getScore();
            normWeight = (Math.log(1 + weight/minWeight)) / Math.log(2);
            e.setScore(normWeight);
        }
    }

    /**
     *  The overall score is computed considering the root and the leaves
     *
     * @param tree
     */
    public static void overallNodeScore(Tree tree) {

        double NScore = 0;
        double i = 0;
        NScore += tree.getRoot().getScore();
        i++;
        ArrayList<Node> sons;
        for (Map.Entry<Node, ArrayList<Node>> e: tree.getSons().entrySet() ){
            sons = e.getValue();
            if (sons.isEmpty() && e.getKey().equals(tree.getRoot())) {
                break;
            } else if (sons.isEmpty()){
                NScore += e.getKey().getScore();
                i++;
            }
        }
        double mean = NScore/i;
        tree.setNodeScore(mean);
    }

    /**
     *  The overall edge score is  1/(1+"sum of"escore
     *
     * @param edges
     * @param tree
     */
    public static void overallEdgeScore(ArrayList<Edge> edges, Tree tree) {

        double sum = 0;
        double i = 0;
        for (Edge e : edges ) {
            for (Map.Entry<Node,ArrayList<Node>> entry : tree.getSons().entrySet()) {
                if (entry.getKey() == e.getFrom()) {
                    for (Node n : entry.getValue()) {
                        if (n == e.getTo()) {
                            sum += e.getScore();
                            i++;
                        }
                    }
                }
            }
        }
        double score = 1/(1+sum);
        tree.setEdgeScore(score);
    }

    /***
     *
     * @param tree
     * @param lambda
     * @param type          The type of computing. "multiplication" or "addition"
     */
    public static void globalScore(Tree tree, double lambda, String type) {

       double globalScore = -1;

        switch (type) {

            case "multiplication" :
                globalScore = tree.getEdgeScore()* Math.pow(tree.getNodeScore(),lambda);
                break;
            case "addition" :
                globalScore = (1-lambda)*tree.getEdgeScore() + lambda*tree.getNodeScore();
                break;
        }

        tree.setGlobalScore(globalScore);

    }
}
