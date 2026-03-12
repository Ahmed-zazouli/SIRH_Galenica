package com.moovapps.ibb.rh.Securite.Workflows;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

import java.util.ArrayList;

public class SeanceFormationParParticipant extends AddSpecificPermission {


    @Override
    public void addSpecificPermissions(IResource instance, ISecurityController securityController) {
        if(instance == null || securityController == null){
            return;
        }
        try {

            IUser salarie = (IUser) instance.getValue("sys_Creator");
            if (salarie == null) {
                return;
            }
            addPermission(securityController, salarie, "read");



            IUser participant = (IUser) instance.getValue("Participant2");
              if(participant!=null){
              addPermission(securityController, participant, "read");
              if(participant.getHierarchicalManager()!=null){

                  addPermission(securityController, participant.getHierarchicalManager(), "read");
              }

             }

            IUser validateur2 = (IUser) instance.getValue("Validateur2");
            if(validateur2!=null){
                addPermission(securityController, validateur2, "read");

            }

            //IStorageResource societe = (IStorageResource) instance.getValue("Societe");
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = null;
          //  if (societe != null) {
                organization = participant.getOrganization();
            //}

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
