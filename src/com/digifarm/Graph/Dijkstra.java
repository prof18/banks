package com.digifarm.Graph;

import java.util.*;

/**
 * Created by marco on 7/7/16.
 */
public class Dijkstra {

    //infinite value
    private static final Integer INFINITE = Integer.MAX_VALUE;
    private double nodeWeight;
    private double edgeWeight;
    //private Graph graph;
    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<Edge> bedges = new ArrayList<>();
    private ArrayList<Node> adjacent;
    private PriorityQueue<Node> nodesQueue = new PriorityQueue<Node>();
    private ArrayList<Edge> edgeList = new ArrayList<>();
    private Node start;


    //   private Node starting;

    public Dijkstra(Graph graph, Node start){

        //this.graph = graph;
        nodes = graph.getNodeSet();
        edges = graph.getEdge();
        bedges = graph.getBedge();
        this.start = start;



        //edges will contain the edge and also the backedge
        for (Edge e : edges) {
            edgeList.add(e);
        }

        for (Edge be : bedges) {
            edgeList.add(be);
        }


        // starting = start;



        start.setWeight(0);
        //nodes.remove(start.getSearchID());
        //il nodo di partenza ha peso zero, gli altri hanno peso infinito
        //TODO : bisogna renderlo efficente
        for (Node n : nodes) {

                n.setWeight(INFINITE);

            if(n.getSearchID() == start.getSearchID())
                n.setWeight(0);

            nodesQueue.add(n);

        }

        /*nodes.put(start.getSearchID(), start);
        nodesQueue.add(start);*/
    }


    /**
     *
     */
    public void visit() {

        double finalWeight;
        HashMap<Integer, Node> nodeMap = new HashMap<>();

        while (!nodesQueue.isEmpty() && nodesQueue.peek().getWeight() != INFINITE) {

            Node minimum = nodesQueue.poll();
            ArrayList<Node> adj = minimum.getAdjacentNodes();
            adjacent = new ArrayList<>(minimum.getAdjacentNodes());

//            Collections.copy(adjacent, minimum.getAdjacentNodes());
            nodeMap.put(minimum.getSearchID(),minimum);
            double startWeight = minimum.getWeight();

            System.out.println("Starting node: " + minimum.toString());
            System.out.println("Start weight: " + startWeight);
            //System.out.println(adjacent.toString());


            for (Node n : minimum.getAdjacentNodes()) {
                if(nodeMap.containsKey(n.getSearchID())) {
                    adjacent.remove(n);
                }
            }

            for (Node to : adjacent) {
                System.out.println("To node: " + to.toString());
                for (Edge edge : edgeList) {
                    //we traverse the graph in reverse direction
                    if (edge.getTo().getSearchID() == minimum.getSearchID() && edge.getFrom().getSearchID() == to.getSearchID()) {
                        finalWeight = startWeight + edge.getWeight();

                        if (finalWeight < to.getWeight()) {
                            System.out.println("Final weigh: " + finalWeight);
                            nodesQueue.remove(to);
                            to.setWeight(finalWeight);
                            to.setPreviousNode(minimum);
                            to.addKeywordNode(start);
                            nodesQueue.add(to);
                            System.out.println("To previous node " + to.getPreviousNode().toString());
                        }


                    }
                }
            }
        }
    }
}