package com.moovapps.Reprise.Helpers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;

import java.util.ArrayList;

public class ClassListHelper {
    public ArrayList<Class<?>> classList ;
    ArrayList<JSONObject> excelAnomaliesalertAnomalisUser = null;
    public ClassListHelper(ArrayList<JSONObject> excelAnomaliesalertAnomalisUser){
        this.excelAnomaliesalertAnomalisUser = excelAnomaliesalertAnomalisUser;
    }
    public ClassListHelper() {
        classList = new ArrayList<>();
        classList.add(Pole.class);
        classList.add(Societe.class);
        classList.add(Departement.class);
        classList.add(Activite.class);
        classList.add(Service.class);
        classList.add(Metier.class);
        classList.add(Fonction.class);
        classList.add(Grade.class);
        classList.add(Circuit.class);
        classList.add(Profil.class);
        classList.add(LienParente.class);
        classList.add(Etudes.class);
        classList.add(Domaine.class);
        classList.add(FamilleCompetence.class);
        classList.add(SousFamilleCompetence.class);
        classList.add(Competence.class);
        classList.add(NiveauCompetence.class);
        classList.add(CompetenceCollaborateur.class);
        classList.add(CompetenceFonction.class);
        classList.add(MissionCollaborateur.class);
        classList.add(MissionFonction.class);
        //here

    }
    public void addAnomalie(IWorkflowInstance instance, String feuille , String ligne , String cordonnee, String anomalie){
       /* String TDName = "";
        ILinkedResource linkedResource = null;
        if(instance.getWorkflow().getWorkflowContainer().getName().equals("ImportationCollaborateurs")){
            TDName = "RapportDImport";
            linkedResource =  instance.createLinkedResource(TDName);
        }else if(instance.getWorkflow().getWorkflowContainer().getName().equals("ImportationReferentiels")){
            TDName  ="RapportDImportDesReferentiels";
            linkedResource =  instance.createLinkedResource(TDName);
            linkedResource.setValue("Feuille",feuille);

        }

        linkedResource.setValue("Ligne",ligne);
        linkedResource.setValue("Cordonnee",cordonnee);
        linkedResource.setValue("Anomalie",anomalie);
        linkedResource.save(Modules.getWorkflowModule().getSysadminContext());
        instance.addLinkedResource(linkedResource);
        instance.save(Modules.getWorkflowModule().getSysadminContext());*/

      /*  JSONObject anomalieDetails = new JSONObject();
        anomalieDetails.put("Feuille",feuille);
        anomalieDetails.put("Ligne",ligne);
        anomalieDetails.put("Cordonnee",cordonnee);
        anomalieDetails.put("Anomalie",anomalie);
        excelAnomaliesalertAnomalisUser.add(anomalieDetails);*/
    }

    public void addAlerte(IWorkflowInstance instance,String feuille , String ligne , String cordonnee,String alerte){
       /* String TDName = "";
        ILinkedResource linkedResource = null;
        if(instance.getWorkflow().getWorkflowContainer().getName().equals("ImportationCollaborateurs")){
            TDName = "RapportDImport";
            linkedResource =  instance.createLinkedResource(TDName);
        }else if(instance.getWorkflow().getWorkflowContainer().getName().equals("ImportationReferentiels")){
            TDName  ="RapportDImportDesReferentiels";
            linkedResource =  instance.createLinkedResource(TDName);
            linkedResource.setValue("Feuille",feuille);

        }

        linkedResource.setValue("Ligne",ligne);
        linkedResource.setValue("Cordonnee",cordonnee);
        linkedResource.setValue("Alerte",alerte);
        linkedResource.save(Modules.getWorkflowModule().getSysadminContext());
        instance.addLinkedResource(linkedResource);
        instance.save(Modules.getWorkflowModule().getSysadminContext());*/
        /*JSONObject alerteDetails = new JSONObject();
        alerteDetails.put("Feuille",feuille);
        alerteDetails.put("Ligne",ligne);
        alerteDetails.put("Cordonnee",cordonnee);
        alerteDetails.put("Anomalie",alerte);
        excelAnomaliesalertAnomalisUser.add(alerteDetails);*/
    }
}
