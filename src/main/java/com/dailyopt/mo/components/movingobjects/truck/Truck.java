package com.dailyopt.mo.components.movingobjects.truck;

import lombok.Getter;
import lombok.Setter;

import com.dailyopt.mo.components.maps.Path;
import com.dailyopt.mo.components.maps.Point;
import com.dailyopt.mo.components.movingobjects.IMovingObject;
import com.dailyopt.mo.components.movingobjects.IServicePoint;
import com.dailyopt.mo.components.movingobjects.MovingObject;
import com.dailyopt.mo.components.movingobjects.RouteSegmentToServicePoint;
import com.dailyopt.mo.controller.ApiController;
import com.dailyopt.mo.model.routevrp.RouteVRPInputPoint;
@Getter
@Setter
public class Truck extends MovingObject {
	/*
	private String id;
	private double lat;
	private double lng;
	private double speed = 11;// m/s (~40km/h)
	*/
	private RouteSegmentToServicePoint[] specifiedRoutes;
	private int startActiveServicePointIndex;// remaining service points to be visited specifiedRoutes[startActiveServicePointIndex-1].servicePoint
	
	private double estimatedNextLat;
	private double estimatedNextLng;
	private int estimatedNextServicePointIndex = 1; 
	
	public void updateServicePoints(RouteVRPInputPoint[] servicePoints){
		RouteSegmentToServicePoint[] R = new RouteSegmentToServicePoint[servicePoints.length];
		for(int i = 0; i < R.length; i++){
			Point[] mapPoints = null;
			IServicePoint servicePoint = new ServicePointDelivery(servicePoints[i].getId(),servicePoints[i].getLat(), servicePoints[i].getLat());
			double length = 0;
			R[i] = new RouteSegmentToServicePoint(mapPoints,servicePoint, length);
		}
		specifiedRoutes = R;
	}
	public IServicePoint[] collectServicePoints(){
		// return all points that must be visited
		if(specifiedRoutes == null) return null;
		IServicePoint[] points = new IServicePoint[specifiedRoutes.length];
		for(int i = estimatedNextServicePointIndex-1; i < specifiedRoutes.length; i++)
			points[i] = specifiedRoutes[i].getServicePoint();
		
		return points;
	}
	public String name(){
		return "Truck";
	}
	public void performService(){
		startActiveServicePointIndex++;
		if(startActiveServicePointIndex > specifiedRoutes.length)
			startActiveServicePointIndex = 1;
		System.out.println(name() + "::performService, startActiveServicePointIndex = " + startActiveServicePointIndex);
	}
	public String toString(){
		IServicePoint[] servicePoints = collectServicePoints();
		String s = id + ": (" + lat + "," + lng + "), service points = ";
		for(int i = 0; i < servicePoints.length; i++) s = s + "{" + servicePoints[i].getId() + "," + servicePoints[i].getLatLng() + "}, ";
		return s;
	}
	public Truck(String id, double lat, double lng) {
		super();
		this.id = id;
		this.lat = lat;
		this.lng = lng;
	}
	public void estimateNextPositionLatLng(int inSecond){
		if(specifiedRoutes == null) return;// lat + "," + lng;
		double L = speed * inSecond;
		double l = 0;
		double t_lat = lat; double t_lng = lng;
		//for(int i = 0; i < specifiedRoutes.length; i++){
		for(int i = startActiveServicePointIndex-1; i < specifiedRoutes.length; i++){	
			IServicePoint p = specifiedRoutes[i].getServicePoint();
			Path path = ApiController.gismap.findPath(t_lat + "," + t_lng, p.getLatLng());
			if(path == null) continue;
			for(int j = 0; j < path.getPoints().length-1; j++){
				l += path.getLengthOfSegment(j);
				if(l > L){
					//return path.getPoints()[j+1].getLatLng();
					estimatedNextLat = path.getPoints()[j+1].getLat();
					estimatedNextLng = path.getPoints()[j+1].getLng();
					estimatedNextServicePointIndex = i;
				}
			}
			t_lat = p.getLat(); t_lng = p.getLng();
		}
		estimatedNextLat = lat;
		estimatedNextLng = lng;
		estimatedNextServicePointIndex = -1;
		//return lat + "," + lng;
	}
	public Truck() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Truck(RouteSegmentToServicePoint[] specifiedRoutes,
			double estimatedNextLat, double estimatedNextLng,
			int estimatedNextServicePointIndex) {
		super();
		this.specifiedRoutes = specifiedRoutes;
		this.estimatedNextLat = estimatedNextLat;
		this.estimatedNextLng = estimatedNextLng;
		this.estimatedNextServicePointIndex = estimatedNextServicePointIndex;
	}
	
}
