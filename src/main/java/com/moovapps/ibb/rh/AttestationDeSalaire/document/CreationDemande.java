package com.moovapps.ibb.rh.AttestationDeSalaire.document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.IOptionList.IOption;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class CreationDemande extends BaseDocumentExtension {
	
	IWorkflowInstance document = null;
	SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
	Collection<IStorageResource> ficheAttestations = null;

	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		if(document.getValue("DateCreationDeLaDemande") == null){			
			document.setValue("DateCreationDeLaDemande", document.getCreatedDate());
			document.setValue("DateCreationDemandeDdMMYYYY", simpleFormat.format(document.getCreatedDate()));
		}
		if(document.getValue("siJeSuisLeDemandeur") == null){
			document.setValue("siJeSuisLeDemandeur", true);
			onJeSuisLeDemandeurChange();
		}
		if(document.getValue("Demandeur2") != null){
			setDemandeurFields();
		}
		return super.onAfterLoad();
	} 

	public void onPropertyChanged(IProperty property) {
		if (property.getName().equals("siJeSuisLeDemandeur")) {
			document.setValue("ModeleAttestationAdministrative", null);
			//document.setValue("AttestationDemande", null);
			document.setValue("AttestationAdministrative", null);

			onJeSuisLeDemandeurChange();
		}else if(property.getName().equals("Demandeur2")){
			document.setValue("ModeleAttestationAdministrative", null);
			//document.setValue("AttestationDemande", null);
			document.setValue("AttestationAdministrative", null);

			onDemandeurChange();
		/*}else if(property.getName().equals("AttestationDemande")){
			onAttestationDemandeChange();
		}else if(property.getName().equals("isAttestationCachetee")){
			onAttestationDemandeChange();*/
		}else if(property.getName().equals("DemandeurDonnee")){
			onDemandeurChange();
		}
		super.onPropertyChanged(property);
	}
	
	private void onAttestationDemandeChange() {
		try{
			String attestationDemande = null;
			if(document.getValue("AttestationDemande") != null){
				attestationDemande = (String)document.getValue("AttestationDemande");
			}

			ArrayList<IAttachment> theAttestation = null;
			if(ficheAttestations != null){
				for (IStorageResource attesttaion : ficheAttestations) {
					if(attesttaion.getValue("sys_Title").equals(attestationDemande)){
						getWorkflowInstance().setValue("DureeDeValidite", attesttaion.getValue("DureeDeValidite")!=null?attesttaion.getValue("DureeDeValidite"):3);
						getWorkflowInstance().setValue("AncienneteNecessaire",attesttaion.getValue("AnciennetePourLaDemanderMois")!=null?attesttaion.getValue("AnciennetePourLaDemanderMois"):3);
					/*	IUser demandeur = (IUser) getWorkflowInstance().getValue("Demandeur2");
						Date dateRequisePourLaDemandeDAttestation = null;
						if(demandeur!=null){
							Date dateEmbaucheSociete = (Date) demandeur.getExtendedAttributes().getValue("DateDEmbauche");
							if(dateEmbaucheSociete!=null){
								Calendar c = Calendar.getInstance();
								c.setTime(dateEmbaucheSociete);
								c.add(Calendar.MONTH,attesttaion.getValue("AnciennetePourLaDemanderMois")!=null? ((Number)attesttaion.getValue("AnciennetePourLaDemanderMois")).intValue():0);
								dateRequisePourLaDemandeDAttestation =  simpleFormat.parse(simpleFormat.format(c.getTime()));
							}else {
								dateRequisePourLaDemandeDAttestation = null;
							}
						}else{
							dateRequisePourLaDemandeDAttestation = null;
						}
						getWorkflowInstance().setValue("DateRequisePourLaDemandeDAttestation",dateRequisePourLaDemandeDAttestation);
*/
						document.setValue("ShowCheckBoxAttestationCachetee", (attesttaion.getValue("AttestationAdministrativeCachetee") == null ||((Collection<IAttachment>)attesttaion.getValue("AttestationAdministrativeCachetee")).size() == 0) ? false : true);
						if((boolean)document.getValue("isAttestationCachetee")){
							theAttestation = (ArrayList)attesttaion.getValue("AttestationAdministrativeCachetee");
						}
						else{
							theAttestation = (ArrayList)attesttaion.getValue("AttestationAdministrative");
						}
						break;
					}
				}
			}
			if(theAttestation != null){
				File file = null;
				for (IAttachment iAttachment : theAttestation)
				{
					try
					{
						file = new File("c://TEST//"+iAttachment.getName());
						FileUtils.writeByteArrayToFile(file, iAttachment.getContent());
						document.setValue("ModeleAttestationAdministrative", null);
						getWorkflowModule().addAttachment(document, "ModeleAttestationAdministrative", file);
						document.refresh("ModeleAttestationAdministrative");
						file.delete();
						file.deleteOnExit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			else
			{
				document.setValue("ModeleAttestationAdministrative", null);
			}
			document.save(getWorkflowModule().getLoggedOnUserContext());
		}catch (Exception e){
			e.printStackTrace();
		}


    /*	String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort())
    			.concat(Turbine.getContextPath().concat("/easysite/workplace/applications/application-attestation-de-salaire-0/GestionDeProcessus/edit-document/").concat(document.getId()+""));    

		ExternalScreen externalScreen = new ExternalScreen(lien.toString());
		Navigator.getNavigator().setCurrentScreen(externalScreen);*/
		//
	}
	public IUser setDemandeur(){
		if(document.getValue("Demandeur2") == null){
			document.setValue("Demandeur2", document.getCreatedBy());
		}
		IUser demandeur = (IUser)document.getValue("Demandeur2");
		/*document.setValue("SupHierarchique", demandeur.getHierarchicalManager());
		document.setValue("Societe2", demandeur.getExtendedAttributes().getValue("SocieteDonnee"));*/
		return demandeur;
	}
	public void onJeSuisLeDemandeurChange() {
		boolean siJeSuisLeDemandeur = (boolean) document.getValue("siJeSuisLeDemandeur");
		if (siJeSuisLeDemandeur) {
			document.setValue("Demandeur2", getDocument().getUser());
		} else {
			document.setValue("DemandeurDonnee",null);
			document.setValue("Demandeur2", null);
			document.setValue("Societe", null);
		}
		onDemandeurChange();
	}

	public void onDemandeurChange() {
		if (document.getValue("Demandeur2") != null) {
			if (((IUser) document.getValue("Demandeur2")).getId().equals(getDocument().getUser().getId())) {
				document.setValue("siJeSuisLeDemandeur", true);
			}
		}
		setDemandeurFields();
	}

	public void setDemandeurFields() {
		IStorageResource demandeurDonnee = (IStorageResource) document.getValue("DemandeurDonnee");
		
		document.setValue("Matricule", demandeurDonnee != null ? demandeurDonnee.getValue("Matricule") : null);
		document.setValue("TitreDemandeur", demandeurDonnee != null ? demandeurDonnee.getValue("Title") : null);
		document.setValue("Nom", demandeurDonnee != null ? demandeurDonnee.getValue("LastName") : null);
		document.setValue("Prenom", demandeurDonnee != null ? demandeurDonnee.getValue("FirstName") : null);
		document.setValue("CIN", demandeurDonnee != null ? demandeurDonnee.getValue("CIN") : null);
		document.setValue("DateDeNaissance", demandeurDonnee != null ? demandeurDonnee.getValue("Birthday") : null);
		document.setValue("NImmatriculationCNSS", demandeurDonnee != null ? demandeurDonnee.getValue("NCNSS") : null);
		document.setValue("EtatCivil", demandeurDonnee != null ? demandeurDonnee.getValue("EtatCivil") : null);
		document.setValue("NombreEnfants", demandeurDonnee != null ? demandeurDonnee.getValue("NombreEnfants") : null);
		document.setValue("Sex", demandeurDonnee != null ? demandeurDonnee.getValue("Sex") : null);
		document.setValue("NumeroDeTelephone", demandeurDonnee != null ? demandeurDonnee.getValue("MobilePhoneNumber") : null);
		document.setValue("Adresse", demandeurDonnee != null ? (demandeurDonnee.getValue("Address1") != null ?  demandeurDonnee.getValue("Address1") +  " " : "" + (demandeurDonnee.getValue("Address2") !=null ? demandeurDonnee.getValue("Address2"):"")) : null);
		document.setValue("Direction", demandeurDonnee != null ? demandeurDonnee.getValue("Direction") : null);
		document.setValue("Departement", demandeurDonnee != null ? demandeurDonnee.getValue("Departement") : null);
		document.setValue("Fonction", demandeurDonnee != null ? demandeurDonnee.getValue("Fonction") : null);
		document.setValue("Categorie", demandeurDonnee != null ? demandeurDonnee.getValue("Categorie") : null);
		document.setValue("TypeDeContrat", demandeurDonnee != null ? demandeurDonnee.getValue("ContractType") : null);
		document.setValue("CategorieDeSalaire", demandeurDonnee != null ? demandeurDonnee.getValue("CategorieDeSalaire") : null);
		document.setValue("DateDEmbauche", demandeurDonnee != null ? demandeurDonnee.getValue("DateDEmbauche") : null);
		document.setValue("NImmatriculationCIMR", demandeurDonnee != null ? demandeurDonnee.getValue("NCIMR") : null);
		Date dateEmbauche = null;
		if (demandeurDonnee != null && document.getValue("DateDEmbauche") != null) {
			dateEmbauche = (Date) document.getValue("DateDEmbauche");
		}
		int anciennete = 0;
		if(dateEmbauche != null){			
			anciennete = getAncienneteInYear(dateEmbauche);
		}
		document.setValue("Anciennete", anciennete);
		document.setValue("AncienneteInDetail", dateEmbauche != null ? getAncienneteInDetail(dateEmbauche) : null);
		document.setValue("DateDEmbaucheSansH", dateEmbauche != null ? simpleFormat.format(dateEmbauche) : null);
		document.setValue("DateCreationSansH", dateEmbauche != null ? simpleFormat.format(new Date()) : null);
		document.setValue("DateDeTitularisation", demandeurDonnee != null ? demandeurDonnee.getValue("DateDeTitularisation") : null);
		document.setValue("DateDEnregistrement", demandeurDonnee != null ? demandeurDonnee.getValue("DateDEnregistrement") : null);
		document.setValue("DateDeSortie", demandeurDonnee != null ? demandeurDonnee.getValue("DateDeSortie") : null);
		Date dateSortie = null;
		if (demandeurDonnee != null && document.getValue("DateDeSortie") != null) {
			dateSortie = (Date) document.getValue("DateDeSortie");
		}
		document.setValue("DateDeSortieSansH", dateSortie != null  ? simpleFormat.format(dateSortie) : null);
		document.setValue("MotifSortie", demandeurDonnee != null ? demandeurDonnee.getValue("MotifSortie") : null);
		document.setValue("Banque", demandeurDonnee != null ? demandeurDonnee.getValue("Banque") : null);
		document.setValue("NCompteBancaire", demandeurDonnee != null ? demandeurDonnee.getValue("NCompteBancaire") : null);
		
		document.setValue("SuperieurHierarchique", demandeurDonnee != null ? demandeurDonnee.getValue("HierarchicalManager") : null);
		document.setValue("Societe", demandeurDonnee != null ? demandeurDonnee.getValue("Societe") : null);
		document.setValue("NomSociete", document.getValue("Societe") != null ? (String)((IStorageResource)document.getValue("Societe")).getValue("sys_Title") : null);
		onDemandeurSocieteChange();
		
		document.setValue("Fonction", demandeurDonnee != null ? demandeurDonnee.getValue("Fonction") : null);
		document.setValue("Salaire", demandeurDonnee != null ? demandeurDonnee.getValue("SalaireDeBase") : null);
		document.setValue("SalaireBrut", demandeurDonnee != null ? demandeurDonnee.getValue("SalaireBrutDH") : null);
		document.setValue("MontantMensuelDeNoteDeFrais", demandeurDonnee != null ? demandeurDonnee.getValue("MontantMensuelDeNoteDeFrais") : null);
		document.setValue("Agence", demandeurDonnee != null ? demandeurDonnee.getValue("AgenceBancaire") : null);
         document.save(getDirectoryModule().getSysadminContext());
		StringNumberConverter stringNumberConverter = new StringNumberConverter();
		//document.setValue("TauxHoraireEnTexte", stringNumberConverter.ConvertNumber((Number)document.getValue("TauxHoraire")));
		//document.setValue("SalaireDeBaseEnTexte", stringNumberConverter.ConvertNumber((Number)document.getValue("SalaireDeBase")));
		document.setValue("SalaireBrutEnTexte", stringNumberConverter.ConvertNumber((Number)document.getValue("SalaireBrut")));
		document.setValue("MontantMensuelDeNoteDeFraisEnTexte", stringNumberConverter.ConvertNumber((Number)document.getValue("MontantMensuelDeNoteDeFrais")));
		//document.setValue("SalaireNetEnTexte", stringNumberConverter.ConvertNumber((Number)document.getValue("SalaireNETDH")));
		//document.setValue("SalaireBrutImposableEnTexte", stringNumberConverter.ConvertNumber((Number)document.getValue("SalaireBrutImposableDH")));
		//document.setValue("RemunerationEnTexte", stringNumberConverter.ConvertNumber((Number)document.getValue("RemunerationDH")));
//	}
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action) {

		IUser validateur = (IUser) getWorkflowInstance().getValue("SuperieurHierarchique");
		boolean siJeSuisLeDemandeur = (boolean) getWorkflowInstance().getValue("siJeSuisLeDemandeur");
	//		if(validateur==null && siJeSuisLeDemandeur){
//			getResourceController().alert("Vous devez avoir un responsable hiérarchique");
//			return false;
//		}
		if(document.getValue("AttestationAdministrative") != null){
			IStorageResource attestation = (IStorageResource) getWorkflowInstance().getValue("AttestationAdministrative");
			IUser demandeur = (IUser) getWorkflowInstance().getValue("Demandeur2");
//			Date dateEmbauche = demandeur!=null?(Date) demandeur.getExtendedAttributes().getValue("DateDEmbauche"):null;
//			if(dateEmbauche == null){
//				getResourceController().alert("Vous n'avez pas l'ancienneté nécessaire pour demander cette attestation");
//				return false;
//			}
//			int minimumAnciennetePourObtenirCetteAttestation = ((Number) attestation.getValue("AnciennetePourLaDemanderMois")).intValue();
//			boolean isAncienne =  checkAnnciente(dateEmbauche,minimumAnciennetePourObtenirCetteAttestation);
//			if(!isAncienne){
//				getResourceController().alert("Vous n'avez pas l'ancienneté nécessaire pour demander cette attestation");
//				//return false;
//			}

			//String attestationDemande = (String)document.getValue("AttestationDemande");
			Collection<IWorkflowInstance> valideAttestations = getValideAttestations(demandeur, attestation);
			/*if(valideAttestations != null  && !valideAttestations.isEmpty()){
				getResourceController().alert("Vous ne pouvez pas demander une attestation du même type si vous en possédez déjà une valide ou si une demande est en cours");
				return false;
			}
			document.setValue("DureeDeValidite",attestation.getValue("DureeDeValidite"));
			if(getWorkflowInstance().getValue("DureeDeValidite") != null){
				try {
					Calendar c = Calendar.getInstance();
					if(getWorkflowInstance().getValue("DateDeRemiseDeLAttestation") != null){
						c.setTime(((Date)getWorkflowInstance().getValue("DateDeRemiseDeLAttestation")));
					}
					c.add(Calendar.MONTH, ((Number)getWorkflowInstance().getValue("DureeDeValidite")).intValue());
					document.setValue("DateFinValidite", simpleFormat.parse(simpleFormat.format(c.getTime())));
				}catch (Exception e){
					e.printStackTrace();
				}
			}*/
			document.setValue("ModeleAttestationAdministrative",getPieceJointe((ArrayList<IAttachment>) attestation.getValue("AttestationAdministrative")));
		}

		if (action.getName().equals("Envoyer")){
			IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
			getWorkflowInstance().setValue("SocieteResponsableRH", societe != null ? societe.getValue("ResponsableRH") : null);
			String pattern = "dd/MM/yyyy";
			SimpleDateFormat formatter = new SimpleDateFormat(pattern);
			String  dateGenerationTEXT = formatter.format(new Date());
			getWorkflowInstance().setValue("DateGenerationAttestation",dateGenerationTEXT);
			getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
		}

		document.save(getWorkflowModule().getSysadminContext());
		return super.onBeforeSubmit(action);
	}

	public ArrayList<IAttachment> getPieceJointe(ArrayList<IAttachment> attachments) {
		if(attachments==null)return null;
		//attachments = (ArrayList<IAttachment>) instance.getValue(key);
		File file = null;
		ArrayList<IAttachment> files = new ArrayList<>();
		if (attachments != null && attachments.size() > 0) {
			for (IAttachment iAttachment : attachments) {
				if (iAttachment != null) {
					try {
						file = new File("c://TEST//" + iAttachment.getName());
						FileUtils.writeByteArrayToFile(file, iAttachment.getContent());
						IAttachment attachment = Modules.getDirectoryModule().createAttachment(Modules.getWorkflowModule().getSysadminContext(), file);
						files.add(attachment);
						file.delete();
						file.deleteOnExit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return files;
	}

	private boolean checkAnnciente(Date dateEmbauche, int requiredMonths) {
		Calendar currentDate = Calendar.getInstance();
		Calendar hireDate = Calendar.getInstance();
		hireDate.setTime(dateEmbauche);

		int monthsDifference = 0;

		// Calculate the year difference
		monthsDifference += (currentDate.get(Calendar.YEAR) - hireDate.get(Calendar.YEAR)) * 12;

		// Calculate the month difference
		monthsDifference += currentDate.get(Calendar.MONTH) - hireDate.get(Calendar.MONTH);

		// Calculate the day difference
		int dayDifference = currentDate.get(Calendar.DAY_OF_MONTH) - hireDate.get(Calendar.DAY_OF_MONTH);

		// Adjust monthsDifference based on day difference
		if (dayDifference < 0) {
			monthsDifference--;
		}

		return monthsDifference >= requiredMonths;
	}


	public int getAncienneteInYear(Date dateEmbauche){
		long difference_In_Years = ((new Date().getTime() - dateEmbauche.getTime()) / 60000) / 525600; 
		return (int)difference_In_Years;
	}
	private Collection<IWorkflowInstance> getValideAttestations(IUser demandeur , IStorageResource attestationDemande){
		try
		{
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "AttestationDeSalaire", organization);
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "DOCUMENTSADMINISTRATIFSINTERNES");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			//controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addEqualsConstraint("TypeDocument", getWorkflowInstance().getValue("TypeDocument"));
			controller.addEqualsConstraint("Demandeur2", demandeur);
			controller.addEqualsConstraint("AttestationAdministrative", attestationDemande);
			controller.addGreaterOrEqualConstraint("DateFinValidite", simpleFormat.parse(simpleFormat.format(new Date())));
			Collection<IWorkflowInstance> collection = controller.evaluate(w);
			return collection;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	public String getAncienneteInDetail(Date dateEmbauche){
		long difference_In_Milliseconds = ((new Date().getTime() - dateEmbauche.getTime())); 
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(difference_In_Milliseconds);
		int years = c.get(Calendar.YEAR) - 1970;
		int months = c.get(Calendar.MONTH);
		int days = c.get(Calendar.DAY_OF_MONTH) - 1;
		return years + " ans " + months + " mois " + days + " jours";  
//		return (int)difference_In_Years;
	}

	public void onDemandeurSocieteChange() {
		IStorageResource societe = null;
		IUser demandeur = (IUser)document.getValue("Demandeur2");
		if(demandeur!=null){
			societe = (IStorageResource) demandeur.getExtendedAttributes().getValue("Societe");
			ficheAttestations = getFicheAttestationBySociete(societe);
			ArrayList<IOption> options = new ArrayList<IOption>();
			if (!ficheAttestations.isEmpty()) {
				for (IStorageResource attestation : ficheAttestations) {
					options.add(getWorkflowModule().createListOption(attestation.getValue("sys_Title"), (String)attestation.getValue("sys_Title")));
				}
			}
			//document.setList("AttestationDemande", societe != null ? options : null);
			// document.setValue("ModeleAttestationAdministrative", societe != null ? societe.getValue("ModeleAttestationDeSalaire") : null);
			document.setValue("AdressSociete", societe != null ? societe.getValue("Adresse") : null);
			document.setValue("Place", societe != null ? societe.getValue("Ville") : null);
			document.setValue("SocieteResponsableRH", societe != null ? societe.getValue("ResponsableRH") : null);
		}

	}

	
	public Collection<IStorageResource> getFicheAttestationBySociete(IStorageResource societe) {
		Collection<IStorageResource> ficheAttestation = null;
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
			IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", getWorkflowInstance().getCatalog().getProject().getOrganization());
			ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheAttestationsAdministratives");
			controller.addEqualsConstraint("Societe", societe);
			ficheAttestation = controller.evaluate(definition);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ficheAttestation;
	}
}
