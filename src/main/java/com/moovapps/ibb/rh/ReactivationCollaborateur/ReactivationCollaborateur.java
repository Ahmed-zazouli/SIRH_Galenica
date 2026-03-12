package com.moovapps.ibb.rh.ReactivationCollaborateur;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;

public class ReactivationCollaborateur extends BaseDocumentExtension {
	
	@Override
	public boolean onAfterLoad() {
		fillSalarieField();
		return super.onAfterLoad();
	}
	
	private void fillSalarieField() {
		Modules.getDirectoryModule().getUsers(getWorkflowModule().getSysadminContext());
	}

}
