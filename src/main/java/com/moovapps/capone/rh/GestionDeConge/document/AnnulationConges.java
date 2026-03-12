package com.moovapps.capone.rh.GestionDeConge.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;

public class AnnulationConges extends BaseDocumentExtension {
	
	IWorkflowInstance document = null;
	IUser demandeur = null;
	
	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		demandeur = (IUser)document.getValue("Demandeur");
		return super.onAfterLoad();
	}
	
	@Override
	public boolean onAfterSubmit(IAction action) {
		if(action.getName().equals("AccepterLaDemandeDAnnulationDeDeConge") || action.getName().equals("AccepterLaDemandeDAnnulationDeDeConge")){
			onAnnulerConge();
		}
		return super.onAfterSubmit(action);
	}
	
	private void onAnnulerConge() {
		String categorieConges = (String)document.getValue("TypeDeConge");
		Float nbrJoursDemandes = (Float)document.getValue("NombreDeJoursDemandes");
		Float nbrJoursExceptionnelle = (Float)document.getValue("NombreDeJoursExceptionnelle");
		if(categorieConges.equals("CN")){
			setDemandeurCongesPayesInfo(nbrJoursDemandes);
			calculateCommunCongesFields(nbrJoursDemandes);
		} else if(categorieConges.equals("CE")){
			setDemandeurCongesSpeciauxInfo(nbrJoursExceptionnelle);
		} else if(categorieConges.equals("SS")){
			setDemandeurCongesSansSoldeInfo(nbrJoursDemandes);
		} else if(categorieConges.equals("CM")){
			setDemandeurCongesMaladieInfo(nbrJoursDemandes);
			calculateCommunCongesFields(nbrJoursDemandes);
		}
		
		demandeur.save(getWorkflowModule().getSysadminContext());
		IStorageResource userFiche = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(demandeur, getWorkflowModule(), getProjectModule(), getWorkflowInstance().getCatalog().getProject().getOrganization());
		userFiche.save(getWorkflowModule().getSysadminContext());
		
		document.save(getWorkflowModule().getSysadminContext());
	}
	
	private void setDemandeurCongesPayesInfo(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") - nbrJoursDemandes);
		document.setValue("demandeurCongesValidesAConsommer", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation"));

	}
	
	private void setDemandeurCongesSpeciauxInfo(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") - nbrJoursDemandes);
		document.setValue("demandeurCongesSpeciauxEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation"));
		
	}
	
	private void setDemandeurCongesSansSoldeInfo(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") - nbrJoursDemandes);
		document.setValue("demandeurCongesSansSoldeEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation"));
	}
	
	private void setDemandeurCongesMaladieInfo(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") - nbrJoursDemandes);
		document.setValue("demandeurCongesMaladieEnCoursDeConsommation", demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
	}

	private void calculateCommunCongesFields(float nbrJoursDemandes) {

		/*get sold interieur
		if akbar mn 0 then minus jours demande then get the absolute valeur and minus it from solde anne en cours */

		demandeur.getExtendedAttributes().setValue("SoldeConges", (Float)demandeur.getExtendedAttributes().getValue("SoldeConges") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("SoldeAnneeEnCours", (Float)demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours") + nbrJoursDemandes);

	}

}
