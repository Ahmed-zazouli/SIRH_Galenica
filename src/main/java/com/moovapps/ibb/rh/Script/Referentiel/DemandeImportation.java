package com.moovapps.ibb.rh.Script.Referentiel;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.moovapps.Reprise.Helpers.ClassListHelper;
import com.moovapps.Reprise.Helpers.ExcelWriter;
import com.moovapps.Reprise.Helpers.LienParente;
import com.moovapps.Reprise.Helpers.Collaborateur;
import com.moovapps.ibb.rh.GestionPaieV2.Excel.NewRecruitExcelMethods;
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
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class DemandeImportation extends BaseDocumentExtension {
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
            File file = createFileFromFileCenterInLocalDrive("Maquette/Importation_CollaborateursV1.xlsx", "Importation_CollaborateursV1.xlsx");
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
        if (action.getName().equals("ImporterLesCollaborateurs")) {
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
                        .asList("_Societe", "_Matricule", "_Actif", "_Civilite", "_Nom", "_Prenom",
                                "_NumTel",

                                "_DateDeNaissance",
                                "_CIN",
                                "_NCNSS",
                                "_Ville",
                                "_Addresse1",

                                "_EtatCivil",
                                "_NombreEnfant",
                                "_Email",
                                "_Identifiant",
                                "_MotDePasse",
                                "_RIB",
                                "_Banque",
                                "_AgenceBancaire",
                                "_DateEmbaucheSociete",
                                "_TypeContrat",
                                "_Roles",
                                "_Direction",
                                "_Departement",
                                "_Division",
                                "_Fonction",
                                "_Suppleants",
                                "_Site",
                                "_ResponsableN1",
                                "_DateSortie","_MotifSortie",
                                "_TypeDeSalaire",
                                "_TauxHoraire",
                                "_SalaireDeBase",
                                "_SalaireBrut",

                                "_SalaireNETDH",
                                "_NMutuelle",
                                "_NomDeLaMutuelle",
                                "_TauxDeLaMutuelle",
                                /*"_NCIMR",*/
                                "_NomRetraite",
                                "_TauxDeLaRetraire",
                                "_NumeroAttribue",

                                "_DroitMensuelle",
                                "_SoldeConges");
                Sheet sheet = workbook.getSheetAt(0);
                Constructor<Collaborateur> constructor = Collaborateur.class.getConstructor(HashMap.class,HashMap.class,HashMap.class,ArrayList.class);
                Collaborateur collaborateurInstance = constructor.newInstance(SuppleantMap,EvaluateurMap,N1Map,excelAnomaliesWorkFlowImportUser);

                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // Loop OverRows
                    isNotAnomalieFound = true;
                    Row row = sheet.getRow(rowIndex);
                    if (rowIndex == 0 || rowIndex == 1) {
                        //Verify maquette
                        //if false break;
                        continue;
                    }


                    String cin = row.getCell(8) != null ? row.getCell(8).getStringCellValue().trim() : "";

                    IStorageResource collaborateur = getOrCreateCollaborateurByCin(cin);


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
                            boolean isCreatedSuccefully = CreateSalarieFromFiche(collaborateur, row, getWorkflowInstance());
                            if (isAllGood) {
                                isAllGood = isCreatedSuccefully;
                            }
                        } else {
                            UpdateSalarieFromFiche(salarie, collaborateur);

                        }
                    }
                    System.out.println((rowIndex+1)+"");
                }
                //HERE
                UpdateN1();
                UpdateEvaluateur();
                UpdateSuppleants();
                //ADD ANOMALIS HERE
                if(excelAnomaliesWorkFlowImportUser!=null && !excelAnomaliesWorkFlowImportUser.isEmpty()){
                   /* for(JSONObject object : excelAnomaliesWorkFlowImportUser){
                        ILinkedResource linkedResource =  getWorkflowInstance().createLinkedResource("RapportDImport");
                       // linkedResource.setValue("Feuille",object.get("SheetName"));
                        linkedResource.setValue("Ligne",object.get("Row"));
                        linkedResource.setValue("Cordonnee",object.get("Column"));
                        if(object.get("Type").equals("Alerte")){
                            linkedResource.setValue("Alerte",object.get("Message"));

                        }else{
                            linkedResource.setValue("Anomalie",object.get("Message"));

                        }
                        linkedResource.save(Modules.getWorkflowModule().getSysadminContext());
                        getWorkflowInstance().addLinkedResource(linkedResource);
                        getWorkflowInstance().save(Modules.getWorkflowModule().getSysadminContext());
                    }*/
                    String filePath =   new ExcelWriter().GenerateExcel(excelAnomaliesWorkFlowImportUser,"C:\\Export\\DISLOG\\Anomalies\\Collaborateurs.xlsx");
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

        user.getExtendedAttributes().setValue("NombreEnfants",collaborateurToCopyFrom.getValue("NombreEnfants"));
        user.getExtendedAttributes().setValue("Direction",collaborateurToCopyFrom.getValue("Direction"));
        user.getExtendedAttributes().setValue("Division",collaborateurToCopyFrom.getValue("Division"));

     //   user.getExtendedAttributes().setValue("NombreEnfants",collaborateurToCopyFrom.getValue("NombreEnfants"));



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
        user.save(getWorkflowModule().getSysadminContext());
        collaborateurToCopyFrom.setValue("MotDePasse",null);
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
            controller.addEqualsConstraint("Identifiant", cin);
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
            }else{
                collaborateur = Modules.getWorkflowModule().createStorageResource(context,definition,"");
            }
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

    public void exportCollaborateurs(IWorkflowInstance workflowInstance, IResourceController resourceController){
        try {
            File file = createFileFromFileCenterInLocalDrive("Maquette/Importation_CollaborateursV1.xlsx", "Export_Collaborateurs.xlsx");

            try (XSSFWorkbook exportWorkbook = new XSSFWorkbook(new FileInputStream(file));
                 FileOutputStream outputStream = new FileOutputStream("c://TEST//SIRH_V2//TmpImportation//tmpExport.xlsx")) {

                XSSFSheet exportWorkbookFirstSheet = exportWorkbook.getSheetAt(0);
                int rowIndex = 2;
                Collection<IStorageResource> collaborateurs = getCollaborateurs((IStorageResource) workflowInstance.getValue("Societe"));

                for (IStorageResource collaborateur : collaborateurs) {
                    XSSFRow row = exportWorkbookFirstSheet.createRow(rowIndex);
                    for (int i = 0; i < VariablesColonnes.sheetFields.get("_ImportationCollaborateur").size(); i++) {
                        if(VariablesColonnes.sheetFields.get("_ImportationCollaborateur").get(i).startsWith("_")){
                            XSSFCell cell = row.createCell(i);
                            String methodName = VariablesColonnes.sheetFields.get("_ImportationCollaborateur").get(i);
                            Method method = ExportCollaborateursExcel.class.getMethod(methodName, IStorageResource.class, Row.class, Cell.class);
                            Object value = method.invoke(new ExportCollaborateursExcel(), collaborateur, row, cell);
                        }

                    }
                    rowIndex++;
                }

                exportWorkbook.write(outputStream);
                exportWorkbook.close();
                outputStream.close();
                File toDelete = new File("c://TEST//SIRH_V2//TmpImportation//tmpExport.xlsx");
                FileUtils.copyFile(toDelete, file);
                toDelete.delete();
                toDelete.deleteOnExit();
            }

            File toDeleteFile = new File("C:\\TEST\\Collaborateurs.xlsx");
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
                    addAnomalie("Feuille 1","99999","Responsable hiérarchique","Le responsable hiérarchique avec l'identifiant "+N1CIN+ " n'éxiste pas sur l 'application");
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
