package com.moovapps.ibb.rh.FicheSalarie.Agent;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;

public class AddSalarieToFichieCollaborateur extends BaseAgent {
    @Override
    protected void execute() {
        ArrayList<IStorageResource> collaborateur = new ArrayList<>();
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"FicheCollaborateur");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            collaborateur = (ArrayList<IStorageResource>) controller.evaluate(definition);


        }catch (Exception e){
            e.printStackTrace();
        }
        for (IStorageResource userReferentiel :collaborateur){
            try {
                IUser userSysteme = getWorkflowModule().getUserByLogin((String) userReferentiel.getValue("Identifiant"));
                userReferentiel.setValue("Salarie", userSysteme);
                userReferentiel.save(getWorkflowModule().getSysadminContext());
            } catch (WorkflowModuleException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
