package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by marco on 7/7/16.
 */
public class Graph {

    ArrayList<Node> nodeSet;
    ArrayList<Edge> edge = new ArrayList<>();
    ArrayList<Edge> bedge = new ArrayList<>();

    public Graph(ArrayList<Node> nodeSet, ArrayList<Edge> edge, ArrayList<Edge> bedge) {
        this.nodeSet = nodeSet;
        this.edge = edge;
        this.bedge = bedge;
    }

    public ArrayList<Node> getNodeSet() {
        return nodeSet;
    }

    public ArrayList<Edge> getEdge() {
        return edge;
    }

    public ArrayList<Edge> getBedge() {
        return bedge;
    }
}
