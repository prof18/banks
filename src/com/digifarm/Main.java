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
            ArrayList<ArrayList<Edge>> edgeList = new ArrayList<>();

            double max = 0;
            double min = Integer.MAX_VALUE;

            for (String term: temp) {

                interest = Utility.createInterestSet(conn, set, term);
                list = Utility.connectNodes(conn, interest, set);

                ArrayList<Edge> edges = list.get(0);
                ArrayList<Edge> backedge = list.get(1);

                Utility.backEdgePoint(backedge);

                for (Edge b : backedge)
                    System.out.println("Backedges with calculated weight: \n" + b.toString());

                max = Utility.maxNodeScore(interest, max);
                min = Utility.minEdgeWeight(edges, min);

                interestList.add(interest);
                edgeList.add(edges);

            }

            System.out.println("max weight: " + max + "\n");

            for (HashMap<Integer, Node> interest2 : interestList) {

                //TODO scegliere qui scala lineare o logaritmica
                Utility.nScoreNorm(interest2, "logarithm", max);
                Node n;
                //stampa di debug dei nodi con i pesi
                for (Map.Entry<Integer, Node> e : interest2.entrySet()) {
                    n = e.getValue();
                    System.out.println("Node with normalized score: " + n.getSearchID() + " weight: " + n.getScore());

                }
            }

            System.out.println("\n");

            for (ArrayList<Edge> e : edgeList ) {

                //only logarithmic scale
                Utility.eWeightNorm(e,min);
                for (Edge ed : e)
                    System.out.println("Edges with normalized weight: \n" + ed.toString());
            }


        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (ClassNotFoundException ce) {
            System.out.println("Unable to find Driver Class");
            ce.printStackTrace();
        }

    }
}