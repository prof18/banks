package com.digifarm;

import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Graph.Edge;
import com.digifarm.Graph.Node;
import com.digifarm.Graph.Utility;

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
            ArrayList<HashMap<Integer, Node>> interestList = new ArrayList<>();
            double max = 0;

            for (String term: temp) {

                interest = Utility.createInterestSet(conn, set, term);
                list = Utility.connectNodes(conn, interest, set);


             /*   double maxWeight = Utility.maxNodeWeight(interest);
                System.out.println("max node weight " + maxWeight);

                //TODO scegliere qui scala lineare o logaritmica
                Utility.nWeightNorm(interest,"logarithm", maxWeight);

                //stampa di debug dei nodi con i pesi
                for (Map.Entry<Integer, Node> e : interest.entrySet()) {
                    n = e.getValue();
                    System.out.println("Node: " + n.getSearchID() + " weight: " + n.getWeight());

                }*/

                ArrayList<Edge> edges = list.get(0);
                ArrayList<Edge> backedge = list.get(1);

                for (Edge e : edges)
                    System.out.println("Edges: \n" + e.toString());

                for (Edge b : backedge)
                    System.out.println("Backedges: \n" + b.toString());

                Utility.backEdgePoint(backedge);

                max = Utility.maxNodeWeight(interest, max);

               /* double minWeight = Utility.minEdgeWeight(edges);
                System.out.println("min edge weight: " + minWeight);*/
                interestList.add(interest);

            }

            System.out.println("max weight: " + max);

            for (HashMap<Integer, Node> interest2 : interestList) {

                //TODO scegliere qui scala lineare o logaritmica
                Utility.nWeightNorm(interest2, "logarithm", max);
                Node n;
                //stampa di debug dei nodi con i pesi
                for (Map.Entry<Integer, Node> e : interest2.entrySet()) {
                    n = e.getValue();
                    System.out.println("Node: " + n.getSearchID() + " weight: " + n.getWeight());

                }
            }


        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (ClassNotFoundException ce) {
            System.out.println("Unable to find Driver Class");
            ce.printStackTrace();
        }

    }
}