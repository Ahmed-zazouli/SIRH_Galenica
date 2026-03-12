package com.moovapps.capone.rh.GestionDeConge.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.capone.rh.GestionDeConge.helpers.JoursOuvrableOuvres;
import com.moovapps.capone.rh.GestionDeConge.helpers.WorkingDaysNumberCalculator;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;

import java.util.*;

public class TraitementRh extends BaseDocumentExtension {

	IWorkflowInstance document = null;
	IUser demandeur = null;
	IStorageResource societe = null;
	
	// SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY = 6, SATURDAY = 7
	ArrayList<Integer> joursOuvrables = new ArrayList<Integer>(Arrays.asList(2,3,4,5,6,7));
	
	// SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY = 6, SATURDAY = 7
	ArrayList<Integer> joursOuvres = new ArrayList<Integer>(Arrays.asList(2,3,4,5,6));
  
	public boolean onAfterLoad() {
	  this.document = getWorkflowInstance();
	  this.demandeur = (IUser)this.document.getValue("Demandeur");
	  this.societe= (IStorageResource)this.document.getValue("Societe");
	  setJourOuvrablesJourOuvres();
	  return super.onAfterLoad();
	}
	
	private void setJourOuvrablesJourOuvres() {
		joursOuvrables =  new JoursOuvrableOuvres().getJoursOuvrables(societe);
		joursOuvres =  new JoursOuvrableOuvres().getJoursOuvres(societe);
	}
  
	public boolean onBeforeSubmit(IAction action) {
		if (action.getName().equals("Cloturer")
				|| action.getName().equals("CloturerLeCongeOuAbsenceDuCollaborateur")
				|| action.getName().equals("ValiderCongeDuCollaborateur2")) {
			onCloturerClick();

			if(!document.getValue("TypeDeConge").equals("Absence")){
				try {
					IContext context = getWorkflowModule().getSysadminContext();
					IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
					ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE);
					IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "CongesConsolides");
					Date startDate = (Date) this.document.getValue("DateDeDebut");
					Date endDate = (Date) this.document.getValue("DateDeFin");
					
					HashMap<String, Object> calculatorResult = new WorkingDaysNumberCalculator( getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,startDate, endDate, false);
					HashMap<Float, Float> nbrDaysInYears = (HashMap<Float, Float>) calculatorResult.get("nbrDaysInYears");
					if (startDate.equals(endDate)) {
						if (this.document.getValue("DebutConge").equals("DJ")){
							nbrDaysInYears.put(Float.valueOf(((Float) nbrDaysInYears.keySet().toArray()[nbrDaysInYears.size() - 1]).floatValue()), Float.valueOf(((Float) nbrDaysInYears.get(nbrDaysInYears.keySet().toArray()[nbrDaysInYears.size() - 1])).floatValue() - 0.5F));						
						}
					} else {
						if (this.document.getValue("DebutConge").equals("DJ")) {
							float t = ((Float) nbrDaysInYears.get(nbrDaysInYears.keySet().toArray()[nbrDaysInYears.size() - 1])).floatValue();
							nbrDaysInYears.put(Float.valueOf(((Float) nbrDaysInYears.keySet().toArray()[0]).floatValue()),Float.valueOf(((Float) nbrDaysInYears.get(nbrDaysInYears.keySet().toArray()[0])).floatValue() - 0.5F));
						}
						if (this.document.getValue("FinConge").equals("DJ")){						
							nbrDaysInYears.put(Float.valueOf(((Float) nbrDaysInYears.keySet().toArray()[nbrDaysInYears.size() - 1]).floatValue()),Float.valueOf(((Float) nbrDaysInYears.get(nbrDaysInYears.keySet().toArray()[nbrDaysInYears.size() - 1])).floatValue() - 0.5F));
						}
					}
					controller.addEqualsConstraint("Collaborateur", this.demandeur);
					Collection<IStorageResource> collabNbrJoursCongesParAnnee = controller.evaluate(definition);
					for (Map.Entry<Float, Float> entry : nbrDaysInYears.entrySet()) {
						float key = ((Float) entry.getKey()).floatValue();
						float value = ((Float) entry.getValue()).floatValue();
						if (collabNbrJoursCongesParAnnee.size() > 0) {
							boolean collabYearUpdated = false;
							for (IStorageResource collabByYear : collabNbrJoursCongesParAnnee) {
								if (collabByYear.getValue("Annee").equals(Float.valueOf(key))) {
									collabYearUpdated = true;
									collabByYear.setValue("NombreDeJoursDeConges", Float.valueOf(((Float) collabByYear.getValue("NombreDeJoursDeConges")).floatValue() + value));
									collabByYear.save(context);
								}
							}
							if (!collabYearUpdated){							
								insertCongesConsolides(context, definition, key, value);
							}
							continue;
						}
						insertCongesConsolides(context, definition, key, value);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return super.onBeforeSubmit(action);
	}
	
	
	private void onCloturerClick() {
		float nbrJoursDemandes = 0;
		if(document.getValue("NombreDeJoursDemandes") != null){
			nbrJoursDemandes = ((Number)document.getValue("NombreDeJoursDemandes")).floatValue();
		}
		float nbrJoursSpeciaux = 0;
		if(document.getValue("NombreDeJoursExceptionnelle") != null){
			nbrJoursSpeciaux = ((Number)document.getValue("NombreDeJoursExceptionnelle")).floatValue();
		}
		
		String categorieConges = (String)document.getValue("TypeDeConge");
		if(categorieConges.equals("CN")){
			onCloturerCongesPayes(nbrJoursDemandes);
			calculateCommunCongesFields(nbrJoursDemandes);
		} else if(categorieConges.equals("CE")){
			nbrJoursDemandes = nbrJoursSpeciaux;
			onCloturerCongesSpeciaux(nbrJoursDemandes);
			//calculateCommunCongesFields(nbrJoursDemandes);
		} else if(categorieConges.equals("CM")){
			onCloturerCongesMaladie(nbrJoursDemandes);
			calculateCommunCongesFields(nbrJoursDemandes);
		} else if(categorieConges.equals("SS")){
			onCloturerCongesSansSolde(nbrJoursDemandes);
			//calculateCommunCongesFields(nbrJoursDemandes);
		} else if(categorieConges.equals("Absence")){
			onCloturerAbsence(nbrJoursDemandes);
		}
		document.setValue("DemandeurCongesEnCoursDeTraitement", (Float)document.getValue("DemandeurCongesEnCoursDeTraitement"));

		IStorageResource userFiche = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(demandeur, getWorkflowModule(), getProjectModule(), getWorkflowInstance().getCatalog().getProject().getOrganization());
		userFiche.save(getWorkflowModule().getSysadminContext());
		demandeur.save(getWorkflowModule().getSysadminContext());

	}
	
	private void onCloturerCongesPayes(float nbrJoursDemandes) {
		//demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation") - nbrJoursDemandes);
		//demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") );
		demandeur.getExtendedAttributes().setValue("CongesPayesPris",(Float)demandeur.getExtendedAttributes().getValue("CongesPayesPris") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("TotalJoursPris",(Float)demandeur.getExtendedAttributes().getValue("TotalJoursPris") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesPayesAnneeEnCours",(Float)demandeur.getExtendedAttributes().getValue("CongesPayesAnneeEnCours") );
		//demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeTraitement") - nbrJoursDemandes);
		//demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement")- nbrJoursDemandes);
	//	document.setValue("demandeurCongesValidesAConsommer", (Float)document.getValue("demandeurCongesValidesAConsommer") );
		//document.setValue("CongesPayesEnCoursDeTraitement", (Float)document.getValue("CongesPayesEnCoursDeTraitement"));
		//document.setValue("DemandeurCongesEnCoursDeTraitement", (Float)document.getValue("DemandeurCongesEnCoursDeTraitement"));
	}
	
	private void onCloturerCongesSpeciaux(float nbrJoursDemandes) {
		//demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation") - nbrJoursDemandes);
		//demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation"));
		demandeur.getExtendedAttributes().setValue("CongesSpeciauxPris",(Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxPris") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("TotalJoursPris",(Float)demandeur.getExtendedAttributes().getValue("TotalJoursPris") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesSpeciauxAnneeEnCours",(Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours"));
		//demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeTraitement") - nbrJoursDemandes);
		//demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement")- nbrJoursDemandes);
		//document.setValue("CongesSpeciauxEnCoursDeTraitement", (Float)document.getValue("CongesSpeciauxEnCoursDeTraitement") );
	}
	
	private void onCloturerCongesMaladie(float nbrJoursDemandes) {
		//demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation") - nbrJoursDemandes);
		//demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") - nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesMaladiePris",(Float)demandeur.getExtendedAttributes().getValue("CongesMaladiePris") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("TotalJoursPris",(Float)demandeur.getExtendedAttributes().getValue("TotalJoursPris") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesMaladieAnneeEnCours",(Float)demandeur.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours"));
		//demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeTraitement") - nbrJoursDemandes);
		//demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement")- nbrJoursDemandes);
		//document.setValue("CongesMaladieEnCoursDeTraitement", (Float)document.getValue("CongesMaladieEnCoursDeTraitement"));
	}
	
	private void onCloturerCongesSansSolde(float nbrJoursDemandes) {
		//demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation") - nbrJoursDemandes);
		//demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation"));
		demandeur.getExtendedAttributes().setValue("CongesSansSoldePris",(Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldePris") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("TotalJoursPris",(Float)demandeur.getExtendedAttributes().getValue("TotalJoursPris") + nbrJoursDemandes);
		demandeur.getExtendedAttributes().setValue("CongesSansSoldeAnneeEnCours",(Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours"));
		//demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeTraitement") - nbrJoursDemandes);
		//demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement")- nbrJoursDemandes);
		//document.setValue("demandeurCongesMaladieEnCoursDeConsommation", (Float)document.getValue("demandeurCongesMaladieEnCoursDeConsommation"));
		//document.setValue("CongesSansSoldeEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeTraitement"));
	}
	
	private void onCloturerAbsence(float nbrJoursDemandes) {
		demandeur.getExtendedAttributes().setValue("AbsenceAnneeEnCours",(Float)demandeur.getExtendedAttributes().getValue("AbsenceAnneeEnCours") + nbrJoursDemandes);
	}

	private void calculateCommunCongesFields(float nbrJoursDemandes) {

		/*get sold interieur
		if akbar mn 0 then minus jours demande then get the absolute valeur and minus it from solde anne en cours */

		demandeur.getExtendedAttributes().setValue("SoldeConges", (Float)demandeur.getExtendedAttributes().getValue("SoldeConges") - nbrJoursDemandes);
		float soldeAnterieur = 0;
		if(demandeur.getExtendedAttributes().getValue("SoldeAnterieur") != null){
			soldeAnterieur = (Float)demandeur.getExtendedAttributes().getValue("SoldeAnterieur");
		}
		float soldeAnneeAnterieur = soldeAnterieur - nbrJoursDemandes;
		if(soldeAnneeAnterieur < 0){
			demandeur.getExtendedAttributes().setValue("SoldeAnterieur",0);
			demandeur.getExtendedAttributes().setValue("SoldeAnneeEnCours", (Float)demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours") + soldeAnneeAnterieur);
		} else{
			demandeur.getExtendedAttributes().setValue("SoldeAnterieur",(Float)demandeur.getExtendedAttributes().getValue("SoldeAnterieur") - nbrJoursDemandes);
		}
	}
	
	public boolean insertCongesConsolides(IContext context, IResourceDefinition definition, float key, float value) {
		try {
			IStorageResource storageRessource = getWorkflowModule().createStorageResource(getWorkflowModule().getSysadminContext(), definition, "");
			storageRessource.setValue("Annee", Float.valueOf(key));
			storageRessource.setValue("Collaborateur", this.demandeur);
			storageRessource.setValue("NombreDeJoursDeConges", Float.valueOf(value));
			storageRessource.save(context);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
