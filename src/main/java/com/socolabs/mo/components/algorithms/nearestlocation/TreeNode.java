package com.socolabs.mo.components.algorithms.nearestlocation;

import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.movingobjects.ILocation;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public class TreeNode extends Block {
    private ArrayList<TreeNode> childNodes;

    public TreeNode(Collection<ILocation> points) {
        super(points);
        childNodes = new ArrayList<>();
    }

    public TreeNode(Collection<ILocation> points, double latLower, double lngLower, double latUpper, double lngUpper) {
        super(points, latLower, lngLower, latUpper, lngUpper);
        childNodes = new ArrayList<>();
    }

    public void addChildNode(TreeNode c) {
        childNodes.add(c);
    }

    public void remove(ILocation p) {
        super.remove(p);
        ArrayList<TreeNode> delLst = new ArrayList<>();
        for (TreeNode c : childNodes) {
            if (c.contains(p)) {
                c.remove(p);
                if (c.isEmpty()) {
                    delLst.add(c);
                }
            }
        }
        childNodes.removeAll(delLst);
    }
}
