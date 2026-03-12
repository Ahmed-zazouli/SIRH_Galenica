package com.moovapps.Sanction.Views;

import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.listeners.ActionListener;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelColumn;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.axemble.vdp.ui.framework.widgets.CtlText;

import java.util.Collection;
import java.util.List;

public class SanctionsRelatives extends BaseViewExtension {

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
                 IWorkflowInstance sanctionToUpdate = (IWorkflowInstance) button.getParam();
                // String sourceID = sourceStorage!=null ? (String) sourceStorage.getValue("ID"):"";
                 //IWorkflowInstance source = getSourceByID(sourceID);
               //  getWorkflowInstance().setValue("isNew",false);
                 getWorkflowInstance().setValue("SanctionToUpdate",sanctionToUpdate);
                 getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
               //  source.setValue("SanctionEvolution",getWorkflowInstance());
                // source.save(getWorkflowModule().getSysadminContext());
              //   String lienSanction = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort()).concat(Turbine.getContextPath().concat("/easysite/workplace/applications/application-gestion-des-sanction-0/GestionDeProcessus/edit-document/" + getWorkflowInstance().getId().toString()));


                //ExternalScreen externalScreen = new ExternalScreen(lienSanction);
                //Navigator.getNavigator().pushScreen(externalScreen);













            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onPrepareColumns(@SuppressWarnings("rawtypes") List viewModelColumns) {
        if(getWorkflowInstance().getValue("DocumentState").equals("Demande envoyée pour validation RH")){
            ViewModelColumn propertyColumn = new ViewModelColumn("Evoluer", "Action");
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