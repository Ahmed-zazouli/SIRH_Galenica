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

public class Pole {

    boolean isAnomalieFound = false;
    private List<String> pole = Arrays.asList("_Pole");

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
               if(poleValue.equals("")){
                   //ANOMALIE
                   continue;
               }
               IStorageResource ressource = getOrCreateStorage(poleValue);
               for(String colonne : pole){
                   if(colonne.startsWith("_")){
                       Method method = this.getClass().getMethod(colonne,ArrayList.class,IStorageResource.class, Row.class,Cell.class);
                       method.invoke(this,alertAnomalis, ressource,row,row.getCell(pole.indexOf(colonne)));
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







   /* ClassListHelper helper = new ClassListHelper();

    public IStorageResource createStorage() {
        try{
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"Pole");
            return Modules.getWorkflowModule().createStorageResource(context,definition,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public List<String> getColumns() {
        return pole;
    }*/

    public boolean _Pole(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
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
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Pole","Merci de vérifier le pôle");
                    isAnomalieFound = true;
                }
            }else{
                instance.setValue("sys_Title",null);
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Pole","Merci d'ajouter le pôle");
                isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }

    public void save(IStorageResource instance){
        if(!isAnomalieFound){
            instance.save(Modules.getWorkflowModule().getSysadminContext());
        }
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
    


    public IStorageResource getOrCreateStorage(String pole) {
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"Pole");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            controller.addEqualsConstraint("sys_Title",pole);
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
