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
import java.util.*;


public class Societe extends BaseAgent {
    private boolean isAnomalieFound = false;
    HashMap<Object,Integer> jours ;
    List<String> hours ;
    List<String> minutes;
    @Override
    protected void execute() {
        try {
            jours =  new HashMap<Object, Integer>() {{
                put("Lundi", 2);
                put("Mardi", 3);
                put("Mercredi", 4);
                put("Jeudi", 5);
                put("Vendredi", 6);
                put("Samedi", 7);
                put("Dimanche", 1);

            }};
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

            // Specify the path to your Excel file
            String filePath = "C://import//DISLOG//Societe.xlsx";
            Columns columnsHelper = new Columns();
            List<String> columns = columnsHelper.societe;
            // Create a FileInputStream to read the Excel file
            FileInputStream fis = new FileInputStream(new File(filePath));

            // Create a workbook object to represent the Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(fis);

            // Get the first sheet in the workbook
            // (You can modify this to loop through all sheets if needed)
            Sheet sheet = workbook.getSheetAt(0);

            // Iterate through each row in the sheet
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                isAnomalieFound = false;
                Row row = sheet.getRow(rowIndex);
                if(rowIndex==0){
                    //Verify maquette
                    //if false break;
                    continue;
                }
                Cell poleCell = row.getCell(0);
                Cell societeCell = row.getCell(1);

                String poleTEXT = poleCell!=null?poleCell.getStringCellValue().trim():"";
                String societeTEXT = societeCell!=null?societeCell.getStringCellValue().trim():"";

                //Create pole storage
                IStorageResource societe = createStorage("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Societe",poleTEXT,societeTEXT);
                for(String colonne : columns){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,IStorageResource.class, Row.class,Cell.class);
                        method.invoke(this, societe,row,row.getCell(columns.indexOf(colonne)));
                    }
                }
                if(!isAnomalieFound){
                    societe.save(getWorkflowModule().getSysadminContext());
                }
                System.out.println();
            }

            // Close the workbook and FileInputStream
            workbook.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IStorageResource createStorage(String organizationName ,  String projetName, String catalogName, String definitionName,String pole , String societe) {
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,organizationName);
            IProject projet = getProjectModule().getProject(context,projetName,organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,catalogName,ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,definitionName);

            IViewController controller = getWorkflowModule().getViewController(context,IResource.class);
            controller.addEqualsConstraint("Pole.sys_Title",pole);
            controller.addEqualsConstraint("sys_Title",societe);
            ArrayList<IStorageResource> data = (ArrayList<IStorageResource>) controller.evaluate(definition);
            if(data!=null && !data.isEmpty()){
                return data.iterator().next();
            }

            return getWorkflowModule().createStorageResource(context,definition,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void _Pole(IStorageResource instance, Row row, Cell cell){
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
                IStorageResource pole =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Pole",filter);
                if(pole!=null){
                    instance.setValue("Pole",pole);
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

    public void _Societe(IStorageResource instance, Row row, Cell cell){
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
                isAnomalieFound = true;
            }
        }else{
            isAnomalieFound=true;
        }
    }

    public void _Adresse(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("Adresse",cellValue);
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _Ville(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("Ville",cellValue);
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _ResponsableRH(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                String cellValueTEXT = (String) cellValue;
                String[] valeurs = cellValueTEXT.split(";");
                ArrayList<IUser> users = new ArrayList<>();
                if(valeurs!=null){
                    for(String cin : valeurs){
                        HashMap<String,Object> filter = new HashMap<>();
                        filter.put("CIN",cin);
                        IStorageResource responsableRH =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                        if(responsableRH!=null){
                            IUser salarie = (IUser) responsableRH.getValue("Salarie");
                            if(salarie!=null && !users.contains(salarie)){
                                users.add(salarie);
                            }

                        }else{
                            // isAnomalieFound = true;
                        }
                    }
                    instance.setValue("ResponsableRH",users);

                }

            }else{
                //isAnomalieFound = true;
            }
        }else{
          //  isAnomalieFound=true;
        }
    }

    public void _ResponsableDEV(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                String cellValueTEXT = (String) cellValue;
                String[] valeurs = cellValueTEXT.split(";");
                ArrayList<IUser> users = new ArrayList<>();
                if(valeurs!=null){
                    for(String cin : valeurs){
                        HashMap<String,Object> filter = new HashMap<>();
                        filter.put("CIN",cin);
                        IStorageResource responsableRH =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                        if(responsableRH!=null){
                            IUser salarie = (IUser) responsableRH.getValue("Salarie");
                            if(salarie!=null && !users.contains(salarie)){
                                users.add(salarie);
                            }

                        }else{
                            // isAnomalieFound = true;
                        }
                    }
                    instance.setValue("ResponsableDev",users);

                }

            }else{
                //isAnomalieFound = true;
            }
        }else{
            //  isAnomalieFound=true;
        }
    }

    public void _Directeur(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("CIN",cellValue);
                IStorageResource directeurSociete =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                if(directeurSociete!=null){
                    instance.setValue("DirecteurSociete",directeurSociete.getValue("Salarie"));
                }else{
                    // isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //  isAnomalieFound=true;
        }
    }

    public void _JoursOuvrablesDebut(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                Integer jourIndex = handleJour(cellValue);
                if(jourIndex!= -1){
                    instance.setValue("JoursOuvrablesDebut",jourIndex);
                }else{
                    //isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _JoursOuvrablesFin(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                Integer jourIndex = handleJour(cellValue);
                if(jourIndex!= -1){
                    instance.setValue("JoursOuvrablesFin",jourIndex);
                }else{
                    //isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _JoursOuvresDebut(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                Integer jourIndex = handleJour(cellValue);
                if(jourIndex!= -1){
                    instance.setValue("JoursOuvresDebut",jourIndex);
                }else{
                    //isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _JoursOuvresFin(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                Integer jourIndex = handleJour(cellValue);
                if(jourIndex!= -1){
                    instance.setValue("JoursOuvresFin",jourIndex);
                }else{
                    //isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _TypeCongesSpeciaux(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap possibleValues = new HashMap<String, String>() {{
                    put("Calendaire", "Calendrier");
                    put("Ouvrable", "Ouvrables");

                }};
                cellValue = handleList(cellValue,possibleValues);
                if(cellValue!= null){
                    instance.setValue("CongeSpeciaux",cellValue);
                }else{
                    //isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _HeureMinuteDebutPremiereShift(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
               String[] heureMinute = handleHeureMinute(cellValue);
                if(heureMinute != null){
                    instance.setValue("HeureDebutPremiereShift",heureMinute[0]);
                    instance.setValue("MinuteDebutPremiereShift",heureMinute[1]);
                }else{
                    //isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _HeureMinuteFinPremiereShift(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                String[] heureMinute = handleHeureMinute(cellValue);
                if(heureMinute != null){
                    instance.setValue("HeureFinPremiereShift",heureMinute[0]);
                    instance.setValue("MinuteFinPremiereShift",heureMinute[1]);
                }else{
                    //isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _HeureMinuteDebutDeuxiemeShift(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                String[] heureMinute = handleHeureMinute(cellValue);
                if(heureMinute != null){
                    instance.setValue("HeureDebutDeuxiemeShift",heureMinute[0]);
                    instance.setValue("MinuteDebutDeuxiemeShift",heureMinute[1]);
                }else{
                    //isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _HeureMinuteFinDeuxiemeShift(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                String[] heureMinute = handleHeureMinute(cellValue);
                if(heureMinute != null){
                    instance.setValue("HeureFinDeuxiemeShift",heureMinute[0]);
                    instance.setValue("MinuteFinDeuxiemeShift",heureMinute[1]);
                }else{
                    //isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _PlafondDeDelaiAvanceSurSalaire(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                int delai = handleNumber(cellValue);
                if(delai!= -1){
                    instance.setValue("PlafondDeDelaiAvanceSurSalaire",delai);
                }else{
                    //ANOMALIE
                }

            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _TypeCloture(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap possibleValues = new HashMap<String, String>() {{
                    put("Mois", "Mois");
                    put("Période", "Période");

                }};
                cellValue = handleList(cellValue,possibleValues);
                if(cellValue!= null){
                    instance.setValue("ClotureSociete",cellValue);
                }else{
                    //isAnomalieFound = true;
                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _PeriodeDeCloture(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                int delai = handleNumber(cellValue);
                if(delai!= -1){
                    instance.setValue("PeriodeDeCloture",delai);
                }else{
                    //ANOMALIE
                }

            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
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

    Integer handleJour(Object jour){
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
}
