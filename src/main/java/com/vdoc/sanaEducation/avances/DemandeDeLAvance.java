package com.vdoc.sanaEducation.avances;

import java.util.Collection;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.ICatalog;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IProject;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IResourceDefinition;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IViewController;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;

public class DemandeDeLAvance extends BaseDocumentExtension {

	private static final long serialVersionUID = -2241863542605217291L;
/*	
	@Override
	public void onPropertyChanged(IProperty property) {
		if (property.getName().equals("Ecole")) {
			if (getWorkflowInstance().getValue("Ecole") != null) {
				
				IStorageResource storage = GetEcole((String)getWorkflowInstance().getValue("Ecole")) ;
				storage.getValue("DirecteurPedagogique");
				storage.getValue("DirecteurEcole");
				//getWorkflowInstance().setValue("DirecteurDeLEtablissement", arg1);
				//getWorkflowInstance().setValue("DirecteurDesRessourcesHumaines", arg1);
			}
		}
		
		super.onPropertyChanged(property);
	}
	*/
	
	@Override
	public boolean onAfterLoad() {
		String ecole = (String) getWorkflowInstance().getValue("Ecole");
		if (ecole != null) 
		{
			IStorageResource storage = GetEcole(ecole) ;
			getWorkflowInstance().setValue("DirEtablissement", storage.getValue("DirecteurEcole"));
		//	getWorkflowInstance().setValue("DirRessoutceHumaines", storage.getValue("DirecteurPedagogique"));
		}
		return super.onAfterLoad();
	}
	
	private IStorageResource GetEcole(String Ecole)
	{
		try
		{ 
			IWorkflowModule iWorkflowModule = getWorkflowModule();
			IContext sysAdminContext = iWorkflowModule.getSysadminContext();
			IProject iProject = getWorkflowInstance().getWorkflow().getCatalog().getProject();
			ICatalog catalog = iWorkflowModule.getCatalog(sysAdminContext, "REFERENTIEL", ICatalog.IType.STORAGE, iProject);
			IResourceDefinition resourceDefinition = iWorkflowModule.getResourceDefinition(sysAdminContext, catalog, "Ecole");
			catalog.getName();
			IViewController viewController = iWorkflowModule.getViewController(sysAdminContext, IResource.class);
			viewController.addEqualsConstraint("sys_Title", Ecole);
			Collection<IStorageResource> storageResources = viewController.evaluate(resourceDefinition);
	
			if(storageResources != null)
			{
				if(!storageResources.isEmpty())
				{
					return storageResources.iterator().next();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	
		
	}
	
	

}
