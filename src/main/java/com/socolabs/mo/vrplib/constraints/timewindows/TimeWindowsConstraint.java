package com.socolabs.mo.vrplib.constraints.timewindows;

import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.entities.INodeWeightManager;
import com.socolabs.mo.vrplib.entities.accumulatedcalculators.ArrivalTimeCalculator;
import com.socolabs.mo.vrplib.entities.accumulatedcalculators.TWViolationCalculator;
import com.socolabs.mo.vrplib.entities.distancemanagers.ServiceTravelTimeManager;
import com.socolabs.mo.vrplib.entities.distancemanagers.TravelTimeManager;
import com.socolabs.mo.vrplib.entities.nodeweightmanagers.ServiceTimeManager;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;

import java.util.HashMap;
import java.util.HashSet;

public class TimeWindowsConstraint implements IVRPFunction {

    private VRPVarRoutes vr;
    private AccumulatedWeightPoints accViolationPoints;

    private double value;
    private double tmpValue;

    public TimeWindowsConstraint(VRPVarRoutes vr,
                                 HashMap<String, HashMap<String, Integer>> travelTimeMap,
                                 HashMap<VRPPoint, Integer> earliestArrivalTimeMap,
                                 HashMap<VRPPoint, Integer> lastestArrivalTimeMap) {
        this.vr = vr;
        // một calculator tính thời điểm đến 1 point
        ArrivalTimeCalculator arrivalTimeCalculator = new ArrivalTimeCalculator(new TravelTimeManager(vr, travelTimeMap), earliestArrivalTimeMap);
        // một AccumulatedWeightPoints để lưu trữ thời điểm đến của từng point
        AccumulatedWeightPoints accArrivalTimePoints = new AccumulatedWeightPoints(vr, arrivalTimeCalculator);
        // TWViolationCalculator sẽ tính độ vi phạm của từng point
        // accViolationPoints là accumulated violation của các points trên path
        accViolationPoints = new AccumulatedWeightPoints(vr, new TWViolationCalculator(accArrivalTimePoints, lastestArrivalTimeMap));
        init();
    }

    public TimeWindowsConstraint(VRPVarRoutes vr,
                                 HashMap<String, HashMap<String, Integer>> travelTimeMap,
                                 HashMap<VRPPoint, Integer> serviceTimeMap,
                                 HashMap<VRPPoint, Integer> earliestArrivalTimeMap,
                                 HashMap<VRPPoint, Integer> lastestArrivalTimeMap) {
        this.vr = vr;
        TravelTimeManager travelTimeManager = new TravelTimeManager(vr, travelTimeMap);
        ServiceTimeManager serviceTimeManager = new ServiceTimeManager(vr, serviceTimeMap);
        ServiceTravelTimeManager serviceTravelTimeManager = new ServiceTravelTimeManager(travelTimeManager, serviceTimeManager);
        // một calculator tính thời điểm đến 1 point
        ArrivalTimeCalculator arrivalTimeCalculator = new ArrivalTimeCalculator(serviceTravelTimeManager, earliestArrivalTimeMap);
        // một AccumulatedWeightPoints để lưu trữ thời điểm đến của từng point
        AccumulatedWeightPoints accArrivalTimePoints = new AccumulatedWeightPoints(vr, arrivalTimeCalculator);
        // TWViolationCalculator sẽ tính độ vi phạm của từng point
        // accViolationPoints là accumulated violation của các points trên path
        accViolationPoints = new AccumulatedWeightPoints(vr, new TWViolationCalculator(accArrivalTimePoints, lastestArrivalTimeMap));
        init();
    }

    public TimeWindowsConstraint(VRPVarRoutes vr,
                                 IDistanceManager travelTimeManager,
                                 INodeWeightManager serviceTimeManager,
                                 HashMap<VRPPoint, Integer> earliestArrivalTimeMap,
                                 HashMap<VRPPoint, Integer> lastestArrivalTimeMap) {
        this.vr = vr;
        ServiceTravelTimeManager serviceTravelTimeManager = new ServiceTravelTimeManager(travelTimeManager, serviceTimeManager);
        // một calculator tính thời điểm đến 1 point
        ArrivalTimeCalculator arrivalTimeCalculator = new ArrivalTimeCalculator(serviceTravelTimeManager, earliestArrivalTimeMap);
        // một AccumulatedWeightPoints để lưu trữ thời điểm đến của từng point
        AccumulatedWeightPoints accArrivalTimePoints = new AccumulatedWeightPoints(vr, arrivalTimeCalculator);
        // TWViolationCalculator sẽ tính độ vi phạm của từng point
        // accViolationPoints là accumulated violation của các points trên path
        accViolationPoints = new AccumulatedWeightPoints(vr, new TWViolationCalculator(accArrivalTimePoints, lastestArrivalTimeMap));
        init();
    }

    private void init() {
        vr.post(this);
        value = 0;
        for (VRPRoute route : vr.getAllRoutes()) {
            VRPPoint endPoint = route.getEndPoint();
            value += accViolationPoints.getWeightValueOfPoint(endPoint);
        }
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public double getTmpValue() {
        return tmpValue;
    }

    @Override
    public void explore() {
        tmpValue = value;
        for (VRPRoute route : vr.getChangedRoutes()) {
            VRPPoint endPoint = route.getEndPoint();
            tmpValue -= accViolationPoints.getWeightValueOfPoint(endPoint);
            tmpValue += accViolationPoints.getTmpWeightValueOfPoint(endPoint);
        }
    }

    @Override
    public void propagate() {
        value = tmpValue;
    }

    @Override
    public void addNewPoint(VRPPoint point) {

    }

    @Override
    public void removePoint(VRPPoint point) {

    }

    @Override
    public void addNewRoute(VRPRoute route) {

    }

    @Override
    public void removeRoute(VRPRoute route) {

    }

    @Override
    public VRPVarRoutes getVarRoutes() {
        return vr;
    }

    @Override
    public HashSet<VRPPoint> getIndependentPoints() {
        return null;
    }

    private int stt;

    @Override
    public int getStt() {
        return stt;
    }

    @Override
    public void setStt(int stt) {
        this.stt = stt;
    }

    @Override
    public boolean verify() {
        return value == tmpValue;
    }

    @Override
    public String name() {
        return "TimeWindowsConstraint";
    }
}
