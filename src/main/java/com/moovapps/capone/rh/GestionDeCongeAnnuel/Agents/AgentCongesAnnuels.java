package com.moovapps.capone.rh.GestionDeCongeAnnuel.Agents;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.capone.rh.GestionDeCongeAnnuel.document.CongeAnnuel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class AgentCongesAnnuels extends BaseAgent {

    @Override
    protected void execute() {
        // get Conges annuels planifié date debut equals date today
        Collection<IWorkflowInstance> instances = getPlaniferCongeAnnuel();
        if (instances == null || instances.isEmpty()) return;
        for (IWorkflowInstance instance : instances) {

            Date dateDebut = (Date) instance.getValue("DateDeDebut");
            Date cureentDate = new Date();
            SimpleDateFormat DateFormat = new SimpleDateFormat("dd/MM/yyyy");
            try {
                dateDebut=DateFormat.parse(DateFormat.format(dateDebut));
                cureentDate=DateFormat.parse(DateFormat.format(cureentDate));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            if (cureentDate.equals(dateDebut)) {
                ITaskInstance taskInstance = instance.getCurrentTaskInstance(getWorkflowModule().getContext(instance.getCreatedBy()));

                if (taskInstance != null) {
                    ITask task = taskInstance.getTask();
                    if (task != null) {
                        IAction iAction1 = task.getAction("Valide");
                        if (iAction1 != null) {


                            new CongeAnnuel(instance,instance.getCreatedBy() , getWorkflowModule() , getProjectModule()).calculateCongesAnnuel();
                            try {
                                getWorkflowModule().end(getWorkflowModule().getContext(instance.getCreatedBy()), taskInstance, iAction1, "Button click by the code");
                            } catch (WorkflowModuleException e) {
                                throw new RuntimeException(e);
                            }

                        }
                        instance.save(getWorkflowModule().getContext(instance.getCreatedBy()));

                    }
                }
            }
        }

    }

    public Collection<IWorkflowInstance> getPlaniferCongeAnnuel() {

        Collection<IWorkflowInstance> collection = Collections.emptyList();
        try {
            IContext sysContext = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(sysContext, "Capone", organization);
            IContext context = Modules.getWorkflowModule().getLoggedOnUserContext();
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "RH", project);
            IWorkflow w = Modules.getWorkflowModule().getWorkflow(context, catalog, "LancementDeCongeAnnuel_1.0");
            IViewController controller = Modules.getWorkflowModule().getViewController(context);
            controller.addEqualsConstraint("DocumentState", "Planifié congé annuelle");

            collection = controller.evaluate(w);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return collection;

    }

}
