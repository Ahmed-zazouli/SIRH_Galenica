package com.moovapps.ibb.rh.AttestationDeSalaire.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.moovapps.ibb.rh.AttestationDeSalaire.document.ButtonsHtml.Generator;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RHHandler extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("Accepter") || action.getName().equals("ApprouverLaDemande") ){
            IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
            getWorkflowInstance().setValue("SocieteResponsableRH", societe != null ? societe.getValue("ResponsableRH") : null);
            String pattern = "dd/MM/yyyy";
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);
            String  dateGenerationTEXT = formatter.format(new Date());
            getWorkflowInstance().setValue("DateGenerationAttestation",dateGenerationTEXT);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        }
        return super.onBeforeSubmit(action);
    }


}
