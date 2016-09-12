package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by marco on 9/12/16.
 */
public class SPIterator<Node> {

    //private int size = 0;
    //private static final int DEFAULT_CAPACITY = 10;
    private ArrayList<Double> distance;
    private ArrayList<Node> list;

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

    /**
     *
     * Es. nella posizione 0, c'è la distanza tra il nodo 0 e il nodo 1 e così via.
     *
     * @param dist
     */
    public void setDistance(double dist) {
        distance.add(dist);
    }

    public double getDistance() {
        return distance.get(0);
    }

    public void deleteDistance() {
        distance.remove(0);
    }

    public ListIterator<Node> createIterator(){
        ListIterator<Node> iterator = list.listIterator();
        return iterator;

    }
}
