package com.digifarm.Exec;

import com.digifarm.BESearch.Dijkstra;
import com.digifarm.BESearch.SPIterator;
import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Graph.*;
import com.digifarm.Graph.Utility;
import com.digifarm.Tree.Tree;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by digifarmer on 7/6/16.
 **/


public class Main {



    public static void main(String[] args) {

        try {

            Scanner in = new Scanner(System.in);
            //ask database username
            System.out.print("Enter Username: ");
            String username = /*in.nextLine();*/ "marco";
            //ask database name
            System.out.print("Enter database name: ");
            String database = /*in.nextLine();*/ "imdb";
            //connect to database
            ConnectionDB conn = new ConnectionDB(username, "", "localhost", "5432", database);
            System.out.println("Connected\n-----------------");

            //container of some db information, like table, columns and tuples
            dbInfo info = new dbInfo();

            //create a graph from all the database
            Utility.createGraph(conn,database,info);

            //ask for keyword
            System.out.print("Enter keyword (Enter to insert another, \"q\" to exit):\n");

            ArrayList<String> temp = new ArrayList<>();
            while (in.hasNext()) {
                //System.out.print("Enter keyword (one for line, q to exit): ");
                String keyword = in.nextLine();
                if (keyword.toLowerCase().compareTo("q") == 0) {
                    break;
                } else
                    temp.add(keyword);
            }

            long start = System.currentTimeMillis();


            HashMap<String, ArrayList<Node>> tableTuple = new HashMap<>();
            //keyword that aren't table
            ArrayList<String> notTable = new ArrayList<>();
            //keyword that are table
            ArrayList<String> tableMatch = new ArrayList<>();

            //list of common nodes
            HashMap<Node, ArrayList<Node>> commonNodes = new HashMap<>();

            //global nodes,edges,backedges lists of interest set
            HashMap<Integer, Node> globalNodeList = new HashMap<>();
            ArrayList<Edge> globalEdgeList = new ArrayList<>();
            ArrayList<Edge> globalBEdgeList = new ArrayList<>();

            //max and min value for normalization
            double max = 0;
            double min = Integer.MAX_VALUE;

            //cycle the keyword provided by the user
            for (String s : temp) {

                boolean isMatched = false;

                //cycle al the table
                for (String table : info.getTableList()) {

                    //if the input was a name of a table --> save all the tuple of the table
                    if (s.toLowerCase().compareTo(table.toLowerCase()) == 0 ) {

                        tableTuple.put(table,Utility.getTableTuple(table,conn));
                        tableMatch.add(table);
                        isMatched = true;
                    }
                }

                if (!isMatched)
                    notTable.add(s);
            }

            HashMap<Integer, Node> interestSet = new HashMap<>();

            ArrayList<ArrayList<Edge>> edgeWrapper = new ArrayList<>();

            //key --> partenza, value --> list of arrivals
            HashMap<Node,ArrayList<Node>> xLevel = new HashMap<>();
            //wrapper. chiave numero livello, valore un merdaio di robe


            HashMap<Integer,Levels> levelWrapper = new HashMap<>();

            //TODO: CHANGE THE DEPTH OF NAVIGATION
            int maxDepth = 3;

            for (int j = 1; j <= maxDepth; j++)
                levelWrapper.put(j,new Levels());


            for (String s : notTable) {

                //depth
                int i = 1;


                //create the interest set for the current keyword
                interestSet = Utility.createInterestSet(conn,s,info,tableMatch);

                //connects the node in the interest set
                //edgeWrapper = Utility.connectNodes(conn,interestSet,info.getNodes(),info,commonNodes);

                HashMap<Node,ArrayList<Node>> backward = new HashMap<>();
                HashMap<Node,ArrayList<Node>> forward = new HashMap<>();



                while (i<=maxDepth) {

                    Levels l = levelWrapper.get(i);
                    if (i==1) {
                        Utility.findBackwardInterest(conn, interestSet, info.getNodes(), info, l);
                        Utility.findForwardInterest(conn, interestSet, info.getNodes(), l);
                    } else {

                        //findBackwardOfBackward
                        Utility.bFindBackward(conn,info.getNodes(),levelWrapper.get(i-1).getBackward(),l,info);
                        //findBackwardOfForward
                        Utility.fFindBackward(conn,info.getNodes(),levelWrapper.get(i-1).getForward(),l,info);
                        //findForwardOfBackward
                        Utility.bFindForward(conn,info.getNodes(),levelWrapper.get(i-1).getBackward(),l);
                        //findForwardOfForward
                        Utility.fFindForward(conn,info.getNodes(),levelWrapper.get(i-1).getForward(),l);
                    }
                    i++;
                }




              /*  ArrayList<Edge> edges = edgeWrapper.get(0);
                ArrayList<Edge> backedges = edgeWrapper.get(1);

                //add node to global list
                Node node;
                for (Map.Entry<Integer,Node> e : interestSet.entrySet()) {
                    node = e.getValue();
                    //needs to check if the node is already in the global list to avoid duplicates
                    if (globalNodeList.containsKey(node.getSearchID())) {
                        //if the node is already int he global list we need to update the adjacent list
                        (globalNodeList.get(node.getSearchID())).mergeNode(node.getAdjacentNodes(),s);
                    } else
                        globalNodeList.put(node.getSearchID(),node);
                }
                //add edge to global list
                for (Edge edge : edges)
                    globalEdgeList.add(edge);
                //add backedge to global list
                for (Edge bedge : backedges)
                    globalBEdgeList.add(bedge);*/

            } //end for keyword


            //add all node to interest set
            for (Map.Entry<Integer,Node> e : interestSet.entrySet()) {

                Node n = e.getValue();
                int index = e.getKey();
                globalNodeList.put(index,n);

            }

            Levels level;
            int depth;


            for (Map.Entry<Integer,Levels> entry : levelWrapper.entrySet()) {

                depth = entry.getKey();
                level = entry.getValue();
                HashMap<Node,ArrayList<Node>> backwards = level.getBackward();
                HashMap<Node,ArrayList<Node>> forward = level.getForward();

                for (Map.Entry<Node,ArrayList<Node>> e : backwards.entrySet()) {

                    Node from = e.getKey();
                    ArrayList<Node> listTo = e.getValue();

                    //ho un cazzo di nodo in comune
                    if (listTo.size() > 1) {

                        int i = 0;
                        ArrayList<String> keyList = new ArrayList<>();
                        Node tempNode = null;

                        for (Node n : listTo) {

                            if (i == 0) {
                                keyList = n.getKeywordList();
                                tempNode = n;
                            }  else {

                                for (String s : keyList) {

                                    if (!n.getKeywordList().contains(s)) {

                                        if ( tempNode != null) {

                                            from.addAdjacentNode(tempNode);
                                            tempNode.addAdjacentNode(from);
                                            tempNode.incrementScore();
                                            if (globalNodeList.containsKey(from.getSearchID())) {
                                                globalNodeList.get(from.getSearchID()).mergeNode(from.getAdjacentNodes(),from.getKeywordList());
                                            } else {
                                                globalNodeList.put(from.getSearchID(),from);
                                            }
                                            Edge edge = new Edge(from,tempNode,1);
                                            //TODO: va bene o i nodi in piu' sono un errore?
                                            if (!globalEdgeList.contains(edge)) {
                                                globalEdgeList.add(edge);
                                                tempNode.incrementScore();
                                                Edge bedge = new Edge(tempNode,from,0);
                                                globalBEdgeList.add(bedge);
                                            }


                                            if (depth > 1) {

                                                int tempD = depth;
                                                while(tempD > 1) {

                                                    Levels l = levelWrapper.get(tempD-1);
                                                    HashMap<Node,ArrayList<Node>> listMinusLevel = l.getBackward();
                                                    if (listMinusLevel.containsKey(tempNode)) {
                                                        ArrayList<Node> toListLevelMinus = listMinusLevel.get(tempNode);
                                                        for (Node newTo : toListLevelMinus) {

                                                            newTo.addAdjacentNode(tempNode);
                                                            tempNode.addAdjacentNode(newTo);

                                                            if (globalNodeList.containsKey(tempNode.getSearchID())) {
                                                                globalNodeList.get(tempNode.getSearchID()).mergeNode(tempNode.getAdjacentNodes(),tempNode.getKeywordList());
                                                            } else {
                                                                globalNodeList.put(tempNode.getSearchID(),tempNode);
                                                            }
                                                            Edge edge1 = new Edge(tempNode,newTo,1);
                                                            if (!globalEdgeList.contains(edge1)) {
                                                                newTo.incrementScore();
                                                                globalEdgeList.add(edge1);
                                                                Edge bedge1 = new Edge(newTo,tempNode,0);
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
                                            globalNodeList.get(from.getSearchID()).mergeNode(from.getAdjacentNodes(),from.getKeywordList());
                                        } else {
                                            globalNodeList.put(from.getSearchID(),from);
                                        }
                                        Edge edge = new Edge(from,n,1);
                                        if (!globalEdgeList.contains(edge)) {
                                            n.incrementScore();
                                            globalEdgeList.add(edge);
                                            Edge bedge = new Edge(n, from, 0);
                                            globalBEdgeList.add(bedge);
                                        }

                                        if (depth > 1) {

                                            int tempD = depth;
                                            while(tempD==1) {

                                                Levels l = levelWrapper.get(tempD-1);
                                                HashMap<Node,ArrayList<Node>> listMinusLevel = l.getBackward();
                                                if (listMinusLevel.containsKey(n)) {
                                                    ArrayList<Node> toListLevelMinus = listMinusLevel.get(n);
                                                    for (Node newTo : toListLevelMinus) {

                                                        newTo.addAdjacentNode(n);
                                                        n.addAdjacentNode(newTo);

                                                        if (globalNodeList.containsKey(tempNode.getSearchID())) {
                                                            globalNodeList.get(n.getSearchID()).mergeNode(n.getAdjacentNodes(),n.getKeywordList());
                                                        } else {
                                                            globalNodeList.put(n.getSearchID(),n);
                                                        }
                                                        Edge edge1 = new Edge(n,newTo,1);
                                                        if (!globalEdgeList.contains(edge1)) {
                                                            newTo.incrementScore();
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

                for (Map.Entry<Node,ArrayList<Node>> e1 : forward.entrySet()) {

                    Node from = e1.getKey();
                    ArrayList<Node> listTo = e1.getValue();

                    //ho un cazzo di nodo in comune
                    if (listTo.size() > 1) {

                        int i = 0;
                        ArrayList<String> keyList = new ArrayList<>();
                        Node tempNode = null;

                        for (Node n : listTo) {

                            if (i==0) {

                                keyList = n.getKeywordList();
                                tempNode = n;
                            } else {

                                for (String s : keyList) {

                                    if (!n.getKeywordList().contains(s)) {

                                        if (tempNode != null) {

                                            from.addAdjacentNode(tempNode);
                                            tempNode.addAdjacentNode(from);

                                            if (globalNodeList.containsKey(tempNode.getSearchID()))
                                                globalNodeList.get(tempNode.getSearchID()).mergeNode(tempNode.getAdjacentNodes(),tempNode.getKeywordList());
                                            else
                                                globalNodeList.put(tempNode.getSearchID(),tempNode);
                                            Edge e = new Edge(from,tempNode,1);
                                            if (!globalEdgeList.contains(e)) {
                                                tempNode.incrementScore();
                                                globalEdgeList.add(e);
                                                Edge bedge = new Edge(tempNode, from, 0);
                                                globalBEdgeList.add(bedge);
                                            }

                                            if (depth > 1) {

                                                int tempD = depth;
                                                while (tempD > 1) {

                                                    Levels l = levelWrapper.get(tempD-1);
                                                    HashMap<Node,ArrayList<Node>> listMaxLevel = l.getForward();
                                                    ArrayList<Node> valueList = new ArrayList<>();
                                                    Node fromKey;
                                                    for (Map.Entry<Node,ArrayList<Node>> e3 : listMaxLevel.entrySet()) {

                                                        fromKey = e3.getKey();
                                                        valueList = e3.getValue();
                                                        if (valueList.contains(tempNode)) {

                                                            tempNode.addAdjacentNode(fromKey);
                                                            fromKey.addAdjacentNode(tempNode);

                                                            if (globalNodeList.containsKey(tempNode.getSearchID()))
                                                                globalNodeList.get(tempNode.getSearchID()).mergeNode(tempNode.getAdjacentNodes(),tempNode.getKeywordList());
                                                            else
                                                                globalNodeList.put(tempNode.getSearchID(),tempNode);
                                                            Edge edge = new Edge(fromKey,tempNode,1);
                                                            if (!globalEdgeList.contains(edge)) {
                                                                tempNode.incrementScore();
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
                                            globalNodeList.get(n.getSearchID()).mergeNode(n.getAdjacentNodes(),n.getKeywordList());
                                        else
                                            globalNodeList.put(n.getSearchID(),n);
                                        Edge e = new Edge(from,n,1);
                                        if (!globalEdgeList.contains(e)) {
                                            n.incrementScore();
                                            globalEdgeList.add(e);
                                            Edge bedge = new Edge(n, from, 0);
                                            globalBEdgeList.add(bedge);
                                        }

                                        if (depth > 1) {

                                            int tempD = depth;
                                            while (tempD > 1) {

                                                Levels l = levelWrapper.get(tempD-1);
                                                HashMap<Node,ArrayList<Node>> listMaxLevel = l.getForward();
                                                ArrayList<Node> valueList = new ArrayList<>();
                                                Node fromKey;
                                                for (Map.Entry<Node,ArrayList<Node>> e3 : listMaxLevel.entrySet()) {

                                                    fromKey = e3.getKey();
                                                    valueList = e3.getValue();
                                                    if (valueList.contains(n)) {

                                                        n.addAdjacentNode(fromKey);
                                                        fromKey.addAdjacentNode(n);

                                                        if (globalNodeList.containsKey(n.getSearchID()))
                                                            globalNodeList.get(n.getSearchID()).mergeNode(n.getAdjacentNodes(),n.getKeywordList());
                                                        else
                                                            globalNodeList.put(n.getSearchID(),n);
                                                        Edge edge = new Edge(fromKey,n,1);
                                                        if (!globalEdgeList.contains(edge)) {
                                                            n.incrementScore();
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


                System.out.println("isjgks");
            }



            //if a node is common to two keyword, add this node to the globalNodeList
            Node connected;
            ArrayList<Node> nodeList;

           /* for (Map.Entry<Node,ArrayList<Node>> entry : commonNodes.entrySet()) {

                connected = entry.getKey();
                nodeList = entry.getValue();

                if (!globalNodeList.containsKey(connected.getSearchID())) {

                    //if size > 1 there is a node in common
                    if (nodeList.size() > 1) {

                        globalNodeList.put(connected.getSearchID(), connected);

                        for (Node n : nodeList) {

                            System.out.println(connected.getTableName() + "->" + connected.getSearchID() + " : " + n.getTableName() + "->" + n.getSearchID());
                            connected.addAdjacentNode(n);
                            Edge edge = new Edge(connected, n, 1);
                            globalEdgeList.add(edge);
                            Edge bedge = new Edge(n, connected, 0);
                            globalBEdgeList.add(bedge);
                            n.incrementScore();
                            n.addAdjacentNode(connected);
                        }
                    }
                }
            }*/

            System.out.println("fine level");

            Utility.connectInterestNodes(conn,interestSet,globalEdgeList,globalBEdgeList);

            interestSet = new HashMap<>();
            info = new dbInfo();
            levelWrapper = new HashMap<>();



            max = Utility.maxNodeScore(globalNodeList, max);
            min = Utility.minEdgeWeight(globalEdgeList, min);

            //assign point to the backedge
            Utility.backEdgePoint(globalBEdgeList);

            // normalize edge weight
            //only logarithmic scale
            Utility.eWeightNorm(globalEdgeList,min);

            //normalize node score
            //TODO scegliere qui scala lineare (fraction) o logaritmica(logarithm)
            Utility.nScoreNorm(globalNodeList, "logarithm", max);

            Graph graph = new Graph(globalNodeList,globalEdgeList,globalBEdgeList);

            //TODO: creare coda(Priority QUEUE) di SPIterator ordinati in base alla distanza(IteratorHeap)
            PriorityQueue<SPIterator> iteratorHeap = new PriorityQueue<>();
            //we need a map to know the respective iterator of a start node.
            HashMap<Node, HashMap<Node,Node>> path = new HashMap<>();

            Node node;
            for (Map.Entry<Integer, Node> e : globalNodeList.entrySet()) {
                SPIterator it = new SPIterator();
                node = e.getValue();
                if (node.isKeywordNode()) {

                    Dijkstra dijkstra = new Dijkstra(graph,node,it);
                    path.put(node,it.getPreviousList());
                    dijkstra.visit();
                    iteratorHeap.add(it);
                }
            }

            Node v;

            //tree heap
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
                    for (String term: notTable)
                        v.createVLi(term);
                    v.setVisited(true);
                }

                Node origin = (Node) spIterator.getOrigin();
                HashMap<String, ArrayList<Node>> vLi = v.getvLi();

                //calculate cross product
                ArrayList<ArrayList<Node>> crossProduct = generateCrossProduct(origin,v);

                //insert origin to v.Li
                ArrayList<String> keywordList = origin.getKeywordList();
                for (String s: keywordList ) {
                    if (vLi.containsKey(s)) {
                        vLi.get(s).add(origin);
                    }
                }

                //cycle on the tuple
                for (ArrayList<Node> tuple : crossProduct ) {

                    Tree tree = new Tree();

                    //v is the root of the tree
                    tree.setRoot(v);

                    for (Node n : tuple ) {

                        HashMap<Node, Node> previousPath = path.get(n);

                        Node previous = v;
                        //find a path from v to each origin node in the tuple
                        //if v = n, the tree is only the root; the cycle isn't necessary
                        while (previous !=  null && v != n) {

                            Node tmp = previous;
                            previous = previousPath.get(previous);
                            tree.addSon(tmp,previous);
                            tree.addFather(tmp,previous);
                        }
                    }

                    ArrayList<Edge> overallEdges = new ArrayList<>();
                    for (Edge e : globalEdgeList) {
                        overallEdges.add(e);
                    }

                    for (Edge be : globalBEdgeList) {
                        overallEdges.add(be);
                    }

                    //calculate node score
                    Utility.overallNodeScore(tree);
                    //calculate edge score
                    Utility.overallEdgeScore(overallEdges,tree);
                    //calculate global score
                    double lambda = 0.2;
                    //TODO choose "multiplication" or "addition"
                    Utility.globalScore(tree,lambda,"addition");

                    HashMap<Node, ArrayList<Node>> sons = tree.getSons();
                    if (sons.get(tree.getRoot()) == null ) {
                        addTree(tree,outputHeap,outputBuffer,HEAP_SIZE);
                    }
                    else if (sons.get(tree.getRoot()).size() == 1 && sons.get(sons.get(tree.getRoot()).get(0)).size() == 0)
                        break;
                    else
                        addTree(tree,outputHeap,outputBuffer,HEAP_SIZE);

                }
            } //[C] while

            while (outputHeap.size() != 0) {
                outputBuffer.add(outputHeap.poll());
            }

            int i=0;
            double lastScore = 0;
            while(i < 10 && !outputBuffer.isEmpty()) {

                Tree Ttemp = outputBuffer.poll();
                lastScore = Ttemp.getGlobalScore();
                System.out.println(Ttemp.toString());
                i++;
            }

            Tree Ttemp = new Tree();
            if(!outputBuffer.isEmpty())
                Ttemp = outputBuffer.poll();

            //if the score is the same, we output also the node in position greater than 10
            while( Double.compare(lastScore , Ttemp.getGlobalScore() ) == 0 && !outputBuffer.isEmpty()) {

                System.out.println(Ttemp.toString());
                Ttemp = outputBuffer.poll();
            }

            long finish = System.currentTimeMillis();
            long execTime = (finish - start)/1000;
            System.out.println("Global Time: " + execTime + " seconds");

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (ClassNotFoundException ce) {
            System.out.println("Unable to find Driver Class");
            ce.printStackTrace();
        }
    }

    /**
     *  Calculate cross product
     *
     * @param origin
     * @param v
     * @return
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

                }  else if (vLiMap.entrySet().size()==1 ){
                    ArrayList<Node> tuple = new ArrayList();
                    tuple.add(origin);
                    crossProduct.add(tuple);
                }
            }
        }
        return crossProduct;
    }

    /**
     *
     * @param tree
     * @param outputHeap
     * @param outputBuffer
     * @param maxHeapSize
     */
    private static void addTree(Tree tree, PriorityQueue<Tree> outputHeap, PriorityQueue<Tree> outputBuffer, int maxHeapSize) {

        if (outputHeap.size() == maxHeapSize ) {
            outputBuffer.add(outputHeap.poll());
            outputHeap.add(tree);
        } else {
            outputHeap.add(tree);
        }
    }
}