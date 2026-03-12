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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Service extends BaseAgent {
    private boolean isAnomalieFound = false;

    @Override
    protected void execute() {
        try{
            String filePath = "C://import//DISLOG//Service.xlsx";
            Columns columnsHelper = new Columns();
            List<String> columns = columnsHelper.service;
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
                IStorageResource service = createStorage("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Service");
                for(String colonne : columns){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,IStorageResource.class, Row.class, Cell.class);
                        method.invoke(this, service,row,row.getCell(columns.indexOf(colonne)));
                    }
                }
                if(!isAnomalieFound){
                    service.save(getWorkflowModule().getSysadminContext());
                }
                System.out.println();
            }

            // Close the workbook and FileInputStream
            workbook.close();
            fis.close();
        }catch (Exception e){
            e.printStackTrace();
        }
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
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("sys_Title",cellValue);
                IStorageResource pole =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Pole",filter);
                if(pole!=null){
                    instance.setValue("Pole",pole);
                }else{
                    isAnomalieFound = true;
                }
            }else{
                isAnomalieFound = true;
            }
        }else{
            isAnomalieFound=true;
        }
    }

    public void _Societe(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Pole",instance.getValue("Pole"));
                filter.put("sys_Title",cellValue);
                IStorageResource societe =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Societe",filter);
                if(societe!=null){
                    instance.setValue("Societe",societe);
                }else{
                    isAnomalieFound = true;
                }
            }else{
                isAnomalieFound = true;
            }
        }else{
            isAnomalieFound=true;
        }
    }

    public void _Departement(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Societe",instance.getValue("Societe"));
                filter.put("sys_Title",cellValue);
                IStorageResource departement =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","departementcommun",filter);
                if(departement!=null){
                    instance.setValue("Departement",departement);
                }else{
                    isAnomalieFound = true;
                }
            }else{
                isAnomalieFound = true;
            }
        }else{
            isAnomalieFound=true;
        }
    }

    public void _Activite(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Departement",instance.getValue("Departement"));
                filter.put("sys_Title",cellValue);
                IStorageResource activite =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Activite",filter);
                if(activite!=null){
                    instance.setValue("Activite",activite);
                }else{
                    isAnomalieFound = true;
                }
            }else{
                isAnomalieFound = true;
            }
        }else{
            isAnomalieFound=true;
        }
    }

    public void _Service(IStorageResource instance, Row row, Cell cell){
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

    IStorageResource handleStorageWithFilter(String organizationName, String projetName , String catalogName, String definitionName, HashMap<String,Object> filter){
        IStorageResource item = null;
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,organizationName);
            IProject projet = getProjectModule().getProject(context,projetName,organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,catalogName,ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,definitionName);
            IViewController controller = getWorkflowModule().getViewController(context,IResource.class);
            if(filter!=null && !filter.isEmpty()){
                for (Map.Entry<String, Object> entry : filter.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    controller.addEqualsConstraint(key,value);
                }
            }
            ArrayList<IStorageResource> items = (ArrayList<IStorageResource>) controller.evaluate(definition);
            if(items!=null && !items.isEmpty()){
                item = items.iterator().next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return item;
    }
}
