package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by marco on 9/17/16.
 */
public class dbInfo {

    //set of nodes
    private HashMap<Integer,Node> nodes = new HashMap<>();
    //table list of the nodes set
    private ArrayList<String> tableList = new ArrayList<>();
    //column map of the nodes set
    //key is the table, value is the list of column
    private HashMap<String, ArrayList<String>> columnList = new HashMap<>();
    //list of found table
    private ArrayList<String> matchedTable = new ArrayList<>();

    public void setNodes(HashMap<Integer, Node> nodes) {
        this.nodes = nodes;
    }

    public void setTableList(ArrayList<String> tableList) {
        this.tableList = tableList;
    }

    public void setColumnList(HashMap<String, ArrayList<String>> columnList) {
        this.columnList = columnList;
    }

    public HashMap<Integer, Node> getNodes() {
        return nodes;
    }

    public ArrayList<String> getTableList() {
        return tableList;
    }

    public void setMatchedTable(ArrayList<String> matchedTable) {
        this.matchedTable = matchedTable;
    }

    public ArrayList<String> getMatchedTable() {
        return matchedTable;
    }

    public HashMap<String, ArrayList<String>> getColumnList() {
        return columnList;
    }
}
