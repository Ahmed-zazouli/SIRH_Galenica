package com.moovapps.capone.rh.GestionDeCongeAnnuel.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.IProjectModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.moovapps.capone.rh.GestionDeConge.helpers.JoursOuvrableOuvres;
import com.moovapps.capone.rh.GestionDeConge.helpers.WorkingDaysNumberCalculator;
import com.moovapps.capone.rh.cummonHelpers.DateValidator;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;
import com.moovapps.capone.rh.navigation.VueCongeOverlapped;

import java.util.*;


public class CongeAnnuel extends BaseDocumentExtension {


    IWorkflowInstance document = null;
    IUser demandeur = null;
    IStorageResource societe = null;
    // SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY = 6, SATURDAY = 7
    ArrayList<Integer> joursOuvrables = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6, 7));

    // SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY = 6, SATURDAY = 7
    ArrayList<Integer> joursOuvres = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6));
    float SoldeConges = 0;
    IWorkflowModule workflowModule = null;
    IDirectoryModule directoryModule = null;
    IProjectModule projectModule = null;
    IContext sysContext = null;

    boolean overlappedIsChecked = true;

    public CongeAnnuel() {
    }

    public CongeAnnuel(IWorkflowInstance Pdocument, IUser Pdemandeur, IWorkflowModule PworkflowModule, IProjectModule PprojectModule) {
        this.document = Pdocument;
        this.demandeur = Pdemandeur;
        this.workflowModule = PworkflowModule;
        this.projectModule = PprojectModule;
        societe = (IStorageResource) demandeur.getExtendedAttributes().getValue("Societe");
        document.setValue("Societe", societe);
        joursOuvrables = JoursOuvrableOuvres.getJoursOuvrables(societe);
        joursOuvres = JoursOuvrableOuvres.getJoursOuvres(societe);

    }

    @Override
    public boolean onAfterLoad() {
        setVariables();
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("Valide")) {
            overlappedIsChecked = true;
            getTableUsers();
            if (overlappedIsChecked) {
                calculateCongesAnnuel();
            } else {
                getResourceController().alert("Merci de vérifier les congés en conflit avec le congé annuel");
                return false;
            }
        }
        if (action.getName().equals("Planifier")) {
            overlappedIsChecked = true;
            getTableUsers();
            if (!overlappedIsChecked) {
                getResourceController().alert("Merci de vérifier les congés en conflit avec le congé annuel");
                return false;
            }
        }
        return super.onBeforeSubmit(action);
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("DateDeDebut") || property.getName().equals("DateDeFin")) {
            setCalculateConge();
        }
        if (property.getName().equals("Departement2") || property.getName().equals("DateDeDebut") || property.getName().equals("DateDeFin")) {
            document.deleteLinkedResources(document.getLinkedResources("UsersDeDepartement"));
            document.deleteLinkedResources(document.getLinkedResources("DiviserCongeAnnuel"));
            if(document.getValue("Departement2") != null && document.getValue("DateDeDebut") != null && document.getValue("DateDeFin") != null){
                IStorageResource departement = (IStorageResource) document.getValue("Departement2");
                fillDepartementUsers(departement);
            }
        }

        /*if (property.getName().equals("DateDebute") || property.getName().equals("DateFin")) {
            Date dateDebute = (Date) document.getValue("DateDebute");
            Date dateFin = (Date) document.getValue("DateFin");
            if (dateDebute != null && dateFin != null) {
                HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(workflowModule, joursOuvrables, joursOuvres).calculateV2(dateDebute, dateFin, false);
                document.setValue("NombreDeJoursCalculer", calculResult.get("nbrJoursDemande"));
            }
        }*/
        super.onPropertyChanged(property);
    }

    public void setVariables() {
        document = getWorkflowInstance();
        demandeur = getWorkflowInstance().getCreatedBy();
        societe = (IStorageResource) demandeur.getExtendedAttributes().getValue("Societe");
        if (societe != null) {
            document.setValue("Societe", societe);
        }
        joursOuvrables = JoursOuvrableOuvres.getJoursOuvrables(societe);
        joursOuvres = JoursOuvrableOuvres.getJoursOuvres(societe);
        workflowModule = getWorkflowModule();
        projectModule = getProjectModule();

    }

    public void setCalculateConge() {
        Date dateDebutConge = (Date) document.getValue("DateDeDebut");
        Date dateFinConge = (Date) document.getValue("DateDeFin");
        if (dateDebutConge != null && dateFinConge != null) {
            HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(workflowModule, joursOuvrables, joursOuvres).calculateV2(societe,dateDebutConge, dateFinConge, false);
            document.setValue("DateFinReel", (Date) calculResult.get("dateFinReel"));
            document.setValue("NombreDeJoursDemandes", calculResult.get("nbrJoursDemande"));
            document.setValue("TotalAbsence", calculResult.get("totalJoursAbsence"));
            document.setValue("totalJoursFerie", calculResult.get("totalJoursFerieInPeriod"));
        }else{
            document.setValue("DateFinReel", null);
            document.setValue("NombreDeJoursDemandes", null);
            document.setValue("TotalAbsence", null);
            document.setValue("totalJoursFerie", null);
        }
    }

    // calculate the Conge Annuel by selected Users in Department
    public void calculateCongesAnnuel() {
        IStorageResource departement = (IStorageResource) document.getValue("Departement2");
        Date dateDebutConge = (Date) document.getValue("DateDeDebut");
        Date dateFinConge = (Date) document.getValue("DateDeFin");
        if (departement != null && dateDebutConge != null && dateFinConge != null) {
            HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(workflowModule, joursOuvrables, joursOuvres).calculateV2(societe,dateDebutConge, dateFinConge, false);

            ArrayList<ILinkedResource> UsersOfDepartment = getTableUsers();
            for (ILinkedResource user : UsersOfDepartment) {
                Float nbrJoursCongesAnnuel = (Float) user.getValue("NombreDeJoursDemandes");

                demandeur = (IUser) user.getValue("Collaborateur");
                SoldeConges = (Float) demandeur.getExtendedAttributes().getValue("SoldeConges");
                demandeur.getExtendedAttributes().setValue("SoldeConges", SoldeConges - nbrJoursCongesAnnuel);
                //user.setValue("SoldeConges", SoldeConges - nbrJoursCongesAnnuel);
                demandeur.getExtendedAttributes().setValue("CongesPayesPris", ((Number) demandeur.getExtendedAttributes().getValue("CongesPayesPris")).floatValue() + nbrJoursCongesAnnuel);
                demandeur.getExtendedAttributes().setValue("TotalJoursPris", ((Number) demandeur.getExtendedAttributes().getValue("TotalJoursPris")).floatValue() + nbrJoursCongesAnnuel);
                IStorageResource userFiche = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(demandeur, workflowModule, projectModule, document.getCatalog().getProject().getOrganization());
                userFiche.save(workflowModule.getSysadminContext());
                demandeur.save(workflowModule.getSysadminContext());
            }
        }

    }


    // get the Users from referential FicheCollaborateur
    // and set it in table dynamique
    private void fillDepartementUsers(IStorageResource departement) {
        ArrayList<IStorageResource> ficheUser = null;
        try {
            Date dateDebutConge = (Date) document.getValue("DateDeDebut");
            Date dateFinConge = (Date) document.getValue("DateDeFin");
            HashMap<String, Object> calculResult = new HashMap<>();
            if (dateDebutConge != null && dateFinConge != null) {
                calculResult = new WorkingDaysNumberCalculator(workflowModule, joursOuvrables, joursOuvres).calculateV2(societe,dateDebutConge, dateFinConge, false);
            }
            Float nbrJoursCongesAnnuel = (Float) calculResult.get("nbrJoursDemande");
            IContext context = workflowModule.getSysadminContext();
            IViewController controller = workflowModule.getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = workflowModule.getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = workflowModule.getResourceDefinition(context, catalog, "FicheCollaborateur");
//            controller.addEqualsConstraint("Salarie", departement);
            controller.addEqualsConstraint("Departement", departement);
            controller.addEqualsConstraint("Societe", societe);
            ficheUser = (ArrayList<IStorageResource>) controller.evaluate(definition);
            ArrayList<IWorkflowInstance> allDemandeOverlapped = new ArrayList<>();

            for (IStorageResource user : ficheUser) {
                ArrayList<ILinkedResource> TD = (ArrayList<ILinkedResource>) document.getLinkedResources("UsersDeDepartement");

                demandeur = (IUser) user.getValue("Salarie");
                SoldeConges = (Float) user.getValue("SoldeConges");
                String NomPrenom = (String) user.getValue("NomPrenom");
                ILinkedResource row = document.createLinkedResource("UsersDeDepartement");
                row.setValue("NomPrenom", NomPrenom);
                row.setValue("SoldeConge", SoldeConges);
                row.setValue("Collaborateur", demandeur);
                row.setValue("DateDeDebut",dateDebutConge);
                row.setValue("DateDeFin",dateFinConge);
                row.setValue("Statut", "OK");
                Collection<IWorkflowInstance> allDemandes = getAllDemandeurDemandes(demandeur);
                float NbDemiJournee = 0;
                nbrJoursCongesAnnuel = (Float) calculResult.get("nbrJoursDemande");
                for (IWorkflowInstance Demande : allDemandes) {
                    Date demandeDebut = (Date) Demande.getValue("DateDeDebut");
                    Date demandeFin = Demande.getValue("DateFinReel") != null ? (Date) Demande.getValue("DateFinReel") : (Date) Demande.getValue("DateDeFin");
                    if (dateDebutConge != null && dateFinConge != null
                            && demandeDebut != null && demandeFin != null) {
                        if (new DateValidator(getResourceController()).isTwoDatesOverlapped(dateDebutConge, dateFinConge, demandeDebut, demandeFin)) {
                            row.setValue("Statut", "Conflit");
                            allDemandeOverlapped.add(Demande);
                            if (Demande.getValue("FinConge").equals("DJ")) {
                                if (new DateValidator(getResourceController()).isDateEqualsDate(demandeFin, dateDebutConge)) {
                                    row.setValue("Statut", "OK");
                                }
                            }
                            if (Demande.getValue("DebutConge").equals("DJ")) {
                                if (new DateValidator(getResourceController()).isDateEqualsDate(demandeDebut, dateFinConge)) {
                                    row.setValue("Statut", "OK");
                                }
                            }
                        }
                    }
                }
                row.setValue("NombreDeJoursDemandes", nbrJoursCongesAnnuel);
                VueCongeOverlapped.allDmande = allDemandeOverlapped;

                row.save(workflowModule.getSysadminContext());
                document.addLinkedResource(row);


            }
            document.save(workflowModule.getSysadminContext());
            DiviseCongeAnnuel(allDemandeOverlapped);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // return the Selected Users in the table
    public ArrayList<ILinkedResource> getTableUsers() {

        ArrayList<ILinkedResource> Users = (ArrayList<ILinkedResource>) document.getLinkedResources("UsersDeDepartement");
        ArrayList<ILinkedResource> exceptionUsers = (ArrayList<ILinkedResource>) document.getLinkedResources("DiviserCongeAnnuel");
        ArrayList<ILinkedResource> selectedUsers = new ArrayList<>();
        for (ILinkedResource user : Users) {
            if (user.getValue("cocher").equals(true) && user.getValue("Statut").equals("OK")) {
                selectedUsers.add(user);
            } else if (user.getValue("cocher").equals(false) && user.getValue("Statut").equals("Conflit")) {
                for (ILinkedResource exceptionUser : exceptionUsers) {
                    if (user.getValue("Collaborateur").equals(exceptionUser.getValue("Collaborateur"))) {
                        selectedUsers.add(exceptionUser);
                    }
                }
            } else if (user.getValue("cocher").equals(true) && user.getValue("Statut").equals("Conflit")) {
                overlappedIsChecked = false;
            }
//          getWorkflowInstance().deleteLinkedResource(user);
//          getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        }
        return selectedUsers;
    }

    public Collection<IWorkflowInstance> getAllDemandeurDemandes(IUser Pdemandeur) {
        Collection<IWorkflowInstance> collection = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
            IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("Demandeur", Pdemandeur);
            controller.addInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("A clôturer", "Clôturé")));
            //controller.addNotInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("En cours", "Refusé", "Annulé")));
            // controller.addNotEqualsConstraint("sys_Reference", document.getValue("sys_Reference"));
            controller.setOrderBy("DateDeDebut", Date.class, true);
            collection = controller.evaluate(w);
            return collection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void DiviseCongeAnnuelV2(ArrayList<IWorkflowInstance> allDemandeOverlapped2, Date annualLeaveStartingDate, Date annualLeaveEndingDate) {
        Map<IUser, List<IWorkflowInstance>> userDemandes = new HashMap<>();

        // Group demandes by demandeur
        for (IWorkflowInstance userDemande : allDemandeOverlapped2) {
            IUser demandeur = (IUser) userDemande.getValue("Demandeur");
            userDemandes.computeIfAbsent(demandeur, k -> new ArrayList<>()).add(userDemande);
        }

        float minusDemiJour = 0;

        for (Map.Entry<IUser, List<IWorkflowInstance>> entry : userDemandes.entrySet()) {
            IUser demandeur = entry.getKey();
            List<IWorkflowInstance> allDemandeOverlapped = entry.getValue();

            Date previousEndDate = annualLeaveStartingDate;
            IWorkflowInstance lastDemande = null;
            for (IWorkflowInstance currentDemande : allDemandeOverlapped) {
                lastDemande = currentDemande;
                Date dateDebutConge = (Date) currentDemande.getValue("DateDeDebut");
                Date dateFinConge = currentDemande.getValue("DateFinReel") != null ? (Date) currentDemande.getValue("DateFinReel") : (Date) currentDemande.getValue("DateDeFin");
                String trancheDebut = (String) currentDemande.getValue("DebutConge");
                String trancheFin = (String) currentDemande.getValue("FinConge");

                // Calculate dateDebutDivise and dateFinDivise
                Date dateDebutDivise = previousEndDate;
                Date dateFinDivise = dateDebutConge;
                String trancheDebutDivision = "TJ";
                String trancheFinDivision = trancheDebut;

                // Update previousEndDate for the next iteration
                previousEndDate = dateFinConge;

                // Handle DJ case
                if ("DJ".equals(trancheDebut)) {
                    minusDemiJour += 0.5;
                    trancheDebutDivision = "DJ";
                }

                if (dateDebutDivise.before(annualLeaveEndingDate) && dateFinDivise.after(annualLeaveStartingDate)) {
                    // Calculate the available range within the annual leave
                    Date availableRangeStart = dateDebutDivise.after(annualLeaveStartingDate) ? dateDebutDivise : annualLeaveStartingDate;
                    Date availableRangeEnd = dateFinDivise.before(annualLeaveEndingDate) ? dateFinDivise : annualLeaveEndingDate;

                    // Create and save linked resource for the available range
                    createAndSaveLinkedResource(document, demandeur, availableRangeStart, availableRangeEnd, trancheDebutDivision, trancheFinDivision, availableRangeEnd, minusDemiJour);
                }

                minusDemiJour = 0;
            }

            // Handle the case after the last demand in the annual leave
            Date dateDebutConge = (Date) lastDemande.getValue("DateDeDebut");
            Date dateFinConge = (Date) lastDemande.getValue("DateDeFin");
            String tracheDebutConge = (String) lastDemande.getValue("DebutConge");
            String tracheFinConge = (String) lastDemande.getValue("FinConge");
            String trancheDebutDivision = "";
            String trancheFinDivision = "";
            Date dateDebutDivise = dateFinConge;
            if (tracheFinConge.equals("TJ")) {
                trancheDebutDivision = "TJ";
            } else if (tracheFinConge.equals("DJ")) {
                minusDemiJour += 0.5;
                trancheDebutDivision = "DJ";
            }
            Date dateFinDivise = annualLeaveEndingDate;
            trancheFinDivision = "TJ";
            if (dateDebutDivise.before(annualLeaveEndingDate) && dateFinDivise.after(annualLeaveStartingDate)) {
                // Calculate the available range within the annual leave
                Date availableRangeStart = dateDebutDivise.after(annualLeaveStartingDate) ? dateDebutDivise : annualLeaveStartingDate;
                Date availableRangeEnd = dateFinDivise.before(annualLeaveEndingDate) ? dateFinDivise : annualLeaveEndingDate;

                // Create and save linked resource for the available range
                createAndSaveLinkedResource(document, demandeur, availableRangeStart, availableRangeEnd, trancheDebutDivision, trancheFinDivision, availableRangeEnd, minusDemiJour);
            }
        }
    }

    private void createAndSaveLinkedResource(IWorkflowInstance document, IUser demandeur, Date dateDebutDivise, Date dateFinDivise, String trancheDebutDivision, String trancheFinDivision, Date dateFinReel, float minusDemiJour) {
        HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutDivise, dateFinDivise, true);
        float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
        ILinkedResource row = document.createLinkedResource("DiviserCongeAnnuel");
        row.setValue("Collaborateur", demandeur);
        row.setValue("DateDeDebut", dateDebutDivise);
        row.setValue("DateDeFin", dateFinDivise);
        row.setValue("DebutConge", trancheDebutDivision);
        row.setValue("FinConge", trancheFinDivision);
        row.setValue("DateFinReel", dateFinReel);
        row.setValue("NombreDeJoursDemandes", nbrJoursDemande - minusDemiJour);
        row.save(workflowModule.getSysadminContext());
        document.addLinkedResource(row);
    }


    public void DiviseCongeAnnuelOLD(ArrayList<IWorkflowInstance> allDemandeOverlapped) {
        Date dateDebut = (Date) document.getValue("DateDeDebut");
        Date dateFin = (Date) document.getValue("DateFinReel");
        Date dateDebutDivise = null;
        Date dateFinDivise = null;
        String DebutConge = null;
        String FinConge = null;
        float minusDemiJour=0 ;

        ArrayList<HashMap<String, Object>> CongeAnnuelDivise = new ArrayList<>();
        for (IWorkflowInstance demandeOverlapped : allDemandeOverlapped) {
            Date dateDebutConge = (Date) demandeOverlapped.getValue("DateDeDebut");
            Date dateFinConge = demandeOverlapped.getValue("DateFinReel") != null ? (Date) demandeOverlapped.getValue("DateFinReel") : (Date) demandeOverlapped.getValue("DateDeFin");;
            IUser collaborateur = (IUser) demandeOverlapped.getValue("Demandeur");
            String DemiJourFin = (String) demandeOverlapped.getValue("FinConge");
            String DemiJourDebut = (String) demandeOverlapped.getValue("DebutConge");
            minusDemiJour=0 ;
            if (allDemandeOverlapped.indexOf(demandeOverlapped) == 0  ) {
                if (dateDebutConge.after(dateDebut)){

                    dateDebutDivise = dateDebut;
                    DebutConge = "TJ";
                    if (DemiJourDebut.equals("DJ")){
                        dateFinDivise = dateDebutConge ;
                        minusDemiJour +=.5 ;
                        FinConge = "DJ";
                    }else {
                        dateFinDivise = new Date(dateDebutConge.getTime() - (1000 * 60 * 60 * 24));
                        FinConge = "TJ";

                    }

                    //  ArrayList<ILinkedResource> TD = (ArrayList<ILinkedResource>) document.getLinkedResources("DiviserCongeAnnuel");
                    HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(),joursOuvrables,joursOuvres).calculateV2(societe,dateDebutDivise, dateFinDivise, true);
                    float nbrJoursDemande = (Float)calculResult.get("nbrJoursDemande");
                    ILinkedResource row = document.createLinkedResource("DiviserCongeAnnuel");
                    row.setValue("Collaborateur", collaborateur);
                    row.setValue("DateDeDebut", dateDebutDivise);
                    row.setValue("DateDeFin", dateFinDivise);
                    row.setValue("DebutConge",DebutConge);
                    row.setValue("FinConge",FinConge);
                    row.setValue("DateFinReel", dateFinDivise);
                    row.setValue("NombreDeJoursDemandes",nbrJoursDemande - minusDemiJour);
                    row.save(workflowModule.getSysadminContext());
                    document.addLinkedResource(row);
                    minusDemiJour=0 ;
                }

                dateFinDivise = null ;
                if (DemiJourFin.equals("DJ")){
                    dateDebutDivise = dateFinConge ;
                    minusDemiJour +=.5 ;
                    DebutConge = "DJ";
                }else {
                    dateDebutDivise = new Date(dateFinConge.getTime() + (1000 * 60 * 60 * 24));
                    DebutConge = "TJ";
                }
            } else if (allDemandeOverlapped.indexOf(demandeOverlapped) > 0) {
                if (dateDebutDivise.equals(dateDebutConge)){
                    dateDebutDivise = null;

                }
                if (dateDebutDivise != null && dateFinDivise == null ){

                    if (DemiJourDebut.equals("DJ")){
                        dateFinDivise = dateDebutConge ;
                        minusDemiJour +=.5 ;
                        FinConge = "DJ";
                    }else {
                        dateFinDivise = new Date(dateDebutConge.getTime() - (1000 * 60 * 60 * 24));
                        FinConge = "TJ";
                    }
                    HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(),joursOuvrables,joursOuvres).calculateV2(societe,dateDebutDivise, dateFinDivise, true);
                    float nbrJoursDemande = (Float)calculResult.get("nbrJoursDemande");
                    if(nbrJoursDemande - minusDemiJour > 0){

                    }
                    ILinkedResource row = document.createLinkedResource("DiviserCongeAnnuel");
                    row.setValue("DateDeDebut", dateDebutDivise);
                    row.setValue("DateDeFin", dateFinDivise);
                    row.setValue("DebutConge",DebutConge);
                    row.setValue("FinConge",FinConge);
                    row.setValue("Collaborateur", collaborateur);
                    row.setValue("DateFinReel", dateFinDivise);
                    row.setValue("NombreDeJoursDemandes",nbrJoursDemande - minusDemiJour);
                    row.save(workflowModule.getSysadminContext());
                    document.addLinkedResource(row);
                    minusDemiJour=0 ;
                    dateFinDivise = null ;
                    dateDebutDivise = null;
                }
                if (dateDebutDivise == null && dateFinDivise == null){
                    if (DemiJourFin.equals("DJ")){
                        dateDebutDivise = dateFinConge ;
                        minusDemiJour +=.5 ;
                        DebutConge = "DJ";
                    }else {
                        dateDebutDivise = new Date(dateFinConge.getTime() + (1000 * 60 * 60 * 24));
                        DebutConge = "TJ";
                    }
                }
            }
            if (allDemandeOverlapped.indexOf(demandeOverlapped) == allDemandeOverlapped.size()-1 &&
                    dateDebutDivise.before(dateFin) || dateDebutDivise.equals(dateFin)){
                dateFinDivise=dateFin;
                FinConge = "TJ";
                HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(),joursOuvrables,joursOuvres).calculateV2(societe,dateDebutDivise, dateFinDivise, true);
                float nbrJoursDemande = (Float)calculResult.get("nbrJoursDemande");
                ILinkedResource row = document.createLinkedResource("DiviserCongeAnnuel");
                row.setValue("DateDeDebut", dateDebutDivise);
                row.setValue("DateDeFin", dateFinDivise);
                row.setValue("DebutConge",DebutConge);
                row.setValue("FinConge",FinConge);
                row.setValue("Collaborateur", collaborateur);
                row.setValue("DateFinReel", dateFinDivise);
                row.setValue("NombreDeJoursDemandes",nbrJoursDemande - minusDemiJour);
                row.save(workflowModule.getSysadminContext());
                document.addLinkedResource(row);

            }


        }
    }




    public void DiviseCongeAnnuel(ArrayList<IWorkflowInstance> allDemandeOverlapped2) {


        Map<IUser, List<IWorkflowInstance>> userDemandes = new HashMap<>();
        // Group demandes by demandeur
        for (IWorkflowInstance userDemande : allDemandeOverlapped2) {
            IUser demandeur = (IUser) userDemande.getValue("Demandeur");
            userDemandes.computeIfAbsent(demandeur, k -> new ArrayList<>()).add(userDemande);
        }



        for (Map.Entry<IUser, List<IWorkflowInstance>> entry : userDemandes.entrySet()) {
            IUser demandeur = entry.getKey();
            demandeur.getFullName();
            List<IWorkflowInstance> allDemandeOverlapped = entry.getValue();
            Date dateDebutAnnuel = (Date) document.getValue("DateDeDebut");
            Date dateFinAnnuel = (Date) document.getValue("DateFinReel");
           // Date previousEndDate = annualLeaveStartingDate;
            IWorkflowInstance lastDemande = null;
            for (int i=0;i<allDemandeOverlapped.size();i++) {
                float minusDemiJour = 0;
                IWorkflowInstance currentDemande = allDemandeOverlapped.get(i);
                IWorkflowInstance nextDemande =i<allDemandeOverlapped.size()-1? allDemandeOverlapped.get(i+1) !=null?allDemandeOverlapped.get(i+1):null:null;
                lastDemande = currentDemande;
                Date currentDemandeDateDebutConge = (Date) currentDemande.getValue("DateDeDebut");
                Date currentDemandeDateFinConge = currentDemande.getValue("DateFinReel") != null ? (Date) currentDemande.getValue("DateFinReel") : (Date) currentDemande.getValue("DateFinReel");

                Date dateDebutDivision = null;
                Date dateFinDivision = null;

                String trancheDebutCurrentDemande =(String) currentDemande.getValue("DebutConge");
                String trancheFinCurrentDemande =(String) currentDemande.getValue("FinConge");

                String trancheDebutNextDemande =nextDemande!=null?(String) nextDemande.getValue("DebutConge"):"";
                String trancheFinNextDemande =nextDemande!=null?(String) nextDemande.getValue("FinConge"):"";

                String trancheDebutDivision = "";
                String trancheFinDivision = "";

                if(currentDemandeDateDebutConge.before(dateDebutAnnuel)){
                    dateDebutDivision = new Date(currentDemandeDateFinConge.getTime() + (1000 * 60 * 60 * 24));
                    //dateDebutDivision = currentDemandeDateFinConge;
                    if(trancheFinCurrentDemande.equals("TJ")){
                        trancheDebutDivision = "TJ";
                    }else{
                        trancheDebutDivision = "DJ";
                        minusDemiJour+=0.5;
                    }

                    if(nextDemande!=null){
                        Date nextDemandeDateDebutConge = (Date) nextDemande.getValue("DateDeDebut");
                        dateFinDivision = new Date(nextDemandeDateDebutConge.getTime() - (1000 * 60 * 60 * 24));

                      //  dateFinDivision = nextDemandeDateDebutConge;
                        if(trancheDebutNextDemande.equals("TJ")){
                            trancheFinDivision = "TJ";
                        }else{
                            trancheFinDivision = "DJ";
                            minusDemiJour+=0.5;
                        }

                    }else {
                        dateFinDivision = dateFinAnnuel;
                        trancheFinDivision = "TJ";
                    }
                }else{
                    dateDebutDivision = dateDebutAnnuel;
                    trancheDebutDivision = "TJ";
                    dateFinDivision = new Date(currentDemandeDateDebutConge.getTime() - (1000 * 60 * 60 * 24));

                    //dateFinDivision = currentDemandeDateDebutConge;
                    if(trancheDebutCurrentDemande.equals("TJ")){
                        trancheFinDivision = "TJ";
                    }else{
                        trancheFinDivision = "DJ";
                        minusDemiJour+=0.5;
                    }
                    dateDebutAnnuel = currentDemandeDateFinConge;
                }


                //Create linked
                HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutDivision, dateFinDivision, true);
                float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
                ILinkedResource row = document.createLinkedResource("DiviserCongeAnnuel");
                row.setValue("Collaborateur", demandeur);
                row.setValue("DateDeDebut", dateDebutDivision);
                row.setValue("DateDeFin", dateFinDivision);
                row.setValue("DebutConge", trancheDebutDivision);
                row.setValue("FinConge", trancheFinDivision);
                row.setValue("DateFinReel", calculResult.get("dateFinReel"));
                row.setValue("NombreDeJoursDemandes", nbrJoursDemande - minusDemiJour);
                row.save(workflowModule.getSysadminContext());
                document.addLinkedResource(row);

            }
            //handle last Demande
            Date lastDemandeDateFinConge = (Date) lastDemande.getValue("DateFinReel");
            if(lastDemandeDateFinConge.before(dateFinAnnuel)){
                lastDemandeDateFinConge = new Date(lastDemandeDateFinConge.getTime() + (1000 * 60 * 60 * 24));
                float minusDemiJour = 0;

                String lastDemandeTrancheFinConge = (String) lastDemande.getValue("FinConge");

                HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,lastDemandeDateFinConge, dateFinAnnuel, true);
                float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
                ILinkedResource row = document.createLinkedResource("DiviserCongeAnnuel");
                row.setValue("Collaborateur", demandeur);

                row.setValue("DateDeDebut",lastDemandeDateFinConge );
                row.setValue("DateDeFin", dateFinAnnuel);
                if(lastDemandeTrancheFinConge.equals("TJ")){
                    row.setValue("DebutConge", "TJ");
                }else{
                    row.setValue("DebutConge", "DJ");
                    minusDemiJour+=0.5;
                }

                row.setValue("FinConge", "TJ");
                row.setValue("DateFinReel", calculResult.get("dateFinReel"));
                row.setValue("NombreDeJoursDemandes", nbrJoursDemande - minusDemiJour);
                row.save(workflowModule.getSysadminContext());
                document.addLinkedResource(row);
            }



        }
    }


}
