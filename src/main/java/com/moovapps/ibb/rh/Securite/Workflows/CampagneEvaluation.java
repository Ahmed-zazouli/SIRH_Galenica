package com.moovapps.ibb.rh.Securite.Workflows;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

public class CampagneEvaluation extends AddSpecificPermission {
    @Override
    public void breakInheritance(ISecurityController securityController) {

    }

    @Override
    public void addSpecificPermissions(IResource instance , ISecurityController securityController) {
        if(instance == null || securityController == null){
            return;
        }
        try {

            IUser createurCampagne = (IUser) instance.getValue("sys_Creator");
            if(createurCampagne == null){
                return;
            }
            addPermission(securityController, createurCampagne, "read");



            IContext context = getWorkflowModule().getSysadminContext();
            IStorageResource societe = (IStorageResource) instance.getValue("Societe");
            if(societe==null)return;
            IOrganization organization = (IOrganization) societe.getValue("Organisation");
            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RH");
            if(RHClientGroupe!=null){
                addPermission(securityController, RHClientGroupe, "read");
            }

           /* IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
            if(ResponsableDevClientGroupe!=null){
                addPermission(securityController, ResponsableDevClientGroupe, "read");
            }*/

            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RHSociete");
            if(RHSocieteGroupe!=null){
                addPermission(securityController, RHSocieteGroupe, "read");
            }

          /*  IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDevSociete");
            if(ResponsableDevSocieteGroupe!=null){
                addPermission(securityController, ResponsableDevSocieteGroupe, "read");
            }

            */



        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
