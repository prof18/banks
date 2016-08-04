package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Created by marco on 7/7/16.
 */
public class Dijkstra {

    //infinite value
    private static final Integer INFINITE = Integer.MAX_VALUE;
    private double nodeWeight;
    private double edgeWeight;
    //private Graph graph;
    private HashMap<Integer, Node> nodes = new HashMap<>();
    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<Edge> bedges = new ArrayList<>();
    private ArrayList<Node> adjacent = new ArrayList<>();
    PriorityQueue<Node> nodesQueue = new PriorityQueue<Node>();
    //   private Node starting;

    public Dijkstra(Graph graph, Node start){

        //this.graph = graph;
        nodes = graph.getNodeSet();
        edges = graph.getEdge();
        bedges = graph.getBedge();
        // starting = start;

        Node n;
        //il nodo di partenza ha peso zero, gli altri hanno peso infinito
        for (Map.Entry<Integer, Node> e : nodes.entrySet()) {
            n = e.getValue();
            if (start.getSearchID() == n.getSearchID()) {
                n.setWeight(0);
                nodesQueue.add(n);
            }
            else
            n.setWeight(INFINITE);
            nodesQueue.add(n);
        }

        visit();
    }


    private void visit() {

        double finalWeight;

        while (!nodesQueue.isEmpty()) {

            Node minimum = nodesQueue.poll();
            adjacent = minimum.getAdjacentNodes();
            double startWeight = minimum.getWeight();

            for (Node to : adjacent) {
                for (Edge edge : edges) {
                    //we traverse the graph in reverse direction
                    if (edge.getTo().getSearchID() == minimum.getSearchID() && edge.getFrom().getSearchID() == to.getSearchID()) {
                        finalWeight = startWeight + edge.getWeight();

                        if (finalWeight < to.getWeight())
                            to.setWeight(finalWeight);
                    }
                }
            }
        }
    }
}