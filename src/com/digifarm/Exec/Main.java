package com.digifarm.Exec;

import com.digifarm.BESearch.Dijkstra;
import com.digifarm.BESearch.SPIterator;
import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Graph.*;
import com.digifarm.Graph.Utility;
import com.digifarm.Tree.Tree;
import com.sun.org.apache.xml.internal.serializer.ElemDesc;
import com.sun.org.apache.xpath.internal.axes.HasPositionalPredChecker;

import java.sql.Array;
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
            String username = in.nextLine();
            //ask database name
            System.out.print("Enter database name: ");
            String database = in.nextLine();
            //connect to database
            ConnectionDB conn = new ConnectionDB(username, "", "localhost", "5432", database);
            System.out.println("Connected\n-----------------");

            dbInfo info = new dbInfo();

            //create a graph from all the database
            Utility.createGraph(conn,database,info);

            //ask for keyword
            System.out.print("Enter keyword (use comma as separator): ");

            String keyword = in.nextLine();
            String[] temp = keyword.split(",");

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

            double max = 0;
            double min = Integer.MAX_VALUE;

            //cycle the keyword provided
            for (String s : temp) {

                boolean isMatched = false;

                //cycle the table
                for (String table : info.getTableList()) {

                    //the input was a name of a table --> save all the tuple of the table
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

            for (String s : notTable) {

                //create the interest set for the current keyword
                interestSet = Utility.createInterestSet(conn,s,info);

                //connects the node in the interest set
                edgeWrapper = Utility.connectNodes(conn,interestSet,info.getNodes(),info,tableMatch,commonNodes);

                ArrayList<Edge> edges = edgeWrapper.get(0);
                ArrayList<Edge> backedges = edgeWrapper.get(1);
               // edgeList = edges;



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
                    globalBEdgeList.add(bedge);


            }

            Node connected;
            ArrayList<Node> nodeList = new ArrayList<>();
            for (Map.Entry<Node,ArrayList<Node>> entry : commonNodes.entrySet()) {

                connected = entry.getKey();
                nodeList = entry.getValue();

                if (!globalNodeList.containsKey(connected.getSearchID())) {

                    if (nodeList.size() > 1) {

                        globalNodeList.put(connected.getSearchID(), connected);

                        for (Node n : nodeList) {

                            System.out.println("Common node " + n.getSearchID());

                            System.out.println(connected.getTableName() + "->" + connected.getSearchID() + " : " + n.getTableName() + "->" + n.getSearchID());
                            //System.out.println("ciao");
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

            }

            max = Utility.maxNodeScore(globalNodeList, max);
            min = Utility.minEdgeWeight(globalEdgeList, min);

            //assign point to the backedge
            Utility.backEdgePoint(globalBEdgeList);
            //
            // normalize edge weight
            //only logarithmic scale
            Utility.eWeightNorm(globalEdgeList,min);



            //TODO: END REWRITING

         /*  /* HashMap<Integer, Node> interest = new HashMap<>();

            ArrayList<ArrayList<Edge>> list = new ArrayList<>();



            ArrayList<Edge> edgeList = new ArrayList<>();

            double max = 0;
            double min = Integer.MAX_VALUE;

            ArrayList<String> matchList = new ArrayList<>();

           // ArrayList<String> notTable = new ArrayList<>();

            for (String s : temp ) {

                boolean isMatched = false;

                for (String t : info.getTableList()) {

                    if (s.toLowerCase().compareTo(t.toLowerCase()) == 0) (Utility.isContained(s.toLowerCase(),t.toLowerCase())) {
                        matchList.add(t);
                        isMatched = true;
                    }

                }
                if(!isMatched)
                    notTable.add(s);
            }

            for (String term: notTable) {

                //interest set creation
                interest = Utility.createInterestSet(conn, term, info);


                //interest set connection
               // list = Utility.connectNodes(conn, interest, info.getNodes(),info,matchList,term);

                Node n = new Node(260,"borders");
                n.addKeyword(term);


                if (term.compareTo("hungary") == 0) {
                    interest.get(3556).addAdjacentNode(n);
                    n.addAdjacentNode(interest.get(3556));
                } else {
                    interest.get(3445).addAdjacentNode(n);
                    n.addAdjacentNode(interest.get(3445));
                }
                n.setKeywordNode(true);
                interest.put(260,n);
                globalNodeList.put(260,n);
                if (term.compareTo("hungary") == 0) {
                    Edge e1 = new Edge(n,interest.get(3556),1);
                    Edge be1 = new Edge(interest.get(3556),n,0);
                    globalEdgeList.add(e1);
                    globalBEdgeList.add(be1);
                } else {
                    Edge e2 = new Edge(n, interest.get(3445), 1);
                    Edge be2 = new Edge(interest.get(3445), n, 0);

                    globalEdgeList.add(e2);

                    globalBEdgeList.add(be2);
                }


                //extract edges and backedge list
                ArrayList<Edge> edges = list.get(0);
                ArrayList<Edge> backedges = list.get(1);
                edgeList = edges;

                max = Utility.maxNodeScore(interest, max);
                min = Utility.minEdgeWeight(edges, min);

                //add node to global list
                Node node;
                for (Map.Entry<Integer,Node> e : interest.entrySet()) {
                    node = e.getValue();
                    //needs to check if the node is already in the global list to avoid duplicates
                    if (globalNodeList.containsKey(node.getSearchID())) {
                        //if the node is already int he global list we need to update the adjacent list
                        (globalNodeList.get(node.getSearchID())).mergeNode(node.getAdjacentNodes(),term);
                    } else
                        globalNodeList.put(node.getSearchID(),node);
                }
                //add edge to global list
                for (Edge edge : edges)
                    globalEdgeList.add(edge);
                //add backedge to global list
                for (Edge bedge : backedges)
                    globalBEdgeList.add(bedge);
            }



            globalNodeList.get(260).addKeyword("slovakia");
            globalNodeList.get(260).addAdjacentNode(globalNodeList.get(3445));
            globalNodeList.get(3556).setScore(1);




            /*//*//**//*debug print
            for (Edge e : globalEdgeList)
                System.out.println("Edges after globalList: \n" + e.toString());
            for (Edge b : globalBEdgeList)
                System.out.println("Backedges after  globaList: \n" + b.toString());
            System.out.println("max score: " + max );
            System.out.println("min weight: " + min + "\n");

            //normalize edge weight
            //only logarithmic scale
            Utility.eWeightNorm(globalEdgeList,min);
            for (Edge ed : globalEdgeList)
                System.out.println("Edges with normalized weight: \n" + ed.toString());*/

            //normalize node score
            //TODO scegliere qui scala lineare (fraction) o logaritmica(logarithm)
            Utility.nScoreNorm(globalNodeList, "logarithm", max);

            /*//debug print: nodes with score
            System.out.println("\n");
            Node nd;
            for (Map.Entry<Integer, Node> e : globalNodeList.entrySet()) {
                nd = e.getValue();
                System.out.println("Node with normalized score: " + nd.getSearchID() + " weight: " + nd.getScore());

            }*/

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
                    //System.out.println("------------------------------");
                    dijkstra.visit();
                    iteratorHeap.add(it);
                    //System.out.println("dada");
                }
            }

            Node v;
            // ArrayList<Tree> treess = new ArrayList<>();



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
                    for (String term: temp)
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
                            // System.out.println("esigrwhjieasjkld");
                            tree.addSon(tmp,previous);
                            tree.addFather(tmp,previous);
                           // System.out.println("hsrs");
                        }
                       // System.out.println("Fine while");
                    }
              //      System.out.println("fine for");

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

                //System.out.println("hello it's me");

            } //[C] while

            while (outputHeap.size() != 0) {
                outputBuffer.add(outputHeap.poll());
            }


           // System.out.println("ciao");

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

            while( Double.compare(lastScore , Ttemp.getGlobalScore() ) == 0 && !outputBuffer.isEmpty()) {

                System.out.println(Ttemp.toString());
                Ttemp = outputBuffer.poll();

            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (ClassNotFoundException ce) {
            System.out.println("Unable to find Driver Class");
            ce.printStackTrace();
        }
    }

    public static ArrayList<ArrayList<Node>> generateCrossProduct(Node origin, Node v) {

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


    public static void addTree(Tree tree, PriorityQueue<Tree> outputHeap, PriorityQueue<Tree> outputBuffer, int maxHeapSize) {

        if (outputHeap.size() == maxHeapSize ) {
            outputBuffer.add(outputHeap.poll());
            outputHeap.add(tree);
        } else {
            outputHeap.add(tree);
        }
    }
}