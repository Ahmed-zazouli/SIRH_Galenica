package com.moovapps.ibb.rh.AvanceSurSalaire.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Collection;

public class TraitementDRH extends BaseDocumentExtension {

	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("Accepter")){
			if(((Number) getWorkflowInstance().getValue("Montant")).doubleValue() != ((Number) getWorkflowInstance().getValue("Somme")).doubleValue() ){
				getResourceController().alert("Merci de vérifier la somme des remboursements mensuelle");
				return false;
			}
			Collection<ILinkedResource> remboursementMensuel = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("GestionAvances");
			for (ILinkedResource remboursement : remboursementMensuel) {
				remboursement.setValue("Statut", "Validé");
				remboursement.save(getWorkflowModule().getSysadminContext());
			}
			getWorkflowInstance().save(getWorkflowModule().getLoggedOnUserContext());
			upsertAvanceParSalarieRef();
		}
		return super.onBeforeSubmit(action);
	}
	
	private void upsertAvanceParSalarieRef() {
		
		float avance = ((Number)getWorkflowInstance().getValue("Montant")).floatValue();
		
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
			ICatalog catalog = getWorkflowModule().getCatalog(context, "AvancesParSalarie", ICatalog.IType.STORAGE);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "AvanceParSalarie");
			controller.addEqualsConstraint("Salarie", getWorkflowInstance().getValue("Beneficiaire"));
			controller.addEqualsConstraint("Societe", getWorkflowInstance().getValue("Societe"));
			controller.addEqualsConstraint("Manager", getWorkflowInstance().getValue("SuperieurHierarchique"));
			controller.addEqualsConstraint("Annee", getWorkflowInstance().getValue("AnneeDemande"));
			controller.addEqualsConstraint("Mois", getWorkflowInstance().getValue("MoisDemande"));
			Collection<IStorageResource> avancesCollection = controller.evaluate(definition);
			if(avancesCollection.size() > 0){				
				IStorageResource collabAvance = avancesCollection.iterator().next();
				collabAvance.setValue("Montant", ((Number)collabAvance.getValue("Montant")).floatValue() + avance);
				collabAvance.save(context);
			} else {
				IStorageResource storageRessource = getWorkflowModule().createStorageResource(getWorkflowModule().getSysadminContext(), definition, "");
				storageRessource.setValue("Salarie", getWorkflowInstance().getValue("Beneficiaire"));
				storageRessource.setValue("Societe", getWorkflowInstance().getValue("Societe"));
				storageRessource.setValue("Manager", getWorkflowInstance().getValue("SuperieurHierarchique"));
				storageRessource.setValue("DateDemande", getWorkflowInstance().getValue("DateDemande"));
				storageRessource.setValue("Annee", getWorkflowInstance().getValue("AnneeDemande"));
				storageRessource.setValue("Mois", getWorkflowInstance().getValue("MoisDemande"));
				storageRessource.setValue("Montant", avance);
				storageRessource.save(context);
			}
			
		} catch(Exception e){
			
			e.printStackTrace();
			
		}
		
	}
	
}
