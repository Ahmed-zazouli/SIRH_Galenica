package com.moovapps.Formation.SuiviProgrammeDeFormation.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class NotationDeQuestionnaireFroid extends BaseDocumentExtension {
    @Override
    public boolean onAfterLoad() {
        return super.onAfterLoad();
    }


    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("Cloture")) {
            setCompetence();
            return false;
        }
        //return false;
        return super.onBeforeSubmit(action);
    }

    private void setCompetence() {
        IUser participant = (IUser) getWorkflowInstance().getValue("Participant2");

        Collection<ILinkedResource> notationDechaqueCompetences = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("NotationDeChaqueCompetence2");
        IStorageResource ficheCollaborateur = getFicheByUser(participant);

        try {

            for (ILinkedResource notationDechaqueCompetence : notationDechaqueCompetences) {
                IStorageResource competence = (IStorageResource) notationDechaqueCompetence.getValue("Competence");
                IContext context = getWorkflowModule().getSysadminContext();
                IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
                ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
                IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "CompetenceSalarie");
                IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
                controller.addEqualsConstraint("Collaborateur", ficheCollaborateur);
                controller.addEqualsConstraint("Competence", competence);
                ArrayList<IStorageResource> competenesDuSalarie = (ArrayList<IStorageResource>) controller.evaluate(definition);
                IStorageResource competenceSalerie = null;
                if (!competenesDuSalarie.isEmpty()) {
                    competenceSalerie = competenesDuSalarie.iterator().next();
                }


                float notation = ((Number) notationDechaqueCompetence.getValue("Notation")).floatValue();
                float pourcentageMinimumPourAcquerirLaCompetence = ((Number) competence.getValue("PourcentageMinimumPourAcquerirLaCompetence")).floatValue();
                if (notation < pourcentageMinimumPourAcquerirLaCompetence && competenceSalerie != null) {
                    competenceSalerie.setValue("DateDExpiration", new Date());
                    competenceSalerie.setValue("Notation0100", notation);
                    competenceSalerie.setValue("NiveauDeCompetence2", null);


                    if (ficheCollaborateur.getValue("Competence") != null) {

                        HashSet<IStorageResource> competences = new HashSet<>((Collection) ficheCollaborateur.getValue("Competence"));
                        competences.remove(competence);

                        ficheCollaborateur.setValue("Competence", competences);
                    }


                    ficheCollaborateur.save(getWorkflowModule().getSysadminContext());
                    competenceSalerie.save(getWorkflowModule().getSysadminContext());
                } else if(competenceSalerie != null){
                    competenceSalerie.setValue("ConfirmeCompetence", true);
                    competenceSalerie.save(getWorkflowModule().getSysadminContext());
                }
                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private IStorageResource getNiveauDeCompetence(IStorageResource comptence, Object notation) {
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "NiveauDeCompetence");
            controller.addGreaterOrEqualConstraint("PourcentageMaximum", notation);
            controller.addLessOrEqualsConstraint("PourcentageMinimum", notation);
            controller.addEqualsConstraint("Competence", comptence);
            return (IStorageResource) controller.evaluate(definition).iterator().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

   /* private void setNiveauDecompetence() {
        double moyenneGenerale =((Number) getWorkflowInstance().getValue("PourcentageDeMoyenneGenerale")).doubleValue();
        if (moyenneGenerale>=50 && moyenneGenerale<=70){
            getWorkflowInstance().setValue("NiveauDeCompetence","Débutant");
        } else if (moyenneGenerale>=71 && moyenneGenerale<=80) {
            getWorkflowInstance().setValue("NiveauDeCompetence","Avancé");
        } else if (moyenneGenerale>=81 && moyenneGenerale<=100) {
            getWorkflowInstance().setValue("NiveauDeCompetence","Expert");
        }else {
            getWorkflowInstance().setValue("NiveauDeCompetence","sans Niveau");
        }
    }*/
}
