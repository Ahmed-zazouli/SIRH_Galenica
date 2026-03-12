package com.moovapps.EVALUATION.Referentiels;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;

public class AnneeEvaluation extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSave() {
        int annee = ((Number) getWorkflowInstance().getValue("Annee")).intValue();
        getWorkflowInstance().setValue("sys_Title",annee+"");
        return super.onBeforeSave();
    }
}
