package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class DistanceElement {

	private int srcCode;
	private int destCode;
	private double distance;
	private double travelTime;
	private int roadBlock;
	
	public int getRoadBlock() {
		return roadBlock;
	}

	public void setRoadBlock(int roadBlock) {
		this.roadBlock = roadBlock;
	}
	
	public double getTravelTime() {
		return travelTime;
	}
	public void setTravelTime(double travelTime) {
		this.travelTime = travelTime;
	}
	public int getSrcCode() {
		return srcCode;
	}
	public void setSrcCode(int srcCode) {
		this.srcCode = srcCode;
	}
	public int getDestCode() {
		return destCode;
	}
	public void setDestCode(int destCode) {
		this.destCode = destCode;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public DistanceElement(int srcCode, int destCode, double distance,
			double travelTime, int roadBlock) {
		super();
		this.srcCode = srcCode;
		this.destCode = destCode;
		this.distance = distance;
		this.travelTime = travelTime;
		this.roadBlock = roadBlock;
	}
	
	public DistanceElement(){
		super();
	}	
}
