package com.digifarm.Graph;

import java.util.ArrayList;

/**
 * Created by marco on 7/7/16.
 **/

public class Node {

    private int searchID;
    private String tableName;
    private ArrayList<Node> adjacent;
    private int weight;

    /**
     * Build a new Node
     *
     * @param searchID      SearchID of the tuple
     * @param tableName     Table's name of the selected tuple.
     */
    public Node(int searchID, String tableName) {
        this.searchID = searchID;
        this.tableName = tableName;
        adjacent = new ArrayList<Node>();
        weight = 0;
    }

    /**
     *
     * @return  searchID    Returns the SearchID of the Node
     */
    public int getSearchID() {
        return searchID;
    }

    /**
     *
     * @return tableName    Returns the TableName of the Node
     */
    public String getTableName() {
        return tableName;
    }

    /**
     *
     * @param n     The node to be inserted in the adjacent list
     */
    public void addAdjacentNode(Node n) {
        adjacent.add(n);
    }

    /**
     *
     * @return      The list of adjacent Nodes is returned
     */
    public ArrayList<Node> getAdjacentNodes() {
        return adjacent;
    }

    public void incrementWeight() {
        weight++;
    }

    public int getWeight() {
        return weight;
    }
}
