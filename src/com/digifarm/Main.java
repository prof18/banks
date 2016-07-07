package com.digifarm;

import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Graph.Node;
import com.digifarm.Graph.Utility;

import java.util.ArrayList;

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

            ConnectionDB conn = new ConnectionDB("nihilus", "", "localhost", "5432", "mondial");
            ArrayList<Node> set = Utility.createGraph(conn);
            /*for (Node n: set) {
                System.out.println("Table: " + n.getTableName() + ", ID: " + n.getSearchID());
            }*/
            ArrayList<Node> interestSet = Utility.createInterestSet(conn, set, "Peru");
            for (Node n: interestSet) {
                System.out.println("Table: " + n.getTableName() + ", ID: " + n.getSearchID());
            }
            System.out.println("Connected");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error");
        }

    }
}