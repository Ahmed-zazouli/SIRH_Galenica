package com.moovapps.Reprise.Helpers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.axemble.vdoc.sdk.ModulesFactory.getWorkflowModule;

public class Domaine {
    ClassListHelper helper = new ClassListHelper();
    boolean isAnomalieFound = false;
    private List<String> domaine = Arrays.asList("_Domaine");



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
                Cell domaineCell = row.getCell(0);
                String domaineValue = domaineCell!=null?domaineCell.getStringCellValue().trim():"";
                if(domaineValue.equals("")){
                    //ANOMALIE
                    continue;
                }
                IStorageResource ressource = getOrCreateStorage(domaineValue);
                for(String colonne : domaine){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,ArrayList.class,IStorageResource.class, Row.class,Cell.class);
                        method.invoke(this,alertAnomalis, ressource,row,row.getCell(domaine.indexOf(colonne)));
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









    public IStorageResource getOrCreateStorage(String domaine) {
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"Domaine");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            controller.addEqualsConstraint("sys_Title",domaine);
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
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"Domaine");
            return Modules.getWorkflowModule().createStorageResource(context,definition,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public List<String> getColumns() {
        return domaine;
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

    public boolean _Domaine(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
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
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Domaine","Merci de vérifier le domaine");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Domaine","Merci d'ajouter le domaine");
                isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }


    public IStorageResource getOrCreateStorage(Row row) {
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"Domaine");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            List<String> columns = Arrays.asList("*sys_Title");
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
