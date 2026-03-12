package com.moovapps.EVALUATION.Helpers;

public class ObjectifModel {
    String Objectif ;
    String Resultat ;
    String Explication ;

    public ObjectifModel(String objectif, String resultat, String explication) {
        Objectif = objectif;
        Resultat = resultat;
        Explication = explication;
    }

    public String getObjectif() {
        return Objectif;
    }

    public void setObjectif(String objectif) {
        Objectif = objectif;
    }

    public String getResultat() {
        return Resultat;
    }

    public void setResultat(String resultat) {
        Resultat = resultat;
    }

    public String getExplication() {
        return Explication;
    }

    public void setExplication(String explication) {
        Explication = explication;
    }
}
