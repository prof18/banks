package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by digifarmer on 7/7/16.
 **/

public class Node implements Comparable<Node> {

    private int searchID;
    private String tableName;
    private ArrayList<Node> adjacent;
    private double score;
    //weight for Dijstra500
    private double weight;
    private Node previousNode;
    private boolean isKeywordNode = false;
    //lista di keyword
    private ArrayList<String> keywordList = new ArrayList<>();
    //v.Li node list for each keyword
    private HashMap<String, ArrayList<Node>> vLi = new HashMap<>();
    //The node is already visited by an iterator
    private boolean isVisited = false;

    public Node(int searchID, String tableName) {
        this.searchID = searchID;
        this.tableName = tableName;
        adjacent = new ArrayList<>();
        score = 0;
    }

    public int getSearchID() {
        return searchID;
    }

    public String getTableName() {
        return tableName;
    }

    public void addAdjacentNode(Node n) {
        adjacent.add(n);
    }

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

    public void setVisited(boolean visit) {
        isVisited = visit;
    }

    public boolean isVisited() {
        return isVisited;
    }

    /**
     * This method merge the adjacent list and the keyword list of two nodes (that they'll be the same btw)
     *
     * @param adjacent2
     * @param keyword
     */
    public void mergeNode(ArrayList<Node> adjacent2, String keyword) {
        ArrayList<Node> adjacent1 = this.getAdjacentNodes();
        ArrayList<String> keyword1 = this.getKeywordList();
        //update adjacent list of the current node
        for (Node n : adjacent2) {
            if (!adjacent1.contains(n))
                adjacent1.add(n);
        }

        //update the keyword list of the current node
        //TODO: CONTROLLARE NEL MAIN CHE LE PAROLE CHIAVE SIANO UNICHE. Ad esempio scrivi milan milan
        if (!keyword1.contains(keyword))
            keyword1.add(keyword);
    }

    public void addKeyword(String keyword) {
        keywordList.add(keyword);
    }

    public ArrayList<String> getKeywordList() {
        return keywordList;
    }

    /**
     *  Craete the nodelist v.Li, one for each keyword
     *
     * @param keyword   The current keyword
     */
    public void createVLi(String keyword) {
        ArrayList<Node> nodes = new ArrayList<>();
        vLi.put(keyword,nodes);
    }

    public HashMap<String, ArrayList<Node>> getvLi() {
        return vLi;
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
