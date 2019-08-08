package com.dailyopt.mo.components.objectmanager;
import java.util.*;

import org.springframework.web.bind.annotation.RequestBody;

import com.dailyopt.mo.components.api.updateplannedroutetruck.UpdatePlannedRouteTruckInput;
import com.dailyopt.mo.components.api.updateservicepoints.UpdateServicePointsInput;
import com.dailyopt.mo.components.maps.GISMap;
import com.dailyopt.mo.components.maps.Point;
import com.dailyopt.mo.components.movingobjects.IServicePoint;
import com.dailyopt.mo.components.movingobjects.MovingObject;
import com.dailyopt.mo.components.movingobjects.RouteSegmentToServicePoint;
import com.dailyopt.mo.components.movingobjects.truck.ServicePointDelivery;
import com.dailyopt.mo.components.movingobjects.truck.Truck;
import com.dailyopt.mo.controller.ApiController;
import com.dailyopt.mo.model.routevrp.Route;
import com.dailyopt.mo.model.routevrp.RouteVRPInputPoint;
import com.google.gson.Gson;

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
	public List<Truck> getTrucks(){
		ArrayList<Truck> L = new ArrayList<Truck>();
		for(MovingObject o: objects){
			if(o instanceof Truck) L.add((Truck)o);
		}
		return L;
	}
	public MovingObject addObject(MovingObject obj){
		if(mID2Object.get(obj.getId())!=null){
			return mID2Object.get(obj.getId());
		}
		objects.add(obj);
		System.out.println(name() + "::addObject, new object " + obj.getId() + ", sz = " + objects.size());
		mID2Object.put(obj.getId(), obj);
		return obj;
	}
	public MovingObject updateLocation(String ID, double lat, double lng){
		System.out.println(name() + "::updateLocation object " + ID + ", new location = (" + lat + "," + lng + ")");
		MovingObject obj = mID2Object.get(ID);
		if(obj == null) return null;
		obj.setLat(lat);
		obj.setLng(lng);
		return obj;
	}
	public String updateServicePoints(UpdateServicePointsInput input){
		MovingObject o = mID2Object.get(input.getObjectId());
		if(o == null) return null;
		if(o instanceof Truck){
			Truck t = (Truck)o;
			t.updateServicePoints(input.getServicePoints());
			return t.toString();
			//Gson gson = new Gson();
			//return gson.toJson(t);
		}
		return "{}";
	}
	public String updatePlannedRouteTruck(UpdatePlannedRouteTruckInput input){
		Truck t = (Truck)mID2Object.get(input.getId());
		if(t == null) return "{}";
		
		Route route = input.getRoute();
		RouteSegmentToServicePoint[] routeSegments = new RouteSegmentToServicePoint[route.getPaths().length];
		for(int i = 0; i < routeSegments.length; i++){
			RouteVRPInputPoint p = route.getPoints()[i];
			Point[] mapPoints = route.getPaths()[i].getPoints();
			IServicePoint servicePoint = new ServicePointDelivery(p.getId(),p.getLat(),p.getLng());
			double length = route.getPaths()[i].getLength();
			routeSegments[i]=new RouteSegmentToServicePoint(mapPoints, servicePoint, length);
		}
		t.setSpecifiedRoutes(routeSegments);
		
		return ApiController.gson.toJson(t);
		
	}
	public Truck getTruckById(String id){
		
    	MovingObject o = mID2Object.get(id);
    	System.out.println(name() + "::getTruckById, id = " + id + " o = " + (o != null ? o.getId() : "NULL"));
    	if(o instanceof Truck) return (Truck)o;
    	return null;
    }
}
