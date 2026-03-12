package com.moovapps.capone.rh.GestionDeConge.document;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdp.activity.domain.TaskInstance;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;
import com.vdoc.sanaEducation.candidatures.aspose.helper.GenerateWordFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



public class PremiereValidation extends BaseDocumentExtension {
	
	IWorkflowInstance document = null;
	IUser demandeur = null;

	
	@Override
	public boolean onAfterLoad() {
		HideButton();
		document = getWorkflowInstance();
		demandeur = (IUser)document.getValue("Demandeur");
		if (getWorkflowInstance().getValue("nouveauNombreDeJoursDeConges") != null){
			//overrideDateConge();
		}
		return super.onAfterLoad();
	}

	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("Accepter") || action.getName().equals("ValiderCongeDuCollaborateur") || action.getName().equals("ValdierLaDemande")){
			//onValiderDemandeConge();
			generer();
		}
//		if(action.getName().equals("RetournerPourModification")){
////			reinitializeDemandeurFields();
////			onRefuserConge();
//			ClickButton("RetournerPourModification2");
//		}
		if(action.getName().equals("Refuser") || action.getName().equals("RefuserCongeOuAbsenceDuCollaborateur") || action.getName().equals("RefuserLaDemande")){
//			reinitializeDemandeurFields();
			//onRefuserConge();
			ClickButton("RefuserCongeOuAbsenceDuCollaborateur2");

		}




		return super.onBeforeSubmit(action);
	}

	public void generer(){
		getWorkflowInstance().setValue("DocumentDeConge", new ArrayList());
		try {

			IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
			if(societe ==null)return;
			ArrayList<IAttachment> documentConeg = (ArrayList<IAttachment>) societe.getValue("DocumentConge") ;

			if (documentConeg != null) {
				IAttachment iAttachment = documentConeg.get(0);
				File file = null;
				try {
					iAttachment.getName();
					file = new File("C:\\TEST"+ "\\"  + iAttachment.getName());
					FileUtils.writeByteArrayToFile(file, iAttachment.getContent());
					GenerateWordFile generateWordFile = new GenerateWordFile();
					InputStream inputStream = generateWordFile.valorization(getWorkflowModule(), getWorkflowInstance(), iAttachment);
					Document document = new Document(inputStream);
					IUser demandeur = null;
					//if (demandeurObject != null) {
					demandeur = (IUser) getWorkflowInstance().getValue("Demandeur");
					//File MyFile = new File("C:\\TEMP" + "\\" + iAttachment.getName() + ".pdf");
					String congeDocumentTitre = societe.getValue("sys_Title")+"-"+demandeur.getFullName();
					document.save("C:\\TEMP" + "\\" + congeDocumentTitre + ".pdf", SaveFormat.PDF);
					getWorkflowModule().addAttachment(getWorkflowInstance(), "DocumentDeConge", new File("C:\\TEMP" + "\\" + congeDocumentTitre + ".pdf"));
					getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
					file.delete();
					file.deleteOnExit();
					File MyFile = new File("C:\\TEMP" + "\\" + congeDocumentTitre + ".pdf");
					MyFile.delete();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				getWorkflowInstance().setValue("DocumentDeConge", null);
				Navigator.getNavigator().getRootNavigator().showAlertBox("le fichier est introuvable");
			}
		} catch (Exception e) {
			Navigator.getNavigator().getRootNavigator().showAlertBox("Un problème est survenu, veuillez contacter votre administrateur");
			e.printStackTrace();
		}
	}

//	private void WordToPdfDocumentConge() {
//
//		// Load the DOCX file
//		Document doc = null;
//		try {
//			doc = new Document("C:\\Users\\CAP ONE\\Desktop\\App Moovapps\\Galenica\\Document de congé.docx");
//
//
//			// Replace a bookmark with text
//			Bookmark bm = doc.getRange().getBookmarks().get("NomEtPrenom");
//			if (bm != null) {
//				bm.setText("Ahmed Zazouli");
//			}
//
//			// Save the updated document
//			doc.save("output.docx");
//
//			// Export to PDF
//			doc.save("output.pdf", SaveFormat.PDF);
//
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}

	private void overrideDateConge() {
		getWorkflowInstance().setValue("DateDeDebut",getWorkflowInstance().getValue("NouvelleDateDebut"));
		getWorkflowInstance().setValue("DateDeFin",getWorkflowInstance().getValue("NouvelleDateFin"));
		getWorkflowInstance().setValue("NombreDeJoursDemandes",getWorkflowInstance().getValue("nouveauNombreDeJoursDeConges"));
		getWorkflowInstance().setValue("totalJoursFerie",getWorkflowInstance().getValue("nouveauTotalJursFeries"));
		getWorkflowInstance().setValue("TotalAbsence",getWorkflowInstance().getValue("NouveauTotalAbsence"));
		getWorkflowInstance().setValue("DebutConge",getWorkflowInstance().getValue("NouvelleTrancheDebut"));
		getWorkflowInstance().setValue("FinConge",getWorkflowInstance().getValue("NouvelleTrancheFin"));
	}

	private void ClickButton(String button) {

			List<TaskInstance> instances = new ArrayList<>();
			try {
				instances = ((com.axemble.vdp.workflow.domain.ProcessWorkflowInstance) getWorkflowInstance()).getTaskInstances(2);

			} catch (ClassCastException classCastException) {
				if (classCastException.getMessage().contains("domain.ProcessWorkflowInstance")) {
					instances = ((com.axemble.vdoc.sdk.impl.ProcessWorkflowInstance) getWorkflowInstance()).getDocument().getWorkflowInstance().getTaskInstances(2);
				}
			}
			try {


			for (TaskInstance taskInstance : instances) {
				ITask task = taskInstance.getTask();

				IAction iAction1 = task.getAction(button);
				if (iAction1 != null) {
					taskInstance.removeOperators();

						taskInstance.addOperator(getWorkflowModule().getOperatorByLogin("sysadmin"));
						getWorkflowModule().end(getWorkflowModule().getSysadminContext(), taskInstance, iAction1, "");

				}
			}
			}catch (Exception ex){
				ex.printStackTrace();
			}


	}

	private void HideButton(){
		try {
			NamedContainer namedContainer = getResourceController().getButtonContainer(2);
			List<IWidget> widgets = namedContainer.getWidgets();
			for (IWidget iWidget : widgets) {
				CtlButton button = (CtlButton) iWidget;
				if (((CtlButton) iWidget).getName().equalsIgnoreCase("Rentrer à parallélisme")) {
					button.setHidden(true);
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void onRefuserConge() {
		String categorieConges = (String)document.getValue("TypeDeConge");
		float nbrJoursDemandes = (Float)document.getValue("NombreDeJoursDemandes");
		if(categorieConges.equals("CN")){
			onRefuserCongesPayes(nbrJoursDemandes);
		} else if(categorieConges.equals("CE")){
			float nbrJoursExceptionnelle = (Float)document.getValue("NombreDeJoursExceptionnelle");
			onRefuserCongesSpeciaux(nbrJoursExceptionnelle);
		} else if(categorieConges.equals("SS")){
			onRefuserCongesSansSolde(nbrJoursDemandes);
		} else if(categorieConges.equals("CM")){
			onRefuserCongesMaladie(nbrJoursDemandes);
		}
		document.setValue("demandeurCongesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));

		demandeur.save(getWorkflowModule().getSysadminContext());
		IStorageResource userFiche = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(demandeur, getWorkflowModule(), getProjectModule(), getWorkflowInstance().getCatalog().getProject().getOrganization());
		userFiche.save(getWorkflowModule().getSysadminContext());
		
		document.save(getWorkflowModule().getSysadminContext());
	}
	
	private void onRefuserCongesPayes(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
		document.setValue("CongesPayesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));

		//document.setValue("demandeurCongesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));

	}
	
	private void onRefuserCongesSpeciaux(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
		document.setValue("demandeurCongesSpeciauxEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation"));
		
	}
	
	private void onRefuserCongesSansSolde(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
		document.setValue("demandeurCongesSansSoldeEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation"));
	}
	
	private void onRefuserCongesMaladie(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") - nbrJoursDemandes);
		document.setValue("demandeurCongesMaladieEnCoursDeConsommation", demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
	}
	
	/*public void reinitialize(){
		reinitializeDemandeurFields();
		reinitializeDocument();
	}*/
	
	/*public void reinitializeDocument(){
		document.setValue("DateDeFin", null);
		document.setValue("DateDeDebut", null);
		document.setValue("NombreDeJoursDemandes", 0);
		document.save(getWorkflowModule().getSysadminContext());
	}*/
	
	public void reinitializeDemandeurFields(){
		float nbrJoursDemandes = (Float)document.getValue("NombreDeJoursDemandes");
		float demandeurNbrJoursDeConges = (Float)demandeur.getExtendedAttributes().getValue("SoldeConges") + nbrJoursDemandes;
		float demandeurNbrJoursEnCoursValidation = (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes;
		float demandeurNbrJoursEnCoursTraitement = (Float) demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement");
		demandeur.getExtendedAttributes().setValue("SoldeConges", demandeurNbrJoursDeConges);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", demandeurNbrJoursEnCoursValidation);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement", demandeurNbrJoursEnCoursTraitement);
		demandeur.save(getWorkflowModule().getSysadminContext());
		document.setValue("DemandeurSoldeConge", demandeurNbrJoursDeConges);
		document.setValue("DemandeurTotalJourEnCoursValidation", demandeurNbrJoursEnCoursValidation);
		
	}

	public void onValiderDemandeConge(){
		String categorieConges = (String)document.getValue("TypeDeConge");
		float nbrJoursDemandes = (Float)document.getValue("NombreDeJoursDemandes");
		if(categorieConges.equals("CN")){
			setDemandeurCongesPayesInfo(nbrJoursDemandes);
		} else if(categorieConges.equals("CE")){
			float nbrJoursExceptionnelle = (Float)document.getValue("NombreDeJoursExceptionnelle");
			setDemandeurCongesSpeciauxInfo(nbrJoursExceptionnelle);
		} else if(categorieConges.equals("SS")){
			setDemandeurCongesSansSoldeInfo(nbrJoursDemandes);
		}else if(categorieConges.equals("CM")){
			setDemandeurCongesMaladieInfo(nbrJoursDemandes);
		}
		document.setValue("demandeurCongesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation")-nbrJoursDemandes);
		document.setValue("demandeurCongesValidesAConsommer", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation")+nbrJoursDemandes);
		//document.setValue("DemandeurCongesEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursTraitement"));

		demandeur.save(getWorkflowModule().getSysadminContext());
		IStorageResource userFiche = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(demandeur, getWorkflowModule(), getProjectModule(), getWorkflowInstance().getCatalog().getProject().getOrganization());
		userFiche.save(getWorkflowModule().getSysadminContext());
		
		document.save(getWorkflowModule().getSysadminContext());
	}
	
	private void setDemandeurCongesPayesInfo(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") + nbrJoursDemandes);

		document.setValue("CongesPayesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));
		document.setValue("CongesPayesEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation"));
		document.setValue("CongesPayesEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeTraitement"));
	}
	private void setDemandeurCongesSpeciauxInfo(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") + nbrJoursDemandes);
		document.setValue("demandeurCongesSpeciauxEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation"));
		document.setValue("demandeurCongesSpeciauxEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation"));
		document.setValue("CongesSpeciauxEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeTraitement"));
	}	
	private void setDemandeurCongesSansSoldeInfo(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") + nbrJoursDemandes);
		document.setValue("demandeurCongesSansSoldeEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation"));
		document.setValue("demandeurCongesSansSoldeEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation"));
		document.setValue("CongesSansSoldeEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeTraitement"));
	}

	private void setDemandeurCongesMaladieInfo(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") + nbrJoursDemandes);
		document.setValue("demandeurCongesMaladieEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation"));
		document.setValue("demandeurCongesMaladieEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
		document.setValue("CongesMaladieEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
	}
}
