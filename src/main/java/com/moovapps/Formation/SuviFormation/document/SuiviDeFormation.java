package com.moovapps.Formation.SuviFormation.document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.*;

public class SuiviDeFormation extends BaseDocumentExtension {
    @Override
    public boolean onAfterLoad() {
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("FinirLaFormation")){
            Collection<IWorkflowInstance> poursParParticipant= getCoursParParticipantByIDSession();
            if (!poursParParticipant.isEmpty()){
                getResourceController().alert("Vous devez terminer toutes les séances d'abord");
                return false;
            }
            setCompetence();
            if (getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid") != null && getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid").equals("Oui")) {
                if(getWorkflowInstance().getValue("DateDeDemarrageDuQuestionnaireAFroid") == null){
                    Calendar dateDeDemarrage = Calendar.getInstance();
                    dateDeDemarrage.add(Calendar.MONTH, ((Number) getWorkflowInstance().getValue("DureeDuQuestionnaireAFroidMois")).intValue());
                    getWorkflowInstance().setValue("DateDeDemarrageDuQuestionnaireAFroid", dateDeDemarrage.getTime());
                    getWorkflowInstance().save("DateDeDemarrageDuQuestionnaireAFroid");
                }
            }
        }


        return super.onBeforeSubmit(action);
    }
    private void setCompetence() {
        if (getWorkflowInstance().getValue("QuizEtape") != null) {
            boolean SiSans = getWorkflowInstance().getValue("QuizEtape").equals("Sans") && (getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAChaux") == null || getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAChaux").equals("Non") )&&
                    (getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid") == null ||getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid").equals("Non")) ;
            boolean SiFormation = getWorkflowInstance().getValue("QuizEtape").equals("Formation") &&  (getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAChaux") == null || getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAChaux").equals("Non")) &&
                    (getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid") == null || getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid").equals("Non"));
            if (SiSans || SiFormation) {
                IUser participant = (IUser) getWorkflowInstance().getValue("Participant2");
                IStorageResource ficheCollaborateur = getFicheByUser(participant);
                IWorkflowInstance formation = (IWorkflowInstance) getWorkflowInstance().getValue("FormationProcessus");
                Collection<ILinkedResource> notationCompetences = (Collection<ILinkedResource>) formation.getValue("NiveauDeCompetencePourPasserLaFormation");
                for (ILinkedResource notationCompetence : notationCompetences) {
                    try {
                        IStorageResource comptence = (IStorageResource) notationCompetence.getValue("Competence");
                        IContext context = getWorkflowModule().getSysadminContext();
                        IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                        IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
                        ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
                        IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "CompetenceSalarie");
                        IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
                        controller.addEqualsConstraint("Collaborateur", ficheCollaborateur);
                        controller.addEqualsConstraint("Competence", comptence);

                        ArrayList<IStorageResource> competenesDuSalarie = (ArrayList<IStorageResource>) controller.evaluate(definition);
                        IStorageResource competenceSalerie = null;

                        if (!competenesDuSalarie.isEmpty()) {
                            competenceSalerie = competenesDuSalarie.iterator().next();
                        } else {
                            competenceSalerie = getWorkflowModule().createStorageResource(context, definition, "");
                            competenceSalerie.setValue("sys_Title", comptence.getValue("sys_Title") + " : " + ficheCollaborateur.getValue("NomPrenom"));
                            competenceSalerie.setValue("Societe", comptence.getValue("Societe"));
                            competenceSalerie.setValue("Domaine", comptence.getValue("Domaine"));
                            competenceSalerie.setValue("FamilleCompetence", comptence.getValue("FamilleCompetence"));
                            competenceSalerie.setValue("SousFamilleCompetence", comptence.getValue("SousFamilleCompetence"));
                            competenceSalerie.setValue("Collaborateur", ficheCollaborateur);
                            competenceSalerie.setValue("Competence", comptence);
                        }

                        if (competenceSalerie.getValue("DateDAcquisitionDeCompetence") == null) {
                            competenceSalerie.setValue("DateDAcquisitionDeCompetence", new Date());
                        } else {
                            competenceSalerie.setValue("DateRenouvellementCompetence", new Date());
                        }
                        competenceSalerie.setValue("ConfirmeCompetence", true);
                        competenceSalerie.setValue("NiveauDeCompetence", notationCompetence.getValue("NiveauCompetenceDeLaFormation"));
                        if (comptence.getValue("TypeDeFormation").equals("Renouvelable") && comptence.getValue("DureeMois") != null) {
                            Calendar dateFin = Calendar.getInstance();
                            dateFin.add(Calendar.MONTH, ((Number) comptence.getValue("DureeMois")).intValue());
                            competenceSalerie.setValue("DateDExpiration", dateFin.getTime());
                        }

                        competenceSalerie.save(getWorkflowModule().getSysadminContext());


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


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


    private Collection<IWorkflowInstance> getCoursParParticipantByIDSession() {
        Collection<IWorkflowInstance> cours = Collections.emptyList();
        try {
            IContext sysContext = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(sysContext, "Formation", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "Formation", project);
            IWorkflow w = Modules.getWorkflowModule().getWorkflow(sysContext, catalog, "CoursParParticipant_1.0");
            IViewController controller = Modules.getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("Participant2", getWorkflowInstance().getValue("Participant2"));
            controller.addNotInConstraint("DocumentState", Arrays.asList("Clôturé(e)", "Achevée"));
            controller.addEqualsConstraint("IDSession", getWorkflowInstance().getValue("IDSession"));
            cours = controller.evaluate(w);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cours;
    }

}
