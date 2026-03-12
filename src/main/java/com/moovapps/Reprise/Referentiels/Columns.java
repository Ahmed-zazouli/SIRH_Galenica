package com.moovapps.Reprise.Referentiels;

import java.util.Arrays;
import java.util.List;

public class Columns {

    public List<String> pole = Arrays.asList("_Pole");
    public List<String> service = Arrays.asList("_Pole","_Societe","_Departement","_Activite","_Service");
    public List<String> niveauCompetence = Arrays.asList("_FamilleCompetence","_SousFamilleCompetence","_Competence");
    public List<String> competence = Arrays.asList("_Domaine","_FamilleCompetence","_SousFamilleCompetence","_Competence","_Definition","_Poids","_TypeDeFormation","_Duree");

    public List<String> etudes = Arrays.asList("_Collaborateur","_Diplome","_Ecole","_DateDeLObtentionDuDiplome");

    public List<String> societe = Arrays.asList("_Pole","_Societe","_Adresse","_Ville","_ResponsableRH","_ResponsableDEV","_Directeur","_JoursOuvrablesDebut","_JoursOuvrablesFin","_JoursOuvresDebut","_JoursOuvresFin","_TypeCongesSpeciaux","_HeureMinuteDebutPremiereShift","_HeureMinuteFinPremiereShift","_HeureMinuteDebutDeuxiemeShift","_HeureMinuteFinDeuxiemeShift","_PlafondDeDelaiAvanceSurSalaire","_TypeCloture","_PeriodeDeCloture");
    public List<String> compenceFonction = Arrays.asList("_Societe","_Fonction","_FamilleCompetence","_SousFamilleCompetence","_Competence","_Poids");
    public List<String> missionFonction = Arrays.asList("_Societe","_Fonction","_Mission","_Details");
    public List<String> lienParente = Arrays.asList("_Collaborateur","_LienParente","_NomPrenom","_DateDeNaissance");
    public List<String> collaborateur = Arrays.asList("_Societe", "_Matricule", "_Actif", "_Civilite", "_Nom", "_Prenom",
            "_NumTel",
            "_DateDeNaissance",
            "_CIN",
            "_NCNSS",
            "_Ville",
            "_Addresse1",
            "_EtatCivil",
            "_NombreEnfant",
            "_Email",
            "_Identifiant",
            "_MotDePasse",
            "_RIB",
            "_Banque",
            "_AgenceBancaire",
            "_DateEmbaucheSociete",
            "_TypeContrat",
            "_Roles",
            "_Direction",
            "_Departement",
            "_Division",
            "_Fonction",
            "_Suppleants",
            "_Site",
            "_ResponsableN1",
            "_DateSortie","_MotifSortie",
            "_TypeDeSalaire",
            "_TauxHoraire",
            "_SalaireDeBase",
            "_SalaireBrut",

            "_SalaireNETDH",
            "_MontantMensuelNoteFrais",
            "_NMutuelle",
            "_NomDeLaMutuelle",
            "_TauxDeLaMutuelle",
            /*"NCIMR",*/
            "_NomRetraite",
            "_TauxDeLaRetraire",
            "_NumeroAttribue",

            "_DroitMensuelle",
            "_SoldeConges"

            );




}
