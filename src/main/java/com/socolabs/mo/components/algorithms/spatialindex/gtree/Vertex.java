package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.movingobjects.ILocation;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class Vertex extends Point {
    private int id;
    private int index;
    private HashMap<GNode, Integer> gnodeIndex;
    private GNode cacheQueryNode;
    private int cacheIndex;

    public Vertex(int id, double lat, double lng) {
        super(lat, lng);
        this.id = id;
        gnodeIndex = new HashMap<>();
    }

    public void setGNodeIndex(GNode g, int index) {
        gnodeIndex.put(g, index);
    }

    public int getGNodeIndex(GNode g) {
//        if (cacheQueryNode != g) {
//            cacheQueryNode = g;
//            cacheIndex = gnodeIndex.get(g);
//        }
//        return cacheIndex;
        return gnodeIndex.get(g);
    }

    public String toString() {
        return "" + id;
    }

    public void clear() {
        cacheIndex = 0;
        cacheQueryNode = null;
        gnodeIndex.clear();
    }
}
