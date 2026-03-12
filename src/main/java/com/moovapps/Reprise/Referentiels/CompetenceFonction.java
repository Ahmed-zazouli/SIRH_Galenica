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

public class CompetenceFonction extends BaseAgent {
    private boolean isAnomalieFound = false;
    @Override
    protected void execute() {
        try{
            String filePath = "C://import//DISLOG//Maquette reprise des données.xlsx";
            Columns columnsHelper = new Columns();
            List<String> columns = columnsHelper.compenceFonction;
            // Create a FileInputStream to read the Excel file
            FileInputStream fis = new FileInputStream(new File(filePath));

            // Create a workbook object to represent the Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(fis);

            // Get the first sheet in the workbook
            // (You can modify this to loop through all sheets if needed)
            Sheet sheet = workbook.getSheetAt(10);

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
                IStorageResource competenceFonction = createStorage("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","CompetenceFonctionPoste");
                for(String colonne : columns){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,IStorageResource.class, Row.class, Cell.class);
                        method.invoke(this, competenceFonction,row,row.getCell(columns.indexOf(colonne)));
                    }
                }
                if(!isAnomalieFound){
                    competenceFonction.save(getWorkflowModule().getSysadminContext());
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

    public void _Fonction(IStorageResource instance, Row row, Cell cell){
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

                IStorageResource fonction =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","fonctions",filter);
                if(fonction!=null){
                    instance.setValue("Fonction",fonction);
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

    public void _FamilleCompetence(IStorageResource instance, Row row, Cell cell){
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

                IStorageResource familleCompetence =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FamilleCompetence",filter);
                if(familleCompetence!=null){
                    instance.setValue("FamilleCompetence",familleCompetence);
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

    public void _SousFamilleCompetence(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("FamilleCompetence",instance.getValue("FamilleCompetence"));
                filter.put("sys_Title",cellValue);

                IStorageResource sousFamilleCompetence =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","SousFamilleCompetence",filter);
                if(sousFamilleCompetence!=null){
                    instance.setValue("SousFamilleCompetence",sousFamilleCompetence);
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

    public void _Competence(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("SousFamilleCompetence",instance.getValue("SousFamilleCompetence"));
                filter.put("sys_Title",cellValue);

                IStorageResource competence =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Competence",filter);
                if(competence!=null){
                    instance.setValue("Competence",competence);
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

    public void _Poids(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                long poids = handleNumber(cellValue);
                if(poids!= -1){
                    instance.setValue("Poids",poids);
                }else{
                    isAnomalieFound=true;
                }

            }else{
                isAnomalieFound = true;
            }
        }else{
            isAnomalieFound=true;
        }
    }

    private long handleNumber(Object cellValue) {
        if(cellValue instanceof Number){
            return Math.round(((Number)cellValue).doubleValue());
        }
        return -1;
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
