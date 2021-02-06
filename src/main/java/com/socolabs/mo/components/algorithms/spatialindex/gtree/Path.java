package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
public class Path {
    private ArrayList<Vertex> vertices;
    private double length;

    public Path() {
        vertices = new ArrayList<>();
        length = 0;
    }

    public Path(double length) {
        this.length = length;
        vertices = new ArrayList<>();
    }

    public void add(Vertex v) {
        vertices.add(v);
    }

    public String toString() {
        String ret = "";
        for (Vertex v : vertices) {
            ret += v + ", ";
        }
        return ret;
    }
}
