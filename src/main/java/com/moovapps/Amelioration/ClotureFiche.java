package com.moovapps.Amelioration;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
//import com.moovapps.Orga.Helpers.AddFilesToDoCenter;

public class ClotureFiche extends BaseDocumentExtension{
@Override
public boolean onBeforeSubmit(IAction action) {

	// TODO Auto-generated method stub
	/*if(action.getName().equals("CloturerLaFiche")){
		HashMap<String, String> fieldsPaths = new HashMap<String, String>();
		//fieldsPaths.put("PieceJoint", "_Societe/_Departements/_Titre/_Reference");
		AddFilesToDoCenter FilesToDoCenter = new AddFilesToDoCenter();
		FilesToDoCenter.AddFileToDocCenter("Documents SMQ", getWorkflowModule(), getDirectoryModule(), getWorkflowInstance(), fieldsPaths);
	}*/
	return super.onBeforeSubmit(action);
}
}

