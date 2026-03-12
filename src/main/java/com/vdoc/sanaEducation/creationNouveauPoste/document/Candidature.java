package com.vdoc.sanaEducation.creationNouveauPoste.document;

import java.util.Collection;

import javax.print.DocFlavor.STRING;

import org.apache.turbine.Turbine;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.interfaces.IWorkflow;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.screens.ExternalScreen;

public class Candidature extends BaseDocumentExtension
{
	
	@Override
	public boolean onAfterLoad()
	{
		Collection<IWorkflowInstance> collection = (Collection<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("Candidature");
		if(collection.isEmpty())
		{
			getAllCandidats(getWorkflowInstance());
		}
		//hna
		
		//Navigator.getNavigator().pushScreen(externalScreen);

	    
		return super.onAfterLoad();
	}
	
	
	private Collection<IWorkflowInstance> getAllCandidats(IWorkflowInstance instance)
	{
		
		try
		{
			
			IContext context = getWorkflowModule().getSysadminContext();
			IContext userContext = getWorkflowModule().getLoggedOnUserContext();
			userContext.getUser().getFullName();
			IWorkflowInstance newInstance = null;
			IWorkflow cvthequeWorkflow = getWorkflowModule().getWorkflow(context, instance.getCatalog(), "CVTHEQUE_1.1");
			IViewController controller = getWorkflowModule().getViewController(context);
			IWorkflow workflow = getWorkflowModule().getWorkflow(context, getWorkflowInstance().getCatalog(), "Candidature_1.0");
			Collection<IWorkflowInstance> collection = controller.evaluate(cvthequeWorkflow);
			int note = 0;
			Collection<IWorkflowInstance> collection2 = (Collection<IWorkflowInstance>) instance.getLinkedWorkflowInstances("Candidature");
			for (IWorkflowInstance workflowInstance : collection)
			{
				note = 0;
//				if(workflowInstance.getValue("Date1erPoste").equals(instance.getValue("Date1erPoste")))
//				{
//					note++;
//				}
//				if(workflowInstance.getValue("DateDeNaissance").equals(instance.getValue("DateDeNaissance")))
//				{
//					note++;
//				}
//				if(workflowInstance.getValue("Prenom").equals(instance.getValue("Prenom")))
//				{
//					note++;
//				}
//				if(workflowInstance.getValue("Nom").equals(instance.getValue("Nom")))
//				{
//					note++;
//				}
				if(workflowInstance.getValue("FonctionActuelle") != null && workflowInstance.getValue("FonctionActuelle").equals(instance.getValue("FonctionActuelle")))
				{
					note++;
				}
//				if(String.valueOf(workflowInstance.getValue("MatieresEnseignees")) != null && String.valueOf(instance.getValue("MatieresEnseignees")) != null)
//				{
//					if(workflowInstance.getValue("MatieresEnseignees").equals(instance.getValue("MatieresEnseignees")))
//					{
//						note++;
//					}
//				}
				if(workflowInstance.getValue("NiveauDEtudes") != null && workflowInstance.getValue("NiveauDEtudes").equals(instance.getValue("NiveauDEtudes")))
				{
					note++;
				}
				if(workflowInstance.getValue("TypeDeDiplome") != null && workflowInstance.getValue("TypeDeDiplome").equals(instance.getValue("TypeDeDiplome")))
				{
					note++;
				}
				if(workflowInstance.getValue("TrancheAnneeExperience") != null && workflowInstance.getValue("TrancheAnneeExperience").equals(instance.getValue("TrancheAnneeExperience")))
				{
					note++;
				}
				if(workflowInstance.getValue("EtablissementFrequentes") != null && workflowInstance.getValue("EtablissementFrequentes").equals(instance.getValue("EtablissementFrequentes")))
				{
					note++;
				}
				if(note>1 && collection2.isEmpty())
				{
					newInstance = getWorkflowModule().createWorkflowInstance(userContext, workflow, "");
					newInstance.setValue("Nom", workflowInstance.getValue("Nom"));
					newInstance.setValue("Prenom", workflowInstance.getValue("Prenom"));
					newInstance.setValue("DateDeNaissance", workflowInstance.getValue("DateDeNaissance"));
					newInstance.setValue("Date1erPoste", workflowInstance.getValue("Date1erPoste"));
					newInstance.setValue("FonctionActuelle", workflowInstance.getValue("FonctionActuelle"));
					newInstance.setValue("MatieresEnseignees", workflowInstance.getValue("MatieresEnseignees"));
					newInstance.setValue("NiveauDEtudes", workflowInstance.getValue("NiveauDEtudes"));
					newInstance.setValue("TypeDeDiplome", workflowInstance.getValue("TypeDeDiplome"));
					newInstance.setValue("TrancheAnneeExperience", workflowInstance.getValue("TrancheAnneeExperience"));
					newInstance.setValue("EtablissementFrequentes", workflowInstance.getValue("EtablissementFrequentes"));
					newInstance.setValue("CVCandidat", workflowInstance.getValue("CVCandidat"));
					newInstance.setValue("LettreMotivation", workflowInstance.getValue("LettreMotivation"));
					newInstance.setValue("FicheEvaluation2", workflowInstance.getValue("FicheEvaluation2"));
					newInstance.setValue("classement", note);
					newInstance.save(userContext);
					instance.addLinkedWorkflowInstance("Candidature", newInstance);
					RemplirTableauxLague(newInstance, workflowInstance);
					RemplirTableauxDernierPosteCandidat(newInstance, workflowInstance);
					instance.save(userContext);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
		
		
	}
	
	
	private void RemplirTableauxLague(IWorkflowInstance instance , IWorkflowInstance workflowInstance)
	{
		
		Collection<ILinkedResource> collection = (Collection<ILinkedResource>) workflowInstance.getLinkedResources("Langue");
		for (ILinkedResource iLinkedResource : collection)
		{
			ILinkedResource resource = instance.createLinkedResource("Langue");
			instance.addLinkedResource(resource);
			resource.setValue("Langue", iLinkedResource.getValue("Langue"));
			resource.setValue("NiveauDeLangue", iLinkedResource.getValue("NiveauDeLangue"));
			resource.save(getWorkflowModule().getSysadminContext());
			instance.save(getWorkflowModule().getSysadminContext());
		}
	}
	
	private void RemplirTableauxDernierPosteCandidat(IWorkflowInstance instance , IWorkflowInstance workflowInstance)
	{
		
		Collection<ILinkedResource> collection = (Collection<ILinkedResource>) workflowInstance.getLinkedResources("DernierPosteCandidat");
		for (ILinkedResource iLinkedResource : collection)
		{
			ILinkedResource resource = instance.createLinkedResource("DernierPosteCandidat");
			instance.addLinkedResource(resource);
			resource.setValue("Etablissement", iLinkedResource.getValue("Etablissement"));
			resource.setValue("PosteOccupe", iLinkedResource.getValue("PosteOccupe"));
			resource.setValue("Periode", iLinkedResource.getValue("Periode"));
			resource.save(getWorkflowModule().getSysadminContext());
			instance.save(getWorkflowModule().getSysadminContext());
		}
	}
	
	
}
