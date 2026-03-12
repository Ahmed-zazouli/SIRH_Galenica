package com.moovapps.Amelioration.PlanAction;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IOptionList.IOption;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class CreationAction extends BaseDocumentExtension{
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("MettreAJour")){
		/*	if(getWorkflowInstance().getValue("CauseRacine3")==null){
				getWorkflowInstance().setValue("CauseRacine3", getWorkflowInstance().getValue("CauseRacine4"));
				getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
			}
			*/
			
		}
			
		
		return super.onBeforeSubmit(action);
	}
	
	@Override
	public boolean onBeforeLoad() {
		String causeRacine = null;
		
		// TODO Auto-generated method stub
		if(getWorkflowInstance().getParentInstance()!=null){
			getWorkflowInstance().setValue("ProcessussConcernes", getWorkflowInstance().getParentInstance().getValue("ProcessussConcernes"));
			getWorkflowInstance().setValue("Societe", getWorkflowInstance().getParentInstance().getValue("Societe"));
	    	ArrayList<IOption> options = new ArrayList<IOption>();
			ArrayList<ILinkedResource> causes = (ArrayList<ILinkedResource>) getWorkflowInstance().getParentInstance().getValue("AnalyseDesCauses");
			if(causes!=null)
			{
				for (ILinkedResource iLinkedResource : causes) {
						String causePriorise = (String) iLinkedResource.getValue("CausesPriorisees");
						if(causePriorise!=null && !causePriorise.trim().equals("")){
							options.add(getWorkflowModule().createListOption(causePriorise, causePriorise));
						}
				}
                getWorkflowInstance().setList("CauseRacine4", (Collection)options);
                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                

			}
			
			
		}
		
		
		return super.onBeforeLoad();
	}
	
	
	private class CustomComparator implements Comparator<ILinkedResource> 
	{
	    @Override
	    public int compare(ILinkedResource o1, ILinkedResource o2) {
	        return o1.getCreatedDate().compareTo(o2.getCreatedDate());
	    }
	}

}

