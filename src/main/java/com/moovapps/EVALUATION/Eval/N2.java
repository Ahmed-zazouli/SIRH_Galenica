package com.moovapps.EVALUATION.Eval;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;

public class N2 extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("CloturerEvaluation") || action.getName().equals("ConfirmerReception")){
           // setNextEvalObjectifs(getWorkflowInstance());
        }
        return super.onBeforeSubmit(action);
    }

    private void setNextEvalObjectifs(IWorkflowInstance eval) {
        IUser collaborateur = (IUser) eval.getValue("CollaborateurEval");
        Integer exerciceEval = ((Number) eval.getValue("AnneeDEvaluation")).intValue();
        if(collaborateur==null || exerciceEval ==null){
            return;
        }
        ArrayList<ILinkedResource> fixObj = (ArrayList<ILinkedResource>) eval.getLinkedResources("ObjFix");
        if(fixObj!=null && !fixObj.isEmpty()){
            for(ILinkedResource obj : fixObj){
                if(obj.getValue("DecisionN1").equals("Validé")){
                    createObjectif(collaborateur,exerciceEval+1,obj);
                }

            }
        }
    }

    private void createObjectif(IUser collaborateur, int exercice, ILinkedResource obj) {
        try{
            String id = obj.getId().toString();
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition objectifsDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "ObjectifsDeLAnnee");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("LinkedResourceIDCreatedFrom",id);
           ArrayList<IStorageResource> objectifsResource = (ArrayList<IStorageResource>) controller.evaluate(objectifsDefinition);
           IStorageResource objectifResource = null;
            if(objectifsResource==null || objectifsResource.isEmpty()){
                objectifResource = getWorkflowModule().createStorageResource(sysContext,objectifsDefinition,"");
            }else{
                objectifResource = objectifsResource.iterator().next();
            }
            objectifResource.setValue("Annee",exercice);
            objectifResource.setValue("Collaborateur",collaborateur);
            objectifResource.setValue("Objectifs",obj.getValue("Objectif"));
            objectifResource.setValue("Poids",obj.getValue("Poids"));
            objectifResource.setValue("IndicateurDePerformance",obj.getValue("IndicateurDePerformance"));
            objectifResource.setValue("LinkedResourceIDCreatedFrom",obj.getId().toString());
            objectifResource.save(getWorkflowModule().getSysadminContext());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
