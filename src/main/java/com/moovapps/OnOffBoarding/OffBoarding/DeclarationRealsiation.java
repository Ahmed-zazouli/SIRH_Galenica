package com.moovapps.OnOffBoarding.OffBoarding;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Calendar;
import java.util.Date;

public class DeclarationRealsiation extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("Accepter")){
            IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
            IUser collaborateur = (IUser) getWorkflowInstance().getValue("Collaborateur");
            Date dateEmbauche = (Date) getWorkflowInstance().getValue("DateDEmbauche") ;
            Date dateDepart = (Date) getWorkflowInstance().getValue("DateDepartCollaborateur");
            String matricule = (String) getWorkflowInstance().getValue("Matricule");
            String duree = "";
            if(dateEmbauche!=null && dateDepart!=null){
                duree = calculateDatePeriod(dateEmbauche,dateDepart);
            }
            CreateHistorique(societe,collaborateur,matricule,dateEmbauche,dateDepart,duree);
        }
        return super.onBeforeSubmit(action);
    }



    public void CreateHistorique(IStorageResource societe, IUser collaborateur, String matricule, Date dateEmbauche, Date dateDepart, String duree){
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",getDirectoryModule().getOrganization(context, "DefaultOrganization"));
            ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
            IResourceDefinition resourceDefinition = getWorkflowModule().getResourceDefinition(context, catalog, "HistoriqueDesDepartDesCollaborateurs");
            IStorageResource historique =  getWorkflowModule().createStorageResource(context,resourceDefinition,null,null);
            historique.setValue("Societe",societe);
            historique.setValue("Collaborateur",collaborateur);
            historique.setValue("Matricule",matricule);
            historique.setValue("DateDEmbaucheSociete",dateEmbauche);
            historique.setValue("DateSortieSociete",dateDepart);
            historique.setValue("Duree",duree);
            historique.save(context);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static String calculateDatePeriod(Date startDate, Date endDate) {
        // Create Calendar instances for the start and end dates
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);

        // Calculate the difference in years, months, and days
        int years = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int months = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
        int days = endCalendar.get(Calendar.DAY_OF_MONTH) - startCalendar.get(Calendar.DAY_OF_MONTH);

        // Adjust for negative differences
        if (days < 0) {
            months--;
            days += startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        if (months < 0) {
            years--;
            months += 12;
        }

        // Construct the result string
        StringBuilder result = new StringBuilder();
        if (years > 0) {
            result.append(years).append(" ").append(years == 1 ? "an" : "ans").append(" ");
        }
        if (months > 0) {
            result.append(months).append(" ").append(months == 1 ? "mois" : "mois").append(" ");
        }
        if (days > 0) {
            result.append(days).append(" ").append(days == 1 ? "jour" : "jours");
        }

        return result.toString();
    }
}
