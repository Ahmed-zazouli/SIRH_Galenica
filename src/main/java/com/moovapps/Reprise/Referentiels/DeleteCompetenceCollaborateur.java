package com.moovapps.Reprise.Referentiels;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;

public class DeleteCompetenceCollaborateur extends BaseAgent {
    @Override
    protected void execute() {
        ArrayList<IStorageResource> data = getData("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","CompetenceSalarie");
        if(data!=null && !data.isEmpty()){
            for(IStorageResource item :data){
                item.delete(getWorkflowModule().getSysadminContext());
                item.save(getWorkflowModule().getSysadminContext());
            }
        }

    }

    private ArrayList<IStorageResource>  getData(String organizationName , String projetName, String catalogName, String definitionName) {
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
