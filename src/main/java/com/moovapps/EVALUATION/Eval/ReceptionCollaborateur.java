package com.moovapps.EVALUATION.Eval;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ReceptionCollaborateur extends BaseDocumentExtension {
    @Override
    public boolean onBeforeLoad() {

        return super.onBeforeLoad();
    }

    @Override
    public boolean onAfterLoad() {
        SetN2ForCollaborateur();
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("ConfirmerReception")){
            setNextEvalObjectifs(getWorkflowInstance());
          //  handleMissions(getWorkflowInstance());
        }
        return super.onBeforeSubmit(action);
    }

    private void SetN2ForCollaborateur(){
        IUser collaborateur = (IUser) getWorkflowInstance().getValue("CollaborateurEval");
        if(collaborateur!=null){
            IUser n2 =collaborateur.getHierarchicalManager()!=null?  collaborateur.getHierarchicalManager().getHierarchicalManager():null;
            if(n2!=null){
                getWorkflowInstance().setValue("NPlus2",n2);
            }/*else{
                getResourceController().alert("Merci de choisir un N+2 pour ce collaborateur");
            }*/
        }
    }

    private void setNextEvalObjectifs(IWorkflowInstance eval) {
        IUser collaborateur = (IUser) eval.getValue("CollaborateurEval");
        IStorageResource exerciceEvaluation = (IStorageResource) eval.getValue("ExerciceEvaluation");
        Integer exerciceEval = exerciceEvaluation!=null?((Number) exerciceEvaluation.getValue("Annee")).intValue():null;
      //  Integer exerciceEval = ((Number) eval.getValue("ExerciceEvaluation")).intValue();
        if(collaborateur==null || exerciceEval ==null){
            return;
        }
        ArrayList<ILinkedResource> fixObj = (ArrayList<ILinkedResource>) eval.getLinkedResources("ObjFix");
        if(fixObj!=null && !fixObj.isEmpty()){
            for(ILinkedResource obj : fixObj){
                if(obj.getValue("DecisionN1").equals("Validé")){
                    createObjectif(collaborateur,exerciceEval+1,obj);
                }

            }
        }
    }

    private void createObjectif(IUser collaborateur, int exercice, ILinkedResource obj) {
        try{
            String id = obj.getId().toString();
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition objectifsDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "ObjectifsDeLAnnee");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("LinkedResourceIDCreatedFrom",id);
            ArrayList<IStorageResource> objectifsResource = (ArrayList<IStorageResource>) controller.evaluate(objectifsDefinition);
            IStorageResource objectifResource = null;
            if(objectifsResource==null || objectifsResource.isEmpty()){
                objectifResource = getWorkflowModule().createStorageResource(sysContext,objectifsDefinition,"");
            }else{
                objectifResource = objectifsResource.iterator().next();
            }

            objectifResource.setValue("Annee",exercice);
            objectifResource.setValue("Collaborateur",collaborateur);
            objectifResource.setValue("Objectifs",obj.getValue("Objectif"));
            objectifResource.setValue("Importance",obj.getValue("Importance"));
            objectifResource.setValue("EcheancePrevue",obj.getValue("EcheancePrevue"));
            objectifResource.setValue("LinkedResourceIDCreatedFrom",obj.getId().toString());
            objectifResource.save(getWorkflowModule().getSysadminContext());
        }catch (Exception e){
            e.printStackTrace();
        }
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
