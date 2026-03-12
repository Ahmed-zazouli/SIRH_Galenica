package com.moovapps.Sanction;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;

public class ValidationRH extends BaseDocumentExtension {
    @Override
    public boolean onBeforeLoad() {
      /*  if(getWorkflowInstance().getValue("isNew").equals(false)){
            IWorkflowInstance sanctionSource = (IWorkflowInstance) getWorkflowInstance().getValue("SanctionSource");
            HashMap<String,String> fieldsMap = new HashMap<>();
            fieldsMap.put("Demandeur","Demandeur");
            fieldsMap.put("Motif","Motif");
            fieldsMap.put("AutreMotif","AutreMotif");
            fieldsMap.put("NatureLegale","NatureLegale");
            fieldsMap.put("Justification","Justification");
            fieldsMap.put("GraviteDeLaFaute","GraviteDeLaFaute");
            fieldsMap.put("Graduel","GraduelGG");
            fieldsMap.put("CommentaireRH","CommentaireRHGG");
            fieldsMap.put("CommentaireDG","CommentaireDGGG");




            new CopyFieldsFromSanctionSourceToSanctionEvolution().Copy(getWorkflowInstance(),sanctionSource,fieldsMap);
        }*/
        return super.onBeforeLoad();
    }
}
