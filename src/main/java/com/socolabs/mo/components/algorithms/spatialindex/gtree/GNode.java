package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import java.util.HashMap;

public class GNode {
    private boolean isLeaf;
    private double[][] distMatrix;
    private HashMap<Vertex, Integer> mVertex2Index;
}
