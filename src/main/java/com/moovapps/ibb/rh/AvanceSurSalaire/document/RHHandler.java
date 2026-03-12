package com.moovapps.ibb.rh.AvanceSurSalaire.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;

public class RHHandler extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("ValiderLAvanceSuSalaire") || action.getName().equals("ApprouverLaDemandeDAvanceSurSalaire") ){
            IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
            getWorkflowInstance().setValue("ResponsableRH", societe != null ? societe.getValue("ResponsableRH") : null);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        }
        return super.onBeforeSubmit(action);
    }
}
