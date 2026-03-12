package com.moovapps.ibb.rh.Securite.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;

public class AddPersmissionOnFicheCollaborateur extends BaseAgent {

    @Override
    protected void execute() {
        try{
            ArrayList<IStorageResource> fiches = getFiches();
            if(fiches!=null && !fiches.isEmpty()){
                for(IStorageResource fiche : fiches){
                    ISecurityController securityController =   getWorkflowModule().getSecurityController(fiche);

                    IContext context = getWorkflowModule().getSysadminContext();
                    IOrganization BPOGroupesOrganization = getDirectoryModule().getOrganization(context, "BPOGroupes");
                    IGroup adminGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"BPO");
                    IGroup DRHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"DRH");
                    IGroup RHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"RH");
                    //IGroup ResponsableDevGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"ResponsableSDEV");

                    addPermission(securityController, adminGroupe, "write");
                    addPermission(securityController, DRHGroupe, "write");
                    addPermission(securityController, RHGroupe, "write");
                   // addPermission(securityController, ResponsableDevGroupe, "read");



                    IUser salarie = (IUser) fiche.getValue("Salarie");
                    addPermission(securityController, salarie, "write");
                    if(salarie.getHierarchicalManager()!=null){
                        addPermission(securityController, salarie.getHierarchicalManager(), "read");
                    }
                    IOrganization organization = salarie.getOrganization();

                    IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RH");
                    if(RHClientGroupe!=null){
                        addPermission(securityController, RHClientGroupe, "write");
                    }

                  /*  IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
                    if(ResponsableDevClientGroupe!=null){
                        addPermission(securityController, ResponsableDevClientGroupe, "read");
                    }**/

                    IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RHSociete");
                    if(RHSocieteGroupe!=null){
                        addPermission(securityController, RHSocieteGroupe, "write");
                    }

                   /* IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDevSociete");
                    if(ResponsableDevSocieteGroupe!=null){
                        addPermission(securityController, ResponsableDevSocieteGroupe, "read");
                    }*/
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void addPermission(ISecurityController securityController, IUser user, String readWriteGrant){
        securityController.addPermission(user, new Object[] { null, readWriteGrant });
        /*if(users != null && !users.isEmpty()){
            for (IUser user : users) {
            }
        }

        if(groups != null && !groups.isEmpty()){
            for (IGroup group : groups) {
                securityController.addPermission(group, new Object[] { null, "write" });
            }
        }*/
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

    public void addPermission(ISecurityController securityController, IGroup group, String readWriteGrant){
        securityController.addPermission(group, new Object[] { null, readWriteGrant });
    }
}
