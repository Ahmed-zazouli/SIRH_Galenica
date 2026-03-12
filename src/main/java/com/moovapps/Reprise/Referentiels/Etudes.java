package com.moovapps.Reprise.Referentiels;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Etudes extends BaseAgent {
    private boolean isAnomalieFound = false;

    @Override
    protected void execute() {
        try{
            String filePath = "C://import//DISLOG//Maquette reprise des données.xlsx";
            Columns columnsHelper = new Columns();
            List<String> columns = columnsHelper.etudes;
            // Create a FileInputStream to read the Excel file
            FileInputStream fis = new FileInputStream(new File(filePath));

            // Create a workbook object to represent the Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(fis);

            // Get the first sheet in the workbook
            // (You can modify this to loop through all sheets if needed)
            Sheet sheet = workbook.getSheetAt(15);

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
                IStorageResource etudes = createStorage("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","TableauEtudes");
                for(String colonne : columns){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,IStorageResource.class, Row.class, Cell.class);
                        method.invoke(this, etudes,row,row.getCell(columns.indexOf(colonne)));
                    }
                }
                if(!isAnomalieFound){
                    etudes.save(getWorkflowModule().getSysadminContext());
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

    public void _Collaborateur(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("CIN",cellValue);
                IStorageResource collaborateur =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                if(collaborateur!=null){
                    instance.setValue("Collaborateur",collaborateur);
                    instance.setValue("Salarie",collaborateur.getValue("Salarie"));
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



    public void _Diplome(IStorageResource instance, Row row, Cell cell){
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

    public void _Ecole(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("Ecole",cellValue);
            }else{
               // isAnomalieFound = true;
            }
        }else{
           // isAnomalieFound=true;
        }
    }

    public void _DateDeLObtentionDuDiplome(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    String[] dateFormats = {"dd/MM/yyyy", "dd-MM-yyyy", "dd_MM_yyyy"};
                    for (String dateFormat : dateFormats) {
                        try{
                            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                            cellValue =  sdf.parse((String) cellValue);
                            if(cellValue!=null){
                                break;
                            }
                        }catch (ParseException e){
                            e.printStackTrace();
                        }
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getDateCellValue();
                }
                if(cellValue!=null){
                    instance.setValue("DateDeLObtentionDuDiplome",cellValue);
                }else{
                    //isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound=true;
            }
        }catch (Exception e){
            //isAnomalieFound=true;
            e.printStackTrace();
        }
    }

    Object handleList(Object item,HashMap<String, String> possibleValues){
        if(possibleValues.get(item)!=null){
            return possibleValues.get(item);
        }
        return null;
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
