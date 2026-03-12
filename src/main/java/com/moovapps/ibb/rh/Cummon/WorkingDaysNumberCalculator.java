package com.moovapps.ibb.rh.Cummon;

import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WorkingDaysNumberCalculator {
	
	IWorkflowModule workflowModule = null;
	ArrayList<Integer> joursOuvrables = null;
	ArrayList<Integer> joursOuvres = null; 
	
	public WorkingDaysNumberCalculator(IWorkflowModule workflowModule, ArrayList<Integer> joursOuvrables, ArrayList<Integer> joursOuvres){
		this.workflowModule = workflowModule;
		this.joursOuvrables = joursOuvrables;
		this.joursOuvres = joursOuvres;
	}
	
	public WorkingDaysNumberCalculator(IWorkflowModule workflowModule){
		this.workflowModule = workflowModule;
	}
	
	public HashMap<String, Object> calculate(IStorageResource societe,Date startDate, Date endDate, boolean saturdayIsAWorkingDay){
		HashMap<String, Object> result = new HashMap<String, Object>();
		HashMap<Float, Float> nbrDaysInYears = new HashMap<Float, Float>();
		
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(startDate);
		
		Calendar endCalendar = new GregorianCalendar();
		endCalendar.setTime(endDate);
		endCalendar.add(Calendar.DAY_OF_WEEK, 1);
		
		boolean isStartDayAWorkingDay = true; 
		boolean isEndDayAWorkingDay = true;
		float nbrWorkingDays = 0;
		float nombreJoursDeCongesParDate = 0;
		float totalJoursFerieInPeriod = 0;
		Collection<IStorageResource> joursFeries = getJoursFeries(societe);
		
		boolean ignoreSaturday = false;
		while (calendar.before(endCalendar)) {
			nombreJoursDeCongesParDate = getNombreJoursFeriesParDate(calendar.getTime(), nombreJoursDeCongesParDate, joursFeries);
			// in order to not count saturday if it's in the end date remove the third part of the if statement (the one below)
			// && (Arrays.asList(new int[]{Calendar.SATURDAY,Calendar.SUNDAY,Calendar.FRIDAY}).indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) > 0)
			if(nombreJoursDeCongesParDate == 1 && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY 
					&& (Arrays.asList(new int[]{Calendar.SATURDAY,Calendar.SUNDAY,Calendar.FRIDAY}).indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) < 0)){
				ignoreSaturday = true;
			}
			// This code handle all non working days
			if(ignoreSaturday || nombreJoursDeCongesParDate > 0 || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || (!saturdayIsAWorkingDay && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
				if(nombreJoursDeCongesParDate > 0 ){
					nombreJoursDeCongesParDate--;
					totalJoursFerieInPeriod++;
				}
				if(isStartDayAWorkingDay && new DateValidator().isDateEqualsDate(calendar.getTime(), startDate)){
					isStartDayAWorkingDay = false;
				}
				if(isEndDayAWorkingDay && new DateValidator().isDateEqualsDate(calendar.getTime(), endDate)){
					isEndDayAWorkingDay = false;
				}
			} 
			// This code is for the all working days 
			else {
				if(nombreJoursDeCongesParDate <= 0){					
					nbrWorkingDays++;
				}
				float numberDaysHolder = 0;
				if(nbrDaysInYears.get((float)calendar.get(Calendar.YEAR)) != null){
					numberDaysHolder = nbrDaysInYears.get((float)calendar.get(Calendar.YEAR));
				}
				nbrDaysInYears.put((float)calendar.get(Calendar.YEAR), numberDaysHolder + 1);
			}
			calendar.add(Calendar.DATE, 1);
		}
		if(endCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && nombreJoursDeCongesParDate <= 0){
			nbrWorkingDays++;
		}
		result.put("nbrWorkingDays", nbrWorkingDays);
		result.put("isStartDayAWorkingDay", isStartDayAWorkingDay);
		result.put("isEndDayAWorkingDay", isEndDayAWorkingDay);
		result.put("nbrDaysInYears", nbrDaysInYears);
		result.put("totalJoursFerieInPeriod", totalJoursFerieInPeriod);
		return result;
	}
	
	public float getNombreJoursFeriesParDate(Date date,float nombreJoursDeCongesParDatePasse , Collection<IStorageResource> joursFeries)
    {
          try
          {
                float nJoursFeries = 0;
                if(joursFeries.size() > 0){
                	SimpleDateFormat jourVariableDateFormat = new SimpleDateFormat("MM-dd");
            		SimpleDateFormat jourFixeDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            		SimpleDateFormat format = jourVariableDateFormat;
                	for (IStorageResource jourFerie : joursFeries) {
                		format = jourVariableDateFormat;
                		if(jourFerie.getValue("DateDebutJourFerie") != null){
                			if(jourFerie.getValue("FeteReligieuse") != null &&(boolean)jourFerie.getValue("FeteReligieuse")){
                				format = jourFixeDateFormat;
                			}
                			if(format.format((Date)jourFerie.getValue("DateDebutJourFerie")).equals(format.format(date))) {
                				if(jourFerie.getValue("NJoursFeries") != null){
                					float nJoursFeriesHolder = (Float)jourFerie.getValue("NJoursFeries");
                					if(nJoursFeries < nJoursFeriesHolder){
                						nJoursFeries = nJoursFeriesHolder;
                					}
                				}
                			}
                		}
    				}
                }
                return nombreJoursDeCongesParDatePasse > nJoursFeries ? nombreJoursDeCongesParDatePasse : nJoursFeries;
          }
          catch (Exception e)
          {
                e.printStackTrace();
          }
          return 0;
    }
	
	public Collection<IStorageResource> getJoursFeries(IStorageResource societe){

		try {
			IContext context = workflowModule.getSysadminContext();
	        IViewController controller = workflowModule.getViewController(context, IResource.class);
	        ICatalog catalog = workflowModule.getCatalog(context, "Referentiels", ICatalog.IType.STORAGE);
	        IResourceDefinition definition = workflowModule.getResourceDefinition(context, catalog, "JoursFeries");
			controller.addEqualsConstraint("Societe",societe);
			Collection<IStorageResource>  joursFeries = controller.evaluate(definition);
			if(joursFeries==null){
				return Collections.emptyList();
			}else{
				return joursFeries;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}
	
	public HashMap<String, Object> calculateV2(IStorageResource societe,Date startDate, Date endDate){
		
		SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
		try {
			startDate = simpleFormat.parse(simpleFormat.format(startDate));
			endDate = simpleFormat.parse(simpleFormat.format(endDate));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		
		Collection<IStorageResource> joursFeries = getJoursFeries(societe);
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		HashMap<Float, Float> nbrDaysInYears = new HashMap<Float, Float>();
		
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(startDate);
		
		Calendar endCalendar = new GregorianCalendar();
		endCalendar.setTime(endDate);
		
		boolean isStartDayAWorkingDay = true;
		boolean isEndDayAWorkingDay = true;
		
		if(joursOuvrables.indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) == -1){
			endCalendar.set(Calendar.DAY_OF_WEEK, joursOuvrables.get(joursOuvrables.size()-1));
			isEndDayAWorkingDay = false;
		} else if(joursOuvres.indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) == -1 || joursOuvres.indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) ==  joursOuvres.size()-1){
			int nbrJoursToAdd= Math.abs(endCalendar.get(Calendar.DAY_OF_WEEK) - joursOuvrables.get(joursOuvrables.size()-1));
			endCalendar.add(Calendar.DAY_OF_WEEK, nbrJoursToAdd);
		}
		
		endCalendar.add(Calendar.DAY_OF_WEEK, 1);

		
		float totalJoursFerieInPeriod = 0;
		
		float nbrJoursDemande = 0;
		float nbrJoursNonOuvrable = 0;
		float nbrJourFerieStillToCount = 0;
		
		boolean lastDayIsAJourFerie = false;
				
		while (calendar.before(endCalendar)) {
			
			// the method "getNombreJoursFeriesParDate" is the on
			nbrJourFerieStillToCount = getNombreJoursFeriesParDate(calendar.getTime(), nbrJourFerieStillToCount, joursFeries);
			
			if(nbrJourFerieStillToCount > 0 || joursOuvrables.indexOf(calendar.get(Calendar.DAY_OF_WEEK)) < 0){
				lastDayIsAJourFerie = true;
				if(nbrJourFerieStillToCount > 0){
					nbrJourFerieStillToCount--;
					totalJoursFerieInPeriod++;
				} else if(joursOuvrables.indexOf(calendar.get(Calendar.DAY_OF_WEEK)) < 0){
					nbrJoursNonOuvrable++;
				}
				
				// Check if first and last day are working days in order to to make tranche debut or tranche fin non editable
				if(/*calendar.getTimeInMillis() == startDate.getTime()*/ new DateValidator().isDateEqualsDate(calendar.getTime(), startDate)){
					isStartDayAWorkingDay = false;
				}
				if(/*calendar.getTimeInMillis() == endDate.getTime() */new DateValidator().isDateEqualsDate(calendar.getTime(), endDate)){
					isEndDayAWorkingDay = false;
				}
			} else {
				
				if(lastDayIsAJourFerie && joursOuvres.indexOf(calendar.get(Calendar.DAY_OF_WEEK)) == -1){
					calendar.add(Calendar.DATE, 1);
					continue;
				}else{					
					lastDayIsAJourFerie = false;
				}
				
				// Add a day to the number of requested days because if it fails the first if where it checks for non working days and holidays
				nbrJoursDemande++;
				
				// This code fill a hashmap where we can find number of days taken by year 
				float numberDaysHolder = 0;
				if(nbrDaysInYears.get((float)calendar.get(Calendar.YEAR)) != null){
					numberDaysHolder = nbrDaysInYears.get((float)calendar.get(Calendar.YEAR));
				}
				nbrDaysInYears.put((float)calendar.get(Calendar.YEAR), numberDaysHolder + 1);
			}
			
			calendar.add(Calendar.DATE, 1);
		}
		
		result.put("nbrJoursDemande", nbrJoursDemande);
		result.put("totalJoursFerieInPeriod", totalJoursFerieInPeriod);
		result.put("totalJoursAbsence", nbrJoursDemande + totalJoursFerieInPeriod);
		result.put("nbrJoursNonOuvrable", nbrJoursNonOuvrable);
		result.put("isStartDayAWorkingDay", isStartDayAWorkingDay);
		result.put("isEndDayAWorkingDay", isEndDayAWorkingDay);
		result.put("nbrDaysInYears", nbrDaysInYears);
		return result;
	}
	
}

