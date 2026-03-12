package com.moovapps.ibb.rh.ImporterPie.Document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Script.Referentiel.VariablesColonnes;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.*;

import static com.moovapps.ibb.rh.Script.Referentiel.VariablesColonnes.indexSheetToMethodImportationDonneePie;
import static com.moovapps.ibb.rh.Script.Referentiel.VariablesColonnes.sheetFieldsImportationDonneePie;

public class ImportationDonneePie extends BaseDocumentExtension {

    ArrayList<JSONObject> excelCommentaireBloquant;

    @Override
    public boolean onAfterLoad() {
       // getWorkflowInstance().setValue("Societe", getWorkflowModule().getLoggedOnUser().getExtendedAttributes().getValue("Societe"));
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("ImporterLesElementsVariablesDeLaPaie")) {
            readFile();

        }

        return super.onBeforeSubmit(action);
    }

    private void readFile() {
        excelCommentaireBloquant = new ArrayList<>();
        File fichierImporter = createFileInCDrive("FichierAImporter", null);

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(fichierImporter));
            Iterator<Sheet> sheets = workbook.sheetIterator();
            while (sheets.hasNext()) {
                Sheet sheet = sheets.next();
                Iterator<Row> rows = sheet.iterator();
                while (rows.hasNext()) {
                    Row row = rows.next();
                    //JSONObject values = new JSONObject();
                    if (row.getRowNum() < 2) {
                        if (row.getRowNum() < 1) {
                            continue;
                        } else {
                            if (!verifyMaquette(workbook, sheet, row)) {
                                JSONObject tmp = new JSONObject();
                                tmp.put("Ligne", "Ligne: " + row.getRowNum());
                                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()));
                                tmp.put("Anomalie", "Merci de vérifier la maquette");
                                excelCommentaireBloquant.add(tmp);
                                break; // erreur dans la maquette
                            }
                            continue;
                        }
                    } else {
                        if (row.getCell(0) == null) {
                            break;
                        } else {
                            for (Map.Entry<Integer, String> entry : indexSheetToMethodImportationDonneePie.entrySet()) {
                                int key = entry.getKey();
                                String value = entry.getValue();
                                if (sheet == workbook.getSheetAt(key)) {
                                    String methodName = indexSheetToMethodImportationDonneePie.get(key);
                                    Method method = this.getClass().getMethod(methodName, XSSFWorkbook.class, Row.class, List.class);
                                    method.invoke(this, workbook, row, sheetFieldsImportationDonneePie.get(value));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            for (JSONObject comment : excelCommentaireBloquant) {
                ILinkedResource commentTD = getWorkflowInstance().createLinkedResource("RapportDImport");
                commentTD.setValue("Ligne", comment.get("Ligne"));
                commentTD.setValue("Cordonnee", comment.get("Cordonnee"));
                commentTD.setValue("Alerte", comment.get("Alerte"));
                commentTD.setValue("Anomalie", comment.get("Anomalie"));
                getWorkflowInstance().addLinkedResource(commentTD);
                commentTD.save(getWorkflowModule().getLoggedOnUserContext());
            }
            getWorkflowInstance().save(getWorkflowModule().getLoggedOnUserContext());
            System.out.println("/************** Fin **************/");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // workflowInstance.save(loggedOnUserContext);
    }

    private Boolean verifyMaquette(XSSFWorkbook workbook, Sheet sheet, Row row) {
        for (Map.Entry<Integer, List<String>> entry : VariablesColonnes.titlesMaquetteImportationDonneePieVerification.entrySet()) {
            int key = entry.getKey();
            List<String> values = entry.getValue();
            if (sheet == workbook.getSheetAt(key)) {
                for (int i = 0; i < values.size(); i++) {
                    if (key == 0) {
                        row = sheet.getRow(1);
                    }
                    if (!row.getCell(i).getStringCellValue().toLowerCase().trim().equals(values.get(i).toLowerCase().trim())) {
                        System.out.println(row.getCell(i));
                        return false;
                    }
                }
                return true;
            }
        }
        return true;
    }

    private File createFileInCDrive(String fromSysName, String newName) {
        ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
        ArrayList<IAttachment> tmp = (ArrayList<IAttachment>) getWorkflowInstance().getValue(fromSysName);
        IAttachment tmpAttachement = null;
        if (tmp != null && tmp.size() > 0) {
            tmpAttachement = tmp.get(0);
        }
        File file = null;
        if (tmpAttachement != null) {
            try {
                if (newName != null) {
                    file = new File("c://Temp_Moovapps//" + tmpAttachement.getName().split("\\.")[tmpAttachement.getName().split("\\.").length - 1]);
                } else {
                    file = new File("c://Temp_Moovapps//" + tmpAttachement.getName());
                }
                FileUtils.writeByteArrayToFile(file, tmpAttachement.getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public void _ImportationDonneesPie(XSSFWorkbook workbook, Row row, List<String> sheetfields) throws NoSuchMethodException {
        boolean shouldICreate = true;
        ImportationDonneePieFromExcel impCollaberateurs = new ImportationDonneePieFromExcel();
        HashMap<String, Object> valueToSysNme = new HashMap<String, Object>() {{
        }};
        for (String field : sheetfields) {
            Cell cell = row.getCell(sheetfields.indexOf(field));
            if (!impCollaberateurs.importer(row, cell, field, excelCommentaireBloquant, valueToSysNme)) {
                shouldICreate = false;
            }
        }
        System.out.println(valueToSysNme);
        // {TypeDeSalaire=Nom, SoldeAnneeEnCours=70000.0, SoldeAnterieur=0.0, SalaireDeBase=null, soldeConges=0.0, TauxHoraire=null, SalaireBrutDH=90.0, DroitMensuelle=70000.0, SalaireNETDH=70000.0, Matricule=8}

        IStorageResource thiscollaborateur = getCollaborateurByMatricule(impCollaberateurs.matricule, (IStorageResource) getWorkflowInstance().getValue("Societe"));
        //  addToRefAndCreateUser(thiscollaborateur, "DefaultOrganization", "REFERENTIELCOMMUN", "REFERENTIEL", "FicheCollaborateur", valueToSysNme);
        for (Map.Entry<String, Object> entry : valueToSysNme.entrySet()) {
            String sysName = entry.getKey();
            Object  value = entry.getValue();

            //if (value != null) {
            thiscollaborateur.setValue(sysName, value);
            //}
        }
        thiscollaborateur.save(getDirectoryModule().getSysadminContext());

    }


    protected IStorageResource getCollaborateurByMatricule(String matricule, IStorageResource societe) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("Matricule", matricule);
            controller.addEqualsConstraint("Societe", societe);
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


