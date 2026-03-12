package com.moovapps.ibb.rh.Cummon.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Collection;

public class FixMatricule extends BaseAgent {
    @Override
    protected void execute() {
        Collection<? extends IUser> users = getDirectoryModule().getUsers(getWorkflowModule().getSysadminContext());
        int i = 0;
        for (IUser user: users) {
            IStorageResource userFiche = getOrCreateFicheCollaborateurByUser(user);
            user.getExtendedAttributes().setValue("Matricule", userFiche.getValue("Matricule"));
            user.save(getWorkflowModule().getSysadminContext());
            LOGGER.error(++i+"");
        }
    }

    public IStorageResource getOrCreateFicheCollaborateurByUser(IUser salarie){
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",getDirectoryModule().getOrganization(context, "DefaultOrganization"));
            ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
            IResourceDefinition resourceDefinition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("Salarie",salarie);
            if(controller.evaluate(resourceDefinition).size()>0){
                return (IStorageResource) controller.evaluate(resourceDefinition).iterator().next();
            }else {
                return getWorkflowModule().createStorageResource(context,resourceDefinition,null,null);

            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
}
