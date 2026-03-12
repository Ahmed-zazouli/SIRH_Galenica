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

public class Societe {


    HashMap<Object,Integer> jours ;
    List<String> hours ;
    List<String> minutes;
    boolean isAnomalieFound = false;
    //ClassListHelper helper = new ClassListHelper();
    public List<String> societe = Arrays.asList("_Pole","_Societe","_Adresse","_Ville","_ResponsableRH","_ResponsableDEV","_Directeur","_JoursOuvrablesDebut","_JoursOuvrablesFin","_JoursOuvresDebut","_JoursOuvresFin","_TypeCongesSpeciaux","_HeureMinuteDebutPremiereShift","_HeureMinuteFinPremiereShift","_HeureMinuteDebutDeuxiemeShift","_HeureMinuteFinDeuxiemeShift","_PlafondDeDelaiAvanceSurSalaire","_TypeCloture","_PeriodeDeCloture");

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

                if(poleValue.equals("") || societeValue.equals("")){
                    //ANOMALIE
                    continue;
                }
                IStorageResource ressource = getOrCreateStorage(poleValue,societeValue);
                for(String colonne : societe){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,ArrayList.class,IStorageResource.class, Row.class,Cell.class);
                        method.invoke(this,alertAnomalis, ressource,row,row.getCell(societe.indexOf(colonne)));
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























   /* public IStorageResource createStorage() {
        try{
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"Societe");
            return Modules.getWorkflowModule().createStorageResource(context,definition,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public List<String> getColumns() {


        return societe;
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

    public boolean _Societe(ArrayList<JSONObject> alertAnomalis , IStorageResource instance, Row row, Cell cell){
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

    public boolean _Adresse(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    instance.setValue("Adresse",cellValue);
                }else{
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Adresse","Merci de vérifier l'adresse");
                    //isAnomalieFound = true;
                }
            }else{
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Adresse","Merci d'ajouter l'adresse");
                //isAnomalieFound=true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;

    }

    public boolean _Ville(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    instance.setValue("Ville",cellValue);
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Ville","Merci de vérifier la ville");

                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Ville","Merci d'ajouter la ville");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;

    }

    public boolean _ResponsableRH(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    String[] cins = ((String) cellValue).split(";");
                    ArrayList<IUser> users = new ArrayList<>();
                    for(String cin : cins){
                        HashMap<String,Object> filter = new HashMap<>();
                        filter.put("CIN",cin);
                        IStorageResource responsableRH =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                        if(responsableRH!=null){
                            users.add((IUser) responsableRH.getValue("Salarie"));
                           // instance.setValue("ResponsableRH",responsableRH.getValue("Salarie"));
                        }else{
                            addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Responsable RH","Merci de vérifier le responsable RH");

                        }
                    }
                     instance.setValue("ResponsableRH",users);

                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Responsable RH","Merci de vérifier le responsable RH");
                }
            }else{
                //  isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Responsable RH","Merci d'ajouter le responsable RH");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _ResponsableDEV(ArrayList<JSONObject> alertAnomalis, IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    String[] cins = ((String) cellValue).split(";");
                    ArrayList<IUser> users = new ArrayList<>();
                    for(String cin : cins){
                        HashMap<String,Object> filter = new HashMap<>();
                        filter.put("CIN",cin);
                        IStorageResource responsableRH =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                        if(responsableRH!=null){
                            users.add((IUser) responsableRH.getValue("Salarie"));
                            // instance.setValue("ResponsableRH",responsableRH.getValue("Salarie"));
                        }else{
                            addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Responsable RH","Merci de vérifier le responsable RH");

                        }
                    }
                    instance.setValue("ResponsableDev",users);

                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Responsable RH","Merci de vérifier le responsable RH");
                }
            }else{
                //  isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Responsable RH","Merci d'ajouter le responsable RH");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _Directeur(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
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
                    IStorageResource directeurSociete =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                    if(directeurSociete!=null){
                        instance.setValue("DirecteurSociete",directeurSociete.getValue("Salarie"));
                    }else{
                        // isAnomalieFound = true;
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Directeur","Merci de vérifier le directeur");

                    }
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Directeur","Merci de vérifier le directeur");

                }
            }else{
                //  isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Directeur","Merci d'ajouter le directeur");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _JoursOuvrablesDebut(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    Integer jourIndex = handleJour(cellValue);
                    if(jourIndex!= -1){
                        instance.setValue("JoursOuvrablesDebut",jourIndex);
                    }else{
                        //isAnomalieFound = true;
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvrables début","Merci de vérifier le jour ouvrable début");

                    }
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvrables début","Merci de vérifier le jour ouvrable début");
                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvrables début","Merci d'ajouter le jour ouvrable début");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _JoursOuvrablesFin(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    Integer jourIndex = handleJour(cellValue);
                    if(jourIndex!= -1){
                        instance.setValue("JoursOuvrablesFin",jourIndex);
                    }else{
                        //isAnomalieFound = true;
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvrables fin","Merci de vérifier le jour ouvrable fin");

                    }
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvrables fin","Merci de vérifier le jour ouvrable fin");
                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvrables fin","Merci d'ajouter le jour ouvrable fin");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _JoursOuvresDebut(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    Integer jourIndex = handleJour(cellValue);
                    if(jourIndex!= -1){
                        instance.setValue("JoursOuvresDebut",jourIndex);
                    }else{
                        //isAnomalieFound = true;
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvres début","Merci de vérifier le jour ouvres début");

                    }
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvres début","Merci de vérifier le jour ouvres début");

                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvres début","Merci d'ajouter le jour ouvres début");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _JoursOuvresFin(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    Integer jourIndex = handleJour(cellValue);
                    if(jourIndex!= -1){
                        instance.setValue("JoursOuvresFin",jourIndex);
                    }else{
                        //isAnomalieFound = true;
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvres fin","Merci de vérifier le jour ouvres fin");
                    }
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvres fin","Merci de vérifier le jour ouvres fin");

                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Jours ouvres fin","Merci d'ajouter le jour ouvres fin");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _TypeCongesSpeciaux(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    HashMap possibleValues = new HashMap<String, String>() {{
                        put("Calendaire", "Calendrier");
                        put("Ouvrable", "Ouvrables");

                    }};
                    cellValue = handleList(cellValue,possibleValues);
                    if(cellValue!= null){
                        instance.setValue("CongeSpeciaux",cellValue);
                    }else{
                        //isAnomalieFound = true;
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type calcul congés spéciaux","Merci de vérifier le type calcul congés spéciaux");

                    }
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type calcul congés spéciaux","Merci de vérifier le type calcul congés spéciaux");
                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type calcul congés spéciaux","Merci d'ajouter le type calcul congés spéciaux");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _HeureMinuteDebutPremiereShift(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    String[] heureMinute = handleHeureMinute(cellValue);
                    if(heureMinute != null){
                        instance.setValue("HeureDebutPremiereShift",heureMinute[0]);
                        instance.setValue("MinuteDebutPremiereShift",heureMinute[1]);
                    }else{
                        //isAnomalieFound = true;
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure début première shift","Merci de vérifier l'heure début première shift");

                    }
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure début première shift","Merci de vérifier l'heure début première shift");

                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure début première shift","Merci d'ajouter l'heure début première shift");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _HeureMinuteFinPremiereShift(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    String[] heureMinute = handleHeureMinute(cellValue);
                    if(heureMinute != null){
                        instance.setValue("HeureFinPremiereShift",heureMinute[0]);
                        instance.setValue("MinuteFinPremiereShift",heureMinute[1]);
                    }else{
                        //isAnomalieFound = true;
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure fin première shift","Merci de vérifier l'heure fin première shift");

                    }
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure fin première shift","Merci de vérifier l'heure fin première shift");
                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure fin première shift","Merci d'ajouter l'heure fin première shift");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _HeureMinuteDebutDeuxiemeShift(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    String[] heureMinute = handleHeureMinute(cellValue);
                    if(heureMinute != null){
                        instance.setValue("HeureDebutDeuxiemeShift",heureMinute[0]);
                        instance.setValue("MinuteDebutDeuxiemeShift",heureMinute[1]);
                    }else{
                        //isAnomalieFound = true;
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure début deuxième shift","Merci de vérifier l'heure début deuxième shift");

                    }
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure début deuxième shift","Merci de vérifier l'heure début deuxième shift");

                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure début deuxième shift","Merci d'ajouter l'heure début deuxième shift");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _HeureMinuteFinDeuxiemeShift(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    String[] heureMinute = handleHeureMinute(cellValue);
                    if(heureMinute != null){
                        instance.setValue("HeureFinDeuxiemeShift",heureMinute[0]);
                        instance.setValue("MinuteFinDeuxiemeShift",heureMinute[1]);
                    }else{
                        //isAnomalieFound = true;
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure fin deuxième shift","Merci de vérifier l'heure fin deuxième shift");

                    }
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure fin deuxième shift","Merci de vérifier l'heure fin deuxième shift");

                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Heure fin deuxième shift","Merci d'ajouter l'heure fin deuxième shift");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _PlafondDeDelaiAvanceSurSalaire(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    int delai = handleNumber(cellValue);
                    if(delai!= -1){
                        instance.setValue("PlafondDeDelaiAvanceSurSalaire",delai);
                    }else{
                        //ANOMALIE
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Autoriser les demandes d'avance sur salaire avant le (J)","Merci de vérifier la valeur saisie");

                    }

                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Autoriser les demandes d'avance sur salaire avant le (J)","Merci de vérifier la valeur saisie");
                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Autoriser les demandes d'avance sur salaire avant le (J)","Merci d'ajouter la valeur saisie");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _TypeCloture(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    HashMap possibleValues = new HashMap<String, String>() {{
                        put("Mois", "Mois");
                        put("Période", "Période");

                    }};
                    cellValue = handleList(cellValue,possibleValues);
                    if(cellValue!= null){
                        instance.setValue("ClotureSociete",cellValue);
                    }else{
                        //isAnomalieFound = true;
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type Clôture","Merci de vérifier le type Clôture");

                    }
                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type Clôture","Merci de vérifier le type Clôture");

                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Type Clôture","Merci d'ajouter le type Clôture");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    public boolean _PeriodeDeCloture(ArrayList<JSONObject> alertAnomalis,IStorageResource instance, Row row, Cell cell){
        try{
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null && !cellValue.equals("")){
                    int delai = handleNumber(cellValue);
                    if(delai!= -1){
                        instance.setValue("PeriodeDeCloture",delai);
                    }else{
                        //ANOMALIE
                        addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Période de clôture","Merci de vérifier le période de clôture");

                    }

                }else{
                    //isAnomalieFound = true;
                    addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Période de clôture","Merci de vérifier le période de clôture");
                }
            }else{
                //isAnomalieFound=true;
                addAlerte(alertAnomalis,row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Période de clôture","Merci d'ajouter le période de clôture");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isAnomalieFound;
    }

    private int handleNumber(Object cellValue) {
        if(cellValue instanceof Number){
            return ((Number) cellValue).intValue();
        }
        return -1;
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

    Integer handleJour(Object jour){
        jours =  new HashMap<Object, Integer>() {{
            put("Lundi", 2);
            put("Mardi", 3);
            put("Mercredi", 4);
            put("Jeudi", 5);
            put("Vendredi", 6);
            put("Samedi", 7);
            put("Dimanche", 1);

        }};
        if(jours.get(jour)!=null){
            return jours.get(jour);
        }
        return -1;
    }

    Object handleList(Object item,HashMap<String, String> possibleValues){
        if(possibleValues.get(item)!=null){
            return possibleValues.get(item);
        }
        return null;
    }

    String[] handleHeureMinute(Object value){
        hours = Arrays.asList("00","01","02","03","04","05","06","07","08","09",
                "10","11","12","13","14","15","16","17","18","19","20",
                "21","22","23");
        minutes = Arrays.asList(
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                "21", "22", "23",
                "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35",
                "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47",
                "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"
        );
        if(value instanceof String){
            String stringValue = (String) value;
            if(stringValue.contains(":")){
                String[] data = stringValue.split(":");
                if(data.length==2){
                    String hour = data[0];
                    String minute = data[1];
                    if(hours.contains(hour)){
                        if(hour.startsWith("0")){
                            hour = hour.substring(1);
                        }
                    }else{
                        //ANOMALIE HOUR
                        return null;
                    }
                    if(minutes.contains(minute)){
                        if(minute.startsWith("0")){
                            minute = minute.substring(1);
                        }
                    }else{
                        //ANOMALIE minutes
                        return null;
                    }
                    return new String[]{hour,minute};
                }else{
                    return null;
                }
            }else {
                return null;
            }
        }else{
            return null;
        }
    }

    public IStorageResource getOrCreateStorage(String pole , String societe) {
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"Societe");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            controller.addEqualsConstraint("Pole.sys_Title",pole);
            controller.addEqualsConstraint("sys_Title",societe);
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
}
