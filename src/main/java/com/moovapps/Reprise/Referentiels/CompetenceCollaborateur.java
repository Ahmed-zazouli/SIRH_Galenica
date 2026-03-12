package com.moovapps.Reprise.Referentiels;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

public class CompetenceCollaborateur extends BaseAgent {
    @Override
    protected void execute() {
        ArrayList<IStorageResource> fiches = getFiches();
        ArrayList<IStorageResource> competences = getCompetences();
        if(fiches!=null && !fiches.isEmpty() && competences!=null && !competences.isEmpty()){
            BigDecimal importance = new BigDecimal("100").divide(new BigDecimal(competences.size()+""),MathContext.DECIMAL128).setScale(2,RoundingMode.DOWN);

            for(IStorageResource fiche : fiches){
                for(IStorageResource comp : competences){
                    IStorageResource compCollaborateur = createStorage("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","CompetenceSalarie");
                    compCollaborateur.setValue("Collaborateur",fiche);
                    compCollaborateur.setValue("Domaine",comp.getValue("Domaine"));
                    compCollaborateur.setValue("FamilleCompetence",comp.getValue("FamilleCompetence"));
                    compCollaborateur.setValue("SousFamilleCompetence",comp.getValue("SousFamilleCompetence"));
                    compCollaborateur.setValue("Competence",comp);
                    compCollaborateur.setValue("Poids", comp.getValue("Poids"));
                    compCollaborateur.setValue("Importance",importance);
                    compCollaborateur.setValue("NiveauDeCompetence", getNivoByComp(comp));
                    compCollaborateur.save(getWorkflowModule().getSysadminContext());

                }
            }
        }
    }

    private IStorageResource getNivoByComp(IStorageResource comp) {

        ArrayList<IStorageResource> data = null;
        IStorageResource item = null;
        try{

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"NiveauDeCompetence");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("Competence",comp);
          //  controller.addEqualsConstraint("sys_Title","Niveau 1");
            data =   (ArrayList<IStorageResource>) controller.evaluate(definition);
            if(data!=null && !data.isEmpty()){
                item = data.iterator().next();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return item;
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
