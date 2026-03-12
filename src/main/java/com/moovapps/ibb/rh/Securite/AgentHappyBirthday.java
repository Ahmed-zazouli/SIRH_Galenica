package com.moovapps.ibb.rh.Securite;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.IUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class AgentHappyBirthday extends BaseAgent {
	
	SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM");

	@Override
	protected void execute() {
		Collection<IUser> allUsers = (Collection<IUser>) getDirectoryModule().getUsers(getWorkflowModule().getSysadminContext());
		for (IUser iUser : allUsers) {
			Calendar c = Calendar.getInstance();
			if((iUser.getExtendedAttributes().getValue("MerciBirthday") != null && (boolean)iUser.getExtendedAttributes().getValue("MerciBirthday"))){
				if(iUser.getBirthday() != null){
					c.setTime(iUser.getBirthday());
					c.add(Calendar.DATE, 1);
					if(simpleFormat.format(c.getTime()).equals(simpleFormat.format(new Date()))){
						iUser.getExtendedAttributes().setValue("MerciBirthday", false);
						iUser.save(getWorkflowModule().getSysadminContext());
					}
				}
			}
		}
	}
}
