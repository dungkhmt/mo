package com.socolabs.mo.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.socolabs.mo.components.api.performservicetruck.PerformServiceTruckInput;
import com.socolabs.mo.components.api.updateplannedroutetruck.UpdatePlannedRouteTruckInput;
import com.socolabs.mo.components.maps.Point;
import com.socolabs.mo.components.maps.distanceelementquery.DistanceElement;
import com.socolabs.mo.components.maps.GISMap;
import com.socolabs.mo.components.maps.distanceelementquery.DistanceElementQuery;
import com.socolabs.mo.components.maps.distanceelementquery.GeneralDistanceElement;
import com.socolabs.mo.components.maps.distanceelementquery.LatLngInput;
import com.socolabs.mo.components.maps.Path;
import com.socolabs.mo.components.maps.graphs.Segment;
import com.socolabs.mo.components.maps.utils.GoogleMapsQuery;
import com.socolabs.mo.components.movingobjects.IMovingObject;
import com.socolabs.mo.components.movingobjects.IServicePoint;
import com.socolabs.mo.components.movingobjects.MovingObject;
import com.socolabs.mo.components.movingobjects.agent.Agent;
import com.socolabs.mo.components.movingobjects.taxi.*;
import com.socolabs.mo.components.movingobjects.truck.PositionTypeAction;
import com.socolabs.mo.components.movingobjects.truck.Truck;
import com.socolabs.mo.components.objectmanager.MovingObjectManager;
import com.socolabs.mo.components.objectmanager.ObjectManager;
import com.socolabs.mo.components.routeplanner.onlinevrp.OnlineVRPPlanner;
import com.socolabs.mo.components.routeplanner.onlinevrp.model.ExecuteRoute;
import com.socolabs.mo.components.routeplanner.onlinevrp.model.OnlineVRPInput;
import com.socolabs.mo.components.routeplanner.onlinevrp.model.OnlineVRPSolution;
import com.socolabs.mo.components.routeplanner.vrp.CWPlanner;
import com.socolabs.mo.components.routeplanner.vrp.VRPPlanner;
import com.socolabs.mo.model.AddModel;
import com.socolabs.mo.model.modelFindPath.ShortestPathInput;
import com.socolabs.mo.model.modelmap.MapWindow;
import com.socolabs.mo.model.routevrp.RouteVRPInput;
import com.socolabs.mo.model.routevrp.RouteVRPInputPoint;
import com.socolabs.mo.model.routevrp.RouteVRPSolution;
import com.socolabs.mo.model.routevrp.RunRouteInput;
import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class ApiController {
	private static Integer count = 0; 
    private static ObjectManager mgr = new ObjectManager();
    public static GISMap gismap = new GISMap();
    public static String region = "";
	public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static GoogleMapsQuery GMQ = new GoogleMapsQuery();
	public static Gson gson = new Gson();

	private boolean initTaxi = false;

	@PostMapping("/add")
    public Integer home(@RequestBody AddModel addModel) {
    	synchronized(count){
    		count = count + 1;
    		System.out.println(count);
    	}
        return addModel.getA()+addModel.getB();
    }
    
	public String name(){
		return "ApiController";
	}

	@PostMapping("/getCurrentDate")
	public String getCurrentDate() {
		LocalDateTime now = LocalDateTime.now();
		System.out.println("getCurrentDate::" + dtf.format(now));
		return dtf.format(now);
	}

	@PostMapping("/getObjects")
	public String getObjects(){
		List<IMovingObject> l_objects = mgr.getObjects();
		Gson gson = new Gson();
		MovingObject[] objects = new MovingObject[l_objects.size()];
		for(int i = 0; i < l_objects.size(); i++)
			objects[i] = (MovingObject) l_objects.get(i);
		String json = gson.toJson(objects);
		//System.out.println("getObjects, json = " + json);
		return json;
	}
	
	@PostMapping("/getTrucks")
	public String getTrucks(){
		List<Truck> l_objects = mgr.getTrucks();
		Gson gson = new Gson();
		Truck[] objects = new Truck[l_objects.size()];
		for(int i = 0; i < l_objects.size(); i++)
			objects[i] = l_objects.get(i);
		String json = gson.toJson(objects);
		//System.out.println("getObjects, json = " + json);
		return json;
	}
	
	@PostMapping("/getCoordinateWindow")
	public String getCoordinateWindow(){
		if (!initTaxi) {
			initTaxi = true;
			initTaxiSystem();
		}
		return gismap.computeCoordinateWindows();
	}
	@PostMapping("/createObject")
    public String createObject(@RequestBody MovingObject obj) {
    	synchronized(mgr){
    		count = count + 1;
    		IMovingObject c_obj = mgr.addObject(obj);
    		
    		System.out.println(name() + "::createObject " + obj.getId() + ", count = " + count);
    		return "{\"id\":\"" + c_obj.getId() + "\",\"lat\":" + c_obj.getLat() + ",\"lng\":" + c_obj.getLng() + "}";
    	}
        //return "{}";
    }
	@PostMapping("/createTruck")
    public String createTruck(@RequestBody Truck obj) {
    	synchronized(mgr){
    		count = count + 1;
    		IMovingObject c_obj = mgr.addObject(obj);
    		
    		System.out.println(name() + "::createTruck " + obj.getId() + ", count = " + count);
    		return "{\"id\":\"" + c_obj.getId() + "\",\"lat\":" + c_obj.getLat() + ",\"lng\":" + c_obj.getLng() + "}";
    	}
        //return "{}";
    }
    
	@PostMapping("/updateLocation")
    public String updateLocation(@RequestBody MovingObject obj) {
    	synchronized(mgr){
    		count = count + 1;
    		//System.out.println(name() + "::updateLocation, object " + obj.getId() + ", count = " + count);
    		/*
    		MovingObject n_obj = mgr.updateLocation(obj.getId(), obj.getLat(), obj.getLng());
    		if(n_obj == null){
    			return "{}";
    		}
    		return "{\"id\":\"" + n_obj.getId() + "\",\"lat\":" + n_obj.getLat() + ",\"lng\":" + n_obj.getLng() + "}";
    		*/
    		Truck t = mgr.getTruckById(obj.getId());
    		PositionTypeAction rs = t.updateLocation(obj.getLat(), obj.getLng());
    		return gson.toJson(rs);
    	}
        //return "{}";
    }
	/*
	@PostMapping("/updateServicePoints")
	public String updateServicePoints(@RequestBody UpdateServicePointsInput input){
		return mgr.updateServicePoints(input);		
	}
	*/
	@PostMapping("/updatePlannededRouteTruck")
	public String updatePlannedRouteTruck(@RequestBody UpdatePlannedRouteTruckInput input){
		
		return mgr.updatePlannedRouteTruck(input);
	}
	@PostMapping("/performServiceTruck")
	public String performServiceTruck(@RequestBody PerformServiceTruckInput input){
		Truck t = mgr.getTruckById(input.getTruckId());
		if(t == null){
			System.out.println(name() + "::performServiceTruck, truck(" + input.getTruckId() + ") NULL");
			return "{}";
		}
		t.performService();
		return "{}";
	}
    
	@PostMapping("/findShortestPath")
    public String findShortestPath(@RequestBody ShortestPathInput input) {
//		Gson gson = new Gson();
//		String inputJson = gson.toJson(input);
		//System.out.println(name() + "::findShortestPath, input = " + inputJson);
		
		//Path path = gismap.findPath(input.getFromID(), input.getToID());
		Path path = gismap.findPath(input.getFromPoint(),input.getToPoint());
		if(path == null){
			//System.out.println(name() + "::findShortestPath, findPath(" + input.getFromPoint() + "," + input.getToPoint() + "), NOT FOUND");;
			return "{}";
		}
		
		String json = gson.toJson(path);
		//System.out.println(name() + "::findShortestPath, result path = " + json);
		return json;
	}
	@PostMapping("/computeRouteVRP")
	public String computeRouteVRP(@RequestBody RouteVRPInput input){
		String json = "{}";
		Gson gson = new Gson();
		
		System.out.println("computeRouteVRP, input = " + gson.toJson(input));
//		VRPPlanner planner = new VRPPlanner();
		System.out.println("CWPlanner");
		CWPlanner planner = new CWPlanner();
		RouteVRPSolution sol = planner.computeRoute(input);
		json = gson.toJson(sol);
		System.out.println("computeRouteVRP, output = " + json);
		return json;
		
	}
	@PostMapping("/loadRouteVRPInput")
	public String loadRouteVRPInput(){
		try{
			Scanner in = new Scanner(new File(VRPPlanner.routeVRPInputFilename));
			String json = in.nextLine();
			in.close();
			System.out.println("loadRouteVRPInput, json = " + json);
			return json;
		}catch(Exception ex){
			ex.printStackTrace();
			return "{}";
		}
	}
	@PostMapping("/runRoute")
	public String runRoute(@RequestBody RunRouteInput input){
		Agent agent = new Agent(input.getVehicleCode());
		agent.setRoute(input.getRoute());
		agent.start();
		
		return "{}";
	}
	@PostMapping("/insertNewRequestsVRP")
	public String insertNewRequestsVRP(@RequestBody RouteVRPInput input){
		List<Truck> trucks = mgr.getTrucks();
		
		ExecuteRoute[] routes = new ExecuteRoute[trucks.size()];
		for(int i = 0; i < routes.length; i++){
			Truck o = trucks.get(i);
			String vehicleCode = o.getId();
			IServicePoint[] servicePoints = o.getServicePoints();//o.collectServicePoints();
			String currentLatLng = o.getLat() + "," + o.getLng();
			o.estimateNextPositionLatLng(ConfigParams.estimateTimeSecond);
			String changeFromLatLng = o.getEstimatedNextLat() + "," + o.getEstimatedNextLng();
			int changeFromServicePointIndex = o.getEstimatedNextServicePointIndex();
			routes[i] = new ExecuteRoute(vehicleCode, servicePoints, currentLatLng, changeFromLatLng, changeFromServicePointIndex);
		}
		RouteVRPInputPoint[] newRequests = new RouteVRPInputPoint[input.getPoints().length];
		for(int i = 0; i < newRequests.length; i++){
			RouteVRPInputPoint p = input.getPoints()[i];
			newRequests[i] = p;
		}
		OnlineVRPInput inp = new OnlineVRPInput(routes,newRequests);
		OnlineVRPPlanner planner = new OnlineVRPPlanner();
		OnlineVRPSolution sol = planner.computeOnlineInsertion(inp);
		for(int i = 0; i < sol.getRoutes().length; i++){
			ExecuteRoute r = sol.getRoutes()[i];
			if(planner.routeChange(r.getVehicleCode())){
				
				Truck t = mgr.getTruckById(r.getVehicleCode());
				System.out.println(name() + "::insertNewRequestsVRP, before update vehicle " + t.getId() + ", service points = " + t.getServicePointString());
				//t.setServicePoints(r.getServicePoints());
				t.updateServicePoints(r.getServicePoints());
				System.out.println(name() + "::insertNewRequestsVRP, after update vehicle " + t.getId() + ", service points = " + t.getServicePointString());
			}
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(sol);
		return json;
	}

	@PostMapping("/{region}/get-distances")
	public String getDistances(@RequestBody LatLngInput input, @PathVariable String region){
		if (!this.region.equals(region)) {
			gismap = null;
			gismap = new GISMap("data/" + region + "Road-connected.txt");
			this.region = region;
		}
		DistanceElement[] res = gismap.getDistanceElements(input);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(res);
		return json;
	}

	@PostMapping("/{region}/get-distance-elements")
	public String getDistanceElements(@RequestBody DistanceElementQuery input, @PathVariable String region){
		if (!this.region.equals(region)) {
			gismap = null;
			gismap = new GISMap("data/" + region + "Road-connected.txt");
			this.region = region;
		}
		gismap.calcDistanceElements(input);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(input);
		return json;
	}

	@PostMapping("/{region}/get-segments-on-window")
	public String getSegmentOnWindow(@RequestBody MapWindow input, @PathVariable String region) {
		if (!this.region.equals(region)) {
			gismap = null;
			gismap = new GISMap("data/" + region + "Road-connected.txt");
			this.region = region;
		}
		Segment[] segments = gismap.getSegmentOnWindow(input.getMinlat(), input.getMinlng(), input.getMaxlat(), input.getMaxlng());
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(segments);
		return json;
	}

	private TaxiAgent taxiAgent;
	private GoogleMapsQuery G;
	private MovingObjectManager moManager;
	private ConcurrentHashMap<String, TaxiState> mId2TaxiState;
	private ConcurrentHashMap<String, TaxiOrder> mOrderId2Order;
	private ConcurrentHashMap<String, TaxiOrder> mTaxeId2Order;

	private int initNbTaxi = 20;
	private int nbOrder = 0;
	private String taxiAgentServer = "http://localhost:8080/";

	private void initTaxiSystem() {
		gson = new Gson();
		MapWindow mw = gismap.computeCoordinateMapWindows();
		moManager = new MovingObjectManager(mw.getMinlat(), mw.getMinlng(), mw.getMaxlat(), mw.getMaxlng());
		mId2TaxiState = new ConcurrentHashMap<>();
		mOrderId2Order = new ConcurrentHashMap<>();
		mTaxeId2Order = new ConcurrentHashMap<>();
		for (int i = 1; i <= initNbTaxi; i++) {
			Point p = gismap.getRandomPoint();
			Taxi taxi = new Taxi("" + i, p.getLat(), p.getLng());
			mId2TaxiState.put(taxi.getId(), new TaxiState(taxi.getId(), "AVAILABLE", taxi, new TaxiRouteElement()));
			moManager.addObject(taxi);
		}
	}

	@PostMapping("/stimulate-taxi")
	public String stimulateTaxi() {
		TaxiState[] taxiStates;
		TaxiOrder[] taxiOrders;
		synchronized (mOrderId2Order) {
			taxiStates = new TaxiState[mId2TaxiState.size()];
			int i = 0;
			for (TaxiState taxiState : mId2TaxiState.values()) {
				taxiStates[i++] = taxiState;
			}
			taxiOrders = new TaxiOrder[mOrderId2Order.size()];
			i = 0;
			for (TaxiOrder order : mOrderId2Order.values()) {
				taxiOrders[i++] = order;
			}
		}
		TaxiSimulation simulation = new TaxiSimulation(taxiStates, taxiOrders);

//		if (taxiAgent == null || !taxiAgent.isAlive()) {
//			Point a = gismap.getRandomPoint();
//			Point b = gismap.getRandomPoint();
//
//			Path path = gismap.findPath(a.getLatLng(), b.getLatLng());
//			Taxi taxi = new Taxi("1", a.getLat(), a.getLng());
//			G = new GoogleMapsQuery();
//			taxiAgent = new TaxiAgent(taxi, new TaxiRouteElement(path.getPoints()), G);
//			taxiAgent.start();
//		}
//		TaxiState[] taxiStates = new TaxiState[1];
//		taxiStates[0] = taxiAgent.getTaxiState();
		String json = gson.toJson(simulation);
		return json;
	}

	@PostMapping("/update-taxi-state")
	public String updateTaxi(@RequestBody TaxiState taxiState) {
//		System.out.println("updateTaxi");
		mId2TaxiState.put(taxiState.getId(), taxiState);
		if (taxiState.getStatus().equals("AVAILABLE")) {
			moManager.addObject(new Taxi(taxiState.getId(), taxiState.getPosition().getLat(), taxiState.getPosition().getLng()));
			TaxiOrder order = mTaxeId2Order.get(taxiState.getId());
			mOrderId2Order.remove(order.getId());
			mTaxeId2Order.remove(taxiState.getId());
		} else if (taxiState.getStatus().equals("DELIVERY_RUNNING")) {
			TaxiOrder order = mTaxeId2Order.get(taxiState.getId());
			order.setPickupPoint(null);
		}
		return null;
	}

	@PostMapping("/run-taxi")
	public String runTaxi(@RequestBody TaxiOrderInput orderInput) {
		TaxiAgent agent = new TaxiAgent(orderInput);
		agent.start();
		return null;
	}

	@PostMapping("/request-taxi")
	public String requestTaxi() {
		Point pickupPoint = gismap.getRandomPoint();
		Point deliveryPoint = gismap.getRandomPoint();
		TaxiOrder order = new TaxiOrder("" + (++nbOrder), pickupPoint, deliveryPoint);
		mOrderId2Order.put(order.getId(), order);
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - startTime < 10 * 1000) { // 10s
			Taxi taxi = (Taxi) moManager.findNearestMovingObject(pickupPoint.getLat(), pickupPoint.getLng());
			if (taxi != null) {
				moManager.removeObject(taxi);
				mTaxeId2Order.put(taxi.getId(), order);
				String json = gson.toJson(new TaxiOrderInput(taxi, order));
				String ouput = TaxiUtils.execPost(taxiAgentServer + "run-taxi", json);
				return null;
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		mOrderId2Order.remove(order.getId());
		return null;
	}


}
