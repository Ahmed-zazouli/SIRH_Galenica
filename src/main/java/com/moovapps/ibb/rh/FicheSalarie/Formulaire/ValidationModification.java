package com.moovapps.ibb.rh.FicheSalarie.Formulaire;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.FicheSalarie.Helpers.UserFicheAnnuaireConvert;

import java.util.*;

public class ValidationModification extends BaseDocumentExtension {
	

	
	IWorkflowInstance document = null;
	IStorageResource ficheSalarie = null;
	ArrayList<String> fields = new ArrayList<String>(Arrays.asList("Title",
			"LastName", "FirstName", "MobilePhoneNumber","CodeMobile","Extension", "Birthday", "Maladie", "CIN", "NCNSS","Ville", "Address1","Adresse2"
			,"EtatCivil", "Sex","DateDEmbauche","DateDEmbaucheGroupe","ContractType","Pole","Societe","Departement","Activite","Service","Metier","Fonction","Grade","Categorie","Profil","ProfilEvaluation","Site","NMutuelle",
			"NomDeLaMutuelle","TauxDeLaMutuelle","NCIMR","TauxDeLaRetraire","NumeroAttribue","NomDeLaRetraiteComplementaire","TauxDeLaRetraiteComplementaire","MontantEpargneRetraite",

			"Email", "NCompteBancaire", "Banque",
			"AgenceBancaire", "NumeroFax","NumeroDAgence","Domiciliation","MainLever"));
	
	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		ficheSalarie = (IStorageResource)document.getValue("FicheSalarie");
		return super.onAfterLoad();
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("ApprouverModification")){
			onApprouverModification();
		}
		return super.onBeforeSubmit(action);
	}
	
	private void onApprouverModification() {
		HashMap<String, Object> oldValues = new HashMap<>();
		for (IProperty prop : ficheSalarie.getDefinition().getProperties()) {
			oldValues.put(prop.getName(), ficheSalarie.getValue(prop.getName()));
		}
		IContext connectedUserContext = getWorkflowModule().getLoggedOnUserContext();
		for (String field : fields) {
			if (document.getValue(field+"Update") != null) {
				ficheSalarie.setValue(field, document.getValue(field+"Update"));
			}
		}
		manageLienParente();
		ficheSalarie.save(getWorkflowModule().getSysadminContext());
		IUser user = UserFicheAnnuaireConvert.fromFicheToUser(ficheSalarie);
		user.save(getWorkflowModule().getSysadminContext());
		saveHistorique(oldValues);
	}

	private void saveHistorique(HashMap oldValues) {

		try {
			Collection<IProperty> properties = (Collection<IProperty>) ficheSalarie.getDefinition().getProperties();

			IStorageResource historique = null;
			StringBuilder champModifier = new StringBuilder();
			for (IProperty prop : properties) {
				if (prop.getDescription().equals("ignore")) continue;
				Object oldValue = oldValues.get(prop.getName());
				Object newValue = ficheSalarie.getValue(prop.getName());
				if ((oldValue == null ^ newValue == null) || (oldValue != null && !customCompare(oldValue, newValue))) {
					if (historique == null) {
						IContext context = getWorkflowModule().getSysadminContext();
						IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
						IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
						ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
						IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Historique");
						historique = getWorkflowModule().createStorageResource(context, definition, "");
						historique.setValue("DateDeModifier", new Date());
						historique.setValue("Auteur", getWorkflowModule().getLoggedOnUser());
						historique.setValue("FicheCollaborateur", ficheSalarie);
						historique.save(getWorkflowModule().getSysadminContext());
					}
					IStorageResource historiqueDetail = null;
					IContext context = getWorkflowModule().getSysadminContext();
					IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
					IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
					ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
					IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "HistoriqueDetail");
					historiqueDetail = getWorkflowModule().createStorageResource(context, definition, "");
					historiqueDetail.setValue("Date", new Date());
					historiqueDetail.setValue("AncianeValue", oldValue);
					historiqueDetail.setValue("Champ", prop.getLabel());
					historiqueDetail.setValue("Historique", historique);
					historiqueDetail.setValue("NouvelleValue", newValue);
					historiqueDetail.setValue("FicheCollaborateur", ficheSalarie);
					historiqueDetail.save(getWorkflowModule().getSysadminContext());
					champModifier.append((champModifier.length() == 0) ? prop.getLabel() : ", " + prop.getLabel());
				}
			}
			historique.setValue("LesChampsModifies", champModifier!=null? champModifier.toString():"");
			historique.save(getWorkflowModule().getSysadminContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
		oldValues = new HashMap<>();
		for (IProperty prop : ficheSalarie.getDefinition().getProperties()) {
			oldValues.put(prop.getName(), ficheSalarie.getValue(prop.getName()));
		}

	}

	boolean customCompare(Object obj1, Object obj2){
		if(obj1 instanceof Number && obj2 instanceof Number){
			return ((Number)obj1).doubleValue() == ((Number)obj2).doubleValue();
			//((Number)obj1).doubleValue() == ((Number)obj2).doubleValue();
		}
		return Objects.equals(obj1, obj2);
	}
	
	private void manageLienParente() {
		Collection<IStorageResource> userLienParente = getUserLienParente();
		for (IStorageResource lienParente : userLienParente) {
			lienParente.delete(getWorkflowModule().getSysadminContext());
		}
		Collection<ILinkedResource> newLienParente = Collections.emptyList();
		if(document.getValue("LienParente") != null){
			newLienParente = (Collection<ILinkedResource>)document.getValue("LienParente");
		}
		addNewLienParente(newLienParente);
	}
	
	private void addNewLienParente(Collection<ILinkedResource> newLienParente) {
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL",ICatalog.IType.STORAGE, project);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "LienParente");
			for (ILinkedResource lienParente : newLienParente) {
				IStorageResource storageRessource = getWorkflowModule().createStorageResource(context, definition, "");
				storageRessource.setValue("LienParente", lienParente.getValue("LienParente"));
				storageRessource.setValue("NomPrenom", lienParente.getValue("NomPrenom"));
				storageRessource.setValue("FicheSalarie", ficheSalarie);
				storageRessource.setValue("Salarie", ficheSalarie.getValue("Salarie"));
				storageRessource.save(context);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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

}
