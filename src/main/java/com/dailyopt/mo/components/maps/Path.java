package com.dailyopt.mo.components.maps;

import com.dailyopt.mo.controller.ApiController;

public class Path {
	private Point[] points;
	private double length;
	
	public Path(Point[] points, double length) {
		super();
		this.points = points;
		this.length = length;
	}
	public double getLengthOfSegment(int idx){
		if(points == null) return 0;
		if(points.length <= 1) return 0;
		if(idx >= points.length-1) return 0;
		Point p1 = points[idx];
		Point p2 = points[idx+1];
		return ApiController.GMQ.computeDistanceHaversine(p1.getLat(), p1.getLng(), p2.getLat(), p2.getLng());
	}
	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public Point[] getPoints() {
		return points;
	}

	public void setPoints(Point[] points) {
		this.points = points;
	}

	public Path(Point[] points) {
		super();
		this.points = points;
	}
	public String toString(){
		String s = "length = " + length;
		for(int i = 0; i < points.length; i++)
			s = s + ", " + points[i].getId();
		
		return s;
	}
	public Path() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
