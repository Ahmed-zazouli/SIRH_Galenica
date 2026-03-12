package com.moovapps.EVALUATION.Eval;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IUser;

public class ReactivationEvaluation extends BaseDocumentExtension {

    @Override
    public boolean onBeforeLoad() {
        String etatEvaluation =getWorkflowInstance().getValue("EtatDEvaluation")!=null? (String) getWorkflowInstance().getValue("EtatDEvaluation"):"";
        if(etatEvaluation.equals("Evaluation clôturée")){
            IUser collaborateur = (IUser) getWorkflowInstance().getValue("CollaborateurEval");
            getWorkflowInstance().setValue("Evaluateur",collaborateur!=null?collaborateur.getExtendedAttributes().getValue("Evaluateur"):null);

            IUser N1 =collaborateur!=null? collaborateur.getHierarchicalManager():null;
            IUser N2 =N1!=null? N1.getHierarchicalManager():null;
            getWorkflowInstance().setValue("NPlus2",N2);
        }
        return super.onBeforeLoad();
    }
}
