package com.moovapps.capone.rh.GestionDeConge.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;

public class DeuxiemeValidation extends BaseDocumentExtension {
	
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
		if(action.getName().equals("AnnulerLaDemandeDeConge") || action.getName().equals("AnnulerLaDemandeDeConge")){
			//reinitializeDemandeurFields();
		}

		if(action.getName().equals("DeclarerLaConsommationDuConge") || action.getName().equals("ConfirmerLaConsommationDeConge")){
			//onValiderConsommationConge();
		}
		return super.onAfterSubmit(action);
	}
	public void onValiderConsommationConge(){
		String categorieConges = (String)document.getValue("TypeDeConge");
		Float nbrJoursDemandes = (Float)document.getValue("NombreDeJoursDemandes");
		if(categorieConges.equals("CN")){
			setDemandeurCongesPayesInfo(nbrJoursDemandes);
		} else if(categorieConges.equals("CE")){
			float nbrJoursExceptionnelle = (Float)document.getValue("NombreDeJoursExceptionnelle");
			setDemandeurCongesSpeciauxInfo(nbrJoursExceptionnelle);
		} else if(categorieConges.equals("SS")){
			setDemandeurCongesSansSoldeInfo(nbrJoursDemandes);
		}else if(categorieConges.equals("CM")){
			setDemandeurCongesMaladie(nbrJoursDemandes);
		}
		document.setValue("demandeurCongesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
		document.setValue("demandeurCongesValidesAConsommer", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation")- nbrJoursDemandes);
		document.setValue("DemandeurCongesEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement")+ nbrJoursDemandes);

		demandeur.save(getWorkflowModule().getSysadminContext());
		IStorageResource userFiche = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(demandeur, getWorkflowModule(), getProjectModule(), getWorkflowInstance().getCatalog().getProject().getOrganization());
		userFiche.save(getWorkflowModule().getSysadminContext());
		document.save(getWorkflowModule().getSysadminContext());
	}
	private void setDemandeurCongesPayesInfo(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation")- nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeTraitement") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement")+ nbrJoursDemandes);
		document.setValue("CongesPayesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));
		document.setValue("CongesPayesEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation"));
		document.setValue("CongesPayesEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeTraitement"));
	}
	private void setDemandeurCongesSpeciauxInfo(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeTraitement") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement")+nbrJoursDemandes);
		document.setValue("demandeurCongesSpeciauxEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation"));
		document.setValue("demandeurCongesSpeciauxEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation"));
		document.setValue("CongesSpeciauxEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeTraitement"));
	}
	private void setDemandeurCongesSansSoldeInfo(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation")-nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeTraitement") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement")+ nbrJoursDemandes);
		document.setValue("demandeurCongesSansSoldeEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation"));
		document.setValue("demandeurCongesSansSoldeEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation"));
		document.setValue("CongesSansSoldeEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeTraitement"));
	}
	private void setDemandeurCongesMaladie(float nbrJoursDemandes){
		demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeTraitement") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement")+ nbrJoursDemandes);
		document.setValue("demandeurCongesMaladieEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
		document.setValue("CongesMaladieEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeTraitement"));
	}
	public void reinitializeDemandeurFields(){
		Float nbrJoursDemandes = (Float)document.getValue("NombreDeJoursDemandes");
		Float demandeurNbrJoursDeConges = (Float)demandeur.getExtendedAttributes().getValue("SoldeConges") + nbrJoursDemandes;
		Float demandeurNbrJoursEnCoursValidation = (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes;
		
		demandeur.getExtendedAttributes().setValue("SoldeConges", demandeurNbrJoursDeConges);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", demandeurNbrJoursEnCoursValidation);
		demandeur.save(getWorkflowModule().getSysadminContext());
	}
}
