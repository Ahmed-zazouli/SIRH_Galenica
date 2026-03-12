package com.moovapps.ibb.rh.Securite;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IUser;

public class Formulaire extends BaseDocumentExtension {
	
	@Override
	public boolean onAfterLoad() {
		getWorkflowInstance().setValue("Salarie", getWorkflowModule().getLoggedOnUser());
		getWorkflowInstance().setValue("JAccepte", false);
		return super.onAfterLoad();
	}
	
	@Override
	public boolean onAfterSave() {
		if((boolean)getWorkflowInstance().getValue("JAccepte")){
			IUser salarie = (IUser)getWorkflowInstance().getValue("Salarie");
			salarie.getExtendedAttributes().setValue("AllowAccess", true);
			salarie.save(getWorkflowModule().getSysadminContext());
		}
		return super.onAfterSave();
	}

}
