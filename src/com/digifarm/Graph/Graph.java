package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by digifarmer on 7/7/16.
 **/
public class Graph {

    HashMap<Integer, Node> nodeSet;
    ArrayList<Edge> edge = new ArrayList<>();
    ArrayList<Edge> bedge = new ArrayList<>();

    /**
     *  Create new Graph, a wrapper for Node map, edge list and backedge list
     *
     * @param nodeSet
     * @param edge
     * @param bedge
     */
    public Graph(HashMap<Integer, Node> nodeSet, ArrayList<Edge> edge, ArrayList<Edge> bedge) {
        this.nodeSet = nodeSet;
        this.edge = edge;
        this.bedge = bedge;
    }

    public HashMap<Integer, Node> getNodeSet() {
        return nodeSet;
    }

    public ArrayList<Edge> getEdge() {
        return edge;
    }

    public ArrayList<Edge> getBedge() {
        return bedge;
    }
}
