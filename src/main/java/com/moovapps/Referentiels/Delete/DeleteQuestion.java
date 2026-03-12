package com.moovapps.Referentiels.Delete;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;

public class DeleteQuestion extends BaseAgent {

    @Override
    protected void execute() {
        ArrayList<IStorageResource> data = getData("DefaultOrganization","Formation","Referentiels","Questions");
        if(data!=null && !data.isEmpty()){
            for(IStorageResource item :data){
                item.setValue("ReponseS",null);
                item.setValue("Competence",null);
                item.save(getWorkflowModule().getSysadminContext());
                ArrayList<IStorageResource> choix = getChoix(item);
                if(choix!=null && !choix.isEmpty()){
                    for(IStorageResource choi : choix){
                        choi.delete(getWorkflowModule().getSysadminContext());
                        choi.save(getWorkflowModule().getSysadminContext());
                    }
                }
                item.delete(getWorkflowModule().getSysadminContext());
                item.save(getWorkflowModule().getSysadminContext());
            }
        }
    }

    private ArrayList<IStorageResource> getChoix(IStorageResource question){
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = getProjectModule().getProject(context,"Formation",organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,"Referentiels",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"Choix");
            IViewController controller = getWorkflowModule().getViewController(context,IResource.class);
            controller.addEqualsConstraint("Question",question);
            return (ArrayList<IStorageResource>) controller.evaluate(definition);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<IStorageResource> getData(String organizationName , String projetName, String catalogName, String definitionName) {
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,organizationName);
            IProject projet = getProjectModule().getProject(context,projetName,organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,catalogName,ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,definitionName);
            IViewController controller = getWorkflowModule().getViewController(context,IResource.class);
            return (ArrayList<IStorageResource>) controller.evaluate(definition);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
