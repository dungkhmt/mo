package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class SchoolBusRoutingInput {
	private Vehicle[] vehicles;
	private int shoolPointId;
	private double lat_school;
	private double long_school;
	private SchoolBusRequest[] requests;
	private MoveAction[] moveActions;
	private DistanceElement[] distances;
	private ConfigParams configParams;
	private SchoolBusRoutingSolution currentSolution;
	private String requestSolutionType;
	
	public SchoolBusRoutingInput(
			Vehicle[] vehicles,
			int shoolPointId,
			double lat_school,
			double long_school,
			SchoolBusRequest[] requests,
			MoveAction[] moveActions,
			DistanceElement[] distances,
			ConfigParams configParams,
			SchoolBusRoutingSolution currentSolution,
			String requestSolutionType){
		super();
		this.vehicles = vehicles;
		this.shoolPointId = shoolPointId;
		this.lat_school = lat_school;
		this.long_school = long_school;
		this.requests = requests;
		this.moveActions = moveActions;
		this.distances = distances;
		this.configParams = configParams;
		this.currentSolution = currentSolution;
		this.requestSolutionType = requestSolutionType;
	}
	
	public SchoolBusRoutingInput(){
		super();
	}
	
	public Vehicle[] getVehicles(){
		return this.vehicles;
	}
	public void setVehicles(Vehicle[] vehicles){
		this.vehicles = vehicles;
	}
	public SchoolBusRequest[] getRequests(){
		return this.requests;
	}
	public void setRequests(SchoolBusRequest[] requests){
		this.requests = requests;
	}
	public MoveAction[] getMoveActions() {
		return moveActions;
	}

	public void setMoveActions(MoveAction[] moveActions) {
		this.moveActions = moveActions;
	}

	public DistanceElement[] getDistances(){
		return this.distances;
	}
	public void setDistances(DistanceElement[] distances){
		this.distances = distances;
	}

	public int getShoolPointId() {
		return shoolPointId;
	}

	public void setShoolPointId(int shoolPointId) {
		this.shoolPointId = shoolPointId;
	}

	public double getLat_school() {
		return lat_school;
	}

	public void setLat_school(double lat_school) {
		this.lat_school = lat_school;
	}

	public double getLong_school() {
		return long_school;
	}

	public void setLong_school(double long_school) {
		this.long_school = long_school;
	}

	public ConfigParams getConfigParams() {
		return configParams;
	}

	public void setConfigParams(ConfigParams configParams) {
		this.configParams = configParams;
	}

	public SchoolBusRoutingSolution getCurrentSolution() {
		return currentSolution;
	}

	public void setCurrentSolution(SchoolBusRoutingSolution currentSolution) {
		this.currentSolution = currentSolution;
	}

	public String getRequestSolutionType() {
		return requestSolutionType;
	}

	public void setRequestSolutionType(String requestSolutionType) {
		this.requestSolutionType = requestSolutionType;
	}
	
}
