package com.digifarm;

import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Graph.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by marco on 7/6/16.
 */


public class Main {
    public static void main(String[] args) {

        try {

            Scanner in = new Scanner(System.in);
            //ask database username
            System.out.print("Enter Username: ");
            String username = in.nextLine();
            //ask database name}
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
            //classe interest da modifare
            ArrayList<Interest> interestList = new ArrayList<>();
            //lista di tutti i nodi degli interest set
            //ArrayList<Node> globalNodeList = new ArrayList<>();
            HashMap<Integer, Node> globalNodeList = new HashMap<>();
            ArrayList<Edge> globalEdgeList = new ArrayList<>();
            ArrayList<Edge> globalBEdgeList = new ArrayList<>();

            double max = 0;
            double min = Integer.MAX_VALUE;

            for (String term: temp) {

                interest = Utility.createInterestSet(conn, set, term);
                list = Utility.connectNodes(conn, interest, set);

                ArrayList<Edge> edges = list.get(0);
                ArrayList<Edge> backedges = list.get(1);

               // Utility.backEdgePoint(backedges);

              /*  for (Edge b : backedges)
                    System.out.println("Backedges with calculated weight: \n" + b.toString());
               */
                max = Utility.maxNodeScore(interest, max);
                min = Utility.minEdgeWeight(edges, min);

                //add item to global list
                //node
                Node node;
                for (Map.Entry<Integer,Node> e : interest.entrySet()) {
                    node = e.getValue();
                    //bisogna controllare se il nodo e' gia' presente nella lista, per evitare doppioni.
                    if (globalNodeList.containsKey(node.getSearchID())) {
                        //aggiungere le nuove adiacenze
                        (globalNodeList.get(node.getSearchID())).mergeNode(node.getAdjacentNodes(),term);

                    } else
                        globalNodeList.put(node.getSearchID(),node);

                }
                //edge
                for (Edge edge : edges)
                    globalEdgeList.add(edge);
                //backedge
                for (Edge bedge : backedges)
                    globalBEdgeList.add(bedge);

                //Interest interestElement = new Interest(globalNodeList,edges,backedges);
                //interestList.add(interestElement);

            }

            Utility.backEdgePoint(globalBEdgeList);

            for (Edge e : globalEdgeList)
                System.out.println("Edges after globalList: \n" + e.toString());

            for (Edge b : globalBEdgeList)
                System.out.println("Backedges after  globaList: \n" + b.toString());


            System.out.println("max score: " + max );
            System.out.println("min weight: " + min + "\n");

            HashMap<Integer, Node> interestSet = new HashMap<>();
            ArrayList<Edge> edges = new ArrayList<>();
            ArrayList<Edge> bedges = new ArrayList<>();

            // for (Interest i : interestList) {

            //obtain data from interest object
            //interestSet = i.getNode();
            // edges = i.getEdge();
            // bedges = i.getBedge();

            //normalize edge weight
            //only logarithmic scale
            Utility.eWeightNorm(globalEdgeList,min);
            for (Edge ed : globalEdgeList)
                System.out.println("Edges with normalized weight: \n" + ed.toString());

            //normalize node score
            //TODO scegliere qui scala lineare (fraction) o logaritmica(logarithm)
            Utility.nScoreNorm(globalNodeList, "logarithm", max);
            //Node n = null;
            //stampa di debug dei nodi con i pesi
            System.out.println("\n");
            Node nd;
            for (Map.Entry<Integer, Node> e : globalNodeList.entrySet()) {
                nd = e.getValue();
                System.out.println("Node with normalized score: " + nd.getSearchID() + " weight: " + nd.getScore());

            }

            //print random node for checking dijstra
            // System.out.println("\nStarting node: " + n.getSearchID());


            Graph graph = new Graph(globalNodeList,globalEdgeList,globalBEdgeList);

            Node node;
            for (Map.Entry<Integer, Node> e : globalNodeList.entrySet()) {
                node = e.getValue();
                if (node.isKeywordNode()) {

                    //TODO: No Thread. Only for test purpose
                    Dijkstra dijkstra = new Dijkstra(graph,node);
                    System.out.println("------------------------------");
                    dijkstra.visit();

                    //TODO: Dijstra with thread
                    /*ExecuteDijstra dijstra = new ExecuteDijstra(graph, node);
                    dijstra.start();*/
                }
            }

            //       }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (ClassNotFoundException ce) {
            System.out.println("Unable to find Driver Class");
            ce.printStackTrace();
        }

    }
}