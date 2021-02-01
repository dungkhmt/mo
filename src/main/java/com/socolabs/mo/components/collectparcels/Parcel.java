package com.socolabs.mo.components.collectparcels;

import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class Parcel implements Comparable<Parcel> {
    private Vertex location;
    private int weight;
    private int moment;

    @Override
    public int compareTo(@NotNull Parcel o) {
        return weight - o.getWeight();
    }
}
