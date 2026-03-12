package com.moovapps.ibb.rh.Securite.Referentiels;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

public class HistoriqueDesDepart extends AddSpecificPermission {

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
    public void addSpecificPermissions(IResource instance ,ISecurityController securityController) {
        try {

            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = null;
            IUser collaborateur = (IUser) getWorkflowInstance().getValue("Collaborateur");
            if(collaborateur!=null){
                addPermission(securityController,collaborateur,"read");
            }
            // IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
            organization = collaborateur!=null?collaborateur.getOrganization():null;
            if(organization==null)return;
            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RH");
            if(RHClientGroupe!=null){
                addPermission(securityController, RHClientGroupe, "read");
            }

            /*IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
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
        super.addSpecificPermissions(instance,securityController);
    }



}
