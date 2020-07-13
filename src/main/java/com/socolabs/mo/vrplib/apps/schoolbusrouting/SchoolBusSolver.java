package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.google.gson.Gson;
import com.socolabs.mo.vrplib.apps.schoolbusrouting.accumulatedcalculators.*;
import com.socolabs.mo.vrplib.apps.schoolbusrouting.functions.SBBoardingScaleTime;
import com.socolabs.mo.vrplib.apps.schoolbusrouting.functions.SBTotalUsedBuses;
import com.socolabs.mo.vrplib.apps.schoolbusrouting.nodewieghtmanagers.*;
import com.socolabs.mo.vrplib.constraints.capacity.CapacityConstraint;
import com.socolabs.mo.vrplib.constraints.leq.Leq;
import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.accumulatedcalculators.AccumulatedEdgeCalculator;
import com.socolabs.mo.vrplib.entities.accumulatedcalculators.AccumulatedNodeCalculator;
import com.socolabs.mo.vrplib.entities.distancemanagers.DistanceManager;
import com.socolabs.mo.vrplib.entities.distancemanagers.TravelTimeManager;
import com.socolabs.mo.vrplib.functions.AccumulatedPointWeightsOnPath;
import com.socolabs.mo.vrplib.functions.max.MaxAccumulatedWeightPoints;
import com.socolabs.mo.vrplib.functions.sum.SumAccumulatedWeightPoints;
import com.socolabs.mo.vrplib.functions.sum.SumRouteFunctions;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;
import com.socolabs.mo.vrplib.search.GreedySearch;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.*;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.service.BusRouteSolver;
import localsearch.domainspecific.vehiclerouting.vrp.utils.DateTimeUtils;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.Direction;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.LatLng;

import java.io.*;
import java.util.*;

public class SchoolBusSolver {

    private SchoolBusRoutingInput input;
    private double boardingTimeScale;

    private HashMap<String, HashMap<String, Double>> distanceMap;
    private HashMap<String, HashMap<String, Integer>> travelTimeMap;
    private HashMap<String, HashMap<String, Integer>> roadBlockMap;
    private HashMap<VRPRoute, Double> mRoute2Capacity;

    private VRPVarRoutes vr;
    private LexMultiFunctions objectiveFuncs;
    private RevAccumulatedWeightPoints revAccTravelTime;
    private RevAccumulatedWeightPoints revAccDistance;
    private AccumulatedWeightPoints accArrivalTime;
    private IDistanceManager travelTimeManager;
    private DistanceManager distanceManager;

    public SchoolBusRoutingSolution solve(SchoolBusRoutingInput input) {
        System.out.println("solve");
        this.input = input;
        stateModel();
        System.out.println("done State Model");
        GreedySearch search = new GreedySearch(vr, objectiveFuncs);
        search.addExplorer(GreedySearch.INSERT_ONE_MOVE);
        search.search(100000, 10 * 60000, false);
        search.addExplorer(GreedySearch.ONE_POINT_MOVE);
        search.addExplorer(GreedySearch.OR_OPT_MOVE);
        search.addExplorer(GreedySearch.TWO_POINTS_MOVE);
        search.addExplorer(GreedySearch.TWO_OPT_MOVE);
        search.addExplorer(GreedySearch.THREE_OPT_MOVE);
        search.addExplorer(GreedySearch.CROSS_EXCHANGE_MOVE);
        search.search(100000, 10 * 60000, true);
        SBUtils.exportSBExcel(vr, accArrivalTime, travelTimeManager, revAccTravelTime, mRoute2Capacity,
                "D:\\Workspace\\VinSchool\\nodejs\\nodejs\\data\\",
                "Imperia");
        return null;//SBUtils.exportSolution(input, vr);
    }

    public SchoolBusRoutingSolution solveStep2(SchoolBusRoutingInput input, double boardingTimeScale) {
        System.out.println("solveStep2");
        this.input = input;
        this.boardingTimeScale = boardingTimeScale;

        stateModel2();
        System.out.println("done State Model 2");
        GreedySearch search = new GreedySearch(vr, objectiveFuncs);
        search.addExplorer(GreedySearch.INSERT_ONE_MOVE);
        search.search(100000, 10 * 60000, false);
        search.addExplorer(GreedySearch.ONE_POINT_MOVE);
        search.addExplorer(GreedySearch.OR_OPT_MOVE);
        search.addExplorer(GreedySearch.TWO_POINTS_MOVE);
        search.addExplorer(GreedySearch.TWO_OPT_MOVE);
        search.addExplorer(GreedySearch.THREE_OPT_MOVE);
        search.addExplorer(GreedySearch.CROSS_EXCHANGE_MOVE);
        search.search(100000, 15 * 60000, true);
        SBUtils.exportSBExcel(vr, accArrivalTime, travelTimeManager, revAccTravelTime, mRoute2Capacity,
                "D:\\Workspace\\VinSchool\\nodejs\\nodejs\\data\\",
                "Imperia");
        return null;//SBUtils.exportSolution(input, vr);
    }

    private void stateModel2() {
        vr = new VRPVarRoutes();
        objectiveFuncs = new LexMultiFunctions();
        System.out.println("initTravelTime");
        initTravelTime();
        System.out.println("createRoutes");
        createRoutes();
        System.out.println("createPoints");
        createPoints();
        System.out.println("addMaximumNumberOfStudents");
        addMaximumNumberOfStudents();
        System.out.println("addCapacityConstraint");
        addCapacityConstraint();
        System.out.println("addGroupConstraint");
        addGroupConstraint();
        System.out.println("addRoadBlockConstraint");
        addRoadBlockConstraint();
        System.out.println("addDistanceConstraint");
        addDistanceConstraint();
        System.out.println("addTravelTimeConstraint");
        addTravelTimeConstraint();
        System.out.println("addTravelTimeLimitConstraint");
        addTravelTimeLimitConstraint();
        System.out.println("addMinimumUsedBusesObjective");
        addMinimumUsedBusesObjective();
//        System.out.println("addBoardingTimeScaleObjective");
//        addBoardingTimeScaleObjective();
        System.out.println("addTravelTimeObjective");
        addTravelTimeObjective();
    }

    private void stateModel() {
        vr = new VRPVarRoutes();
        objectiveFuncs = new LexMultiFunctions();
        System.out.println("initTravelTime");
        initTravelTime();
        System.out.println("createRoutes");
        createRoutes();
        System.out.println("createPoints");
        createPoints();
        System.out.println("addMaximumNumberOfStudents");
        addMaximumNumberOfStudents();
        System.out.println("addCapacityConstraint");
        addCapacityConstraint();
        System.out.println("addGroupConstraint");
        addGroupConstraint();
        System.out.println("addRoadBlockConstraint");
        addRoadBlockConstraint();
        System.out.println("addDistanceConstraint");
        addDistanceConstraint();
        System.out.println("addTravelTimeConstraint");
        addTravelTimeConstraint();
        System.out.println("addMinimumUsedBusesObjective");
        addMinimumUsedBusesObjective();
        System.out.println("addBoardingTimeScaleObjective");
        addBoardingTimeScaleObjective();
        System.out.println("addTravelTimeObjective");
        addTravelTimeObjective();
    }

    private void addDistanceConstraint() {
        distanceManager = new DistanceManager(vr, distanceMap);
        revAccDistance = new RevAccumulatedWeightPoints(new SBRevDistanceCalculator(distanceManager));
//        SumAccumulatedWeightPoints totalDistanceViolations = new SumAccumulatedWeightPoints(
//                new AccumulatedWeightPoints(
//                        new AccumulatedNodeCalculator(
//                                new NWMDistanceViolationAtPoint(vr, revAccDistance,
//                                        0.01 * input.getConfigParams().getBoardingTimeScale2())
//                        )
//                )
//        );
//        objectiveFuncs.add(totalDistanceViolations, LexMultiFunctions.MINIMIZE, "distanceViolations");

    }

    private void addGroupConstraint() {
        SumAccumulatedWeightPoints totalGroupViolations = new SumAccumulatedWeightPoints(
                new AccumulatedWeightPoints(
                        new AccumulatedNodeCalculator(
                                new NWMGroupViolationAtPoint(vr))));
        objectiveFuncs.add(totalGroupViolations, LexMultiFunctions.MINIMIZE, "groupViolations");
    }

    private void addMinimumUsedBusesObjective() {
        SBTotalUsedBuses totalUsedBuses = new SBTotalUsedBuses(vr);
        objectiveFuncs.add(totalUsedBuses, LexMultiFunctions.MINIMIZE, "totalUsedBuses");
    }

    private void addMaximumNumberOfStudents() {
        SBNumberValidStudentCalculator nbValidStudentCalculator = new SBNumberValidStudentCalculator(vr);
        SumAccumulatedWeightPoints totalValidStudents = new SumAccumulatedWeightPoints(
                new AccumulatedWeightPoints(nbValidStudentCalculator));
        objectiveFuncs.add(totalValidStudents, LexMultiFunctions.MAXIMIZE, "totalValidStudents");
    }

    private void addRoadBlockConstraint() {
        TravelTimeManager roadBlockManager = new TravelTimeManager(vr, roadBlockMap);
        SBRoadBlockViolationCalculator roadBlockViolationCalculator = new SBRoadBlockViolationCalculator(roadBlockManager);
        AccumulatedWeightPoints accRoadBlockViolations = new AccumulatedWeightPoints(roadBlockViolationCalculator);
        SumAccumulatedWeightPoints roadBlockConstraint = new SumAccumulatedWeightPoints(accRoadBlockViolations);
        objectiveFuncs.add(roadBlockConstraint, LexMultiFunctions.MINIMIZE, "roadBlockC");
    }

    private void addTravelTimeConstraint() {
        travelTimeManager = new TravelTimeManager(vr, travelTimeMap);
        NWMPickupServiceTimeAtPoint serviceTimeManager = new NWMPickupServiceTimeAtPoint(vr);
        SBArrivalTimeCalculator arrivalTimeCalculator = new SBArrivalTimeCalculator(travelTimeManager,
                                                                                    serviceTimeManager,
                                                                                    input.getConfigParams().getEarliestDateTimePickupAtPoint());
        accArrivalTime = new AccumulatedWeightPoints(arrivalTimeCalculator);
        HashMap<VRPRoute, IVRPFunction> mRoute2TimeViolation = new HashMap<>();
        int latestDeliveryTime = input.getConfigParams().getLatestDateTimeDeliveryAtSchool();
        for (VRPRoute r : mRoute2Capacity.keySet()) {
            AccumulatedPointWeightsOnPath arrivalTimeAtSchool = new AccumulatedPointWeightsOnPath(accArrivalTime, r.getEndPoint());
            mRoute2TimeViolation.put(r, new Leq(arrivalTimeAtSchool, latestDeliveryTime));
        }
        // thời gian đến trường ko được sau thời điểm latestDateTimeDeliveryAtSchool
        SumRouteFunctions arrivalTimeAtSchoolConstraint = new SumRouteFunctions(vr, mRoute2TimeViolation);
        objectiveFuncs.add(arrivalTimeAtSchoolConstraint, LexMultiFunctions.MINIMIZE, "arrivalTimeC");

        SBRevTimeCalculator revTimeCalculator = new SBRevTimeCalculator(travelTimeManager, serviceTimeManager);
        // tổng thời gian đi từ 1 điểm u đến trường
        revAccTravelTime = new RevAccumulatedWeightPoints(revTimeCalculator);


    }

    private void addTravelTimeLimitConstraint() {
        // vi phạm về thời gian di chuyển từ 1 điểm u đến trường
        NWMTimeViolationAtPoint travelTimeViolationAtPoint = new NWMTimeViolationAtPoint(vr, revAccTravelTime, boardingTimeScale);
//         tổng vi phạm của các điểm từ điểm bắt đầu đến điểm u
        AccumulatedWeightPoints travelTimeViolationAcc = new AccumulatedWeightPoints(new AccumulatedNodeCalculator(travelTimeViolationAtPoint));
//         constraint giới hạn thời gian di chuyển từ 1 điểm không được vượt quá 180% so với đường đi ngắn nhất đến trường
        SumAccumulatedWeightPoints totalTravelTimeViolations = new SumAccumulatedWeightPoints(travelTimeViolationAcc);
        objectiveFuncs.add(totalTravelTimeViolations, LexMultiFunctions.MINIMIZE, "travelTimeLimitC");

    }

    private void addBoardingTimeScaleObjective() {
        AccumulatedWeightPoints accBoardingDistanceRatio = new AccumulatedWeightPoints(new SBBoardingTimeScaleCalculator(revAccTravelTime));
        MaxAccumulatedWeightPoints boardingTimeScaleObjective = new MaxAccumulatedWeightPoints(accBoardingDistanceRatio);
        objectiveFuncs.add(boardingTimeScaleObjective, LexMultiFunctions.MINIMIZE, "boardingTimeScaleObjective");
    }

    private void addTravelTimeObjective() {
        // tính thời gian ứng với từng point trong hàm mục tiêu
        // hàm mục tiêu là tổng thời gian di chuyển của các route + tổng độ chênh lệch giữa thời gian đi trực tiếp với thời gian đi thực tế của từng point đến trường

        NWMTravelTimeObjective nwmTravelTimeObjective = new NWMTravelTimeObjective(vr, revAccTravelTime,  travelTimeManager, false);
        AccumulatedWeightPoints travelTimeObjectiveAcc = new AccumulatedWeightPoints(new AccumulatedNodeCalculator(nwmTravelTimeObjective));
        SumAccumulatedWeightPoints travelTimeObjective = new SumAccumulatedWeightPoints(travelTimeObjectiveAcc);
        objectiveFuncs.add(travelTimeObjective, LexMultiFunctions.MINIMIZE, "maxTravelTimeObj");
//        NWMTravelTimeObjective nwmTravelTimeObjective1 = new NWMTravelTimeObjective(vr, revAccTravelTime, true);
//        AccumulatedWeightPoints travelTimeObjectiveAcc1 = new AccumulatedWeightPoints(new AccumulatedNodeCalculator(nwmTravelTimeObjective1));
//        SumAccumulatedWeightPoints travelTimeObjective1 = new SumAccumulatedWeightPoints(travelTimeObjectiveAcc1);
//        objectiveFuncs.add(travelTimeObjective1, LexMultiFunctions.MINIMIZE, "totalTravelTimeObj");

    }

    private void addCapacityConstraint() {
        NWMStudentNumberAtPoint studentNumber = new NWMStudentNumberAtPoint(vr);
        CapacityConstraint capacityConstraint = new CapacityConstraint(vr, studentNumber, mRoute2Capacity);
        objectiveFuncs.add(capacityConstraint, LexMultiFunctions.MINIMIZE, "capacity");
    }

    private void createRoutes() {
        mRoute2Capacity = new HashMap<>();
        String schoolLocationId = "" + input.getShoolPointId();
        for (Vehicle v : input.getVehicles()) {
            SchoolBusPickupPoint startPoint = new SchoolBusPickupPoint(schoolLocationId, vr, input, travelTimeMap, distanceMap);
            SchoolBusPickupPoint endPoint = new SchoolBusPickupPoint(schoolLocationId, vr, input, travelTimeMap, distanceMap);
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
//            SchoolBusRoute route = findRoute(roadBlockCap + 1);
            double routeCap = roadBlockCap;//mRoute2Capacity.get(route);

            int demand = 0;
            for (ArrayList<SchoolBusRequest> subRequests : locationRequests) {
                demand += subRequests.size();
            }
            if (demand <= routeCap) {
                SchoolBusPickupPoint point = new SchoolBusPickupPoint("" + locationID, vr, input, travelTimeMap, distanceMap);
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
                        System.out.println(orderedSubRequests.size());
                    } else {
                        subRequests = orderedSubRequests.last();
                        if (cnt + subRequests.size() <= routeCap) {
                            dividedRequests.addAll(subRequests);
                            cnt += subRequests.size();
                            orderedSubRequests.remove(subRequests);
                        } else {
                            SchoolBusPickupPoint point = new SchoolBusPickupPoint("" + locationID, vr, input, travelTimeMap, distanceMap);
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
        distanceMap = new HashMap<>();
        double timeScale = 1.0 + 0.01 * input.getConfigParams().getTimeScale();
        for (DistanceElement e : input.getDistances()) {
            String src = "" + e.getSrcCode();
            String dest = "" + e.getDestCode();
            if (!travelTimeMap.containsKey(src)) {
                travelTimeMap.put(src, new HashMap<>());
                roadBlockMap.put(src, new HashMap<>());
                distanceMap.put(src, new HashMap<>());
            }
            travelTimeMap.get(src).put(dest, (int) Math.ceil(timeScale * e.getTravelTime()));
            roadBlockMap.get(src).put(dest, e.getRoadBlock());
            distanceMap.get(src).put(dest, e.getDistance());
        }
    }

    public static void main(String[] args) throws FileNotFoundException {

        GoogleMapsQuery G = new GoogleMapsQuery();
        //G.getTravelTime(21, 105, 21.01, 105, "driving");
//        LatLng l1 = new LatLng(20.9553953, 105.7693546);
//        LatLng l2 = new LatLng(20.9698378, 105.7741762);
//
//        long departure_time = (long) DateTimeUtils.dateTime2Int("2020-07-11 07:00:00");
//        Direction direction = G.getDirection(l1.lat, l1.lng, l2.lat, l2.lng, "driving", departure_time);
//        System.out.println(direction.getDurations() + " " + direction.getDistances());
//
//        System.exit(0);
        Gson g = new Gson();
        String inputFile = "D:\\Workspace\\VinSchool\\Gb\\12-1025rx2u0qkcj1rmpdbzx1ol4xze_time9_22_432nextyear-input_new.json";
        BufferedReader in = new BufferedReader(new FileReader(inputFile));
        SchoolBusRoutingInput input = g.fromJson(in, SchoolBusRoutingInput.class);
        ArrayList<SchoolBusRequest> avaiRequests = new ArrayList<>();
        HashSet<Integer> idLocations = new HashSet<>();
        for (DistanceElement de : input.getDistances()) {
            if (de.getDestCode() == input.getShoolPointId()) {
                idLocations.add(de.getSrcCode());
            }
        }
        HashSet<Integer> requestIdSet = new HashSet<>();
        for (SchoolBusRequest r : input.getRequests()) {
            if (idLocations.contains(r.getPickupLocationId())) {// && idLocations.contains(r.getDeliveryLocationId())) {
                avaiRequests.add(r);
                requestIdSet.add(r.getId());
            } else {

            }
        }
        System.out.println(avaiRequests.size());
//        System.exit(0);
//        input.getConfigParams().setBoardingTimeScale1(250);
//        input.getConfigParams().setBoardingTimeScale2(400);
//
        SchoolBusRequest[] newRequests = new SchoolBusRequest[avaiRequests.size()];
        for (int i = 0; i < newRequests.length; i++) {
            newRequests[i] = avaiRequests.get(i);
        }
        input.setRequests(newRequests);
        System.out.println(newRequests.length);

//        SBUtils.recreateTravelTimeMatrix(input, inputFile.split(".json")[0] + "_new.json");
//        input.setRequestSolutionType("new");
//        String inputJson = g.toJson(input);
//        try {
//            BufferedWriter fo = new BufferedWriter(new FileWriter("D:\\Workspace\\VinSchool\\Times\\Times\\4212315_times_city_input.json"));
//            fo.write(inputJson);
//            fo.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.exit(0);
        SchoolBusSolver solver = new SchoolBusSolver();
        solver.solve(input);
//        System.out.println(boardingTimeScale);
//        solver = new SchoolBusSolver();
//        SchoolBusRoutingSolution sol = solver.solveStep2(input, boardingTimeScale);
//        input.setCurrentSolution(null);
//        input.setMoveActions(null);
//        BusRouteSolver brSolver = new BusRouteSolver();
//        SchoolBusRoutingSolution solution = brSolver.randomSolve(input);

    }
}
