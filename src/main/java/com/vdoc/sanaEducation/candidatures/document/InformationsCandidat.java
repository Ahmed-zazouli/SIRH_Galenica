package com.vdoc.sanaEducation.candidatures.document;

//import java.awt.event.ActionEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.listeners.ConfirmBoxListener;
import com.axemble.vdp.ui.framework.foundation.screens.IScreen;
import org.apache.turbine.Turbine;

import recrutement.candidature.CreationNouveauPosteAPourvoir;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.screens.ExternalScreen;
import com.axemble.vdp.ui.framework.foundation.screens.Screen;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.axemble.vdp.ui.framework.widgets.CtlText;

public class InformationsCandidat extends BaseDocumentExtension {


    @Override
    public boolean onBeforeLoad() {
        RemplirLangue();
        SetInfoPoste();
        String[] parameterValues = Navigator.getNavigator().getExecutionContext().getRequest().getParameterValues("flag");
        String flag = null;
        if (parameterValues != null && parameterValues.length > 0) {
            flag = parameterValues[0];
        }
        if (flag != null) {
            getWorkflowInstance().setValue("IsCandidatureSpontanee", "Oui");
        } else {
            IWorkflowInstance parentInstance = getWorkflowInstance().getParentInstance();
            if (parentInstance != null) {

                getWorkflowInstance().setValue("IntitulePosteAPourvoir", parentInstance.getValue("IntitulePosteAPourvoir") != null ? parentInstance.getValue("IntitulePosteAPourvoir") : null);
                getWorkflowInstance().setValue("DescriptifPoste", parentInstance.getValue("DescriptifPoste") != null ? parentInstance.getValue("DescriptifPoste") : null);
                getWorkflowInstance().setValue("PosteDateDeCreation", parentInstance.getValue("sys_CreationDate") != null ? parentInstance.getValue("sys_CreationDate") : null);
                getWorkflowInstance().setValue("IntroductionGroupe", parentInstance.getValue("IntroductionGroupe") != null ? parentInstance.getValue("IntroductionGroupe") : null);
                getWorkflowInstance().setValue("IntroductionEtablissement", parentInstance.getValue("IntroductionEtablissement") != null ? parentInstance.getValue("IntroductionEtablissement") : null);
                IStorageResource pays = (IStorageResource) parentInstance.getValue("Pays");
                IStorageResource ville = (IStorageResource) parentInstance.getValue("Ville");
                if (pays != null) {
                    getWorkflowInstance().setValue("paysPosteAPourvoir", pays.getValue("sys_Title"));
                }
                if (ville != null) {
                    getWorkflowInstance().setValue("VillePosteAPourvoir", ville.getValue("sys_Title"));
                }


                getWorkflowInstance().setValue("Groupe", parentInstance.getValue("Groupe") != null ? parentInstance.getValue("Groupe") : null);
                getWorkflowInstance().setValue("EcoleClient", parentInstance.getValue("EcoleClient") != null ? parentInstance.getValue("EcoleClient") : null);


            }
        }

        return super.onBeforeLoad();
    }


    @Override
    public boolean onBeforeSubmit(IAction action) {
        confirmCandidature();
        getWorkflowInstance().save(getWorkflowModule().getLoggedOnUserContext());
        getWorkflowInstance().getParentInstance().save(getWorkflowModule().getLoggedOnUserContext());
        return super.onBeforeSubmit(action);
    }

    @Override
    public boolean onAfterLoad() {

        return super.onAfterLoad();
    }

    ArrayList<IStorageResource> makeCopy(ArrayList<IStorageResource> storages){
        ArrayList<IStorageResource> copy = new ArrayList<IStorageResource>();
        for(IStorageResource storageResource:storages){
            copy.add(storageResource);
        }
        return copy;
    }
    void RemplirLangue() {
        ArrayList<ILinkedResource> parentLangue = (ArrayList<ILinkedResource>) getWorkflowInstance().getParentInstance().getLinkedResources("Langue");
        ArrayList<ILinkedResource> chiledLangue = (ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("Langue");

        if (parentLangue.isEmpty()) return;
        if (chiledLangue.isEmpty()) {
            for (ILinkedResource langueLine : parentLangue) {
                ILinkedResource langue = getWorkflowInstance().createLinkedResource("Langue");
                langue.setValue("Langue", langueLine.getValue("Langue"));
                langue.setValue("NiveauDeLangue2", langueLine.getValue("NiveauDeLangue2"));
                langue.save(getWorkflowModule().getSysadminContext());
                getWorkflowInstance().addLinkedResource(langue);

            }
        }

    }

    void SetInfoPoste() {
        IWorkflowInstance Poste = getWorkflowInstance().getParentInstance();
        IWorkflowInstance candidature = getWorkflowInstance();

        candidature.setValue("DateDeCreationDuPoste", Poste.getValue("sys_CreationDate") != null ? Poste.getValue("sys_CreationDate") : null);
        candidature.setValue("IntroductionDeLEtablissement", Poste.getValue("IntroductionEtablissement") != null ? Poste.getValue("IntroductionEtablissement") : null);
        if (Poste.getValue("Ville") != null) {
            candidature.setValue("Ville3", ((IStorageResource) Poste.getValue("Ville")).getValue("sys_Title"));

        }
        if (Poste.getValue("Pays") != null) {
            candidature.setValue("Pays3", ((IStorageResource) Poste.getValue("Pays")).getValue("sys_Title"));

        }

        candidature.setValue("NiveauDEtudeMulti", Poste.getValue("NiveauDEtudeMulti") != null ? Poste.getValue("NiveauDEtudeMulti") : null);
        candidature.setValue("DisciplineDeDiplomeMulti", Poste.getValue("DisciplineDeDiplomeMulti") != null ? Poste.getValue("DisciplineDeDiplomeMulti") : null);
        candidature.setValue("DisciplineDeDiplomeAutre", Poste.getValue("DisciplineDiplomeAutre") != null ? Poste.getValue("DisciplineDiplomeAutre") : null);
        candidature.setValue("NombreDeCandidatureSouhaite", Poste.getValue("NombreDeCandidatureSouhaite") != null ? Poste.getValue("NombreDeCandidatureSouhaite") : null);
        candidature.setValue("DateDeDemarrageSouhaite", Poste.getValue("DateDeDemarrageSouhaite") != null ? Poste.getValue("DateDeDemarrageSouhaite") : null);
        candidature.setValue("ExperienceDansLeSystemeAFE", Poste.getValue("ExperiencesDansLeSystemeAFE") != null ? Poste.getValue("ExperiencesDansLeSystemeAFE") : null);
        candidature.setValue("TypeContrat", Poste.getValue("TypeContrat") != null ? Poste.getValue("TypeContrat") : null);
        candidature.setValue("TypeContratAutre", Poste.getValue("TypeContratAutre") != null ? Poste.getValue("TypeContratAutre") : null);
        candidature.setValue("DiplomeMulti", Poste.getValue("DiplomeMulti") != null ? Poste.getValue("DiplomeMulti") : null);
        candidature.setValue("AnneesDExperience", Poste.getValue("AnneesDExperience") != null ? Poste.getValue("AnneesDExperience") : null);
        candidature.setValue("AutreTypeDuDiplome2", Poste.getValue("AutreTypeDuDiplome2") != null ? Poste.getValue("AutreTypeDuDiplome2") : null);
        candidature.setValue("DisciplineDeDiplomeAutre", Poste.getValue("DisciplineDiplomeAutre") != null ? Poste.getValue("DisciplineDiplomeAutre") : null);

        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());

    }


    /*private static com.axemble.vdoc.sdk.utils.Logger LOG = com.axemble.vdoc.sdk.utils.Logger.getLogger(InformationsCandidat.class);
    protected MyConfirmBoxListener myConfirmBoxListener = null;

    public class MyConfirmBoxListener implements ConfirmBoxListener {
        protected IAction iAction = null;
        protected boolean accept = false;

        public MyConfirmBoxListener(IAction iAction) {
            super();
            this.iAction = iAction;
        }

        public void onCancel(ActionEvent actionEvent) {
            accept = false;
        }

        public void onOk(ActionEvent actionEvent) {
            CtlButton actionButton = getResourceController().getButton(iAction.getLabel(), IResourceController.BOTTOM_CONTAINER);

            *//*executer l’action declenchant l’alerte*//*

            Navigator.getNavigator().enqueue2(new ActionEvent(actionButton, ActionEvent.ONCLICK));

            *//* pour ne pas lancer à nouveau le confirm box on met accept a true*//*
            accept = true;
        }

        public boolean isAccept() {
            return accept;
        }
    }*/

    /* on before submit sera appeler lorsquel’utilisateur execute une action de confirmation ou non */

    /*public boolean onBeforeSubmit(IAction action) {
        try {
            boolean myTest = false;

            if (myConfirmBoxListener == null) {
                myConfirmBoxListener = new MyConfirmBoxListener(action);
            }

            if ((!myTest) && (!myConfirmBoxListener.isAccept())) {
                Navigator.getNavigator().getRootNavigator().showConfirmBox("Etes-vous certain ?", myConfirmBoxListener);
                                *//* ou bien a travers l'interface ResourceController
				 *
				getResourceController().confirm("Etes-vous certain ?", myConfirmBoxListener);
				*//*
                return false;
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null) {
                message = "";
            }
            LOG.error("Error in ConfirmBoxDocumentExtension onBeforeSubmit method : " + e.getClass() + " - " + message);
        }
        return super.onBeforeSubmit(action);
    }*/

    public void confirmCandidature() {
        try {
            if(getWorkflowModule().getLoggedOnUser().isAnonymous()){
                Navigator.getNavigator().getRootNavigator().showAlertBox("Nous accusons réception de votre candidature et vous remercions de l'intérêt que vous portez à notre établissement .\n" );
               getResourceController().alert("test");
                /* + "Nous allons procéder à l'étude de votre dossier .\n" +
                        "Si votre profil correspond au poste proposé,nous vous contacterons dans les meilleurs délais.");*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
