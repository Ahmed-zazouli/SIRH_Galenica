package com.moovapps.capone.rh.GestionDeRetard;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.capone.rh.cummonHelpers.DateValidator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DeclarationDeRetard extends BaseDocumentExtension {
	
	public IWorkflowInstance document = null;
	public IUser demandeur = null;
	public String amITheConcernedPersonField = "siJeSuisLeRetardataire";
	public String concernedPersonField = "retardataire";
	public String validateurField = "Validateur";
	public String startTimeField = "dateEtHeureDebutPrevisionnelle";
	public String estimatedEndTimeField = "dateEtHeureFinPrevisionnelle";
	public String dateField = "dateDuRetard";
	public String startHourField = "heureDebut";
	public String startMinuteField = "minuteDebut";
	public String estimatedEndHourField = "heureFinPrevu";
	public String estimatedEndMinuteField = "minuteFinPrevu";
	public String estimatedDurationInMinutesField = "dureeRetardPrevisionnelleEnMinutes";
	public String estimatedDurationField = "dureeRetardPrevisionnelle";
	
	public String actualEndTimeField = "dateEtHeureFinReelle";
	public String actualDurationInMinutesField = "dureeRetardReelleEnMinutes";
	public String actualDurationField = "dureeRetardReelle";
	public String actualEndHourField = "heureFinReelle";
	public String actualEndMinuteField = "MinutesFinReelle";
	
	public String trancheRetardField = "TrancheRetard";
	Integer heureDebutSociete = null;
	Integer minuteDebutSociete = null;
	Integer heureDebutPauseSociete = null;
	Integer minuteDebutPauseSociete = null;
	Integer heureFinPauseSociete = null;
	Integer minuteFinPauseSociete = null;
	Integer heureFinSociete = null;
	Integer minuteFinSociete = null;
	
	Integer dureeAutoriseMatin = null;// (heureDebutPauseSociete - heureDebutSociete) * 60 +( minuteDebutPauseSociete - minuteDebutSociete);
	Integer dureeAutoriseApresMidi = null;// (heureFinSociete - heureFinPauseSociete) * 60 +( minuteFinSociete - minuteFinPauseSociete);
	
	@Override
	public boolean onBeforeLoad() {
		document = getWorkflowInstance();

		if(document.getValue(amITheConcernedPersonField) == null){
			document.setValue(amITheConcernedPersonField, true);
			onAmITheConcernedPersonFieldChange();
		}
		setSocieteRelatedFields();
		IStorageResource societe = (IStorageResource) document.getValue("Societe");
		document.setValue("TypeDeValidationRetard",societe!=null?societe.getValue("TypeDeValidationRetard"):null);
		return super.onBeforeLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property) {
		if(property.getName().equals(amITheConcernedPersonField)){
			onAmITheConcernedPersonFieldChange();
		} else if(property.getName().equals(concernedPersonField)){
			onConcernedPersonChange();
		} else if(property.getName().equals(dateField)){
			onDateConcernedChange();
		} else if(property.getName().equals(trancheRetardField)){
			onDateConcernedChange();
		} else if(property.getName().equals(estimatedDurationInMinutesField)){
			onDateConcernedChange();
		} else if(property.getName().equals("DemandeurDonnee")){
			IStorageResource demandeurDonnee = (IStorageResource) getWorkflowInstance().getValue("DemandeurDonnee");
			IUser demandeur = demandeurDonnee!=null?(IUser) demandeurDonnee.getValue("Salarie"):null;
			getWorkflowInstance().setValue("retardataire",demandeur);
			onConcernedPersonChange();
		}
		super.onPropertyChanged(property);
	}
	
	public void onAmITheConcernedPersonFieldChange(){
		boolean amITheConcernedPerson = (boolean)document.getValue(amITheConcernedPersonField);
		if(amITheConcernedPerson){
			document.setValue(concernedPersonField, getDocument().getUser());
			onConcernedPersonChange();
		} else {
			resetForm();
		}
	}
	public void onConcernedPersonChange(){
		if(document.getValue(concernedPersonField) != null){
			if(((IUser)document.getValue(concernedPersonField)).getId().equals(getDocument().getUser().getId())){
				document.setValue(amITheConcernedPersonField, true);
			}
			
			IUser retardataire = (IUser)document.getValue(concernedPersonField);
			IUser hierarchicalManager = retardataire.getHierarchicalManager();
			document.setValue("SupHierarchique", hierarchicalManager);

			document.setValue("Societe", retardataire.getExtendedAttributes().getValue("Societe"));
			IStorageResource societe = (IStorageResource) retardataire.getExtendedAttributes().getValue("Societe");
			document.setValue("ResponsableRH",societe != null ? societe.getValue("ResponsableRH") : null);
			document.setValue("TypeDeValidationRetard",societe != null ? societe.getValue("TypeDeValidationRetard") : null);


			if( hierarchicalManager != null){
				if(document.getValue("TypeDeValidationRetard") != null && document.getValue("TypeDeValidationRetard").equals("Responsable Hiérarchique")){
					document.setValue("DestinataireRetard", hierarchicalManager);
				} else {
					document.setValue("DestinataireRetard", null);
				}
			}
			setSocieteRelatedFields();
			
			
		}
	}

	
	public void setSocieteRelatedFields(){
		
		IUser retardataire = (IUser)document.getValue(concernedPersonField);
		IStorageResource societe = (IStorageResource) retardataire.getExtendedAttributes().getValue("Societe");
	
		if(societe != null){
			
			heureDebutSociete = Integer.valueOf(((String)societe.getValue("HeureDebutPremiereShift")));
			minuteDebutSociete = Integer.valueOf(((String)societe.getValue("MinuteDebutPremiereShift")));
			heureDebutPauseSociete = Integer.valueOf(((String)societe.getValue("HeureFinPremiereShift")));
			minuteDebutPauseSociete = (Integer.valueOf((String)societe.getValue("MinuteFinPremiereShift")));
			heureFinPauseSociete = Integer.valueOf(((String)societe.getValue("HeureDebutDeuxiemeShift")));
			minuteFinPauseSociete = Integer.valueOf(((String)societe.getValue("MinuteDebutDeuxiemeShift")));
			heureFinSociete = Integer.valueOf(((String)societe.getValue("HeureFinDeuxiemeShift")));
			minuteFinSociete = Integer.valueOf(((String)societe.getValue("MinuteFinDeuxiemeShift")));
			dureeAutoriseMatin = (heureDebutPauseSociete - heureDebutSociete) * 60 +( minuteDebutPauseSociete - minuteDebutSociete);
			dureeAutoriseApresMidi = (heureFinSociete - heureFinPauseSociete) * 60 +( minuteFinSociete - minuteFinPauseSociete);
		
			
			onDateConcernedChange();
		} else {
			resetForm();
		}

	}
	
	public void onEstimatedDurationChange(){
		int durationInMinutes = 0;
		if (document.getValue(estimatedDurationInMinutesField) != null) {
			durationInMinutes = ((Number)document.getValue(estimatedDurationInMinutesField)).intValue();
			if (durationInMinutes > 0) {
				if((dureeAutoriseMatin != null && dureeAutoriseApresMidi != null) && ((document.getValue(trancheRetardField).equals("Matin") && durationInMinutes > dureeAutoriseMatin) || (document.getValue(trancheRetardField).equals("Après midi") && durationInMinutes > dureeAutoriseApresMidi))){
					getResourceController().alert("Vous avez dépasser la durée autorisée");
					document.setValue(estimatedDurationField, null);
					document.setValue(estimatedDurationInMinutesField, null);
				}
				document.setValue(estimatedDurationField, new DateValidator().durationToString(durationInMinutes));
			} else {
				getResourceController().alert("La durée ne peut pas être négative ou égal à 0");
				document.setValue(estimatedDurationField, null);
				document.setValue(estimatedDurationInMinutesField, null);
			}
		} else {
			document.setValue(estimatedDurationField, null);
			document.setValue(estimatedDurationInMinutesField, null);
		}
		document.setValue(estimatedEndTimeField, calculateHeureFin());
	}

	public Date calculateHeureFin(){
		if(document.getValue(estimatedDurationInMinutesField) != null && document.getValue(startTimeField) != null){
			Calendar c = Calendar.getInstance();
			c.setTime((Date)document.getValue(startTimeField));
			c.add(Calendar.MINUTE, ((Number)document.getValue(estimatedDurationInMinutesField)).intValue());
			return c.getTime();
		}
		return null;
	}
	
	public void onTrancheRetardChange(){
		if(document.getValue(dateField) == null || heureDebutSociete == null || minuteDebutSociete == null || heureFinPauseSociete == null || minuteFinPauseSociete == null){
			document.setValue(startTimeField, null);
			return;
		}
		Date debutRetard = new Date();
		if(document.getValue(dateField) != null){
			debutRetard = (Date)document.getValue(dateField);
		}
		String trancheRetard = "Matin";
		if(document.getValue("TrancheRetard") != null){
			trancheRetard = (String)document.getValue("TrancheRetard");
		}
		if(trancheRetard.equals("Matin")){
			Calendar c = Calendar.getInstance();
			c.setTime(debutRetard);
			c.set(Calendar.HOUR_OF_DAY, heureDebutSociete);
			c.set(Calendar.MINUTE, minuteDebutSociete);
			document.setValue(startTimeField, c.getTime());
		} else if (trancheRetard.equals("Après midi")) {
			Calendar c = Calendar.getInstance();
			c.setTime(debutRetard);
			c.set(Calendar.HOUR_OF_DAY, heureFinPauseSociete);
			c.set(Calendar.MINUTE, minuteFinPauseSociete);
			document.setValue(startTimeField, c.getTime());
		}
	}

	public void onDateConcernedChange(){
		onTrancheRetardChange();
		onEstimatedDurationChange();
	}
	
	public boolean validateDates(){
		if(document.getValue(startTimeField) == null || document.getValue(estimatedEndTimeField) == null){
			return false;
		}
		return true;
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("declarer")){
			if(!validateDates()){
				return false;
			}
			boolean siJesuisLeDemandeur = (boolean) getWorkflowInstance().getValue("siJeSuisLeRetardataire");
			if(siJesuisLeDemandeur && getWorkflowInstance().getValue("TypeDeValidationRetard").equals("Responsable Hiérarchique")){
				IUser validator = (IUser) getWorkflowInstance().getValue("DestinataireRetard");
				if(validator==null){
					getResourceController().alert("Vous devez avoir un responsable hiérarchique");
					return false;
				}
			}
			Calendar debutRetard = Calendar.getInstance();
			debutRetard.setTime((Date)document.getValue(startTimeField));
			Calendar estimatedFinRetard = Calendar.getInstance();
			estimatedFinRetard.setTime((Date)document.getValue(estimatedEndTimeField));
			
			document.setValue(actualDurationInMinutesField, document.getValue(estimatedDurationInMinutesField));
			document.setValue(actualDurationField, document.getValue(estimatedDurationField));
			
			document.setValue(startHourField, debutRetard.get(Calendar.HOUR_OF_DAY));
			document.setValue(startMinuteField, debutRetard.get(Calendar.MINUTE));
			
			document.setValue(estimatedEndHourField, estimatedFinRetard.get(Calendar.HOUR_OF_DAY));
			document.setValue(estimatedEndMinuteField, estimatedFinRetard.get(Calendar.MINUTE));
			
			document.setValue(actualEndHourField, estimatedFinRetard.get(Calendar.HOUR_OF_DAY));
			document.setValue(actualEndMinuteField, estimatedFinRetard.get(Calendar.MINUTE));
			
			document.setValue(actualEndTimeField, document.getValue(estimatedEndTimeField));
			
			if(document.getValue(trancheRetardField).equals("Matin")){
				document.setValue("DureeAutoriseeCloture", dureeAutoriseMatin);
			} else if(document.getValue(trancheRetardField).equals("Après midi")){
				document.setValue("DureeAutoriseeCloture", dureeAutoriseApresMidi);
			}
			
			document.save(getWorkflowModule().getSysadminContext());
		}
		return super.onBeforeSubmit(action);
	}
	
	public void resetForm(){
		document.setValue(startHourField, null);
		document.setValue(startMinuteField, null);
		document.setValue(estimatedEndHourField, null);
		document.setValue(estimatedEndMinuteField, null);
		// document.setValue(estimatedDurationInMinutesField, null);
		// document.setValue(estimatedDurationField, null);
		document.setValue(actualEndHourField, null);
		document.setValue(actualEndMinuteField, null);
		// document.setValue(actualDurationInMinutesField, null);
		//document.setValue(actualDurationField, null);
		document.setValue(startTimeField, null);
		document.setValue(estimatedEndTimeField, null);
		document.setValue(actualEndTimeField, null);
		
		document.setValue("Societe", null);
		document.setValue(concernedPersonField, null);
		document.setValue("SupHierarchique", null);
		document.setValue("TypeDeValidationRetard", null);
		document.setValue("DestinataireRetard", null);
		document.setValue("ResponsableRH", null);
		
		heureDebutSociete = null;
		minuteDebutSociete = null;
		heureDebutPauseSociete = null;
		minuteDebutPauseSociete = null;
		heureFinPauseSociete = null;
		minuteFinPauseSociete = null;
		heureFinSociete = null;
		minuteFinSociete = null;
		dureeAutoriseMatin = null;
		dureeAutoriseApresMidi = null;
	}

	
	
}