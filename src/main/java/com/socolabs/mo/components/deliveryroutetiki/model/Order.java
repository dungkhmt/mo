package com.socolabs.mo.components.deliveryroutetiki.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {
	private OrderItem[] items;
	private String lateDeliveryTime;
	private String latlng;
}
