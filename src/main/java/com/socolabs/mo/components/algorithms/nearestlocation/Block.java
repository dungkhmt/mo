package com.socolabs.mo.components.algorithms.nearestlocation;

import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.maps.utils.GoogleMapsQuery;
import com.socolabs.mo.components.movingobjects.ILocation;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class Block {
    public static GoogleMapsQuery G = new GoogleMapsQuery();

    private double latUpper;
    private double latLower;
    private double lngUpper;
    private double lngLower;
    private Collection<ILocation> points;

    public Block(Collection<ILocation> points) {
        this.points = points;
        ILocation fp = points.iterator().next();
        latLower = latUpper = fp.getLat();
        lngLower = lngUpper = fp.getLng();
        for (ILocation p : points) {
            latLower = Math.min(latLower, p.getLat());
            latUpper = Math.max(latUpper, p.getLat());
            lngLower = Math.min(lngLower, p.getLng());
            lngUpper = Math.max(lngUpper, p.getLng());
        }
    }

    public Block(Collection<ILocation> points, double latLower, double lngLower, double latUpper, double lngUpper) {
        this.points = points;
        this.latLower = latLower;
        this.latUpper = latUpper;
        this.lngLower = lngLower;
        this.lngUpper = lngUpper;
    }

    public void add(ILocation p) {
        points.add(p);
    }

    public void remove(ILocation p) {
        points.remove(p);
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public int size() {
        return points.size();
    }

    public boolean isInRange(ILocation p) {
        double lat = p.getLat();
        double lng = p.getLng();
        return latLower <= lat && lat <= latUpper && lngLower <= lng && lng <= lngUpper;
    }

    public boolean isInside(double lat1, double lng1, double lat2, double lng2) {
        return lat1 <= latLower && latUpper <= lat2 && lng1 <= lngLower && lngUpper <= lng2;
    }

    public boolean isOverlap(double lat1, double lng1, double lat2, double lng2) {
        return lat1 <= latUpper && lat2 >= latLower && lng1 <= lngUpper && lng2 >= lngLower;
    }

    public boolean contains(ILocation p) {
        if (isInRange(p)) {
            return points.contains(p);
        }
        return false;
    }

    public Pair<ILocation, Double> findNearestPoint(double lat, double lng) {
        double bestDist = 1e18;
        ILocation bestPoint = null;
        for (ILocation p : points) {
            double dist = G.computeDistanceHaversine(lat, lng, p.getLat(), p.getLng());
            if (dist < bestDist) {
                bestDist = dist;
                bestPoint = p;
            }
        }
        return new Pair<>(bestPoint, bestDist);
    }

    public double estimateMinDist(double lat, double lng) {
        if (lat >= latLower && lat <= latUpper && lng >= lngLower && lng <= lngUpper) {
            return 0;
        }
        if (lat <= latLower && lng <= lngLower) {
            return G.computeDistanceHaversine(lat, lng, latLower, lngLower);
        }
        if (lat <= latLower && lng >= lngUpper) {
            return G.computeDistanceHaversine(lat, lng, latLower, lngUpper);
        }
        if (lat >= latUpper && lng <= lngLower) {
            return G.computeDistanceHaversine(lat, lng, latUpper, lngLower);
        }
        if (lat >= latUpper && lng >= lngUpper) {
            return G.computeDistanceHaversine(lat, lng, latUpper, lngUpper);
        }
        if (lat < latLower && lng >= lngLower && lng <= lngUpper) {
            return G.computeDistanceHaversine(lat, lng, latLower, lng);
        }
        if (lat > latUpper && lng >= lngLower && lng <= lngUpper) {
            return G.computeDistanceHaversine(lat, lng, latUpper, lng);
        }
        if (lng < lngLower && lat >= latLower && lat <= latUpper) {
            return G.computeDistanceHaversine(lat, lng, lat, lngLower);
        }
        if (lng > lngUpper && lat >= latLower && lat <= latUpper) {
            return G.computeDistanceHaversine(lat, lng, lat, lngUpper);
        }
        return 0;
    }
}
