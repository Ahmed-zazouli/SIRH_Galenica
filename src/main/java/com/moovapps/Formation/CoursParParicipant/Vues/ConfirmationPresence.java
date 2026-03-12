package com.moovapps.Formation.CoursParParicipant.Vues;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.impl.ProcessStorageResource;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.activity.domain.TaskInstance;
import com.axemble.vdp.ui.framework.components.events.ActionEvent;
import com.axemble.vdp.ui.framework.components.events.ChangeEvent;
import com.axemble.vdp.ui.framework.components.listeners.ActionListener;
import com.axemble.vdp.ui.framework.components.listeners.ChangeListener;
import com.axemble.vdp.ui.framework.composites.IDocumentComposite;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.screens.ExternalScreen;
import com.axemble.vdp.ui.framework.widgets.*;
import com.axemble.vdp.ui.framework.widgets.blocks.sys.layouts.FormSectionBlock;
import com.axemble.vdp.workflow.domain.ProcessWorkflowInstance;
import com.moovapps.ibb.rh.FicheSalarie.Formulaire.CreationSalarie;
import org.apache.turbine.Turbine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ConfirmationPresence extends BaseViewExtension {

    @Override
    public void onPrepareItem(ViewItem iViewItem) {
        try {
            ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            IResource resource = iViewItem.getResource();
            if ((resource.getValue("Present") == null ||
                    !(boolean)resource.getValue("Present")) && ((IUser) resource.getValue("Validateur2")).getId().toString().equals(getDirectoryModule().getLoggedOnUser().getId().toString()) && viewModelItem.getValue("DocumentState").equals("Confirmation de présence")) {
                CtlCheckBox ctlCheckBox = new CtlCheckBox(resource.getValue("Present") != null && (boolean) resource.getValue("Present"));
                ctlCheckBox.setSysname("Present");
                ctlCheckBox.setThrowEvents(true);
                ctlCheckBox.setParam(resource);

                @SuppressWarnings("serial")
                ChangeListener listener = new ChangeListener() {
                    private static final long serialVersionUID = 1L;

                    public void onChange(ChangeEvent paramChangeEvent) {
                        IContext context = getWorkflowModule().getLoggedOnUserContext();
                        IResource resourceClicked = null;
                        Object object = paramChangeEvent.getSource();
                        ((CtlCheckBox)object).setEditable(false);
                       /* try {
                            ProcessStorageResource resource = (ProcessStorageResource) iViewItem.getResource();
                        } catch (WorkflowModuleException e) {
                            throw new RuntimeException(e);
                        }*/

                        if (CtlTextBox.class.equals(object.getClass())) {
                            CtlTextBox txtBox = (CtlTextBox) object;
                            resourceClicked = (IResource) txtBox.getParam();
                            resourceClicked.setValue("Present", txtBox.getLabel());
                            resourceClicked.save(context);
                        } else if (CtlNumber.class.equals(object.getClass())) {
                            CtlNumber number = (CtlNumber) object;
                            resourceClicked = (IResource) number.getParam();
                            resourceClicked.setValue("Present", number.getFloatValue());
                            resourceClicked.save(context);
                        } else if (CtlComboBox.class.equals(object.getClass())) {
                            CtlComboBox box = (CtlComboBox) object;
                            resourceClicked = (IResource) box.getParam();
                            resourceClicked.setValue("Present", box.getSelectedKey());
                            resourceClicked.save(context);
                        } else if (CtlAutocompleteList.class.equals(object.getClass())) {
                            CtlAutocompleteList box = (CtlAutocompleteList) object;
                            resourceClicked = (IResource) box.getParam();
                            resourceClicked.setValue("Present", box.getSelectedKey());
                            resourceClicked.save(context);
                            getView().refreshItems();
                        } else if (CtlRadioGroup.class.equals(object.getClass())) {
                            CtlRadioGroup radio = (CtlRadioGroup) object;
                            resourceClicked = (IResource) radio.getParam();
                            resourceClicked.setValue("Present", radio.getSelectedKey());
                            resourceClicked.save(context);
                        } else if (CtlDate.class.equals(object.getClass())) {
                            CtlDate date = (CtlDate) object;
                            resourceClicked = (IResource) date.getParam();
                            resourceClicked.setValue("Present", date.getDate());
                            resourceClicked.save(context);
                        } else if (CtlCheckBox.class.equals(object.getClass())) {

                            CtlCheckBox checkBox = (CtlCheckBox) object;
                            try {
                                //ViewItem iWorkflowInstance1 =  (ViewItem) checkBox.getParam();
//                               IResource r = iWorkflowInstance1.getResource();
//                               String ref = (String) r.getValue("sys_Reference");
                                IWorkflowInstance instance = (IWorkflowInstance) checkBox.getParam();
                                //IWorkflowInstance instance = getWorkflowInstancebyReference((String) iWorkrflowInstance1.getValue("sys_Reference"));
                                IUser validateur = (IUser) instance.getValue("Validateur2");
                                ITaskInstance iTaskInstance = instance.getCurrentTaskInstance(getWorkflowModule().getContext(validateur));
                                ITask task = iTaskInstance.getTask();
                                IAction iAction1 = task.getAction("Valide");
                                getWorkflowModule().end(getWorkflowModule().getContext(validateur), iTaskInstance, iAction1, "");
                                instance.save(getWorkflowModule().getSysadminContext());
                            } catch (Exception e) {
                                try {
                                     checkBox = (CtlCheckBox) object;
                                    ProcessStorageResource processStorageResourceinstance = (ProcessStorageResource) checkBox.getParam();
                                    IWorkflowInstance instance = getWorkflowInstancebyReference((String) processStorageResourceinstance.getValue("sys_Reference"));
                                    IUser validateur = (IUser) instance.getValue("Validateur2");
                                    ITaskInstance iTaskInstance = instance.getCurrentTaskInstance(getWorkflowModule().getContext(validateur));
                                    ITask task = iTaskInstance.getTask();
                                    IAction iAction1 = task.getAction("Valide");
                                    getWorkflowModule().end(getWorkflowModule().getContext(validateur), iTaskInstance, iAction1, "");
                                    instance.save(getWorkflowModule().getSysadminContext());

                                }catch (Exception e2) {
                                    e2.printStackTrace();

                                }
                            }
                            resourceClicked = (IResource) checkBox.getParam();
                            resourceClicked.setValue("Present", checkBox.getValidationObject());
                            resourceClicked.save(context);
                        }
                    }
                };
                ctlCheckBox.addChangeListener(listener);
                viewModelItem.setValue("Present", ctlCheckBox);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    IWorkflowInstance getWorkflowInstancebyReference(String reference) {

        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Formation", project);
            IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "CoursParParticipant_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("sys_Reference", reference);
            ArrayList<IWorkflowInstance> instances = (ArrayList<IWorkflowInstance>) controller.evaluate(w);
            if (instances != null && !instances.isEmpty()) {
                return instances.iterator().next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void refreshSection(String sectionName) {
        IDocumentComposite documentComposite = null;
        try {
            documentComposite = (IDocumentComposite) Navigator.getNavigator().getCurrentNavigation();
        } catch (ClassCastException e) {
            documentComposite = (IDocumentComposite) Navigator.getNavigator().getRootNavigator().getPartByName("ezs").getCurrentNavigation();
        }
        //documentComposite.getBody().getViews().iterator().next().refresh();
        List<FormSectionBlock> Sections = documentComposite.getBody().getSections();
        Sections.forEach(section -> {
            if (section.getTitle() != null && section.getTitle().equals(sectionName)) {
                section.refresh();
                section.refreshWidgets();
            }
        });
    }
    /*@Override
    public void onPrepareItem(ViewItem iViewItem) {
        try {
            ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            //row dans le document
            ProcessStorageResource resource = (ProcessStorageResource) iViewItem.getResource();
            if(resource.getValue("DocumentState").equals("Confirmation de présence")){
                CtlButton declarationPresence = new CtlButton("ConfirmerPresence", new CtlText("Confirmer la présence"));
                //button.setStyle("border-radius: 15px !important;font-weight: none  !important;font-size: 16px !important;border: 2px !important;");
                declarationPresence.setParam(resource);
                viewModelItem.setValue("ConfirmationPresence", declarationPresence);
                declarationPresence.addActionListener(onConfirmerPresence);

                //row dans le document
                CtlButton declarationAbsence = new CtlButton("DeclarerAbsence", new CtlText("Déclarer l'absence"));
                //button.setStyle("border-radius: 15px !important;font-weight: none  !important;font-size: 16px !important;border: 2px !important;");
                declarationAbsence.setParam(resource);
                viewModelItem.setValue("DeclarationAbsence", declarationAbsence);
                declarationAbsence.addActionListener(onDeclarerAbsence);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    protected ActionListener onDeclarerAbsence = event -> {
        try {
            IResource resource = (IResource) ((CtlButton)event.getSource()).getParam();
            resource.setValue("PresentS", false);
            resource.save(getWorkflowModule().getSysadminContext());
            passerALetapeSuivante(resource);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };



    protected ActionListener onConfirmerPresence = event -> {
        try {
            IResource resource = (IResource) ((CtlButton)event.getSource()).getParam();
            resource.setValue("Present", true);
            resource.save(getWorkflowModule().getSysadminContext());
            passerALetapeSuivante(resource);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    private void passerALetapeSuivante(IResource resource) {
        try {
            List<TaskInstance> instances = ((ProcessWorkflowInstance)resource).getTaskInstances(2);
            List<String> actions = Arrays.asList("ValiderLAchevementDuCours");
            while ( !instances.isEmpty() ){
                TaskInstance taskInstance = instances.iterator().next();
                taskInstance.removeOperators();
                taskInstance.addOperator(getWorkflowModule().getOperatorByLogin("sysadmin"));
                ITask task = taskInstance.getTask();

                for (String action : actions) {
                    IAction iAction1 = task.getAction(action);
                    if (iAction1 != null) {
                        getWorkflowModule().end(getWorkflowModule().getSysadminContext(), taskInstance, iAction1, "");
                        resource.save(getWorkflowModule().getSysadminContext());
                        break;
                    }
                }
                instances = ((ProcessWorkflowInstance)resource).getTaskInstances(2);

            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/
}
