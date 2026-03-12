package com.moovapps.ibb.rh.Securite.Workflows;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

import java.util.ArrayList;

public class MiseADispositionDocumentsPaie extends AddSpecificPermission {

    @Override
    public void breakInheritance(ISecurityController securityController) {

    }

    @Override
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

            ArrayList<IUser> RHs = (ArrayList<IUser>) instance.getValue("RH");
            if(RHs!=null && !RHs.isEmpty()){
                for(IUser rh : RHs){
                    addPermission(securityController, rh, "read");

                }
            }

            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = null;
            IStorageResource societe = (IStorageResource) instance.getValue("Societe");
            if(societe!=null){
                organization = (IOrganization) societe.getValue("Organisation");
            }
            if(organization==null)return;

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
