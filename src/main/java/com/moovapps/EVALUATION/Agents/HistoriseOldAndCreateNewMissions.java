package com.moovapps.EVALUATION.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HistoriseOldAndCreateNewMissions extends BaseAgent {
//OLD
    @Override
    protected void execute() {
        ArrayList<IWorkflowInstance> campagnes = getCloturedCampagnes();
        if(campagnes==null ||campagnes.isEmpty()){
            return;
        }
        for(IWorkflowInstance campagne : campagnes){
            ArrayList<IWorkflowInstance> evals =(ArrayList<IWorkflowInstance>) campagne.getLinkedWorkflowInstances("Evaluations");
            if(evals == null || evals.isEmpty()){
                continue;
            }
            for(IWorkflowInstance eval : evals){
                handleMissions(eval);
            }
        }
    }

    ArrayList<IWorkflowInstance> getCloturedCampagnes (){

        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "EvalutionCollaborateur", ICatalog.IType.NORMAL, project);
            IWorkflow campagneWorkflow = getWorkflowModule().getWorkflow(sysContext,catalog,"ApplicationCompagne_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("EtatDeLaCampagne","Campagne clôturée");
            return (ArrayList<IWorkflowInstance>) controller.evaluate(campagneWorkflow);

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void handleMissions(IWorkflowInstance eval) {
        IStorageResource ficheCollaborateur = (IStorageResource) eval.getValue("FicheCollaborateur");
        if(ficheCollaborateur==null){
            return;
        }
        ArrayList<ILinkedResource> missions = (ArrayList<ILinkedResource>) eval.getLinkedResources("Missions2");
        if(missions!=null && !missions.isEmpty()){
            for(ILinkedResource mission : missions){
                String missionID = (String) mission.getValue("MissionRessourceID");
                boolean isActif = (boolean) mission.getValue("StatutActifInactif");
                if(!isActif){
                    historise(mission);
                }else{
                    if(missionID==null || missionID.isEmpty()){
                        creer(ficheCollaborateur,mission);
                    }
                }

            }
        }
    }

    private void creer(IStorageResource collaborateur, ILinkedResource mission) {

        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"MissionCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(context,IResource.class);
            controller.addEqualsConstraint("IDLinkedRessource",mission.getId().toString());
            ArrayList<IStorageResource> missionsActive = (ArrayList<IStorageResource>) controller.evaluate(definition);
            if(missionsActive==null || missionsActive.isEmpty()){
                IStorageResource missionsCollaborateurs = getWorkflowModule().createStorageResource(context,definition,"");
                missionsCollaborateurs.setValue("Collaborateur",collaborateur);
                missionsCollaborateurs.setValue("sys_Title",mission.getValue("Mission"));
                missionsCollaborateurs.setValue("Details",mission.getValue("Details"));
                missionsCollaborateurs.setValue("DateAffectation",new Date());
                missionsCollaborateurs.setValue("IDLinkedRessource",mission.getId().toString());
                missionsCollaborateurs.save(getWorkflowModule().getSysadminContext());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void historise(ILinkedResource mission) {
       try{
           IContext context = getWorkflowModule().getSysadminContext();
           IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
           IProject projet = getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
           ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
           IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"HistoriqueMissionsCollaborateurs");
           IViewController controller = getWorkflowModule().getViewController(context,IResource.class);
           controller.addEqualsConstraint("IDLinkedRessource",mission.getId().toString());
           ArrayList<IStorageResource> missionsInactive = (ArrayList<IStorageResource>) controller.evaluate(definition);
           if(missionsInactive==null || missionsInactive.isEmpty()){
               IStorageResource historiqueMission = getWorkflowModule().createStorageResource(context,definition,"");
               historiqueMission.setValue("Collaborateur",mission.getParentInstance().getValue("FicheCollaborateur"));
               historiqueMission.setValue("Mission",mission.getValue("Mission"));
               historiqueMission.setValue("Details",mission.getValue("Details"));
               historiqueMission.setValue("DateAffectation",mission.getValue("DateAffectation"));
               historiqueMission.setValue("DateFin",new Date());
               historiqueMission.setValue("Duree",getDurationFromTwoDate((Date)mission.getValue("DateAffectation"),new Date()));
               historiqueMission.save(getWorkflowModule().getSysadminContext());
               String missionID = (String) mission.getValue("MissionRessourceID");
               IStorageResource missionStorage = (IStorageResource) getWorkflowModule().getResource(missionID);
               missionStorage.delete(getWorkflowModule().getSysadminContext());
           }

       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public static String getDurationFromTwoDate(Date start,Date end){
        if(start==null || end==null)return "";
        long difference_In_Milliseconds = ((end.getTime() - start.getTime()));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(difference_In_Milliseconds);
        int years = c.get(Calendar.YEAR) - 1970;
        int months = c.get(Calendar.MONTH);
        int days = c.get(Calendar.DAY_OF_MONTH);
        return years + " ans " + months + " mois " + days + " jours";
    }
}
