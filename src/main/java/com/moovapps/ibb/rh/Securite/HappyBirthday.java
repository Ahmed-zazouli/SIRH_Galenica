package com.moovapps.ibb.rh.Securite;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.site.extensions.BasePageExtension;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HappyBirthday extends BasePageExtension {

	SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM");

	
	@Override
	public boolean onBeforeLoad() {
		IUser connectedUser = getSiteModule().getLoggedOnUser();
		
		if(connectedUser.getBirthday() != null && simpleFormat.format(connectedUser.getBirthday()).equals(simpleFormat.format(new Date())) && (connectedUser.getExtendedAttributes().getValue("MerciBirthday") == null || !(boolean)connectedUser.getExtendedAttributes().getValue("MerciBirthday"))){
			try {
				String racine = this.getExecutionContext().getRequest().getBaseUrl();
				String uri = racine + "/easysite/workplace/reservoir-de-donnees-ibb-rh/edit-document/21120";// BPO 10817
				Modules.getPortalModule().killCurrentSession();
				getExecutionContext().getResponse().sendRedirect(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.onBeforeLoad();
	}
	
}