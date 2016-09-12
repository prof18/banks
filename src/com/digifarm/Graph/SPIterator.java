package com.digifarm.Graph;

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by marco on 9/12/16.
 */
public class SPIterator<Node> implements Comparable<SPIterator> {

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

    public double getDistance(int position) {
        return distance.get(position);
    }

    public void deleteDistance() {
        distance.remove(0);
    }

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
            if ( this.getDistance(i) < spIterator.getDistance(i))
               return -1;
            else if (this.getDistance(i) > spIterator.getDistance(i))
                return 1;

        }

        return 0;

    }
}
