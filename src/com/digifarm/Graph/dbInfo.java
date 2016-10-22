package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by digifarme on 9/17/16.
 *
 * It's a class that contains some db information, like tables and tuples
 **/
public class dbInfo {

    //set of nodes
    private HashMap<Integer,Node> nodes = new HashMap<>();
    //table list of the nodes set
    private ArrayList<String> tableList = new ArrayList<>();

    public void setNodes(HashMap<Integer, Node> nodes) {
        this.nodes = nodes;
    }

    public void setTableList(ArrayList<String> tableList) {
        this.tableList = tableList;
    }

    public HashMap<Integer, Node> getNodes() {
        return nodes;
    }

    public ArrayList<String> getTableList() {
        return tableList;
    }

}
