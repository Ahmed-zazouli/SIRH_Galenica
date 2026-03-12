package com.moovapps.capone.rh.GestionDeConge.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.ITask;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdp.activity.domain.TaskInstance;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.axemble.vdp.ui.framework.widgets.CtlText;
import com.moovapps.EVALUATION.Eval.HideButton;

import java.util.ArrayList;
import java.util.List;

public class DemandeModificationDesDatesDeConge extends BaseDocumentExtension {
    @Override
    public boolean onAfterLoad() {
         HideButton();

        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {

//        if (action.getName().equals("DemandeModificationDesDatesDeConge")&& !"Validée".equals(getWorkflowInstance().getValue("EtatDeParallelisme"))){
//            ClickButton("RentrerAParallelisme");
//        }

        return super.onBeforeSubmit(action);
    }

    private void HideButton(){
        try {
            NamedContainer namedContainer = getResourceController().getButtonContainer(2);
            List<IWidget> widgets = namedContainer.getWidgets();
            for (IWidget iWidget : widgets) {
                CtlButton button = (CtlButton) iWidget;
                if (((CtlButton) iWidget).getName().equalsIgnoreCase("Retourner pour modification")) {
                        button.setHidden(true);
                }

                if (((CtlButton) iWidget).getName().equalsIgnoreCase("Refuser congé ou absence du collaborateur ")) {
                    button.setHidden(true);
                }

                if (((CtlButton) iWidget).getName().equalsIgnoreCase("Confirmer consommation du congé") && !"Validée".equals(getWorkflowInstance().getValue("EtatDeParallelisme")) ) {
                    button.setHidden(true);
                }
                if (((CtlButton) iWidget).getName().equalsIgnoreCase("Demande annulation demande du congé") && !"Validée".equals(getWorkflowInstance().getValue("EtatDeParallelisme")) ) {
                    button.setHidden(true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void ClickButton(String button) {

        List<TaskInstance> instances = new ArrayList<>();
        try {
            instances = ((com.axemble.vdp.workflow.domain.ProcessWorkflowInstance) getWorkflowInstance()).getTaskInstances(2);

        } catch (ClassCastException classCastException) {
            if (classCastException.getMessage().contains("domain.ProcessWorkflowInstance")) {
                instances = ((com.axemble.vdoc.sdk.impl.ProcessWorkflowInstance) getWorkflowInstance()).getDocument().getWorkflowInstance().getTaskInstances(2);
            }
        }
        try {


            for (TaskInstance taskInstance : instances) {
                ITask task = taskInstance.getTask();

                IAction iAction1 = task.getAction(button);
                if (iAction1 != null) {
                    taskInstance.removeOperators();

                    taskInstance.addOperator(getWorkflowModule().getOperatorByLogin("sysadmin"));
                    getWorkflowModule().end(getWorkflowModule().getSysadminContext(), taskInstance, iAction1, "");

                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }
}
