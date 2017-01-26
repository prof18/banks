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

/**
 * Created by digifarmer on 7/7/16.
 **/
public class Edge {

    private double score;
    private Node from;
    private Node to;

    /**
     * Build a new Edge between two Node.
     *
     * @param from      Starting Node
     * @param to        Destination Node
     * @param weight    Edge's Weight
     */
    public Edge(Node from, Node to, int weight) {

        this.from = from;
        this.to = to;
        this.score = weight;

    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "score=" + score +
                ", from=" + from.getSearchID() +
                ", to=" + to.getSearchID() +
                '}';
    }
}
