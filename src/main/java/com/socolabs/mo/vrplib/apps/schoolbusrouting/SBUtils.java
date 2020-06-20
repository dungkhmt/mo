package com.socolabs.mo.vrplib.apps.schoolbusrouting;

import com.socolabs.mo.vrplib.core.VRPPoint;
import com.socolabs.mo.vrplib.core.VRPRoute;
import com.socolabs.mo.vrplib.core.VRPVarRoutes;
import com.socolabs.mo.vrplib.entities.IDistanceManager;
import com.socolabs.mo.vrplib.invariants.AccumulatedWeightPoints;
import com.socolabs.mo.vrplib.invariants.RevAccumulatedWeightPoints;
import localsearch.domainspecific.vehiclerouting.apps.schoolbusrouting.model.Utils;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
        for (VRPRoute r : vr.getAllRoutes()) {
            if (r.getNbPoints() == 0) {
                continue;
            }
            SchoolBusRoute sbR = (SchoolBusRoute) r;
            for (VRPPoint p = r.getStartPoint(); p != null; p = p.getNext()) {
                SchoolBusPickupPoint sbP = (SchoolBusPickupPoint) p;
                row = reqSheet.createRow(++i);
                cell = row.createCell(0);
                cell.setCellValue(sbR.getTruckCode());
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
                cell.setCellValue(sbP.size());
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
}
