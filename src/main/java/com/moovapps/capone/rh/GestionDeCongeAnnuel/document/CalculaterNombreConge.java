package com.moovapps.capone.rh.GestionDeCongeAnnuel.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.moovapps.capone.rh.GestionDeConge.helpers.JoursOuvrableOuvres;
import com.moovapps.capone.rh.GestionDeConge.helpers.WorkingDaysNumberCalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class CalculaterNombreConge extends BaseDocumentExtension {
    // SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY = 6, SATURDAY = 7
    ArrayList<Integer> joursOuvrables = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6, 7));

    // SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY = 6, SATURDAY = 7
    ArrayList<Integer> joursOuvres = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6));

    @Override
    public void onPropertyChanged(IProperty property) {

        if (property.getName().equals("DateDebute") || property.getName().equals("DateFin")) {
            setVariables();
            Date dateDebute = (Date) getWorkflowInstance().getValue("DateDebute");
            Date dateFin = (Date) getWorkflowInstance().getValue("DateFin");
            if (dateDebute != null && dateFin != null){
                IUser  demandeur = getWorkflowInstance().getCreatedBy();
                IStorageResource societe = (IStorageResource) demandeur.getExtendedAttributes().getValue("Societe");
                HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebute, dateFin,false);
                getWorkflowInstance().setValue("NombreDeJoursCalculer", calculResult.get("nbrJoursDemande"));
            }

        }
        super.onPropertyChanged(property);
    }

    public void setVariables() {
       IUser  demandeur = getWorkflowInstance().getCreatedBy();
       IStorageResource societe = (IStorageResource) demandeur.getExtendedAttributes().getValue("Societe");
        joursOuvrables = JoursOuvrableOuvres.getJoursOuvrables(societe);
        joursOuvres = JoursOuvrableOuvres.getJoursOuvres(societe);


    }
}
