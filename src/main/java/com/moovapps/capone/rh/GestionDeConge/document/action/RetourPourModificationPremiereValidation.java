package com.moovapps.capone.rh.GestionDeConge.document.action;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.activity.domain.TaskInstance;
import com.moovapps.capone.rh.GestionDeConge.helpers.JoursOuvrableOuvres;
import com.moovapps.capone.rh.GestionDeConge.helpers.WorkingDaysNumberCalculator;
import com.moovapps.capone.rh.cummonHelpers.DateValidator;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;

import java.text.SimpleDateFormat;
import java.util.*;

public class RetourPourModificationPremiereValidation extends BaseDocumentExtension {
	
	IWorkflowInstance document = null;
	IUser demandeur = null;
	IStorageResource societe = null;

	// SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY = 6, SATURDAY = 7
	ArrayList<Integer> joursOuvrables = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6, 7));

	// SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY = 6, SATURDAY = 7
	ArrayList<Integer> joursOuvres = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6));

	private Date getDateDebutConge() {
		Date dateDebutConge = null;
		if (document.getValue("suggestedDateDebut") != null) {
			dateDebutConge = (Date) document.getValue("suggestedDateDebut");
		}
		return dateDebutConge;
	}

	private Date getDateFinConge() {
		Date dateFinConge = null;
		if (document.getValue("suggestedDateFin") != null) {
			dateFinConge = (Date) document.getValue("suggestedDateFin");
		}
		return dateFinConge;
	}

	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		demandeur = (IUser) document.getValue("Demandeur");
		//setDemandeurCongesFields();
		//String societeName = null;
	//	if(document.getValue("SocieteDonnee") != null){
			societe= (IStorageResource)document.getValue("Societe");;
		//}
		setJourOuvrablesJourOuvres();
		document.setValue("DemandeurTotalJourEnCoursValidation", demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
		return super.onAfterLoad();
	}
	
	
	private void setJourOuvrablesJourOuvres() {
		joursOuvrables =  new JoursOuvrableOuvres().getJoursOuvrables(societe);
		joursOuvres =  new JoursOuvrableOuvres().getJoursOuvres(societe);
	}

	@Override
	public void onPropertyChanged(IProperty property) {
		if (property.getName().equals("suggestedDateDebut")) {
			document.setValue("suggestedNombreDeJoursDeConges", 0);
			document.setValue("suggestedTotalJoursFerie", 0);
			document.setValue("suggestedTotalAbsence", 0);
			onDateDeDebutChange();
		} else if (property.getName().equals("suggestedDateFin")) {
			document.setValue("suggestedNombreDeJoursDeConges", 0);
			document.setValue("suggestedTotalJoursFerie", 0);
			document.setValue("suggestedTotalAbsence", 0);
			onDateDeFinChange();
		} else if (property.getName().equals("SuggestedDebutConge")) {
			onTrancheDebutChange();
		} else if (property.getName().equals("SuggestedFinConge")) {
			onTrancheFinChange();
		}
		super.onPropertyChanged(property);
	}

	public void onTypeCongeExceptionnelleChange() {
		if (document.getValue("TypeCongeExceptionnelle") != null) {
			IStorageResource congeExceptionnelle = (IStorageResource) document.getValue("TypeCongeExceptionnelle");
			if (congeExceptionnelle.getValue("NbrJours") != null) {
				Number nbrJoursExceptionnelle = (Number)congeExceptionnelle.getValue("NbrJours");
				document.setValue("NombreDeJoursExceptionnelle", nbrJoursExceptionnelle);
				if (document.getValue("suggestedDateDebut") != null) {
					Date dateDebut = (Date) document.getValue("suggestedDateDebut");
					Calendar calFin = Calendar.getInstance();
					calFin.setTime(dateDebut);
					calFin.add(Calendar.DATE, nbrJoursExceptionnelle.intValue() - 1);
					document.setValue("suggestedDateFin", calFin.getTime());
					document.setValue("suggestedNombreDeJoursDeConges", 0);
					document.setValue("suggestedTotalJoursFerie", 0);
				} else {
					setDemandeurCongesFields();
				}
			}
		} else {
			setDemandeurCongesFields();
		}
	}

	private void onDateDeDebutChange() {
		calculateConges();
	}

	private void onDateDeFinChange() {
		calculateConges();
	}

	private void onTrancheDebutChange() {
		calculateConges();
	}

	private void onTrancheFinChange() {
		calculateConges();
	}

	public void resetForm() {
		document.setValue("suggestedDateDebut", null);
		document.setValue("suggestedDateFin", null);
		document.setValue("suggestedNombreDeJoursDeConges", 0);
		document.setValue("suggestedTotalJoursFerie", 0);
		document.setValue("suggestedTotalAbsence", 0);
		document.setValue("SuggestedDebutConge", "TJ");
		document.setValue("SuggestedFinConge", "TJ");
	}

	public void setDemandeurCongesFields() {
		float nbrJoursDemandes = 0;
		if (document.getValue("suggestedNombreDeJoursDeConges") != null) {
			nbrJoursDemandes = ((Number) document.getValue("suggestedNombreDeJoursDeConges")).floatValue();
		}
		float nbrJoursSpeciaux = 0;
		if (document.getValue("NombreDeJoursExceptionnelle") != null) {
			nbrJoursSpeciaux = ((Number) document.getValue("NombreDeJoursExceptionnelle")).floatValue();
		}
		
		float SoldeConges = (Float) demandeur.getExtendedAttributes().getValue("SoldeConges");
		float SoldeAnterieur = (Float) demandeur.getExtendedAttributes().getValue("SoldeAnterieur");
		float CongesPayesPris = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesPris");
		float joursEnCoursConsommation = (Float) demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation");
		float JourSEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation");
		float CongesPayesEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation");
		float CongesPayesEnCoursDeConsommation = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation");
		float CongesPayesEnCoursDeTraitement = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeTraitement");
		float CongesPayesN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesN1");
		float CongesSpeciauxPris = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxPris");
		float CongesSpeciauxN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxN1");
		float CongesSpeciauxEnCoursDeConsommation = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation");
		float CongesMaladiePris = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladiePris");
		float CongesMaladieN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieN1");
		float CongesMaladieEnCoursDeConsommation = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation");
		float CongesSansSoldePris = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldePris");
		float CongesSansSoldeN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeN1");
		float CongesSansSoldeEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation");

		// set rubrique congés  info
		document.setValue("demandeurSoldeCongesPayesReel", SoldeConges);
		document.setValue("demandeurSoldeAnterieur", SoldeAnterieur);
		document.setValue("demandeurCongesPris", CongesPayesPris);
		document.setValue("demandeurCongesValidesAConsommer", joursEnCoursConsommation);
		document.setValue("demandeurSoldeCongesPayesProjete", SoldeConges - joursEnCoursConsommation - JourSEnCoursDeValidation - nbrJoursDemandes);
		document.setValue("demandeurCongesEnCoursDeValidation", JourSEnCoursDeValidation + nbrJoursDemandes);

		// set rubrique congés payés info
		document.setValue("CongesPayesN1", CongesPayesN1);
		document.setValue("CongesPayesPris", CongesPayesPris);
		document.setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation);
		document.setValue("CongesPayesEnCoursDeConsommation", CongesPayesEnCoursDeConsommation);
		document.setValue("CongesPayeesEnCoursDeTraitement", CongesPayesEnCoursDeTraitement);

		// set rubrique congés spéciaux info
		document.setValue("demandeurCongesSpeciauxPris", CongesSpeciauxPris);
		document.setValue("demandeurCongesSpeciauxN1", CongesSpeciauxN1);
		document.setValue("demandeurCongesSpeciauxEnCoursDeConsommation", CongesSpeciauxEnCoursDeConsommation);
		// document.setValue("demandeurCongesSpeciauxEnCoursDeValidation",
		// CongesSpeciauxEnCoursDeValidation + nbrJoursSpeciaux);

		// set rubrique congés maladie info
		document.setValue("demandeurCongesMaladiePris", CongesMaladiePris);
		document.setValue("demandeurCongesMaladieN1", CongesMaladieN1);
		document.setValue("demandeurCongesMaladieEnCoursDeConsommation", CongesMaladieEnCoursDeConsommation);

		// set rubrique congés sans solde info
		document.setValue("demandeurCongesSansSoldePris", CongesSansSoldePris);
		document.setValue("demandeurCongesSansSoldeN1", CongesSansSoldeN1);
		document.setValue("demandeurCongesSansSoldeEnCoursDeValidation", CongesSansSoldeEnCoursDeValidation);

	}

	public boolean validateForm() {
		float nbrJoursDemandes = 0;
		float soldeConges = (Float)demandeur.getExtendedAttributes().getValue("SoldeConges");
		if (document.getValue("suggestedNombreDeJoursDeConges") != null && !document.getValue("suggestedNombreDeJoursDeConges").equals(0)) {
			nbrJoursDemandes = (Float) document.getValue("suggestedNombreDeJoursDeConges");
		}
		if (!validateDates(document.getValue("suggestedDateDebut"), document.getValue("suggestedDateFin"))) {
			return false;
		}
		if (document.getValue("TypeDeConge").equals("CN") && !validateSolde(nbrJoursDemandes, soldeConges)) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean onBeforeSave() {
		if(!validateForm()){ 
			return false;
		}
		//onRetourConge();
		return super.onBeforeSave();
	}

	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("RetournerPourModification")){
			ClickButton("RetournerPourModification2");
		}
		return super.onBeforeSubmit(action);
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

	private void onRetourConge() {
		String categorieConges = (String)document.getValue("TypeDeConge");
		Float nbrJoursDemandes = (Float)document.getValue("NombreDeJoursDemandes");
		Float nbrJoursExceptionnelle = (Float)document.getValue("NombreDeJoursExceptionnelle");
		if(categorieConges.equals("CN")){
			onRetourCongesPayes(nbrJoursDemandes);
		} else if(categorieConges.equals("CE")){
			onRetourCongesSpeciaux(nbrJoursExceptionnelle);
		} else if(categorieConges.equals("SS")){
			onRetourCongesSansSolde(nbrJoursDemandes);
		} else if(categorieConges.equals("CM")){
			onRetourCongesMaladie(nbrJoursDemandes);
		}
		
		demandeur.save(getWorkflowModule().getSysadminContext());
		IStorageResource userFiche = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(demandeur, getWorkflowModule(), getProjectModule(), getWorkflowInstance().getCatalog().getProject().getOrganization());
		userFiche.save(getWorkflowModule().getSysadminContext());
		
//		document.save(getWorkflowModule().getSysadminContext());
	}
	
	private void onRetourCongesPayes(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
		document.setValue("demandeurCongesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));

	}
	
	private void onRetourCongesSpeciaux(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
		document.setValue("demandeurCongesSpeciauxEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation"));
		
	}
	
	private void onRetourCongesSansSolde(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
		document.setValue("demandeurCongesSansSoldeEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation"));
	}
	
	private void onRetourCongesMaladie(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") - nbrJoursDemandes);
		document.setValue("demandeurCongesMaladieEnCoursDeConsommation", demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
	}

	public boolean validateDates(Object startDate, Object endDate) {
		Date dateDebut = null;
		Date dateFin = null;
		if (startDate != null) {
			dateDebut = (Date) startDate;
		}
		if (endDate != null) {
			dateFin = (Date) endDate;
		}

		if (dateDebut != null) {
			if (new DateValidator(getResourceController()).isDateBeforeDate(dateDebut, new Date())) {
				onInvalidForm("La date de debut de congés doit être aprés la date aujourd'hui");
				return false;
			}
		}
		if (dateFin != null) {
			if (new DateValidator(getResourceController()).isDateBeforeDate(dateFin, new Date())) {
				onInvalidForm("La date de fin de congés doit être aprés la date aujourd'hui");
				return false;
			}
		}

		if (dateDebut != null && dateFin != null) {
			if (new DateValidator(getResourceController()).isDateAfterDate(dateDebut, dateFin)) {
				onInvalidForm("La date de fin doit être aprés la date de début");
				return false;
			}
			if (!isDatesOvelapped(dateDebut, dateFin)) {
				document.setValue("suggestedDateDebut", null);
				onInvalidForm();
				return false;
			}
		}
		
		return true;
	}

	private Collection<IWorkflowInstance> getAllDemandeurDemandes() {
		Collection<IWorkflowInstance> collection = null;
		try {
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("Demandeur", demandeur);
			controller.addNotInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("En cours", "Refusé", "Annulé")));
			controller.addNotEqualsConstraint("sys_Reference", document.getValue("sys_Reference"));
			collection = controller.evaluate(w);
			return collection;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isDatesOvelapped(Date dateDebut, Date dateFin) {
		SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
		Collection<IWorkflowInstance> allDemandes = getAllDemandeurDemandes();
		for (IWorkflowInstance demande : allDemandes) {
			Date demandeDebut = (Date) demande.getValue("DateDeDebut");
			Date demandeFin = (Date) demande.getValue("DateDeFin");
			if (dateDebut != null && dateFin != null) {
				if (new DateValidator(getResourceController()).isTwoDatesOverlapped(dateDebut, dateFin, demandeDebut, demandeFin)) {
					getResourceController().alert("Vous avez déja séléctionner cette période dans autre demande.\nLa période séléctionnée est " + simpleFormat.format(demandeDebut) + " jusqu’à " + simpleFormat.format(demandeFin));
					return false;
				}
			} else if (dateDebut != null) {
				if (new DateValidator(getResourceController()).isDateInsideRange(dateDebut, demandeDebut, demandeFin)) {
					onInvalidForm();
					getResourceController().alert("La date de début sélectionnée et entre une période dans une autre demande.\nLa période est " + simpleFormat.format(demandeDebut) + " jusqu’à " + simpleFormat.format(demandeFin));
					return false;
				}
			} else if (dateFin != null) {
				if (new DateValidator(getResourceController()).isDateInsideRange(dateFin, demandeDebut, demandeFin)) {
					onInvalidForm();
					getResourceController().alert("La date de fin sélectionnée et entre une période dans une autre demande.\nLa période est " + simpleFormat.format(demandeDebut) + " jusqu’à " + simpleFormat.format(demandeFin));
					return false;
				}
			}

		}
		return true;
	}

	public boolean validateSolde(float nbrJoursDemandes, float soldeConges) {
		if (soldeConges <= 0 || soldeConges < nbrJoursDemandes) {
			if (soldeConges <= 0) {
				getResourceController().alert("Vous n'avez pas un solde de congés");
			}
			if (soldeConges < nbrJoursDemandes) {
				getResourceController().alert("Le nombre de jours demandés (" + nbrJoursDemandes + ") doit être inférieur ou égal au solde congés (" + soldeConges + ")");
			}
			onInvalidForm();
			document.setValue("suggestedNombreDeJoursDeConges", 0);
			return false;
		}
		return true;
	}

	public void onInvalidForm(String... message) {
		document.setValue("suggestedDateDebut", null);
		document.setValue("suggestedDateFin", null);
		document.setValue("DemandeurSoldeConge", demandeur.getExtendedAttributes().getValue("SoldeConges"));
		document.setValue("DemandeurTotalJourEnCoursValidation", demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
		document.setValue("suggestedNombreDeJoursDeConges", 0);
		if (message.length > 0) {
			getResourceController().alert(message[0]);
		}
	}


	private float calculateTrancheMinusValue(Date dateDebutConge, Date dateFinConge, int diffJoursOuvrablesJoursOuvres , boolean injoursOuvrablesNotjoursOuvres) {
		float heureDebutFinMinus = 0;
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(dateFinConge);

		String debut = (String) document.getValue("SuggestedDebutConge");
		String fin = (String) document.getValue("SuggestedFinConge");

		if (dateDebutConge.equals(dateFinConge)) {
			// Same day leave
			if ("TJ".equals(debut) && "TJ".equals(fin)) {
				// Morning only → 0.5 day
				heureDebutFinMinus += 0.5;
				if (injoursOuvrablesNotjoursOuvres){
					heureDebutFinMinus+=diffJoursOuvrablesJoursOuvres;
				}
			} else if ("DJ".equals(debut) && "DJ".equals(fin)) {
				// Afternoon only → 0.5 day
				heureDebutFinMinus += 0.5;

			} else if ("DJ".equals(debut) && "TJ".equals(fin)) {
				// Invalid: end before start
				getResourceController().alert("Tranche de congé invalide : fin avant début pour le même jour.");
				getWorkflowInstance().setValue("SuggestedFinConge","DJ");
				//throw new IllegalArgumentException("Tranche de congé invalide : fin avant début pour le même jour.");
			}
			// If (TJ, DJ): full day → no subtraction
		} else {
			// Multi-day leave
			if ("DJ".equals(debut)) {
				heureDebutFinMinus += 0.5;
			}
			if ("TJ".equals(fin)) {
				heureDebutFinMinus += 0.5;
				if (injoursOuvrablesNotjoursOuvres){
					heureDebutFinMinus+=diffJoursOuvrablesJoursOuvres;
				}
			}
		}

		return heureDebutFinMinus;
	}

	public void calculateConges() {
		String categorieConges = (String) document.getValue("TypeDeConge");
		if (categorieConges.equals("CN")) {
			calculateCongesPayes();
		} else if (categorieConges.equals("CE")) {
			calculateCongesSpeciaux();
		} else if (categorieConges.equals("CM")) {
			calculateCongesMaladie();
		} else if (categorieConges.equals("SS")) {
			calculateCongesSansSolde();
		}
		if (validateForm()) {
			setDemandeurCongesFields();
		} else {
			resetForm();
		}
	}
	
	public void calculateCongesPayes() {
		Date dateDebutConge = getDateDebutConge();
		Date dateFinConge = getDateFinConge();
		if (dateDebutConge != null && dateFinConge != null) {

			HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutConge, dateFinConge, false);

			boolean isStartDayAWorkingDay = (boolean) calculResult.get("isStartDayAWorkingDay");
			boolean isEndDayAWorkingDay = (boolean) calculResult.get("isEndDayAWorkingDay");
			float totalJoursFerieInPeriod = (Float) calculResult.get("totalJoursFerieInPeriod");
			float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");

			if (!isStartDayAWorkingDay) {
				document.setValue("SuggestedDebutConge", "TJ");
			}
			if (!isEndDayAWorkingDay) {
				document.setValue("SuggestedFinConge", "TJ");
			}
			document.setValue("isSuggestedStartDayAWorkingDay", isStartDayAWorkingDay);
			document.setValue("isSuggestedEndDayAWorkingDay", isEndDayAWorkingDay);
			document.setValue("suggestedTotalJoursFerie", totalJoursFerieInPeriod);

			// The method "calculateTrancheMinusValue" calculate the value of
			// the tranches
			// example => (if the tranche debut is "Demi journée" and tranche
			// fin est "Toute la journée" it returns "-0.5")
			nbrJoursDemande -= calculateTrancheMinusValue(dateDebutConge, dateFinConge,(int)calculResult.get("diffJoursOuvrablesJoursOuvres"),(boolean)calculResult.get("injoursOuvrablesNotjoursOuvres"));

			document.setValue("suggestedNombreDeJoursDeConges", nbrJoursDemande);
		} else {
			setDemandeurCongesFields();
		}
	}
	
	public void calculateCongesSpeciaux() {
		onTypeCongeExceptionnelleChange();
	}

	public void calculateCongesMaladie() {
		Date dateDebutConge = getDateDebutConge();
		Date dateFinConge = getDateFinConge();
		if (dateDebutConge != null && dateFinConge != null) {

			HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutConge, dateFinConge,false);

			boolean isStartDayAWorkingDay = (boolean) calculResult.get("isSuggestedStartDayAWorkingDay");
			boolean isEndDayAWorkingDay = (boolean) calculResult.get("isSuggestedEndDayAWorkingDay");
			float totalJoursFerieInPeriod = (Float) calculResult.get("totalJoursFerieInPeriod");
			float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");

			if (!isStartDayAWorkingDay) {
				document.setValue("SuggestedDebutConge", "TJ");
			}
			if (!isEndDayAWorkingDay) {
				document.setValue("SuggestedFinConge", "TJ");
			}
			document.setValue("isSuggestedStartDayAWorkingDay", isStartDayAWorkingDay);
			document.setValue("isSuggestedEndDayAWorkingDay", isEndDayAWorkingDay);
			document.setValue("suggestedTotalJoursFerie", totalJoursFerieInPeriod);

			// The method "calculateTrancheMinusValue" calculate the value of
			// the tranches
			// example => (if the tranche debut is "Demi journée" and tranche
			// fin est "Toute la journée" it returns "-0.5")
			nbrJoursDemande -= calculateTrancheMinusValue(dateDebutConge, dateFinConge, (int)calculResult.get("diffJoursOuvrablesJoursOuvres"),(boolean)calculResult.get("injoursOuvrablesNotjoursOuvres"));

			document.setValue("suggestedNombreDeJoursDeConges", nbrJoursDemande);
		} else {
			setDemandeurCongesFields();
		}
	}

	public void calculateCongesSansSolde() {
		Date dateDebutConge = getDateDebutConge();
		Date dateFinConge = getDateFinConge();
		if (dateDebutConge != null && dateFinConge != null) {

			HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutConge, dateFinConge,false);

			boolean isStartDayAWorkingDay = (boolean) calculResult.get("isSuggestedStartDayAWorkingDay");
			boolean isEndDayAWorkingDay = (boolean) calculResult.get("isSuggestedEndDayAWorkingDay");
			float totalJoursFerieInPeriod = (Float) calculResult.get("totalJoursFerieInPeriod");
			float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");

			if (!isStartDayAWorkingDay) {
				document.setValue("SuggestedDebutConge", "TJ");
			}
			if (!isEndDayAWorkingDay) {
				document.setValue("SuggestedFinConge", "TJ");
			}
			document.setValue("isSuggestedStartDayAWorkingDay", isStartDayAWorkingDay);
			document.setValue("isSuggestedEndDayAWorkingDay", isEndDayAWorkingDay);
			document.setValue("suggestedTotalJoursFerie", totalJoursFerieInPeriod);

			// The method "calculateTrancheMinusValue" calculate the value of the tranches
			// example => (if the tranche debut is "Demi journée" and tranche fin est "Toute la journée" it returns "-0.5")
			nbrJoursDemande -= calculateTrancheMinusValue(dateDebutConge, dateFinConge, (int)calculResult.get("diffJoursOuvrablesJoursOuvres"),(boolean)calculResult.get("injoursOuvrablesNotjoursOuvres"));

			document.setValue("suggestedNombreDeJoursDeConges", nbrJoursDemande);
		} else {
			setDemandeurCongesFields();
		}
	}

}