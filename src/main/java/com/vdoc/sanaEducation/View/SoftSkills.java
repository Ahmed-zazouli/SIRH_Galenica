package com.vdoc.sanaEducation.View;

import java.util.Collection;

import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.framework.components.events.ChangeEvent;
import com.axemble.vdp.ui.framework.components.listeners.ChangeListener;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.widgets.CtlAutocompleteList;
import com.axemble.vdp.ui.framework.widgets.list.Option;
import com.vdoc.sanaEducation.EvaluationCollaborateur.Entretien;

public class SoftSkills extends BaseViewExtension {
	
	private static final Logger log = Logger.getLogger(Entretien.class) ;
	protected IContext context = null;
	@Override
	public void onPrepareItem(ViewItem iViewItem)
	{
		// TODO Auto-generated method stub
		
		try
		{
			context = getWorkflowModule().getLoggedOnUserContext();
			
			ViewModelItem viewModelItem = iViewItem.getViewModelItem();
			ILinkedResource iLinkedResource = (ILinkedResource) iViewItem.getResource();
			
			// Resultat
			if(getWorkflowInstance().getValue("DocumentState").equals("En cours"))
			{
				CtlAutocompleteList Resultat = new CtlAutocompleteList();
				Resultat.setThrowEvents(true);
				Resultat.setOptions((Collection<Option>) iLinkedResource.getList("Resultat"));
				Resultat.setParam(iLinkedResource);
				Resultat.setSelectedKey(iLinkedResource.getValue("Resultat"));
				Resultat.setSysname("Resultat");
				Resultat.addChangeListener(listener);
				
				viewModelItem.setValue("Resultat", Resultat);
				
			}
		
		}
		catch (WorkflowModuleException e)
		{
			log.error(e);
		}
		
	}

	protected ChangeListener listener = new ChangeListener()
	{
		public void onChange(ChangeEvent paramChangeEvent)
		{
			ILinkedResource iLinkedResource = null;
			Object object = paramChangeEvent.getSource();
			
			
			 
			 if (CtlAutocompleteList.class.equals(object.getClass()))
			{
				CtlAutocompleteList box = (CtlAutocompleteList) object;
				iLinkedResource = (ILinkedResource) box.getParam();
				iLinkedResource.setValue(box.getSysname(), box.getSelectedKey());
				iLinkedResource.setValue("Resultat", box.getSelectedKey());
				iLinkedResource.setValue("Total", Float.valueOf((String) box.getSelectedKey()));
				iLinkedResource.save(context);
				CALCULTOTAL();
				getView().refresh();
			}
	
		}
	};
	void CALCULTOTAL () {
		
		Collection<ILinkedResource> linkedResource = (Collection<ILinkedResource>) getWorkflowInstance().getLinkedResources("SoftSkills");
		String Resultat = null ;
		Float Somme = 0F ;
		Float number = 0F;
		
		for (ILinkedResource iLinkedResource : linkedResource) {
			number =	(Float) iLinkedResource.getValue("Total");
			if(number != null)
			{
				//number = Float.valueOf(Resultat);
				Somme += number ;
			}
			
			
			
		}	
		getWorkflowInstance().setValue("RaitingSoftSkills", Somme);
	
}
}
