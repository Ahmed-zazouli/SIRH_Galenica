package com.moovapps.Formation.SuviFormation.document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.framework.composites.IDocumentComposite;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.widgets.blocks.sys.layouts.FormSectionBlock;

import java.util.*;

public class QuestionnaireChaud extends CummonSuivi {
    @Override
    public boolean onAfterLoad() {

        setQuestionsReponse();
        if (getWorkflowInstance().getValue("SouhaitezVousAcquerirLaCompetencePar").equals("Attestation")) {
            setTableNotation();
        }
        refreshSection("${@LOC(\"SuiviDeFormation_1.1:ActionStage:QuestionnaireAChaud:label\")}");
        return super.onAfterLoad();
    }


    @Override
    public String getTableauDesNotes() {
        return "NotationDeChaqueCompetence";
    }

    @Override
    public String getPourcentageMinimumPourAcquisitionDuCompetence() {
        return "PourcentageMinimumPourAcquisitionDuCompetence";
    }

    @Override
    public String getIDToUSe() {
        return "IDSession";
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("FinirLaQuestionnaire")) {
            if (getWorkflowInstance().getValue("SouhaitezVousAcquerirLaCompetencePar").equals("Attestation")) {
                calulateNotationSiParAttestation();
            }else {
                calculateNotation() ;
            }
             ;
            if (getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid") != null && getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid").equals("Oui")) {
                if (getWorkflowInstance().getValue("DateDeDemarrageDuQuestionnaireAFroid") == null) {
                    Calendar dateDeDemarrage = Calendar.getInstance();
                    dateDeDemarrage.add(Calendar.MONTH, ((Number) getWorkflowInstance().getValue("DureeDuQuestionnaireAFroidMois")).intValue());
                    getWorkflowInstance().setValue("DateDeDemarrageDuQuestionnaireAFroid", dateDeDemarrage.getTime());
                    getWorkflowInstance().save("DateDeDemarrageDuQuestionnaireAFroid");
                }
            }
        }
        //return false;
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

    @Override
    public String getNotationField() {
        return "Notation0100";
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
                    questionReponse.setValue("IDSession", question.getValue("IDSession"));
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

  /*  private void setTableNotation() {
        ILinkedResource notationDeChaqueCompetence = (ILinkedResource) getWorkflowInstance().getValue("NotationDeChaqueCompetence");
        if (notationDeChaqueCompetence != null) {
            getWorkflowInstance().deleteLinkedResource(notationDeChaqueCompetence);
        }
        IWorkflowInstance formationWorkflow = (IWorkflowInstance) getWorkflowInstance().getValue("FormationProcessus");
        ArrayList<ILinkedResource> NiveauEtCompetences = (ArrayList<ILinkedResource>) formationWorkflow.getValue("NiveauDeCompetencePourPasserLaFormation");
        for (ILinkedResource tableDeCompetence : NiveauEtCompetences) {
            notationDeChaqueCompetence = getWorkflowInstance().createLinkedResource(getTableauDesNotes());
            notationDeChaqueCompetence.setValue("Competence", tableDeCompetence.getValue("Competence"));
            notationDeChaqueCompetence.setValue("PourcentageMinimumPourAcquisitionDeLaCompetence", tableDeCompetence.getValue("PourcentageMinimumPourAcquisitionDuCompetence"));
            notationDeChaqueCompetence.setValue("NiveauDeCompetence", tableDeCompetence.getValue("NiveauCompetenceDeLaFormation"));
            notationDeChaqueCompetence.save(getWorkflowModule().getSysadminContext());

            notationDeChaqueCompetence.save(getWorkflowModule().getSysadminContext());
            getWorkflowInstance().addLinkedResource(notationDeChaqueCompetence);
        }
    }*/

//    private Collection<IStorageResource> getQuestionsReponseByIDSession(IStorageResource competence) {
//        Collection<IStorageResource> QuestionsReponse = null;
//        try {
//            IContext context = getWorkflowModule().getSysadminContext();
//            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
//            IProject project = getProjectModule().getProject(context, "Formation", organization);
//            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
//            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions_Reponses");
//            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
//            controller.addEqualsConstraint("IDSession", getWorkflowInstance().getValue("IDSession"));
//            controller.addEqualsConstraint("IDSeanceParticipant", getWorkflowInstance().getValue("IDSeanceParticipant"));
//            if (competence != null) {
//                controller.addInConstraint("Competence", competence);
//            }
//            QuestionsReponse = controller.evaluate(definition);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return QuestionsReponse;
//    }

//    private Collection<IStorageResource> getQuestionsByID(String idFieldName) {
//        Collection<IStorageResource> Questions = Collections.emptyList();
//        try {
//            IContext context = getWorkflowModule().getSysadminContext();
//            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
//            IProject project = getProjectModule().getProject(context, "Formation", organization);
//            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
//            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions");
//            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
//            controller.addEqualsConstraint(idFieldName, getWorkflowInstance().getValue(idFieldName));
//            Questions = controller.evaluate(definition);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return Questions;
//    }

//    private void calculateNotation() {
//        // Collection<IWorkflowInstance> coursParParticipant = getCoursParParticipantByIDSession(getWorkflowInstance());
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
//            }
//            poidsTotale += poidsTotaleCompetence;
//            notationCorrecte += notationCorrecteCompetence;
//
//            try {
//                ILinkedResource notationDeChaqueCompetence = getWorkflowInstance().createLinkedResource("NotationDeChaqueCompetence");
//                IStorageResource competence = (IStorageResource) NiveauEtCompetence.getValue("Competence");
//                double notation = (notationCorrecteCompetence / poidsTotaleCompetence) * 100;
//                double pourcentageMinimumPourAcquerirLaCompetence = ((Number) NiveauEtCompetence.getValue("PourcentageMinimumPourAcquisitionDuCompetence")).doubleValue();
//                notationDeChaqueCompetence.setValue("Competence", competence);
//                notationDeChaqueCompetence.setValue("Notation", notation);
//                notationDeChaqueCompetence.setValue("PourcentageMinimumPourAcquisitionDeLaCompetence", NiveauEtCompetence.getValue("PourcentageMinimumPourAcquisitionDuCompetence"));
//                notationDeChaqueCompetence.setValue("NiveauDeCompetence", NiveauEtCompetence.getValue("NiveauCompetenceDeLaFormation"));
//                if (notation >= pourcentageMinimumPourAcquerirLaCompetence) {
//                    getWorkflowInstance().setValue("VousAvezUnCompetence", true);
//                    notationDeChaqueCompetence.save(getWorkflowModule().getSysadminContext());
//                    setCompetence(notationDeChaqueCompetence);
//                } else {
//                    notationDeChaqueCompetence.save(getWorkflowModule().getSysadminContext());
//                }
//                getWorkflowInstance().addLinkedResource(notationDeChaqueCompetence);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        try {
//            getWorkflowInstance().setValue("NotationQuestionnaireAChaud0100", (notationCorrecte / poidsTotale) * 100);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//       /*  notationCorrecte = 0;
//       for (IWorkflowInstance cours : coursParParticipant) {
//            double notation = cours.getValue("Notation0100") != null ? ((Number) cours.getValue("Notation0100")).doubleValue() : 0;
//            notationCorrecte += notation;
//
//        }
//
//        try {
//            getWorkflowInstance().setValue("NotationQuestionnaireAChaud0100", (notationCorrecte / coursParParticipant.size()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//            double pourcentageNotationQuestionnaireAChaud = ((((Number) getWorkflowInstance().getValue("NotationQuestionnaireAChaud0100")).doubleValue()) *
//                    (((Number) getWorkflowInstance().getValue("PourcentageDeQuestionnaireAChaud")).doubleValue())) / 100;
//            double pourcentageNotationQuestionnaireAFroid = ((((Number) getWorkflowInstance().getValue("NotationQuestionnaireAFroid0100")).doubleValue()) *
//                    (((Number) getWorkflowInstance().getValue("PourcentageDeQuestionnaireAFroid")).doubleValue())) / 100;
//            getWorkflowInstance().setValue("PourcentageDeMoyenneGenerale", pourcentageNotationQuestionnaireAChaud + pourcentageNotationQuestionnaireAFroid);
//            */
//        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
//
//    }

    public Collection<IWorkflowInstance> getCoursParParticipantByIDSession(IWorkflowInstance instance) {
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
            controller.addEqualsConstraint("IDSession", instance.getValue("IDSession"));
            cours = controller.evaluate(w);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cours;
    }

//    private void setCompetence(ILinkedResource notationCompetence) {
//        IUser participant = (IUser) getWorkflowInstance().getValue("Participant2");
//        IStorageResource ficheCollaborateur = getFicheByUser(participant);
//        try {
//            IStorageResource comptence = (IStorageResource) notationCompetence.getValue("Competence");
//            IContext context = getWorkflowModule().getSysadminContext();
//            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
//            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
//            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
//            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "CompetenceSalarie");
//            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
//            controller.addEqualsConstraint("Collaborateur", ficheCollaborateur);
//            controller.addEqualsConstraint("Competence", comptence);
//
//            ArrayList<IStorageResource> competenesDuSalarie = (ArrayList<IStorageResource>) controller.evaluate(definition);
//            IStorageResource competenceSalerie = null;
//            float notation = ((Number) notationCompetence.getValue("Notation")).floatValue();
//            float pourcentageMinimumPourAcquerirLaCompetence = ((Number) comptence.getValue("PourcentageMinimumPourAcquerirLaCompetence")).floatValue();
//            if (notation >= pourcentageMinimumPourAcquerirLaCompetence) {
//                if (!competenesDuSalarie.isEmpty()) {
//                    competenceSalerie = competenesDuSalarie.iterator().next();
//                } else {
//                    competenceSalerie = getWorkflowModule().createStorageResource(context, definition, "");
//                    competenceSalerie.setValue("sys_Title", comptence.getValue("sys_Title") + " : " + ficheCollaborateur.getValue("NomPrenom"));
//                    competenceSalerie.setValue("Societe", comptence.getValue("Societe"));
//                    competenceSalerie.setValue("FamilleCompetence", comptence.getValue("FamilleCompetence"));
//                    competenceSalerie.setValue("SousFamilleCompetence", comptence.getValue("SousFamilleCompetence"));
//                    competenceSalerie.setValue("Collaborateur", ficheCollaborateur);
//                    competenceSalerie.setValue("Competence", comptence);
//                }
//                competenceSalerie.setValue("DateDAcquisitionDeCompetence", new Date());
//                competenceSalerie.setValue("Notation0100", notation);
//                competenceSalerie.setValue("NiveauDeCompetence2", notationCompetence.getValue("NiveauDeCompetence"));
//                competenceSalerie.setValue("ConfirmeCompetence", false);
//                if (comptence.getValue("TypeDeFormation").equals("Renouvelable") && comptence.getValue("DureeMois") != null) {
//                    Calendar dateFin = Calendar.getInstance();
//                    dateFin.add(Calendar.MONTH, ((Number) comptence.getValue("DureeMois")).intValue());
//                    competenceSalerie.setValue("DateDExpiration", dateFin.getTime());
//                }
//                competenceSalerie.save(getWorkflowModule().getSysadminContext());
//            }
//                /*if(competenesDuSalarie.size() > 0){
//                    competenceSalerie = competenesDuSalarie.iterator().next();
//                    competenceSalerie.setValue("DateDAcquisitionDeCompetence", new Date());
//                    competenceSalerie.setValue("Notation0100", notation);
//                    competenceSalerie.setValue("NiveauDeCompetence2", notationDechaqueCompetence.getValue("NiveauDeCompetence"));
//                    competenceSalerie.setValue("ConfirmeCompetence",false);
//
//                    if (comptence.getValue("TypeDeFormation").equals("Renouvelable") && comptence.getValue("DureeMois") != null) {
//                        Calendar dateFin = Calendar.getInstance();
//                        dateFin.add(Calendar.MONTH, ((Number) comptence.getValue("DureeMois")).intValue());
//                        competenceSalerie.setValue("DateDExpiration", dateFin.getTime());
//                    }
//                    competenceSalerie.save(getWorkflowModule().getSysadminContext());
//
//                }else {
//                    if (notation >= pourcentageMinimumPourAcquerirLaCompetence) {
//                        competenceSalerie = getWorkflowModule().createStorageResource(context, definition, "");
//                        competenceSalerie.setValue("sys_Title", comptence.getValue("sys_Title") + " : " +ficheCollaborateur.getValue("NomPrenom") );
//                        competenceSalerie.setValue("Societe",comptence.getValue("Societe"));
//                        competenceSalerie.setValue("FamilleCompetence",comptence.getValue("FamilleCompetence"));
//                        competenceSalerie.setValue("Collaborateur", ficheCollaborateur);
//                        competenceSalerie.setValue("Competence", comptence);
//                        competenceSalerie.setValue("DateDAcquisitionDeCompetence", new Date());
//                        competenceSalerie.setValue("Notation0100", notation);
//                        competenceSalerie.setValue("NiveauDeCompetence2", notationDechaqueCompetence.getValue("NiveauDeCompetence"));
//                        competenceSalerie.setValue("ConfirmeCompetence",false);
//
//                        if (comptence.getValue("TypeDeFormation").equals("Renouvelable") && comptence.getValue("DureeMois") != null) {
//                            Calendar dateFin = Calendar.getInstance();
//                            dateFin.add(Calendar.MONTH, ((Number) comptence.getValue("DureeMois")).intValue());
//                            competenceSalerie.setValue("DateDExpiration", dateFin.getTime());
//                        }
//                        competenceSalerie.save(getWorkflowModule().getSysadminContext());
//                    }*/
//
//
//                    /*if (ficheCollaborateur.getValue("Competence")!=null ) {
//                        HashSet<IStorageResource> competences =new HashSet<>((Collection) ficheCollaborateur.getValue("Competence"));
//                        competences.add(comptence);
//                        ficheCollaborateur.setValue("Competence", competences);
//                    }else {
//                        ficheCollaborateur.setValue("Competence", comptence);
//                    }
//                    ficheCollaborateur.save(getWorkflowModule().getSysadminContext());
//                }*/
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    private IStorageResource getFicheByUser(IUser user) {
//        try {
//            IContext context = getWorkflowModule().getSysadminContext();
//            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
//            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
//            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
//            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
//            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
//            controller.addEqualsConstraint("Salarie", user);
//            return (IStorageResource) controller.evaluate(definition).iterator().next();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


}
