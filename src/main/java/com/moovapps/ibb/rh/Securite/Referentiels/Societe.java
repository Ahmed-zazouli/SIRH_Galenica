package com.moovapps.ibb.rh.Securite.Referentiels;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

public class Societe extends AddSpecificPermission {

    @Override
    public ISecurityController getSecuriteController() {
        try {
            return getWorkflowModule().getSecurityController(getDocument().getResource());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void addManagementGroupes(ISecurityController securityController){
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization BPOGroupesOrganization = getDirectoryModule().getOrganization(context, "BPOGroupes");
            IGroup adminGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"BPO");
            IGroup DRHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"DRH");
            IGroup RHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"RH");

            addPermission(securityController, adminGroupe, "write");
            addPermission(securityController, DRHGroupe, "write");
            addPermission(securityController, RHGroupe, "write");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void addSpecificPermissions(IResource instance ,ISecurityController securityController) {
        try {
            /*IUser salarie = (IUser) getWorkflowInstance().getValue("Salarie");
            if(salarie!=null){
                addPermission(securityController, salarie, "write");
            }
            if(salarie.getHierarchicalManager()!=null){
                addPermission(securityController, salarie.getHierarchicalManager(), "read");

            }*/
            IContext context = getWorkflowModule().getSysadminContext();
            //IOrganization organization = salarie.getOrganization();
            IOrganization organization = (IOrganization) getWorkflowInstance().getValue("Organisation");

            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RH");
            if(RHClientGroupe!=null){
                addPermission(securityController, RHClientGroupe, "write");
            }



            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RHSociete");
            if(RHSocieteGroupe!=null){
                addPermission(securityController, RHSocieteGroupe, "write");
            }



        }catch (Exception e){
            e.printStackTrace();
        }
        super.addSpecificPermissions(instance,securityController);
    }

}
