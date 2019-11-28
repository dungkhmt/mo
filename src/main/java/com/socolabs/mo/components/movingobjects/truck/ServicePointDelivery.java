package com.socolabs.mo.components.movingobjects.truck;

import lombok.Getter;
import lombok.Setter;

import com.socolabs.mo.components.movingobjects.IServicePoint;
@Getter
@Setter
public class ServicePointDelivery implements IServicePoint{
	private String id;
	private double lat;
	private double lng;
	
	public String getLatLng(){
		return lat + "," + lng;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "DELIVERY";
	}

	public ServicePointDelivery(String id, double lat, double lng) {
		super();
		this.id = id;
		this.lat = lat;
		this.lng = lng;
	}
	
}
