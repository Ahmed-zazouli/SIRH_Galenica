package com.moovapps.ibb.rh.Cummon.Agents;

import com.axemble.vdoc.sdk.interfaces.IStorageResource;

import java.util.Objects;

public class MatriculeEtablissementModel {
    String matricule;
    IStorageResource etablissement ;

   public MatriculeEtablissementModel(String mat,IStorageResource etablissement){
        this.matricule = mat;
        this.etablissement = etablissement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatriculeEtablissementModel)) return false;
        MatriculeEtablissementModel that = (MatriculeEtablissementModel) o;
        return Objects.equals(matricule, that.matricule) && Objects.equals(etablissement, that.etablissement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matricule, etablissement);
    }
}
