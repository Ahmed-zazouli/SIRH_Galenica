package com.moovapps.Formation.Programme.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdp.ui.framework.composites.IDocumentComposite;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.widgets.blocks.sys.layouts.FormSectionBlock;

import java.util.List;
import java.util.UUID;

public class Programme extends BaseDocumentExtension {

    @Override
    public boolean onAfterLoad() {
        getWorkflowInstance().setValue("Societe", getWorkflowInstance().getParentInstance().getValue("Societe"));
        getWorkflowInstance().setValue("Departement",getWorkflowInstance().getParentInstance().getValue("Departement"));
        getWorkflowInstance().setValue("Division",getWorkflowInstance().getParentInstance().getValue("Division"));
        getWorkflowInstance().setValue("ProfilS",getWorkflowInstance().getParentInstance().getValue("ProfilS"));
  //      getWorkflowInstance().setValue("Pole", getWorkflowInstance().getParentInstance().getValue("Pole"));
       // getWorkflowInstance().setValue("Activite", getWorkflowInstance().getParentInstance().getValue("Activite"));
        getWorkflowInstance().setValue("PlanFormationProcessus", getWorkflowInstance().getParentInstance());
        if(getWorkflowInstance().getValue("IDProgramme") == null){
            getWorkflowInstance().setValue("IDProgramme", UUID.randomUUID().toString());
        }
       if(getWorkflowInstance().getValue("IDProgrammeQuestionFroid") == null){
            getWorkflowInstance().setValue("IDProgrammeQuestionFroid", UUID.randomUUID().toString());
        }
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        refreshSection("Quizz à chaud");
        refreshSection("Questionnaire à froid");
        return super.onAfterLoad();
    }

    private void refreshSection(String sectionName){
        IDocumentComposite documentComposite = null;
        try {
            documentComposite = (IDocumentComposite) Navigator.getNavigator().getCurrentNavigation();
        } catch (ClassCastException e) {
            documentComposite = (IDocumentComposite)Navigator.getNavigator().getRootNavigator().getPartByName("ezs").getCurrentNavigation();
        }
        //documentComposite.getBody().getViews().iterator().next().refresh();

        List<FormSectionBlock> Sections =  documentComposite.getBody().getSections();
        Sections.forEach(section ->{
            if(section.getTitle() != null && section.getTitle().equals(sectionName)){
                section.refresh();
            }
        });
    }
}
