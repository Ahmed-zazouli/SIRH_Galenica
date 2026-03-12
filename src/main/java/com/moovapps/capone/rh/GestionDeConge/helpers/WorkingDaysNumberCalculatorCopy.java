package com.moovapps.capone.rh.GestionDeConge.helpers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;

import java.text.SimpleDateFormat;
import java.util.*;

public class WorkingDaysNumberCalculatorCopy {

	IWorkflowModule workflowModule = null;
	ArrayList<Integer> joursOuvrables = null;
	ArrayList<Integer> joursOuvres = null;
	//private float nbrJoursDemande;

	public WorkingDaysNumberCalculatorCopy(IWorkflowModule workflowModule, ArrayList<Integer> joursOuvrables, ArrayList<Integer> joursOuvres) {
		this.workflowModule = workflowModule;
		this.joursOuvrables = joursOuvrables;
		this.joursOuvres = joursOuvres;
	}


	public WorkingDaysNumberCalculatorCopy(IWorkflowModule workflowModule) {
		this.workflowModule = workflowModule;
	}

//	public HashMap<String, Object> calculate(Date startDate, Date endDate, boolean saturdayIsAWorkingDay){
//		HashMap<String, Object> result = new HashMap<String, Object>();
//		HashMap<Float, Float> nbrDaysInYears = new HashMap<Float, Float>();
//
//		Calendar calendar = new GregorianCalendar();
//		calendar.setTime(startDate);
//
//		Calendar endCalendar = new GregorianCalendar();
//		endCalendar.setTime(endDate);
//		endCalendar.add(Calendar.DAY_OF_WEEK, 1);
//
//		boolean isStartDayAWorkingDay = true;
//		boolean isEndDayAWorkingDay = true;
//		float nbrWorkingDays = 0;
//		float nombreJoursDeCongesParDate = 0;
//		float totalJoursFerieInPeriod = 0;
//		Collection<IStorageResource> joursFeries = getJoursFeries();
//
//		boolean ignoreSaturday = false;
//		while (calendar.before(endCalendar)) {
//			nombreJoursDeCongesParDate = getNombreJoursFeriesParDate(calendar.getTime(), nombreJoursDeCongesParDate, joursFeries);
//			// in order to not count saturday if it's in the end date remove the third part of the if statement (the one below)
//			// && (Arrays.asList(new int[]{Calendar.SATURDAY,Calendar.SUNDAY,Calendar.FRIDAY}).indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) > 0)
//			if(nombreJoursDeCongesParDate == 1 && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
//					&& (Arrays.asList(new int[]{Calendar.SATURDAY,Calendar.SUNDAY,Calendar.FRIDAY}).indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) < 0)){
//				ignoreSaturday = true;
//			}
//			// This code handle all non working days
//			if(ignoreSaturday || nombreJoursDeCongesParDate > 0 || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || (!saturdayIsAWorkingDay && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
//				if(nombreJoursDeCongesParDate > 0 ){
//					nombreJoursDeCongesParDate--;
//					totalJoursFerieInPeriod++;
//				}
//				if(isStartDayAWorkingDay && new DateValidator().isDateEqualsDate(calendar.getTime(), startDate)){
//					isStartDayAWorkingDay = false;
//				}
//				if(isEndDayAWorkingDay && new DateValidator().isDateEqualsDate(calendar.getTime(), endDate)){
//					isEndDayAWorkingDay = false;
//				}
//			}
//			// This code is for the all working days
//			else {
//				if(nombreJoursDeCongesParDate <= 0){
//					nbrWorkingDays++;
//				}
//				float numberDaysHolder = 0;
//				if(nbrDaysInYears.get((Float)calendar.get(Calendar.YEAR)) != null){
//					numberDaysHolder = nbrDaysInYears.get((Float)calendar.get(Calendar.YEAR));
//				}
//				nbrDaysInYears.put((Float)calendar.get(Calendar.YEAR), numberDaysHolder + 1);
//			}
//			calendar.add(Calendar.DATE, 1);
//		}
//		if(endCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && nombreJoursDeCongesParDate <= 0){
//			nbrWorkingDays++;
//		}
//		result.put("nbrWorkingDays", nbrWorkingDays);
//		result.put("isStartDayAWorkingDay", isStartDayAWorkingDay);
//		result.put("isEndDayAWorkingDay", isEndDayAWorkingDay);
//		result.put("nbrDaysInYears", nbrDaysInYears);
//		result.put("totalJoursFerieInPeriod", totalJoursFerieInPeriod);
//		return result;
//	}
//

	public HashMap<String, Object> calculateV2(IStorageResource societe,Date startDate, Date endDate, boolean ignoreJoursOuvrable) {

		HashMap<String, Object> result = new HashMap<String, Object>();
		HashMap<Float, Float> nbrDaysInYears = new HashMap<Float, Float>();

		Calendar dateFinReelCalendar = Calendar.getInstance();
		dateFinReelCalendar.setTime(endDate);

		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(startDate);

		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(endDate);

		Calendar keepingDateEndCalendar = Calendar.getInstance();
		keepingDateEndCalendar.setTime(endDate);

		SimpleDateFormat DateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat DateFormat2 = new SimpleDateFormat("dd/MM");

		boolean isStartDayAWorkingDay = true;
		boolean isEndDayAWorkingDay = true;


		ArrayList<String> joursFeries = getJoursFeriers(societe,"Referentiels", "JoursFeries");
		float nbrJoursDemande = 0;
		float totalJoursFerieInPeriod = 0;
		float nbrJoursNonOuvrable = 0;
		String dateFormat1 = "";
		String dateFormat2 = "";

		String dateEndFormat1 = DateFormat1.format(endDate);
		String dateEndFormat2 = DateFormat2.format(endDate);

		String dateStartFormat1 = DateFormat1.format(startDate);
		String dateStartFormat2 = DateFormat2.format(startDate);
		int nbrJoursToAdd = 0;
		int nbrJoursToDelete = 0;
		if (!ignoreJoursOuvrable && !joursFeries.contains(dateEndFormat1) && !joursFeries.contains(dateEndFormat2)) {

			if (joursOuvres.indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) == joursOuvres.size() - 1) {
				nbrJoursToAdd = Math.abs(joursOuvrables.size() - joursOuvres.size());
				endCalendar.add(Calendar.DAY_OF_WEEK, nbrJoursToAdd);
				dateFinReelCalendar.add(Calendar.DAY_OF_WEEK, nbrJoursToAdd);

			} else if (!joursOuvres.contains(endCalendar.get(Calendar.DAY_OF_WEEK)) &&
					joursOuvrables.contains(endCalendar.get(Calendar.DAY_OF_WEEK))) {
				nbrJoursToAdd = joursOuvrables.size() - 1 -
						joursOuvrables.indexOf(endCalendar.get(Calendar.DAY_OF_WEEK));
				endCalendar.add(Calendar.DAY_OF_WEEK, nbrJoursToAdd);
				dateFinReelCalendar.add(Calendar.DAY_OF_WEEK, nbrJoursToAdd);
			}

			if (!joursOuvres.contains(endCalendar.get(Calendar.DAY_OF_WEEK)) &&
					joursOuvrables.contains(endCalendar.get(Calendar.DAY_OF_WEEK))) {
				Calendar EndCalendar1 = Calendar.getInstance();
				EndCalendar1.setTime(dateFinReelCalendar.getTime());
				nbrJoursToDelete = joursOuvrables.indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) - (joursOuvres.size() - 1);
				EndCalendar1.add(Calendar.DATE, -nbrJoursToDelete);
				String dateEndFormat4 = DateFormat1.format(EndCalendar1.getTime());
				String dateEndFormat5 = DateFormat2.format(EndCalendar1.getTime());
				if (!joursFeries.contains(dateEndFormat4) && !joursFeries.contains(dateEndFormat5)) {
					nbrJoursToDelete = 0;
				}

			}
		}

		if (!joursOuvrables.contains(endCalendar.get(Calendar.DAY_OF_WEEK))) {
			Calendar EndCalendar1 = Calendar.getInstance();
			EndCalendar1.setTime(endDate);
			nbrJoursToDelete += 7 - joursOuvres.size();
			EndCalendar1.add(Calendar.DATE, -nbrJoursToDelete);
			String dateEndFormat4 = DateFormat1.format(EndCalendar1.getTime());
			String dateEndFormat5 = DateFormat2.format(EndCalendar1.getTime());
			if (!joursFeries.contains(dateEndFormat4) && !joursFeries.contains(dateEndFormat5)) {
				nbrJoursToDelete = 0;
			} else {
				nbrJoursToDelete = joursOuvrables.size() - joursOuvres.size();
			}
		} else if (joursFeries.contains(dateEndFormat1) || joursFeries.contains(dateEndFormat2)) {
			//joursOuvres.indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) == 0
			Calendar EndCalendar1 = Calendar.getInstance();
			EndCalendar1.setTime(endDate);
			String dateEndFormat4 = DateFormat1.format(EndCalendar1.getTime());
			String dateEndFormat5 = DateFormat2.format(EndCalendar1.getTime());
			boolean ifJoursFeries = false;
			while (joursOuvrables.contains(EndCalendar1.get(Calendar.DAY_OF_WEEK))) {
				dateEndFormat4 = DateFormat1.format(EndCalendar1.getTime());
				dateEndFormat5 = DateFormat2.format(EndCalendar1.getTime());
				if (joursFeries.contains(dateEndFormat4) || joursFeries.contains(dateEndFormat5)) {
					ifJoursFeries = true;
				}
				EndCalendar1.add(Calendar.DATE, -1);
			}
			if (ifJoursFeries) {
				nbrJoursToDelete += 7 - joursOuvres.size();
				EndCalendar1.add(Calendar.DATE, -nbrJoursToDelete);
				dateEndFormat4 = DateFormat1.format(EndCalendar1.getTime());
				dateEndFormat5 = DateFormat2.format(EndCalendar1.getTime());
				if (!joursFeries.contains(dateEndFormat4) && !joursFeries.contains(dateEndFormat5)) {
					nbrJoursToDelete = 0;
				} else {
					nbrJoursToDelete = joursOuvrables.size() - joursOuvres.size();
				}
			}


		}


		if (!joursOuvres.contains(endCalendar.get(Calendar.DAY_OF_WEEK))
				|| joursFeries.contains(dateEndFormat1) || joursFeries.contains(dateEndFormat2)) {
			isEndDayAWorkingDay = false;
		}
		if (!joursOuvres.contains(startCalendar.get(Calendar.DAY_OF_WEEK))
				|| joursFeries.contains(dateStartFormat1) || joursFeries.contains(dateStartFormat2)) {
			isStartDayAWorkingDay = false;
		}

		endCalendar.add(Calendar.DATE, 1);

		while (startCalendar.before(endCalendar)) {
			nbrJoursDemande++;
			dateFormat1 = DateFormat1.format(startCalendar.getTime());
			dateFormat2 = DateFormat2.format(startCalendar.getTime());

			if (joursFeries != null && !joursFeries.isEmpty() && (joursFeries.contains(dateFormat1) || joursFeries.contains(dateFormat2))) {
				nbrJoursDemande--;
				if (keepingDateEndCalendar.before(startCalendar)) {
					break;
				}
				totalJoursFerieInPeriod++;
			} else if (!joursOuvrables.contains(startCalendar.get(Calendar.DAY_OF_WEEK))) {
				nbrJoursDemande--;
				nbrJoursNonOuvrable++;

			}
			// This code fill a hashmap where we can find number of days taken by year
			float numberDaysHolder = 0;
			if (nbrDaysInYears.get((float) startCalendar.get(Calendar.YEAR)) != null) {
				numberDaysHolder = nbrDaysInYears.get((float) startCalendar.get(Calendar.YEAR));
			}
			nbrDaysInYears.put((float) startCalendar.get(Calendar.YEAR), numberDaysHolder + 1);

			startCalendar.add(Calendar.DATE, 1);
		}
		nbrJoursDemande = nbrJoursDemande - nbrJoursToDelete;

		result.put("nbrJoursDemande", nbrJoursDemande);
		result.put("totalJoursFerieInPeriod", totalJoursFerieInPeriod);
		result.put("totalJoursAbsence", nbrJoursDemande + totalJoursFerieInPeriod + nbrJoursToDelete);
		result.put("nbrJoursNonOuvrable", nbrJoursNonOuvrable);
		result.put("isStartDayAWorkingDay", isStartDayAWorkingDay);
		result.put("isEndDayAWorkingDay", isEndDayAWorkingDay);
		result.put("nbrDaysInYears", nbrDaysInYears);
		result.put("nbrJoursToAdd", nbrJoursToAdd);
		result.put("dateFinReel", dateFinReelCalendar.getTime());

		return result;
	}

	public ArrayList<String> getJoursFeriers(IStorageResource societe,String catalogName, String name) {
		try {
			IContext context = Modules.getDirectoryModule().getSysadminContext();
			IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
			IProject project = Modules.getProjectModule().getProject(context, "Capone", organization);
			ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, catalogName, 4, project);
			IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
			IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, name);

			controller.addEqualsConstraint("Societe",societe);

			Collection<IStorageResource> storageResources = controller.evaluate(definition);
			ArrayList<String> jourFeriers = new ArrayList<>();
			SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat DateFor2 = new SimpleDateFormat("dd/MM");
			if(storageResources==null)return new ArrayList<>();
			for (IStorageResource resouce : storageResources) {
				Date d = (Date) resouce.getValue("DateDebutJourFerie");
				String d1;
				float NomberDesJoures = (Float) resouce.getValue("NJoursFeries");
				boolean FeteReligieuse = true;
				if (resouce.getValue("FeteReligieuse") == null ||
						!((Boolean) resouce.getValue("FeteReligieuse"))) {
					FeteReligieuse = false;
				}
				for (int i = 0; i < NomberDesJoures; i++) {

					if (FeteReligieuse) {
						d1 = DateFor.format(d);

						jourFeriers.add(d1);
					} else {
						d1 = DateFor2.format(d);
						jourFeriers.add(d1);
					}
					d = new Date(d.getTime() +  (1000 * 60 * 60 * 24));
				}

			}

			return jourFeriers;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public HashMap<String, Object> calculateDateFinCongeSpeciaux(IStorageResource societe,Date startDate, Date endDate) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		HashMap<Float, Float> nbrDaysInYears = new HashMap<Float, Float>();

		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(startDate);

		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(endDate);
		SimpleDateFormat DateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat DateFormat2 = new SimpleDateFormat("dd/MM");


		ArrayList<String> joursFeries = getJoursFeriers(societe,"Referentiels", "JoursFeries");
		float nbrJoursDemande = 0;
		float totalJoursFerieInPeriod = 0;
		float nbrJoursNonOuvrable = 0;
		String dateFormat1 = "";
		String dateFormat2 = "";


		endCalendar.add(Calendar.DATE, 1);

		while (startCalendar.before(endCalendar)) {
			nbrJoursDemande++;
			dateFormat1 = DateFormat1.format(startCalendar.getTime());
			dateFormat2 = DateFormat2.format(startCalendar.getTime());

			if (!joursOuvrables.contains(startCalendar.get(Calendar.DAY_OF_WEEK))) {
				nbrJoursDemande--;
				nbrJoursNonOuvrable++;
				endCalendar.add(Calendar.DATE, 1);
			}

			// Getting the Joures férié
			if (joursFeries != null && !joursFeries.isEmpty()) {
				if (joursFeries.contains(dateFormat1) || joursFeries.contains(dateFormat2)) {
					nbrJoursDemande--;
					totalJoursFerieInPeriod++;
					endCalendar.add(Calendar.DATE, 1);
				}

			}
			// This code fill a hashmap where we can find number of days taken by year
			float numberDaysHolder = 0;
			if (nbrDaysInYears.get((float) startCalendar.get(Calendar.YEAR)) != null) {
				numberDaysHolder = nbrDaysInYears.get((float) startCalendar.get(Calendar.YEAR));
			}
			nbrDaysInYears.put((float) startCalendar.get(Calendar.YEAR), numberDaysHolder + 1);

			startCalendar.add(Calendar.DATE, 1);
		}
		endCalendar.add(Calendar.DATE, -1);

		result.put("nbrJoursDemande", nbrJoursDemande);
		result.put("totalJoursFerieInPeriod", totalJoursFerieInPeriod);
		result.put("totalJoursAbsence", nbrJoursDemande + totalJoursFerieInPeriod);
		result.put("nbrJoursNonOuvrable", nbrJoursNonOuvrable);
		result.put("DateFinCongeSpeciaux", endCalendar.getTime());
		result.put("nbrDaysInYears", nbrDaysInYears);

		return result;
	}
}
