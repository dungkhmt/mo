package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.google.gson.Gson;
import com.socolabs.mo.vrplib.apps.schoolbusrouting.accumulatedcalculators.*;
import com.socolabs.mo.vrplib.apps.schoolbusrouting.functions.SBBoardingScaleTime;
import com.socolabs.mo.vrplib.apps.schoolbusrouting.functions.SBMarginObjective;
import com.socolabs.mo.vrplib.apps.schoolbusrouting.functions.SBTotalUsedBuses;
import com.socolabs.mo.vrplib.apps.schoolbusrouting.nodewieghtmanagers.*;
import com.socolabs.mo.vrplib.constraints.capacity.CapacityConstraint;
import com.socolabs.mo.vrplib.constraints.leq.Leq;
import com.socolabs.mo.vrplib.core.IVRPFunction;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.entities.LexMultiFunctions;
import com.socolabs.mo.vrplib.entities.accumulatedcalculators.AccumulatedEdgeCalculator;
import com.socolabs.mo.vrplib.entities.accumulatedcalculators.AccumulatedNodeCalculator;
import com.socolabs.mo.vrplib.entities.distancemanagers.DistanceManager;
import com.socolabs.mo.vrplib.entities.distancemanagers.TravelTimeManager;
import com.socolabs.mo.vrplib.functions.AccumulatedPointWeightsOnPath;
import com.socolabs.mo.vrplib.functions.NumberPointsOnRoute;
import com.socolabs.mo.vrplib.functions.div.DivConstantFunction;
import com.socolabs.mo.vrplib.functions.max.MaxAccumulatedWeightPoints;
import com.socolabs.mo.vrplib.functions.max.MaxRouteFunctions;
import com.socolabs.mo.vrplib.functions.min.MinAccumulatedWeightPoints;
import com.socolabs.mo.vrplib.functions.min.MinRouteFunctions;
import com.socolabs.mo.vrplib.functions.sum.SumAccumulatedWeightPoints;
import com.socolabs.mo.vrplib.functions.sum.SumRouteFunctions;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;
import com.socolabs.mo.vrplib.search.GreedySearch;
import com.socolabs.mo.vrplib.utils.CBLSVRP;
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
    private SBTotalUsedBuses totalUsedBuses;
    private AccumulatedWeightPoints accBoardingDistanceRatio;
    private AccumulatedWeightPoints accNumberVisitedPoints;


    private boolean verifying;

    public SchoolBusSolver() {

    }

    public SchoolBusSolver(boolean verifying) {
        this.verifying = verifying;
    }

    public SchoolBusRoutingSolution solve(SchoolBusRoutingInput input, String cluster) {
        for (SchoolBusRequest r : input.getRequests()) {
            if (input.getConfigParams().getServicePickupDuration() < r.getServicePickupDuration()) {
                input.getConfigParams().setServicePickupDuration(r.getServicePickupDuration());
            }
        }

//        input.getConfigParams().setTimeScale(0);
        int timePar = 5;
        HashSet<Integer> locationSet = new HashSet<>();
        for (SchoolBusRequest re : input.getRequests()) {
            locationSet.add(re.getPickupLocationId());
        }
        if (locationSet.size() > 100) {
            timePar = 10;
        }
//        if (locationSet.size() > 200) {
//            timePar = 15;
//        }
        System.out.println("solve");
        this.input = input;
        stateModel();
        System.out.println("done State Model");
        GreedySearch search = new GreedySearch(vr, objectiveFuncs);
        search.addExplorer(GreedySearch.INSERT_ONE_MOVE);
        search.search(100000, 10 * 60000, false);

//        for (VRPPoint p : vr.getAllPoints()) {
//            SchoolBusPickupPoint pp = (SchoolBusPickupPoint) p;
//            for (SchoolBusRequest re : pp.getRequests()) {
//                if (re.getPickupLocationId() == 201) {
//                    SchoolBusRoute route = (SchoolBusRoute) p.getRoute();
//                    System.out.println("cap = " + route.getCapacity());
//                }
//            }
//        }
//        System.exit(-1);
        search.addExplorer(GreedySearch.ONE_POINT_MOVE);
        search.addExplorer(GreedySearch.OR_OPT_MOVE);
        search.addExplorer(GreedySearch.TWO_POINTS_MOVE);
        search.addExplorer(GreedySearch.TWO_OPT_MOVE);
        search.addExplorer(GreedySearch.THREE_OPT_MOVE);
        search.addExplorer(GreedySearch.CROSS_EXCHANGE_MOVE);




        search.search(100000, timePar * 60000, true);

//        if (objectiveFuncs.getValueOfFunctionName("maxBoardingTimeScaleObjective")
//                > 1.0 * input.getConfigParams().getBoardingTimeScale1() / 100
//                && input.getConfigParams().getTimeScale() < 40) {
//            input.getConfigParams().setTimeScale(input.getConfigParams().getTimeScale() + 10);
//            SchoolBusSolver nextSolver = new SchoolBusSolver();
//            return nextSolver.solve(input, cluster);
//        }

        createObjectiveStep2();
        search.search(100000, timePar * 60000, true);

        try {
            SBUtils.exportSBExcel(input, vr, accArrivalTime, travelTimeManager, revAccTravelTime, mRoute2Capacity,
                    "D:\\Workspace\\VinSchool\\nodejs\\source\\nodejs\\data\\",
                    cluster);
        } catch (Exception e) {
//            System.out.println("SchoolBusSolver::ERROR exportSBExcel");
        }
        return SBUtils.exportSolution(input, vr);
    }

    private void createObjectiveStep2() {

        int idx = objectiveFuncs.size() - 1;

        HashMap<String, HashMap<String, Double>> newDistanceMap = new HashMap<>();
        for (String s : distanceMap.keySet()) {
            HashMap<String, Double> distanceVector = new HashMap<>();
            newDistanceMap.put(s, distanceVector);
            HashMap<String, Double> dv = distanceMap.get(s);
            for (String t : dv.keySet()) {
                if (s.equals("" + input.getShoolPointId())) {
                    distanceVector.put(t, 0.0);
                } else {
                    distanceVector.put(t, dv.get(t));
                }
            }
        }
        distanceManager = new DistanceManager(vr, newDistanceMap);
        AccumulatedWeightPoints accDistance = new AccumulatedWeightPoints(new AccumulatedEdgeCalculator(distanceManager));
        MaxAccumulatedWeightPoints minMaxDistance = new MaxAccumulatedWeightPoints(accDistance);
        objectiveFuncs.insert(idx++, minMaxDistance, LexMultiFunctions.MINIMIZE, "minMaxDistance");

        accNumberVisitedPoints = new AccumulatedWeightPoints(new SBNumberVisitedPointsCalculator(vr));
        SBMarginObjective marginObjective = new SBMarginObjective(vr, accNumberVisitedPoints);
        objectiveFuncs.insert(idx++, marginObjective, LexMultiFunctions.MINIMIZE, "marginObjective");

//        idx++;
//        MaxRouteFunctions maxminNbVisitedPointsObjective = new MaxRouteFunctions(vr, nbPointsOnRoutes);

//        MaxAccumulatedWeightPoints maxminNbVisitedPointsObjective = new MaxAccumulatedWeightPoints(accNumberVisitedPoints);
//        objectiveFuncs.insert(idx++, maxminNbVisitedPointsObjective, LexMultiFunctions.MINIMIZE, "maxminNbVisitedPointsObjective");

//        MinAccumulatedWeightPoints minmaxNbVisitedPointsObjective = new MinAccumulatedWeightPoints(accNumberVisitedPoints);
//        objectiveFuncs.insert(idx++, minmaxNbVisitedPointsObjective, LexMultiFunctions.MAXIMIZE, "minmaxNbVisitedPointsObjective");

//        MinRouteFunctions minmaxNbVisitedPointsObjective = new MinRouteFunctions(vr, nbPointsOnRoutes);
//        objectiveFuncs.insert(idx + 1, minmaxNbVisitedPointsObjective, LexMultiFunctions.MAXIMIZE, "maxminNbVisitedPointsObjective");
//        idx++;
//        HashMap<VRPRoute, IVRPFunction> mRoute2Div = new HashMap<>();
//        for (VRPRoute route : vr.getAllRoutes()) {
//            mRoute2Div.put(route, new DivConstantFunction(100, nbPointsOnRoutes.get(route)));
//        }
//        SumRouteFunctions sumDiv = new SumRouteFunctions(vr, mRoute2Div);
//        objectiveFuncs.insert(idx, sumDiv, LexMultiFunctions.MINIMIZE, "nbPointRateObjective");

    }

    public void setVerifying() {
        vr.setVerifying(verifying);
    }

    private void stateModel() {
        vr = new VRPVarRoutes();
        setVerifying();
        objectiveFuncs = new LexMultiFunctions();
        System.out.println("initTravelTime");
        initTravelTime();
        System.out.println("createRoutes");
        createRoutes();
        System.out.println("createPoints");
        createPoints();
//        System.out.println("addMaxVisitedNbLocationsConstraint");
//        addMaxVisitedNbLocationsConstraint();
        System.out.println("addMaximumNumberOfStudents");
        addMaximumNumberOfStudents();
        System.out.println("addCapacityConstraint");
        addCapacityConstraint();
        System.out.println("addGroupConstraint");
        addGroupConstraint();
        System.out.println("addRoadBlockConstraint");
        addRoadBlockConstraint();
        System.out.println("addTravelTimeConstraint");
        addTravelTimeConstraint();
//        System.out.println("addDistanceConstraint");
//        addDistanceConstraint();
        System.out.println("addMinimumUsedBusesObjective");
        addMinimumUsedBusesObjective();
        System.out.println("addBoardingTimeScaleObjective");
        addBoardingTimeScaleObjective();
        System.out.println("addTravelTimeObjective");
        addTravelTimeObjective();
    }

    private void addMaxVisitedNbLocationsConstraint() {
//        HashMap<VRPRoute, IVRPFunction> nbPointsOnRoutes = new HashMap<>();
        for (VRPRoute route : vr.getAllRoutes()) {
//            nbPointsOnRoutes.put(route, new NumberPointsOnRoute(vr, route));
            Leq l = new Leq(new NumberPointsOnRoute(vr, route), input.getConfigParams().getMaxVisitedNumberPoints());
            vr.addSatisfiedConstraint(l);
        }

    }

    private void addMarginObjective() {
        accNumberVisitedPoints = new AccumulatedWeightPoints(new SBNumberVisitedPointsCalculator(vr));
        SBMarginObjective marginObjective = new SBMarginObjective(vr, accNumberVisitedPoints);
        objectiveFuncs.add(marginObjective, LexMultiFunctions.MINIMIZE, "marginObjective");
    }

    private void addDistanceConstraint() {
        HashMap<String, HashMap<String, Double>> newDistanceMap = new HashMap<>();
        for (String s : distanceMap.keySet()) {
            HashMap<String, Double> distanceVector = new HashMap<>();
            newDistanceMap.put(s, distanceVector);
            HashMap<String, Double> dv = distanceMap.get(s);
            for (String t : dv.keySet()) {
                if (s.equals("" + input.getShoolPointId())) {
                    distanceVector.put(t, 0.0);
                } else {
                    distanceVector.put(t, dv.get(t));
                }
            }
        }
        distanceManager = new DistanceManager(vr, newDistanceMap);
        AccumulatedWeightPoints accDistance = new AccumulatedWeightPoints(new AccumulatedEdgeCalculator(distanceManager));
        MaxAccumulatedWeightPoints minMaxDistance = new MaxAccumulatedWeightPoints(accDistance);
        objectiveFuncs.add(minMaxDistance, LexMultiFunctions.MINIMIZE, "minMaxDistance");
//        revAccDistance = new RevAccumulatedWeightPoints(new SBRevDistanceCalculator(distanceManager));
//        SumAccumulatedWeightPoints totalDistanceViolations = new SumAccumulatedWeightPoints(1
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
        totalUsedBuses = new SBTotalUsedBuses(vr);
        objectiveFuncs.add(totalUsedBuses, LexMultiFunctions.MINIMIZE, "totalUsedBuses");
//        Leq nbBusesLimit = new Leq(totalUsedBuses, 45);
//        vr.addSatisfiedConstraint(nbBusesLimit);
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
        accBoardingDistanceRatio = new AccumulatedWeightPoints(new SBBoardingTimeScaleCalculator(revAccTravelTime, input.getConfigParams().getServicePickupDuration()));
        MaxAccumulatedWeightPoints boardingTimeScaleObjective = new MaxAccumulatedWeightPoints(accBoardingDistanceRatio);
        objectiveFuncs.add(boardingTimeScaleObjective, LexMultiFunctions.MINIMIZE, "maxBoardingTimeScaleObjective");

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
        ArrayList<Vehicle> vehicles = new ArrayList<>();
        for (Vehicle v : input.getVehicles()) {
            vehicles.add(v);
        }
        Collections.sort(vehicles, new Comparator<Vehicle>() {
            @Override
            public int compare(Vehicle o1, Vehicle o2) {
                return o2.getCap() - o1.getCap();
            }
        });
        for (Vehicle v : vehicles) {
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
            if (locationID == 31) {
                System.out.println("debug");
            }
            HashSet<ArrayList<SchoolBusRequest>> locationRequests = mLocationId2Requests.get(locationID);
            int roadBlock = 0;
            try {
                roadBlock = roadBlockMap.get("" + locationID).get("" + input.getShoolPointId());
            } catch (NullPointerException e) {
                roadBlock = 0;
            }
            int roadBlockCap = SBUtils.getRoadBloakCap(roadBlock);
            SchoolBusRoute route = findRoute(roadBlockCap);
            double routeCap = roadBlockCap;
            try {
                routeCap = mRoute2Capacity.get(route);
            } catch (Exception e) {

            }

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
                        if (o2.size() - o1.size() != 0) {
                            return o2.size() - o1.size();
                        }
                        return o1.get(0).getIdPerson() - o2.get(0).getIdPerson();
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
//                        System.out.println(orderedSubRequests.size());
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
                if (cnt > 0) {
                    SchoolBusPickupPoint point = new SchoolBusPickupPoint("" + locationID, vr, input, travelTimeMap, distanceMap);
                    for (SchoolBusRequest r : dividedRequests) {
                        point.addRequest(r);
                    }
                }
            }
        }
        for (SchoolBusRequest re : input.getRequests()) {
            boolean ok = false;
            for (VRPPoint p : vr.getAllPoints()) {
                SchoolBusPickupPoint pp = (SchoolBusPickupPoint) p;
                if (pp.getRequests().contains(re)) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                System.out.println(re.getId());
            }
        }
//        System.exit(0);
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
        for (int x : locationIdSet) {
            for (int y : locationIdSet) {
                String src = "" + x;
                String dest = "" + y;
                try {
                    int d = travelTimeMap.get(src).get(dest);
                } catch (Exception e) {
                    if (!travelTimeMap.containsKey(src)) {
                        travelTimeMap.put(src, new HashMap<>());
                        roadBlockMap.put(src, new HashMap<>());
                        distanceMap.put(src, new HashMap<>());
                    }
                    if (x == y) {
                        travelTimeMap.get(src).put(dest, 0);
                        roadBlockMap.get(src).put(dest, 0);
                        distanceMap.get(src).put(dest, 0.);
                    } else {
                        travelTimeMap.get(src).put(dest, CBLSVRP.MAX_INT);
                        roadBlockMap.get(src).put(dest, 0);
                        distanceMap.get(src).put(dest, 1e9);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {

//        File f = new File("D:\\Workspace\\VinSchool\\20200731\\VSC");
//
//        // Populates the array with names of files and directories
//        String[] pathnames = f.list();
//
//        // For each pathname in the pathnames array
//        for (String pathname : pathnames) {
//            // Print the names of files and directories
//            System.out.println(pathname);
//            Gson g = new Gson();
//            String inputFile = "D:\\Workspace\\VinSchool\\20200731\\VSC\\" + pathname;//"D:\\Workspace\\VinSchool\\Times_20200718\\34-105qjx04py4ysixre1pjhoe5jds_time11_41_707nextyear-input_new.json";
//            BufferedReader in = new BufferedReader(new FileReader(inputFile));
//            SchoolBusRoutingInput input = g.fromJson(in, SchoolBusRoutingInput.class);
//
//            input.getConfigParams().setTimeScale(15);
//            SchoolBusSolver solver = new SchoolBusSolver();
//            SchoolBusRoutingSolution sol = solver.solve(input, pathname.split(".json")[0]);
//////
//            String outputJson = g.toJson(sol);
//            try {
//                BufferedWriter fo = new BufferedWriter(new FileWriter(inputFile.split(".json")[0] + "_output.json"));
//                fo.write(outputJson);
//                fo.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        System.exit(0);

        Gson g = new Gson();
        String inputFile = "D:\\Workspace\\VinSchool\\20200731\\Inputs\\Name577-input.json";//"D:\\Workspace\\VinSchool\\Times_20200718\\34-105qjx04py4ysixre1pjhoe5jds_time11_41_707nextyear-input_new.json";
        BufferedReader in = new BufferedReader(new FileReader(inputFile));
        SchoolBusRoutingInput input = g.fromJson(in, SchoolBusRoutingInput.class);

//        ArrayList<SchoolBusRequest> avaiRequests = new ArrayList<>();
//        HashSet<Integer> idLocations = new HashSet<>();
//        for (DistanceElement de : input.getDistances()) {
//            if (de.getDestCode() == input.getShoolPointId()) {
//                idLocations.add(de.getSrcCode());
//            }
//        }
//        HashSet<Integer> requestIdSet = new HashSet<>();
//        for (SchoolBusRequest r : input.getRequests()) {
//            if (idLocations.contains(r.getPickupLocationId())) {// && idLocations.contains(r.getDeliveryLocationId())) {
//                avaiRequests.add(r);
//                requestIdSet.add(r.getId());
//            } else {
//
//            }
//        }
//        System.out.println(avaiRequests.size());
//        SchoolBusRequest[] newRequests = new SchoolBusRequest[avaiRequests.size()];
//        for (int i = 0; i < newRequests.length; i++) {
//            newRequests[i] = avaiRequests.get(i);
//        }
//        input.setRequests(newRequests);
//        SBUtils.recreateTravelTimeMatrix(input, inputFile.split(".json")[0] + "_new.json");
//        System.exit(0);
//        System.out.println(newRequests.length);
//        input.getConfigParams().setTimeScale(0);
//        SBUtils.recreateTravelTimeMatrixUsingOpenStreetMap(input, 20, inputFile.split(".json")[0] + "_openstreetmap.json");
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
//        for (DistanceElement de : input.getDistances()) {
//            de.setTravelTime(de.getDistance() / (1000 * 12) * 3600);
//        }
//        for (SchoolBusRequest re : input.getRequests()) {
//            re.setServicePickupDuration(180);
//        }
//        input.getConfigParams().setTimeScale(25);

//        input.getConfigParams().setServicePickupDuration(180);
        SchoolBusSolver solver = new SchoolBusSolver();
        SchoolBusRoutingSolution sol = solver.solve(input, "Imperia");
////////
//        String outputJson = g.toJson(sol);
//        try {
//            BufferedWriter fo = new BufferedWriter(new FileWriter(inputFile.split(".json")[0] + "_output.json"));
//            fo.write(outputJson);
//            fo.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(boardingTimeScale);
//        solver = new SchoolBusSolver();
//        SchoolBusRoutingSolution sol = solver.solveStep2(input, boardingTimeScale);
//        input.setCurrentSolution(null);
//        input.setMoveActions(null);
//        BusRouteSolver brSolver = new BusRouteSolver();
//        SchoolBusRoutingSolution solution = brSolver.randomSolve(input);
//
//        String outputJson = g.toJson(solution);
//        try {
//            BufferedWriter fo = new BufferedWriter(new FileWriter(inputFile.split(".json")[0] + "_output.json"));
//            fo.write(outputJson);
//            fo.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
}
