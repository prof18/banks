/*
*   Copyright 2017 Marco Gomiero, Luca Rossi
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
*/

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
