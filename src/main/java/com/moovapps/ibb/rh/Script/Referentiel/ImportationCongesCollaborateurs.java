package com.moovapps.ibb.rh.Script.Referentiel;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.moovapps.Reprise.Helpers.Collaborateur;
import com.moovapps.Reprise.Helpers.ExcelWriter;
import com.moovapps.ibb.rh.Script.Referentiel.Collaborateurs.ExportCollaborateursExcel;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class ImportationCongesCollaborateurs extends BaseDocumentExtension {
    boolean isNotAnomalieFound = true;

    HashMap<IStorageResource,String> SuppleantMap = new HashMap<IStorageResource,String>();
    HashMap<IStorageResource,String> EvaluateurMap = new HashMap<IStorageResource,String>();
    HashMap<IStorageResource,String> N1Map = new HashMap<IStorageResource,String>();
    ArrayList<JSONObject> excelAnomaliesWorkFlowImportUser = new ArrayList<>();


    @Override
    public boolean onAfterLoad() {
        //getWorkflowInstance().deleteLinkedResources(getWorkflowInstance().getLinkedResources("RapportDImport"));
        //getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        if (getWorkflowInstance().getValue("FichierAExporter") == null) {
            File file = createFileFromFileCenterInLocalDrive("Maquette/Importation_Conges_CollaborateursV1.xlsx", "Importation_Conges_CollaborateursV1.xlsx");
            if (file == null) {
                getResourceController().alert("Maquette introuvable !!");
            } else {
                ArrayList<IAttachment> tmpImportationCollaborateurs = new ArrayList<>();
                IAttachment tmpImportationCollaborateursAttachment = Modules.getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
                tmpImportationCollaborateurs.add(tmpImportationCollaborateursAttachment);
                getWorkflowInstance().setValue("FichierAExporter", tmpImportationCollaborateurs);
                file.delete();
                file.deleteOnExit();
            }
        }
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        boolean isAllGood = false;
        if (action.getName().equals("ImporterLesConges")) {
            getWorkflowInstance().deleteLinkedResources(getWorkflowInstance().getLinkedResources("RapportDImport"));
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
            isAllGood = true;
            try {
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
                if (file == null) {
                    return true;
                }

                // Create a FileInputStream to read the Excel file
                FileInputStream fis = new FileInputStream(file);
                // Create a workbook object to represent the Excel file
                XSSFWorkbook workbook = new XSSFWorkbook(fis);
                List<String> columns = Arrays
                        .asList(
                                "Societe",
                                "Matricule",
                                "Nom",
                                "Prenom",
                                "CIN",
                                "_DroitMensuelle",
                                "_SoldeConges");
                Sheet sheet = workbook.getSheetAt(0);
                Constructor<Collaborateur> constructor = Collaborateur.class.getConstructor(HashMap.class,HashMap.class,HashMap.class,ArrayList.class);
                Collaborateur collaborateurInstance = constructor.newInstance(SuppleantMap,EvaluateurMap,N1Map,excelAnomaliesWorkFlowImportUser);

                int currentDemandeAnnee =getWorkflowInstance().getValue("Annee2")!=null?((Number)  ((IStorageResource) getWorkflowInstance().getValue("Annee2")).getValue("Valeur")).intValue():-1;
                int currentDemandeMois =getWorkflowInstance().getValue("Mois2")!=null?((Number)  ((IStorageResource) getWorkflowInstance().getValue("Mois2")).getValue("Valeur")).intValue():-1;

                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // Loop OverRows
                    isNotAnomalieFound = true;
                    Row row = sheet.getRow(rowIndex);
                    if (rowIndex == 0 || rowIndex == 1) {
                        //Verify maquette
                        //if false break;
                        continue;
                    }

                    int cinIndex = columns.indexOf("CIN");
                    String cin = "";
                    if (cinIndex >= 0) {
                        cin  = row.getCell(cinIndex) != null ? row.getCell(cinIndex).getStringCellValue().trim() : "";
                    }

                    if(cin.equals("")){
                        addAnomalie(row.getSheet().getSheetName(), (row.getRowNum() + 1) + "", "CIN", "Merci d'ajouter la CIN");
                        continue;
                    }
                    //String cin = row.getCell(columns.indexOf("CIN")) != null ? row.getCell(columns.indexOf("CIN")).getStringCellValue().trim() : "";

                    IStorageResource collaborateur = getOrCreateCollaborateurByCin(cin);

                    if(collaborateur==null){
                        addAnomalie(row.getSheet().getSheetName(), (row.getRowNum() + 1) + "", "CIN", "Merci de vérifier la CIN");
                        continue;
                    }

                    int collaborateurAnnee =collaborateur.getValue("Annee")!=null?((Number)  ((IStorageResource) collaborateur.getValue("Annee")).getValue("Valeur")).intValue():-1;
                    int collaborateurMois =collaborateur.getValue("Mois")!=null?((Number)  ((IStorageResource) collaborateur.getValue("Mois")).getValue("Valeur")).intValue():-1;

                    if(currentDemandeAnnee < collaborateurAnnee){
                        continue;
                    }
                    else if( (currentDemandeAnnee == collaborateurAnnee ) && currentDemandeMois < collaborateurMois){
                        continue;
                    }

                    for (String colonne : columns) {
                        if (colonne.startsWith("_")) {
                            Method method = Collaborateur.class.getMethod(colonne, IStorageResource.class, Row.class, Cell.class);
                            Object isAnomalieFoundObject = method.invoke(collaborateurInstance, collaborateur, row, row.getCell(columns.indexOf(colonne)));
                            if (isNotAnomalieFound) {
                                isNotAnomalieFound = (boolean) isAnomalieFoundObject;
                            }
                        }
                    }


                    if (isNotAnomalieFound) {
                        IUser salarie = (IUser) collaborateur.getValue("Salarie");
                        if (salarie == null) {
                           /* boolean isCreatedSuccefully = CreateSalarieFromFiche(collaborateur, row, getWorkflowInstance());
                            if (isAllGood) {
                                isAllGood = isCreatedSuccefully;
                            }*/
                        } else {
                            UpdateSalarieFromFiche(salarie, collaborateur);

                        }
                    }
                    System.out.println((rowIndex+1)+"");
                }
                //HERE
                //UpdateN1();
                //UpdateEvaluateur();
                //UpdateSuppleants();
                //ADD ANOMALIS HERE
                if(excelAnomaliesWorkFlowImportUser!=null && !excelAnomaliesWorkFlowImportUser.isEmpty()){
                    String filePath =   new ExcelWriter().GenerateExcel(excelAnomaliesWorkFlowImportUser,"C:\\Export\\DISLOG\\Anomalies\\Conges_Collaborateurs.xlsx");
                    if(!filePath.equals("")){
                        createVerificationImportWorkflowInstance(getWorkflowInstance(), filePath);
                    }
                }

                workbook.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isAllGood){
            return super.onBeforeSubmit(action);
        }

        return false;
    }

    private void createVerificationImportWorkflowInstance(IWorkflowInstance instance,String anomalieFile){
        try {
            IContext context = getWorkflowModule().getSysadminContext();

            IAttachment attachement = getDirectoryModule().createAttachment(context,new File(anomalieFile));
            ArrayList<IAttachment>  attachements = new ArrayList();
            attachements.add(attachement);
            instance.setValue("Anomalies2",attachements);
            instance.save(getWorkflowModule().getSysadminContext());
            //fillAnomaliesLinkedResources(verificationImportInstance);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //ClassListHelper helper = new ClassListHelper();
    public void addAnomalie(String sheetName,String row,String column , String message){
        JSONObject anomalie = new JSONObject();
        anomalie.put("Type","Anomalie");
        anomalie.put("SheetName",sheetName);
        anomalie.put("Row",row);
        anomalie.put("Column",column);
        anomalie.put("Message",message);
        excelAnomaliesWorkFlowImportUser.add(anomalie);
    }

    public void addAlerte(String sheetName,String row,String column , String message){
        JSONObject anomalie = new JSONObject();
        anomalie.put("Type","Alerte");
        anomalie.put("SheetName",sheetName);
        anomalie.put("Row",row);
        anomalie.put("Column",column);
        anomalie.put("Message",message);
        excelAnomaliesWorkFlowImportUser.add(anomalie);
    }
    private boolean CreateSalarieFromFiche(IStorageResource collaborateur,Row row , IWorkflowInstance workflowInstance) {
        try{
            String login =collaborateur.getValue("Identifiant")!=null? (String) collaborateur.getValue("Identifiant"):"";
            String password =collaborateur.getValue("MotDePasse")!=null? (String) collaborateur.getValue("MotDePasse"):"";
            if(!login.equals("") && !password.equals("")){
                if(isSameLoginExist(login)){
                    //L'identifiant exist deja
                    addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Identifiant","L'identifiant existe déja");

                }else{
                    IStorageResource societe = (IStorageResource) collaborateur.getValue("Societe");
                    IOrganization societeOrganization = (IOrganization) societe.getValue("Organisation");
                    try{
                        IUser salarie = getDirectoryModule().createUser(getWorkflowModule().getSysadminContext(),login,password,societeOrganization);
                        setUserFields(salarie,collaborateur,societeOrganization);
                    }catch (Exception e){
                        //ANomali + e .get cause
                        addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Identifiant ou Mot de passe",""+e.getCause());
                        e.printStackTrace();
                        return false;
                    }
                }

            }else {
                addAnomalie(row.getSheet().getSheetName(),(row.getRowNum()+1)+"","Identifiant ou Mot de passe","L'identifiant ou le mot de passe est vide");
                return false;
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return true;

    }
    private void UpdateSalarieFromFiche(IUser salarie, IStorageResource collaborateur) {
        IStorageResource societe = (IStorageResource) collaborateur.getValue("Societe");
        IOrganization societeOrganization = (IOrganization) societe.getValue("Organisation");
        setUserFields(salarie,collaborateur,societeOrganization);
    }

    private IUser setUserFields(IUser user, IResource collaborateurToCopyFrom, IOrganization organizationSociete) {
        user.getExtendedAttributes().setValue("DroitMensuelle",collaborateurToCopyFrom.getValue("DroitMensuelle"));
        user.getExtendedAttributes().setValue("SoldeAnneeEnCours",collaborateurToCopyFrom.getValue("SoldeAnneeEnCours"));
        user.getExtendedAttributes().setValue("SoldeAnterieur",collaborateurToCopyFrom.getValue("SoldeAnterieur"));
        user.getExtendedAttributes().setValue("SoldeConges",collaborateurToCopyFrom.getValue("SoldeConges"));
        collaborateurToCopyFrom.setValue("Annee",getWorkflowInstance().getValue("Annee2"));
        collaborateurToCopyFrom.setValue("Mois",getWorkflowInstance().getValue("Mois2"));

        user.save(getWorkflowModule().getSysadminContext());
        collaborateurToCopyFrom.save(getWorkflowModule().getSysadminContext());
        return user;
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

    IStorageResource getOrCreateCollaborateurByCin(String cin){
        IStorageResource collaborateur = null;
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("CIN", cin);
            ArrayList<IStorageResource> data = (ArrayList<IStorageResource>) controller.evaluate(definition);
            if(data!=null && !data.isEmpty()){
                collaborateur =  data.iterator().next();
            }/*else{
                collaborateur = Modules.getWorkflowModule().createStorageResource(context,definition,"");
            }*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return collaborateur;
    }

    boolean isSameLoginExist(String login){
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("Identifiant", login);
            int size =  controller.evaluateSize(definition);
            if(size > 0){
                return true;
            }else{
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void exportCongesCollaborateurs(IWorkflowInstance workflowInstance, IResourceController resourceController){
        try {
            File file = createFileFromFileCenterInLocalDrive("Maquette/Importation_Conges_CollaborateursV1.xlsx", "Export_Conges_Collaborateurs.xlsx");

            try (XSSFWorkbook exportWorkbook = new XSSFWorkbook(new FileInputStream(file));
                 FileOutputStream outputStream = new FileOutputStream("c://TEST//SIRH_V2//TmpImportation//tmpExportConges.xlsx")) {

                XSSFSheet exportWorkbookFirstSheet = exportWorkbook.getSheetAt(0);
                int rowIndex = 2;
                Collection<IStorageResource> collaborateurs = getCollaborateurs((IStorageResource) workflowInstance.getValue("Societe"));

                for (IStorageResource collaborateur : collaborateurs) {
                    XSSFRow row = exportWorkbookFirstSheet.createRow(rowIndex);
                    for (int i = 0; i < VariablesColonnes.sheetFieldsImportationCongesCollaborateurs.get("_ImportationCongesCollaborateurs").size(); i++) {
                        if(VariablesColonnes.sheetFieldsImportationCongesCollaborateurs.get("_ImportationCongesCollaborateurs").get(i).startsWith("_")){
                            XSSFCell cell = row.createCell(i);
                            String methodName = VariablesColonnes.sheetFieldsImportationCongesCollaborateurs.get("_ImportationCongesCollaborateurs").get(i);
                            Method method = ExportCollaborateursExcel.class.getMethod(methodName, IStorageResource.class, Row.class, Cell.class);
                            Object value = method.invoke(new ExportCollaborateursExcel(), collaborateur, row, cell);
                        }

                    }
                    rowIndex++;
                }

                exportWorkbook.write(outputStream);
                exportWorkbook.close();
                outputStream.close();
                File toDelete = new File("c://TEST//SIRH_V2//TmpImportation//tmpExportConges.xlsx");
                FileUtils.copyFile(toDelete, file);
                toDelete.delete();
                toDelete.deleteOnExit();
            }

            File toDeleteFile = new File("C:\\TEST\\Conges_Collaborateurs.xlsx");
            FileUtils.copyFile(file, toDeleteFile);
            IAttachment tmpJournalPaieAttachment = Modules.getDirectoryModule().createAttachment(Modules.getWorkflowModule().getSysadminContext(), toDeleteFile);
            ArrayList<IAttachment> tmpNouvellesRecruesList = new ArrayList<>();
            tmpNouvellesRecruesList.add(tmpJournalPaieAttachment);
            toDeleteFile.delete();
            toDeleteFile.deleteOnExit();
            file.delete();
            file.deleteOnExit();
            workflowInstance.setValue("FichierDExport", tmpNouvellesRecruesList);
            workflowInstance.save(Modules.getWorkflowModule().getLoggedOnUserContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    protected Collection<IStorageResource> getCollaborateurs(IStorageResource societe) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("Societe", societe);
            return controller.evaluate(definition);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private File createFileFromFileCenterInLocalDrive(String filePath, String fileName) {
        try {
            ILibraryModule libraryModule = Modules.getLibraryModule();
            IContext sysContext = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            ILibrary library = libraryModule.getLibrary(sysContext, organization, "Espace System");
            if (library != null) {
                IFile file = libraryModule.getFileByPath(sysContext, library, filePath);
                if (file == null) {
                    getResourceController().alert("Le fichier de modele n'existe pas dans l'espace documentaire...Veuillez contacter votre administrateur !");
                } else {
                    String[] filePathDivided = filePath.split("/");
                    IAttachment attachment = libraryModule.getAttachment(file, filePathDivided[filePathDivided.length - 1]);
                    File tmpFile = new File("c://TEST//SIRH_V2//TmpImportation//" + fileName);
                    FileUtils.writeByteArrayToFile(tmpFile, attachment.getContent());
                    return tmpFile;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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



}
