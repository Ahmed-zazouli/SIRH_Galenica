package com.moovapps.capone.rh.GestionDeConge.document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.capone.rh.GestionDeConge.helpers.JoursOuvrableOuvres;
import com.moovapps.capone.rh.GestionDeConge.helpers.WorkingDaysNumberCalculator;
import com.moovapps.capone.rh.GestionDeCongeAnnuel.Agents.AgentCongesAnnuels;
import com.moovapps.capone.rh.cummonHelpers.DateValidator;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;

import java.text.SimpleDateFormat;
import java.util.*;

public class DemandeConge2Copy extends BaseDocumentExtension {

    public IWorkflowInstance document = null;
    public IUser demandeur = null;
    public IStorageResource societe = null;
    // SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY = 6, SATURDAY = 7
    ArrayList<Integer> joursOuvrables = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6, 7));

    // SUNDAY = 1, MONDAY = 2, TUESDAY = 3, WEDNESDAY = 4, THURSDAY = 5, FRIDAY = 6, SATURDAY = 7
    ArrayList<Integer> joursOuvres = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6));

   // float SoldeAnneeEnCours = 0;
    //float SoldeAnterieur = 0;
    float SoldeConges = 0;
    /*float CongesPayesEnCoursDeValidation = 0;
    float CongesPayesEnCoursDeConsommation = 0;
    float CongesPayesEnCoursDeTraitement = 0;
    float CongesSpeciauxEnCoursDeValidation = 0;
    float CongesSpeciauxEnCoursDeConsommation = 0;
    float CongesMaladieEnCoursDeValidation = 0;
    float CongesMaladieEnCoursDeConsommation = 0;
    float CongesSansSoldeEnCoursDeValidation = 0;
    float CongesSansSoldeEnCoursDeConsommation = 0;
    float JourSEnCoursDeValidation = 0;

    float joursEnCoursConsommation = 0;
    float CongesPayesAnneeEnCours = 0;
    float CongesPayesN1 = 0;
    float CongesPayesPris = 0;
    float CongesSpeciauxAnneeEnCours = 0;
    float CongesSpeciauxN1 = 0;
    float CongesSpeciauxPris = 0;
    float CongesMaladieAnneeEnCours = 0;
    float CongesMaladieN1 = 0;
    float CongesMaladiePris = 0;
    float CongesSansSoldeAnneeEnCours = 0;
    float CongesSansSoldeN1 = 0;
    float CongesSansSoldePris = 0;
    float TotalJoursPris = 0;
    float JourSEnCoursDeTraitement = 0;
    float CongesSpeciauxEnCoursDeTraitement = 0;
    //float CongesPayeesEnCoursDeTraitement = 0;
    float CongesSansSoldeEnCoursDeTraitement = 0;
    float CongesMaladieEnCoursDeTraitement = 0;*/

    private Date getDateDebutConge() {
        Date dateDebutConge = null;
        if (document.getValue("DateDeDebut") != null) {
            dateDebutConge = (Date) document.getValue("DateDeDebut");
        }
        return dateDebutConge;
    }

    private Date getDateFinConge() {
        Date dateFinConge = null;
        if (document.getValue("DateDeFin") != null) {
            dateFinConge = (Date) document.getValue("DateDeFin");
        }
        return dateFinConge;
    }

    @Override
    public boolean onAfterLoad() {
        document = getWorkflowInstance();
        demandeur = setDemandeur();
        setSocieteFields();
        setJourOuvrablesJourOuvres();
        getDemandeurFields();
        setDemandeurCongesFields();
        //document.setValue("DemandeurTotalJourEnCoursValidation", demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
        //document.setValue("CongesEnCoursDeTraitement", demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement"));
        //document.setValue("CongesEnCoursDeConsommation", demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation"));

        if (document.getValue("siJeSuisLeDemandeur") == null) {
            document.setValue("siJeSuisLeDemandeur", true);
            onAmITheConcernedPersonFieldChange();
        }

        return super.onAfterLoad();
    }

    public void onAmITheConcernedPersonFieldChange() {
        boolean amITheConcernedPerson = (boolean) document.getValue("siJeSuisLeDemandeur");
        if (amITheConcernedPerson) {
            document.setValue("Demandeur", getDocument().getUser());
            onConcernedPersonChange();
        } else {
            document.setValue("DemandeurDonnee",null);
            document.setValue("Demandeur", null);
            onConcernedPersonChange();
            resetForm();
        }
    }

    public void onConcernedPersonChange() {
        if (document.getValue("Demandeur") != null) {
            if (((IUser) document.getValue("Demandeur")).getId().equals(getDocument().getUser().getId())) {
                document.setValue("siJeSuisLeDemandeur", true);
            }
            demandeur = (IUser) document.getValue("Demandeur");
            document.setValue("SupHierarchique", demandeur.getHierarchicalManager());
            // societe = (IStorageResource) demandeur.getExtendedAttributes().getValue("Societe");
            // document.setValue("Societe", societe);
        } else {
            demandeur = null;
            document.setValue("SupHierarchique", null);
//            document.setValue("Societe", null);
        }
        setJourOuvrablesJourOuvres();
        getDemandeurFields();
        setDemandeurCongesFields();
        setValidateurConge();
//        setSocieteFields();
    }

    private void setJourOuvrablesJourOuvres() {
        joursOuvrables = JoursOuvrableOuvres.getJoursOuvrables(societe);
        joursOuvres = JoursOuvrableOuvres.getJoursOuvres(societe);
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("siJeSuisLeDemandeur")) {
            onAmITheConcernedPersonFieldChange();
        } else if (property.getName().equals("Demandeur")) {
            onConcernedPersonChange();
        } else if (property.getName().equals("TypeDeConge")) {
            onCategorieCongesChange();
        } else if (property.getName().equals("DateDeDebut")) {
            document.setValue("NombreDeJoursDemandes", 0);
            document.setValue("totalJoursFerie", 0);
            document.setValue("TotalAbsence", 0);

            onDateDeDebutChange();
        } else if (property.getName().equals("DateDeFin")) {
            document.setValue("NombreDeJoursDemandes", 0);
            document.setValue("totalJoursFerie", 0);
            document.setValue("TotalAbsence", 0);
            onDateDeFinChange();
            setCongeSuppleant();
        } else if (property.getName().equals("DebutConge")) {
            onTrancheDebutChange();
            setCongeSuppleant();
        } else if (property.getName().equals("FinConge")) {
            onTrancheFinChange();
        } else if (property.getName().equals("TypeCongeExceptionnelle")) {
            onTypeCongeExceptionnelleChange();
        }else if(property.getName().equals("DemandeurDonnee")){
            IStorageResource demandeurDonnee = (IStorageResource) getWorkflowInstance().getValue("DemandeurDonnee");
            IUser demandeur = demandeurDonnee!=null?(IUser) demandeurDonnee.getValue("Salarie"):null;
            getWorkflowInstance().setValue("Demandeur",demandeur);
            onConcernedPersonChange();
        }
        super.onPropertyChanged(property);
    }

    private void getDemandeurFields() {
       /* if (demandeur != null && demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours") != null) {
            SoldeAnneeEnCours = (Float) demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("SoldeAnterieur") != null) {
            SoldeAnterieur = (Float) demandeur.getExtendedAttributes().getValue("SoldeAnterieur");
        }*/
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("SoldeConges") != null) {
            SoldeConges = (Float) demandeur.getExtendedAttributes().getValue("SoldeConges");
        }
       /* if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation") != null) {
            CongesPayesEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation") != null) {
            CongesPayesEnCoursDeConsommation = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeTraitement") != null) {
            CongesPayesEnCoursDeTraitement = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeTraitement");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation") != null) {
            CongesSpeciauxEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation") != null) {
            CongesSpeciauxEnCoursDeConsommation = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeTraitement") != null) {
            CongesSpeciauxEnCoursDeTraitement = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeTraitement");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation") != null) {
            CongesMaladieEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation") != null) {
            CongesMaladieEnCoursDeConsommation = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeTraitement") != null) {
            CongesMaladieEnCoursDeTraitement = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeTraitement");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation") != null) {
            CongesSansSoldeEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation") != null) {
            CongesSansSoldeEnCoursDeConsommation = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation");
        }
//CongesSansSoldeEnCoursDeTraitement
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeTraitement") != null) {
            CongesSansSoldeEnCoursDeTraitement = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeTraitement");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") != null) {
            JourSEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement") != null) {
            JourSEnCoursDeTraitement = (Float) demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement");
        }

        if (demandeur != null && demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") != null) {
            joursEnCoursConsommation = (Float) demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesAnneeEnCours") != null) {
            CongesPayesAnneeEnCours = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesAnneeEnCours");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesN1") != null) {
            CongesPayesN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesN1");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesPris") != null) {
            CongesPayesPris = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesPris");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours") != null) {
            CongesSpeciauxAnneeEnCours = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxN1") != null) {
            CongesSpeciauxN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxN1");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxPris") != null) {
            CongesSpeciauxPris = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxPris");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours") != null) {
            CongesMaladieAnneeEnCours = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladieN1") != null) {
            CongesMaladieN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieN1");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladiePris") != null) {
            CongesMaladiePris = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladiePris");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours") != null) {
            CongesSansSoldeAnneeEnCours = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldeN1") != null) {
            CongesSansSoldeN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeN1");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldePris") != null) {
            CongesSansSoldePris = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldePris");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("TotalJoursPris") != null) {
            TotalJoursPris = (Float) demandeur.getExtendedAttributes().getValue("TotalJoursPris");
        }*/
    }

    public void onTypeCongeExceptionnelleChange() {
        if (document.getValue("TypeCongeExceptionnelle") != null) {
            IStorageResource congeExceptionnelle = (IStorageResource) document.getValue("TypeCongeExceptionnelle");
            societe = (IStorageResource) demandeur.getExtendedAttributes().getValue("Societe");
            if (congeExceptionnelle.getValue("NbrJours") != null) {
                Number nbrJoursExceptionnelle = (Number) congeExceptionnelle.getValue("NbrJours");
                document.setValue("NombreDeJoursExceptionnelle", nbrJoursExceptionnelle);
                if (document.getValue("DateDeDebut") != null) {
                    Date dateDebut = (Date) document.getValue("DateDeDebut");
                    Calendar calFin = Calendar.getInstance();
                    calFin.setTime(dateDebut);
                    calFin.add(Calendar.DATE, nbrJoursExceptionnelle.intValue() - 1);

                    if (societe.getValue("CongeSpeciaux").equals("Ouvrables")) {
                        HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateDateFinCongeSpeciaux(societe,dateDebut, calFin.getTime());
                        Date DateDeFin = (Date) calculResult.get("DateFinCongeSpeciaux");
                        document.setValue("DateDeFin", DateDeFin);
                        document.setValue("DateFinReel", DateDeFin);
                    } else if (societe.getValue("CongeSpeciaux").equals("Calendrier")) {
                        document.setValue("DateDeFin", calFin.getTime());
                        document.setValue("DateFinReel", calFin.getTime());
                    }


                    document.setValue("NombreDeJoursDemandes", 0);
                    document.setValue("totalJoursFerie", 0);
                } else {
                    setDemandeurCongesFields();
                }
            }
        } else {
            setDemandeurCongesFields();
        }
    }

    private void onDateDeDebutChange() {
        calculateConges(false);
        canRequestConge();
    }

    private void onDateDeFinChange() {
        calculateConges(false);
        canRequestConge();
    }

    private void onTrancheDebutChange() {
        calculateConges(false);
        canRequestConge();
    }

    private void onTrancheFinChange() {
        calculateConges(false);
        canRequestConge();
    }

    public IUser setDemandeur() {
        if (document.getValue("Demandeur") == null) {
            document.setValue("Demandeur", document.getCreatedBy());
        }
        demandeur = (IUser) document.getValue("Demandeur");
        document.setValue("SupHierarchique", demandeur.getHierarchicalManager());
        document.setValue("Societe", demandeur.getExtendedAttributes().getValue("Societe"));
        return demandeur;
    }

    public void setSocieteFields() {
        societe = (IStorageResource) demandeur.getExtendedAttributes().getValue("Societe");
        document.setValue("ResponsableRH", societe != null ? societe.getValue("ResponsableRH") : null);


        //document.setValue("Societe1", demandeur.getExtendedAttributes().getValue("SocieteDonnee"));
        setValidateurConge();

    }

    private void setValidateurConge(){
        IUser hierarchicalManager =demandeur!=null? demandeur.getHierarchicalManager():null;
        document.setValue("TypeDeValidationConge", societe != null ? societe.getValue("TypeDeValidationConge") : null);
        if (document.getValue("TypeDeValidationConge") != null && document.getValue("TypeDeValidationConge").equals("Responsable Hiérarchique")) {
            document.setValue("ValidateurConge", hierarchicalManager);

        }

    }

    public void onCategorieCongesChange() {
        resetForm();
        //Validateur
        if (!document.getValue("TypeDeConge").equals("CM") && !document.getValue("TypeDeConge").equals("Absence")) {
            if (document.getValue("TypeDeValidationConge").equals("Responsable Hiérarchique")) {
                setValidateurConge();
            }
        }
    }

    public void resetForm() {
        document.setValue("DateDeDebut", null);
        document.setValue("DateDeFin", null);
        document.setValue("DateFinReel", null);
        document.setValue("NombreDeJoursDemandes", 0);
        document.setValue("totalJoursFerie", 0);
        document.setValue("TotalAbsence", 0);
        document.setValue("DebutConge", "TJ");
        document.setValue("FinConge", "TJ");
      //  document.setValue("ValidateurConge", null);
        document.setValue("TypeCongeExceptionnelle", null);
        document.setValue("NombreDeJoursExceptionnelle", null);

       // SoldeAnneeEnCours = 0;
        //SoldeAnterieur = 0;
        SoldeConges = 0;
       /* CongesPayesEnCoursDeValidation = 0;
        CongesPayesEnCoursDeConsommation = 0;
        CongesPayesEnCoursDeTraitement = 0;
        CongesSpeciauxEnCoursDeValidation = 0;
        CongesSpeciauxEnCoursDeConsommation = 0;
        CongesMaladieEnCoursDeValidation = 0;
        CongesMaladieEnCoursDeConsommation = 0;
        CongesSansSoldeEnCoursDeValidation = 0;
        CongesSansSoldeEnCoursDeConsommation = 0;
        CongesSansSoldeEnCoursDeTraitement = 0;
        JourSEnCoursDeValidation = 0;
        joursEnCoursConsommation = 0;
        CongesPayesAnneeEnCours = 0;
        CongesPayesN1 = 0;
        CongesPayesPris = 0;
        CongesSpeciauxAnneeEnCours = 0;
        CongesSpeciauxN1 = 0;
        CongesSpeciauxPris = 0;
        CongesMaladieAnneeEnCours = 0;
        CongesMaladieN1 = 0;
        CongesMaladiePris = 0;
        CongesSansSoldeAnneeEnCours = 0;
        CongesSansSoldeN1 = 0;
        CongesSansSoldePris = 0;
        TotalJoursPris = 0;
       // CongesPayeesEnCoursDeTraitement = 0;
        CongesSpeciauxEnCoursDeTraitement = 0;
        CongesMaladieEnCoursDeTraitement = 0;
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours") != null) {
            SoldeAnneeEnCours = (Float) demandeur.getExtendedAttributes().getValue("SoldeAnneeEnCours");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("SoldeAnterieur") != null) {
            SoldeAnterieur = (Float) demandeur.getExtendedAttributes().getValue("SoldeAnterieur");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("SoldeConges") != null) {
            SoldeConges = (Float) demandeur.getExtendedAttributes().getValue("SoldeConges");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation") != null) {
            CongesPayesEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation") != null) {
            CongesPayesEnCoursDeConsommation = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation");
        }*/
        /*new*/
       /* if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeTraitement") != null) {
            CongesPayesEnCoursDeTraitement = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeTraitement");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation") != null) {
            CongesSpeciauxEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation") != null) {
            CongesSpeciauxEnCoursDeConsommation = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation");
        }*/
        /*new*/
      /*  if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeTraitement") != null) {
            CongesSpeciauxEnCoursDeTraitement = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeTraitement");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation") != null) {
            CongesMaladieEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation") != null) {
            CongesMaladieEnCoursDeConsommation = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeTraitement") != null) {
            CongesMaladieEnCoursDeTraitement = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeTraitement");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation") != null) {
            CongesSansSoldeEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation") != null) {
            CongesSansSoldeEnCoursDeConsommation = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation");
        }*/
//CongesSansSoldeEnCoursDeTraitement
       /* if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeTraitement") != null) {
            CongesSansSoldeEnCoursDeTraitement = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeTraitement");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") != null) {
            JourSEnCoursDeValidation = (Float) demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") != null) {
            joursEnCoursConsommation = (Float) demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement") != null) {
            JourSEnCoursDeTraitement = (Float) demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesAnneeEnCours") != null) {
            CongesPayesAnneeEnCours = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesAnneeEnCours");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesN1") != null) {
            CongesPayesN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesN1");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesPayesPris") != null) {
            CongesPayesPris = (Float) demandeur.getExtendedAttributes().getValue("CongesPayesPris");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours") != null) {
            CongesSpeciauxAnneeEnCours = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxAnneeEnCours");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxN1") != null) {
            CongesSpeciauxN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxN1");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSpeciauxPris") != null) {
            CongesSpeciauxPris = (Float) demandeur.getExtendedAttributes().getValue("CongesSpeciauxPris");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours") != null) {
            CongesMaladieAnneeEnCours = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieAnneeEnCours");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladieN1") != null) {
            CongesMaladieN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieN1");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesMaladiePris") != null) {
            CongesMaladiePris = (Float) demandeur.getExtendedAttributes().getValue("CongesMaladiePris");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours") != null) {
            CongesSansSoldeAnneeEnCours = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeAnneeEnCours");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldeN1") != null) {
            CongesSansSoldeN1 = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldeN1");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("CongesSansSoldePris") != null) {
            CongesSansSoldePris = (Float) demandeur.getExtendedAttributes().getValue("CongesSansSoldePris");
        }
        if (demandeur != null && demandeur.getExtendedAttributes().getValue("TotalJoursPris") != null) {
            TotalJoursPris = (Float) demandeur.getExtendedAttributes().getValue("TotalJoursPris");
        }*/

        //if (document.getValue("TypeDeConge").equals("CM")) {
            //document.setValue("ValidateurConge", null);
        //}
    }

    public void setDemandeurCongesFields() {
        float nbrJoursDemandes = 0;
        if (document.getValue("NombreDeJoursDemandes") != null) {
            nbrJoursDemandes = ((Number) document.getValue("NombreDeJoursDemandes")).floatValue();
        }
        float nbrJoursSpeciaux = 0;
        if (document.getValue("NombreDeJoursExceptionnelle") != null) {
            nbrJoursSpeciaux = ((Number) document.getValue("NombreDeJoursExceptionnelle")).floatValue();
        }
        // set rubrique congés info
        document.setValue("demandeurSoldeCongesPayesReel", demandeur!=null?demandeur.getExtendedAttributes().getValue("SoldeConges"):null);
        document.setValue("DroitMensuelle", demandeur!=null?demandeur.getExtendedAttributes().getValue("DroitMensuelle"):null);
       // document.setValue("demandeurSoldeCongesPayesProjete", SoldeConges - (CongesPayesEnCoursDeConsommation + CongesMaladieEnCoursDeConsommation + CongesSpeciauxEnCoursDeConsommation + CongesSansSoldeEnCoursDeConsommation) - (CongesPayesEnCoursDeValidation + CongesSpeciauxEnCoursDeValidation + CongesSansSoldeEnCoursDeValidation + CongesMaladieEnCoursDeValidation) - (CongesPayesEnCoursDeTraitement + CongesMaladieEnCoursDeTraitement + CongesSpeciauxEnCoursDeTraitement + CongesSansSoldeEnCoursDeTraitement) - nbrJoursDemandes - nbrJoursSpeciaux);
        //document.setValue("demandeurSoldeCongesPayesProjete", SoldeConges - (CongesPayesEnCoursDeConsommation + CongesMaladieEnCoursDeConsommation) - (CongesPayesEnCoursDeValidation + CongesMaladieEnCoursDeValidation)-(CongesPayesEnCoursDeTraitement + CongesMaladieEnCoursDeTraitement) - nbrJoursDemandes);
        /*document.setValue("demandeurCongesValidesAConsommer", joursEnCoursConsommation);
        document.setValue("demandeurCongesEnCoursDeValidation", JourSEnCoursDeValidation + nbrJoursDemandes + nbrJoursSpeciaux);
        document.setValue("DemandeurCongesEnCoursDeTraitement", JourSEnCoursDeTraitement);
        document.setValue("demandeurSoldeAnterieur", SoldeAnterieur);
        document.setValue("demandeurCongesPris", TotalJoursPris);*/

        // set rubrique congés payés info
     /*   document.setValue("CongesPayesN1", CongesPayesN1);
        document.setValue("CongesPayesPris", CongesPayesPris);
        document.setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation + nbrJoursDemandes);
        document.setValue("CongesPayesEnCoursDeConsommation", CongesPayesEnCoursDeConsommation);
        document.setValue("CongesPayesEnCoursDeTraitement", CongesPayesEnCoursDeTraitement);*/

        // 	set rubrique congés spéciaux info
      /*  document.setValue("demandeurCongesSpeciauxPris", CongesSpeciauxPris);
        document.setValue("demandeurCongesSpeciauxN1", CongesSpeciauxN1);
        document.setValue("demandeurCongesSpeciauxEnCoursDeValidation", CongesSpeciauxEnCoursDeValidation + nbrJoursSpeciaux);
        document.setValue("demandeurCongesSpeciauxEnCoursDeConsommation", CongesSpeciauxEnCoursDeConsommation);
        document.setValue("CongesSpeciauxEnCoursDeTraitement", CongesSpeciauxEnCoursDeTraitement);*/

        // set rubrique congés maladie info
       /* document.setValue("demandeurCongesMaladiePris", CongesMaladiePris);
        document.setValue("demandeurCongesMaladieN1", CongesMaladieN1);
        document.setValue("demandeurCongesMaladieEnCoursDeConsommation", CongesMaladieEnCoursDeConsommation + nbrJoursDemandes);
        //	document.setValue("demandeurCongesMaladieEnCoursDeConsommation", CongesMaladieEnCoursDeConsommation);
        document.setValue("CongesMaladieEnCoursDeTraitement", CongesMaladieEnCoursDeTraitement);*/

        // set rubrique congés sans solde info
        /*document.setValue("demandeurCongesSansSoldePris", CongesSansSoldePris);
        document.setValue("demandeurCongesSansSoldeN1", CongesSansSoldeN1);
        document.setValue("demandeurCongesSansSoldeEnCoursDeValidation", CongesSansSoldeEnCoursDeValidation + nbrJoursDemandes);
        document.setValue("demandeurCongesSansSoldeEnCoursDeConsommation", CongesSansSoldeEnCoursDeConsommation);
        document.setValue("CongesSansSoldeEnCoursDeTraitement", CongesSansSoldeEnCoursDeTraitement);*/

        //	document.save(getWorkflowModule().getSysadminContext());
    }

    public void calculateConges(boolean formJoursFeriers) {
        String categorieConges = (String) document.getValue("TypeDeConge");
        if (categorieConges.equals("CN")) {
            calculateCongesPayes();
        } else if (categorieConges.equals("CE")) {
            calculateCongesSpeciaux();
        } else if (categorieConges.equals("CM")) {
            calculateCongesMaladie();
        } else if (categorieConges.equals("SS")) {
            calculateCongesSansSolde();
        } else if (categorieConges.equals("Absence")) {
            calculateAbsence();
        }
        if (!formJoursFeriers) {
            if (validateForm()) {
                setDemandeurCongesFields();
            } else {
                resetForm();
            }
        }
    }

    public boolean validateForm() {
       /* float nbrJoursDemandes = 0;
        float soldeConges = 0;
        if (document.getValue("NombreDeJoursDemandes") != null && !document.getValue("NombreDeJoursDemandes").equals(0)) {
            nbrJoursDemandes = (Float) document.getValue("NombreDeJoursDemandes");
        }*/
        if (!validateDates(document.getValue("DateDeDebut"), document.getValue("DateFinReel")) || !validateDates(document.getValue("DateDeDebut"), document.getValue("DateDeFin"))) {
            return false;
        }
       /* if (document.getValue("TypeDeConge").equals("CN") && !validateSolde(nbrJoursDemandes, SoldeConges)) {
            return false;
        }*/
        return true;
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("Envoyer") || action.getName().equals("SoumettreCongeOuAbsence")) {
            if (!validateForm()) {
                return false;
            }
            if(document.getValue("TypeDeConge").equals("CN") && !canRequestConge()){
                return false;
            }
            boolean siJesuisLeDemandeur = (boolean) getWorkflowInstance().getValue("siJeSuisLeDemandeur");

            if(siJesuisLeDemandeur && getWorkflowInstance().getValue("TypeDeValidationConge").equals("Responsable Hiérarchique")){
                IUser validator = (IUser) getWorkflowInstance().getValue("ValidateurConge");
                if(validator==null){
                    getResourceController().alert("Vous devez avoir un responsable hiérarchique");
                    return false;
                }
            }

            setDemandeurFields();
            reinitializeNextForm();
            setDemandeurFieldsForSignet();
        }

        return super.onBeforeSubmit(action);
    }

    private void setDemandeurFieldsForSignet() {
        IUser demandeur = (IUser) getWorkflowInstance().getValue("Demandeur");
        if(demandeur!=null){
            String pattern = "dd/MM/yyyy";

            // Create a DateTimeFormatter with the specified pattern
            SimpleDateFormat  formatter = new SimpleDateFormat(pattern);

            String dateDebutTEXT = "";
            String dateFinTEXT = "";
            String nombreDeJourDemande = "";
            String typeConge = (String) getWorkflowInstance().getValue("TypeDeConge");
            if(typeConge.equals("CN")){
                Date dateDeDebut = (Date) getWorkflowInstance().getValue("DateDeDebut");
                Date dateDeFin =(Date) getWorkflowInstance().getValue("DateFinReel");
                dateDebutTEXT = formatter.format(dateDeDebut);
                dateFinTEXT = formatter.format(dateDeFin);
                nombreDeJourDemande = ((Number) getWorkflowInstance().getValue("NombreDeJoursDemandes")).doubleValue()+"";
            }else if(typeConge.equals("CE")){
                Date dateDeDebut = (Date) getWorkflowInstance().getValue("DateDeDebut");
                Date dateDeFin =(Date) getWorkflowInstance().getValue("DateFinReel");
                dateDebutTEXT = formatter.format(dateDeDebut);
                dateFinTEXT = formatter.format(dateDeFin);
                nombreDeJourDemande = ((Number) getWorkflowInstance().getValue("NombreDeJoursExceptionnelle")).doubleValue()+"";

            }else if(typeConge.equals("CM")){
                Date dateDeDebut = (Date) getWorkflowInstance().getValue("DateDeDebut");
                Date dateDeFin =(Date) getWorkflowInstance().getValue("DateFinReel");
                dateDebutTEXT = formatter.format(dateDeDebut);
                dateFinTEXT = formatter.format(dateDeFin);
                nombreDeJourDemande = ((Number) getWorkflowInstance().getValue("NombreDeJoursDemandes")).doubleValue()+"";
            }else if(typeConge.equals("SS")){
                Date dateDeDebut = (Date) getWorkflowInstance().getValue("DateDeDebut");
                Date dateDeFin =(Date) getWorkflowInstance().getValue("DateFinReel");
                dateDebutTEXT = formatter.format(dateDeDebut);
                dateFinTEXT = formatter.format(dateDeFin);
                nombreDeJourDemande = ((Number) getWorkflowInstance().getValue("NombreDeJoursDemandes")).doubleValue()+"";
            }
            getWorkflowInstance().setValue("DateDeDepartConge",dateDebutTEXT);
            getWorkflowInstance().setValue("DateDeRetourConge",dateFinTEXT);
            getWorkflowInstance().setValue("NombreDeJoursDemandesTEXT",nombreDeJourDemande);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        }
    }

    private void setDemandeurFields() {

        float nbrJoursDemandes = 0;
        if (document.getValue("NombreDeJoursDemandes") != null) {
            nbrJoursDemandes = ((Number) document.getValue("NombreDeJoursDemandes")).floatValue();
        }
        float nbrJoursSpeciaux = 0;
        if (document.getValue("NombreDeJoursExceptionnelle") != null) {
            nbrJoursSpeciaux = ((Number) document.getValue("NombreDeJoursExceptionnelle")).floatValue();
        }

        String categorieConges = (String) document.getValue("TypeDeConge");
        if (categorieConges.equals("CN")) {
            setDemandeurCongesPayesInfo(nbrJoursDemandes);
        } else if (categorieConges.equals("CE")) {
            setDemandeurCongesSpeciauxInfo(nbrJoursSpeciaux);
        } else if (categorieConges.equals("CM")) {
            setDemandeurCongesMaladieInfo(nbrJoursDemandes);
        } else if (categorieConges.equals("SS")) {
            setDemandeurCongesSansSoldeInfo(nbrJoursDemandes);
        }
       /* document.setValue("DemandeurTotalJourEnCoursValidation", demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
        document.setValue("CongesEnCoursDeConsommation", demandeur.getExtendedAttributes().getValue("JourSEnCoursDeConsommation"));
        //document.setValue("CongesEnCoursDeValidation", demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));
        document.setValue("CongesEnCoursDeTraitement", demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement"));
        //
        document.setValue("demandeurCongesEnCoursDeValidation", JourSEnCoursDeValidation + nbrJoursDemandes);
        document.setValue("demandeurCongesValidesAConsommer", joursEnCoursConsommation);
        document.setValue("DemandeurCongesEnCoursDeTraitement", JourSEnCoursDeTraitement);*/
        IStorageResource userFiche = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(demandeur, getWorkflowModule(), getProjectModule(), getWorkflowInstance().getCatalog().getProject().getOrganization());
        userFiche.save(getWorkflowModule().getSysadminContext());
        demandeur.save(getWorkflowModule().getSysadminContext());
    }

    private void setDemandeurCongesPayesInfo(float nbrJoursDemandes) {
        /*demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation + nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", JourSEnCoursDeValidation + nbrJoursDemandes);
        document.setValue("CongesPayesEnCoursDeValidation", CongesPayesEnCoursDeValidation + nbrJoursDemandes);
        document.setValue("CongesPayesEnCoursDeTraitement", CongesPayesEnCoursDeTraitement );
        document.setValue("CongesPayesEnCoursDeConsommation", CongesPayesEnCoursDeConsommation);*/
        //demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeTraitement", CongesPayesEnCoursDeTraitement);
        //demandeur.getExtendedAttributes().setValue("CongesEnCoursDeTraitement", JourSEnCoursDeTraitement);
        //demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation", CongesPayesEnCoursDeConsommation);
        //demandeur.getExtendedAttributes().setValue("CongesEnCoursDeConsommation", joursEnCoursConsommation);


    }

    private void setDemandeurCongesSpeciauxInfo(float nbrJoursDemandes) {
       /* demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeValidation", CongesSpeciauxEnCoursDeValidation + nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", JourSEnCoursDeValidation + nbrJoursDemandes);
        document.setValue("demandeurCongesSpeciauxEnCoursDeValidation", CongesSpeciauxEnCoursDeValidation + nbrJoursDemandes);*/
        //document.setValue("CongesSpeciauxEnCoursDeTraitement", CongesSpeciauxEnCoursDeTraitement );
    }

    private void setDemandeurCongesMaladieInfo(float nbrJoursDemandes) {
        /*demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeValidation", (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation") + nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", (Float) demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation") + nbrJoursDemandes);
        document.setValue("demandeurCongesMaladieEnCoursDeValidation", demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation"));*/
    }

    private void setDemandeurCongesSansSoldeInfo(float nbrJoursDemandes) {
       /* demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation", CongesSansSoldeEnCoursDeValidation + nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", JourSEnCoursDeValidation + nbrJoursDemandes);
        document.setValue("demandeurCongesSansSoldeEnCoursDeValidation", CongesSansSoldeEnCoursDeValidation + nbrJoursDemandes);*/
    }

    public void reinitializeNextForm() {
        document.setValue("isDateCongesSuggested", false);
        document.setValue("suggestedDateDebut", null);
        document.setValue("PremiereValidationCommentaire", null);
        document.setValue("suggestedDateFin", null);
        document.setValue("SuggestedDebutConge", "TJ");
        document.setValue("SuggestedFinConge", "TJ");
        document.setValue("suggestedNombreDeJoursDeConges", 0);
        document.save(getWorkflowModule().getSysadminContext());
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

        if (dateDebut != null && dateFin != null) {
            if (new DateValidator(getResourceController()).isDateAfterDate(dateDebut, dateFin)) {
                onInvalidForm("La date de fin doit être aprés la date de début");
                return false;
            }
        }

        if (!isDatesOvelapped(dateDebut, dateFin)) {
            document.setValue("DateDeDebut", null);
            onInvalidForm();
            return false;
        }
        return true;
    }

    public Collection<IWorkflowInstance> getAllDemandeurDemandes() {
        Collection<IWorkflowInstance> collection = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
            IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("Demandeur", demandeur);
            controller.addNotInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("En cours", "Refusé", "Annulé")));
            controller.addNotEqualsConstraint("sys_Reference", document.getValue("sys_Reference"));
            collection = controller.evaluate(w);
            return collection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isDatesOvelapped(Date dateDebut, Date dateFin) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
        Collection<IWorkflowInstance> allDemandes = getAllDemandeurDemandes();
        allDemandes.addAll(new AgentCongesAnnuels().getPlaniferCongeAnnuel());
        for (IWorkflowInstance demande : allDemandes) {
            Date demandeDebut = (Date) demande.getValue("DateDeDebut");
            Date demandeFin = (Date) demande.getValue("DateFinReel");
            if(demandeFin == null) {
                demandeFin = (Date) demande.getValue("DateDeFin");
            }
            if (dateDebut != null && dateFin != null) {
                if (new DateValidator(getResourceController()).isTwoDatesOverlapped(dateDebut, dateFin, demandeDebut, demandeFin)) {
                    getResourceController()
                            .alert("Vous avez déja séléctionné cette période dans une autre demande.\nLa période séléctionnée est "
                                    + simpleFormat.format(demandeDebut) + " jusqu’à " + simpleFormat.format(demandeFin));
                    return false;
                }
            } else if (dateDebut != null) {
                if (new DateValidator(getResourceController()).isDateInsideRange(dateDebut, demandeDebut, demandeFin)) {
                    onInvalidForm();
                    getResourceController().
                            alert("La date de début sélectionnée est incluse dans une autre demande.\nLa période est "
                                    + simpleFormat.format(demandeDebut) + " jusqu’à " + simpleFormat.format(demandeFin));
                    return false;
                }
            } else if (dateFin != null) {
                if (new DateValidator(getResourceController()).isDateInsideRange(dateFin, demandeDebut, demandeFin)) {
                    onInvalidForm();
                    getResourceController().alert("La date de fin sélectionnée est incluse dans une autre demande.\nLa période est " + simpleFormat.format(demandeDebut) + " jusqu’à " + simpleFormat.format(demandeFin));
                    return false;
                }
            }

        }
        return true;
    }

    public boolean validateSolde(float nbrJoursDemandes, float soldeConges) {
        if (soldeConges <= 0 || soldeConges < nbrJoursDemandes) {
            if (soldeConges <= 0) {
                getResourceController().alert("Vous n'avez pas un solde de congés");
            }
            if (soldeConges < nbrJoursDemandes) {
                getResourceController().alert("Le nombre de jours demandés (" + nbrJoursDemandes + ") doit être inférieur ou égal au solde congés (" + soldeConges + ")");
            }
            onInvalidForm();
            document.setValue("NombreDeJoursDemandes", 0);
            return false;
        }
        return true;
    }

    public void onInvalidForm(String... message) {
        document.setValue("DateDeDebut", null);
        document.setValue("DateDeFin", null);
        document.setValue("DateFinReel", null);
        document.setValue("DemandeurSoldeConge", SoldeConges);
       // document.setValue("DemandeurTotalJourEnCoursValidation", JourSEnCoursDeValidation);
        document.setValue("NombreDeJoursDemandes", 0);
        if (message.length > 0) {
            getResourceController().alert(message[0]);
        }
    }

    public void calculateCongesPayes() {

        Date dateDebutConge = getDateDebutConge();
        Date dateFinConge = getDateFinConge();
        if (dateDebutConge != null && dateFinConge != null) {

            HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutConge, dateFinConge, false);

            boolean isStartDayAWorkingDay = (boolean) calculResult.get("isStartDayAWorkingDay");
            boolean isEndDayAWorkingDay = (boolean) calculResult.get("isEndDayAWorkingDay");
            float totalJoursFerieInPeriod = (Float) calculResult.get("totalJoursFerieInPeriod");
            float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
            int nbrJoursToAdd = (int) calculResult.get("nbrJoursToAdd");
            Date dateFinReel = (Date) calculResult.get("dateFinReel");

            if (!isStartDayAWorkingDay) {
                document.setValue("DebutConge", "TJ");
            }
            if (!isEndDayAWorkingDay) {
                document.setValue("FinConge", "TJ");
            }
            document.setValue("isStartDayAWorkingDay", isStartDayAWorkingDay);
            document.setValue("isEndDayAWorkingDay", isEndDayAWorkingDay);
            document.setValue("totalJoursFerie", totalJoursFerieInPeriod);
            document.setValue("DateFinReel", dateFinReel);

            // The method "calculateTrancheMinusValue" calculate the value of the tranches
            // example => (if the tranche debut is "Demi journée" and tranche fin est "Toute la journée" it returns "-0.5")
            nbrJoursDemande -= calculateTrancheMinusValue(document,dateDebutConge, dateFinConge, nbrJoursToAdd);

            document.setValue("NombreDeJoursDemandes", nbrJoursDemande);
        } else {
            setDemandeurCongesFields();
        }
    }

    private float calculateTrancheMinusValue(IWorkflowInstance document , Date dateDebutConge, Date dateFinConge, int nbrJoursToAdd) {
        float heureDebutFinMinus = 0;
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(dateFinConge);
        if (dateDebutConge.equals(dateFinConge)) {
            if (document.getValue("DebutConge").equals("DJ")) {
                heureDebutFinMinus += .5;
            }
        } else {
            if (document.getValue("DebutConge").equals("DJ")) {
                heureDebutFinMinus += .5;
            }
            if (document.getValue("FinConge").equals("DJ") && joursOuvres.indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) != joursOuvres.size() - 1) {
                heureDebutFinMinus += .5;
            }
            if (document.getValue("FinConge").equals("DJ") && joursOuvres.indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) == joursOuvres.size() - 1) {
                heureDebutFinMinus += .5;
                heureDebutFinMinus += nbrJoursToAdd;
            }
        }
        return heureDebutFinMinus;
    }

    public void calculateCongesSpeciaux() {

        onTypeCongeExceptionnelleChange();
    }

    public void calculateCongesMaladie() {
        Date dateDebutConge = getDateDebutConge();
        Date dateFinConge = getDateFinConge();
        if (dateDebutConge != null && dateFinConge != null) {

            HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutConge, dateFinConge, false);

            boolean isStartDayAWorkingDay = (boolean) calculResult.get("isStartDayAWorkingDay");
            boolean isEndDayAWorkingDay = (boolean) calculResult.get("isEndDayAWorkingDay");
            float totalJoursFerieInPeriod = (Float) calculResult.get("totalJoursFerieInPeriod");
            float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
            int nbrJoursToAdd = (int) calculResult.get("nbrJoursToAdd");
            Date dateFinReel = (Date) calculResult.get("dateFinReel");

            if (!isStartDayAWorkingDay) {
                document.setValue("DebutConge", "TJ");
            }
            if (!isEndDayAWorkingDay) {
                document.setValue("FinConge", "TJ");
            }
            document.setValue("isStartDayAWorkingDay", isStartDayAWorkingDay);
            document.setValue("isEndDayAWorkingDay", isEndDayAWorkingDay);
            document.setValue("totalJoursFerie", totalJoursFerieInPeriod);
            document.setValue("DateFinReel", dateFinReel);

            // The method "calculateTrancheMinusValue" calculate the value of the tranches
            // example => (if the tranche debut is "Demi journée" and tranche fin est "Toute la journée" it returns "-0.5")
            nbrJoursDemande -= calculateTrancheMinusValue(document,dateDebutConge, dateFinConge, nbrJoursToAdd);

            document.setValue("NombreDeJoursDemandes", nbrJoursDemande);
        } else {
            setDemandeurCongesFields();
        }
    }

    public void calculateCongesSansSolde() {
        Date dateDebutConge = getDateDebutConge();
        Date dateFinConge = getDateFinConge();
        if (dateDebutConge != null && dateFinConge != null) {

            HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutConge, dateFinConge, false);

            boolean isStartDayAWorkingDay = (boolean) calculResult.get("isStartDayAWorkingDay");
            boolean isEndDayAWorkingDay = (boolean) calculResult.get("isEndDayAWorkingDay");
            float totalJoursFerieInPeriod = (Float) calculResult.get("totalJoursFerieInPeriod");
            float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
            int nbrJoursToAdd = (int) calculResult.get("nbrJoursToAdd");
            Date dateFinReel = (Date) calculResult.get("dateFinReel");

            if (!isStartDayAWorkingDay) {
                document.setValue("DebutConge", "TJ");
            }
            if (!isEndDayAWorkingDay) {
                document.setValue("FinConge", "TJ");
            }
            document.setValue("isStartDayAWorkingDay", isStartDayAWorkingDay);
            document.setValue("isEndDayAWorkingDay", isEndDayAWorkingDay);
            document.setValue("totalJoursFerie", totalJoursFerieInPeriod);
            document.setValue("DateFinReel", dateFinReel);

            // The method "calculateTrancheMinusValue" calculate the value of the tranches
            // example => (if the tranche debut is "Demi journée" and tranche fin est "Toute la journée" it returns "-0.5")
            nbrJoursDemande -= calculateTrancheMinusValue(document,dateDebutConge, dateFinConge, nbrJoursToAdd);

            document.setValue("NombreDeJoursDemandes", nbrJoursDemande);
//			document.setValue("demandeurCongesSansSoldeEnCoursDeValidation", (Float)document.getValue("demandeurCongesSansSoldeEnCoursDeValidation") + nbrJoursDemande);
        } else {
            setDemandeurCongesFields();
        }
    }

    public void calculateAbsence() {
        Date dateDebutConge = getDateDebutConge();
        Date dateFinConge = getDateFinConge();
        if (dateDebutConge != null && dateFinConge != null) {

            HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutConge, dateFinConge, false);

            boolean isStartDayAWorkingDay = (boolean) calculResult.get("isStartDayAWorkingDay");
            boolean isEndDayAWorkingDay = (boolean) calculResult.get("isEndDayAWorkingDay");
            float totalJoursFerieInPeriod = (Float) calculResult.get("totalJoursFerieInPeriod");
            float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
            int nbrJoursToAdd = (int) calculResult.get("nbrJoursToAdd");
            Date dateFinReel = (Date) calculResult.get("dateFinReel");

            if (!isStartDayAWorkingDay) {
                document.setValue("DebutConge", "TJ");
            }
            if (!isEndDayAWorkingDay) {
                document.setValue("FinConge", "TJ");
            }
            document.setValue("isStartDayAWorkingDay", isStartDayAWorkingDay);
            document.setValue("isEndDayAWorkingDay", isEndDayAWorkingDay);
            document.setValue("totalJoursFerie", totalJoursFerieInPeriod);
            document.setValue("DateFinReel", dateFinReel);

            // The method "calculateTrancheMinusValue" calculate the value of the tranches
            // example => (if the tranche debut is "Demi journée" and tranche fin est "Toute la journée" it returns "-0.5")
            nbrJoursDemande -= calculateTrancheMinusValue(document,dateDebutConge, dateFinConge, nbrJoursToAdd);

            document.setValue("NombreDeJoursDemandes", nbrJoursDemande);
//			document.setValue("demandeurCongesSansSoldeEnCoursDeValidation", (Float)document.getValue("demandeurCongesSansSoldeEnCoursDeValidation") + nbrJoursDemande);
        } else {
            setDemandeurCongesFields();
        }
    }

    public void setCongeSuppleant() {
        document.deleteLinkedResources(document.getLinkedResources("Suppleant"));
        Collection<IWorkflowInstance> congesSuppleant = getCongeSuppleant();
        Date dateDebut = (Date) document.getValue("DateDeDebut");
        Date dateFin = (Date) document.getValue("DateDeFin");
        boolean isOverlapped = false;

        for (IWorkflowInstance demande : congesSuppleant) {
            isOverlapped = false;
            Date demandeDebut = (Date) demande.getValue("DateDeDebut");
            Date demandeFin = (Date) demande.getValue("DateFinReel");
            if(demandeDebut != null && demandeFin != null){
                if (dateDebut != null && dateFin != null) {
                    if (new DateValidator(getResourceController()).isTwoDatesOverlapped(dateDebut, dateFin, demandeDebut, demandeFin)) {
                        isOverlapped = true;

                    }
                } else if (dateDebut != null) {
                    if (new DateValidator(getResourceController()).isDateInsideRange(dateDebut, demandeDebut, demandeFin)) {
                        isOverlapped = true;

                    }
                } else if (dateFin != null) {
                    if (new DateValidator(getResourceController()).isDateInsideRange(dateFin, demandeDebut, demandeFin)) {
                        isOverlapped = true;

                    }
                }
            }


            if (isOverlapped){

                ILinkedResource row = document.createLinkedResource("Suppleant");
                row.setValue("DateDebut", demande.getValue("DateDeDebut"));
                row.setValue("DateFin", demande.getValue("DateDeFin"));
                row.setValue("Collaborateur", demande.getValue("Demandeur"));
                row.setValue("CategorieConge", demande.getText("TypeDeConge"));
                row.save(getWorkflowModule().getSysadminContext());
                document.addLinkedResource(row);

            }

        }

    }


    public Collection<IWorkflowInstance> getCongeSuppleant() {
        IUser user = (IUser) document.getValue("Demandeur");
        if(user==null)return  Collections.emptyList();
        IStorageResource ficheCollaborateur = getFicheByUser(user);

        Collection<IWorkflowInstance> collection = Collections.emptyList();
        if(ficheCollaborateur==null)return collection;
        if (ficheCollaborateur.getValue("Suppleant") != null) {
            ArrayList<IStorageResource> Suppleants = (ArrayList<IStorageResource>) ficheCollaborateur.getValue("Suppleant");
            ArrayList<IUser> users = new ArrayList<>();
            for (IStorageResource suppleant : Suppleants) {
                users.add((IUser) suppleant.getValue("Salarie"));
            }
            if(users==null || users.isEmpty()){
                return Collections.emptyList();
            }
            try {
                IContext sysContext = Modules.getWorkflowModule().getSysadminContext();
                IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                IProject project = Modules.getProjectModule().getProject(sysContext, "Capone", organization);
                IContext context = Modules.getWorkflowModule().getLoggedOnUserContext();
                ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "RH", project);
                IWorkflowContainer w = Modules.getWorkflowModule().getWorkflowContainer(context, catalog, "GestionDeConges");
                IViewController controller = Modules.getWorkflowModule().getViewController(context);
                controller.addInConstraint("Demandeur", users);

                collection = controller.evaluate(w);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return collection;

    }

    private IStorageResource getFicheByUser(IUser user) {
        if(user==null)return null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("Salarie", user);
            ArrayList<IStorageResource> data = (ArrayList<IStorageResource>) controller.evaluate(definition);
            return data!=null ? data.iterator().next() : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean canRequestConge() {
        Date dateDebut = getDateDebutConge();
        Date dateFinConge = getDateFinConge();
        float projeteLastConge = 0f;
        float totalJourDemande = 0f;
        if(dateDebut==null || dateFinConge == null){
            return false;
        }
        //dd-MM-yy
        Calendar c = Calendar.getInstance();
        int annee = c.get(Calendar.YEAR);
        int mois = c.get(Calendar.MONTH);
        try{
            annee =demandeur.getExtendedAttributes().getValue("Annee")!=null?((Number)  ((IStorageResource) demandeur.getExtendedAttributes().getValue("Annee")).getValue("Valeur")).intValue():annee;
            mois =demandeur.getExtendedAttributes().getValue("Mois")!=null?((Number)  ((IStorageResource) demandeur.getExtendedAttributes().getValue("Mois")).getValue("Valeur")).intValue():mois;

          /*  if(demandeur.getExtendedAttributes().getValue("Annee")!=null &&
                    ((IStorageResource) demandeur.getExtendedAttributes().getValue("Annee")).getValue("sys_Title")!=null &&
                    !((IStorageResource) demandeur.getExtendedAttributes().getValue("Annee")).getValue("sys_Title").equals("")){
                annee =  Integer.parseInt((String) ((IStorageResource) demandeur.getExtendedAttributes().getValue("Annee")).getValue("sys_Title"));
            }
            if(demandeur.getExtendedAttributes().getValue("Mois")!=null &&
                    ((IStorageResource) demandeur.getExtendedAttributes().getValue("Mois")).getValue("sys_Title")!=null &&
                    !((IStorageResource) demandeur.getExtendedAttributes().getValue("Mois")).getValue("sys_Title").equals("")){
                mois =  Integer.parseInt((String) ((IStorageResource) demandeur.getExtendedAttributes().getValue("Mois")).getValue("sys_Title"));
            }*/
        }catch (Exception e){
            e.printStackTrace();
        }
        c.set(Calendar.YEAR,annee);
        c.set(Calendar.MONTH,mois);
        if(societe.getValue("ClotureSociete")!=null) {
            if (societe.getValue("ClotureSociete").equals("Mois")) {
                c.set(Calendar.DAY_OF_MONTH, 1);
            } else {
                if(societe.getValue("PeriodeDeCloture")!=null){
                    c.add(Calendar.MONTH, -1);
                    int maxDayOfMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                    int periode = ((Number) societe.getValue("PeriodeDeCloture")).intValue();
                    if(periode>maxDayOfMonth){
                        periode = maxDayOfMonth;
                    }
                    c.set(Calendar.DAY_OF_MONTH, periode);
                }else{
                    c.set(Calendar.DAY_OF_MONTH, 1);
                }

            }
        }else{
            c.set(Calendar.DAY_OF_MONTH, 1);
        }

        c.add(Calendar.DATE,-1);
        ArrayList<IWorkflowInstance> allDemandeurDemandesFromStartingDate =  getAllDemandeurDemandesFromStartingDate(c.getTime());
        allDemandeurDemandesFromStartingDate.add(getWorkflowInstance());
        c.add(Calendar.DATE,1);
        Date dateDebutCurrentMonth = c.getTime();
        if(allDemandeurDemandesFromStartingDate!=null && !allDemandeurDemandesFromStartingDate.isEmpty()){
            //float totalJourDemande = 0;
            for(IWorkflowInstance conge : allDemandeurDemandesFromStartingDate){
                Date dateDebutConge = (Date) conge.getValue("DateDeDebut");
                if(dateDebutConge.before(dateDebutCurrentMonth)){//back here
                    if (conge.getValue("TypeCongeExceptionnelle") != null){
                        if (societe.getValue("CongeSpeciaux").equals("Ouvrables")) {
                            HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutCurrentMonth, (Date) conge.getValue("DateDeFin"), false);
                            float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
                            int nbrJoursToAdd = (int) calculResult.get("nbrJoursToAdd");
                            nbrJoursDemande -= calculateTrancheMinusValue(conge,dateDebutCurrentMonth, (Date) conge.getValue("DateDeFin"), nbrJoursToAdd);
                            totalJourDemande+=nbrJoursDemande;
                            continue;
                        }else{
                            HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutCurrentMonth, (Date) conge.getValue("DateDeFin"), true);
                            float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
                            int nbrJoursToAdd = (int) calculResult.get("nbrJoursToAdd");
                            nbrJoursDemande -= calculateTrancheMinusValue(conge,dateDebutCurrentMonth, (Date) conge.getValue("DateDeFin"), nbrJoursToAdd);
                            totalJourDemande+=nbrJoursDemande;
                            continue;
                        }

                    }else{
                        HashMap<String, Object> calculResult = new WorkingDaysNumberCalculator(getWorkflowModule(), joursOuvrables, joursOuvres).calculateV2(societe,dateDebutCurrentMonth, (Date) conge.getValue("DateDeFin"), false);
                        float nbrJoursDemande = (Float) calculResult.get("nbrJoursDemande");
                        int nbrJoursToAdd = (int) calculResult.get("nbrJoursToAdd");
                        nbrJoursDemande -= calculateTrancheMinusValue(conge,dateDebutCurrentMonth, (Date) conge.getValue("DateDeFin"), nbrJoursToAdd);
                        totalJourDemande+=nbrJoursDemande;
                        continue;
                    }

                }
                float nbrJourDemande = conge.getValue("NombreDeJoursDemandes")!=null?((Number) conge.getValue("NombreDeJoursDemandes")).floatValue():0;
                float nbrJourExeptionel = conge.getValue("NombreDeJoursExceptionnelle")!=null?((Number) conge.getValue("NombreDeJoursExceptionnelle")).floatValue():0;

                totalJourDemande+=(nbrJourDemande+nbrJourExeptionel);
            }

            IWorkflowInstance lastConge = allDemandeurDemandesFromStartingDate.get(allDemandeurDemandesFromStartingDate.size()-1);
            Date dateFinLastConge = (Date) lastConge.getValue("DateDeFin");
            /*float*/ projeteLastConge = calculateProjeteLastConge(dateDebutCurrentMonth,dateFinLastConge);
            if(totalJourDemande<=projeteLastConge){
                return true;
            }
        }
       /* getResourceController().alert(
                "Votre solde de congés, qu'il s'agisse de votre solde actuel " +
                        "ou du solde projeté à la date de fin de congé sélectionnée ("+projeteLastConge+" jours)," +
                        " est insuffisant pour effectuer cette demande. " +
                        "Le total des jours demandés ("+totalJourDemande+" jours) correspond " +
                        "à la somme des jours de congé demandés pour ce mois ou à des dates ultérieures." +
                        " Merci de vérifier votre solde ou de choisir une autre période.");*/
        //getResourceController().alert("Votre solde de congé est insuffisant pour effectuer cette demande.");


        return true;
    }


    public ArrayList<IWorkflowInstance> getAllDemandeurDemandesFromStartingDate(Date dateDebut) {
        ArrayList<IWorkflowInstance> collection = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
            IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("Demandeur", demandeur);
            controller.addNotInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("En cours", "Refusé","Refusée", "Annulé","Annulée")));
            controller.addGreaterConstraint("DateFinReel",dateDebut);
            controller.setOrderBy("DateFinReel",Date.class,true);
            controller.addNotEqualsConstraint("sys_Reference", document.getValue("sys_Reference"));
            collection = (ArrayList<IWorkflowInstance>) controller.evaluate(w);
            return collection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private float calculateProjeteLastConge(Date dateDebut, Date dateDebutLastConge) {
        if (demandeur != null) {
            float soldeCongeSage = demandeur.getExtendedAttributes().getValue("SoldeConges") != null ? (float) demandeur.getExtendedAttributes().getValue("SoldeConges") : 0;
            float demandeurDroitMensuel = demandeur.getExtendedAttributes().getValue("DroitMensuelle") != null ? (float) demandeur.getExtendedAttributes().getValue("DroitMensuelle") : 0;
            int monthsBetween = calculateMonthsBetween(dateDebut, dateDebutLastConge);
            return soldeCongeSage + (monthsBetween * demandeurDroitMensuel);
        }
        return 0;
    }

    public static int calculateMonthsBetween(Date debut, Date fin) {
        Calendar c = Calendar.getInstance();
        c.setTime(debut);
        int startYear = c.get(Calendar.YEAR);
        int startMonth = c.get(Calendar.MONTH); // January = 0
        c.setTime(fin);
        int endYear = c.get(Calendar.YEAR);
        int endMonth = c.get(Calendar.MONTH); // January = 0
        return (endYear - startYear) * 12 + (endMonth - startMonth);
    }

}