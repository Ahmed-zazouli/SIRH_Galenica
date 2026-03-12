package com.moovapps.Formation.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public class AgentDemarrageSeanceParParticipant extends BaseAgent {

    @Override
    protected void execute() {
        Collection<IWorkflowInstance> instances = getCoursParParticipantADemarrer();
        for (IWorkflowInstance instance : instances) {
            try {
                ITaskInstance iTaskInstance = instance.getCurrentTaskInstance(getWorkflowModule().getSysadminContext());
                ITask task = iTaskInstance.getTask();
                IAction iAction1 = task.getAction("CommencerLaSeanceDeFormation");
                getWorkflowModule().end(getWorkflowModule().getSysadminContext(), iTaskInstance, iAction1, "");
                instance.save(getWorkflowModule().getSysadminContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private Collection<IWorkflowInstance> getCoursParParticipantADemarrer() {
        try {
            IOrganization organization = getDirectoryModule().getOrganization(getWorkflowModule().getSysadminContext(), "DefaultOrganization");
            IProject project = getProjectModule().getProject(getWorkflowModule().getSysadminContext(), "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(getWorkflowModule().getSysadminContext(), "Formation", project);
            IViewController controller = getWorkflowModule().getViewController(getWorkflowModule().getSysadminContext());
            controller.addInConstraint("sys_WorkflowContainer", Arrays.asList("CoursParParticipant"));
            Calendar cStart = Calendar.getInstance();
            cStart.set(Calendar.HOUR_OF_DAY, 0);
            cStart.set(Calendar.MINUTE, 0);
            cStart.set(Calendar.SECOND, 0);
           /* Calendar cEnd = Calendar.getInstance();
            cEnd.add(Calendar.DATE, 1);
            cEnd.set(Calendar.HOUR_OF_DAY, 0);
            cEnd.set(Calendar.MINUTE, 0);
            cEnd.set(Calendar.SECOND, 0);*/
            controller.addGreaterOrEqualConstraint("DateDeDebut", cStart.getTime());
            //controller.addLessConstraint("DateDeDebut", cEnd.getTime());
            controller.addEqualsConstraint("DocumentState", "En cours");
            return controller.evaluate(catalog);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
