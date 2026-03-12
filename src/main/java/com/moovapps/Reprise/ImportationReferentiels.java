package com.moovapps.Reprise;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.Reprise.Helpers.ClassListHelper;
import com.moovapps.Reprise.Helpers.Collaborateur;
import com.moovapps.Reprise.Helpers.LienParente;
import com.moovapps.Reprise.Referentiels.Columns;
import com.moovapps.Reprise.Referentiels.Pole;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ImportationReferentiels extends BaseDocumentExtension {
    boolean isAnomalieFound = false;

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("Importer")){
            Importer();
            if(isAnomalieFound){
                return false;
            }
        }
        return super.onBeforeSubmit(action);
    }

    public void Importer(){
        try {
            ClassListHelper helper = new ClassListHelper();
            // Specify the path to your Excel file
            ArrayList<IAttachment> tmp = (ArrayList<IAttachment>) getWorkflowInstance().getValue("FichierAImporter");
            IAttachment tmpAttachement = null;
            if (tmp != null && tmp.size() > 0) {
                tmpAttachement = tmp.get(0);
            }
            File file = null;
            if (tmpAttachement != null) {
                try {
                    file = new File("c://Temp_Moovapps//" + tmpAttachement.getName());

                    FileUtils.writeByteArrayToFile(file, tmpAttachement.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(file==null){
                return;
            }

            // Create a FileInputStream to read the Excel file
            FileInputStream fis = new FileInputStream(file);

            // Create a workbook object to represent the Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            if(!verifySheetsNames(workbook)){

                return;
            }
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) { // Loop Over sheets
                if(helper.classList==null  || helper.classList.size()<=i || helper.classList.get(i)==null){
                    continue;
                }
                Method getStringListMethod = helper.classList.get(i)!=null? helper.classList.get(i).getMethod("getColumns")!=null? helper.classList.get(i).getMethod("getColumns"):null:null;
                if(getStringListMethod==null)continue;
                List<String> columns = (List<String>) getStringListMethod.invoke(helper.classList.get(i).newInstance());
                Sheet sheet = workbook.getSheetAt(i);
                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // Loop OverRows
                    isAnomalieFound = false;
                    Row row = sheet.getRow(rowIndex);
                    if(rowIndex==0){
                        //Verify maquette
                        //if false break;
                        continue;
                    }


                    //Create pole storage
                    Method createStorageMethode =  helper.classList.get(i).getMethod("createStorage");
                    IStorageResource item = (IStorageResource) createStorageMethode.invoke(helper.classList.get(i).newInstance());


                    for(String colonne : columns){
                        if(colonne.startsWith("_")){
                            Method method = helper.classList.get(i).getMethod(colonne,IWorkflowInstance.class,IStorageResource.class, Row.class, Cell.class);
                            Object isAnomalieFoundObject =  method.invoke(helper.classList.get(i).newInstance(),getWorkflowInstance(), item,row,row.getCell(columns.indexOf(colonne)));
                            //if(isAnomalieFound==false){
                                isAnomalieFound = (boolean) isAnomalieFoundObject;
                            //}
                        }
                    }
                   // Method saveMethod = helper.classList.get(i).getMethod("save",IStorageResource.class);
                   // saveMethod.invoke(helper.classList.get(i).newInstance(),item);
                    //isNew(item,columns) // THE columns should be the sysNames || declare isNew on the helper class for each
                    //if(!isNew) DONT SAVE
                    if(!isAnomalieFound){

                        if(helper.classList.get(i) == LienParente.class){
                            Method method = helper.classList.get(i).getMethod("IncreaseNombreEnfant",IStorageResource.class);
                            method.invoke(helper.classList.get(i).newInstance(), item);
                        }

                        item.save(getWorkflowModule().getSysadminContext());
                    }
                    System.out.println();
                }
            }


            workbook.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    List<String> sheets = Arrays.asList("Pole","Societe","Departement","Activite","Service","Metier","Fonction","Grade","Circuit","Profil","LienParente","Etudes","Domaine","FamilleCompetence","SousFamilleCompetence","Competence","NiveauCompetence","Theme","SousTheme");
    ArrayList<JSONObject>  excelAnomaliesWorkFlowImportRefs = new ArrayList();
    public void ImporterV2(){
        try{
            ArrayList<IAttachment> tmp = (ArrayList<IAttachment>) getWorkflowInstance().getValue("FichierAImporter");
            IAttachment tmpAttachement = null;
            if (tmp != null && tmp.size() > 0) {
                tmpAttachement = tmp.get(0);
            }
            File file = null;
            if (tmpAttachement != null) {
                try {
                    file = new File("c://Temp_Moovapps//" + tmpAttachement.getName());

                    FileUtils.writeByteArrayToFile(file, tmpAttachement.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(file==null){
                return;
            }
            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            if(!verifySheetsNames(workbook)){
                return;
            }
            //int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < sheets.size(); i++){
                Sheet sheet = workbook.getSheetAt(i);
                Class<?> sheetClass = Class.forName(sheets.get(i));
                Constructor<?> constructor = sheetClass.getDeclaredConstructor(ArrayList.class);
                Object instance = constructor.newInstance(excelAnomaliesWorkFlowImportRefs);
                Method method = instance.getClass().getMethod("CreateOrUpdateStorages",Sheet.class,ArrayList.class);
                method.invoke(instance, sheet,excelAnomaliesWorkFlowImportRefs);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


     public boolean verifySheetsNames(XSSFWorkbook workbook){
        if(workbook.getSheetName(0).equals("Pôle")){
            return true;
        }
        return false;
     }


}
