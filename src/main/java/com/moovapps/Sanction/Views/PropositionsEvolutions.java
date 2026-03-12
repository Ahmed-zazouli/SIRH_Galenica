package com.moovapps.Sanction.Views;

import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.listeners.ActionListener;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelColumn;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.screens.ExternalScreen;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.axemble.vdp.ui.framework.widgets.CtlText;
import org.apache.turbine.Turbine;

import java.util.Collection;
import java.util.List;

public class PropositionsEvolutions extends BaseViewExtension {

    @Override
    public void onPrepareItem(ViewItem iViewItem) {
        //setReferenceClickable(iViewItem);
        try {
            ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            IResource resource = iViewItem.getResource();
            //row dans le document

            CtlButton button = new CtlButton("Evoluer", new CtlText("Evoluer"));
            button.setParam(resource);
            button.addActionListener(sanction);
            viewModelItem.setValue("Evoluer", button);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected ActionListener sanction = new ActionListener()
    {
        private static final long serialVersionUID = -7040270052488139831L;

        public void onClick(ActionEvent paramActionEvent) {
            try
            {
                CtlButton button = (CtlButton) paramActionEvent.getSource();
                IStorageResource sourceStorage = (IStorageResource) button.getParam();
              //creer wf
                //remplir
                //passe l etape
                //go to it
                IWorkflowInstance faute = createFaute();
                if(faute == null)return;
                RemplirFauteFields(faute,sourceStorage);
                GoToTheRHEtape(faute);
                //Navigator code
                String lienFaute = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort()).concat(Turbine.getContextPath().concat("/easysite/workplace/applications/application-gestion-des-sanction-0/GestionDeProcessus/edit-document/" + faute.getId().toString()));
                ExternalScreen externalScreen = new ExternalScreen(lienFaute);
                Navigator.getNavigator().pushScreen(externalScreen);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    private void GoToTheRHEtape(IWorkflowInstance faute) {
        try{
            ITaskInstance taskInstance = faute.getCurrentTaskInstance(getWorkflowModule().getContext(faute.getCreatedBy()));
            if(taskInstance!=null){
                ITask task = taskInstance.getTask();
                if(task!=null){
                    IAction iAction1 = task.getAction("Envoyer");
                    if(iAction1!=null){
                        getWorkflowModule().end(getWorkflowModule().getContext(faute.getCreatedBy()), taskInstance, iAction1, "");
                    }
                    faute.save(getWorkflowModule().getContext(faute.getCreatedBy()));

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void RemplirFauteFields(IWorkflowInstance faute , IStorageResource sanction){
        faute.setValue("PersonneConcernee",sanction.getValue("PersonneConcernee"));
        faute.setValue("Motif",sanction.getValue("Motif"));
        faute.setValue("AutreMotif",sanction.getValue("AutreMotif"));
        faute.setValue("NatureLegale",sanction.getValue("NatureLegale"));
        faute.setValue("Justification",sanction.getValue("Justification"));
        faute.setValue("GraviteDeLaFaute",sanction.getValue("GraviteDeLaFaute"));
        faute.setValue("ID",faute.getId().toString());
        faute.setValue("sys_Title",faute.getValue("sys_Reference"));
        faute.setValue("isNew",false);
        faute.setValue("Sanction",sanction);
        faute.save(getWorkflowModule().getSysadminContext());

    }

    private IWorkflowInstance createFaute(){
        IWorkflowInstance faute = null;
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IContext userContext = getWorkflowModule().getLoggedOnUserContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,"");
            IProject project = getProjectModule().getProject(context,"Sanction",organization);
            ICatalog sanctionCatalog  = getWorkflowModule().getCatalog(context,"GestionDesSanctions",project);
            IWorkflow sanction = getWorkflowModule().getWorkflow(context, sanctionCatalog, "CreationSanction_1.0");
            faute = getWorkflowModule().createWorkflowInstance(userContext,sanction,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return faute;
    }

    @Override
    public void onPrepareColumns(@SuppressWarnings("rawtypes") List viewModelColumns) {
        if(getWorkflowInstance().getValue("DocumentState").equals("Demande envoyée pour validation RH")){
            ViewModelColumn propertyColumn = new ViewModelColumn("Evoluer", "Evoluer");
            propertyColumn.setZone(ViewModelColumn.TYPE_OTHER);
            viewModelColumns.add(propertyColumn);
        }

        super.onPrepareColumns(viewModelColumns);
    }

    IWorkflowInstance getSourceByID(String id){
        IWorkflowInstance source = null;
        try
        {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Sanction", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "GestionDesSanctions", project);
            IWorkflow workflow = getWorkflowModule().getWorkflow(context, catalog, "CreationSanction_1.0");
            IViewController controller = getWorkflowModule().getViewController(context);
            controller.addEqualsConstraint("ID",id);
            Collection<IWorkflowInstance> collection =  controller.evaluate(workflow);
            if(collection!=null && !collection.isEmpty()){
                source = collection.iterator().next();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return source;
    }
}