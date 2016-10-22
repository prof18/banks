package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by digifarmer on 9/30/16.
 **/
public class Levels {

    /*
    *   Key     --> The Backward node
    *   Value   --> List of the nodes pointed by the key
    *
    *   We find the backward nodes starting from the node in the list. We populate
    *   the map in this way, because it'll be more easy to create the edge.
    */
    private HashMap<Node, ArrayList<Node>> backward = new HashMap<>();
    /*
    *   Key     --> The input node
    *   Value   --> List of the forward nodes of the key
    */
    private HashMap<Node, ArrayList<Node>> forward = new HashMap<>();

    public HashMap<Node, ArrayList<Node>> getBackward() {
        return backward;
    }

    public HashMap<Node, ArrayList<Node>> getForward() {
        return forward;
    }

}
