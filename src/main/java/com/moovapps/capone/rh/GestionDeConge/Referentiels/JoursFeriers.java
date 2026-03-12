package com.moovapps.capone.rh.GestionDeConge.Referentiels;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.activity.domain.ActionTaskInstance;
import com.moovapps.capone.rh.GestionDeConge.document.DemandeConge2;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class JoursFeriers extends BaseDocumentExtension {

    @Override
    public void onPropertyChanged(IProperty property) {
        if(property.getName().equals("FeteReligieuse")|| property.getName().equals("DateDebutJourFerie")){
            Date date = (Date) getWorkflowInstance().getValue("DateDebutJourFerie");
            if(date!=null){
                Boolean isFeteReligieuse = (Boolean) getWorkflowInstance().getValue("FeteReligieuse");
                if(isFeteReligieuse!=null){
                    if(isFeteReligieuse){
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        String dateText = dateFormat.format(date);
                        getWorkflowInstance().setValue("Date",dateText);
//                        if (getWorkflowInstance().getValue("NJoursFeries") != null){
//                            getDemandeCongeByDateJourFerie();
//                        }

                    }else{
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
                        String dateText = dateFormat.format(date);
                        getWorkflowInstance().setValue("Date",dateText);
                    }
                }else{
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
                    String dateText = dateFormat.format(date);
                    getWorkflowInstance().setValue("Date",dateText);
                }

            }else{
                getWorkflowInstance().setValue("Date",null);
            }



            Date dateJourFerier = (Date) getWorkflowInstance().getValue("DateDebutJourFerie");
            if(dateJourFerier!=null){
                // Create a Calendar instance and set it to the current date
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateJourFerier);

                // Get the day of the week as an integer (Sunday is 1, Monday is 2, ..., Saturday is 7)
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                // Convert the integer to the day name
                String dayName = getDayName(dayOfWeek);
                getWorkflowInstance().setValue("Jour",dayName);

            }else {
                getWorkflowInstance().setValue("Jour",null);
            }

        }
        super.onPropertyChanged(property);
    }

    private void getDemandeCongeByDateJourFerie() {
        Collection<IWorkflowInstance> demandeConge = new ArrayList<IWorkflowInstance>();
        Date date = (Date) getWorkflowInstance().getValue("DateDebutJourFerie");
        int NJoursFeries =((Number) getWorkflowInstance().getValue("NJoursFeries")).intValue();
        ArrayList<Date> jourFeries = new ArrayList<Date>();
        jourFeries.add(date);
        for (int i=1 ; i<NJoursFeries;i++){
            Date datePlus = new Date(date.getTime() + (1000 * 60 * 60 * 24 * i));
            jourFeries.add(datePlus);
        }
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IUser connectedUser = getWorkflowModule().getLoggedOnUser();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
            IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            for (Date jourFerie :jourFeries) {
                controller.addGreaterOrEqualConstraint("DateFinReel", jourFerie);
                controller.addLessOrEqualsConstraint("DateDeDebut", jourFerie);

                if (!controller.evaluate(w).isEmpty()) {
                    if (!demandeConge.containsAll(controller.evaluate(w))){

                        demandeConge.addAll(controller.evaluate(w)) ;

                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();

        }


    }

    @Override
    public boolean onBeforeLoad() {

        Date date = (Date) getWorkflowInstance().getValue("DateDebutJourFerie");
        if(date!=null){
            Boolean isFeteReligieuse = (Boolean) getWorkflowInstance().getValue("FeteReligieuse");
            if(isFeteReligieuse!=null){
                if(isFeteReligieuse){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    String dateText = dateFormat.format(date);
                    getWorkflowInstance().setValue("Date",dateText);
                }else{
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
                    String dateText = dateFormat.format(date);
                    getWorkflowInstance().setValue("Date",dateText);
                }
            }else{
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
                String dateText = dateFormat.format(date);
                getWorkflowInstance().setValue("Date",dateText);
            }

        }else{
            getWorkflowInstance().setValue("Date",null);
        }

        Date dateJourFerier = (Date) getWorkflowInstance().getValue("DateDebutJourFerie");
        if(dateJourFerier!=null){
            // Create a Calendar instance and set it to the current date
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateJourFerier);

            // Get the day of the week as an integer (Sunday is 1, Monday is 2, ..., Saturday is 7)
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            // Convert the integer to the day name
            String dayName = getDayName(dayOfWeek);
            getWorkflowInstance().setValue("Jour",dayName);

        }else {
            getWorkflowInstance().setValue("Jour",null);
        }

        return super.onBeforeLoad();
    }

    @Override
    public boolean onBeforeSave() {
//       ArrayList<IResource> congeOverlapped= getDocsSinceVue("uril://vdoc/workflowContainerView/DefaultOrganization/Capone/RH:0/GestionDeConges/VueJourFerier");
//        RecalculateNbJours(congeOverlapped);
        return super.onBeforeSave();
        //return false;
    }

    @Override
    public boolean onAfterSave() {
        ArrayList<IResource> congeOverlapped= getDocsSinceVue("uril://vdoc/workflowContainerView/DefaultOrganization/Capone/RH:0/GestionDeConges/VueJourFerier");
        RecalculateNbJours(congeOverlapped);
        return super.onAfterSave();
    }

    private void RecalculateNbJours(ArrayList<IResource> congeOverlapped) {
        for (IResource conge : congeOverlapped) {
            conge.setValue("NombreDeJoursDemandesOld", conge.getValue("NombreDeJoursDemandes"));
            DemandeConge2 demandeConge2 =new DemandeConge2();
            demandeConge2.document = (IWorkflowInstance) conge;
            demandeConge2.societe = (IStorageResource) conge.getValue("Societe");
            demandeConge2.calculateConges(true);
            conge.save(getWorkflowModule().getSysadminContext());

            setDemandeurFields((IWorkflowInstance) conge);
        }
    }

    private void setDemandeurFields(IWorkflowInstance document) {
        IUser demandeur = (IUser) document.getValue("Demandeur");

        float diff = 0;
        if (document.getValue("NombreDeJoursDemandes") != null) {
             diff = ((Number) document.getValue("NombreDeJoursDemandesOld")).floatValue() - ((Number) document.getValue("NombreDeJoursDemandes")).floatValue();
        }


        String categorieConges = (String) document.getValue("TypeDeConge");
        if (categorieConges.equals("CN")) {
            setDemandeurCongesPayesInfo(document);
        } else if (categorieConges.equals("CE")) {
            //setDemandeurCongesSpeciauxInfo(document);
        } else if (categorieConges.equals("CM")) {
            setDemandeurCongesMaladieInfo(document);
        } else if (categorieConges.equals("SS")) {
            setDemandeurCongesSansSoldeInfo(document);
        } else if (categorieConges.equals("Absence")) {
            demandeur.getExtendedAttributes().setValue("AbsenceAnneeEnCours",diff);
        }


        demandeur.save(getWorkflowModule().getSysadminContext());
    }

    private void setDemandeurCongesPayesInfo(IWorkflowInstance document) {
        IUser demandeur = (IUser) document.getValue("Demandeur");
        IStorageResource DemandeurRef = getCollaborateur(demandeur);
        float diff = ((Number) document.getValue("NombreDeJoursDemandesOld")).floatValue() - ((Number) document.getValue("NombreDeJoursDemandes")).floatValue();

        String statut = (String) document.getValue("DocumentState");

        float CongesPayesEnCoursDeValidation =((Number) DemandeurRef.getValue("CongesPayesEnCoursDeValidation")).floatValue();
        float JourSEnCoursDeValidation =((Number) DemandeurRef.getValue("JourSEnCoursDeValidation")).floatValue();

        float CongesPayesEnCoursDeConsommation =((Number) DemandeurRef.getValue("CongesPayesEnCoursDeConsommation")).floatValue();
        float JourSEnCoursDeConsommation =((Number) DemandeurRef.getValue("joursEnCoursConsommation")).floatValue();

        float CongesPayesEnCoursDeTraitement =((Number) DemandeurRef.getValue("CongesPayesEnCoursDeTraitement")).floatValue();
        float JourSEnCoursDeTraitement =((Number) DemandeurRef.getValue("JourSEnCoursDeTraitement")).floatValue();

        float CongesPayesPris =((Number) DemandeurRef.getValue("CongesPayesPris")).floatValue();
        float TotalJoursPris =((Number) DemandeurRef.getValue("TotalJoursPris")).floatValue();

        float SoldeConges =((Number) DemandeurRef.getValue("SoldeConges")).floatValue();
        float SoldeAnneeEnCours =((Number) DemandeurRef.getValue("SoldeAnneeEnCours")).floatValue();

        float CongesPayesAnneeEnCours =((Number) DemandeurRef.getValue("CongesPayesAnneeEnCours")).floatValue();

        if (statut.equals("En cours de validation")) {
            demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation - diff);
            DemandeurRef.setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation - diff);
            demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", JourSEnCoursDeValidation - diff); demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation - diff);
            DemandeurRef.setValue("JourSEnCoursDeValidation", JourSEnCoursDeValidation - diff);
            document.setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation - diff);

        } else if (statut.equals("Validée")) {
            demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation", CongesPayesEnCoursDeConsommation - diff);
            DemandeurRef.setValue("CongesPayesEnCoursDeConsommation", CongesPayesEnCoursDeConsommation - diff);
            demandeur.getExtendedAttributes().setValue("JourSEnCoursDeConsommation", JourSEnCoursDeConsommation - diff);
            DemandeurRef.setValue("JourSEnCoursDeConsommation", JourSEnCoursDeConsommation - diff);
            document.setValue("CongesPayesEnCoursDeConsommation", CongesPayesEnCoursDeConsommation - diff);
        } else if (statut.equals("A clôturer")) {
            demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeTraitement", CongesPayesEnCoursDeTraitement - diff);
            DemandeurRef.setValue("CongesPayesEnCoursDeTraitement", CongesPayesEnCoursDeTraitement - diff);
            demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement", JourSEnCoursDeTraitement - diff);
            DemandeurRef.setValue("JourSEnCoursDeTraitement", JourSEnCoursDeTraitement - diff);
            document.setValue("JourSEnCoursDeTraitement", JourSEnCoursDeTraitement - diff);
        } else if (statut.equals("Clôturée")) {
            demandeur.getExtendedAttributes().setValue("CongesPayesPris", CongesPayesPris - diff);
            DemandeurRef.setValue("CongesPayesPris", CongesPayesPris - diff);
            demandeur.getExtendedAttributes().setValue("TotalJoursPris", TotalJoursPris - diff);
            DemandeurRef.setValue("TotalJoursPris", TotalJoursPris - diff);
            demandeur.getExtendedAttributes().setValue("SoldeConges", SoldeConges + diff);
            DemandeurRef.setValue("SoldeConges", SoldeConges + diff);
            demandeur.getExtendedAttributes().setValue("SoldeAnneeEnCours", SoldeAnneeEnCours + diff);
            DemandeurRef.setValue("SoldeAnneeEnCours", SoldeAnneeEnCours + diff);

            demandeur.getExtendedAttributes().setValue("CongesPayesAnneeEnCours", CongesPayesAnneeEnCours - diff);
            DemandeurRef.setValue("CongesPayesAnneeEnCours", CongesPayesAnneeEnCours - diff);

            document.setValue("CongesPayesPris", CongesPayesPris - diff);

        }

        demandeur.save(getWorkflowModule().getSysadminContext());
        DemandeurRef.save(getWorkflowModule().getSysadminContext());
        document.save(getWorkflowModule().getSysadminContext());


    }


    private void setDemandeurCongesSpeciauxInfo(IWorkflowInstance document) {
        IUser demandeur = (IUser) document.getValue("Demandeur");
        IStorageResource DemandeurRef = getCollaborateur(demandeur);
        float diff = ((Number) document.getValue("NombreDeJoursDemandesOld")).floatValue() - ((Number) document.getValue("NombreDeJoursDemandes")).floatValue();
        String statut = (String) document.getValue("DocumentState");

        float CongesPayesEnCoursDeValidation =((Number) DemandeurRef.getValue("CongesPayesEnCoursDeValidation")).floatValue();
        float JourSEnCoursDeValidation =((Number) DemandeurRef.getValue("JourSEnCoursDeValidation")).floatValue();

        float CongesPayesEnCoursDeConsommation =((Number) DemandeurRef.getValue("CongesPayesEnCoursDeConsommation")).floatValue();
        float JourSEnCoursDeConsommation =((Number) DemandeurRef.getValue("JourSEnCoursDeConsommation")).floatValue();

        float CongesPayesEnCoursDeTraitement =((Number) DemandeurRef.getValue("CongesPayesEnCoursDeTraitement")).floatValue();
        float JourSEnCoursDeTraitement =((Number) DemandeurRef.getValue("JourSEnCoursDeTraitement")).floatValue();

        float CongesPayesPris =((Number) DemandeurRef.getValue("CongesPayesPris")).floatValue();
        float TotalJoursPris =((Number) DemandeurRef.getValue("TotalJoursPris")).floatValue();

        float SoldeConges =((Number) DemandeurRef.getValue("SoldeConges")).floatValue();
        float SoldeAnneeEnCours =((Number) DemandeurRef.getValue("SoldeAnneeEnCours")).floatValue();

        if (statut.equals("En cours de validation")) {
            DemandeurRef.setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation - diff);
            demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", JourSEnCoursDeValidation - diff);
            document.setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation - diff);

        } else if (statut.equals("Validée")) {
            demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation", CongesPayesEnCoursDeConsommation - diff);
            demandeur.getExtendedAttributes().setValue("JourSEnCoursDeConsommation", JourSEnCoursDeConsommation - diff);
            document.setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation - diff);
        } else if (statut.equals("A clôturer")) {
            demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeTraitement", CongesPayesEnCoursDeTraitement - diff);
            demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement", JourSEnCoursDeTraitement - diff);
            document.setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation - diff);
        } else if (statut.equals("Clôturée")) {
            demandeur.getExtendedAttributes().setValue("CongesPayesPris", CongesPayesPris - diff);
            demandeur.getExtendedAttributes().setValue("TotalJoursPris", TotalJoursPris - diff);
            demandeur.getExtendedAttributes().setValue("SoldeConges", SoldeConges + diff);
            demandeur.getExtendedAttributes().setValue("SoldeAnneeEnCours", SoldeAnneeEnCours + diff);
            document.setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation - diff);
        }

        demandeur.save(getWorkflowModule().getSysadminContext());
        document.save(getWorkflowModule().getSysadminContext());
    }

    private void setDemandeurCongesMaladieInfo(IWorkflowInstance document) {
        IUser demandeur = (IUser) document.getValue("Demandeur");
        IStorageResource DemandeurRef = getCollaborateur(demandeur);
        float diff = ((Number) document.getValue("NombreDeJoursDemandesOld")).floatValue() - ((Number) document.getValue("NombreDeJoursDemandes")).floatValue();
        String statut = (String) document.getValue("DocumentState");

        float CongesMaladieEnCoursDeValidation =((Number) DemandeurRef.getValue("CongesMaladieEnCoursDeValidation")).floatValue();
        float JourSEnCoursDeValidation =((Number) DemandeurRef.getValue("JourSEnCoursDeValidation")).floatValue();

        float CongesMaladieEnCoursDeConsommation =((Number) DemandeurRef.getValue("CongesMaladieEnCoursDeConsommation")).floatValue();
        float JourSEnCoursDeConsommation =((Number) DemandeurRef.getValue("JourSEnCoursDeConsommation")).floatValue();

        float CongesMaladieEnCoursDeTraitement =((Number) DemandeurRef.getValue("CongesMaladieEnCoursDeTraitement")).floatValue();
        float JourSEnCoursDeTraitement =((Number) DemandeurRef.getValue("JourSEnCoursDeTraitement")).floatValue();

        float CongesMaladiePris =((Number) DemandeurRef.getValue("CongesMaladiePris")).floatValue();
        float TotalJoursPris =((Number) DemandeurRef.getValue("TotalJoursPris")).floatValue();

        float SoldeConges =((Number) DemandeurRef.getValue("SoldeConges")).floatValue();
        float SoldeAnneeEnCours =((Number) DemandeurRef.getValue("SoldeAnneeEnCours")).floatValue();

        float CongesMaladieAnneeEnCours =((Number) DemandeurRef.getValue("CongesMaladieAnneeEnCours")).floatValue();

        if (statut.equals("En cours de validation")) {
            demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation", CongesMaladieEnCoursDeValidation - diff);
            DemandeurRef.setValue("CongesPayesEnCoursDeValidation", CongesMaladieEnCoursDeValidation - diff);
           // demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", JourSEnCoursDeValidation - diff);
            document.setValue("CongesMaladieEnCoursDeValidation", CongesMaladieEnCoursDeValidation - diff);

        } else if (statut.equals("Validée")) {
            demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation", CongesMaladieEnCoursDeConsommation - diff);
            DemandeurRef.setValue("CongesPayesEnCoursDeConsommation", CongesMaladieEnCoursDeConsommation - diff);
            //demandeur.getExtendedAttributes().setValue("JourSEnCoursDeConsommation", JourSEnCoursDeConsommation - diff);
            document.setValue("CongesMaladieEnCoursDeConsommation", CongesMaladieEnCoursDeConsommation - diff);
        } else if (statut.equals("A clôturer")) {
            demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeTraitement", CongesMaladieEnCoursDeTraitement - diff);
            DemandeurRef.setValue("CongesPayesEnCoursDeTraitement", CongesMaladieEnCoursDeTraitement - diff);
          //  demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement", JourSEnCoursDeTraitement - diff);
            document.setValue("CongesMaladieEnCoursDeTraitement", CongesMaladieEnCoursDeTraitement - diff);
        } else if (statut.equals("Clôturée")) {
            demandeur.getExtendedAttributes().setValue("CongesMaladiePris", CongesMaladiePris - diff);
            DemandeurRef.setValue("CongesMaladiePris", CongesMaladiePris - diff);
            demandeur.getExtendedAttributes().setValue("CongesMaladieAnneeEnCours", CongesMaladieAnneeEnCours - diff);
            DemandeurRef.setValue("CongesMaladiePris", CongesMaladieAnneeEnCours - diff);
            //demandeur.getExtendedAttributes().setValue("TotalJoursPris", TotalJoursPris - diff);
           // demandeur.getExtendedAttributes().setValue("SoldeConges", SoldeConges + diff);
            // demandeur.getExtendedAttributes().setValue("SoldeAnneeEnCours", SoldeAnneeEnCours + diff);
            document.setValue("CongesMaladiePris", CongesMaladiePris - diff);
        }

        demandeur.save(getWorkflowModule().getSysadminContext());
        DemandeurRef.save(getWorkflowModule().getSysadminContext());
        document.save(getWorkflowModule().getSysadminContext());
    }

    private void setDemandeurCongesSansSoldeInfo(IWorkflowInstance document) {
        IUser demandeur = (IUser) document.getValue("Demandeur");
        IStorageResource DemandeurRef = getCollaborateur(demandeur);
        float diff = ((Number) document.getValue("NombreDeJoursDemandesOld")).floatValue() - ((Number) document.getValue("NombreDeJoursDemandes")).floatValue();
        String statut = (String) document.getValue("DocumentState");

        float CongesSansSoldeEnCoursDeValidation =((Number) DemandeurRef.getValue("CongesSansSoldeEnCoursDeValidation")).floatValue();
        float JourSEnCoursDeValidation =((Number) DemandeurRef.getValue("JourSEnCoursDeValidation")).floatValue();

        float CongesSansSoldeEnCoursDeConsommation =((Number) DemandeurRef.getValue("CongesSansSoldeEnCoursDeConsommation")).floatValue();
        float JourSEnCoursDeConsommation =((Number) DemandeurRef.getValue("JourSEnCoursDeConsommation")).floatValue();

        float CongesSansSoldeEnCoursDeTraitement =((Number) DemandeurRef.getValue("CongesSansSoldeEnCoursDeTraitement")).floatValue();
        float JourSEnCoursDeTraitement =((Number) DemandeurRef.getValue("JourSEnCoursDeTraitement")).floatValue();

        float CongesSansSoldePris =((Number) DemandeurRef.getValue("CongesSansSoldePris")).floatValue();
        float TotalJoursPris =((Number) DemandeurRef.getValue("TotalJoursPris")).floatValue();

        float SoldeConges =((Number) DemandeurRef.getValue("SoldeConges")).floatValue();
        float SoldeAnneeEnCours =((Number) DemandeurRef.getValue("SoldeAnneeEnCours")).floatValue();

        float CongesSansSoldeAnneeEnCours =((Number) DemandeurRef.getValue("CongesSansSoldeAnneeEnCours")).floatValue();

        if (statut.equals("En cours de validation")) {
            demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation", CongesSansSoldeEnCoursDeValidation - diff);
            DemandeurRef.setValue("CongesSansSoldeEnCoursDeValidation", CongesSansSoldeEnCoursDeValidation - diff);
            //demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", JourSEnCoursDeValidation - diff);
            document.setValue("CongesSansSoldeEnCoursDeValidation", CongesSansSoldeEnCoursDeValidation - diff);

        } else if (statut.equals("Validée")) {
            demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeConsommation", CongesSansSoldeEnCoursDeConsommation - diff);
            DemandeurRef.setValue("CongesSansSoldeEnCoursDeConsommation", CongesSansSoldeEnCoursDeConsommation - diff);
            //demandeur.getExtendedAttributes().setValue("JourSEnCoursDeConsommation", JourSEnCoursDeConsommation - diff);
            document.setValue("CongesSansSoldeEnCoursDeConsommation", CongesSansSoldeEnCoursDeConsommation - diff);
        } else if (statut.equals("A clôturer")) {
            demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeTraitement", CongesSansSoldeEnCoursDeTraitement - diff);
            DemandeurRef.setValue("CongesSansSoldeEnCoursDeTraitement", CongesSansSoldeEnCoursDeTraitement - diff);
            //demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement", JourSEnCoursDeTraitement - diff);
            document.setValue("CongesSansSoldeEnCoursDeTraitement", CongesSansSoldeEnCoursDeTraitement - diff);
        } else if (statut.equals("Clôturée")) {
            demandeur.getExtendedAttributes().setValue("CongesSansSoldePris", CongesSansSoldePris - diff);
            DemandeurRef.setValue("CongesSansSoldePris", CongesSansSoldePris - diff);

            demandeur.getExtendedAttributes().setValue("CongesSansSoldeAnneeEnCours", CongesSansSoldeAnneeEnCours - diff);
            DemandeurRef.setValue("CongesSansSoldeAnneeEnCours", CongesSansSoldeAnneeEnCours - diff);
           // demandeur.getExtendedAttributes().setValue("TotalJoursPris", TotalJoursPris - diff);
            //demandeur.getExtendedAttributes().setValue("SoldeConges", SoldeConges + diff);
            //demandeur.getExtendedAttributes().setValue("SoldeAnneeEnCours", SoldeAnneeEnCours + diff);
            document.setValue("CongesSansSoldePris", CongesSansSoldePris - diff);
        }

        demandeur.save(getWorkflowModule().getSysadminContext());
        DemandeurRef.save(getWorkflowModule().getSysadminContext());
        document.save(getWorkflowModule().getSysadminContext());
    }

    private IStorageResource getCollaborateur(IUser demandeur) {
        IStorageResource fiche = null;
        try {

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "FicheCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            controller.addEqualsConstraint("Salarie", demandeur);
            ArrayList<IStorageResource> data = (ArrayList<IStorageResource>) controller.evaluate(definition);
            if (data != null && !data.isEmpty()) {
                fiche = data.iterator().next();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return fiche;
    }

    private ArrayList<IResource> getDocsSinceVue(String urlVue) {
        final ArrayList<IResource> arrayList = new ArrayList<IResource>();
        final String[] lien = { urlVue };
        try {
            String[] array;
            for (int length = (array = lien).length, i = 0; i < length; ++i) {
                final String string = array[i];
                final IView view = (IView) this.getWorkflowModule().getElementByProtocolURI(string);
                final ByteArrayInputStream bais = new ByteArrayInputStream(view.getXmlDefinition());
                final IContext sysContext = this.getWorkflowModule().getSysadminContext();
                final IOrganization organization = this.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                final IProject project = this.getProjectModule().getProject(sysContext, "Capone", organization);
                final IViewController viewController = this.getWorkflowModule().getViewController(this.getWorkflowModule().getLoggedOnUserContext(), project, (InputStream) bais);
                final Collection<Object> resources = (Collection<Object>) viewController.evaluate();
                for (final Object iResource : resources) {
                    IWorkflowInstance instance = null;
                    if (iResource instanceof ActionTaskInstance) {
                        instance = (IWorkflowInstance) ((ActionTaskInstance) iResource).getWorkflowInstance();
                    } else {
                        instance = (IWorkflowInstance) iResource;
                    }
                    arrayList.add(instance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
}

    private static String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "Dimanche";
            case Calendar.MONDAY:
                return "Lundi";
            case Calendar.TUESDAY:
                return "Mardi";
            case Calendar.WEDNESDAY:
                return "Mercredi";
            case Calendar.THURSDAY:
                return "Jeudi";
            case Calendar.FRIDAY:
                return "Vendredi";
            case Calendar.SATURDAY:
                return "Samedi";
            default:
                return "Unknown";
        }
    }
}
