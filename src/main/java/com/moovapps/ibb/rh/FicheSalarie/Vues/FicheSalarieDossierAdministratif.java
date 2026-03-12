package com.moovapps.ibb.rh.FicheSalarie.Vues;

import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.framework.components.events.ChangeEvent;
import com.axemble.vdp.ui.framework.components.listeners.ChangeListener;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.widgets.*;
import org.apache.turbine.Turbine;

import java.util.Date;

public class FicheSalarieDossierAdministratif extends BaseViewExtension {
	
//	@Override
//	public void init() {
//		if(!isUserABPoUser()){			
//			getView().setHideButtons(true);
//		}
//		super.init();
//	}

	@Override
	public void onPrepareItem(ViewItem iViewItem) {
		setReferenceClickable(iViewItem);
		// SetClientClickable(iViewItem);
		setActifClickable(iViewItem);
	}
	
	private void setActifClickable(ViewItem iViewItem) {
		try 
		{
			ViewModelItem viewModelItem = iViewItem.getViewModelItem();
			ILinkedResource iLinkedResource = (ILinkedResource)iViewItem.getResource();
			
			CtlCheckBox ctlCheckBox = new CtlCheckBox((boolean)iLinkedResource.getValue("Actif"));
			ctlCheckBox.setSysname("Actif");
			ctlCheckBox.setThrowEvents(true);
			ctlCheckBox.setParam(iLinkedResource);
			
			
			@SuppressWarnings("serial")
			ChangeListener listener = new ChangeListener() {
				private static final long serialVersionUID = 1L;

				public void onChange(ChangeEvent paramChangeEvent) {
					IContext context = getWorkflowModule().getLoggedOnUserContext();
					ILinkedResource iLinkedResource = null;
					Object object = paramChangeEvent.getSource();

					if (CtlTextBox.class.equals(object.getClass())) {
						CtlTextBox txtBox = (CtlTextBox) object;
						iLinkedResource = (ILinkedResource) txtBox.getParam();
						iLinkedResource.setValue("Actif", txtBox.getLabel());
						iLinkedResource.save(context);
					} else if (CtlNumber.class.equals(object.getClass())) {
						CtlNumber number = (CtlNumber) object;
						iLinkedResource = (ILinkedResource) number.getParam();
						iLinkedResource.setValue("Actif", number.getFloatValue());
						iLinkedResource.save(context);
					} else if (CtlComboBox.class.equals(object.getClass())) {
						CtlComboBox box = (CtlComboBox) object;
						iLinkedResource = (ILinkedResource) box.getParam();
						iLinkedResource.setValue("Actif", box.getSelectedKey());
						iLinkedResource.save(context);
					} else if (CtlAutocompleteList.class.equals(object.getClass())) {
						CtlAutocompleteList box = (CtlAutocompleteList) object;
						iLinkedResource = (ILinkedResource) box.getParam();
						iLinkedResource.setValue("Actif",  box.getSelectedKey());
						iLinkedResource.save(context);
						getView().refreshItems();
					} else if (CtlRadioGroup.class.equals(object.getClass())) {
						CtlRadioGroup radio = (CtlRadioGroup) object;
						iLinkedResource = (ILinkedResource) radio.getParam();
						iLinkedResource.setValue("Actif", radio.getSelectedKey());
						iLinkedResource.save(context);
					} else if (CtlDate.class.equals(object.getClass())) {
						CtlDate date = (CtlDate) object;
						iLinkedResource = (ILinkedResource) date.getParam();
						iLinkedResource.setValue("Actif", date.getDate());
						iLinkedResource.save(context);
					} else if(CtlCheckBox.class.equals(object.getClass())){
						CtlCheckBox checkBox = (CtlCheckBox) object;
						iLinkedResource = (ILinkedResource) checkBox.getParam();
						iLinkedResource.setValue("Actif", checkBox.getValidationObject());
						enableDisableUser(iLinkedResource, checkBox.getValidationObject(), getWorkflowModule());
						iLinkedResource.save(context);
					}
				}
			};
			ctlCheckBox.addChangeListener(listener);
			
			viewModelItem.setValue("Actif", ctlCheckBox);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void enableDisableUser(IResource resource, Object actif, IWorkflowModule workflowModule) {
		if((boolean)actif == false){
			try {
				IUser salarie = (IUser) resource.getValue("Salarie");
				Date dateSortie = new Date();
				//resource.setValue("Exit", dateSortie);
				
				resource.setValue("DateDeSortie", dateSortie);

				salarie.setExit(dateSortie);
				salarie.disable();
				salarie.save(workflowModule.getSysadminContext());
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}else{
			IUser salarie = (IUser) resource.getValue("Salarie");
		//	resource.setValue("Exit", null);
			
			resource.setValue("DateDeSortie", null);

			salarie.setExit(null);
			salarie.enable();
		}
	}
	
	private void SetClientClickable(ViewItem iViewItem) {
		try 
		{
			ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            String link = "/moovapps/easysite/workplace/salarie/edit-document/" + iViewItem.getResource().getId().toInt() + "?flag=false";
            CtlHyperLink ctlHyperLink = new CtlHyperLink("link", new CtlText((String) viewModelItem.getValue("Societe.sys_Title")));
            ctlHyperLink.setUrl(link);
            String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort());
            viewModelItem.setValue("Societe.sys_Title", ctlHyperLink);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void setReferenceClickable(ViewItem iViewItem) {
		try 
		{
			ViewModelItem viewModelItem = iViewItem.getViewModelItem();
            String link = "/moovapps/easysite/workplace/salarie/edit-document/" + iViewItem.getResource().getId().toInt() + "?flag=false";
            CtlHyperLink ctlHyperLink = new CtlHyperLink("link", new CtlText((String) viewModelItem.getValue("Matricule")));
            ctlHyperLink.setUrl(link);
            String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort());
            viewModelItem.setValue("Matricule", ctlHyperLink);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	private boolean isUserABPoUser() {
//		try {			
//			return getWorkflowModule().getLoggedOnUser().isMemberOf(getWorkflowModule().getGroupByName("BPO"), true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return false;
//	}

}
