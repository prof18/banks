package com.digifarm.Graph;

import java.util.ArrayList;

/**
 * Created by marco on 7/7/16.
 **/

public class Node implements Comparable<Node> {


    private int searchID;
    private String tableName;
    private ArrayList<Node> adjacent;
    private double score;
    //weight for Dijstra
    private double weight;
    private Node previousNode;
    private boolean isKeywordNode = false;
    private ArrayList<Node> keywordNode = new ArrayList<>();

    /**
     * Build a new Node
     *
     * @param searchID  SearchID of the tuple
     * @param tableName Table's name of the selected tuple.
     */
    public Node(int searchID, String tableName) {
        this.searchID = searchID;
        this.tableName = tableName;
        adjacent = new ArrayList<Node>();
        score = 0;
    }

    /**
     * @return searchID    Returns the SearchID of the Node
     */
    public int getSearchID() {
        return searchID;
    }

    /**
     * @return tableName    Returns the TableName of the Node
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param n The node to be inserted in the adjacent list
     */
    public void addAdjacentNode(Node n) {
        adjacent.add(n);
    }

    /**
     * @return The list of adjacent Nodes is returned
     */
    public ArrayList<Node> getAdjacentNodes() {
        return adjacent;
    }

    public void incrementScore() {
        score++;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Node getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(Node previousNode) {
        this.previousNode = previousNode;
    }

    public boolean isKeywordNode() {
        return isKeywordNode;
    }

    public void setKeywordNode(boolean keywordNode) {
        isKeywordNode = keywordNode;
    }

    public ArrayList<Node> getKeywordNode() {
        return keywordNode;
    }

    public void addKeywordNode(Node node) {
        keywordNode.add(node);

    }

    @Override
    public int compareTo(Node node) {

        if (this.weight < node.weight)
            return -1;
        else if (this.weight > node.weight)
            return 1;
        else
            return 0;
    }

    @Override
    public String toString() {

        return "Node{" +
                "searchID=" + searchID +
                ", tableName='" + tableName + '\'' +
                ", score=" + score +
                ", weight=" + weight +
                '}';

    }
}
