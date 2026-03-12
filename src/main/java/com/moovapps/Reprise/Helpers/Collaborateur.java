package com.moovapps.Reprise.Helpers;

import com.axemble.vdoc.core.helpers.PasswordHelper;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.impl.ProcessWorkflowModule;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.Reprise.Referentiels.Columns;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.lang.reflect.Method;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.axemble.vdoc.sdk.ModulesFactory.*;

public class Collaborateur {
    HashMap<IStorageResource,String> SuppleantMap = null;

    HashMap<IStorageResource,String> EvaluateurMap = new HashMap<>();
    HashMap<IStorageResource,String> N1Map = new HashMap<>();
    ArrayList<JSONObject> excelAnomaliesalertAnomalisUser = new ArrayList<>();
    int counter = 0;



    public boolean isAnomalieFound = false;
    boolean isUpdate = false;
    HashMap<String,String> groupesMapper = new HashMap<String,String>();
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);
    HashMap<String,String> secretGroupesMapper = new HashMap<>();

    public  Collaborateur(){}

    public Collaborateur(HashMap<IStorageResource,String> SuppleantMap ,HashMap<IStorageResource,String> EvaluateurMap,HashMap<IStorageResource,String> N1Map ,  ArrayList<JSONObject>  excelAnomaliesalertAnomalisUser){
        this.SuppleantMap = SuppleantMap;
        this.EvaluateurMap = EvaluateurMap;
        this.N1Map = N1Map;
        this.excelAnomaliesalertAnomalisUser = excelAnomaliesalertAnomalisUser;

        groupesMapper.put("Salarié","Salarie");
        groupesMapper.put("SALARIE","Salarie");
        groupesMapper.put("Manager","Manager");
        groupesMapper.put("MANAGER","Manager");
        groupesMapper.put("RH","RH");
        groupesMapper.put("ADMIN","BPO");
        groupesMapper.put("Responsables DEV","ResponsableSDEV");
        groupesMapper.put("CODIR","CODIR");

        secretGroupesMapper.put("RH","RH");
        secretGroupesMapper.put("Manager","Manager");
        secretGroupesMapper.put("MANAGER","Manager");
        secretGroupesMapper.put("Responsables DEV","ResponsableSDEV");
    }
    ClassListHelper helper = new ClassListHelper(excelAnomaliesalertAnomalisUser);

    public void CreateOrUpdateStorages(Sheet sheet, ArrayList<JSONObject> alertAnomalis){
        try{
            this.excelAnomaliesalertAnomalisUser = alertAnomalis;
            groupesMapper.put("Salarié","Salarie");
            groupesMapper.put("SALARIE","Salarie");
            groupesMapper.put("Manager","Manager");
            groupesMapper.put("MANAGER","Manager");
            groupesMapper.put("RH","RH");
            groupesMapper.put("ADMIN","BPO");
            groupesMapper.put("Responsables DEV","ResponsableSDEV");
            groupesMapper.put("CODIR","CODIR");

            secretGroupesMapper.put("RH","RH");
            secretGroupesMapper.put("Manager","Manager");
            secretGroupesMapper.put("MANAGER","Manager");
            secretGroupesMapper.put("Responsables DEV","ResponsableSDEV");

            for (int rowIndex = 2; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                isAnomalieFound = false;
                Row row = sheet.getRow(rowIndex);
                if(rowIndex==0){
                    //Verify maquette
                    //if false break;
                    continue;
                }
                Cell cin = row.getCell(8);
                String cinValue = "";
                if(cin !=null){
                    cin.setCellType(CellType.STRING);
                    cinValue=  cin.getStringCellValue().trim();
                }
                if(cinValue.equals("")){
                    //ANOMALIE
                    continue;
                }
                IStorageResource ressource = createOrGetStorage(cinValue);
                Columns columnsHelper = new Columns();
                List<String> columns = columnsHelper.collaborateur;
                for(String colonne : columns){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,IStorageResource.class, Row.class,Cell.class);
                        method.invoke(this, ressource,row,row.getCell(columns.indexOf(colonne)));
                    }
                }
                if(!isAnomalieFound){
                    addToRefAndCreateUser(ressource);
                }

                System.out.println();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private IStorageResource createOrGetStorage(String CIN) {
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",getDirectoryModule().getOrganization(context, "DefaultOrganization"));
            ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
            IResourceDefinition resourceDefinition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("CIN",CIN);
            ArrayList<IStorageResource> fiches = (ArrayList<IStorageResource>) controller.evaluate(resourceDefinition);
            if(fiches!=null && !fiches.isEmpty()){
                return fiches.iterator().next();
            }
            return Modules.getWorkflowModule().createStorageResource(context,resourceDefinition,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void addAnomalie(String sheetName,String row,String column , String message){
        JSONObject anomalie = new JSONObject();
        anomalie.put("Type","Anomalie");
        anomalie.put("Feuille",sheetName);
        anomalie.put("Ligne",row);
        anomalie.put("Cordonnee",column);
        anomalie.put("Anomalie",message);
        excelAnomaliesalertAnomalisUser.add(anomalie);
    }

    public void addAlerte(String sheetName,String row,String column , String message){
        JSONObject anomalie = new JSONObject();
        anomalie.put("Type","Alerte");
        anomalie.put("Feuille",sheetName);
        anomalie.put("Ligne",row);
        anomalie.put("Cordonnee",column);
        anomalie.put("Alerte",message);
        excelAnomaliesalertAnomalisUser.add(anomalie);
    }


    public boolean _Pole(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("sys_Title",cellValue);
                    IStorageResource pole =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Pole",filter);
                    instance.setValue("Pole",pole);
                    if(pole==null){
                        addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Pôle","Merci de vérifier le pôle");
                        return false;
                    }
                }else{
                    instance.setValue("Pole",null);
                    addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Pôle","Merci de vérifier le pôle");
                    return false;
                }
            }else{
                //instance.setValue("Pole",null);
                addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Pôle","Merci d'ajouter le pôle");
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean _Societe(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("sys_Title",cellValue);
                    IStorageResource societe =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Societe",filter);
                    //IOrganization societeOrganization = societe!=null?(IOrganization) societe.getValue("Organisation"):null;
                    instance.setValue("Societe",societe);
                    //instance.setValue("Organisation",societeOrganization);
                    if(societe==null){
                        addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Société","Merci de vérifier la société");
                        return false;
                    }
                }else{
                    instance.setValue("Societe",null);
                    addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Société","Merci de vérifier la société");
                    return false;
                }
            }else{
               // instance.setValue("Societe",null);
                addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Société","Merci d'ajouter la société");
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;

    }

    public boolean _Matricule(IStorageResource instance, Row row, Cell cell  ){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                cell.setCellType(CellType.STRING);
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("Matricule",cellValue);
                }else{
                    addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Matricule","Merci de vérifier le matricule");
                    instance.setValue("Matricule",null);
                    return false;
                }
            }else{
                addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Matricule","Merci d'ajouter le matricule");
                //instance.setValue("Matricule",null);
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean _Actif(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim().toLowerCase();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    if(cellValue.equals("oui")){
                        instance.setValue("Actif",true);
                    }else if(cellValue.equals("non")){
                        instance.setValue("Actif",false);

                    }else{
                        instance.setValue("Actif",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Actif","Merci de choisir Oui ou Non");
                        //return false;
                    }

                }else{
                    instance.setValue("Actif",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Actif","Merci de choisir Oui ou Non");
                   // return false;
                }
            }else{
                //instance.setValue("Actif",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Actif","Merci de choisir Oui ou Non");
               // return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;

    }

    public boolean _Civilite(IStorageResource instance, Row row, Cell cell ){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap possibleValues = new HashMap<String, String>() {{
                        put("M", "Mr");
                        put("Mr", "Mr");
                        put("M.", "Mr");
                        put("Monsieur", "Mr");
                        put("Mlle", "Mlle");
                        put("F", "Mme");
                        put("Madame", "Mme");
                        put("Mme", "Mme");

                    }};
                    cellValue = handleList(cellValue,possibleValues);
                    if(cellValue!= null){
                        instance.setValue("Title",cellValue);
                    }else{
                        instance.setValue("Title",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Civilité","Merci de choisir M. ou Mme ou Mlle");
                       // return false;
                    }
                }else{
                    instance.setValue("Title",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Civilité","Merci de choisir M. ou Mme ou Mlle");
                   // return false;
                }
            }else{
                //instance.setValue("Title",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Civilité","Merci de choisir M. ou Mme ou Mlle");
              //  return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

       return true;
    }
    public boolean _Nom(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue==null){
                    addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Nom","Merci de vérifier le nom");
                    return false;
                }
                instance.setValue("LastName",cellValue);
                setNomPrenom(instance);
            }else{
               // instance.setValue("LastName",null);
               // setNomPrenom(instance);
                addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Nom","Merci d'ajouter le nom");
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;

    }

    public boolean _Prenom(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue==null){
                    addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Prénom","Merci de vérifier le prénom");
                    return false;
                }
                instance.setValue("FirstName",cellValue);
                setNomPrenom(instance);

            }else{
//                instance.setValue("FirstName",null);
//                setNomPrenom(instance);
                addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Prénom","Merci d'ajouter le prénom");
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }

    public boolean _NumTel(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                cell.setCellType(CellType.STRING);
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                //
                instance.setValue("MobilePhoneNumber",cellValue);

                if(cellValue==null){
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Numéro de téléphone perso","Merci de vérifier le numéro de téléphone perso");
                   // return false;
                }
            }else{
                // instance.setValue("MobilePhoneNumber",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Numéro de téléphone perso","Merci d'ajouter le numéro de téléphone perso");
                //return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }

    public boolean _CodeMobile(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                cell.setCellType(CellType.STRING);
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                instance.setValue("MobilePhoneNumber",cellValue);
                if(cellValue==null){
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Numéro de téléphone pro","Merci de vérifier le numéro de téléphone pro");
                    //return false;
                }
            }else{
                //instance.setValue("MobilePhoneNumber",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Numéro de téléphone pro","Merci d'ajouter le numéro de téléphone pro");
                //return false;
            }
        }catch (Exception e){
            e.printStackTrace();
           // addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Code mobile","Merci de vérifier le code mobile");
          //  return false;
        }

        return true;

    }

    public boolean _Extension(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                cell.setCellType(CellType.STRING);
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                instance.setValue("Extension",cellValue);
                if(cellValue==null){
                    instance.setValue("Extension",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Extension","Merci de vérifier le code extension pro");
                  //  return false;
                }
            }else{
                //instance.setValue("Extension",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Extension","Merci d'ajouter le code extension pro");
             //   return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            //addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Extension","Merci de vérifier l'éxtension");
            //return false;
        }

        return true;
    }

    public boolean _DateDeNaissance(IStorageResource instance, Row row, Cell cell){
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
                            if (cellValue!=null && !cellValue.equals("") ){
                                break;
                            }
                        }catch (ParseException e){
                           // e.printStackTrace();
                        }
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getDateCellValue();
                }
                instance.setValue("Birthday",cellValue);

                if(cellValue==null){
                    instance.setValue("Birthday",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date de naisssance","Merci de vérifier la date de naissance");
                  //  return false;
                }
            }else{
               // instance.setValue("Birthday",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date de naisssance","Merci d'ajouter la date de naissance");
              //  return false;
            }
        }catch (Exception e){
            e.printStackTrace();
           // addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date de naisssance","Merci de vérifier la date de naissance");
           // return false;
        }
        return true;
    }

    public boolean _CIN(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                instance.setValue("CIN",cellValue);
                if(cellValue==null){
                    instance.setValue("CIN",null);
                    addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","CIN","Merci de vérifier la CIN");
                    return false;
                }
            }else{
                //instance.setValue("CIN",null);
                addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","CIN","Merci d'ajouter la CIN");
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
          //  addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","CIN","Merci de vérifier la CIN");
           // return false;
        }

       return true;
    }

    public boolean _NCNSS(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                cell.setCellType(CellType.STRING);
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                instance.setValue("NCNSS",cellValue);
                if(cellValue==null){
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","N° Immatriculation CNSS","Merci de vérifier le N° Immatriculation CNSS");
                   // return false;
                }
            }else{
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","N° Immatriculation CNSS","Merci d'ajouter le N° Immatriculation CNSS");
               // instance.setValue("NCNSS",null);
                //return false;
            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }
            return true;

    }

    public boolean _Ville(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                instance.setValue("Ville",cellValue);

                if(cellValue==null){
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Ville","Merci de vérifier la ville");
                    //return false;
                }
            }else{
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Ville","Merci d'ajouter la ville");
               // instance.setValue("Ville",null);
               // return false;
            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }

        return true;
    }

    public boolean _Addresse1(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                instance.setValue("Address1",cellValue);

                if(cellValue==null){
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Adresse 1","Merci de vérifier l'adresse 1");
                   // return false;
                }
            }else{
               // instance.setValue("Address1",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Adresse 1","Merci d'ajouter l'adresse 1");
                //return false;
            }

        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }

        return true;
    }

    public boolean _Addresse2(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                instance.setValue("Adresse2",cellValue);
                if(cellValue==null){
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Adresse 2","Merci de vérifier l'adresse 2");

                }
            }else{
               // instance.setValue("Adresse2",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Adresse 2","Merci d'ajouter l'adresse 2");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }
        return true;

    }

    public boolean _EtatCivil(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                HashMap possibleValues = new HashMap<String, String>() {{
                    put("Célibataire", "Célibataire");
                    put("Marié(e)", "Marié(e)");
                    put("Divorcé(e)", "Divorcé(e)");
                    put("Veuf(ve)", "Veuf(ve)");

                }};
                cellValue = handleList(cellValue,possibleValues);
                instance.setValue("EtatCivil",cellValue);

                if(cellValue==null){
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Etat civil","Merci de vérifier l'Etat civil");

                }
            }else{
               // instance.setValue("EtatCivil",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Etat civil","Merci d'ajouter l'Etat civil");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }

        return true;

    }

    public boolean _NombreEnfant(IStorageResource instance, Row row, Cell cell  ){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                cell.setCellType(CellType.STRING);
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("NombreEnfants",cellValue);
                }else{
                    addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Nombre d'enfants","Merci de vérifier le nombre d'enfants");
                    instance.setValue("NombreEnfants",null);
                    return false;
                }
            }else{
                addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Nombre d'enfants","Merci d'ajouter le nombre d'enfants");
               // instance.setValue("NombreEnfants",null);
               // return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean _Maladie(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                instance.setValue("Maladie",cellValue);
                if(cellValue==null){
                  //  addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Maladie","Merci de vérifier la maladie");
                    //return false;
                }
            }else{
               // instance.setValue("Maladie",null);
              //  return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }
            return true;

    }

    public boolean _Email(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    if(isValidEmail((String)cellValue)){
                        instance.setValue("Email",cellValue);
                    }else{
                        instance.setValue("Email",null);
                         addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Email","Merci de vérifier l'Email");
                    }

                }else{
                    instance.setValue("Email",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Email","Merci de vérifier l'Email");
                }
            }else{
                //instance.setValue("Email",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Email","Merci d'ajouter l'Email");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }

    public boolean _Identifiant(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }

                if (cellValue!=null && !cellValue.equals("") ){
                  String  login =
                            Normalizer.normalize((String) cellValue, Normalizer.Form.NFD)
                                    .replaceAll("[^\\p{ASCII}]", "");
                    instance.setValue("Identifiant",login);
                }else{
                    addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Identifiant","Merci de vérifier l'identifiant");
                    return false;
                }
            }else{
                addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Identifiant","Merci d'ajouter l'identifiant");
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
      /*  if(!isUpdate){
            String nom = (String) instance.getValue("LastName");
            String prenom =(String) instance.getValue("FirstName");
            if(nom!=null && prenom!=null){
                String login = prenom+"."+nom;
                login = login.replaceAll("\\s", "");
                login =
                        Normalizer
                                .normalize(login, Normalizer.Form.NFD)
                                .replaceAll("[^\\p{ASCII}]", "");
                instance.setValue("Identifiant",login);
            }
        }*/
       /* if(!isUpdate){
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue()+"";
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("Identifiant",cellValue);
                }else{
                    isAnomalieFound = true;
                }
            }else{
                isAnomalieFound=true;
            }
        }*/

    }

    public boolean _MotDePasse(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }

                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("MotDePasse",cellValue);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        /* instance.setValue("MotDePasse","D3mo@demo@DEMO");*/

        //if(!isUpdate){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if (cellValue!=null && !cellValue.equals("") ){
                try {
                    if (PasswordHelper.checkPasswordStrength((String) cellValue, "")) {
                        instance.setValue("MotDePasse", cellValue);
                    } else {
                        addAnomalie(row.getSheet().getSheetName(), (row.getRowNum() + 1) + "", "Mot de passe", "Merci de vérifier le Mot de passe");
                        isAnomalieFound = true;
                        return false;
                    }

                }catch (Exception e) {
                    addAnomalie(row.getSheet().getSheetName(), (row.getRowNum() + 1) + "", "Mot de passe", "Merci de vérifier le Mot de passe");
                    isAnomalieFound = true;
                    return false;
                }
            }else{
                //addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Mot de passe","Merci de vérifier le Mot de passe");
                //isAnomalieFound = true;
                //return false;
            }
        }else{
            //addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Mot de passe","Merci de vérifier le Mot de passe");
            //isAnomalieFound = true;
            //return false;
        }
        // }
        return true;

    }

    public boolean _RIB(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                cell.setCellType(CellType.STRING);
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                        instance.setValue("NCompteBancaire",cellValue);

                }else{
                    instance.setValue("NCompteBancaire",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","RIB","Merci de vérifier le RIB");

                }
            }else{
                //instance.setValue("NCompteBancaire",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","RIB","Merci d'ajouter le RIB");

            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }
    return true;

    }

    public boolean _Banque(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("Banque",cellValue);
                }else{
                    instance.setValue("Banque",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Banque","Merci de vérifier la banque");

                }
            }else{
                //instance.setValue("Banque",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Banque","Merci d'ajouter la banque");

            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }
        return true;

    }

    public boolean _AgenceBancaire(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    instance.setValue("AgenceBancaire",cellValue);
                }else{
                    instance.setValue("AgenceBancaire",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Agence bancaire","Merci de vérifier l'agence bancaire");

                }
            }else{
                //instance.setValue("AgenceBancaire",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Agence bancaire","Merci d'ajouter l'agence bancaire");

            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }

        return true;
    }
    public boolean _NumeroFax(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                cell.setCellType(CellType.STRING);
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("NumeroFax",cellValue);
                }else{
                    instance.setValue("NumeroFax",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Numéro fax","Merci de vérifier le numéro fax");

                }
            }else{
                //instance.setValue("NumeroFax",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Numéro fax","Merci d'ajouter le numéro fax");

            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }

        return true;
    }

    public boolean _NumeroAgence(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                cell.setCellType(CellType.STRING);
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("NumeroDAgence",cellValue);
                }else{
                    instance.setValue("NumeroDAgence",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Numéro d'agence","Merci de vérifier le numéro d'agence");

                }
            }else{
               // instance.setValue("NumeroDAgence",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Numéro d'agence","Merci d'ajouter le numéro d'agence");

            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }
        return true;
    }

    public boolean _Domiciliation(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim().toLowerCase();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    if(cellValue.equals("oui")){
                        instance.setValue("Domiciliation",true);

                    }else if(cellValue.equals("non")){
                        instance.setValue("Domiciliation",false);

                    }else{
                        instance.setValue("Domiciliation",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Domiciliation","Merci de vérifier la domiciliation (la valeur doit étre Oui ou Non)");

                    }

                }else{
                    instance.setValue("Domiciliation",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Domiciliation","Merci de vérifier la domiciliation (la valeur doit étre Oui ou Non)");

                }
            }else{
                //instance.setValue("Domiciliation",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Domiciliation","Merci d'ajouter la domiciliation (la valeur doit étre Oui ou Non)");

            }
        }catch (Exception e){
            e.printStackTrace();
          //  return false;
        }
        return true;
    }

    public boolean _MainLever(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim().toLowerCase();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    if(cellValue.equals("oui")){
                        instance.setValue("MainLever",true);

                    }else if(cellValue.equals("non")){
                        instance.setValue("MainLever",false);

                    }else{
                        instance.setValue("MainLever",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Main lever","Merci de vérifier la main lever (la valeur doit étre Oui ou Non)");
                    }

                }else{
                    instance.setValue("MainLever",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Main lever","Merci de vérifier la main lever (la valeur doit étre Oui ou Non)");

                }
            }else{
                //instance.setValue("MainLever",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Main lever","Merci d'ajouter la main lever (la valeur doit étre Oui ou Non)");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }
        return true;

    }

    public boolean _DateEmbaucheSociete(IStorageResource instance, Row row, Cell cell){
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
                            if (cellValue!=null && !cellValue.equals("") ){
                                break;
                            }
                        }catch (ParseException e){
                         //   e.printStackTrace();
                        }
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getDateCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("DateDEmbauche",cellValue);
                }else{
                    instance.setValue("DateDEmbauche",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date d'embauche société","Merci de vérifier la date d'embauche société");

                }
            }else{
               // instance.setValue("DateDEmbauche",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date d'embauche société","Merci d'ajouter la date d'embauche société");

            }
        }catch (Exception e){
            //instance.setValue("DateDEmbauche",null);
            e.printStackTrace();
            //return false;
        }
        return true;
    }

    public boolean _DateEmbaucheGroupe(IStorageResource instance, Row row, Cell cell){
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
                            if (cellValue!=null && !cellValue.equals("") ){
                                break;
                            }
                        }catch (ParseException e){
                           // e.printStackTrace();
                        }
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getDateCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("DateDEmbaucheGroupe",cellValue);
                }else{
                    instance.setValue("DateDEmbaucheGroupe",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date d'embauche groupe","Merci de vérifier la date d'embauche groupe");
                }
            }else{
                //instance.setValue("DateDEmbaucheGroupe",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date d'embauche groupe","Merci d'ajouter la date d'embauche groupe");
            }
        }catch (Exception e){
           // instance.setValue("DateDEmbaucheGroupe",null);
            e.printStackTrace();
          //  return false;
        }
        return true;
    }

    public boolean _TypeContrat(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap possibleValues = new HashMap<String, String>() {{
                        put("CDI", "CDI");
                        put("CDD", "CDD");
                        put("ANAPEC", "ANAPEC");
                        put("CSTG", "CSTG");
                    }};
                    cellValue = handleList(cellValue,possibleValues);
                    if(cellValue!= null){
                        instance.setValue("ContractType",cellValue);
                    }else{
                        instance.setValue("ContractType",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type de contrat","Merci de vérifier le type de contrat (la valeur doit étre CDD ou CDI ou ANAPEC ou CSTG)");

                    }
                }else{
                    instance.setValue("ContractType",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type de contrat","Merci de vérifier le type de contrat (la valeur doit étre CDD ou CDI ou ANAPEC ou CSTG)");

                }
            }else{
                //instance.setValue("ContractType",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type de contrat","Merci d'ajouter le type de contrat (la valeur doit étre CDD ou CDI ou ANAPEC ou CSTG)");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }

        return true;

    }

    public boolean _Roles(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    Set<IGroup> cellGroupes = handleGroupes((String) cellValue,row);
                    Set<IGroup> cellSecretGroupes = handleSecretGroupesGroupes((String) cellValue,(IStorageResource) instance.getValue("Societe"),row);
                    if(cellGroupes!= null){
                        /*List<IGroup> groupList = Optional.ofNullable((List<IGroup>) instance.getValue("Groupes"))
                                .orElse(Collections.emptyList());*/
                        List<IGroup> groupList = Optional.ofNullable((List<IGroup>) instance.getValue("Groupes"))
                                .orElse(Collections.emptyList())
                                .stream()
                                .filter(group -> "RHSociete".equals(group.getName()) ||"ResponsableDevSociete".equals(group.getName())) // Assuming there's a getName() method in IGroup
                                .collect(Collectors.toList());

                        Set<IGroup> alreadyGroupes = new HashSet<>(groupList);
                        Set<IGroup> combinedGroupes = new HashSet<>(alreadyGroupes);
                        combinedGroupes.addAll(cellGroupes);
                        instance.setValue("Groupes", combinedGroupes);
                    }

                    if(cellSecretGroupes!= null){
                      /*  List<IGroup> groupList = Optional.ofNullable((List<IGroup>) instance.getValue("SecretGroupes"))
                                .orElse(Collections.emptyList());*/

                      //  Set<IGroup> alreadySecretGroupes = new HashSet<>(groupList);
                     //   Set<IGroup> combinedGroupes = new HashSet<>(alreadySecretGroupes);
                      //  combinedGroupes.addAll(cellSecretGroupes);
                        instance.setValue("SecretGroupes", cellSecretGroupes);

                    }
                }else{
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Rôles","Merci de vérifier les rôles (Salarié, Manager, RH) séparés par ;");

                }
            }else{
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Rôles","Merci d'ajouter les rôles (Salarié, Manager, RH) séparés par ;");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }

        return true;
    }

    public boolean _ResponsableN1(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("CIN",cellValue);
                    IStorageResource hierarchicalManager =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                    if(hierarchicalManager!=null){
                        instance.setValue("HierarchicalManager",hierarchicalManager.getValue("Salarie"));
                    }else{
                       // N1Map.put(instance,(String)cellValue);
                    }
                }else{
                    instance.setValue("HierarchicalManager",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Responsable (N+1)","Merci de vérifier le responsable (N+1)");

                }
            }else{
                //instance.setValue("HierarchicalManager",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Responsable (N+1)","Merci d'ajouter le responsable (N+1)");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }

        return true;

    }

    public boolean _DateSortie(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    if (cellValue != null && !cellValue.equals("")) {
                        String[] dateFormats = {"dd/MM/yyyy", "dd-MM-yyyy", "dd_MM_yyyy"};
                        for (String dateFormat : dateFormats) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                                cellValue = sdf.parse((String) cellValue);
                                if (cellValue != null && !cellValue.equals("")) {
                                    break;
                                }
                            } catch (ParseException e) {
                                // e.printStackTrace();
                            }
                        }
                    }

                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getDateCellValue();
                }


                if(cellValue==null || cellValue.equals("")){
                    instance.setValue("DateDeSortie",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date de date de sortie","Merci de vérifier la date de date de sortie");
                    //  return false;
                }else {
                    instance.setValue("DateDeSortie",cellValue);
                }
            }else{
                //instance.setValue("DateDeSortie",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date de date de sortie","Merci d'ajouter la date de date de sortie");
                //  return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            // addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date de naisssance","Merci de vérifier la date de naissance");
            // return false;
        }
        return true;
    }

    public boolean _MotifSortie(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("MotifSortie",cellValue);
                }else{
                    instance.setValue("MotifSortie",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","MotifSortie","Merci de vérifier le motif sortie");

                }
            }else{
                //instance.setValue("MotifSortie",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","MotifSortie","Merci d'ajouter le motif sortie");

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }

    public boolean _Evaluateur(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("CIN",cellValue);
                    IStorageResource evaluateur =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                    if(evaluateur!=null){
                        instance.setValue("Evaluateur",evaluateur.getValue("Salarie"));
                    }else{
                        EvaluateurMap.put(instance,(String)cellValue);
                    }
                }else{
                    instance.setValue("Evaluateur",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Evaluateur","Merci de vérifier l'Evaluateur");

                }
            }else{
                //instance.setValue("Evaluateur",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Evaluateur","Merci d'ajouter l'Evaluateur");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }

        return true;
    }

    public boolean _Suppleants(IStorageResource instance, Row row, Cell cell){
        try {
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    String[] suppleants = ((String) cellValue).split(";");
                    Set<IStorageResource> suppleantsStorage = new HashSet<>();
                    for(String suppleantCIN : suppleants){
                        suppleantCIN = suppleantCIN.trim();
                        HashMap<String,Object> filter = new HashMap<>();
                        filter.put("Identifiant",suppleantCIN);
                        IStorageResource suppleant =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                        if(suppleant!=null){
                            suppleantsStorage.add(suppleant);
                        }else{
                            SuppleantMap.put(instance,suppleantCIN);
                        }
                    }
               /* Set<IStorageResource> alreadySuppleants = Optional.ofNullable((Set<IStorageResource>) instance.getValue("Suppleant"))
                        .orElse(Collections.emptySet());
                alreadySuppleants.addAll(suppleantsStorage);*/
                    instance.setValue("Suppleant",suppleantsStorage);
                }else{
                    instance.setValue("Suppleant",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Suppléant(s)","Merci de vérifier les suppléant(s) (Les CIN doivent être séparés par ;)");

                }
            }else{
               // instance.setValue("Suppleant",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Suppléant(s)","Merci d'ajouter les suppléant(s) (Les CIN doivent être séparés par ;)");
            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }

        return true;
    }



    public boolean _Activite(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("Departement",instance.getValue("Departement"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource activite =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Activite",filter);
                    if(activite!=null){
                        instance.setValue("Activite",activite);
                    }else{
                        instance.setValue("Activite",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Activité","Merci de vérifier l'activité "+cellValue);

                    }
                }else{
                    instance.setValue("Activite",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Activité","Merci de vérifier l'activité");

                }
            }else{
                //instance.setValue("Activite",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Activité","Merci d'ajouter l'activité");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }
        return true;

    }

    public boolean _Service(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("Activite",instance.getValue("Activite"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource service =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Service",filter);
                    if(service!=null){
                        instance.setValue("Service",service);
                    }else{
                        instance.setValue("Service",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Service","Merci de vérifier le service "+cellValue);

                    }
                }else{
                    instance.setValue("Service",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Service","Merci de vérifier le service");

                }
            }else{
                //instance.setValue("Service",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Service","Merci d'ajouter le service");

            }
        }catch (Exception e){
            e.printStackTrace();
         //   return false;
        }
        return true;

    }



    public boolean _Metier(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("Societe",instance.getValue("Societe"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource metier =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Metier",filter);
                    if(metier!=null){
                        instance.setValue("Metier",metier);
                    }else{
                        instance.setValue("Metier",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Métier","Merci de vérifier le métier "+cellValue);
                    }
                }else{
                    instance.setValue("Metier",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Métier","Merci de vérifier le métier");

                }
            }else{
                //instance.setValue("Metier",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Métier","Merci d'ajouter le métier");
            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }
        return true;
    }

    public boolean _Direction(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("Societe",instance.getValue("Societe"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource departement =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Direction",filter);
                    if(departement!=null){
                        instance.setValue("Direction",departement);
                    }else{
                        instance.setValue("Direction",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Direction","Merci de vérifier la direction "+cellValue);

                    }
                }else{
                    instance.setValue("Direction",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Direction","Merci de vérifier la direction");

                }
            }else{
                //instance.setValue("Direction",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Direction","Merci d'ajouter la direction");

            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }

        return true;
    }

    public boolean _Departement(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("Societe",instance.getValue("Societe"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource departement =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","departementcommun",filter);
                    if(departement!=null){
                        instance.setValue("Departement",departement);
                    }else{
                        instance.setValue("Departement",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Département","Merci de vérifier la département "+cellValue);

                    }
                }else{
                    instance.setValue("Departement",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Département","Merci de vérifier la département");

                }
            }else{
                //instance.setValue("Departement",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Département","Merci d'ajouter la département");

            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }

        return true;
    }

    public void _Division(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if (cellValue!=null && !cellValue.equals("") ){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Societe",instance.getValue("Societe"));
                filter.put("Direction",instance.getValue("Direction"));
                filter.put("Departement",instance.getValue("Departement"));

                filter.put("sys_Title",cellValue);
                IStorageResource division =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Division",filter);
                if(division!=null){
                    instance.setValue("Division",division);
                }else{
                    instance.setValue("Division",null);
                    //  isAnomalieFound = true;
                }
            }else{
                instance.setValue("Division",null);
                //  isAnomalieFound = true;
            }
        }else{
            //instance.setValue("Division",null);
            //  isAnomalieFound=true;
        }
    }

    public boolean _Site(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("Societe",instance.getValue("Societe"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource site =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Site",filter);
                    if(site!=null){
                        instance.setValue("Site",site);
                    }else{
                        instance.setValue("Site",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Site","Merci de vérifier le site "+cellValue);
                    }
                }else{
                    instance.setValue("Site",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Site","Merci de vérifier le site");
                }
            }else{
                //instance.setValue("Site",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Site","Merci d'ajouter le site");
            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }

        return true;

    }

    public boolean _Fonction(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("Societe",instance.getValue("Societe"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource fonction =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","fonctions",filter);
                    if(fonction!=null){
                        instance.setValue("Fonction",fonction);
                    }else{
                        instance.setValue("Fonction",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Fonction","Merci de vérifier la fonction "+cellValue);

                    }
                }else{
                    instance.setValue("Fonction",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Fonction","Merci de vérifier la fonction");
                }
            }else{
                //instance.setValue("Fonction",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Fonction","Merci d'ajouter la fonction");

            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }

        return true;
    }

    public boolean _Grade(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("Societe",instance.getValue("Societe"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource grade =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Grade",filter);
                    if(grade!=null){
                        instance.setValue("Grade",grade);
                    }else{
                        instance.setValue("Grade",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Grade","Merci de vérifier la grade "+cellValue);

                    }
                }else{
                    instance.setValue("Grade",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Grade","Merci de vérifier la grade");

                }
            }else{
                //instance.setValue("Grade",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Grade","Merci d'ajouter la grade");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }
        return true;
    }

    public boolean _Circuit(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    //  filter.put("Societe",instance.getValue("Societe"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource categorie =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Categorie",filter);
                    if(categorie!=null){
                        instance.setValue("Categorie",categorie);
                    }else{
                        instance.setValue("Categorie",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Circuit","Merci de vérifier le circuit "+cellValue);
                    }
                }else{
                    instance.setValue("Categorie",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Circuit","Merci de vérifier le circuit");

                }
            }else{
                //instance.setValue("Categorie",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Circuit","Merci d'ajouter le circuit");
            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }

        return true;
    }

    public boolean _ProfilEvaluation(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("Societe",instance.getValue("Societe"));
                    filter.put("sys_Title",cellValue);
                    IStorageResource profil =  handleStorageWithFilter("DefaultOrganization","EVAL","Referentiels","Profil",filter);
                    if(profil!=null){
                        instance.setValue("ProfilEvaluation",profil);
                    }else{
                        instance.setValue("ProfilEvaluation",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Profil évaluation","Merci de vérifier le profil évaluation "+cellValue);

                    }
                }else{
                    instance.setValue("ProfilEvaluation",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Profil évaluation","Merci de vérifier le profil évaluation");

                }
            }else{
                //instance.setValue("ProfilEvaluation",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Profil évaluation","Merci d'ajouter le profil évaluation");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;

        }
        return true;
    }

    public boolean _TypeDeSalaire(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    HashMap possibleValues = new HashMap<String, String>() {{
                        put("Horaire", "Horaire");
                        put("Mensuel", "Mensuel");

                    }};
                    cellValue = handleList(cellValue,possibleValues);
                    if(cellValue!= null){
                        instance.setValue("TypeDeSalaire",cellValue);
                    }else{
                        instance.setValue("TypeDeSalaire",null);
                        addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type de salaire","Merci de vérifier le type de salaire (Horaire/Mensuel)");

                    }
                }else{
                    instance.setValue("TypeDeSalaire",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type de salaire","Merci de vérifier le type de salaire (Horaire/Mensuel)");

                }
            }else{
                //instance.setValue("TypeDeSalaire",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type de salaire","Merci d'ajouter le type de salaire (Horaire/Mensuel)");

            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }

        return true;
    }

    public boolean _TauxHoraire(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    if(((String)cellValue).contains("%")){
                        cellValue = ((String) cellValue).replace("%","");
                    }
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("TauxHoraire",cellValue);
                }else{
                    instance.setValue("TauxHoraire",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Taux horaire (MAD)","Merci de vérifier le taux horaire (MAD)");

                }
            }else{
               // instance.setValue("TauxHoraire",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Taux horaire (MAD)","Merci d'ajouter le taux horaire (MAD)");

            }
        }catch (Exception e){
            e.printStackTrace();
          //  return false;
        }

        return true;

    }

    public boolean _SalaireDeBase(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("SalaireDeBase",cellValue);
                }else{
                    instance.setValue("SalaireDeBase",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Salaire de base (MAD)","Merci de vérifier le salaire de base (MAD)");

                }
            }else{
                //instance.setValue("SalaireDeBase",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Salaire de base (MAD)","Merci d'ajouter le salaire de base (MAD)");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }

       return true;
    }

    public boolean _SalaireBrut(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("SalaireBrutDH",cellValue);
                }else{
                    instance.setValue("SalaireBrutDH",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Salaire Brut (MAD)","Merci de vérifier le salaire Brut (MAD)");

                }
            }else{
                //instance.setValue("SalaireBrutDH",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Salaire Brut (MAD)","Merci d'ajouter le salaire Brut (MAD)");

            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }

        return true;

    }

    public boolean _SalaireBrutImposableDH(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("SalaireBrutImposableDH",cellValue);
                }else{
                    instance.setValue("SalaireBrutImposableDH",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Salaire brut imposable (MAD)","Merci de vérifier le salaire brut imposable (MAD)");

                }
            }else{
               // instance.setValue("SalaireBrutImposableDH",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Salaire brut imposable (MAD)","Merci d'ajouter le salaire brut imposable (MAD)");

            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }

        return true;
    }

    public boolean _RemunerationDH(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("RemunerationDH",cellValue);
                }else{
                    instance.setValue("RemunerationDH",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Rémunération (MAD)","Merci de vérifier la rémunération (MAD)");

                }
            }else{
                //instance.setValue("RemunerationDH",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Rémunération (MAD)","Merci d'ajouter la rémunération (MAD)");

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;

    }

    public boolean _SalaireNETDH(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("SalaireNETDH",cellValue);
                }else{
                    instance.setValue("SalaireNETDH",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Salaire NET (MAD)","Merci de vérifier le salaire NET (MAD)");

                }
            }else{
                //instance.setValue("SalaireNETDH",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Salaire NET (MAD)","Merci d'ajouter le salaire NET (MAD)");

            }
        }catch (Exception e){
            e.printStackTrace();

        }

        return true;

    }

    public boolean _MontantMensuelNoteFrais(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("MontantMensuelDeNoteDeFrais",cellValue);
                }else{
                    instance.setValue("MontantMensuelDeNoteDeFrais",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Montant mensuel de note de frais  (MAD)","Merci de vérifier le montant mensuel de note de frais (MAD)");

                }
            }else{
               // instance.setValue("MontantMensuelDeNoteDeFrais",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Montant mensuel de note de frais  (MAD)","Merci d'ajouter le montant mensuel de note de frais  (MAD)");

            }
        }catch (Exception e){
            e.printStackTrace();
            // return false;
        }

        return true;

    }

    public boolean _NMutuelle(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                cell.setCellType(CellType.STRING);
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("NMutuelle",cellValue);
                }else{
                    instance.setValue("NMutuelle",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","N° Mutuelle","Merci de vérifier le N° Mutuelle");

                }
            }else{
               // instance.setValue("NMutuelle",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","N° Mutuelle","Merci d'ajouter le N° Mutuelle");

            }
        }catch (Exception e){
            e.printStackTrace();
            //return false;
        }

        return true;
    }

    public boolean _NomDeLaMutuelle(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("NomDeLaMutuelle",cellValue);
                }else{
                    instance.setValue("NomDeLaMutuelle",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Nom mutuelle","Merci de vérifier le nom mutuelle");

                }
            }else{
               // instance.setValue("NomDeLaMutuelle",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Nom mutuelle","Merci d'ajouter le nom mutuelle");
            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }

        return true;
    }

    public boolean _TauxDeLaMutuelle(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    if(((String)cellValue).contains("%")){
                        cellValue = ((String) cellValue).replace("%","");
                    }
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("TauxDeLaMutuelle",(((Number)cellValue).floatValue()) * 100.0);
                }else{
                    instance.setValue("TauxDeLaMutuelle",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Taux mutuelle (%)","Merci de vérifier le taux mutuelle (%)");

                }
            }else{
                //instance.setValue("TauxDeLaMutuelle",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Taux mutuelle (%)","Merci d'ajouter le taux mutuelle (%)");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }

    public boolean _NCIMR(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("NCIMR",cellValue);
                }else{
                    instance.setValue("NCIMR",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","N° CIMR","Merci de vérifier le N° CIMR");

                }
            }else{
                //instance.setValue("NCIMR",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","N° CIMR","Merci d'ajouter le N° CIMR");
            }
        }catch (Exception e){
            e.printStackTrace();
           // return false;
        }
        return true;

    }

    public boolean _NomRetraite(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("NomRetraite",cellValue);
                }else{
                    instance.setValue("NomRetraite",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","N° CIMR","Merci de vérifier le nom retraite");

                }
            }else{
                //instance.setValue("NomRetraite",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","N° CIMR","Merci d'ajouter le nom retraite");

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }

    public boolean _TauxDeLaRetraire(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK) && !cell.equals("")){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    if (!cellValue.equals("")) {
                        if (((String) cellValue).contains("%")) {
                            cellValue = ((String) cellValue).replace("%", "");
                        }
                        try {
                            cellValue = NumberFormat.getInstance().parse((String) cellValue);
                        } catch (NumberFormatException e) {
                            System.out.println("Cannot convert the string to a numeric value.");
                        }
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("TauxDeLaRetraire",cellValue);
                }else{
                    instance.setValue("TauxDeLaRetraire",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Taux retraite (%)","Merci de vérifier le taux retraite (%)");

                }
            }else{
                //instance.setValue("TauxDeLaRetraire",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Taux retraite (%)","Merci d'ajouter le taux retraite (%)");

            }
        }catch (Exception e){
            e.printStackTrace();

        }
        return true;
    }

    public boolean _NumeroAttribue(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("NumeroAttribue",cellValue);
                }else{
                    instance.setValue("NumeroAttribue",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Numéro attribué","Merci de vérifier le numéro attribué");

                }
            }else{
                //instance.setValue("NumeroAttribue",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Numéro attribué","Merci d'ajouter le numéro attribué");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }

    public boolean _NomDeLaRetraiteComplementaire(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("NomDeLaRetraiteComplementaire",cellValue);
                }else{
                    instance.setValue("NomDeLaRetraiteComplementaire",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Nom retraite complémentaire","Merci de vérifier le nom retraite complémentaire");

                }
            }else{
                //instance.setValue("NomDeLaRetraiteComplementaire",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Nom retraite complémentaire","Merci d'ajouter le nom retraite complémentaire");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public boolean _TauxDeLaRetraiteComplementaire(IStorageResource instance, Row row, Cell cell){
        try{

            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    if(((String)cellValue).contains("%")){
                        cellValue = ((String) cellValue).replace("%","");
                    }
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("TauxDeLaRetraiteComplementaire",cellValue);
                }else{
                    instance.setValue("TauxDeLaRetraiteComplementaire",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Taux retraite complémentaire (%)","Merci de vérifier le taux retraite complémentaire (%)");

                }
            }else{
                //instance.setValue("TauxDeLaRetraiteComplementaire",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Taux retraite complémentaire (%)","Merci d'ajouter le taux retraite complémentaire (%)");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public boolean _MontantEpargneRetraite(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("MontantEpargneRetraite",cellValue);
                }else{
                    instance.setValue("MontantEpargneRetraite",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Montant épargne retraite","Merci de vérifier le montant épargne retraite");

                }
            }else{
               // instance.setValue("MontantEpargneRetraite",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Montant épargne retraite","Merci d'ajouter le montant épargne retraite");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }

    public boolean _DroitMensuelle(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("DroitMensuelle",cellValue);
                }else{
                    instance.setValue("DroitMensuelle",null);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Droit mensuel","Merci de vérifier le droit mensuel");

                }
            }else{
              //  instance.setValue("DroitMensuelle",null);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Droit mensuel","Merci d'ajouter le droit mensuel");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }
    public boolean _SoldeAnneeEnCours(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("SoldeAnneeEnCours",cellValue);
                }else{
                    instance.setValue("SoldeAnneeEnCours",0);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Solde année en cours","Merci de vérifier le solde année en cours");

                }
            }else{
                //instance.setValue("SoldeAnneeEnCours",0);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Solde année en cours","Merci d'ajouter le solde année en cours");

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }
    public boolean _SoldeAnterieur(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("SoldeAnterieur",cellValue);
                }else{
                    instance.setValue("SoldeAnterieur",0);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Solde année antérieure","Merci de vérifier le solde année antérieure");

                }
            }else{
                //instance.setValue("SoldeAnterieur",0);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Solde année antérieure","Merci d'ajouter le solde année antérieure");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }

    public boolean _SoldeConges(IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                    try {
                        cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot convert the string to a numeric value.");
                    }
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if (cellValue!=null && !cellValue.equals("") ){
                    instance.setValue("SoldeConges",cellValue);
                }else{
                    instance.setValue("SoldeConges",0);
                    addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Solde congés","Merci de vérifier le solde congés");

                }
            }else{
                //instance.setValue("SoldeConges",0);
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Solde congés","Merci d'ajouter le solde congés");

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }
    public void setNomPrenom(IStorageResource collaborateur){
        String nom =collaborateur.getValue("LastName")!=null? (String) collaborateur.getValue("LastName"):"";
        String prenom = collaborateur.getValue("FirstName")!=null?(String)collaborateur.getValue("FirstName"):"";
        String fullName = nom+" "+prenom;
        collaborateur.setValue("NomPrenom",fullName);
    }


    /*public void addAnomalie(IWorkflowInstance alertAnomalis,String feuille , String ligne , String cordonnee,String alerte){
        JSONObject anomalieDetails = new JSONObject();
        anomalieDetails.put("Feuille",feuille);
        anomalieDetails.put("Ligne",ligne);
        anomalieDetails.put("Cordonnee",cordonnee);
        anomalieDetails.put("Anomalie",alerte);
       // alertAnomalis.add(anomalieDetails);
    }

    public void addAlerte(IWorkflowInstance alertAnomalis,String feuille , String ligne , String cordonnee,String alerte){
        JSONObject anomalieDetails = new JSONObject();
        anomalieDetails.put("Feuille",feuille);
        anomalieDetails.put("Ligne",ligne);
        anomalieDetails.put("Cordonnee",cordonnee);
        anomalieDetails.put("Alerte",alerte);
       // alertAnomalis.add(anomalieDetails);
    }*/



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
            }else {

                 item = Modules.getWorkflowModule().createStorageResource(context,definition,"");
                for (Map.Entry<String, Object> entry : filter.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    item.setValue(key, value);
                    item.save(Modules.getWorkflowModule().getSysadminContext());

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return item;
    }

    Object handleList(Object item,HashMap<String, String> possibleValues){
        if(item!=null && possibleValues.get(item)!=null){
            return possibleValues.get(item);
        }
        return null;
    }

    Set<IGroup> handleGroupes(String cellValue,Row row ){
        if(cellValue==null || cellValue.isEmpty())return null;
        Set<IGroup> groupes = new HashSet<>();
      //  groupes.add(getGroupe(groupesMapper.get("Salarié")));
        String[] groupesNames = cellValue.split(";");
        for(String groupeName : groupesNames){
            if(groupesMapper.get(groupeName)!=null){
                IGroup groupe = getGroupe(groupesMapper.get(groupeName));
                if(groupe!=null){
                    groupes.add(groupe);
                }
            }else{
                addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Rôles","Merci de vérifier le rôle "+groupeName);

            }

        }
        return groupes;
    }

    Set<IGroup> handleSecretGroupesGroupes(String cellValue,IStorageResource societe,Row row ){


        if(cellValue==null || cellValue.isEmpty())return null;
        Set<IGroup> groupes = new HashSet<>();
        String[] groupesNames = cellValue.split(";");
        for(String groupeName : groupesNames){
            if(secretGroupesMapper.get(groupeName)!=null){
                IGroup sercretGroup = getGroupByName(secretGroupesMapper.get(groupeName),(IOrganization) societe.getValue("Organisation"));
                if(sercretGroup != null){
                    groupes.add(sercretGroup);
                }
            }else{
                //addAlerte(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Rôles","Merci de vérifier le rôle "+groupeName);

            }

        }
        return groupes;
    }
    protected IGroup getGroupByName(String groupeName, IOrganization organization) {
        try {
          /*  if(groupeName.equals("News readers")){
                groupeName = "NEWS_ANIMATION";
            }else if(groupeName.equals("Workplace FAQ administrators")){
                groupeName = "WP_ADMIN_MIDDLE_OFFICE_FAQ";
            }else if(groupeName.equals("salarié")){
                groupeName = "Salarie";
            }else if(groupeName.equals("RH")){
                groupeName = "RH";
            }else if(groupeName.equals("Manager")){
                groupeName = "Manager";
            }else if(groupeName.equals("admin")){
                groupeName = "BPO";
            }*/
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IGroup group = Modules.getDirectoryModule().getGroup(context, organization, organization.getName() + groupeName);
           /* if (group == null) {
                group = Modules.getDirectoryModule().getGroup(context, organization, groupeName);
            }*/
            return group;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    private IGroup getGroupe(String groupeID) {
        try{
            return ((ProcessWorkflowModule) Modules.getWorkflowModule()).getGroupByName(groupeID);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isValidEmail(String email) {
        if(email==null || email.isEmpty()){
            return false;
        }
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidRib(String RIB){
        if(RIB==null || RIB.isEmpty()){
            return false;
        }
        if(RIB.length()!=24){
            return false;
        }
        return true;
    }



    public void addToRefAndCreateUser(IStorageResource collaborateur) {
        IStorageResource storageResource = null;
        try {
            // Create / Update Fiche Collaborateur
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            if (collaborateur == null) {
                storageResource = Modules.getWorkflowModule().createStorageResource(context, definition, "", "");
            } else {
                storageResource = collaborateur;
            }
            if (storageResource != null) {

                // Create / Update User in Administration
                IStorageResource societe = (IStorageResource) storageResource.getValue("Societe");
                IOrganization organizationSociete = (IOrganization) societe.getValue("Organisation");

                if (storageResource.getValue("Salarie") != null) {
                    updateSalarie(storageResource, organizationSociete);
                } else {
                    insertSalarie(storageResource, organizationSociete);
                }


                int nbrEnfant = 0;

                storageResource.setValue("MotDePasse", null);
                storageResource.setValue("ConfirmerMotDePasse", null);
                //storageResource.setValue("NombreEnfants", nbrEnfant);
            }
        } catch (Exception ex) {
            if(ex.getMessage().equals("L'identifiant existe déja")){
                //System.err.println("Error : L'identifiant : "+storageResource.getValue("Identifiant") + " existe déjà ! de "+storageResource.getValue("LastName")+" "+storageResource.getValue("FirstName") );
                System.err.println("Error : L importation du "+storageResource.getValue("LastName")+" "+storageResource.getValue("FirstName")+" est impossible car L'identifiant : "+storageResource.getValue("Identifiant") + " existe déjà !" );

                /*JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: ");
                tmp.put("Cordonnee", "Sheet: Feuil1");
                tmp.put(NatureCommentaireExcel.ANOMALIE.label, "L'importation du "+storageResource.getValue("LastName")+" "+storageResource.getValue("FirstName")+" est impossible car L'identifiant : "+storageResource.getValue("Identifiant") + " existe déjà !" );
                excelCommentaireBloquant.add(tmp);*/
            }else {
                ex.printStackTrace();
            }
        }
    }

    private void insertSalarie(IResource collaborateurToCopyFrom, IOrganization organizationSociete) {
        IContext context = Modules.getWorkflowModule().getSysadminContext();
        IUser newUser = null;

        try {
            // Modules.getDirectoryModule().beginTransaction();//(IOrganization)collaborateurToCopyFrom.getValue("Organisation")
            String identifiant = (String) collaborateurToCopyFrom.getValue("Identifiant");
            String motPasse = (String) collaborateurToCopyFrom.getValue("MotDePasse");
            String nom = (String) collaborateurToCopyFrom.getValue("LastName");
            String prenom = (String) collaborateurToCopyFrom.getValue("FirstName") ;
            if(identifiant== null){
                motPasse = "F7@kP9!sQ2#LmX$";
                identifiant = Normalizer.normalize(prenom.substring(0, 1).toUpperCase() + "." + nom, Normalizer.Form.NFD)
                        .replaceAll("[^\\p{ASCII}]", "");
                collaborateurToCopyFrom.setValue("Identifiant",identifiant);


            }
            newUser = Modules.getDirectoryModule().createUser(context, identifiant, motPasse, organizationSociete);
            newUser = setUserFields(newUser, collaborateurToCopyFrom, organizationSociete);
            newUser.save(context);
            // Modules.getDirectoryModule().commitTransaction();
            collaborateurToCopyFrom.setValue("Salarie", newUser);
            collaborateurToCopyFrom.setValue("Organisation", organizationSociete);
            collaborateurToCopyFrom.save(context);
            counter++;
            System.out.println(counter+") "+newUser.getFullName()  + " a été créer");
        } catch (Exception ex) {
            if(ex.getMessage().equals("L'identifiant existe déja")){
                //System.err.println("Error : L'identifiant : "+storageResource.getValue("Identifiant") + " existe déjà ! de "+storageResource.getValue("LastName")+" "+storageResource.getValue("FirstName") );
                System.err.println("Error : L importation du "+collaborateurToCopyFrom.getValue("LastName")+" "+collaborateurToCopyFrom.getValue("FirstName")+" est impossible car L'identifiant : "+collaborateurToCopyFrom.getValue("Identifiant") + " existe déjà !" );

                /*JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: ");
                tmp.put("Cordonnee", "Sheet: Feuil1");
                tmp.put(NatureCommentaireExcel.ANOMALIE.label, "L'importation du "+collaborateurToCopyFrom.getValue("LastName")+" "+collaborateurToCopyFrom.getValue("FirstName")+" est impossible car L'identifiant : "+collaborateurToCopyFrom.getValue("Identifiant") + " existe déjà ! " );
                excelCommentaireBloquant.add(tmp);*/
            }else {
                ex.printStackTrace();
            }
        }
    }

    private void updateSalarie(IStorageResource storageResource, IOrganization organizationSociete) {
        IContext context = Modules.getWorkflowModule().getSysadminContext();
        IUser user = (IUser) storageResource.getValue("Salarie");
        if (storageResource.getValue("Identifiant") != null){
            user.setLogin((String) storageResource.getValue("Identifiant"));
        }
        if(storageResource.getValue("MotDePasse") != null){
            user.setPassword((String) storageResource.getValue("MotDePasse"));
        }
        user = setUserFields(user, storageResource, organizationSociete);
        user.save(context);
        storageResource.setValue("Salarie", user);
        storageResource.setValue("Organisation", organizationSociete);
        storageResource.save(context);
        counter++;
        System.out.println(counter+") "+user.getFullName() + " a été mis à jour");

    }

    private IUser setUserFields(IUser user, IResource collaborateurToCopyFrom, IOrganization organizationSociete) {
        user.save(Modules.getWorkflowModule().getSysadminContext());
        user.setOrganization(organizationSociete);
        user.setLanguage("fr");
        collaborateurToCopyFrom.setValue("Organisation",organizationSociete);
        collaborateurToCopyFrom.setValue("Salarie",user);
        user.getExtendedAttributes().setValue("Salarie", collaborateurToCopyFrom.getValue("Salarie"));
        user.getExtendedAttributes().setValue("Pole",collaborateurToCopyFrom.getValue("Pole"));
        user.getExtendedAttributes().setValue("Societe",collaborateurToCopyFrom.getValue("Societe"));
        user.getExtendedAttributes().setValue("Matricule",collaborateurToCopyFrom.getValue("Matricule"));
        Boolean isActif = collaborateurToCopyFrom.getValue("Actif")!=null?(Boolean) collaborateurToCopyFrom.getValue("Actif"):null;
        if(isActif){
            user.enable();
        }else {
            user.disable();
        }
        //FIRST
        //user.save(getWorkflowModule().getSysadminContext());

        user.setTitle((String) collaborateurToCopyFrom.getValue("Title"));
        user.setFirstName((String) collaborateurToCopyFrom.getValue("FirstName"));
        user.setLastName((String) collaborateurToCopyFrom.getValue("LastName"));
        user.setMobilePhoneNumber((String)collaborateurToCopyFrom.getValue("MobilePhoneNumber"));
        user.getExtendedAttributes().setValue("CodeMobile",collaborateurToCopyFrom.getValue("CodeMobile"));
        user.getExtendedAttributes().setValue("Extension",collaborateurToCopyFrom.getValue("Extension"));
        user.setBirthday((Date) collaborateurToCopyFrom.getValue("Birthday"));
        user.getExtendedAttributes().setValue("CIN",collaborateurToCopyFrom.getValue("CIN"));
        user.getExtendedAttributes().setValue("NCNSS",collaborateurToCopyFrom.getValue("NCNSS"));
        user.getExtendedAttributes().setValue("Ville",collaborateurToCopyFrom.getValue("Ville"));
        user.setAddress1((String) collaborateurToCopyFrom.getValue("Address1"));
        user.setAddress2((String) collaborateurToCopyFrom.getValue("Adresse2"));
        user.getExtendedAttributes().setValue("EtatCivil",collaborateurToCopyFrom.getValue("EtatCivil"));
        user.getExtendedAttributes().setValue("Maladie",collaborateurToCopyFrom.getValue("Maladie"));
        user.setEmail((String) collaborateurToCopyFrom.getValue("Email"));
        user.getExtendedAttributes().setValue("NCompteBancaire",collaborateurToCopyFrom.getValue("NCompteBancaire"));
        user.getExtendedAttributes().setValue("Banque",collaborateurToCopyFrom.getValue("Banque"));
        user.getExtendedAttributes().setValue("AgenceBancaire",collaborateurToCopyFrom.getValue("AgenceBancaire"));
        user.getExtendedAttributes().setValue("NumeroFax",collaborateurToCopyFrom.getValue("NumeroFax"));
        user.getExtendedAttributes().setValue("NumeroDAgence",collaborateurToCopyFrom.getValue("NumeroDAgence"));
        user.getExtendedAttributes().setValue("Domiciliation",collaborateurToCopyFrom.getValue("Domiciliation"));
        user.getExtendedAttributes().setValue("MainLever",collaborateurToCopyFrom.getValue("MainLever"));
        user.getExtendedAttributes().setValue("DateDEmbauche",collaborateurToCopyFrom.getValue("DateDEmbauche"));
        user.getExtendedAttributes().setValue("DateDEmbaucheGroupe",collaborateurToCopyFrom.getValue("DateDEmbaucheGroupe"));
        user.getExtendedAttributes().setValue("ContractType",collaborateurToCopyFrom.getValue("ContractType"));
        //SECOND
        // user.save(getWorkflowModule().getSysadminContext());
        user.getExtendedAttributes().setValue("Groupes", collaborateurToCopyFrom.getValue("Groupes"));
        user.getExtendedAttributes().setValue("SecretGroupes", collaborateurToCopyFrom.getValue("SecretGroupes"));
        user.setHierarchicalManager((IUser) collaborateurToCopyFrom.getValue("HierarchicalManager"));
        user.getExtendedAttributes().setValue("Evaluateur",collaborateurToCopyFrom.getValue("Evaluateur"));
        user.getExtendedAttributes().setValue("Suppleant",collaborateurToCopyFrom.getValue("Suppleant"));
        user.getExtendedAttributes().setValue("Direction",collaborateurToCopyFrom.getValue("Direction"));
        user.getExtendedAttributes().setValue("Departement",collaborateurToCopyFrom.getValue("Departement"));
        user.getExtendedAttributes().setValue("Service",collaborateurToCopyFrom.getValue("Service"));
        user.getExtendedAttributes().setValue("Activite",collaborateurToCopyFrom.getValue("Activite"));
        user.getExtendedAttributes().setValue("Metier",collaborateurToCopyFrom.getValue("Metier"));
        user.getExtendedAttributes().setValue("Site",collaborateurToCopyFrom.getValue("Site"));
        user.getExtendedAttributes().setValue("Fonction",collaborateurToCopyFrom.getValue("Fonction"));
        user.getExtendedAttributes().setValue("Grade",collaborateurToCopyFrom.getValue("Grade"));
        user.getExtendedAttributes().setValue("Categorie",collaborateurToCopyFrom.getValue("Categorie"));
        user.getExtendedAttributes().setValue("ProfilEVAL",collaborateurToCopyFrom.getValue("ProfilEvaluation"));
        user.getExtendedAttributes().setValue("TypeDeSalaire",collaborateurToCopyFrom.getValue("TypeDeSalaire"));
        user.getExtendedAttributes().setValue("TauxHoraire",collaborateurToCopyFrom.getValue("TauxHoraire"));
        user.getExtendedAttributes().setValue("SalaireDeBase",collaborateurToCopyFrom.getValue("SalaireDeBase"));
        user.getExtendedAttributes().setValue("SalaireBrut",collaborateurToCopyFrom.getValue("SalaireBrutDH"));
        user.getExtendedAttributes().setValue("SalaireBrutImposableDH",collaborateurToCopyFrom.getValue("SalaireBrutImposableDH"));
        user.getExtendedAttributes().setValue("RemunerationDH",collaborateurToCopyFrom.getValue("RemunerationDH"));
        user.getExtendedAttributes().setValue("MontantMensuelDeNoteDeFrais",collaborateurToCopyFrom.getValue("MontantMensuelDeNoteDeFrais"));
        user.getExtendedAttributes().setValue("Salaire",collaborateurToCopyFrom.getValue("SalaireNETDH"));
        user.getExtendedAttributes().setValue("NMutuelle",collaborateurToCopyFrom.getValue("NMutuelle"));
        user.getExtendedAttributes().setValue("NomDeLaMutuelle",collaborateurToCopyFrom.getValue("NomDeLaMutuelle"));
        user.getExtendedAttributes().setValue("TauxDeLaMutuelle",collaborateurToCopyFrom.getValue("TauxDeLaMutuelle"));
        user.getExtendedAttributes().setValue("NCIMR",collaborateurToCopyFrom.getValue("NCIMR"));
        user.getExtendedAttributes().setValue("NomRetraite",collaborateurToCopyFrom.getValue("NomRetraite"));
        user.getExtendedAttributes().setValue("TauxDeLaRetraire",collaborateurToCopyFrom.getValue("TauxDeLaRetraire"));
        user.getExtendedAttributes().setValue("NumeroAttribue",collaborateurToCopyFrom.getValue("NumeroAttribue"));
        user.getExtendedAttributes().setValue("NomDeLaRetraiteComplementaire",collaborateurToCopyFrom.getValue("NomDeLaRetraiteComplementaire"));
        user.getExtendedAttributes().setValue("TauxDeLaRetraiteComplementaire",collaborateurToCopyFrom.getValue("TauxDeLaRetraiteComplementaire"));
        user.getExtendedAttributes().setValue("MontantEpargneRetraite",collaborateurToCopyFrom.getValue("MontantEpargneRetraite"));
        user.getExtendedAttributes().setValue("DroitMensuelle",collaborateurToCopyFrom.getValue("DroitMensuelle"));
        user.getExtendedAttributes().setValue("SoldeAnneeEnCours",collaborateurToCopyFrom.getValue("SoldeAnneeEnCours"));
        user.getExtendedAttributes().setValue("SoldeAnterieur",collaborateurToCopyFrom.getValue("SoldeAnterieur"));
        user.getExtendedAttributes().setValue("SoldeConges",collaborateurToCopyFrom.getValue("SoldeConges"));
        user.getExtendedAttributes().setValue("NombreEnfants", collaborateurToCopyFrom.getValue("NombreEnfants"));
        File file = null;
        ArrayList<IAttachment> tmp = (ArrayList<IAttachment>) collaborateurToCopyFrom.getValue("Avatar");
        IAttachment ficheAvatar = null;
        if (tmp != null && tmp.size() > 0) {
            ficheAvatar = tmp.iterator().next();
        }
        if (ficheAvatar != null) {
            try {
                file = new File("c://TEST//" + ficheAvatar.getName());
                FileUtils.writeByteArrayToFile(file, ficheAvatar.getContent());
                IAttachment userAvatar = Modules.getDirectoryModule().createAttachment(Modules.getWorkflowModule().getSysadminContext(), file);
                user.setAvatar(userAvatar);
                file.delete();
                file.deleteOnExit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            user.setAvatar(null);
        }

        user.setExit(collaborateurToCopyFrom.getValue("DateDeSortie") != null ? (Date) collaborateurToCopyFrom.getValue("DateDeSortie") : null);
        user.getExtendedAttributes().setValue("MotifSortie", collaborateurToCopyFrom.getValue("MotifSortie"));





        // Droit au congés
        user.getExtendedAttributes().setValue("DroitMensuelle", collaborateurToCopyFrom.getValue("DroitMensuelle"));
        user.getExtendedAttributes().setValue("AbsenceAnneeEnCours", collaborateurToCopyFrom.getValue("AbsenceAnneeEnCours"));
        user.getExtendedAttributes().setValue("AbsenceAnterieure", collaborateurToCopyFrom.getValue("AbsenceAnterieure"));
        user.getExtendedAttributes().setValue("SoldeAnneeEnCours", collaborateurToCopyFrom.getValue("SoldeAnneeEnCours"));
        user.getExtendedAttributes().setValue("SoldeAnterieur", collaborateurToCopyFrom.getValue("SoldeAnterieur"));
        user.getExtendedAttributes().setValue("SoldeConges", collaborateurToCopyFrom.getValue("SoldeConges"));
        user.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation", collaborateurToCopyFrom.getValue("CongesPayesEnCoursDeValidation"));
        user.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation", collaborateurToCopyFrom.getValue("CongesPayesEnCoursDeConsommation"));
        user.getExtendedAttributes().setValue("CongesPayesEnCoursDeTraitement", collaborateurToCopyFrom.getValue("CongesPayesEnCoursDeTraitement"));
        user.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeValidation", collaborateurToCopyFrom.getValue("CongesSpeciauxEnCoursDeValidation"));
        user.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeConsommation", collaborateurToCopyFrom.getValue("CongesSpeciauxEnCoursDeConsommation"));
        user.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeTraitement", collaborateurToCopyFrom.getValue("CongesSpeciauxEnCoursDeTraitement"));
        user.getExtendedAttributes().setValue("CongesMaladieEnCoursDeValidation", collaborateurToCopyFrom.getValue("CongesMaladieEnCoursDeValidation"));
        user.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation", collaborateurToCopyFrom.getValue("CongesMaladieEnCoursDeConsommation"));
        user.getExtendedAttributes().setValue("CongesMaladieEnCoursDeTraitement", collaborateurToCopyFrom.getValue("CongesMaladieEnCoursDeTraitement"));
        user.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation", collaborateurToCopyFrom.getValue("CongesSansSoldeEnCoursDeValidation"));
        user.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeConsommation", collaborateurToCopyFrom.getValue("CongesSansSoldeEnCoursDeConsommation"));
        user.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeTraitement", collaborateurToCopyFrom.getValue("CongesSansSoldeEnCoursDeTraitement"));
        user.getExtendedAttributes().setValue("JourSEnCoursDeValidation", collaborateurToCopyFrom.getValue("JourSEnCoursDeValidation"));
        user.getExtendedAttributes().setValue("joursEnCoursConsommation", collaborateurToCopyFrom.getValue("JourSEnCoursDeConsommation"));
        user.getExtendedAttributes().setValue("JourSEnCoursDeTraitement", collaborateurToCopyFrom.getValue("JourSEnCoursDeTraitement"));
        user.getExtendedAttributes().setValue("CongesPayesAnneeEnCours", collaborateurToCopyFrom.getValue("CongesPayesAnneeEnCours"));
        user.getExtendedAttributes().setValue("CongesPayesN1", collaborateurToCopyFrom.getValue("CongesPayesN1"));
        user.getExtendedAttributes().setValue("CongesPayesPris", collaborateurToCopyFrom.getValue("CongesPayesPris"));
        user.getExtendedAttributes().setValue("CongesSpeciauxAnneeEnCours", collaborateurToCopyFrom.getValue("CongesSpeciauxAnneeEnCours"));
        user.getExtendedAttributes().setValue("CongesSpeciauxN1", collaborateurToCopyFrom.getValue("CongesSpeciauxN1"));
        user.getExtendedAttributes().setValue("CongesSpeciauxPris", collaborateurToCopyFrom.getValue("CongesSpeciauxPris"));
        user.getExtendedAttributes().setValue("CongesMaladieAnneeEnCours", collaborateurToCopyFrom.getValue("CongesMaladieAnneeEnCours"));
        user.getExtendedAttributes().setValue("CongesMaladieN1", collaborateurToCopyFrom.getValue("CongesMaladieN1"));
        user.getExtendedAttributes().setValue("CongesMaladiePris", collaborateurToCopyFrom.getValue("CongesMaladiePris"));
        user.getExtendedAttributes().setValue("CongesSansSoldeAnneeEnCours", collaborateurToCopyFrom.getValue("CongesSansSoldeAnneeEnCours"));
        user.getExtendedAttributes().setValue("CongesSansSoldeN1", collaborateurToCopyFrom.getValue("CongesSansSoldeN1"));
        user.getExtendedAttributes().setValue("CongesSansSoldePris", collaborateurToCopyFrom.getValue("CongesSansSoldePris"));
        user.getExtendedAttributes().setValue("TotalJoursPris", collaborateurToCopyFrom.getValue("TotalJoursPris"));
        user.getExtendedAttributes().setValue("CategorieDeSalairee", collaborateurToCopyFrom.getValue("CategorieDeSalaire"));
        user.save(Modules.getWorkflowModule().getSysadminContext());
        collaborateurToCopyFrom.setValue("MotDePasse",null);
        collaborateurToCopyFrom.save(Modules.getWorkflowModule().getSysadminContext());
        return user;
    }

    public void UpdateN1() {
        for (Map.Entry<IStorageResource, String> entry : N1Map.entrySet()) {
            IStorageResource collaborateur = entry.getKey();
            String N1CIN = entry.getValue();
            if(collaborateur==null){
                continue;
            }
            if (N1CIN != null) {
                IUser N1 = getUserByCIN(N1CIN);
                if (N1 != null) {
                    collaborateur.setValue("HierarchicalManager",N1);
                    collaborateur.save(Modules.getWorkflowModule().getSysadminContext());
                    IUser salarie = (IUser) collaborateur.getValue("Salarie");
                    salarie.setHierarchicalManager(N1);
                    salarie.save(Modules.getWorkflowModule().getSysadminContext());

                } else {
                    //addAnomalie(AnnuaireFromExcel.AnomalieTypes.A,"Merci de vérifier le matricule du responsable hiérarchique "+matricule,index,"Responsable hiérarchique");
                }
            }
            // ...
        }

    }

    public void UpdateEvaluateur() {
        for (Map.Entry<IStorageResource, String> entry : EvaluateurMap.entrySet()) {
            IStorageResource collaborateur = entry.getKey();
            String N1CIN = entry.getValue();
            if(collaborateur==null){
                continue;
            }
            if (N1CIN != null) {
                IUser N1 = getUserByCIN(N1CIN);
                if (N1 != null) {
                    collaborateur.setValue("Evaluateur",N1);
                    collaborateur.save(Modules.getWorkflowModule().getSysadminContext());
                    IUser salarie = (IUser) collaborateur.getValue("Salarie");
                    salarie.getExtendedAttributes().setValue("Evaluateur",N1);
                    salarie.save(Modules.getWorkflowModule().getSysadminContext());

                } else {
                    //addAnomalie(AnnuaireFromExcel.AnomalieTypes.A,"Merci de vérifier le matricule du responsable hiérarchique "+matricule,index,"Responsable hiérarchique");
                }
            }
            // ...
        }

    }

    public void UpdateSuppleants() {
        for (Map.Entry<IStorageResource, String> entry : SuppleantMap.entrySet()) {
            IStorageResource collaborateur = entry.getKey();
            String N1CIN = entry.getValue();
            if(collaborateur==null){
                continue;
            }
            if (N1CIN != null) {
                IUser N1 = getUserByCIN(N1CIN);
                if (N1 != null) {
                    collaborateur.setValue("Suppleant",N1);
                    collaborateur.save(Modules.getWorkflowModule().getSysadminContext());
                    IUser salarie = (IUser) collaborateur.getValue("Salarie");
                    salarie.getExtendedAttributes().setValue("Suppleant",N1);
                    salarie.save(Modules.getWorkflowModule().getSysadminContext());

                } else {
                    //addAnomalie(AnnuaireFromExcel.AnomalieTypes.A,"Merci de vérifier le matricule du responsable hiérarchique "+matricule,index,"Responsable hiérarchique");
                }
            }
            // ...
        }

    }

    IUser getUserByCIN(String cin){
        IUser user = null;
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("CIN", cin);
            Collection<IStorageResource> fiches = controller.evaluate(definition);
            if (fiches != null && !fiches.isEmpty()) {
                IStorageResource ficheSalarie = fiches.iterator().next();
                user = (IUser) ficheSalarie.getValue("Salarie");
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }
}
