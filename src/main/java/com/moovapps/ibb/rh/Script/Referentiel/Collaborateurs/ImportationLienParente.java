package com.moovapps.ibb.rh.Script.Referentiel.Collaborateurs;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Cummon.Agents.AnnuaireFromExcel;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class ImportationLienParente extends BaseAgent {

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
                        String nonPrenomLienParente = "";
                        if(firstSheet.getRow(rowIndex).getCell(2)!=null ){
                            firstSheet.getRow(rowIndex).getCell(2).setCellType(CellType.STRING);
                            nonPrenomLienParente = firstSheet.getRow(rowIndex).getCell(2).getStringCellValue().trim();
                            if(nonPrenomLienParente.equals("")){
                                //MERCI D'AJOUTER nonPrenomLienParente
                                rowIndex++;
                                continue;
                            }
                        }else {
                            //MERCI D'AJOUTER nonPrenomLienParente
                            rowIndex++;
                            continue;
                        }


                        String lienParente = "";
                        if(firstSheet.getRow(rowIndex).getCell(1)!=null ){
                            firstSheet.getRow(rowIndex).getCell(1).setCellType(CellType.STRING);
                            lienParente = firstSheet.getRow(rowIndex).getCell(1).getStringCellValue().trim();
                            if(lienParente.equals("")){
                                //MERCI D'AJOUTER lienParente
                                rowIndex++;
                                continue;
                            }else if(( !lienParente.equals("conjoint") && !lienParente.equals("enfant"))){
                                //MERCI DE VERIFIER lienParente
                                rowIndex++;
                                continue;
                            }
                        }else{
                            //MERCI D'AJOUTER lienParente
                            rowIndex++;
                            continue;
                        }


                        Date dateNaissance = null;
                        if(firstSheet.getRow(rowIndex).getCell(3)!=null ){
                            Object cellValue = null;
                            CellType cellType = firstSheet.getRow(rowIndex).getCell(3).getCellType();
                            if (cellType.equals(CellType.NUMERIC)) {
                                cellValue = firstSheet.getRow(rowIndex).getCell(3).getDateCellValue();
                                dateNaissance =   (Date)cellValue;
                            } else if (cellType.equals(CellType.STRING)) {
                                try {
                                    cellValue = firstSheet.getRow(rowIndex).getCell(3).getStringCellValue().trim();
                                    String date = (String) cellValue;
                                    String replacedValue = "";
                                    if (date.contains("/"))
                                        replacedValue = date.replace('/', '-');
                                    else
                                        replacedValue = date;
                                    cellValue = new SimpleDateFormat("dd-MM-yyyy").parse(replacedValue);
                                    dateNaissance =  (Date)cellValue;
                                } catch (Exception e) {
                                    //  cellValue = null;
                                    dateNaissance =  null;
                                    // e.printStackTrace();
                                }
                            }
                            if(dateNaissance==null){
                                //MERCI DE VERIFIER LA DATE NAISSANCE
                            }
                        }else{
                            //MERCI D'AJOUTER LA DATE NAISSANCE
                        }


                        IStorageResource lienParenteStorage = getOrCreateLienParente(collaborateur,nonPrenomLienParente);
                        lienParenteStorage.setValue("FicheSalarie",collaborateur);
                        lienParenteStorage.setValue("Salarie",collaborateur.getValue("Salarie"));
                        lienParenteStorage.setValue("LienParente",lienParente);
                        lienParenteStorage.setValue("NomPrenom",nonPrenomLienParente);
                        lienParenteStorage.setValue("DateDeNaissance",dateNaissance);
                        lienParenteStorage.save(getWorkflowModule().getSysadminContext());
                        rowIndex++;
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }

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

    IStorageResource getOrCreateLienParente(IStorageResource collaborateur , String nomPrenomLien) {
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "LienParente");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("FicheSalarie", collaborateur);
            controller.addEqualsConstraint("NomPrenom", nomPrenomLien);
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
}
