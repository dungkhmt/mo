package com.socolabs.mo.model.routevrp;

public class RouteVRPInputPoint {
	private String id;
	private double lat;
	private double lng;
	private String type;
	private int info;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getInfo() {
		return info;
	}
	public void setInfo(int info) {
		this.info = info;
	}
	public String getLatLng(){
		return lat + "," + lng;
	}
	public RouteVRPInputPoint(String id, double lat, double lng, String type,
			int info) {
		super();
		this.id = id;
		this.lat = lat;
		this.lng = lng;
		this.type = type;
		this.info = info;
	}
	public RouteVRPInputPoint() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
