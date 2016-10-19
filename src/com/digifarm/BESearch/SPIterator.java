package com.digifarm.BESearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * Created by digifarmer on 9/12/16.
 **/
public class SPIterator<Node> implements Comparable<SPIterator> {

    private ArrayList<Double> distance;
    private ArrayList<Node> list;
    private Node origin;
    //map to take reference of the previous node.
    private HashMap<Node, Node> previousList;

    /**
     *  Create an iterator with a list of Nodes and a list of distances.
     *  The distances in 0 is the distance between node 0 and node 1. The distance in 1 is the
     *  distance between node 0 and node 2 etc.
     *
     */
    public SPIterator() {
        list = new ArrayList<>();
        distance = new ArrayList<>();
        previousList = new HashMap<>();
    }

    public void add(Node n) {
        list.add(n);
    }

    public void setDistance(double dist) {
        distance.add(dist);
    }

    public double getDistance(int position) {
        return distance.get(position);
    }

    public void deleteDistance() {
        if (distance.size() != 0)
            distance.remove(0);
    }

    public Node getOrigin() {
        return  origin;
    }

    public void setOrigin(Node n) {
        origin = n;
    }


    public void addPrevious(Node current, Node previous) {
        previousList.put(current,previous);
    }

    public HashMap<Node, Node> getPreviousList() {
        return previousList;
    }

    //create an iterator on the list.
    public ListIterator<Node> createIterator(){
        ListIterator<Node> iterator = list.listIterator();
        return iterator;
    }

    @Override
    public int compareTo(SPIterator spIterator) {

        int l1 = this.distance.size();
        int l2 = spIterator.distance.size();
        int minL = Math.min(l1,l2);

        for (int i = 0; i < minL; i++) {
            if( Double.compare(this.getDistance(i), spIterator.getDistance(i)) < 0)
               return -1;
            else if( Double.compare(this.getDistance(i), spIterator.getDistance(i)) > 0)
                return 1;
        }
        return 0;
    }
}
