package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class BusInfomation {
	private int id;
	private int cap;
	private double fillingrate;
	private int nbStops;
	private int travelTime;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCap() {
		return cap;
	}
	public void setCap(int cap) {
		this.cap = cap;
	}
	public double getFillingrate() {
		return fillingrate;
	}
	public void setFillingrate(double fillingrate) {
		this.fillingrate = fillingrate;
	}
	public int getNbStops() {
		return nbStops;
	}
	public void setNbStops(int nbStops) {
		this.nbStops = nbStops;
	}
	public int getTravelTime() {
		return travelTime;
	}
	public void setTravelTime(int travelTime) {
		this.travelTime = travelTime;
	}
	public BusInfomation(int id,
			int cap,
			double fillingrate,
			int nbStops,
			int travelTime){
		super();
		this.id = id;
		this.cap = cap;
		this.fillingrate = fillingrate;
		this.nbStops = nbStops;
		this.travelTime = travelTime;
	}
	public BusInfomation() {
		super();
		// TODO Auto-generated constructor stub
	}

}
