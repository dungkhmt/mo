package com.dailyopt.mo.components.routeplanner.vrp;

import com.dailyopt.mo.components.maps.Path;
import com.dailyopt.mo.controller.ApiController;
import com.dailyopt.mo.model.modelFindPath.ShortestPathSolution;
import com.dailyopt.mo.model.routevrp.DistanceElement;
import com.dailyopt.mo.model.routevrp.Route;
import com.dailyopt.mo.model.routevrp.RouteVRPInput;
import com.dailyopt.mo.model.routevrp.RouteVRPInputPoint;
import com.dailyopt.mo.model.routevrp.RouteVRPSolution;
import com.google.gson.Gson;

import java.io.PrintWriter;
import java.util.*;

import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.leq.Leq;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedNodeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.ConstraintViolationsVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.functions.MaxVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyCrossExchangeMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOnePointMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove3Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove4Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove5Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove6Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove7Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove8Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.INeighborhoodExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.search.GenericLocalSearch;
public class VRPPlanner {
	public static String routeVRPInputFilename = "RouteVRPInput.json";
	
	private RouteVRPInput input;
	
	// raw data input
	private int nbVehicles;
	private int nbClients;
	private int capacity;
	private double[] x;
	private double[] y; // (x[i],y[i]): toa do cua diem i = 0,1,2,...,nbClients
	private double[] demand;
	private double[][] cost;
	private int depot;// depot = 0
	private HashMap<Integer, RouteVRPInputPoint> mID2InputPoint;
	
	// object mapping
	private ArrayList<Point> startPoints;
	private ArrayList<Point> endPoints;
	private ArrayList<Point> clientPoints;
	private ArrayList<Point> allPoints;
	HashMap<Point, Integer> mPoint2ID = new HashMap<Point, Integer>();
	HashMap<Integer, Point> mID2Point = new HashMap<Integer, Point>();
	private NodeWeightsManager nwm;
	private ArcWeightsManager awm;
	
	
	// modelling
	private VRManager mgr;
	private VarRoutesVR XR;
	private IFunctionVR[] accDemand;
	private IFunctionVR[] distance;
	private ConstraintSystemVR CS;
	private IFunctionVR obj;
	private IFunctionVR totalDistance;
	private LexMultiFunctions F;
	
	private void mapRawData(){
		nbClients = input.getPoints().length-1;
		double totalW = 0;
		int N = nbClients + 1;
		demand = new double[N];
		x = new double[N];
		y = new double[N];
		int idx = 0;
		depot = 0;
		mID2InputPoint = new HashMap<Integer, RouteVRPInputPoint>();
		
		for(int i = 0; i<input.getPoints().length; i++){
			RouteVRPInputPoint p = input.getPoints()[i];
			if(p.getType().equals("Depot")){
				capacity = p.getInfo();
				demand[depot] = 0;
				x[depot] = p.getLat();
				y[depot] = p.getLng();
				mID2InputPoint.put(depot, p);
			}else{
				totalW += p.getInfo();
				idx++;
				demand[idx] = p.getInfo();
				x[idx] = p.getLat();
				y[idx] = p.getLng();
				mID2InputPoint.put(idx, p);
			}
		}
		nbVehicles = (int)(totalW/capacity);
		if(nbVehicles*capacity < totalW) nbVehicles += 1;
		cost = new double[N][N];
		ArrayList<DistanceElement> l_distances = new ArrayList<DistanceElement>();
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				RouteVRPInputPoint pi = mID2InputPoint.get(i);
				RouteVRPInputPoint pj = mID2InputPoint.get(j);
				DistanceElement de = input.getDistanceElement(pi.getId(), pj.getId());
				if(de == null){
					Path P = ApiController.gismap.findPath(x[i] + "," + y[i], x[j] + "," + y[j]);
					cost[i][j] = P.getLength();
					System.out.println("mapRawData, compute path(" + i + "," + j + ") = " + P.toString());
					de = new DistanceElement(pi.getId(), pj.getId(),cost[i][j]);
				}else{
					cost[i][j] = de.getDistance();
				}
				l_distances.add(de);
			}
		}
		DistanceElement[] distances = new DistanceElement[l_distances.size()];
		for(int i = 0; i < l_distances.size(); i++)
			distances[i] = l_distances.get(i);
		input.setDistances(distances);
		
		// store to external files
		try{
		Gson gson = new Gson();
			String json = gson.toJson(input);
			PrintWriter out = new PrintWriter(routeVRPInputFilename);
			out.print(json);
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void greedySearch(){
		greedyConstructive();
		optimizeTwoOptMove();
		reoptimizeTwoOptMoveOneRoute();
	}
	public RouteVRPSolution computeRoute(RouteVRPInput input){
		this.input = input;
		mapRawData();
		mapping();
		stateModel();
		search(1000, 2);
		//greedySearch();
		
		Route[] routes = new Route[XR.getNbRoutes()];
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			ArrayList<Point> arr = new ArrayList<Point>();
			for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
				arr.add(p);
			}
			arr.add(XR.endPoint(k));
			RouteVRPInputPoint[] points = new RouteVRPInputPoint[arr.size()];
			for(int i = 0; i < arr.size(); i++){
				Point p = arr.get(i);
				points[i] = mID2InputPoint.get(p.ID);
			}
			Path[] paths = new Path[points.length-1];
			for(int i = 0; i < paths.length; i++){
				paths[i] = ApiController.gismap.findPath(points[i].getLat() + "," + points[i].getLng(), 
						points[i+1].getLat() + "," + points[i+1].getLng());
			}
			routes[k-1] = new Route(points, distance[k-1].getValue(),paths);
		}
		for(Point p1: allPoints){
			int i = mPoint2ID.get(p1);
			RouteVRPInputPoint ip = mID2InputPoint.get(p1.ID);
			for(Point p2: allPoints){
				int j = mPoint2ID.get(p2);
				RouteVRPInputPoint jp = mID2InputPoint.get(p2.ID);	
				//System.out.println("computeRoute, d[" + ip.getId() + "," + jp.getId() + "] = cost[" + i + "," + j + "] = " + 
				//cost[i][j] + " = " + awm.getWeight(p1, p2));
			}
		}
		
		RouteVRPSolution sol = new RouteVRPSolution(routes);
		return sol;
	}
	
	public void mapping(){
		clientPoints = new ArrayList<Point>();
		allPoints = new ArrayList<Point>();
		startPoints = new ArrayList<Point>();
		endPoints = new ArrayList<Point>();
	
		
		for(int k = 1; k <= nbVehicles; k++){
			Point s = new Point(depot);
			startPoints.add(s);
			allPoints.add(s);
			mPoint2ID.put(s, depot);
			Point t = new Point(depot);
			endPoints.add(t);
			allPoints.add(t);
			mPoint2ID.put(t, depot);
		}
		for(int i= 1; i <= nbClients; i++){
				Point p = new Point(i);
				clientPoints.add(p);
				allPoints.add(p);
				mPoint2ID.put(p, p.ID);
				mID2Point.put(p.ID, p);
		}
		
		nwm = new NodeWeightsManager(allPoints);
		awm = new ArcWeightsManager(allPoints);
		for(Point p: clientPoints)
			nwm.setWeight(p, demand[mPoint2ID.get(p)]);
		for(Point p: startPoints)
			nwm.setWeight(p, 0);
		for(Point p: endPoints)
			nwm.setWeight(p, 0);
		
		for(Point p1: allPoints){
			int i = mPoint2ID.get(p1);
			for(Point p2: allPoints){
				int j = mPoint2ID.get(p2);
				awm.setWeight(p1, p2, cost[i][j]);
			}
		}
		
		
	}
	
	public void stateModel(){
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		for(int i = 0; i < startPoints.size(); i++){
			Point s = startPoints.get(i);
			Point t = endPoints.get(i);
			XR.addRoute(s, t);
		}
		for(Point p: clientPoints)
			XR.addClientPoint(p);
		
		CS = new ConstraintSystemVR(mgr);
		AccumulatedWeightNodesVR awn = new AccumulatedWeightNodesVR(XR, nwm);
		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		accDemand = new IFunctionVR[XR.getNbRoutes()];
		
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			accDemand[k-1] = new AccumulatedNodeWeightsOnPathVR(awn, XR.endPoint(k));
			CS.post(new Leq(accDemand[k-1], capacity));			
		}
		
		distance = new IFunctionVR[XR.getNbRoutes()];
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			distance[k-1] = new AccumulatedEdgeWeightsOnPathVR(awe, XR.endPoint(k));
		}
		
		obj = new MaxVR(distance);
		
		totalDistance = new TotalCostVR(XR, awm);
		
		F = new LexMultiFunctions();
		F.add(new ConstraintViolationsVR(CS));
		F.add(obj);
		F.add(totalDistance);
		mgr.close();
	}
	public void reoptimizeTwoOptMoveOneRoute(int k){
		int it = 0;
		while(it <= 100000){
			it++;
			double minEval = Integer.MAX_VALUE;
			Point sel_x = null;
			Point sel_y = null;
			for (Point x = XR.startPoint(k); x != XR.endPoint(k); x = XR.next(x)) {
				for (Point y = XR.next(x); y != XR.endPoint(k); y = XR.next(y)) {
					if (XR.checkPerformTwoOptMoveOneRoute(x, y)) {
						double eval = totalDistance.evaluateTwoOptMoveOneRoute(x, y);
						if(eval < minEval && eval < 0){
							minEval = eval; sel_x = x; sel_y = y;
						}
					}
				}
			}
			if(sel_x != null){
				mgr.performTwoOptMoveOneRoute(sel_x, sel_y);
				System.out.println("MinMaxCVRP::reoptimizeTwoOptMoveOneRoute(" + k + "), it = " + it + ", eval = " + minEval + ", distance = " + totalDistance.getValue());
			}else{
				//System.out.println("MinMaxCVRP::reoptimizeTwoOptMoveOneRoute(" + k + ") BREAK");
				break;
			}
		}
	}
	public void greedyConstructive(){
		HashSet<Point> cand = new HashSet<Point>();
		for(Point p: clientPoints) cand.add(p);
		while(cand.size() > 0){
			double minEval = Integer.MAX_VALUE;
			Point sel_p = null;
			Point sel_x = null;
			for(Point p: cand){
				for(int k = 1; k <= XR.getNbRoutes(); k++){
					for(Point x = XR.startPoint(k); x != XR.endPoint(k); x = XR.next(x)){
						if(CS.evaluateAddOnePoint(p, x) <= 0){
							double eval = totalDistance.evaluateAddOnePoint(p, x);
							if(eval < minEval){
								minEval = eval;
								sel_p = p; sel_x = x;
							}
						}
					}
				}
			}
			if(sel_p != null){
				mgr.performAddOnePoint(sel_p, sel_x);
				cand.remove(sel_p);
				System.out.println("greedyContructive, violations = " + CS.violations() + ", totalDistance = " + totalDistance.getValue() + ", XR = " + XR.toString());
			}else{
				break;
			}
		}
	}
	public void reoptimizeTwoOptMoveOneRoute(){
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			reoptimizeTwoOptMoveOneRoute(k);
		}
	}
	public void optimizeTwoOptMove(){
		int it = 0;
		while(it < 10000){
			double minEval = Integer.MAX_VALUE;
			Point sel_x = null;
			Point sel_y = null;
			for (int i = 1; i <= XR.getNbRoutes(); i++) {
				for (int j = i + 1; j <= XR.getNbRoutes(); j++) {
					for (Point x = XR.next(XR.getStartingPointOfRoute(i)); XR.isClientPoint(x); x = XR.next(x)) {
						for (Point y = XR.next(XR.getStartingPointOfRoute(j)); XR.isClientPoint(y); y = XR.next(y)) {
							if (XR.checkPerformTwoOptMove(x, y)) {
								if(CS.evaluateTwoOptMove1(x, y) <= 0){
									double eval = totalDistance.evaluateTwoOptMove1(x, y);
									if(eval < 0 && eval < minEval){
										minEval = eval; sel_x = x; sel_y = y;
									}
								}
							}
						}
					}
				}
			}
			if(sel_x == null){
				break;
			}else{
				mgr.performTwoOptMove1(sel_x, sel_y);
				System.out.println("optimizeTwoOptMove, eval = " + minEval + ", violations = " + CS.violations() + ", totalDistance = " + totalDistance.getValue());
			}
		}
	}

	public void search(int maxIter, int timeLimit) {
		HashSet<Point> mandatory = new HashSet<Point>();
		for(Point p: clientPoints) mandatory.add(p);
		
		ArrayList<INeighborhoodExplorer> NE = new ArrayList<INeighborhoodExplorer>();
		NE.add(new GreedyOnePointMoveExplorer(XR, F));
		NE.add(new GreedyCrossExchangeMoveExplorer(XR, F));
		NE.add(new GreedyTwoOptMove1Explorer(XR, F));
		NE.add(new GreedyTwoOptMove2Explorer(XR, F));
		NE.add(new GreedyTwoOptMove3Explorer(XR, F));
		NE.add(new GreedyTwoOptMove4Explorer(XR, F));
		NE.add(new GreedyTwoOptMove5Explorer(XR, F));
		NE.add(new GreedyTwoOptMove6Explorer(XR, F));
		NE.add(new GreedyTwoOptMove7Explorer(XR, F));
		NE.add(new GreedyTwoOptMove8Explorer(XR, F));
		//NE.add(new GreedyTwoOptMoveOneRouteExplorer(XR, F));
		
		GenericLocalSearch se = new GenericLocalSearch(mgr);
		se.verbose = false;
		se.getNeighbohoodExplorerManager().verbose = false;
		
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.setMaxStable(50);
		
		se.search(maxIter, timeLimit);
		
		optimizeTwoOptMove();
		reoptimizeTwoOptMoveOneRoute();
		
		//print();

	}

}
