package com.moovapps.ibb.rh.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HistorisationDesJoursFeriers extends BaseAgent {
    //THIS AGENT GONNA RUN ANNUALY in the first day in the new year
    @Override
    protected void execute() {
       try{
           ArrayList<IStorageResource> joursFeriers = getJoursFeries();
           if(joursFeriers!=null && !joursFeriers.isEmpty()){
               IContext sysContext = getWorkflowModule().getSysadminContext();
               IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
               IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
               ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
               IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"HistoriqueDesJoursFeries");
               Calendar calendar = Calendar.getInstance();
               for(IStorageResource jourFerier : joursFeriers ){
                   HistoriseJourFerier(jourFerier,sysContext, definition);
                   boolean isFeteReligieus = jourFerier.getValue("FeteReligieuse")!=null?(boolean) jourFerier.getValue("FeteReligieuse"):false;
                   Date dateJourFerier = (Date) jourFerier.getValue("DateDebutJourFerie");
                   calendar.setTime(dateJourFerier);
                   calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                   if(isFeteReligieus){
                       jourFerier.delete(sysContext);
                       jourFerier.save(sysContext);
                   }else{
                       jourFerier.setValue("DateDebutJourFerie",calendar.getTime());
                       jourFerier.save(sysContext);
                   }
               }
           }

       }catch (Exception e){
           e.printStackTrace();
       }
    }

    private void HistoriseJourFerier(IStorageResource jourFerier , IContext sysContext,  IResourceDefinition historiqueDefinition) {
        try{
            IStorageResource historique = getWorkflowModule().createStorageResource(sysContext,historiqueDefinition,"");
            historique.setValue("Annee", Calendar.getInstance().get(Calendar.YEAR) -1);
            historique.setValue("Societe", jourFerier.getValue("Societe"));
            historique.setValue("sys_Title", jourFerier.getValue("sys_Title"));
            historique.setValue("FeteReligieuse", jourFerier.getValue("FeteReligieuse"));
            historique.setValue("DateDebutJourFerie", jourFerier.getValue("DateDebutJourFerie"));
            historique.setValue("Date", jourFerier.getValue("Date"));
            historique.setValue("NJoursFeries", jourFerier.getValue("NJoursFeries"));
            historique.setValue("Jour", jourFerier.getValue("Jour"));

            boolean isFeteReligieus = jourFerier.getValue("FeteReligieuse")!=null?(boolean) jourFerier.getValue("FeteReligieuse"):false;
            if(!isFeteReligieus){
                historique.setValue("JourFerie", jourFerier);
            }
            historique.save(sysContext);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    ArrayList<IStorageResource> getJoursFeries(){
        ArrayList<IStorageResource> joursFeriers = null;
        try{

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"JoursFeries");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addLessConstraint("DateDebutJourFerie",new Date());
            joursFeriers =   (ArrayList<IStorageResource>) controller.evaluate(definition);


        }catch (Exception e){
            e.printStackTrace();
        }
        return joursFeriers;
    }

}
