package com.moovapps.ibb.rh.DossierMaladie.action;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.moovapps.ibb.rh.Controllers.Helpers.DateHelper;

import java.util.Date;

public class DossierRembourse extends BaseDocumentExtension {
	
	@Override
	public void onPropertyChanged(IProperty property) {
		if(property.getName().equals("MontantRembourse")){
			calculatePorcentageRembourse();
		}
		super.onPropertyChanged(property);
	}
	
	public void calculatePorcentageRembourse(){
		float montantDesSoins = 0;
		float montantRembourse = 0;
		if(getWorkflowInstance().getValue("MontantRembourse") != null){
			montantDesSoins = (Float)getWorkflowInstance().getValue("MontantDesSoins");
			montantRembourse = (Float)getWorkflowInstance().getValue("MontantRembourse");
			getWorkflowInstance().setValue("PourcentageRembourse", (int)(montantRembourse * 100 / montantDesSoins));
		}else{
			getWorkflowInstance().setValue("PourcentageRembourse", null);
		}
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("RemboursementDuDossierMaladie")){
			boolean isDateRemboursementAvantDateMiseEnLigne = false;
			try {
				isDateRemboursementAvantDateMiseEnLigne = new DateHelper().isDateBeforeDate((Date)getWorkflowInstance().getValue("DateDeRemboursement"), (Date)getWorkflowInstance().getValue("sys_CreationDate"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(isDateRemboursementAvantDateMiseEnLigne){
//				getResourceController().alert("La date de remboursement doit être supérieur a la date mise en ligne");
//				return false;
			}
		}
		return super.onBeforeSubmit(action);
	}

}
