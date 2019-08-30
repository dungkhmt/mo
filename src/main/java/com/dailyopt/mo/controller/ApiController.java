package com.dailyopt.mo.controller;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import com.dailyopt.mo.components.api.performservicetruck.PerformServiceTruckInput;
import com.dailyopt.mo.components.api.updateplannedroutetruck.UpdatePlannedRouteTruckInput;
import com.dailyopt.mo.components.api.updateservicepoints.UpdateServicePointsInput;
import com.dailyopt.mo.components.maps.GISMap;
import com.dailyopt.mo.components.maps.Path;
import com.dailyopt.mo.components.maps.utils.GoogleMapsQuery;
import com.dailyopt.mo.components.movingobjects.IServicePoint;
import com.dailyopt.mo.components.movingobjects.MovingObject;
import com.dailyopt.mo.components.movingobjects.agent.Agent;
import com.dailyopt.mo.components.movingobjects.truck.PositionTypeAction;
import com.dailyopt.mo.components.movingobjects.truck.Truck;
import com.dailyopt.mo.components.objectmanager.ObjectManager;
import com.dailyopt.mo.components.routeplanner.onlinevrp.OnlineVRPPlanner;
import com.dailyopt.mo.components.routeplanner.onlinevrp.model.ExecuteRoute;
import com.dailyopt.mo.components.routeplanner.onlinevrp.model.OnlineVRPInput;
import com.dailyopt.mo.components.routeplanner.onlinevrp.model.OnlineVRPSolution;
import com.dailyopt.mo.components.routeplanner.vrp.CWPlanner;
import com.dailyopt.mo.components.routeplanner.vrp.VRPPlanner;
import com.dailyopt.mo.model.AddModel;
import com.dailyopt.mo.model.modelFindPath.ShortestPathInput;
import com.dailyopt.mo.model.routevrp.RouteVRPInput;
import com.dailyopt.mo.model.routevrp.RouteVRPInputPoint;
import com.dailyopt.mo.model.routevrp.RouteVRPSolution;
import com.dailyopt.mo.model.routevrp.RunRouteInput;
import com.google.gson.Gson;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class ApiController {
	private static Integer count = 0; 
    private static ObjectManager mgr = new ObjectManager();
    public static GISMap gismap = new GISMap();
    
	public static GoogleMapsQuery GMQ = new GoogleMapsQuery();
	public static Gson gson = new Gson();
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
	
	@PostMapping("/getObjects")
	public String getObjects(){
		List<MovingObject> l_objects = mgr.getObjects();
		Gson gson = new Gson();
		MovingObject[] objects = new MovingObject[l_objects.size()];
		for(int i = 0; i < l_objects.size(); i++)
			objects[i] = l_objects.get(i);
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
		return gismap.computeCoordinateWindows();
	}
	@PostMapping("/createObject")
    public String createObject(@RequestBody MovingObject obj) {
    	synchronized(mgr){
    		count = count + 1;
    		MovingObject c_obj = mgr.addObject(obj);
    		
    		System.out.println(name() + "::createObject " + obj.getId() + ", count = " + count);
    		return "{\"id\":\"" + c_obj.getId() + "\",\"lat\":" + c_obj.getLat() + ",\"lng\":" + c_obj.getLng() + "}";
    	}
        //return "{}";
    }
	@PostMapping("/createTruck")
    public String createTruck(@RequestBody Truck obj) {
    	synchronized(mgr){
    		count = count + 1;
    		MovingObject c_obj = mgr.addObject(obj);
    		
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
		Gson gson = new Gson();
		String inputJson = gson.toJson(input);
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
}
