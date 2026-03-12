package com.moovapps.EVALUATION.Models;

import java.util.ArrayList;

public class NotesModel {
    private String id;
    private String parentID;
    private String name;
    private ArrayList<Float> notes;

    public NotesModel(String id, String parentID, String name, ArrayList<Float> notes) {
        this.id = id;
        this.parentID = parentID;
        this.name = name;
        this.notes = notes;
    }

    public NotesModel() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
