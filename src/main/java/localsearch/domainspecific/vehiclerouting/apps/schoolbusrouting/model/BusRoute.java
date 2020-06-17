package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class BusRoute {
	
	private Vehicle vehicle;
	private int busID;
	private String busName;
	private int nbPersons;
	private double fillingrate;
	private int isResolved;
	private int nbStops;
	private int travelTime;
	private double travelDistance;
	private int updateFlag;
	private RouteElement[] nodes;
	private RouteElement[] reverses;
	private RouteElement[] basicReverses;
	
	private int startingDateTimePickup;
	private int startingDateTimeDelivery;

	public BusRoute(Vehicle vehicle, int busID, String busName, int nbPersons,
                    double fillingrate, int isResolved, int nbStops,
                    int startingDateTimePickup,
                    int startingDateTimeDelivery,
                    int travelTime, double travelDistance,
                    int updateFlag,
                    RouteElement[] nodes,
                    RouteElement[] reverses,
                    RouteElement[] basicReverses){
		super();
		this.vehicle = vehicle;
		this.busID = busID;
		this.busName = busName;
		this.nbPersons = nbPersons;
		this.fillingrate = fillingrate;
		this.isResolved = isResolved;
		this.nbStops = nbStops;
		this.startingDateTimePickup = startingDateTimePickup;
		this.startingDateTimeDelivery = startingDateTimeDelivery;
		this.travelTime = travelTime;
		this.travelDistance = travelDistance;
		this.updateFlag = updateFlag;
		this.nodes = nodes;
		this.reverses = reverses;
		this.basicReverses = basicReverses;
	}
	public BusRoute() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Vehicle getVehicle(){
		return this.vehicle;
	}
	public void setVehicle(Vehicle vehicle){
		this.vehicle = vehicle;
	}
	public int getBusID() {
		return busID;
	}
	public void setBusID(int busID) {
		this.busID = busID;
	}
	public String getBusName() {
		return busName;
	}
	public void setBusName(String busName) {
		this.busName = busName;
	}
	public int getNbPersons(){
		return this.nbPersons;
	}
	public void setNbPersons(int nbPersons){
		this.nbPersons = nbPersons;
	}
	public RouteElement[] getNodes(){
		return this.nodes;
	}
	public void setNodes(RouteElement[] nodes){
		this.nodes = nodes;
	}
	public double getFillingrate() {
		return fillingrate;
	}
	public void setFillingrate(double fillingrate) {
		this.fillingrate = fillingrate;
	}
	public int getIsResolved() {
		return isResolved;
	}
	public void setIsResolved(int isResolved) {
		this.isResolved = isResolved;
	}
	public int getNbStops() {
		return nbStops;
	}
	public void setNbStops(int nbStops) {
		this.nbStops = nbStops;
	}
	public int getStartingDateTimePickup() {
		return startingDateTimePickup;
	}
	public void setStartingDateTimePickup(int startingDateTimePickup) {
		this.startingDateTimePickup = startingDateTimePickup;
	}
	public int getStartingDateTimeDelivery() {
		return startingDateTimeDelivery;
	}
	public void setStartingDateTimeDelivery(int startingDateTimeDelivery) {
		this.startingDateTimeDelivery = startingDateTimeDelivery;
	}
	public int getTravelTime() {
		return travelTime;
	}
	public void setTravelTime(int travelTime) {
		this.travelTime = travelTime;
	}
	public double getTravelDistance() {
		return travelDistance;
	}
	public void setTravelDistance(double travelDistance) {
		this.travelDistance = travelDistance;
	}
	public int getUpdateFlag() {
		return updateFlag;
	}
	public void setUpdateFlag(int updateFlag) {
		this.updateFlag = updateFlag;
	}
	public RouteElement[] getReverses() {
		return reverses;
	}
	public void setReverses(RouteElement[] reverses) {
		this.reverses = reverses;
	}
	public RouteElement[] getBasicReverses() {
		return basicReverses;
	}
	public void setBasicReverses(RouteElement[] basicReverses) {
		this.basicReverses = basicReverses;
	}
}
