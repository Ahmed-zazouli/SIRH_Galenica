package com.moovapps.EVALUATION.ButtonsHTML;

import java.util.ArrayList;

public class CompetenceModel {
    String id;
    String parentID;
    String name ;
    String categorie ;

    public CompetenceModel(String id, String parentID, String name, ArrayList<Float> notes, ArrayList<CompetenceModel> childs , String categorie) {
        this.id = id;
        this.parentID = parentID;
        this.name = name;
        this.notes = notes;
        this.childs = childs;
        this.categorie = categorie;
    }
    public CompetenceModel(){

    }

    ArrayList<Float> notes ;
    ArrayList<CompetenceModel> childs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Float> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<Float> notes) {
        this.notes = notes;
    }

    public ArrayList<CompetenceModel> getChilds() {
        return childs;
    }

    public void setChilds(ArrayList<CompetenceModel> childs) {
        this.childs = childs;
    }
}
