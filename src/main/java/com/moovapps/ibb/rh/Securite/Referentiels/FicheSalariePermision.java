package com.moovapps.ibb.rh.Securite.Referentiels;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

import java.util.ArrayList;

public class FicheSalariePermision extends AddSpecificPermission {
    @Override
    public ISecurityController getSecuriteController() {
        try {
            return getWorkflowModule().getSecurityController(getDocument().getResource());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void addManagementGroupes(ISecurityController securityController) {
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization BPOGroupeOrganization = getDirectoryModule().getOrganization(context, "BPOGroupes");
            IGroup adminGroupe = getDirectoryModule().getGroup(context, BPOGroupeOrganization,"BPO");
            IGroup DRHGroupe = getDirectoryModule().getGroup(context, BPOGroupeOrganization,"DRH");
            IGroup RHGroupe = getDirectoryModule().getGroup(context, BPOGroupeOrganization,"RH");
            //IGroup ResponsableDevGroupe = getDirectoryModule().getGroup(context, BPOGroupeOrganization,"ResponsableSDEV");

            addPermission(securityController, adminGroupe, "write");
            addPermission(securityController, DRHGroupe, "write");
            addPermission(securityController, RHGroupe, "write");
           // addPermission(securityController, ResponsableDevGroupe, "read");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void addSpecificPermissions(IResource instance ,ISecurityController securityController) {
        try {
            IUser salarie = (IUser) getWorkflowInstance().getValue("Salarie");
            if(salarie!=null){
                addPermission(securityController, salarie, "write");
            }
            if(salarie.getHierarchicalManager()!=null){
                addPermission(securityController, salarie.getHierarchicalManager(), "read");

            }
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = salarie.getOrganization();

            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RH");
            if(RHClientGroupe!=null){
                addPermission(securityController, RHClientGroupe, "write");
            }

            /*IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
            if(ResponsableDevClientGroupe!=null){
                addPermission(securityController, ResponsableDevClientGroupe, "read");
            }*/

            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RHSociete");
            if(RHSocieteGroupe!=null){
                addPermission(securityController, RHSocieteGroupe, "write");
            }

            /*IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDevSociete");
            if(ResponsableDevSocieteGroupe!=null){
                addPermission(securityController, ResponsableDevSocieteGroupe, "read");
            }*/

        }catch (Exception e){
            e.printStackTrace();
        }
        super.addSpecificPermissions(instance,securityController);
    }




}
