package com.moovapps.ibb.rh.Securite.Workflows;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

import java.util.ArrayList;

public class ObjectifsAVerifiePermissions {

    public void breakInheritance(ISecurityController securityController) {

    }

    public void addSpecificPermissions(ISecurityController securityController,IWorkflowInstance instance) {
        try {
            IWorkflowModule workflowModule = Modules.getWorkflowModule();
            IDirectoryModule directoryModule = Modules.getDirectoryModule();

            IUser salarie = (IUser) instance.getValue("CollaborateurEval");
            if(salarie == null){
                return;
            }
            addPermission(securityController, salarie, "read");

            IUser salarieN1 = (IUser) instance.getValue("ResponsableHierarchique");
            if(salarieN1 != null){
                addPermission(securityController, salarieN1, "read");
            }

            /*ArrayList<IUser> validateurConge = (ArrayList<IUser>) instance.getValue("ValidateurS");
            if(validateurConge != null && !validateurConge.isEmpty()){
                addPermission(securityController, validateurConge, "read");
            }*/

            IContext context = workflowModule.getSysadminContext();
            IOrganization organization = salarie.getOrganization();

            IGroup RHClientGroupe = directoryModule.getGroup(context, organization,organization.getName()+"RH");
            if(RHClientGroupe!=null){
                addPermission(securityController, RHClientGroupe, "read");
            }

           /* IGroup ResponsableDevClientGroupe = directoryModule.getGroup(context, organization,organization.getName()+"ResponsablesDev");
            if(ResponsableDevClientGroupe!=null){
                addPermission(securityController, ResponsableDevClientGroupe, "read");
            }*/

            IGroup RHSocieteGroupe = directoryModule.getGroup(context, organization,organization.getName()+"RHSociete");
            if(RHSocieteGroupe!=null){
                addPermission(securityController, RHSocieteGroupe, "read");
            }

          /*  IGroup ResponsableDevSocieteGroupe = directoryModule.getGroup(context, organization,organization.getName()+"ResponsablesDevSociete");
            if(ResponsableDevSocieteGroupe!=null){
                addPermission(securityController, ResponsableDevSocieteGroupe, "read");
            }*/


        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public ISecurityController getSecuriteController(IWorkflowInstance instance){
        try {
            return Modules.getWorkflowModule().getSecurityController(instance);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public void buildPermissions(IWorkflowInstance instance) {
        if(instance == null){
            return;
        }
        try {
            ISecurityController securityController = getSecuriteController(instance);
            breakInheritance(securityController);
            addManagementGroupes(securityController);
            addSpecificPermissions(securityController,instance);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public void addManagementGroupes(ISecurityController securityController){
        try {
            IWorkflowModule workflowModule = Modules.getWorkflowModule();
            IDirectoryModule directoryModule = Modules.getDirectoryModule();
            IContext context = workflowModule.getSysadminContext();
            IOrganization BPOGroupesOrganization = directoryModule.getOrganization(context, "BPOGroupes");
            IGroup adminGroupe = directoryModule.getGroup(context, BPOGroupesOrganization,"BPO");
            IGroup DRHGroupe = directoryModule.getGroup(context, BPOGroupesOrganization,"DRH");
            IGroup RHGroupe = directoryModule.getGroup(context, BPOGroupesOrganization,"RH");
           // IGroup ResponsableDevGroupe = directoryModule.getGroup(context, BPOGroupesOrganization,"ResponsableSDEV");

            addPermission(securityController, adminGroupe, "read");
            addPermission(securityController, DRHGroupe, "read");
            addPermission(securityController, RHGroupe, "read");
         //   addPermission(securityController, ResponsableDevGroupe, "read");

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void addPermission(ISecurityController securityController, IUser user, String readWriteGrant){
        securityController.addPermission(user, new Object[] { null, readWriteGrant });
    }

    public void addPermission(ISecurityController securityController, IGroup group, String readWriteGrant){
        securityController.addPermission(group, new Object[] { null, readWriteGrant });
    }

    public void addPermission(ISecurityController securityController, ArrayList<IUser> user, String readWriteGrant){
        securityController.addPermission(user, new Object[] { null, readWriteGrant });
    }
}
