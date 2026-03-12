package com.moovapps.ibb.rh.GestionPaieV2.Formulaires;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Calendar;
import java.util.Collection;

public class DemandeGeneral extends BaseDocumentExtension {

    @Override
    public boolean onAfterLoad() {
        IUser Createur = (IUser) getWorkflowInstance().getValue("sys_Creator");
        IStorageResource societe = (IStorageResource) Createur.getExtendedAttributes().getValue("Societe");
        getWorkflowInstance().setValue("Societe2",societe);
        if(getWorkflowInstance().getValue("DateDemande") == null){
            Calendar c = Calendar.getInstance();
            getWorkflowInstance().setValue("DateDemande",c.getTime());
        }
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("Confirmer") || action.getName().equals("Refuser")){
            getWorkflowInstance().setValue("Confirmateur",getWorkflowModule().getLoggedOnUser());
            getWorkflowInstance().save(getWorkflowModule().getLoggedOnUserContext());
        }

        return super.onBeforeSubmit(action);
    }

    /*
        private void addDemandeToHistoriqueDemande(IAction action ,  IWorkflowInstance workflowInstance) {
            try {
                IContext sysContext = getWorkflowModule().getSysadminContext();
                IProject project = getProjectModule().getProject(sysContext, "RemonteVariableDeLaPaie", getWorkflowInstance().getCatalog().getProject().getOrganization());
                ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "JournalDeLaPaie", 4, project);
                IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "HistoriqueDemande");
                IStorageResource historiqueDemande = getWorkflowModule().createStorageResource(sysContext, definition, "", "");
                historiqueDemande.setValue("sys_Title",workflowInstance.getValue("Demande"));
                historiqueDemande.setValue("CreateurDemande",workflowInstance.getValue("sys_Creator"));
                historiqueDemande.setValue("DateDemande",workflowInstance.getValue("DateDemande"));
                historiqueDemande.setValue("StatusDemande",workflowInstance.getValue(action.getLabel()));
                historiqueDemande.save(sysContext);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    */
}
