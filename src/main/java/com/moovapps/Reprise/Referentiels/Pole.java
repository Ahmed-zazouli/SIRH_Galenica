package com.moovapps.Reprise.Referentiels;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.List;


public class Pole extends BaseAgent {
private boolean isAnomalieFound = false;
    @Override
    protected void execute() {
        try {
            // Specify the path to your Excel file
            String filePath = "C://import//DISLOG//Maquette reprise des données.xlsx";
            Columns columnsHelper = new Columns();
            List<String> columns = columnsHelper.pole;
            // Create a FileInputStream to read the Excel file
            FileInputStream fis = new FileInputStream(new File(filePath));

            // Create a workbook object to represent the Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(fis);

            // Get the first sheet in the workbook
            // (You can modify this to loop through all sheets if needed)
           Sheet sheet = workbook.getSheetAt(0);

            // Iterate through each row in the sheet
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                isAnomalieFound = false;
                Row row = sheet.getRow(rowIndex);
                if(rowIndex==0){
                    //Verify maquette
                    //if false break;
                    continue;
                }

                //Create pole storage
                IStorageResource pole = createStorage("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Pole");
                for(String colonne : columns){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,IStorageResource.class, Row.class,Cell.class);
                        method.invoke(this, pole,row,row.getCell(columns.indexOf(colonne)));
                    }
                }
                if(!isAnomalieFound){
                    pole.save(getWorkflowModule().getSysadminContext());
                }
                System.out.println();
            }

            // Close the workbook and FileInputStream
            workbook.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IStorageResource createStorage(String organizationName ,  String projetName, String catalogName, String definitionName) {
         try{
             IContext context = getWorkflowModule().getSysadminContext();
             IOrganization organization = getDirectoryModule().getOrganization(context,organizationName);
             IProject projet = getProjectModule().getProject(context,projetName,organization);
             ICatalog catalog = getWorkflowModule().getCatalog(context,catalogName,ICatalog.IType.STORAGE,projet);
             IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,definitionName);
             return getWorkflowModule().createStorageResource(context,definition,"");
         }catch (Exception e){
             e.printStackTrace();
         }
        return null;
    }

    public void _Pole(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("sys_Title",cellValue);
            }else{
                isAnomalieFound = true;
            }
        }else{
            isAnomalieFound=true;
        }
    }
}
