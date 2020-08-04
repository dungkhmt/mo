package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.google.gson.Gson;
import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.maps.distanceelementquery.DistanceElementQuery;
import com.socolabs.mo.components.maps.distanceelementquery.GeneralDistanceElement;
import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.*;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.utils.DateTimeUtils;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.Direction;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SBUtils {

    public final static int BOARDING_TIME_1 = 900;
    public final static int BOARDING_TIME_2 = 1800;

    public static int getRoadBloakCap(int roadBlock) {
        if (roadBlock == Utils.CAP_FLEXIBILITY) {
            return Utils.CAP_45;
        }
        if (roadBlock == Utils.CAP_29) {
            return Utils.CAP_29;
        }
        return Utils.CAP_16;
    }

    public static void exportSBExcel(SchoolBusRoutingInput input,
                                     VRPVarRoutes vr,
                                     AccumulatedWeightPoints accArrivalTime,
                                     IDistanceManager travelTimeManager,
                                     RevAccumulatedWeightPoints revAccTravelTime,
                                     HashMap<VRPRoute, Double> mRoute2Capacity,
                                     String dir, String note) {
        HashMap<String, String> mLocationId2Address = new HashMap<>();
        if (input != null && input.getCurrentSolution() != null) {
            for (BusRoute r : input.getCurrentSolution().getBusRoutes()) {
                for (RouteElement node : r.getNodes()) {
                    mLocationId2Address.put("" + node.getLocationId(), node.getNodeName());
                }
            }
        }

        try {
            for (SchoolBusRequest re : input.getRequests()) {
                if (!mLocationId2Address.containsKey(re.getPickupLocationId())) {
                    mLocationId2Address.put("" + re.getPickupLocationId(), re.getAddress());
                }
            }
        } catch (Exception e) {

        }

        String fo = dir + "solution-" + note + ".xlsx";
        XSSFWorkbook workbook = new XSSFWorkbook();
        String str;

        XSSFSheet reqSheet = workbook.createSheet("route");
        Row row = reqSheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("BusID");
        cell = row.createCell(1);
        cell.setCellValue("Diem don");
        cell = row.createCell(2);
        cell.setCellValue("Thoi gian don");
        cell = row.createCell(3);
        cell.setCellValue("So HS");
        cell = row.createCell(4);
        cell.setCellValue("Danh Sach HS");
        cell = row.createCell(5);
        cell.setCellValue("Lat");
        cell = row.createCell(6);
        cell.setCellValue("Lng");
        cell = row.createCell(7);
        cell.setCellValue("Dia chi");
//        cell = row.createCell(8);
//        cell.setCellValue("Thoi gian ngoi tren xe chieu di");
//        cell = row.createCell(9);
//        cell.setCellValue("Diem tra");
//        cell = row.createCell(10);
//        cell.setCellValue("Thoi gian tra");
//        cell = row.createCell(11);
//        cell.setCellValue("Thoi gian ngoi tren xe chieu ve");

        int i = 0;
        int busId = 0;
        for (VRPRoute r : vr.getAllRoutes()) {
            if (r.getNbPoints() == 0) {
                continue;
            }
            busId ++;
            int totalStudents = 0;
            SchoolBusRoute sbR = (SchoolBusRoute) r;
            for (VRPPoint p = r.getStartPoint(); p != null; p = p.getNext()) {
                SchoolBusPickupPoint sbP = (SchoolBusPickupPoint) p;
                row = reqSheet.createRow(++i);
                cell = row.createCell(0);
                cell.setCellValue(busId);
                cell = row.createCell(1);
                cell.setCellValue(sbP.getLocationCode());
                cell = row.createCell(2);
                int t = (int) accArrivalTime.getWeightValueOfPoint(p);
                int h = (int) (t / 3600);
                int m = (int) ((t - h * 3600) / 60);
                int s = (int) (t - h * 3600 - m * 60);
                str = h + ":" + m + ":" + s;
                cell.setCellValue(str);
                cell = row.createCell(3);
                totalStudents += sbP.size();
                if (p.isEndPoint()) {
                    cell.setCellValue(totalStudents + "/" + mRoute2Capacity.get(r));
                } else {
                    cell.setCellValue(sbP.size());
                }
                cell = row.createCell(4);
                cell.setCellValue(sbP.getStudentLst());
                cell = row.createCell(5);
                cell.setCellValue(sbP.getLat());
                cell = row.createCell(6);
                cell.setCellValue(sbP.getLng());
                if (mLocationId2Address.containsKey(sbP.getLocationCode())) {
                    cell = row.createCell(7);
                    cell.setCellValue(mLocationId2Address.get(sbP.getLocationCode()));
                }
//                cell.setCellValue(travelTimeManager.getDistance(p, r.getEndPoint()));
//                cell = row.createCell(8);
//                cell.setCellValue(revAccTravelTime.getWeightValueOfPoint(p));
            }
        }
        try{
            FileOutputStream outputStream = new FileOutputStream(fo);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (IOException | EncryptedDocumentException ex) {
            System.out.println("export error");
            ex.printStackTrace();
        }
    }

    public static SchoolBusRoutingSolution exportSolution(SchoolBusRoutingInput input, VRPVarRoutes vr) {
        HashMap<Integer, HashMap<Integer, DistanceElement>> distanceElementMap = new HashMap<>();
        for (DistanceElement de : input.getDistances()) {
            int src = de.getSrcCode();
            int dest = de.getDestCode();
            if (!distanceElementMap.containsKey(src)) {
                distanceElementMap.put(src, new HashMap<>());
            }
            distanceElementMap.get(src).put(dest, de);
        }
        HashMap<Integer, Vehicle> mVehicleId2Vehicle = new HashMap<>();
        for (Vehicle v : input.getVehicles()) {
            mVehicleId2Vehicle.put(v.getId(), v);
        }
        ArrayList<BusRoute> busRoutes = new ArrayList<>();
        HashMap<Integer, Integer> varIndexMap = new HashMap<>();
//        ArrayList<SchoolBusRequest> unScheduledRequests = new ArrayList<>();
        int numberBuses = 0;
        double totalDistance = 0;
        for (VRPRoute r : vr.getAllRoutes()) {
            if (r.getNbPoints() > 0) {
                numberBuses ++;
                int nbPersons = 0;
                int nbStops = 0;
                double travelTime = 0;
                double travelDistance = 0;
                int updateFlag = 0;
                int isResolved = 0;
                int curPoint = input.getShoolPointId();
                ArrayList<RouteElement> nodeList = new ArrayList<>();
                int arrivalTime = input.getConfigParams().getEarliestDateTimePickupAtPoint();
                for (VRPPoint p = r.getStartPoint().getNext(); p != null; p = p.getNext()) {
                    SchoolBusPickupPoint sbP = (SchoolBusPickupPoint) p;
                    nbPersons += sbP.size();
                    nbStops ++;
                    DistanceElement de = distanceElementMap.get(curPoint).get(Integer.parseInt(sbP.getLocationCode()));
                    if (nbStops > 1) {
                        arrivalTime += de.getTravelTime();
                    }

                    curPoint = Integer.parseInt(sbP.getLocationCode());
                    if (!varIndexMap.containsKey(curPoint)) {
                        varIndexMap.put(curPoint, 0);
                    }
                    varIndexMap.put(curPoint, varIndexMap.get(curPoint) + 1);
                    int[] hsList = new int[sbP.size()];
                    int[] listRegisterId = new int[sbP.size()];
                    int ii = 0;
                    for (SchoolBusRequest request : sbP.getRequests()) {
                        hsList[ii] = request.getIdPerson();
                        listRegisterId[ii] = request.getId();
                        ii++;
                    }
                    try {
                        nodeList.add(new RouteElement(
                                curPoint,
                                varIndexMap.get(curPoint),
                                "PICKUP_POINT",
                                arrivalTime,
                                (int) ((int) arrivalTime + sbP.getPickupServiceTime()),
                                (int) distanceElementMap.get(curPoint).get(input.getShoolPointId()).getTravelTime(),
                                0,
                                hsList,
                                listRegisterId
                        ));
                    } catch (Exception e) {
                        nodeList.add(new RouteElement(
                                curPoint,
                                varIndexMap.get(curPoint),
                                "PICKUP_POINT",
                                arrivalTime,
                                (int) ((int) arrivalTime + sbP.getPickupServiceTime()),
                                (int) 0,
                                0,
                                hsList,
                                listRegisterId
                        ));
                    }
                    if (nbStops > 1) {
                        travelTime += de.getTravelTime();
                        travelDistance += de.getDistance();
                    }
                    arrivalTime += sbP.getPickupServiceTime();
                }
//                DistanceElement de = distanceElementMap.get(curPoint).get(input.getShoolPointId());
//                travelTime += de.getTravelTime();
//                travelDistance += de.getDistance();
//                arrivalTime += de.getTravelTime();
                totalDistance += travelDistance;
                int extraTime = Math.max(0, input.getConfigParams().getEarliestDatetimeArrivalSchool() - arrivalTime);
                int deliveryTime = Math.max(input.getConfigParams().getEarliestDatetimeArrivalSchool(), arrivalTime);
                RouteElement[] nodes = new RouteElement[nodeList.size()];
                int curTravelTime = 0;
                curPoint = input.getShoolPointId();
                for (int ii = nodes.length - 1; ii >= 0; ii--) {
                    nodes[ii] = nodeList.get(ii);
                    nodes[ii].setArrivalTime(nodes[ii].getArrivalTime() + extraTime);
                    try {
                        curTravelTime += distanceElementMap.get(nodes[ii].getLocationId()).get(curPoint).getTravelTime();
                    } catch (Exception e) {
                    }
                    nodes[ii].setTravelTime(curTravelTime);
                    curPoint = nodes[ii].getLocationId();
                }
                int ii = 0;
                curTravelTime = 0;
                curPoint = input.getShoolPointId();
                arrivalTime = input.getConfigParams().getEarliestDateTimePickupAtSchool();
                RouteElement[] reverses = new RouteElement[nodes.length - 1];
                for (VRPPoint p = r.getEndPoint().getPrev(); p != r.getStartPoint(); p = p.getPrev()) {
                    SchoolBusPickupPoint sbP = (SchoolBusPickupPoint) p;
                    int deliveryLocationId = sbP.getRequests().get(0).getDeliveryLocationId();
                    DistanceElement de = distanceElementMap.get(curPoint).get(deliveryLocationId);
                    curTravelTime += de.getTravelTime();
                    curPoint = deliveryLocationId;
                    arrivalTime += de.getTravelTime();
                    reverses[ii] = new RouteElement(
                            curPoint,
                            nodes[nodes.length - ii - 1].getVarIndex(),
                            "DELIVERY_POINT",
                            arrivalTime,
                            arrivalTime + sbP.getRequests().get(0).getServiceDeliveryDuration(),
                            ((int)distanceElementMap.get(input.getShoolPointId()).get(curPoint).getTravelTime()),
                            curTravelTime,
                            nodes[nodes.length - ii - 1].getHsList(),
                            nodes[nodes.length - ii - 1].getListRegisterId()
                    );
                    arrivalTime += sbP.getRequests().get(0).getServiceDeliveryDuration();
                    ii++;
                }
                SchoolBusRoute sbR = (SchoolBusRoute) r;
                busRoutes.add(new BusRoute(
                        mVehicleId2Vehicle.get(Integer.parseInt(sbR.getTruckCode())),
                        Integer.parseInt(sbR.getTruckCode()),
                        "Bus_" + sbR.getTruckCode(),
                        nbPersons,
                        ((int) (1.0 * nbPersons / sbR.getCapacity() * 100)),
                        isResolved,
                        nbStops,
                        input.getConfigParams().getEarliestDateTimePickupAtPoint() + extraTime,
                        deliveryTime,
                        ((int)travelTime),
                        travelDistance,
                        updateFlag,
                        nodes,
                        reverses,
                        null
                ));
            }
        }

        BusRoute[] busRoutesArr = new BusRoute[busRoutes.size()];
        busRoutes.toArray(busRoutesArr);
        return new SchoolBusRoutingSolution(busRoutesArr,
                new SchoolBusRequest[0],
                new StatisticInformation(input.getRequests().length,
                        0,
                        0,
                        totalDistance,
                        numberBuses,
                        null));
    }

    public static int calcDifferenceBetween2Solutions(SchoolBusRoutingSolution randomSolution,
                                                      SchoolBusRoutingSolution officialSolution) {
        HashMap<Integer, HashSet<Integer>> mLocation2PrevLocationOfRandomSol = new HashMap<>();
        for (BusRoute r : randomSolution.getBusRoutes()) {
            int prevLocationId = -1;
            for (RouteElement e : r.getNodes()) {
                int locationId = e.getLocationId();
                if (!mLocation2PrevLocationOfRandomSol.containsKey(locationId)) {
                    mLocation2PrevLocationOfRandomSol.put(locationId, new HashSet<>());
                }
                mLocation2PrevLocationOfRandomSol.get(locationId).add(prevLocationId);
                prevLocationId = e.getLocationId();
            }
        }
        for (SchoolBusRequest re : randomSolution.getUnScheduledRequests()) {
            int locationId = re.getPickupLocationId();
            if (!mLocation2PrevLocationOfRandomSol.containsKey(locationId)) {
                mLocation2PrevLocationOfRandomSol.put(locationId, new HashSet<>());
            }
            mLocation2PrevLocationOfRandomSol.get(locationId).add(-1);
        }
        HashMap<Integer, HashSet<Integer>> mLocation2PrevLocationOfOfficialSol = new HashMap<>();
        for (BusRoute r : officialSolution.getBusRoutes()) {
            int prevLocationId = -1;
            for (RouteElement e : r.getNodes()) {
                int locationId = e.getLocationId();
                if (!mLocation2PrevLocationOfOfficialSol.containsKey(locationId)) {
                    mLocation2PrevLocationOfOfficialSol.put(locationId, new HashSet<>());
                }
                mLocation2PrevLocationOfOfficialSol.get(locationId).add(prevLocationId);
                prevLocationId = e.getLocationId();
            }
        }
        for (SchoolBusRequest re : officialSolution.getUnScheduledRequests()) {
            int locationId = re.getPickupLocationId();
            if (!mLocation2PrevLocationOfOfficialSol.containsKey(locationId)) {
                mLocation2PrevLocationOfOfficialSol.put(locationId, new HashSet<>());
            }
            mLocation2PrevLocationOfOfficialSol.get(locationId).add(-1);
        }
        int nbChange = 0;
        for (Integer locationId : mLocation2PrevLocationOfOfficialSol.keySet()) {
            HashSet<Integer> randomSet = mLocation2PrevLocationOfRandomSol.get(locationId);
            HashSet<Integer> officialSet = mLocation2PrevLocationOfOfficialSol.get(locationId);
            if (randomSet == null && officialSet != null) {
                nbChange += officialSet.size();
            }
            if (randomSet != null && officialSet == null) {
                nbChange += randomSet.size();
            }
            for (int x : officialSet) {
                if (!randomSet.contains(x)) {
                    nbChange++;
                }
            }
        }
        return nbChange;
    }

    public static void recreateTravelTimeMatrix(SchoolBusRoutingInput input, String savePath) {
        HashSet<Integer> locationIdSet = new HashSet<>();
        HashMap<Integer, Pair<Double, Double>> mLocationId2LatLng = new HashMap<>();
        locationIdSet.add(input.getShoolPointId());
        mLocationId2LatLng.put(input.getShoolPointId(), new Pair<>(input.getLat_school(), input.getLong_school()));
        for (SchoolBusRequest r : input.getRequests()) {
            if (r.getLat_pickup() != 0 && r.getLong_pickup() != 0 && r.getLat_pickup() != r.getLong_pickup()) {
                locationIdSet.add(r.getPickupLocationId());
                mLocationId2LatLng.put(r.getPickupLocationId(),
                        new Pair<>(r.getLat_pickup(), r.getLong_pickup()));
            }
//            if (r.getLat_delivery() != 0 && r.getLong_delivery() != 0 && r.getLat_delivery() != r.getLong_delivery()) {
//                locationIdSet.add(r.getDeliveryLocationId());
//                mLocationId2LatLng.put(r.getDeliveryLocationId(),
//                        new Pair<>(r.getLat_delivery(), r.getLong_delivery()));
//            }
        }
        int n = locationIdSet.size();
        GoogleMapsQuery GMQ = new GoogleMapsQuery();
        long departure_time = (long) DateTimeUtils.dateTime2Int("2020-08-17 07:30:00");
        for (int i = 0; i < input.getDistances().length; i++) {
            try {
                DistanceElement de = input.getDistances()[i];
                int src = de.getSrcCode();
                int dest = de.getDestCode();
                if (src != 2808 && dest != 2808 && src != 4264 && dest != 4264) {
                    continue;
                }
                if (src == dest) {
                    de.setTravelTime(0);
                    de.setDistance(0);
                    continue;
                }
                Pair<Double, Double> srcLatLng = mLocationId2LatLng.get(src);
                Pair<Double, Double> destLatLng = mLocationId2LatLng.get(dest);
                int cnt = 0;
                while (cnt < 10) {
                    Direction direction = GMQ.getDirection(srcLatLng.first, srcLatLng.second, destLatLng.first, destLatLng.second, "driving", departure_time);
                    if (direction != null) {
                        System.out.println(i + " -> duration = " + direction.getDurations() + " distance = " + direction.getDistances());
                        if (direction.getDurations() > 0) {
                            de.setTravelTime(direction.getDurations());
                            de.setDistance(direction.getDistances());
                            break;
                        }
                    }
                    cnt++;
                }
            } catch (Exception e) {

            }
        }
        Gson g = new Gson();
        String inputJson = g.toJson(input);
        try {
            BufferedWriter fo = new BufferedWriter(new FileWriter(savePath));
            fo.write(inputJson);
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void recreateTravelTimeMatrixUsingOpenStreetMap(SchoolBusRoutingInput input, double speed, String savePath) {
        HashSet<Integer> locationIdSet = new HashSet<>();
        HashMap<Integer, Pair<Double, Double>> mLocationId2LatLng = new HashMap<>();
        locationIdSet.add(input.getShoolPointId());
        mLocationId2LatLng.put(input.getShoolPointId(), new Pair<>(input.getLat_school(), input.getLong_school()));
        for (SchoolBusRequest r : input.getRequests()) {
            if (r.getLat_pickup() != 0 && r.getLong_pickup() != 0 && r.getLat_pickup() != r.getLong_pickup()) {
                locationIdSet.add(r.getPickupLocationId());
                mLocationId2LatLng.put(r.getPickupLocationId(),
                        new Pair<>(r.getLat_pickup(), r.getLong_pickup()));
            }
            if (r.getLat_delivery() != 0 && r.getLong_delivery() != 0 && r.getLat_delivery() != r.getLong_delivery()) {
                locationIdSet.add(r.getDeliveryLocationId());
                mLocationId2LatLng.put(r.getDeliveryLocationId(),
                        new Pair<>(r.getLat_delivery(), r.getLong_delivery()));
            }
        }

        DistanceElementQuery distanceElementQuery = new DistanceElementQuery();
        distanceElementQuery.setParams("OpenStreetMap");
        int n = mLocationId2LatLng.size();
        GeneralDistanceElement[] generalDistanceElements = new GeneralDistanceElement[n * n];
        int i = 0;
        for (int x : mLocationId2LatLng.keySet()) {
            Pair<Double, Double> xLocation = mLocationId2LatLng.get(x);
            for (int y : mLocationId2LatLng.keySet()) {
                Pair<Double, Double> yLocation = mLocationId2LatLng.get(y);
                generalDistanceElements[i++] = new GeneralDistanceElement(
                        "" + x,
                        xLocation.first,
                        xLocation.second,
                        "" + y,
                        yLocation.first,
                        yLocation.second,
                        0,
                        0,
                        0
                        );
            }
        }
        distanceElementQuery.setElements(generalDistanceElements);
        Gson g = new Gson();
        String queryJson = g.toJson(distanceElementQuery);

        long cur = System.currentTimeMillis();

        try {

            String url = "http://13.229.140.7:7474/BusHanoiCity/get-distance-elements";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Setting basic post request
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("apikey", "6OCqwI5lsE9H3RdXaJ5kucvScPQsqfGF");
            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(queryJson);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            System.out.println("nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();

            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            in.close();

            System.out.println("request time = " + (System.currentTimeMillis() - cur));
            distanceElementQuery = g.fromJson(response.toString(), DistanceElementQuery.class);

            DistanceElement[] newDistanceElements = new DistanceElement[distanceElementQuery.getElements().length];
            i = 0;
            for (GeneralDistanceElement gde : distanceElementQuery.getElements()) {
                newDistanceElements[i++] = new DistanceElement(Integer.parseInt(gde.getFromId()),
                        Integer.parseInt(gde.getToId()),
                        gde.getDistance(),
                        gde.getDistance() / (1000 * speed) * 3600,
                        0);
            }
            input.setDistances(newDistanceElements);
        } catch (Exception e) {
            System.out.println("request time = " + (System.currentTimeMillis() - cur));
        }

        String inputJson = g.toJson(input);
        try {
            BufferedWriter fo = new BufferedWriter(new FileWriter(savePath));
            fo.write(inputJson);
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void forecaseTimeScaleToGGMap(String dataFile) {
        
    }

    public static double mean(double[] dataLst) {
        return new DescriptiveStatistics(dataLst).getMean();
    }

    public static double std(double[] dataLst) {
        return new DescriptiveStatistics(dataLst).getStandardDeviation();
    }
}
