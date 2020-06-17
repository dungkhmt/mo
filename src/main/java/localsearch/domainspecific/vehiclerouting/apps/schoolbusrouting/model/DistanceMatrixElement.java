package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

import java.util.HashMap;
import java.util.HashSet;

public class DistanceMatrixElement {
	private HashSet<Integer> s_pointId;
	private HashMap<Integer, Integer> mPointId2Index;
	private double[][] distance;
	private double[][] travelTime;
	private int[][] roadBlock;
	
	
	public DistanceMatrixElement(HashSet<Integer> s_pointId,
			double[][] distance, double[][] travelTime,
			int[][] roadBlock){
		super();
		this.s_pointId = s_pointId;
		this.distance = distance;
		this.travelTime = travelTime;
		this.roadBlock = roadBlock;
	}
	public DistanceMatrixElement(){
		super();
	}
	public HashSet<Integer> getS_pointId() {
		return s_pointId;
	}
	public void setS_pointId(HashSet<Integer> s_pointId) {
		this.s_pointId = s_pointId;
	}
	public HashMap<Integer, Integer> getmPointId2Index() {
		return mPointId2Index;
	}
	public void setmPointId2Index(HashMap<Integer, Integer> mPointId2Index) {
		this.mPointId2Index = mPointId2Index;
	}
	public double[][] getDistance() {
		return distance;
	}
	public void setDistance(double[][] distance) {
		this.distance = distance;
	}
	public double[][] getTravelTime() {
		return travelTime;
	}
	public void setTravelTime(double[][] travelTime) {
		this.travelTime = travelTime;
	}
	public int[][] getRoadBlock() {
		return roadBlock;
	}
	public void setRoadBlock(int[][] roadBlock) {
		this.roadBlock = roadBlock;
	}
	
}
