package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class RouteElement {

	private int locationId;
	private int varIndex;
	private String action;
	
	private int arrivalTime;
	private int departureTime;
	private int directTime;
	private int travelTime;
	
	private int[] hsList;
	private int[] listRegisterId;
	
	
	public RouteElement(int locationId, int varIndex, String action,
			int arrivalTime, int departureTime,
			int directTime, int travelTime,
			int[] hsList,
			int[] listRegisterId){
		super();
		this.locationId = locationId;
		this.varIndex = varIndex;
		this.action = action;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.directTime = directTime;
		this.travelTime = travelTime;
		this.hsList = hsList;
		this.listRegisterId = listRegisterId;
	}
	public RouteElement() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int[] getListRegisterId() {
		return listRegisterId;
	}
	public void setListRegisterId(int[] listRegisterId) {
		this.listRegisterId = listRegisterId;
	}
	public int getLocationId(){
		return this.locationId;
	}
	public void setLocationId(int locationId){
		this.locationId = locationId;
	}
	public int getVarIndex() {
		return varIndex;
	}
	public void setVarIndex(int varIndex) {
		this.varIndex = varIndex;
	}
	public String getAction(){
		return this.action;
	}
	public void setAction(String action){
		this.action = action;
	}
	public int getArrivalTime(){
		return this.arrivalTime;
	}
	public void setArrivalTime(int arrivalTime){
		this.arrivalTime = arrivalTime;
	}
	public int getDepartureTime(){
		return this.departureTime;
	}
	public void setDepartureTime(int departureTime){
		this.departureTime = departureTime;
	}
	public int getDirectTime() {
		return directTime;
	}
	public void setDirectTime(int directTime) {
		this.directTime = directTime;
	}
	public int getTravelTime() {
		return travelTime;
	}
	public void setTravelTime(int travelTime) {
		this.travelTime = travelTime;
	}
	public int[] getHsList(){
		return this.hsList;
	}
	public void setHsList(int[] hsList){
		this.hsList = hsList;
	}
}
