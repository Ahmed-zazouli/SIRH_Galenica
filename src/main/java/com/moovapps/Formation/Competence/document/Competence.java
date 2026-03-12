package com.moovapps.Formation.Competence.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;

import java.util.Calendar;

public class Competence extends BaseDocumentExtension {
    @Override
    public boolean onAfterLoad() {
        getWorkflowInstance().setValue("Societe",getWorkflowInstance().getCreatedBy().getExtendedAttributes().getValue("Societe"));
        return super.onAfterLoad();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("DureeMois")) {
            calculateDateFinCompetence();
        }
        super.onPropertyChanged(property);
    }

    private void calculateDateFinCompetence() {
        if (getWorkflowInstance().getValue("DureeMois") != null) {
            Calendar dateFin = Calendar.getInstance();
            dateFin.add(Calendar.MONTH, ((Number) getWorkflowInstance().getValue("DureeMois")).intValue());
            getWorkflowInstance().setValue("DateDExpiration", dateFin.getTime());

        }
    }
}
