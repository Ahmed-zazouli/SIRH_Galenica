package com.moovapps.ibb.rh.Script.Referentiel.Collaborateurs;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class ImportationCompetencesCollaborateur extends BaseAgent {

    @Override
    protected void execute() {
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            File excelFile = new File("D:\\IEG Maquette\\IEG20032023.xlsx");

            XSSFWorkbook workbook;
            int rowIndex = 0;

            workbook = new XSSFWorkbook(new FileInputStream(excelFile));
            XSSFSheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> annuaireIterator = firstSheet.iterator();
            rowIndex = 0;

            while (annuaireIterator.hasNext()) {
                Row nextRow = annuaireIterator.next();
                if (rowIndex == 0) {
                    // VERIFY MAQUETTE break;

                }

                // rowIndex++;
                if (nextRow == null) continue;
                if (rowIndex > 0) {
                    String cin = "";
                    if(firstSheet.getRow(rowIndex).getCell(0)!=null ){
                        firstSheet.getRow(rowIndex).getCell(0).setCellType(CellType.STRING);
                        cin = firstSheet.getRow(rowIndex).getCell(0).getStringCellValue().trim();
                        if(cin.equals("")){
                            //MERCI D'AJOUTER CIN
                            rowIndex++;
                            continue;
                        }
                    }else{
                        //MERCI D'AJOUTER CIN
                        rowIndex++;
                        continue;
                    }

                    IStorageResource collaborateur = getCollaborateurByCIN(cin);
                    if(collaborateur==null){
                        //MERCI DE VERIFIER CIN
                        rowIndex++;
                        continue;
                    }
                    IStorageResource societe = (IStorageResource) collaborateur.getValue("Societe");


                    String familleCompetence = "";
                    IStorageResource familleCompetenceStorage = null;
                    if(firstSheet.getRow(rowIndex).getCell(1)!=null ){
                        firstSheet.getRow(rowIndex).getCell(1).setCellType(CellType.STRING);
                        familleCompetence = firstSheet.getRow(rowIndex).getCell(1).getStringCellValue().trim();
                        if(familleCompetence.equals("")){
                            //MERCI D'AJOUTER familleCompetence
                            rowIndex++;
                            continue;
                        }else{
                            HashMap<String,Object> filter = new HashMap<>();
                            filter.put("Societe",societe);
                             familleCompetenceStorage =  handleStorageFiltrer("REFERENTIELCOMMUN","REFERENTIEL",firstSheet.getRow(rowIndex).getCell(1),"FamilleCompetence","sys_Title",filter);
                            if(familleCompetenceStorage==null){
                                //MERCI DE VERIFIER familleCompetence
                                rowIndex++;
                                continue;
                            }
                        }
                    }else {
                        //MERCI D'AJOUTER familleCompetence
                        rowIndex++;
                        continue;
                    }




                    String sousFamilleCompetence = "";
                    IStorageResource sousFamilleCompetenceStorage = null;
                    if(firstSheet.getRow(rowIndex).getCell(2)!=null ){
                        firstSheet.getRow(rowIndex).getCell(2).setCellType(CellType.STRING);
                        sousFamilleCompetence = firstSheet.getRow(rowIndex).getCell(2).getStringCellValue().trim();
                        if(sousFamilleCompetence.equals("")){
                            //MERCI D'AJOUTER sousFamilleCompetence
                            rowIndex++;
                            continue;
                        }else{
                            HashMap<String,Object> filter = new HashMap<>();
                            filter.put("FamilleCompetence",familleCompetenceStorage);
                             sousFamilleCompetenceStorage =  handleStorageFiltrer("REFERENTIELCOMMUN","REFERENTIEL",firstSheet.getRow(rowIndex).getCell(2),"SousFamilleCompetence","sys_Title",filter);
                            if(sousFamilleCompetenceStorage==null){
                                //MERCI DE VERIFIER sousFamilleCompetenceStorage
                                rowIndex++;
                                continue;
                            }
                        }
                    }else {
                        //MERCI D'AJOUTER sousFamilleCompetenceStorage
                        rowIndex++;
                        continue;
                    }



                    String competence = "";
                    IStorageResource competenceStorage = null;
                    if(firstSheet.getRow(rowIndex).getCell(3)!=null ){
                        firstSheet.getRow(rowIndex).getCell(3).setCellType(CellType.STRING);
                        competence = firstSheet.getRow(rowIndex).getCell(3).getStringCellValue().trim();
                        if(competence.equals("")){
                            //MERCI D'AJOUTER competence
                            rowIndex++;
                            continue;
                        }else{
                            HashMap<String,Object> filter = new HashMap<>();
                            filter.put("SousFamilleCompetence",sousFamilleCompetenceStorage);
                            competenceStorage =  handleStorageFiltrer("REFERENTIELCOMMUN","REFERENTIEL",firstSheet.getRow(rowIndex).getCell(3),"Competence","sys_Title",filter);
                            if(competenceStorage==null){
                                //MERCI DE VERIFIER competenceStorage
                                rowIndex++;
                                continue;
                            }
                        }
                    }else {
                        //MERCI D'AJOUTER competenceStorage
                        rowIndex++;
                        continue;
                    }


                    BigDecimal poids = null;
                    if(firstSheet.getRow(rowIndex).getCell(4)!=null){
                        //Number cellValue = null;
                        if (firstSheet.getRow(rowIndex).getCell(4).getCellType() == CellType.NUMERIC) {
                            poids = BigDecimal.valueOf(firstSheet.getRow(rowIndex).getCell(4).getNumericCellValue());
                        }else{
                            //MERCI DE VERIFIER LE POIDS
                        }
                    }else{
                        //MERCI D'AJOUTER LE POIDS
                    }


                    IStorageResource competenceCollaborateur = getOrCreateCompetenceCollaborateur(collaborateur,competenceStorage);
                    competenceCollaborateur.setValue("Societe",societe);
                    competenceCollaborateur.setValue("Collaborateur",collaborateur);
                    competenceCollaborateur.setValue("FamilleCompetence",familleCompetenceStorage);
                    competenceCollaborateur.setValue("SousFamilleCompetence",sousFamilleCompetence);
                    competenceCollaborateur.setValue("Competence",competenceStorage);
                    competenceCollaborateur.setValue("Poids",poids);
                    competenceCollaborateur.save(getWorkflowModule().getSysadminContext());
                    rowIndex++;
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    IStorageResource getOrCreateCompetenceCollaborateur(IStorageResource collaborateur , IStorageResource competence) {
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "CompetenceSalarie");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("Collaborateur", collaborateur);
            controller.addEqualsConstraint("Competence", competence);
            ArrayList<IStorageResource> data = (ArrayList<IStorageResource>) controller.evaluate(definition);
            if (data != null && !data.isEmpty()) {
                return data.iterator().next();
            } else {
                return getWorkflowModule().createStorageResource(context, definition, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    IStorageResource handleStorageFiltrer(String projet , String catalog,Cell excelCell,String storageDefinition , String fieldName ,HashMap<String,Object> filter ){
        String cellValue;
        if (excelCell.getCellType() == CellType.NUMERIC) {
            // Handle numeric cells
            cellValue = String.valueOf(excelCell.getNumericCellValue()).trim();
        } else {
            // Handle string cells
            cellValue = excelCell.getStringCellValue().trim();
        }
        return getStorageByFieldAndFilter(projet,catalog,storageDefinition,fieldName,cellValue,filter);

    }

    protected IStorageResource getStorageByFieldAndFilter(String projet , String catalogTEXT,String storageDefinition, String fieldName, String filedValue , HashMap<String,Object> filter) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, projet, organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, catalogTEXT, 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, storageDefinition);
            controller.addEqualsConstraint(fieldName, filedValue);
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                controller.addEqualsConstraint(key, value);
            }
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getCollaborateurByCIN(String cin) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("CIN", cin);
            //controller.addEqualsConstraint("Societe", societe);
            Collection<IStorageResource> collaberateurs = controller.evaluate(definition);
            if (!collaberateurs.isEmpty()) {
                return (IStorageResource) collaberateurs.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
