package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Getter
@Setter
public class GNode {
    private int depth;
    private boolean isLeaf;
    private HashSet<Vertex> borders;
    private HashSet<Vertex> childBorders;

    private GNode parent;
    private ArrayList<GNode> children;

    public GNode(int depth) {
        this.depth = depth;
        isLeaf = false;
        borders = new HashSet<>();
        parent = null;
        children = new ArrayList<>();
    }

    public void addBorder(Vertex v) {
        borders.add(v);
    }

    public boolean isBorder(Vertex v) {
        return borders.contains(v);
    }
}
