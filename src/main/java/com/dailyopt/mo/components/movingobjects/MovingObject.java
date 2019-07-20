package com.dailyopt.mo.components.movingobjects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovingObject {
	private String id;
	private double lat;
	private double lng;
	public MovingObject(String id, double lat, double lng) {
		super();
		this.id = id;
		this.lat = lat;
		this.lng = lng;
	}
	public MovingObject() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
