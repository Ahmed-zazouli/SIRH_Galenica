package com.moovapps.ibb.rh.Script.Referentiel;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.moovapps.ibb.rh.Script.Referentiel.Collaborateurs.ImportationCollaborateursFromExcel;
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

import static com.axemble.vdoc.sdk.Modules.getWorkflowModule;
import static com.moovapps.ibb.rh.Script.Referentiel.VariablesColonnes.*;

public class ImportationMain extends GetDataFromReferentielAndAdministration {

    IWorkflowModule workflowModule = null;
    IWorkflowInstance workflowInstance = null;
    IContext loggedOnUserContext = null;

    ArrayList<JSONObject> excelCommentaireBloquant;
    int counter = 0;


    public ImportationMain(IWorkflowModule workflowModule, IWorkflowInstance workflowInstance, IContext loggedOnUserContext) {
        this.workflowModule = workflowModule;
        this.workflowInstance = workflowInstance;
        this.loggedOnUserContext = loggedOnUserContext;
    }


    public void importFromExcelToRef(Boolean create) {
        workflowInstance.deleteLinkedResources(workflowInstance.getLinkedResources("RapportDImport"));
        File fichierImporter = createFileInCDrive(workflowInstance, "FichierAImporter", null);
        readFile(fichierImporter, create);
    }

    private File createFileInCDrive(IResource docFrom, String fromSysName, String newName) {
        ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
        ArrayList<IAttachment> tmp = (ArrayList<IAttachment>) docFrom.getValue(fromSysName);
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

    private void readFile(File fichierImporter, boolean create) {
        excelCommentaireBloquant = new ArrayList<>() ;
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(fichierImporter));
            Iterator<Sheet> sheets = workbook.sheetIterator();
            while (sheets.hasNext()) {
                Sheet sheet = sheets.next();
                Iterator<Row> rows = sheet.iterator();
                while (rows.hasNext()) {
                    Row row = rows.next();
                    //JSONObject values = new JSONObject();
                    if (row.getRowNum() < 1) {
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
                            for (Map.Entry<Integer, String> entry : indexSheetToMethod.entrySet()) {
                                int key = entry.getKey();
                                String value = entry.getValue();
                                if (sheet == workbook.getSheetAt(key)) {
                                    String methodName = indexSheetToMethod.get(key);
                                    Method method = this.getClass().getMethod(methodName, XSSFWorkbook.class, Row.class, List.class, Boolean.class);
                                    method.invoke(this, workbook, row, sheetFields2.get(value), create);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            for (JSONObject comment : excelCommentaireBloquant) {
                ILinkedResource commentTD = workflowInstance.createLinkedResource("RapportDImport");
                commentTD.setValue("Ligne", comment.get("Ligne"));
                commentTD.setValue("Cordonnee", comment.get("Cordonnee"));
                commentTD.setValue("Alerte", comment.get("Alerte"));
                commentTD.setValue("Anomalie", comment.get("Anomalie"));
                workflowInstance.addLinkedResource(commentTD);
                commentTD.save(loggedOnUserContext);
            }
            workflowInstance.save(loggedOnUserContext);
            System.out.println("/************** Fin **************/");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
       // workflowInstance.save(loggedOnUserContext);
    }

    public void _ImportationCollaborateur(XSSFWorkbook workbook, Row row, List<String> sheetfields, Boolean create) throws NoSuchMethodException {
        boolean shouldICreate = true;
        ImportationCollaborateursFromExcel impCollaberateurs = new ImportationCollaborateursFromExcel();
        HashMap<String, Object> valueToSysNme = new HashMap<String, Object>() {{
        }};
        for (String field : sheetfields) {
            Cell cell = row.getCell(sheetfields.indexOf(field));
            if (!impCollaberateurs.importer(row, cell, field, excelCommentaireBloquant, valueToSysNme)) {
                shouldICreate = false;
            }
        }

        if (create && shouldICreate) {
            IStorageResource thiscollaborateurRef = getCollaborateurByMatricule("NomPrenom", impCollaberateurs.nomPrenom);
            IUser thiscollaborateur = null;
            if (thiscollaborateurRef != null) {
                 thiscollaborateur = (IUser) thiscollaborateurRef.getValue("Salarie");
            }
            for (Map.Entry<String, Object> entry : valueToSysNme.entrySet()) {
                String sysName = entry.getKey();
                Object  value = entry.getValue();
                //if (value != null) {
                if (thiscollaborateurRef != null) {
                    thiscollaborateurRef.setValue(sysName, value);
                    thiscollaborateur.getExtendedAttributes().setValue(sysName, value);
                }
                //}
            }
            if (thiscollaborateurRef != null) {
                thiscollaborateur.save(getWorkflowModule().getSysadminContext());
                thiscollaborateur.setHierarchicalManager(thiscollaborateurRef.getValue("HierarchicalManager") != null ? (IUser) thiscollaborateurRef.getValue("HierarchicalManager") : null);
                thiscollaborateurRef.save(getWorkflowModule().getSysadminContext());
            }
           // addToRefAndCreateUser(thiscollaborateur, "DefaultOrganization", "REFERENTIELCOMMUN", "REFERENTIEL", "FicheCollaborateur", valueToSysNme);
        }

    }


    private Boolean verifyMaquette(XSSFWorkbook workbook, Sheet sheet, Row row) {
        for (Map.Entry<Integer, List<String>> entry : VariablesColonnes.titlesMaquetteVerification.entrySet()) {
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

    public void addToRefAndCreateUser(IStorageResource collaborateur, String SysNameOrganization, String SysNameProject, String sysNameCatalog, String sysNameReferentiel, HashMap<String, Object> valueToSysNme) {
        IStorageResource storageResource = null;
        try {
            // Create / Update Fiche Collaborateur
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, SysNameOrganization);
            IProject project = Modules.getProjectModule().getProject(context, SysNameProject, organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, sysNameCatalog, 4, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, sysNameReferentiel);
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            if (collaborateur == null) {
                storageResource = getWorkflowModule().createStorageResource(context, definition, "", "");
            } else {
                storageResource = collaborateur;
            }
            if (storageResource != null) {

                for (Map.Entry<String, Object> entry : valueToSysNme.entrySet()) {
                    String sysName = entry.getKey();
                    Object  value = entry.getValue();
                    //if (value != null) {
                        storageResource.setValue(sysName, value);
                    //}
                }
                storageResource.setValue("SoldeConges",
                        (storageResource.getValue("SoldeAnterieur") != null ? ((Number)storageResource.getValue("SoldeAnterieur")).floatValue() : 0) +
                                (storageResource.getValue("SoldeAnneeEnCours") != null ? ((Number)storageResource.getValue("SoldeAnneeEnCours")).floatValue() : 0));
                storageResource.save(context);



                // Create / Update User in Administration
                IStorageResource societe = (IStorageResource) storageResource.getValue("Societe");
                IOrganization organizationSociete = (IOrganization) societe.getValue("Organisation");

                if (storageResource.getValue("Salarie") != null) {
                    updateSalarie(storageResource, organizationSociete);
                } else {
                    insertSalarie(storageResource, organizationSociete);
                }

                Collection<IStorageResource> userLiensParente = getUserLienParente(storageResource);
                int nbrEnfant = 0;
                for (IStorageResource lienParente : userLiensParente) {
                    if (lienParente.getValue("LienParente").equals("Enfant")) {
                        nbrEnfant++;
                    }
                }
                storageResource.setValue("MotDePasse", null);
                storageResource.setValue("ConfirmerMotDePasse", null);
                storageResource.setValue("NombreEnfants", nbrEnfant);
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
        IContext context = getWorkflowModule().getSysadminContext();
        IUser newUser = null;

        try {
           // Modules.getDirectoryModule().beginTransaction();//(IOrganization)collaborateurToCopyFrom.getValue("Organisation")
            newUser = Modules.getDirectoryModule().createUser(context, (String) collaborateurToCopyFrom.getValue("Identifiant"), (String) collaborateurToCopyFrom.getValue("MotDePasse"), organizationSociete);
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
        IContext context = getWorkflowModule().getSysadminContext();
        IUser user = (IUser) storageResource.getValue("Salarie");
        user.setLogin((String) storageResource.getValue("Identifiant"));
        user = setUserFields(user, storageResource, organizationSociete);
        user.save(context);
        storageResource.setValue("Salarie", user);
        storageResource.setValue("Organisation", organizationSociete);
        storageResource.save(context);
        counter++;
        System.out.println(counter+") "+user.getFullName() + " a été mis à jour");

    }

    private IUser setUserFields(IUser user, IResource collaborateurToCopyFrom, IOrganization organizationSociete) {
        user.setOrganization(organizationSociete);
        user.setLanguage("fr");
        user.getExtendedAttributes().setValue("Matricule", collaborateurToCopyFrom.getValue("Matricule"));
        user.getExtendedAttributes().setValue("Salarie", collaborateurToCopyFrom.getValue("Salarie"));
        user.setTitle(collaborateurToCopyFrom.getValue("Title") != null ? (String) collaborateurToCopyFrom.getValue("Title") : null);
        user.setFirstName(collaborateurToCopyFrom.getValue("FirstName") != null ? (String) collaborateurToCopyFrom.getValue("FirstName") : null);
        user.setLastName(collaborateurToCopyFrom.getValue("LastName") != null ? (String) collaborateurToCopyFrom.getValue("LastName") : null);
        user.setMobilePhoneNumber(collaborateurToCopyFrom.getValue("MobilePhoneNumber") != null ? (String) collaborateurToCopyFrom.getValue("MobilePhoneNumber") : null);
        user.setBirthday(collaborateurToCopyFrom.getValue("Birthday") != null ? (Date) collaborateurToCopyFrom.getValue("Birthday") : null);
        user.getExtendedAttributes().setValue("CIN", collaborateurToCopyFrom.getValue("CIN"));
        user.getExtendedAttributes().setValue("NCNSS", collaborateurToCopyFrom.getValue("NCNSS"));
        user.getExtendedAttributes().setValue("NCIMR", collaborateurToCopyFrom.getValue("NCIMR"));
        user.getExtendedAttributes().setValue("NMutuelle", collaborateurToCopyFrom.getValue("NMutuelle"));
       // user.getExtendedAttributes().setValue("Site",collaborateurToCopyFrom.getValue("Site"));
        user.setAddress1(collaborateurToCopyFrom.getValue("Ville") != null ? (String) collaborateurToCopyFrom.getValue("Ville") : null);
       // user.setAddress1(collaborateurToCopyFrom.getValue("Site") != null ? (String) collaborateurToCopyFrom.getValue("Site") : null);
        user.setAddress1(collaborateurToCopyFrom.getValue("Address1") != null ? (String) collaborateurToCopyFrom.getValue("Address1") : null);
        user.getExtendedAttributes().setValue("EtatCivil", collaborateurToCopyFrom.getValue("EtatCivil"));
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
                IAttachment userAvatar = Modules.getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
                user.setAvatar(userAvatar);
                file.delete();
                file.deleteOnExit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            user.setAvatar(null);
        }

        // Informations login
        user.setEmail(collaborateurToCopyFrom.getValue("Email") != null ? (String) collaborateurToCopyFrom.getValue("Email") : null);
        if (collaborateurToCopyFrom.getValue("MotDePasse") != null && !collaborateurToCopyFrom.getValue("MotDePasse").equals("")) {
            user.setPassword((String) collaborateurToCopyFrom.getValue("MotDePasse"));
        }

        // Informations bancaire
        user.getExtendedAttributes().setValue("Banque", collaborateurToCopyFrom.getValue("Banque"));
        user.getExtendedAttributes().setValue("NCompteBancaire", collaborateurToCopyFrom.getValue("NCompteBancaire"));
        user.getExtendedAttributes().setValue("AgenceBancaire", collaborateurToCopyFrom.getValue("AgenceBancaire"));

        // Affectation
        user.getExtendedAttributes().setValue("DateDEmbauche", collaborateurToCopyFrom.getValue("DateDEmbauche"));
        //user.setContractType(collaborateurToCopyFrom.getValue("ContractType") != null ? (String)collaborateurToCopyFrom.getValue("ContractType") : null);
        //user.getExtendedAttributes().setValue("Societe", collaborateurToCopyFrom.getValue("Societe"));
        user.getExtendedAttributes().setValue("Societe", collaborateurToCopyFrom.getValue("Societe"));
        user.getExtendedAttributes().setValue("ContractType", collaborateurToCopyFrom.getValue("ContractType"));
        user.getExtendedAttributes().setValue("Groupes", collaborateurToCopyFrom.getValue("Groupes"));
        user.getExtendedAttributes().setValue("SecretGroupes", collaborateurToCopyFrom.getValue("SecretGroupes"));

        user.getExtendedAttributes().setValue("Direction", collaborateurToCopyFrom.getValue("Direction"));
        user.getExtendedAttributes().setValue("Departement", collaborateurToCopyFrom.getValue("Departement"));
        user.getExtendedAttributes().setValue("Fonction", collaborateurToCopyFrom.getValue("Fonction"));
        user.getExtendedAttributes().setValue("Categorie", collaborateurToCopyFrom.getValue("Categorie"));
        user.getExtendedAttributes().setValue("Profil", collaborateurToCopyFrom.getValue("Profil"));
        user.getExtendedAttributes().setValue("Suppleant", collaborateurToCopyFrom.getValue("Suppleant"));
        user.getExtendedAttributes().setValue("Division", collaborateurToCopyFrom.getValue("Division"));

//        user.getExtendedAttributes().setValue("DirectionText", collaborateurToCopyFrom.getValue("DirectionText"));
//        user.getExtendedAttributes().setValue("DepartementText", collaborateurToCopyFrom.getValue("DepartementText"));
//        user.getExtendedAttributes().setValue("FonctionText", collaborateurToCopyFrom.getValue("FonctionText"));
//        user.getExtendedAttributes().setValue("CategorieText", collaborateurToCopyFrom.getValue("CategorieText"));

        user.setHierarchicalManager(collaborateurToCopyFrom.getValue("HierarchicalManager") != null ? (IUser) collaborateurToCopyFrom.getValue("HierarchicalManager") : null);
        //	user.setExit(collaborateurToCopyFrom.getValue("Exit") != null ? (Date)collaborateurToCopyFrom.getValue("Exit") : null);
        user.setExit(collaborateurToCopyFrom.getValue("DateDeSortie") != null ? (Date) collaborateurToCopyFrom.getValue("DateDeSortie") : null);
        user.getExtendedAttributes().setValue("MotifSortie", collaborateurToCopyFrom.getValue("MotifSortie"));

        // Données salariales
        user.getExtendedAttributes().setValue("TypeDeSalaire", collaborateurToCopyFrom.getValue("TypeDeSalaire"));
        user.getExtendedAttributes().setValue("TauxHoraire", collaborateurToCopyFrom.getValue("TauxHoraire"));
        user.getExtendedAttributes().setValue("SalaireDeBase", collaborateurToCopyFrom.getValue("SalaireDeBase"));
        user.getExtendedAttributes().setValue("SalaireBrut", collaborateurToCopyFrom.getValue("SalaireBrutDH"));
        user.getExtendedAttributes().setValue("Salaire", collaborateurToCopyFrom.getValue("SalaireNETDH"));
        user.getExtendedAttributes().setValue("MontantMensuelDeNoteDeFrais", collaborateurToCopyFrom.getValue("MontantMensuelDeNoteDeFrais"));

        // Indemnités

        // Mutuelle
        user.getExtendedAttributes().setValue("NomDeLaMutuelle", collaborateurToCopyFrom.getValue("NomDeLaMutuelle"));
        user.getExtendedAttributes().setValue("TauxDeLaMutuelle", collaborateurToCopyFrom.getValue("TauxDeLaMutuelle"));

        // Retraite
        user.getExtendedAttributes().setValue("NomRetraite", collaborateurToCopyFrom.getValue("NomRetraite"));
        user.getExtendedAttributes().setValue("TauxDeLaRetraire", collaborateurToCopyFrom.getValue("TauxDeLaRetraire"));
        user.getExtendedAttributes().setValue("NumeroAttribue", collaborateurToCopyFrom.getValue("NumeroAttribue"));

        // Retraite complémentaire
        user.getExtendedAttributes().setValue("NomDeLaRetraiteComplementaire", collaborateurToCopyFrom.getValue("NomDeLaRetraiteComplementaire"));
        user.getExtendedAttributes().setValue("TauxDeLaRetraiteComplementaire", collaborateurToCopyFrom.getValue("TauxDeLaRetraiteComplementaire"));
        user.getExtendedAttributes().setValue("MontantEpargneRetraite", collaborateurToCopyFrom.getValue("MontantEpargneRetraite"));

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
//		user.getExtendedAttributes().setValue("DateDeTitularisation", collaborateurToCopyFrom.getValue("DateDeTitularisation"));
//		user.getExtendedAttributes().setValue("DateDEnregistrement", collaborateurToCopyFrom.getValue("DateDEnregistrement"));

        if (collaborateurToCopyFrom.getValue("Actif") == null || (boolean) collaborateurToCopyFrom.getValue("Actif")) {
            user.enable();
        }else {
            user.disable();
        }

        return user;
    }

    private Collection<IStorageResource> getUserLienParente(IStorageResource storageResource) {
        Collection<IStorageResource> liensParente = Collections.emptyList();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "LienParente");
            controller.addEqualsConstraint("FicheSalarie", storageResource);
            liensParente = controller.evaluate(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return liensParente;
    }

    private IResourceDefinition getFicheCollaborateurDefinition() {
        IResourceDefinition definition = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return definition;
    }


}
