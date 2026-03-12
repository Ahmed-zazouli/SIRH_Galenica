package com.moovapps.ibb.rh.AjoutSalarie.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

public class AjoutSalarie extends BaseDocumentExtension {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	IWorkflowInstance document = null;
	
	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		return super.onAfterLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property) {
		if(property.getName().equals("Societe")){
			setOrganization();
		}
		super.onPropertyChanged(property);
	}
	
	private void setOrganization() {
		String societeName = null;
		if(document.getValue("Societe") != null){
			societeName = (String)document.getValue("Societe");
		}
		IStorageResource societe = null;
		document.setValue("Organisation", societe != null ? societe.getValue("Organisation") : null);
//		IUser x = getDirectoryModule().createUser(context,login,passwor,org);
	}
	
/*	@SuppressWarnings("unchecked")
	public IStorageResource getSociete(String societeName) {
		IStorageResource societe = null;
		if(societeName != null){			
			try {
				IContext context = getWorkflowModule().getSysadminContext();
				IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
				IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",getWorkflowInstance().getCatalog().getProject().getOrganization());
				ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
				IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
				controller.addEqualsConstraint("sys_Title", societeName);
				Collection<IStorageResource> societes = controller.evaluate(definition);
				if (!societes.isEmpty()){				
					societe = societes.iterator().next();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return societe;
	}*/

}