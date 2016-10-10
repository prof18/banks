package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by digifarmer on 9/30/16.
 **/
public class Levels {

    private HashMap<Node, ArrayList<Node>> backward = new HashMap<>();
    private HashMap<Node, ArrayList<Node>> forward = new HashMap<>();

    public HashMap<Node, ArrayList<Node>> getBackward() {
        return backward;
    }

    public HashMap<Node, ArrayList<Node>> getForward() {
        return forward;
    }

}
