package recrutement.candidature.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class DeleteCandidatures extends BaseAgent {
    @Override
    protected void execute() {
        Collection<IWorkflowInstance> Postes = getData();
        for(IWorkflowInstance instance:Postes){

            instance.setValue("NiveauDEtudeMulti",null);
            instance.setValue("NiveauDEtudes",null);
            instance.save(getWorkflowModule().getSysadminContext());
            Collection<IWorkflowInstance> candidatures =  (Collection<IWorkflowInstance>) instance.getLinkedWorkflowInstances("Candidature");
            for(IWorkflowInstance candidature:candidatures){
                candidature.setValue("NiveauDEtude",null);
                candidature.setValue("NiveauDEtudes",null);
                candidature.save(getWorkflowModule().getSysadminContext());
            }

        }
    }

    private Collection<IWorkflowInstance> getData()
    {
        Collection<IWorkflowInstance> collection = Collections.emptyList();
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IUser connectedUser = getWorkflowModule().getLoggedOnUser();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Recrutement", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Recrutement", project);
            IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "CreationDUnNouveauPosteAPourvoir_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            // controller.addEqualsConstraint("Demandeur", connectedUser);
           // controller.addNotInConstraint("DocumentState", Arrays.asList("En cours","Terminé","Evaluation RH défavorable","Refus de l'offre","Candidature rejeté"));
            collection = controller.evaluate(w);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return collection;
    }
}
