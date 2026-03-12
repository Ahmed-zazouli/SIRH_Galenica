package recrutement.candidature;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.*;

public class Planificationtable extends BaseDocumentExtension {
    //int currentEvaluateur = 0;
    //ArrayList<Integer> checkOrder = new ArrayList<Integer>();
    //ArrayList<IUser> myEvaluateurs = new ArrayList<IUser>();

    //ArrayList<Date> entretiensDates = new ArrayList<Date>();
    //ArrayList<String> entretiensName = new ArrayList<String>();
    //boolean isSorted = true;
    //boolean isOk = true;
    //int totalEntretiens = 0;

    @Override
    public boolean onAfterLoad() {
        // TODO Auto-generated method stub
        IContext context = getWorkflowModule().getSysadminContext();
        Collection<ILinkedResource> typeProduit = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("PlanificationEntretiens");
        int order = 1;

        if (typeProduit.isEmpty()) {


            // 1ere ligne
            ILinkedResource linkedResource1 = getWorkflowInstance().createLinkedResource("PlanificationEntretiens");
            linkedResource1.setValue("ordre", order);
            linkedResource1.setValue("Entretien", "Entretien Métier 1");
            linkedResource1.save(context);
            getWorkflowInstance().addLinkedResource(linkedResource1);

            // 2eme ligne
            ILinkedResource linkedResource2 = getWorkflowInstance().createLinkedResource("PlanificationEntretiens");
            linkedResource2.setValue("ordre", order + 1);
            linkedResource2.setValue("Entretien", "Entretien Métier 2");
            linkedResource2.save(context);
            getWorkflowInstance().addLinkedResource(linkedResource2);

            // 3eme ligne
            ILinkedResource linkedResource3 = getWorkflowInstance().createLinkedResource("PlanificationEntretiens");
            linkedResource3.setValue("ordre", order + 2);
            linkedResource3.setValue("Entretien", "Entretien Métier 3");
            linkedResource3.save(context);
            getWorkflowInstance().addLinkedResource(linkedResource3);

            // 4eme ligne
            ILinkedResource linkedResource4 = getWorkflowInstance().createLinkedResource("PlanificationEntretiens");
            linkedResource4.setValue("ordre", order + 3);
            linkedResource4.setValue("Entretien", "Entretien RH");
            linkedResource4.save(context);
            getWorkflowInstance().addLinkedResource(linkedResource4);


            getWorkflowInstance().save(context);
        }


        return super.onAfterLoad();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        // TODO Auto-generated method stub
        if (property.getName().equals("")) {

        }
        super.onPropertyChanged(property);
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("Planifier")) {
            if(!onPlaniferClick()){
                return false;
            }
            getWorkflowInstance().getParentInstance().save(getWorkflowModule().getLoggedOnUserContext());
        }
        return super.onBeforeSubmit(action);
    }

    Comparator<ILinkedResource> byOrder = new Comparator<ILinkedResource>() {
        @Override
        public int compare(ILinkedResource o1, ILinkedResource o2) {
            return ((Number)o1.getValue("ordre")).intValue() - ((Number)o2.getValue("ordre")).intValue();
        }
    };

    private boolean onPlaniferClick(){
        List<ILinkedResource> entretiens = (List<ILinkedResource>) getWorkflowInstance().getLinkedResources("PlanificationEntretiens");
        Collections.sort(entretiens, byOrder);
        if(!allowPlanification((ArrayList<ILinkedResource>)entretiens)){
            //getResourceController().alert("Merci de vérifier que vous avez bien séléctionner l'ordre et que l'évaluateur est 3amr f ga3 les entretiens");
            return false;
        };
//

        //
        for (int i = 0; i < entretiens.size(); i++) {
            ILinkedResource entretien = entretiens.get(i);
            if (i == 0) {
                getWorkflowInstance().setValue("EvaluateurActuel", entretien.getValue("Evaluateur"));
                getWorkflowInstance().setValue("OrdreActuel", entretien.getValue("ordre"));
                getWorkflowInstance().setValue("DateDEntretien2", entretien.getValue("DateDEntretien"));
                getWorkflowInstance().setValue("Entretien", entretien.getValue("Entretien"));
              //  getWorkflowInstance().setValue("PremierEvaluateur", entretien.getValue("Evaluateur"));

            }
            ILinkedResource eval = getWorkflowInstance().createLinkedResource("EvaluationRH");
            eval.setValue("Ordre", entretien.getValue("ordre"));
            eval.setValue("Entretien", entretien.getValue("Entretien"));
            eval.setValue("Evaluateur", entretien.getValue("Evaluateur"));
            eval.setValue("DateDEntretien", entretien.getValue("DateDEntretien"));

            getWorkflowInstance().addLinkedResource(eval);
            eval.save(getWorkflowModule().getSysadminContext());
        }
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        return true;
    }

    private boolean allowPlanification(ArrayList<ILinkedResource> entretiens){
        int currentOrder = 1;
        for (ILinkedResource entretien: entretiens) {
            if(entretien.getValue("ordre") == null){
                getResourceController().alert("Prière de vérifier l’ordre des entretiens");
                return false;
            } else if(entretien.getValue("Evaluateur")==null){
                getResourceController().alert("Prière de vérifier les évaluateurs");
                return false;
            } else {
                if(currentOrder != ((Number)entretien.getValue("ordre")).intValue()){
                    getResourceController().alert("Prière de vérifier l’ordre des entretiens");
                    return false;
                }
                currentOrder++;
            }
        }
        return true;
    }

}
