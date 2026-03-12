package com.moovapps.ibb.rh.OnBoarding.Processus;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.activity.domain.TaskInstance;
import com.axemble.vdp.workflow.domain.ProcessWorkflowInstance;

import java.util.*;

public class CreationOffBoarding extends BaseDocumentExtension {


    @Override
    public boolean onBeforeLoad() {
       //responsablesTraitement = ( ArrayList<IUser> ) getWorkflowInstance().getValue("ResponsablesRealisation2");
        return super.onBeforeLoad();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("Collaborateur3")){
            if (getWorkflowInstance().getValue("Collaborateur3") != null) {
                IStorageResource collaborateur = (IStorageResource) getWorkflowInstance().getValue("Collaborateur3");
                getWorkflowInstance().setValue("Collaborateur", collaborateur.getValue("Salarie"));
                getWorkflowInstance().setValue("Fonction", collaborateur.getValue("Fonction"));
            }

        }
        if(property.getName().equals("Collaborateur")){
            onCollaborateurChange();
            setCategorieRelatedData();
            setParcours();

        }


        super.onPropertyChanged(property);
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("LancerLOffboardingDuCollaborateur")){
            onLancerLOnboardingDuCollaborateur();
            createEtapeOffboarding();

        }
        //return false ;
        return super.onBeforeSubmit(action);
    }

    private void createEtapeOffboarding() {
        ArrayList<IUser> responsablesTraitement = new ArrayList<>();
        ArrayList<ILinkedResource> parcoursDIntegration = (ArrayList<ILinkedResource>) getWorkflowInstance().getValue("ParcoursDeDepartAdministratif");
        parcoursDIntegration.addAll((ArrayList<ILinkedResource>) getWorkflowInstance().getValue("ParcoursDeDepartMetier"));
        for (ILinkedResource parcour : parcoursDIntegration) {
            try {
                IWorkflow etapeOnBoardingWorkflow = getWorkflowModule().getWorkflow(getWorkflowModule().getSysadminContext(), getWorkflowInstance().getCatalog(), "EtapeOffBoarding_1.0");
                IWorkflowInstance etapeOffBoarding = getWorkflowModule().createWorkflowInstance(getDirectoryModule().getContext((IUser) parcour.getValue("Interlocuteur")), etapeOnBoardingWorkflow, "");
                etapeOffBoarding.setValue("Etape",parcour.getValue("Etape"));
                etapeOffBoarding.setValue("Onboardeur2",parcour.getValue("Interlocuteur"));
                etapeOffBoarding.setValue("Collaborateur", getWorkflowInstance().getValue("Collaborateur"));
                etapeOffBoarding.setValue("TypeDeParcours",parcour.getValue("TypeDeParcours"));
                etapeOffBoarding.save(getWorkflowModule().getSysadminContext());
                getWorkflowInstance().addLinkedWorkflowInstance("EtapeOffBoarding",etapeOffBoarding);
                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                responsablesTraitement.add((IUser) parcour.getValue("Interlocuteur"));

                passerALEtapeSuivante(etapeOffBoarding);


            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        if (getWorkflowInstance().getValue("Collaborateur") != null && getWorkflowInstance().getValue("RespN1") != null) {
            responsablesTraitement.add((IUser) getWorkflowInstance().getValue("Collaborateur"));
            responsablesTraitement.add((IUser) getWorkflowInstance().getValue("RespN1"));
        }

        getWorkflowInstance().setValue("ResponsablesRealisation2",responsablesTraitement);
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
    }

    private void passerALEtapeSuivante(IWorkflowInstance etapeOnBoarding){
        try{
            List<TaskInstance> instances = ((ProcessWorkflowInstance) etapeOnBoarding).getTaskInstances(2);
            List<String> actions = Arrays.asList("TransmettreEtapePourTraitement");
            for (TaskInstance taskInstance : instances) {

                ITask task = taskInstance.getTask();
                for (String action1 : actions) {
                    IAction iAction1 = task.getAction(action1);
                    if (iAction1 != null) {
                        taskInstance.removeOperators();
                        taskInstance.addOperator(getWorkflowModule().getOperatorByLogin("sysadmin"));
                        getWorkflowModule().end(getWorkflowModule().getSysadminContext(), taskInstance, iAction1, "");
                        etapeOnBoarding.save(getWorkflowModule().getSysadminContext());
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void onLancerLOnboardingDuCollaborateur(){
        HashSet<IUser> responsablesRealisation = new HashSet<>();
        for (ILinkedResource etapeMetier: getWorkflowInstance().getLinkedResources("ParcoursDIntegrationMetier")) {
            responsablesRealisation.add((IUser) etapeMetier.getValue("Interlocuteur"));
        }
        for (ILinkedResource etapeMetier: getWorkflowInstance().getLinkedResources("ParcoursDIntegrationAdministratif")) {
            responsablesRealisation.add((IUser) etapeMetier.getValue("Interlocuteur"));
        }
        if(responsablesRealisation != null) {}
        getWorkflowInstance().setValue("ResponsablesRealisation", responsablesRealisation);
    }

    private void setCategorieRelatedData(){
        IStorageResource categorie = (IStorageResource) getWorkflowInstance().getValue("Categorie");
        if(categorie == null){
            return;
        }
        int delaiParcoursAdmin = categorie.getValue("DelaiParcoursDIntegrationAdministratif") != null ? ((Number)categorie.getValue("DelaiParcoursDIntegrationAdministratif")).intValue() : 15;
        int delaiParcoursMetier = categorie.getValue("DelaiParcoursDIntegrationMetier") != null ?  ((Number)categorie.getValue("DelaiParcoursDIntegrationMetier")).intValue() : 15;
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, delaiParcoursAdmin);
        getWorkflowInstance().setValue("DateFinParcoursAdminPrev", c.getTime());
        c = Calendar.getInstance();
        c.add(Calendar.DATE, delaiParcoursMetier);
        getWorkflowInstance().setValue("DateFinParcoursMetierPrev", c.getTime());


    }

    private void setParcours(){
        if(getWorkflowInstance().getValue("Categorie") != null){
            IStorageResource categorie = (IStorageResource) getWorkflowInstance().getValue("Categorie");
            ArrayList<IStorageResource> parcoursAdministratif = getParcoursAdministratif(categorie);
            if(parcoursAdministratif != null){
                getWorkflowInstance().deleteLinkedResources(getWorkflowInstance().getLinkedResources("ParcoursDeDepartAdministratif"));
                for (IStorageResource etape :parcoursAdministratif) {
                    ILinkedResource parcousAdminTD = getWorkflowInstance().createLinkedResource("ParcoursDeDepartAdministratif");
                    parcousAdminTD.setValue("Etape", etape.getValue("sys_Title"));
                    parcousAdminTD.setValue("Ordre", etape.getValue("Ordre"));
                    parcousAdminTD.setValue("DelaiJours", etape.getValue("DelaiJours"));
                    parcousAdminTD.setValue("Interlocuteur", etape.getValue("Interlocuteur"));
                    if (etape.getValue("Fonction") != null){
                        parcousAdminTD.setValue("Fonction", etape.getValue("Fonction"));
                    }
                    if (etape.getValue("Departement") != null){
                        parcousAdminTD.setValue("Departement", etape.getValue("Departement"));
                    }
                    parcousAdminTD.setValue("TypeDeParcours","Administratif");
                    getWorkflowInstance().addLinkedResource(parcousAdminTD);
                    parcousAdminTD.save(getWorkflowModule().getSysadminContext());
                }
            }
            ArrayList<IStorageResource> parcoursMetier = getParcoursMetier(categorie);
            if(parcoursMetier != null){
                getWorkflowInstance().deleteLinkedResources(getWorkflowInstance().getLinkedResources("ParcoursDeDepartMetier"));
                for (IStorageResource etape :parcoursMetier) {
                    ILinkedResource parcousMetierTD = getWorkflowInstance().createLinkedResource("ParcoursDeDepartMetier");
                    parcousMetierTD.setValue("Etape", etape.getValue("sys_Title"));
                    parcousMetierTD.setValue("Ordre", etape.getValue("Ordre"));
                    parcousMetierTD.setValue("DelaiJours", etape.getValue("DelaiJours"));
                    parcousMetierTD.setValue("Interlocuteur", etape.getValue("Interlocuteur"));
                    if (etape.getValue("Fonction") != null ){
                        parcousMetierTD.setValue("Fonction", etape.getValue("Fonction"));
                    }
                    if (etape.getValue("Departement") != null){
                        parcousMetierTD.setValue("Departement", etape.getValue("Departement"));
                    }
                    parcousMetierTD.setValue("TypeDeParcours","Metier");
                    getWorkflowInstance().addLinkedResource(parcousMetierTD);
                    parcousMetierTD.save(getWorkflowModule().getSysadminContext());
                }
            }
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        }
    }

    ArrayList<IStorageResource> getParcoursAdministratif(IStorageResource categorie){
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "OnBoardingOffBoarding", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "ReferentielOnBoardingOffBoarding",ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "ParcoursDeDepartAdministratif");
            controller.addEqualsConstraint("Categorie", categorie);
            return (ArrayList<IStorageResource>) controller.evaluate(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    ArrayList<IStorageResource> getParcoursMetier(IStorageResource categorie){
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "OnBoardingOffBoarding", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "ReferentielOnBoardingOffBoarding",ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "ParcoursDeDepartMetier");
            controller.addEqualsConstraint("Categorie", categorie);
            return (ArrayList<IStorageResource>) controller.evaluate(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onCollaborateurChange(){
        IUser user = (IUser) getWorkflowInstance().getValue("Collaborateur");
        if(user != null){
            getWorkflowInstance().setValue("Matricule", user.getExtendedAttributes().getValue("Matricule"));
            getWorkflowInstance().setValue("DateDEmbauche", user.getExtendedAttributes().getValue("DateDEmbauche"));
            //getWorkflowInstance().setValue("Fonction", user.getExtendedAttributes().getValue("Fonction2"));
            getWorkflowInstance().setValue("Categorie", user.getExtendedAttributes().getValue("Categorie"));
            IUser respN1 = user.getHierarchicalManager();
            if(respN1 != null){
                getWorkflowInstance().setValue("RespN1", respN1);
                getWorkflowInstance().setValue("FonctionN1", respN1.getExtendedAttributes().getValue("Fonction"));
            }
        }else {
            getWorkflowInstance().setValue("Matricule", null);
            getWorkflowInstance().setValue("DateDEmbauche", null);
            getWorkflowInstance().setValue("Fonction", null);
            getWorkflowInstance().setValue("Categorie", null);
            getWorkflowInstance().setValue("RespN1", null);
            getWorkflowInstance().setValue("FonctionN1", null);
            getWorkflowInstance().deleteLinkedResources(getWorkflowInstance().getLinkedResources("ParcoursDIntegrationAdministratif"));
            getWorkflowInstance().deleteLinkedResources(getWorkflowInstance().getLinkedResources("ParcoursDIntegrationMetier"));
        }
    }
}
