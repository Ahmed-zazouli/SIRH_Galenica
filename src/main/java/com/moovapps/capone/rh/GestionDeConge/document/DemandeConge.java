package com.moovapps.capone.rh.GestionDeConge.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.capone.rh.GestionDeConge.helpers.WorkingDaysNumberCalculator;
import com.moovapps.capone.rh.cummonHelpers.DateValidator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class DemandeConge extends BaseDocumentExtension {

	/*
	 * IWorkflowInstance document = null;
	 * IUser demandeur = null;
	 * 
	 * // SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY =
	 * 6, SATURDAY = 7
	 * ArrayList<Integer> joursOuvrables = new ArrayList<Integer>(Arrays.asList(2,
	 * 3, 4, 5, 6, 7));
	 * 
	 * // SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY =
	 * 6, SATURDAY = 7
	 * ArrayList<Integer> joursOuvres = new ArrayList<Integer>(Arrays.asList(2, 3,
	 * 4, 5, 6));
	 * 
	 * float SoldeAnneeEnCours = 0;
	 * float SoldeAnterieur = 0;
	 * float SoldeConges = 0;
	 * float CongesPayesEnCoursDeValidation = 0;
	 * float CongesPayesEnCoursDeConsommation = 0;
	 * float CongesSpeciauxEnCoursDeValidation = 0;
	 * float CongesSpeciauxEnCoursDeConsommation = 0;
	 * float CongesMaladieEnCoursDeValidation = 0;
	 * float CongesMaladieEnCoursDeConsommation = 0;
	 * float CongesSansSoldeEnCoursDeValidation = 0;
	 * float CongesSansSoldeEnCoursDeConsommation = 0;
	 * float JourSEnCoursDeValidation = 0;
	 * float joursEnCoursConsommation = 0;
	 * float CongesPayesAnneeEnCours = 0;
	 * float CongesPayesN1 = 0;
	 * float CongesPayesPris = 0;
	 * float CongesSpeciauxAnneeEnCours = 0;
	 * float CongesSpeciauxN1 = 0;
	 * float CongesSpeciauxPris = 0;
	 * float CongesMaladieAnneeEnCours = 0;
	 * float CongesMaladieN1 = 0;
	 * float CongesMaladiePris = 0;
	 * float CongesSansSoldeAnneeEnCours = 0;
	 * float CongesSansSoldeN1 = 0;
	 * float CongesSansSoldePris = 0;
	 * float TotalJoursPris = 0;
	 * 
	 * private Date getDateDebutConge() {
	 * Date dateDebutConge = null;
	 * if (document.getValue("DateDeDebut") != null) {
	 * dateDebutConge = (Date) document.getValue("DateDeDebut");
	 * }
	 * return dateDebutConge;
	 * }
	 * 
	 * private Date getDateFinConge() {
	 * Date dateFinConge = null;
	 * if (document.getValue("DateDeFin") != null) {
	 * dateFinConge = (Date) document.getValue("DateDeFin");
	 * }
	 * return dateFinConge;
	 * }
	 * 
	 * @Override
	 * public boolean onAfterLoad() {
	 * document = getWorkflowInstance();
	 * demandeur = setDemandeur();
	 * getDemandeurFields();
	 * setDemandeurCongesFields();
	 * document.setValue("DemandeurTotalJourEnCoursValidation",
	 * demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
	 * return super.onAfterLoad();
	 * }
	 * 
	 * @Override
	 * public void onPropertyChanged(IProperty property) {
	 * if (property.getName().equals("TypeDeConge")) {
	 * onCategorieCongesChange();
	 * } else if (property.getName().equals("DateDeDebut")) {
	 * document.setValue("NombreDeJoursDemandes", 0);
	 * document.setValue("totalJoursFerie", 0);
	 * document.setValue("TotalAbsence", 0);
	 * onDateDeDebutChange();
	 * } else if (property.getName().equals("DateDeFin")) {
	 * document.setValue("NombreDeJoursDemandes", 0);
	 * document.setValue("totalJoursFerie", 0);
	 * document.setValue("TotalAbsence", 0);
	 * onDateDeFinChange();
	 * } else if (property.getName().equals("DebutConge")) {
	 * onTrancheDebutChange();
	 * } else if (property.getName().equals("FinConge")) {
	 * onTrancheFinChange();
	 * } else if (property.getName().equals("TypeCongeExceptionnelle")) {
	 * onTypeCongeExceptionnelleChange();
	 * }
	 * super.onPropertyChanged(property);
	 * }
	 * 
	 * private void getDemandeurFields() {
	 * if (demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours") != null)
	 * {
	 * SoldeAnneeEnCours = (Float)
	 * demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("SoldeAnterieur") != null) {
	 * SoldeAnterieur = (Float)
	 * demandeur.getExtendedAttributes().getValue("SoldeAnterieur");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("SoldeConges") != null) {
	 * SoldeConges = (Float)
	 * demandeur.getExtendedAttributes().getValue("SoldeConges");
	 * }
	 * if
	 * (demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation")
	 * != null) {
	 * CongesPayesEnCoursDeValidation = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesPayesEnCoursDeConsommation") != null) {
	 * CongesPayesEnCoursDeConsommation = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation"
	 * );
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesSpeciauxEnCoursDeValidation") != null) {
	 * CongesSpeciauxEnCoursDeValidation = (Float)
	 * demandeur.getExtendedAttributes().getValue(
	 * "CongesSpeciauxEnCoursDeValidation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesSpeciauxEnCoursDeConsommation") != null) {
	 * CongesSpeciauxEnCoursDeConsommation = (Float)
	 * demandeur.getExtendedAttributes().getValue(
	 * "CongesSpeciauxEnCoursDeConsommation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesMaladieEnCoursDeValidation") != null) {
	 * CongesMaladieEnCoursDeValidation = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation"
	 * );
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesMaladieEnCoursDeConsommation") != null) {
	 * CongesMaladieEnCoursDeConsommation = (Float)
	 * demandeur.getExtendedAttributes().getValue(
	 * "CongesMaladieEnCoursDeConsommation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesSansSoldeEnCoursDeValidation") != null) {
	 * CongesSansSoldeEnCoursDeValidation = (Float)
	 * demandeur.getExtendedAttributes().getValue(
	 * "CongesSansSoldeEnCoursDeValidation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesSansSoldeEnCoursDeConsommation") != null) {
	 * CongesSansSoldeEnCoursDeConsommation = (Float)
	 * demandeur.getExtendedAttributes().getValue(
	 * "CongesSansSoldeEnCoursDeConsommation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") !=
	 * null) {
	 * JourSEnCoursDeValidation = (Float)
	 * demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") !=
	 * null) {
	 * joursEnCoursConsommation = (Float)
	 * demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesPayesAnneeEnCours") !=
	 * null) {
	 * CongesPayesAnneeEnCours = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesPayesAnneeEnCours");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesPayesN1") != null) {
	 * CongesPayesN1 = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesPayesN1");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesPayesPris") != null) {
	 * CongesPayesPris = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesPayesPris");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours")
	 * != null) {
	 * CongesSpeciauxAnneeEnCours = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSpeciauxN1") != null) {
	 * CongesSpeciauxN1 = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSpeciauxN1");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSpeciauxPris") != null)
	 * {
	 * CongesSpeciauxPris = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSpeciauxPris");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours")
	 * != null) {
	 * CongesMaladieAnneeEnCours = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesMaladieN1") != null) {
	 * CongesMaladieN1 = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesMaladieN1");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesMaladiePris") != null)
	 * {
	 * CongesMaladiePris = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesMaladiePris");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours")
	 * != null) {
	 * CongesSansSoldeAnneeEnCours = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSansSoldeN1") != null)
	 * {
	 * CongesSansSoldeN1 = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSansSoldeN1");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSansSoldePris") !=
	 * null) {
	 * CongesSansSoldePris = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSansSoldePris");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("TotalJoursPris") != null) {
	 * TotalJoursPris = (Float)
	 * demandeur.getExtendedAttributes().getValue("TotalJoursPris");
	 * }
	 * }
	 * 
	 * public void onTypeCongeExceptionnelleChange() {
	 * if (document.getValue("TypeCongeExceptionnelle") != null) {
	 * IStorageResource congeExceptionnelle = (IStorageResource)
	 * document.getValue("TypeCongeExceptionnelle");
	 * if (congeExceptionnelle.getValue("NbrJours") != null) {
	 * Number nbrJoursExceptionnelle =
	 * (Number)congeExceptionnelle.getValue("NbrJours");
	 * document.setValue("NombreDeJoursExceptionnelle", nbrJoursExceptionnelle);
	 * if (document.getValue("DateDeDebut") != null) {
	 * Date dateDebut = (Date) document.getValue("DateDeDebut");
	 * Calendar calFin = Calendar.getInstance();
	 * calFin.setTime(dateDebut);
	 * calFin.add(Calendar.DATE, nbrJoursExceptionnelle.intValue() - 1);
	 * document.setValue("DateDeFin", calFin.getTime());
	 * document.setValue("NombreDeJoursDemandes", 0);
	 * document.setValue("totalJoursFerie", 0);
	 * } else {
	 * setDemandeurCongesFields();
	 * }
	 * }
	 * } else {
	 * setDemandeurCongesFields();
	 * }
	 * }
	 * 
	 * private void onDateDeDebutChange() {
	 * calculateConges();
	 * }
	 * 
	 * private void onDateDeFinChange() {
	 * calculateConges();
	 * }
	 * 
	 * private void onTrancheDebutChange() {
	 * calculateConges();
	 * }
	 * 
	 * private void onTrancheFinChange() {
	 * calculateConges();
	 * }
	 * 
	 * public IUser setDemandeur() {
	 * if (document.getValue("Demandeur") == null) {
	 * document.setValue("Demandeur", document.getCreatedBy());
	 * }
	 * demandeur = (IUser) document.getValue("Demandeur");
	 * document.setValue("SupHierarchique", demandeur.getHierarchicalManager());
	 * document.setValue("Societe",
	 * demandeur.getExtendedAttributes().getValue("Societe"));
	 * setSocieteFields();
	 * return demandeur;
	 * }
	 * 
	 * public void setSocieteFields() {
	 * String societeName = null;
	 * if (document.getValue("Societe") != null) {
	 * societeName = (String) document.getValue("Societe");
	 * }
	 * IStorageResource societe = null;
	 * if (societeName != null) {
	 * societe = getSociete(societeName);
	 * }
	 * document.setValue("ResponsableRH", societe != null ?
	 * societe.getValue("ResponsableRH") : null);
	 * }
	 * 
	 * public void onCategorieCongesChange() {
	 * resetForm();
	 * }
	 * 
	 * public void resetForm() {
	 * document.setValue("DateDeDebut", null);
	 * document.setValue("DateDeFin", null);
	 * document.setValue("NombreDeJoursDemandes", 0);
	 * document.setValue("totalJoursFerie", 0);
	 * document.setValue("TotalAbsence", 0);
	 * document.setValue("DebutConge", "TJ");
	 * document.setValue("FinConge", "TJ");
	 * 
	 * SoldeAnneeEnCours = 0;
	 * SoldeAnterieur = 0;
	 * SoldeConges = 0;
	 * CongesPayesEnCoursDeValidation = 0;
	 * CongesPayesEnCoursDeConsommation = 0;
	 * CongesSpeciauxEnCoursDeValidation = 0;
	 * CongesSpeciauxEnCoursDeConsommation = 0;
	 * CongesMaladieEnCoursDeValidation = 0;
	 * CongesMaladieEnCoursDeConsommation = 0;
	 * CongesSansSoldeEnCoursDeValidation = 0;
	 * CongesSansSoldeEnCoursDeConsommation = 0;
	 * JourSEnCoursDeValidation = 0;
	 * joursEnCoursConsommation = 0;
	 * CongesPayesAnneeEnCours = 0;
	 * CongesPayesN1 = 0;
	 * CongesPayesPris = 0;
	 * CongesSpeciauxAnneeEnCours = 0;
	 * CongesSpeciauxN1 = 0;
	 * CongesSpeciauxPris = 0;
	 * CongesMaladieAnneeEnCours = 0;
	 * CongesMaladieN1 = 0;
	 * CongesMaladiePris = 0;
	 * CongesSansSoldeAnneeEnCours = 0;
	 * CongesSansSoldeN1 = 0;
	 * CongesSansSoldePris = 0;
	 * TotalJoursPris = 0;
	 * 
	 * if (demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours") != null)
	 * {
	 * SoldeAnneeEnCours = (Float)
	 * demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("SoldeAnterieur") != null) {
	 * SoldeAnterieur = (Float)
	 * demandeur.getExtendedAttributes().getValue("SoldeAnterieur");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("SoldeConges") != null) {
	 * SoldeConges = (Float)
	 * demandeur.getExtendedAttributes().getValue("SoldeConges");
	 * }
	 * if
	 * (demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation")
	 * != null) {
	 * CongesPayesEnCoursDeValidation = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesPayesEnCoursDeConsommation") != null) {
	 * CongesPayesEnCoursDeConsommation = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation"
	 * );
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesSpeciauxEnCoursDeValidation") != null) {
	 * CongesSpeciauxEnCoursDeValidation = (Float)
	 * demandeur.getExtendedAttributes().getValue(
	 * "CongesSpeciauxEnCoursDeValidation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesSpeciauxEnCoursDeConsommation") != null) {
	 * CongesSpeciauxEnCoursDeConsommation = (Float)
	 * demandeur.getExtendedAttributes().getValue(
	 * "CongesSpeciauxEnCoursDeConsommation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesMaladieEnCoursDeValidation") != null) {
	 * CongesMaladieEnCoursDeValidation = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation"
	 * );
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesMaladieEnCoursDeConsommation") != null) {
	 * CongesMaladieEnCoursDeConsommation = (Float)
	 * demandeur.getExtendedAttributes().getValue(
	 * "CongesMaladieEnCoursDeConsommation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesSansSoldeEnCoursDeValidation") != null) {
	 * CongesSansSoldeEnCoursDeValidation = (Float)
	 * demandeur.getExtendedAttributes().getValue(
	 * "CongesSansSoldeEnCoursDeValidation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue(
	 * "CongesSansSoldeEnCoursDeConsommation") != null) {
	 * CongesSansSoldeEnCoursDeConsommation = (Float)
	 * demandeur.getExtendedAttributes().getValue(
	 * "CongesSansSoldeEnCoursDeConsommation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") !=
	 * null) {
	 * JourSEnCoursDeValidation = (Float)
	 * demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") !=
	 * null) {
	 * joursEnCoursConsommation = (Float)
	 * demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesPayesAnneeEnCours") !=
	 * null) {
	 * CongesPayesAnneeEnCours = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesPayesAnneeEnCours");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesPayesN1") != null) {
	 * CongesPayesN1 = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesPayesN1");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesPayesPris") != null) {
	 * CongesPayesPris = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesPayesPris");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours")
	 * != null) {
	 * CongesSpeciauxAnneeEnCours = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSpeciauxN1") != null) {
	 * CongesSpeciauxN1 = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSpeciauxN1");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSpeciauxPris") != null)
	 * {
	 * CongesSpeciauxPris = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSpeciauxPris");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours")
	 * != null) {
	 * CongesMaladieAnneeEnCours = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesMaladieN1") != null) {
	 * CongesMaladieN1 = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesMaladieN1");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesMaladiePris") != null)
	 * {
	 * CongesMaladiePris = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesMaladiePris");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours")
	 * != null) {
	 * CongesSansSoldeAnneeEnCours = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSansSoldeN1") != null)
	 * {
	 * CongesSansSoldeN1 = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSansSoldeN1");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("CongesSansSoldePris") !=
	 * null) {
	 * CongesSansSoldePris = (Float)
	 * demandeur.getExtendedAttributes().getValue("CongesSansSoldePris");
	 * }
	 * if (demandeur.getExtendedAttributes().getValue("TotalJoursPris") != null) {
	 * TotalJoursPris = (Float)
	 * demandeur.getExtendedAttributes().getValue("TotalJoursPris");
	 * }
	 * 
	 * if (document.getValue("TypeDeConge").equals("CM")) {
	 * document.setValue("ValidateurConge", null);
	 * }
	 * }
	 * 
	 * public IStorageResource getSociete(String societeName) {
	 * IStorageResource societe = null;
	 * try {
	 * IContext context = getWorkflowModule().getSysadminContext();
	 * IViewController controller = getWorkflowModule().getViewController(context,
	 * IResource.class);
	 * IProject project = getProjectModule().getProject(context,
	 * "REFERENTIELCOMMUN",
	 * getWorkflowInstance().getCatalog().getProject().getOrganization());
	 * ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", 4,
	 * project);
	 * IResourceDefinition definition =
	 * getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
	 * controller.addEqualsConstraint("sys_Title", societeName);
	 * Collection<IStorageResource> societes = controller.evaluate(definition);
	 * if (!societes.isEmpty()) {
	 * societe = societes.iterator().next();
	 * }
	 * } catch (Exception e) {
	 * e.printStackTrace();
	 * }
	 * return societe;
	 * }
	 * 
	 * public void setDemandeurCongesFields() {
	 * float nbrJoursDemandes = 0;
	 * if (document.getValue("NombreDeJoursDemandes") != null) {
	 * nbrJoursDemandes = ((Number)
	 * document.getValue("NombreDeJoursDemandes")).floatValue();
	 * }
	 * float nbrJoursSpeciaux = 0;
	 * if (document.getValue("NombreDeJoursExceptionnelle") != null) {
	 * nbrJoursSpeciaux = ((Number)
	 * document.getValue("NombreDeJoursExceptionnelle")).floatValue();
	 * }
	 * 
	 * // set rubrique congés payés info
	 * document.setValue("demandeurSoldeCongesPayesReel", SoldeConges);
	 * document.setValue("demandeurSoldeAnterieur", SoldeAnterieur);
	 * document.setValue("demandeurCongesPris", CongesPayesPris);
	 * document.setValue("demandeurCongesValidesAConsommer",
	 * joursEnCoursConsommation);
	 * document.setValue("demandeurSoldeCongesPayesProjete", SoldeConges -
	 * joursEnCoursConsommation - JourSEnCoursDeValidation - nbrJoursDemandes);
	 * document.setValue("demandeurCongesEnCoursDeValidation",
	 * JourSEnCoursDeValidation + nbrJoursDemandes);
	 * 
	 * // set rubrique congés spéciaux info
	 * document.setValue("demandeurCongesSpeciauxPris", CongesSpeciauxPris);
	 * document.setValue("demandeurCongesSpeciauxN1", CongesSpeciauxN1);
	 * document.setValue("demandeurCongesSpeciauxEnCoursDeConsommation",
	 * CongesSpeciauxEnCoursDeConsommation);
	 * // document.setValue("demandeurCongesSpeciauxEnCoursDeValidation",
	 * // CongesSpeciauxEnCoursDeValidation + nbrJoursSpeciaux);
	 * 
	 * // set rubrique congés maladie info
	 * document.setValue("demandeurCongesMaladiePris", CongesMaladiePris);
	 * document.setValue("demandeurCongesMaladieN1", CongesMaladieN1);
	 * document.setValue("demandeurCongesMaladieEnCoursDeConsommation",
	 * CongesMaladieEnCoursDeConsommation);
	 * 
	 * // set rubrique congés sans solde info
	 * document.setValue("demandeurCongesSansSoldePris", CongesSansSoldePris);
	 * document.setValue("demandeurCongesSansSoldeN1", CongesSansSoldeN1);
	 * document.setValue("demandeurCongesSansSoldeEnCoursDeValidation",
	 * CongesSansSoldeEnCoursDeValidation);
	 * 
	 * }
	 * 
	 * public void calculateConges() {
	 * String categorieConges = (String) document.getValue("TypeDeConge");
	 * if (categorieConges.equals("CN")) {
	 * calculateCongesPayes();
	 * } else if (categorieConges.equals("CE")) {
	 * calculateCongesSpeciaux();
	 * } else if (categorieConges.equals("CM")) {
	 * calculateCongesMaladie();
	 * } else if (categorieConges.equals("SS")) {
	 * calculateCongesSansSolde();
	 * }
	 * if (validateForm()) {
	 * setDemandeurCongesFields();
	 * } else {
	 * resetForm();
	 * }
	 * }
	 * 
	 * public boolean validateForm() {
	 * float nbrJoursDemandes = 0;
	 * float soldeConges = 0;
	 * if (document.getValue("NombreDeJoursDemandes") != null &&
	 * !document.getValue("NombreDeJoursDemandes").equals(0)) {
	 * nbrJoursDemandes = (Float) document.getValue("NombreDeJoursDemandes");
	 * }
	 * if (!validateDates(document.getValue("DateDeDebut"),
	 * document.getValue("DateDeFin"))) {
	 * return false;
	 * }
	 * if (document.getValue("TypeDeConge").equals("CN") &&
	 * !validateSolde(nbrJoursDemandes, SoldeConges)) {
	 * return false;
	 * }
	 * return true;
	 * }
	 * 
	 * @Override
	 * public boolean onBeforeSubmit(IAction action) {
	 * if (action.getName().equals("Envoyer")) {
	 * if (!validateForm()) {
	 * return false;
	 * }
	 * setDemandeurFields();
	 * reinitializeNextForm();
	 * }
	 * return super.onBeforeSubmit(action);
	 * }
	 * 
	 * private void setDemandeurFields() {
	 * float nbrJoursDemandes = 0;
	 * if (document.getValue("NombreDeJoursDemandes") != null) {
	 * nbrJoursDemandes = ((Number)
	 * document.getValue("NombreDeJoursDemandes")).floatValue();
	 * }
	 * float nbrJoursSpeciaux = 0;
	 * if (document.getValue("NombreDeJoursExceptionnelle") != null) {
	 * nbrJoursSpeciaux = ((Number)
	 * document.getValue("NombreDeJoursExceptionnelle")).floatValue();
	 * }
	 * 
	 * String categorieConges = (String) document.getValue("TypeDeConge");
	 * if (categorieConges.equals("CN")) {
	 * setDemandeurCongesPayesInfo(nbrJoursDemandes);
	 * } else if (categorieConges.equals("CE")) {
	 * setDemandeurCongesSpeciauxInfo(nbrJoursSpeciaux);
	 * } else if (categorieConges.equals("CM")) {
	 * setDemandeurCongesMaladieInfo(nbrJoursDemandes);
	 * } else if (categorieConges.equals("SS")) {
	 * setDemandeurCongesSansSoldeInfo(nbrJoursDemandes);
	 * }
	 * document.setValue("DemandeurTotalJourEnCoursValidation",
	 * demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
	 * IStorageResource userFiche = new
	 * UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(demandeur,
	 * getWorkflowModule(), getProjectModule(),
	 * getWorkflowInstance().getCatalog().getProject().getOrganization());
	 * userFiche.save(getWorkflowModule().getSysadminContext());
	 * demandeur.save(getWorkflowModule().getSysadminContext());
	 * }
	 * 
	 * private void setDemandeurCongesPayesInfo(float nbrJoursDemandes) {
	 * demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation",
	 * CongesPayesEnCoursDeValidation + nbrJoursDemandes);
	 * demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",
	 * JourSEnCoursDeValidation + nbrJoursDemandes);
	 * }
	 * 
	 * private void setDemandeurCongesSpeciauxInfo(float nbrJoursDemandes) {
	 * demandeur.getExtendedAttributes().setValue(
	 * "CongesSpeciauxEnCoursDeValidation", CongesSpeciauxEnCoursDeValidation +
	 * nbrJoursDemandes);
	 * demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",
	 * JourSEnCoursDeValidation + nbrJoursDemandes);
	 * document.setValue("demandeurCongesSpeciauxEnCoursDeValidation",
	 * CongesSpeciauxEnCoursDeValidation + nbrJoursDemandes);
	 * }
	 * 
	 * private void setDemandeurCongesMaladieInfo(float nbrJoursDemandes) {
	 * demandeur.getExtendedAttributes().setValue(
	 * "CongesMaladieEnCoursDeConsommation", CongesMaladieEnCoursDeConsommation +
	 * nbrJoursDemandes);
	 * demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",
	 * joursEnCoursConsommation + nbrJoursDemandes);
	 * document.setValue("demandeurCongesMaladieEnCoursDeConsommation",
	 * demandeur.getExtendedAttributes().getValue(
	 * "CongesMaladieEnCoursDeConsommation"));
	 * }
	 * 
	 * private void setDemandeurCongesSansSoldeInfo(float nbrJoursDemandes) {
	 * demandeur.getExtendedAttributes().setValue(
	 * "CongesSansSoldeEnCoursDeValidation", CongesSansSoldeEnCoursDeValidation +
	 * nbrJoursDemandes);
	 * demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",
	 * JourSEnCoursDeValidation + nbrJoursDemandes);
	 * document.setValue("demandeurCongesSansSoldeEnCoursDeValidation",
	 * CongesSansSoldeEnCoursDeValidation + nbrJoursDemandes);
	 * }
	 * 
	 * public void reinitializeNextForm() {
	 * document.setValue("isDateCongesSuggested", false);
	 * document.setValue("suggestedDateDebut", null);
	 * document.setValue("PremiereValidationCommentaire", null);
	 * document.setValue("suggestedDateFin", null);
	 * document.setValue("SuggestedDebutConge", "TJ");
	 * document.setValue("SuggestedFinConge", "TJ");
	 * document.setValue("suggestedNombreDeJoursDeConges", 0);
	 * document.save(getWorkflowModule().getSysadminContext());
	 * }
	 * 
	 * public boolean validateDates(Object startDate, Object endDate) {
	 * Date dateDebut = null;
	 * Date dateFin = null;
	 * if (startDate != null) {
	 * dateDebut = (Date) startDate;
	 * }
	 * if (endDate != null) {
	 * dateFin = (Date) endDate;
	 * }
	 * 
	 * if (dateDebut != null) {
	 * if (new DateValidator(getResourceController()).isDateBeforeDate(dateDebut,
	 * new Date())) {
	 * onInvalidForm("La date de debut de congés doit être aprés la date aujourd'hui"
	 * );
	 * return false;
	 * }
	 * }
	 * if (dateFin != null) {
	 * if (new DateValidator(getResourceController()).isDateBeforeDate(dateFin, new
	 * Date())) {
	 * onInvalidForm("La date de fin de congés doit être aprés la date aujourd'hui"
	 * );
	 * return false;
	 * }
	 * }
	 * 
	 * if (dateDebut != null && dateFin != null) {
	 * if (new DateValidator(getResourceController()).isDateAfterDate(dateDebut,
	 * dateFin)) {
	 * onInvalidForm("La date de fin doit être aprés la date de début");
	 * return false;
	 * }
	 * }
	 * 
	 * if (!isDatesOvelapped(dateDebut, dateFin)) {
	 * document.setValue("DateDeDebut", null);
	 * onInvalidForm();
	 * return false;
	 * }
	 * return true;
	 * }
	 * 
	 * private Collection<IWorkflowInstance> getAllDemandeurDemandes() {
	 * Collection<IWorkflowInstance> collection = null;
	 * try {
	 * IContext sysContext = getWorkflowModule().getSysadminContext();
	 * IOrganization organization = getDirectoryModule().getOrganization(sysContext,
	 * "DefaultOrganization");
	 * IProject project = getProjectModule().getProject(sysContext, "Capone",
	 * organization);
	 * ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
	 * IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext,
	 * catalog, "GestionDeConges");
	 * IViewController controller =
	 * getWorkflowModule().getViewController(sysContext);
	 * controller.addEqualsConstraint("Demandeur", demandeur);
	 * controller.addNotInConstraint("DocumentState", new
	 * ArrayList<String>(Arrays.asList("En cours", "Refusé", "Annulé")));
	 * controller.addNotEqualsConstraint("sys_Reference",
	 * document.getValue("sys_Reference"));
	 * collection = controller.evaluate(w);
	 * return collection;
	 * } catch (Exception e) {
	 * e.printStackTrace();
	 * }
	 * return null;
	 * }
	 * 
	 * public boolean isDatesOvelapped(Date dateDebut, Date dateFin) {
	 * SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
	 * Collection<IWorkflowInstance> allDemandes = getAllDemandeurDemandes();
	 * for (IWorkflowInstance demande : allDemandes) {
	 * Date demandeDebut = (Date) demande.getValue("DateDeDebut");
	 * Date demandeFin = (Date) demande.getValue("DateDeFin");
	 * if (dateDebut != null && dateFin != null) {
	 * if (new
	 * DateValidator(getResourceController()).isTwoDatesOverlapped(dateDebut,
	 * dateFin, demandeDebut, demandeFin)) {
	 * getResourceController().
	 * alert("Vous avez déja séléctionner cette période dans autre demande.\nLa période séléctionnée est "
	 * + simpleFormat.format(demandeDebut) + " jusqu’à " +
	 * simpleFormat.format(demandeFin));
	 * return false;
	 * }
	 * } else if (dateDebut != null) {
	 * if (new DateValidator(getResourceController())
	 * .isDateInsideRange(dateDebut, demandeDebut, demandeFin)) {
	 * onInvalidForm();
	 * getResourceController().
	 * alert("La date de début sélectionnée et entre une période dans une autre demande.\nLa période est "
	 * + simpleFormat.format(demandeDebut) + " jusqu’à " +
	 * simpleFormat.format(demandeFin));
	 * return false;
	 * }
	 * } else if (dateFin != null) {
	 * if (new DateValidator(getResourceController())
	 * .isDateInsideRange(dateFin, demandeDebut, demandeFin)) {
	 * onInvalidForm();
	 * getResourceController().
	 * alert("La date de fin sélectionnée et entre une période dans une autre demande.\nLa période est "
	 * + simpleFormat.format(demandeDebut) + " jusqu’à " +
	 * simpleFormat.format(demandeFin));
	 * return false;
	 * }
	 * }
	 * 
	 * }
	 * return true;
	 * }
	 * 
	 * public boolean validateSolde(float nbrJoursDemandes, float soldeConges) {
	 * if (soldeConges <= 0 || soldeConges < nbrJoursDemandes) {
	 * if (soldeConges <= 0) {
	 * getResourceController().alert("Vous n'avez pas un solde de congés");
	 * }
	 * if (soldeConges < nbrJoursDemandes) {
	 * getResourceController().alert("Le nombre de jours demandés (" +
	 * nbrJoursDemandes + ") doit être inférieur ou égal au solde congés (" +
	 * soldeConges + ")");
	 * }
	 * onInvalidForm();
	 * document.setValue("NombreDeJoursDemandes", 0);
	 * return false;
	 * }
	 * return true;
	 * }
	 * 
	 * public void onInvalidForm(String... message) {
	 * document.setValue("DateDeDebut", null);
	 * document.setValue("DateDeFin", null);
	 * document.setValue("DemandeurSoldeConge", SoldeConges);
	 * document.setValue("DemandeurTotalJourEnCoursValidation",
	 * JourSEnCoursDeValidation);
	 * document.setValue("NombreDeJoursDemandes", 0);
	 * if (message.length > 0) {
	 * getResourceController().alert(message[0]);
	 * }
	 * }
	 * 
	 * public void calculateCongesPayes() {
	 * Date dateDebutConge = getDateDebutConge();
	 * Date dateFinConge = getDateFinConge();
	 * if (dateDebutConge != null && dateFinConge != null) {
	 * 
	 * HashMap<String, Object> calculResult = new
	 * WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables,
	 * joursOuvres).calculateV2(dateDebutConge, dateFinConge);
	 * 
	 * boolean isStartDayAWorkingDay = (boolean)
	 * calculResult.get("isStartDayAWorkingDay");
	 * boolean isEndDayAWorkingDay = (boolean)
	 * calculResult.get("isEndDayAWorkingDay");
	 * float totalJoursFerieInPeriod = (Float)
	 * calculResult.get("totalJoursFerieInPeriod");
	 * float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
	 * 
	 * if (!isStartDayAWorkingDay) {
	 * document.setValue("DebutConge", "TJ");
	 * }
	 * if (!isEndDayAWorkingDay) {
	 * document.setValue("FinConge", "TJ");
	 * }
	 * document.setValue("isStartDayAWorkingDay", isStartDayAWorkingDay);
	 * document.setValue("isEndDayAWorkingDay", isEndDayAWorkingDay);
	 * document.setValue("totalJoursFerie", totalJoursFerieInPeriod);
	 * 
	 * // The method "calculateTrancheMinusValue" calculate the value of
	 * // the tranches
	 * // example => (if the tranche debut is "Demi journée" and tranche
	 * // fin est "Toute la journée" it returns "-0.5")
	 * nbrJoursDemande -= calculateTrancheMinusValue(dateDebutConge, dateFinConge);
	 * 
	 * document.setValue("NombreDeJoursDemandes", nbrJoursDemande);
	 * } else {
	 * setDemandeurCongesFields();
	 * }
	 * }
	 * 
	 * private float calculateTrancheMinusValue(Date dateDebutConge, Date
	 * dateFinConge) {
	 * float heureDebutFinMinus = 0;
	 * if (dateDebutConge.equals(dateFinConge)) {
	 * if (document.getValue("DebutConge").equals("DJ")) {
	 * heureDebutFinMinus += .5;
	 * }
	 * } else {
	 * if (document.getValue("DebutConge").equals("DJ")) {
	 * heureDebutFinMinus += .5;
	 * }
	 * if (document.getValue("FinConge").equals("DJ")) {
	 * heureDebutFinMinus += .5;
	 * }
	 * }
	 * return heureDebutFinMinus;
	 * }
	 * 
	 * public void calculateCongesSpeciaux() {
	 * onTypeCongeExceptionnelleChange();
	 * }
	 * 
	 * public void calculateCongesMaladie() {
	 * Date dateDebutConge = getDateDebutConge();
	 * Date dateFinConge = getDateFinConge();
	 * if (dateDebutConge != null && dateFinConge != null) {
	 * 
	 * HashMap<String, Object> calculResult = new
	 * WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables,
	 * joursOuvres).calculateV2(dateDebutConge, dateFinConge);
	 * 
	 * boolean isStartDayAWorkingDay = (boolean)
	 * calculResult.get("isStartDayAWorkingDay");
	 * boolean isEndDayAWorkingDay = (boolean)
	 * calculResult.get("isEndDayAWorkingDay");
	 * float totalJoursFerieInPeriod = (Float)
	 * calculResult.get("totalJoursFerieInPeriod");
	 * float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
	 * 
	 * if (!isStartDayAWorkingDay) {
	 * document.setValue("DebutConge", "TJ");
	 * }
	 * if (!isEndDayAWorkingDay) {
	 * document.setValue("FinConge", "TJ");
	 * }
	 * document.setValue("isStartDayAWorkingDay", isStartDayAWorkingDay);
	 * document.setValue("isEndDayAWorkingDay", isEndDayAWorkingDay);
	 * document.setValue("totalJoursFerie", totalJoursFerieInPeriod);
	 * 
	 * // The method "calculateTrancheMinusValue" calculate the value of
	 * // the tranches
	 * // example => (if the tranche debut is "Demi journée" and tranche
	 * // fin est "Toute la journée" it returns "-0.5")
	 * nbrJoursDemande -= calculateTrancheMinusValue(dateDebutConge, dateFinConge);
	 * 
	 * document.setValue("NombreDeJoursDemandes", nbrJoursDemande);
	 * } else {
	 * setDemandeurCongesFields();
	 * }
	 * }
	 * 
	 * public void calculateCongesSansSolde() {
	 * Date dateDebutConge = getDateDebutConge();
	 * Date dateFinConge = getDateFinConge();
	 * if (dateDebutConge != null && dateFinConge != null) {
	 * 
	 * HashMap<String, Object> calculResult = new
	 * WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables,
	 * joursOuvres).calculateV2(dateDebutConge, dateFinConge);
	 * 
	 * boolean isStartDayAWorkingDay = (boolean)
	 * calculResult.get("isStartDayAWorkingDay");
	 * boolean isEndDayAWorkingDay = (boolean)
	 * calculResult.get("isEndDayAWorkingDay");
	 * float totalJoursFerieInPeriod = (Float)
	 * calculResult.get("totalJoursFerieInPeriod");
	 * float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
	 * 
	 * if (!isStartDayAWorkingDay) {
	 * document.setValue("DebutConge", "TJ");
	 * }
	 * if (!isEndDayAWorkingDay) {
	 * document.setValue("FinConge", "TJ");
	 * }
	 * document.setValue("isStartDayAWorkingDay", isStartDayAWorkingDay);
	 * document.setValue("isEndDayAWorkingDay", isEndDayAWorkingDay);
	 * document.setValue("totalJoursFerie", totalJoursFerieInPeriod);
	 * 
	 * // The method "calculateTrancheMinusValue" calculate the value of
	 * // the tranches
	 * // example => (if the tranche debut is "Demi journée" and tranche
	 * // fin est "Toute la journée" it returns "-0.5")
	 * nbrJoursDemande -= calculateTrancheMinusValue(dateDebutConge, dateFinConge);
	 * 
	 * document.setValue("NombreDeJoursDemandes", nbrJoursDemande);
	 * // document.setValue("demandeurCongesSansSoldeEnCoursDeValidation",
	 * // (Float)document.getValue("demandeurCongesSansSoldeEnCoursDeValidation")
	 * // + nbrJoursDemande);
	 * } else {
	 * setDemandeurCongesFields();
	 * }
	 * }
	 * 
	 * }
	 */

	public float nbrJoursDejaDemandes = 0;
	public IUser demandeur = null;
	public IWorkflowInstance document = null;
	IStorageResource societe = null;
	SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
	public String dateDebutField = "DateDeDebut";
	public String dateFinField = "DateDeFin";
	public String debutCongeField = "DebutConge";
	public String finCongeField = "FinConge";
	public String nombreDeJoursDeCongesField = "NombreDeJoursDemandes";
	public String isStartDayAWorkingDayField = "isStartDayAWorkingDay";
	public String isEndDayAWorkingDayField = "isEndDayAWorkingDay";
	public String totalJoursFerieField = "totalJoursFerie";
	boolean saturdayIsAWorkingDay = true;

	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		if (document.getValue("Demandeur") == null) {
			demandeur = document.getCreatedBy();
			document.setValue("Demandeur", demandeur);
		} else {
			demandeur = (IUser) document.getValue("Demandeur");
		}
		IUser hierarchicalManager = demandeur.getHierarchicalManager();
		if (hierarchicalManager != null && document.getValue("SupHierarchique") == null) {
			document.setValue("SupHierarchique", hierarchicalManager);
		}
		/*
		 * String societeName = null;
		 * if(document.getValue("Societe")!=null){
		 * societeName = (String)document.getValue("Societe");
		 * }
		 */
		IStorageResource societe = (IStorageResource) document.getValue("Societe2");
		// IStorageResource test = (IStorageResource)
		// demandeur.getExtendedAttributes().getValue("SocieteDonnee");
		// document.setValue("Societe2", test);
		document.setValue("ResponsableRh2", societe.getValue("ResponsableRH"));

		// document.setValue("ResponsableRH",societeName!=null ?
		// getSociete(societeName)!=null ?
		// getSociete(societeName).getValue("ResponsableRH"):null : null);
		if (document.getValue("NombreDeJoursDemandes") != null) {
			nbrJoursDejaDemandes = (Float) document.getValue("NombreDeJoursDemandes");
		}
		float documentTotalNbrJoursDeConges = 0;
		float demandeurSoldeConges = 0;
		if (demandeur.getExtendedAttributes().getValue("SoldeConges") != null) {
			demandeurSoldeConges = (Float) demandeur.getExtendedAttributes().getValue("SoldeConges");
			documentTotalNbrJoursDeConges = (Float) demandeur.getExtendedAttributes().getValue("SoldeConges")
					- nbrJoursDejaDemandes;
		}
		document.setValue("DemandeurSoldeConge", documentTotalNbrJoursDeConges);
		document.setValue("SoldeAnterieur", demandeurSoldeConges);
		float demandeurSoldeAnneeMoinsUn = 0;
		if (demandeur.getExtendedAttributes().getValue("SoldeRecupere") != null) {
			demandeurSoldeAnneeMoinsUn = (Float) demandeur.getExtendedAttributes().getValue("SoldeRecupere");
		}
		document.setValue("SoldeAnnee1", demandeurSoldeAnneeMoinsUn);
		float documentTotalEnCoursValidation = 0;
		if (demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") != null) {
			documentTotalEnCoursValidation = (Float) demandeur.getExtendedAttributes()
					.getValue("JourSEnCoursDeValidation") + nbrJoursDejaDemandes;
		}
		document.setValue("DemandeurTotalJourEnCoursValidation", documentTotalEnCoursValidation);

		float documentTotalEnCoursConsommation = 0;
		if (demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") != null) {
			documentTotalEnCoursConsommation = (Float) demandeur.getExtendedAttributes()
					.getValue("joursEnCoursConsommation");
		}
		document.setValue("CongesEnCoursDeConsommation", documentTotalEnCoursConsommation);

		float demandeurTotalJoursPris = 0;
		if (demandeur.getExtendedAttributes().getValue("TotalJoursPris") != null) {
			demandeurTotalJoursPris = (Float) demandeur.getExtendedAttributes().getValue("TotalJoursPris");
		}
		document.setValue("demandeurTotalJoursPris", demandeurTotalJoursPris);

		// validateForm();
		return superOnAfterLoad();
	}

	public boolean superOnAfterLoad() {
		return super.onAfterLoad();
	}

	@Override
	public void onPropertyChanged(IProperty property) {
		if (property.getName().equals("TypeDeConge")) {
			onTypeCongeChange();
		} else if (property.getName().equals("TypeCongeExceptionnelle")) {
			onTypeCongeExceptionnelleChange();
		} else if (property.getName().equals(dateDebutField)) {
			onDateDeDebutChange();
		} else if (property.getName().equals(dateFinField)) {
			onDateDeFinChange();
		} else if (property.getName().equals(debutCongeField) || property.getName().equals(finCongeField)) {
			onHeureDebutFinChange(societe);
		}
		superOnPropertyChanged(property);
	}

	public void superOnPropertyChanged(IProperty property) {
		super.onPropertyChanged(property);
	}

	@Override
	public boolean onBeforeSubmit(IAction action) {
		if (action.getName().equals("Envoyer")) {
			if (!validateForm()) {
				return false;
			}
			demandeur.getExtendedAttributes().setValue("SoldeConges", document.getValue("DemandeurSoldeConge"));
			demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",
					document.getValue("DemandeurTotalJourEnCoursValidation"));
			demandeur.save(getWorkflowModule().getSysadminContext());
			reinitializeSuggestedDatesActionForm();
		}
		return false;// superOnBeforeSubmit(action);
	}

	public boolean superOnBeforeSubmit(IAction action) {
		return super.onBeforeSubmit(action);
	}

	public void onTypeCongeChange() {
		if (document.getValue("TypeDeConge").equals("CN")) {
			document.setValue("TypeCongeExceptionnelle", null);
			document.setValue("NombreDeJoursExceptionnelle", null);
			document.setValue(dateFinField, null);
			document.setValue(totalJoursFerieField, 0);
			calculateNombreJoursDemandes(societe);
		} else if (document.getValue("TypeDeConge").equals("CE")) {
			document.setValue(dateFinField, null);
			document.setValue(debutCongeField, "TJ");
			document.setValue(finCongeField, "TJ");
			document.setValue(totalJoursFerieField, null);
		}
		document.setValue("NombreDeJoursDemandes", 0);
		document.setValue("totalJoursFerie", 0);
	}

	public void onTypeCongeExceptionnelleChange() {
		if (document.getValue("TypeCongeExceptionnelle") != null) {
			IStorageResource congeExceptionnelle = (IStorageResource) document.getValue("TypeCongeExceptionnelle");
			if (congeExceptionnelle.getValue("NbrJours") != null) {
				Number nbrJoursExceptionnelle = (Number) congeExceptionnelle.getValue("NbrJours");
				document.setValue("NombreDeJoursExceptionnelle", nbrJoursExceptionnelle);
				if (document.getValue(dateDebutField) != null) {
					Date dateDebut = (Date) document.getValue(dateDebutField);
					Calendar calFin = Calendar.getInstance();
					calFin.setTime(dateDebut);
					calFin.add(Calendar.DATE, nbrJoursExceptionnelle.intValue() - 1);
					document.setValue(dateFinField, calFin.getTime());
					document.setValue("NombreDeJoursDemandes", 0);
					document.setValue("totalJoursFerie", 0);
				}
			}
		}
	}

	public void onDateDeDebutChange() {
		if (document.getValue("TypeDeConge").equals("CE")) {
			onTypeCongeExceptionnelleChange();
		} else if (document.getValue(dateDebutField) != null) {
			if (document.getValue(dateFinField) != null) {
				calculateNombreJoursDemandes(societe);
			} else {
				validateDates(document.getValue(dateDebutField), document.getValue(dateFinField));
			}
		}
	}

	public void onDateDeFinChange() {
		if (document.getValue(dateFinField) != null) {
			if (document.getValue(dateDebutField) != null) {
				calculateNombreJoursDemandes(societe);
			} else {
				validateDates(document.getValue(dateDebutField), document.getValue(dateFinField));
			}

		}
	}

	public void onHeureDebutFinChange(IStorageResource societe) {
		calculateNombreJoursDemandes(societe);
	}

	public void calculateNombreJoursDemandes(IStorageResource societe) {
		float nombreJoursDemandes = 0;
		Date dateDebut = (Date) document.getValue(dateDebutField);
		Date dateFin = (Date) document.getValue(dateFinField);

		if (document.getValue("TypeDeConge").equals("CE")) {
			onTypeCongeExceptionnelleChange();
			dateFin = (Date) document.getValue(dateFinField);
		} else if (document.getValue("TypeDeConge").equals("CN") || document.getValue("TypeDeConge").equals("SS")) {
			if (dateDebut != null && dateFin != null) {
				if (dateDebut.equals(dateFin)) {
					document.setValue(finCongeField, document.getValue(debutCongeField));
				}
			}
			// setCollaborateurExtendedAttributs(nombreJoursDemandes);
		}
		nombreJoursDemandes = calculateNbrJoursDemandes(societe, dateDebut, dateFin);
		if (document.getValue("TypeDeConge").equals("CN") || document.getValue("TypeDeConge").equals("SS")) {
			document.setValue(nombreDeJoursDeCongesField, nombreJoursDemandes);
		}
		setCollaborateurExtendedAttributs(nombreJoursDemandes);
		validateForm();
	}

	public void setCollaborateurExtendedAttributs(float nbrJoursDemande) {
		float totalNbrJoursRecupere = 0;
		float nbrJoursCongeAnneeEnCours = 0;
		float totalNbrJoursDeConges = 0;
		float totalJoursPris = 0;
		float nombreJoursEnCoursDeValidation = 0;
		if (demandeur.getExtendedAttributes().getValue("SoldeRecupere") != null) {
			totalNbrJoursRecupere = ((Float) demandeur.getExtendedAttributes().getValue("SoldeRecupere"));
		}
		if (demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours") != null) {
			nbrJoursCongeAnneeEnCours = ((Float) demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours"));
		}
		if (demandeur.getExtendedAttributes().getValue("SoldeConges") != null) {
			totalNbrJoursDeConges = ((Float) demandeur.getExtendedAttributes().getValue("SoldeConges"));
		}
		if (demandeur.getExtendedAttributes().getValue("TotalJoursPris") != null) {
			totalJoursPris = ((Float) demandeur.getExtendedAttributes().getValue("TotalJoursPris"));
		}
		if (demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") != null) {
			nombreJoursEnCoursDeValidation = (Float) demandeur.getExtendedAttributes()
					.getValue("JourSEnCoursDeValidation");
		}
		nombreJoursEnCoursDeValidation = nombreJoursEnCoursDeValidation + nbrJoursDemande;
		// totalNbrJoursDeConges = nbrJoursCongeAnneeEnCours + totalNbrJoursRecupere -
		// totalJoursPris - nombreJoursEnCoursDeValidation;
		if (document.getValue("TypeDeConge").equals("CN")) {
			document.setValue("DemandeurSoldeConge", totalNbrJoursDeConges);
		}
		document.setValue("DemandeurTotalJourEnCoursValidation", nombreJoursEnCoursDeValidation);
	}

	public float calculateNbrJoursDemandes(IStorageResource societe, Date startDate, Date endDate) {
		HashMap<String, Object> calculatorResult = new WorkingDaysNumberCalculator(getWorkflowModule())
				.calculateV2(societe, startDate, endDate, saturdayIsAWorkingDay);
		boolean isStartDayAWorkingDay = (boolean) calculatorResult.get("isStartDayAWorkingDay");
		boolean isEndDayAWorkingDay = (boolean) calculatorResult.get("isEndDayAWorkingDay");
		if (!isStartDayAWorkingDay) {
			document.setValue(debutCongeField, "TJ");
		}
		if (!isEndDayAWorkingDay) {
			document.setValue(finCongeField, "TJ");
		}
		document.setValue(isStartDayAWorkingDayField, isStartDayAWorkingDay);
		document.setValue(isEndDayAWorkingDayField, isEndDayAWorkingDay);
		float joursEnCoursDeValidation = (Float) calculatorResult.get("nbrWorkingDays");
		// getResourceController().alert("SLM");
		float totalJoursFerieInPeriod = (Float) calculatorResult.get("totalJoursFerieInPeriod");
		document.setValue(totalJoursFerieField, totalJoursFerieInPeriod);
		float heureDebutFinMinus = 0;
		if (startDate.equals(endDate)) {
			if (document.getValue(debutCongeField).equals("DJ")) {
				heureDebutFinMinus += .5;
			}
		} else {
			if (document.getValue(debutCongeField).equals("DJ")) {
				heureDebutFinMinus += .5;
			}
			if (document.getValue(finCongeField).equals("DJ")) {
				heureDebutFinMinus += .5;
			}
		}
		joursEnCoursDeValidation -= heureDebutFinMinus;
		return joursEnCoursDeValidation;
	}

	public float getNombreJoursFeriesParDate(Date date, float nombreJoursDeCongesParDatePasse,
			Collection<IStorageResource> joursFeries) {
		try {
			float nJoursFeries = 0;
			if (joursFeries.size() > 0) {
				SimpleDateFormat jourVariableDateFormat = new SimpleDateFormat("MM-dd");
				SimpleDateFormat jourFixeDateFormat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat format = jourVariableDateFormat;
				for (IStorageResource jourFerie : joursFeries) {
					format = jourVariableDateFormat;
					if (jourFerie.getValue("DateDebutJourFerie") != null) {
						if (jourFerie.getValue("FeteReligieuse") != null
								&& (boolean) jourFerie.getValue("FeteReligieuse")) {
							format = jourFixeDateFormat;
						}
						if (format.format((Date) jourFerie.getValue("DateDebutJourFerie"))
								.equals(format.format(date))) {
							if (jourFerie.getValue("NJoursFeries") != null) {
								float nJoursFeriesHolder = (Float) jourFerie.getValue("NJoursFeries");
								if (nJoursFeries < nJoursFeriesHolder) {
									nJoursFeries = nJoursFeriesHolder;
								}
							}
						}
					}
				}
			}
			return nombreJoursDeCongesParDatePasse > nJoursFeries ? nombreJoursDeCongesParDatePasse : nJoursFeries;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public boolean validateForm() {
		float nbrJoursDemandes = 0;
		float soldeConges = 0;
		if (document.getValue(nombreDeJoursDeCongesField) != null
				&& !document.getValue(nombreDeJoursDeCongesField).equals(0)) {
			nbrJoursDemandes = (Float) document.getValue(nombreDeJoursDeCongesField);
		}
		if (demandeur.getExtendedAttributes().getValue("SoldeConges") != null) {
			soldeConges = (Float) demandeur.getExtendedAttributes().getValue("SoldeConges");
		}

		if (!validateDates(document.getValue(dateDebutField), document.getValue(dateFinField))) {
			return false;
		}
		if (!document.getValue("TypeDeConge").equals("CE") && !validateSolde(nbrJoursDemandes, soldeConges)) {
			return false;
		}
		return true;
	}

	public boolean validateDates(Object startDate, Object endDate) {
		Date dateDebut = null;
		Date dateFin = null;
		if (startDate != null) {
			dateDebut = (Date) startDate;
		}
		if (endDate != null) {
			dateFin = (Date) endDate;
		}

		if (dateDebut != null) {
			if (new DateValidator(getResourceController()).isDateBeforeDate(dateDebut, new Date())) {
				onInvalidForm("La date de debut de congés doit être aprés la date aujourd'hui");
				return false;
			}
		}
		if (dateFin != null) {
			if (new DateValidator(getResourceController()).isDateBeforeDate(dateFin, new Date())) {
				onInvalidForm("La date de fin de congés doit être aprés la date aujourd'hui");
				return false;
			}
		}

		if (dateDebut != null && dateFin != null) {
			if (new DateValidator(getResourceController()).isDateAfterDate(dateDebut, dateFin)) {
				onInvalidForm("La date de fin doit être aprés la date de début");
				return false;
			}
		}

		if (!isDatesOvelapped(dateDebut, dateFin)) {
			document.setValue(dateDebutField, null);
			onInvalidForm();
			return false;
		}
		return true;
	}

	public boolean validateSolde(float nbrJoursDemandes, float soldeConges) {
		if (soldeConges <= 0 || soldeConges < nbrJoursDemandes) {
			if (soldeConges <= 0) {
				getResourceController().alert("Vous n'avez pas un solde de congés");
			}
			if (soldeConges < nbrJoursDemandes) {
				getResourceController().alert("Le nombre de jours demandés (" + nbrJoursDemandes
						+ ") doit être inférieur ou égal au solde congés (" + soldeConges + ")");
			}
			onInvalidForm();
			document.setValue(nombreDeJoursDeCongesField, 0);
			return false;
		}
		return true;
	}

	public boolean isDatesOvelapped(Date dateDebut, Date dateFin) {
		Collection<IWorkflowInstance> allDemandes = getAllDemandeurDemandes();
		for (IWorkflowInstance demande : allDemandes) {
			Date demandeDebut = (Date) demande.getValue("DateDeDebut");
			Date demandeFin = (Date) demande.getValue("DateDeFin");
			if (dateDebut != null && dateFin != null) {
				if (new DateValidator(getResourceController()).isTwoDatesOverlapped(dateDebut, dateFin, demandeDebut,
						demandeFin)) {
					getResourceController().alert(
							"Vous avez déja séléctionner cette période dans autre demande.\nLa période séléctionnée est "
									+ simpleFormat.format(demandeDebut) + " jusqu’à "
									+ simpleFormat.format(demandeFin));
					return false;
				}
			} else if (dateDebut != null) {
				if (new DateValidator(getResourceController()).isDateInsideRange(dateDebut, demandeDebut, demandeFin)) {
					onInvalidForm();
					getResourceController().alert(
							"La date de début sélectionnée et entre une période dans une autre demande.\nLa période est "
									+ simpleFormat.format(demandeDebut) + " jusqu’à "
									+ simpleFormat.format(demandeFin));
					return false;
				}
			} else if (dateFin != null) {
				if (new DateValidator(getResourceController()).isDateInsideRange(dateFin, demandeDebut, demandeFin)) {
					onInvalidForm();
					getResourceController().alert(
							"La date de fin sélectionnée et entre une période dans une autre demande.\nLa période est "
									+ simpleFormat.format(demandeDebut) + " jusqu’à "
									+ simpleFormat.format(demandeFin));
					return false;
				}
			}

		}
		return true;
	}

	public void onInvalidForm(String... message) {
		document.setValue(dateDebutField, null);
		document.setValue(dateFinField, null);
		document.setValue("DemandeurSoldeConge", demandeur.getExtendedAttributes().getValue("SoldeConges"));
		document.setValue("DemandeurTotalJourEnCoursValidation",
				demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
		document.setValue(nombreDeJoursDeCongesField, 0);
		if (message.length > 0) {
			getResourceController().alert(message[0]);
		}
	}

	private Collection<IWorkflowInstance> getAllDemandeurDemandes() {
		Collection<IWorkflowInstance> collection = null;
		try {
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("Demandeur", demandeur);
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			controller.addNotEqualsConstraint("DocumentState", "Refusé");
			controller.addNotEqualsConstraint("sys_Reference", document.getValue("sys_Reference"));
			collection = controller.evaluate(w);
			return collection;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void reinitializeSuggestedDatesActionForm() {
		document.setValue("isDateCongesSuggested", false);
		document.setValue("suggestedDateDebut", null);
		document.setValue("PremiereValidationCommentaire", null);
		document.setValue("suggestedDateFin", null);
		document.setValue("SuggestedDebutConge", "TJ");
		document.setValue("SuggestedFinConge", "TJ");
		document.setValue("suggestedNombreDeJoursDeConges", 0);
		document.save(getWorkflowModule().getSysadminContext());
	}

	/*
	 * public IStorageResource getSociete(String societeName) {
	 * IStorageResource societe = null;
	 * try {
	 * IContext context = getWorkflowModule().getSysadminContext();
	 * IViewController controller = getWorkflowModule().getViewController(context,
	 * IResource.class);
	 * IProject project =
	 * getProjectModule().getProject(context,"REFERENTIELCOMMUN",getWorkflowInstance
	 * ().getCatalog().getProject().getOrganization());
	 * ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4,
	 * project);
	 * IResourceDefinition definition =
	 * getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
	 * controller.addEqualsConstraint("sys_Title", societeName);
	 * Collection<IStorageResource> societes = controller.evaluate(definition);
	 * if (!societes.isEmpty()){
	 * societe = societes.iterator().next();
	 * }
	 * } catch (Exception e) {
	 * e.printStackTrace();
	 * }
	 * return societe;
	 * }
	 */

}
