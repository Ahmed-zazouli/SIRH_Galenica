package com.moovapps.capone.rh.GestionDeRetard;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.capone.rh.cummonHelpers.DateValidator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public class DeclarationFinRetard extends BaseDocumentExtension {
	
	public IWorkflowInstance document = null;
	/*public String dateField = "dateDuRetard";
	public String startTimeField = "dateEtHeureDebutPrevisionnelle";
	public String declarationFinField = "finRetard";
	public String commentaireValidationFinField = "commentaireValidationFinRetard";
	public String actualEndTimeField = "dateEtHeureFinReelle";
	public String actualDurationInMinutesField = "dureeRetardReelleEnMinutes";
	public String actualDurationField = "dureeRetardReelle";
	public String actualEndHourField = "heureFinReelle";
	public String actualEndMinuteField = "MinutesFinReelle";*/
	
	Integer dureeautorise = 0;
	
	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		dureeautorise = ((Number)document.getValue("DureeAutoriseeCloture")).intValue();
		return super.onAfterLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property) {
		if(property.getName().equals("dateEtHeureFinReelle")){
			onActualEndTimeChange();
		} else if(property.getName().equals("heureFinReelle") || property.getName().equals("MinutesFinReelle")){
			setDatesFields();
			onActualEndTimeChange();
		}
		super.onPropertyChanged(property);
	}
	
	public void setDatesFields(){
		Calendar calendar = Calendar.getInstance();
		Date date = (Date)document.getValue("dateDuRetard");
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt((String)document.getValue("heureFinReelle")));
		calendar.set(Calendar.MINUTE, Integer.parseInt((String)document.getValue("MinutesFinReelle")));
		document.setValue("dateEtHeureFinReelle", calendar.getTime());    
	}
	
	public void onActualEndTimeChange(){
		Date startTime = (Date)document.getValue("dateEtHeureDebutPrevisionnelle");
		Date endTime = null;
		if(document.getValue("dateEtHeureFinReelle") != null){
			endTime = (Date)document.getValue("dateEtHeureFinReelle");
			calculateDuration(startTime, endTime);
		}
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("finRetard")){
			Date startTime = (Date)document.getValue("dateEtHeureDebutPrevisionnelle");
			Date endTime = (Date)document.getValue("dateEtHeureFinReelle");
			if(!validateDates(startTime, endTime)){
				return false;
			} else {
				calculateDuration(startTime, endTime);
			}
			document.setValue("commentaireValidationFinRetard", "");
			document.save(getWorkflowModule().getSysadminContext());
			setReferentielRetardataire();
		}
		return superOnBeforeSubmit(action);
	}
	
	private void setReferentielRetardataire() {
		
		SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM");
		SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");
		IUser retardataire =null;
		if(document.getValue("retardataire") != null){
			retardataire = (IUser)document.getValue("retardataire");
		}
		if(retardataire == null){
			return;
		}
		try {
			IContext context = getWorkflowModule().getSysadminContext();
	        IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
	        ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", 4);
	        
	        IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Retardataires");
	        controller.addEqualsConstraint("Retardataire", retardataire);
	        controller.addEqualsConstraint("AnneeRetard", yearDateFormat.format((Date)document.getValue("dateDuRetard")));
	        controller.addEqualsConstraint("MoisRetard", monthDateFormat.format((Date)document.getValue("dateDuRetard")));
	        Collection<IStorageResource> retardataireRetards = controller.evaluate(definition);
	        if(retardataireRetards.size() > 0){
	        	for (IStorageResource retard : retardataireRetards) {
					retard.setValue("NbRetards", Float.valueOf(((Float)retard.getValue("NbRetards")).floatValue() + 1));
					retard.setValue("DureeRetardsEnMinutes", ((Number)retard.getValue("DureeRetardsEnMinutes")).floatValue() + ((Number)document.getValue("dureeRetardReelleEnMinutes")).floatValue());
					retard.setValue("DureeRetards", new DateValidator().durationToStringIncludingDays(((Number)retard.getValue("DureeRetardsEnMinutes")).longValue()));
					retard.setValue("SuperieurHierarchique", retardataire.getHierarchicalManager());
					retard.setValue("Societe", retardataire.getExtendedAttributes().getValue("Societe"));
					retard.save(context);
				}
	        }else{
	        	insertRetardataireRetards(context,definition,retardataire);
	        }
	      } catch (Exception e) {
	        e.printStackTrace();
	      } 
	}
	
	private boolean insertRetardataireRetards(IContext context, IResourceDefinition definition, IUser retardataire) {
		SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM");
		SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");
		SimpleDateFormat franceDateFormat = new SimpleDateFormat("MMMM yyyy",Locale.FRANCE);
	    try {
	        IStorageResource storageRessource = getWorkflowModule().createStorageResource(getWorkflowModule().getSysadminContext(), definition, "");
	        storageRessource.setValue("AnneeRetard", yearDateFormat.format((Date)document.getValue("dateDuRetard")));
	        storageRessource.setValue("DureeRetards", document.getValue("dureeRetardReelle"));
	        storageRessource.setValue("DureeRetardsEnMinutes", document.getValue("dureeRetardReelleEnMinutes"));
	        storageRessource.setValue("MoisRetard", monthDateFormat.format((Date)document.getValue("dateDuRetard")));
	        storageRessource.setValue("NbRetards", 1);
	        storageRessource.setValue("SuperieurHierarchique", retardataire.getHierarchicalManager());
	        storageRessource.setValue("DateRetard", franceDateFormat.format((Date)document.getValue("dateDuRetard")).substring(0,1).toUpperCase() + franceDateFormat.format((Date)document.getValue("dateDuRetard")).substring(1).toLowerCase() );
	        storageRessource.setValue("Retardataire", retardataire);
	        storageRessource.setValue("Societe", retardataire.getExtendedAttributes().getValue("Societe"));
	        storageRessource.save(context);
	        return true;
	      } catch (Exception e) {
	    	  e.printStackTrace();
	        return false;
	      } 
	    }
	
	public boolean superOnBeforeSubmit(IAction action){
		return super.onBeforeSubmit(action);
	}
	
	public void calculateDuration(Date startTime, Date endTime){
		if(validateDates(startTime, endTime)){
			long retardDurationInMinutes = new DateValidator().getDurationBetweenTwoDatesInMin(startTime, endTime);
			if(retardDurationInMinutes > dureeautorise){
				getResourceController().alert("Vous avez dépasser la durée autorisée");
				document.setValue("dateEtHeureFinReelle", null);
				document.setValue("dureeRetardReelleEnMinutes", null);
				document.setValue("dureeRetardReelle", null);
				return;
			}
			document.setValue("dureeRetardReelleEnMinutes", retardDurationInMinutes);
			document.setValue("dureeRetardReelle", new DateValidator().durationToString(retardDurationInMinutes));
		} else {
			document.setValue("dateEtHeureFinReelle", null);
			document.setValue("dureeRetardReelleEnMinutes", null);
			document.setValue("dureeRetardReelle", null);
		}
	}
	
	public boolean validateDates(Date startTime, Date endTime){
		if(endTime == null){
			getResourceController().alert("L'heure de retour doit être rensiegnée");
			return false;
		}
		if(endTime.before(startTime)){
			getResourceController().alert("Le temps de retour doit être aprés le temps de départ");
			return false;
		}
		return true;
	}
	
}
