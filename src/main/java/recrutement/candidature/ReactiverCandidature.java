package recrutement.candidature;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IUser;

import java.util.ArrayList;
import java.util.List;

public class ReactiverCandidature extends BaseDocumentExtension {

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("DesLePremierEvaluateur")){
            List<ILinkedResource> entretiens = (List<ILinkedResource>) getWorkflowInstance().getLinkedResources("EvaluationRH");
            getWorkflowInstance().getValue("OrdreActuel");
            ILinkedResource entretien = entretiens.get(0);
            ((IUser)entretien.getValue("Evaluateur")).getFullName();
                getWorkflowInstance().setValue("OrdreActuel",1);
                getWorkflowInstance().setValue("EvaluateurActuel",entretien.getValue("Evaluateur"));
              //  getWorkflowInstance().deleteLinkedResources(entretiens);
                for(ILinkedResource resource : entretiens){
                    resource.setValue("DateDEntretien", null);
                   // resource.setValue("Entretien", null);
                    resource.setValue("NoteGlobale", null);
                    resource.setValue("DecisionDEntretien", null);
                    resource.setValue("FicheDEvaluation", new ArrayList<>());
                    resource.save(getWorkflowModule().getSysadminContext());
                    //resource.delete(getWorkflowModule().getLoggedOnUserContext());
                }
                getWorkflowInstance().save(getWorkflowModule().getLoggedOnUserContext());


        }else if(action.getName().equals("DesLeDernierEvaluateur")){//Action28 des le dernier evaluateur
            List<ILinkedResource> entretiens = (List<ILinkedResource>) getWorkflowInstance().getLinkedResources("EvaluationRH");
            int currentOrder = ((Number) getWorkflowInstance().getValue("OrdreActuel")).intValue();
            ILinkedResource lastEntretien = entretiens.get(currentOrder-2);
            ((IUser) lastEntretien.getValue("Evaluateur")).getFullName();
            getWorkflowInstance().setValue("EvaluateurActuel",lastEntretien.getValue("Evaluateur"));
            lastEntretien.setValue("DateDEntretien", null);
            //lastEvalrensengné.setValue("Entretien", null);
            lastEntretien.setValue("NoteGlobale", null);
            lastEntretien.setValue("DecisionDEntretien", null);
            //lastEvalrensengné.setValue("DateDEntretienReel", null);
            lastEntretien.setValue("FicheDEvaluation", new ArrayList<>());
            lastEntretien.save(getWorkflowModule().getLoggedOnUserContext());

            getWorkflowInstance().setValue("OrdreActuel",currentOrder-1);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
            }
        else  if(action.getName().equals("DesLaPlanificationDesEntretiens")){//Action26 des la planification
            List<ILinkedResource> entretiens = (List<ILinkedResource>) getWorkflowInstance().getLinkedResources("EvaluationRH");
            List<ILinkedResource> planifficationEntretiens = (List<ILinkedResource>) getWorkflowInstance().getLinkedResources("PlanificationEntretiens");

            getWorkflowInstance().deleteLinkedResources(entretiens);

            for(ILinkedResource resource : planifficationEntretiens){
                resource.setValue("Evaluateur", null);
                resource.save(getWorkflowModule().getSysadminContext());
                //resource.delete(getWorkflowModule().getLoggedOnUserContext());
            }
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());

        }

        return super.onBeforeSubmit(action);
    }
}


