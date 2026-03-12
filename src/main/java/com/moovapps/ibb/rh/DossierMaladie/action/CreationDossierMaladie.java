package com.moovapps.ibb.rh.DossierMaladie.action;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.impl.ProcessLinkedResource;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.IOptionList.IOption;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class CreationDossierMaladie extends BaseDocumentExtension {
	
	IWorkflowInstance document = null;
	
	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		if(document.getValue("siJeSuisLeDemadeur") == null){
			document.setValue("siJeSuisLeDemadeur", true);
			onJeSuisLeDemandeurChange();
		}
		IUser demandeur = null;
		if(document.getValue("Demandeur") != null){
			demandeur = (IUser)document.getValue("Demandeur");
		}
		if(demandeur != null){			
			if(document.getValue("LienDeParente") != null && document.getValue("LienDeParente").equals("Lui / elle même")){
				ArrayList<IOption> options = new ArrayList<IOption>();
				options.add(getWorkflowModule().createListOption(demandeur.getFullName(), demandeur.getFullName()));
				document.setList("NomDuMalade", options);
				document.setValue("NomDuMalade", demandeur.getFullName());
			}
		}else {
			document.setList("NomDuMalade", new ArrayList<IOption>());
			document.setValue("NomDuMalade", null);
		}
		return super.onAfterLoad();
	}
	
	public void onPropertyChanged(IProperty property) {
		if (property.getName().equals("siJeSuisLeDemadeur")) {
			onJeSuisLeDemandeurChange();
		}else if(property.getName().equals("Demandeur")){
			onDemandeurChange();
		} else if(property.getName().equals("MontantsSoins")){
			Collection<ProcessLinkedResource> MontansSoins = Collections.emptyList();
			if(document.getValue("MontantsSoins") != null){
				MontansSoins = (Collection<ProcessLinkedResource>)document.getValue("MontantsSoins");
			}
			float montantSoins = 0;
			for (ProcessLinkedResource montantSoin : MontansSoins) {
				if(montantSoin.getValue("MontantSoin") != null){					
					montantSoins += ((Number)montantSoin.getValue("MontantSoin")).floatValue();
				}
			}
			document.setValue("MontantDesSoins",montantSoins);
		} else if(property.getName().equals("LienDeParente")){
			onLienParenteChange();
		}else if(property.getName().equals("DemandeurDonnee")){
			IStorageResource demandeurDonnee = (IStorageResource) getWorkflowInstance().getValue("DemandeurDonnee");
			IUser demandeur = demandeurDonnee!=null?(IUser) demandeurDonnee.getValue("Salarie"):null;
			getWorkflowInstance().setValue("Demandeur",demandeur);
			onDemandeurChange();
		}
		super.onPropertyChanged(property);
	}
	
	private void onLienParenteChange() {
		document.setValue("NomDuMalade", null);
		document.setList("NomDuMalade", new ArrayList<IOption>());
		IUser demandeur = null;
		Collection<IStorageResource> membresFamille = null;
		if(document.getValue("Demandeur") != null){
			demandeur = (IUser)document.getValue("Demandeur");
		}
		if(demandeur != null){
			if(document.getValue("LienDeParente") != null){
				if(document.getValue("LienDeParente").equals("Lui / elle même")){
					ArrayList<IOption> options = new ArrayList<IOption>();
					options.add(getWorkflowModule().createListOption(demandeur.getFullName(), demandeur.getFullName()));
					document.setList("NomDuMalade", options);
					document.setValue("NomDuMalade", demandeur.getFullName());
				} else { 
					membresFamille = getMembresFamilleParDemandeur(demandeur);
					ArrayList<IOption> options = new ArrayList<IOption>();
					if (!membresFamille.isEmpty()) {
						for (IStorageResource membre : membresFamille) {
							options.add(getWorkflowModule().createListOption(membre.getValue("NomPrenom"), (String)membre.getValue("NomPrenom")));
						}
					}
					document.setList("NomDuMalade", options);
				}
			}
		}
	}
	
	public Collection<IStorageResource> getMembresFamilleParDemandeur(IUser demandeur) {
		Collection<IStorageResource> membresFamille = null;
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
			IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", getWorkflowInstance().getCatalog().getProject().getOrganization());
			ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "LienParente");
			controller.addEqualsConstraint("Salarie", demandeur);
			controller.addEqualsConstraint("LienParente", document.getValue("LienDeParente"));
			membresFamille = controller.evaluate(definition);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return membresFamille;
	}
	
	public void onJeSuisLeDemandeurChange() {
		boolean siJeSuisLeDemandeur = (boolean) document.getValue("siJeSuisLeDemadeur");
		ArrayList<IOption> options = new ArrayList<IOption>();
		if (siJeSuisLeDemandeur) {
			options.add(getWorkflowModule().createListOption("Lui / elle même", "Moi même"));
			document.setValue("Demandeur", getDocument().getUser());
		} else {
			options.add(getWorkflowModule().createListOption("Lui / elle même", "Lui / elle même"));
			document.setValue("Demandeur", null);
			document.setValue("Societe", null);
		}
		options.add(getWorkflowModule().createListOption("Enfant", "Enfant"));
		options.add(getWorkflowModule().createListOption("Conjoint", "Conjoint"));

		//document.setList("LienDeParente", options);
		
		onDemandeurChange();
	}
	
	public void onDemandeurChange() {
		IUser demandeur = (IUser)document.getValue("Demandeur");
		if (demandeur != null && demandeur.getId().equals(getDocument().getUser().getId())) {
			document.setValue("siJeSuisLeDemadeur", true);		
			ArrayList<IOption> options = new ArrayList<IOption>();
			options.add(getWorkflowModule().createListOption("Lui / elle même", "Moi même"));
			options.add(getWorkflowModule().createListOption("Enfant", "Enfant"));
			options.add(getWorkflowModule().createListOption("Conjoint", "Conjoint"));
			document.setList("LienDeParente", options);
		}
		if(demandeur != null){
		document.setValue("SuperieurHierarchique", demandeur != null ? demandeur.getHierarchicalManager():null);
		document.setValue("Societe", demandeur.getExtendedAttributes().getValue("Societe"));
		IStorageResource societe = (IStorageResource) demandeur.getExtendedAttributes().getValue("Societe");
		document.setValue("ResponsableRH",societe != null ? societe.getValue("ResponsableRH") : null);
		
		onLienParenteChange();
		}
	}
	

	
}
