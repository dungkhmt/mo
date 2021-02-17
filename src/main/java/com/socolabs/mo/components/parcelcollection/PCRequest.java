package com.socolabs.mo.components.parcelcollection;

import com.socolabs.mo.components.algorithms.spatialindex.gtree.Vertex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class PCRequest {
    private int moment;
    private int weight;
    private Vertex location;
}
