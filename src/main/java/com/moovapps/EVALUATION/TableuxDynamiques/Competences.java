package com.moovapps.EVALUATION.TableuxDynamiques;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;

public class Competences extends BaseDocumentExtension {

    @Override
    public boolean onBeforeLoad() {
        if(getWorkflowInstance().getParentInstance()!=null){
            IUser collaborateur = (IUser) getWorkflowInstance().getParentInstance().getValue("CollaborateurEval");
            IUser N1 = (IUser) getWorkflowInstance().getParentInstance().getValue("ResponsableHierarchique");
            if(collaborateur!=null){
                if(collaborateur.equals(getWorkflowModule().getLoggedOnUser())){
                    getWorkflowInstance().setValue("isCollaborateur",true);
                    getWorkflowInstance().setValue("isN1",false);

                }
            }
            if(N1!=null){
                if(N1.equals(getWorkflowModule().getLoggedOnUser())){
                    getWorkflowInstance().setValue("isN1",true);
                    getWorkflowInstance().setValue("isCollaborateur",false);

                }
            }
        }
        return super.onBeforeLoad();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("AutoEvaluation")){
           // setNotationAutoEval();
        } else if (property.getName().equals("Evaluation")) {
            //setNotationEvaluateurEval();
        }
        super.onPropertyChanged(property);
    }

    private void setNotationAutoEval(){
        if(getWorkflowInstance().getValue("AutoEvaluation") != null){

            IStorageResource autoEvaluation = (IStorageResource)getWorkflowInstance().getValue("AutoEvaluation");
            float valeurAutoEval =  ((Number)autoEvaluation.getValue("Valeur")).floatValue();
            float poidsAutoEval =  ((Number)getWorkflowInstance().getValue("Poids")).floatValue();
            getWorkflowInstance().setValue("ValeurAutoEval", valeurAutoEval);
            getWorkflowInstance().setValue("PoidsAutoEval", poidsAutoEval);
            getWorkflowInstance().setValue("NotationAutoEval", valeurAutoEval * poidsAutoEval);
        }else {
            getWorkflowInstance().setValue("ValeurAutoEval", null);
            getWorkflowInstance().setValue("PoidsAutoEval", null);
            getWorkflowInstance().setValue("NotationAutoEval", null);
        }
    }

    private void setNotationEvaluateurEval(){
        if(getWorkflowInstance().getValue("Evaluation") != null){
            IStorageResource autoEvaluation = (IStorageResource)getWorkflowInstance().getValue("Evaluation");
            float valeurEvaluateurEval =  ((Number)autoEvaluation.getValue("Valeur")).floatValue();
            float poidsEvaluateurEval =  ((Number)getWorkflowInstance().getValue("Poids")).floatValue();
            getWorkflowInstance().setValue("ValeurEvaluateurEval", valeurEvaluateurEval);
            getWorkflowInstance().setValue("PoidsEvaluateurEval", poidsEvaluateurEval);
            getWorkflowInstance().setValue("NotationEvaluateurEval", valeurEvaluateurEval * poidsEvaluateurEval);
        }else {
            getWorkflowInstance().setValue("ValeurEvaluateurEval", null);
            getWorkflowInstance().setValue("PoidsEvaluateurEval", null);
            getWorkflowInstance().setValue("NotationEvaluateurEval", null);
        }
    }

    @Override
    public boolean onAfterLoad() {
        return super.onAfterLoad();
    }




}
