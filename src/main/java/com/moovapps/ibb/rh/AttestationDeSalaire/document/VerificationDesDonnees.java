package com.moovapps.ibb.rh.AttestationDeSalaire.document;

import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IProperty;

public class VerificationDesDonnees extends CreationDemande {
	
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		setDemandeurFields();
		return true;
	} 

	public void onPropertyChanged(IProperty property) {}
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		return true;
	}


}
