package com.vdoc.sanaEducation.EvaluationCollaborateur;

import java.util.Collection;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IViewController;

public class Entretien extends BaseDocumentExtension {

	private static final long serialVersionUID = 268268688691556L;
	
	@Override
	public boolean onAfterLoad() 
	  
	{
		Collection<ILinkedResource> linkedResource = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("HardSkills");
		Collection<ILinkedResource> linkedResource2 = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("SoftSkills");
		IContext context = getWorkflowModule().getSysadminContext();

		
		IUser user = getWorkflowModule().getLoggedOnUser();
		IUser user2 = (IUser) getWorkflowInstance().getValue("Collaborateur");
		if (getWorkflowInstance().getValue("Collaborateur") != null) {
			getResourceController().showBodyBlock("Collaborateur", !user2.getFullName().equals(user.getFullName()));
				
		}
		if(linkedResource.isEmpty())
		{
			Collection<IStorageResource> hardSkillsCollection =  GetCritereByCategorie("Hard skills");
			if(hardSkillsCollection != null)
			{
				for (IStorageResource iStorageResource : hardSkillsCollection) 
				{
				ILinkedResource iLinkedResource = getWorkflowInstance().createLinkedResource("HardSkills");
				getWorkflowInstance().addLinkedResource(iLinkedResource);
				iLinkedResource.setValue("Critere",iStorageResource.getValue("Critere"));
				iLinkedResource.save(context);
				getWorkflowInstance().save(context);
				}
			}
		}
		if(linkedResource2.isEmpty())
		{
			Collection<IStorageResource> SoftSkillsCollection =  GetCritereByCategorie("Soft skills");
			if(SoftSkillsCollection != null)
			{
				for (IStorageResource iStorageResource : SoftSkillsCollection) 
				{
				ILinkedResource iLinkedResource = getWorkflowInstance().createLinkedResource("SoftSkills");
				getWorkflowInstance().addLinkedResource(iLinkedResource);
				iLinkedResource.setValue("Critere",iStorageResource.getValue("Critere"));
				iLinkedResource.save(context);
				getWorkflowInstance().save(context);
				}
			}
		}
		
		return super.onAfterLoad();
	}
	/*
	@Override
	public void onPropertyChanged(IProperty property) {
		if (property.getName().equals("Collaborateur")) {
		IUser user = (IUser) getWorkflowInstance().getValue(("Collaborateur"));
		getWorkflowInstance().setValue("Nom", user.getLastName());
		getWorkflowInstance().setValue("Prenom", user.getFirstName());
		}

		  
		
		super.onPropertyChanged(property);
	}
	
	*/
	
	public Collection<IStorageResource>  GetCritereByCategorie (String cat){
		Collection <IStorageResource> Collection = null ;
		try {
		IProject project = getWorkflowInstance().getCatalog().getProject();
		IContext context = getWorkflowModule().getSysadminContext();
		ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL" , ICatalog.IType.STORAGE, project);
		IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Criteres");	
			IViewController viewController = getWorkflowModule().getViewController(context, IResource.class);
			viewController.addEqualsConstraint("Categorie", cat);
			Collection = viewController.evaluate(definition);
			if(!Collection.isEmpty())
			{
				return Collection;
			}
//			for (IStorageResource iStorageResource : Collection) {
//				if (cat == "hard skills") {
//					
//				} else  {
//					ILinkedResource iLinkedResource = getWorkflowInstance().createLinkedResource("SoftSkills");
//					getWorkflowInstance().addLinkedResource(iLinkedResource);
//					iLinkedResource.setValue("Categorie", iStorageResource.getValue("Categorie"));
//					iLinkedResource.save(context);
//					getWorkflowInstance().save(context);				}
//				
//			}
		} catch (Exception e) {
			
		}
		return null;
	}
	
	void CALCULTOTAL () {
		
				Collection<ILinkedResource> linkedResource = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("HardSkills");
				String Resultat = null ;
				Float Somme = 0F ;
				Float number = 0F;
				
				for (ILinkedResource iLinkedResource : linkedResource) {
					Resultat =	(String) iLinkedResource.getValue("Resultat");
					number = Float.valueOf(Resultat);
					Somme += number ;
				}	
				getWorkflowInstance().setValue("RaitingHardSkills", number);
			
	}
	

}
