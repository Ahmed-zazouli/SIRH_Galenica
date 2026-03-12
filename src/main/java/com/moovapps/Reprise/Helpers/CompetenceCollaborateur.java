package com.moovapps.Reprise.Helpers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.axemble.vdoc.sdk.ModulesFactory.getWorkflowModule;

public class CompetenceCollaborateur {
    ClassListHelper helper = new ClassListHelper();
    boolean isAnomalieFound = false;
    private List<String> competenceCollaborateur = Arrays.asList("_Collaborateur","_Domaine","_FamilleCompetence","_SousFamilleCompetence","_Competence","_NiveauDeCompetence","_Poids","_DateDAcquisitionDeCompetence"/*,"_DateRenouvellementCompetence","_DateDExpiration","_Notation","_Notation2","_ConfirmeCompetence"*/);



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

                Cell domaineCell = row.getCell(1);
                String domaineValue = domaineCell!=null?domaineCell.getStringCellValue().trim():"";

                Cell familleCompetenceCell = row.getCell(2);
                String familleCompetenceValue = familleCompetenceCell!=null?familleCompetenceCell.getStringCellValue().trim():"";

                Cell sousFamilleCompetenceCell = row.getCell(3);
                String sousFamilleCompetenceValue = sousFamilleCompetenceCell!=null?sousFamilleCompetenceCell.getStringCellValue().trim():"";

                Cell competenceCell = row.getCell(4);
                String competenceValue = competenceCell!=null?competenceCell.getStringCellValue().trim():"";


                if(domaineValue.equals("") || familleCompetenceValue.equals("") || sousFamilleCompetenceValue.equals("") || competenceValue.equals("")){
                    //ANOMALIE
                    continue;
                }
                IStorageResource ressource = getOrCreateStorage(collaborateurCINValue, domaineValue,familleCompetenceValue,sousFamilleCompetenceValue,competenceValue);
                for(String colonne : competenceCollaborateur){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,ArrayList.class,IStorageResource.class, Row.class,Cell.class);
                        method.invoke(this,alertAnomalis, ressource,row,row.getCell(competenceCollaborateur.indexOf(colonne)));
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




    public IStorageResource getOrCreateStorage(String cin ,String domaine , String familleCompetence , String sousFamilleCompetence,String competence) {
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"CompetenceSalarie");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            controller.addEqualsConstraint("Domaine.sys_Title",domaine);
            controller.addEqualsConstraint("FamilleCompetence.sys_Title",familleCompetence);
            controller.addEqualsConstraint("SousFamilleCompetence.sys_Title",sousFamilleCompetence);
            controller.addEqualsConstraint("Competence.sys_Title",competence);
            controller.addEqualsConstraint("Collaborateur.CIN",cin);


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
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"CompetenceSalarie");
            return Modules.getWorkflowModule().createStorageResource(context,definition,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public List<String> getColumns() {
        return competenceCollaborateur;
    }


    public boolean _Collaborateur(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
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
                    filter.put("CIN",cellValue);
                    IStorageResource collaborateur =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                    if(collaborateur!=null){
                        instance.setValue("Collaborateur",collaborateur);
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
                isAnomalieFound=true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
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
                if(cellValue!=null && !cellValue.equals("")){
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("sys_Title",cellValue);
                    IStorageResource domaine =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Domaine",filter);
                    if(domaine!=null){
                        instance.setValue("Domaine",domaine);
                    }else{
                        addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Domaine","Merci de vérifier le domaine");
                        isAnomalieFound = true;
                    }
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Domaine","Merci de vérifier le domaine");
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

    public boolean _FamilleCompetence(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
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
                    filter.put("Domaine",instance.getValue("Domaine"));
                    filter.put("sys_Title",cellValue);

                    IStorageResource familleCompetence =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FamilleCompetence",filter);
                    if(familleCompetence!=null){
                        instance.setValue("FamilleCompetence",familleCompetence);
                    }else{
                        addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Famille compétence","Merci de vérifier la famille compétence");
                        isAnomalieFound = true;
                    }
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Famille compétence","Merci de vérifier la famille compétence");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Famille compétence","Merci d'ajouter la famille compétence");
                isAnomalieFound=true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _SousFamilleCompetence(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
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
                    filter.put("FamilleCompetence",instance.getValue("FamilleCompetence"));
                    filter.put("sys_Title",cellValue);

                    IStorageResource sousFamilleCompetence =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","SousFamilleCompetence",filter);
                    if(sousFamilleCompetence!=null){
                        instance.setValue("SousFamilleCompetence",sousFamilleCompetence);
                    }else{
                        addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Sous Famille compétence","Merci de vérifier la sous famille compétence");
                        isAnomalieFound = true;
                    }
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Sous Famille compétence","Merci de vérifier la sous famille compétence");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Sous Famille compétence","Merci d'ajouter la sous famille compétence");
                isAnomalieFound=true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _Competence(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
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
                    filter.put("SousFamilleCompetence",instance.getValue("SousFamilleCompetence"));
                    filter.put("sys_Title",cellValue);

                    IStorageResource competence =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Competence",filter);
                    if(competence!=null){
                        instance.setValue("Competence",competence);
                    }else{
                        addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Compétence","Merci de vérifier la compétence");
                        isAnomalieFound = true;
                    }
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Compétence","Merci de vérifier la compétence");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Compétence","Merci d'ajouter la compétence");
                isAnomalieFound=true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _NiveauDeCompetence(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
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
                    filter.put("Competence",instance.getValue("Competence"));
                    filter.put("sys_Title",cellValue);

                    IStorageResource niveauDeCompetence =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","NiveauDeCompetence",filter);
                    if(niveauDeCompetence!=null){
                        instance.setValue("NiveauDeCompetence",niveauDeCompetence);
                    }else{
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Niveau de compétence","Merci de vérifier le niveau de compétence");
                       // isAnomalieFound = true;
                    }
                }else{
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Niveau de compétence","Merci de vérifier le niveau de compétence");
                  //  isAnomalieFound = true;
                }
            }else{
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Niveau de compétence","Merci d'ajouter le niveau de compétence");
                //isAnomalieFound=true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _Poids(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null){
                    instance.setValue("Poids",new BigDecimal(Double.toString((double)cellValue)));
                }else{
                    addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Poids","Merci de vérifier le poids");
                    isAnomalieFound = true;
                }
            }else{
                addAnomalie(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Poids","Merci d'ajouter le poids");
                isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }

    public boolean _DateDAcquisitionDeCompetence(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
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
                    instance.setValue("DateDAcquisitionDeCompetence",cellValue);
                }else{
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date d'acquisition compétence","Merci de vérifier la date d'acquisition compétence");
                  //  isAnomalieFound = true;
                }
            }else{
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date d'acquisition compétence","Merci d'ajouter la date d'acquisition compétence");
              //  isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }
    public boolean _DateRenouvellementCompetence(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
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
                    instance.setValue("DateRenouvellementCompetence",cellValue);
                }else{
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date renouvellement compétence","Merci de vérifier la date renouvellement compétence");
                 //   isAnomalieFound = true;
                }
            }else{
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date renouvellement compétence","Merci d'ajouter la date renouvellement compétence");
              //  isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }
    public boolean _DateDExpiration(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
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
                    instance.setValue("DateDExpiration",cellValue);
                }else{
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date d'expiration compétence","Merci de vérifier la date d'expiration compétence");
                 //   isAnomalieFound = true;
                }
            }else{
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Date d'expiration compétence","Merci d'ajouter la date d'expiration compétence");
               // isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }




    public boolean _Notation(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null){
                    instance.setValue("Notation0100",cellValue);
                }else{
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Notation (0 - 100)","Merci de vérifier la notation (0 - 100)");
                    // isAnomalieFound = true;
                }
            }else{
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Notation (0 - 100)","Merci d'ajouter la notation (0 - 100)");
                // isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }

    public boolean _Notation2(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null){
                    instance.setValue("Notation0100AFroid",cellValue);
                }else{
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Notation Questionnaire à froid (0 - 100)","Merci de vérifier la notation Questionnaire à froid (0 - 100)");
                    // isAnomalieFound = true;
                }
            }else{
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Notation Questionnaire à froid (0 - 100)","Merci d'ajouter la notation Questionnaire à froid (0 - 100)");
                // isAnomalieFound = true;

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isAnomalieFound;
    }

    public boolean _ConfirmeCompetence(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Boolean cellValue = handleBoolean(cell);
                if(cellValue!=null){
                    instance.setValue("ConfirmeCompetence",cellValue);
                }else{
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Compétence confimée","Merci de vérifier la confirmation compétence");

                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Compétence confimée","Merci d'ajouter la confirmation compétence");
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
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"CompetenceSalarie");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            List<String> columns = Arrays.asList("-Collaborateur","_Domaine","_FamilleCompetence","_SousFamilleCompetence","_Competence");
            for (String column : columns) {
                Cell cell = row.getCell(columns.indexOf(column));
                String cellValue = cell != null ? cell.getStringCellValue().trim() : "";
                if (!cellValue.equals("")) {
                    if(column.startsWith("-")){
                        controller.addEqualsConstraint(column.replace("-",""+".CIN"),cellValue);
                    }else if(column.startsWith("_")){
                        controller.addEqualsConstraint(column.replace("_",""+".sys_Title"),cellValue);
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
