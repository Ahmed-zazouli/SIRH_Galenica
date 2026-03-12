package com.moovapps.ibb.rh.Securite.Agents.Workflows;


import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;


public class AddPermissionOnFicheAttestation extends BaseAgent {

    @Override
    protected void execute() {
        try {
            ArrayList<IWorkflowInstance> attestations = getAttestations();
            for (IWorkflowInstance attestation : attestations){

                try {
                    ISecurityController securityController = getSecuriteController(attestation);
                    breakInheritance(securityController);
                    addManagementGroupes(securityController);
                    addSpecificPermissions(attestation,securityController);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ISecurityController getSecuriteController(IWorkflowInstance workflowInstance){
        try {
            return Modules.getWorkflowModule().getSecurityController(workflowInstance);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public void breakInheritance(ISecurityController securityController){
        securityController.breakInheritance(1, new Object[] { null, "write" });
    }

    public void addManagementGroupes(ISecurityController securityController){
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization BPOGroupesOrganization = Modules.getDirectoryModule().getOrganization(context, "BPOGroupes");
            IGroup adminGroupe = Modules.getDirectoryModule().getGroup(context, BPOGroupesOrganization,"BPO");
            IGroup DRHGroupe = Modules.getDirectoryModule().getGroup(context, BPOGroupesOrganization,"DRH");
            IGroup RHGroupe = Modules.getDirectoryModule().getGroup(context, BPOGroupesOrganization,"RH");
            // IGroup ResponsableDevGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"ResponsableSDEV");

            addPermission(securityController, adminGroupe, "read");
            addPermission(securityController, DRHGroupe, "read");
            addPermission(securityController, RHGroupe, "read");
            //addPermission(securityController, ResponsableDevGroupe, "read");

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

    public void addSpecificPermissions(IResource instance , ISecurityController securityController) {
        if(instance==null || securityController==null){
            return;
        }
        try {
            IUser salarie = (IUser) instance.getValue("Demandeur2");
            if(salarie == null){
                return;
            }
            addPermission(securityController, salarie, "read");

            /*IUser N1 = salarie.getHierarchicalManager();
            if(N1 != null){
                addPermission(securityController, N1, "read");
            }*/

            ArrayList<IUser>  validateur = (ArrayList<IUser>) instance.getValue("SocieteResponsableRH");
            if(validateur != null && !validateur.isEmpty()){
                addPermission(securityController, validateur, "read");
            }

            IContext context = getWorkflowModule().getSysadminContext();
            //IOrganization monOrganization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IOrganization organization = salarie.getOrganization();
            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RH");
            if(RHClientGroupe!=null){
                addPermission(securityController, RHClientGroupe, "read");
            }
            //IStorageResource attestationAdministrative = (IStorageResource) instance.getValue("AttestationAdministrative");
            /*if(attestationAdministrative!=null){
                Boolean accesResponsableSDEV = (Boolean) attestationAdministrative.getValue("AccesResponsableSDEV");
                if(accesResponsableSDEV!=null && accesResponsableSDEV){

                    IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
                    if(ResponsableDevClientGroupe!=null){
                        addPermission(securityController, ResponsableDevClientGroupe, "read");
                    }
                    IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDevSociete");
                    if(ResponsableDevSocieteGroupe!=null){
                        addPermission(securityController, ResponsableDevSocieteGroupe, "read");
                    }
                }

                }*/


            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RHSociete");
            if(RHSocieteGroupe!=null){
                addPermission(securityController, RHSocieteGroupe, "read");
            }




        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private ArrayList<IWorkflowInstance> getAttestations() {
        ArrayList<IWorkflowInstance> collection = new ArrayList<>();
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "AttestationDeSalaire", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("sys_WorkflowContainer","DOCUMENTSADMINISTRATIFSINTERNES");

            collection = (ArrayList<IWorkflowInstance>) controller.evaluate(catalog);
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return collection;
    }
}
