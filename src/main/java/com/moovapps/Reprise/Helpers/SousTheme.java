package com.moovapps.Reprise.Helpers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.lang.reflect.Method;
import java.util.*;

import static com.axemble.vdoc.sdk.ModulesFactory.getWorkflowModule;

public class SousTheme {
    boolean isAnomalieFound = false;
    private List<String> sousTheme = Arrays.asList("_Theme","_SousTheme");

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
                Cell themeCell = row.getCell(0);
                String themeValue = themeCell!=null?themeCell.getStringCellValue().trim():"";

                Cell sousThemeCell = row.getCell(0);
                String sousThemeValue = sousThemeCell!=null?sousThemeCell.getStringCellValue().trim():"";
                if(themeValue.equals("") || sousThemeValue.equals("")){
                    //ANOMALIE
                    continue;
                }
                IStorageResource ressource = getOrCreateStorage(themeValue,sousThemeValue);
                for(String colonne : sousTheme){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,ArrayList.class,IStorageResource.class, Row.class,Cell.class);
                        method.invoke(this,alertAnomalis, ressource,row,row.getCell(sousTheme.indexOf(colonne)));
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

    public boolean _Theme(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
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
                    IStorageResource pole =  handleStorageWithFilter("DefaultOrganization","Formation","Referentiels","Theme",filter);
                    if(pole!=null){
                        instance.setValue("Theme",pole);
                    }else{
                        addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Théme","Merci de vérifier le théme");
                        isAnomalieFound = true;
                    }
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Théme","Merci de vérifier le théme");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Théme","Merci d'ajouter le théme");
                isAnomalieFound=true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }


    public boolean _SousTheme(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
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
                    instance.setValue("sys_Title",null);
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Théme","Merci de vérifier le sous théme");
                    isAnomalieFound = true;
                }
            }else{
                instance.setValue("sys_Title",null);
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Théme","Merci d'ajouter le sous théme");
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



    public IStorageResource getOrCreateStorage(String theme,String sousTheme) {
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"Formation",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"Referentiels",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"SousTheme");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            controller.addEqualsConstraint("sys_Title",sousTheme);
            controller.addEqualsConstraint("Theme.sys_Title",theme);
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
