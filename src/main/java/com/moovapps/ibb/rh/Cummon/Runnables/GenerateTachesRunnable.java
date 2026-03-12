package com.moovapps.ibb.rh.Cummon.Runnables;

import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;

import java.util.Collection;
import java.util.Date;

public class GenerateTachesRunnable implements Runnable {
	
	private IAction action;
	private IWorkflowInstance workflowInstance;
	private IWorkflowModule wokflowModule;

	public GenerateTachesRunnable(IAction action, IWorkflowInstance workflowInstance, IWorkflowModule wokflowModule) {
		this.action=action;
		this.workflowInstance=workflowInstance;
		this.wokflowModule=wokflowModule;
	}

	@Override
	public void run() {
		try
		{
			if(action.getName().equals("Valider"))
			{
				if(workflowInstance.getValue("ValidationProjet") != null)
				{
					if(workflowInstance.getValue("ValidationProjet").equals("Validation directe"))
					{
						IContext sysContext = wokflowModule.getSysadminContext();
						IUser chefProjet = (IUser) workflowInstance.getValue("client_reservoir");
						IContext userContext = wokflowModule.getContext(chefProjet);
						IContext internvenantContext = null;
						ICatalog catalog = workflowInstance.getCatalog();
						IWorkflow tacheIWorkflow = wokflowModule.getWorkflow(sysContext, catalog, "GestionDesTaches_1.0");
						IWorkflowInstance workflowInstanceTache = null;
						Collection<ILinkedResource> collection = (Collection<ILinkedResource>) workflowInstance.getLinkedResources("Tache");
						Date debut = null;
						Date fin = null;
						for (ILinkedResource iLinkedResource : collection)
						{
							internvenantContext = wokflowModule.getContext((IUser) iLinkedResource.getValue("Responsable"));
							workflowInstanceTache = wokflowModule.createWorkflowInstance(internvenantContext, tacheIWorkflow, null);
							workflowInstanceTache.setValue("Projet", workflowInstance.getValue("Designation"));
							workflowInstanceTache.setValue("MasterPlan", iLinkedResource.getValue("MasterPlan"));
							workflowInstanceTache.setValue("Client", iLinkedResource.getValue("Client"));
							workflowInstanceTache.setValue("id", iLinkedResource.getValue("id"));
							workflowInstanceTache.setValue("Phase", iLinkedResource.getValue("Phase"));
							workflowInstanceTache.setValue("SousPhase", iLinkedResource.getValue("SousPhase"));
							workflowInstanceTache.setValue("Tache2", iLinkedResource.getValue("Tache"));
							workflowInstanceTache.setValue("NatureTache", iLinkedResource.getValue("NatureTache"));
							workflowInstanceTache.setValue("Responsable", iLinkedResource.getValue("Responsable"));
							workflowInstanceTache.setValue("client_reservoir", workflowInstance.getValue("client_reservoir"));
							workflowInstanceTache.setValue("DateDebut", iLinkedResource.getValue("DateDebut"));
							workflowInstanceTache.setValue("DateFin", iLinkedResource.getValue("DateFin"));
							workflowInstanceTache.setValue("DateDebutActualisee", iLinkedResource.getValue("DateDebut"));
							workflowInstanceTache.setValue("DateFinActualisee", iLinkedResource.getValue("DateFin"));
							workflowInstanceTache.setValue("Intervenant", iLinkedResource.getValue("Intervenant"));
							workflowInstanceTache.setValue("Charge", iLinkedResource.getValue("ChargeJ"));
							iLinkedResource.setValue("uri", workflowInstanceTache.getProtocolURI());
							iLinkedResource.save(internvenantContext);
							debut = (Date) iLinkedResource.getValue("DateDebut");
							fin = (Date) iLinkedResource.getValue("DateFin");
							workflowInstance.addLinkedWorkflowInstance("Taches", workflowInstanceTache);
							workflowInstanceTache.save(internvenantContext);
//							if(iLinkedResource.getValue("Responsable") != null)
//							{
//								ITaskInstance taskInstance = workflowInstance.getCurrentTaskInstance(userContext);
//								ITask task = taskInstance.getTask();
//								IAction iAction = task.getAction("Envoyer");
//								wokflowModule.end(userContext, taskInstance, iAction, "");
//								workflowInstance.save(userContext);
//							}
						}
						workflowInstance.save(userContext);
					}
				}
				
			}
			if(action.getName().equals("Valider2"))
			{
				IContext sysContext = wokflowModule.getSysadminContext();
				IContext userContext = wokflowModule.getLoggedOnUserContext();
				ICatalog catalog = workflowInstance.getCatalog();
				IWorkflow tacheIWorkflow = wokflowModule.getWorkflow(sysContext, catalog, "GestionDesTaches_1.0");
				IWorkflowInstance workflowInstance = null;
				Collection<ILinkedResource> collection = (Collection<ILinkedResource>) workflowInstance.getLinkedResources("Tache");
				Date debut = null;
				Date fin = null;
				for (ILinkedResource iLinkedResource : collection)
				{
					workflowInstance = wokflowModule.createWorkflowInstance(userContext, tacheIWorkflow, null);
					workflowInstance.setValue("Projet", workflowInstance.getValue("Designation"));
					workflowInstance.setValue("MasterPlan", iLinkedResource.getValue("MasterPlan"));
					workflowInstance.setValue("Client", iLinkedResource.getValue("Client"));
					workflowInstance.setValue("id", iLinkedResource.getValue("id"));
					workflowInstance.setValue("Phase", iLinkedResource.getValue("Phase"));
					workflowInstance.setValue("SousPhase", iLinkedResource.getValue("SousPhase"));
					workflowInstance.setValue("Tache2", iLinkedResource.getValue("Tache"));
					workflowInstance.setValue("NatureTache", iLinkedResource.getValue("NatureTache"));
					workflowInstance.setValue("Responsable", iLinkedResource.getValue("Responsable"));
					workflowInstance.setValue("client_reservoir", workflowInstance.getValue("client_reservoir"));
					workflowInstance.setValue("DateDebut", iLinkedResource.getValue("DateDebut"));
					workflowInstance.setValue("DateFin", iLinkedResource.getValue("DateFin"));
					workflowInstance.setValue("Intervenant", iLinkedResource.getValue("Intervenant"));
					workflowInstance.setValue("Charge", iLinkedResource.getValue("ChargeJ"));
					iLinkedResource.setValue("uri", workflowInstance.getProtocolURI());
					iLinkedResource.save(userContext);
					debut = (Date) iLinkedResource.getValue("DateDebut");
					fin = (Date) iLinkedResource.getValue("DateFin");
					workflowInstance.addLinkedWorkflowInstance("Taches", workflowInstance);
					workflowInstance.save(userContext);
					if(iLinkedResource.getValue("Responsable") != null)
					{
						ITaskInstance taskInstance = workflowInstance.getCurrentTaskInstance(userContext);
						ITask task = taskInstance.getTask();
						IAction iAction = task.getAction("Envoyer");
						wokflowModule.end(userContext, taskInstance, iAction, "");
						workflowInstance.save(userContext);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
