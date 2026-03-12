package com.vdoc.helpers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.exceptions.SDKException;
import com.axemble.vdoc.sdk.interfaces.IConfiguration;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;

public class DirectoryService
{
	/** the default class logger */
	private static com.axemble.vdoc.sdk.utils.Logger LOG = com.axemble.vdoc.sdk.utils.Logger.getLogger(DirectoryService.class);

	public static IContext getSysContext()
	{
		IDirectoryModule directoryModule = Modules.getDirectoryModule();

		try
		{
			return directoryModule.getContextByLogin("sysadmin");
		}
		finally
		{
			Modules.releaseModule(directoryModule);
		}
	}

	public static IOrganization getOrganization(IDirectoryModule directoryModule , IWorkflowModule workflowModule) throws ModuleException
	{
		IConfiguration configuration = workflowModule.getConfiguration();
		String orgaName = "DefaultOrganization";
		IOrganization organization = directoryModule.getOrganization(getSysContext(), orgaName);

		if (organization == null)
		{
			String message = directoryModule.getStaticString("LG_VISIATIV_TIMESHEET_COLLABORATORS_GROUP_ORGA_NULL", orgaName);
			throw new SDKException(message);
		}
		return organization;
	}

	
}
