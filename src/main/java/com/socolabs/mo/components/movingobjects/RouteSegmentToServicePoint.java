package com.socolabs.mo.components.movingobjects;

import lombok.Getter;
import lombok.Setter;

import com.socolabs.mo.components.maps.Point;

@Getter
@Setter
public class RouteSegmentToServicePoint {
	private Point[] mapPoints;
	private IServicePoint servicePoint;
	private double length;
	public RouteSegmentToServicePoint(Point[] mapPoints,
			IServicePoint servicePoint, double length) {
		super();
		this.mapPoints = mapPoints;
		this.servicePoint = servicePoint;
		this.length = length;
	}
	public RouteSegmentToServicePoint() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
