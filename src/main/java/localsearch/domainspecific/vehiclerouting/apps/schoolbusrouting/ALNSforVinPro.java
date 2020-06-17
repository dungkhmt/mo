package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting;//package localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Random;
//import java.util.logging.Level;
//
//import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.service.BusRouteSolver;
//import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
//import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
//import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
//import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
//import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
//import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
//import localsearch.domainspecific.vehiclerouting.vrp.functions.CapacityConstraintViolationsVR;
//import localsearch.domainspecific.vehiclerouting.vrp.invariants.EarliestArrivalTimeVR;
//
//public class ALNSforVinPro {
//
//	private BusRouteSolver bsSolver;
//	
//	HashMap<Point, Integer> nChosed;
//	HashMap<Point, Boolean> removeAllowed;
//	
//	private int nRemovalOperators = 13;
//	private int nInsertionOperators = 14;
//	
//	//parameters
//	private int lower_removal = (int) 1*(BusScheduling.nRequest)/100;
//	private int upper_removal = (int) 15*(BusScheduling.nRequest)/100;
//	private int sigma1 = 3;
//	private int sigma2 = 1;
//	private int sigma3 = -5;
//	private double rp = 0.1;
//	private int nw = 1;
//	private double shaw1st = 0.5;
//	private double shaw2nd = 0.2;
//	private double shaw3rd = 0.1;
//	//private double temperature = 200;
//	//private double cooling_rate = 0.9995;
//	private int nTabu = 5;
//	//private double shaw4th = 0.2;
//	
//	public ALNSforVinPro(BusRouteSolver bsSolver){
//		this.bsSolver = bsSolver;
//		nChosed = new HashMap<Point, Integer>();
//		removeAllowed = new HashMap<Point, Boolean>();
//		//ArrayList<Point> clientPoints = XR.getClientPoints();
//		for(int i = 0; i < bsSolver.pickupPoints.size(); i++){
//			Point pi = bsSolver.pickupPoints.get(i);
//			nChosed.put(pi, 0);
//			removeAllowed.put(pi, true);
//		}
//	}
//	
//	public SolutionVinPro search(int maxIter, int timeLimit){
//		//insertion operators selection probabilities
//		double[] pti = new double[nInsertionOperators];
//		//removal operators selection probabilities
//		double[] ptd = new double[nRemovalOperators];
//		
//		//wi - number of times used during last iteration
//		int[] wi = new int[nInsertionOperators];
//		int[] wd = new int[nRemovalOperators];
//		
//		//pi_i - score of operator
//		int[] si = new int[nInsertionOperators];
//		int[] sd = new int[nRemovalOperators];
//		
//		
//		//init probabilites
//		for(int i=0; i<nInsertionOperators; i++){
//			pti[i] = 1.0/nInsertionOperators;
//			wi[i] = 1;
//			si[i] = 0;
//		}
//		for(int i=0; i<nRemovalOperators; i++){
//			ptd[i] = 1.0/nRemovalOperators;
//			wd[i] = 1;
//			sd[i] = 0;
//		}
//		
//		int it = 0;
//		
//		double best_cost = bsSolver.objective.getValue();
//		SolutionVinPro best_solution = new SolutionVinPro(bsSolver.XR, bsSolver.rejectPoints, best_cost);
//		
//		double start_search_time = System.currentTimeMillis();
//
//		Random r = new Random();
//		while( (System.currentTimeMillis()-start_search_time) < timeLimit && it++ < maxIter){
//			
//			double current_cost = bsSolver.objective.getValue();
//			SolutionVinPro current_solution = new SolutionVinPro(bsSolver.XR, bsSolver.rejectPoints, current_cost);
//			
//			//int i_selected_removal = get_operator(ptd);
//			int i_selected_removal = r.nextInt(8);
//			/*
//			 * Select remove operator
//			 */
//			//long timeRemoveStart = System.currentTimeMillis();
//			switch(i_selected_removal){
//			
//				case 0: random_removal(); break;
//				case 1: route_removal(); break;
//				//case 2: late_arrival_removal(); break;
//				//case 3: shaw_removal(); break;
//				case 2: proximity_based_removal(); break;
//				//case 5: time_based_removal(); break;
//				case 3: worst_removal(); break;
//				case 4: forbidden_removal(0); break;
//				case 5: forbidden_removal(1); break;
//				case 6: forbidden_removal(2); break;
//				case 7: forbidden_removal(3); break;
//			}
//			//long timeRemoveEnd = System.currentTimeMillis();
//			//long timeRemove = timeRemoveEnd - timeRemoveStart;
//			
//			//int i_selected_insertion = get_operator(pti);
//			int i_selected_insertion = r.nextInt(8);
//
//			/*
//			 * Select insertion operator
//			 */
//			//long timeInsertStart = Sy
//			switch(i_selected_insertion){
//				
//				case 0: greedy_insertion(); break;
//				//case 1: greedy_insertion_noise_function(); break;
//				case 1: second_best_insertion(); break;
//				//case 3: second_best_insertion_noise_function(); break;
//				case 2: regret_n_insertion(2); break;
//				//case 5: regret_n_insertion(3); break;
//				case 3: first_possible_insertion();break;
//				case 4: sort_before_insertion(0); break;
//				case 5: sort_before_insertion(1); break;
//				case 6: sort_before_insertion(2); break;
//				case 7: sort_before_insertion(3); break;
//			}
//			double new_cost = bsSolver.objective.getValue();
//			
//			/*
//			 * if new solution has cost better than current solution
//			 * 		update current solution =  new solution
//			 * 		if new solution has best cost
//			 * 			update best cost
//			 */
//			int new_nb_reject_points = bsSolver.rejectPoints.size();
//			int current_nb_reject_points = current_solution.get_rejectPoints().size();
//			if( new_nb_reject_points < current_nb_reject_points
//					|| (new_nb_reject_points == current_nb_reject_points && new_cost < current_cost)){
//				
//				int best_nb_reject_points = best_solution.get_rejectPoints().size();
//				
//				if(new_nb_reject_points < best_nb_reject_points
//						|| (new_nb_reject_points == best_nb_reject_points && new_cost < best_cost)){
//					
//					best_cost = new_cost;
//					best_solution = new SolutionVinPro(bsSolver.XR, bsSolver.rejectPoints, best_cost);
//				}
//			}
//			/*
//			 * if new solution has cost worst than current solution
//			 * 		because XR is new solution
//			 * 			copy current current solution to new solution if don't change solution
//			 */
//			else{
//				current_solution.copy2XR(bsSolver.XR);
//				bsSolver.rejectPoints = current_solution.get_rejectPoints();
//				//}
//			}
//			
//			//temperature = cooling_rate*temperature;
//		}
//		return best_solution;
//	}
//	
//	private void random_removal(){
//		Random R = new Random();
//		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
//		
//		ArrayList<Point> clientPoints = bsSolver.XR.getClientPoints();
//		Collections.shuffle(clientPoints);
//		
//		int inRemove = 0 ;
//		for(int i=0; i<clientPoints.size(); i++){
//			if(inRemove == nRemove)
//				break;
//			Point pr1 = clientPoints.get(i);
//			if(!removeAllowed.get(pr1))
//				continue;
//			if(bsSolver.rejectPoints.contains(pr1))
//				continue;
//
//			inRemove++;
//
//			bsSolver.mgr.performRemoveOnePoint(pr1);
//		
//			bsSolver.rejectPoints.add(pr1);
//			nChosed.put(pr1, nChosed.get(pr1)+1);
//			
//		}
//		//System.out.println("random_removal done");
//	}
//	
//	private void route_removal(){
//		Random R = new Random();
//		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
//		
//		int cnt = 0;
//		while(cnt < nRemove){
//			int K = bsSolver.XR.getNbRoutes();
//			R = new Random();
//			int iRouteRemoval = R.nextInt(K)+1;
//		
//			Point x = bsSolver.XR.getStartingPointOfRoute(iRouteRemoval);
//			Point next_x = bsSolver.XR.next(x);
//			while(next_x != bsSolver.XR.getTerminatingPointOfRoute(iRouteRemoval)){
//				x = next_x;
//				next_x = bsSolver.XR.next(x);
//				//if(!removeAllowed.get(x))
//					//continue;
//				bsSolver.rejectPoints.add(x);
//
//				bsSolver.mgr.performRemoveOnePoint(x);
//				nChosed.put(x, nChosed.get(x)+1);
//				cnt++;
//			}
//			if(bsSolver.rejectPoints.size() == bsSolver.XR.getClientPoints().size())
//				break;
//		}
//	}
//	
//	private void late_arrival_removal(){
//		Random R = new Random();
//		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
//		
//		int iRemove = 0;
//		while(iRemove++ != nRemove){
//			
//			double deviationMax = Double.MIN_VALUE;
//			Point removedPickup = null;
//			
//			for(int k = 1; k <= bsSolver.XR.getNbRoutes(); k++){
//				Point x = bsSolver.XR.getStartingPointOfRoute(k);
//				for(x = bsSolver.XR.next(x); x != bsSolver.XR.getTerminatingPointOfRoute(k); x = bsSolver.XR.next(x)){
//					if(!removeAllowed.get(x))
//						continue;
//					
//					double arrivalTime = bsSolver.eat.getEarliestArrivalTime(bsSolver.XR.prev(x))+ 
//							bsSolver.serviceDuration.get(bsSolver.XR.prev(x))+
//							bsSolver.awm.getDistance(bsSolver.XR.prev(x), x);
//					
//					
//					double serviceTime = 1.0*bsSolver.earliestAllowedArrivalTime.get(x);
//					serviceTime = arrivalTime > serviceTime ? arrivalTime : serviceTime;
//					
//					double depatureTime = serviceTime + bsSolver.serviceDuration.get(x);
//					
//					double deviation = depatureTime - arrivalTime;
//					if(deviation > deviationMax){
//						deviationMax = deviation;
//						removedPickup = x;
//					}
//				}
//			}
//			
//			if(removedPickup == null)
//				break;
//			
//			bsSolver.rejectPoints.add(removedPickup);
//			nChosed.put(removedPickup, nChosed.get(removedPickup)+1);
//			
//			bsSolver.mgr.performRemoveOnePoint(removedPickup);
//		}
//	}
//
//	private void shaw_removal(){
//		Random R = new Random();
//		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
//		
//		ArrayList<Point> clientPoints = bsSolver.XR.getClientPoints();
//		int ipRemove;
//		
//		/*
//		 * select randomly request r1 and its delivery dr1
//		 */
//		Point r1;
//		do{
//			ipRemove = R.nextInt(clientPoints.size());
//			r1 = clientPoints.get(ipRemove);	
//		//}while(search_input.rejectPoints.contains(r1) || !removeAllowed.get(r1));
//		}while(bsSolver.rejectPoints.contains(r1));
//		/*
//		 * Remove request most related with r1
//		 */
//		int inRemove = 0;
//		while(inRemove++ != nRemove && r1 !=null){
//			
//			Point removedPickup = null;
//			double relatedMin =  Double.MAX_VALUE;
//			
//			int routeOfR1 = bsSolver.XR.route(r1);
//			/*
//			 * Compute arrival time at request r1 and its delivery dr1
//			 */
//			Point td = bsSolver.XR.prev(r1);
//			
//			double arrivalTimeR1 = bsSolver.eat.getEarliestArrivalTime(bsSolver.XR.prev(r1))+
//					bsSolver.serviceDuration.get(bsSolver.XR.prev(r1))+
//					bsSolver.awm.getDistance(bsSolver.XR.prev(r1), r1);
//			
//			double serviceTimeR1 = 1.0*bsSolver.earliestAllowedArrivalTime.get(r1);
//			serviceTimeR1 = arrivalTimeR1 > serviceTimeR1 ? arrivalTimeR1 : serviceTimeR1;
//			
//			double depatureTimeR1 = serviceTimeR1 + bsSolver.serviceDuration.get(r1);
//			
//			bsSolver.mgr.performRemoveOnePoint(r1);
//			bsSolver.rejectPoints.add(r1);
//			nChosed.put(r1, nChosed.get(r1)+1);
//			
//			/*
//			 * find the request is the most related with r1
//			 */
//			for(int k = 1; k <= bsSolver.XR.getNbRoutes(); k++){
//				Point x = bsSolver.XR.getStartingPointOfRoute(k);
//				for(x = bsSolver.XR.next(x); x != bsSolver.XR.getTerminatingPointOfRoute(k); x = bsSolver.XR.next(x)){
//					
//					if(!removeAllowed.get(x))
//						continue;
//					
//					/*
//					 * Compute arrival time of x and its delivery dX
//					 */
//					double arrivalTimeX = bsSolver.eat.getEarliestArrivalTime(bsSolver.XR.prev(x))+
//							bsSolver.serviceDuration.get(bsSolver.XR.prev(x))+
//							bsSolver.awm.getDistance(bsSolver.XR.prev(x), x);
//					
//					double serviceTimeX = 1.0*bsSolver.earliestAllowedArrivalTime.get(x);
//					serviceTimeX = arrivalTimeX > serviceTimeX ? arrivalTimeX : serviceTimeX;
//					
//					double depatureTimeX = serviceTimeX + bsSolver.serviceDuration.get(x);
//					
//					/*
//					 * Compute related between r1 and x
//					 */
//					int lr1x;
//					if(routeOfR1 == k){
//						lr1x = 1;
//					}else{
//						lr1x = -1;
//					}
//					
//					double related = shaw1st*(bsSolver.awm.getDistance(r1, x)) +
//							shaw2nd*(Math.abs(depatureTimeR1-depatureTimeX))+
//							shaw3rd*lr1x;
//					if(related < relatedMin){
//						relatedMin = related;
//						removedPickup = x;
//					}
//				}
//			}
//			
//			r1 = removedPickup;
//		}
//		
//	}
//	
//	private void proximity_based_removal(){
//		
//		Random R = new Random();
//		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
//		
//		ArrayList<Point> clientPoints = bsSolver.XR.getClientPoints();
//		int ipRemove;
//		
//		/*
//		 * select randomly request r1 and its delivery dr1
//		 */
//		Point r1;
//		do{
//			ipRemove = R.nextInt(clientPoints.size());
//			r1 = clientPoints.get(ipRemove);	
//		//}while(search_input.rejectPoints.contains(r1) || !removeAllowed.get(r1));
//		}while(bsSolver.rejectPoints.contains(r1));
//		/*
//		 * Remove request most related with r1
//		 */
//		int inRemove = 0;
//		while(inRemove++ != nRemove && r1 != null){
//			
//			Point removedPickup = null;
//			double relatedMin =  Double.MAX_VALUE;
//			
//			bsSolver.rejectPoints.add(r1);
//			nChosed.put(r1, nChosed.get(r1)+1);
//			
//			bsSolver.mgr.performRemoveOnePoint(r1);
//			
//			/*
//			 * find the request is the most related with r1
//			 */
//			for(int k = 1; k <= bsSolver.XR.getNbRoutes(); k++){
//				Point x = bsSolver.XR.getStartingPointOfRoute(k);
//				for(x = bsSolver.XR.next(x); x != bsSolver.XR.getTerminatingPointOfRoute(k); x = bsSolver.XR.next(x)){
//					if(!removeAllowed.get(x))
//						continue;
//					
//					/*
//					 * Compute related between r1 and x
//					 */
//					
//					double related = shaw1st*(bsSolver.awm.getDistance(r1, x));
//					
//					if(related < relatedMin){
//						relatedMin = related;
//						removedPickup = x;
//					}
//				}
//			}
//			
//			r1 = removedPickup;
//		}
//	}
//	
//	private void time_based_removal(){
//		Random R = new Random();
//		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
//		
//		ArrayList<Point> clientPoints = bsSolver.XR.getClientPoints();
//		int ipRemove;
//		
//		/*
//		 * select randomly request r1 and its delivery dr1
//		 */
//		Point r1;
//		do{
//			ipRemove = R.nextInt(clientPoints.size());
//			r1 = clientPoints.get(ipRemove);	
//		//}while(search_input.rejectPoints.contains(r1) || !removeAllowed.get(r1));
//		}while(bsSolver.rejectPoints.contains(r1) );
//		/*
//		 * Remove request most related with r1
//		 */
//		int inRemove = 0;
//		while(inRemove++ != nRemove && r1 != null){
//			
//			Point removedPickup = null;
//			double relatedMin =  Double.MAX_VALUE;
//			
//			/*
//			 * Compute arrival time at request r1 and its delivery dr1
//			 */
//			double arrivalTimeR1 = bsSolver.eat.getEarliestArrivalTime(bsSolver.XR.prev(r1))+
//					bsSolver.serviceDuration.get(bsSolver.XR.prev(r1))+
//					bsSolver.awm.getDistance(bsSolver.XR.prev(r1), r1);
//			
//			double serviceTimeR1 = 1.0*bsSolver.earliestAllowedArrivalTime.get(r1);
//			serviceTimeR1 = arrivalTimeR1 > serviceTimeR1 ? arrivalTimeR1 : serviceTimeR1;
//			
//			double depatureTimeR1 = serviceTimeR1 + bsSolver.serviceDuration.get(r1);
//			
//			bsSolver.rejectPoints.add(r1);
//			nChosed.put(r1, nChosed.get(r1));
//			
//			bsSolver.mgr.performRemoveOnePoint(r1);
//			
//			/*
//			 * find the request is the most related with r1
//			 */
//			for(int k = 1; k <= bsSolver.XR.getNbRoutes(); k++){
//				Point x = bsSolver.XR.getStartingPointOfRoute(k);
//				for(x = bsSolver.XR.next(x); x != bsSolver.XR.getTerminatingPointOfRoute(k); x = bsSolver.XR.next(x)){
//					
//					if(!removeAllowed.get(x))
//						continue;
//				
//					/*
//					 * Compute arrival time of x and its delivery dX
//					 */
//					double arrivalTimeX =  bsSolver.eat.getEarliestArrivalTime(bsSolver.XR.prev(x))+
//							bsSolver.serviceDuration.get(bsSolver.XR.prev(x))+
//							bsSolver.awm.getDistance(bsSolver.XR.prev(x), x);
//					
//					double serviceTimeX = 1.0*bsSolver.earliestAllowedArrivalTime.get(x);
//					serviceTimeX = arrivalTimeX > serviceTimeX ? arrivalTimeX : serviceTimeX;
//					
//					double depatureTimeX = serviceTimeX + bsSolver.serviceDuration.get(x);
//					
//					
//					/*
//					 * Compute related between r1 and x
//					 */
//					
//					double related = shaw2nd*(Math.abs(depatureTimeR1-depatureTimeX));
//					
//					if(related < relatedMin){
//						relatedMin = related;
//						removedPickup = x;
//					}
//				}
//			}
//			
//			r1 = removedPickup;
//		}
//	}
//	
//	private void worst_removal(){
//		Random R = new Random();
//		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
//	
//		int inRemove = 0;
//		while(inRemove++ != nRemove){
//			
//			double maxCost = Double.MIN_VALUE;
//			Point removedPickup = null;
//			
//			for(int k = 1; k <= bsSolver.XR.getNbRoutes(); k++){
//				Point x = bsSolver.XR.getStartingPointOfRoute(k);
//				for(x = bsSolver.XR.next(x); x != bsSolver.XR.getTerminatingPointOfRoute(k); x = bsSolver.XR.next(x)){
//					
//					if(!removeAllowed.get(x))
//						continue;
//					
//					double cost = bsSolver.objective.evaluateRemoveOnePoint(x);
//					if(cost > maxCost){
//						maxCost = cost;
//						removedPickup = x;
//					}
//				}
//			}
//			
//			if(removedPickup == null)
//				break;
//			
//			bsSolver.rejectPoints.add(removedPickup);
//			nChosed.put(removedPickup, nChosed.get(removedPickup)+1);
//			
//			bsSolver.mgr.performRemoveOnePoint(removedPickup);
//		}
//	}
//		
//	private void forbidden_removal(int nRemoval){
//		
//		for(int i=0; i < bsSolver.pickupPoints.size(); i++){
//			Point pi = bsSolver.pickupPoints.get(i);
//			
//			if(nChosed.get(pi) > nTabu){
//				removeAllowed.put(pi, false);
//			}
//		}
//		
//		switch(nRemoval){
//			case 0: random_removal(); break;
//			case 1: route_removal(); break;
//			//case 2: late_arrival_removal(); break;
//			//case 3: shaw_removal(); break;
//			case 2: proximity_based_removal(); break;
//			//case 5: time_based_removal(); break;
//			case 3: worst_removal(); break;
//		}
//		
//		for(int i=0; i < bsSolver.pickupPoints.size(); i++){
//			Point pi = bsSolver.pickupPoints.get(i);
//			removeAllowed.put(pi, true);
//		}
//	}
//	
//	public boolean checkDistanceConstraint(Point x, Point y, Point st){
//		double xst = bsSolver.awm.getWeight(x, st);
//		double yst = bsSolver.awm.getWeight(y, st);
//		double yx = bsSolver.awm.getWeight(y, x);
//		double nyst = bsSolver.awm.getWeight(bsSolver.XR.next(y), st);
//		double xny = bsSolver.awm.getWeight(x, bsSolver.XR.next(y));
//		int r = bsSolver.XR.route(st);
//		Point en = bsSolver.XR.getTerminatingPointOfRoute(r);
//		double yny = bsSolver.awm.getWeight(y, bsSolver.XR.next(y));
//		double te = bsSolver.eat.getEarliestArrivalTime().get(en);
//		double sttime = bsSolver.eat.getEarliestArrivalTime(bsSolver.XR.next(st));
//		if(y != st){
//			double endtime = te + yx + xny - yny;
//			double drtime = bsSolver.awm.getWeight(bsSolver.XR.next(st), en);
//			if(endtime - sttime > 1.5* drtime)
//				return false;
//		}
//		else{
//			double endtime = te + xny;
//			double drtime = bsSolver.awm.getWeight(x, en);
//			if(endtime - sttime > 1.5*drtime)
//				return false;			
//		}
////		if(y == st &&  xst < nyst)
////			return false;
////		if(xst > yst || xst < nyst)
////			return false;
//////		HashMap<Point, Double> earliestArrivalTime = eat.getEarliestArrivalTime();
//////		double endTime = earliestArrivalTime.get(XR.getTerminatingPointOfRoute(XR.route(y)));
////		if(y == st && xny + nyst > 1.3*xst)
////			return false;
////		if(y != st && yx + xst > 1.3*yst)
////				return false;
//		return true;
//	}
//	
////	public boolean checkDistanceConstraint(Point x, Point y, Point st){
////		if(y == st && awm.getWeight(x, st) >= awm.getWeight(XR.next(st), st))
////			return true;
////		if(awm.getWeight(x, st) <= awm.getWeight(y, st) && awm.getWeight(x, st) >= awm.getWeight(XR.next(y), st))
////			return true;
////		return false;
////	}
// 	private void greedy_insertion(){
//		for(int i=0; i<bsSolver.rejectPoints.size(); i++){
//			Point pickup = bsSolver.rejectPoints.get(i);
//			Point best_insertion = null;
//			
//			double best_objective = Double.MAX_VALUE;
//					
//			for(int r = 1; r <= bsSolver.XR.getNbRoutes(); r++){
//				Point st = bsSolver.XR.getStartingPointOfRoute(r);
//				for(Point p = bsSolver.XR.getStartingPointOfRoute(r); p != bsSolver.XR.getTerminatingPointOfRoute(r); p = bsSolver.XR.next(p)){
//					if(bsSolver.S.evaluateAddOnePoint(pickup, p) == 0 && bsSolver.capCons.evaluateAddOnePoint(pickup, p) == 0 
//							&& bsSolver.checkSizeBus(r, bsSolver.getRoadBlock(p.getID(), pickup.getID())) 
//							&& checkDistanceConstraint(pickup, p, st)){
//						double cost = bsSolver.objective.evaluateAddOnePoint(pickup, p);
//						if(cost < best_objective){
//							best_objective = cost;
//							best_insertion = p;
//						}
//					}					
//				}
//			}
//			if(best_insertion != null){
//				bsSolver.mgr.performAddOnePoint(pickup, best_insertion);
//				bsSolver.rejectPoints.remove(pickup);
//				i--;
//			}
//		}
//		
//	}
//	
//// 	private void greedy_insertion_noise_function(){
//// 		
//// 		BusScheduling.LOGGER.log(Level.INFO,"Inserting peoples to route");
//// 		
//// 		for(int i=0; i<search_input.rejectPoints.size(); i++){
////			Point pickup = search_input.rejectPoints.get(i);
////			
////			Point best_insertion_pickup = null;
////			
////			double best_objective = Double.MAX_VALUE;
////			
////			for(int r = 1; r <= XR.getNbRoutes(); r++){
////				Point st = XR.getStartingPointOfRoute(r);
////				for(Point p = XR.getStartingPointOfRoute(r); p != XR.getTerminatingPointOfRoute(r); p = XR.next(p)){
////					//check constraint
////					if(S.evaluateAddOnePoint(pickup, p) == 0 && capCons.evaluateAddOnePoint(pickup, p) == 0 
////							&& checkSizeBus(r, pickup.getName()) && checkDistanceConstraint(pickup, p, st)){
////						//cost improve
////						double cost = objective.evaluateAddOnePoint(pickup, p);
////						double ran = Math.random()*2-1;
////						cost += BusScheduling.MAX_DISTANCE*0.1*ran;
////						if( cost < best_objective){
////							best_objective = cost;
////							best_insertion_pickup = p;
////						}
////					}
////				}
////			}
////			
////			if(best_insertion_pickup != null){
////				mgr.performAddOnePoint(pickup, best_insertion_pickup);
////				search_input.rejectPoints.remove(pickup);
////				i--;
////			}
////		}
//// 	}
// 	
// 	private void second_best_insertion(){
// 		for(int i = 0; i < bsSolver.rejectPoints.size(); i++){
//			Point pickup = bsSolver.rejectPoints.get(i);
//			
//			Point second_best_insertion_pickup = null;
//			
//			double best_objective = Double.MAX_VALUE;
//			double second_best_objective = Double.MAX_VALUE;
//			
//			for(int r = 1; r <= bsSolver.XR.getNbRoutes(); r++){
//				Point st = bsSolver.XR.getStartingPointOfRoute(r);
//				for(Point p = bsSolver.XR.getStartingPointOfRoute(r); p != bsSolver.XR.getTerminatingPointOfRoute(r); p = bsSolver.XR.next(p)){
//					if(bsSolver.S.evaluateAddOnePoint(pickup, p) == 0 
//							&& bsSolver.capCons.evaluateAddOnePoint(pickup, p) == 0 
//							&& bsSolver.checkSizeBus(r, bsSolver.getRoadBlock(p.getID(), pickup.getID())) 
//							&& checkDistanceConstraint(pickup, p, st)){
//						//cost improve
//						double cost = bsSolver.objective.evaluateAddOnePoint(pickup, p);
//						if( cost <= best_objective){
//							second_best_objective = best_objective;
//							best_objective = cost;
//						}else{
//							if(cost < second_best_objective){
//								second_best_objective = cost;
//								second_best_insertion_pickup = p;
//							}
//						}
//					}
//				}
//			}
//			
//			if(second_best_insertion_pickup != null){
//				bsSolver.mgr.performAddOnePoint(pickup, second_best_insertion_pickup);
//				bsSolver.rejectPoints.remove(pickup);
//				i--;
//			}
//		}
// 	}
//
//// 	private void second_best_insertion_noise_function(){
//// 		for(int i=0; i<search_input.rejectPoints.size(); i++){
////			Point pickup = search_input.rejectPoints.get(i);
////			
////			Point second_best_insertion_pickup = null;
////			
////			double best_objective = Double.MAX_VALUE;
////			double second_best_objective = Double.MAX_VALUE;
////			
////			for(int r = 1; r <= XR.getNbRoutes(); r++){
////				Point st = XR.getStartingPointOfRoute(r);
////				for(Point p = XR.getStartingPointOfRoute(r); p != XR.getTerminatingPointOfRoute(r); p = XR.next(p)){
////					//check constraint
////					if(S.evaluateAddOnePoint(pickup, p) == 0 && capCons.evaluateAddOnePoint(pickup, p) == 0 
////							&& checkSizeBus(r, pickup.getName()) && checkDistanceConstraint(pickup, p, st)){
////						//cost improve
////						double cost = objective.evaluateAddOnePoint(pickup, p);
////						double ran = Math.random()*2-1;
////						cost += BusScheduling.MAX_DISTANCE*0.1*ran;
////						if( cost <= best_objective){
////							second_best_objective = best_objective;
////							best_objective = cost;
////						}else{
////							if(cost < second_best_objective){
////								second_best_objective = cost;
////								second_best_insertion_pickup = p;
////							}
////						}
////					}
////				}
////			}
////			
////			if(second_best_insertion_pickup != null){
////				mgr.performAddOnePoint(pickup, second_best_insertion_pickup);
////				search_input.rejectPoints.remove(pickup);
////				i--;
////			}
////		}
//// 	}
//
// 	private void regret_n_insertion(int n){
// 		for(int i = 0; i < bsSolver.rejectPoints.size(); i++){
//			Point pickup = bsSolver.rejectPoints.get(i);
//			
//			Point best_insertion_pickup = null;
//			
//			double n_best_objective[] = new double[n];
//			double best_regret_value = Double.MIN_VALUE;
//			
//			for(int it=0; it<n; it++){
//				n_best_objective[it] = Double.MAX_VALUE;
//			}
//			
//			for(int r = 1; r <= bsSolver.XR.getNbRoutes(); r++){
//				Point st = bsSolver.XR.getStartingPointOfRoute(r);
//				for(Point p = bsSolver.XR.getStartingPointOfRoute(r); p != bsSolver.XR.getTerminatingPointOfRoute(r); p = bsSolver.XR.next(p)){
//					if(bsSolver.S.evaluateAddOnePoint(pickup, p) == 0 
//							&& bsSolver.capCons.evaluateAddOnePoint(pickup, p) == 0 
//							&& bsSolver.checkSizeBus(r, bsSolver.getRoadBlock(p.getID(), pickup.getID()))
//							&& checkDistanceConstraint(pickup, p, st)){
//						//cost improve
//						double cost = bsSolver.objective.evaluateAddOnePoint(pickup, p);
//						for(int it = 0; it < n; it++){
//							if(n_best_objective[it] > cost){
//								for(int it2 = n-1; it2 > it; it2--){
//									n_best_objective[it2] = n_best_objective[it2-1];
//								}
//								n_best_objective[it] = cost;
//								break;
//							}
//						}
//						double regret_value = 0;
//						for(int it=1; it<n; it++){
//							regret_value += Math.abs(n_best_objective[it] - n_best_objective[0]);
//						}
//						if(regret_value > best_regret_value){
//							best_regret_value = regret_value;
//							best_insertion_pickup = p;
//						}
//					}
//				}
//			}
//			
//			if(best_insertion_pickup != null){
//				bsSolver.mgr.performAddOnePoint(pickup, best_insertion_pickup);
//				bsSolver.rejectPoints.remove(pickup);
//				i--;
//			}
//		}
// 	}
// 	
//	private void first_possible_insertion(){
//		for(int i = 0; i < bsSolver.rejectPoints.size(); i++){
//			Point pickup = bsSolver.rejectPoints.get(i);
//			boolean finded = false;
//					
//			for(int r = 1; r <= bsSolver.XR.getNbRoutes(); r++){
//				if(finded)
//					break;
//				Point st = bsSolver.XR.getStartingPointOfRoute(r);
//				for(Point p = bsSolver.XR.getStartingPointOfRoute(r); p != bsSolver.XR.getTerminatingPointOfRoute(r); p = bsSolver.XR.next(p)){
//					if(bsSolver.S.evaluateAddOnePoint(pickup, p) == 0 
//							&& bsSolver.capCons.evaluateAddOnePoint(pickup, p) == 0 
//							&& bsSolver.checkSizeBus(r, bsSolver.getRoadBlock(p.getID(), pickup.getID()))
//							&& checkDistanceConstraint(pickup, p, st)){
//						bsSolver.mgr.performAddOnePoint(pickup, p);
//						bsSolver.rejectPoints.remove(pickup);
//						i--;
//						finded = true;
//						break;
//					}
//				}
//			}
//		}
//	}
// 	
//	private void sort_before_insertion(int iInsertion){
//		sort_reject_people();
//		
//		switch(iInsertion){
//			case 0: greedy_insertion(); break;
//			//case 1: greedy_insertion_noise_function(); break;
//			case 1: second_best_insertion(); break;
//			//case 3: second_best_insertion_noise_function(); break;
//			case 2: regret_n_insertion(2); break;
//			//case 5: regret_n_insertion(3); break;
//			case 3: first_possible_insertion(); break;
//		}
//		
//		Collections.shuffle(bsSolver.rejectPoints);
//	}
//	
//	private void sort_reject_people(){
//		Collections.shuffle(bsSolver.rejectPoints);
//	}
//	
//	//roulette-wheel mechanism
// 	private int get_operator(double[] p){
// 		//String message = "probabilities input \n";
// 		
// 		int n = p.length;
//		double[] s = new double[n];
//		s[0] = 0+p[0];
//		
//		//String messagep = ("p = ["+p[0]+", ");
//		//String messages = ("s = ["+s[0]+", ");
//		
//		for(int i=1; i<n; i++){
//			//messagep += (p[i]+", ");
//			s[i] = s[i-1]+p[i]; 
//			//messages += (s[i]+", ");
//		}
//		//messagep += ("]");
//		//messages += ("]");
//		
//		double r = s[n-1]*Math.random();
//		//String messr = ("radom value = " + r);
//		
//		if(r>=0 && r <= s[0])
//			return 0;
//		
//		for(int i=1; i<n; i++){
//			if(r>s[i-1] && r<=s[i])
//				return i;
//		}
//		return -1;
//	}
//	
//}
