package com.moovapps.ibb.rh.FicheSalarie.Formulaire;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Cummon.DateHelper;

import java.util.*;

public class MesInfosPersoPro extends BaseDocumentExtension {
	
	private enum FIELDS{




		Title("Civilité"),
		LastName("Nom"), FirstName("Prénom"), MobilePhoneNumber("Numéro de téléphone"),CodeMobile("Code Mobile"),
		Extension("Extension"),
		Birthday("Date de naisssance"), Maladie("Maladie"), CIN("CIN"), NCNSS("N° Immatriculation CNSS"),Ville("Ville"), Address1("Adresse 1"),Adresse2("Adresse2"),
		EtatCivil("Etat Civil"), Sex("Genre"),DateDEmbauche("Date d'embauche société"),DateDEmbaucheGroupe("Date d'embauche groupe"),ContractType("ContractType"),
		Pole("Pôle"),Societe("Société"),Departement("Département"),Activite("Activité"),Service("Service"),Metier("Métier"),
		Fonction("Fonction"),Grade("Grade"),
		Categorie("Circuit"),Profil("Profil"),ProfilEvaluation("Profil Evaluation"),Site("Site"),NMutuelle("N° Mutuelle"),
		NomDeLaMutuelle("Nom de la mutuelle"),TauxDeLaMutuelle("Taux de la mutuelle (%)"),NCIMR("N° CIMR"),
		TauxDeLaRetraire("Taux de la retraire (%)"),NumeroAttribue("Numéro attribué"),
		NomDeLaRetraiteComplementaire("Nom de la retraite complémentaire"),
		TauxDeLaRetraiteComplementaire("Taux de la Retraite Complémentaire (%)"),MontantEpargneRetraite("Montant épargne retraite"),

		Email("Email"), NCompteBancaire("RIB"), Banque("Banque"),
		AgenceBancaire("Agence bancaire"), NumeroFax("Numéro fax"),NumeroDAgence("Numéro d'agence"),
		Domiciliation("Domiciliation"),MainLever("Main lever"),LienParente("Lien de parenté");
		
		public final String label;

	    private FIELDS(String label) {
	        this.label = label;
	    }
	}
	
	IWorkflowInstance document = null;
	IUser salarie = null;
	IStorageResource ficheSalarie = null;
	Collection<IStorageResource> userLiensParente = Collections.emptyList();
	ArrayList<String> fields = new ArrayList<String>(Arrays.asList("Title",
			"LastName", "FirstName", "MobilePhoneNumber","CodeMobile","Extension", "Birthday", "Maladie", "CIN", "NCNSS","Ville", "Address1","Adresse2"
			,"EtatCivil", "Sex","DateDEmbauche","DateDEmbaucheGroupe","ContractType","Pole","Societe","Departement","Activite","Service","Metier","Fonction","Grade","Categorie","Profil","ProfilEvaluation","Site","NMutuelle",
			"NomDeLaMutuelle","TauxDeLaMutuelle","NCIMR","TauxDeLaRetraire","NumeroAttribue","NomDeLaRetraiteComplementaire","TauxDeLaRetraiteComplementaire","MontantEpargneRetraite",

			"Email", "NCompteBancaire", "Banque",
			"AgenceBancaire", "NumeroFax","NumeroDAgence","Domiciliation","MainLever"));
	
	@Override
	public boolean onAfterLoad() {

		document = getWorkflowInstance();
		salarie = (IUser)document.getValue("sys_Creator");
		ficheSalarie = getFicheSalarie(salarie);
		// ficheSalarie =(IStorageResource)salarie.getExtendedAttributes().getValue("FicheSalarie");
		document.setValue("FicheSalarie", ficheSalarie);
	
		IStorageResource societe=null;
		societe = (IStorageResource)salarie.getExtendedAttributes().getValue("Societe");
		document.setValue("Societe", societe); 
		document.setValue("ResponsableRH", societe != null ? societe.getValue("ResponsableRH") : null);
		setOldFields();
		if(document.getValue("DateDEmbauche") != null){				
			document.setValue("AncienneteInDetail", DateHelper.getDurationFromADateToNow((Date)document.getValue("DateDEmbauche")));
		}else {
			document.setValue("AncienneteInDetail", null);
		}
		setLienParente();
		return super.onAfterLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property) {
		setTargetNullOnSourceChange(property,"PoleUpdate","SocieteUpdate");
		setTargetNullOnSourceChange(property,"SocieteUpdate","DepartementUpdate");
		setTargetNullOnSourceChange(property,"DepartementUpdate","ActiviteUpdate");
		setTargetNullOnSourceChange(property,"ActiviteUpdate","ServiceUpdate");
		setTargetNullOnSourceChange(property,"SocieteUpdate","MetierUpdate");
		setTargetNullOnSourceChange(property,"SocieteUpdate","FonctionUpdate");
		setTargetNullOnSourceChange(property,"FonctionUpdate","GradeUpdate");
		setTargetNullOnSourceChange(property,"SocieteUpdate","SiteUpdate");
		if(property.getName().equals("LienParente")){
			onLienParenteChange();
		}
		super.onPropertyChanged(property);
	}
	private void setTargetNullOnSourceChange(IProperty property,String source,String target){
		if(property.getName().equals(source)){
			getWorkflowInstance().setValue(target,null);
		}
	}
	private void onLienParenteChange() {
		Collection<ILinkedResource> tableauDynamiqueLienParente = (Collection<ILinkedResource>)document.getLinkedResources("LienParente");
		int nbrEnfant = 0;
		for (ILinkedResource tdLienParente : tableauDynamiqueLienParente) {
			if(tdLienParente.getValue("LienParente") != null && tdLienParente.getValue("LienParente").equals("Enfant")){
				nbrEnfant++;
			}
		}
		document.setValue("NombreEnfantsUpdate", nbrEnfant);
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("SoumettreLaDemandeDeModification")){
			boolean isLienParenteUpdated = isLienParenteUpdated();
			document.setValue("isLienParenteUpdated", isLienParenteUpdated);
			if(!checkIfAtLeastOneFieldUpdated() && !isLienParenteUpdated){
				getResourceController().alert("Vous n'avez fait aucune modification");
				return false;
			}
			document.setValue("LesChampsModifie", getUpdatedFields());;
		}
		document.save(getWorkflowModule().getSysadminContext());
		return super.onBeforeSubmit(action);
	}
	
	private String getUpdatedFields() {
		ArrayList<String> fieldsUpdated = new ArrayList<>();
		for (String field : fields) {
			if (document.getValue(field+"Update") != null) {
				try {
					if(document.getValue(field) == null || !(((Number)document.getValue(field)).floatValue() == ((Number)document.getValue(field+"Update")).floatValue())){
						fieldsUpdated.add(FIELDS.valueOf(field).label);
					}
				} catch (Exception e) {
					if(!document.getValue(field).equals(document.getValue(field+"Update"))){
						fieldsUpdated.add(FIELDS.valueOf(field).label);
					}
				}
				
			}
		}
		if((boolean)document.getValue("isLienParenteUpdated")){
			fieldsUpdated.add(FIELDS.valueOf("LienParente").label);
		}
		return fieldsUpdated.toString().replace("[", "").replace("]", "");
	}
	
	private void setLienParente() {
		//if(!(boolean)document.getValue("isLienParenteUpdated")){
			Collection<ILinkedResource> tableauDynamiqueLienParente = (Collection<ILinkedResource>)document.getLinkedResources("LienParente");
			document.deleteLinkedResources(tableauDynamiqueLienParente);
			userLiensParente = getUserLienParente();
			for (IStorageResource lien : userLiensParente) {
				ILinkedResource linkedResource = document.createLinkedResource("LienParente");
				linkedResource.setValue("LienParente", lien.getValue("LienParente"));
				linkedResource.setValue("NomPrenom", lien.getValue("NomPrenom"));
				document.addLinkedResource(linkedResource);
			}
		//}
	}
	
	private Collection<IStorageResource> getUserLienParente() {
		Collection<IStorageResource> liensParente = Collections.emptyList();
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
			IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
			IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
			ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL",ICatalog.IType.STORAGE, project);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "LienParente");
			controller.addEqualsConstraint("FicheSalarie", ficheSalarie);
			liensParente = controller.evaluate(definition);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return liensParente;
	}
	
	private boolean isLienParenteUpdated() {
		boolean isLienParenteUpdated = false;
		Collection<ILinkedResource> tableauDynamiqueLienParente = (Collection<ILinkedResource>)document.getLinkedResources("LienParente");
		if(userLiensParente.size() != tableauDynamiqueLienParente.size()){
			isLienParenteUpdated = true;
		} else {
			for (ILinkedResource tdLienParente : tableauDynamiqueLienParente) {
				boolean lienParenteFound = true;
				for (IStorageResource srLienParente : userLiensParente) {
					if (!(tdLienParente.getValue("LienParente").equals(
							srLienParente.getValue("LienParente"))
							&& tdLienParente.getValue("NomPrenom").equals(
									srLienParente.getValue("NomPrenom")))) {
						lienParenteFound = false;
					} else {
						lienParenteFound = true;
					}
				}
				if(!lienParenteFound) return true;
			}
		}
		return isLienParenteUpdated;
	}
	
	private boolean checkIfAtLeastOneFieldUpdated() {
		boolean isAtLeastOneFieldUpdated = false;
		for (String field : fields) {
			if (document.getValue(field + "Update") != null) {
				if(!document.getValue(field + "Update").equals(document.getValue(field))){
					isAtLeastOneFieldUpdated = true;
				}
			}
		}
		return isAtLeastOneFieldUpdated;
	}
	
	private void setOldFields() {
		// info perso
		
		document.setValue("Matricule", ficheSalarie.getValue("Matricule"));

		for (String field : fields) {
			document.setValue(field, ficheSalarie.getValue(field));

		}

		/*document.setValue("Title", ficheSalarie.getValue("Title"));
		document.setValue("LastName", ficheSalarie.getValue("LastName"));
		document.setValue("FirstName", ficheSalarie.getValue("FirstName"));
		document.setValue("Birthday", ficheSalarie.getValue("Birthday"));
		document.setValue("CIN", ficheSalarie.getValue("CIN"));
		document.setValue("NCIMR", ficheSalarie.getValue("NCIMR"));
		document.setValue("NMutuelle", ficheSalarie.getValue("NMutuelle"));

		document.setValue("NCNSS", ficheSalarie.getValue("NCNSS"));
		document.setValue("Address1", ficheSalarie.getValue("Address1"));
		document.setValue("EtatCivil", ficheSalarie.getValue("EtatCivil"));
		document.setValue("NombreEnfants", ficheSalarie.getValue("NombreEnfants"));
		
		
		// infos pro
		
			// infos login
		document.setValue("Email", ficheSalarie.getValue("Email"));
		document.setValue("Identifiant", ficheSalarie.getValue("Identifiant"));
		document.setValue("MotDePasse", ficheSalarie.getValue("MotDePasse"));
		
			// infos bancaire
		document.setValue("NCompteBancaire", ficheSalarie.getValue("NCompteBancaire"));
		document.setValue("Banque", ficheSalarie.getValue("Banque"));
		document.setValue("AgenceBancaire", ficheSalarie.getValue("AgenceBancaire"));
		
			// affectation 
		document.setValue("DateDEmbauche", ficheSalarie.getValue("DateDEmbauche"));
		document.setValue("ContractType", ficheSalarie.getValue("ContractType"));
		document.setValue("Societe", ficheSalarie.getValue("Societe"));
		document.setValue("Direction", ficheSalarie.getValue("Direction"));
		document.setValue("Departement2", ficheSalarie.getValue("Departement2"));
		document.setValue("Categorie", ficheSalarie.getValue("Categorie"));
		document.setValue("Fonction", ficheSalarie.getValue("Fonction"));
		document.setValue("HierarchicalManager", ficheSalarie.getValue("HierarchicalManager"));
//		document.setValue("Exit", ficheSalarie.getValue("Exit"));
		
		if(document.getValue("DateDeSortie")!=null){
			document.setValue("DateDeSortie", ficheSalarie.getValue("DateDeSortie"));
		}
		document.setValue("MotifSortie", ficheSalarie.getValue("MotifSortie"));
		
			// données salariales
		document.setValue("TypeDeSalaire", ficheSalarie.getValue("TypeDeSalaire"));
		document.setValue("SalaireNETDH", ficheSalarie.getValue("SalaireNETDH"));

				// Mutuelle
		document.setValue("NomDeLaMutuelle", ficheSalarie.getValue("NomDeLaMutuelle"));
		document.setValue("TauxDeLaMutuelle", ficheSalarie.getValue("TauxDeLaMutuelle"));
				// Retraite
		document.setValue("NomRetraite", ficheSalarie.getValue("NomRetraite"));
		document.setValue("TauxDeLaRetraire", ficheSalarie.getValue("TauxDeLaRetraire"));
		document.setValue("NumeroAttribue", ficheSalarie.getValue("NumeroAttribue"));
				// Reatraire complémentaire
		document.setValue("NomDeLaRetraiteComplementaire", ficheSalarie.getValue("NomDeLaRetraiteComplementaire"));
		document.setValue("TauxDeLaRetraiteComplementaire", ficheSalarie.getValue("TauxDeLaRetraiteComplementaire"));
		document.setValue("MontantEpargneRetraite", ficheSalarie.getValue("MontantEpargneRetraite"));
				// Congés & absence
		document.setValue("DroitMensuelle", ficheSalarie.getValue("DroitMensuelle"));
		document.setValue("AbsenceAnneeEnCours", ficheSalarie.getValue("AbsenceAnneeEnCours"));
		document.setValue("AbsenceAnterieure", ficheSalarie.getValue("AbsenceAnterieure"));
				// Congés
		document.setValue("SoldeAnneeEnCours", ficheSalarie.getValue("SoldeAnneeEnCours"));
		document.setValue("SoldeAnterieur", ficheSalarie.getValue("SoldeAnterieur"));
		document.setValue("SoldeConges", ficheSalarie.getValue("SoldeConges"));
		document.setValue("CongesPayesEnCoursDeValidation", ficheSalarie.getValue("CongesPayesEnCoursDeValidation"));
		document.setValue("CongesPayesEnCoursDeConsommation", ficheSalarie.getValue("CongesPayesEnCoursDeConsommation"));
		document.setValue("CongesPayesEnCoursDeTraitement", ficheSalarie.getValue("CongesPayesEnCoursDeTraitement"));
		document.setValue("CongesSpeciauxEnCoursDeValidation", ficheSalarie.getValue("CongesSpeciauxEnCoursDeValidation"));
		document.setValue("CongesSpeciauxEnCoursDeConsommation", ficheSalarie.getValue("CongesSpeciauxEnCoursDeConsommation"));
		document.setValue("CongesSpeciauxEnCoursDeTraitement", ficheSalarie.getValue("CongesSpeciauxEnCoursDeTraitement"));
		document.setValue("CongesMaladieEnCoursDeValidation", ficheSalarie.getValue("CongesMaladieEnCoursDeValidation"));
		document.setValue("CongesMaladieEnCoursDeConsommation", ficheSalarie.getValue("CongesMaladieEnCoursDeConsommation"));
		document.setValue("CongesMaladieEnCoursDeTraitement", ficheSalarie.getValue("CongesMaladieEnCoursDeTraitement"));
		document.setValue("CongesSansSoldeEnCoursDeValidation", ficheSalarie.getValue("CongesSansSoldeEnCoursDeValidation"));
		document.setValue("CongesSansSoldeEnCoursDeConsommation", ficheSalarie.getValue("CongesSansSoldeEnCoursDeConsommation"));
		document.setValue("CongesSansSoldeEnCoursDeTraitement", ficheSalarie.getValue("CongesSansSoldeEnCoursDeTraitement"));

		document.setValue("JourSEnCoursDeValidation", ficheSalarie.getValue("JourSEnCoursDeValidation"));
		document.setValue("JourSEnCoursDeConsommation", ficheSalarie.getValue("JourSEnCoursDeConsommation"));
		document.setValue("JourSEnCoursDeTraitement", ficheSalarie.getValue("JourSEnCoursDeTraitement"));

		document.setValue("CongesPayesAnneeEnCours", ficheSalarie.getValue("CongesPayesAnneeEnCours"));
		document.setValue("CongesPayesN1", ficheSalarie.getValue("CongesPayesN1"));
		document.setValue("CongesPayesPris", ficheSalarie.getValue("CongesPayesPris"));
		document.setValue("CongesSpeciauxAnneeEnCours", ficheSalarie.getValue("CongesSpeciauxAnneeEnCours"));
		document.setValue("CongesSpeciauxN1", ficheSalarie.getValue("CongesSpeciauxN1"));
		document.setValue("CongesSpeciauxPris", ficheSalarie.getValue("CongesSpeciauxPris"));
		document.setValue("CongesMaladieAnneeEnCours", ficheSalarie.getValue("CongesMaladieAnneeEnCours"));
		document.setValue("CongesMaladieN1", ficheSalarie.getValue("CongesMaladieN1"));
		document.setValue("CongesMaladiePris", ficheSalarie.getValue("CongesMaladiePris"));
		document.setValue("CongesSansSoldeAnneeEnCours", ficheSalarie.getValue("CongesSansSoldeAnneeEnCours"));
		document.setValue("CongesSansSoldeN1", ficheSalarie.getValue("CongesSansSoldeN1"));
		document.setValue("CongesSansSoldePris", ficheSalarie.getValue("CongesSansSoldePris"));
		document.setValue("TotalJoursPris", ficheSalarie.getValue("TotalJoursPris"));*/
	}
	
	private IStorageResource getFicheSalarie(IUser user) {
		IStorageResource ficheSalarie = null;
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
			IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
			IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
			ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL",ICatalog.IType.STORAGE, project);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
			controller.addEqualsConstraint("Salarie", user);
			Collection<IStorageResource> demandeurFiches = controller.evaluate(definition);
			if (!demandeurFiches.isEmpty()){				
				ficheSalarie = demandeurFiches.iterator().next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ficheSalarie;
	}
	
	
}
