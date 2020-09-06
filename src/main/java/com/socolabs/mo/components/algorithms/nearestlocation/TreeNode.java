package com.socolabs.mo.components.algorithms.nearestlocation;

import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.movingobjects.ILocation;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

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


    public void getPointsOnWindow(HashSet<ILocation> windowPoints, double latLower, double lngLower, double latUpper, double lngUpper) {
        if (isInside(latLower, lngLower, latUpper, lngUpper)) {
            windowPoints.addAll(getPoints());
        }
        for (TreeNode tn : childNodes) {
            if (tn.isOverlap(latLower, lngLower, latUpper, lngUpper)) {
                tn.getPointsOnWindow(windowPoints, latLower, lngLower, latUpper, lngUpper);
            }
        }
        if (childNodes.size() == 0 && size() > 0) {
            for (ILocation p : getPoints()) {
                if (latLower <= p.getLat() && p.getLat() <= latUpper && lngLower <= p.getLng() && p.getLng() <= lngUpper) {
                    windowPoints.add(p);
                }
            }
        }
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
