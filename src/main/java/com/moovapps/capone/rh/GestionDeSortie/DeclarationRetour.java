package com.moovapps.capone.rh.GestionDeSortie;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.capone.rh.cummonHelpers.DateValidator;

import java.util.Calendar;
import java.util.Date;

public class DeclarationRetour extends BaseDocumentExtension {

	public IWorkflowInstance document = null;
	public String dateField = "dateSortie";
	public String startTimeField = "dateHeureSortie";
	public String declarationFinField = "FinSortie";
	public String commentaireValidationFinField = "commentaireValidationFinRetard";
	public String actualEndTimeField = "dateHeureRetourReelle";
	public String actualDurationInMinutesField = "dureeSortieMinutesReelle";
	public String actualDurationField = "dureeSortieReelle";
	public String actualEndHourField = "heureRetourReelle";
	public String actualEndMinuteField = "minutesRetourReelle";

	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		return super.onAfterLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property) {
		if (property.getName().equals(actualEndTimeField)) {
			onActualEndTimeChange();
		} else if (property.getName().equals(actualEndHourField) || property.getName().equals(actualEndMinuteField)) {
			setDatesFields();
			onActualEndTimeChange();
		}
		super.onPropertyChanged(property);
	}

	public void setDatesFields() {
		Calendar calendar = Calendar.getInstance();
		Date date = (Date) document.getValue(dateField);
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt((String) document.getValue(actualEndHourField)));
		calendar.set(Calendar.MINUTE, Integer.parseInt((String) document.getValue(actualEndMinuteField)));
		document.setValue(actualEndTimeField, calendar.getTime());
	}

	public void onActualEndTimeChange() {
		Date startTime = (Date) document.getValue(startTimeField);
		Date endTime = null;
		if (document.getValue(actualEndTimeField) != null) {
			endTime = (Date) document.getValue(actualEndTimeField);
			calculateDuration(startTime, endTime);
		}
	}

	@Override
	public boolean onBeforeSubmit(IAction action) {
		if (action.getName().equals(declarationFinField)) {
			Date startTime = (Date) document.getValue(startTimeField);
			Date endTime = (Date) document.getValue(actualEndTimeField);
			if (!validateDates(startTime, endTime)){				
				return false;
			}
			calculateDuration(startTime, endTime);
			document.setValue(commentaireValidationFinField, "");
			document.save(getWorkflowModule().getSysadminContext());
		}
		return super.onBeforeSubmit(action);
	}

	public void calculateDuration(Date startTime, Date endTime) {
		if (validateDates(startTime, endTime)) {
			long retardDurationInMinutes = (new DateValidator()).getDurationBetweenTwoDatesInMin(startTime, endTime);
			document.setValue(actualDurationInMinutesField, Long.valueOf(retardDurationInMinutes));
			document.setValue(actualDurationField, (new DateValidator()).durationToString(retardDurationInMinutes));
		} else {
			document.setValue(actualDurationInMinutesField, null);
			document.setValue(actualDurationField, null);
		}
	}

	public boolean validateDates(Date startTime, Date endTime) {
		if (endTime.before(startTime)) {
			getResourceController().alert("Le temps de retour doit être aprés le temps de départ");
			return false;
		}
		return true;
	}
}
