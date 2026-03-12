package com.moovapps.ibb.rh.Script.Referentiel;

public enum NatureCommentaireExcel {

    ANOMALIE("Anomalie"),
    ALERTE("Alerte");
    public final String label;

    NatureCommentaireExcel(String label) {
        this.label = label;
    }
}
