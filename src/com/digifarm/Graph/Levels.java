package com.digifarm.Graph;

import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by marco on 9/30/16.
 */
public class Levels {

    HashMap<Node,ArrayList<Node>> backward = new HashMap<>();
    HashMap<Node,ArrayList<Node>> forward = new HashMap<>();

    public HashMap<Node, ArrayList<Node>> getBackward() {
        return backward;
    }

    public void setBackward(HashMap<Node, ArrayList<Node>> backward) {
        this.backward = backward;
    }

    public HashMap<Node, ArrayList<Node>> getForward() {
        return forward;
    }

    public void setForward(HashMap<Node, ArrayList<Node>> forward) {
        this.forward = forward;
    }
}
