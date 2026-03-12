package com.moovapps.capone.rh.GestionDeConge.agent;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;

public class AgentResetSoldeConge extends BaseAgent {
    @Override
    protected void execute() {

        ArrayList<IStorageResource> collaborateurs =getFiches();
        for (IStorageResource collaborateur : collaborateurs){
            if (collaborateur != null) {
                IUser user = (IUser) collaborateur.getValue("Salarie");
                if (user != null) {
                    user.getExtendedAttributes().setValue("SoldeConges", 0);
                    user.save(getWorkflowModule().getSysadminContext());
                }

                collaborateur.setValue("SoldeConges", 0);
                collaborateur.save(getWorkflowModule().getSysadminContext());
            }
        }

    }


    ArrayList<IStorageResource> getFiches(){
        ArrayList<IStorageResource> fiches = null;
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4,project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"FicheCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            fiches =(ArrayList<IStorageResource>) controller.evaluate(definition);
        }catch (Exception e){
            e.printStackTrace();
        }

        return fiches;
    }
}
