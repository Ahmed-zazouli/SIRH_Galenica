package com.moovapps.ibb.rh.Securite.Workflows;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

import java.util.ArrayList;

public class CongesPermissions extends AddSpecificPermission {
    @Override
    public void breakInheritance(ISecurityController securityController) {
        securityController.breakInheritance(1, new Object[] { null, "write" });
    }

    @Override
    public void addSpecificPermissions(IResource instance,ISecurityController securityController) {
        if(instance == null || securityController == null ){
            return;
        }
        try {

            IUser salarie = (IUser) instance.getValue("Demandeur");
            if(salarie == null){
                return;
            }
            addPermission(securityController, salarie, "read");

           /* IUser N1 = salarie.getHierarchicalManager();
            if(N1 != null){
                addPermission(securityController, N1, "read");
            }*/

            IUser validateurConge = (IUser) instance.getValue("ValidateurConge");
            if(validateurConge != null ){
                addPermission(securityController, validateurConge, "read");
            }

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

           /* IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
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
}
