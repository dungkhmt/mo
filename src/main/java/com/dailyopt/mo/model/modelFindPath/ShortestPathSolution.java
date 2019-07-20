package com.dailyopt.mo.model.modelFindPath;

import com.dailyopt.mo.components.maps.Point;

public class ShortestPathSolution {
	private Point[] points;


	public Point[] getPoints() {
		return points;
	}


	public void setPoints(Point[] points) {
		this.points = points;
	}


	public ShortestPathSolution(Point[] points) {
		super();
		this.points = points;
	}


	public ShortestPathSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
