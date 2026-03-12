package com.moovapps.Formation.SessionFormation.document;

import com.axemble.sdk.components.sys.forms.ScreenDescriptionComponent;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdoc.storage.ui.core.providers.views.ResourceViewProvider;
import com.axemble.vdp.ui.core.document.CustomSubFormController;
import com.axemble.vdp.ui.core.providers.IViewProvider;
import com.axemble.vdp.ui.framework.composites.IDocumentComposite;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.xml.XMLDocument;
import com.axemble.vdp.ui.framework.composites.xml.XMLView;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.parts.NavigationPart;
import com.axemble.vdp.ui.framework.widgets.blocks.BlockWidget;
import com.axemble.vdp.ui.framework.widgets.blocks.sys.layouts.FormSectionBlock;
import com.moovapps.Formation.Cours.document.CreationCours;


import java.lang.reflect.Field;
import java.util.*;

public class SessionFormation extends BaseDocumentExtension {
    //BaseResourceDefinitionExtension

    ArrayList<IStorageResource> participantsOld;

    @Override
    public boolean onAfterLoad() {
//        IStorageResource formation = (IStorageResource) getWorkflowInstance().getValue("Formation");
//        IStorageResource planDeFormation = (IStorageResource) formation.getValue("PlanDeFormation");
//        if(planDeFormation!=null){
//            getWorkflowInstance().setValue("PlanDeFormation",planDeFormation);
//
//       }
        if (getWorkflowInstance().getValue("IDSession") == null) {
            getWorkflowInstance().setValue("IDSession", UUID.randomUUID().toString());
            getWorkflowInstance().setValue("SouhaitezVousGererLesQuestionnaireAChaux", getWorkflowInstance().getParentInstance().getValue("SouhaitezVousGererLesQuestionnaireAChaux"));
            getWorkflowInstance().setValue("SouhaitezVousGererLesQuestionnaireAFroid", getWorkflowInstance().getParentInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid"));
        }
        if (getWorkflowInstance().getValue("IDSessionQuestionFroid") == null) {
            getWorkflowInstance().setValue("IDSessionQuestionFroid", UUID.randomUUID().toString());
        }
        getWorkflowInstance().setValue("Societe", getWorkflowInstance().getParentInstance().getValue("Societe"));
        getWorkflowInstance().setValue("Departement", getWorkflowInstance().getParentInstance().getValue("Departement"));
        getWorkflowInstance().setValue("Division", getWorkflowInstance().getParentInstance().getValue("Division"));
        getWorkflowInstance().setValue("ProfilS", getWorkflowInstance().getParentInstance().getValue("ProfilS"));
        getWorkflowInstance().setValue("FormationProcessus", getWorkflowInstance().getParentInstance());
        getWorkflowInstance().setValue("PlanFormationProcessus", getWorkflowInstance().getParentInstance().getValue("PlanFormationProcessus"));
        getWorkflowInstance().setValue("ProgrammeFormationProcessus", getWorkflowInstance().getParentInstance().getValue("ProgrammeFormationProcessus"));


        //getWorkflowInstance().setValue("MontantSessionDeFormation", getWorkflowInstance().getParentInstance().getValue("MontantFormationMAD"));
        //getWorkflowInstance().setValue("MontantSessionDeFormation", getWorkflowInstance().getParesntInstance().getValue("MontantFormationMAD"));
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        setQuestions();
        setQuestionsFroid();
        setParticipants();

        // refresh Section
        refreshSection("Quizz à chaud");
        refreshSection("Questionnaire à froid");
        participantsOld = (ArrayList<IStorageResource>) getWorkflowInstance().getValue("Participants");


        /*IDocumentComposite documentComposite = null;
            try {
                documentComposite = (IDocumentComposite) Navigator.getNavigator().getCurrentNavigation();
            } catch (ClassCastException e) {
                documentComposite = (IDocumentComposite)Navigator.getNavigator().getRootNavigator().getPartByName("ezs").getCurrentNavigation();
            }
            ((XMLDocument) documentComposite).refreshWidgets();*/

        //refrechEcrandPersonnalisableDossierTechnique();


        return super.onAfterLoad();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("Participants") || property.getName().equals("MontantSessionDeFormation")) {
            if (getWorkflowInstance().getValue("Participants") != null && getWorkflowInstance().getValue("MontantSessionDeFormation") != null) {
                ArrayList<IStorageResource> participants = (ArrayList<IStorageResource>) getWorkflowInstance().getValue("Participants");
                float size = ((Number) participants.size()).floatValue();
                float montantSessionDeFormation = ((Number) getWorkflowInstance().getValue("MontantSessionDeFormation")).floatValue();

                getWorkflowInstance().setValue("MontantParParticipantMAD", (double) (montantSessionDeFormation / size));


            }

        }
        super.onPropertyChanged(property);
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("CreerLeSession")) {
            IWorkflowInstance planFormation = (IWorkflowInstance) getWorkflowInstance().getValue("PlanFormationProcessus");
            IWorkflowInstance formation = (IWorkflowInstance) getWorkflowInstance().getValue("FormationProcessus");
            IWorkflowInstance programmeFormation = (IWorkflowInstance) getWorkflowInstance().getValue("ProgrammeFormationProcessus");

            //set the participants of formation
            if (planFormation != null && formation != null) {
                Collection<IWorkflowInstance> sesstionPlanDeFormation = getListParticipantsV2(formation, null, planFormation); // get all session with the same formation
                ArrayList<IStorageResource> emptyParticipants = new ArrayList<>();
                if (sesstionPlanDeFormation != null && !sesstionPlanDeFormation.isEmpty()) {
                    for (IWorkflowInstance session : sesstionPlanDeFormation) {
                        ArrayList<IStorageResource> participants = (ArrayList<IStorageResource>) session.getValue("Participants");
                        if (participants != null && !participants.isEmpty()) {
                            for (IStorageResource participant : participants) {
                                if (!emptyParticipants.contains(participant)) {
                                    emptyParticipants.add(participant);
                                }
                            }
                        }
                    }
                    getWorkflowInstance().getParentInstance().setValue("Participants", emptyParticipants);
                    getWorkflowInstance().getParentInstance().save(getWorkflowModule().getSysadminContext());

                }


            }

                //set the participants of Plan formation
                if (planFormation != null) {
                    Collection<IWorkflowInstance> sesstionPlanDeFormation = getListParticipantsV2(null, null, planFormation); // get all session with the same formation
                    ArrayList<IStorageResource> emptyParticipants = new ArrayList<>();
                    if (sesstionPlanDeFormation != null && !sesstionPlanDeFormation.isEmpty()) {
                        for (IWorkflowInstance session : sesstionPlanDeFormation) {
                            ArrayList<IStorageResource> participants = (ArrayList<IStorageResource>) session.getValue("Participants");
                            if (participants != null && !participants.isEmpty()) {
                                for (IStorageResource participant : participants) {
                                    if (!emptyParticipants.contains(participant)) {
                                        emptyParticipants.add(participant);
                                    }
                                }
                            }
                        }
                        getWorkflowInstance().getParentInstance().getParentInstance().getParentInstance().setValue("Participants", emptyParticipants);
                        getWorkflowInstance().getParentInstance().getParentInstance().getParentInstance().save(getWorkflowModule().getSysadminContext());
                    }

                }
                //set the participants of Programme formation
                if (programmeFormation != null) {
                    Collection<IWorkflowInstance> sesstionPlanDeFormation = getListParticipantsV2(null, programmeFormation, planFormation); // get all session with the same formation
                    ArrayList<IStorageResource> emptyParticipants = new ArrayList<>();
                    if (sesstionPlanDeFormation != null && !sesstionPlanDeFormation.isEmpty()) {
                        for (IWorkflowInstance session : sesstionPlanDeFormation) {
                            ArrayList<IStorageResource> participants = (ArrayList<IStorageResource>) session.getValue("Participants");
                            if (participants != null && !participants.isEmpty()) {
                                for (IStorageResource participant : participants) {
                                    if (!emptyParticipants.contains(participant)) {
                                        emptyParticipants.add(participant);
                                    }
                                }
                            }
                        }
                        getWorkflowInstance().getParentInstance().getParentInstance().setValue("Participants", emptyParticipants);
                        getWorkflowInstance().getParentInstance().getParentInstance().save(getWorkflowModule().getSysadminContext());
                    }

                }

            //return false;

            double MontantSessionDeFormation = ((Number) getWorkflowInstance().getValue("MontantSessionDeFormation")).floatValue();
            double MontantTotalDeFormationMAD = ((Number) getWorkflowInstance().getParentInstance().getValue("MontantTotalDeFormationMAD")).floatValue();
            double Total = MontantTotalDeFormationMAD + MontantSessionDeFormation;
            getWorkflowInstance().getParentInstance().setValue("MontantTotalDeFormationMAD", Total);
            getWorkflowInstance().getParentInstance().save(getWorkflowModule().getSysadminContext());

            createSuiviProgrammeDeFormationAndSuiviDeFormation();
            createSeancePraticipants();
        }

        if (action.getName().equals("ReturnPourModification")) {
            double MontantSessionDeFormation = ((Number) getWorkflowInstance().getValue("MontantSessionDeFormation")).floatValue();
            double MontantTotalDeFormationMAD = ((Number) getWorkflowInstance().getParentInstance().getValue("MontantTotalDeFormationMAD")).floatValue();
            double Total = MontantTotalDeFormationMAD - MontantSessionDeFormation;
            getWorkflowInstance().getParentInstance().setValue("MontantTotalDeFormationMAD", Total);
        }
       // return false;
        return super.onBeforeSubmit(action);
    }

    private void createSeancePraticipants() {
        ArrayList<IStorageResource> participants = (ArrayList<IStorageResource>) getWorkflowInstance().getValue("Participants");
        ArrayList<IWorkflowInstance> seances = getSeanceBySession();
        for (IWorkflowInstance seance : seances) {
            seance.setValue("Participants", participants);
            seance.save(getWorkflowModule().getSysadminContext());
            try {
                for (IStorageResource participant : participants) {

                    IWorkflowInstance seanceParParticipant = getSeanceParticipantsByIDSeance((IUser) participant.getValue("Salarie"), (String) seance.getValue("IDSeance"));
                    IWorkflowInstance coursParParticipant = null;
                    if (seanceParParticipant == null) {
                        IWorkflow coursParParticipantWorkflow = getWorkflowModule().getWorkflow(getWorkflowModule().getSysadminContext(), getWorkflowInstance().getCatalog(), "CoursParParticipant_1.0");
                        coursParParticipant = getWorkflowModule().createWorkflowInstance(getDirectoryModule().getSysadminContext(), coursParParticipantWorkflow, "");

                        coursParParticipant.setValue("Societe", seance.getValue("Societe"));
                        coursParParticipant.setValue("IDSession", seance.getValue("IDSession"));
                        String IDSeancePartcicipant = UUID.randomUUID().toString();
                        coursParParticipant.setValue("IDSeanceParticipant", IDSeancePartcicipant);
                        coursParParticipant.setValue("IDSeance", seance.getValue("IDSeance"));
                        coursParParticipant.setValue("PlanFormationProcessus", seance.getValue("PlanFormationProcessus"));
                        coursParParticipant.setValue("ProgrammeFormationProcessus", seance.getValue("ProgrammeFormationProcessus"));
                        coursParParticipant.setValue("FormationProcessus", seance.getValue("FormationProcessus"));
                        coursParParticipant.setValue("SessionDeFormationProcessus", seance.getValue("SessionDeFormationProcessus"));
                        coursParParticipant.setValue("SeanceDeFormationProcessus", seance);//getSeanceDeFormationParReference((String) seance.getValue("sys_Reference")));
                        coursParParticipant.setValue("DateDeDebut", seance.getValue("DateDebut"));//getSeanceDeFormationParReference((String) seance.getValue("sys_Reference")));
                        coursParParticipant.setValue("HeureMinuteDebut", seance.getValue("HeureMinuteDebut"));
                        coursParParticipant.setValue("HeureMinuteFin", seance.getValue("HeureMinuteFin"));


                        coursParParticipant.setValue("EstCeQueCeCoursComprendUnQuiz", seance.getValue("EstCeQueCeCoursComprendUnQuiz"));
                        coursParParticipant.setValue("QuizPJ", seance.getValue("QuizPJ"));
                        coursParParticipant.setValue("RessourcesDuCours", new CreationCours().resourceDuCours(seance, "RessourcesDuCours"));
                        coursParParticipant.setValue("Commentaire", seance.getValue("Commentaire"));
                        coursParParticipant.setValue("Participant2", participant.getValue("Salarie"));
                        coursParParticipant.setValue("Active", true);

                        IUser validateur = null;
                        if (getWorkflowInstance().getValue("NatureDeFormateur").equals("Interne")) {
                            validateur = (IUser) getWorkflowInstance().getValue("FormateurInterne");
                        } else {
                            validateur = (IUser) getWorkflowInstance().getValue("RepresentantFormateur");
                        }
                        coursParParticipant.setValue("Validateur2", validateur);
                        coursParParticipant.save(getWorkflowModule().getSysadminContext());

                        seance.addLinkedWorkflowInstance("CoursParParticipant", coursParParticipant);
                        seance.save(getWorkflowModule().getSysadminContext());
                    } else {
                        seanceParParticipant.setValue("Active", true);
                    }
                }

                for (IStorageResource participantold : participantsOld) {
                    if (!participants.contains(participantold)) {
                        IWorkflowInstance seanceParParticipant = getSeanceParticipantsByIDSeance((IUser) participantold.getValue("Salarie"), (String) seance.getValue("IDSeance"));
                        if (seanceParParticipant != null) {
                            seanceParParticipant.setValue("Active", false);
                            seanceParParticipant.save(getWorkflowModule().getSysadminContext());
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private IWorkflowInstance getSeanceParticipantsByIDSeance(IUser salarie, String idSeance) {
        IWorkflowInstance seanceParticipants = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Formation", project);
            IWorkflow w = getWorkflowModule().getWorkflow(context, catalog, "CoursParParticipant_1.0");
            IViewController controller = getWorkflowModule().getViewController(context);
            controller.addEqualsConstraint("IDSeance", idSeance);
            controller.addEqualsConstraint("Participant2", salarie);
            if (!controller.evaluate(w).isEmpty()) {
                seanceParticipants = (IWorkflowInstance) controller.evaluate(w).iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return seanceParticipants;
    }

    private ArrayList<IWorkflowInstance> getSeanceBySession() {
        ArrayList<IWorkflowInstance> seances = new ArrayList<>();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Formation", project);
            IWorkflow w = getWorkflowModule().getWorkflow(context, catalog, "Cours_1.0");
            IViewController controller = getWorkflowModule().getViewController(context);
            controller.addEqualsConstraint("SessionDeFormationProcessus.id", getWorkflowInstance().getId().toString());
            seances = (ArrayList<IWorkflowInstance>) controller.evaluate(w);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return seances;
    }

    private void setParticipants() {
        ArrayList<ILinkedResource> niveauDeCompetencePourPasserLaFormation = (ArrayList<ILinkedResource>) getWorkflowInstance().getParentInstance().getValue("NiveauDeCompetencePourPasserLaFormation");
        Collection<IStorageResource> participants = new ArrayList<>();
        if (niveauDeCompetencePourPasserLaFormation != null) {
            for (ILinkedResource niveauCompetence : niveauDeCompetencePourPasserLaFormation) {
                participants.addAll((Collection<? extends IStorageResource>) niveauCompetence.getValue("Participants"));
            }
            getWorkflowInstance().setValue("Participants3", participants);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        }
    }

    private void setQuestions() {
        Collection<IStorageResource> QuestionsSession = getQuestionsByIDSession("IDSession");
        if (QuestionsSession.isEmpty()) {
            Collection<IStorageResource> QuestionsFromation = getQuestionsByIDFormation("IDFormation");
            for (IStorageResource questionFormation : QuestionsFromation) {
                try {
                    ArrayList<IStorageResource> questionFormationReponses = (ArrayList<IStorageResource>) questionFormation.getValue("ReponseS");
                    IContext sysadminContext = getWorkflowModule().getSysadminContext();
                    IOrganization organization = getDirectoryModule().getOrganization(sysadminContext, "DefaultOrganization");
                    IProject project = getProjectModule().getProject(sysadminContext, "Formation", organization);
                    ICatalog catalog = getWorkflowModule().getCatalog(sysadminContext, "Referentiels", ICatalog.IType.STORAGE, project);
                    IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysadminContext, catalog, "Questions");
                    IStorageResource questionSession = getWorkflowModule().createStorageResource(sysadminContext, definition, "", "");
                    questionSession.setValue("sys_Title", questionFormation.getValue("sys_Title"));
                    questionSession.setValue("IDSession", getWorkflowInstance().getValue("IDSession"));
                    questionSession.setValue("Poids", questionFormation.getValue("Poids"));
                    questionSession.setValue("Competence", questionFormation.getValue("Competence"));
                    questionSession.save(getWorkflowModule().getSysadminContext());

                    Collection<IStorageResource> choixFormation = getChoixByQuestion(questionFormation);
                    ArrayList<IStorageResource> questionSessionReponses = new ArrayList<>();
                    for (IStorageResource choix : choixFormation) {
                        IResourceDefinition definitionChoix = getWorkflowModule().getResourceDefinition(sysadminContext, catalog, "Choix");
                        IStorageResource QuestionsChoix = getWorkflowModule().createStorageResource(sysadminContext, definitionChoix, "", "");
                        QuestionsChoix.setValue("Question", questionSession);
                        QuestionsChoix.setValue("CodeChoix", choix.getValue("CodeChoix"));
                        QuestionsChoix.setValue("sys_Title", choix.getValue("sys_Title"));
                        QuestionsChoix.setValue("Choix", choix.getValue("Choix"));
                        if (questionFormationReponses.contains(choix)) {
                            questionSessionReponses.add(QuestionsChoix);
                        }
                        QuestionsChoix.save(getWorkflowModule().getSysadminContext());
                    }
                    questionSession.setValue("ReponseS", questionSessionReponses);
                    questionSession.save(sysadminContext);
                    getWorkflowInstance().save(sysadminContext);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private Collection<IStorageResource> getChoixByQuestion(IStorageResource question) {
        Collection<IStorageResource> Choix = Collections.emptyList();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Choix");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("Question", question);
            Choix = controller.evaluate(definition);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Choix;
    }

    private void setQuestionsFroid() {
        Collection<IStorageResource> QuestionsSession = getQuestionsByIDSession("IDSessionQuestionFroid");
        if (QuestionsSession.isEmpty()) {
            Collection<IStorageResource> QuestionsFromation = getQuestionsByIDFormation("IDFormationQuestionFroid");
            for (IStorageResource questionFormation : QuestionsFromation) {
                try {
                    ArrayList<IStorageResource> questionFormationReponses = (ArrayList<IStorageResource>) questionFormation.getValue("ReponseS");
                    IContext sysadminContext = getWorkflowModule().getSysadminContext();
                    IOrganization organization = getDirectoryModule().getOrganization(sysadminContext, "DefaultOrganization");
                    IProject project = getProjectModule().getProject(sysadminContext, "Formation", organization);
                    ICatalog catalog = getWorkflowModule().getCatalog(sysadminContext, "Referentiels", ICatalog.IType.STORAGE, project);
                    IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysadminContext, catalog, "Questions");
                    IStorageResource questionSession = getWorkflowModule().createStorageResource(sysadminContext, definition, "", "");
                    questionSession.setValue("sys_Title", questionFormation.getValue("sys_Title"));
                    questionSession.setValue("IDSessionQuestionFroid", getWorkflowInstance().getValue("IDSessionQuestionFroid"));
                    questionSession.setValue("Poids", questionFormation.getValue("Poids"));
                    questionSession.setValue("Competence", questionFormation.getValue("Competence"));
                    questionSession.save(getWorkflowModule().getSysadminContext());

                    Collection<IStorageResource> choixFormation = getChoixByQuestion(questionFormation);
                    ArrayList<IStorageResource> questionSessionReponses = new ArrayList<>();
                    for (IStorageResource choix : choixFormation) {
                        IResourceDefinition definitionChoix = getWorkflowModule().getResourceDefinition(sysadminContext, catalog, "Choix");
                        IStorageResource QuestionsChoix = getWorkflowModule().createStorageResource(sysadminContext, definitionChoix, "", "");
                        QuestionsChoix.setValue("Question", questionSession);
                        QuestionsChoix.setValue("CodeChoix", choix.getValue("CodeChoix"));
                        QuestionsChoix.setValue("sys_Title", choix.getValue("sys_Title"));
                        QuestionsChoix.setValue("Choix", choix.getValue("Choix"));
                        if (questionFormationReponses.contains(choix)) {
                            questionSessionReponses.add(QuestionsChoix);
                        }
                        QuestionsChoix.save(getWorkflowModule().getSysadminContext());
                    }
                    questionSession.setValue("ReponseS", questionSessionReponses);
                    questionSession.save(sysadminContext);
                    getWorkflowInstance().save(sysadminContext);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        /*Collection<IStorageResource> QuestionsSession = getQuestionsByIDSession("IDSessionQuestionFroid");
        if (QuestionsSession.isEmpty()) {
            Collection<IStorageResource> QuestionsFromation = getQuestionsByIDFormation("IDFormationQuestionFroid");
            for (IStorageResource question : QuestionsFromation) {
                try {
                    IContext context = getWorkflowModule().getSysadminContext();
                    IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                    IProject project = getProjectModule().getProject(context, "Formation", organization);
                    ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
                    IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions");
                    IStorageResource questionReponse = getWorkflowModule().createStorageResource(context, definition, "", "");
                    questionReponse.setValue("sys_Title", question.getValue("sys_Title"));
                    questionReponse.setValue("IDSessionQuestionFroid", getWorkflowInstance().getValue("IDSessionQuestionFroid"));
                    questionReponse.setValue("Poids", question.getValue("Poids"));
                    questionReponse.setValue("ReponseS", question.getValue("ReponseS"));
                    questionReponse.setValue("Competence",question.getValue("Competence"));
                    questionReponse.save(getWorkflowModule().getSysadminContext());
                    getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }*/
    }

    private Collection<IStorageResource> getQuestionsByIDSession(String id) {
        Collection<IStorageResource> Questions = Collections.emptyList();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint(id, getWorkflowInstance().getValue(id));
            Questions = controller.evaluate(definition);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Questions;
    }

    private Collection<IStorageResource> getQuestionsByIDFormation(String id) {
        Collection<IStorageResource> Questions = Collections.emptyList();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint(id, getWorkflowInstance().getParentInstance().getValue(id));
            Questions = controller.evaluate(definition);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Questions;
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

    public CtlAbstractView refrechEcrandPersonnalisableDossierTechnique() {
        try {
            IDocumentComposite documentComposite = null;
            try {
                documentComposite = (IDocumentComposite) Navigator.getNavigator().getCurrentNavigation();
            } catch (ClassCastException e) {
                documentComposite = (IDocumentComposite) Navigator.getNavigator().getRootNavigator().getPartByName("ezs").getCurrentNavigation();
            }
            Field fieldEntries = CustomSubFormController.class.getDeclaredField("entries");
            fieldEntries.setAccessible(true);
            List entries = (List) fieldEntries.get(documentComposite.getBody());
            for (Object entry : entries) {
                if (entry instanceof BlockWidget) {
                    BlockWidget blockWidget = (BlockWidget) entry;
                    for (IWidget widget : blockWidget.getChildrenRecursively()) {
                        if (widget instanceof ScreenDescriptionComponent) {
                            Field fieldPart = ScreenDescriptionComponent.class.getDeclaredField("part");
                            fieldPart.setAccessible(true);
                            NavigationPart navigationPart = (NavigationPart) fieldPart.get(widget);
                            if (navigationPart.getCurrentNavigation() instanceof XMLView) {
                                XMLView xmlView = (XMLView) navigationPart.getCurrentNavigation();
                                // had l statement li lta7t filter l composant li radi i t refresha
                                //if (xmlView.getName().contains("XMLViewProvider_uril://vdoc/resourceDefinitionView/DefaultOrganization/ApplicationZero/Referentiels:4/ProduitCommande/")) {
                                IViewProvider provider = ((CtlAbstractView) xmlView).getProvider();
                                if (provider instanceof ResourceViewProvider) {
                                    ((ResourceViewProvider) provider).loadModel();
                                    provider.init();
                                    provider.getColumns();
                                    ((CtlAbstractView) xmlView).refresh();
                                }
                                //return (CtlAbstractView)xmlView;
                                //}
                            }
                        }
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            LOGGER.error("Can't find form embeded view!", e);
        }
        return null;
        //throw new NoSuchElementException("Unable to find view in current form with input configuration");
    }

    private void createSuiviProgrammeDeFormationAndSuiviDeFormation() {
        try {
            ArrayList<IStorageResource> participants = (ArrayList<IStorageResource>) getWorkflowInstance().getParentInstance().getValue("Participants");

            for (IStorageResource participant : participants) {
                IUser salarie = (IUser) participant.getValue("Salarie");
                IWorkflowInstance suiviProgrammeDeFormation = getSuiviProgrammeDeFormation((IUser) participant.getValue("Salarie"));
                if (suiviProgrammeDeFormation == null) {
                    IWorkflow suiviDeFormationWorkflow = getWorkflowModule().getWorkflow(getWorkflowModule().getSysadminContext(), getWorkflowInstance().getCatalog(), "SuiviProgrammeDeFormation_1.0");
                    suiviProgrammeDeFormation = getWorkflowModule().createWorkflowInstance(getWorkflowModule().getContext((IUser) participant.getValue("Salarie")), suiviDeFormationWorkflow, "");
                }
                IWorkflowInstance suiviDeFormation = getSuiviDeFromation((IUser) participant.getValue("Salarie"));
                if (suiviDeFormation == null) {
                    IWorkflow suiviDeFormationWorkflow = getWorkflowModule().getWorkflow(getWorkflowModule().getSysadminContext(), getWorkflowInstance().getCatalog(), "SuiviDeFormation_1.1");
                    suiviDeFormation = getWorkflowModule().createWorkflowInstance(getWorkflowModule().getContext((IUser) participant.getValue("Salarie")), suiviDeFormationWorkflow, "");
                }
                String IDSeanceParticipant = UUID.randomUUID().toString();
                // create suivi programme de formation
                IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
               suiviProgrammeDeFormation.setValue("Societe", societe);
                suiviProgrammeDeFormation.setValue("PlanFormationProcessus", getWorkflowInstance().getValue("PlanFormationProcessus"));
                suiviProgrammeDeFormation.setValue("ProgrammeFormationProcessus", getWorkflowInstance().getValue("ProgrammeFormationProcessus"));
                suiviProgrammeDeFormation.setValue("FormationProcessus", getWorkflowInstance().getValue("FormationProcessus"));
                suiviProgrammeDeFormation.setValue("SessionDeFormationProcessus", getWorkflowInstance());
                suiviProgrammeDeFormation.setValue("Participant2", participant.getValue("Salarie"));
                if (participant.getValue("HierarchicalManager") != null) {
                    suiviProgrammeDeFormation.setValue("RespHierarchique", participant.getValue("HierarchicalManager"));
                } else if (societe.getValue("ResponsableRH") != null) {
                    suiviProgrammeDeFormation.setValue("ResponsableRH2", societe.getValue("ResponsableRH"));
                }

                suiviProgrammeDeFormation.setValue("DateDeDebut", getWorkflowInstance().getValue("DateDeDebut"));
                suiviProgrammeDeFormation.setValue("DateDeFin", getWorkflowInstance().getValue("DateDeFin2"));
                suiviProgrammeDeFormation.setValue("IDSession", getWorkflowInstance().getValue("IDSession"));
                suiviProgrammeDeFormation.setValue("IDProgramme", getWorkflowInstance().getParentInstance().getParentInstance().getValue("IDProgramme"));
                suiviProgrammeDeFormation.setValue("IDProgrammeQuestionFroid", getWorkflowInstance().getParentInstance().getParentInstance().getValue("IDProgrammeQuestionFroid"));
                suiviProgrammeDeFormation.setValue("QuizEtape", getWorkflowInstance().getParentInstance().getParentInstance().getValue("QuizEtape"));
                suiviProgrammeDeFormation.setValue("SouhaitezVousGererLesQuestionnaireAChaux", getWorkflowInstance().getParentInstance().getParentInstance().getValue("SouhaitezVousGererLesQuestionnaireAChaux"));
                suiviProgrammeDeFormation.setValue("SouhaitezVousGererLesQuestionnaireAFroid", getWorkflowInstance().getParentInstance().getParentInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid"));
                suiviProgrammeDeFormation.setValue("SouhaitezVousAcquerirLaCompetencePar", getWorkflowInstance().getParentInstance().getParentInstance().getValue("SouhaitezVousAcquerirLaCompetencePar"));

                suiviProgrammeDeFormation.setValue("IDSeanceParticipant", IDSeanceParticipant);
                if (getWorkflowInstance().getParentInstance().getValue("DureeDuQuestionnaireAFroidMois") != null) {
                    suiviProgrammeDeFormation.setValue("DureeDuQuestionnaireAFroidMois", getWorkflowInstance().getParentInstance().getValue("DureeDuQuestionnaireAFroidMois"));
                }else if (getWorkflowInstance().getParentInstance().getParentInstance().getValue("DureeDuQuestionnaireAFroidMois") != null){
                    suiviProgrammeDeFormation.setValue("DureeDuQuestionnaireAFroidMois", getWorkflowInstance().getParentInstance().getParentInstance().getValue("DureeDuQuestionnaireAFroidMois"));
                }
                IUser validateur = null;
                if (getWorkflowInstance().getValue("NatureDeFormateur").equals("Interne")) {
                    validateur = (IUser) getWorkflowInstance().getValue("FormateurInterne");
                } else {
                    validateur = (IUser) getWorkflowInstance().getValue("RepresentantFormateur");
                }
                suiviProgrammeDeFormation.setValue("Formateur2", validateur);

                suiviProgrammeDeFormation.save(getWorkflowModule().getSysadminContext());
                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                ISecurityController securityController = getSecuriteController(suiviProgrammeDeFormation);
                breakInheritance(securityController);
                addManagementGroupes(securityController);
                addSpecificPermissionsProgrammeFormation(suiviProgrammeDeFormation,securityController);

                // create Suivi de Formation
                suiviDeFormation.setValue("Societe", societe);
                suiviDeFormation.setValue("PlanFormationProcessus", getWorkflowInstance().getValue("PlanFormationProcessus"));
                suiviDeFormation.setValue("ProgrammeFormationProcessus", getWorkflowInstance().getValue("ProgrammeFormationProcessus"));
                suiviDeFormation.setValue("FormationProcessus", getWorkflowInstance().getValue("FormationProcessus"));
                suiviDeFormation.setValue("SessionDeFormationProcessus", getWorkflowInstance());
                suiviDeFormation.setValue("Participant2", participant.getValue("Salarie"));
                if (participant.getValue("HierarchicalManager") != null) {
                    suiviDeFormation.setValue("RespHierarchique", (IUser)participant.getValue("HierarchicalManager"));
                } else if (societe.getValue("ResponsableRH") != null) {
                    suiviDeFormation.setValue("ResponsableRH2", societe.getValue("ResponsableRH"));
                }
                suiviDeFormation.setValue("DateDeDebut", getWorkflowInstance().getValue("DateDeDebut"));
                suiviDeFormation.setValue("DateDeFin", getWorkflowInstance().getValue("DateDeFin2"));
                suiviDeFormation.setValue("IDSession", getWorkflowInstance().getValue("IDSession"));
                suiviDeFormation.setValue("IDSessionQuestionFroid", getWorkflowInstance().getValue("IDSessionQuestionFroid"));
                suiviDeFormation.setValue("QuizEtape", getWorkflowInstance().getParentInstance().getParentInstance().getValue("QuizEtape"));
                suiviDeFormation.setValue("SouhaitezVousGererLesQuestionnaireAChaux", getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAChaux"));
                suiviDeFormation.setValue("SouhaitezVousGererLesQuestionnaireAFroid", getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid"));
                suiviDeFormation.setValue("SouhaitezVousAcquerirLaCompetencePar", getWorkflowInstance().getParentInstance().getValue("SouhaitezVousAcquerirLaCompetencePar"));

                suiviDeFormation.setValue("IDSeanceParticipant", IDSeanceParticipant);
                if (getWorkflowInstance().getParentInstance().getValue("DureeDuQuestionnaireAFroidMois") != null) {
                    suiviDeFormation.setValue("DureeDuQuestionnaireAFroidMois", getWorkflowInstance().getParentInstance().getValue("DureeDuQuestionnaireAFroidMois"));
                }else if (getWorkflowInstance().getParentInstance().getParentInstance().getValue("DureeDuQuestionnaireAFroidMois") != null){
                    suiviDeFormation.setValue("DureeDuQuestionnaireAFroidMois", getWorkflowInstance().getParentInstance().getParentInstance().getValue("DureeDuQuestionnaireAFroidMois"));
                }
                if (getWorkflowInstance().getValue("NatureDeFormateur").equals("Interne")) {
                    validateur = (IUser) getWorkflowInstance().getValue("FormateurInterne");
                } else {
                    validateur = (IUser) getWorkflowInstance().getValue("RepresentantFormateur");
                }
                suiviDeFormation.setValue("Formateur2", validateur);
                suiviDeFormation.setValue("PourcentageMinimumPourAcquerirLaCompetence", getWorkflowInstance().getParentInstance().getValue("PourcentageMinimumPourAcquerirLaCompetence"));
                suiviDeFormation.setValue("PourcentageDeQuestionnaireAChaud", getWorkflowInstance().getParentInstance().getValue("PourcentageDeQuestionnaireAChaud"));
                suiviDeFormation.setValue("PourcentageDeQuestionnaireAFroid", getWorkflowInstance().getParentInstance().getValue("PourcentageDeQuestionnaireAFroid"));
                suiviDeFormation.save(getWorkflowModule().getSysadminContext());
                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                 securityController = getSecuriteController(suiviDeFormation);
                breakInheritance(securityController);
                addManagementGroupes(securityController);
                addSpecificPermissionsFormation(suiviDeFormation,securityController);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private IWorkflowInstance getSuiviDeFromation(IUser salarie) {
        IWorkflowInstance suiviFormation = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Formation", project);
            IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "SuiviDeFormation_1.1");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("IDSession", getWorkflowInstance().getValue("IDSession"));
            // controller.addEqualsConstraint("PlanFormationProcessus.id",((IWorkflowInstance) getWorkflowInstance().getValue("PlanFormationProcessus")).getId().toString() );
            controller.addEqualsConstraint("Participant2", salarie);
            if (!controller.evaluate(w).isEmpty()) {
                suiviFormation = (IWorkflowInstance) controller.evaluate(w).iterator().next();
            }

            return suiviFormation;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private IWorkflowInstance getSuiviProgrammeDeFormation(IUser salarie) {
        IWorkflowInstance suiviProgrammeDeFormation = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Formation", project);
            IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "SuiviProgrammeDeFormation_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("IDSession", getWorkflowInstance().getValue("IDSession"));
            // controller.addEqualsConstraint("PlanFormationProcessus.id",((IWorkflowInstance) getWorkflowInstance().getValue("PlanFormationProcessus")).getId().toString() );
            controller.addEqualsConstraint("Participant2", salarie);
            if (!controller.evaluate(w).isEmpty()) {
                suiviProgrammeDeFormation = (IWorkflowInstance) controller.evaluate(w).iterator().next();
            }

            return suiviProgrammeDeFormation;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean onAfterSave() {

//        IStorageResource formation = (IStorageResource) getWorkflowInstance().getValue("Formation");
//        IStorageResource planDeFormation = (IStorageResource) formation.getValue("PlanDeFormation");
//        if (planDeFormation != null) {
//            ArrayList<IStorageResource> sesstionPlanDeFormation = getListParticipants(planDeFormation); // get all session with the same formation
//            ArrayList<IStorageResource> emptyParticipants = new ArrayList<>();
//            if (sesstionPlanDeFormation != null && !sesstionPlanDeFormation.isEmpty()) {
//                for (IStorageResource session : sesstionPlanDeFormation) {
//                    ArrayList<IStorageResource> participants = (ArrayList<IStorageResource>) session.getValue("Participants");
//                    if (participants != null && !participants.isEmpty()) {
//                        for (IStorageResource participant : participants) {
//                            if (!emptyParticipants.contains(participant)) {
//                                emptyParticipants.add(participant);
//                            }
//                        }
//                    }
//                }
//                planDeFormation.setValue("Participants", emptyParticipants);
//                planDeFormation.save(getWorkflowModule().getSysadminContext());
//            }
//
//        }
//
//        if (formation != null) {
//            ArrayList<IStorageResource> sesstionFormation = getListParticipants(formation); // get all session with the same formation
//            ArrayList<IStorageResource> emptyParticipants = new ArrayList<>();
//            if (sesstionFormation != null && !sesstionFormation.isEmpty()) {
//                for (IStorageResource sestion : sesstionFormation) {
//                    ArrayList<IStorageResource> participants = (ArrayList<IStorageResource>) sestion.getValue("Participants");
//                    if (participants != null && !participants.isEmpty()) {
//                        for (IStorageResource participant : participants) {
//                            if (!emptyParticipants.contains(participant)) {
//                                emptyParticipants.add(participant);
//                            }
//                        }
//                    }
//                }
//                formation.setValue("Participants", emptyParticipants);
//                formation.save(getWorkflowModule().getSysadminContext());
//            }
//
//        }
        return super.onAfterSave();
    }

    public Collection<IWorkflowInstance> getListParticipantsV2(IWorkflowInstance Pformation, IWorkflowInstance PprogrammeFormation, IWorkflowInstance PplanFromation) {
        Collection<IWorkflowInstance> Participants = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Formation", project);
            IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "SessionDeFormation_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            if (PprogrammeFormation != null) {
                controller.addEqualsConstraint("PlanFormationProcessus.id", PplanFromation.getId().toString());
                controller.addEqualsConstraint("ProgrammeFormationProcessus.id", PprogrammeFormation.getId().toString());
            } else if (Pformation != null) {
                controller.addEqualsConstraint("FormationProcessus.id", Pformation.getId().toString());
                controller.addEqualsConstraint("PlanFormationProcessus.id", PplanFromation.getId().toString());
            } else {
                controller.addEqualsConstraint("PlanFormationProcessus.id", PplanFromation.getId().toString());
            }

            Participants = controller.evaluate(w);
            return Participants;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public ArrayList<IStorageResource> getListParticipants(IStorageResource formation) {
        ArrayList<IStorageResource> Participants = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", 4, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "SessionDeFormation");
            if (formation.getValue("NatureFormation") != null) {
                controller.addEqualsConstraint("Formation", formation);
            } else {
                controller.addEqualsConstraint("PlanDeFormation", formation);
            }

            Participants = (ArrayList<IStorageResource>) controller.evaluate(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Participants;

    }


    public ISecurityController getSecuriteController(IWorkflowInstance instance){
        try {
            return getWorkflowModule().getSecurityController(instance);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void breakInheritance(ISecurityController securityController){
        securityController.breakInheritance(1, new Object[] { null, "write" });
    }

    public void addManagementGroupes(ISecurityController securityController){
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization BPOGroupesOrganization = getDirectoryModule().getOrganization(context, "BPOGroupes");
            IGroup adminGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"BPO");
            IGroup DRHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"DRH");
            IGroup RHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"RH");
            // IGroup ResponsableDevGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"ResponsableSDEV");

            addPermission(securityController, adminGroupe, "read");
            addPermission(securityController, DRHGroupe, "read");
            addPermission(securityController, RHGroupe, "read");
            //addPermission(securityController, ResponsableDevGroupe, "read");

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void addPermission(ISecurityController securityController, IGroup group, String readWriteGrant){
        securityController.addPermission(group, new Object[] { null, readWriteGrant });
    }

    public void addPermission(ISecurityController securityController, IUser user, String readWriteGrant){
        securityController.addPermission(user, new Object[] { null, readWriteGrant });
    }

    public void addSpecificPermissionsProgrammeFormation(IResource instance , ISecurityController securityController) {
        if(instance == null || securityController == null){
            return;
        }
        try {

            IUser salarie = (IUser) instance.getValue("sys_Creator");
            if (salarie == null) {
                return;
            }
            addPermission(securityController, salarie, "read");

            IUser formateurInterne = (IUser) instance.getValue("Formateur2");
            if(formateurInterne!=null){
                addPermission(securityController, formateurInterne, "read");
            }

//            IUser representantFormateur = (IUser) instance.getValue("RepresentantFormateur");
//            if(representantFormateur!=null){
//                addPermission(securityController, representantFormateur, "read");
//            }

            IUser participant = (IUser) instance.getValue("Participant2");
            if(participant!=null){
                addPermission(securityController, participant, "read");
                if(participant.getHierarchicalManager()!=null){
                    addPermission(securityController, participant.getHierarchicalManager(), "read");
                }
            }
            IUser respHierarchique = (IUser) instance.getValue("RespHierarchique");
            if(respHierarchique!=null ){

                addPermission(securityController, respHierarchique, "read");

            }

            IStorageResource societe = (IStorageResource) instance.getValue("Societe");
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = null;
            if (societe != null) {
                organization = (IOrganization) societe.getValue("Organisation");
            }

            if (organization == null) return;
            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "RH");
            if (RHClientGroupe != null) {
                addPermission(securityController, RHClientGroupe, "read");
            }

            IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,"ResponsableSDEV");
            if(ResponsableDevClientGroupe!=null){
                addPermission(securityController, ResponsableDevClientGroupe, "read");
            }

            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "RHSociete");
            if (RHSocieteGroupe != null) {
                addPermission(securityController, RHSocieteGroupe, "read");
            }

            IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "ResponsablesDevSociete");
            if (ResponsableDevSocieteGroupe != null) {
                addPermission(securityController, ResponsableDevSocieteGroupe, "read");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addSpecificPermissionsFormation(IResource instance , ISecurityController securityController) {
        if(instance == null || securityController == null){
            return;
        }
        try {

            IUser salarie = (IUser) instance.getValue("sys_Creator");
            if (salarie == null) {
                return;
            }
            addPermission(securityController, salarie, "read");

            IUser formateurInterne = (IUser) instance.getValue("Formateur2");
            if(formateurInterne!=null){
                addPermission(securityController, formateurInterne, "read");
            }

//            IUser representantFormateur = (IUser) instance.getValue("RepresentantFormateur");
//            if(representantFormateur!=null){
//                addPermission(securityController, representantFormateur, "read");
//            }

            IUser participant = (IUser) instance.getValue("Participant2");
            if(participant!=null){
                addPermission(securityController, participant, "read");
                if(participant.getHierarchicalManager()!=null){
                    addPermission(securityController, participant.getHierarchicalManager(), "read");
                }
            }
            IUser respHierarchique = (IUser) instance.getValue("RespHierarchique");
            if(respHierarchique!=null ){

                addPermission(securityController, respHierarchique, "read");

            }

            IStorageResource societe = (IStorageResource) instance.getValue("Societe");
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = null;
            if (societe != null) {
                organization = (IOrganization) societe.getValue("Organisation");
            }

            if (organization == null) return;
            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "RH");
            if (RHClientGroupe != null) {
                addPermission(securityController, RHClientGroupe, "read");
            }

            IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,"ResponsableSDEV");
            if(ResponsableDevClientGroupe!=null){
                addPermission(securityController, ResponsableDevClientGroupe, "read");
            }

            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "RHSociete");
            if (RHSocieteGroupe != null) {
                addPermission(securityController, RHSocieteGroupe, "read");
            }

            IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "ResponsablesDevSociete");
            if (ResponsableDevSocieteGroupe != null) {
                addPermission(securityController, ResponsableDevSocieteGroupe, "read");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
