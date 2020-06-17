package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class StatisticInformation {
	private int totalPupils;
	private int timeViolation;
	private int capViolation;
	private double totalDistance;
	private int numberBuses;
	private MovingResult[] movingResults;

	
	public int getTimeViolation() {
		return timeViolation;
	}

	public void setTimeViolation(int timeViolation) {
		this.timeViolation = timeViolation;
	}

	public int getCapViolation() {
		return capViolation;
	}

	public void setCapViolation(int capViolation) {
		this.capViolation = capViolation;
	}

	public int getTotalPupils() {
		return totalPupils;
	}

	public void setTotalPupils(int totalPupils) {
		this.totalPupils = totalPupils;
	}

	public int getNumberBuses() {
		return numberBuses;
	}

	public void setNumberBuses(int numberBuses) {
		this.numberBuses = numberBuses;
	}

	public double getTotalDistance() {
		return totalDistance;
	}

	public void setTotalDistance(double totalDistance) {
		this.totalDistance = totalDistance;
	}
	
	public MovingResult[] getMovingResults() {
		return movingResults;
	}

	public void setMovingResults(MovingResult[] movingResults) {
		this.movingResults = movingResults;
	}

	public StatisticInformation(int totalPupils, int timeViolation, int capViolation,
			double totalDistance, int numberBuses, MovingResult[] movingResults) {
		super();
		this.totalPupils = totalPupils;
		this.timeViolation = timeViolation;
		this.capViolation = capViolation;
		this.totalDistance = totalDistance;
		this.numberBuses = numberBuses;
		this.movingResults = movingResults;
	}

	public StatisticInformation() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
