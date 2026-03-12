package com.moovapps.capone.rh.GestionDeConge.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;

public class RHHandler extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("ValdierLaDemande") || action.getName().equals("ValiderCongeDuCollaborateur") || action.getName().equals("ConfirmerLaConsommationDeConge")){
            IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
            getWorkflowInstance().setValue("ResponsableRH", societe != null ? societe.getValue("ResponsableRH") : null);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        }
        return super.onBeforeSubmit(action);
    }
}
