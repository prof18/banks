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
 * Created by digifarmer on 7/7/16.
 **/
public class Graph {

    HashMap<Integer, Node> nodeSet;
    ArrayList<Edge> edge = new ArrayList<>();
    ArrayList<Edge> bedge = new ArrayList<>();

    /**
     *  Create new Graph, a wrapper for Node map, edge list and backedge list
     *
     * @param nodeSet   Map of nodes
     * @param edge      List of Edge
     * @param bedge     List of Backedge
     */
    public Graph(HashMap<Integer, Node> nodeSet, ArrayList<Edge> edge, ArrayList<Edge> bedge) {
        this.nodeSet = nodeSet;
        this.edge = edge;
        this.bedge = bedge;
    }

    public HashMap<Integer, Node> getNodeSet() {
        return nodeSet;
    }

    public ArrayList<Edge> getEdge() {
        return edge;
    }

    public ArrayList<Edge> getBedge() {
        return bedge;
    }
}
