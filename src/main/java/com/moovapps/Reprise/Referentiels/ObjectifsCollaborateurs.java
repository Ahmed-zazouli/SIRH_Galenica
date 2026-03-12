package com.moovapps.Reprise.Referentiels;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ObjectifsCollaborateurs extends BaseAgent {

    @Override
    protected void execute() {
        ArrayList<IStorageResource> fiches = getFiches();
        ArrayList<String> missions = new ArrayList<String>(
                Arrays.asList("Objectif 1", "Objectif 2"));

        if(fiches!=null && !fiches.isEmpty()){
            for(IStorageResource fiche : fiches){
                for(String mission : missions){
                    IStorageResource objectifsCollaborateur = createStorage("DefaultOrganization","EVAL","Referentiels","ObjectifsDeLAnnee");
                    objectifsCollaborateur.setValue("Collaborateur",fiche.getValue("Salarie"));
                    objectifsCollaborateur.setValue("Annee",2023);
                    objectifsCollaborateur.setValue("Objectifs",mission);
                    objectifsCollaborateur.setValue("Importance",50);
                    objectifsCollaborateur.setValue("EcheancePrevue",new Date());
                    objectifsCollaborateur.save(getWorkflowModule().getSysadminContext());
                }
            }
        }
    }

    ArrayList<IStorageResource> getFiches(){
        ArrayList<IStorageResource> fiches = null;
        try{

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"FicheCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            fiches =   (ArrayList<IStorageResource>) controller.evaluate(definition);


        }catch (Exception e){
            e.printStackTrace();
        }
        return fiches;
    }

    private IStorageResource createStorage(String organizationName ,  String projetName, String catalogName, String definitionName) {
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,organizationName);
            IProject projet = getProjectModule().getProject(context,projetName,organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,catalogName,ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,definitionName);
            return getWorkflowModule().createStorageResource(context,definition,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
