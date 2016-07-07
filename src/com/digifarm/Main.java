package com.digifarm;

import com.digifarm.DBConnection.ConnectionDB;

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

            ConnectionDB conn = new ConnectionDB("marco", "", "localhost", "5432", "imdb");
            System.out.println("Connected");

        } catch (Exception e) {

            System.out.println("Error");
        }

    }
}