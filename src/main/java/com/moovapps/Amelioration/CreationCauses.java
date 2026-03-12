package com.moovapps.Amelioration;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
//import com.moovapps.Orga.Helpers.SaveStorageRessource;

public class CreationCauses extends BaseDocumentExtension{
	
	@Override
	public boolean onAfterLoad() {
		
		if(!getWorkflowModule().getLoggedOnUser().equals(getWorkflowInstance().getValue("sys_Creator")))
		{
			IResourceController iResourceController = getResourceController();
		      NamedContainer namedContainer = iResourceController.getButtonContainer(2);
		      List<IWidget> widgets = namedContainer.getWidgets();
		      for (IWidget iWidget : widgets){
		         CtlButton button = (CtlButton)iWidget;
//				if(button.getName().equals("Changer le responsable")){
					//button.setHidden(true);
//				}
		      }
		}

		return super.onAfterLoad();
	}
	
	
	
	
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		
		try {                           
			if(action.getName().equals("CreerLesCausesRacines"))
			{
				ArrayList<ILinkedResource> causes = (ArrayList<ILinkedResource>) getWorkflowInstance().getValue("AnalyseDesCauses");
				if(causes!=null)
				{
					for (ILinkedResource iLinkedResource : causes) {
						ArrayList<String> causeList = new ArrayList<>();
						causeList.add((String) iLinkedResource.getValue("_1erPourquoi"));
						causeList.add((String) iLinkedResource.getValue("_2emePourquoi"));
						causeList.add((String) iLinkedResource.getValue("_3emePourquoi"));
						causeList.add((String) iLinkedResource.getValue("_4emePourquoi"));
						causeList.add((String) iLinkedResource.getValue("_5emePourquoi"));
						causeList.add((String) iLinkedResource.getValue("_6emePourquoi"));
						causeList.add((String) iLinkedResource.getValue("_7emePourquoi"));
						causeList.add((String) iLinkedResource.getValue("_8emePourquoi"));
						causeList.add((String) iLinkedResource.getValue("_9emePourquoi"));
						causeList.add((String) iLinkedResource.getValue("_10emePourquoi"));						
						causeList.removeAll(Collections.singleton(null));
						iLinkedResource.setValue("CausesPriorisees", !causeList.isEmpty()? causeList.get(causeList.size()-1):null);
						iLinkedResource.save(getWorkflowModule().getSysadminContext());
						getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
						
						if(!causeList.isEmpty()){
							IContext sysContext = Modules.getWorkflowModule().getSysadminContext();
							IContext Context = Modules.getWorkflowModule().getLoggedOnUserContext();
					        IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
							IProject project = Modules.getProjectModule().getProject(sysContext, "WIZEORGA", organization);
					        ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
							IResourceDefinition iResourceDefinition = Modules.getWorkflowModule().getResourceDefinition(sysContext, catalog, "CauseRacine");	
							IStorageResource iStorageResource = Modules.getWorkflowModule().createStorageResource(Context, iResourceDefinition, null);
							iStorageResource.setValue("sys_Title",  causeList.get(causeList.size()-1));
							iStorageResource.setValue("ID", getWorkflowInstance().getId().toString());
							iStorageResource.save(Context);
							getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
						}
						
						
					}
				}
			}
		} catch (Exception e) {
		}
	
		return super.onBeforeSubmit(action);
	}
}
