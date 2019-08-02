package localsearch.domainspecific.vehiclerouting.apps.ai4vn;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;







import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.leq.Leq;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.LexMultiValues;
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
import localsearch.domainspecific.vehiclerouting.vrp.moves.TwoOptMove1;
import localsearch.domainspecific.vehiclerouting.vrp.moves.TwoOptMoveOneRoute;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyCrossExchangeMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOnePointMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOrOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOrOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove3Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove4Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove5Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove6Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove7Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove8Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove3Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove4Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove5Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove6Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove7Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove8Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMoveOneRouteExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.INeighborhoodExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.search.GenericLocalSearch;

public class MinMaxCVRP {
	// raw data input
	public int nbVehicles;
	public int nbClients;
	public int capacity;
	public int[] x;
	public int[] y; // (x[i],y[i]): toa do cua diem i = 0,1,2,...,nbClients
	public int[] demand;
	public double[][] cost;
	public int depot;// depot = 0
	
	// object mapping
	public ArrayList<Point> startPoints;
	public ArrayList<Point> endPoints;
	public ArrayList<Point> clientPoints;
	public ArrayList<Point> allPoints;
	HashMap<Point, Integer> mPoint2ID = new HashMap<Point, Integer>();
	HashMap<Integer, Point> mID2Point = new HashMap<Integer, Point>();
	public NodeWeightsManager nwm;
	public ArcWeightsManager awm;
	
	
	// modelling
	public VRManager mgr;
	public VarRoutesVR XR;
	public IFunctionVR[] accDemand;
	public IFunctionVR[] distance;
	public ConstraintSystemVR CS;
	public IFunctionVR obj;
	public IFunctionVR totalDistance;
	public LexMultiFunctions F;
	
	// result
	public double best_obj;
	public double time_to_best;
	
	// visualization
	String[] colors = {"red","green","blue","brown","yellow","gray","black"};
	int canvasHeight = 600;
	int canvasWidth = 1000;
	int rad = 20;
	public void readData(String fn){
		try{
			Scanner in = new Scanner(new File(fn));
		
			nbClients = in.nextInt();
			nbVehicles = in.nextInt();
			int N = nbClients;
			x = new int[N+1];
			y = new int[N+1];
			depot = in.nextInt();
			x[depot] = in.nextInt();
			y[depot] = in.nextInt();
			capacity = in.nextInt();
			demand = new int[N+1];
			for(int i = 1; i <= N; i++){
				int id = in.nextInt();
				x[id] = in.nextInt();
				y[id] = in.nextInt();
				demand[id] = in.nextInt();
			}
			cost = new double[N+1][N+1];
			for(int i = 0; i <= N; i++){
				for(int j = 0; j <= N; j++){
					cost[i][j] = Math.sqrt((x[i]-x[j])*(x[i]-x[j]) + (y[i]-y[j])*(y[i]-y[j]));
				}
			}
			in.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
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
		
		
		//NE.add(new GreedyOrOptMove1Explorer(XR, F));
		//NE.add(new GreedyOrOptMove2Explorer(XR, F));
		//NE.add(new GreedyThreeOptMove1Explorer(XR, F));
		//NE.add(new GreedyThreeOptMove2Explorer(XR, F));
		//NE.add(new GreedyThreeOptMove3Explorer(XR, F));
		//NE.add(new GreedyThreeOptMove4Explorer(XR, F));
		//NE.add(new GreedyThreeOptMove5Explorer(XR, F));
		//NE.add(new GreedyThreeOptMove6Explorer(XR, F));
		//NE.add(new GreedyThreeOptMove7Explorer(XR, F));
		//NE.add(new GreedyThreeOptMove8Explorer(XR, F));
		NE.add(new GreedyTwoOptMove1Explorer(XR, F));
		NE.add(new GreedyTwoOptMove2Explorer(XR, F));
		NE.add(new GreedyTwoOptMove3Explorer(XR, F));
		NE.add(new GreedyTwoOptMove4Explorer(XR, F));
		NE.add(new GreedyTwoOptMove5Explorer(XR, F));
		NE.add(new GreedyTwoOptMove6Explorer(XR, F));
		NE.add(new GreedyTwoOptMove7Explorer(XR, F));
		NE.add(new GreedyTwoOptMove8Explorer(XR, F));
		//NE.add(new GreedyTwoOptMoveOneRouteExplorer(XR, F));
		
		//NE.add(new GreedyKPointsMoveExplorer(XR, F, 2, mandatory));
		
		//MMSearch se = new MMSearch(mgr);
		GenericLocalSearch se = new GenericLocalSearch(mgr);
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.setMaxStable(50);
		
		se.search(maxIter, timeLimit);
		
		optimizeTwoOptMove();
		reoptimizeTwoOptMoveOneRoute();
		
		System.out.println("search finished, XR = " + XR.toString());
		//print();

	}
	public void mySearch(){
		greedyConstructive();
		optimizeTwoOptMove();
		reoptimizeTwoOptMoveOneRoute();
		System.out.println("mySearch, violations = " + CS.violations() + ", totalDistance = " + totalDistance.getValue() + ", XR = " + XR.toString());
	}
	public void drawCircle(PrintWriter out, int centerX, int centerY, int radius, String label){
	      out.println("ctx.beginPath();");
	      out.println("ctx.arc(" + centerX + "," + centerY + "," +  radius + ", 0, 2 * Math.PI, false);");
	      out.println("ctx.fillStyle = 'white';");
	      out.println("ctx.fill();");
	      out.println("ctx.lineWidth = 2;");
	      out.println("ctx.strokeStyle = '#003300';");
	      out.println("ctx.stroke();");
		
	      out.println("ctx.font = \"20px Arial\";");
	      out.println("ctx.fillStyle = 'black';");
	      out.println("ctx.fillText(\"" + label + "\", " + (centerX - 5) + "," +  (centerY + 5) + ");");
	}
	public void drawLine(PrintWriter out, int fromX, int fromY, int toX, int toY, String color){
		out.println("ctx.beginPath();");
		out.println("ctx.moveTo(" + fromX + "," + fromY + ")");
		out.println("ctx.lineTo(" + toX + "," + toY + ")");
		out.println("ctx.strokeStyle = '" + color + "';");
		out.println("ctx.stroke()");
	}
	public boolean exists(int x, int y, ArrayList<Integer> X, ArrayList<Integer> Y){
		for(int i = 0; i < X.size(); i++){
			if(X.get(i) == x && Y.get(i) == y) return true;
		}
		return false;
	}
	public void genData(String filename, int nbClients, int nbVehicles){
		try{
			Random R = new Random();
			PrintWriter out = new PrintWriter(filename);
			int[] demand = new int[nbClients + 1];
			int cap = 0;
			int total = 0;
			for(int i = 1; i <= nbClients; i++){
				demand[i] = 1;
				demand[i] = R.nextInt(100) + 10;
				total += demand[i];
			}
			cap = total/nbVehicles + 2;
			out.println(nbClients + " " + nbVehicles);
			out.println(0 + " " + canvasWidth/2 + " " + canvasHeight/2 + " " + cap);
			ArrayList<Integer> X = new ArrayList<Integer>();
			ArrayList<Integer> Y = new ArrayList<Integer>();
			
			for(int i = 1; i <= nbClients; i++){
				int x = 0; int y = 0;
				do{
					x = (R.nextInt((canvasWidth-100)/(4*rad)) + 1)*4*rad;
					y = (R.nextInt((canvasHeight-100)/(4*rad)) + 1)*4*rad;
				}while(exists(x,y,X,Y));
				out.println(i + " " + x + " " + y + " " + demand[i]);
				X.add(x); Y.add(y);
			}
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void printHTML(String filename){
		try{
			PrintWriter out = new PrintWriter(filename);
			out.println("<!DOCTYPE html>");
			out.println("<html>");
			out.println("<body>");
			out.println("<canvas id=\"myCanvas\" width=\"" + canvasWidth + "\" height=\"" + canvasHeight + "\"></canvas>");
			out.println("<script>");
			out.println("var canvas = document.getElementById(\"myCanvas\");");
			out.println("var ctx = canvas.getContext(\"2d\");");
			
			for(int k = 1; k <= XR.getNbRoutes(); k++){
				Point s = XR.startPoint(k);
				//int from = mPoint2ID.get(s);
				Point p = s;
				Point np = null;
				while(p != XR.endPoint(k)){
					np = XR.next(p);
					int from = mPoint2ID.get(p);
					int to = mPoint2ID.get(np);
					drawLine(out, x[from], y[from], x[to], y[to],colors[k-1]);
					p = XR.next(p);
				}
				int from = mPoint2ID.get(p);
				int to = mPoint2ID.get(np);
				drawLine(out, x[from], y[from], x[to], y[to],colors[k-1]);
			}
			drawCircle(out, x[0], y[0], rad,"0");
			for(int i = 1; i <= nbClients; i++){
				drawCircle(out, x[i], y[i], 20,i+"");
			}
			out.println("</script>");
			out.println("</body>");
			out.println("</html>");
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void print(){
		System.out.println("capacity = " + capacity);
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			System.out.println("Route[" + k + "] = " + XR.routeString(k) + ", accDemand = " + accDemand[k-1].getValue() + 
					", distance = " + distance[k-1].getValue());
		}
		System.out.println("obj = " + obj.getValue());
	}
	
	public void interactiveRun(){
		Scanner in = new Scanner(System.in);
		while(true){
			System.out.print("enter command:");
			String line = in.nextLine();
			if(line.equals("q"))break;
			String[] s = line.split(" ");
			String cmd = s[0].trim();
			if(cmd.equals("html")){
				String fn = s[1].trim();
				printHTML(fn);
			}else if(cmd.equals("two-opt")){
				int x_id = Integer.valueOf(s[1].trim());
				int kx = Integer.valueOf(s[2].trim());
				int y_id = Integer.valueOf(s[3].trim());
				int ky = Integer.valueOf(s[4].trim());
				Point x = null; 
				if(x_id > 0) x = mID2Point.get(x_id); else x = XR.startPoint(kx);
				Point y = null; 
				if(y_id > 0) y = mID2Point.get(y_id); else y = XR.startPoint(ky);
				
				//double eval = totalDistance.evaluateTwoOptMoveOneRoute(x, y);
				double eval = totalDistance.evaluateTwoOptMove1(x, y);
				System.out.println("x = " + x.ID + ", y = " + y.ID + ", eval = " + eval + ", distance = " + totalDistance.getValue());
				//mgr.performTwoOptMoveOneRoute(x, y);
				mgr.performTwoOptMove1(x, y);
				reoptimizeTwoOptMoveOneRoute();
				System.out.println("after move x = " + x.ID + ", y = " + y.ID + ", eval = " + eval + ", violations = " + CS.violations() + 
						", distance = " + totalDistance.getValue());
				System.out.println("XR = " + XR.toString());
				
				
			}
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		MinMaxCVRP vrp = new MinMaxCVRP();
		
		
		int N = 40;
		int K = 5;
		//String filename = "data/min-max-cvrp-plane/mmvrp-20-3.inp"; 
		//String filename = "data/min-max-cvrp-plane/mmvrp-40-4.inp";
		String filename = "data/min-max-cvrp-plane/mmvrp-40-5.inp";
		//vrp.genData(filename, N, K);
		vrp.readData(filename);
		//vrp.readData("data/min-max-cvrp-plane/mmvrp-6-2.inp");
		//vrp.readData("data/min-max-cvrp-plane/mmvrp-60.inp");
		vrp.mapping();
		vrp.stateModel();
		vrp.search(1000,300);
		//vrp.mySearch();
		vrp.printHTML("min-max-vrp.html");
		vrp.interactiveRun();
	}

}
