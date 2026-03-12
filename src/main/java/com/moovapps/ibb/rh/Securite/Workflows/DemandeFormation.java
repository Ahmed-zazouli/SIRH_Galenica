package com.moovapps.ibb.rh.Securite.Workflows;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

import java.util.ArrayList;

public class DemandeFormation extends AddSpecificPermission {

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
            if (salarie == null) {
                return;
            }
            addPermission(securityController, salarie, "read");

            IStorageResource demandeur = (IStorageResource) instance.getValue("Demandeur");
            if(demandeur!=null){
                IUser user = (IUser) demandeur.getValue("Salarie");
                addPermission(securityController, user, "read");

            }


            ArrayList<IUser> responsableRH2 = (ArrayList<IUser>) instance.getValue("ResponsableRH");
            if(responsableRH2!=null && !responsableRH2.isEmpty()){
                for(IUser user : responsableRH2){
                    addPermission(securityController, user, "read");

                }
            }

            ArrayList<IUser> ResponsableDev = (ArrayList<IUser>) instance.getValue("ResponsableDev");
            if(ResponsableDev!=null && !ResponsableDev.isEmpty()){
                for(IUser user : ResponsableDev){
                    addPermission(securityController, user, "read");

                }
            }

            IStorageResource societe = (IStorageResource) instance.getValue("Societe");
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = null;
            if (societe != null) {
                organization = (IOrganization) societe.getValue("Organisation");
            }

            if (organization == null) return;
            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "RH");
            if (RHClientGroupe != null) {
                addPermission(securityController, RHClientGroupe, "read");
            }

            IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "ResponsablesDev");
            if (ResponsableDevClientGroupe != null) {
                addPermission(securityController, ResponsableDevClientGroupe, "read");
            }

            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "RHSociete");
            if (RHSocieteGroupe != null) {
                addPermission(securityController, RHSocieteGroupe, "read");
            }

            IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "ResponsablesDevSociete");
            if (ResponsableDevSocieteGroupe != null) {
                addPermission(securityController, ResponsableDevSocieteGroupe, "read");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
