package com.moovapps.ibb.rh.Securite.Workflows;

import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Securite.commons.AddSpecificPermission;

import java.util.ArrayList;

public class EvaluationPermissions extends AddSpecificPermission {
    @Override
    public void breakInheritance(ISecurityController securityController) {

    }

    @Override
    public void addSpecificPermissions(IResource instance , ISecurityController securityController) {
        if(instance == null || securityController == null){
            return;
        }
        try {

            IUser collaborateur = (IUser) instance.getValue("CollaborateurEval");
            if(collaborateur == null){
                return;
            }
            addPermission(securityController, collaborateur, "read");

            /*IUser Resp = collaborateur.getHierarchicalManager();
            if(Resp != null){
                addPermission(securityController, Resp, "read");
                IUser N2 = Resp.getHierarchicalManager();
                if(N2 != null){
                    addPermission(securityController, N2, "read");
                }
            }*/

            IUser N1 = (IUser) instance.getValue("ResponsableHierarchique");
            if(N1 != null){
                addPermission(securityController, N1, "read");
            }
            IUser NPlus2 = (IUser) instance.getValue("NPlus2");
            if(NPlus2 != null){
                addPermission(securityController, N1, "read");
            }
            IUser Evaluateur = (IUser) instance.getValue("Evaluateur");
            if(Evaluateur != null){
                addPermission(securityController, Evaluateur, "read");
            }

            ArrayList<IUser> validateur = (ArrayList<IUser>) instance.getValue("ResponsableSRH");
            if(validateur != null && !validateur.isEmpty()){
                addPermission(securityController, validateur, "read");
            }


           /* IUser N2 = (IUser) instance.getValue("NPlus2");
            if(N2 != null){
                addPermission(securityController, N2, "read");
            }*/

            IContext context = getWorkflowModule().getSysadminContext();
            //IOrganization monOrganization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IOrganization organization = collaborateur.getOrganization();
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
