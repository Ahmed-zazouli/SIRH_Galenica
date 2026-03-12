package com.moovapps.ibb.rh.Script.Referentiel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class VariablesColonnes {
    public static HashMap<Integer, List<String>> titlesMaquetteVerification = new HashMap<Integer, List<String>>() {{
        put(0, Arrays.asList("Société","Matricule","Actif","Civilité","Nom","Prénom","Numéro de téléphone","Date de naisssance","CIN","N° Immatriculation CNSS","Ville","Adresse",
                "Etat Civil","Nombre d'enfants","Email","Identifiant","Mot de passe","RIB (24 chiffres)","Banque","Agence bancaire","Date d'embauche","Type de contrat","Groupes",
                "Direction","Département","Division","Fonction","Profil","Suppléant","Site","Supérieur hiérarchique","Compétence","Date sortie de l’entreprise","Motif sortie de l’entreprise","Type de salaire",
                "Taux horaire (DH)","Salaire de base (DH)","Salaire Brut (DH)","Salaire NET (DH)","N° Mutuelle","Nom mutuelle","Taux mutuelle (%)","Nom retraite","Taux retraite (%)",
                "Numéro attribué",/*"Nom retraite complémentaire","Taux retraite complémentaire (%)","Montant épargne retraite",*/"Droit mensuel","Solde année en cours","Solde antérieur"));
    }};

    public static HashMap<Integer, String> indexSheetToMethod = new HashMap<Integer, String>() {{
        put(0, "_ImportationCollaborateur");

    }};
    public static HashMap<Integer, String> indexSheetToMethodImportationDonneePie= new HashMap<Integer, String>() {{
        put(0, "_ImportationDonneesPie");

    }};

    public static HashMap<String, List<String>> sheetFields = new HashMap<String, List<String>>() {{
        put("_ImportationCollaborateur",
                Arrays.asList("_Societe","_Matricule", "_Actif", "_Civilite", "_Nom", "_Prenom", "_NumTelephone","_DateNaisssance", "_CIN", "_NImmatriculationCNSS","_Ville",
                        "_Adresse", "_EtatCivil","_NombreEnfant",
                        "_Email","_Identifiant","_MotDePasse",
                        "_RIB","_Banque","_AgenceBancaire",
                        "_DateDEmbauche","_TypeDeContrat","_Groupes",
                        "_Direction","_Departement","_Division",
                        "_Fonction","_Suppleant","_Site","_SuperieurHierarchique",
                        "_DateSortie","_MotifSortie",

                        "_TypeDeSalaire","_TauxHoraire","_SalaireDeBase","_SalaireBrut","_SalaireNET","_MontantMensuelNoteFrais",
                        "_NMutuelle", "_NomMutuelle","_TauxMutuelle"/*,"_NCIMR"*/,"_NomRetraite","_TauxRetraire","_NumeroAttribue",
                        "_DroitMensuel","_SoldeConges"));

    }};
    public static HashMap<String, List<String>> sheetFields2 = new HashMap<String, List<String>>() {{
        put("_ImportationCollaborateur",
                Arrays.asList("_Nom","_Prenom", "_SuperieurHierarchique", "_DroitMensuel","_SoldeAnneeEnCours","_SoldeAnterieur","_SoldeConges"));

    }};

    public static HashMap<Integer, List<String>> titlesMaquetteImportationDonneePieVerification = new HashMap<Integer, List<String>>() {{
        put(0, Arrays.asList("Matricule","Nom","Prénom","Type de salaire","Taux horaire (DH)","Salaire de base (DH)","Salaire Brut (DH)",
                "Salaire NET (DH)","Droit mensuel","Solde année en cours","Solde antérieur","Solde congés"));
    }};

    public static HashMap<String, List<String>> sheetFieldsImportationDonneePie = new HashMap<String, List<String>>() {{
        put("_ImportationDonneesPie",
                Arrays.asList("_Matricule","_Nom","_Prenom","_TypeDeSalaire","_TauxHoraire","_SalaireDeBase","_SalaireBrut",
                        "_SalaireNET","_DroitMensuel","_SoldeAnneeEnCours","_SoldeAnterieur","_SoldeConges"));
    }};

    public static HashMap<String, List<String>> sheetFieldsImportationCongesCollaborateurs = new HashMap<String, List<String>>() {{
        put("_ImportationCongesCollaborateurs",
                Arrays.asList(
                        "_Societe",
                        "_Matricule",
                        "_Nom",
                        "_Prenom",
                        "_CIN",
                        "_DroitMensuelle",
                       /* "_SoldeAnneeEnCours",
                        "_SoldeAnterieur",*/
                        "_SoldeConges"));
    }};

}
