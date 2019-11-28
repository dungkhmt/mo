package com.socolabs.mo.components.deliveryroutetiki.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryRouteInput {
	private Order[] orders;
	private Depot depot;
	private Vehicle[] vehicles;
	private DistanceElement[] distances;
}
