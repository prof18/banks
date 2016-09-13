package com.digifarm.BESearch;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by digifarmer on 9/12/16.
 **/
public class SPIterator<Node> implements Comparable<SPIterator> {

    private ArrayList<Double> distance;
    private ArrayList<Node> list;

    /**
     *  Create an iterator with a list of Nodes and a list of distances.
     *  The distances in 0 is the distance between node 0 and node 1. The distance in 1 is the
     *  distance between node 0 and node 2 etc.
     *
     */
    public SPIterator() {
        list = new ArrayList<>();
        distance = new ArrayList<>();
    }

    public void add(Node n) {
        list.add(n);
    }

    /*
    public Node getNode() {
        //distance.remove(0);
        return list.get(0);
    }*/

    /*public void removeNode(int index) {
        list.remove(index);
    }*/

    public int getSize() {
        return list.size();
    }

    public void setDistance(double dist) {
        distance.add(dist);
    }

    public double getDistance(int position) {
        return distance.get(position);
    }

    public void deleteDistance() {
        distance.remove(0);
    }

    //create an iterator on the list.
    //TODO: reimplement the iterator
    public ListIterator<Node> createIterator(){
        ListIterator<Node> iterator = list.listIterator();
        return iterator;

    }

    //TODO: fix errors on sorting
    @Override
    public int compareTo(SPIterator spIterator) {

        int l1 = this.distance.size();
        int l2 = spIterator.distance.size();
        int minL = Math.min(l1,l2);

        for (int i = 0; i < minL; i++) {
            if ( (int) this.getDistance(i) < (int) spIterator.getDistance(i))
               return -1;
            else if ((int) this.getDistance(i) > (int) spIterator.getDistance(i))
                return 1;

        }

        return 0;

    }
}
