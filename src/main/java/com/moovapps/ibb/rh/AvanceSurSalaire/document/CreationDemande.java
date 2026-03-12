package com.moovapps.ibb.rh.AvanceSurSalaire.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Controllers.Helpers.DateHelper;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Integer.parseInt;

public class CreationDemande extends BaseDocumentExtension {
	IWorkflowInstance document = null;
	float pourcentageMax = 40;
	double avanceMax = 3000;

	boolean activateLimit = true;

	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		document.setValue("siJeSuisLeDemandeur", true);
		onJeSuisLeDemandeurChange();
		SimpleDateFormat franceDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.FRANCE);
		document.setValue("DateDemande",franceDateFormat.format((Date) document.getValue("sys_CreationDate")).substring(0, 1).toUpperCase() + franceDateFormat.format((Date) document.getValue("sys_CreationDate")).substring(1).toLowerCase());
		if(document.getValue("Beneficiaire") != null){

		}
		/*if (isPastPlafondDelai()) {
			getResourceController().alert("Plafond de délai avance sur salaire est passé ");
		}*/
		return super.onAfterLoad();
	}

	@Override
	public boolean onBeforeSubmit(IStorageKey actionKey) {


		if (isPastPlafondDelai()) {
			getResourceController().alert("Plafond de délai avance sur salaire est passé ");
			return false;
		}

		IUser validateur = (IUser) getWorkflowInstance().getValue("SuperieurHierarchique");
		boolean siJeSuisLeDemandeur = (boolean) getWorkflowInstance().getValue("siJeSuisLeDemandeur");
		if(validateur==null && siJeSuisLeDemandeur){
			getResourceController().alert("Vous devez avoir un responsable hiérarchique");
			return false;
		}
		return super.onBeforeSubmit(actionKey);
	}

	public void onPropertyChanged(IProperty property) {
		if (property.getName().equals("siJeSuisLeDemandeur")) {
			onJeSuisLeDemandeurChange();
			remplirTabGestionAvance();
		}else if (property.getName().equals("Beneficiaire")) {
			onDemandeurChange();
			calculatePorcentageDemande();
			remplirTabGestionAvance();
		}else if (property.getName().equals("Societe")) {
			getBaseRemeboursement();
			remplirTabGestionAvance();
		}else if(property.getName().equals("Montant")){
			calculatePorcentageDemande();
			remplirTabGestionAvance();
		}else if(property.getName().equals("NombreDeMensualite")){
			remplirTabGestionAvance();
		}else if(property.getName().equals("DemandeurDonnee")){
			IStorageResource demandeurDonnee = (IStorageResource) getWorkflowInstance().getValue("DemandeurDonnee");
			IUser demandeur = demandeurDonnee!=null?(IUser) demandeurDonnee.getValue("Salarie"):null;
			getWorkflowInstance().setValue("Beneficiaire",demandeur);
			onDemandeurChange();
			calculatePorcentageDemande();
			remplirTabGestionAvance();
		}
		super.onPropertyChanged(property);
	}
	private boolean isPastPlafondDelai(){
		IUser user=getWorkflowModule().getLoggedOnUser();
		IStorageResource societe = (IStorageResource) user.getExtendedAttributes().getValue("Societe");;
		float plafondDelai = ((Number) societe.getValue("PlafondDeDelaiAvanceSurSalaire")).floatValue();
		Calendar currentDate = Calendar.getInstance();
		currentDate.setTime(new Date());
		int dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH);


		return dayOfMonth > plafondDelai ;
	}

	private void getBaseRemeboursement() {
		IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
		if(societe!=null){
			if(societe.getValue("BaseDeRemboursement") != null){
				getWorkflowInstance().setValue("BaseDeRemboursement",societe.getValue("BaseDeRemboursement"));
			}
		}

	}

	private void remplirTabGestionAvance() {
		getWorkflowInstance().deleteLinkedResources(getWorkflowInstance().getLinkedResources("GestionAvances"));
		if(getWorkflowInstance().getValue("Montant") != null && getWorkflowInstance().getValue("NombreDeMensualite") != null){
			if((Float)getWorkflowInstance().getValue("Montant") < 1){
				getWorkflowInstance().deleteLinkedResources(getWorkflowInstance().getLinkedResources("GestionAvances"));
			}else{
				Float montant = (Float) getWorkflowInstance().getValue("Montant");
				Number nbrMois = parseInt((String) getWorkflowInstance().getValue("NombreDeMensualite"));
				Double montantMois = montant / nbrMois.doubleValue() ;
				Calendar dateActuelle = Calendar.getInstance();
				dateActuelle.set(Calendar.DATE, 1);
				if(getWorkflowInstance().getValue("BaseDeRemboursement") == null ){

					for (int i = 1 ; i <= nbrMois.intValue() ; i++){
						ILinkedResource linkedResource = getWorkflowInstance().createLinkedResource("GestionAvances");
						linkedResource.setValue("Mois",i);
						dateActuelle.add(Calendar.MONTH, 1);
						Date DateRemboursement  = dateActuelle.getTime();
						linkedResource.setValue("Date",DateRemboursement);
						linkedResource.setValue("Montant",montantMois);
						linkedResource.setValue("Societe", getWorkflowInstance().getValue("Societe"));

						if(getWorkflowInstance().getValue("Beneficiaire") != null){
							linkedResource.setValue("Salarie",getWorkflowInstance().getValue("Beneficiaire"));
						}else{
							linkedResource.setValue("Salarie",getWorkflowInstance().getValue("sys_Creator"));

						}
						getWorkflowInstance().addLinkedResource(linkedResource);
					}
				}else{
					Double baseDeRemboursement = (Double) getWorkflowInstance().getValue("BaseDeRemboursement");
					Double temp1 = montantMois % baseDeRemboursement;
					Double temp2 = montantMois - temp1;
					Double tempMontant = 0.0;
					Double coefficient =  (temp2 / baseDeRemboursement);
					if(temp1 >= (baseDeRemboursement/2)){
						coefficient += 1 ;
					}
					montantMois = baseDeRemboursement * coefficient ;
					for(int i = 1 ; i <= nbrMois.intValue()  ; i++){
						ILinkedResource linkedResource = getWorkflowInstance().createLinkedResource("GestionAvances");
						linkedResource.setValue("Mois",i);
						dateActuelle.add(Calendar.MONTH, 1);
						Date DateRemboursement  = dateActuelle.getTime();
						linkedResource.setValue("Date",DateRemboursement);
						if(i != nbrMois.intValue()){
							tempMontant+= montantMois;
							linkedResource.setValue("Montant",montantMois);
						}else{
							Double montantRest = montant - tempMontant;
							linkedResource.setValue("Montant",montantRest);
						}
						if(getWorkflowInstance().getValue("Beneficiaire") != null){
							linkedResource.setValue("Salarie",getWorkflowInstance().getValue("Beneficiaire"));
						}else{
							linkedResource.setValue("Salarie",getWorkflowInstance().getValue("sys_Creator"));
						}
						getWorkflowInstance().addLinkedResource(linkedResource);
					}

				}

				getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
			}

		}
	}

	public void onJeSuisLeDemandeurChange() {
		boolean siJeSuisLeDemandeur = (boolean) document.getValue("siJeSuisLeDemandeur");
		if (siJeSuisLeDemandeur) {
			document.setValue("Beneficiaire", getDocument().getUser());
		} else {
			document.setValue("DemandeurDonnee",null);
			document.setValue("Beneficiaire", null);
		}
		onDemandeurChange();
	}
	
	public void calculatePorcentageDemande(){
		float salaire = 0;
		float avanceDemande = 0;
		if(document.getValue("DemandeurSalaire") != null && document.getValue("Montant") != null){
			salaire = ((Number)document.getValue("DemandeurSalaire")).floatValue();
			avanceDemande = ((Number)document.getValue("Montant")).floatValue();
			int pourcentageDemande = (int)(avanceDemande * 100 / salaire);
			document.setValue("PourcentageDemande", pourcentageDemande);
			document.setValue("TrancheSalaire", pourcentageDemande < 30 ? "moin 30%" : "plus 30%" );
		} else{
			document.setValue("PourcentageDemande", null);
			document.setValue("TrancheSalaire", null);
		}
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("Envoyer")){
			document.setValue("DateDemande", new DateHelper().formatDate((Date)document.getValue("sys_CreationDate"), "MM-yyyy"));
			if(document.getValue("PourcentageDemande") != null){				
				int pourcentageDemande = ((Number)document.getValue("PourcentageDemande")).intValue();
				float avanceDemande = ((Number)document.getValue("Montant")).floatValue();
				if(activateLimit){				
					if(pourcentageDemande > pourcentageMax){
						getResourceController().alert("Vous ne pouvez pas dépasser " + pourcentageMax + "% du salaire");
						return false;
					}
//					else if(avanceDemande > avanceMax){
//						getResourceController().alert("Vous ne pouvez pas dépasser "+ avanceMax +"(DH)");
//						return false;
//					}
				}
			}else{
				//getResourceController().alert("Merci de vérifier le salaire dans la fiche salairé");
			}
		}
		return super.onBeforeSubmit(action);
	}
	IStorageResource societe=null;

	public void onDemandeurChange() {
		IUser benificiere = (IUser)document.getValue("Beneficiaire");
		if (benificiere != null && benificiere.getId().equals(getDocument().getUser().getId())) {
			document.setValue("siJeSuisLeDemandeur", true);
		}
	//if(benificiere != null && benificiere.getExtendedAttributes().getValue("Societe") != null){

		document.setValue("SuperieurHierarchique", benificiere != null ? benificiere.getHierarchicalManager():null);
		document.setValue("Matricule",  benificiere != null ?  benificiere.getExtendedAttributes().getValue("Matricule") : null);
		document.setValue("Fonction",  benificiere != null ? benificiere.getExtendedAttributes().getValue("Fonction") : null);
		document.setValue("DemandeurSalaire",  benificiere != null ? benificiere.getExtendedAttributes().getValue("Salaire"): null);
		document.setValue("Societe",benificiere != null ? benificiere.getExtendedAttributes().getValue("Societe"): null );
	//	if(benificiere != null && benificiere.getExtendedAttributes().getValue("SocieteDonnee") != null){	
			societe = benificiere!=null? (IStorageResource) benificiere.getExtendedAttributes().getValue("Societe"):null;
			document.setValue("ResponsableRH",societe!=null ? societe.getValue("ResponsableRH") : null);

		//}//}
		//document.setValue("ResponsableRH",societe!=null ? societe.getValue("ResponsableRH") : null);
		calcualteTotalDemandeThisMonth();
	}

	
	private void calcualteTotalDemandeThisMonth() {
		Collection<IWorkflowInstance> ThisMonthsDemandesAvances = getAllDemandeurDemandesAvance();
		float totalDemande = 0;
		for (IWorkflowInstance demandeAvance : ThisMonthsDemandesAvances) {
			if(demandeAvance.getValue("Montant") != null){
				String ref = (String)demandeAvance.getValue("sys_Reference"); 
				totalDemande += (Float)demandeAvance.getValue("Montant");
			}
		}
		document.setValue("TotalAvanceDemandeeThisMonth", totalDemande);
	}
	
	
	private Collection<IWorkflowInstance> getAllDemandeurDemandesAvance()
    {
          Collection<IWorkflowInstance> collection = Collections.emptyList();
          IUser demandeur = null;
          if(document.getValue("Beneficiaire") != null){
        	  demandeur = (IUser)document.getValue("Beneficiaire");
          }
          if(demandeur != null){        	  
        	  try
        	  {
        		  IContext sysContext = getWorkflowModule().getSysadminContext();
        		  IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
        		  IProject project = getProjectModule().getProject(sysContext, "GestionDesAvances", organization);
        		  ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "GestionDesAvances", project);
        		  // IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "GestionDesAvances_1.0");
        		  IViewController controller = getWorkflowModule().getViewController(sysContext);
				  controller.addInConstraint("sys_WorkflowContainer", Arrays.asList("GestionDesAvances"));
        		  controller.addEqualsConstraint("Beneficiaire", demandeur);
        		  
        		  Calendar monthStart = Calendar.getInstance();
        		  monthStart.setTime(new Date());
        		  monthStart.set(Calendar.DATE, monthStart.getActualMinimum(Calendar.DATE));
				  monthStart.add(Calendar.DATE,-1);
        		  
        		  Calendar monthEnd = Calendar.getInstance();
        		  monthEnd.setTime(new Date());
        		  monthEnd.set(Calendar.DATE, monthEnd.getActualMaximum(Calendar.DATE));
				  monthEnd.add(Calendar.DATE,1);

				  controller.addGreaterConstraint("sys_CreationDate", monthStart.getTime());
        		 controller.addLessConstraint("sys_CreationDate", monthEnd.getTime());
        		  
        		  controller.addNotEqualsConstraint("sys_Reference", document.getValue("sys_Reference"));
        		  
        		  controller.addNotInConstraint("DocumentState", Arrays.asList("En cours","Refusée","En attente d'approbation du demandeur","Retournée"));
        		  
        		  collection = controller.evaluate(catalog);
        	  }
        	  catch (Exception e)
        	  {
        		  e.printStackTrace();
        	  }
          }
          return collection;
    }
}
