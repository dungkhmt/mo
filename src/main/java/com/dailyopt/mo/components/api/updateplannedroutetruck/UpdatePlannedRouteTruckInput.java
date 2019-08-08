package com.dailyopt.mo.components.api.updateplannedroutetruck;

import com.dailyopt.mo.model.routevrp.Route;

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
