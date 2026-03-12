package com.moovapps.capone.rh.GestionDeSortie;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.capone.rh.cummonHelpers.DateValidator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;

public class DemandeDeSortie extends BaseDocumentExtension {

	public IWorkflowInstance document = null;
	public String amITheConcernedPersonField = "siJeSuisLeDemandeur";
	public String concernedPersonField = "Demandeur";
//	public String validateurField = "Validateur";
	public String startTimeField = "dateHeureSortie";
	public String estimatedEndTimeField = "dateHeureRetourPrevu";
	public String dateField = "dateSortie";
	public String startHourField = "heureSortie";
	public String startMinuteField = "minutesSortie";
	public String estimatedEndHourField = "heureRetourPrevu";
	public String estimatedEndMinuteField = "minutesRetourPrevu";
	public String estimatedDurationInMinutesField = "dureeSortieMinutesPrevu";
	public String estimatedDurationField = "dureeSortiePrevu";
	public String actualEndTimeField = "dateHeureRetourReelle";
	public String actualDurationInMinutesField = "dureeSortieMinutesReelle";
	public String actualDurationField = "dureeSortieReelle";
	public String actualEndHourField = "heureRetourReelle";
	public String actualEndMinuteField = "minutesRetourReelle";

	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		document.setValue("siJeSuisLeDemandeur", true);



		return super.onAfterLoad();
	}




	public void onPropertyChanged(IProperty property) {
		if (property.getName().equals(amITheConcernedPersonField)) { 
			onAmITheConcernedPersonFieldChange();
		} else if (property.getName().equals(concernedPersonField)) {
			onConcernedPersonChange();
		} else if (property.getName().equals(startTimeField)) {
			onStartTimeChange();
		} else if (property.getName().equals(estimatedEndTimeField)) {
			onDateEtHeureFinChange();
		} else if (property.getName().equals(dateField) || property.getName().equals(startHourField) || property.getName().equals(startMinuteField)) {
			if (property.getName().equals(dateField)) {
				setDatesFields(estimatedEndHourField, estimatedEndMinuteField, estimatedEndTimeField);
			}
			setDatesFields(startHourField, startMinuteField, startTimeField);
			onStartTimeChange();
		} else if (property.getName().equals(dateField) || property.getName().equals(estimatedEndHourField) || property.getName().equals(estimatedEndMinuteField)) {
			if (property.getName().equals(dateField)) {
				setDatesFields(startHourField, startMinuteField, startTimeField);
			}
			setDatesFields(estimatedEndHourField, estimatedEndMinuteField, estimatedEndTimeField);
			onDateEtHeureFinChange();
		}else if(property.getName().equals("DemandeurDonnee")){
			IStorageResource demandeurDonnee = (IStorageResource) getWorkflowInstance().getValue("DemandeurDonnee");
			IUser demandeur = demandeurDonnee!=null?(IUser) demandeurDonnee.getValue("Salarie"):null;
			getWorkflowInstance().setValue("Demandeur",demandeur);
			onConcernedPersonChange();
		}
		super.onPropertyChanged(property);
	}



	public void setDatesFields(String hoursField, String minutesFields, String fieldToSet) {
		if (document.getValue(dateField) != null && document.getValue(hoursField) != null && document.getValue(minutesFields) != null) {
			Calendar calendar = Calendar.getInstance();
			Date date = (Date) document.getValue(dateField);
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt((String) document.getValue(hoursField)));
			calendar.set(Calendar.MINUTE, Integer.parseInt((String) document.getValue(minutesFields)));
			document.setValue(fieldToSet, calendar.getTime());
		} else {
			document.setValue(estimatedDurationInMinutesField, null);
			document.setValue(estimatedDurationField, null);
			document.setValue(fieldToSet, null);
			document.setValue(actualEndTimeField, document.getValue(estimatedEndTimeField));
			document.setValue(actualDurationInMinutesField, document.getValue(estimatedDurationInMinutesField));
			document.setValue(actualDurationField, document.getValue(estimatedDurationField));
		}
	}

	public void onAmITheConcernedPersonFieldChange() {
		boolean amITheConcernedPerson = ((Boolean) document.getValue(amITheConcernedPersonField)).booleanValue();
		if (amITheConcernedPerson) {
			document.setValue(concernedPersonField, getDocument().getUser());
			onConcernedPersonChange();
		} else {
			document.setValue("Societe", null);
			document.setValue(concernedPersonField, null);
			document.setValue("SupHierarchique", null);
			document.setValue("TypeDeValidationSortie", null);
			document.setValue("DestinataireSortie", null);
			document.setValue("ResponsableRH", null);
		}
		
	}
	public void setSocieteFields(){
		
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
	
	public void onConcernedPersonChange() {
 		if (document.getValue(concernedPersonField) != null) {
			if (((IUser) document.getValue(concernedPersonField)).getId().equals(getDocument().getUser().getId())){				
				document.setValue(amITheConcernedPersonField, true);
			}
			IUser demandeur = (IUser) document.getValue(concernedPersonField);
			IUser hierarchicalManager = demandeur.getHierarchicalManager();
			document.setValue("SupHierarchique", hierarchicalManager);

			//societe flield
			document.setValue("Societe", demandeur.getExtendedAttributes().getValue("Societe"));
			IStorageResource societe = (IStorageResource) demandeur.getExtendedAttributes().getValue("Societe");
			document.setValue("ResponsableRH",societe != null ? societe.getValue("ResponsableRH") : null);
			document.setValue("TypeDeValidationSortie",societe != null ? societe.getValue("TypeDeValidationSortie") : null);
			//destinataire


				if(document.getValue("TypeDeValidationSortie") != null && document.getValue("TypeDeValidationSortie").equals("Responsable Hiérarchique")){
					document.setValue("DestinataireSortie", hierarchicalManager);
					
				} else {
					document.setValue("DestinataireSortie", null);
				}


		}
	}

	public void onStartTimeChange() {
		Date startTime = null;
		Date endTime = null;
		if (document.getValue(startTimeField) != null && document.getValue(estimatedEndTimeField) != null) {
			startTime = (Date) document.getValue(startTimeField);
			endTime = (Date) document.getValue(estimatedEndTimeField);
			calculateDuration(startTime, endTime);
		}
	}

	public void onDateEtHeureFinChange() {
		Date startTime = null;
		Date endTime = null;
		if (document.getValue(estimatedEndTimeField) != null && document.getValue(startTimeField) != null) {
			endTime = (Date) document.getValue(estimatedEndTimeField);
			startTime = (Date) document.getValue(startTimeField);
			calculateDuration(startTime, endTime);
		}
	}

	public void calculateDuration(Date startTime, Date endTime) {
		if (validateDates(startTime, endTime)) {
			long retardDurationInMinutes = (new DateValidator()).getDurationBetweenTwoDatesInMin(startTime, endTime);
			if (retardDurationInMinutes == 0L) {
				getResourceController().alert("Vous n'etes pas en retard");
				onInvalidForm();
			} else {
				document.setValue(estimatedDurationInMinutesField, Long.valueOf(retardDurationInMinutes));
				document.setValue(estimatedDurationField, (new DateValidator()).durationToString(retardDurationInMinutes));
			}
		} else {
			onInvalidForm();
		}
		document.setValue(actualEndHourField, document.getValue(estimatedEndHourField));
		document.setValue(actualEndMinuteField, document.getValue(estimatedEndMinuteField));
		document.setValue(actualEndTimeField, document.getValue(estimatedEndTimeField));
		document.setValue(actualDurationInMinutesField, document.getValue(estimatedDurationInMinutesField));
		document.setValue(actualDurationField, document.getValue(estimatedDurationField));
	}

	public boolean validateDates(Date startTime, Date endTime) {
		if (startTime == null || endTime == null){			
			return false;
		}
		if (endTime.before(startTime)) {
			getResourceController().alert("Le temps de retour doit être aprés le temps de départ");
			return false;
		}
		return true;
	}

	public boolean onBeforeSubmit(IAction action) {
		if (action.getName().equals("Demander")) {
			Date startTime = (Date) document.getValue(startTimeField);
			Date endTime = (Date) document.getValue(estimatedEndTimeField);
			if (!validateDates(startTime, endTime)){				
				return false;
			}
			boolean siJesuisLeDemandeur = (boolean) getWorkflowInstance().getValue("siJeSuisLeDemandeur");

			if(siJesuisLeDemandeur &&  getWorkflowInstance().getValue("TypeDeValidationSortie").equals("Responsable Hiérarchique")){
				IUser validator = (IUser) getWorkflowInstance().getValue("DestinataireSortie");
				if(validator==null){
					getResourceController().alert("Vous devez avoir un responsable hiérarchique");
					return false;
				}
			}

			calculateDuration(startTime, endTime);

			if(getWorkflowInstance().getValue("Societe") != null){
				IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");

				document.setValue("ModeleDocumentSortie",getPieceJointe((ArrayList<IAttachment>) societe.getValue("DocumentSortie")));
			}
			if (getWorkflowInstance().getValue("Demandeur") != null){
				IUser demandeur = (IUser) getWorkflowInstance().getValue("Demandeur");
				getWorkflowInstance().setValue("NomPrenom",demandeur.getFullName());
				getWorkflowInstance().setValue("Matricule" , demandeur.getExtendedAttributes().getValue("Matricule"));
			}
		}


		document.save(getWorkflowModule().getSysadminContext());
		return super.onBeforeSubmit(action);
	}

	public void onInvalidForm() {
		document.setValue(startHourField, null);
		document.setValue(startMinuteField, null);
		document.setValue(estimatedEndHourField, null);
		document.setValue(estimatedEndMinuteField, null);
		document.setValue(estimatedDurationInMinutesField, null);
		document.setValue(estimatedDurationField, null);
		document.setValue(startTimeField, null);
		document.setValue(estimatedEndTimeField, null);
	}
	
}