package com.digifarm.Graph;

/**
 * Created by marco on 7/7/16.
 **/

public class Node {

    private int searchID;
    private String tableName;

    public Node(int searchID, String tableName) {
        this.searchID = searchID;
        this.tableName = tableName;
    }

    public int getSearchID() {
        return searchID;
    }

    public String getTableName() {
        return tableName;
    }
}
