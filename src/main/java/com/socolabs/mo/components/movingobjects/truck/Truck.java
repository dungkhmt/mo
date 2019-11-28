package com.socolabs.mo.components.movingobjects.truck;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

import com.socolabs.mo.components.maps.Path;
import com.socolabs.mo.components.movingobjects.IServicePoint;
import com.socolabs.mo.components.movingobjects.MovingObject;
import com.socolabs.mo.components.movingobjects.RouteSegmentToServicePoint;
import com.socolabs.mo.controller.ApiController;
@Getter
@Setter
public class Truck extends MovingObject {
	/*
	private String id;
	private double lat;
	private double lng;
	private double speed = 11;// m/s (~40km/h)
	*/
	private IServicePoint[] servicePoints; 
	
	private RouteSegmentToServicePoint[] specifiedRoutes;
	private int currentRouteSegmentIndex; // current route segment of point (lat,lng)
	private int currentPointIndex;// current index of point (lat,lng) in specifiedRoutes[currentRouteSegmentIndex]
	private int startActiveServicePointIndex;// remaining service points to be visited specifiedRoutes[startActiveServicePointIndex-1].servicePoint
	
	
	private ArrayList<IServicePoint> unChangeServicePoints = new ArrayList<IServicePoint>();
	private double estimatedNextLat;
	private double estimatedNextLng;
	private int estimatedNextServicePointIndex = 1; 
	private ArrayList<IServicePoint> canbeChangeServicePoints = new ArrayList<IServicePoint>();
	
	
	private void computeRoutes(){
		specifiedRoutes = new RouteSegmentToServicePoint[servicePoints.length];
		double lat_i = lat; double lng_i = lng;
		for(int i = 0; i < servicePoints.length; i++){
			Path path = ApiController.gismap.findPath(lat_i + "," + lng_i, servicePoints[i].getLatLng());
			specifiedRoutes[i] = new RouteSegmentToServicePoint(path.getPoints(), servicePoints[i], path.getLength());
			lat_i = servicePoints[i].getLat();
			lng_i = servicePoints[i].getLng();
		}
	}
	/*
	public void updateServicePoints(RouteVRPInputPoint[] servicePoints){
		RouteSegmentToServicePoint[] R = new RouteSegmentToServicePoint[servicePoints.length];
		for(int i = 0; i < R.length; i++){
			Point[] mapPoints = null;
			IServicePoint servicePoint = new ServicePointDelivery(servicePoints[i].getId(),servicePoints[i].getLat(), servicePoints[i].getLat());
			double length = 0;
			R[i] = new RouteSegmentToServicePoint(mapPoints,servicePoint, length);
		}
		specifiedRoutes = R;
	}*/
	public void updateServicePoints(IServicePoint[] servicePoints){
		this.servicePoints = servicePoints;
		computeRoutes();
		currentRouteSegmentIndex = 0;
		currentPointIndex = 0;
	}
	public PositionTypeAction updateLocation(double lat, double lng){
		// update location, return next {location, type, action}
		PositionTypeAction rs = null;
		this.lat = lat; this.lng = lng;
		if(currentPointIndex == specifiedRoutes[currentRouteSegmentIndex].getMapPoints().length-1){
			if(currentRouteSegmentIndex == specifiedRoutes.length-1){
				double n_lat = specifiedRoutes[currentRouteSegmentIndex].getMapPoints()[currentPointIndex].getLat();
				double n_lng = specifiedRoutes[currentRouteSegmentIndex].getMapPoints()[currentPointIndex].getLng();
				
				rs = new PositionTypeAction(n_lat, n_lng, PositionTypeAction.TYPE_SERVICE_POINT, PositionTypeAction.ACTION_STOP);
			}else{
				currentRouteSegmentIndex++;
				currentPointIndex = 0;
				double n_lat = specifiedRoutes[currentRouteSegmentIndex].getMapPoints()[currentPointIndex].getLat();
				double n_lng = specifiedRoutes[currentRouteSegmentIndex].getMapPoints()[currentPointIndex].getLng();
				
				rs =  new PositionTypeAction(n_lat, n_lng, PositionTypeAction.TYPE_SERVICE_POINT, PositionTypeAction.ACTION_DELIVERY);
			}
		}else{
			currentPointIndex++;
			double n_lat = specifiedRoutes[currentRouteSegmentIndex].getMapPoints()[currentPointIndex].getLat();
			double n_lng = specifiedRoutes[currentRouteSegmentIndex].getMapPoints()[currentPointIndex].getLng();
			
			rs = new PositionTypeAction(n_lat, n_lng, PositionTypeAction.TYPE_MAP_POINT, PositionTypeAction.ACTION_PASS);
		
		}
		//System.out.println(name() + "::updateLocation, currentPointIndex = " + currentPointIndex + ", currentRouteSegmentIndex = " + 
		//currentRouteSegmentIndex + ", specifiedRoutes.length = " + specifiedRoutes.length + ", servicePoints.length = " + 
		//		servicePoints.length + ", servicePoints = " + getServicePointString() + ", rs = " + ApiController.gson.toJson(rs));
		return rs;
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
		IServicePoint[] tmp = new IServicePoint[servicePoints.length-1];
		for(int i = 1; i < servicePoints.length; i++)
			tmp[i-1] = servicePoints[i];
		updateServicePoints(tmp);
		
		System.out.println(name() + "::updateLocation, currentPointIndex = " + currentPointIndex + ", currentRouteSegmentIndex = " + 
				currentRouteSegmentIndex + ", specifiedRoutes.length = " + specifiedRoutes.length + ", servicePoints.length = " + 
						servicePoints.length + ", servicePoints = " + getServicePointString());
		
		//servicePoints = tmp;
		//System.out.println(name() + "::performService, startActiveServicePointIndex = " + startActiveServicePointIndex);
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
	public String getServicePointString(){
		if(servicePoints == null) return "NULL";
		String s = "ID: ";
		for(int i = 0; i < servicePoints.length; i++)
			s = s + servicePoints[i].getId() + ", ";
		return s;
	}
	public void estimateNextPositionLatLng(int inSecond){
		/*
		 * apply simple strategy assuming computation time is zero
		 */
		estimatedNextLat = lat;
		estimatedNextLng = lng;
		estimatedNextServicePointIndex = 0;
		
		/*
		if(specifiedRoutes == null) return;// lat + "," + lng;
		double L = speed * inSecond;
		double l = 0;
		double t_lat = lat; double t_lng = lng;
		unChangeServicePoints.clear();
		canbeChangeServicePoints.clear();
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
		*/
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
