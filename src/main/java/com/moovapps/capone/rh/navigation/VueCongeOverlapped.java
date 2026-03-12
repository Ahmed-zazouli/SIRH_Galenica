package com.moovapps.capone.rh.navigation;

import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdp.ui.core.document.CoreDocument;
import com.axemble.vdp.ui.framework.composites.EmptyNavigation;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.composites.xml.XMLChildDocument;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.moovapps.capone.rh.helper.BaseCollectionViewProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class VueCongeOverlapped extends BaseCollectionViewProvider<IWorkflowInstance> {
   public static ArrayList<IWorkflowInstance> allDmande =new ArrayList<>();
   public IUser demandeur = null;
   ArrayList<IWorkflowInstance> detail=new ArrayList<>();
    public VueCongeOverlapped(INavigateContext context, CtlAbstractView view) {
        super(context, view);
    }


    @Override
    public Collection<IWorkflowInstance> getObjects() {
        try {
            if(Navigator.getNavigator().getParentNavigator().getCurrentScreen().getRootWidget() instanceof EmptyNavigation){
                return Collections.emptyList();
            }
            CoreDocument coreDocument = (CoreDocument) ((XMLChildDocument) Navigator.getNavigator().getParentNavigator().getCurrentScreen().getRootWidget()).getContext().getParameterMap().get("document");
            if (coreDocument != null) {
                IResource resource = this.getWorkflowModule().getResource(coreDocument);
                if(demandeur == null && resource.getValue("Collaborateur") != null){
                    demandeur = (IUser) resource.getValue("Collaborateur");
                }
                if(demandeur == null){
                    return Collections.emptyList();
                }
                if (resource instanceof ILinkedResource) {
                    IWorkflowInstance parentInstance = ((ILinkedResource)resource).getParentInstance();
                    if (parentInstance != null) {
                        //return getAllDemandeurDemandes((IUser) resource.getValue("User"));
                       detail = (ArrayList<IWorkflowInstance>) allDmande.stream().filter(d->d.getValue("Demandeur").equals(demandeur)).collect(Collectors.toList());
                    }
                }
            }else if(demandeur != null){
                detail = (ArrayList<IWorkflowInstance>) allDmande.stream().filter(d->d.getValue("Demandeur").equals(demandeur)).collect(Collectors.toList());
            }
        } catch (ModuleException var6) {
            System.err.println(var6);
        }
        return detail;
    }

//    private void setReferenceClickable(ViewModelItem iViewItem, IResource resource) {
//        try
//        {
//            ViewModelItem viewModelItem = iViewItem;
//            String link = "https://www.google.com";///moovapps/easysite/workplace/salarie/edit-document/" + resource.getId().toInt();
//            CtlHyperLink ctlHyperLink = new CtlHyperLink("link", new CtlText(((IUser)viewModelItem.getValue("Demandeur")).getFullName()));
//            ctlHyperLink.setUrl(link);
//            //String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort());
//            viewModelItem.setValue("Demandeur", ctlHyperLink);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private void setNombreJoursDeduire(ViewModelItem iViewItem, IResource resource) {
//        try
//        {
//            ViewModelItem viewModelItem = iViewItem;
//
//
//            CtlNumber ctlNumber = new CtlNumber();
//            ctlNumber.setSysname("NombreJoursDeduire");
//            float NombreJoursDeduire ;
//            if (viewModelItem.getValue("NombreJoursDeduire")==null){
//                NombreJoursDeduire =0 ;
//            }else {
//                 NombreJoursDeduire = (Float) viewModelItem.getValue("NombreJoursDeduire");
//
//            }
//            ctlNumber.setNumberValue(((Number) NombreJoursDeduire));
//            ctlNumber.setThrowEvents(true);
//            ctlNumber.setParam(resource);
//
//            /*CtlCheckBox ctlCheckBox = new CtlCheckBox((boolean)iLinkedResource.getValue("Actif"));
//            ctlCheckBox.setSysname("Actif");
//            ctlCheckBox.setThrowEvents(true);
//            ctlCheckBox.setParam(iLinkedResource);*/
//
//
//            @SuppressWarnings("serial")
//            ChangeListener listener = new ChangeListener() {
//                private static final long serialVersionUID = 1L;
//
//                public void onChange(ChangeEvent paramChangeEvent) {
//                    IContext context = getWorkflowModule().getLoggedOnUserContext();
//                    IResource iLinkedResource = null;
//                    Object object = paramChangeEvent.getSource();
//
//                    if (CtlTextBox.class.equals(object.getClass())) {
//                        CtlTextBox txtBox = (CtlTextBox) object;
//                        iLinkedResource = (ILinkedResource) txtBox.getParam();
//                        iLinkedResource.setValue("Actif", txtBox.getLabel());
//                        iLinkedResource.save(context);
//                    } else if (CtlNumber.class.equals(object.getClass())) {
//                        CtlNumber number = (CtlNumber) object;
//                        iLinkedResource = (IResource) number.getParam();
//                        iLinkedResource.setValue("NombreDeJoursDeduits", number.getFloatValue());
//                        iLinkedResource.save(context);
//                    } else if (CtlComboBox.class.equals(object.getClass())) {
//                        CtlComboBox box = (CtlComboBox) object;
//                        iLinkedResource = (ILinkedResource) box.getParam();
//                        iLinkedResource.setValue("Actif", box.getSelectedKey());
//                        iLinkedResource.save(context);
//                    } else if (CtlAutocompleteList.class.equals(object.getClass())) {
//                        CtlAutocompleteList box = (CtlAutocompleteList) object;
//                        iLinkedResource = (ILinkedResource) box.getParam();
//                        iLinkedResource.setValue("Actif",  box.getSelectedKey());
//                        iLinkedResource.save(context);
//                        getView().refreshItems();
//                    } else if (CtlRadioGroup.class.equals(object.getClass())) {
//                        CtlRadioGroup radio = (CtlRadioGroup) object;
//                        iLinkedResource = (ILinkedResource) radio.getParam();
//                        iLinkedResource.setValue("Actif", radio.getSelectedKey());
//                        iLinkedResource.save(context);
//                    } else if (CtlDate.class.equals(object.getClass())) {
//                        CtlDate date = (CtlDate) object;
//                        iLinkedResource = (ILinkedResource) date.getParam();
//                        iLinkedResource.setValue("Actif", date.getDate());
//                        iLinkedResource.save(context);
//                    } else if(CtlCheckBox.class.equals(object.getClass())){
//                        CtlCheckBox checkBox = (CtlCheckBox) object;
//                        iLinkedResource = (ILinkedResource) checkBox.getParam();
//                        iLinkedResource.setValue("Actif", checkBox.getValidationObject());
//                        //enableDisableUser(iLinkedResource, checkBox.getValidationObject(), getWorkflowModule());
//                        iLinkedResource.save(context);
//                    }
//                }
//            };
//            ctlNumber.addChangeListener(listener);
//
//            viewModelItem.setValue("NombreJoursDeduire", ctlNumber);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private Collection<IWorkflowInstance> getAllDemandeurDemandes(IUser Pdemandeur) {
//        Collection<IWorkflowInstance> collection = null;
//        try {
//            IContext sysContext = getWorkflowModule().getSysadminContext();
//            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
//            IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
//            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
//            IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
//            IViewController controller = getWorkflowModule().getViewController(sysContext);
//            controller.addEqualsConstraint("Demandeur", Pdemandeur);
//            controller.addNotInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("En cours", "Refusé", "Annulé")));
//            collection = controller.evaluate(w);
//            return collection;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }



    @Override
    public ViewModelItem fetchLine(IWorkflowInstance object) {
        ViewModelItem viewModelItem = new ViewModelItem(object);
        viewModelItem.setValue("DateDebut", object.getValue("DateDeDebut"));
        viewModelItem.setValue("DateFin", object.getValue("DateDeFin"));
        float NombreJours = (Float) object.getValue("NombreDeJoursDemandes");
        if (NombreJours ==0){
            NombreJours = (Float) object.getValue("NombreDeJoursExceptionnelle");
        }
        viewModelItem.setValue("NombreJours", NombreJours);
        viewModelItem.setValue("DebutConge", object.getText("DebutConge"));
        viewModelItem.setValue("FinConge", object.getText("FinConge"));
      //  viewModelItem.setValue("NombreJoursDeduire", object.getValue("NombreDeJoursDeduits"));
        viewModelItem.setValue("Statut", object.getValue("DocumentState"));
        viewModelItem.setValue("TypeConge", object.getText("TypeDeConge"));
        //setNombreJoursDeduire(viewModelItem, object);
        return viewModelItem;
    }





}
