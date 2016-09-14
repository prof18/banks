package com.digifarm.Tree;

import com.digifarm.Graph.Edge;
import com.digifarm.Graph.Node;

import java.util.ArrayList;

/**
 * Created by marco on 9/14/16.
 */
public class Tree {

    private TNode root;
    //private ArrayList<TNode> tNodes = new ArrayList<>();
    private ArrayList<Edge> tEdges = new ArrayList<>();

    public TNode getRoot() {
        return root;
    }

    public void setRoot(TNode root) {

        this.root = root;
    }

    public ArrayList<Edge> gettEdges() {
        return tEdges;
    }

    public void settEdges(ArrayList<Edge> tEdges) {
        this.tEdges = tEdges;
    }
}
