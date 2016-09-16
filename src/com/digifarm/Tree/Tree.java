package com.digifarm.Tree;

import com.digifarm.Graph.Edge;
import com.digifarm.Graph.Node;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by marco on 9/14/16.
 */
public class Tree {

    private Node root;
    //list of tree node
    //private ArrayList<Node> nodeList;
    private HashMap<Node, ArrayList<Node>> sons;
    private HashMap<Node, Node> father;

    public Tree() {
        sons = new HashMap<>();
        father = new HashMap<>();
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {

        this.root = root;
    }

    public void addSon(Node current, Node son) {

        //is the root
        //if (current != null) {

            if (sons.containsKey(current)) {
                for (Node n : sons.get(current)) {
                    if (n == son) {
                        break;
                    } else {
                        sons.get(current).add(son);
                    }
                }

            } else {
                ArrayList<Node> sonList = new ArrayList<>();
                sonList.add(son);
                sons.put(current, sonList);
            }
      //  }


    }

    public HashMap<Node, ArrayList<Node>> getSons() {
        return sons;
    }

    public void addFather(Node ancenstor, Node current) {
        father.put(current,ancenstor);
    }
}
