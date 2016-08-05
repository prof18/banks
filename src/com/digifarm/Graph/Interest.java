package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by marco on 8/5/16.
 */
public class Interest {

    private HashMap<Integer, Node> node = new HashMap<>();
    private ArrayList<Edge> edge = new ArrayList<>();
    private ArrayList<Edge> bedge = new ArrayList<>();

    public Interest(HashMap<Integer, Node> node, ArrayList<Edge> edge, ArrayList<Edge> bedge) {
        this.node = node;
        this.edge = edge;
        this.bedge = bedge;
    }

    public HashMap<Integer, Node> getNode() {
        return node;
    }

    public ArrayList<Edge> getEdge() {
        return edge;
    }

    public ArrayList<Edge> getBedge() {
        return bedge;
    }
}
