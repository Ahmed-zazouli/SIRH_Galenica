package com.moovapps.EVALUATION.TableuxDynamiques;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SecurityHandler extends BaseDocumentExtension {
   // HashMap<String, List<String>> canUpdate = new HashMap<>();


    @Override
    public boolean onBeforeLoad() {
        IUser collaborateur = (IUser) getWorkflowInstance().getParentInstance().getValue("CollaborateurEval");
        IUser evaluateur = (IUser) getWorkflowInstance().getParentInstance().getValue("Evaluateur");
        IUser connectedUser = getWorkflowModule().getLoggedOnUser();
        String etatDEvaluation = (String) getWorkflowInstance().getParentInstance().getValue("EtatDEvaluation");

        if(collaborateur!=null && connectedUser.getId().toString().equals(collaborateur.getId().toString())){
            if(etatDEvaluation.equals("A débuter")){
                // collab Modify
                getWorkflowInstance().setValue("CollaborateurCanModify",true);
                getWorkflowInstance().setValue("EvaluateurCanModify",false);
                getWorkflowInstance().setValue("CollaborateurCantModify",false);
                getWorkflowInstance().setValue("EvaluateurCantModify",false);




            }else{
                //collab Read
                getWorkflowInstance().setValue("CollaborateurCanModify",false);
                getWorkflowInstance().setValue("EvaluateurCanModify",false);
                getWorkflowInstance().setValue("CollaborateurCantModify",true);
                getWorkflowInstance().setValue("EvaluateurCantModify",false);

            }
        }else if(evaluateur!=null && connectedUser.getId().toString().equals(evaluateur.getId().toString())){
            if(etatDEvaluation.equals("Auto-évaluation réalisée") || etatDEvaluation.equals("Entretien programmé") ){
                // N1 Modify
                getWorkflowInstance().setValue("CollaborateurCanModify",false);
                getWorkflowInstance().setValue("EvaluateurCanModify",true);
                getWorkflowInstance().setValue("CollaborateurCantModify",false);
                getWorkflowInstance().setValue("EvaluateurCantModify",false);


            }else{
                // N1 Read

                getWorkflowInstance().setValue("CollaborateurCanModify",false);
                getWorkflowInstance().setValue("EvaluateurCanModify",false);
                getWorkflowInstance().setValue("CollaborateurCantModify",false);
                getWorkflowInstance().setValue("EvaluateurCantModify",true);
            }
        } else {
            // SHOW DEFAULT
            getWorkflowInstance().setValue("CollaborateurCanModify",false);
            getWorkflowInstance().setValue("EvaluateurCanModify",false);
            getWorkflowInstance().setValue("CollaborateurCantModify",false);
            getWorkflowInstance().setValue("EvaluateurCantModify",false);

        }
       // getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        return super.onBeforeLoad();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
       // getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        getWorkflowInstance().getName();
        //if()
        super.onPropertyChanged(property);
    }


}
