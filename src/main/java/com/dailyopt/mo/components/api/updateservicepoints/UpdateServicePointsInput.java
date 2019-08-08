package com.dailyopt.mo.components.api.updateservicepoints;

import lombok.Getter;
import lombok.Setter;

import com.dailyopt.mo.components.maps.Point;
import com.dailyopt.mo.model.routevrp.RouteVRPInputPoint;
@Getter
@Setter

public class UpdateServicePointsInput {
	private String objectId;
	private RouteVRPInputPoint[] servicePoints;
	public UpdateServicePointsInput(String objectId, RouteVRPInputPoint[] servicePoints) {
		super();
		this.objectId = objectId;
		this.servicePoints = servicePoints;
	}
	public UpdateServicePointsInput() {
		super();
		// TODO Auto-generated constructor stub
	}
		
}