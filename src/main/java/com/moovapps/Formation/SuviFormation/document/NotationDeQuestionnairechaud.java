package com.moovapps.Formation.SuviFormation.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.*;

public class NotationDeQuestionnairechaud extends BaseDocumentExtension {
    @Override
    public boolean onAfterLoad() {
        return super.onAfterLoad();
    }



    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("Cloture2")){
            setCompetence();
            if (getWorkflowInstance().getValue("DateDeDemarrageDuQuestionnaireAFroid")==null){
                Calendar dateDeDemarrage =Calendar.getInstance();
                dateDeDemarrage.add(Calendar.MONTH ,((Number)getWorkflowInstance().getValue("DureeDuQuestionnaireAFroidMois")).intValue());
                getWorkflowInstance().setValue("DateDeDemarrageDuQuestionnaireAFroid",dateDeDemarrage.getTime());
            }
        }
        return super.onBeforeSubmit(action);
    }

    private void setCompetence() {
        IUser participant = (IUser) getWorkflowInstance().getValue("Participant2");

      Collection<ILinkedResource>  notationDechaqueCompetences = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("NotationDeChaqueCompetence");
        IStorageResource ficheCollaborateur = getFicheByUser(participant);

            try {

                for (ILinkedResource notationDechaqueCompetence: notationDechaqueCompetences) {
                    IStorageResource comptence = (IStorageResource) notationDechaqueCompetence.getValue("Competence");
                    IContext context = getWorkflowModule().getSysadminContext();
                    IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
                    IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
                    ICatalog catalog  = getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,project);
                    IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"CompetenceSalarie");
                    IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
                    controller.addEqualsConstraint("Collaborateur", ficheCollaborateur);
                    controller.addEqualsConstraint("Competence", comptence);

                    ArrayList<IStorageResource> competenesDuSalarie = (ArrayList<IStorageResource>) controller.evaluate(definition);
                    IStorageResource competenceSalerie = null;
                    float notation = ((Number) notationDechaqueCompetence.getValue("Notation")).floatValue();
                    float pourcentageMinimumPourAcquerirLaCompetence =((Number) comptence.getValue("PourcentageMinimumPourAcquerirLaCompetence")).floatValue();
                    IStorageResource niveauDeCompetence = getNiveauDeCompetence(comptence,((Number) notationDechaqueCompetence.getValue("Notation")).floatValue());
                    if(competenesDuSalarie.size() > 0){
                        competenceSalerie = competenesDuSalarie.iterator().next();
                        competenceSalerie.setValue("DateDAcquisitionDeCompetence", new Date());
                        competenceSalerie.setValue("Notation0100", notation);
                        competenceSalerie.setValue("NiveauDeCompetence2", niveauDeCompetence);
                        competenceSalerie.setValue("ConfirmeCompetence",false);

                        if (comptence.getValue("TypeDeFormation").equals("Renouvelable") && comptence.getValue("DureeMois") != null) {
                            Calendar dateFin = Calendar.getInstance();
                            dateFin.add(Calendar.MONTH, ((Number) comptence.getValue("DureeMois")).intValue());
                            competenceSalerie.setValue("DateDExpiration", dateFin.getTime());
                        }

                    }else {
                    if (notation >= pourcentageMinimumPourAcquerirLaCompetence) {
                        competenceSalerie = getWorkflowModule().createStorageResource(context, definition, "");
                        competenceSalerie.setValue("sys_Title", comptence.getValue("sys_Title") + " : " +ficheCollaborateur.getValue("NomPrenom") );
                        competenceSalerie.setValue("Societe",comptence.getValue("Societe"));
                        competenceSalerie.setValue("FamilleCompetence",comptence.getValue("FamilleCompetence"));
                        competenceSalerie.setValue("Collaborateur", ficheCollaborateur);
                        competenceSalerie.setValue("Competence", comptence);
                        competenceSalerie.setValue("DateDAcquisitionDeCompetence", new Date());
                        competenceSalerie.setValue("Notation0100", notation);
                        competenceSalerie.setValue("NiveauDeCompetence2", niveauDeCompetence);
                        competenceSalerie.setValue("ConfirmeCompetence",false);

                        if (comptence.getValue("TypeDeFormation").equals("Renouvelable") && comptence.getValue("DureeMois") != null) {
                            Calendar dateFin = Calendar.getInstance();
                            dateFin.add(Calendar.MONTH, ((Number) comptence.getValue("DureeMois")).intValue());
                            competenceSalerie.setValue("DateDExpiration", dateFin.getTime());
                        }
                    }
                        competenceSalerie.save(getWorkflowModule().getSysadminContext());


                        if (ficheCollaborateur.getValue("Competence")!=null ) {

                            HashSet<IStorageResource> competences =new HashSet<>((Collection) ficheCollaborateur.getValue("Competence"));
                            competences.add(comptence);

                            ficheCollaborateur.setValue("Competence", competences);
                        }else {
                            ficheCollaborateur.setValue("Competence", comptence);
                        }


                        ficheCollaborateur.save(getWorkflowModule().getSysadminContext());
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }


    }

    private IStorageResource getNiveauDeCompetence(IStorageResource comptence , float notation) {
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "NiveauDeCompetence");
            controller.addGreaterOrEqualConstraint("PourcentageMaximum", notation);
            controller.addLessOrEqualsConstraint("PourcentageMinimum",notation);
            controller.addEqualsConstraint("Competence",comptence);
            return (IStorageResource) controller.evaluate(definition).iterator().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private IStorageResource getFicheByUser(IUser user){
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
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
