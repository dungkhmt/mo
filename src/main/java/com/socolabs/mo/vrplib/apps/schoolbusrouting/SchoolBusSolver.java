package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.constraints.capacity.CapacityConstraint;
import com.socolabs.mo.vrplib.constraints.leq.Leq;
import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.entities.accumulatedcalculators.AccumulatedNodeCalculator;
import com.socolabs.mo.vrplib.entities.distancemanagers.TravelTimeManager;
import com.socolabs.mo.vrplib.functions.AccumulatedPointWeightsOnPath;
import com.socolabs.mo.vrplib.functions.sum.SumAccumulatedWeightPoints;
import com.socolabs.mo.vrplib.functions.sum.SumRouteFunctions;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.*;

import java.util.*;

public class SchoolBusSolver {

    private SchoolBusRoutingInput input;

    private HashMap<String, HashMap<String, Integer>> travelTimeMap;
    private HashMap<String, HashMap<String, Integer>> roadBlockMap;
    private HashMap<VRPRoute, Double> mRoute2Capacity;

    private HashMap<Integer, ArrayList<SchoolBusRequest>> mLocationId2Requests;

    private VRPVarRoutes vr;

    public SchoolBusRoutingSolution solve(SchoolBusRoutingInput input) {
        this.input = input;
        stateModel();

        return null;
    }

    private void initSolution() {

    }

    private void stateModel() {
        vr = new VRPVarRoutes();
        initTravelTime();
        createRoutes();
        createPoints();
        addMaximumNumberOfStudents();
        addCapacityConstraint();
        addGroupConstraint();
        addRoadBlockConstraint();
        addMinimumUsedBuses();
        addTravelTimeConstraint();
    }

    private void addGroupConstraint() {
        SumAccumulatedWeightPoints totalGroupViolations = new SumAccumulatedWeightPoints(
                new AccumulatedWeightPoints(
                        new AccumulatedNodeCalculator(
                                new NWMGroupViolationAtPoint(vr))));

    }

    private void addMinimumUsedBuses() {
        SBTotalUsedBuses totalUsedBuses = new SBTotalUsedBuses(vr);
    }

    private void addMaximumNumberOfStudents() {
        SBNumberValidStudentCalculator nbValidStudentCalculator = new SBNumberValidStudentCalculator(vr);
        SumAccumulatedWeightPoints totalValidStudents = new SumAccumulatedWeightPoints(
                new AccumulatedWeightPoints(nbValidStudentCalculator));
    }

    private void addRoadBlockConstraint() {
        TravelTimeManager roadBlockManager = new TravelTimeManager(vr, roadBlockMap);
        SBRoadBlockViolationCalculator roadBlockViolationCalculator = new SBRoadBlockViolationCalculator(roadBlockManager);
        AccumulatedWeightPoints accRoadBlockViolations = new AccumulatedWeightPoints(roadBlockViolationCalculator);
        SumAccumulatedWeightPoints roadBlockConstraint = new SumAccumulatedWeightPoints(accRoadBlockViolations);
    }

    private void addTravelTimeConstraint() {
        IDistanceManager travelTimeManager = new TravelTimeManager(vr, travelTimeMap);
        NWMPickupServiceTimeAtPoint serviceTimeManager = new NWMPickupServiceTimeAtPoint(vr);
        SBArrivalTimeCalculator arrivalTimeCalculator = new SBArrivalTimeCalculator(travelTimeManager,
                                                                                    serviceTimeManager,
                                                                                    input.getConfigParams().getEarliestDateTimePickupAtPoint());
        AccumulatedWeightPoints accArrivalTime = new AccumulatedWeightPoints(arrivalTimeCalculator);
        HashMap<VRPRoute, IVRPFunction> mRoute2TimeViolation = new HashMap<>();
        int latestDeliveryTime = input.getConfigParams().getLatestDateTimeDeliveryAtSchool();
        for (VRPRoute r : mRoute2Capacity.keySet()) {
            AccumulatedPointWeightsOnPath arrivalTimeAtSchool = new AccumulatedPointWeightsOnPath(accArrivalTime, r.getEndPoint());
            mRoute2TimeViolation.put(r, new Leq(arrivalTimeAtSchool, latestDeliveryTime));
        }
        // thời gian đến trường ko được sau thời điểm latestDateTimeDeliveryAtSchool
        SumRouteFunctions arrivalTimeAtSchoolConstraint = new SumRouteFunctions(vr, mRoute2TimeViolation);

        SBRevTimeCalculator revTimeCalculator = new SBRevTimeCalculator(travelTimeManager, serviceTimeManager);
        // tổng thời gian đi từ 1 điểm u đến trường
        RevAccumulatedWeightPoints revAccTravelTime = new RevAccumulatedWeightPoints(revTimeCalculator);
        // vi phạm về thời gian di chuyển từ 1 điểm u đến trường
        NWMTimeViolationAtPoint travelTimeViolationAtPoint = new NWMTimeViolationAtPoint(vr, revAccTravelTime);
        // tổng vi phạm của các điểm từ điểm bắt đầu đến điểm u
        AccumulatedWeightPoints travelTimeViolationAcc = new AccumulatedWeightPoints(new AccumulatedNodeCalculator(travelTimeViolationAtPoint));
        // constraint giới hạn thời gian di chuyển từ 1 điểm không được vượt quá 180% so với đường đi ngắn nhất đến trường
        SumAccumulatedWeightPoints totalTravelTimeViolations = new SumAccumulatedWeightPoints(travelTimeViolationAcc);

        // tính thời gian ứng với từng point trong hàm mục tiêu
        // hàm mục tiêu là tổng thời gian di chuyển của các route + tổng độ chênh lệch giữa thời gian đi trực tiếp với thời gian đi thực tế của từng point đến trường
        NWMTravelTimeObjective nwmTravelTimeObjective = new NWMTravelTimeObjective(vr, revAccTravelTime);
        AccumulatedWeightPoints travelTimeObjectiveAcc = new AccumulatedWeightPoints(new AccumulatedNodeCalculator(nwmTravelTimeObjective));
        SumAccumulatedWeightPoints travelTimeObjective = new SumAccumulatedWeightPoints(travelTimeObjectiveAcc);
    }

    private void addCapacityConstraint() {
        NWMStudentNumberAtPoint studentNumber = new NWMStudentNumberAtPoint(vr);
        CapacityConstraint capacityConstraint = new CapacityConstraint(vr, studentNumber, mRoute2Capacity);
    }

    private void createRoutes() {
        mRoute2Capacity = new HashMap<>();
        String schoolLocationId = "" + input.getShoolPointId();
        for (Vehicle v : input.getVehicles()) {
            SchoolBusPickupPoint startPoint = new SchoolBusPickupPoint(schoolLocationId, vr, input, travelTimeMap);
            SchoolBusPickupPoint endPoint = new SchoolBusPickupPoint(schoolLocationId, vr, input, travelTimeMap);
            SchoolBusRoute route = new SchoolBusRoute(startPoint, endPoint, "" + v.getId(), v.getCap(), vr);
            mRoute2Capacity.put(route, 1.0 * v.getCap());
        }
    }

    private void createPoints() {
        HashMap<Integer, HashSet<ArrayList<SchoolBusRequest>>> mLocationId2Requests = new HashMap<>();
        HashMap<String, ArrayList<SchoolBusRequest>> mSiblingCode2Requests = new HashMap<>();
        for (SchoolBusRequest r : input.getRequests()) {
            String siblingCode = r.getSiblingCode();
            if (siblingCode != null && siblingCode.length() > 0) {
                if (!mSiblingCode2Requests.containsKey(siblingCode)) {
                    mSiblingCode2Requests.put(siblingCode, new ArrayList<>());
                }
                mSiblingCode2Requests.get(siblingCode).add(r);
            }
        }
        for (SchoolBusRequest r : input.getRequests()) {
            int locationID = r.getPickupLocationId();
            if (!mLocationId2Requests.containsKey(locationID)) {
                mLocationId2Requests.put(locationID, new HashSet<>());
            }
            String siblingCode = r.getSiblingCode();
            if (siblingCode != null && siblingCode.length() > 0) {
                mLocationId2Requests.get(locationID).add(mSiblingCode2Requests.get(siblingCode));
            } else {
                ArrayList<SchoolBusRequest> newRequestLst = new ArrayList<>();
                newRequestLst.add(r);
                mLocationId2Requests.get(locationID).add(newRequestLst);
            }
        }
        // cho tất cả các requests cùng locationId vào thành 1 point
        // nếu số lượng requests cần nhiều hơn 1 xe thì tách ra thành nhiều points
        for (int locationID : mLocationId2Requests.keySet()) {
            HashSet<ArrayList<SchoolBusRequest>> locationRequests = mLocationId2Requests.get(locationID);
            int roadBlock = 0;
            try {
                roadBlock = roadBlockMap.get("" + locationID).get("" + input.getShoolPointId());
            } catch (NullPointerException e) {
                roadBlock = 0;
            }
            int roadBlockCap = SBUtils.getRoadBloakCap(roadBlock);
            SchoolBusRoute route = findRoute(roadBlockCap);
            double routeCap = mRoute2Capacity.get(route);

            int demand = 0;
            for (ArrayList<SchoolBusRequest> subRequests : locationRequests) {
                demand += subRequests.size();
            }
            if (demand <= routeCap) {
                SchoolBusPickupPoint point = new SchoolBusPickupPoint("" + locationID, vr, input, travelTimeMap);
                for (ArrayList<SchoolBusRequest> subRequests : locationRequests) {
                    for (SchoolBusRequest r : subRequests) {
                        point.addRequest(r);
                    }
                }
            } else {
                TreeSet<ArrayList<SchoolBusRequest>> orderedSubRequests = new TreeSet<ArrayList<SchoolBusRequest>>(new Comparator<ArrayList<SchoolBusRequest>>() {
                    @Override
                    public int compare(ArrayList<SchoolBusRequest> o1, ArrayList<SchoolBusRequest> o2) {
                        return o2.size() - o1.size();
                    }
                });
                orderedSubRequests.addAll(locationRequests);
                ArrayList<SchoolBusRequest> dividedRequests = new ArrayList<>();
                int cnt = 0;
                while (!orderedSubRequests.isEmpty()) {
                    ArrayList<SchoolBusRequest> subRequests = orderedSubRequests.first();
                    if (cnt + subRequests.size() <= routeCap) {
                        dividedRequests.addAll(subRequests);
                        cnt += subRequests.size();
                        orderedSubRequests.remove(subRequests);
                    } else {
                        subRequests = orderedSubRequests.last();
                        if (cnt + subRequests.size() <= routeCap) {
                            dividedRequests.addAll(subRequests);
                            cnt += subRequests.size();
                            orderedSubRequests.remove(subRequests);
                        } else {
                            SchoolBusPickupPoint point = new SchoolBusPickupPoint("" + locationID, vr, input, travelTimeMap);
                            for (SchoolBusRequest r : dividedRequests) {
                                point.addRequest(r);
                            }
                            dividedRequests.clear();
                            cnt = 0;
                        }
                    }
                }
            }
        }
    }

    private SchoolBusRoute findRoute(int roadBlockCap) {
        double bestCap = 0;
        SchoolBusRoute bestRoute = null;
        for (Map.Entry<VRPRoute, Double> e : mRoute2Capacity.entrySet()) {
            if (e.getValue() <= roadBlockCap) {
                if (e.getValue() > bestCap) {
                    bestRoute = (SchoolBusRoute) e.getKey();
                }
            }
        }
        return bestRoute;
    }

    // khoi tao hashmap travel time giua cac locations
    private void initTravelTime() {
        HashSet<Integer> locationIdSet = new HashSet<>();
        for (SchoolBusRequest r : input.getRequests()) {
            int locationId = r.getPickupLocationId();
            locationIdSet.add(locationId);
        }
        locationIdSet.add(input.getShoolPointId());
        travelTimeMap = new HashMap<>();
        roadBlockMap = new HashMap<>();
        double timeScale = 1.0 + 0.01 * input.getConfigParams().getTimeScale();
        for (DistanceElement e : input.getDistances()) {
            String src = "" + e.getSrcCode();
            String dest = "" + e.getDestCode();
            if (!travelTimeMap.containsKey(src)) {
                travelTimeMap.put(src, new HashMap<>());
                roadBlockMap.put(src, new HashMap<>());
            }
            travelTimeMap.get(src).put(dest, (int) Math.ceil(timeScale * e.getTravelTime()));
            roadBlockMap.get(src).put(dest, e.getRoadBlock());
        }
    }
}
