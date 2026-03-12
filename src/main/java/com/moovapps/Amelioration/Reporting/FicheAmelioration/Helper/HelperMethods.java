package com.moovapps.Amelioration.Reporting.FicheAmelioration.Helper;

import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HelperMethods {
	
	public static Collection<IWorkflowInstance> deleteDuplicant(Collection<IWorkflowInstance> c){
		List data = new ArrayList<>(c);
		for(int i=0;i<data.size()-1;i++){
			IWorkflowInstance precedent = (IWorkflowInstance) data.get(i);
			IWorkflowInstance next = (IWorkflowInstance) data.get(i+1);
			if(precedent.getId().toString().equals(next.getId().toString())){
				data.remove(i);
				i--;
			}
		}
		return (Collection<IWorkflowInstance>)data;
	}

}
