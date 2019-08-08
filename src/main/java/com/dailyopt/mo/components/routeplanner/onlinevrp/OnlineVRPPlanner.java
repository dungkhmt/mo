package com.dailyopt.mo.components.routeplanner.onlinevrp;

import java.util.ArrayList;

import com.dailyopt.mo.components.maps.Path;
import com.dailyopt.mo.components.maps.Point;
import com.dailyopt.mo.components.movingobjects.IServicePoint;
import com.dailyopt.mo.components.movingobjects.truck.ServicePointDelivery;
import com.dailyopt.mo.components.routeplanner.onlinevrp.model.ExecuteRoute;
import com.dailyopt.mo.components.routeplanner.onlinevrp.model.OnlineVRPInput;
import com.dailyopt.mo.components.routeplanner.onlinevrp.model.OnlineVRPSolution;
import com.dailyopt.mo.components.routeplanner.onlinevrp.model.RouteSegment;
import com.dailyopt.mo.controller.ApiController;
import com.dailyopt.mo.model.routevrp.RouteVRPInputPoint;

/*
 * online insertion in VRP
 */
public class OnlineVRPPlanner {
	private OnlineVRPInput input;
	private ExecuteRoute[] routes;
	private RouteVRPInputPoint[] newRequests;
	
	private double evaluateExtraDistanceInsert(ExecuteRoute r, int routeSegmentIndex, RouteVRPInputPoint p){
		// return extra distance when inserting point p into routeSegment r[routeSegmentIndex]
		RouteSegment rs = r.getRouteSegments()[routeSegmentIndex];
		if(routeSegmentIndex == r.getChangeFromRouteSegmentIndex()){			
			Point x = rs.getPoint(r.getChangeFromIndexOfRouteSegment());
			Path p1 = ApiController.gismap.findPath(x.getLatLng(), p.getLatLng());
			Path p2 = ApiController.gismap.findPath(p.getLatLng(), rs.getServicePoint().getLatLng());
			return p1.getLength() + p2.getLength() - rs.computeLengthFromToEnd(r.getChangeFromIndexOfRouteSegment());
		}else{
			Path p1 = ApiController.gismap.findPath(rs.getFirstPoint().getLatLng(), p.getLatLng());
			Path p2 = ApiController.gismap.findPath(p.getLatLng(), rs.getLastPoint().getLatLng());
			return p1.getLength() + p2.getLength() - rs.getLength();
		}
		
	}
	private void greedyInsertPoint(IServicePoint p){
		double minExtraDistance = Integer.MAX_VALUE;
		int sel_i = -1;
		int sel_j = -1;
		for(int i = 0; i < routes.length; i++){
			ExecuteRoute r = routes[i];
			for(int j = r.getChangeFromServicePointIndex(); j < r.getServicePoints().length; j++){
				Path p1 = null;
				Path p2 = null;
				Path p3 = null;
				double eval = 0;
				RouteSegment rs = r.getRouteSegments()[j];
				if(j == r.getChangeFromServicePointIndex()){			
					
					p1 = ApiController.gismap.findPath(r.getChangeFromLatLng(), p.getLatLng());
					p2 = ApiController.gismap.findPath(p.getLatLng(), r.getServicePoints()[j].getLatLng());
					p3 = ApiController.gismap.findPath(r.getChangeFromLatLng(), r.getServicePoints()[j].getLatLng());
							
					eval = p1.getLength() + p2.getLength() - p3.getLength();
				}else{
					p1 = ApiController.gismap.findPath(r.getServicePoints()[j-1].getLatLng(), p.getLatLng());
					p2 = ApiController.gismap.findPath(p.getLatLng(), r.getServicePoints()[j].getLatLng());
					p3 = ApiController.gismap.findPath(r.getServicePoints()[j-1].getLatLng(), r.getServicePoints()[j].getLatLng());
					eval = p1.getLength() + p2.getLength() - p3.getLength();
				}
				if(eval < minExtraDistance){
					minExtraDistance = eval;
					sel_i = i; sel_j = j;
				}
			}
		}
		// update route
		ExecuteRoute r = routes[sel_i];
		r.insertServicePoint(sel_j, p);
	}
	private void greedyInsert(RouteVRPInputPoint p){
		double minExtraDistance = Integer.MAX_VALUE;
		int sel_i = -1;
		int sel_j = -1;
		Path sel_p1 = null;
		Path sel_p2 = null;
		for(int i = 0; i < routes.length; i++){
			ExecuteRoute r = routes[i];
			for(int j = r.getChangeFromRouteSegmentIndex(); j < r.getRouteSegments().length; j++){
				//double extra = evaluateExtraDistanceInsert(r, j, p);
				//if(extra < minExtraDistance){
				//	minExtraDistance = extra; sel_i = i; sel_j = j;
				//}
				Path p1 = null;
				Path p2 = null;
				double eval = 0;
				RouteSegment rs = r.getRouteSegments()[j];
				if(j == r.getChangeFromRouteSegmentIndex()){			
					Point x = rs.getPoint(r.getChangeFromIndexOfRouteSegment());
					p1 = ApiController.gismap.findPath(x.getLatLng(), p.getLatLng());
					p2 = ApiController.gismap.findPath(p.getLatLng(), rs.getServicePoint().getLatLng());
					eval = p1.getLength() + p2.getLength() - rs.computeLengthFromToEnd(r.getChangeFromIndexOfRouteSegment());
				}else{
					p1 = ApiController.gismap.findPath(rs.getFirstPoint().getLatLng(), p.getLatLng());
					p2 = ApiController.gismap.findPath(p.getLatLng(), rs.getLastPoint().getLatLng());
					eval = p1.getLength() + p2.getLength() - rs.getLength();
				}
				if(eval < minExtraDistance){
					minExtraDistance = eval;
					sel_p1 = p1; sel_p2 = p2; sel_i = i; sel_j = j;
				}
			}
		}
		// update route
		ExecuteRoute r = routes[sel_i];
		if(sel_j == r.getChangeFromRouteSegmentIndex()){
			RouteSegment rs = r.getRouteSegments()[sel_j];
			ArrayList<Point> L1 = new ArrayList<Point>();
			for(int k = 0; k < r.getChangeFromIndexOfRouteSegment(); k++){
				L1.add(rs.getMapPoints()[k]);
			}
			for(int k = 0; k < sel_p1.getPoints().length; k++){
				L1.add(sel_p1.getPoints()[k]);
			}
			RouteSegment new_rs1 = new RouteSegment(L1,p);
			RouteSegment new_rs2 = new RouteSegment(sel_p2.getPoints(), rs.getServicePoint(), sel_p2.getLength());
			r.replaceRouteSegment(sel_j, new_rs1, new_rs2);
		}else{
			RouteSegment rs = r.getRouteSegments()[sel_j];
			RouteSegment new_rs1 = new RouteSegment(sel_p1.getPoints(), p, sel_p1.getLength());
			RouteSegment new_rs2 = new RouteSegment(sel_p2.getPoints(), rs.getServicePoint(), sel_p2.getLength());
			r.replaceRouteSegment(sel_j, new_rs1, new_rs2);
		}
	}
	public OnlineVRPSolution computeOnlineInsertion(OnlineVRPInput input){
		this.input = input;
		routes = input.getRoutes();
		newRequests = input.getNewRequests();
		for(RouteVRPInputPoint p: newRequests){
			ServicePointDelivery pp = new ServicePointDelivery(p.getId(), p.getLat(), p.getLng());
			greedyInsertPoint(pp);
		}
		return null;
	}
}
