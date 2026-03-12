package com.moovapps.ibb.rh.FicheSalarie.Formulaire;


import com.axemble.vdoc.core.helpers.PasswordHelper;
import com.axemble.vdoc.directory.exceptions.InvalidPasswordException;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.listeners.ConfirmBoxListener;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.workflow.domain.ProcessWorkflowInstance;
import com.moovapps.ibb.rh.Cummon.DateHelper;
import com.moovapps.ibb.rh.FicheSalarie.Vues.FicheSalarieDossierAdministratif;
import org.apache.commons.io.FileUtils;

import javax.transaction.Transaction;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class CreationSalarie extends BaseDocumentExtension {

    DateHelper dateHelper = new DateHelper();

    String typeContrat = null;
    HashMap<String, Object> oldValues = null;
    IStorageResource oldFonction = null;
    String oldContrat = "";
    Date oldDateSortie = null;
    Boolean oldIsActif = false;

    @SuppressWarnings("unchecked")
    @Override
    public boolean onAfterLoad() {
        oldValues = new HashMap<>();
        for (IProperty prop : getWorkflowInstance().getDefinition().getProperties()) {
            oldValues.put(prop.getName(), getWorkflowInstance().getValue(prop.getName()));
        }


//		try {
//			 oldWorkflow = DeepCopyUtil.deepCopy(getWorkflowInstance());
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		} catch (ClassNotFoundException e) {
//			throw new RuntimeException(e);
//		}
        typeContrat = (String) getWorkflowInstance().getValue("ContractType");
        String[] parameterValues = Navigator.getNavigator().getExecutionContext().getRequest().getParameterValues("flag");
        String flag = null;
        if (parameterValues != null && parameterValues.length > 0) {
            flag = parameterValues[0];
        }
        getWorkflowInstance().setValue("flag", flag != null ? flag : "false");

        if (getWorkflowInstance().getValue("DateDEmbauche") != null) {
            getWorkflowInstance().setValue("AncienneteInDetail", dateHelper.getDurationFromADateToNow((Date) getWorkflowInstance().getValue("DateDEmbauche")));
        } else {
            getWorkflowInstance().setValue("AncienneteInDetail", null);
        }

        if (getWorkflowInstance().getValue("DateDEmbaucheGroupe") != null) {
            getWorkflowInstance().setValue("AncienneteInDetailGroupe", dateHelper.getDurationFromADateToNow((Date) getWorkflowInstance().getValue("DateDEmbaucheGroupe")));
        } else {
            getWorkflowInstance().setValue("AncienneteInDetailGroupe", null);
        }

        oldFonction = (IStorageResource) getWorkflowInstance().getValue("Fonction");
        oldContrat = getWorkflowInstance().getValue("ContractType")!=null?(String)getWorkflowInstance().getValue("ContractType"):"";
        oldDateSortie = (Date) getWorkflowInstance().getValue("DateDeSortie");
        oldIsActif =(Boolean) getWorkflowInstance().getValue("Actif");
        return super.onAfterLoad();
    }

    //IResource societe=null;
    @Override
    public void onPropertyChanged(IProperty property) {
        setTargetNullOnSourceChange(property,"Pole","Societe");
        setTargetNullOnSourceChange(property,"Societe","Departement");
        setTargetNullOnSourceChange(property,"Departement","Activite");
        setTargetNullOnSourceChange(property,"Activite","Service");
        setTargetNullOnSourceChange(property,"Societe","Metier");
        setTargetNullOnSourceChange(property,"Societe","Fonction");
        setTargetNullOnSourceChange(property,"Fonction","Grade");
        setTargetNullOnSourceChange(property,"Societe","Site");


        if (property.getName().equals("Societe")) {
            //	document.setValue("Direction", null);
            //	document.setValue("Groupes", null);
            setOrganization();
        } else if (property.getName().equals("DateDEmbauche")) {
            if (getWorkflowInstance().getValue("DateDEmbauche") != null) {
                getWorkflowInstance().setValue("AncienneteInDetail", dateHelper.getDurationFromADateToNow((Date) getWorkflowInstance().getValue("DateDEmbauche")));
            } else {
                getWorkflowInstance().setValue("AncienneteInDetail", null);
            }
        } else if (property.getName().equals("Groupes")) {
            onGroupeChange();
        } else if (property.getName().equals("ContractType")) {
            onTypeContratChange();
        }else if (property.getName().equals("DateDEmbaucheGroupe")) {
            if (getWorkflowInstance().getValue("DateDEmbaucheGroupe") != null) {
                getWorkflowInstance().setValue("AncienneteInDetailGroupe", dateHelper.getDurationFromADateToNow((Date) getWorkflowInstance().getValue("DateDEmbaucheGroupe")));
            } else {
                getWorkflowInstance().setValue("AncienneteInDetailGroupe", null);
            }
        }else if(property.getName().equals("Fonction")){
            if(getWorkflowInstance().getValue("Fonction")!=null){
                getResourceController().confirm("vous venez de changer la fonction votre missions seront réinitialiser", new ConfirmBoxListener() {
                    @Override
                    public void onOk(ActionEvent event) {
                        //get  mission collaborateur then historise  then delete
                        ArrayList<IStorageResource> missions = getMissionsCollaborateur();
                        if(missions!=null && !missions.isEmpty()){
                            for(IStorageResource mission : missions){
                                historiseMissionCollaborateur(mission);
                            }
                        }

                        // get fonction mission // create them for collaboarteur
                        IStorageResource fontion = (IStorageResource) getWorkflowInstance().getValue("Fonction");
                        ArrayList<IStorageResource> missionsFonction = getMissionsFonction(fontion);
                        if(missionsFonction!=null && !missionsFonction.isEmpty()){
                            for(IStorageResource mission : missionsFonction){
                                createMissionForCollaborateurFromMissionFonction(mission,getFicheCollaborateurByReference((String) getWorkflowInstance().getValue("sys_Reference")));
                            }
                        }
                    }

                    @Override
                    public void onCancel(ActionEvent event) {


                    }
                });
            }
        }
        super.onPropertyChanged(property);
    }

    private void onTypeContratChange() {
       // getWorkflowInstance().setValue("DateDEmbauche", null);
        getWorkflowInstance().setValue("NCompteBancaire", null);
        getWorkflowInstance().setValue("Banque", null);
        getWorkflowInstance().setValue("AgenceBancaire", null);
        getWorkflowInstance().setValue("TauxHoraire", null);
        getWorkflowInstance().setValue("SalaireDeBase", null);
        getWorkflowInstance().setValue("SalaireBrutDH", null);
        getWorkflowInstance().setValue("SalaireNETDH", null);
        //getWorkflowInstance().setValue("Matricule", null);

        //document.setValue("Identifiant", null);
    }

    private void onGroupeChange() {
        ArrayList<IGroup> groupes = null;
        if (getWorkflowInstance().getValue("Groupes") != null && ((ArrayList<IGroup>) getWorkflowInstance().getValue("Groupes")).size() > 0) {
            groupes = (ArrayList<IGroup>) getWorkflowInstance().getValue("Groupes");
        }

        boolean isSalarieRH = false;
        boolean isSalarieManager = false;
        if (groupes != null) {
            for (IGroup groupe : groupes) {
                if (groupe.getName().equals("RH")) {
                    isSalarieRH = true;
                } else if (groupe.getName().equals("Manager")) {
                    isSalarieManager = true;
                }
            }
        }
        ArrayList<IGroup> groups = new ArrayList<>();
        if (isSalarieRH) {
            try {
                IGroup organizationRhGroup = getDirectoryModule().getGroup(getWorkflowModule().getSysadminContext(), (IOrganization) getWorkflowInstance().getValue("Organisation"), ((IOrganization) getWorkflowInstance().getValue("Organisation")).getName() + "RH");
                groups.add(organizationRhGroup);
//				document.setValue("SecretGroupes", Arrays.asList(organizationGroup));
            } catch (DirectoryModuleException e) {
                e.printStackTrace();
            }
        }
        if (isSalarieManager) {
            try {
                IGroup organizationManagerGroup = getDirectoryModule().getGroup(getWorkflowModule().getSysadminContext(), (IOrganization) getWorkflowInstance().getValue("Organisation"), ((IOrganization) getWorkflowInstance().getValue("Organisation")).getName() + "Manager");
                groups.add(organizationManagerGroup);
//				document.setValue("SecretGroupes", Arrays.asList(organizationGroup));
            } catch (DirectoryModuleException e) {
                e.printStackTrace();
            }
        }
        getWorkflowInstance().setValue("SecretGroupes", groups);
    }

    private void setOrganization() {
        IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
        getWorkflowInstance().setValue("Organisation", societe != null ? societe.getValue("Organisation") : null);

    }


    @Override
    public boolean onBeforeSave() {
        if (getWorkflowInstance().getValue("MotDePasse") != null && !getWorkflowInstance().getValue("MotDePasse").equals("")) {
            if (getWorkflowInstance().getValue("Salarie")== null && CheckIfExists() )  {
                getResourceController().alert("L’identifiant saisi est déjà utilisé. Merci de saisir un identifiant différent");
                return false;

            }
            if (!getWorkflowInstance().getValue("MotDePasse").equals(getWorkflowInstance().getValue("ConfirmerMotDePasse"))) {
                getResourceController().alert("Merci de vérifier le mot de passe");
                return false;
            } else {
                try {
                    PasswordHelper.checkPasswordStrength((String) getWorkflowInstance().getValue("MotDePasse"), "");
                } catch (InvalidPasswordException e) {
                    getResourceController().alert("La valeur saisie doit comporter :\n" +
                            "au moins 8 caractères\n" +
                            "au moins 1 caractère en majuscule\n" +
                            "au moins 1 chiffre\n" +
                            "au moins 1 caractère non alphanumérique (par exemple $ # %)");
                    e.printStackTrace();
                    return false;
                }
            }
        }
       /* if (typeContrat != null && !getWorkflowInstance().getValue("ContractType").equals(typeContrat)) {
            duplicateSalarie();
            return false;
        }*/
        if (getWorkflowInstance().getValue("Salarie") != null) {
            updateSalarie();
        } else {
            insertSalarie(getWorkflowInstance());
        }

        Collection<IStorageResource> userLiensParente = getUserLienParente();
        int nbrEnfant = 0;
        for (IStorageResource lienParente : userLiensParente) {
            if (lienParente.getValue("LienParente").equals("Enfant")) {
                nbrEnfant++;
            }
        }

        getWorkflowInstance().setValue("MotDePasse", null);
        getWorkflowInstance().setValue("ConfirmerMotDePasse", null);
       //getWorkflowInstance().setValue("NombreEnfants", nbrEnfant);

        saveHistorique();
        HistoriseFonction();
        HistoriseContrat();


        return super.onBeforeSave();
    }

    private void HistoriseFonction(){
        IStorageResource currentFonction = (IStorageResource) getWorkflowInstance().getValue("Fonction");
        if(oldFonction !=null){
            if(currentFonction!=null && !oldFonction.getId().toString().equals(currentFonction.getId().toString()) ){
                // Historise
                if(getWorkflowInstance().getValue("Salarie")!=null){
                    createHistoriqueFonction((IUser) getWorkflowInstance().getValue("Salarie"),oldFonction);

                }

            }else if(currentFonction==null){
                // Historise
                if(getWorkflowInstance().getValue("Salarie")!=null){
                    createHistoriqueFonction((IUser) getWorkflowInstance().getValue("Salarie"),oldFonction);

                }
            }
        }
        oldFonction = currentFonction;
    }


    private void HistoriseContrat(){
        String currentContract = getWorkflowInstance().getValue("ContractType") != null ? (String) getWorkflowInstance().getValue("ContractType") : "";
        Date currentDateSortie = (Date) getWorkflowInstance().getValue("DateDeSortie");
        Boolean currentIsActif = (Boolean) getWorkflowInstance().getValue("Actif");

        if ((!currentContract.equals(oldContrat) && !oldContrat.equals("")) ||
                (currentDateSortie != null && oldDateSortie == null) ||
                (currentDateSortie != null && oldDateSortie != null && !oldDateSortie.equals(currentDateSortie)) ||
                (currentIsActif == false && (oldIsActif == null || !currentIsActif.equals(oldIsActif)))) {
            // Historize
            if (getWorkflowInstance().getValue("Salarie") != null) {
                createHistoriqueContrat(oldContrat);
                oldContrat = currentContract;
                oldDateSortie = currentDateSortie;
                oldIsActif = currentIsActif;
            }

        }


    }

    private void createHistoriqueFonction(IUser salarie , IStorageResource oldFonction){
        try{
            String pattern = "dd-MM-yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL",4, project);
            IResourceDefinition historiqueFonctionDefinition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"HistoriqueDesPostesParCollaborateur");
            IStorageResource historiqueSalarie = getLastHistoriqueSalarie(historiqueFonctionDefinition,salarie);
            if(historiqueSalarie==null){
                // de null to new Date
                IStorageResource historique = getWorkflowModule().createStorageResource(sysContext,historiqueFonctionDefinition,"");
                historique.setValue("Societe",getWorkflowInstance().getValue("Societe"));
                historique.setValue("Collaborateur",getFicheCollaborateurByReference((String) getWorkflowInstance().getValue("sys_Reference")));
                historique.setValue("Salarie",salarie);
                historique.setValue("Fonction",oldFonction);
                historique.setValue("DateDebutPoste",null);
                historique.setValue("DateFinPoste",new Date());
                historique.setValue("sys_Title","du "+"au "+simpleDateFormat.format( new Date()));
                historique.save(sysContext);
            }else{
                // de historique date fin to new Date
                Date lastDateFin = (Date) historiqueSalarie.getValue("DateFinPoste");
                IStorageResource historique = getWorkflowModule().createStorageResource(sysContext,historiqueFonctionDefinition,"");
                historique.setValue("Societe",getWorkflowInstance().getValue("Societe"));
                historique.setValue("Collaborateur",getFicheCollaborateurByReference((String) getWorkflowInstance().getValue("sys_Reference")));
                historique.setValue("Salarie",salarie);
                historique.setValue("Fonction",oldFonction);
                historique.setValue("DateDebutPoste",lastDateFin);
                historique.setValue("DateFinPoste",new Date());
                historique.setValue("sys_Title","du "+simpleDateFormat.format(lastDateFin)+" au "+simpleDateFormat.format( new Date()));
                historique.save(sysContext);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void createHistoriqueContrat(String oldContrat){
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL",4, project);
            IResourceDefinition historiqueContratDefinition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"HistoriqueDesContratsCollaborateurs");
            IStorageResource historique = getWorkflowModule().createStorageResource(sysContext,historiqueContratDefinition,"");
            historique.setValue("Collaborateur",getFicheCollaborateurByReference((String) getWorkflowInstance().getValue("sys_Reference")));
            historique.setValue("Matricule",getWorkflowInstance().getValue("Matricule"));
            historique.setValue("Contrat",oldContrat);
            historique.setValue("DateDEmbaucheSociete",getWorkflowInstance().getValue("DateDEmbauche"));
            historique.setValue("DateSortie",new Date());
            historique.setValue("Duree",dateHelper.getDurationFromADateToNow((Date)getWorkflowInstance().getValue("DateDEmbauche")));
            historique.save(sysContext);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private IStorageResource getFicheCollaborateurByReference(String reference){
        IStorageResource fiche = null;
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL",4, project);
            IResourceDefinition ficheCollaborateurDefinition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"FicheCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("sys_Reference",reference);
            ArrayList<IStorageResource> fiches = (ArrayList<IStorageResource>) controller.evaluate(ficheCollaborateurDefinition);
            if(fiches!=null && !fiches.isEmpty()){
                fiche = fiches.iterator().next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return fiche;
    }

    private IStorageResource getLastHistoriqueSalarie(IResourceDefinition historiqueFonctionDefinition, IUser salarie){
        try
        {
            IViewController controller = getWorkflowModule().getViewController(getWorkflowModule().getSysadminContext(),IResource.class);
            controller.setOrderBy("sys_CreationDate",Date.class,false);
            controller.addEqualsConstraint("Salarie",salarie);
            ArrayList<IStorageResource> historiques = (ArrayList<IStorageResource>) controller.evaluate(historiqueFonctionDefinition);
            if(historiques==null || historiques.isEmpty()){
                return null;
            }else{
                return historiques.get(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void saveHistorique() {
        IStorageResource ficheCollaborateur = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("Salarie", getWorkflowInstance().getValue("Salarie"));
            if(!controller.evaluate(definition).isEmpty()) {
                ficheCollaborateur = (IStorageResource) controller.evaluate(definition).iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(ficheCollaborateur != null){
            Collection<IProperty> properties = (Collection<IProperty>) getWorkflowInstance().getDefinition().getProperties();

            IStorageResource historique = null;
            StringBuilder champModifier = new StringBuilder();
            for (IProperty prop : properties) {
                if (prop.getDescription().equals("ignore")) continue;
                Object oldValue = oldValues.get(prop.getName());
                Object newValue = getWorkflowInstance().getValue(prop.getName());
                if ((oldValue == null ^ newValue == null) || (oldValue != null && !customCompare(oldValue, newValue))) {

                    IContext context = getWorkflowModule().getSysadminContext();
                    IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                    IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
                    ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
                    IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Historique");
                    if (historique == null) {
                        historique = getWorkflowModule().createStorageResource(context, definition, "");
                    }
                    historique.setValue("DateDeModifier", new Date());
                    historique.setValue("Auteur", getWorkflowModule().getLoggedOnUser());
                    historique.setValue("FicheCollaborateur", ficheCollaborateur);
                    historique.save(getWorkflowModule().getSysadminContext());

                    IStorageResource historiqueDetail = null;
                    // context = getWorkflowModule().getSysadminContext();
                    //organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                    //project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
                    //catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
                    definition = getWorkflowModule().getResourceDefinition(context, catalog, "HistoriqueDetail");
                    historiqueDetail = getWorkflowModule().createStorageResource(context, definition, "");
                    historiqueDetail.setValue("Date", new Date());
                    if (oldValue instanceof IStorageResource) {
                        historiqueDetail.setValue("AncianeValue", ((IStorageResource) oldValue).getValue("sys_Title"));
                    } else {
                        if (oldValue != null) {
                            historiqueDetail.setValue("AncianeValue", oldValue.toString());

                        }

                    }
                    historiqueDetail.setValue("Champ", prop.getLabel());
                    historiqueDetail.setValue("Historique", historique);
                    if (newValue instanceof IStorageResource) {
                        historiqueDetail.setValue("NouvelleValue", ((IStorageResource) newValue).getValue("sys_Title"));
                    } else {
                        if (newValue != null) {
                            historiqueDetail.setValue("NouvelleValue", newValue.toString());

                        }

                    }
                    historiqueDetail.setValue("FicheCollaborateur", ficheCollaborateur);
                    historiqueDetail.save(getWorkflowModule().getSysadminContext());
                    champModifier.append((champModifier.length() == 0) ? prop.getLabel() : ", " + prop.getLabel());

                    historique.setValue("LesChampsModifies", champModifier.toString());
                    historique.save(getWorkflowModule().getSysadminContext());


                }
            }
        }

        } catch (Exception e) {
            e.printStackTrace();
        }
        oldValues = new HashMap<>();
        for (IProperty prop : getWorkflowInstance().getDefinition().getProperties()) {
            oldValues.put(prop.getName(), getWorkflowInstance().getValue(prop.getName()));
        }

    }

    boolean customCompare(Object obj1, Object obj2){
        if(obj1 instanceof Number && obj2 instanceof Number){
            return ((Number)obj1).doubleValue() == ((Number)obj2).doubleValue();
            //((Number)obj1).doubleValue() == ((Number)obj2).doubleValue();
        }
        return Objects.equals(obj1, obj2);
    }

    private void duplicateSalarie() {
        try {
            IStorageResource newCollaborateur = getWorkflowModule().createStorageResource(getWorkflowModule().getSysadminContext(), getFicheCollaborateurDefinition(), "");
            copyCollaborateur(newCollaborateur);
            insertSalarie(newCollaborateur);
            newCollaborateur.save(getWorkflowModule().getSysadminContext());
            getWorkflowModule().commitTransaction();

            IStorageResource currentCollab = getCollaborateurByMatricule(getWorkflowInstance().getName());
            currentCollab.setValue("Actif", false);
            FicheSalarieDossierAdministratif.enableDisableUser(currentCollab, false, getWorkflowModule());
            currentCollab.save(getWorkflowModule().getSysadminContext());
			/*String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort())
					.concat(Turbine.getContextPath().concat("/easysite/workplace/salarie/edit-document/").concat(newCollaborateur.getId()+""));    
			
			ExternalScreen externalScreen = new ExternalScreen(lien.toString());
			Navigator.getNavigator().setCurrentScreen(externalScreen);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IStorageResource getCollaborateurByMatricule(String matricule) {
        IStorageResource collaborateur = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = getFicheCollaborateurDefinition();
            controller.addEqualsConstraint("sys_Reference", matricule);
            if (definition != null) {
                Collection<IStorageResource> holder = controller.evaluate(definition);
                if (holder.size() > 0) {
                    collaborateur = holder.iterator().next();
                }
                holder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collaborateur;

    }

    private void copyCollaborateur(IStorageResource newCollaborateur) {
        // Hidden fields
        newCollaborateur.setValue("Salarie", null);
        newCollaborateur.setValue("Matricule", null);
        newCollaborateur.setValue("Groupes", getWorkflowInstance().getValue("Groupes"));
        newCollaborateur.setValue("SecretGroupes", getWorkflowInstance().getValue("SecretGroupes"));
        newCollaborateur.setValue("NomPrenom", getWorkflowInstance().getValue("NomPrenom"));
        newCollaborateur.setValue("Societe", getWorkflowInstance().getValue("Societe"));
        newCollaborateur.setValue("Organisation", getWorkflowInstance().getValue("Organisation"));

        newCollaborateur.setValue("Title", getWorkflowInstance().getValue("Title"));
        newCollaborateur.setValue("FirstName", getWorkflowInstance().getValue("FirstName"));
        newCollaborateur.setValue("LastName", getWorkflowInstance().getValue("LastName"));
        newCollaborateur.setValue("MobilePhoneNumber", getWorkflowInstance().getValue("MobilePhoneNumber"));
        newCollaborateur.setValue("CodeMobile", getWorkflowInstance().getValue("CodeMobile"));
        newCollaborateur.setValue("Extension", getWorkflowInstance().getValue("Extension"));
        newCollaborateur.setValue("Birthday", getWorkflowInstance().getValue("Birthday"));
        newCollaborateur.setValue("CIN", getWorkflowInstance().getValue("CIN"));
        newCollaborateur.setValue("NCNSS", getWorkflowInstance().getValue("NCNSS"));
        newCollaborateur.setValue("Address1", getWorkflowInstance().getValue("Address1"));
        newCollaborateur.setValue("Address2", getWorkflowInstance().getValue("Address2"));

        newCollaborateur.setValue("EtatCivil", getWorkflowInstance().getValue("EtatCivil"));
        newCollaborateur.setValue("NombreEnfants", getWorkflowInstance().getValue("NombreEnfants"));
        File file = null;
        ArrayList<IAttachment> tmp = (ArrayList<IAttachment>) getWorkflowInstance().getValue("Avatar");
        IAttachment ficheAvatar = null;
        if (tmp != null && tmp.size() > 0) {
            ficheAvatar = tmp.iterator().next();
        }
        if (ficheAvatar != null) {
            try {
                file = new File("c://TEST//" + ficheAvatar.getName());
                FileUtils.writeByteArrayToFile(file, ficheAvatar.getContent());
                IAttachment userAvatar = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
                newCollaborateur.setValue("Avatar", userAvatar);
                file.delete();
                file.deleteOnExit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            newCollaborateur.setValue("Avatar", null);
        }

        // Informations login
        newCollaborateur.setValue("Email", getWorkflowInstance().getValue("Email"));
        newCollaborateur.setValue("Identifiant", getWorkflowInstance().getValue("Identifiant"));
        newCollaborateur.setValue("MotDePasse", getWorkflowInstance().getValue("MotDePasse"));
        newCollaborateur.setValue("ConfirmerMotDePasse", getWorkflowInstance().getValue("ConfirmerMotDePasse"));

        // Informations bancaire
        newCollaborateur.setValue("Banque", getWorkflowInstance().getValue("Banque"));
        newCollaborateur.setValue("NCompteBancaire", getWorkflowInstance().getValue("NCompteBancaire"));
        newCollaborateur.setValue("AgenceBancaire", getWorkflowInstance().getValue("AgenceBancaire"));

        // Affectation
        newCollaborateur.setValue("DateDEmbauche", getWorkflowInstance().getValue("DateDEmbauche"));
        newCollaborateur.setValue("DateDEmbaucheGroupe", getWorkflowInstance().getValue("DateDEmbaucheGroupe"));

        newCollaborateur.setValue("ContractType", getWorkflowInstance().getValue("ContractType"));
        //newCollaborateur.setValue("Societe", document.getValue("Societe"));
        newCollaborateur.setValue("Societe", getWorkflowInstance().getValue("Societe"));
        newCollaborateur.setValue("Groupes", getWorkflowInstance().getValue("Groupes"));
        newCollaborateur.setValue("SecretGroupes", getWorkflowInstance().getValue("SecretGroupes"));
        newCollaborateur.setValue("Direction", getWorkflowInstance().getValue("Direction"));
        newCollaborateur.setValue("Departement", getWorkflowInstance().getValue("Departement"));
        newCollaborateur.setValue("Service", getWorkflowInstance().getValue("Service"));
        newCollaborateur.setValue("Activite", getWorkflowInstance().getValue("Activite"));
        newCollaborateur.setValue("Pole", getWorkflowInstance().getValue("Pole"));
        newCollaborateur.setValue("Metier", getWorkflowInstance().getValue("Metier"));
        newCollaborateur.setValue("Division", getWorkflowInstance().getValue("Division"));

        newCollaborateur.setValue("Fonction", getWorkflowInstance().getValue("Fonction"));
        newCollaborateur.setValue("Grade", getWorkflowInstance().getValue("Grade"));
        newCollaborateur.setValue("Categorie", getWorkflowInstance().getValue("Categorie"));
        newCollaborateur.setValue("HierarchicalManager", getWorkflowInstance().getValue("HierarchicalManager"));
//		newCollaborateur.setValue("Exit", document.getValue("Exit"));
        newCollaborateur.setValue("DateDeSortie", getWorkflowInstance().getValue("DateDeSortie"));
        newCollaborateur.setValue("MotifSortie", getWorkflowInstance().getValue("MotifSortie"));
        newCollaborateur.setValue("Domiciliation", getWorkflowInstance().getValue("Domiciliation"));
        newCollaborateur.setValue("MainLever", getWorkflowInstance().getValue("MainLever"));

        // Données salariales
        newCollaborateur.setValue("TypeDeSalaire", getWorkflowInstance().getValue("TypeDeSalaire"));
        newCollaborateur.setValue("TauxHoraire", getWorkflowInstance().getValue("TauxHoraire"));
        newCollaborateur.setValue("SalaireDeBase", getWorkflowInstance().getValue("SalaireDeBase"));
        newCollaborateur.setValue("SalaireBrutDH", getWorkflowInstance().getValue("SalaireBrutDH"));
        newCollaborateur.setValue("SalaireNETDH", getWorkflowInstance().getValue("SalaireNETDH"));
        newCollaborateur.setValue("NumeroFax", getWorkflowInstance().getValue("NumeroFax"));
        newCollaborateur.setValue("NumeroDAgence", getWorkflowInstance().getValue("NumeroDAgence"));

        // Indemnités

        // Mutuelle
        newCollaborateur.setValue("NomDeLaMutuelle", getWorkflowInstance().getValue("NomDeLaMutuelle"));
        newCollaborateur.setValue("TauxDeLaMutuelle", getWorkflowInstance().getValue("TauxDeLaMutuelle"));

        // Retraite
        newCollaborateur.setValue("NomRetraite", getWorkflowInstance().getValue("NomRetraite"));
        newCollaborateur.setValue("TauxDeLaRetraire", getWorkflowInstance().getValue("TauxDeLaRetraire"));
        newCollaborateur.setValue("NumeroAttribue", getWorkflowInstance().getValue("NumeroAttribue"));

        // Retraite complémentaire
        newCollaborateur.setValue("NomDeLaRetraiteComplementaire", getWorkflowInstance().getValue("NomDeLaRetraiteComplementaire"));
        newCollaborateur.setValue("TauxDeLaRetraiteComplementaire", getWorkflowInstance().getValue("TauxDeLaRetraiteComplementaire"));
        newCollaborateur.setValue("MontantEpargneRetraite", getWorkflowInstance().getValue("MontantEpargneRetraite"));


        // Droit au congés
        newCollaborateur.setValue("DroitMensuelle", getWorkflowInstance().getValue("DroitMensuelle"));
        newCollaborateur.setValue("AbsenceAnneeEnCours", getWorkflowInstance().getValue("AbsenceAnneeEnCours"));
        newCollaborateur.setValue("AbsenceAnterieure", getWorkflowInstance().getValue("AbsenceAnterieure"));
        newCollaborateur.setValue("SoldeAnneeEnCours", getWorkflowInstance().getValue("SoldeAnneeEnCours"));
        newCollaborateur.setValue("SoldeAnterieur", getWorkflowInstance().getValue("SoldeAnterieur"));
        newCollaborateur.setValue("SoldeConges", getWorkflowInstance().getValue("SoldeConges"));
        newCollaborateur.setValue("CongesPayesEnCoursDeValidation", getWorkflowInstance().getValue("CongesPayesEnCoursDeValidation"));
        newCollaborateur.setValue("CongesPayesEnCoursDeConsommation", getWorkflowInstance().getValue("CongesPayesEnCoursDeConsommation"));
        newCollaborateur.setValue("CongesPayesEnCoursDeTraitement", getWorkflowInstance().getValue("CongesPayesEnCoursDeTraitement"));

        newCollaborateur.setValue("CongesSpeciauxEnCoursDeValidation", getWorkflowInstance().getValue("CongesSpeciauxEnCoursDeValidation"));
        newCollaborateur.setValue("CongesSpeciauxEnCoursDeConsommation", getWorkflowInstance().getValue("CongesSpeciauxEnCoursDeConsommation"));
        newCollaborateur.setValue("CongesSpeciauxEnCoursDeTraitement", getWorkflowInstance().getValue("CongesSpeciauxEnCoursDeTraitement"));

        newCollaborateur.setValue("CongesMaladieEnCoursDeValidation", getWorkflowInstance().getValue("CongesMaladieEnCoursDeValidation"));
        newCollaborateur.setValue("CongesMaladieEnCoursDeConsommation", getWorkflowInstance().getValue("CongesMaladieEnCoursDeConsommation"));
        newCollaborateur.setValue("CongesMaladieEnCoursDeTraitement", getWorkflowInstance().getValue("CongesMaladieEnCoursDeTraitement"));

        newCollaborateur.setValue("CongesSansSoldeEnCoursDeValidation", getWorkflowInstance().getValue("CongesSansSoldeEnCoursDeValidation"));
        newCollaborateur.setValue("CongesSansSoldeEnCoursDeConsommation", getWorkflowInstance().getValue("CongesSansSoldeEnCoursDeConsommation"));
        newCollaborateur.setValue("CongesSansSoldeEnCoursDeTraitement", getWorkflowInstance().getValue("CongesSansSoldeEnCoursDeTraitement"));

        newCollaborateur.setValue("JourSEnCoursDeValidation", getWorkflowInstance().getValue("JourSEnCoursDeValidation"));
        newCollaborateur.setValue("JoursEnCoursConsommation", getWorkflowInstance().getValue("JourSEnCoursDeConsommation"));
        newCollaborateur.setValue("JourSEnCoursDeTraitement", getWorkflowInstance().getValue("JourSEnCoursDeTraitement"));

        newCollaborateur.setValue("CongesPayesAnneeEnCours", getWorkflowInstance().getValue("CongesPayesAnneeEnCours"));
        newCollaborateur.setValue("CongesPayesN1", getWorkflowInstance().getValue("CongesPayesN1"));
        newCollaborateur.setValue("CongesPayesPris", getWorkflowInstance().getValue("CongesPayesPris"));
        newCollaborateur.setValue("CongesSpeciauxAnneeEnCours", getWorkflowInstance().getValue("CongesSpeciauxAnneeEnCours"));
        newCollaborateur.setValue("CongesSpeciauxN1", getWorkflowInstance().getValue("CongesSpeciauxN1"));
        newCollaborateur.setValue("CongesSpeciauxPris", getWorkflowInstance().getValue("CongesSpeciauxPris"));
        newCollaborateur.setValue("CongesMaladieAnneeEnCours", getWorkflowInstance().getValue("CongesMaladieAnneeEnCours"));
        newCollaborateur.setValue("CongesMaladieN1", getWorkflowInstance().getValue("CongesMaladieN1"));
        newCollaborateur.setValue("CongesMaladiePris", getWorkflowInstance().getValue("CongesMaladiePris"));
        newCollaborateur.setValue("CongesSansSoldeAnneeEnCours", getWorkflowInstance().getValue("CongesSansSoldeAnneeEnCours"));
        newCollaborateur.setValue("CongesSansSoldeN1", getWorkflowInstance().getValue("CongesSansSoldeN1"));
        newCollaborateur.setValue("CongesSansSoldePris", getWorkflowInstance().getValue("CongesSansSoldePris"));
        newCollaborateur.setValue("TotalJoursPris", getWorkflowInstance().getValue("TotalJoursPris"));
    }

    private IResourceDefinition getFicheCollaborateurDefinition() {
        IResourceDefinition definition = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return definition;
    }

    private void insertSalarie(IResource collaborateurToCopyFrom) {
        IContext context = getWorkflowModule().getSysadminContext();
        IUser newUser = null;
        try {
            //getDirectoryModule().beginTransaction();//(IOrganization)collaborateurToCopyFrom.getValue("Organisation")
            //newUser = getDirectoryModule().createUser(context, (String)collaborateurToCopyFrom.getValue("Identifiant"),(String)collaborateurToCopyFrom.getValue("MotDePasse") ,(IOrganization)document.getValue("Organisation"));
            newUser = getDirectoryModule().createUser(context, (String) collaborateurToCopyFrom.getValue("Identifiant"), (String) collaborateurToCopyFrom.getValue("MotDePasse"), (IOrganization) collaborateurToCopyFrom.getValue("Organisation"));
            newUser = setUserFields(newUser, collaborateurToCopyFrom);
            newUser.save(context);
            //getDirectoryModule().commitTransaction();
            collaborateurToCopyFrom.setValue("Salarie", newUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSalarie() {
        IContext context = getWorkflowModule().getSysadminContext();
        IUser user = (IUser) getWorkflowInstance().getValue("Salarie");
        user.setLogin((String) getWorkflowInstance().getValue("Identifiant"));
        user = setUserFields(user, getWorkflowInstance());
        getWorkflowInstance().setValue("Salarie", user);
        user.save(context);
    }

    private IUser setUserFields(IUser user, IResource collaborateurToCopyFrom) {
// 		User annuaire built in fields

//		user.setOrganization((IOrganization)collaborateurToCopyFrom.getValue("Organisation"));
//		
//		user.setSex(collaborateurToCopyFrom.getValue("Sex") != null ? (String)collaborateurToCopyFrom.getValue("Sex") : null);

//		File file = null;
//		ArrayList<IAttachment> tmp = (ArrayList<IAttachment>)collaborateurToCopyFrom.getValue("Avatar");
//		IAttachment ficheAvatar = null;
//		if(tmp != null && tmp.size() > 0){
//			ficheAvatar = tmp.iterator().next();
//		}
//		if(ficheAvatar != null){
//			try 
//			{
//				file = new File("c://TEST//" + ficheAvatar.getName());
//				FileUtils.writeByteArrayToFile(file, ficheAvatar.getContent());
//				IAttachment userAvatar = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
//				user.setAvatar(userAvatar);
//				file.delete();
//				file.deleteOnExit();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} else {
//			user.setAvatar(null);
//		}

        // user Extended fields
        user.setOrganization((IOrganization) collaborateurToCopyFrom.getValue("Organisation"));

        // Informations personnelles

        // if they want the matricule to be auto generated
//		user.getExtendedAttributes().setValue("Matricule", collaborateurToCopyFrom.getValue("sys_Reference"));
        // if they want the matricule to be a normal text field
        user.getExtendedAttributes().setValue("ProfilEVAL",collaborateurToCopyFrom.getValue("ProfilEvaluation"));
        user.getExtendedAttributes().setValue("Evaluateur",collaborateurToCopyFrom.getValue("Evaluateur"));
        user.getExtendedAttributes().setValue("Suppleant",collaborateurToCopyFrom.getValue("Suppleant"));
        user.getExtendedAttributes().setValue("ResponsableN2",collaborateurToCopyFrom.getValue("ResponsableN2"));
        user.getExtendedAttributes().setValue("Maladie",collaborateurToCopyFrom.getValue("Maladie"));

        user.getExtendedAttributes().setValue("Matricule", collaborateurToCopyFrom.getValue("Matricule"));
        user.getExtendedAttributes().setValue("Salarie", collaborateurToCopyFrom.getValue("Salarie"));
        user.setTitle(collaborateurToCopyFrom.getValue("Title") != null ? (String) collaborateurToCopyFrom.getValue("Title") : null);
        user.setFirstName(collaborateurToCopyFrom.getValue("FirstName") != null ? (String) collaborateurToCopyFrom.getValue("FirstName") : null);
        user.setLastName(collaborateurToCopyFrom.getValue("LastName") != null ? (String) collaborateurToCopyFrom.getValue("LastName") : null);
        user.setMobilePhoneNumber(collaborateurToCopyFrom.getValue("MobilePhoneNumber") != null ? (String) collaborateurToCopyFrom.getValue("MobilePhoneNumber") : null);
        user.getExtendedAttributes().setValue("Metier", collaborateurToCopyFrom.getValue("Metier"));
        user.getExtendedAttributes().setValue("Division", collaborateurToCopyFrom.getValue("Division"));
        user.getExtendedAttributes().setValue("CodeMobile", collaborateurToCopyFrom.getValue("CodeMobile"));
        user.getExtendedAttributes().setValue("Extension", collaborateurToCopyFrom.getValue("Extension"));

        user.setBirthday(collaborateurToCopyFrom.getValue("Birthday") != null ? (Date) collaborateurToCopyFrom.getValue("Birthday") : null);
        user.getExtendedAttributes().setValue("CIN", collaborateurToCopyFrom.getValue("CIN"));
        user.getExtendedAttributes().setValue("NCNSS", collaborateurToCopyFrom.getValue("NCNSS"));
        user.setAddress1(collaborateurToCopyFrom.getValue("Address1") != null ? (String) collaborateurToCopyFrom.getValue("Address1") : null);
        user.setAddress2(collaborateurToCopyFrom.getValue("Address2") != null ? (String) collaborateurToCopyFrom.getValue("Address2") : null);

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
                IAttachment userAvatar = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
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
        user.getExtendedAttributes().setValue("NumeroFax", collaborateurToCopyFrom.getValue("NumeroFax"));
        user.getExtendedAttributes().setValue("NumeroDAgence", collaborateurToCopyFrom.getValue("NumeroDAgence"));
        user.getExtendedAttributes().setValue("Domiciliation", collaborateurToCopyFrom.getValue("Domiciliation"));
        user.getExtendedAttributes().setValue("MainLever", collaborateurToCopyFrom.getValue("MainLever"));

        boolean isActif = getWorkflowInstance().getValue("Actif")!=null?(boolean) getWorkflowInstance().getValue("Actif"):true;
        Date dateSortie = (Date) getWorkflowInstance().getValue("DateDeSortie");
        if(!isActif || dateSortie!=null){
            getWorkflowInstance().setValue("Actif",false);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
            user.disable();
        }
        // Affectation
        user.getExtendedAttributes().setValue("DateDEmbauche", collaborateurToCopyFrom.getValue("DateDEmbauche"));
        user.getExtendedAttributes().setValue("DateDEmbaucheGroupe", collaborateurToCopyFrom.getValue("DateDEmbaucheGroupe"));

        //user.setContractType(collaborateurToCopyFrom.getValue("ContractType") != null ? (String)collaborateurToCopyFrom.getValue("ContractType") : null);
        //user.getExtendedAttributes().setValue("Societe", collaborateurToCopyFrom.getValue("Societe"));
        user.getExtendedAttributes().setValue("Societe", collaborateurToCopyFrom.getValue("Societe"));
        user.getExtendedAttributes().setValue("ContractType", collaborateurToCopyFrom.getValue("ContractType"));
        user.getExtendedAttributes().setValue("Groupes", collaborateurToCopyFrom.getValue("Groupes"));
        user.getExtendedAttributes().setValue("SecretGroupes", collaborateurToCopyFrom.getValue("SecretGroupes"));
        user.getExtendedAttributes().setValue("Direction", collaborateurToCopyFrom.getValue("Direction"));
        user.getExtendedAttributes().setValue("Departement", collaborateurToCopyFrom.getValue("Departement"));
        user.getExtendedAttributes().setValue("Fonction", collaborateurToCopyFrom.getValue("Fonction"));
        user.getExtendedAttributes().setValue("Grade", collaborateurToCopyFrom.getValue("Grade"));

        user.getExtendedAttributes().setValue("Service", collaborateurToCopyFrom.getValue("Service"));
        user.getExtendedAttributes().setValue("Activite", collaborateurToCopyFrom.getValue("Activite"));
        user.getExtendedAttributes().setValue("Pole", collaborateurToCopyFrom.getValue("Pole"));
        user.getExtendedAttributes().setValue("Categorie", collaborateurToCopyFrom.getValue("Categorie"));
        user.setHierarchicalManager(collaborateurToCopyFrom.getValue("HierarchicalManager") != null ? (IUser) collaborateurToCopyFrom.getValue("HierarchicalManager") : null);
        //	user.setExit(collaborateurToCopyFrom.getValue("Exit") != null ? (Date)collaborateurToCopyFrom.getValue("Exit") : null);
        user.setExit(collaborateurToCopyFrom.getValue("DateDeSortie") != null ? (Date) collaborateurToCopyFrom.getValue("DateDeSortie") : null);
        user.getExtendedAttributes().setValue("MotifSortie", collaborateurToCopyFrom.getValue("MotifSortie"));

        //Suivi Médical
        user.getExtendedAttributes().setValue("DateVisiteMedicaleDEmbauche",collaborateurToCopyFrom.getValue("DateVisiteMedicaleDEmbauche"));
        user.getExtendedAttributes().setValue("DateDerniereVisiteMedicale",collaborateurToCopyFrom.getValue("DateDerniereVisiteMedicale"));
        user.getExtendedAttributes().setValue("DateRadioThorax",collaborateurToCopyFrom.getValue("DateRadioThorax"));
        user.getExtendedAttributes().setValue("AttestationAptitudePhysique",collaborateurToCopyFrom.getValue("AttestationAptitudePhysique"));

        // Données salariales
        user.getExtendedAttributes().setValue("TypeDeSalaire", collaborateurToCopyFrom.getValue("TypeDeSalaire"));
        user.getExtendedAttributes().setValue("TauxHoraire", collaborateurToCopyFrom.getValue("TauxHoraire"));
        user.getExtendedAttributes().setValue("SalaireDeBase", collaborateurToCopyFrom.getValue("SalaireDeBase"));
        user.getExtendedAttributes().setValue("SalaireBrut", collaborateurToCopyFrom.getValue("SalaireBrutDH"));
        user.getExtendedAttributes().setValue("Salaire", collaborateurToCopyFrom.getValue("SalaireNETDH"));
        user.getExtendedAttributes().setValue("SalaireBrutImposableDH", collaborateurToCopyFrom.getValue("SalaireBrutImposableDH"));
        user.getExtendedAttributes().setValue("RemunerationDH", collaborateurToCopyFrom.getValue("RemunerationDH"));
        user.getExtendedAttributes().setValue("MontantMensuelDeNoteDeFrais", collaborateurToCopyFrom.getValue("MontantMensuelDeNoteDeFrais"));

        // Indemnités

        // Mutuelle
        user.getExtendedAttributes().setValue("NomDeLaMutuelle", collaborateurToCopyFrom.getValue("NomDeLaMutuelle"));
        user.getExtendedAttributes().setValue("TauxDeLaMutuelle", collaborateurToCopyFrom.getValue("TauxDeLaMutuelle"));

        // Retraite
        user.getExtendedAttributes().setValue("NomRetraite", collaborateurToCopyFrom.getValue("NomRetraite"));
        user.getExtendedAttributes().setValue("TauxDeLaRetraire", collaborateurToCopyFrom.getValue("TauxDeLaRetraire"));
        user.getExtendedAttributes().setValue("NumeroAttribue", collaborateurToCopyFrom.getValue("NumeroAttribue"));
        user.getExtendedAttributes().setValue("NCIMR", collaborateurToCopyFrom.getValue("NCIMR"));

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

        return user;
    }

    private Collection<IStorageResource> getUserLienParente() {
        Collection<IStorageResource> liensParente = Collections.emptyList();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "LienParente");
            controller.addEqualsConstraint("FicheSalarie", getDocument().getResource());
            liensParente = controller.evaluate(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return liensParente;
    }



    ArrayList<IStorageResource> getMissionsCollaborateur(){
        ArrayList<IStorageResource> missions = null;
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL",4, project);
            IResourceDefinition ficheCollaborateurDefinition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"MissionCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("Collaborateur",getFicheCollaborateurByReference( (String)getWorkflowInstance().getValue("sys_Reference")));
            missions = (ArrayList<IStorageResource>) controller.evaluate(ficheCollaborateurDefinition);

        }catch (Exception e){
            e.printStackTrace();
        }
        return missions;
    }

    private void historiseMissionCollaborateur(IStorageResource mission) {
        if(mission==null)return;
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"HistoriqueMissionsCollaborateurs");

                IStorageResource historiqueMission = getWorkflowModule().createStorageResource(context,definition,"");
                historiqueMission.setValue("Collaborateur",mission.getValue("Collaborateur"));
                historiqueMission.setValue("Mission",mission.getValue("sys_Title"));
                historiqueMission.setValue("Details",mission.getValue("Details"));
                historiqueMission.setValue("DateAffectation",mission.getValue("DateAffectation"));
                historiqueMission.setValue("DateFin",new Date());
                historiqueMission.setValue("Duree",getDurationFromTwoDate((Date)mission.getValue("DateAffectation"),new Date()));
                historiqueMission.save(getWorkflowModule().getSysadminContext());
                mission.delete(getWorkflowModule().getSysadminContext());


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    ArrayList<IStorageResource> getMissionsFonction(IStorageResource fonction){
        ArrayList<IStorageResource> missions = null;
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL",4, project);
            IResourceDefinition ficheCollaborateurDefinition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"MissionFonctionPoste");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("Fonction",fonction);
            missions = (ArrayList<IStorageResource>) controller.evaluate(ficheCollaborateurDefinition);

        }catch (Exception e){
            e.printStackTrace();
        }
        return missions;
    }

    private void createMissionForCollaborateurFromMissionFonction(IStorageResource missionFonction,IStorageResource collaborateur){
        if(missionFonction==null || collaborateur==null){
            return;
        }
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL",4, project);
            IResourceDefinition missionsCollaborateurDefinition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"MissionCollaborateur");
            IStorageResource missionCollaborateur = getWorkflowModule().createStorageResource(sysContext,missionsCollaborateurDefinition,"");
            missionCollaborateur.setValue("Collaborateur",collaborateur);
            missionCollaborateur.setValue("DateAffectation",new Date());
            missionCollaborateur.setValue("sys_Title",missionFonction.getValue("sys_Title"));
            missionCollaborateur.setValue("Details",missionFonction.getValue("Details"));
            missionCollaborateur.save(getWorkflowModule().getSysadminContext());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean CheckIfExists() {

        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "FicheCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            controller.addEqualsConstraint("Identifiant",getWorkflowInstance().getValue("Identifiant"));
            if (!controller.evaluate(definition).isEmpty()) {
                return true;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getDurationFromTwoDate(Date start,Date end){
        if(start==null || end==null)return "";
        long difference_In_Milliseconds = ((end.getTime() - start.getTime()));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(difference_In_Milliseconds);
        int years = c.get(Calendar.YEAR) - 1970;
        int months = c.get(Calendar.MONTH);
        int days = c.get(Calendar.DAY_OF_MONTH);
        return years + " ans " + months + " mois " + days + " jours";
    }

    private void setTargetNullOnSourceChange(IProperty property,String source,String target){
        if(property.getName().equals(source)){
            getWorkflowInstance().setValue(target,null);
        }
    }

}

