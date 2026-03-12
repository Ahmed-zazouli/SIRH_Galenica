package com.vdoc.sanaEducation.candidatures.view;

import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IWorkflow;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.listeners.ActionListener;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelColumn;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.screens.ExternalScreen;
import com.axemble.vdp.ui.framework.runtime.InternalExecutionContext;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.axemble.vdp.ui.framework.widgets.CtlText;
import com.axemble.vdp.views.query.Definition;
import com.axemble.vdp.views.query.Field;
import com.axemble.vdp.views.query.Fieldgroup;
import com.axemble.vdp.views.query.ObjectFactory;
import org.apache.turbine.Turbine;

import java.util.List;


public class CandidatureSpontaneVueAnonyme extends BaseViewExtension {

    /**
     *
     */
    private static final long serialVersionUID = -1974364744827254415L;

    @Override
    public boolean onPrepareView(Definition viewDefinition) {

        Navigator navigator = Navigator.getNavigator();
        InternalExecutionContext ec = navigator.getExecutionContext();
        String[] ecoles = (String[]) ec.getContext().getParameterObject("ecole");
        if (ecoles != null) {
            if (ecoles.length > 0) {
                String ecole = ecoles[0];
                ObjectFactory objectFactory = new ObjectFactory();
                Fieldgroup mainFieldGroup = viewDefinition.getFilters().getFieldgroup();
                Field myField = objectFactory.createField();
                myField.setName("Ecole_ASPOSE");
                myField.setOperator("contains");
                myField.setValue(ecole);
                mainFieldGroup.getFieldgroupOrField().add(myField);
            } else {
                ObjectFactory objectFactory = new ObjectFactory();
                Fieldgroup mainFieldGroup = viewDefinition.getFilters().getFieldgroup();
                Field myField = objectFactory.createField();
                myField.setName("Ecole_ASPOSE");
                myField.setOperator("contains");
                myField.setValue("indisponible");
                mainFieldGroup.getFieldgroupOrField().add(myField);
            }
        }
        return super.onPrepareView(viewDefinition);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPrepareColumns(@SuppressWarnings("rawtypes") List viewModelColumns) {
        ViewModelColumn viewModelColumn2 = new ViewModelColumn("Candidature", "Candidature", ViewModelColumn.TYPE_OTHER);
        viewModelColumns.add(viewModelColumns.size(), viewModelColumn2);
        super.onPrepareColumns(viewModelColumns);
    }

    @Override
    public void onPrepareItem(ViewItem iViewItem) {
        try {
            ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            //row dans le document
            IWorkflowInstance workflowInstance = (IWorkflowInstance) iViewItem.getResource();
            CtlButton button = new CtlButton("ENVOYER", new CtlText("ENVOYER"));
            button.setParam(workflowInstance);
            button.addActionListener(Candidature);
            //topButtonContainer.add(button);
            viewModelItem.setValue("Candidature", button);

            //String acceder = "Accéder";
            //if(workflowInstance.getValue("IntitulePosteAPourvoir") != null){
            //	acceder = (String) workflowInstance.getValue("IntitulePosteAPourvoir");
            //}
            //CtlButton button2 = new CtlButton("POSTE", new CtlText(acceder));
            //button2.setParam(workflowInstance);
            //button2.addActionListener(Candidature);
            //row dans la vue
            //viewModelItem.setValue("IntitulePosteAPourvoir", button2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected ActionListener Candidature = new ActionListener() {
        private static final long serialVersionUID = -7040270052488139831L;

        public void onClick(ActionEvent paramActionEvent) {
            try {
                IContext context = getWorkflowModule().getLoggedOnUserContext();
                IContext sysContext = getWorkflowModule().getSysadminContext();
                CtlButton button = (CtlButton) paramActionEvent.getSource();
                IWorkflowInstance parentInstance = (IWorkflowInstance) button.getParam();
                IWorkflowInstance instance = null;
                //CandidatureSpontanee_1.0
                //Candidature_1.0
                IWorkflow workflow = getWorkflowModule().getWorkflow(sysContext, parentInstance.getCatalog(), "Candidature_1.0");
                instance = getWorkflowModule().createWorkflowInstance(context, workflow, "");
                instance.setValue("IsCandidatureSpontanee","Oui");
                instance.save(sysContext);
                //CandidatureSpontanee
                //Candidature
                parentInstance.addLinkedWorkflowInstance("Candidature", instance);
                parentInstance.save(sysContext);
//				String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort())
//						.concat(Turbine.getContextPath().concat(instance.getURI()));
                String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort()).concat(Turbine.getContextPath().concat(instance.getURI().replace("/easysite/plugin:sys.process/", "/easysite/workplace/candidature/candidature/edit-document/"))) + "?flag=true";
                //lien = "http://94.23.6.149:7080/vdoc/easysite/workplace/processus/create-document/workflow-container/22";
                ExternalScreen externalScreen = new ExternalScreen(lien);
                Navigator.getNavigator().pushScreen(externalScreen);


                //Navigator.getNavigator().setcurr
                //Navigator.getNavigator().setSessionPersistent(true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

}
