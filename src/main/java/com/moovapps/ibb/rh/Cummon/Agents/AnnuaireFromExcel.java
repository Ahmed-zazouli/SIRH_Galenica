

package com.moovapps.ibb.rh.Cummon.Agents;

import com.axemble.vdoc.core.helpers.PasswordHelper;
import com.axemble.vdoc.core.helpers.ValidatorHelper;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.DateUtils;
import com.moovapps.ibb.rh.Cummon.Helpers.ExcelWriter;
import com.moovapps.ibb.rh.Cummon.IEGAnnuaireColonnes;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
//import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import static com.axemble.vdoc.sdk.Modules.getDirectoryModule;
import static com.axemble.vdoc.sdk.Modules.getWorkflowModule;
import static com.axemble.vdoc.sdk.ModulesFactory.getProjectModule;

public class AnnuaireFromExcel extends BaseAgent {
    IWorkflowModule workflowModule = null;
    boolean isUpdate = false;
    private IContext loggedOnUserContext;
    IEGAnnuaireColonnes Maquetts = new IEGAnnuaireColonnes();
    ArrayList<JSONObject> excelAnomalies = new ArrayList<>();
    List<String> colonnes = Maquetts.colonnes;
    //IWorkflowInstance document = null;
    ArrayList<JSONObject> excelCommentaire = new ArrayList<>();
    HashMap<String, IUser> usersMap = new HashMap<>();
    HashMap<String, IStorageResource> EtablissementMap = new HashMap<>();
    HashMap<String, IGroup> GroupeMap = new HashMap<>();
    HashMap<String, IStorageResource> TypeContratMap = new HashMap<>();
    HashMap<IUser, String> N1Map = new HashMap<>();
    boolean isAnomalieFound = false;
    ArrayList<JSONObject> excelCommentaireBloquant = new ArrayList<>();
    ArrayList<String> errors = new ArrayList<>();
    public enum AnomalieTypes {
        A("Alert"),
        B("Anomalie");

        public final String label;

        private AnomalieTypes(String label) {
            this.label = label;
        }
    }
    public AnnuaireFromExcel(IWorkflowModule workflowModule, IWorkflowInstance document, IContext loggedOnUserContext, Logger LOGGER) {
        this.workflowModule = workflowModule;
        //this.document = document;
        this.loggedOnUserContext = loggedOnUserContext;
        getUsersAndToHashmap();
    }

    public AnnuaireFromExcel() {
        super();
    }

    @Override
    public void execute() {
        this.workflowModule = getWorkflowModule();
        getUsersAndToHashmap();
        Run();
        setN1();

    }



    private void setN1() {
        for (Map.Entry<IUser, String> entry : N1Map.entrySet()) {
            IUser user = entry.getKey();
            String N1LOgin = entry.getValue();
            if (N1LOgin != null) {
                String[] split = N1LOgin.split(",");
                String login = split[0];
                int index = Integer.valueOf(split[1]);
                IUser N1 = getDirectoryModule().getUserByLogin(login);
                if (N1 != null) {
                    user.setHierarchicalManager(N1);
                    IStorageResource userRef = getOrCreateFicheCollaborateurByUser(user);
                    userRef.setValue("HierarchicalManager",N1);
                    user.save(getWorkflowModule().getSysadminContext());
                    userRef.save(getWorkflowModule().getSysadminContext());
                } else {
                    addAnomalie(AnomalieTypes.A,"Merci de vérifier le login du responsable hiérarchique",index,"Responsable hiérarchique");
                }
            }
            // ...
        }

    }

    private void getUsersAndToHashmap() {
        Collection<IUser> users = getAllUsers();
        for (IUser user : users) {
            usersMap.put(user.getFullName().toLowerCase(), user);
        }
    }

    private Collection<IUser> getAllUsers() {
        return (Collection<IUser>) Modules.getDirectoryModule().getUsers(workflowModule.getSysadminContext());
    }

    private boolean verifyMaquette(XSSFRow row) {
        if (row.getCell(0).getStringCellValue().equals("Etablissement")
                && row.getCell(1).getStringCellValue().equals("Matricule")
                && row.getCell(2).getStringCellValue().equals("Login")
                && row.getCell(3).getStringCellValue().equals("Mot de passe")
                && row.getCell(4).getStringCellValue().equals("Prénom")
                && row.getCell(5).getStringCellValue().equals("Nom")
                && row.getCell(6).getStringCellValue().equals("Fonction")
                && row.getCell(7).getStringCellValue().equals("N+1")
                && row.getCell(8).getStringCellValue().equals("Affectation")
                && row.getCell(9).getStringCellValue().equals("Type du contrat")
                && row.getCell(10).getStringCellValue().equals("Date d'embauche")
                && row.getCell(11).getStringCellValue().equals("N° Immatriculation CNSS")
                && row.getCell(12).getStringCellValue().equals("CIN")
                && row.getCell(13).getStringCellValue().equals("Date de naissance")
                && row.getCell(14).getStringCellValue().equals("Adresse")
                && row.getCell(15).getStringCellValue().equals("Etat civil")
                && row.getCell(16).getStringCellValue().equals("Email")
                && row.getCell(17).getStringCellValue().equals("Salaire de base")
                && row.getCell(18).getStringCellValue().equals("Salaire brut")
                && row.getCell(19).getStringCellValue().equals("Salaire net")) {
            return true;
        }
        return false;
    }

    IOrganization getOrganizationByEtablissement(String etablissement) {
        try {
            IStorageResource Etablissement = null;

            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);

            controller.addEqualsConstraint("sys_Title", etablissement);
            if (definition != null) {
                Collection<IStorageResource> holder = controller.evaluate(definition);
                if (holder.size() > 0) {
                    Etablissement = holder.iterator().next();
                    if (Etablissement != null)
                        return (IOrganization) Etablissement.getValue("Organisation");
                }

            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public IStorageResource getOrCreateFicheCollaborateurByUser(IUser salarie){
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",getDirectoryModule().getOrganization(context, "DefaultOrganization"));
            ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
            IResourceDefinition resourceDefinition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            IViewController controller = workflowModule.getViewController(context, IResource.class);
            controller.addEqualsConstraint("Salarie",salarie);
            if(controller.evaluate(resourceDefinition).size()>0){
                return (IStorageResource) controller.evaluate(resourceDefinition).iterator().next();
            }else {
                return getWorkflowModule().createStorageResource(context,resourceDefinition,null,null);

            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    IUser getOrCreateUser(String etablissement, String matricule, int rowIndex, String login, String password) {

        IUser user = null;
        IUser finalUser = null;
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization etablissementOrganization = null;
                if (getOrganizationByEtablissement(etablissement) != null) {
                    etablissementOrganization = getOrganizationByEtablissement(etablissement);
                } else {
                   addAnomalie(AnomalieTypes.B,"Merci de vérifier l'etablissement",rowIndex,"Etablissement");
                   isAnomalieFound = true;
                }

                user = getUserByEtablissementMatriculleV2(matricule,etablissement);

                if(user!=null){
                    isUpdate = true;
                    return user;
                }else{
                    //create user
                    if(etablissementOrganization!=null){
                        if(!PasswordHelper.checkPasswordStrength(password, "")){
                            addAnomalie(AnomalieTypes.B,"Merci de vérifier le mot de passe",rowIndex,"Password");
                        }else{
                            finalUser = getDirectoryModule().createUser(sysContext, login, password, etablissementOrganization);
                        }
                    }else{
                        addAnomalie(AnomalieTypes.B,"Merci de vérifier l'établissement",rowIndex,"Etablissement");
                        isAnomalieFound = true;
                    }

                }



        }catch (Exception e){

        }
        return finalUser;


    }
    public String getAncienneteInDetail(Date dateEmbauche) {
        if (dateEmbauche == null) {
            return null;
        }
        long difference_In_Milliseconds = ((new Date().getTime() - dateEmbauche.getTime()));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(difference_In_Milliseconds);
        int years = c.get(Calendar.YEAR) - 1970;
        int months = c.get(Calendar.MONTH);
        int days = c.get(Calendar.DAY_OF_MONTH) - 1;
        return years + " ans " + months + " mois "/* + days + " jours"*/;
//		return (int)difference_In_Years;
    }

    void setUserCongeData(IUser user){
        user.getExtendedAttributes().setValue("AbsenceAnneeEnCours",0);
        user.getExtendedAttributes().setValue("AbsenceAnterieure",0);

        user.getExtendedAttributes().setValue("DroitMensuelle",0);
        user.getExtendedAttributes().setValue("SoldeAnneeEnCours",0);
        user.getExtendedAttributes().setValue("SoldeAnterieur",0);
        user.getExtendedAttributes().setValue("SoldeConges",0);
        user.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation",0);
        user.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation",0);
        user.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeValidation",0);
        user.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeConsommation",0);
        user.getExtendedAttributes().setValue("CongesMaladieEnCoursDeValidation",0);
        user.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation",0);
        user.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation",0);
        user.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeConsommation",0);
        user.getExtendedAttributes().setValue("JourSEnCoursDeValidation",0);
        user.getExtendedAttributes().setValue("joursEnCoursConsommation",0);
        user.getExtendedAttributes().setValue("CongesPayesAnneeEnCours",0);
        user.getExtendedAttributes().setValue("CongesPayesN1",0);
        user.getExtendedAttributes().setValue("CongesPayesPris",0);
        user.getExtendedAttributes().setValue("CongesSpeciauxAnneeEnCours",0);
        user.getExtendedAttributes().setValue("CongesSpeciauxN1",0);
        user.getExtendedAttributes().setValue("CongesSpeciauxPris",0);
        user.getExtendedAttributes().setValue("CongesMaladieAnneeEnCours",0);
        user.getExtendedAttributes().setValue("CongesMaladieN1",0);
        user.getExtendedAttributes().setValue("CongesMaladiePris",0);
        user.getExtendedAttributes().setValue("CongesSansSoldeAnneeEnCours",0);
        user.getExtendedAttributes().setValue("CongesSansSoldeN1",0);
        user.getExtendedAttributes().setValue("CongesSansSoldePris",0);
        user.getExtendedAttributes().setValue("TotalJoursPris",0);
    }
    void  setUserFileds(IStorageResource ficheCollaborateur,IUser user){
        ficheCollaborateur.setValue("Organisation",user.getOrganization());
        ficheCollaborateur.setValue("Actif",user.isEnable());
        ficheCollaborateur.setValue("Matricule",user.getExtendedAttributes().getValue("Matricule"));
        ficheCollaborateur.setValue("justImported",user.getExtendedAttributes().getValue("justImported"));

        //civilité
        ficheCollaborateur.setValue("Title",user.getTitle());
        ficheCollaborateur.setValue("LastName",user.getLastName());
        ficheCollaborateur.setValue("FirstName",user.getFirstName());
        ficheCollaborateur.setValue("MobilePhoneNumber",user.getMobilePhoneNumber());
        ficheCollaborateur.setValue("Groupe",user.getExtendedAttributes().getValue("Groupe"));

        ficheCollaborateur.setValue("Birthday",user.getBirthday());
        ficheCollaborateur.setValue("CIN",user.getExtendedAttributes().getValue("CIN"));
        ficheCollaborateur.setValue("NCNSS",user.getExtendedAttributes().getValue("NCNSS"));
        ficheCollaborateur.setValue("Address1",user.getAddress1());
        ficheCollaborateur.setValue("EtatCivil",user.getExtendedAttributes().getValue("EtatCivil"));
        //ficheCollaborateur.setValue("Avatar",user.getTitle());
        ficheCollaborateur.setValue("NombreEnfants",user.getExtendedAttributes().getValue("NombreEnfants"));
        ficheCollaborateur.setValue("Email",user.getEmail());
        ficheCollaborateur.setValue("Identifiant",user.getLogin());
       // ficheCollaborateur.setValue("MotDePasse","D3mo@demo");
        //ficheCollaborateur.setValue("ConfirmerMotDePasse",user.getTitle());
        ficheCollaborateur.setValue("NCompteBancaire",user.getExtendedAttributes().getValue("NCompteBancaire"));
        ficheCollaborateur.setValue("Banque",user.getExtendedAttributes().getValue("Banque"));
        ficheCollaborateur.setValue("AgenceBancaire",user.getExtendedAttributes().getValue("AgenceBancaire"));
        ficheCollaborateur.setValue("DateDEmbauche",user.getExtendedAttributes().getValue("DateDEmbauche"));
        ficheCollaborateur.setValue("AncienneteInDetail",getAncienneteInDetail((Date)user.getExtendedAttributes().getValue("DateDEmbauche")));
        ficheCollaborateur.setValue("TypeDuContrat",user.getExtendedAttributes().getValue("TypeDuContrat"));
        //Etablisement
        ficheCollaborateur.setValue("SocieteDonnee",user.getExtendedAttributes().getValue("SocieteDonnee"));
        ficheCollaborateur.setValue("Groupes",user.getExtendedAttributes().getValue("Groupes"));

        ficheCollaborateur.setValue("HierarchicalManager",user.getHierarchicalManager());
        ficheCollaborateur.setValue("Exit",user.getExit());
        ficheCollaborateur.setValue("MotifSortie",user.getExtendedAttributes().getValue("MotifSortie"));
        ficheCollaborateur.setValue("TauxHoraire",user.getExtendedAttributes().getValue("TauxHoraire"));
        ficheCollaborateur.setValue("SalaireDeBase",user.getExtendedAttributes().getValue("SalaireDeBase"));
        ficheCollaborateur.setValue("SalaireBrutDH",user.getExtendedAttributes().getValue("SalaireBrut"));
        ficheCollaborateur.setValue("SalaireNETDH",user.getExtendedAttributes().getValue("Salaire"));
        ficheCollaborateur.setValue("Salaire",user.getExtendedAttributes().getValue("Salaire"));
        ficheCollaborateur.setValue("AbsenceAnneeEnCours",user.getExtendedAttributes().getValue("AbsenceAnneeEnCours"));
        ficheCollaborateur.setValue("AbsenceAnterieure",user.getExtendedAttributes().getValue("AbsenceAnterieure"));

        ficheCollaborateur.setValue("DroitMensuelle",user.getExtendedAttributes().getValue("DroitMensuelle"));
        ficheCollaborateur.setValue("SoldeAnneeEnCours",user.getExtendedAttributes().getValue("SoldeAnneeEnCours"));
        ficheCollaborateur.setValue("SoldeAnterieur",user.getExtendedAttributes().getValue("SoldeAnterieur"));
        ficheCollaborateur.setValue("SoldeConges",user.getExtendedAttributes().getValue("SoldeConges"));
        ficheCollaborateur.setValue("CongesPayesEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));
        ficheCollaborateur.setValue("CongesPayesEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation"));
        ficheCollaborateur.setValue("CongesSpeciauxEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation"));
        ficheCollaborateur.setValue("CongesSpeciauxEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation"));
        ficheCollaborateur.setValue("CongesMaladieEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation"));
        ficheCollaborateur.setValue("CongesMaladieEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
        ficheCollaborateur.setValue("CongesSansSoldeEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation"));
        ficheCollaborateur.setValue("CongesSansSoldeEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation"));
        ficheCollaborateur.setValue("JourSEnCoursDeValidation",user.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
        ficheCollaborateur.setValue("joursEnCoursConsommation",user.getExtendedAttributes().getValue("joursEnCoursConsommation"));
        ficheCollaborateur.setValue("CongesPayesAnneeEnCours",user.getExtendedAttributes().getValue("CongesPayesAnneeEnCours"));
        ficheCollaborateur.setValue("CongesPayesN1",user.getExtendedAttributes().getValue("CongesPayesN1"));
        ficheCollaborateur.setValue("CongesPayesPris",user.getExtendedAttributes().getValue("CongesPayesPris"));
        ficheCollaborateur.setValue("CongesSpeciauxAnneeEnCours",user.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours"));
        ficheCollaborateur.setValue("CongesSpeciauxN1",user.getExtendedAttributes().getValue("CongesSpeciauxN1"));
        ficheCollaborateur.setValue("CongesSpeciauxPris",user.getExtendedAttributes().getValue("CongesSpeciauxPris"));
        ficheCollaborateur.setValue("CongesMaladieAnneeEnCours",user.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours"));
        ficheCollaborateur.setValue("CongesMaladieN1",user.getExtendedAttributes().getValue("CongesMaladieN1"));
        ficheCollaborateur.setValue("CongesMaladiePris",user.getExtendedAttributes().getValue("CongesMaladiePris"));
        ficheCollaborateur.setValue("CongesSansSoldeAnneeEnCours",user.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours"));
        ficheCollaborateur.setValue("CongesSansSoldeN1",user.getExtendedAttributes().getValue("CongesSansSoldeN1"));
        ficheCollaborateur.setValue("CongesSansSoldePris",user.getExtendedAttributes().getValue("CongesSansSoldePris"));
        ficheCollaborateur.setValue("TotalJoursPris",user.getExtendedAttributes().getValue("TotalJoursPris"));

        ficheCollaborateur.setValue("Fonction",user.getExtendedAttributes().getValue("Fonction"));
        ficheCollaborateur.setValue("Fonction2",user.getExtendedAttributes().getValue("Fonction2"));
        ficheCollaborateur.setValue("DisciplineEnseignee",user.getExtendedAttributes().getValue("DisciplineEnseignee"));

        //ficheCollaborateur.setValue("ConfirmerMotDePasse","D3mo@demo");
        ficheCollaborateur.setValue("Salarie",user);






        ficheCollaborateur.save(getWorkflowModule().getSysadminContext());
    }
    private void addAnomalie(AnomalieTypes anomalieOuAlerte, String message, int rowIndex, String column){
        JSONObject tmp = new JSONObject();
        tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
        tmp.put("Cordonnée",  "Cellule: " + column);
        tmp.put(anomalieOuAlerte.label, message);
        excelAnomalies.add(tmp);

    }
    private void Run() {
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            File excelFile = new File("D:\\IEG Maquette\\IEG20032023.xlsx");

            XSSFWorkbook workbook;
            int rowIndex = 0;
            try {
                workbook = new XSSFWorkbook(new FileInputStream(excelFile));
                XSSFSheet firstSheet = workbook.getSheetAt(0);
                Iterator<Row> annuaireIterator = firstSheet.iterator();
                rowIndex = 0;

                while (annuaireIterator.hasNext()) {
                    isAnomalieFound = false;
                    isUpdate = false;
                    Row nextRow = annuaireIterator.next();
                    if (rowIndex == 0) {
                        if (false/* !verifyMaquette(firstSheet.getRow(rowIndex))*/) {
                            JSONObject tmp = new JSONObject();
                            tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                            tmp.put("Cordonnée", "Maquette");
                            tmp.put("Anomalie", "Merci de vérifier la maquette utilisé");
                            excelCommentaireBloquant.add(tmp);
                            break;
                        }
                    }
                    // rowIndex++;
                    if (nextRow == null) continue;
                    if (rowIndex > 0) {
                        if (firstSheet.getRow(rowIndex) == null) {
                            break;
                        }

                        IUser user = null;
                        IStorageResource ficheCollaborateur = null;
                        String etablissment = "";
                        if(firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Etablissement"))!=null &&firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Etablissement")).getCellType()!= CellType.BLANK){
                            etablissment = firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Etablissement")).getStringCellValue().trim();
                        }else{
                            addAnomalie(AnomalieTypes.B, "Le nom etablissement est obligatoire",rowIndex,"Etablissement");
                            isAnomalieFound = true;
                        }

                        String matricule = "";
                        if(firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Matricule"))!=null &&firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Matricule")).getCellType() != CellType.BLANK){
                            firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Matricule")).setCellType(CellType.STRING);
                            matricule =  firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Matricule")).getStringCellValue().trim() ;// ((Number)firstSheet.getRow(rowIndex).getCell(1).getNumericCellValue()).intValue()+"";
                        }else{
                            addAnomalie(AnomalieTypes.A, "Le matricule est vide",rowIndex,"Matricule");

                        }
                        String login = "";
                        if(firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Login"))!=null && firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Login")).getCellType() != CellType.BLANK){
                            login = firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Login")).getStringCellValue().trim();
                        }else{
                            addAnomalie(AnomalieTypes.B, "Le login est obligatoire",rowIndex,"Login");
                            isAnomalieFound = true;
                        }
                        String password = "";
                        if(firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Password"))!=null && firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Password")).getCellType() != CellType.BLANK){
                            password = firstSheet.getRow(rowIndex).getCell(colonnes.indexOf("_Password")).getStringCellValue().trim();
                        }else{
                            addAnomalie(AnomalieTypes.B, "Le mot de passe est obligatoire",rowIndex,"Password");
                            isAnomalieFound = true;
                        }
                        user = getOrCreateUser(etablissment, matricule, rowIndex, login, password);
                        if(user == null){
                            rowIndex++;
                            continue;
                        }
                        user.setLanguage("fr");
                        for (int i = 0; i < colonnes.size(); i++) {

                            String colonne = colonnes.get(i);
                            Cell cell = null;
                            if (firstSheet.getRow(rowIndex) != null && firstSheet.getRow(rowIndex).getCell(i) != null) {
                                cell = firstSheet.getRow(rowIndex).getCell(i);
                            }
                            if (colonne.startsWith("_")) {
                                try {
                                    Method method = this.getClass().getMethod(colonnes.get(i), IUser.class, Cell.class, int.class);
                                    method.invoke( this,user, cell, rowIndex);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        LOGGER.error(rowIndex+"");
                        user.save(sysContext);
                        ficheCollaborateur = getOrCreateFicheCollaborateurByUser(user);
                        if(ficheCollaborateur!=null){
                            setUserFileds(ficheCollaborateur,user);
                        }
                    }

                    rowIndex++;
                }
                if(!excelAnomalies.isEmpty()){

                    String filePath =   new ExcelWriter().GenerateExcel(excelAnomalies,"D:\\Export\\IEG\\Anomalies\\user-anomalies.xlsx");
                    if(!filePath.equals("")){
                        //add piece joint
                    }



                }

            } catch (Exception e) {
                if( e.getMessage()!=null && e.getMessage().equals("L'identifiant existe déja")){
                    addAnomalie(AnomalieTypes.B, "L'identifiant exist deja",rowIndex,"Login");
                    isAnomalieFound = true;
                }
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (JSONObject comment : excelCommentaireBloquant) {
             /*   ILinkedResource commentTD = document.createLinkedResource("CommentaireExcel2");
                commentTD.setValue("Ligne", comment.get("Ligne"));
                commentTD.setValue("Cordonnee", comment.get("Cordonnée"));
                commentTD.setValue("Anomalie", comment.get("Anomalie"));
                document.addLinkedResource(commentTD);
                commentTD.save(loggedOnUserContext);*/
        }
    }

    IUser getUserByEtablissementMatriculle(String login, String etablissement) {
        String userEtablissement = "";
        String userLogin = "";
        for (IUser user : usersMap.values()) {
            if (user.getExtendedAttributes().getValue("SocieteDonnee") != null) {
                userEtablissement = (String) ((IStorageResource) user.getExtendedAttributes().getValue("SocieteDonnee")).getValue("sys_Title");
            }
            userLogin = user.getLogin();

            if (!userEtablissement.equals("")) {
                if (userEtablissement.equals(etablissement) && userLogin.equals(login)) {
                    return user;
                }
            }
        }
        return null;
    }

    IUser getUserByEtablissementMatriculleV2(String matricule, String etablissement) {
        String userEtablissement = "";
        String userMatricule = "";
        for (IUser user : usersMap.values()) {
            if (user.getExtendedAttributes().getValue("SocieteDonnee") != null) {
                userEtablissement = (String) ((IStorageResource) user.getExtendedAttributes().getValue("SocieteDonnee")).getValue("sys_Title");
            }
            userMatricule = (String) user.getExtendedAttributes().getValue("Matricule");

            if (!userEtablissement.equals("")) {
                if (userEtablissement.equals(etablissement) && userMatricule.equals(matricule)) {
                    return user;
                }
            }
        }
        return null;
    }


    public void _N1(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if(cellValue!=null){
                if(cellValue instanceof  String){
                    IUser N1 = getDirectoryModule().getUserByLogin((String)cellValue);
                    if (N1 != null) {
                        user.setHierarchicalManager(N1);
                    } else {
                        N1Map.put(user, cellValue + "," + rowIndex);
                    }
                }
            }
        }
    }

    public void _Etablissement(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                IStorageResource etablissement = getEtablissement(((String) cellValue).toLowerCase());
                if (etablissement == null) {
                    addAnomalie(AnomalieTypes.B,"Merci de vérifier l'etablissement",rowIndex,"Etablissement");
                    isAnomalieFound = true;
                } else {
                    user.getExtendedAttributes().setValue("SocieteDonnee", etablissement);
                    IOrganization orga = (IOrganization) etablissement.getValue("Organisation");
                    user.setOrganization(orga);

                }

            }

        }


    }

    private IStorageResource getEtablissement(String EtablissementName) {
        if (EtablissementMap.containsKey(EtablissementName)) {
            return EtablissementMap.get(EtablissementName);
        } else {
            return getEtablissementFromDBByName(EtablissementName);
        }
    }

    private IStorageResource getEtablissementFromDBByName(String EtablissementName) {
        IStorageResource Etablissement = null;
        try {
            IContext context = workflowModule.getSysadminContext();
            IViewController controller = workflowModule.getViewController(context, IResource.class);
            IResourceDefinition definition = getEtablissementNameDefinition();
            controller.addEqualsConstraint("sys_Title", EtablissementName);
            if (definition != null) {
                Collection<IStorageResource> holder = controller.evaluate(definition);
                if (holder.size() > 0) {
                    Etablissement = holder.iterator().next();
                    EtablissementMap.put(EtablissementName, Etablissement);
                }
                holder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Etablissement;
    }

    private IResourceDefinition getEtablissementNameDefinition() {
        IResourceDefinition definition = null;
        try {
            IContext context = workflowModule.getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            definition = workflowModule.getResourceDefinition(context, catalog, "Societe");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return definition;
    }

    ////////////////////////////

    public ArrayList<IGroup> getGroupes(String[] grouupes, int rowIndex) {
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization IEGOrga = getDirectoryModule().getOrganization(sysContext, "IEGGroupes");
            ArrayList<IGroup> groupes = new ArrayList<IGroup>();
            for (String groupe : grouupes) {
                if (groupe != null && !groupe.equals("")) {
                    if (groupe.equals("Salarie")) {
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "Salarie"));
                    } else if (groupe.equals("RH")) {
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "RH"));
                    } else if (groupe.equals("Direction")) {
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "GroupePersonnelDeDirection"));
                    } else if (groupe.equals("Administratif")) {
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "Collaborateurs_Eval"));
                    } else if (groupe.equals("Manager")) {
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "Manager"));
                    } else if (groupe.equals("Evaluateur")) {
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "Evaluateurs"));
                    } else if (groupe.equals("Enseignant")) {
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "GroupeEnseignants"));
                    } else if(groupe.equals("IEGGroupe")){
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "ADMIN"));
                    } else if(groupe.equals("DRH")){
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "DRH"));
                    }else if(groupe.equals("Consultation evaluation")){
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "ConsultationEvaluation"));
                    }else if(groupe.equals("Conseiller pédagogique")){
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "SupportPedagogique"));
                    }else if(groupe.equals("Documentaliste")){
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "Documentaliste"));
                    }else if(groupe.equals("Conseiller Principal d’Education")){
                        groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "CPE"));
                    }
                    else {
                       addAnomalie(AnomalieTypes.A,"Merci de verifier le groupe : " + groupe,rowIndex,"Roles");
                    }
                }

            }
            groupes.add(getDirectoryModule().getGroup(sysContext, IEGOrga, "NEWS_ANIMATION"));
            return groupes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void _Affectation(IUser user, Cell cell, int rowIndex) {

        String[] groupes = null;

        if (cell != null && cell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                String groupeValue = ((String) cellValue);
                groupes = groupeValue.split(",");

                ArrayList<IGroup> userGroupes = getGroupes(groupes, rowIndex);
                if (userGroupes == null) {
                    /*if (!errors.contains("Alerte " + "Merci d'adjuster l'affectation: " + cellValue)) {
                        JSONObject tmp = new JSONObject();
                        tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                        tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_Affectation")));
                        tmp.put("Alerte", "Merci d'adjuster l'affectation: " + cellValue);
                        excelCommentaire.add(tmp);
                        errors.add("Merci d'adjuster l'affectation: " + cellValue);
                    }*/
                } else {
                    try {
                        IContext context = getWorkflowModule().getSysadminContext();
                        IOrganization organization = getDirectoryModule().getOrganization(context, "IEGGroupes");
                        IGroup GroupeEnseignants = getDirectoryModule().getGroup(context, organization, "GroupeEnseignants");
                        IGroup GroupeAdministratif = getDirectoryModule().getGroup(context, organization, "Collaborateurs_Eval");
                        IGroup GroupeDirection = getDirectoryModule().getGroup(context, organization, "GroupePersonnelDeDirection");
                        int sum = 0;
                        for (IGroup g : userGroupes) {
                            if (g.getId().equals(GroupeEnseignants.getId())) {
                                sum += 1;
                                //  groupesToRemove.add(g);
                                user.getExtendedAttributes().setValue("Groupe", "GroupeEnseignants");
                            }
                            if (g.getId().equals(GroupeAdministratif.getId())) {
                                sum += 1;
                                // groupesToRemove.add(g);
                                user.getExtendedAttributes().setValue("Groupe", "Collaborateurs_Eval");
                            }
                            if (g.getId().equals(GroupeDirection.getId())) {
                                sum += 1;
                                // groupesToRemove.add(g);
                                user.getExtendedAttributes().setValue("Groupe", "GroupePersonnelDeDirection");
                            }
                            if (sum <= 1) {
                                user.getExtendedAttributes().setValue("Groupes", userGroupes);
                                g.addMember(user);
                            } else {
                               addAnomalie(AnomalieTypes.A,"Merci de choisir just un group (Enseignant / Direction / Administrtatif) ",rowIndex,"Affectation");
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }else{
                addAnomalie(AnomalieTypes.B,"Merci d'ajouter l'affectation: ",rowIndex,"Affectation");
                isAnomalieFound = true;
            }

        }


    }

    public void _Matricule(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                user.getExtendedAttributes().setValue("Matricule", cellValue);
            }/* else {
                if (!errors.contains("Alerte " + "Merci d'ajouter le matricule: " + cellValue)) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                    tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_Matricule")));
                    tmp.put("Alerte", "Merci d'ajouter le matricule: " + cellValue);
                    excelCommentaire.add(tmp);
                    errors.add("Alerte " + "Merci d'ajouter le matricule: " + cellValue);
                }
                // }
            }*/

        }

    }


    ////////////////////////////


    public void _Prenom(IUser user, Cell cell, int rowIndex) {
        //boolean continueFactureImport = true;
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                user.setFirstName((String) cellValue);
                // instance.setValue("Categorie2",getProcedureCategorie());
            } else {
                addAnomalie(AnomalieTypes.B,"Merci d'ajouter le prénom ",rowIndex,"Prénom");
                isAnomalieFound = true;
            }

        }
    }

    public void _Nom(IUser user, Cell cell, int rowIndex) {
        //boolean continueFactureImport = true;
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                user.setLastName((String) cellValue);                // instance.setValue("Categorie2",getProcedureCategorie());
            } else {
                addAnomalie(AnomalieTypes.B,"Merci d'ajouter le nom ",rowIndex,"Nom");
                isAnomalieFound = true;
            }

        }


    }

    public void _Fonction(IUser user, Cell cell, int rowIndex) {
        //boolean continueFactureImport = true;
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                user.getExtendedAttributes().setValue("Fonction", cellValue);
            } else {
                addAnomalie(AnomalieTypes.A,"Merci d'ajouter la fonction ",rowIndex,"Fonction");
            }

        }
    }

    public void _CIN(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                user.getExtendedAttributes().setValue("CIN", cellValue);
            } else {
                addAnomalie(AnomalieTypes.A,"Merci d'ajouter le CIN ",rowIndex,"CIN");
            }

        }
    }

    public void _CNSS(IUser user, Cell cell, int rowIndex) {
        //boolean continueFactureImport = true;
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                // cell.setCellType(CellType.STRING);
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                user.getExtendedAttributes().setValue("NCNSS", cellValue+"");
                // instance.setValue("Categorie2",getProcedureCategorie());
            } else {
                addAnomalie(AnomalieTypes.A,"Merci d'ajouter la CNSS ",rowIndex,"CNSS");
            }

        }
    }

    public void _Adresse(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                user.setAddress1((String) cellValue);
            } else {
                addAnomalie(AnomalieTypes.A,"Merci d'ajouter l'adresse ",rowIndex,"Adresse");

            }

        }
    }

    public void _DateEmbauche(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getDateCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                try {
                    cellValue = cell.getStringCellValue().trim();
                    cellValue = new SimpleDateFormat("dd/MM/yyyy").parse((String) cellValue);
                }catch (Exception e ){
                   addAnomalie(AnomalieTypes.A,"Merci de vérifier la date d'embauche ",rowIndex,"Date d'embauche");
                    e.printStackTrace();
                }
            }
            if (cellValue != null) {
                // Date date = DateUtils.parseDate((String) cellValue, "dd-MM-yyyy");
                user.getExtendedAttributes().setValue("DateDEmbauche",cellValue);



            } else {
                addAnomalie(AnomalieTypes.A,"Merci d'ajouter la date d'embauche ",rowIndex,"Date d'embauche");

            }
        }
    }

    public void _DateNaissance(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getDateCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                try {
                    cellValue = cell.getStringCellValue().trim();
                    cellValue = new SimpleDateFormat("dd/MM/yyyy").parse((String) cellValue);
                }catch (Exception e ){
                    addAnomalie(AnomalieTypes.A,"Merci de vérifier la date de naissance ",rowIndex,"Date de naissance");
                    e.printStackTrace();
                }
            }
            if (cellValue != null) {
                user.setBirthday((Date)cellValue);
            } else {
                addAnomalie(AnomalieTypes.A,"Merci d'ajouter la date de naissance ",rowIndex,"Date de naissance");
            }
        }
    }

    public void _Email(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                try{
                    if(ValidatorHelper.validate( cellValue, IValidators.VALIDATOR_EMAIL )) {
                        user.setEmail((String) cellValue);
                    }
                }catch (Exception e){
                    addAnomalie(AnomalieTypes.A,"Merci de vérifier l'email ",rowIndex,"Email");
                }

            } else {
                addAnomalie(AnomalieTypes.A,"Merci d'ajouter l'email ",rowIndex,"Email");

            }
        }
    }


    public void _Sex(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                String EtatCivil = (String) cellValue;
                if (EtatCivil.equals("F")) {
                     user.setSex("F");
                    user.setTitle("Mme");
                } else if (EtatCivil.equals("M") || EtatCivil.equals("H")) {
                     user.setSex("H");
                    user.setTitle("Mr");
                } else {
                    addAnomalie(AnomalieTypes.A,"Merci de vérifier le sex ",rowIndex,"Sex");
                }

            } else {
                addAnomalie(AnomalieTypes.A,"Merci d'ajouter le sex ",rowIndex,"Sex");

            }
        }
    }

    public void _Contrat(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                IStorageResource contrat = getContrat(((String) cellValue).toLowerCase());
                if (contrat == null) {
                    addAnomalie(AnomalieTypes.A,"Merci de vérifier le type contrat ",rowIndex,"Type contrat");
                } else {
                    user.getExtendedAttributes().setValue("TypeDuContrat", contrat);
                }

            }else{
                addAnomalie(AnomalieTypes.A,"Merci d'ajouter le type contrat ",rowIndex,"Type contrat");

            }

        }


    }

    private IStorageResource getContrat(String contratName) {
        if (TypeContratMap.containsKey(contratName)) {
            return TypeContratMap.get(contratName);
        } else {
            return getContratFromDBByName(contratName);
        }
    }

    private IStorageResource getContratFromDBByName(String contratName) {
        IStorageResource Contrat = null;
        try {
            IContext context = workflowModule.getSysadminContext();
            IViewController controller = workflowModule.getViewController(context, IResource.class);
            IResourceDefinition definition = getContratNameDefinition();
            controller.addEqualsConstraint("sys_Title", contratName);
            if (definition != null) {
                Collection<IStorageResource> holder = controller.evaluate(definition);
                if (holder.size() > 0) {
                    Contrat = holder.iterator().next();
                    TypeContratMap.put(contratName, Contrat);
                }
                holder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Contrat;
    }

    private IResourceDefinition getContratNameDefinition() {
        IResourceDefinition definition = null;
        try {
            IContext context = workflowModule.getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Recrutement", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);

            //IProject project = getProjectModule().getProject(context,"S2MRecouvrement", document.getCatalog().getProject().getOrganization());
            //ICatalog catalog = workflowModule.getCatalog(context,"Referenciel", ICatalog.IType.STORAGE, project);
            definition = workflowModule.getResourceDefinition(context, catalog, "TypeContrat");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return definition;
    }

    public void _SalaireDeBase(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                if(cellValue instanceof Number){
                    Double salaire = (Double) cellValue;
                    if(salaire>0){
                        user.getExtendedAttributes().setValue("SalaireDeBase", cellValue);
                    }else{
                        if (!errors.contains("Alerte " + "Merci de vérifier le salaire de base: " + cellValue)) {
                            JSONObject tmp = new JSONObject();
                            tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                            tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_SalaireDeBase")));
                            tmp.put("Alerte", "Merci de vérifier le salaire de base: " + cellValue);
                            excelCommentaire.add(tmp);
                            errors.add("Alerte " + "Merci de vérifier le salaire de base: " + cellValue);
                        }
                    }
                }else{
                    if (!errors.contains("Alerte " + "Merci de vérifier le salaire de base: " + cellValue)) {
                        JSONObject tmp = new JSONObject();
                        tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                        tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_SalaireDeBase")));
                        tmp.put("Alerte", "Merci de vérifier le salaire de base: " + cellValue);
                        excelCommentaire.add(tmp);
                        errors.add("Alerte " + "Merci de vérifier le salaire de base: " + cellValue);
                    }
                }

            } else {
                if (!errors.contains("Alerte " + "Merci d'ajouter le salaire de base: " + cellValue)) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                    tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_SalaireDeBase")));
                    tmp.put("Alerte", "Merci d'ajouter le salaire de base: " + cellValue);
                    excelCommentaire.add(tmp);
                    errors.add("Alerte " + "Merci d'ajouter le salaire de base: " + cellValue);
                }
                // }
            }

        }

    }

    public void _Login(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                user.setLogin((String) cellValue);
            } else {
                if (!errors.contains("Alerte " + "Merci d'ajouter le login: " + cellValue)) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                    tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_Login")));
                    tmp.put("Alerte", "Merci d'ajouter le login: " + cellValue);
                    excelCommentaire.add(tmp);
                    errors.add("Alerte " + "Merci d'ajouter le login: " + cellValue);
                }
                // }
            }

        }

    }

    public void _SalaireBrut(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                if(cellValue instanceof Number){
                    Double salaire = (Double) cellValue;
                    if(salaire>0){
                        user.getExtendedAttributes().setValue("SalaireBrut", cellValue);
                    }else{
                        if (!errors.contains("Alerte " + "Merci de vérifier le salaire brut: " + cellValue)) {
                            JSONObject tmp = new JSONObject();
                            tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                            tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_SalaireBrut")));
                            tmp.put("Alerte", "Merci de vérifier le salaire brut: " + cellValue);
                            excelCommentaire.add(tmp);
                            errors.add("Alerte " + "Merci de vérifier le salaire brut: " + cellValue);
                        }
                    }
                }else{
                    if (!errors.contains("Alerte " + "Merci de vérifier le salaire brut: " + cellValue)) {
                        JSONObject tmp = new JSONObject();
                        tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                        tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_SalaireBrut")));
                        tmp.put("Alerte", "Merci de vérifier le salaire brut: " + cellValue);
                        excelCommentaire.add(tmp);
                        errors.add("Alerte " + "Merci de vérifier le salaire brut: " + cellValue);
                    }
                }

            } else {
                if (!errors.contains("Alerte " + "Merci d'ajouter le salaire brut: " + cellValue)) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                    tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_SalaireBrut")));
                    tmp.put("Alerte", "Merci d'ajouter le salaire brut: " + cellValue);
                    excelCommentaire.add(tmp);
                    errors.add("Alerte " + "Merci d'ajouter le salaire brut: " + cellValue);
                }
                // }
            }

        }

    }

    public void _SalaireNet(IUser user, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                if(cellValue instanceof Number){
                    Double salaire = (Double) cellValue;
                    if(salaire>0){
                        user.getExtendedAttributes().setValue("Salaire", cellValue);
                    }else{
                        if (!errors.contains("Alerte " + "Merci de vérifier le salaire net: " + cellValue)) {
                            JSONObject tmp = new JSONObject();
                            tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                            tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_SalaireNet")));
                            tmp.put("Alerte", "Merci de vérifier le salaire net: " + cellValue);
                            excelCommentaire.add(tmp);
                            errors.add("Alerte " + "Merci de vérifier le salaire net: " + cellValue);
                        }
                    }
                }else{
                    if (!errors.contains("Alerte " + "Merci de vérifier le salaire net: " + cellValue)) {
                        JSONObject tmp = new JSONObject();
                        tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                        tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_SalaireNet")));
                        tmp.put("Alerte", "Merci de vérifier le salaire net: " + cellValue);
                        excelCommentaire.add(tmp);
                        errors.add("Alerte " + "Merci de vérifier le salaire net: " + cellValue);
                    }
                }

            } else {
                if (!errors.contains("Alerte " + "Merci d'ajouter le salaire net: " + cellValue)) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (rowIndex + 1));
                    tmp.put("Cordonnée", "Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("_SalaireNet")));
                    tmp.put("Alerte", "Merci d'ajouter le salaire net: " + cellValue);
                    excelCommentaire.add(tmp);
                    errors.add("Alerte " + "Merci d'ajouter le salaire net: " + cellValue);
                }
                // }
            }

        }

    }




}

