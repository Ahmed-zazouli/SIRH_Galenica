package com.moovapps.Formation.TableDynamique;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;

import java.util.ArrayList;

public class NiveauDeCompetencePourPasserLaFormation extends BaseDocumentExtension {
    @Override
    public boolean onAfterLoad() {
        if (getWorkflowInstance().getParentInstance().getValue("Activite") != null) {
            getWorkflowInstance().setValue("Activite", getWorkflowInstance().getParentInstance().getValue("Activite"));
        }
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSave() {
        return super.onBeforeSave();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("CompetenceCollaborateur")){
            setParticipantsParCompetenceCollaborateur();
        }
        super.onPropertyChanged(property);
    }

    private void setParticipantsParCompetenceCollaborateur() {
        ArrayList<IStorageResource> competencesCollaborateur = (ArrayList<IStorageResource>) getWorkflowInstance().getValue("CompetenceCollaborateur");
        ArrayList<IStorageResource> participants = new ArrayList<>();
        for (IStorageResource competenceCollaborateur : competencesCollaborateur ){
            participants.add((IStorageResource) competenceCollaborateur.getValue("Collaborateur"));
        }
        getWorkflowInstance().setValue("Participants",participants);
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());

    }
}
