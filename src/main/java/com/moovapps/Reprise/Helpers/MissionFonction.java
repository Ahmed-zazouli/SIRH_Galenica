package com.moovapps.Reprise.Helpers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.axemble.vdoc.sdk.ModulesFactory.getWorkflowModule;

public class MissionFonction {
    ClassListHelper helper = new ClassListHelper();
    boolean isAnomalieFound = false;
    private List<String> missionFonction = Arrays.asList("_Pole","_Societe","_Fonction","_Mission","_Details");

    public void CreateOrUpdateStorages(Sheet sheet, ArrayList<JSONObject> alertAnomalis){
        try{
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                isAnomalieFound = false;
                Row row = sheet.getRow(rowIndex);
                if(rowIndex==0){
                    //Verify maquette
                    //if false break;
                    continue;
                }
                Cell poleCell = row.getCell(0);
                String poleValue = poleCell!=null?poleCell.getStringCellValue().trim():"";

                Cell societeCell = row.getCell(1);
                String societeValue = societeCell!=null?societeCell.getStringCellValue().trim():"";

                Cell fonctionCell = row.getCell(2);
                String fonctionValue = fonctionCell!=null?fonctionCell.getStringCellValue().trim():"";



                if(poleValue.equals("") || societeValue.equals("") || fonctionValue.equals("")){
                    //ANOMALIE
                    continue;
                }
                IStorageResource ressource = getOrCreateStorage(poleValue,societeValue,fonctionValue);
                for(String colonne : missionFonction){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,ArrayList.class,IStorageResource.class, Row.class,Cell.class);
                        method.invoke(this,alertAnomalis, ressource,row,row.getCell(missionFonction.indexOf(colonne)));
                    }
                }
                if(!isAnomalieFound){
                    ressource.save(getWorkflowModule().getSysadminContext());
                }
                System.out.println();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }










    public IStorageResource getOrCreateStorage(String pole , String societe,String fonction) {
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"MissionFonctionPoste");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            controller.addEqualsConstraint("Pole.sys_Title",pole);
            controller.addEqualsConstraint("Societe.sys_Title",societe);
            controller.addEqualsConstraint("Fonction.sys_Title",fonction);

            ArrayList<IStorageResource> items = ( ArrayList<IStorageResource>) controller.evaluate(definition);
            if(items!=null && !items.isEmpty()){
                return items.iterator().next();
            }else{
                return Modules.getWorkflowModule().createStorageResource(context,definition,"");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public void addAnomalie(ArrayList<JSONObject> alertAnomalis,String feuille , String ligne , String cordonnee,String alerte){
        JSONObject anomalieDetails = new JSONObject();
        anomalieDetails.put("Feuille",feuille);
        anomalieDetails.put("Ligne",ligne);
        anomalieDetails.put("Cordonnee",cordonnee);
        anomalieDetails.put("Anomalie",alerte);
        alertAnomalis.add(anomalieDetails);
    }

    public void addAlerte(ArrayList<JSONObject> alertAnomalis,String feuille , String ligne , String cordonnee,String alerte){
        JSONObject anomalieDetails = new JSONObject();
        anomalieDetails.put("Feuille",feuille);
        anomalieDetails.put("Ligne",ligne);
        anomalieDetails.put("Cordonnee",cordonnee);
        anomalieDetails.put("Alerte",alerte);
        alertAnomalis.add(anomalieDetails);
    }








    public IStorageResource createStorage() {
        try{
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"MissionFonctionPoste");
            return Modules.getWorkflowModule().createStorageResource(context,definition,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public List<String> getColumns() {
        return missionFonction;
    }


    public boolean _Pole(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("sys_Title",cellValue);
                    IStorageResource pole =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Pole",filter);
                    if(pole!=null){
                        instance.setValue("Pole",pole);
                    }else{
                        addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Pole","Merci de vérifier le pôle");
                        isAnomalieFound = true;
                    }
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Pole","Merci de vérifier le pôle");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Pole","Merci d'ajouter le pôle");
                isAnomalieFound=true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _Societe(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("Pole",instance.getValue("Pole"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource societe =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Societe",filter);
                    if(societe!=null){
                        instance.setValue("Societe",societe);
                    }else{
                        addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Société","Merci de vérifier la société");
                        isAnomalieFound = true;
                    }
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Société","Merci de vérifier la société");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Société","Merci d'ajouter la société");
                isAnomalieFound=true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _Fonction(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("Societe",instance.getValue("Societe"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource fonction =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","fonctions",filter);
                    if(fonction!=null){
                        instance.setValue("Fonction",fonction);
                    }else{
                        addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Fonction","Merci de vérifier la fonction");
                        isAnomalieFound = true;
                    }
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Fonction","Merci de vérifier la fonction");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Fonction","Merci d'ajouter la fonction");
                isAnomalieFound=true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }



    public boolean _Mission(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    instance.setValue("sys_Title",cellValue);
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Mission","Merci de vérifier la mission");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Mission","Merci d'ajouter la mission");
                isAnomalieFound=true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;

    }




    public boolean _Details(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    instance.setValue("Details",cellValue);
                }else{
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Détails","Merci de vérifier le champ détails");
                    //  isAnomalieFound = true;
                }
            }else{
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Détails","Merci d'ajouter le champ détails");
                //  isAnomalieFound=true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }







    Boolean handleBoolean(Cell excelCell){
        String cellValue = null;
        if (excelCell.getCellType() == CellType.STRING) {
            // Handle numeric cells
            cellValue = excelCell.getStringCellValue().trim().toLowerCase();
            if(cellValue.equals("oui")){
                return true;
            }else if(cellValue.equals("non")){
                return false;
            }
        }
        return null;
    }



    IStorageResource handleStorageWithFilter(String organizationName, String projetName , String catalogName, String definitionName, HashMap<String,Object> filter){
        IStorageResource item = null;
        try{
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,organizationName);
            IProject projet = Modules.getProjectModule().getProject(context,projetName,organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,catalogName,ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,definitionName);
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
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

    Object handleList(Object item,HashMap<String, String> possibleValues){
        if(possibleValues.get(item)!=null){
            return possibleValues.get(item);
        }
        return null;
    }

    public IStorageResource getOrCreateStorage(Row row) {
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"MissionFonctionPoste");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            List<String> columns =  Arrays.asList("_Pole","_Societe","_Fonction","*sys_Title");
            for (String column : columns) {
                Cell cell = row.getCell(columns.indexOf(column));
                String cellValue = cell != null ? cell.getStringCellValue().trim() : "";
                if (!cellValue.equals("")) {
                    if(column.startsWith("_")){
                        controller.addEqualsConstraint(column.replace("_",""+".sys_Title"),cellValue);
                    }else{
                        controller.addEqualsConstraint(column.replace("*",""),cellValue);
                    }

                }
            }
            ArrayList<IStorageResource> items = ( ArrayList<IStorageResource>) controller.evaluate(definition);
            if(items!=null && !items.isEmpty()){
                return items.iterator().next();
            }else{
                return Modules.getWorkflowModule().createStorageResource(context,definition,"");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
