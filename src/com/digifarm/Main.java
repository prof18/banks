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
/*
	Banks implementation.

	TO DO:

		0) implementazione della classe TupleNode
			- lista con i nodi adiacenti
			- nome della tabella e chiave primaria

		1) implementazione della lettura dei dati dal database e costruzione dei nodi
			- costruisco il grafo popolando ogni TupleNode con: adiacenze e chiave primaria

		2) implementazione della navigazione con Dijkstra

*/

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
            ArrayList<Interest> interestList = new ArrayList<>();

            double max = 0;
            double min = Integer.MAX_VALUE;

            for (String term: temp) {

                interest = Utility.createInterestSet(conn, set, term);
                list = Utility.connectNodes(conn, interest, set);

                ArrayList<Edge> edges = list.get(0);
                ArrayList<Edge> backedges = list.get(1);

                Utility.backEdgePoint(backedges);

                for (Edge b : backedges)
                    System.out.println("Backedges with calculated weight: \n" + b.toString());

                max = Utility.maxNodeScore(interest, max);
                min = Utility.minEdgeWeight(edges, min);

                Interest interestElement = new Interest(interest,edges,backedges);
                interestList.add(interestElement);

            }

            System.out.println("max score: " + max );
            System.out.println("min weight: " + min + "\n");

            HashMap<Integer, Node> interestSet = new HashMap<>();
            ArrayList<Edge> edges = new ArrayList<>();
            ArrayList<Edge> bedges = new ArrayList<>();

            for (Interest i : interestList) {

                //obtain data from interest object
                interestSet = i.getNode();
                edges = i.getEdge();
                bedges = i.getBedge();

                //normalize edge weight
                //only logarithmic scale
                Utility.eWeightNorm(edges,min);
                for (Edge ed : edges)
                    System.out.println("Edges with normalized weight: \n" + ed.toString());

                //normalize node score
                //TODO scegliere qui scala lineare (fraction) o logaritmica(logarithm)
                Utility.nScoreNorm(interestSet, "logarithm", max);
                Node n = null;
                //stampa di debug dei nodi con i pesi
                System.out.println("\n");
                for (Map.Entry<Integer, Node> e : interestSet.entrySet()) {
                    n = e.getValue();
                    System.out.println("Node with normalized score: " + n.getSearchID() + " weight: " + n.getScore());

                }

                System.out.println("\nStarting node: " + n.getSearchID());


                Graph graph = new Graph(interestSet,edges,bedges);

                //need to obtain the start node
                Dijkstra dijkstra = new Dijkstra(graph,n);
                dijkstra.visit();


            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (ClassNotFoundException ce) {
            System.out.println("Unable to find Driver Class");
            ce.printStackTrace();
        }

    }
}