package com.moovapps.Formation.SuiviProgrammeDeFormation.document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.framework.composites.IDocumentComposite;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.widgets.blocks.sys.layouts.FormSectionBlock;
import com.moovapps.Formation.SuviFormation.document.CummonSuivi;

import java.util.*;

public class QuestionnaireChaud extends CummonSuivi {
    @Override
    public boolean onAfterLoad() {

        setQuestionsReponse();
        if (getWorkflowInstance().getValue("SouhaitezVousAcquerirLaCompetencePar").equals("Attestation")){
            setTableNotation();
        }
        refreshSection("Questions_Reponses");
        return super.onAfterLoad();
    }

    @Override
    public String getTableauDesNotes() {
        return "NotationDeChaqueCompetence";
    }
    @Override
    public String getPourcentageMinimumPourAcquisitionDuCompetence(){
        return "PourcentageMinimumPourAcquisitionDuCompetence";
    }

    @Override
    public String getIDToUSe() {
        return "IDProgramme";
    }
    @Override
    public String getNotationField() {
        return "Notation0100";
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("FinirLaQuestionnaire")) {
            if (getWorkflowInstance().getValue("SouhaitezVousAcquerirLaCompetencePar").equals("Attestation")) {
                calulateNotationSiParAttestation();
            }else {
                calculateNotation() ;
            }
            if (getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid") != null && getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid").equals("Oui")) {
                if(getWorkflowInstance().getValue("DateDeDemarrageDuQuestionnaireAFroid") == null){
                    Calendar dateDeDemarrage = Calendar.getInstance();
                    dateDeDemarrage.add(Calendar.MONTH, getWorkflowInstance().getValue("DureeDuQuestionnaireAFroidMois") != null ? ((Number) getWorkflowInstance().getValue("DureeDuQuestionnaireAFroidMois")).intValue() : 3);
                    getWorkflowInstance().setValue("DateDeDemarrageDuQuestionnaireAFroid", dateDeDemarrage.getTime());
                    getWorkflowInstance().save("DateDeDemarrageDuQuestionnaireAFroid");
                }
            }
        }

//        if (action.getName().equals("FinirLaQuestionnaire")) {
//
//            calculateNotation();
//            setCompetence();
//            if (getWorkflowInstance().getValue("DateDeDemarrageDuQuestionnaireAFroid")==null){
//                Calendar dateDeDemarrage =Calendar.getInstance();
//                dateDeDemarrage.add(Calendar.MONTH ,((Number)getWorkflowInstance().getValue("DureeDuQuestionnaireAFroidMois")).intValue());
//                getWorkflowInstance().setValue("DateDeDemarrageDuQuestionnaireAFroid",dateDeDemarrage.getTime());
//            }
//        }

       return super.onBeforeSubmit(action);
    }
    private void refreshSection(String sectionName) {
        IDocumentComposite documentComposite = null;
        try {
            documentComposite = (IDocumentComposite) Navigator.getNavigator().getCurrentNavigation();
        } catch (ClassCastException e) {
            documentComposite = (IDocumentComposite) Navigator.getNavigator().getRootNavigator().getPartByName("ezs").getCurrentNavigation();
        }
        //documentComposite.getBody().getViews().iterator().next().refresh();

        List<FormSectionBlock> Sections = documentComposite.getBody().getSections();
        Sections.forEach(section -> {
            if (section.getTitle() != null && section.getTitle().equals(sectionName)) {
                section.refresh();
            }
        });
    }

    private void setQuestionsReponse() {
        Collection<IStorageResource> QuestionsReponse = getQuestionsReponseByID(getIDToUSe(), null);
        if (QuestionsReponse.isEmpty()) {
            Collection<IStorageResource> Questions = getQuestionsByID(getIDToUSe());
            for (IStorageResource question : Questions) {
                try {
                    IContext context = getWorkflowModule().getSysadminContext();
                    IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                    IProject project = getProjectModule().getProject(context, "Formation", organization);
                    ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
                    IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions_Reponses");
                    IStorageResource questionReponse = getWorkflowModule().createStorageResource(context, definition, "", "");
                    questionReponse.setValue("Question", question);
                    questionReponse.setValue("IDProgramme", question.getValue("IDProgramme"));
                    questionReponse.setValue("Poids", question.getValue("Poids"));
                    questionReponse.setValue("ReponseS", question.getValue("ReponseS"));
                    questionReponse.setValue("IDSeanceParticipant", getWorkflowInstance().getValue("IDSeanceParticipant"));
                    questionReponse.setValue("Competence", question.getValue("Competence"));
                    questionReponse.save(getWorkflowModule().getSysadminContext());
                    getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    public Collection<IWorkflowInstance> getCoursParParticipantByIDProgramme(IWorkflowInstance instance) {
        Collection<IWorkflowInstance> cours = Collections.emptyList();
        try {
            IContext sysContext = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(sysContext, "Formation", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "Formation", project);
            IWorkflow w = Modules.getWorkflowModule().getWorkflow(sysContext, catalog, "CoursParParticipant_1.0");
            IViewController controller = Modules.getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("Participant2", instance.getValue("Participant2"));
            controller.addEqualsConstraint("EstCeQueCeCoursComprendUnQuiz", "Oui");
            controller.addEqualsConstraint("DocumentState", "Clôturé(e)");
            controller.addEqualsConstraint("IDProgramme", instance.getValue("IDProgramme"));
            cours = controller.evaluate(w);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cours;
    }


}
