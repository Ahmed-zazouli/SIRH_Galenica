package com.moovapps.capone.rh.GestionDeConge.agent;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;

import java.util.Collection;

public class SoldeCongesAgent extends BaseAgent {

	@Override
	protected void execute() {
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			Collection<IUser> collaborateurs = (Collection<IUser>) getDirectoryModule().getUsers(context);
			for (IUser collab : collaborateurs) {
				float soldeConges = 0, soldeCongeAnneeEnCours = 0, droitMensuelle = 0 ;
				if (collab.getExtendedAttributes().getValue("SoldeConges") != null) {
					soldeConges = (Float) collab.getExtendedAttributes().getValue("SoldeConges");
				}
				if (collab.getExtendedAttributes().getValue("SoldeAnneeEnCours") != null) {
					soldeCongeAnneeEnCours = (Float) collab.getExtendedAttributes().getValue("SoldeAnneeEnCours");
				}
				if (collab.getExtendedAttributes().getValue("DroitMensuelle") != null) {
					droitMensuelle = (Float) collab.getExtendedAttributes().getValue("DroitMensuelle");
				}
				collab.getExtendedAttributes().setValue("SoldeAnneeEnCours", soldeCongeAnneeEnCours + droitMensuelle);
				collab.getExtendedAttributes().setValue("SoldeConges", soldeConges + droitMensuelle);
				collab.save(context);
				IStorageResource ficheUser = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(collab, getWorkflowModule(), getProjectModule(), getDirectoryModule().getOrganization(context, "DefaultOrganization"));
				ficheUser.save(context);
			}
		} catch (Exception e) {

		}
	}
}
