package com.moovapps.Formation.SuiviProgrammeDeFormation.document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.framework.composites.IDocumentComposite;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.widgets.blocks.sys.layouts.FormSectionBlock;
import com.moovapps.Formation.SuviFormation.document.CummonSuivi;

import java.util.*;
import java.util.stream.Collectors;

public class QuestionnaireFroid extends CummonSuivi {
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
    public String getNotationField() {
        return "Notation0100AFroid";
    }
    @Override
    public String getTableauDesNotes() {
        return "NotationDeChaqueCompetence2";
    }
    @Override
    public String getPourcentageMinimumPourAcquisitionDuCompetence(){
        return "PourcentageMinimumPourAcquisitionDeLaCompetenceQuestionnaireAFroid";
    }

    @Override
    public String getIDToUSe() {
        return "IDProgrammeQuestionFroid";
    }

    @Override
    public void setConfirmer(IStorageResource competenceSalerie) {
        competenceSalerie.setValue("ConfirmeCompetence", true);
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        /*if (action.getName().equals("FinirLaQuestionnaire2")) {
            calculateNotation();
            setCompetence();

        }*/
        if (action.getName().equals("FinirLaQuestionnaire2")) {
            if (getWorkflowInstance().getValue("SouhaitezVousAcquerirLaCompetencePar").equals("Attestation")) {
                calulateNotationSiParAttestation();
            }else {
                calculateNotation() ;
            }
        }

        //return false ;
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

    private IResourceDefinition getQuestionReponseDefinition() {
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions_Reponses");
            return definition;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setQuestionsReponse() {
        Collection<IStorageResource> QuestionsReponse = getQuestionsReponseByID(getIDToUSe(), null);
        if (QuestionsReponse.isEmpty()) {
            Collection<IStorageResource> earnedCompetences = getEarnedCompetences();
            Collection<IStorageResource> questions = getQuestionsByID(getIDToUSe());
            for (IStorageResource question : questions) {
                List<IStorageResource> earnedCompetencesInThisQuestion = earnedCompetences.stream().filter(e -> ((ArrayList<IStorageResource>) question.getValue("Competence")).contains(e)).collect(Collectors.toList());
                if (!((ArrayList<IStorageResource>) question.getValue("Competence")).stream().filter(e -> earnedCompetences.contains(e)).collect(Collectors.toList()).isEmpty()) {
                    try {
                        IResourceDefinition definition = getQuestionReponseDefinition();
                        IStorageResource questionReponse = getWorkflowModule().createStorageResource(getWorkflowModule().getSysadminContext(), definition, "", "");
                        questionReponse.setValue("Question", question);
                        questionReponse.setValue("IDProgrammeQuestionFroid", question.getValue("IDProgrammeQuestionFroid"));
                        questionReponse.setValue("Poids", question.getValue("Poids"));
                        questionReponse.setValue("ReponseS", question.getValue("ReponseS"));
                        questionReponse.setValue("Competence", earnedCompetencesInThisQuestion);
                        questionReponse.setValue("IDSeanceParticipant", getWorkflowInstance().getValue("IDSeanceParticipant"));
                        questionReponse.save(getWorkflowModule().getSysadminContext());
                        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid").equals("Oui") &&
                        getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAChaux").equals("Non") ||
                        getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAChaux") == null ){
                    try {
                        IResourceDefinition definition = getQuestionReponseDefinition();
                        IStorageResource questionReponse = getWorkflowModule().createStorageResource(getWorkflowModule().getSysadminContext(), definition, "", "");
                        questionReponse.setValue("Question", question);
                        questionReponse.setValue("IDProgrammeQuestionFroid", question.getValue("IDProgrammeQuestionFroid"));
                        questionReponse.setValue("Poids", question.getValue("Poids"));
                        questionReponse.setValue("ReponseS", question.getValue("ReponseS"));
                        questionReponse.setValue("Competence", question.getValue("Competence"));
                        questionReponse.setValue("IDSeanceParticipant", getWorkflowInstance().getValue("IDSeanceParticipant"));
                        questionReponse.save(getWorkflowModule().getSysadminContext());
                        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

        private Collection<IStorageResource> getEarnedCompetences () {
            ArrayList<ILinkedResource> earnedCompetenceTD = (ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("NotationDeChaqueCompetence");
            return earnedCompetenceTD.stream().filter(e -> ((Number) e.getValue("Notation")).intValue() > ((Number) e.getValue("PourcentageMinimumPourAcquisitionDeLaCompetence")).intValue()).map(e -> (IStorageResource) e.getValue("Competence")).collect(Collectors.toList());
        }


        private IStorageResource getFicheByUser (IUser user){
            try {
                IContext context = getWorkflowModule().getSysadminContext();
                IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
                IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
                ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
                IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
                controller.addEqualsConstraint("Salarie", user);
                return (IStorageResource) controller.evaluate(definition).iterator().next();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        private IStorageResource getCompetenceSalarie (IStorageResource competence, IStorageResource ficheCollaborateur)
        {
            IStorageResource competenceSalerie = null;
            try {
                IContext context = getWorkflowModule().getSysadminContext();
                IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
                ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
                IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "CompetenceSalarie");
                IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
                controller.addEqualsConstraint("Collaborateur", ficheCollaborateur);
                controller.addEqualsConstraint("Competence", competence);
                ArrayList<IStorageResource> competenesDuSalarie = (ArrayList<IStorageResource>) controller.evaluate(definition);

                if (!competenesDuSalarie.isEmpty()) {
                    competenceSalerie = competenesDuSalarie.iterator().next();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return competenceSalerie;
        }

    public Collection<IWorkflowInstance> getCoursParParticipantByIDSession() {
        Collection<IWorkflowInstance> cours = Collections.emptyList();
        try {
            IContext sysContext = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(sysContext, "Formation", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "Formation", project);
            IWorkflow w = Modules.getWorkflowModule().getWorkflow(sysContext, catalog, "CoursParParticipant_1.0");
            IViewController controller = Modules.getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("Participant2", getWorkflowInstance().getValue("Participant2"));
            controller.addEqualsConstraint("EstCeQueCeCoursComprendUnQuiz", "Oui");
            controller.addEqualsConstraint("DocumentState", "Clôturé(e)");
            controller.addEqualsConstraint("IDSession", getWorkflowInstance().getValue("IDSession"));
            cours = controller.evaluate(w);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cours;
    }




    }

