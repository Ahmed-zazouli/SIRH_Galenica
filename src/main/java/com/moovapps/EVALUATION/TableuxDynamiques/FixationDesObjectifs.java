package com.moovapps.EVALUATION.TableuxDynamiques;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IUser;

public class FixationDesObjectifs extends BaseDocumentExtension {

    @Override
    public boolean onBeforeLoad() {
        IUser evaluateur = (IUser)getWorkflowInstance().getParentInstance().getValue("Evaluateur");
        if(((IUser) getWorkflowInstance().getValue("sys_Creator")).getLogin().equals("sysadmin") && evaluateur!=null &&  getWorkflowModule().getLoggedOnUser().getId().toString().equals((evaluateur).getId().toString())) {
            getWorkflowInstance().setValue("DecisionN1","Validé");

        }
        if(evaluateur!=null && ((IUser) getWorkflowInstance().getValue("sys_Creator")).getId().toString().equals((evaluateur).getId().toString())){
            getWorkflowInstance().setValue("DecisionN1","Validé");
        }

        IUser collaborateur = (IUser) getWorkflowInstance().getParentInstance().getValue("CollaborateurEval");
        IUser connectedUser = getWorkflowModule().getLoggedOnUser();
        // IUser creator = getWorkflowInstance().getCreatedBy();
        String etatDEvaluation = (String) getWorkflowInstance().getParentInstance().getValue("EtatDEvaluation");

        if(collaborateur!=null && connectedUser.getId().toString().equals(collaborateur.getId().toString())){
            if(etatDEvaluation.equals("A débuter")){
                // collab Modify
                getWorkflowInstance().setValue("CollaborateurCanModify",true);
                getWorkflowInstance().setValue("EvaluateurCreatorCanModify",false);
                getWorkflowInstance().setValue("EvaluateurNotCreatorCanModify",false);
                getWorkflowInstance().setValue("CollaborateurCantModify",false);
                getWorkflowInstance().setValue("EvaluateurCantModify",false);



            }else{
                //collab Read
                getWorkflowInstance().setValue("CollaborateurCanModify",false);
                getWorkflowInstance().setValue("EvaluateurCreatorCanModify",false);
                getWorkflowInstance().setValue("EvaluateurNotCreatorCanModify",false);
                getWorkflowInstance().setValue("CollaborateurCantModify",true);
                getWorkflowInstance().setValue("EvaluateurCantModify",false);

            }
        }else if(evaluateur!=null && connectedUser.getId().toString().equals(evaluateur.getId().toString())){
            if(etatDEvaluation.equals("Auto-évaluation réalisée") || etatDEvaluation.equals("Entretien programmé") ){
                if ( (evaluateur!=null && ((IUser) getWorkflowInstance().getValue("sys_Creator")).getId().toString().equals(evaluateur.getId().toString())) ||
                        (((IUser) getWorkflowInstance().getValue("sys_Creator")).getLogin().equals("sysadmin") &&
                                evaluateur!=null &&
                                getWorkflowModule().getLoggedOnUser().getId().toString().equals(
                                        (evaluateur).getId().toString()))) {
                    // N1 Modify
                    getWorkflowInstance().setValue("CollaborateurCanModify",false);
                    getWorkflowInstance().setValue("EvaluateurCreatorCanModify",true);
                    getWorkflowInstance().setValue("EvaluateurNotCreatorCanModify",false);
                    getWorkflowInstance().setValue("CollaborateurCantModify",false);
                    getWorkflowInstance().setValue("EvaluateurCantModify",false);

                }else{
                    // N1 Modify
                    getWorkflowInstance().setValue("CollaborateurCanModify",false);
                    getWorkflowInstance().setValue("EvaluateurCreatorCanModify",false);
                    getWorkflowInstance().setValue("EvaluateurNotCreatorCanModify",true);
                    getWorkflowInstance().setValue("CollaborateurCantModify",false);
                    getWorkflowInstance().setValue("EvaluateurCantModify",false);


                }


            }else{
                // N1 Read
                getWorkflowInstance().setValue("CollaborateurCanModify",false);
                getWorkflowInstance().setValue("EvaluateurCreatorCanModify",false);
                getWorkflowInstance().setValue("EvaluateurNotCreatorCanModify",false);
                getWorkflowInstance().setValue("CollaborateurCantModify",false);
                getWorkflowInstance().setValue("EvaluateurCantModify",true);

            }
        } else {
            // SHOW DEFAULT
            getWorkflowInstance().setValue("CollaborateurCanModify",false);
            getWorkflowInstance().setValue("EvaluateurCreatorCanModify",false);
            getWorkflowInstance().setValue("EvaluateurNotCreatorCanModify",false);
            getWorkflowInstance().setValue("CollaborateurCantModify",false);
            getWorkflowInstance().setValue("EvaluateurCantModify",false);


        }








       /* if(getWorkflowInstance().getParentInstance()!=null){
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
        }*/
        return super.onBeforeLoad();
    }

    @Override
    public boolean onAfterLoad() {
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSave() {
      /*  if(getWorkflowInstance().getValue("isCreatedByCollaborateur")==null){
            //boolean isCreated = (boolean) getWorkflowInstance().getValue("isCreatedByCollaborateur");
            IUser connectedUser = getWorkflowModule().getLoggedOnUser();
            IUser collaborateur = (IUser) getWorkflowInstance().getParentInstance().getValue("CollaborateurEval");
            IUser evaluateur = (IUser) getWorkflowInstance().getParentInstance().getValue("ResponsableHierarchique");
            if(connectedUser.getId().toString().equals(collaborateur.getId().toString())){
                getWorkflowInstance().setValue("isCreatedByCollaborateur",true);
            }else if( connectedUser.getId().toString().equals(evaluateur.getId().toString())){
                getWorkflowInstance().setValue("isCreatedByCollaborateur",false);
                getWorkflowInstance().setValue("DecisionN1","Validé");
            }
        }*/


        return super.onBeforeSave();
   /*  if(getWorkflowInstance().getParentInstance()!=null){


            ArrayList<ILinkedResource> fixations = (ArrayList<ILinkedResource>) getWorkflowInstance().getParentInstance().getLinkedResources("ObjFix");
            if (fixations != null && !fixations.isEmpty()) {
                int sommePoids = 0;
                for (ILinkedResource fixation : fixations) {
                    String decision = (String) fixation.getValue("DecisionN1");
                    if(decision.equals("")||decision.equals("Non validé")||decision.equals("A validé")){
                        continue;
                    }
                    int poids = fixation.getValue("Poids") != null ? ((Number) fixation.getValue("Poids")).intValue() : 1;
                    sommePoids += poids;
                }
                ///  if(sommePoids>100){
                    getResourceController().alert("Vous ne pouvez pas dépasser 100 % dans la somme des poids");
                    //return false;
                }else if(sommePoids!=100){
                    getResourceController().alert("La somme des poids doit etre 100");

                }///
                // else{
                //getWorkflowInstance().getParentInstance().setValue("TotalFix", sommePoids);
              //  getWorkflowInstance().getParentInstance().save(getWorkflowModule().getSysadminContext());
                //}
            }
            // getWorkflowInstance().save(getWorkflowModule().getSysadminContext());


     }

        return super.onBeforeSave();*/
    }

    @Override
    public boolean onAfterSave() {
        LOGGER.error("test");
        return super.onAfterSave();
    }
}
