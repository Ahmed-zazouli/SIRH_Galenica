

package com.moovapps.ibb.rh.Securite.Referentiels;

        import com.axemble.vdoc.sdk.interfaces.*;
        import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

public class CongeExeptionnelle extends AddSpecificPermission {

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
           // IGroup ResponsableDevGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"ResponsableSDEV");

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

            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = null;
            IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
            organization = societe!=null?(IOrganization) societe.getValue("Organisation"):null;
            if(organization==null)return;
            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RH");
            if(RHClientGroupe!=null){
                addPermission(securityController, RHClientGroupe, "write");
            }

            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RHSociete");
            if(RHSocieteGroupe!=null){
                addPermission(securityController, RHSocieteGroupe, "write");
            }

           /* IGroup ResponsablesDevSocieteGroupe = getDirectoryModule().getGroup(context, organization,"ResponsableDevSociete");
            if(ResponsablesDevSocieteGroupe!=null){
                addPermission(securityController, ResponsablesDevSocieteGroupe, "read");
            }*/

        }catch (Exception e){
            e.printStackTrace();
        }
        super.addSpecificPermissions(instance,securityController);
    }



}

