package com.dailyopt.mo.components.api.performservicetruck;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformServiceTruckInput {
	private String truckId;

	public PerformServiceTruckInput(String truckId) {
		super();
		this.truckId = truckId;
	}

	public PerformServiceTruckInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
