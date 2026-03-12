package com.moovapps.ibb.rh.Cummon.Helpers;

import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.axemble.vdoc.sdk.Modules.getDirectoryModule;
import static com.axemble.vdoc.sdk.Modules.getWorkflowModule;


public class ExcelWriter {

    public String GenerateExcel(ArrayList<JSONObject> anomalies,String outPutFilePath){
        File file = new File(outPutFilePath);
        //<div>
        ArrayList<String> anomalieFields = new ArrayList<>(Arrays.asList("Ligne","Cordonnée","Anomalie","Alert"));
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
        cell3.setCellValue("Alert");
        for(JSONObject anomalie : anomalies){
            Row row = sheet.createRow(rowIndex);
            for(int i=0;i<4;i++){
                Cell cell = row.createCell(i);
                if(anomalie.get(anomalieFields.get(i))==null){
                    continue;
                }
                cell.setCellValue(anomalie.get(anomalieFields.get(i)).toString());
            }
            rowIndex ++;
        }
        try (FileOutputStream fileOut = new FileOutputStream(outPutFilePath)) {
            workbook.write(fileOut);
            return outPutFilePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String ExcelToPdf(String path,String outPutFilePath){
        String destinationPath = outPutFilePath ;
        try{
            Workbook workbook = new XSSFWorkbook(path);
            // Get the first sheet
            Sheet sheet = workbook.getSheetAt(0);
            // Create a new PDF document
            Document pdf = new Document();
            PdfWriter.getInstance(pdf, new FileOutputStream(destinationPath));
            pdf.open();

            // Iterate over the rows and cells of the sheet
            for (Row row : sheet) {
                for (Cell cell : row) {
                    // Get the cell value as a string
                    String value = cell.getStringCellValue();
                    // Create a new paragraph with the cell value
                    Paragraph p = new Paragraph(value, FontFactory.getFont(FontFactory.COURIER, 12));
                    // Add the paragraph to the PDF document
                    pdf.add(p);
                }
            }
            pdf.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return destinationPath;
    }

    public void SetPieceJointe(String pdfPath, IWorkflowInstance instance,String fieldName){
        File file = new File(pdfPath);
        ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
        IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
        tmpAttachementCollection.add(tmpJournalPaieAttachment);
        instance.setValue(fieldName, tmpAttachementCollection);
        instance.save(getWorkflowModule().getSysadminContext());
    }
}
