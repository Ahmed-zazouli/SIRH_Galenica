package com.moovapps.Formation.Cours.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;

import java.util.*;

public class NotationDeCours extends BaseDocumentExtension {
    @Override
    public boolean onAfterSubmit(IAction action) {
        if (action.getName().equals("ValiderLAchevementDuCours")) {
           // setCompetence();
        }


        return super.onAfterSubmit(action);
    }

    public boolean onBeforeLoad() {
     /*   IResourceController iResourceController = getResourceController();
        NamedContainer namedContainer = iResourceController.getButtonContainer(2);
        List<IWidget> widgets = namedContainer.getWidgets();
        for (IWidget iWidget : widgets){
            CtlButton button = (CtlButton)iWidget;
            if(button.getName().equals("Retourner au questionnaire")){
                button.setHidden(true);
            }
        }*/
        return super.onBeforeLoad();
   }

    public void setCompetence() {
        IUser participant = (IUser) getWorkflowInstance().getValue("Participant2");

        Collection<IWorkflowInstance> allCours = getAllCours(participant);
        float scoring = 0 ;
        boolean isAllCloture = true ;
        for (IWorkflowInstance cours : allCours){
            if (!cours.getValue("DocumentState").equals("Clôturé")){
                isAllCloture = false ;
                break;
            }else {
                scoring += ((Number) cours.getValue("Notation0100")).floatValue() ;
            }
        }
        float pourcentageResult = scoring / allCours.size();
        float pourcentageMinimum = ((Number) getWorkflowInstance().getParentInstance().getParentInstance().getParentInstance().getValue("PourcentageMinimumPourAcquerirLaCompetence")).floatValue();
        if (isAllCloture && pourcentageResult >=pourcentageMinimum ) {
            try {
                IStorageResource ficheCollaborateur = getFicheByUser(participant);
                ArrayList<IStorageResource> competenesAAcquerir = (ArrayList<IStorageResource>) getWorkflowInstance().getParentInstance().getParentInstance().getParentInstance().getValue("Competence");
                for (IStorageResource comptence: competenesAAcquerir) {
                    IContext context = getWorkflowModule().getSysadminContext();
                    IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
                    IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
                    ICatalog catalog  = getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,project);
                    IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"CompetenceSalarie");
                    IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
                    controller.addEqualsConstraint("Collaborateur", ficheCollaborateur);
                    controller.addEqualsConstraint("Competence", comptence);

                    IStorageResource competenceSalerie = null;
                    ArrayList<IStorageResource> competenesDuSalarie = (ArrayList<IStorageResource>) controller.evaluate(definition);
                    if(competenesDuSalarie.size() > 0){
                        competenceSalerie = competenesDuSalarie.iterator().next();

                    }else {
                        competenceSalerie = getWorkflowModule().createStorageResource(context,definition,"");
                        competenceSalerie.setValue("Collaborateur", ficheCollaborateur);
                        competenceSalerie.setValue("Competence", comptence);
                        competenceSalerie.setValue("DateDAcquisitionDeCompetence", new Date());
                        competenceSalerie.setValue("Notation0100", pourcentageResult);


                        if (comptence.getValue("TypeDeFormation").equals("Renouvelable") && comptence.getValue("DureeMois") != null){
                            Calendar dateFin = Calendar.getInstance();
                            dateFin.add(Calendar.MONTH, ((Number) comptence.getValue("DureeMois")).intValue());
                            competenceSalerie.setValue("DateDExpiration",dateFin.getTime());
                        }
                    }
                    competenceSalerie.save(getWorkflowModule().getSysadminContext());
                }

            }catch (Exception e){
                e.printStackTrace();
            }


            IStorageResource ParticipantFiche = null;

            try {
                IContext context = getWorkflowModule().getSysadminContext();
                IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
                IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
                ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
                IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
                controller.addEqualsConstraint("Salarie", participant);
                Collection<IStorageResource> demandeurFiches = controller.evaluate(definition);
                if (!demandeurFiches.isEmpty()) {
                    ParticipantFiche = demandeurFiches.iterator().next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (ParticipantFiche.getValue("Competence")!=null ) {

                HashSet<IStorageResource> competence =new HashSet<>((Collection) ParticipantFiche.getValue("Competence"));
                //ArrayList<IStorageResource> competenceToAdd = (ArrayList<IStorageResource>) getWorkflowInstance().getParentInstance().getParentInstance().getParentInstance().getValue("Competence");
                competence.addAll((Collection<? extends IStorageResource>) getWorkflowInstance().getParentInstance().getParentInstance().getParentInstance().getValue("Competence"));

                ParticipantFiche.setValue("Competence", competence);
            }else {
                ParticipantFiche.setValue("Competence", getWorkflowInstance().getParentInstance().getParentInstance().getParentInstance().getValue("Competence"));
            }


            ParticipantFiche.save(getWorkflowModule().getSysadminContext());
        }
    }

    public Collection<IWorkflowInstance> getAllCours(IUser Pparticipant) {
        Collection<IWorkflowInstance> collection = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Formation", project);
            IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "CoursParParticipant_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("Participant2", Pparticipant);
            controller.addEqualsConstraint("SiQuiz",true);
            //controller.addNotEqualsConstraint("DocumentState", "Clôturé");
            // controller.addEqualsConstraint("Formation2", Pformation);
            controller.addEqualsConstraint("SessionDeFormationProcessus", getWorkflowInstance().getValue("SessionDeFormationProcessus"));
            collection = controller.evaluate(w);
            return collection;
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

}
