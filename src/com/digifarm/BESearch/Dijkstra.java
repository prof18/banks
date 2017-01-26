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

package com.digifarm.BESearch;

import com.digifarm.Graph.Edge;
import com.digifarm.Graph.Graph;
import com.digifarm.Graph.Node;

import java.util.*;


/**
 * Created by digifarmer on 7/7/16.
 **/
public class Dijkstra {

    //infinite value
    private static final Integer INFINITE = Integer.MAX_VALUE;
    private HashMap<Integer, Node> nodes = new HashMap<>();
    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<Edge> bedges = new ArrayList<>();
    private ArrayList<Node> adjacent;
    //node queue with weight as priority
    private PriorityQueue<Node> nodesQueue = new PriorityQueue<>();
    //list of edge and backedge
    private ArrayList<Edge> edgeList = new ArrayList<>();
    private Node start;
    //Iterator
    private SPIterator spIt;

    public Dijkstra(Graph graph, Node start, SPIterator it){

        nodes = graph.getNodeSet();
        edges = graph.getEdge();
        bedges = graph.getBedge();
        this.start = start;
        spIt = it;
        //add starting node to the iterator
        spIt.add(start);
        spIt.addPrevious(start,null);
        spIt.setOrigin(start);

        //edges will contain the edge and also the backedge
        for (Edge e : edges)
            edgeList.add(e);

        for (Edge be : bedges)
            edgeList.add(be);

        start.setWeight(0);

        //starting node has zero weight, the others infinite
        Node n;
        for (Map.Entry<Integer, Node> e : nodes.entrySet()) {
                n = e.getValue();
                n.setWeight(INFINITE);
            if(n.getSearchID() == start.getSearchID())
                n.setWeight(0);
            nodesQueue.add(n);
        }
    }

    public void visit() {

        double finalWeight;
        //this map is used to avoid duplicates nodes
        HashMap<Integer, Node> nodeMap = new HashMap<>();

        while (!nodesQueue.isEmpty() && nodesQueue.peek().getWeight() != INFINITE) {

            Node minimum = nodesQueue.poll();
            adjacent = new ArrayList<>(minimum.getAdjacentNodes());

            nodeMap.put(minimum.getSearchID(),minimum);
            double startWeight = minimum.getWeight();

            //necessary check to avoid nodes revisiting.
            //the father has as adjacent the son and vice versa
            for (Node n : minimum.getAdjacentNodes()) {
                if(nodeMap.containsKey(n.getSearchID())) {
                    adjacent.remove(n);
                }
            }

            for (Node to : adjacent) {
                for (Edge edge : edgeList) {
                    //we traverse the graph in reverse direction
                    if (edge.getTo().getSearchID() == minimum.getSearchID() && edge.getFrom().getSearchID() == to.getSearchID()) {
                        finalWeight = startWeight + edge.getScore();
                        if (finalWeight < to.getWeight()) {
                            nodesQueue.remove(to);
                            to.setWeight(finalWeight);
                            to.setPreviousNode(minimum);
                            spIt.add(to);
                            spIt.addPrevious(to,minimum);
                            spIt.setDistance(finalWeight);
                            nodesQueue.add(to);
                        }
                    }
                }
            }
        }
    }
}