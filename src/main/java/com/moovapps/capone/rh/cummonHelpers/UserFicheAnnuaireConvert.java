package com.moovapps.capone.rh.cummonHelpers;

import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IProjectModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;

import java.util.Collection;
import java.util.Date;

public class UserFicheAnnuaireConvert {
	
	public IUser fromFicheToUser(IStorageResource userFiche){
		IUser user = (IUser)userFiche.getValue("Salarie");
		
		// user Extended fields
		user.setOrganization((IOrganization)userFiche.getValue("Organisation"));
		
		// Informations personnelles
		
		// if they want the matricule to be auto generated
		user.getExtendedAttributes().setValue("Matricule", userFiche.getValue("sys_Reference"));
		// if they want the matricule to be a normal text field
		// user.getExtendedAttributes().setValue("Matricule", userFiche.getValue("Matricule"));
		user.setTitle(userFiche.getValue("Title") != null ? (String)userFiche.getValue("Title") : null);
		user.setFirstName(userFiche.getValue("FirstName") != null ? (String)userFiche.getValue("FirstName") : null);
		user.setLastName(userFiche.getValue("LastName") != null ? (String)userFiche.getValue("LastName") : null);
		user.setMobilePhoneNumber(userFiche.getValue("MobilePhoneNumber") != null ? (String)userFiche.getValue("MobilePhoneNumber") : null);
		user.setBirthday(userFiche.getValue("Birthday") != null ? (Date)userFiche.getValue("Birthday") : null);
		user.getExtendedAttributes().setValue("CIN", userFiche.getValue("CIN"));
		user.getExtendedAttributes().setValue("NCNSS", userFiche.getValue("NCNSS"));
		user.setAddress1(userFiche.getValue("Address1") != null ? (String)userFiche.getValue("Address1") : null);
		user.getExtendedAttributes().setValue("EtatCivil", userFiche.getValue("EtatCivil"));
		user.getExtendedAttributes().setValue("NombreEnfants", userFiche.getValue("NombreEnfants"));
		
		// Informations login
		user.setEmail(userFiche.getValue("Email") != null ? (String)userFiche.getValue("Email") : null);
		user.setPassword((String)userFiche.getValue("MotDePasse"));

		
		// Informations bancaire
		user.getExtendedAttributes().setValue("Banque", userFiche.getValue("Banque"));
		user.getExtendedAttributes().setValue("NCompteBancaire", userFiche.getValue("NCompteBancaire"));
		user.getExtendedAttributes().setValue("AgenceBancaire", userFiche.getValue("AgenceBancaire"));
		
		
		// Affectation
		user.getExtendedAttributes().setValue("DateDEmbauche", userFiche.getValue("DateDEmbauche"));
		user.setContractType(userFiche.getValue("ContractType") != null ? (String)userFiche.getValue("ContractType") : null);
		user.getExtendedAttributes().setValue("Societe", userFiche.getValue("Societe"));
		user.getExtendedAttributes().setValue("Groupes", userFiche.getValue("Groupes"));
		user.getExtendedAttributes().setValue("SecretGroupes", userFiche.getValue("SecretGroupes"));
		user.getExtendedAttributes().setValue("Direction", userFiche.getValue("Direction"));
		user.getExtendedAttributes().setValue("Categorie", userFiche.getValue("Categorie"));
		user.getExtendedAttributes().setValue("Departement", userFiche.getValue("Departement"));
		user.getExtendedAttributes().setValue("Fonction", userFiche.getValue("Fonction"));
		user.setHierarchicalManager(userFiche.getValue("HierarchicalManager") != null ? (IUser)userFiche.getValue("HierarchicalManager") : null);
		user.setExit(userFiche.getValue("Exit") != null ? (Date)userFiche.getValue("Exit") : null);
		user.setExit(userFiche.getValue("DateDeSortie") != null ? (Date)userFiche.getValue("DateDeSortie") : null);

		user.getExtendedAttributes().setValue("MotifSortie", userFiche.getValue("MotifSortie"));
		
		
		// Données salariales
		user.getExtendedAttributes().setValue("TauxHoraire", userFiche.getValue("TauxHoraire"));
		user.getExtendedAttributes().setValue("SalaireDeBase", userFiche.getValue("SalaireDeBase"));
		user.getExtendedAttributes().setValue("SalaireBrut", userFiche.getValue("SalaireBrutDH"));
		user.getExtendedAttributes().setValue("Salaire", userFiche.getValue("Salaire"));
		
		// Indemnités
		
		// Mutuelle
		user.getExtendedAttributes().setValue("NomDeLaMutuelle", userFiche.getValue("NomDeLaMutuelle"));
		user.getExtendedAttributes().setValue("TauxDeLaMutuelle", userFiche.getValue("TauxDeLaMutuelle"));
		
		// Retraite
		user.getExtendedAttributes().setValue("NomRetraite", userFiche.getValue("NomRetraite"));
		user.getExtendedAttributes().setValue("TauxDeLaRetraire", userFiche.getValue("TauxDeLaRetraire"));
		user.getExtendedAttributes().setValue("NumeroAttribue", userFiche.getValue("NumeroAttribue"));
		
		// Retraite complémentaire
		user.getExtendedAttributes().setValue("NomDeLaRetraiteComplementaire", userFiche.getValue("NomDeLaRetraiteComplementaire"));
		user.getExtendedAttributes().setValue("TauxDeLaRetraiteComplementaire", userFiche.getValue("TauxDeLaRetraiteComplementaire"));
		user.getExtendedAttributes().setValue("MontantEpargneRetraite", userFiche.getValue("MontantEpargneRetraite"));
		
		
		// Droit au congés
		user.getExtendedAttributes().setValue("DroitMensuelle", userFiche.getValue("DroitMensuelle"));
		user.getExtendedAttributes().setValue("AbsenceAnneeEnCours", userFiche.getValue("AbsenceAnneeEnCours"));
		user.getExtendedAttributes().setValue("AbsenceAnterieure", userFiche.getValue("AbsenceAnterieure"));
		user.getExtendedAttributes().setValue("SoldeAnneeEnCours", userFiche.getValue("SoldeAnneeEnCours"));
		user.getExtendedAttributes().setValue("SoldeAnterieur", userFiche.getValue("SoldeAnterieur"));
		user.getExtendedAttributes().setValue("SoldeConges", userFiche.getValue("SoldeConges"));
		user.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation", userFiche.getValue("CongesPayesEnCoursDeValidation"));
		user.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation", userFiche.getValue("CongesPayesEnCoursDeConsommation"));
		user.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeValidation", userFiche.getValue("CongesSpeciauxEnCoursDeValidation"));
		user.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeConsommation", userFiche.getValue("CongesSpeciauxEnCoursDeConsommation"));
		user.getExtendedAttributes().setValue("CongesMaladieEnCoursDeValidation", userFiche.getValue("CongesMaladieEnCoursDeValidation"));
		user.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation", userFiche.getValue("CongesMaladieEnCoursDeConsommation"));
		user.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation", userFiche.getValue("CongesSansSoldeEnCoursDeValidation"));
		user.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeConsommation", userFiche.getValue("CongesSansSoldeEnCoursDeConsommation"));
		user.getExtendedAttributes().setValue("JourSEnCoursDeValidation", userFiche.getValue("JourSEnCoursDeValidation"));
		user.getExtendedAttributes().setValue("joursEnCoursConsommation", userFiche.getValue("joursEnCoursConsommation"));
		user.getExtendedAttributes().setValue("CongesPayesAnneeEnCours", userFiche.getValue("CongesPayesAnneeEnCours"));
		user.getExtendedAttributes().setValue("CongesPayesN1", userFiche.getValue("CongesPayesN1"));
		user.getExtendedAttributes().setValue("CongesPayesPris", userFiche.getValue("CongesPayesPris"));
		user.getExtendedAttributes().setValue("CongesSpeciauxAnneeEnCours", userFiche.getValue("CongesSpeciauxAnneeEnCours"));
		user.getExtendedAttributes().setValue("CongesSpeciauxN1", userFiche.getValue("CongesSpeciauxN1"));
		user.getExtendedAttributes().setValue("CongesSpeciauxPris", userFiche.getValue("CongesSpeciauxPris"));
		user.getExtendedAttributes().setValue("CongesMaladieAnneeEnCours", userFiche.getValue("CongesMaladieAnneeEnCours"));
		user.getExtendedAttributes().setValue("CongesMaladieN1", userFiche.getValue("CongesMaladieN1"));
		user.getExtendedAttributes().setValue("CongesMaladiePris", userFiche.getValue("CongesMaladiePris"));
		user.getExtendedAttributes().setValue("CongesSansSoldeAnneeEnCours", userFiche.getValue("CongesSansSoldeAnneeEnCours"));
		user.getExtendedAttributes().setValue("CongesSansSoldeN1", userFiche.getValue("CongesSansSoldeN1"));
		user.getExtendedAttributes().setValue("CongesSansSoldePris", userFiche.getValue("CongesSansSoldePris"));
		user.getExtendedAttributes().setValue("TotalJoursPris", userFiche.getValue("TotalJoursPris"));
		
//		user.setOrganization((IOrganization)userFiche.getValue("Organisation"));
//		user.setPassword((String)userFiche.getValue("MotDePasse"));
//		user.setFirstName(userFiche.getValue("FirstName") != null ? (String)userFiche.getValue("FirstName") : null);
//		user.setLastName(userFiche.getValue("LastName") != null ? (String)userFiche.getValue("LastName") : null);
//		user.setTitle(userFiche.getValue("Title") != null ? (String)userFiche.getValue("Title") : null);
//		user.setMobilePhoneNumber(userFiche.getValue("MobilePhoneNumber") != null ? (String)userFiche.getValue("MobilePhoneNumber") : null);
//		user.setEmail(userFiche.getValue("Email") != null ? (String)userFiche.getValue("Email") : null);
//		user.setAddress1(userFiche.getValue("Address1") != null ? (String)userFiche.getValue("Address1") : null);
//		user.setSex(userFiche.getValue("Sex") != null ? (String)userFiche.getValue("Sex") : null);
//		user.setBirthday(userFiche.getValue("Birthday") != null ? (Date)userFiche.getValue("Birthday") : null);
//		
//		user.setHierarchicalManager(userFiche.getValue("HierarchicalManager") != null ? (IUser)userFiche.getValue("HierarchicalManager") : null);
//		user.setExit(userFiche.getValue("Exit") != null ? (Date)userFiche.getValue("Exit") : null);
//		user.setContractType(userFiche.getValue("ContractType") != null ? (String)userFiche.getValue("ContractType") : null);
//		user.getExtendedAttributes().setValue("CIN", userFiche.getValue("CIN"));
//		user.getExtendedAttributes().setValue("NCNSS", userFiche.getValue("NCNSS"));
//		user.getExtendedAttributes().setValue("Societe", userFiche.getValue("Societe"));
//		user.getExtendedAttributes().setValue("Matricule", userFiche.getValue("Matricule"));
//		user.getExtendedAttributes().setValue("EtatCivil", userFiche.getValue("EtatCivil"));
//		user.getExtendedAttributes().setValue("NombreEnfants", userFiche.getValue("NombreEnfants"));
//		user.getExtendedAttributes().setValue("Departement", userFiche.getValue("Departement"));
//		user.getExtendedAttributes().setValue("Fonction", userFiche.getValue("Fonction"));
//		user.getExtendedAttributes().setValue("CategorieDeSalaire", userFiche.getValue("CategorieDeSalaire"));
//		user.getExtendedAttributes().setValue("DateDEmbauche", userFiche.getValue("DateDEmbauche"));
//		user.getExtendedAttributes().setValue("DateDeTitularisation", userFiche.getValue("DateDeTitularisation"));
//		user.getExtendedAttributes().setValue("DateDEnregistrement", userFiche.getValue("DateDEnregistrement"));
//		user.getExtendedAttributes().setValue("MotifSortie", userFiche.getValue("MotifSortie"));
//		user.getExtendedAttributes().setValue("Banque", userFiche.getValue("Banque"));
//		user.getExtendedAttributes().setValue("NCompteBancaire", userFiche.getValue("NCompteBancaire"));
//		user.getExtendedAttributes().setValue("Salaire", userFiche.getValue("Salaire"));
//		
//		user.getExtendedAttributes().setValue("DroitMensuelle", userFiche.getValue("DroitMensuelle"));
//		user.getExtendedAttributes().setValue("SoldeAnneeEnCours", userFiche.getValue("SoldeAnneeEnCours"));
//		user.getExtendedAttributes().setValue("SoldeAnterieur", userFiche.getValue("SoldeAnterieur"));
//		user.getExtendedAttributes().setValue("SoldeConges", userFiche.getValue("SoldeConges"));
//		user.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation", userFiche.getValue("CongesPayesEnCoursDeValidation"));
//		user.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation", userFiche.getValue("CongesPayesEnCoursDeConsommation"));
//		user.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeValidation", userFiche.getValue("CongesSpeciauxEnCoursDeValidation"));
//		user.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeConsommation", userFiche.getValue("CongesSpeciauxEnCoursDeConsommation"));
//		user.getExtendedAttributes().setValue("CongesMaladieEnCoursDeValidation", userFiche.getValue("CongesMaladieEnCoursDeValidation"));
//		user.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation", userFiche.getValue("CongesMaladieEnCoursDeConsommation"));
//		user.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation", userFiche.getValue("CongesSansSoldeEnCoursDeValidation"));
//		user.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeConsommation", userFiche.getValue("CongesSansSoldeEnCoursDeConsommation"));
//		user.getExtendedAttributes().setValue("JourSEnCoursDeValidation", userFiche.getValue("JourSEnCoursDeValidation"));
//		user.getExtendedAttributes().setValue("joursEnCoursConsommation", userFiche.getValue("joursEnCoursConsommation"));
//		user.getExtendedAttributes().setValue("CongesPayesAnneeEnCours", userFiche.getValue("CongesPayesAnneeEnCours"));
//		user.getExtendedAttributes().setValue("CongesPayesN1", userFiche.getValue("CongesPayesN1"));
//		user.getExtendedAttributes().setValue("CongesPayesPris", userFiche.getValue("CongesPayesPris"));
//		user.getExtendedAttributes().setValue("CongesSpeciauxAnneeEnCours", userFiche.getValue("CongesSpeciauxAnneeEnCours"));
//		user.getExtendedAttributes().setValue("CongesSpeciauxN1", userFiche.getValue("CongesSpeciauxN1"));
//		user.getExtendedAttributes().setValue("CongesSpeciauxPris", userFiche.getValue("CongesSpeciauxPris"));
//		user.getExtendedAttributes().setValue("CongesMaladieAnneeEnCours", userFiche.getValue("CongesMaladieAnneeEnCours"));
//		user.getExtendedAttributes().setValue("CongesMaladieN1", userFiche.getValue("CongesMaladieN1"));
//		user.getExtendedAttributes().setValue("CongesMaladiePris", userFiche.getValue("CongesMaladiePris"));
//		user.getExtendedAttributes().setValue("CongesSansSoldeAnneeEnCours", userFiche.getValue("CongesSansSoldeAnneeEnCours"));
//		user.getExtendedAttributes().setValue("CongesSansSoldeN1", userFiche.getValue("CongesSansSoldeN1"));
//		user.getExtendedAttributes().setValue("CongesSansSoldePris", userFiche.getValue("CongesSansSoldePris"));
//		user.getExtendedAttributes().setValue("TotalJoursPris", userFiche.getValue("TotalJoursPris"));
		return user;
	}
	
	public IStorageResource fronUserToFiche(IUser user, IWorkflowModule workflowModule, IProjectModule projectModule, IOrganization projectOrganization){
		IStorageResource userFiche = getFicheUser(user, workflowModule, projectModule, projectOrganization);

		// user Extended fields
		userFiche.setValue("Organisation",user.getOrganization());

		// Informations personnelles

		// if they want the matricule to be auto generated
		userFiche.setValue("sys_Reference",user.getExtendedAttributes().getValue("Matricule"));
		// if they want the matricule to be a normal text field
		// userFiche.setValue("Matricule",user.getExtendedAttributes().getValue("Matricule"));
		userFiche.setValue("Title",user.getTitle());
		userFiche.setValue("FirstName",user.getFirstName());
		userFiche.setValue("LastName",user.getLastName());
		userFiche.setValue("MobilePhoneNumber",user.getMobilePhoneNumber());
		userFiche.setValue("Birthday",user.getBirthday());
		userFiche.setValue("CIN",user.getExtendedAttributes().getValue("CIN"));
		userFiche.setValue("NCNSS",user.getExtendedAttributes().getValue("NCNSS"));
		userFiche.setValue("Address1",user.getAddress1());
		userFiche.setValue("EtatCivil",user.getExtendedAttributes().getValue("EtatCivil"));
		userFiche.setValue("NombreEnfants",user.getExtendedAttributes().getValue("NombreEnfants"));

		// Informations login
		userFiche.setValue("Email",user.getEmail());
		// userFiche.setValue("MotDePasse",user.setPassword());


		// Informations bancaire
		userFiche.setValue("Banque",user.getExtendedAttributes().getValue("Banque"));
		userFiche.setValue("NCompteBancaire",user.getExtendedAttributes().getValue("NCompteBancaire"));
		userFiche.setValue("AgenceBancaire",user.getExtendedAttributes().getValue("AgenceBancaire"));


		// Affectation
		userFiche.setValue("DateDEmbauche",user.getExtendedAttributes().getValue("DateDEmbauche"));
		userFiche.setValue("ContractType",user.getContractType());
		userFiche.setValue("Societe",user.getExtendedAttributes().getValue("Societe"));
		userFiche.setValue("SecretGroupes",user.getExtendedAttributes().getValue("SecretGroupes"));
		userFiche.setValue("Groupes",user.getExtendedAttributes().getValue("Groupes"));
		userFiche.setValue("Direction",user.getExtendedAttributes().getValue("Direction"));
		userFiche.setValue("Departement",user.getExtendedAttributes().getValue("Departement"));
		userFiche.setValue("Fonction",user.getExtendedAttributes().getValue("Fonction"));
		userFiche.setValue("HierarchicalManager",user.getHierarchicalManager());
		userFiche.setValue("Exit",user.getExit());
		userFiche.setValue("MotifSortie",user.getExtendedAttributes().getValue("MotifSortie"));


		// Données salariales
		userFiche.setValue("TauxHoraire",user.getExtendedAttributes().getValue("TauxHoraire"));
		userFiche.setValue("SalaireDeBase",user.getExtendedAttributes().getValue("SalaireDeBase"));
		userFiche.setValue("SalaireBrutDH",user.getExtendedAttributes().getValue("SalaireBrut"));
		userFiche.setValue("Salaire",user.getExtendedAttributes().getValue("Salaire"));

		// Indemnités

		// Mutuelle
		userFiche.setValue("NomDeLaMutuelle",user.getExtendedAttributes().getValue("NomDeLaMutuelle"));
		userFiche.setValue("TauxDeLaMutuelle",user.getExtendedAttributes().getValue("TauxDeLaMutuelle"));

		// Retraite
		userFiche.setValue("NomRetraite",user.getExtendedAttributes().getValue("NomRetraite"));
		userFiche.setValue("TauxDeLaRetraire",user.getExtendedAttributes().getValue("TauxDeLaRetraire"));
		userFiche.setValue("NumeroAttribue",user.getExtendedAttributes().getValue("NumeroAttribue"));

		// Retraite complémentaire
		userFiche.setValue("NomDeLaRetraiteComplementaire",user.getExtendedAttributes().getValue("NomDeLaRetraiteComplementaire"));
		userFiche.setValue("TauxDeLaRetraiteComplementaire",user.getExtendedAttributes().getValue("TauxDeLaRetraiteComplementaire"));
		userFiche.setValue("MontantEpargneRetraite",user.getExtendedAttributes().getValue("MontantEpargneRetraite"));


		// Droit au congés
		userFiche.setValue("DroitMensuelle",user.getExtendedAttributes().getValue("DroitMensuelle"));
		userFiche.setValue("AbsenceAnneeEnCours",user.getExtendedAttributes().getValue("AbsenceAnneeEnCours"));
		userFiche.setValue("AbsenceAnterieure",user.getExtendedAttributes().getValue("AbsenceAnterieure"));
		userFiche.setValue("SoldeAnneeEnCours",user.getExtendedAttributes().getValue("SoldeAnneeEnCours"));
		userFiche.setValue("SoldeAnterieur",user.getExtendedAttributes().getValue("SoldeAnterieur"));
		userFiche.setValue("SoldeConges",user.getExtendedAttributes().getValue("SoldeConges"));
		userFiche.setValue("CongesPayesEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));
		userFiche.setValue("CongesPayesEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation"));
		userFiche.setValue("CongesSpeciauxEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation"));
		userFiche.setValue("CongesSpeciauxEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation"));
		userFiche.setValue("CongesMaladieEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation"));
		userFiche.setValue("CongesMaladieEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
		userFiche.setValue("CongesSansSoldeEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation"));
		userFiche.setValue("CongesSansSoldeEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation"));
		userFiche.setValue("JourSEnCoursDeValidation",user.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
		userFiche.setValue("joursEnCoursConsommation",user.getExtendedAttributes().getValue("joursEnCoursConsommation"));
		userFiche.setValue("CongesPayesAnneeEnCours",user.getExtendedAttributes().getValue("CongesPayesAnneeEnCours"));
		userFiche.setValue("CongesPayesN1",user.getExtendedAttributes().getValue("CongesPayesN1"));
		userFiche.setValue("CongesPayesPris",user.getExtendedAttributes().getValue("CongesPayesPris"));
		userFiche.setValue("CongesSpeciauxAnneeEnCours",user.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours"));
		userFiche.setValue("CongesSpeciauxN1",user.getExtendedAttributes().getValue("CongesSpeciauxN1"));
		userFiche.setValue("CongesSpeciauxPris",user.getExtendedAttributes().getValue("CongesSpeciauxPris"));
		userFiche.setValue("CongesMaladieAnneeEnCours",user.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours"));
		userFiche.setValue("CongesMaladieN1",user.getExtendedAttributes().getValue("CongesMaladieN1"));
		userFiche.setValue("CongesMaladiePris",user.getExtendedAttributes().getValue("CongesMaladiePris"));
		userFiche.setValue("CongesSansSoldeAnneeEnCours",user.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours"));
		userFiche.setValue("CongesSansSoldeN1",user.getExtendedAttributes().getValue("CongesSansSoldeN1"));
		userFiche.setValue("CongesSansSoldePris",user.getExtendedAttributes().getValue("CongesSansSoldePris"));
		userFiche.setValue("TotalJoursPris",user.getExtendedAttributes().getValue("TotalJoursPris"));
		
		
//		IStorageResource userFiche = getFicheUser(user, workflowModule, projectModule, projectOrganization);
//		userFiche.setValue("Organisation", user.getOrganization());
//		// cant change password
//		userFiche.setValue("FirstName",user.getFirstName());
//		userFiche.setValue("LastName",user.getLastName());
//		userFiche.setValue("Title",user.getTitle());
//		userFiche.setValue("MobilePhoneNumber",user.getMobilePhoneNumber());
//		userFiche.setValue("Email",user.getEmail());
//		userFiche.setValue("Address1",user.getAddress1());
//		userFiche.setValue("Sex",user.getSex());
//		userFiche.setValue("Birthday",user.getBirthday());
//		userFiche.setValue("Avatar", user.getAvatar());
//		userFiche.setValue("HierarchicalManager",user.getHierarchicalManager());
//		userFiche.setValue("Exit",user.getExit());
//		userFiche.setValue("ContractType",user.getContractType());
//		userFiche.setValue("CIN",user.getExtendedAttributes().getValue("CIN"));
//		userFiche.setValue("NCNSS",user.getExtendedAttributes().getValue("NCNSS"));
//		userFiche.setValue("Societe",user.getExtendedAttributes().getValue("Societe"));
//		userFiche.setValue("Matricule",user.getExtendedAttributes().getValue("Matricule"));
//		userFiche.setValue("EtatCivil",user.getExtendedAttributes().getValue("EtatCivil"));
//		userFiche.setValue("NombreEnfants",user.getExtendedAttributes().getValue("NombreEnfants"));
//		userFiche.setValue("Departement",user.getExtendedAttributes().getValue("Departement"));
//		userFiche.setValue("Fonction",user.getExtendedAttributes().getValue("Fonction"));
//		userFiche.setValue("CategorieDeSalaire",user.getExtendedAttributes().getValue("CategorieDeSalaire"));
//		userFiche.setValue("DateDEmbauche",user.getExtendedAttributes().getValue("DateDEmbauche"));
//		userFiche.setValue("DateDeTitularisation",user.getExtendedAttributes().getValue("DateDeTitularisation"));
//		userFiche.setValue("DateDEnregistrement",user.getExtendedAttributes().getValue("DateDEnregistrement"));
//		userFiche.setValue("MotifSortie",user.getExtendedAttributes().getValue("MotifSortie"));
//		userFiche.setValue("Banque",user.getExtendedAttributes().getValue("Banque"));
//		userFiche.setValue("NCompteBancaire",user.getExtendedAttributes().getValue("NCompteBancaire"));
//		userFiche.setValue("Salaire",user.getExtendedAttributes().getValue("Salaire"));
//		userFiche.setValue("DroitMensuelle",user.getExtendedAttributes().getValue("DroitMensuelle"));
//		userFiche.setValue("SoldeAnneeEnCours",user.getExtendedAttributes().getValue("SoldeAnneeEnCours"));
//		userFiche.setValue("SoldeAnterieur",user.getExtendedAttributes().getValue("SoldeAnterieur"));
//		userFiche.setValue("SoldeConges",user.getExtendedAttributes().getValue("SoldeConges"));
//		userFiche.setValue("CongesPayesEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));
//		userFiche.setValue("CongesPayesEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation"));
//		userFiche.setValue("CongesSpeciauxEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation"));
//		userFiche.setValue("CongesSpeciauxEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation"));
//		userFiche.setValue("CongesMaladieEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation"));
//		userFiche.setValue("CongesMaladieEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
//		userFiche.setValue("CongesSansSoldeEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation"));
//		userFiche.setValue("CongesSansSoldeEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation"));
//		userFiche.setValue("JourSEnCoursDeValidation",user.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
//		userFiche.setValue("joursEnCoursConsommation",user.getExtendedAttributes().getValue("joursEnCoursConsommation"));
//		userFiche.setValue("CongesPayesAnneeEnCours",user.getExtendedAttributes().getValue("CongesPayesAnneeEnCours"));
//		userFiche.setValue("CongesPayesN1",user.getExtendedAttributes().getValue("CongesPayesN1"));
//		userFiche.setValue("CongesPayesPris",user.getExtendedAttributes().getValue("CongesPayesPris"));
//		userFiche.setValue("CongesSpeciauxAnneeEnCours",user.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours"));
//		userFiche.setValue("CongesSpeciauxN1",user.getExtendedAttributes().getValue("CongesSpeciauxN1"));
//		userFiche.setValue("CongesSpeciauxPris",user.getExtendedAttributes().getValue("CongesSpeciauxPris"));
//		userFiche.setValue("CongesMaladieAnneeEnCours",user.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours"));
//		userFiche.setValue("CongesMaladieN1",user.getExtendedAttributes().getValue("CongesMaladieN1"));
//		userFiche.setValue("CongesMaladiePris",user.getExtendedAttributes().getValue("CongesMaladiePris"));
//		userFiche.setValue("CongesSansSoldeAnneeEnCours",user.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours"));
//		userFiche.setValue("CongesSansSoldeN1",user.getExtendedAttributes().getValue("CongesSansSoldeN1"));
//		userFiche.setValue("CongesSansSoldePris",user.getExtendedAttributes().getValue("CongesSansSoldePris"));
//		userFiche.setValue("TotalJoursPris",user.getExtendedAttributes().getValue("TotalJoursPris"));
//		userFiche.setValue("AbsenceAnneeEnCours",user.getExtendedAttributes().getValue("AbsenceAnneeEnCours"));
//		userFiche.setValue("AbsenceAnterieure",user.getExtendedAttributes().getValue("AbsenceAnterieure"));
		return userFiche;
	}
	
	public IStorageResource fromUserToFicheOnlyConges(IUser user, IWorkflowModule workflowModule, IProjectModule projectModule, IOrganization projectOrganization){
		IStorageResource userFiche = getFicheUser(user, workflowModule, projectModule, projectOrganization);
		userFiche.setValue("SoldeAnneeEnCours",user.getExtendedAttributes().getValue("SoldeAnneeEnCours"));
		userFiche.setValue("SoldeAnterieur",user.getExtendedAttributes().getValue("SoldeAnterieur"));
		userFiche.setValue("SoldeConges",user.getExtendedAttributes().getValue("SoldeConges"));
		userFiche.setValue("CongesPayesEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));
		userFiche.setValue("CongesPayesEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation"));
		userFiche.setValue("CongesSpeciauxEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation"));
		userFiche.setValue("CongesSpeciauxEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation"));
		userFiche.setValue("CongesMaladieEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation"));
		userFiche.setValue("CongesMaladieEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
		userFiche.setValue("CongesSansSoldeEnCoursDeValidation",user.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation"));
		userFiche.setValue("CongesSansSoldeEnCoursDeConsommation",user.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation"));
		userFiche.setValue("JourSEnCoursDeValidation",user.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
		userFiche.setValue("joursEnCoursConsommation",user.getExtendedAttributes().getValue("joursEnCoursConsommation"));
		userFiche.setValue("CongesPayesAnneeEnCours",user.getExtendedAttributes().getValue("CongesPayesAnneeEnCours"));
		userFiche.setValue("CongesPayesN1",user.getExtendedAttributes().getValue("CongesPayesN1"));
		userFiche.setValue("CongesPayesPris",user.getExtendedAttributes().getValue("CongesPayesPris"));
		userFiche.setValue("CongesSpeciauxAnneeEnCours",user.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours"));
		userFiche.setValue("CongesSpeciauxN1",user.getExtendedAttributes().getValue("CongesSpeciauxN1"));
		userFiche.setValue("CongesSpeciauxPris",user.getExtendedAttributes().getValue("CongesSpeciauxPris"));
		userFiche.setValue("CongesMaladieAnneeEnCours",user.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours"));
		userFiche.setValue("CongesMaladieN1",user.getExtendedAttributes().getValue("CongesMaladieN1"));
		userFiche.setValue("CongesMaladiePris",user.getExtendedAttributes().getValue("CongesMaladiePris"));
		userFiche.setValue("CongesSansSoldeAnneeEnCours",user.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours"));
		userFiche.setValue("CongesSansSoldeN1",user.getExtendedAttributes().getValue("CongesSansSoldeN1"));
		userFiche.setValue("CongesSansSoldePris",user.getExtendedAttributes().getValue("CongesSansSoldePris"));
		userFiche.setValue("TotalJoursPris",user.getExtendedAttributes().getValue("TotalJoursPris"));
		userFiche.setValue("AbsenceAnneeEnCours",user.getExtendedAttributes().getValue("AbsenceAnneeEnCours"));
		userFiche.setValue("AbsenceAnterieure",user.getExtendedAttributes().getValue("AbsenceAnterieure"));
		return userFiche;
	}
	
	private IStorageResource getFicheUser(IUser user,IWorkflowModule workflowModule,IProjectModule projectModule,IOrganization projectOrganization) {
		IStorageResource demandeurFiche = null;
		try {
			IContext context = workflowModule.getSysadminContext();
			IViewController controller = workflowModule.getViewController(context, IResource.class);
			IProject project = projectModule.getProject(context, "REFERENTIELCOMMUN", projectOrganization);
			ICatalog catalog = workflowModule.getCatalog(context, "REFERENTIEL",ICatalog.IType.STORAGE, project);
			IResourceDefinition definition = workflowModule.getResourceDefinition(context, catalog, "FicheCollaborateur");
			controller.addEqualsConstraint("Salarie", user);
			Collection<IStorageResource> demandeurFiches = controller.evaluate(definition);
			if (!demandeurFiches.isEmpty()){				
				demandeurFiche = demandeurFiches.iterator().next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return demandeurFiche;
	}
	
}