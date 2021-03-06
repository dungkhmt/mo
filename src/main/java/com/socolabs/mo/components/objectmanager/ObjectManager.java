package com.socolabs.mo.components.objectmanager;
import java.util.*;

import com.socolabs.mo.components.api.updateplannedroutetruck.UpdatePlannedRouteTruckInput;
import com.socolabs.mo.components.movingobjects.IMovingObject;
import com.socolabs.mo.components.movingobjects.truck.ServicePointDelivery;
import com.socolabs.mo.components.movingobjects.truck.Truck;

import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.movingobjects.IServicePoint;
import com.socolabs.mo.components.movingobjects.MovingObject;
import com.socolabs.mo.components.movingobjects.RouteSegmentToServicePoint;
import com.socolabs.mo.controller.ApiController;
import com.socolabs.mo.model.routevrp.Route;
import com.socolabs.mo.model.routevrp.RouteVRPInputPoint;

public class ObjectManager implements ObjectManagerInterface{
	private ArrayList<IMovingObject> objects;
	private Map<String, IMovingObject> mID2Object;
	private List<ObjectCluster> clusters;
	
	public String name(){
		return "ObjectManager";
	}
	public ObjectManager(){
		objects = new ArrayList<IMovingObject>();
		mID2Object = new HashMap<String, IMovingObject>();
		//gismap = new GISMap();
	}
	public List<IMovingObject> getObjects(){
		return objects;
	}
	public List<Truck> getTrucks(){
		ArrayList<Truck> L = new ArrayList<Truck>();
		for(IMovingObject o: objects){
			if(o instanceof Truck) L.add((Truck)o);
		}
		return L;
	}
	public IMovingObject addObject(IMovingObject obj){
		if(mID2Object.get(obj.getId())!=null){
			return mID2Object.get(obj.getId());
		}
		objects.add(obj);
		System.out.println(name() + "::addObject, new object " + obj.getId() + ", sz = " + objects.size());
		mID2Object.put(obj.getId(), obj);
		return obj;
	}

	@Override
	public IMovingObject removeObject(IMovingObject obj) {
		return null;
	}

	public IMovingObject updateLocation(String ID, double lat, double lng){
		System.out.println(name() + "::updateLocation object " + ID + ", new location = (" + lat + "," + lng + ")");
		IMovingObject obj = mID2Object.get(ID);
		if(obj == null) return null;
		obj.setLat(lat);
		obj.setLng(lng);
		return obj;
	}
	/*
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
	*/
	public String updatePlannedRouteTruck(UpdatePlannedRouteTruckInput input){
		Truck t = (Truck)mID2Object.get(input.getId());
		if(t == null) return "{}";
		
		Route route = input.getRoute();
		RouteSegmentToServicePoint[] routeSegments = new RouteSegmentToServicePoint[route.getPaths().length];
		
		for(int i = 0; i < routeSegments.length; i++){
			RouteVRPInputPoint p = route.getPoints()[i+1];
			Point[] mapPoints = route.getPaths()[i].getPoints();
			IServicePoint servicePoint = new ServicePointDelivery(p.getId(),p.getLat(),p.getLng());
			double length = route.getPaths()[i].getLength();
			routeSegments[i]=new RouteSegmentToServicePoint(mapPoints, servicePoint, length);
		}
		t.setSpecifiedRoutes(routeSegments);
		IServicePoint[] servicePoints = new IServicePoint[routeSegments.length];
		for(int i = 0; i < servicePoints.length; i++){
			servicePoints[i] = routeSegments[i].getServicePoint();
		}
		t.setServicePoints(servicePoints);

		return ApiController.gson.toJson(t);
		
	}
	public Truck getTruckById(String id){
		
    	IMovingObject o = mID2Object.get(id);
    	//System.out.println(name() + "::getTruckById, id = " + id + " o = " + (o != null ? o.getId() : "NULL"));
    	if(o instanceof Truck) return (Truck)o;
    	return null;
    }
}
