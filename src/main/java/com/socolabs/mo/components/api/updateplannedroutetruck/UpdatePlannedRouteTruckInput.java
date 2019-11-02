package com.socolabs.mo.components.api.updateplannedroutetruck;

import com.socolabs.mo.model.routevrp.Route;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePlannedRouteTruckInput {
	private String id;
	private Route route;
	public UpdatePlannedRouteTruckInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	public UpdatePlannedRouteTruckInput(String id, Route route) {
		super();
		this.id = id;
		this.route = route;
	}
	
	
}
