package com.digifarm.Graph;

import java.util.ArrayList;

/**
 * Created by marco on 7/7/16.
 **/
public class Edge {

    private double weight;
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

    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     *
     * @return weight   Returns edge's weight
     */
    public double getWeight() {
        return weight;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "weight=" + weight +
                ", from=" + from.getSearchID() +
                ", to=" + to.getSearchID() +
                '}';
    }
}
