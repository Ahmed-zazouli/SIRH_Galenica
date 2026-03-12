package com.moovapps.Amelioration.PlanAction;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;

public class ValidationAction extends BaseDocumentExtension{
	
	@Override
	public boolean onBeforeLoad() {
		try{

		}catch(Exception e){
			e.printStackTrace();
		}
		return super.onBeforeLoad();
	}

}
