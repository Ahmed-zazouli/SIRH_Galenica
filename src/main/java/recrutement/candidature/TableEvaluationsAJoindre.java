package recrutement.candidature;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.turbine.Turbine;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.screens.ExternalScreen;

public class TableEvaluationsAJoindre extends BaseDocumentExtension {
	
	
	
	public boolean onAfterLoad() {
		//IWorkflowInstance instance = getWorkflowInstance();
		//String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort()).concat(Turbine.getContextPath().concat(instance.getURI().replace("/easysite/plugin:sys.process/", "/easysite/workplace/processus/edit-document/"))) + "?flag=true";
		//ExternalScreen externalScreen = new ExternalScreen(lien.toString());
		//Navigator.getNavigator().pushScreen(externalScreen);
				IContext context = getWorkflowModule().getSysadminContext();
				Collection<ILinkedResource> typeProduit = (Collection<ILinkedResource>)getWorkflowInstance().getLinkedResources("EvaluationsAJoindre");
				if (typeProduit.isEmpty())
				{
					
					
					// 2eme ligne
					ILinkedResource linkedResource2 = getWorkflowInstance().createLinkedResource("EvaluationsAJoindre");
					linkedResource2.setValue("Entretien", "Entretien Métier 1");
					linkedResource2.save(context);
					getWorkflowInstance().addLinkedResource(linkedResource2);
					
					// 3eme ligne
					ILinkedResource linkedResource3 = getWorkflowInstance().createLinkedResource("EvaluationsAJoindre");
					linkedResource3.setValue("Entretien", "Entretien Métier 2");
					linkedResource3.save(context);
					getWorkflowInstance().addLinkedResource(linkedResource3);
					
					// 4eme ligne
					ILinkedResource linkedResource4 = getWorkflowInstance().createLinkedResource("EvaluationsAJoindre");
					linkedResource4.setValue("Entretien", "Entretien Métier 3");
					linkedResource4.save(context);
					getWorkflowInstance().addLinkedResource(linkedResource4);
					
					// 1ere ligne
					ILinkedResource linkedResource1 = getWorkflowInstance().createLinkedResource("EvaluationsAJoindre");
					linkedResource1.setValue("Entretien", "Entretien RH");
					linkedResource1.save(context);
					getWorkflowInstance().addLinkedResource(linkedResource1);
					
					
					
					getWorkflowInstance().save(context);
				}
		return super.onAfterLoad();
	}
	
	

}