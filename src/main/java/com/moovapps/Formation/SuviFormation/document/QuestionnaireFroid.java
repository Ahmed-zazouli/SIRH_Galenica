package com.moovapps.Formation.SuviFormation.document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.framework.composites.IDocumentComposite;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.widgets.blocks.sys.layouts.FormSectionBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionnaireFroid extends CummonSuivi {

    @Override
    public boolean onAfterLoad() {

        setQuestionsReponse();
        if (getWorkflowInstance().getValue("SouhaitezVousAcquerirLaCompetencePar").equals("Attestation")){
            setTableNotation();
        }
        refreshSection("${@LOC(\"SuiviDeFormation_1.1:ActionStage:QuestionnaireAFroid:label\")}");
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("FinirLaFormation2")) {
            if (getWorkflowInstance().getValue("SouhaitezVousAcquerirLaCompetencePar").equals("Attestation")) {
                calulateNotationSiParAttestation();
            }else {
                calculateNotation() ;
            }
        }
       //return false;
        return super.onBeforeSubmit(action);
    }

    @Override
    public String getNotationField() {
        return "Notation0100AFroid";
    }

    @Override
    public String getPourcentageMinimumPourAcquisitionDuCompetence(){
        return "PourcentageMinimumPourAcquisitionDeLaCompetenceQuestionnaireAFroid";
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
                        questionReponse.setValue("IDSessionQuestionFroid", question.getValue("IDSessionQuestionFroid"));
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
                        questionReponse.setValue("IDSessionQuestionFroid", question.getValue("IDSessionQuestionFroid"));
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



        /*Collection<IStorageResource> QuestionsReponse = getQuestionsReponseByIDSession(null);
        if (QuestionsReponse.isEmpty()) {
            IUser participant = (IUser) getWorkflowInstance().getValue("Participant2");
            IStorageResource ficheCollaborateur = getFicheByUser(participant);
            Collection<IStorageResource> Questions = getQuestionsByIDSession();
            for (IStorageResource question : Questions) {
                Collection<IStorageResource> competences = (Collection<IStorageResource>) question.getValue("Competence");
                Collection<IStorageResource> earnedCompetence= new ArrayList<>(Collections.emptyList());
                for (IStorageResource comptence:competences){
                    IStorageResource competence = getCompetenceSalarie(comptence ,ficheCollaborateur);
                    if (competence != null){
                        earnedCompetence.add(competence);
                    }
                }
                if (!earnedCompetence.isEmpty()) {
                    try {
                        IContext context = getWorkflowModule().getSysadminContext();
                        IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                        IProject project = getProjectModule().getProject(context, "Formation", organization);
                        ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
                        IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions_Reponses");
                        IStorageResource questionReponse = getWorkflowModule().createStorageResource(context, definition, "", "");
                        questionReponse.setValue("Question", question);
                        questionReponse.setValue("IDSessionQuestionFroid", question.getValue("IDSessionQuestionFroid"));
                        questionReponse.setValue("Poids", question.getValue("Poids"));
                        questionReponse.setValue("ReponseS", question.getValue("ReponseS"));
                        questionReponse.setValue("Competence",earnedCompetence);
                        questionReponse.setValue("IDSeanceParticipant", getWorkflowInstance().getValue("IDSeanceParticipant"));
                        questionReponse.save(getWorkflowModule().getSysadminContext());
                        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }*/

    }

    private Collection<IStorageResource> getEarnedCompetences() {
        ArrayList<ILinkedResource> earnedCompetenceTD = (ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("NotationDeChaqueCompetence");
        return earnedCompetenceTD.stream().filter(e -> ((Number) e.getValue("Notation")).intValue() > ((Number) e.getValue("PourcentageMinimumPourAcquisitionDeLaCompetence")).intValue()).map(e -> (IStorageResource) e.getValue("Competence")).collect(Collectors.toList());
    }

    private IStorageResource getFicheByUser(IUser user) {
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

    private IStorageResource getCompetenceSalarie(IStorageResource competence, IStorageResource ficheCollaborateur) {
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

    /*private Collection<IStorageResource> getQuestionsReponseByIDSession(IStorageResource competence) {
        Collection<IStorageResource> QuestionsReponse = Collections.emptyList();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions_Reponses");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("IDSessionQuestionFroid", getWorkflowInstance().getValue("IDSessionQuestionFroid"));
            controller.addEqualsConstraint("IDSeanceParticipant", getWorkflowInstance().getValue("IDSeanceParticipant"));
            if (competence != null) {
                controller.addInConstraint("Competence", competence);
            }
            QuestionsReponse = controller.evaluate(definition);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return QuestionsReponse;
    }*/

    /*private Collection<IStorageResource> getQuestionsByIDSession() {
        Collection<IStorageResource> Questions = Collections.emptyList();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("IDSessionQuestionFroid", getWorkflowInstance().getValue("IDSessionQuestionFroid"));
            Questions = controller.evaluate(definition);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Questions;
    }*/

    @Override
    public String getTableauDesNotes() {
        return "NotationDeChaqueCompetence2";
    }

    @Override
    public String getIDToUSe() {
        return "IDSessionQuestionFroid";
    }

    @Override
    public void setConfirmer(IStorageResource competenceSalerie) {
        competenceSalerie.setValue("ConfirmeCompetence", true);
    }
    //    private void calculateNotation() {
//
//        IWorkflowInstance formationWorkflow = (IWorkflowInstance) getWorkflowInstance().getValue("FormationProcessus");
//        ArrayList<ILinkedResource> NiveauEtCompetences = (ArrayList<ILinkedResource>) formationWorkflow.getValue("NiveauDeCompetencePourPasserLaFormation");
//        double poidsTotale = 0;
//        double notationCorrecte = 0;
//        for (ILinkedResource NiveauEtCompetence : NiveauEtCompetences) {
//            Collection<IStorageResource> questionReponseDeCompetence = getQuestionsReponseByIDSession((IStorageResource) NiveauEtCompetence.getValue("Competence"));
//            double poidsTotaleCompetence = 0;
//            double notationCorrecteCompetence = 0;
//            for (IStorageResource question : questionReponseDeCompetence) {
//                double notation = question.getValue("Notation") != null ? ((Number) question.getValue("Notation")).doubleValue() : 0;
//                double poids = question.getValue("Poids") != null ? ((Number) question.getValue("Poids")).doubleValue() : 0;
//                poidsTotaleCompetence += poids;
//                notationCorrecteCompetence += notation;
//                poidsTotale += poids;
//                notationCorrecte += notation;
//
//            }
//            try {
//                ILinkedResource notationDeChaqueCompetence = getWorkflowInstance().createLinkedResource("NotationDeChaqueCompetence2");
//                IStorageResource competence = (IStorageResource) NiveauEtCompetence.getValue("Competence");
//                double notation = (notationCorrecteCompetence / poidsTotaleCompetence) * 100;
//                double pourcentageMinimumPourAcquerirLaCompetence = ((Number) competence.getValue("PourcentageMinimumPourAcquerirLaCompetence")).doubleValue();
//                notationDeChaqueCompetence.setValue("Competence", competence);
//                notationDeChaqueCompetence.setValue("Notation", notation);
//                notationDeChaqueCompetence.save(getWorkflowModule().getSysadminContext());
//                getWorkflowInstance().addLinkedResource(notationDeChaqueCompetence);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            getWorkflowInstance().setValue("NotationQuestionnaireAFroid0100", (notationCorrecte / poidsTotale) * 100);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
//
//    }

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



//    private void setCompetence() {
//        IUser participant = (IUser) getWorkflowInstance().getValue("Participant2");
//        Collection<ILinkedResource>  notationDechaqueCompetences = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("NotationDeChaqueCompetence2");
//        IStorageResource ficheCollaborateur = getFicheByUser(participant);
//        try {
//            for (ILinkedResource notationDechaqueCompetence: notationDechaqueCompetences) {
//                IStorageResource comptence = (IStorageResource) notationDechaqueCompetence.getValue("Competence");
//                IContext context = getWorkflowModule().getSysadminContext();
//                IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
//                IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
//                ICatalog catalog  = getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,project);
//                IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"CompetenceSalarie");
//                IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
//                controller.addEqualsConstraint("Collaborateur", ficheCollaborateur);
//                controller.addEqualsConstraint("Competence", comptence);
//                ArrayList<IStorageResource> competenesDuSalarie = (ArrayList<IStorageResource>) controller.evaluate(definition);
//                IStorageResource competenceSalerie = null;
//                competenceSalerie = competenesDuSalarie.iterator().next();
//                float notation = ((Number) notationDechaqueCompetence.getValue("Notation")).floatValue();
//                float pourcentageMinimumPourAcquerirLaCompetence =((Number) comptence.getValue("PourcentageMinimumPourAcquerirLaCompetence")).floatValue();
//                if (notation < pourcentageMinimumPourAcquerirLaCompetence) {
//                    competenceSalerie.setValue("DateDExpiration",new Date() );
//                    competenceSalerie.setValue("Notation0100", notation);
//                    competenceSalerie.setValue("NiveauDeCompetence2",null);
//                    if (ficheCollaborateur.getValue("Competence")!=null ) {
//                        HashSet<IStorageResource> competences =new HashSet<>((Collection) ficheCollaborateur.getValue("Competence"));
//                        competences.remove(comptence);
//                        ficheCollaborateur.setValue("Competence", competences);
//                    }
//                    ficheCollaborateur.save(getWorkflowModule().getSysadminContext());
//                }else {
//                    competenceSalerie.setValue("ConfirmeCompetence",true);
//                }
//                competenceSalerie.save(getWorkflowModule().getSysadminContext());
//                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }


}
