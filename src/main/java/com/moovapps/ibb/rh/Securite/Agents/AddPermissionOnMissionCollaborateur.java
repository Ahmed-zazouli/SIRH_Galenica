


package com.moovapps.ibb.rh.Securite.Agents;

        import com.axemble.vdoc.sdk.agent.base.BaseAgent;
        import com.axemble.vdoc.sdk.interfaces.*;

        import java.util.ArrayList;

public class AddPermissionOnMissionCollaborateur extends BaseAgent {

    @Override
    protected void execute() {
        try{
            ArrayList<IStorageResource> competencesCollaborateur = getCompetencesCollaborateur();
            if(competencesCollaborateur!=null && !competencesCollaborateur.isEmpty()){
                for(IStorageResource competenceCollaborateur : competencesCollaborateur){
                    ISecurityController securityController = getWorkflowModule().getSecurityController(competenceCollaborateur);

                    IContext context = getWorkflowModule().getSysadminContext();

                    IOrganization BPOGroupesOrganization = getDirectoryModule().getOrganization(context, "BPOGroupes");
                    IGroup adminGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"BPO");
                    IGroup DRHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"DRH");
                    IGroup RHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"RH");
                    IGroup ResponsableDevGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"ResponsableSDEV");

                    addPermission(securityController, adminGroupe, "read");
                    addPermission(securityController, DRHGroupe, "read");
                    addPermission(securityController, RHGroupe, "read");
                    addPermission(securityController, ResponsableDevGroupe, "write");

                    IOrganization organization = null;
                    IStorageResource fiche = (IStorageResource) competenceCollaborateur.getValue("Collaborateur");
                    IUser collaborateur = fiche!=null?(IUser) fiche.getValue ("Salarie"):null;;
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

                    IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDev");
                    if(ResponsableDevClientGroupe!=null){
                        addPermission(securityController, ResponsableDevClientGroupe, "write");
                    }

                    IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"RHSociete");
                    if(RHSocieteGroupe!=null){
                        addPermission(securityController, RHSocieteGroupe, "read");
                    }

                    IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization,organization.getName()+"ResponsablesDevSociete");
                    if(ResponsableDevSocieteGroupe!=null){
                        addPermission(securityController, ResponsableDevSocieteGroupe, "write");
                    }

                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    ArrayList<IStorageResource> getCompetencesCollaborateur(){
        ArrayList<IStorageResource> fiches = null;
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4,project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"MissionCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            fiches =(ArrayList<IStorageResource>) controller.evaluate(definition);
        }catch (Exception e){
            e.printStackTrace();
        }

        return fiches;
    }

    public void addPermission(ISecurityController securityController, IUser user, String readWriteGrant){
        securityController.addPermission(user, new Object[] { null, readWriteGrant });
    }

    public void addPermission(ISecurityController securityController, IGroup group, String readWriteGrant){
        securityController.addPermission(group, new Object[] { null, readWriteGrant });
    }
}



