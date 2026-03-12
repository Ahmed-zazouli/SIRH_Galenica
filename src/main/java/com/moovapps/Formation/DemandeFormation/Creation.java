package com.moovapps.Formation.DemandeFormation;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

public class Creation extends BaseDocumentExtension {

    @Override
    public boolean onAfterLoad() {
        if(getWorkflowInstance().getValue("Societe") == null) {
            IStorageResource societe = (IStorageResource) getWorkflowInstance().getCreatedBy().getExtendedAttributes().getValue("Societe");
            getWorkflowInstance().setValue("Societe" , societe) ;
        }
        if (getWorkflowInstance().getValue("siJeSuisLeDemandeur") == null) {
            getWorkflowInstance().setValue("siJeSuisLeDemandeur", true);
            onAmITheConcernedPersonFieldChange();
        }

        return super.onAfterLoad();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("siJeSuisLeDemandeur")){
            onAmITheConcernedPersonFieldChange() ;
        } else if (property.getName().equals("Demandeur")) {
            onConcernedPersonChange();
            IStorageResource societe = null;
            if (getWorkflowInstance().getValue("Demandeur") != null) {
                IStorageResource demandeur = (IStorageResource) getWorkflowInstance().getValue("Demandeur");
                societe = (IStorageResource) demandeur.getValue("Societe");
                getWorkflowInstance().setValue("Pole" , demandeur.getValue("Pole"));
                getWorkflowInstance().setValue("Societe" , societe);
                 getWorkflowInstance().setValue("Societe", societe);
                getWorkflowInstance().setValue("ResponsableRH", societe != null ? societe.getValue("ResponsableRH") : null);
                getWorkflowInstance().setValue("ResponsableDev", societe != null ? societe.getValue("ResponsableDev") : null);
            }else {
                getWorkflowInstance().setValue("Pole" , null);
                getWorkflowInstance().setValue("Societe" , null);
                getWorkflowInstance().setValue("ResponsableRH",  null);
                getWorkflowInstance().setValue("ResponsableDev", null);
            }
        }
        super.onPropertyChanged(property);
    }

    public void onAmITheConcernedPersonFieldChange() {
        boolean amITheConcernedPerson = (boolean) getWorkflowInstance().getValue("siJeSuisLeDemandeur");
        if (amITheConcernedPerson) {
            getWorkflowInstance().setValue("Demandeur", getCollaborateurBySalarie(getDocument().getUser()));
            onConcernedPersonChange();
        } else {
            getWorkflowInstance().setValue("Demandeur", null);
            onConcernedPersonChange();

        }
    }
    public void onConcernedPersonChange() {
        IUser demandeur = null ;
        if (getWorkflowInstance().getValue("Demandeur") != null) {
            if (((IStorageResource) getWorkflowInstance().getValue("Demandeur")).equals(getCollaborateurBySalarie(getDocument().getUser()))) {
                getWorkflowInstance().setValue("siJeSuisLeDemandeur", true);
            }


        }

    }
    private IStorageResource getCollaborateurBySalarie(IUser salarie) {
        IStorageResource collaborateur = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("Salarie", salarie);
            if (!controller.evaluate(definition).isEmpty()) {
                collaborateur = (IStorageResource) controller.evaluate(definition).iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collaborateur;

    }
}
