package com.digifarm.Exec;

import com.digifarm.BESearch.Dijkstra;
import com.digifarm.BESearch.SPIterator;
import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Graph.*;
import com.digifarm.Tree.Tree;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by digifarmer on 7/6/16.
 **/

//TODO: CLOSE DB CONNECTION
public class Main {

    public static void main (String[] args) {

        try {

            Scanner in = new Scanner(System.in);
            //ask database username
            System.out.print("Enter Username: ");
            String username = /*in.nextLine();*/ "marco";
            //ask database name
            System.out.print("Enter database name: ");
            String database = /*in.nextLine();*/ "mondial";
            //connectB to database
            ConnectionDB conn = new ConnectionDB(username, "", "localhost", "5432", database);
            System.out.println("Connected\n-----------------");

            //container of some db information, like tables and tuples
            dbInfo info = new dbInfo();

            //create a graph from all the database
            Utility.createGraph(conn, database, info);

            //ask for keyword
            System.out.print("Enter keyword (Enter to insert another, \"q\" to exit):\n");
            ArrayList<String> temp = new ArrayList<>();
            while (in.hasNext()) {
                String keyword = in.nextLine();
                if (keyword.toLowerCase().compareTo("q") == 0) {
                    break;
                } else
                    temp.add(keyword);
            }

            long start = System.currentTimeMillis();

            //keywords that aren't table
            ArrayList<String> notTable = new ArrayList<>();
            //keywords that are table
            ArrayList<String> tableMatch = new ArrayList<>();
            //global node's,edge's,backedge's lists of interest set
            HashMap<Integer, Node> globalNodeList = new HashMap<>();
            ArrayList<Edge> globalEdgeList = new ArrayList<>();
            ArrayList<Edge> globalBEdgeList = new ArrayList<>();

            //max and min value for normalization
            double max = 0;
            double min = Integer.MAX_VALUE;

            //cycle the keyword provided by the user to filter between table and "classic" keyword
            for (String s : temp) {

                boolean isMatched = false;
                //cycle all the table
                for (String table : info.getTableList()) {

                    //if the input was a name of a table --> add to another list
                    if (s.toLowerCase().compareTo(table.toLowerCase()) == 0) {

                        tableMatch.add(table);
                        isMatched = true;
                    }
                }
                //add the "classic" keywords to another table
                if (!isMatched)
                    notTable.add(s);
            }

            //map of nodes that contains a match of the keyword
            HashMap<Integer, Node> interestSet;

        /*
        *   Key   --> Actual level of depth.
        *   Value --> A container of backwards and forwards nodes of all the nodes
        *             in the actual level
        */
            HashMap<Integer, Levels> levelWrapper = new HashMap<>();

            //TODO: Here you can choose the max depth of navigation
            int maxDepth = 3;

            //create containers for every level
            for (int j = 1; j <= maxDepth; j++)
                levelWrapper.put(j, new Levels());

            //cycle for every keyword
            for (String s : notTable) {

                //actual depth
                int i = 1;
                //create the interest set for the current keyword
                interestSet = Utility.createInterestSet(conn, s, info);

                while (i <= maxDepth) {

                    Levels l = levelWrapper.get(i);

                    //When we are in the first level, we need to navigate from the nodes in the interest set
                    //otherwise we need to navigate from the previous level
                    if (i == 1) {

                        Utility.findBackwardInterest(conn, interestSet, info.getNodes(), info, l);
                        Utility.findForwardInterest(conn, interestSet, info.getNodes(), l);

                    } else {

                        //find Backward Of Backward
                        Utility.bFindBackward(conn, info.getNodes(), levelWrapper.get(i - 1).getBackward(), l, info);
                        //find Backward Of Forward
                        Utility.fFindBackward(conn, info.getNodes(), levelWrapper.get(i - 1).getForward(), l, info);
                        //find Forward Of Backward
                        Utility.bFindForward(conn, info.getNodes(), levelWrapper.get(i - 1).getBackward(), l);
                        //find Forward Of Forward
                        Utility.fFindForward(conn, info.getNodes(), levelWrapper.get(i - 1).getForward(), l);
                    }

                    i++;
                }

                //add all the node from the interest set to the
                for (Map.Entry<Integer, Node> e : interestSet.entrySet()) {

                    Node n = e.getValue();
                    int index = e.getKey();
                    globalNodeList.put(index, n);

                }

                //connect nodes of the interest set to each other
                Utility.connectInterestNodes(conn, interestSet, globalEdgeList, globalBEdgeList);
            }

            //we do this only when there isn't table's name in the keyword.
            if (tableMatch.isEmpty()) {

                Levels level;
                int depth;

                //cycle all the levels
                for (Map.Entry<Integer, Levels> entry : levelWrapper.entrySet()) {

                    depth = entry.getKey();
                    level = entry.getValue();
                    HashMap<Node, ArrayList<Node>> backwards = level.getBackward();
                    HashMap<Node, ArrayList<Node>> forward = level.getForward();

                    //cycle all the backward nodes
                    for (Map.Entry<Node, ArrayList<Node>> e : backwards.entrySet()) {

                        Node from = e.getKey();
                        ArrayList<Node> listTo = e.getValue();

                        //the list has size greater than one, so the key node is a common ancestor. The nodes in the list
                        //must belong to different keyword
                        if (listTo.size() > 1) {

                            int i = 0;
                            ArrayList<String> keyList = new ArrayList<>();
                            Node tempNode = null;

                            //in the first iteration we need to save the node and the node's keyword list to compare to
                            //other nodes in the following iteration
                            for (Node n : listTo) {

                                if (i == 0) {
                                    keyList = n.getKeywordList();
                                    tempNode = n;
                                } else {

                                    for (String s : keyList) {

                                        //TODO: METODO & BABBA AND CAPRA --> QUESTIONE DELLE KEYWORD
                                        //check if the Node A keyword is not a keyword of Node B
                                        if (!n.getKeywordList().contains(s)) {

                                            if (tempNode != null) {

                                                from.addAdjacentNode(tempNode);
                                                tempNode.addAdjacentNode(from);
                                                tempNode.incrementScore();
                                                //if the node is present yet, merge
                                                if (globalNodeList.containsKey(from.getSearchID())) {
                                                    globalNodeList.get(from.getSearchID()).mergeNode(from.getAdjacentNodes(),
                                                            from.getKeywordList());
                                                } else {
                                                    globalNodeList.put(from.getSearchID(), from);
                                                }
                                                Edge edge = new Edge(from, tempNode, 1);
                                                tempNode.incrementScore();
                                                //if the edge is already in the globalList, skip the adding
                                                if (!globalEdgeList.contains(edge)) {
                                                    globalEdgeList.add(edge);
                                                    Edge bedge = new Edge(tempNode, from, 0);
                                                    globalBEdgeList.add(bedge);
                                                }

                                                //now we need to rebuild the path
                                                if (depth > 1) {

                                                    int tempD = depth;
                                                    while (tempD > 1) {

                                                        //previous level. We need to know for the navigation
                                                        Levels l = levelWrapper.get(tempD - 1);
                                                        //backward of the level-1
                                                        HashMap<Node, ArrayList<Node>> listMinusLevel = l.getBackward();
                                                        if (listMinusLevel.containsKey(tempNode)) {
                                                            ArrayList<Node> toListLevelMinus = listMinusLevel.get(tempNode);
                                                            //add the corresponding edge
                                                            for (Node newTo : toListLevelMinus) {
                                                                newTo.addAdjacentNode(tempNode);
                                                                tempNode.addAdjacentNode(newTo);
                                                                if (globalNodeList.containsKey(tempNode.getSearchID())) {
                                                                    globalNodeList.get(tempNode.getSearchID()).mergeNode(tempNode.getAdjacentNodes(), tempNode.getKeywordList());
                                                                } else {
                                                                    globalNodeList.put(tempNode.getSearchID(), tempNode);
                                                                }
                                                                Edge edge1 = new Edge(tempNode, newTo, 1);
                                                                newTo.incrementScore();
                                                                if (!globalEdgeList.contains(edge1)) {

                                                                    globalEdgeList.add(edge1);
                                                                    Edge bedge1 = new Edge(newTo, tempNode, 0);
                                                                    globalBEdgeList.add(bedge1);
                                                                }
                                                            }

                                                        }
                                                        tempD--;
                                                    }
                                                }
                                                tempNode = null;
                                            }

                                            from.addAdjacentNode(n);
                                            n.addAdjacentNode(from);

                                            if (globalNodeList.containsKey(from.getSearchID())) {
                                                globalNodeList.get(from.getSearchID()).mergeNode(from.getAdjacentNodes(), from.getKeywordList());
                                            } else {
                                                globalNodeList.put(from.getSearchID(), from);
                                            }
                                            Edge edge = new Edge(from, n, 1);
                                            n.incrementScore();
                                            if (!globalEdgeList.contains(edge)) {

                                                globalEdgeList.add(edge);
                                                Edge bedge = new Edge(n, from, 0);
                                                globalBEdgeList.add(bedge);
                                            }

                                            if (depth > 1) {

                                                int tempD = depth;
                                                while (tempD > 1) {

                                                    Levels l = levelWrapper.get(tempD - 1);
                                                    HashMap<Node, ArrayList<Node>> listMinusLevel = l.getBackward();
                                                    if (listMinusLevel.containsKey(n)) {
                                                        ArrayList<Node> toListLevelMinus = listMinusLevel.get(n);
                                                        for (Node newTo : toListLevelMinus) {
                                                            newTo.addAdjacentNode(n);
                                                            n.addAdjacentNode(newTo);

                                                            if (globalNodeList.containsKey(n.getSearchID())) {
                                                                globalNodeList.get(n.getSearchID()).mergeNode(n.getAdjacentNodes(), n.getKeywordList());
                                                            } else {
                                                                globalNodeList.put(n.getSearchID(), n);
                                                            }
                                                            Edge edge1 = new Edge(n, newTo, 1);
                                                            newTo.incrementScore();
                                                            if (!globalEdgeList.contains(edge1)) {

                                                                globalEdgeList.add(edge1);
                                                                Edge bedge1 = new Edge(newTo, n, 0);
                                                                globalBEdgeList.add(bedge1);
                                                            }
                                                        }
                                                    }
                                                    tempD--;
                                                }
                                            }
                                        }
                                    }
                                }
                                i++;
                            }
                        }
                    }

                    //cycle all the the forward node
                    for (Map.Entry<Node, ArrayList<Node>> e1 : forward.entrySet()) {

                        Node from = e1.getKey();
                        ArrayList<Node> listTo = e1.getValue();

                        //the list has size greater than one, so the key node is a common ancestor. The nodes in the list
                        //must belong to different keyword
                        if (listTo.size() > 1) {

                            int i = 0;
                            ArrayList<String> keyList = new ArrayList<>();
                            Node tempNode = null;

                            //in the first iteration we need to save the node and the node's keyword list to compare to
                            //other nodes in the following iteration
                            for (Node n : listTo) {

                                if (i == 0) {
                                    keyList = n.getKeywordList();
                                    tempNode = n;
                                } else {

                                    for (String s : keyList) {

                                        //check if the Node A keyword is not a keyword of Node B
                                        if (!n.getKeywordList().contains(s)) {

                                            if (tempNode != null) {

                                                from.addAdjacentNode(tempNode);
                                                tempNode.addAdjacentNode(from);
                                                //if the node is present yet, merge
                                                if (globalNodeList.containsKey(tempNode.getSearchID()))
                                                    globalNodeList.get(tempNode.getSearchID()).mergeNode(tempNode.getAdjacentNodes(), tempNode.getKeywordList());
                                                else
                                                    globalNodeList.put(tempNode.getSearchID(), tempNode);
                                                Edge e = new Edge(from, tempNode, 1);
                                                tempNode.incrementScore();
                                                //if the edge is already in the globalList, skip the adding
                                                if (!globalEdgeList.contains(e)) {
                                                    globalEdgeList.add(e);
                                                    Edge bedge = new Edge(tempNode, from, 0);
                                                    globalBEdgeList.add(bedge);
                                                }


                                                //now we need to rebuild the path
                                                if (depth > 1) {

                                                    int tempD = depth;
                                                    while (tempD > 1) {

                                                        //previous level. We need to know for the navigation
                                                        Levels l = levelWrapper.get(tempD - 1);
                                                        //backward of the level-1
                                                        HashMap<Node, ArrayList<Node>> listMaxLevel = l.getForward();
                                                        ArrayList<Node> valueList;
                                                        Node fromKey;
                                                        //add the corresponding edge
                                                        for (Map.Entry<Node, ArrayList<Node>> e3 : listMaxLevel.entrySet()) {

                                                            fromKey = e3.getKey();
                                                            valueList = e3.getValue();
                                                            if (valueList.contains(tempNode)) {

                                                                tempNode.addAdjacentNode(fromKey);
                                                                fromKey.addAdjacentNode(tempNode);
                                                                if (globalNodeList.containsKey(tempNode.getSearchID()))
                                                                    globalNodeList.get(tempNode.getSearchID()).mergeNode(tempNode.getAdjacentNodes(), tempNode.getKeywordList());
                                                                else
                                                                    globalNodeList.put(tempNode.getSearchID(), tempNode);
                                                                Edge edge = new Edge(fromKey, tempNode, 1);
                                                                tempNode.incrementScore();
                                                                if (!globalEdgeList.contains(edge)) {

                                                                    globalEdgeList.add(edge);
                                                                    Edge bedge1 = new Edge(tempNode, fromKey, 0);
                                                                    globalBEdgeList.add(bedge1);
                                                                }

                                                                break;
                                                            }
                                                        }
                                                        tempD--;
                                                    }
                                                }
                                                tempNode = null;
                                            }

                                            from.addAdjacentNode(n);
                                            n.addAdjacentNode(from);

                                            if (globalNodeList.containsKey(n.getSearchID()))
                                                globalNodeList.get(n.getSearchID()).mergeNode(n.getAdjacentNodes(), n.getKeywordList());
                                            else
                                                globalNodeList.put(n.getSearchID(), n);
                                            Edge e = new Edge(from, n, 1);
                                            n.incrementScore();
                                            if (!globalEdgeList.contains(e)) {

                                                globalEdgeList.add(e);
                                                Edge bedge = new Edge(n, from, 0);
                                                globalBEdgeList.add(bedge);
                                            }

                                            if (depth > 1) {

                                                int tempD = depth;
                                                while (tempD > 1) {

                                                    Levels l = levelWrapper.get(tempD - 1);
                                                    HashMap<Node, ArrayList<Node>> listMaxLevel = l.getForward();
                                                    ArrayList<Node> valueList;
                                                    Node fromKey;
                                                    for (Map.Entry<Node, ArrayList<Node>> e3 : listMaxLevel.entrySet()) {

                                                        fromKey = e3.getKey();
                                                        valueList = e3.getValue();
                                                        if (valueList.contains(n)) {

                                                            n.addAdjacentNode(fromKey);
                                                            fromKey.addAdjacentNode(n);

                                                            if (globalNodeList.containsKey(n.getSearchID()))
                                                                globalNodeList.get(n.getSearchID()).mergeNode(n.getAdjacentNodes(), n.getKeywordList());
                                                            else
                                                                globalNodeList.put(n.getSearchID(), n);
                                                            Edge edge = new Edge(fromKey, n, 1);
                                                            n.incrementScore();
                                                            if (!globalEdgeList.contains(edge)) {

                                                                globalEdgeList.add(edge);
                                                                Edge bedge1 = new Edge(n, fromKey, 0);
                                                                globalBEdgeList.add(bedge1);
                                                            }
                                                            break;
                                                        }
                                                    }
                                                    tempD--;
                                                }
                                            }
                                        }
                                    }
                                }
                                i++;
                            }
                        }
                    }
                    System.out.println("Path built");
                }
            } else { //if there is a table name in the keyword, we don't need to check if the keyword in the listTo are different

                Levels level;
                int depth;

                for (Map.Entry<Integer, Levels> entry : levelWrapper.entrySet()) {

                    depth = entry.getKey();
                    level = entry.getValue();
                    HashMap<Node, ArrayList<Node>> backwards = level.getBackward();
                    HashMap<Node, ArrayList<Node>> forward = level.getForward();

                    for (Map.Entry<Node, ArrayList<Node>> e : backwards.entrySet()) {

                        Node from = e.getKey();
                        ArrayList<Node> listTo = e.getValue();

                        for (Node n : listTo) {

                            from.addAdjacentNode(n);
                            n.addAdjacentNode(from);

                            if (globalNodeList.containsKey(from.getSearchID())) {
                                globalNodeList.get(from.getSearchID()).mergeNode(from.getAdjacentNodes(), from.getKeywordList());
                            } else {
                                globalNodeList.put(from.getSearchID(), from);
                            }
                            Edge edge = new Edge(from, n, 1);
                            n.incrementScore();
                            if (!globalEdgeList.contains(edge)) {

                                globalEdgeList.add(edge);
                                Edge bedge = new Edge(n, from, 0);
                                globalBEdgeList.add(bedge);
                            }

                            if (depth > 1) {

                                int tempD = depth;
                                while (tempD > 1) {

                                    Levels l = levelWrapper.get(tempD - 1);
                                    HashMap<Node, ArrayList<Node>> listMinusLevel = l.getBackward();
                                    if (listMinusLevel.containsKey(n)) {
                                        ArrayList<Node> toListLevelMinus = listMinusLevel.get(n);
                                        for (Node newTo : toListLevelMinus) {

                                            newTo.addAdjacentNode(n);
                                            n.addAdjacentNode(newTo);

                                            if (globalNodeList.containsKey(n.getSearchID())) {
                                                globalNodeList.get(n.getSearchID()).mergeNode(n.getAdjacentNodes(), n.getKeywordList());
                                            } else {
                                                globalNodeList.put(n.getSearchID(), n);
                                            }
                                            Edge edge1 = new Edge(n, newTo, 1);
                                            newTo.incrementScore();
                                            if (!globalEdgeList.contains(edge1)) {

                                                globalEdgeList.add(edge1);
                                                Edge bedge1 = new Edge(newTo, n, 0);
                                                globalBEdgeList.add(bedge1);
                                            }

                                        }
                                    }
                                    tempD--;
                                }
                            }

                        }
                    }

                    for (Map.Entry<Node, ArrayList<Node>> e1 : forward.entrySet()) {

                        Node from = e1.getKey();
                        ArrayList<Node> listTo = e1.getValue();

                        for (Node n : listTo) {

                            from.addAdjacentNode(n);
                            n.addAdjacentNode(from);

                            if (globalNodeList.containsKey(n.getSearchID()))
                                globalNodeList.get(n.getSearchID()).mergeNode(n.getAdjacentNodes(), n.getKeywordList());
                            else
                                globalNodeList.put(n.getSearchID(), n);
                            Edge e = new Edge(from, n, 1);
                            n.incrementScore();
                            if (!globalEdgeList.contains(e)) {

                                globalEdgeList.add(e);
                                Edge bedge = new Edge(n, from, 0);
                                globalBEdgeList.add(bedge);
                            }

                            if (depth > 1) {

                                int tempD = depth;
                                while (tempD > 1) {

                                    Levels l = levelWrapper.get(tempD - 1);
                                    HashMap<Node, ArrayList<Node>> listMaxLevel = l.getForward();
                                    ArrayList<Node> valueList;
                                    Node fromKey;
                                    for (Map.Entry<Node, ArrayList<Node>> e3 : listMaxLevel.entrySet()) {

                                        fromKey = e3.getKey();
                                        valueList = e3.getValue();
                                        if (valueList.contains(n)) {

                                            n.addAdjacentNode(fromKey);
                                            fromKey.addAdjacentNode(n);

                                            if (globalNodeList.containsKey(n.getSearchID()))
                                                globalNodeList.get(n.getSearchID()).mergeNode(n.getAdjacentNodes(), n.getKeywordList());
                                            else
                                                globalNodeList.put(n.getSearchID(), n);
                                            Edge edge = new Edge(fromKey, n, 1);
                                            n.incrementScore();
                                            if (!globalEdgeList.contains(edge)) {

                                                globalEdgeList.add(edge);
                                                Edge bedge1 = new Edge(n, fromKey, 0);
                                                globalBEdgeList.add(bedge1);
                                            }
                                            break;
                                        }
                                    }
                                    tempD--;
                                }
                            }
                        }
                    }
                }
                System.out.println("Path built (table as keyword)");
            }

            //Cleaning useless data

            interestSet = new HashMap<>();
            info = new dbInfo();
            levelWrapper = new HashMap<>();

            max = Utility.maxNodeScore(globalNodeList, max);
            min = Utility.minEdgeWeight(globalEdgeList, min);

            //Assign point to the backedge
            Utility.backEdgePoint(globalBEdgeList);

            //Normalize edge weight. only logarithmic scale
            Utility.eWeightNorm(globalEdgeList, min);

            //Normalize node score
            //TODO choose linear scale (fraction) or logarithm(logarithm)
            Utility.nScoreNorm(globalNodeList, "logarithm", max);

            Graph graph = new Graph(globalNodeList, globalEdgeList, globalBEdgeList);

            //Heap of iterator order by node's weight
            PriorityQueue<SPIterator> iteratorHeap = new PriorityQueue<>();
            //The key is the starting node of the iterator. The value is a map containing the relationship between two nodes
            HashMap<Node, HashMap<Node, Node>> path = new HashMap<>();

            //Iterator Creation
            Node node;
            for (Map.Entry<Integer, Node> e : globalNodeList.entrySet()) {
                SPIterator it = new SPIterator();
                node = e.getValue();
                //the iterator start only from keyword node
                if (node.isKeywordNode()) {

                    Dijkstra dijkstra = new Dijkstra(graph, node, it);
                    path.put(node, it.getPreviousList());
                    dijkstra.visit();
                    iteratorHeap.add(it);
                }
            }

            Node v;

            //Tree heap
            int HEAP_SIZE = 500;
            PriorityQueue<Tree> outputHeap = new PriorityQueue<>();
            PriorityQueue<Tree> outputBuffer = new PriorityQueue<>();

            while (!iteratorHeap.isEmpty()) {

                //remove first iterator from heap
                SPIterator spIterator = iteratorHeap.poll();
                ListIterator<Node> iterator = spIterator.createIterator();
                v = iterator.next();
                iterator.remove();
                //remove the first distance because we have polled the corresponding node
                spIterator.deleteDistance();
                //if there is another nodes, reinsert the iterator in the heap
                if (iterator.hasNext())
                    iteratorHeap.add(spIterator);
                //the init of the nodelist v.Li
                if (!v.isVisited()) {
                    for (String term : notTable)
                        v.createVLi(term);
                    v.setVisited(true);
                }

                Node origin = (Node) spIterator.getOrigin();
                HashMap<String, ArrayList<Node>> vLi = v.getvLi();

                //calculate cross product
                ArrayList<ArrayList<Node>> crossProduct = generateCrossProduct(origin, v);

                //insert origin to v.Li
                ArrayList<String> keywordList = origin.getKeywordList();
                for (String s : keywordList) {
                    if (vLi.containsKey(s)) {
                        vLi.get(s).add(origin);
                    }
                }

                //cycle on the tuple
                for (ArrayList<Node> tuple : crossProduct) {

                    Tree tree = new Tree();
                    //v is the root of the tree
                    tree.setRoot(v);

                    //tree population

                    for (Node n : tuple) {

                        //get the path of an interator
                        HashMap<Node, Node> previousPath = path.get(n);

                        Node previous = v;
                        //find a path from v to each origin node in the tuple
                        //if v = n, the tree is only the root; the cycle isn't necessary
                        while (previous != null && v != n) {

                            Node tmp = previous;
                            previous = previousPath.get(previous);
                            tree.addSon(tmp, previous);
                            tree.addFather(tmp, previous);
                        }
                    }

                    //add all edges and backedges to a global list
                    ArrayList<Edge> overallEdges = new ArrayList<>();
                    for (Edge e : globalEdgeList)
                        overallEdges.add(e);

                    for (Edge be : globalBEdgeList)
                        overallEdges.add(be);

                    //calculate node score
                    Utility.overallNodeScore(tree);
                    //calculate edge score
                    Utility.overallEdgeScore(overallEdges, tree);
                    //calculate global score
                    double lambda = 0.2;
                    //TODO choose "multiplication" or "addition"
                    Utility.globalScore(tree, lambda, "addition");

                    HashMap<Node, ArrayList<Node>> sons = tree.getSons();

                    //if the root hasn't sons, add the tree
                    if (sons.get(tree.getRoot()) == null) {
                        addTree(tree, outputHeap, outputBuffer, HEAP_SIZE, tableMatch);
                    }
                    //if root has only one child, continue /*duplicate result*/
                    else if (sons.get(tree.getRoot()).size() == 1 && sons.get(sons.get(tree.getRoot()).get(0)).size() == 0)
                        break;
                    else
                        addTree(tree, outputHeap, outputBuffer, HEAP_SIZE, tableMatch);
                }
            } //[C] while

            while (outputHeap.size() != 0)

                outputBuffer.add(outputHeap.poll());

            int i = 1;
            //the score of the last output tree. We need it for outputting nodes with the same score but in position greater than ten
            double lastScore = 0;
            //We need to output 10 relevant results.

            while (i < 11 && !outputBuffer.isEmpty()) {

                Tree Ttemp = outputBuffer.poll();
                lastScore = Ttemp.getGlobalScore();
                System.out.println(i);
                System.out.println(Ttemp.toString());
                i++;
            }

            Tree Ttemp = new Tree();
            if (!outputBuffer.isEmpty())
                Ttemp = outputBuffer.poll();

            //if the score is the same, we output also the node in position greater than 10
            while (Double.compare(lastScore, Ttemp.getGlobalScore()) == 0 && !outputBuffer.isEmpty()) {

                System.out.println(10);
                System.out.println(Ttemp.toString());
                Ttemp = outputBuffer.poll();
            }

            long finish = System.currentTimeMillis();
            long execTime = (finish - start) / 1000;
            System.out.println("Global Time: " + execTime + " seconds");
            conn.closeDBConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate cross product
     *
     * @param origin    The origin node
     * @param v         The current node
     * @return          The result of the cross product
     */
    private static ArrayList<ArrayList<Node>> generateCrossProduct(Node origin, Node v) {

        //crossProduct wrapper
        ArrayList<ArrayList<Node>> crossProduct = new ArrayList();
        //map of vLi
        HashMap<String, ArrayList<Node>> vLiMap = v.getvLi();
        //keyword list of origin
        ArrayList<String> originKeyList = origin.getKeywordList();

        for (String oK : originKeyList) {

            for (Map.Entry<String, ArrayList<Node>> entry : vLiMap.entrySet()) {

                //if the node has only one keyword, the crossproduct's result has only the origin node. Else if the node
                //hasn't keyword, the crossproduct's result is null. Otherwise compute it.
                if (oK.compareTo(entry.getKey()) != 0) {

                    ArrayList<Node> nodeL = entry.getValue();

                    if (nodeL.size() != 0) {

                        ArrayList<Node> tuple = new ArrayList();
                        tuple.add(origin);

                        for (Node n : nodeL) {
                            if (n.getSearchID() != origin.getSearchID())
                                tuple.add(n);
                        }
                        crossProduct.add(tuple);
                    }

                } else if (vLiMap.entrySet().size() == 1) {
                    ArrayList<Node> tuple = new ArrayList();
                    tuple.add(origin);
                    crossProduct.add(tuple);
                }
            }
        }
        return crossProduct;
    }

    /**
     *  This method add a tree to the output heap
     *
     * @param tree              Tree
     * @param outputHeap        Output Heap
     * @param outputBuffer      Output Buffer
     * @param maxHeapSize       Maximum size of the heap
     * @param tableMatch        List of the table keyword
     */
    private static void addTree(Tree tree, PriorityQueue<Tree> outputHeap, PriorityQueue<Tree> outputBuffer,
                                int maxHeapSize, ArrayList<String> tableMatch) {

        boolean cond = false;

        //We add the tree only if contains a node from the table requested by the user.
        if (!tableMatch.isEmpty()) {

            for (String s : tableMatch) {

                //check the root
                if (tree.getRoot().getTableName().toLowerCase().compareTo(s.toLowerCase()) == 0)
                    cond = true;

                //check the other nodes
                for (Map.Entry<Node, ArrayList<Node>> entry : tree.getSons().entrySet()) {
                    for (Node nd : entry.getValue()) {

                        if (nd != null && nd.getTableName().toLowerCase().compareTo(s.toLowerCase()) == 0)
                            cond = true;
                    }
                }
            }

            if (cond) {
                if (outputHeap.size() == maxHeapSize) {
                    outputBuffer.add(outputHeap.poll());
                    outputHeap.add(tree);
                    cond = false;
                } else {
                    outputHeap.add(tree);
                    cond = false;
                }
            }

        } else { //if the user doesn't request table, add all the tree

            if (outputHeap.size() == maxHeapSize) {
                outputBuffer.add(outputHeap.poll());
                outputHeap.add(tree);

            } else {
                outputHeap.add(tree);
            }
        }
    }
}