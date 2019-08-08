package com.dailyopt.mo.components.routeplanner.onlinevrp.model;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

import com.dailyopt.mo.components.movingobjects.IServicePoint;

@Getter
@Setter
public class ExecuteRoute {
	private String vehicleCode;
	private IServicePoint[] servicePoints;// sequence of service points being visited
	private String currentLatLng;
	private String changeFromLatLng;
	private int changeFromServicePointIndex;// new point (if any) can only be inserted right-before servicePoints[changeFromServicePointIndex]
	
	
	
	
	
	private RouteSegment[] routeSegments;// itinerary = routeSegments[0]::routeSegments[1]::. . .
	private int changeFromRouteSegmentIndex;
	private int changeFromIndexOfRouteSegment;// change from point 
					// routeSegments[changeFromRouteSegmentIndex].getPoint(changeFromIndexOfRouteSegment)
					// when reschedule
	
	public void insertServicePoint(int index, IServicePoint p){
		// insert p right-before position index
		IServicePoint[] L = new IServicePoint[servicePoints.length+1];
		for(int i = 0; i < index; i++)
			L[i] = servicePoints[i];
		L[index] = p;
		for(int i = index; i < servicePoints.length; i++)
			L[i+1] = servicePoints[i];
		servicePoints = L;
	}
	public String getVehicleCode() {
		return vehicleCode;
	}
	public void setVehicleCode(String vehicleCode) {
		this.vehicleCode = vehicleCode;
	}
	public RouteSegment[] getRouteSegments() {
		return routeSegments;
	}
	public void setRouteSegments(RouteSegment[] routeSegments) {
		this.routeSegments = routeSegments;
	}
	public int getChangeFromRouteSegmentIndex() {
		return changeFromRouteSegmentIndex;
	}
	public void setChangeFromRouteSegmentIndex(int changeFromRouteSegmentIndex) {
		this.changeFromRouteSegmentIndex = changeFromRouteSegmentIndex;
	}
	public int getChangeFromIndexOfRouteSegment() {
		return changeFromIndexOfRouteSegment;
	}
	public void setChangeFromIndexOfRouteSegment(int changeFromIndexOfRouteSegment) {
		this.changeFromIndexOfRouteSegment = changeFromIndexOfRouteSegment;
	}
	public ExecuteRoute(String vehicleCode, RouteSegment[] routeSegments,
			int changeFromRouteSegmentIndex, int changeFromIndexOfRouteSegment) {
		super();
		this.vehicleCode = vehicleCode;
		this.routeSegments = routeSegments;
		this.changeFromRouteSegmentIndex = changeFromRouteSegmentIndex;
		this.changeFromIndexOfRouteSegment = changeFromIndexOfRouteSegment;
	}
	public ExecuteRoute() {
		super();
		// TODO Auto-generated constructor stub
	}
	public void replaceRouteSegment(int idx, RouteSegment rs1, RouteSegment rs2){
		// return routeSegments[idx]
		// insert rs1 and then rs2 at the position idx
		RouteSegment[] newRouteSegments = new RouteSegment[routeSegments.length+1];
		for(int i = 0; i < idx; i++){
			newRouteSegments[i] = routeSegments[i];
		}
		newRouteSegments[idx] = rs1;
		newRouteSegments[idx+1] = rs2;
		for(int i = idx+1; i < routeSegments.length; i++){
			newRouteSegments[i+1] = routeSegments[i];
		}
		routeSegments = newRouteSegments;
	}
	public ExecuteRoute(String vehicleCode, IServicePoint[] servicePoints,
			String currentLatLng, String changeFromLatLng,
			int changeFromServicePointIndex) {
		super();
		this.vehicleCode = vehicleCode;
		this.servicePoints = servicePoints;
		this.currentLatLng = currentLatLng;
		this.changeFromLatLng = changeFromLatLng;
		this.changeFromServicePointIndex = changeFromServicePointIndex;
	}
							
	
	
}
