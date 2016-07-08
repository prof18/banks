package com.digifarm.Graph;

/**
 * Created by marco on 7/7/16.
 **/

public class Node {

    private int searchID;
    private String tableName;

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
}
