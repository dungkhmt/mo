package com.socolabs.mo.components.algorithms.nearestlocation;

import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.maps.graphs.Node;
import com.socolabs.mo.components.movingobjects.ILocation;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class QuadTree {
    public static int MIN_NB_POINT_PER_BLOCK = 10;
    public static int MAX_DEPTH = 20;

    double latLower;
    double latUpper;
    double lngLower;
    double lngUpper;
    private TreeNode root;
    private Collection<ILocation> points;

    public QuadTree(Collection<ILocation> points) {
        this.points = new HashSet<>(points);
        ILocation fp = points.iterator().next();
        latLower = fp.getLat();
        latUpper = fp.getLat();
        lngLower = fp.getLng();
        lngUpper = fp.getLng();
        for (ILocation p : points) {
            latLower = Math.min(latLower, p.getLat());
            latUpper = Math.max(latUpper, p.getLat());
            lngLower = Math.min(lngLower, p.getLng());
            lngUpper = Math.max(lngUpper, p.getLng());
        }
        initTree();
    }

    public QuadTree(Collection<Node> points, double latLower, double lngLower, double latUpper, double lngUpper) {
        this.points = new HashSet<>(points);
        this.latLower = latLower;
        this.latUpper = latUpper;
        this.lngLower = lngLower;
        this.lngUpper = lngUpper;
        initTree();
    }

    public QuadTree(double latLower, double lngLower, double latUpper, double lngUpper) {
        this.points = new HashSet<>();
        this.latLower = latLower;
        this.latUpper = latUpper;
        this.lngLower = lngLower;
        this.lngUpper = lngUpper;
        initTree();
    }

    public Collection<ILocation> getPointsOnWindow(double latLower, double lngLower, double latUpper, double lngUpper) {
        HashSet<ILocation> pointsOnWindow = new HashSet<>();
        root.getPointsOnWindow(pointsOnWindow, latLower, lngLower, latUpper, lngUpper);
        return pointsOnWindow;
    }

    public ILocation findNearestPoint(double lat, double lng) {
        double minDist = 1e18;
        ILocation nearestPoint = null;
        PriorityQueue<Pair<TreeNode, Double>> pq = new PriorityQueue<>(new Comparator<Pair<TreeNode, Double>>() {
            @Override
            public int compare(Pair<TreeNode, Double> o1, Pair<TreeNode, Double> o2) {
                if (o1.second < o2.second) {
                    return -1;
                }
                if (o1.second > o2.second) {
                    return 1;
                }
                return 0;
            }
        });
        pq.add(new Pair<>(root, root.estimateMinDist(lat, lng)));
        while (!pq.isEmpty()) {
            Pair<TreeNode, Double> td = pq.poll();
            if (td.second >= minDist) {
                break;
            }
            TreeNode t = td.first;
            if (t.getChildNodes().isEmpty()) {
                Pair<ILocation, Double> bestPoint = t.findNearestPoint(lat, lng);
                if (bestPoint.second < minDist) {
                    minDist = bestPoint.second;
                    nearestPoint = bestPoint.first;
                }
            } else {
                for (TreeNode c : t.getChildNodes()) {
                    pq.add(new Pair<>(c, c.estimateMinDist(lat, lng)));
                }
            }
        }
        return nearestPoint;
    }

    public ILocation findNearestPoint(String latlng) {
        String[] s = latlng.split(",");
        double lat = Double.valueOf(s[0].trim());
        double lng = Double.valueOf(s[1].trim());
        return findNearestPoint(lat, lng);
    }

    private void initTree() {
        System.out.println(name() + ":: initTree .....");
        double t0 = System.currentTimeMillis();
        root = buildTree(points, latLower, lngLower, latUpper, lngUpper, 1);
        double t = System.currentTimeMillis() - t0;
        System.out.println(name() + ":: initTree finished, time = " + (t / 1000) + "s");
    }

    private TreeNode buildTree(Collection<ILocation> points, double latLower, double lngLower, double latUpper, double lngUpper, int depth) {
        TreeNode t = new TreeNode(points, latLower, lngLower, latUpper, lngUpper);
        if (points.size() > MIN_NB_POINT_PER_BLOCK && depth < MAX_DEPTH) {
            double latMid = (latLower + latUpper) / 2;
            double lngMid = (lngLower + lngUpper) / 2;
            HashSet<ILocation> l1 = new HashSet<>();
            HashSet<ILocation> l2 = new HashSet<>();
            HashSet<ILocation> l3 = new HashSet<>();
            HashSet<ILocation> l4 = new HashSet<>();
            for (ILocation p : points) {
                if (p.getLat() < latMid) {
                    if (p.getLng() < lngMid) {
                        l1.add(p);
                    } else {
                        l2.add(p);
                    }
                } else {
                    if (p.getLng() < lngMid) {
                        l3.add(p);
                    } else {
                        l4.add(p);
                    }
                }
            }
            if (!l1.isEmpty()) {
                t.addChildNode(buildTree(l1, latLower, lngLower, latMid, lngMid, depth + 1));
            }
            if (!l2.isEmpty()) {
                t.addChildNode(buildTree(l2, latLower, lngMid, latMid, lngUpper, depth + 1));
            }
            if (!l3.isEmpty()) {
                t.addChildNode(buildTree(l3, latMid, lngLower, latUpper, lngMid, depth + 1));
            }
            if (!l4.isEmpty()) {
                t.addChildNode(buildTree(l4, latMid, lngMid, latUpper, lngUpper, depth + 1));
            }
        }
        return t;
    }

    private void add(TreeNode t, ILocation np, double latLower, double lngLower, double latUpper, double lngUpper, int depth) {
        t.add(np);
        double latMid = (latLower + latUpper) / 2;
        double lngMid = (lngLower + lngUpper) / 2;
        if (t.getChildNodes().isEmpty()) {
            Collection<ILocation> points = t.getPoints();
            if (points.size() > MIN_NB_POINT_PER_BLOCK && depth < MAX_DEPTH) {
                HashSet<ILocation> l1 = new HashSet<>();
                HashSet<ILocation> l2 = new HashSet<>();
                HashSet<ILocation> l3 = new HashSet<>();
                HashSet<ILocation> l4 = new HashSet<>();
                for (ILocation p : points) {
                    if (p.getLat() < latMid) {
                        if (p.getLng() < lngMid) {
                            l1.add(p);
                        } else {
                            l2.add(p);
                        }
                    } else {
                        if (p.getLng() < lngMid) {
                            l3.add(p);
                        } else {
                            l4.add(p);
                        }
                    }
                }
                if (!l1.isEmpty()) {
                    t.addChildNode(buildTree(l1, latLower, lngLower, latMid, lngMid, depth + 1));
                }
                if (!l2.isEmpty()) {
                    t.addChildNode(buildTree(l2, latLower, lngMid, latMid, lngUpper, depth + 1));
                }
                if (!l3.isEmpty()) {
                    t.addChildNode(buildTree(l3, latMid, lngLower, latUpper, lngMid, depth + 1));
                }
                if (!l4.isEmpty()) {
                    t.addChildNode(buildTree(l4, latMid, lngMid, latUpper, lngUpper, depth + 1));
                }
            }
        } else {
            boolean added = false;
            for (TreeNode c : t.getChildNodes()) {
                if (c.isInRange(np)) {
                    add(c, np, c.getLatLower(), c.getLngLower(), c.getLatUpper(), c.getLngUpper(), depth + 1);
                    added = true;
                    break;
                }
            }
            if (!added) {
                HashSet<ILocation> l = new HashSet<>();
                l.add(np);
                if (np.getLat() < latMid) {
                    if (np.getLng() < lngMid) {
                        t.addChildNode(buildTree(l, latLower, lngLower, latMid, lngMid, depth + 1));
                    } else {
                        t.addChildNode(buildTree(l, latLower, lngMid, latMid, lngUpper, depth + 1));
                    }
                } else {
                    if (np.getLng() < lngMid) {
                        t.addChildNode(buildTree(l, latMid, lngLower, latUpper, lngMid, depth + 1));
                    } else {
                        t.addChildNode(buildTree(l, latMid, lngMid, latUpper, lngUpper, depth + 1));
                    }
                }
            }
        }
    }

    public void add(ILocation p) {
        add(root, p, latLower, lngLower, latUpper, lngUpper, 1);
    }

    public void remove(ILocation p) {
        root.remove(p);
    }

    public String name() {
        return "QuadTree";
    }
}
