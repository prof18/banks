package com.digifarm.Graph;

import java.util.ArrayList;

/**
 * Created by marco on 7/7/16.
 **/
public class Edge {

    private int weight;
    private Node from;
    private Node to;
  
    /**
     * Build a new Edge between two Node.
     *
     * @param from      Starting Node
     * @param to        Destination Node
     * @param weight    Edge's Weight
     */
    public Edge(Node from, Node to, int weight) {

        this.from = from;
        this.to = to;
        this.weight = weight;


    }

    /**
     *
     * @return weight   Returns edge's weight
     */
    public int getWeight() {
        return weight;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }
}
