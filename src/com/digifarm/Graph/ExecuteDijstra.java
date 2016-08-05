package com.digifarm.Graph;

/**
 * Created by marco on 8/5/16.
 */
public class ExecuteDijstra extends Thread {

    private Dijkstra dijkstra;

    public ExecuteDijstra(Graph graph, Node node) {

        dijkstra = new Dijkstra(graph,node);

    }

    public void run() {
        dijkstra.visit();
    }
}
