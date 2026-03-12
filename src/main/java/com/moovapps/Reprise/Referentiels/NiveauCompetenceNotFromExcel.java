package com.moovapps.Reprise.Referentiels;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;

public class NiveauCompetenceNotFromExcel extends BaseAgent {

    @Override
    protected void execute() {
        ArrayList<IStorageResource> competences = getCompetences();
        if(competences!=null && !competences.isEmpty()){
            for(IStorageResource competence : competences){
                IStorageResource niveauCompetence = createStorage("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","NiveauDeCompetence");
                niveauCompetence.setValue("Domaine",competence.getValue("Domaine"));
                niveauCompetence.setValue("FamilleCompetence",competence.getValue("FamilleCompetence"));
                niveauCompetence.setValue("SousFamilleCompetence",competence.getValue("SousFamilleCompetence"));
                niveauCompetence.setValue("Competence",competence);
                niveauCompetence.setValue("sys_Title","Niveau 1");
                niveauCompetence.save(getWorkflowModule().getSysadminContext());
            }
        }
    }

    ArrayList<IStorageResource> getCompetences(){
        ArrayList<IStorageResource> data = null;
        try{

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"Competence");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addNotEqualsConstraint("sys_Title","Compétence TEST");
            data =   (ArrayList<IStorageResource>) controller.evaluate(definition);


        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
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
