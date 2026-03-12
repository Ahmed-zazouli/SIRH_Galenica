package com.moovapps.capone.rh.GestionDeSortie;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.capone.rh.cummonHelpers.DateValidator;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public class ValidationRetour extends BaseDocumentExtension {
	
	IWorkflowInstance document = null;
	
	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		return super.onAfterLoad();
	}

	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("approuverRetour")){
			setReferentielRetardataire();
		}
		return super.onBeforeSubmit(action);
	}
	
	private void setReferentielRetardataire() {

		SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM");
		SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");
		IUser demandeur = null;
		if (document.getValue("Demandeur") != null) {
			demandeur = (IUser) document.getValue("Demandeur");
		}
		if (demandeur == null) {
			return;
		}
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
			ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", 4);

			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "SortiesDemandeurs");
			controller.addEqualsConstraint("Demandeur", demandeur);
			controller.addEqualsConstraint("AnneeSorties", yearDateFormat.format((Date) document.getValue("dateSortie")));
			controller.addEqualsConstraint("MoisSorties", monthDateFormat.format((Date) document.getValue("dateSortie")));
			Collection<IStorageResource> retardataireRetards = controller.evaluate(definition);
			if (retardataireRetards.size() > 0) {
				for (IStorageResource retard : retardataireRetards) {
					retard.setValue("NbSorties", Float.valueOf(((Float) retard.getValue("NbSorties")).floatValue() + 1));
					retard.setValue("DureeSortiesEnMinutes", ((Number) retard.getValue("DureeSortiesEnMinutes")).floatValue() + ((Number) document.getValue("dureeSortieMinutesReelle")).floatValue());
					retard.setValue("DureeSorties", new DateValidator().durationToStringIncludingDays(((Number) retard.getValue("DureeSortiesEnMinutes")).longValue()));
					retard.setValue("SuperieurHierarchique", demandeur.getHierarchicalManager());
					retard.setValue("Societe", demandeur.getExtendedAttributes().getValue("Societe"));
					retard.save(context);
				}
			} else {
				insertRetardataireRetards(context, definition, demandeur);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean insertRetardataireRetards(IContext context, IResourceDefinition definition, IUser demandeur) {
		SimpleDateFormat monthDateFormat = new SimpleDateFormat("MM");
		SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");
		SimpleDateFormat franceDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.FRANCE);
		try {
			IStorageResource storageRessource = getWorkflowModule().createStorageResource(getWorkflowModule().getSysadminContext(), definition, "");
			storageRessource.setValue("AnneeSorties", yearDateFormat.format((Date) document.getValue("dateSortie")));
			storageRessource.setValue("DureeSorties", document.getValue("dureeSortieReelle"));
			storageRessource.setValue("DureeSortiesEnMinutes", document.getValue("dureeSortieMinutesReelle"));
			storageRessource.setValue("MoisSorties", monthDateFormat.format((Date) document.getValue("dateSortie")));
			storageRessource.setValue("NbSorties", 1);
			storageRessource.setValue("SuperieurHierarchique", demandeur.getHierarchicalManager());
			storageRessource.setValue("DateSortie", franceDateFormat.format((Date) document.getValue("dateSortie")).substring(0, 1).toUpperCase() + franceDateFormat.format((Date) document.getValue("dateSortie")).substring(1).toLowerCase());
			storageRessource.setValue("Demandeur", demandeur);
			storageRessource.setValue("Societe", demandeur.getExtendedAttributes().getValue("Societe"));
			storageRessource.save(context);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
