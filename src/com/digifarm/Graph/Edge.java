package com.digifarm.Graph;

/**
 * Created by digifarmer on 7/7/16.
 **/
public class Edge {

    private double score;
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
        this.score = weight;

    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
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
                "score=" + score +
                ", from=" + from.getSearchID() +
                ", to=" + to.getSearchID() +
                '}';
    }
}
