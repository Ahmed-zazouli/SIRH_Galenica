package com.moovapps.ibb.rh.Securite.Workflows;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

import java.util.ArrayList;

public class AvancesPermission extends AddSpecificPermission {
    @Override
    public void breakInheritance(ISecurityController securityController) {
        securityController.breakInheritance(1, new Object[] { null, "write" });
    }
    @Override
    public void addManagementGroupes(ISecurityController securityController){
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization BPOGroupesOrganization = getDirectoryModule().getOrganization(context, "BPOGroupes");
            IGroup adminGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"BPO");
            IGroup DRHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"DRH");
            IGroup RHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"RH");
          //  IGroup ResponsableDevGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"ResponsableSDEV");

            addPermission(securityController, adminGroupe, "read");
            addPermission(securityController, DRHGroupe, "read");
            addPermission(securityController, RHGroupe, "read");
          //  addPermission(securityController, ResponsableDevGroupe, "read");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void addSpecificPermissions(IResource instance , ISecurityController securityController) {

        if(instance == null || securityController == null){
            return;
        }
        try {

            IUser salarie = (IUser) instance.getValue("Beneficiaire");
            if(salarie == null){
                return;
            }
            addPermission(securityController, salarie, "read");

            /*IUser N1 = salarie.getHierarchicalManager();
            if(N1 != null){
                addPermission(securityController, N1, "read");
            }*/

            ArrayList<IUser> ResponsableRH = (ArrayList<IUser>) instance.getValue("ResponsableRH");
            if(ResponsableRH != null && !ResponsableRH.isEmpty()){
                addPermission(securityController, ResponsableRH, "read");
            }

//            IUser SuperieurHierarchique = (IUser) instance.getValue("SuperieurHierarchique") ;
//            if(SuperieurHierarchique != null ){
//                addPermission(securityController, SuperieurHierarchique, "read");
//            }

            IContext context = getWorkflowModule().getSysadminContext();
            //IOrganization monOrganization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IOrganization organization = salarie.getOrganization();
            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RH");
            if(RHClientGroupe!=null){
               addPermission(securityController, RHClientGroupe, "read");
            }

          /*  IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
            if(ResponsableDevClientGroupe!=null){
                addPermission(securityController, ResponsableDevClientGroupe, "read");
            }*/

            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RHSociete");
            if(RHSocieteGroupe!=null){
                addPermission(securityController, RHSocieteGroupe, "read");
            }

            /*IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDevSociete");
            if(ResponsableDevSocieteGroupe!=null){
                addPermission(securityController, ResponsableDevSocieteGroupe, "read");
            }*/


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
