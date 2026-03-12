package com.moovapps.EVALUATION.CampagneEval;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.activity.domain.TaskInstance;
import com.axemble.vdp.workflow.domain.ProcessWorkflowInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Cloture extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("CloturerLaCampagneDEvaluation")){
            ArrayList<IWorkflowInstance> evals =  (ArrayList<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("Evaluations");
            for(IWorkflowInstance eval : evals){
                clotureEval(eval);
               // setNextEvalObjectifs(eval);
            }
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

    private void clotureEval(IWorkflowInstance evalInstance) {
        try{
            List<TaskInstance> instances = ((ProcessWorkflowInstance)evalInstance).getTaskInstances(2);
            List<String> actions = Arrays.asList("Cloturer","Cloturer2","Cloturer3","Cloturer4","Cloturer5","Cloturer7","Cloturer6");
            while (!instances.isEmpty() ) {
                TaskInstance taskInstance = instances.iterator().next();
                taskInstance.removeOperators();
                taskInstance.addOperator(getWorkflowModule().getOperatorByLogin("sysadmin"));
                ITask task = taskInstance.getTask();
                for (String action : actions) {
                    IAction iAction1 = task.getAction(action);
                    if (iAction1 != null) {
                        getWorkflowModule().end(getWorkflowModule().getSysadminContext(), taskInstance, iAction1, "");
                        evalInstance.setValue("TypeDeCloture","Clôture forcée");
                        evalInstance.save(getWorkflowModule().getSysadminContext());
                        break;
                    }
                }
            }
                //instances = ((ProcessWorkflowInstance)evalInstance).getTaskInstances(2);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
