package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.google.gson.Gson;
import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
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
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
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

    public static void exportSBExcel(VRPVarRoutes vr,
                                     AccumulatedWeightPoints accArrivalTime,
                                     IDistanceManager travelTimeManager,
                                     RevAccumulatedWeightPoints revAccTravelTime,
                                     HashMap<VRPRoute, Double> mRoute2Capacity,
                                     String dir, String note) {
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
        cell.setCellValue("Thoi gian di thang");
        cell = row.createCell(8);
        cell.setCellValue("Thoi gian ngoi tren xe chieu di");
        cell = row.createCell(9);
        cell.setCellValue("Diem tra");
        cell = row.createCell(10);
        cell.setCellValue("Thoi gian tra");
        cell = row.createCell(11);
        cell.setCellValue("Thoi gian ngoi tren xe chieu ve");

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
                cell = row.createCell(7);
                cell.setCellValue(travelTimeManager.getDistance(p, r.getEndPoint()));
                cell = row.createCell(8);
                cell.setCellValue(revAccTravelTime.getWeightValueOfPoint(p));
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
        for (VRPRoute r : vr.getAllRoutes()) {
            if (r.getNbPoints() > 0) {
                int nbPersons = 0;
                int nbStops = 0;
                double travelTime = 0;
                double travelDistance = 0;
                int updateFlag = 0;
                int isResolved = 0;
                int curPoint = input.getShoolPointId();
                ArrayList<RouteElement> nodeList = new ArrayList<>();
                int arrivalTime = input.getConfigParams().getEarliestDateTimePickupAtPoint();
                for (VRPPoint p = r.getStartPoint().getNext(); p != r.getEndPoint(); p = p.getNext()) {
                    SchoolBusPickupPoint sbP = (SchoolBusPickupPoint) p;
                    nbPersons += sbP.size();
                    nbStops ++;
                    DistanceElement de = distanceElementMap.get(curPoint).get(Integer.parseInt(sbP.getLocationCode()));
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
                    if (nbStops > 1) {
                        travelTime += de.getTravelTime();
                        travelDistance += de.getDistance();
                    }
                    arrivalTime += sbP.getPickupServiceTime() + de.getTravelTime();
                }
                DistanceElement de = distanceElementMap.get(curPoint).get(input.getShoolPointId());
                travelTime += de.getTravelTime();
                travelDistance += de.getDistance();
                arrivalTime += de.getTravelTime();
                int extraTime = Math.max(0, input.getConfigParams().getEarliestDatetimeArrivalSchool() - arrivalTime);
                RouteElement[] nodes = new RouteElement[nodeList.size()];
                int curTravelTime = 0;
                curPoint = input.getShoolPointId();
                for (int ii = nodes.length - 1; ii >= 0; ii--) {
                    nodes[ii] = nodeList.get(ii);
                    nodes[ii].setArrivalTime(nodes[ii].getArrivalTime() + extraTime);
                    curTravelTime += distanceElementMap.get(nodes[ii].getLocationId()).get(curPoint).getTravelTime();
                    nodes[ii].setTravelTime(curTravelTime);
                    curPoint = nodes[ii].getLocationId();
                }
                int ii = 0;
                curTravelTime = 0;
                curPoint = input.getShoolPointId();
                arrivalTime = input.getConfigParams().getEarliestDateTimePickupAtSchool();
                RouteElement[] reverses = new RouteElement[nodes.length];
                for (VRPPoint p = r.getEndPoint().getPrev(); p != r.getStartPoint(); p = p.getPrev()) {
                    SchoolBusPickupPoint sbP = (SchoolBusPickupPoint) p;
                    int deliveryLocationId = sbP.getRequests().get(0).getDeliveryLocationId();
                    de = distanceElementMap.get(curPoint).get(deliveryLocationId);
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
                        ((int) nbPersons / sbR.getCapacity() * 100),
                        isResolved,
                        nbStops,
                        0,
                        0,
                        ((int)travelTime),
                        travelDistance,
                        updateFlag,
                        nodes,
                        reverses,
                        null
                ));
            }
        }
        return null;
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
            if (r.getLat_delivery() != 0 && r.getLong_delivery() != 0 && r.getLat_delivery() != r.getLong_delivery()) {
                locationIdSet.add(r.getDeliveryLocationId());
                mLocationId2LatLng.put(r.getDeliveryLocationId(),
                        new Pair<>(r.getLat_delivery(), r.getLong_delivery()));
            }
        }
        int n = locationIdSet.size();
        GoogleMapsQuery GMQ = new GoogleMapsQuery();
        long departure_time = (long) DateTimeUtils.dateTime2Int("2020-07-11 07:00:00");
        for (int i = 0; i < input.getDistances().length; i++) {
            try {
                DistanceElement de = input.getDistances()[i];
                int src = de.getSrcCode();
                int dest = de.getDestCode();
                Pair<Double, Double> srcLatLng = mLocationId2LatLng.get(src);
                Pair<Double, Double> destLatLng = mLocationId2LatLng.get(dest);
                Direction direction = GMQ.getDirection(srcLatLng.first, srcLatLng.second, destLatLng.first, destLatLng.second, "driving", departure_time);
                if (direction != null) {
                    System.out.println(i + " -> duration = " + direction.getDurations() + " distance = " + direction.getDistances());
                    de.setTravelTime(direction.getDurations());
                    de.setDistance(direction.getDistances());
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

    public static void forecaseTimeScaleToGGMap(String dataFile) {

    }

    public static double mean(ArrayList<Double> dataLst) {
        double m = 0;
        m = dataLst.stream().reduce(0.0, Double::sum);
        return m / dataLst.size();
    }
}
