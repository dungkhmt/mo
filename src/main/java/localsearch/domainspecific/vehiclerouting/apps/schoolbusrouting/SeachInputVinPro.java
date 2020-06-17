package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting;

import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;

import java.util.ArrayList;
import java.util.HashMap;

public class SeachInputVinPro {
	public ArrayList<Point> pickupPoints;
	public ArrayList<Point> rejectPoints;
	public double[] capList;
	public HashMap<Point, Integer> earliestAllowedArrivalTime;
	public HashMap<Point, Integer> serviceDuration;
	public HashMap<Point, Integer> lastestAllowedArrivalTime;
	
	public SeachInputVinPro(ArrayList<Point> pickupPoints, ArrayList<Point> rejectPoints,
                            double[] capList,
                            HashMap<Point, Integer> earliestAllowedArrivalTime,
                            HashMap<Point, Integer> serviceDuration,
                            HashMap<Point, Integer> lastestAllowedArrivalTime) {
		
		super();
		this.pickupPoints = pickupPoints;
		this.rejectPoints = rejectPoints;
		this.capList = capList;
		this.earliestAllowedArrivalTime = earliestAllowedArrivalTime;
		this.serviceDuration = serviceDuration;
		this.lastestAllowedArrivalTime = lastestAllowedArrivalTime;
	}

	public ArrayList<Point> getPickupPoints() {
		return pickupPoints;
	}

	public void setPickupPoints(ArrayList<Point> pickupPoints) {
		this.pickupPoints = pickupPoints;
	}

	public ArrayList<Point> getRejectPoints() {
		return rejectPoints;
	}

	public void setRejectPoints(ArrayList<Point> rejectPoints) {
		this.rejectPoints = rejectPoints;
	}

	public HashMap<Point, Integer> getEarliestAllowedArrivalTime() {
		return earliestAllowedArrivalTime;
	}

	public void setEarliestAllowedArrivalTime(
			HashMap<Point, Integer> earliestAllowedArrivalTime) {
		this.earliestAllowedArrivalTime = earliestAllowedArrivalTime;
	}

	public HashMap<Point, Integer> getServiceDuration() {
		return serviceDuration;
	}

	public void setServiceDuration(HashMap<Point, Integer> serviceDuration) {
		this.serviceDuration = serviceDuration;
	}

	public HashMap<Point, Integer> getLastestAllowedArrivalTime() {
		return lastestAllowedArrivalTime;
	}

	public void setLastestAllowedArrivalTime(
			HashMap<Point, Integer> lastestAllowedArrivalTime) {
		this.lastestAllowedArrivalTime = lastestAllowedArrivalTime;
	}
	
	public double[] getCapList() {
		return capList;
	}

	public void setCapList(double[] capList) {
		this.capList = capList;
	}
}
