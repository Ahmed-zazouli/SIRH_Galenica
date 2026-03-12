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

public class Etudes {
    ClassListHelper helper = new ClassListHelper();
    boolean isAnomalieFound = false;
    private List<String> etudes = Arrays.asList("_Collaborateur","_Diplome","_Ecole","_DateDeLObtentionDuDiplome");


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
                Cell collaborateurCINCell = row.getCell(0);
                String collaborateurCINValue = collaborateurCINCell!=null?collaborateurCINCell.getStringCellValue().trim():"";

                Cell diplomeCell = row.getCell(1);
                String diplomeValue = diplomeCell!=null?diplomeCell.getStringCellValue().trim():"";



                if(collaborateurCINValue.equals("") || diplomeValue.equals("")){
                    //ANOMALIE
                    continue;
                }
                IStorageResource ressource = getOrCreateStorage(collaborateurCINValue,diplomeValue);
                for(String colonne : etudes){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,ArrayList.class,IStorageResource.class, Row.class,Cell.class);
                        method.invoke(this,alertAnomalis, ressource,row,row.getCell(etudes.indexOf(colonne)));
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









    public IStorageResource getOrCreateStorage(String cin , String diplome) {
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"TableauEtudes");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            controller.addEqualsConstraint("Collaborateur.CIN",cin);
            controller.addEqualsConstraint("sys_Title",diplome);

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

    public IStorageResource createStorage() {
        try{
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"TableauEtudes");
            return Modules.getWorkflowModule().createStorageResource(context,definition,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public List<String> getColumns() {
        return etudes;
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

    public boolean _Collaborateur(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !((String) cellValue).trim().equals("")){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("CIN",cellValue);
                    IStorageResource collaborateur =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                    if(collaborateur!=null){
                        instance.setValue("Collaborateur",collaborateur);
                        instance.setValue("Salarie",collaborateur.getValue("Salarie"));
                    }else{
                        addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Collaborateur","Merci de vérifier le collaborateur");
                        isAnomalieFound = true;
                    }
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Collaborateur","Merci de vérifier le collaborateur");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Collaborateur","Merci d'ajouter le collaborateur");
                isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }



    public boolean _Diplome(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !((String) cellValue).trim().equals("")){
                    instance.setValue("sys_Title",cellValue);
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Diplôme","Merci de vérifier le diplôme");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Diplôme","Merci d'ajouter le diplôme");
                isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }

    public boolean _Ecole(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !((String) cellValue).trim().equals("")){
                    instance.setValue("Ecole",cellValue);
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Ecole","Merci de vérifier l'école");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Ecole","Merci d'ajouter l'école");
                isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }

    public boolean _DateDeLObtentionDuDiplome(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
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
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date de l'obtention du diplôme","Merci de vérifier la date de l'obtention du diplôme");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date de l'obtention du diplôme","Merci d'ajouter la date de l'obtention du diplôme");
                isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
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
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"TableauEtudes");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            List<String> columns =  Arrays.asList("_Collaborateur","*sys_Title","*Ecole");
            for (String column : columns) {
                Cell cell = row.getCell(columns.indexOf(column));
                String cellValue = cell != null ? cell.getStringCellValue().trim() : "";
                if (!cellValue.equals("")) {
                    if(column.startsWith("_")){
                        controller.addEqualsConstraint(column.replace("_",""+".CIN"),cellValue);
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
