package com.moovapps.ibb.rh.Securite;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IResourceController;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.axemble.vdp.ui.framework.widgets.CtlText;

import java.util.List;

public class HappyBirthdayMerci extends BaseDocumentExtension {
	
	@Override
	public boolean onAfterLoad() {
		masquerBoutton();
		return super.onAfterLoad();
	}
	
	private void masquerBoutton() {
		try {
			IResourceController iResourceController = getResourceController();
			NamedContainer namedContainer = iResourceController.getButtonContainer(2);
			List<IWidget> widgets = namedContainer.getWidgets();
			for (IWidget iWidget : widgets) {
				if (((CtlButton) iWidget).getName().toLowerCase().equals("saveandclose")) {
					((CtlButton) iWidget).setLabel(new CtlText("Merci"));
					continue;
				}
				CtlButton button = (CtlButton) iWidget;
				button.setHidden(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	@Override
	public boolean onAfterSave() {
		getWorkflowModule().getLoggedOnUser().getExtendedAttributes().setValue("MerciBirthday", true);
		getWorkflowModule().getLoggedOnUser().save(getWorkflowModule().getSysadminContext());
		return super.onAfterSave();
	}

}
