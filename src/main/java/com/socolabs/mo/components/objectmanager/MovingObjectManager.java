package com.socolabs.mo.components.objectmanager;

import com.socolabs.mo.components.algorithms.nearestlocation.QuadTree;
import com.socolabs.mo.components.maps.GISMap;
import com.socolabs.mo.components.movingobjects.IMovingObject;
import com.socolabs.mo.components.movingobjects.MovingObject;
import com.socolabs.mo.model.modelmap.MapWindow;

import java.util.HashMap;

public class MovingObjectManager implements ObjectManagerInterface{

    private HashMap<String, IMovingObject> mId2MovingObjects;

    private QuadTree nearestObjFinder;

    public MovingObjectManager(double latLower, double lngLower, double latUpper, double lngUpper) {
        mId2MovingObjects = new HashMap<>();
        nearestObjFinder = new QuadTree(latLower, lngLower, latUpper, lngUpper);
    }

    public synchronized IMovingObject findNearestMovingObject(String latlng) {
        String[] s = latlng.split(",");
        double lat = Double.valueOf(s[0].trim());
        double lng = Double.valueOf(s[1].trim());
        return findNearestMovingObject(lat, lng);
    }

    public synchronized IMovingObject findNearestMovingObject(double lat, double lng) {
        return (IMovingObject) nearestObjFinder.findNearestPoint(lat, lng);
    }

    public static void main(String[] args) {
        GISMap gismap = new GISMap();
        MapWindow mw = gismap.computeCoordinateMapWindows();
        MovingObjectManager mgr = new MovingObjectManager(mw.getMinlat(), mw.getMinlng(), mw.getMaxlat(), mw.getMaxlng());
    }

    @Override
    public synchronized IMovingObject addObject(IMovingObject obj) {
        if (mId2MovingObjects.get(obj.getId()) != null) {
            return mId2MovingObjects.get(obj.getId());
        }
        mId2MovingObjects.put(obj.getId(), obj);
        nearestObjFinder.add(obj);
        System.out.println(name() + "::addObject, new object " + obj.getId() + ", size = " + mId2MovingObjects.size());
        return obj;
    }

    @Override
    public synchronized IMovingObject removeObject(IMovingObject obj) {
        nearestObjFinder.remove(obj);
        if (!mId2MovingObjects.containsKey(obj.getId())) {
            System.out.println(name() + "::removeObject Exception");
        }
        mId2MovingObjects.remove(obj.getId());
        System.out.println(name() + "::removeObject, removed object " + obj.getId() + ", size = " + mId2MovingObjects.size());
        return obj;
    }

    @Override
    public synchronized IMovingObject updateLocation(String ID, double lat, double lng) {
        if (!mId2MovingObjects.containsKey(ID)) {
            System.out.println(name() + "::updateLocation -> Exception " + ID + " doesn't exist in OM");
            System.exit(-1);
        }
        IMovingObject obj = mId2MovingObjects.get(ID);
        nearestObjFinder.remove(obj);
        obj.setLat(lat);
        obj.setLng(lng);
        nearestObjFinder.add(obj);
        return obj;
    }

    public String name() {
        return "MovingObjectManager";
    }
}