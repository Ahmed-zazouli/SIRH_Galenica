package com.moovapps.Formation.Referentiels;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;

public class QuestionReponses extends BaseDocumentExtension {
    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("ReponseCoorecte")) {
        }
        super.onPropertyChanged(property);
    }

    @Override
    public boolean onBeforeSave() {
        if (getWorkflowInstance().getValue("ReponseS").equals(getWorkflowInstance().getValue("ReponseCoorecte"))) {
            getWorkflowInstance().setValue("Notation", getWorkflowInstance().getValue("Poids"));
        } else {
            getWorkflowInstance().setValue("Notation", 0);
        }
        return super.onBeforeSave();
    }
}
