package com.moovapps.EVALUATION.Eval;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;

public class VariableChampsHandler extends BaseDocumentExtension {

    @Override
    public boolean onBeforeLoad() {
        String etatEvaluation =getWorkflowInstance().getValue("EtatDEvaluation")!=null? (String) getWorkflowInstance().getValue("EtatDEvaluation"):"";
        /*if(!etatEvaluation.equals("Evaluation clôturée")){
                IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
                getWorkflowInstance().setValue("ResponsableRH", societe != null ? societe.getValue("ResponsableRH") : null);

        }*/
        if(etatEvaluation.equals("A débuter") || etatEvaluation.equals("Auto-évaluation réalisée") || etatEvaluation.equals("Evaluation réalisée")){
            IUser collaborateur = (IUser) getWorkflowInstance().getValue("CollaborateurEval");
             getWorkflowInstance().setValue("Evaluateur",collaborateur!=null?collaborateur.getExtendedAttributes().getValue("Evaluateur"):null);
        }
        if(etatEvaluation.equals("Evaluation finalisée")){
            IUser collaborateur = (IUser) getWorkflowInstance().getValue("CollaborateurEval");
            IUser N1 =collaborateur!=null? collaborateur.getHierarchicalManager():null;
            IUser N2 =N1!=null? N1.getHierarchicalManager():null;
            getWorkflowInstance().setValue("NPlus2",N2);
        }
        return super.onBeforeLoad();
    }
}
