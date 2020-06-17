package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class Vehicle {

	private int id;
	private int cap;
	
	public Vehicle(int id, int cap){
		super();
		this.id = id;
		this.cap = cap;
	}
	public Vehicle() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public int getId(){
		return this.id;
	}
	public void setId(int id){
		this.id = id;
	}
	public int getCap(){
		return this.cap;
	}
	public void setCap(int cap){
		this.cap = cap;
	}

}
