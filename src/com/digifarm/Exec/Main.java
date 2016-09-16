package com.digifarm.Exec;

import com.digifarm.BESearch.Dijkstra;
import com.digifarm.BESearch.SPIterator;
import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Graph.*;
import com.digifarm.Graph.Utility;
import com.digifarm.Tree.TNode;
import com.digifarm.Tree.Tree;
import com.sun.corba.se.impl.javax.rmi.CORBA.Util;
import com.sun.corba.se.impl.util.*;

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

            HashMap<Integer, Node> set = Utility.createGraph(conn,database);

            //ask for keyword
            System.out.print("Enter keyword (use space as separator): ");

            String keyword = in.nextLine();
            String[] temp = keyword.split(" ");

            HashMap<Integer, Node> interest = new HashMap<>();
            ArrayList<ArrayList<Edge>> list = new ArrayList<>();

            //global nodes,edges,backedges lists of interest set
            HashMap<Integer, Node> globalNodeList = new HashMap<>();
            ArrayList<Edge> globalEdgeList = new ArrayList<>();
            ArrayList<Edge> globalBEdgeList = new ArrayList<>();

            ArrayList<Edge> edgeList = new ArrayList<>();

            double max = 0;
            double min = Integer.MAX_VALUE;

            for (String term: temp) {

                //interest set creation
                interest = Utility.createInterestSet(conn, set, term);
                //interest set connection
                list = Utility.connectNodes(conn, interest, set);

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

            /*//initialize the nodelist v.Li for each keyword
            Node n;
            for (Map.Entry<Integer,Node> e : globalNodeList.entrySet()) {
                n = e.getValue();
                for (String term: temp)
                    n.createVLi(term);
            }*/

            //assign point to the backedge
            Utility.backEdgePoint(globalBEdgeList);


            //debug print
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
                System.out.println("Edges with normalized weight: \n" + ed.toString());

            //normalize node score
            //TODO scegliere qui scala lineare (fraction) o logaritmica(logarithm)
            Utility.nScoreNorm(globalNodeList, "logarithm", max);

            //debug print: nodes with score
            System.out.println("\n");
            Node nd;
            for (Map.Entry<Integer, Node> e : globalNodeList.entrySet()) {
                nd = e.getValue();
                System.out.println("Node with normalized score: " + nd.getSearchID() + " weight: " + nd.getScore());

            }

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
                    System.out.println("------------------------------");
                    dijkstra.visit();
                    iteratorHeap.add(it);
                }
            }

            Node v;
            ArrayList<Tree> treess = new ArrayList<>();

            //tree heap
            int HEAP_SIZE = 32;
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
                ArrayList<ArrayList<Node>> crossProduct = generateCrossProduct(origin,v,temp);

                //insert origin to v.Li
                ArrayList<String> keywordList = v.getKeywordList();
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
                            tree.addSon(previous,tmp);
                            tree.addFather(tmp,previous);
                            System.out.println("hsrs");
                        }
                        System.out.println("Fine while");
                    }
                    System.out.println("fine for");

                    //calculate node score
                    Utility.overallNodeScore(tree);
                    //calculate edge score
                    Utility.overallEdgeScore(edgeList,tree);
                    //calculate global score
                    double lambda = 0.2;
                    //TODO choose "multiplication" or "addition"
                    Utility.globalScore(tree,lambda,"addition");

                    HashMap<Node, ArrayList<Node>> sons = tree.getSons();
                    if (sons.get(tree.getRoot()) == null ) {
                        addTree(tree,outputHeap,outputBuffer,HEAP_SIZE);
                    }
                    else if (sons.get(tree.getRoot()).size() == 1)
                        break;
                    else
                        addTree(tree,outputHeap,outputBuffer,HEAP_SIZE);

                }

                System.out.println("hello it's me");

            } //[C] while

            while (outputHeap.size() != 0) {
                outputBuffer.add(outputHeap.poll());
            }

            System.out.println("ciao");

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (ClassNotFoundException ce) {
            System.out.println("Unable to find Driver Class");
            ce.printStackTrace();
        }
    }

    public static ArrayList<ArrayList<Node>> generateCrossProduct(Node origin, Node v, String[] keyword) {

        //container of the cross products
        ArrayList<ArrayList<Node>> crossProduct = new ArrayList<>();

        //obtain map of vL.i
        HashMap<String, ArrayList<Node>> vLi = v.getvLi();

        for (String k : keyword ) {

            //v.L of k
            ArrayList<Node> list = vLi.get(k);

            if (list.size() != 0) {

                //obtain the keyword of the node v
                ArrayList<String> keywordList = v.getKeywordList();
                //for each key of the node, we need to add the node present in the v.Lkey
                for (String key : keywordList ) {

                    //the REAL Cross Product
                    ArrayList<Node> tuple = new ArrayList<>();
                    tuple.add(origin);

                    ArrayList<Node> nodeList = vLi.get(key);
                    for (Node n : nodeList )
                        tuple.add(n);
                    crossProduct.add(tuple);
                }

                return crossProduct;
            }
        }
        //the crossProduct is empty if any v.Li is empty
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