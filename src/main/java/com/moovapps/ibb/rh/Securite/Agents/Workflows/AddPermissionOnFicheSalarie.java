package com.moovapps.ibb.rh.Securite.Agents.Workflows;


import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;


public class AddPermissionOnFicheSalarie extends BaseAgent {

    @Override
    protected void execute() {
        try {
            ArrayList<IWorkflowInstance> fichesSalarie = getFichesSalarie();
            for (IWorkflowInstance ficheSalarie : fichesSalarie){

                try {
                    ISecurityController securityController = getSecuriteController(ficheSalarie);
                    breakInheritance(securityController);
                    addManagementGroupes(securityController);
                    addSpecificPermissions(ficheSalarie,securityController);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ISecurityController getSecuriteController(IWorkflowInstance workflowInstance){
        try {
            return Modules.getWorkflowModule().getSecurityController(workflowInstance);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public void breakInheritance(ISecurityController securityController){
        securityController.breakInheritance(1, new Object[] { null, "write" });
    }

    public void addManagementGroupes(ISecurityController securityController){
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization BPOGroupesOrganization = Modules.getDirectoryModule().getOrganization(context, "BPOGroupes");
            IGroup adminGroupe = Modules.getDirectoryModule().getGroup(context, BPOGroupesOrganization,"BPO");
            IGroup DRHGroupe = Modules.getDirectoryModule().getGroup(context, BPOGroupesOrganization,"DRH");
            IGroup RHGroupe = Modules.getDirectoryModule().getGroup(context, BPOGroupesOrganization,"RH");
            // IGroup ResponsableDevGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"ResponsableSDEV");

            addPermission(securityController, adminGroupe, "read");
            addPermission(securityController, DRHGroupe, "read");
            addPermission(securityController, RHGroupe, "read");
            //addPermission(securityController, ResponsableDevGroupe, "read");

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void addPermission(ISecurityController securityController, IUser user, String readWriteGrant){
        securityController.addPermission(user, new Object[] { null, readWriteGrant });
    }

    public void addPermission(ISecurityController securityController, IGroup group, String readWriteGrant){
        securityController.addPermission(group, new Object[] { null, readWriteGrant });
    }

    public void addPermission(ISecurityController securityController, ArrayList<IUser> user, String readWriteGrant){
        securityController.addPermission(user, new Object[] { null, readWriteGrant });
    }

    public void addSpecificPermissions(IResource instance , ISecurityController securityController) {
        if(instance == null || securityController == null){
            return;
        }
        try {

            IUser salarie = (IUser) instance.getValue("sys_Creator");
            if(salarie == null){
                return;
            }
            addPermission(securityController, salarie, "read");
            ArrayList<IUser> responsableRH = (ArrayList<IUser>) instance.getValue("ResponsableRH");
            if(responsableRH != null){
                addPermission(securityController, responsableRH, "read");
            }


            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = salarie.getOrganization();

            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RH");
            if(RHClientGroupe!=null){
                addPermission(securityController, RHClientGroupe, "read");
            }

         /*   IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
            if(ResponsableDevClientGroupe!=null){
                addPermission(securityController, ResponsableDevClientGroupe, "read");
            }*/

            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RHSociete");
            if(RHSocieteGroupe!=null){
                addPermission(securityController, RHSocieteGroupe, "read");
            }

           /* IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDevSociete");
            if(ResponsableDevSocieteGroupe!=null){
                addPermission(securityController, ResponsableDevSocieteGroupe, "read");
            }*/


        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private ArrayList<IWorkflowInstance> getFichesSalarie() {
        ArrayList<IWorkflowInstance> collection = new ArrayList<>();
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Salarie", project);
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("sys_WorkflowContainer","FicheSalarie");

            collection = (ArrayList<IWorkflowInstance>) controller.evaluate(catalog);
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return collection;
    }
}
