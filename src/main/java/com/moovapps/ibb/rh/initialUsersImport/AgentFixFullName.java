package com.moovapps.ibb.rh.initialUsersImport;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Collection;

public class AgentFixFullName extends BaseAgent {

    @Override
    protected void execute() {
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL",ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            Collection<IStorageResource> demandeurFiches = controller.evaluate(definition);
            if (!demandeurFiches.isEmpty()){
                for (IStorageResource salarie : demandeurFiches) {
                    salarie.setValue("NomPrenom", salarie.getValue("LastName") + " " +  salarie.getValue("FirstName"));
                    salarie.save("NomPrenom");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
