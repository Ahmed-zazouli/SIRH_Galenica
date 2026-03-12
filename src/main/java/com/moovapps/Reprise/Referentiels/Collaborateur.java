package com.moovapps.Reprise.Referentiels;

import com.axemble.vdoc.core.helpers.PasswordHelper;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.impl.ProcessWorkflowModule;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Cummon.Agents.AnnuaireFromExcel;
import com.moovapps.ibb.rh.Script.Referentiel.ImportationMain;
import com.moovapps.ibb.rh.Script.Referentiel.NatureCommentaireExcel;
import org.apache.commons.io.FileUtils;
import org.apache.ecs.html.S;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Collaborateur extends BaseAgent {
    private boolean isAnomalieFound = false;
    private boolean isUpdate = false;
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);
    HashMap<String,String> groupesMapper = new HashMap<>();
    HashMap<String,String> secretGroupesMapper = new HashMap<>();

    HashMap<IStorageResource,String> N1Map = new HashMap<>();
    HashMap<IStorageResource,String> EvaluateurMap = new HashMap<>();
    HashMap<IStorageResource,String> SuppleantMap = new HashMap<>();


    @Override
    protected void execute() {

        /*ImportationMain importation = new ImportationMain(getWorkflowModule(), null, getDirectoryModule().getLoggedOnUserContext());
        importation.importFromExcelToRef(true);*/


        groupesMapper.put("Salarié","21");
        groupesMapper.put("SALARIE","21");

        groupesMapper.put("Manager","23");
        groupesMapper.put("MANAGER","23");
        groupesMapper.put("RH","10103");
        groupesMapper.put("ADMIN","24");
        groupesMapper.put("News readers","1");

        secretGroupesMapper.put("RH","RH");
        secretGroupesMapper.put("Manager","Manager");
        secretGroupesMapper.put("MANAGER","Manager");


        try{
            String filePath = "C://import//GALENICA//Collaborateurs.xlsx";
            Columns columnsHelper = new Columns();
            List<String> columns = columnsHelper.collaborateur;
            // Create a FileInputStream to read the Excel file
            FileInputStream fis = new FileInputStream(new File(filePath));

            // Create a workbook object to represent the Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(fis);

            // Get the first sheet in the workbook
            // (You can modify this to loop through all sheets if needed)
            Sheet sheet = workbook.getSheetAt(0);
            System.out.println(sheet.getLastRowNum());

            // Iterate through each row in the sheet
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                isAnomalieFound = false;
                isUpdate = false;
                Row row = sheet.getRow(rowIndex);
                if(rowIndex==0 || rowIndex==1){
                    //Verify maquette
                    //if false break;
                    continue;
                }
                String CIN = row.getCell(8)!=null?row.getCell(8).getStringCellValue().trim():"";
                //Create pole storage
                IStorageResource collaborateur = createOrGetStorage("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",CIN);
                for(String colonne : columns){
                    if(colonne.startsWith("_")){
                        Method method = this.getClass().getMethod(colonne,IStorageResource.class, Row.class, Cell.class);
                        method.invoke(this, collaborateur,row,row.getCell(columns.indexOf(colonne)));
                    }
                }
                if(!isAnomalieFound){
                    collaborateur.save(getWorkflowModule().getSysadminContext());
                    CreateOrUpdateUser(collaborateur,isUpdate);
                }
                System.out.println(rowIndex);
            }
            UpdateN1();
            UpdateEvaluateur();
            UpdateSuppleants();

            // Close the workbook and FileInputStream
            workbook.close();
            fis.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void CreateOrUpdateUser(IStorageResource collaborateur, boolean isUpdate) {
        String login = (String) collaborateur.getValue("Identifiant");
        String password = (String) collaborateur.getValue("MotDePasse");
        IStorageResource societe = (IStorageResource) collaborateur.getValue("Societe");
        IOrganization societeOrganization = (IOrganization) societe.getValue("Organisation");
        try{
            IUser salarie = null;
            if(isUpdate){
                salarie = (IUser) collaborateur.getValue("Salarie");
            }else{
                salarie = getDirectoryModule().createUser(getWorkflowModule().getSysadminContext(),login,password,societeOrganization);
            }
            setUserFields(salarie,collaborateur,societeOrganization);

        }catch (Exception e){
            e.printStackTrace();
        }
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
                HashMap<String,Object> filter = new HashMap<>();
               // filter.put("Pole",instance.getValue("Pole"));
                filter.put("sys_Title",cellValue);
                IStorageResource societe =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Societe",filter);
                if(societe!=null){
                    instance.setValue("Societe",societe);
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

    public void _Matricule(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = ((Number) cell.getNumericCellValue()).intValue()+"";
            }
            if(cellValue!=null){
                instance.setValue("Matricule",cellValue);
            }else{
                instance.setValue("Matricule",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Matricule",null);
            // isAnomalieFound=true;
        }
    }

    public void _Actif(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim().toLowerCase();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                if(cellValue.equals("oui")){
                    instance.setValue("Actif",true);

                }else if(cellValue.equals("non")){
                    instance.setValue("Actif",false);

                }else{
                    instance.setValue("Actif",null);
                    //isAnomalieFound = true;
                }

            }else{
                instance.setValue("Actif",null);
                //isAnomalieFound = true;
            }
        }else{
            instance.setValue("Actif",null);
            //isAnomalieFound=true;
        }
    }

    public void _Civilite(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap possibleValues = new HashMap<String, String>() {{
                    put("M.", "Mr");
                    put("Monsieur", "Mr");
                    put("Mlle", "Mlle");
                    put("Mme", "Mme");
                    put("Madame", "Mme");

                }};
                cellValue = handleList(cellValue,possibleValues);
                if(cellValue!= null){
                    instance.setValue("Title",cellValue);
                }else{
                    instance.setValue("Title",null);
                    //isAnomalieFound = true;
                }
            }else{
                instance.setValue("Title",null);
                //isAnomalieFound = true;
            }
        }else{
            instance.setValue("Title",null);
            //isAnomalieFound=true;
        }
    }
    public void _Nom(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("LastName",cellValue);
                setNomPrenom(instance);
            }else{
                isAnomalieFound = true;
            }
        }else{
            isAnomalieFound=true;
        }
    }

    public void _Prenom(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("FirstName",cellValue);
                setNomPrenom(instance);
            }else{
                isAnomalieFound = true;
            }
        }else{
            isAnomalieFound=true;
        }
    }

    public void _NumTel(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("CodeMobile",cellValue);
            }else{
                instance.setValue("CodeMobile",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("CodeMobile",null);
            // isAnomalieFound=true;
        }
    }

    public void _CodeMobile(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("MobilePhoneNumber",cellValue);
            }else{
                instance.setValue("MobilePhoneNumber",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("MobilePhoneNumber",null);
            // isAnomalieFound=true;
        }
    }

    public void _Extension(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("Extension",cellValue);
            }else{
                instance.setValue("Extension",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Extension",null);
            // isAnomalieFound=true;
        }
    }


    public void _DateSortie(IStorageResource instance, Row row, Cell cell){
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
                    instance.setValue("Exit",cellValue);
                    instance.setValue("DateDeSortie",cellValue);

                }else{
                    instance.setValue("Exit",null);
                    instance.setValue("DateDeSortie",null);

                    //isAnomalieFound = true;
                }
            }else{
                instance.setValue("Exit",null);
                instance.setValue("DateDeSortie",null);

                //isAnomalieFound=true;
            }
        }catch (Exception e){
            //isAnomalieFound=true;
            e.printStackTrace();
        }
    }

    public void _DateDeNaissance(IStorageResource instance, Row row, Cell cell){
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
                    instance.setValue("Birthday",cellValue);
                }else{
                    instance.setValue("Birthday",null);
                    //isAnomalieFound = true;
                }
            }else{
                instance.setValue("Birthday",null);
                //isAnomalieFound=true;
            }
        }catch (Exception e){
            //isAnomalieFound=true;
            e.printStackTrace();
        }
    }

    public void _CIN(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("CIN",cellValue);
            }else{
                isAnomalieFound = true;
            }
        }else{
            isAnomalieFound=true;
        }
    }

    public void _NCNSS(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("NCNSS",cellValue);
            }else{
                instance.setValue("NCNSS",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("NCNSS",null);
            // isAnomalieFound=true;
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
                instance.setValue("Ville",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Ville",null);
            // isAnomalieFound=true;
        }
    }

    public void _MotifSortie(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("MotifSortie",cellValue);
            }else{
                instance.setValue("MotifSortie",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("MotifSortie",null);
            // isAnomalieFound=true;
        }
    }

    public void _Address1(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("Address1",cellValue);
            }else{
                instance.setValue("Address1",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Address1",null);
            // isAnomalieFound=true;
        }
    }

    public void _Adresse2(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("Adresse2",cellValue);
            }else{
                instance.setValue("Adresse2",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Adresse2",null);
            // isAnomalieFound=true;
        }
    }

    public void _EtatCivil(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap possibleValues = new HashMap<String, String>() {{
                    put("Célibataire", "Célibataire");
                    put("Marié(e)", "Marié(e)");
                    put("Divorcé(e)", "Divorcé(e)");
                    put("Veuf(ve)", "Veuf(ve)");

                }};
                cellValue = handleList(cellValue,possibleValues);
                if(cellValue!= null){
                    instance.setValue("EtatCivil",cellValue);
                }else{
                    instance.setValue("EtatCivil",null);
                    //isAnomalieFound = true;
                }
            }else{
                instance.setValue("EtatCivil",null);
                //isAnomalieFound = true;
            }
        }else{
            instance.setValue("EtatCivil",null);
            //isAnomalieFound=true;
        }
    }

    public void _Maladie(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("Maladie",cellValue);
            }else{
                instance.setValue("Maladie",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Maladie",null);
            // isAnomalieFound=true;
        }
    }

    public void _Email(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                if(isValidEmail((String)cellValue)){
                    instance.setValue("Email",cellValue);
                }else{
                    instance.setValue("Email",null);
                    // isAnomalieFound = true;
                }

            }else{
                instance.setValue("Email",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Email",null);
            // isAnomalieFound=true;
        }
    }

    public void _Identifiant(IStorageResource instance, Row row, Cell cell){
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
        if(!isUpdate){
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue()+"";
                }
                if(cellValue!=null){
                    cellValue =
                            Normalizer
                                    .normalize((String) cellValue, Normalizer.Form.NFD)
                                    .replaceAll("[^\\p{ASCII}]", "");
                    instance.setValue("Identifiant",cellValue);
                }else{
                    isAnomalieFound = true;
                }
            }else{
                isAnomalieFound=true;
            }
        }

    }

    public void _MotDePasse(IStorageResource instance, Row row, Cell cell){
        instance.setValue("MotDePasse","D3mo@demo@DEMO");

       /* if(!isUpdate){
            if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
                Object cellValue = null;
                if(cell.getCellType().equals(CellType.STRING)){
                    cellValue = cell.getStringCellValue().trim();
                }else if(cell.getCellType().equals(CellType.NUMERIC)){
                    cellValue = cell.getNumericCellValue();
                }
                if(cellValue!=null){
                    if(PasswordHelper.checkPasswordStrength((String)cellValue,"")){
                        instance.setValue("MotDePasse",cellValue);
                    }else{
                        // isAnomalieFound = true;
                    }

                }else{
                    // isAnomalieFound = true;
                }
            }else{
                // isAnomalieFound=true;
            }
        }*/

    }

    public void _RIB(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
             //   if(isValidRib((String)cellValue)){
                    instance.setValue("NCompteBancaire",cellValue);
              //  }else{
                //    instance.setValue("NCompteBancaire",null);
                    // isAnomalieFound = true;
                //}

            }else{
                instance.setValue("NCompteBancaire",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("NCompteBancaire",null);
            // isAnomalieFound=true;
        }
    }

    public void _Banque(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("Banque",cellValue);
            }else{
                instance.setValue("Banque",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Banque",null);
            // isAnomalieFound=true;
        }
    }

    public void _AgenceBancaire(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("AgenceBancaire",cellValue);
            }else{
                instance.setValue("AgenceBancaire",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("AgenceBancaire",null);
            // isAnomalieFound=true;
        }
    }
    public void _NumeroFax(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("NumeroFax",cellValue);
            }else{
                instance.setValue("NumeroFax",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("NumeroFax",null);
            // isAnomalieFound=true;
        }
    }

    public void _NumeroAgence(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("NumeroDAgence",cellValue);
            }else{
                instance.setValue("NumeroDAgence",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("NumeroDAgence",null);
            // isAnomalieFound=true;
        }
    }

    public void _Domiciliation(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim().toLowerCase();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                if(cellValue.equals("oui")){
                    instance.setValue("Domiciliation",true);

                }else if(cellValue.equals("non")){
                    instance.setValue("Domiciliation",false);

                }else{
                    instance.setValue("Domiciliation",null);
                    //isAnomalieFound = true;
                }

            }else{
                instance.setValue("Domiciliation",null);
                //isAnomalieFound = true;
            }
        }else{
            instance.setValue("Domiciliation",null);
            //isAnomalieFound=true;
        }
    }

    public void _MainLever(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim().toLowerCase();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                if(cellValue.equals("oui")){
                    instance.setValue("MainLever",true);

                }else if(cellValue.equals("non")){
                    instance.setValue("MainLever",false);

                }else{
                    instance.setValue("MainLever",null);
                    //isAnomalieFound = true;
                }

            }else{
                instance.setValue("MainLever",null);
                //isAnomalieFound = true;
            }
        }else{
            instance.setValue("MainLever",null);
            //isAnomalieFound=true;
        }
    }

    public void _DateEmbaucheSociete(IStorageResource instance, Row row, Cell cell){
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
                    instance.setValue("DateDEmbauche",cellValue);
                }else{
                    instance.setValue("DateDEmbauche",null);
                    //isAnomalieFound = true;
                }
            }else{
                instance.setValue("DateDEmbauche",null);
                //isAnomalieFound=true;
            }
        }catch (Exception e){
            instance.setValue("DateDEmbauche",null);
            //isAnomalieFound=true;
            e.printStackTrace();
        }
    }

    public void _DateEmbaucheGroupe(IStorageResource instance, Row row, Cell cell){
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
                    instance.setValue("DateDEmbaucheGroupe",cellValue);
                }else{
                    instance.setValue("DateDEmbaucheGroupe",null);
                    //isAnomalieFound = true;
                }
            }else{
                instance.setValue("DateDEmbaucheGroupe",null);
                //isAnomalieFound=true;
            }
        }catch (Exception e){
            instance.setValue("DateDEmbaucheGroupe",null);
            //isAnomalieFound=true;
            e.printStackTrace();
        }
    }

    public void _TypeContrat(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
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
                    //isAnomalieFound = true;
                }
            }else{
                instance.setValue("ContractType",null);
                //isAnomalieFound = true;
            }
        }else{
            instance.setValue("ContractType",null);
            //isAnomalieFound=true;
        }
    }

    public void _Roles(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                Set<IGroup> cellGroupes = handleGroupes((String) cellValue);

                Set<IGroup> cellSecretGroupes = handleSecretGroupesGroupes((String) cellValue,(IStorageResource) instance.getValue("Societe"));
                if(cellGroupes!= null){
                    /*List<IGroup> groupList = Optional.ofNullable((List<IGroup>) instance.getValue("Groupes"))
                            .orElse(Collections.emptyList());*/

                  //  Set<IGroup> alreadyGroupes = new HashSet<>(groupList);
                  //  Set<IGroup> combinedGroupes = new HashSet<>(alreadyGroupes);
                   // combinedGroupes.addAll(cellGroupes);
                    instance.setValue("Groupes", cellGroupes);
                }

                if(cellSecretGroupes!= null){
                  /*  List<IGroup> groupList = Optional.ofNullable((List<IGroup>) instance.getValue("SecretGroupes"))
                            .orElse(Collections.emptyList());*/

                    //Set<IGroup> alreadySecretGroupes = new HashSet<>(groupList);
                 //   Set<IGroup> combinedGroupes = new HashSet<>(alreadySecretGroupes);
                 //   combinedGroupes.addAll(cellSecretGroupes);
                    instance.setValue("SecretGroupes", cellSecretGroupes);

                }
            }else{
                //isAnomalieFound = true;
            }
        }else{
            //isAnomalieFound=true;
        }
    }

    public void _ResponsableN1(IStorageResource instance, Row row, Cell cell){
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
                IStorageResource hierarchicalManager =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                if(hierarchicalManager!=null){
                    instance.setValue("HierarchicalManager",hierarchicalManager.getValue("Salarie"));
                }else{
                    N1Map.put(instance,(String)cellValue);
                }
            }else{
                instance.setValue("HierarchicalManager",null);
                //isAnomalieFound = true;
            }
        }else{
            instance.setValue("HierarchicalManager",null);
            //  isAnomalieFound=true;
        }
    }

    public void _Evaluateur(IStorageResource instance, Row row, Cell cell){
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
                IStorageResource evaluateur =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","FicheCollaborateur",filter);
                if(evaluateur!=null){
                    instance.setValue("Evaluateur",evaluateur.getValue("Salarie"));
                }else{
                    EvaluateurMap.put(instance,(String)cellValue);
                }
            }else{
                instance.setValue("Evaluateur",null);
                //isAnomalieFound = true;
            }
        }else{
            instance.setValue("Evaluateur",null);
            //  isAnomalieFound=true;
        }
    }

    public void _Suppleants(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                String[] suppleants = ((String) cellValue).split(",");
                Set<IStorageResource> suppleantsStorage = new HashSet<>();
                for(String suppleantCIN : suppleants){
                    suppleantCIN = suppleantCIN.trim();
                    HashMap<String,Object> filter = new HashMap<>();
                    filter.put("CIN",suppleantCIN);
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
                //isAnomalieFound = true;
            }
        }else{
            instance.setValue("Suppleant",null);
            //  isAnomalieFound=true;
        }
    }

    public void _Direction(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Societe",instance.getValue("Societe"));
                filter.put("sys_Title",cellValue);
                IStorageResource direction =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Direction",filter);
                if(direction!=null){
                    instance.setValue("Direction",direction);
                }else{
                    instance.setValue("Direction",null);
                    //isAnomalieFound = true;
                }
            }else{
                instance.setValue("Direction",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Direction",null);
            //  isAnomalieFound=true;
        }
    }

    public void _Departement(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Societe",instance.getValue("Societe"));
                // filter.put("Direction",instance.getValue("Direction"));
                filter.put("sys_Title",cellValue);
                IStorageResource departement =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","departementcommun",filter);
                if(departement!=null){
                    instance.setValue("Departement",departement);
                }else{
                    instance.setValue("Departement",null);
                    // isAnomalieFound = true;
                }
            }else{
                instance.setValue("Departement",null);
                //isAnomalieFound = true;
            }
        }else{
            instance.setValue("Departement",null);
            //isAnomalieFound=true;
        }
    }

    public void _Activite(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Departement",instance.getValue("Departement"));
                filter.put("sys_Title",cellValue);
                IStorageResource activite =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Activite",filter);
                if(activite!=null){
                    instance.setValue("Activite",activite);
                }else{
                    instance.setValue("Activite",null);
                    //   isAnomalieFound = true;
                }
            }else{
                instance.setValue("Activite",null);
                //  isAnomalieFound = true;
            }
        }else{
            instance.setValue("Activite",null);
            //isAnomalieFound=true;
        }
    }

    public void _Service(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Activite",instance.getValue("Activite"));
                filter.put("sys_Title",cellValue);
                IStorageResource service =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Service",filter);
                if(service!=null){
                    instance.setValue("Service",service);
                }else{
                    instance.setValue("Service",null);
                    //     isAnomalieFound = true;
                }
            }else{
                instance.setValue("Service",null);
                //  isAnomalieFound = true;
            }
        }else{
            instance.setValue("Service",null);
            // isAnomalieFound=true;
        }
    }



    public void _Metier(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Societe",instance.getValue("Societe"));
                filter.put("sys_Title",cellValue);
                IStorageResource metier =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Metier",filter);
                if(metier!=null){
                    instance.setValue("Metier",metier);
                }else{
                    instance.setValue("Metier",null);
                    //  isAnomalieFound = true;
                }
            }else{
                instance.setValue("Metier",null);
                //  isAnomalieFound = true;
            }
        }else{
            instance.setValue("Metier",null);
            // isAnomalieFound=true;
        }
    }

    public void _Division(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Societe",instance.getValue("Societe"));
               // filter.put("Direction",instance.getValue("Direction"));
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
            instance.setValue("Division",null);
            //  isAnomalieFound=true;
        }
    }

    public void _Site(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Societe",instance.getValue("Societe"));
                filter.put("sys_Title",cellValue);
                IStorageResource site =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Site",filter);
                if(site!=null){
                    instance.setValue("Site",site);
                }else{
                    instance.setValue("Site",null);
                    //  isAnomalieFound = true;
                }
            }else{
                instance.setValue("Site",null);
                //  isAnomalieFound = true;
            }
        }else{
            instance.setValue("Site",null);
            //  isAnomalieFound=true;
        }
    }

    public void _Fonction(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Societe",instance.getValue("Societe"));
                filter.put("sys_Title",cellValue);
                IStorageResource fonction =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","fonctions",filter);
                if(fonction!=null){
                    instance.setValue("Fonction",fonction);
                }else{
                    instance.setValue("Fonction",null);
                    // isAnomalieFound = true;
                }
            }else{
                instance.setValue("Fonction",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Fonction",null);
            //  isAnomalieFound=true;
        }
    }

    public void _Grade(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Societe",instance.getValue("Societe"));
                //filter.put("Fonction",instance.getValue("Fonction"));

                filter.put("sys_Title",cellValue);
                IStorageResource grade =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Grade",filter);
                if(grade!=null){
                    instance.setValue("Grade",grade);
                }else{
                    instance.setValue("Grade",null);
                    //  isAnomalieFound = true;
                }
            }else{
                instance.setValue("Grade",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Grade",null);
            //  isAnomalieFound=true;
        }
    }

    public void _Categorie(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                //  filter.put("Societe",instance.getValue("Societe"));
                filter.put("sys_Title",cellValue);
                IStorageResource categorie =  handleStorageWithFilter("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","Categorie",filter);
                if(categorie!=null){
                    instance.setValue("Categorie",categorie);
                }else{
                    instance.setValue("Categorie",null);
                    // isAnomalieFound = true;
                }
            }else{
                instance.setValue("Categorie",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("Categorie",null);
            // isAnomalieFound=true;
        }
    }

    public void _ProfilEvaluation(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap<String,Object> filter = new HashMap<>();
                filter.put("Societe",instance.getValue("Societe"));
                filter.put("sys_Title",cellValue);
                IStorageResource profil =  handleStorageWithFilter("DefaultOrganization","EVAL","Referentiels","Profil",filter);
                if(profil!=null){
                    instance.setValue("ProfilEvaluation",profil);
                }else{
                    instance.setValue("ProfilEvaluation",null);
                    // isAnomalieFound = true;
                }
            }else{
                instance.setValue("ProfilEvaluation",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("ProfilEvaluation",null);
            // isAnomalieFound=true;
        }
    }

    public void _TypeDeSalaire(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                HashMap possibleValues = new HashMap<String, String>() {{
                    put("Horaire", "Horaire");
                    put("Mensuel", "Mensuel");

                }};
                cellValue = handleList(cellValue,possibleValues);
                if(cellValue!= null){
                    instance.setValue("TypeDeSalaire",cellValue);
                }else{
                    instance.setValue("TypeDeSalaire",null);
                    //isAnomalieFound = true;
                }
            }else{
                instance.setValue("TypeDeSalaire",null);
                //isAnomalieFound = true;
            }
        }else{
            instance.setValue("TypeDeSalaire",null);
            //isAnomalieFound=true;
        }
    }

    public void _TauxHoraire(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                if(((String)cellValue).contains("%")){
                    cellValue = ((String) cellValue).replace("%","");
                }
                try {
                    //cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("TauxHoraire",cellValue);
            }else{
                instance.setValue("TauxHoraire",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("TauxHoraire",null);
            // isAnomalieFound=true;
        }
    }

    public void _SalaireDeBase(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    //cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("SalaireDeBase",cellValue);
            }else{
                instance.setValue("SalaireDeBase",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("SalaireDeBase",null);
            // isAnomalieFound=true;
        }
    }



    public void _NombreEnfant(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    //cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("NombreEnfants",cellValue);
            }else{
                instance.setValue("NombreEnfants",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("NombreEnfants",null);
            // isAnomalieFound=true;
        }
    }

    public void _SalaireBrut(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("SalaireBrutDH",cellValue);
            }else{
                instance.setValue("SalaireBrutDH",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("SalaireBrutDH",null);
            // isAnomalieFound=true;
        }
    }

    public void _SalaireBrutImposableDH(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    //  cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("SalaireBrutImposableDH",cellValue);
            }else{
                instance.setValue("SalaireBrutImposableDH",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("SalaireBrutImposableDH",null);
            // isAnomalieFound=true;
        }
    }

    public void _RemunerationDH(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("RemunerationDH",cellValue);
            }else{
                instance.setValue("RemunerationDH",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("RemunerationDH",null);
            // isAnomalieFound=true;
        }
    }

    public void _SalaireNETDH(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    //cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("SalaireNETDH",cellValue);
            }else{
                instance.setValue("SalaireNETDH",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("SalaireNETDH",null);
            // isAnomalieFound=true;
        }
    }

    public void _NMutuelle(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("NMutuelle",cellValue);
            }else{
                instance.setValue("NMutuelle",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("NMutuelle",null);
            // isAnomalieFound=true;
        }
    }

    public void _NomDeLaMutuelle(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("NomDeLaMutuelle",cellValue);
            }else{
                instance.setValue("NomDeLaMutuelle",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("NomDeLaMutuelle",null);
            // isAnomalieFound=true;
        }
    }

    public void _TauxDeLaMutuelle(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                if(((String)cellValue).contains("%")){
                    cellValue = ((String) cellValue).replace("%","");
                }
                try {
                    //cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);
                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("TauxDeLaMutuelle",cellValue);
            }else{
                instance.setValue("TauxDeLaMutuelle",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("TauxDeLaMutuelle",null);
            // isAnomalieFound=true;
        }
    }

    public void _NCIMR(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("NCIMR",cellValue);
            }else{
                instance.setValue("NCIMR",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("NCIMR",null);
            // isAnomalieFound=true;
        }
    }

    public void _NomRetraite(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("NomRetraite",cellValue);
            }else{
                instance.setValue("NomRetraite",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("NomRetraite",null);
            // isAnomalieFound=true;
        }
    }

    public void _TauxDeLaRetraire(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                if(((String)cellValue).contains("%")){
                    cellValue = ((String) cellValue).replace("%","");
                }
                try {
                    //cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("TauxDeLaRetraire",cellValue);
            }else{
                instance.setValue("TauxDeLaRetraire",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("TauxDeLaRetraire",null);
            // isAnomalieFound=true;
        }
    }

    public void _NumeroAttribue(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("NumeroAttribue",cellValue);
            }else{
                instance.setValue("NumeroAttribue",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("NumeroAttribue",null);
            // isAnomalieFound=true;
        }
    }

    public void _NomDeLaRetraiteComplementaire(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("NomDeLaRetraiteComplementaire",cellValue);
            }else{
                instance.setValue("NomDeLaRetraiteComplementaire",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("NomDeLaRetraiteComplementaire",null);
            // isAnomalieFound=true;
        }
    }

    public void _TauxDeLaRetraiteComplementaire(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                if(((String)cellValue).contains("%")){
                    cellValue = ((String) cellValue).replace("%","");
                }
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("TauxDeLaRetraiteComplementaire",cellValue);
            }else{
                instance.setValue("TauxDeLaRetraiteComplementaire",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("TauxDeLaRetraiteComplementaire",null);
            // isAnomalieFound=true;
        }
    }

    public void _MontantEpargneRetraite(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    //cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("MontantEpargneRetraite",cellValue);
            }else{
                instance.setValue("MontantEpargneRetraite",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("MontantEpargneRetraite",null);
            // isAnomalieFound=true;
        }
    }

    public void _DroitMensuelle(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("DroitMensuelle",cellValue);
            }else{
                instance.setValue("DroitMensuelle",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("DroitMensuelle",null);
            // isAnomalieFound=true;
        }
    }
    public void _SoldeAnneeEnCours(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("SoldeAnneeEnCours",cellValue);
            }else{
                instance.setValue("SoldeAnneeEnCours",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("SoldeAnneeEnCours",null);
            // isAnomalieFound=true;
        }
    }
    public void _SoldeAnterieur(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("SoldeAnterieur",cellValue);
            }else{
                instance.setValue("SoldeAnterieur",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("SoldeAnterieur",null);
            // isAnomalieFound=true;
        }
    }

    public void _SoldeConges(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("SoldeConges",cellValue);
            }else{
                instance.setValue("SoldeConges",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("SoldeConges",null);
            // isAnomalieFound=true;
        }
    }
    public void setNomPrenom(IStorageResource collaborateur){
        String nom =collaborateur.getValue("LastName")!=null? (String) collaborateur.getValue("LastName"):"";
        String prenom = collaborateur.getValue("FirstName")!=null?(String)collaborateur.getValue("FirstName"):"";
        String fullName = nom+" "+prenom;
        collaborateur.setValue("NomPrenom",fullName);
    }


    private IStorageResource createOrGetStorage(String organizationName ,  String projetName, String catalogName, String definitionName,String CIN) {
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,organizationName);
            IProject projet = getProjectModule().getProject(context,projetName,organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,catalogName,ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,definitionName);
            IViewController controller = getWorkflowModule().getViewController(context,IResource.class);
            controller.addEqualsConstraint("CIN",CIN);
            ArrayList<IStorageResource> data = ( ArrayList<IStorageResource>) controller.evaluate(definition);
            if(data!=null && !data.isEmpty()){
                isUpdate = true;
                return data.iterator().next();
            }
            return getWorkflowModule().createStorageResource(context,definition,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
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

    Object handleList(Object item,HashMap<String, String> possibleValues){
        if(possibleValues.get(item)!=null){
            return possibleValues.get(item);
        }
        return null;
    }

    Set<IGroup> handleGroupes(String cellValue){
        if(cellValue==null || cellValue.isEmpty())return null;
        Set<IGroup> groupes = new HashSet<>();
        groupes.add(getGroupe(groupesMapper.get("Salarié")));
        String[] groupesNames = cellValue.split(";");
        for(String groupeName : groupesNames){
            if(groupesMapper.get(groupeName)!=null){
                IGroup groupe = getGroupe(groupesMapper.get(groupeName));
                if(groupe!=null){
                    groupes.add(groupe);
                }
            }else{
                //ANOMALIE
            }

        }
        return groupes;
    }

    Set<IGroup> handleSecretGroupesGroupes(String cellValue,IStorageResource societe){


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
                //ANOMALIE
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
            }else if(groupeName.equals("SALARIE")){
                groupeName = "Salarie";
            }else if(groupeName.equals("MANAGER")){
                groupeName = "Manager";
            }
            else if(groupeName.equals("RH")){
                groupeName = "RH";
            }else if(groupeName.equals("Manager")){
                groupeName = "Manager";
            }else if(groupeName.equals("admin")){
                groupeName = "BPO";
            }*/
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IGroup group = Modules.getDirectoryModule().getGroup(context, organization, organization.getName() + groupeName);
          /*  if (group == null) {
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
            return ((ProcessWorkflowModule) getWorkflowModule()).getGroup(groupeID);
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
        if(RIB==null){
            return false;
        }
        if(RIB.length()!=24){
            return false;
        }
        return true;
    }



    private IUser setUserFields(IUser user, IResource collaborateurToCopyFrom, IOrganization organizationSociete) {
        user.save(getWorkflowModule().getSysadminContext());
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
        user.getExtendedAttributes().setValue("Division",collaborateurToCopyFrom.getValue("Division"));

        user.getExtendedAttributes().setValue("MotifSortie",collaborateurToCopyFrom.getValue("MotifSortie"));
        user.setExit((Date) collaborateurToCopyFrom.getValue("DateDeSortie"));


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
        user.save(getWorkflowModule().getSysadminContext());
        collaborateurToCopyFrom.setValue("MotDePasse",null);
        collaborateurToCopyFrom.save(getWorkflowModule().getSysadminContext());
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
                    collaborateur.save(getWorkflowModule().getSysadminContext());
                    IUser salarie = (IUser) collaborateur.getValue("Salarie");
                    salarie.setHierarchicalManager(N1);
                    salarie.save(getWorkflowModule().getSysadminContext());

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
                    collaborateur.save(getWorkflowModule().getSysadminContext());
                    IUser salarie = (IUser) collaborateur.getValue("Salarie");
                    salarie.getExtendedAttributes().setValue("Evaluateur",N1);
                    salarie.save(getWorkflowModule().getSysadminContext());

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
                    collaborateur.save(getWorkflowModule().getSysadminContext());
                    IUser salarie = (IUser) collaborateur.getValue("Salarie");
                    salarie.getExtendedAttributes().setValue("Suppleant",N1);
                    salarie.save(getWorkflowModule().getSysadminContext());

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
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
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
