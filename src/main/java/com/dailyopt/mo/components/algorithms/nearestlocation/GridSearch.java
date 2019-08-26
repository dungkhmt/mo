package com.dailyopt.mo.components.algorithms.nearestlocation;

import com.dailyopt.mo.components.movingobjects.ILocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GridSearch implements INearestLocationAlgorithm {

    private double lowerLat;
    private double lowerLng;
    private double upperLat;
    private double upperLng;
    private double latCellLen;
    private double lngCellLen;
    private int nbLatSeg;
    private int nbLngSeg;
    private IDistance disObj;
    private HashSet<ILocation>[][] clusters;
    private HashMap<ILocation, HashSet<ILocation>> mIL2Cluster;

    public GridSearch(double lowerLat, double lowerLng, double upperLat, double upperLng, int nbLatSeg, int nbLngSeg, IDistance disObj) {
        this.lowerLat = lowerLat;
        this.lowerLng = lowerLng;
        this.upperLat = upperLat;
        this.upperLng = upperLng;
        this.nbLatSeg = nbLatSeg;
        this.nbLngSeg = nbLngSeg;
        this.disObj = disObj;
        latCellLen = (upperLat - lowerLat) / nbLatSeg;
        lngCellLen = (upperLng - lowerLng) / nbLngSeg;
        clusters = new HashSet[nbLatSeg + 1][nbLngSeg + 1];
        for (int i = 0; i <= nbLatSeg; i++) {
            for (int j = 0; j <= nbLngSeg; j++) {
                clusters[i][j] = new HashSet<>();
            }
        }
        mIL2Cluster = new HashMap<>();
    }

    @Override
    public void updateLocation(ILocation p) {
        if (!mIL2Cluster.containsKey(p)) {
            addLocation(p);
        } else {
            mIL2Cluster.get(p).remove(p);
            addLocation(p);
        }
    }

    @Override
    public void addLocation(ILocation p) {
        int i = (int)((p.getLat() - lowerLat) / latCellLen);
        int j = (int)((p.getLng() - lowerLng) / lngCellLen);
        if (i < 0 || j < 0 || i > nbLatSeg || j > nbLngSeg) {
            System.out.println(name() +
                    "::addLocation -> Exception new location is not in the region " +
                    "p(" + p.getLat() + ", " + p.getLng() + "); " +
                    "region(" + lowerLat + ", " + lowerLng + ", " + upperLat + ", " + upperLng + ")");
            System.exit(-1);
        }
        clusters[i][j].add(p);
        mIL2Cluster.put(p, clusters[i][j]);
    }

    @Override
    public void removeLocation(ILocation p) {
        if (mIL2Cluster.containsKey(p)) {
            mIL2Cluster.get(p).remove(p);
            mIL2Cluster.remove(p);
        }
    }

    @Override
    public ILocation findNearestLocation(ILocation p) {
        return null;
    }

    @Override
    public ArrayList<ILocation> findKNearestLocations(ILocation p) {
        return null;
    }

    public String name() {
        return "GridSearch";
    }
}
