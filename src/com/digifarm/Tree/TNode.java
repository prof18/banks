package com.digifarm.Tree;

import com.digifarm.Graph.Node;

import java.util.ArrayList;

/**
 * Created by marco on 9/14/16.
 */
public class TNode {

    private Node n;
    private Node father;
    private ArrayList<TNode> sons = new ArrayList<>();

    public TNode(Node n) {
        this.n = n;
    }

    public Node getFather() {
        return father;
    }

    public void setFather(Node father) {
        this.father = father;
    }

    public ArrayList<TNode> getSons() {
        return sons;
    }

    public void addSon(TNode son) {
        sons.add(son);
    }

    public Node getN() {

        return n;
    }

    public void setN(Node n) {
        this.n = n;
    }
}
