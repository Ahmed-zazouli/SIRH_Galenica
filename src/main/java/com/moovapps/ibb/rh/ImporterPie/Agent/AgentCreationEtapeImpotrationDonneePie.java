package com.moovapps.ibb.rh.ImporterPie.Agent;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Collection;
import java.util.Date;

public class AgentCreationEtapeImpotrationDonneePie extends BaseAgent {

    @Override
    protected void execute() {
        Collection<IStorageResource> allSocietes = getAllSocietes();
        for (IStorageResource societe : allSocietes){
        IWorkflowInstance instance = createInstance();
        instance.setValue("DateDeCreation",new Date());
        instance.setValue("Societe" , societe);
        instance.setValue("ResponsableRH", societe.getValue("ResponsableRH"));

        instance.save(getWorkflowModule().getSysadminContext());

            try {
                ITaskInstance iTaskInstance = instance.getCurrentTaskInstance(getWorkflowModule().getSysadminContext());
                ITask task = iTaskInstance.getTask();
                IAction iAction1 = task.getAction("CommoncerLImportationDonnerPie");
                getWorkflowModule().end(getWorkflowModule().getSysadminContext(), iTaskInstance, iAction1, "");
                instance.save(getWorkflowModule().getSysadminContext());

            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    }

    private Collection<IStorageResource> getAllSocietes()  {
        Collection<IStorageResource> allSocietes = null ;
        try {
        IContext context = getWorkflowModule().getSysadminContext();
        IOrganization organization = getDirectoryModule().getOrganization(context , "DefaultOrganization");
        IProject project= getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
        ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,project);
        IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"Societe");
        IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
        allSocietes =   controller.evaluate(definition);
    }catch (Exception ex){
            ex.printStackTrace();
        }
        return allSocietes ;
    }

    public IWorkflowInstance createInstance(){
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject project = getProjectModule().getProject(context,"RemonteVariableDeLaPaie",organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,"ImportationDesDonneesDeLaPie", ICatalog.IType.NORMAL,project);
            IWorkflow workflow = getWorkflowModule().getWorkflow(context,catalog,"ImportationDesDonneesDeLaPie_1.0");
            IWorkflowInstance instance = getWorkflowModule().createWorkflowInstance(context,workflow,"");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            return instance;

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

   /* private Collection<IWorkflowInstance> getImportationDesDonneesDeLaPie() {
        try {
            IOrganization organization = getDirectoryModule().getOrganization(getWorkflowModule().getSysadminContext(), "DefaultOrganization");
            IProject project = getProjectModule().getProject(getWorkflowModule().getSysadminContext(), "RemonteVariableDeLaPaie", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(getWorkflowModule().getSysadminContext(), "ImportationDesDonneesDeLaPie", project);
            IViewController controller = getWorkflowModule().getViewController(getWorkflowModule().getSysadminContext());
            controller.addInConstraint("sys_WorkflowContainer", Arrays.asList("ImportationDesDonneesDeLaPie"));
            Calendar cStart = Calendar.getInstance();
            cStart.set(Calendar.HOUR_OF_DAY, 0);
            cStart.set(Calendar.MINUTE, 0);
            cStart.set(Calendar.SECOND, 0);
            Calendar cEnd = Calendar.getInstance();
            cEnd.add(Calendar.DATE, 1);
            cEnd.set(Calendar.HOUR_OF_DAY, 0);
            cEnd.set(Calendar.MINUTE, 0);
            cEnd.set(Calendar.SECOND, 0);

            return controller.evaluate(catalog);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
*/

}
