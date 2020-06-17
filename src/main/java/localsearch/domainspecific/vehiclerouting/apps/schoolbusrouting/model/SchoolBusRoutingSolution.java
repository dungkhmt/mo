package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model;

public class SchoolBusRoutingSolution {
	private BusRoute[] busRoutes;
	private SchoolBusRequest[] unScheduledRequests;
	private StatisticInformation statisticInformation;
	
	public SchoolBusRoutingSolution(BusRoute[] busRoutes,
                                    SchoolBusRequest[] unScheduledRequests,
                                    StatisticInformation statisticInformation) {
		super();
		this.busRoutes = busRoutes;
		this.unScheduledRequests = unScheduledRequests;
		this.statisticInformation = statisticInformation;
	}
	
	public SchoolBusRoutingSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public BusRoute[] getBusRoutes(){
		return this.busRoutes;
	}
	public void setBusRoute(BusRoute[] busRoutes){
		this.busRoutes = busRoutes;
	}
	public SchoolBusRequest[] getUnScheduledRequests(){
		return this.unScheduledRequests;
	}
	public void setUnScheduledRequests(SchoolBusRequest[] unScheduledRequests){
		this.unScheduledRequests = unScheduledRequests;
	}
	public StatisticInformation getStatisticInformation() {
		return statisticInformation;
	}
	public void setStatisticInformation(StatisticInformation statisticInformation) {
		this.statisticInformation = statisticInformation;
	}
}
