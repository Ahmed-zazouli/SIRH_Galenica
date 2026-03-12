package com.moovapps.Reprise.Helpers;

import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;

public class ExcelWriter {

    public String GenerateExcel(ArrayList<JSONObject> anomalies, String outPutFilePath){
        //<div>
       // ArrayList<String> anomalieFields = new ArrayList<>(Arrays.asList("Ligne","Cordonnée","Anomalie","Alert"));
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        int rowIndex = 1;
        Row firstRow = sheet.createRow(0);
        Cell firstCell = firstRow.createCell(0);
        firstCell.setCellValue("Ligne");
        Cell cell1 = firstRow.createCell(1);
        cell1.setCellValue("Cordonnée");
        Cell cell2 = firstRow.createCell(2);
        cell2.setCellValue("Anomalie");
        Cell cell3 = firstRow.createCell(3);
        cell3.setCellValue("Alerte");
        for(JSONObject anomalie : anomalies){
            Row row = sheet.createRow(rowIndex);
                Cell cell = row.createCell(0);
                if(anomalie.get("Row")!=null){
                    cell.setCellValue((String) anomalie.get("Row"));

                }

            Cell cell22 = row.createCell(1);
            if(anomalie.get("Column")!=null){
                cell22.setCellValue((String) anomalie.get("Column"));
            }

            String anomalieOrAlert = (String) anomalie.get("Type");
            if(anomalieOrAlert.equals("Anomalie")){
                Cell cell33 = row.createCell(2);
                if(anomalie.get("Message")!=null){
                    cell33.setCellValue((String) anomalie.get("Message"));
                }
            }else{
                Cell cell44 = row.createCell(3);
                if(anomalie.get("Message")!=null){
                    cell44.setCellValue((String) anomalie.get("Message"));
                }
            }
            rowIndex ++;
        }

        Path filePath = Paths.get(outPutFilePath);

        try{
            Files.createDirectories(filePath.getParent());
            if(!Files.exists(filePath)){
                Files.createFile(filePath);
            }

            OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            workbook.write(outputStream);
            outputStream.close();
            return outPutFilePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String GenerateRefExcel(ArrayList<JSONObject> anomalies, String outPutFilePath){
        //<div>
        // ArrayList<String> anomalieFields = new ArrayList<>(Arrays.asList("Ligne","Cordonnée","Anomalie","Alert"));
        /*

        anomalieDetails.put("Feuille",feuille);
        anomalieDetails.put("Ligne",ligne);
        anomalieDetails.put("Cordonnee",cordonnee);
        anomalieDetails.put("Anomalie",alerte);
         */
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        int rowIndex = 1;
        Row firstRow = sheet.createRow(0);
        Cell sheetCell = firstRow.createCell(0);
        sheetCell.setCellValue("Feuille");

        Cell firstCell = firstRow.createCell(1);
        firstCell.setCellValue("Ligne");
        Cell cell1 = firstRow.createCell(2);
        cell1.setCellValue("Cordonnée");
        Cell cell2 = firstRow.createCell(3);
        cell2.setCellValue("Anomalie");
        Cell cell3 = firstRow.createCell(4);
        cell3.setCellValue("Alerte");
        for(JSONObject anomalie : anomalies){
            Row row = sheet.createRow(rowIndex);
            Cell cell = row.createCell(0);
            if(anomalie.get("Feuille")!=null){
                cell.setCellValue((String) anomalie.get("Feuille"));
            }

            Cell cell22 = row.createCell(1);
            if(anomalie.get("Ligne")!=null){
                cell22.setCellValue((String) anomalie.get("Ligne"));

            }

            Cell cell222 = row.createCell(2);
            if(anomalie.get("Cordonnée")!=null){
                cell222.setCellValue((String) anomalie.get("Cordonnée"));

            }
            Cell cell33 = row.createCell(3);

            if(anomalie.get("Anomalie")!=null){
                cell33.setCellValue((String) anomalie.get("Anomalie"));
            }

            Cell cell44 = row.createCell(4);

            if(anomalie.get("Alerte")!=null){
                cell44.setCellValue((String) anomalie.get("Alerte"));
            }
            rowIndex ++;
        }

        Path filePath = Paths.get(outPutFilePath);

        try{
            Files.createDirectories(filePath.getParent());
            if(!Files.exists(filePath)){
                Files.createFile(filePath);
            }

            OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            workbook.write(outputStream);
            outputStream.close();
            return outPutFilePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
