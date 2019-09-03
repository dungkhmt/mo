package com.dailyopt.mo.components.algorithms.nearestlocation;

import com.dailyopt.mo.components.maps.Point;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class TreeNode {
    private Block block;
    private ArrayList<TreeNode> childNodes;

    public TreeNode(ArrayList<Point> points) {
        block = new Block(points);
        childNodes = new ArrayList<>();
    }

    public void addChildNode(TreeNode c) {
        childNodes.add(c);
    }
}
