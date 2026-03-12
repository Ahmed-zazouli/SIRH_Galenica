package com.moovapps.Formation.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

public class AgentCompetenceExpire extends BaseAgent {
    @Override
    protected void execute() {
        Collection<IStorageResource> delete= getAllCompetenceExpire();
        for (IStorageResource item : delete){
           // item.setValue("Question", null);
            item.delete(getWorkflowModule().getSysadminContext());
            item.save(getWorkflowModule().getSysadminContext());
        }
       /*Collection<IStorageResource> allCompetenceExpire = getAllCompetenceExpire();

       for (IStorageResource competenceExpire : allCompetenceExpire) {

           IStorageResource ParticipantFiche = (IStorageResource) competenceExpire.getValue("Collaborateur");


           if (ParticipantFiche.getValue("Competence") != null) {

               HashSet<IStorageResource> competence = new HashSet<>((Collection) ParticipantFiche.getValue("Competence"));
               //HashSet<IStorageResource> competence = new HashSet<>();
               //competence.add((IStorageResource) competenceExpire.getValue("Competence"));
               competence.remove((IStorageResource) competenceExpire.getValue("Competence"));
               ParticipantFiche.setValue("Competence", competence);
           }


           ParticipantFiche.save(getWorkflowModule().getSysadminContext());

       }*/
    }


    private Collection<IStorageResource> getAllCompetenceExpire() {
        Collection<IStorageResource> competenesDuSalarie = Collections.emptyList();
        try{
        IContext context = getWorkflowModule().getSysadminContext();
        IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
        IProject project = getProjectModule().getProject(context,"Formation",organization);
        ICatalog catalog  = getWorkflowModule().getCatalog(context,"Referentiels",ICatalog.IType.STORAGE,project);
        //IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"CompetenceSalarie");
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"Choix");
        IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            //controller.addLessConstraint("DateDExpiration",new Date());
         competenesDuSalarie =  controller.evaluate(definition);
    } catch (Exception e) {
        e.printStackTrace();
    }
        return  competenesDuSalarie;
    }
}
