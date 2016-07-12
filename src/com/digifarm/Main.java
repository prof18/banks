package com.digifarm;

import com.digifarm.DBConnection.ConnectionDB;
import com.digifarm.Graph.Node;
import com.digifarm.Graph.Utility;

import java.nio.channels.Pipe;
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
            System.out.println("Enter Username:");
            String username = in.nextLine();
            //ask database name
            System.out.println("Enter database name: ");
            String database = in.nextLine();
            //connect to database
            ConnectionDB conn = new ConnectionDB(username, "", "localhost", "5432", database);
            System.out.println("Connected\n-----------------");

            HashMap<Integer, Node> set = Utility.createGraph(conn,database);
            //Utility.connectNodes(conn, set);
            Node n;
           /* for (Map.Entry<Integer,Node> e: set.entrySet()) {
                n = e.getValue();
                System.out.println("Table: " + n.getTableName() + ", ID: " + n.getSearchID());
            }*/

            //Utility.connectNodes(conn, set);

            //ask for keyword
            System.out.println("Enter keyword (comma as separator without spaces):");
            long before1 = System.currentTimeMillis();
            String keyword = in.nextLine();
            String[] temp;
            temp = keyword.split(" ");

            for (String term: temp ) {
                ArrayList<Node> interest = Utility.createInterestSet(conn, set, term);
            }

            long after1 = System.currentTimeMillis();
            long time1 = (after1 - before1)/1000;
            System.out.println("Interest Set Builded in: " + time1 + " seconds");

        } catch (SQLException sqle) {
            sqle.printStackTrace();

        } catch (ClassNotFoundException ce) {
            System.out.println("Unable to find Driver Class");
            ce.printStackTrace();
        }

    }
}