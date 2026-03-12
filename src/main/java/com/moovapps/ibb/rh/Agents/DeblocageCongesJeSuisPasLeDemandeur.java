package com.moovapps.ibb.rh.Agents;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.activity.domain.TaskInstance;
import com.axemble.vdp.workflow.domain.ProcessWorkflowInstance;

import java.util.Arrays;
import java.util.List;

public class DeblocageCongesJeSuisPasLeDemandeur extends BaseAgent {

    @Override
    protected void execute() {
        IWorkflowInstance conge = getDemandeConges("CONG-19/12/24-0049");
        changeOperator(conge);

    }

    private IWorkflowInstance getDemandeConges(String reference){
        try {
            IContext sysContext = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(sysContext, "Capone", organization);
            IContext context = Modules.getWorkflowModule().getLoggedOnUserContext();
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "RH", project);
            IWorkflowContainer w = Modules.getWorkflowModule().getWorkflowContainer(context, catalog, "GestionDeConges");
            IViewController controller = Modules.getWorkflowModule().getViewController(context);
            controller.addEqualsConstraint("sys_Reference",reference);
            return (IWorkflowInstance) controller.evaluate(w).iterator().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void changeOperator(IWorkflowInstance conge){
        try{
            List<TaskInstance> instances = ((ProcessWorkflowInstance) conge).getTaskInstances(2);
            for (TaskInstance taskInstance : instances) {
                taskInstance.removeOperators();
                taskInstance.addOperator(getWorkflowModule().getOperatorByLogin("sysadmin"));
                conge.save(getWorkflowModule().getSysadminContext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
