package com.dailyopt.mo.components.movingobjects;

import com.dailyopt.mo.components.maps.Path;
import com.dailyopt.mo.controller.ApiController;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovingObject implements IMovingObject {
	protected String id;
	protected double lat;
	protected double lng;
	protected double speed = 11;// m/s (~40km/h)
	/*
	private RouteSegmentToServicePoint[] specifiedRoutes;
	
	private double estimatedNextLat;
	private double estimatedNextLng;
	private int estimatedNextServicePointIndex; 
	
	public IServicePoint[] collectServicePoints(){
		// return all points that must be visited
		if(specifiedRoutes == null) return null;
		IServicePoint[] points = new IServicePoint[specifiedRoutes.length];
		for(int i = 0; i < specifiedRoutes.length; i++)
			points[i] = specifiedRoutes[i].getServicePoint();
		
		return points;
	}
			
	public MovingObject(String id, double lat, double lng) {
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
		for(int i = 0; i < specifiedRoutes.length; i++){
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
	*/
	
	public MovingObject() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void estimateNextPositionLatLng(int inSecond) {
		// TODO Auto-generated method stub
		
	}
	
	
}
