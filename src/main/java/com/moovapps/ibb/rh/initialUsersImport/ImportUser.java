package com.moovapps.ibb.rh.initialUsersImport;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.IProjectModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;

import java.util.Collection;


public class ImportUser extends BaseAgent {

	private IWorkflowModule workflowModule;
	private IDirectoryModule directoryModule;
	private IContext sysContext;
	private IProjectModule projectModule;

	@Override
	protected void execute() {
		this.workflowModule = Modules.getWorkflowModule();
	     this.directoryModule = Modules.getDirectoryModule();
	     this.sysContext = workflowModule.getSysadminContext();
	     this.projectModule = Modules.getProjectModule();
	 /*List<String> newRecruitsColonnes = Arrays.asList(


		 );*/
	     try {
			int i = 1;

			 Collection<IUser> users = (Collection<IUser>) directoryModule.getUsers(workflowModule.getSysadminContext());
			 /*ArrayList<IUser> users = new ArrayList<>();
			 users.add(directoryModule.getUserByLogin("UMP.EDDERMOUNE.AIMAD"));*/
			 for (IUser user : users) {
	    		// IUser user = directoryModule.getUserByLogin(login);
			     /*IViewController controller = workflowModule.getViewController(sysContext, IResource.class);
					IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
					IProject project = projectModule.getProject(sysContext, "REFERENTIELCOMMUN", organization);
					ICatalog catalog = workflowModule.getCatalog(sysContext, "REFERENTIEL", 4, project);
					IResourceDefinition definition = workflowModule.getResourceDefinition(sysContext, catalog, "FicheCollaborateur");*/
					// IStorageResource userFiche = workflowModule.createStorageResource(workflowModule.getSysadminContext(), definition, "");
				 	IStorageResource userFiche = getFicheByUser(user);

					/*userFiche.setValue("Salarie", user);
					userFiche.setValue("Organisation", user.getOrganization());
	
				    userFiche.setValue("sys_Reference", user.getExtendedAttributes().getValue("Matricule"));
				    userFiche.setValue("Matricule", user.getExtendedAttributes().getValue("Matricule"));
				    userFiche.setValue("MotDePasse", null);
				    userFiche.setValue("ConfirmerMotDePasse", null);
				    userFiche.setValue("Identifiant", user.getLogin());
				    
				    userFiche.setValue("Title", user.getTitle());
				    userFiche.setValue("FirstName", user.getFirstName());
				    userFiche.setValue("LastName", user.getLastName());
				    userFiche.setValue("MobilePhoneNumber", user.getMobilePhoneNumber());
				    userFiche.setValue("Birthday", user.getBirthday());
				    userFiche.setValue("CIN", user.getExtendedAttributes().getValue("CIN"));
				    userFiche.setValue("NCNSS", user.getExtendedAttributes().getValue("NCNSS") != null ? (String)user.getExtendedAttributes().getValue("NCNSS") : null);
				    userFiche.setValue("Address1", user.getAddress1());
				    userFiche.setValue("EtatCivil", user.getExtendedAttributes().getValue("EtatCivil"));
				    userFiche.setValue("NombreEnfants", user.getExtendedAttributes().getValue("NombreEnfants"));
	
				    userFiche.setValue("Email", user.getEmail());
	
				    userFiche.setValue("Banque", user.getExtendedAttributes().getValue("Banque"));
				    userFiche.setValue("NCompteBancaire",user.getExtendedAttributes().getValue("NCompteBancaire") != null ? (String)user.getExtendedAttributes().getValue("NCompteBancaire") : null);
				    userFiche.setValue("AgenceBancaire", user.getExtendedAttributes().getValue("AgenceBancaire"));
	
				    userFiche.setValue("DateDEmbauche", user.getExtendedAttributes().getValue("DateDEmbauche"));
				    userFiche.setValue("ContractType", user.getExtendedAttributes().getValue("ContractType"));
				    userFiche.setValue("Societe", user.getExtendedAttributes().getValue("Societe"));
				    userFiche.setValue("SecretGroupes", user.getExtendedAttributes().getValue("SecretGroupes"));
				    userFiche.setValue("Groupes", user.getExtendedAttributes().getValue("Groupes"));
				    userFiche.setValue("Direction", user.getExtendedAttributes().getValue("Direction"));
				    userFiche.setValue("Departement", user.getExtendedAttributes().getValue("Departement"));
				    userFiche.setValue("Fonction", user.getExtendedAttributes().getValue("Fonction"));
				    userFiche.setValue("HierarchicalManager", user.getHierarchicalManager());
			//	    userFiche.setValue("Exit", user.getExit());
				    userFiche.setValue("DateDeSortie", user.getExit());

				    userFiche.setValue("MotifSortie", user.getExtendedAttributes().getValue("MotifSortie"));
	
				    userFiche.setValue("TauxHoraire", user.getExtendedAttributes().getValue("TauxHoraire"));
				    userFiche.setValue("SalaireDeBase", user.getExtendedAttributes().getValue("SalaireDeBase"));
				    userFiche.setValue("SalaireBrutDH", user.getExtendedAttributes().getValue("SalaireBrut"));*/
				    userFiche.setValue("SalaireNETDH", user.getExtendedAttributes().getValue("Salaire"));
	
				    /*userFiche.setValue("NomDeLaMutuelle", user.getExtendedAttributes().getValue("NomDeLaMutuelle"));
				    userFiche.setValue("TauxDeLaMutuelle", user.getExtendedAttributes().getValue("TauxDeLaMutuelle"));
	
				    userFiche.setValue("NomRetraite", user.getExtendedAttributes().getValue("NomRetraite"));
				    userFiche.setValue("TauxDeLaRetraire", user.getExtendedAttributes().getValue("TauxDeLaRetraire"));
				    userFiche.setValue("NumeroAttribue", user.getExtendedAttributes().getValue("NumeroAttribue"));
	
				    userFiche.setValue("NomDeLaRetraiteComplementaire", user.getExtendedAttributes().getValue("NomDeLaRetraiteComplementaire"));
				    userFiche.setValue("TauxDeLaRetraiteComplementaire", user.getExtendedAttributes().getValue("TauxDeLaRetraiteComplementaire"));
				    userFiche.setValue("MontantEpargneRetraite", user.getExtendedAttributes().getValue("MontantEpargneRetraite"));
	
				    userFiche.setValue("DroitMensuelle", user.getExtendedAttributes().getValue("DroitMensuelle"));
				    userFiche.setValue("AbsenceAnneeEnCours", user.getExtendedAttributes().getValue("AbsenceAnneeEnCours"));
				    userFiche.setValue("AbsenceAnterieure", user.getExtendedAttributes().getValue("AbsenceAnterieure"));
				    userFiche.setValue("SoldeAnneeEnCours", user.getExtendedAttributes().getValue("SoldeAnneeEnCours"));
				    userFiche.setValue("SoldeAnterieur", user.getExtendedAttributes().getValue("SoldeAnterieur"));
				    userFiche.setValue("SoldeConges", user.getExtendedAttributes().getValue("SoldeConges"));
				    userFiche.setValue("CongesPayesEnCoursDeValidation", user.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));
				    userFiche.setValue("CongesPayesEnCoursDeConsommation", user.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation"));
				    userFiche.setValue("CongesPayesEnCoursDeTraitement", user.getExtendedAttributes().getValue("CongesPayesEnCoursDeTraitement"));
				    userFiche.setValue("CongesSpeciauxEnCoursDeValidation", user.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation"));
				    userFiche.setValue("CongesSpeciauxEnCoursDeConsommation", user.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation"));
				    userFiche.setValue("CongesSpeciauxEnCoursDeTraitement", user.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeTraitement"));
				    userFiche.setValue("CongesMaladieEnCoursDeValidation", user.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation"));
				    userFiche.setValue("CongesMaladieEnCoursDeConsommation", user.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
				    userFiche.setValue("CongesMaladieEnCoursDeTraitement", user.getExtendedAttributes().getValue("CongesMaladieEnCoursDeTraitement"));
				    userFiche.setValue("CongesSansSoldeEnCoursDeValidation", user.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation"));
				    userFiche.setValue("CongesSansSoldeEnCoursDeConsommation", user.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation"));
				    userFiche.setValue("CongesSansSoldeEnCoursDeTraitement", user.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeTraitement"));
				    userFiche.setValue("JourSEnCoursDeValidation", user.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
				    userFiche.setValue("JourSEnCoursDeConsommation", user.getExtendedAttributes().getValue("joursEnCoursConsommation"));
				    userFiche.setValue("JourSEnCoursDeTraitement", user.getExtendedAttributes().getValue("JourSEnCoursDeTraitement"));
				    userFiche.setValue("CongesPayesAnneeEnCours", user.getExtendedAttributes().getValue("CongesPayesAnneeEnCours"));
				    userFiche.setValue("CongesPayesN1", user.getExtendedAttributes().getValue("CongesPayesN1"));
				    userFiche.setValue("CongesPayesPris", user.getExtendedAttributes().getValue("CongesPayesPris"));
				    userFiche.setValue("CongesSpeciauxAnneeEnCours", user.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours"));
				    userFiche.setValue("CongesSpeciauxN1", user.getExtendedAttributes().getValue("CongesSpeciauxN1"));
				    userFiche.setValue("CongesSpeciauxPris", user.getExtendedAttributes().getValue("CongesSpeciauxPris"));
				    userFiche.setValue("CongesMaladieAnneeEnCours", user.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours"));
				    userFiche.setValue("CongesMaladieN1", user.getExtendedAttributes().getValue("CongesMaladieN1"));
				    userFiche.setValue("CongesMaladiePris", user.getExtendedAttributes().getValue("CongesMaladiePris"));
				    userFiche.setValue("CongesSansSoldeAnneeEnCours", user.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours"));
				    userFiche.setValue("CongesSansSoldeN1", user.getExtendedAttributes().getValue("CongesSansSoldeN1"));
				    userFiche.setValue("CongesSansSoldePris", user.getExtendedAttributes().getValue("CongesSansSoldePris"));
				    userFiche.setValue("TotalJoursPris", user.getExtendedAttributes().getValue("TotalJoursPris"));*/
				    userFiche.save(sysContext);
					 LOGGER.error(i+"");
					 i++;
				    if(i==30){
				    	System.gc();
				    }
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private IStorageResource getFicheByUser(IUser user){
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
			IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
			IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
			ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
			controller.addEqualsConstraint("Salarie", user);
			return (IStorageResource) controller.evaluate(definition).iterator().next();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
