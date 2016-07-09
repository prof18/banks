package com.digifarm.Graph;

import java.util.ArrayList;

/**
 * Created by marco on 7/7/16.
 **/

public class Node {

    private int searchID;
    private String tableName;
    private ArrayList<Node> adjacent = new ArrayList<>();
    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<Edge> backEdges = new ArrayList<>();

    /**
     * Build a new Node
     *
     * @param searchID      SearchID of the tuple
     * @param tableName     Table's name of the selected tuple.
     */
    public Node(int searchID, String tableName) {
        this.searchID = searchID;
        this.tableName = tableName;
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
     * @return adjacent     Return the list of Adjacent Node
     */
    public ArrayList<Node> getAdjacent() {
        return adjacent;
    }

    /**
     *
     * @param adjacent      Set the list of Adjacent Node
     */
    public void setAdjacent(ArrayList<Node> adjacent) {
        this.adjacent = adjacent;
    }

    public ArrayList<Edge> getBackEdges() {
        return backEdges;
    }

    public void setBackEdges(ArrayList<Edge> backEdges) {
        this.backEdges = backEdges;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }
}
