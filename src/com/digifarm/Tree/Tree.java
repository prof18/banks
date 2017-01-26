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

package com.digifarm.Tree;

import com.digifarm.Graph.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by digifarmer on 9/14/16.
 **/
public class Tree implements Comparable<Tree> {

    private Node root;
    //key is father, value is list of sons
    private HashMap<Node, ArrayList<Node>> sons;
    //key is son, value is father
    private HashMap<Node, Node> father;

    private double nodeScore;
    private double edgeScore;
    private double globalScore;


    public Tree() {
        sons = new HashMap<>();
        father = new HashMap<>();
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {

        this.root = root;
    }

    public void addSon(Node current, Node son) {

        ArrayList<Node> toAdd = new ArrayList<>();

        if (sons.containsKey(current)) {
            for (Node n : sons.get(current)) {
                if (n == son) {
                    break;
                } else {
                    toAdd.add(son);
                }
            }
            for (Node n : toAdd) {
                sons.get(current).add(son);
            }

        } else {
            ArrayList<Node> sonList = new ArrayList<>();
            sonList.add(son);
            sons.put(current, sonList);
        }
    }

    public HashMap<Node, ArrayList<Node>> getSons() {
        return sons;
    }

    public void addFather(Node ancenstor, Node current) {
        father.put(current,ancenstor);
    }

    public double getNodeScore() {
        return nodeScore;
    }

    public void setNodeScore(double nodeScore) {
        this.nodeScore = nodeScore;
    }

    public double getEdgeScore() {
        return edgeScore;
    }

    public void setEdgeScore(double edgeScore) {
        this.edgeScore = edgeScore;
    }

    public double getGlobalScore() {
        return globalScore;
    }

    public void setGlobalScore(double globalScore) {
        this.globalScore = globalScore;
    }

    @Override
    public int compareTo(Tree tree) {

        if (Double.compare(this.getGlobalScore(), tree.getGlobalScore()) > 0)
            return -1;
        else if (Double.compare(this.getGlobalScore(), tree.getGlobalScore()) < 0)
            return 1;
        else
            return 0;
    }

    @Override
    public String toString() {

        double score = getGlobalScore();
        if (Double.isNaN(score)) {
            score = 0;
        }
        String nodeId = "Root " + getRoot().getSearchID();
        String FatherAndSons = " ";

        for(Map.Entry<Node, ArrayList<Node>> entry : getSons().entrySet() ) {

            FatherAndSons += '\n' + "Father : " + entry.getKey().getSearchID() + " Sons : ";

            for( Node e : entry.getValue()) {

                if (e != null)
                    FatherAndSons +=  e.getSearchID() + " ";
                else
                    FatherAndSons += "Hasn't sons";
            }
        }

        String Tree ="-----------------------------" + '\n' +
                "Tree " + '\n' +
                "Score " + score + '\n' +
                nodeId +
                FatherAndSons + '\n' ;

        return Tree;
    }



}
