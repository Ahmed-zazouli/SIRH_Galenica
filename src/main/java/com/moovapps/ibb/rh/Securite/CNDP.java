package com.moovapps.ibb.rh.Securite;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.site.extensions.BasePageExtension;

public class CNDP extends BasePageExtension {

	@Override
	public boolean onBeforeLoad() {
		IUser connectedUser = getSiteModule().getLoggedOnUser();
		if(!connectedUser.isSysadmin() && ( connectedUser.getExtendedAttributes().getValue("AllowAccess") == null || !(boolean)connectedUser.getExtendedAttributes().getValue("AllowAccess"))){
			try {
				String racine = this.getExecutionContext().getRequest().getBaseUrl();
				String uri = racine + "/easysite/workplace/salarie/edit-document/42";// BPO 10817
				Modules.getPortalModule().killCurrentSession();
				getExecutionContext().getResponse().sendRedirect(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.onBeforeLoad();
	}
	
}
