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
    private HashSet<GNode> occurrenceList;

    private HashSet<Vertex> containingMOVertices;

    public GNode(int depth) {
        this.depth = depth;
        isLeaf = false;
        borders = new HashSet<>();
        parent = null;
        children = new ArrayList<>();
        occurrenceList = new HashSet<>();
        containingMOVertices = new HashSet<>();
    }

    public void addMovingObjectChild(GNode c) {
        occurrenceList.add(c);
    }

    public void removeMovingObjectChild(GNode c) {
        occurrenceList.remove(c);
    }

    public void addMovingObjectVertex(Vertex v) {
        containingMOVertices.add(v);
    }

    public void removeMovingObjectVertex(Vertex v) {
        containingMOVertices.remove(v);
    }

    public void addBorder(Vertex v) {
        borders.add(v);
    }

    public boolean isBorder(Vertex v) {
        return borders.contains(v);
    }

    public boolean isMovingObjectVertex(Vertex v) {
        return containingMOVertices.contains(v);
    }
}
