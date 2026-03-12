package com.moovapps.Amelioration;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IResourceController;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;

import java.util.List;

public class CreationPlanActionDeLaFiche extends BaseDocumentExtension{
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
}
