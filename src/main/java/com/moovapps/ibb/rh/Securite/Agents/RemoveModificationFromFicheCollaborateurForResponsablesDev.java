package com.moovapps.ibb.rh.Securite.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;

public class RemoveModificationFromFicheCollaborateurForResponsablesDev extends BaseAgent {

    @Override
    protected void execute() {
        try{
            ArrayList<IStorageResource> fiches = getFiches();
            if(fiches!=null && !fiches.isEmpty()) {
                for (IStorageResource fiche : fiches) {
                    ISecurityController securityController =   getWorkflowModule().getSecurityController(fiche);

                    IContext context = getWorkflowModule().getSysadminContext();
                    IOrganization BPOGroupesOrganization = getDirectoryModule().getOrganization(context, "BPOGroupes");
                    IGroup ResponsableDevGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"ResponsableSDEV");
                    securityController.removePermission(ResponsableDevGroupe, new Object[] { null, "write" });
                    securityController.addPermission(ResponsableDevGroupe, new Object[] { null, "read" });

                    IUser salarie = (IUser) fiche.getValue("Salarie");

                    IOrganization organization = salarie.getOrganization();



                    IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
                    if(ResponsableDevClientGroupe!=null){
                        securityController.removePermission(ResponsableDevClientGroupe, new Object[] { null, "write" });
                        securityController.addPermission(ResponsableDevClientGroupe, new Object[] { null, "read" });

                    }



                    IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDevSociete");
                    if(ResponsableDevSocieteGroupe!=null){
                        securityController.removePermission(ResponsableDevSocieteGroupe, new Object[] { null, "write" });
                        securityController.addPermission(ResponsableDevSocieteGroupe, new Object[] { null, "read" });

                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
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
