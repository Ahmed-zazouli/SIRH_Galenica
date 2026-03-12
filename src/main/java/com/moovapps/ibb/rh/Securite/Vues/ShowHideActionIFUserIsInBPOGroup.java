package com.moovapps.ibb.rh.Securite.Vues;

import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;

public class ShowHideActionIFUserIsInBPOGroup extends BaseViewExtension {
	
	@Override
	public void init() {
		if(!isUserABPoUser()){			
			getView().setHideButtons(true);
		}
		super.init();
	}
	
	private boolean isUserABPoUser() {
		try {			
			return getWorkflowModule().getLoggedOnUser().isMemberOf(getWorkflowModule().getGroupByName("BPO"), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void onPrepareItem(ViewItem arg0) {
		
	}
}
