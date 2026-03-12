package com.moovapps.Amelioration;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;

import java.util.ArrayList;

public class CreationFicheAmelioration extends BaseDocumentExtension {

	@Override
	public boolean onBeforeSubmit(IAction action) {

		return super.onBeforeSubmit(action);
	}

	@Override
	public void onPropertyChanged(IProperty property) {
		try {

			if (property.getName().equals("ProcessussConcernes")) {
				getWorkflowInstance().setValue("UtilisateursConcernes", null);
				ArrayList<IStorageResource> processus = (ArrayList<IStorageResource>) getWorkflowInstance().getValue("ProcessussConcernes");
				ArrayList<IUser> pilotes = new ArrayList<>();
				if(processus!=null && !processus.isEmpty()){
					for(IStorageResource proc : processus){
						IUser pilote = (IUser) proc.getValue("Pilote");
						if(pilote!=null){
							pilotes.add(pilote);
						}
					}
				}
				getWorkflowInstance().setValue("UtilisateursConcernes", pilotes);
				getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		super.onPropertyChanged(property);
	}

}
