package com.moovapps.Formation.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.activity.domain.ActionTaskInstance;
import com.moovapps.Formation.SuiviProgrammeDeFormation.document.SuiviProgrammeDeFormation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

public class AgentDemarrageQuestionnaireFroid extends BaseAgent {

    @Override
    protected void execute() {
        Collection<IWorkflowInstance> instances = getQuestionnaireFroidADemarrer();
        for (IWorkflowInstance instance : instances) {
            try {
                ITaskInstance iTaskInstance = instance.getCurrentTaskInstance(getWorkflowModule().getSysadminContext());
                ITask task = iTaskInstance.getTask();
                IAction iAction1 = task.getAction("Commence");
                getWorkflowModule().end(getWorkflowModule().getSysadminContext(), iTaskInstance, iAction1, "");
                instance.save(getWorkflowModule().getSysadminContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private Collection<IWorkflowInstance> getQuestionnaireFroidADemarrer(){
        final ArrayList<IWorkflowInstance> data = new ArrayList<IWorkflowInstance>();
        final String[] lien = { "uril://vdoc/catalogView/DefaultOrganization/Formation/Formation:0/SuiviFormationSuiviProgrammeFormation" };
        try {
            String[] array;
            for (int length = (array = lien).length, i = 0; i < length; ++i) {
                final String string = array[i];
                final IView view = (IView) this.getWorkflowModule().getElementByProtocolURI(string);
                final ByteArrayInputStream bais = new ByteArrayInputStream(view.getXmlDefinition());
                final IContext sysContext = this.getWorkflowModule().getSysadminContext();
                final IOrganization organization = this.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                final IProject project = this.getProjectModule().getProject(sysContext, "Formation", organization);
                final IViewController viewController = this.getWorkflowModule().getViewController(this.getWorkflowModule().getLoggedOnUserContext(), project, (InputStream) bais);
                final Collection<Object> resources = (Collection<Object>) viewController.evaluate();
                for (final Object iResource : resources) {
                    IWorkflowInstance instance = null;
                    if (iResource instanceof ActionTaskInstance) {
                        instance = (IWorkflowInstance) ((ActionTaskInstance) iResource).getWorkflowInstance();
                    } else {
                        instance = (IWorkflowInstance) ((com.axemble.vdp.workflow.domain.ProcessWorkflowInstance) iResource);
                    }
                    data.add(instance);
                }
            }
        } catch (ModuleException e) {
            e.printStackTrace();
        }
        return data;
    }

}
