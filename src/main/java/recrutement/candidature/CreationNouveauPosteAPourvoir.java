package recrutement.candidature;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdp.ui.core.document.fields.DateField;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.screens.Screen;
import com.vdoc.sanaEducation.candidatures.document.InformationsCandidat;
public class CreationNouveauPosteAPourvoir extends BaseDocumentExtension {

	
	@Override
	public void onPropertyChanged(IProperty property) {
		// TODO Auto-generated method stub
		//((DateField)this.getDocument().getDefaultWidget("DateDeDemarrageSouhaite")).setStartSelectionRange("02/05/1920");
		if(property.getName().equals("Groupe")){
			IStorageResource storage = 	(IStorageResource)getWorkflowInstance().getValue("Groupe");
			//getWorkflowInstance().setValue("IntroductionGroupe", (String)storage.getValue("IntroductionGroupe"));
		}else if(property.getName().equals("EcoleClient")){
			IStorageResource storage = 	(IStorageResource)getWorkflowInstance().getValue("EcoleClient");
//			getWorkflowInstance().setValue("IntroductionEtablissement", (String)storage.getValue("sys_Title"));
			getWorkflowInstance().setValue("IntroductionEtablissement", storage.getValue("IntroductionEtablissement"));
		}
		super.onPropertyChanged(property);
	}

	@Override
	public boolean onAfterSubmit(IAction action) {
		getWorkflowInstance().save(getWorkflowModule().getLoggedOnUserContext());
		return super.onAfterSubmit(action);
	}
}
