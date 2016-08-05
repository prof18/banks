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
    private PriorityQueue<Node> nodesQueue = new PriorityQueue<Node>();

    //   private Node starting;

    public Dijkstra(Graph graph, Node start){

        //this.graph = graph;
        nodes = graph.getNodeSet();
        edges = graph.getEdge();
        bedges = graph.getBedge();

        //edges will contain the edge and also the backedge
        for (Edge be : bedges)
           edges.add(be);

        // starting = start;


        Node n;
        start.setWeight(0);
        nodes.remove(start.getSearchID());
        //il nodo di partenza ha peso zero, gli altri hanno peso infinito
        for (Map.Entry<Integer, Node> e : nodes.entrySet()) {
                n = e.getValue();
                n.setWeight(INFINITE);
                nodesQueue.add(n);
        }

        nodes.put(start.getSearchID(), start);
        nodesQueue.add(start);
    }


    /**
     *
     */
    public void visit() {

        double finalWeight;
        ArrayList<Node> nodeList = new ArrayList<>();

        while (!nodesQueue.isEmpty()) {

            Node minimum = nodesQueue.poll();
            nodeList.add(minimum);
            adjacent = minimum.getAdjacentNodes();
            double startWeight = minimum.getWeight();

            System.out.println("Starting node: " + minimum.toString());
            System.out.println("Start weight: " + startWeight);

            for (Node to : adjacent) {
                System.out.println("To node: " + to.toString());
                for (Edge edge : edges) {
                    //we traverse the graph in reverse direction
                    if (edge.getTo().getSearchID() == minimum.getSearchID() && edge.getFrom().getSearchID() == to.getSearchID()) {
                        finalWeight = startWeight + edge.getWeight();

                        if (finalWeight < to.getWeight()) {
                            System.out.println("Final weigh: " + finalWeight);
                            nodesQueue.remove(to);
                            to.setWeight(finalWeight);
                            to.setPreviousNode(minimum);
                            nodesQueue.add(to);
                            System.out.println("To previous node " + to.getPreviousNode().toString());
                        }


                    }
                }
            }
        }
    }
}