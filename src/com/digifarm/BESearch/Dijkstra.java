package com.digifarm.BESearch;

import com.digifarm.Graph.Edge;
import com.digifarm.Graph.Graph;
import com.digifarm.Graph.Node;

import java.util.*;


/**
 * Created by digifarmer on 7/7/16.
 **/
public class Dijkstra {

    //infinite value
    private static final Integer INFINITE = Integer.MAX_VALUE;
    private HashMap<Integer, Node> nodes = new HashMap<>();
    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<Edge> bedges = new ArrayList<>();
    private ArrayList<Node> adjacent;
    //node queue with weight as priority
    private PriorityQueue<Node> nodesQueue = new PriorityQueue<>();
    //list of edge and backedge
    private ArrayList<Edge> edgeList = new ArrayList<>();
    private Node start;
    //Iterator
    private SPIterator spIt;

    public Dijkstra(Graph graph, Node start, SPIterator it){

        nodes = graph.getNodeSet();
        edges = graph.getEdge();
        bedges = graph.getBedge();
        this.start = start;
        spIt = it;
        //add starting node to the iterator
        it.add(start);

        //edges will contain the edge and also the backedge
        for (Edge e : edges) {
            edgeList.add(e);
        }

        for (Edge be : bedges) {
            edgeList.add(be);
        }

        start.setWeight(0);

        //starting node has zero weight, the others infinite
        //TODO : bisogna renderlo efficente?
        Node n;
        for (Map.Entry<Integer, Node> e : nodes.entrySet()) {
                n = e.getValue();
                n.setWeight(INFINITE);
            if(n.getSearchID() == start.getSearchID())
                n.setWeight(0);
            nodesQueue.add(n);
        }
    }

    public void visit() {

        double finalWeight;
        //this map is used to avoid duplicates nodes
        HashMap<Integer, Node> nodeMap = new HashMap<>();

        while (!nodesQueue.isEmpty() && nodesQueue.peek().getWeight() != INFINITE) {

            Node minimum = nodesQueue.poll();
            adjacent = new ArrayList<>(minimum.getAdjacentNodes());

            nodeMap.put(minimum.getSearchID(),minimum);
            double startWeight = minimum.getWeight();

            System.out.println("Starting node: " + minimum.toString());
            System.out.println("Start weight: " + startWeight);

            //necessary check to avoid nodes revisiting.
            //the father has as adjacent the son and vice versa
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
                            //TODO: non bisogna farlo qua, ma con l'iteratore. Vedi prospettive.txt
                            //to.addNodeToVLi(start);
                            spIt.add(to);
                            spIt.setDistance(finalWeight);
                            nodesQueue.add(to);
                            System.out.println("To previous node " + to.getPreviousNode().toString());
                        }
                    }
                }
            }
        }
    }
}