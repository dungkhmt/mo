package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Edge {
    private Vertex startPoint;
    private Vertex endPoint;
    private double weight;

    public String toString() {
        return "(" + startPoint + " -> " + endPoint + " :: " + weight + ")";
    }
}
