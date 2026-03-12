package com.moovapps.ibb.rh.OffBoarding.Processus;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;
import java.util.HashSet;

public class CreationOffBoarding extends BaseDocumentExtension {

    @Override
    public void onPropertyChanged(IProperty property) {
        if(property.getName().equals("Collaborateur")){
            onCollaborateurChange();
            setCategorieRelatedData();
            setParcours();
        }
        super.onPropertyChanged(property);
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("LancerLOnboardingDuCollaborateur")){
            onLancerLOnboardingDuCollaborateur();
        }
        return super.onBeforeSubmit(action);
    }

    private void onLancerLOnboardingDuCollaborateur(){
        HashSet<IUser> responsablesRealisation = new HashSet<>();
        for (ILinkedResource etapeMetier: getWorkflowInstance().getLinkedResources("ParcoursDIntegrationMetier")) {
            responsablesRealisation.add((IUser) etapeMetier.getValue("Interlocuteur"));
        }
        for (ILinkedResource etapeMetier: getWorkflowInstance().getLinkedResources("ParcoursDIntegrationAdministratif")) {
            responsablesRealisation.add((IUser) etapeMetier.getValue("Interlocuteur"));
        }
        getWorkflowInstance().setValue("ResponsablesRealisation", responsablesRealisation);
    }

    private void setCategorieRelatedData(){

    }

    private void setParcours(){
        if(getWorkflowInstance().getValue("Categorie") != null){
            IStorageResource categorie = (IStorageResource) getWorkflowInstance().getValue("Categorie");
            ArrayList<IStorageResource> parcoursAdministratif = getParcoursAdministratif(categorie);
            if(parcoursAdministratif != null){
                for (IStorageResource etape :parcoursAdministratif) {
                    ILinkedResource parcousAdminTD = getWorkflowInstance().createLinkedResource("ParcoursDIntegrationAdministratif");
                    parcousAdminTD.setValue("Etape", etape.getValue("sys_Title"));
                    parcousAdminTD.setValue("Ordre", etape.getValue("Ordre"));
                    parcousAdminTD.setValue("DelaiJours", etape.getValue("DelaiJours"));
                    parcousAdminTD.setValue("Interlocuteur", etape.getValue("Interlocuteur"));
                    parcousAdminTD.setValue("Fonction", etape.getValue("Fonction"));
                    parcousAdminTD.setValue("Direction", etape.getValue("Direction"));
                    getWorkflowInstance().addLinkedResource(parcousAdminTD);
                    parcousAdminTD.save(getWorkflowModule().getSysadminContext());
                }
            }
            ArrayList<IStorageResource> parcoursMetier = getParcoursMetier(categorie);
            if(parcoursAdministratif != null){
                for (IStorageResource etape :parcoursMetier) {
                    ILinkedResource parcousMetierTD = getWorkflowInstance().createLinkedResource("ParcoursDIntegrationMetier");
                    parcousMetierTD.setValue("Etape", etape.getValue("sys_Title"));
                    parcousMetierTD.setValue("Ordre", etape.getValue("Ordre"));
                    parcousMetierTD.setValue("DelaiJours", etape.getValue("DelaiJours"));
                    parcousMetierTD.setValue("Interlocuteur", etape.getValue("Interlocuteur"));
                    parcousMetierTD.setValue("Fonction", etape.getValue("Fonction"));
                    parcousMetierTD.setValue("Direction", etape.getValue("Direction"));
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
            getWorkflowInstance().setValue("Fonction", user.getExtendedAttributes().getValue("Fonction"));
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
        }
    }
}
