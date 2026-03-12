package com.moovapps.Formation.SuviFormation.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.*;

public class CummonSuivi extends BaseDocumentExtension {

    public String getIDToUSe() {
        return null;
    }

    public void calculateNotation() {
        // Collection<IWorkflowInstance> coursParParticipant = getCoursParParticipantByIDSession(getWorkflowInstance());
        IWorkflowInstance formationWorkflow = (IWorkflowInstance) getWorkflowInstance().getValue("FormationProcessus");
        ArrayList<ILinkedResource> NiveauEtCompetences = (ArrayList<ILinkedResource>) formationWorkflow.getValue("NiveauDeCompetencePourPasserLaFormation");
        double poidsTotale = 0;
        double notationCorrecte = 0;
        for (ILinkedResource NiveauEtCompetence : NiveauEtCompetences) {
            Collection<IStorageResource> questionReponseDeCompetence = getQuestionsReponseByID(getIDToUSe(), (IStorageResource) NiveauEtCompetence.getValue("Competence"));
            double poidsTotaleCompetence = 0;
            double notationCorrecteCompetence = 0;
            for (IStorageResource question : questionReponseDeCompetence) {
                double notation = question.getValue("Notation") != null ? ((Number) question.getValue("Notation")).doubleValue() : 0;
                double poids = question.getValue("Poids") != null ? ((Number) question.getValue("Poids")).doubleValue() : 0;
                poidsTotaleCompetence += poids;
                notationCorrecteCompetence += notation;
            }
            poidsTotale += poidsTotaleCompetence;
            notationCorrecte += notationCorrecteCompetence;

            try {
                ILinkedResource notationDeChaqueCompetence = getWorkflowInstance().createLinkedResource(getTableauDesNotes());
                IStorageResource competence = (IStorageResource) NiveauEtCompetence.getValue("Competence");
                double notation = 0;
                if (poidsTotaleCompetence != 0) {
                    notation = (notationCorrecteCompetence / poidsTotaleCompetence) * 100;
                }
                double pourcentageMinimumPourAcquerirLaCompetence = ((Number) NiveauEtCompetence.getValue(getPourcentageMinimumPourAcquisitionDuCompetence())).doubleValue();
                notationDeChaqueCompetence.setValue("Competence", competence);
                notationDeChaqueCompetence.setValue("Notation", notation);
                notationDeChaqueCompetence.setValue("PourcentageMinimumPourAcquisitionDeLaCompetence", pourcentageMinimumPourAcquerirLaCompetence);
                notationDeChaqueCompetence.setValue("NiveauDeCompetence", NiveauEtCompetence.getValue("NiveauCompetenceDeLaFormation"));
                if (notation >= pourcentageMinimumPourAcquerirLaCompetence) {
                    getWorkflowInstance().setValue("VousAvezUnCompetence", true);
                    notationDeChaqueCompetence.save(getWorkflowModule().getSysadminContext());
                    setCompetence(notationDeChaqueCompetence);
                } else {
                    notationDeChaqueCompetence.save(getWorkflowModule().getSysadminContext());
                }
                getWorkflowInstance().addLinkedResource(notationDeChaqueCompetence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            if (poidsTotale != 0) {
                getWorkflowInstance().setValue("NotationQuestionnaireAChaud0100", (notationCorrecte / poidsTotale) * 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


       /*  notationCorrecte = 0;
       for (IWorkflowInstance cours : coursParParticipant) {
            double notation = cours.getValue("Notation0100") != null ? ((Number) cours.getValue("Notation0100")).doubleValue() : 0;
            notationCorrecte += notation;

        }

        try {
            getWorkflowInstance().setValue("NotationQuestionnaireAChaud0100", (notationCorrecte / coursParParticipant.size()));
        } catch (Exception e) {
            e.printStackTrace();
        }

            double pourcentageNotationQuestionnaireAChaud = ((((Number) getWorkflowInstance().getValue("NotationQuestionnaireAChaud0100")).doubleValue()) *
                    (((Number) getWorkflowInstance().getValue("PourcentageDeQuestionnaireAChaud")).doubleValue())) / 100;
            double pourcentageNotationQuestionnaireAFroid = ((((Number) getWorkflowInstance().getValue("NotationQuestionnaireAFroid0100")).doubleValue()) *
                    (((Number) getWorkflowInstance().getValue("PourcentageDeQuestionnaireAFroid")).doubleValue())) / 100;
            getWorkflowInstance().setValue("PourcentageDeMoyenneGenerale", pourcentageNotationQuestionnaireAChaud + pourcentageNotationQuestionnaireAFroid);
            */
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());

    }

    public String getTableauDesNotes() {
        return null;
    }

    public String getPourcentageMinimumPourAcquisitionDuCompetence() {
        return null;
    }

    public Collection<IStorageResource> getQuestionsReponseByID(String idField, IStorageResource competence) {
        Collection<IStorageResource> QuestionsReponse = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions_Reponses");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint(idField, getWorkflowInstance().getValue(idField));
            controller.addEqualsConstraint("IDSeanceParticipant", getWorkflowInstance().getValue("IDSeanceParticipant"));
            if (competence != null) {
                controller.addInConstraint("Competence", competence);
            }
            QuestionsReponse = controller.evaluate(definition);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return QuestionsReponse;
    }

    public void setCompetence(ILinkedResource notationCompetence) {
        IUser participant = (IUser) getWorkflowInstance().getValue("Participant2");
        IStorageResource ficheCollaborateur = getFicheByUser(participant);
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
            float notation = ((Number) notationCompetence.getValue("Notation")).floatValue();
            float pourcentageMinimumPourAcquerirLaCompetence = ((Number) notationCompetence.getValue("PourcentageMinimumPourAcquisitionDeLaCompetence")).floatValue();
            if (notation >= pourcentageMinimumPourAcquerirLaCompetence) {
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
                competenceSalerie.setValue(getNotationField(), notation);
                competenceSalerie.setValue("NiveauDeCompetence", notationCompetence.getValue("NiveauDeCompetence"));
                if (getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid") != null && getWorkflowInstance().getValue("SouhaitezVousGererLesQuestionnaireAFroid").equals("Oui")) {
                    competenceSalerie.setValue("ConfirmeCompetence", false);
                } else {
                    competenceSalerie.setValue("ConfirmeCompetence", true);
                }
                if (comptence.getValue("TypeDeFormation").equals("Renouvelable") && comptence.getValue("DureeMois") != null) {
                    Calendar dateFin = Calendar.getInstance();
                    dateFin.add(Calendar.MONTH, ((Number) comptence.getValue("DureeMois")).intValue());
                    competenceSalerie.setValue("DateDExpiration", dateFin.getTime());
                }
                setConfirmer(competenceSalerie);
                competenceSalerie.save(getWorkflowModule().getSysadminContext());
            }
                /*if(competenesDuSalarie.size() > 0){
                    competenceSalerie = competenesDuSalarie.iterator().next();
                    competenceSalerie.setValue("DateDAcquisitionDeCompetence", new Date());
                    competenceSalerie.setValue("Notation0100", notation);
                    competenceSalerie.setValue("NiveauDeCompetence2", notationDechaqueCompetence.getValue("NiveauDeCompetence"));
                    competenceSalerie.setValue("ConfirmeCompetence",false);

                    if (comptence.getValue("TypeDeFormation").equals("Renouvelable") && comptence.getValue("DureeMois") != null) {
                        Calendar dateFin = Calendar.getInstance();
                        dateFin.add(Calendar.MONTH, ((Number) comptence.getValue("DureeMois")).intValue());
                        competenceSalerie.setValue("DateDExpiration", dateFin.getTime());
                    }
                    competenceSalerie.save(getWorkflowModule().getSysadminContext());

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
                        competenceSalerie.setValue("NiveauDeCompetence2", notationDechaqueCompetence.getValue("NiveauDeCompetence"));
                        competenceSalerie.setValue("ConfirmeCompetence",false);

                        if (comptence.getValue("TypeDeFormation").equals("Renouvelable") && comptence.getValue("DureeMois") != null) {
                            Calendar dateFin = Calendar.getInstance();
                            dateFin.add(Calendar.MONTH, ((Number) comptence.getValue("DureeMois")).intValue());
                            competenceSalerie.setValue("DateDExpiration", dateFin.getTime());
                        }
                        competenceSalerie.save(getWorkflowModule().getSysadminContext());
                    }*/


                    /*if (ficheCollaborateur.getValue("Competence")!=null ) {
                        HashSet<IStorageResource> competences =new HashSet<>((Collection) ficheCollaborateur.getValue("Competence"));
                        competences.add(comptence);
                        ficheCollaborateur.setValue("Competence", competences);
                    }else {
                        ficheCollaborateur.setValue("Competence", comptence);
                    }
                    ficheCollaborateur.save(getWorkflowModule().getSysadminContext());
                }*/


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void setConfirmer(IStorageResource competenceSalerie) {
    }

    public String getNotationField() {
        return "";
    }

    public Collection<IStorageResource> getQuestionsByID(String idFieldName) {
        Collection<IStorageResource> Questions = Collections.emptyList();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint(idFieldName, getWorkflowInstance().getValue(idFieldName));
            Questions = controller.evaluate(definition);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Questions;
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

    public void setTableNotation() {
       if (getWorkflowInstance().getValue(getTableauDesNotes()) == null) {
           ArrayList<ILinkedResource> NiveauEtCompetences = new ArrayList<>();
           if(getTableauDesNotes().equals("NotationDeChaqueCompetence")) {
               IWorkflowInstance formationWorkflow = (IWorkflowInstance) getWorkflowInstance().getValue("FormationProcessus");
                NiveauEtCompetences = (ArrayList<ILinkedResource>) formationWorkflow.getValue("NiveauDeCompetencePourPasserLaFormation");
               for (ILinkedResource tableDeCompetence : NiveauEtCompetences) {
                   ILinkedResource notationDeChaqueCompetence = getWorkflowInstance().createLinkedResource(getTableauDesNotes());
                   notationDeChaqueCompetence.setValue("Competence", tableDeCompetence.getValue("Competence"));
                   notationDeChaqueCompetence.setValue("PourcentageMinimumPourAcquisitionDeLaCompetence", tableDeCompetence.getValue(getPourcentageMinimumPourAcquisitionDuCompetence()));
                   notationDeChaqueCompetence.setValue("NiveauDeCompetence", tableDeCompetence.getValue("NiveauCompetenceDeLaFormation"));
                   notationDeChaqueCompetence.save(getWorkflowModule().getSysadminContext());

                   notationDeChaqueCompetence.save(getWorkflowModule().getSysadminContext());
                   getWorkflowInstance().addLinkedResource(notationDeChaqueCompetence);
               }
           }else {
               NiveauEtCompetences = (ArrayList<ILinkedResource>) getWorkflowInstance().getValue("NotationDeChaqueCompetence");
               for (ILinkedResource tableDeCompetence : NiveauEtCompetences) {
                   double notation = ((Number) tableDeCompetence.getValue("Notation")).doubleValue();
                   double pourcentageMinimumPourAcquisitionDeLaCompetence = ((Number) tableDeCompetence.getValue("PourcentageMinimumPourAcquisitionDeLaCompetence")).doubleValue();
                   if (pourcentageMinimumPourAcquisitionDeLaCompetence <= notation) {
                       ILinkedResource notationDeChaqueCompetence = getWorkflowInstance().createLinkedResource(getTableauDesNotes());
                       notationDeChaqueCompetence.setValue("Competence", tableDeCompetence.getValue("Competence"));
                       notationDeChaqueCompetence.setValue("PourcentageMinimumPourAcquisitionDeLaCompetence", tableDeCompetence.getValue("PourcentageMinimumPourAcquisitionDeLaCompetence"));
                       notationDeChaqueCompetence.setValue("NiveauDeCompetence", tableDeCompetence.getValue("NiveauDeCompetence"));
                       notationDeChaqueCompetence.save(getWorkflowModule().getSysadminContext());

                       notationDeChaqueCompetence.save(getWorkflowModule().getSysadminContext());
                       getWorkflowInstance().addLinkedResource(notationDeChaqueCompetence);
                   }
               }
           }


       }
    }

    public void calulateNotationSiParAttestation() {
        ArrayList<ILinkedResource> notationDeChaqueCompetence = (ArrayList<ILinkedResource>) getWorkflowInstance().getValue(getTableauDesNotes());

        for (ILinkedResource tableCompetence : notationDeChaqueCompetence) {
            double notation = tableCompetence.getValue("Notation") != null ? ((Number) tableCompetence.getValue("Notation")).doubleValue() : 0;
            double pourcentageMinimumPourAcquerirLaCompetence = tableCompetence.getValue(getPourcentageMinimumPourAcquisitionDuCompetence()) !=null ? ((Number) tableCompetence.getValue(getPourcentageMinimumPourAcquisitionDuCompetence())).doubleValue() : 0;
            if (notation >= pourcentageMinimumPourAcquerirLaCompetence) {
                getWorkflowInstance().setValue("VousAvezUnCompetence", true);
                setCompetence(tableCompetence);
                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
            }
        }
    }

}
