package com.moovapps.capone.rh.GestionDeConge.agent;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;

import java.util.Collection;

public class YearlyCongesAgent extends BaseAgent {

	@Override
	protected void execute() {
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			Collection<IUser> collaborateurs = (Collection<IUser>) getDirectoryModule().getUsers(context);
			for (IUser collab : collaborateurs) {
				setCongesAnterieur(collab);
				setCongesPayesAnterieur(collab);
				setCongesSpeciauxAnterieur(collab);
				setCongesMaladieAnterieur(collab);
				setCongesSansSoldeAnterieur(collab);
				setAbsencesAnterieur(collab);
				collab.save(context);
				IStorageResource ficheUser = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(collab, getWorkflowModule(), getProjectModule(), getDirectoryModule().getOrganization(context, "DefaultOrganization"));
				ficheUser.save(context);
			}
		} catch (Exception e) {

		}
	}
	
	private void setCongesAnterieur(IUser collab) {
		Float congesAnneeEnCours = 0f;
		Float congesAnneeAnterieur = 0f;
		if(collab.getExtendedAttributes().getValue("SoldeAnneeEnCours") != null){
			congesAnneeEnCours = (Float)collab.getExtendedAttributes().getValue("SoldeAnneeEnCours");
		}
		if(collab.getExtendedAttributes().getValue("SoldeAnterieur") != null){
			congesAnneeAnterieur = (Float)collab.getExtendedAttributes().getValue("SoldeAnterieur");
		}
		collab.getExtendedAttributes().setValue("SoldeAnneeEnCours", 0);
		collab.getExtendedAttributes().setValue("SoldeAnterieur", congesAnneeAnterieur + congesAnneeEnCours);
	}
	
	private void setCongesPayesAnterieur(IUser collab) {
		Float congesPayesAnneeEnCours = 0f;
		Float congesPayesAnneeAnterieur = 0f;
		if(collab.getExtendedAttributes().getValue("CongesPayesAnneeEnCours") != null){
			congesPayesAnneeEnCours = (Float)collab.getExtendedAttributes().getValue("CongesPayesAnneeEnCours");
		}
		if(collab.getExtendedAttributes().getValue("CongesPayesN1") != null){
			congesPayesAnneeAnterieur = (Float)collab.getExtendedAttributes().getValue("CongesPayesN1");
		}
		collab.getExtendedAttributes().setValue("CongesPayesAnneeEnCours", 0);
		collab.getExtendedAttributes().setValue("CongesPayesN1", congesPayesAnneeAnterieur + congesPayesAnneeEnCours);
	}
	
	private void setCongesSpeciauxAnterieur(IUser collab) {
		Float congesSpeciauxAnneeEnCours = 0f;
		Float congesSpeciauxAnneeAnterieur = 0f;
		if(collab.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours") != null){
			congesSpeciauxAnneeEnCours = (Float)collab.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours");
		}
		if(collab.getExtendedAttributes().getValue("CongesSpeciauxN1") != null){
			congesSpeciauxAnneeAnterieur = (Float)collab.getExtendedAttributes().getValue("CongesSpeciauxN1");
		}
		collab.getExtendedAttributes().setValue("CongesSpeciauxAnneeEnCours", 0);
		collab.getExtendedAttributes().setValue("CongesSpeciauxN1", congesSpeciauxAnneeAnterieur + congesSpeciauxAnneeEnCours);
	}
	
	private void setCongesMaladieAnterieur(IUser collab) {
		Float congesMaladieAnneeEnCours = 0f;
		Float congesMaladieAnneeAnterieur = 0f;
		if(collab.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours") != null){
			congesMaladieAnneeEnCours = (Float)collab.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours");
		}
		if(collab.getExtendedAttributes().getValue("CongesMaladieN1") != null){
			congesMaladieAnneeAnterieur = (Float)collab.getExtendedAttributes().getValue("CongesMaladieN1");
		}
		collab.getExtendedAttributes().setValue("CongesMaladieAnneeEnCours", 0);
		collab.getExtendedAttributes().setValue("CongesMaladieN1", congesMaladieAnneeAnterieur + congesMaladieAnneeEnCours);
	}
	
	private void setCongesSansSoldeAnterieur(IUser collab) {
		Float congesSansSoldeAnneeEnCours = 0f;
		Float congesSansSoldeAnneeAnterieur = 0f;
		if(collab.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours") != null){
			congesSansSoldeAnneeEnCours = (Float)collab.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours");
		}
		if(collab.getExtendedAttributes().getValue("CongesSansSoldeN1") != null){
			congesSansSoldeAnneeAnterieur = (Float)collab.getExtendedAttributes().getValue("CongesSansSoldeN1");
		}
		collab.getExtendedAttributes().setValue("CongesSansSoldeAnneeEnCours", 0);
		collab.getExtendedAttributes().setValue("CongesSansSoldeN1", congesSansSoldeAnneeEnCours + congesSansSoldeAnneeAnterieur);
	}
	
	private void setAbsencesAnterieur(IUser collab) {
		Float absenceAnneeEnCours = 0f;
		Float absenceAnneeAnterieur = 0f;
		if(collab.getExtendedAttributes().getValue("AbsenceAnneeEnCours") != null){
			absenceAnneeEnCours = (Float)collab.getExtendedAttributes().getValue("AbsenceAnneeEnCours");
		}
		if(collab.getExtendedAttributes().getValue("AbsenceAnterieure") != null){
			absenceAnneeAnterieur = (Float)collab.getExtendedAttributes().getValue("AbsenceAnterieure");
		}
		collab.getExtendedAttributes().setValue("AbsenceAnneeEnCours", 0);
		collab.getExtendedAttributes().setValue("AbsenceAnterieure", absenceAnneeEnCours + absenceAnneeAnterieur);
	}

}
