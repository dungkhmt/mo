package com.socolabs.mo.components.algorithms.spatialindex.gtree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.socolabs.mo.components.algorithms.nearestlocation.Pair;
import com.socolabs.mo.components.maps.GISMap;
import com.socolabs.mo.components.maps.distanceelementquery.DistanceElementQuery;
import com.socolabs.mo.components.maps.distanceelementquery.GeneralDistanceElement;
import com.socolabs.mo.components.maps.graphs.Graph;
import localsearch.domainspecific.vehiclerouting.vrp.utils.DateTimeUtils;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.Direction;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GTree {

    private final static int NB_FANOUT = 1 << 2;
    private final static int MAX_LEAF_SIZE = 1 << 7;
    private final static int NB_SHORTCUT = 1 << 20;

    private Graph g;

    public GTree(Graph g) {
        this.g = g;
    }

    public void buildTree() {

    }

    public static void main(String[] args) throws IOException {

    }

    public static void genVNMInput() throws IOException {
        GISMap gismap = new GISMap();
        DistanceElementQuery input = new DistanceElementQuery();
        FileInputStream file = new FileInputStream(new File("Data_dailyopt_092019.xlsx"));

        XSSFWorkbook workbook = new XSSFWorkbook(file);

        XSSFSheet sheet = workbook.getSheet("Đơn hàng");
        HashMap<String, Integer> mNameCol2Idx = new HashMap<>();
        HashMap<Integer, Pair<Double, Double>> mLocation2LatLng = new HashMap<>();
        for (Row row : sheet) {
            if (mNameCol2Idx.size() < 2) {
                for (int idx = 0; idx < row.getHeight(); idx++) {
                    Cell cell = row.getCell(idx);
                    try {
                        if (cell.getCellType() == CellType.STRING) {
                            String nameCol = cell.getStringCellValue();
                            if (nameCol.equals("SITE_NUM")) {
                                mNameCol2Idx.put("SITE_NUM", idx);
                            } else if (nameCol.equals("latlng")) {
                                mNameCol2Idx.put("latlng", idx);
                            }
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            } else {
                Cell siteCell = row.getCell(mNameCol2Idx.get("SITE_NUM"));
                Cell latlngCell = row.getCell(mNameCol2Idx.get("latlng"));
                int siteNum = (int) siteCell.getNumericCellValue();
                String[] latlng = latlngCell.getStringCellValue().split(",");
                mLocation2LatLng.put(siteNum, new Pair<>(Double.parseDouble(latlng[0]), Double.parseDouble(latlng[1])));
            }
        }
        mLocation2LatLng.put(1000000001, new Pair<> (10.8808226,106.6339871));
        mLocation2LatLng.put(1000000002, new Pair<> (10.8428112,106.7586882));
        mLocation2LatLng.put(1000000003, new Pair<> (10.9211894,106.8611579));
        mLocation2LatLng.put(1000000004, new Pair<> (10.8401923,106.7623801));

        GoogleMapsQuery GMQ = new GoogleMapsQuery();
        long departure_time = (long) DateTimeUtils.dateTime2Int("2020-08-24 08:00:00");
        GeneralDistanceElement[] elements = new GeneralDistanceElement[mLocation2LatLng.size() * mLocation2LatLng.size()];
        int i = 0;
        for (Map.Entry<Integer, Pair<Double, Double>> from : mLocation2LatLng.entrySet()) {
            for (Map.Entry<Integer, Pair<Double, Double>> to : mLocation2LatLng.entrySet()) {
                elements[i++] = new GeneralDistanceElement(
                        ""+from.getKey(),
                        from.getValue().first,
                        from.getValue().second,
                        ""+to.getKey(),
                        to.getValue().first,
                        to.getValue().second,
                        0,
                        0,
                        0
                );
            }
        }
        System.out.println("query");
        input.setElements(elements);
        gismap.calcDistanceElements(input);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter fw = new FileWriter("distance_elements.json");
        gson.toJson(input, fw);
        fw.close();
        System.out.println("Done");
    }
}
