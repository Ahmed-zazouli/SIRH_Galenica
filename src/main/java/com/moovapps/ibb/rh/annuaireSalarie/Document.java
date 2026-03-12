package com.moovapps.ibb.rh.annuaireSalarie;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.moovapps.ibb.rh.Cummon.DateHelper;

import java.util.Date;

public class Document extends BaseDocumentExtension {

	@Override
	public boolean onAfterLoad() {
		if(getWorkflowInstance().getValue("DateDEmbauche") != null){				
			getWorkflowInstance().setValue("AncienneteInDetail", new DateHelper().getDurationFromADateToNow((Date)getWorkflowInstance().getValue("DateDEmbauche")));
		}else {
			getWorkflowInstance().setValue("AncienneteInDetail", null);
		}
		return super.onAfterLoad();
	}
	
}
