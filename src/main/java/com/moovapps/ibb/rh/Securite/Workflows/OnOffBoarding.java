package com.moovapps.ibb.rh.Securite.Workflows;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

import java.util.ArrayList;

public class OnOffBoarding extends AddSpecificPermission {
    @Override
    public void breakInheritance(ISecurityController securityController) {

    }

    @Override
    public void addSpecificPermissions(IResource instance , ISecurityController securityController) {
        if(instance == null || securityController == null){
            return;
        }
        try {

            IUser salarie = (IUser) instance.getValue("Collaborateur");
            if(salarie == null){
                return;
            }
            addPermission(securityController, salarie, "read");

            IUser responsableHierarchique = (IUser) instance.getValue("RespN1");
            if(responsableHierarchique!=null){
                addPermission(securityController, responsableHierarchique, "read");
            }

            ArrayList<IUser>  responsablesRealisation2 = ( ArrayList<IUser>) instance.getValue("ResponsablesRealisation2");
            if(responsablesRealisation2!=null && !responsablesRealisation2.isEmpty()){
               for(IUser user : responsablesRealisation2){
                   addPermission(securityController, user, "read");

               }

            }

            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = salarie.getOrganization();

            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RH");
            if(RHClientGroupe!=null){
                addPermission(securityController, RHClientGroupe, "read");
            }

            IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
            if(ResponsableDevClientGroupe!=null){
                addPermission(securityController, ResponsableDevClientGroupe, "read");
            }

            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RHSociete");
            if(RHSocieteGroupe!=null){
                addPermission(securityController, RHSocieteGroupe, "read");
            }

            IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDevSociete");
            if(ResponsableDevSocieteGroupe!=null){
                addPermission(securityController, ResponsableDevSocieteGroupe, "read");
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
