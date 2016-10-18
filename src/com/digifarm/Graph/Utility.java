package com.digifarm.Graph;

import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Tree.Tree;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

//TODO CLOSE DB CONNECTION
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
    public static void createGraph(ConnectionDB dbConn, String DBName, dbInfo info) {

        HashMap<Integer, Node> set = new HashMap<>();
        Connection conn = dbConn.getDBConnection();
        Statement stmn, stmn2, stm3;
        ArrayList<String> tableList = new ArrayList<>();

        try {

            stmn = conn.createStatement();
            stmn2 = conn.createStatement();
            stm3 = conn.createStatement();
            ResultSet tuple, column;
            String tableName;
            int id;
            String columnName;
            long before = System.currentTimeMillis();
            //query the database to know tables names
            ResultSet table = stmn.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_CATALOG='" + DBName + "' ORDER BY TABLE_NAME;");
            System.out.println("\nCreating graph from DB. Please Wait...\n-----------------" );

            while(table.next()) {

                tableName = table.getString(1);
                if(!tableName.startsWith("pg_") && !tableName.startsWith("sql_")) {
                    //populate table list
                    tableList.add(tableName);
                    //now we know the table name, we need to extract the search_id for each tuple
                    tuple = stmn2.executeQuery("SELECT __search_id FROM " + tableName);
                    //populate set of nodes
                    while(tuple.next()) {
                        id = tuple.getInt(1);
                        set.put(id, new Node(id,tableName));
                    }
                }
            }
            //set the table list
            info.setTableList(tableList);
            long after = System.currentTimeMillis();
            long time = (after - before);
            System.out.println("Database Created in: " + time + " millis\n-----------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        info.setNodes(set);
    }

    /**
     *  This method create a set of interest nodes filtered by keywords
     *
     * @param   dbConn          An instance of ConnectionDB
     * @see     ConnectionDB
     *
     * @param   match           Keyword
     * @return  interestSet     The interest set of node
     */

    public static HashMap<Integer, Node> createInterestSet(ConnectionDB dbConn, String match, dbInfo info) {

        long before1 = System.currentTimeMillis();
        HashMap<Integer, Node> interestSet = new HashMap<>();
        Connection conn = dbConn.getDBConnection();
        //get the node set
        HashMap<Integer, Node> set = info.getNodes();

        try {

            Statement stmn = conn.createStatement();
            ResultSet columns;
            ResultSetMetaData meta;
            Node n;
            System.out.println("\nCreating Interest Set for \"" + match + "\". Please Wait...\n-----------------" );

            //check if a Node contains the match string
            for(Map.Entry<Integer, Node> e : set.entrySet()) {

                n = e.getValue();
                //extract all columns from this tuple
                columns = stmn.executeQuery("SELECT * FROM " + n.getTableName() + " WHERE __search_id = " + n.getSearchID());
                //check if a match occurs
                while (columns.next()) {
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
     * This method check if the sentence's words is contained in another sentence
     *
     * @param s1    The main string on which s2 is searched
     * @param s2    The string to be searched inside s1
     * @return      If s2 is found inside s1 the return value is true
     */

    public static boolean isContained(String s1, String s2) {

        //split on word boundaries
        String[] splited = s1.split("\\W+");
        String[] splited2 = s2.split("\\W+");

        int i = 0;
        //string to check if a complete word is matched. For example "the darkness" doesn't match "the [...] the"
        String prec = null;
        for (String string : splited2) {

            for(String string1 : splited) {

                if (string.toLowerCase().compareTo(string1.toLowerCase()) == 0) {
                    if ((prec == null) || (string.toLowerCase().compareTo(prec.toLowerCase()) != 0)) {
                        i++;
                        prec = string;
                    }
                }
            }
        }
        if (i == splited2.length)
            return true;

        return false;
    }

    public static void connect(Node from, Node to, HashMap<Integer, Node> globalNodeList, ArrayList<Edge> globalEdgeList,
                        ArrayList<Edge> globalBEdgeList) {

        from.addAdjacentNode(to);
        to.addAdjacentNode(from);
        to.incrementScore();
        //if the node is present yet, merge
        if (globalNodeList.containsKey(from.getSearchID())) {
            globalNodeList.get(from.getSearchID()).mergeNode(from.getAdjacentNodes(),
                    from.getKeywordList());
        } else {
            globalNodeList.put(from.getSearchID(), from);
        }
        Edge edge = new Edge(from, to, 1);
        to.incrementScore();
        //if the edge is already in the globalList, skip the adding
        if (!globalEdgeList.contains(edge)) {
            globalEdgeList.add(edge);
            Edge bedge = new Edge(to, from, 0);
            globalBEdgeList.add(bedge);
        }
    }

    /**
     *  this method finds the forward nodes of the nodes in the interest set
     *
     * @param dBconn            An instance of ConnectionDB
     * @see   ConnectionDB
     * @param interestSet       The intereset set of nodes
     * @param set               The set of nodes
     * @param level             The wrapper of the level of navigation
     */
    public static void findForwardInterest(ConnectionDB dBconn, HashMap<Integer,Node> interestSet, HashMap<Integer,Node> set,
                                           Levels level) {

        //key --> starting node, value --> destination node. In this case value is the forward node of the key
        HashMap<Node,ArrayList<Node>> forwardMap = level.getForward();
        //TODO: ADD COMMENT AFTER REVIEWING FOREINGKEYTABLE
        HashMap <String, ArrayList<String>> sqlKey = foreignKeyTable(dBconn);
        ArrayList<String> queries;

        Connection conn = dBconn.getDBConnection();
        Statement statement;
        ResultSet resultSet;

        Node n;

        for (Map.Entry<Integer,Node> entry : interestSet.entrySet()) {

            n = entry.getValue();
            queries = sqlKey.get(n.getTableName());
            if (queries != null && !queries.isEmpty()) {

                for (String q : queries) {

                    try {

                        //check if there is a foreign key
                        q += " WHERE t1.__search_id = " + n.getSearchID();

                        statement = conn.createStatement();
                        resultSet = statement.executeQuery(q);

                        while (resultSet.next()) {

                            Node connected = set.get(resultSet.getInt(2));
                            if (forwardMap.containsKey(n)) {
                                if (forwardMap.get(n) != null && !forwardMap.get(n).contains(connected))
                                    forwardMap.get(n).add(connected);
                            } else {
                                ArrayList<Node> list = new ArrayList<>();
                                list.add(connected);
                                forwardMap.put(n,list);
                            }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     *  serve a trovare i backward dell'interestset
     *
     * @param dBconn
     * @param interestSet
     * @param set
     * @param info
     * @param level
     */
    public static void findBackwardInterest(ConnectionDB dBconn, HashMap<Integer,Node> interestSet, HashMap<Integer,Node> set,
                                            dbInfo info, Levels level) {

        HashMap<Node,ArrayList<Node>> backwardMap = level.getBackward();

        HashMap <String, ArrayList<String>> sqlKey = foreignKeyTable(dBconn);
        ArrayList<String> queries;

        Connection conn = dBconn.getDBConnection();
        Statement statement;
        ResultSet resultSet;

        Node n;

        for (Map.Entry<Integer,Node> entry : interestSet.entrySet()) {

            n = entry.getValue();

            for(String table : info.getTableList()) {

                queries = sqlKey.get(table);
                if (queries != null && !queries.isEmpty()) {
                    //ciclo solo se ci sono query disponibili
                    for (String s : queries) {

                        try {
                            s += " WHERE t2.__search_id = '" + n.getSearchID() + "'";

                            statement = conn.createStatement();
                            resultSet = statement.executeQuery(s);

                            while (resultSet.next()) {

                                Node connected = set.get(resultSet.getInt(1));

                                if (backwardMap.containsKey(connected)) {
                                    if (backwardMap.get(connected) != null && !backwardMap.get(connected).contains(n))
                                         backwardMap.get(connected).add(n);
                                } else {
                                    ArrayList<Node> list = new ArrayList<>();
                                    list.add(n);
                                    backwardMap.put(connected, list);
                                }
                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    /**
     *
     *  serve per trovare i backward di altri backward
     *
     * @param dBconn
     * @param set
     * @param backwards
     * @param level
     * @param info
     */
    public static void bFindBackward(ConnectionDB dBconn, HashMap<Integer, Node> set, HashMap<Node,ArrayList<Node>> backwards,
                              Levels level, dbInfo info) {

        HashMap<Node,ArrayList<Node>> backwardMap = level.getBackward();

        HashMap <String, ArrayList<String>> sqlKey = foreignKeyTable(dBconn);
        ArrayList<String> queries;

        Connection conn = dBconn.getDBConnection();
        Statement statement;
        ResultSet resultSet;

        Node n;

        for (Map.Entry<Node,ArrayList<Node>> entry : backwards.entrySet()) {

            n = entry.getKey();

            for(String table : info.getTableList()) {

                queries = sqlKey.get(table);
                if (queries != null && !queries.isEmpty()) {
                    //ciclo solo se ci sono query disponibili
                    for (String s : queries) {

                        try {
                            s += " WHERE t2.__search_id = '" + n.getSearchID() + "'";

                            statement = conn.createStatement();
                            resultSet = statement.executeQuery(s);

                            while (resultSet.next()) {

                                Node connected = set.get(resultSet.getInt(1));

                                if (backwardMap.containsKey(connected)) {
                                    if (backwardMap.get(connected) != null && !backwardMap.get(connected).contains(n))
                                        backwardMap.get(connected).add(n);
                                } else {
                                    ArrayList<Node> list = new ArrayList<>();
                                    list.add(n);
                                    backwardMap.put(connected, list);
                                }
                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    //bacckward of foward
    public static void fFindBackward(ConnectionDB dBconn, HashMap<Integer,Node> set, HashMap<Node,ArrayList<Node>> forward,
                                     Levels level, dbInfo info) {

        HashMap<Node,ArrayList<Node>> backwardMap = level.getBackward();

        HashMap <String, ArrayList<String>> sqlKey = foreignKeyTable(dBconn);
        ArrayList<String> queries;

        Connection conn = dBconn.getDBConnection();
        Statement statement;
        ResultSet resultSet;

        Node n;
        ArrayList<Node> nodeList = new ArrayList<>();

        for (Map.Entry<Node,ArrayList<Node>> entry : forward.entrySet()) {

            nodeList = entry.getValue();

            for (Node node : nodeList) {

                for(String table : info.getTableList()) {

                    queries = sqlKey.get(table);
                    if (queries != null && !queries.isEmpty()) {
                        //ciclo solo se ci sono query disponibili
                        for (String s : queries) {

                            try {
                                s += " WHERE t2.__search_id = '" + node.getSearchID() + "'";

                                statement = conn.createStatement();
                                resultSet = statement.executeQuery(s);

                                while (resultSet.next()) {

                                    Node connected = set.get(resultSet.getInt(1));

                                    if (backwardMap.containsKey(connected)) {
                                        if (backwardMap.get(connected) != null && !backwardMap.get(connected).contains(node))
                                            backwardMap.get(connected).add(node);
                                    } else {
                                        ArrayList<Node> list = new ArrayList<>();
                                        list.add(node);
                                        backwardMap.put(connected, list);
                                    }
                                }

                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    //trova i forward dei backward
    public static void bFindForward(ConnectionDB dBconn, HashMap<Integer,Node> set, HashMap<Node,ArrayList<Node>> backward,
                                    Levels level) {

        HashMap<Node,ArrayList<Node>> forwardMap = level.getForward();

        HashMap <String, ArrayList<String>> sqlKey = foreignKeyTable(dBconn);
        ArrayList<String> queries;

        Connection conn = dBconn.getDBConnection();
        Statement statement;
        ResultSet resultSet;

        Node n;

        for (Map.Entry<Node,ArrayList<Node>> entry : backward.entrySet()) {

            n = entry.getKey();

            queries = sqlKey.get(n.getTableName());
            if (queries != null && !queries.isEmpty()) {

                for (String q : queries) {

                    try {

                        q += " WHERE t1.__search_id = " + n.getSearchID();

                        statement = conn.createStatement();
                        resultSet = statement.executeQuery(q);

                        while (resultSet.next()) {

                            Node connected = set.get(resultSet.getInt(2));
                            if (forwardMap.containsKey(n)) {
                                if (forwardMap.get(n) != null && !forwardMap.get(n).contains(connected))
                                    forwardMap.get(n).add(connected);
                            } else {

                                ArrayList<Node> list = new ArrayList<>();
                                list.add(connected);
                                forwardMap.put(n,list);
                            }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //trova i forward dei forward
    public static void fFindForward(ConnectionDB dBconn, HashMap<Integer,Node> set, HashMap<Node,ArrayList<Node>> forward,
                                    Levels level) {

        HashMap<Node, ArrayList<Node>> forwardMap = level.getForward();

        HashMap <String, ArrayList<String>> sqlKey = foreignKeyTable(dBconn);
        ArrayList<String> queries;

        Connection conn = dBconn.getDBConnection();
        Statement statement;
        ResultSet resultSet;

        Node n;
        ArrayList<Node> nodeList = new ArrayList<>();

        for (Map.Entry<Node,ArrayList<Node>> entry : forward.entrySet()) {

            nodeList = entry.getValue();

            for (Node node : nodeList) {

                queries = sqlKey.get(node.getTableName());
                if (queries != null && !queries.isEmpty()) {

                    for (String q : queries) {

                        try {

                            q += " WHERE t1.__search_id = " + node.getSearchID();

                            statement = conn.createStatement();
                            resultSet = statement.executeQuery(q);

                            while (resultSet.next()) {

                                Node connected = set.get(resultSet.getInt(2));
                                if (forwardMap.containsKey(node)) {
                                    if (forwardMap.get(node) != null && !forwardMap.get(node).contains(connected))
                                        forwardMap.get(node).add(connected);
                                } else {

                                    ArrayList<Node> list = new ArrayList<>();
                                    list.add(connected);
                                    forwardMap.put(node,list);
                                }
                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    public static void connectInterestNodes(ConnectionDB dBconn, HashMap<Integer, Node> interestSet, ArrayList<Edge> globalEdges,
                                            ArrayList<Edge> globalBedge) {


        ArrayList<String> queries;

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

                            connected = interestSet.get(rs.getInt(2));
                            if (connected != null) {
                                //System.out.println(n.getTableName() + "->" + n.getSearchID() + " : " + connected.getTableName() + "->" + connected.getSearchID());
                                n.addAdjacentNode(connected);
                                //assign score to the node --> indegree of the node
                                edge = new Edge(n, connected, 1);
                                if (!globalEdges.contains(edge)) {
                                    globalEdges.add(edge);
                                    connected.incrementScore();
                                    backedge = new Edge(connected, n, 0);
                                    globalBedge.add(backedge);
                                }
                            }
                        }
                    } catch (SQLException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }


    }

   /*

    public static ArrayList<ArrayList<Edge>> connectNodes(ConnectionDB dBconn, HashMap<Integer, Node> interestSet,
                                        HashMap<Integer, Node> nodeList, dbInfo info, HashMap<Node, ArrayList<Node>> commonNodes) {

        long before = System.currentTimeMillis();

        ArrayList<String> queries;
        ArrayList<Edge> edges = new ArrayList<>();
        ArrayList<Edge> backedges = new ArrayList<>();
        ArrayList<Node> toAdd = new ArrayList<>();

        HashMap <String, ArrayList<String>> sqlKey = foreignKeyTable(dBconn);
        Node n;

        Connection conn = dBconn.getDBConnection();
        Statement statement, statement1, statement4, statement5;
        ResultSet resultSet, resultSet1, resultSet4, resultSet5;

        String tbl;
        int counter = 0;
        String table2;


        for (Map.Entry<Integer, Node> e : interestSet.entrySet()) {
            n = e.getValue();
            queries = sqlKey.get(n.getTableName());
            if(queries != null && !queries.isEmpty()) {
                for (String q : queries) {
                    String tmpQ = q;
                    try {
                        q += " WHERE t1.__search_id = " + n.getSearchID();

                        statement = conn.createStatement();
                        resultSet = statement.executeQuery(q);

                        //extract the list of adjacent nodes
                        Node connected;
                        Edge edge;
                        Edge backedge;

                        while(resultSet.next()) {

                            connected = nodeList.get(resultSet.getInt(2));
                            System.out.println(n.getTableName() + "->" + n.getSearchID() + " : " + connected.getTableName() + "->" + connected.getSearchID());
                            n.addAdjacentNode(connected);
                            edge = new Edge(n,connected,1);
                            edges.add(edge);
                            backedge = new Edge(connected,n,0);
                            backedges.add(backedge);
                            //assign score to the node --> indegree of the node
                            connected.incrementScore();
                            //the destination node could not be in the interest set. It isn't a keyword node
                            if (!interestSet.containsKey(resultSet.getInt(2))) {
                                toAdd.add(connected);
                            }
                            connected.addAdjacentNode(n);
                        }
                    } catch (SQLException e2) {
                        e2.printStackTrace();
                    }


                }
            }

            //devo ottenere i nodi in più Es. il famoso 260
            ArrayList<String> sqlQuery;
            for (String table : info.getTableList()) {

                sqlQuery = sqlKey.get(table);
                //ciclo solo se ci sono query disponibili
                if (sqlQuery != null && !sqlQuery.isEmpty()) {
                    for (String s : sqlQuery) {

                        try {
                            s += " WHERE t2.__search_id = '" + n.getSearchID() + "';";

                            statement1 = conn.createStatement();
                            resultSet1 = statement1.executeQuery(s);

                            Node connected;
                            Edge edge;
                            Edge bedge;

                            while(resultSet1.next()) {

                                //TODO: ADD THE NODE ONLY IF NECESSARY
                                connected = nodeList.get(resultSet1.getInt(1));


                                if (commonNodes.get(connected) != null) {
                                    commonNodes.get(connected).add(n);
                                } else {
                                    ArrayList<Node> nodelist = new ArrayList<>();
                                    nodelist.add(n);
                                    commonNodes.put(connected,nodelist);
                                }
                            }

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }

        for (Node node : toAdd) {
            if (interestSet.containsKey(node)) {

                for(String s : node.getKeywordList())

                    interestSet.get(node).mergeNode(node.getAdjacentNodes(), s);

            } else
                interestSet.put(node.getSearchID(), node);

        }


        long after = System.currentTimeMillis();

        System.out.println("Nodes connected in: " + (after - before) / 1000 + " seconds\n");

        ArrayList<ArrayList<Edge>> list = new ArrayList<>();
        list.add(edges);
        list.add(backedges);
        return list;
    }*/

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
                    "ON c3.conrelid = c1.oid INNER JOIN pg_class AS c2 ON c3.confrelid = c2.oid;\n");
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
        NScore += tree.getRoot().getScore() * tree.getRoot().getKeywordList().size();
        i++;

        ArrayList<Node> sons;
        for (Map.Entry<Node, ArrayList<Node>> e: tree.getSons().entrySet() ){
            sons = e.getValue();
            if (sons.isEmpty() && e.getKey().equals(tree.getRoot())) {
                break;
            } else if (sons.isEmpty()){
                NScore += e.getKey().getScore() * e.getKey().getKeywordList().size();
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

    /**
     *  Compute the global score of a tree
     *
     * @param tree          Input tree
     * @param lambda        Variable needed to compute the score
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