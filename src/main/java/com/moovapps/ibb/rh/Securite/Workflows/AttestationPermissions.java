package com.moovapps.ibb.rh.Securite.Workflows;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

import java.util.ArrayList;

public class AttestationPermissions extends AddSpecificPermission {

    @Override
    public void breakInheritance(ISecurityController securityController) {
        securityController.breakInheritance(1, new Object[] { null, "write" });

    }

    @Override
    public void addManagementGroupes(ISecurityController securityController) {
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
            //addPermission(securityController, ResponsableDevGroupe, "read");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void addSpecificPermissions(IResource instance , ISecurityController securityController) {
        if(instance==null || securityController==null){
            return;
        }
        try {
            IUser salarie = (IUser) instance.getValue("Demandeur2");
            if(salarie == null){
                return;
            }
            addPermission(securityController, salarie, "read");

            /*IUser N1 = salarie.getHierarchicalManager();
            if(N1 != null){
                addPermission(securityController, N1, "read");
            }*/

           ArrayList<IUser>  validateur = (ArrayList<IUser>) instance.getValue("SocieteResponsableRH") ;
            if(validateur != null && !validateur.isEmpty()){
                addPermission(securityController, validateur, "read");
            }

            IContext context = getWorkflowModule().getSysadminContext();
            //IOrganization monOrganization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IOrganization organization = salarie.getOrganization();
            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RH");
            if(RHClientGroupe!=null){
                addPermission(securityController, RHClientGroupe, "read");
            }
            //IStorageResource attestationAdministrative = (IStorageResource) instance.getValue("AttestationAdministrative");
            /*if(attestationAdministrative!=null){
                Boolean accesResponsableSDEV = (Boolean) attestationAdministrative.getValue("AccesResponsableSDEV");
                if(accesResponsableSDEV!=null && accesResponsableSDEV){

                    IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
                    if(ResponsableDevClientGroupe!=null){
                        addPermission(securityController, ResponsableDevClientGroupe, "read");
                    }
                    IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDevSociete");
                    if(ResponsableDevSocieteGroupe!=null){
                        addPermission(securityController, ResponsableDevSocieteGroupe, "read");
                    }
                }

                }*/


            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RHSociete");
            if(RHSocieteGroupe!=null){
                addPermission(securityController, RHSocieteGroupe, "read");
            }




        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
