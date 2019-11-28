package com.socolabs.mo.components.movingobjects.truck;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PositionTypeAction {
	public static final String ACTION_PASS = "ACTION_PASS";
	public static final String ACTION_DELIVERY = "ACTION_DELIVERY";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String TYPE_MAP_POINT = "MAP_POINT";
	public static final String TYPE_SERVICE_POINT = "SERVICE_POINT";
	
	private double lat;
	private double lng;
	private String type;
	private String action;
	public PositionTypeAction() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PositionTypeAction(double lat, double lng, String type, String action) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.type = type;
		this.action = action;
	}
	
}
