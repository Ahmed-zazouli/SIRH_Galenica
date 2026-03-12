package com.moovapps.ibb.rh.FicheSalarie.Formulaire;

import com.axemble.vdoc.sdk.interfaces.IStorageKey;
import com.axemble.vdoc.sdk.workflow.extensions.BaseResourceDefinitionExtension;

public class CollaborateurAudit extends BaseResourceDefinitionExtension {
	
	@Override
	public void onRemove(IStorageKey key) {
//		try {
//			Resource resource = (Resource) getResource();
//			IUser salarie = (IUser) resource.getValue("Salarie");
//			salarie.setExit(new Date());
//			salarie.disable();
//			salarie.save(getWorkflowModule().getSysadminContext());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
}