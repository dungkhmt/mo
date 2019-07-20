package com.dailyopt.mo.components.objectmanager;
import java.util.*;

import com.dailyopt.mo.components.maps.GISMap;
import com.dailyopt.mo.components.movingobjects.MovingObject;

public class ObjectManager implements ObjectManagerInterface{
	private ArrayList<MovingObject> objects;
	private Map<String, MovingObject> mID2Object;
	private List<ObjectCluster> clusters;
	
	public String name(){
		return "ObjectManager";
	}
	public ObjectManager(){
		objects = new ArrayList<MovingObject>();
		mID2Object = new HashMap<String, MovingObject>();
		//gismap = new GISMap();
	}
	public List<MovingObject> getObjects(){
		return objects;
	}
	public MovingObject addObject(MovingObject obj){
		objects.add(obj);
		System.out.println(name() + "::addObject, new object " + obj.getId() + ", sz = " + objects.size());
		mID2Object.put(obj.getId(), obj);
		return obj;
	}
	public MovingObject updateLocation(String ID, double lat, double lng){
		System.out.println(name() + "::updateLocation object " + ID + ", new location = (" + lat + "," + lng + ")");
		MovingObject obj = mID2Object.get(ID);
		obj.setLat(lat);
		obj.setLng(lng);
		return obj;
	}
}
