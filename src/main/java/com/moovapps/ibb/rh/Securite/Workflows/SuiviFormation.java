package com.moovapps.ibb.rh.Securite.Workflows;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

import java.util.ArrayList;

public class SuiviFormation extends AddSpecificPermission {


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

            IUser formateurInterne = (IUser) instance.getValue("Formateur2");
            if(formateurInterne!=null){
                addPermission(securityController, formateurInterne, "read");
            }

//            IUser representantFormateur = (IUser) instance.getValue("RepresentantFormateur");
//            if(representantFormateur!=null){
//                addPermission(securityController, representantFormateur, "read");
//            }

            IUser participant = (IUser) instance.getValue("Participant2");
            if(participant!=null){
                addPermission(securityController, participant, "read");
                if(participant.getHierarchicalManager()!=null){
                    addPermission(securityController, participant.getHierarchicalManager(), "read");
                }
            }
            IUser respHierarchique = (IUser) instance.getValue("RespHierarchique");
            if(respHierarchique!=null ){

                    addPermission(securityController, respHierarchique, "read");

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

            IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,"ResponsableSDEV");
            if(ResponsableDevClientGroupe!=null){
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
