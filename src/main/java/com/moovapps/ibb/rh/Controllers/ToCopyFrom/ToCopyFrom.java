package com.moovapps.ibb.rh.Controllers.ToCopyFrom;

import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext.IRequest;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.utils.StreamUtils;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class ToCopyFrom extends BaseController {

	protected static final Logger log = Logger.getLogger(ToCopyFrom.class);

	/**
	 * @see com.axemble.vdp.ui.framework.foundation.controllers.CustomController#parseRequest(com.axemble.vdp.ui.framework.runtime.IExecutionContext.IRequest)
	 */
	public void parseRequest(IRequest iRequest) throws IOException
	{
		
	}
	
	/**
	 * @see com.axemble.vdoc.sdk.controllers.BaseController#doProcess(com.axemble.vdp.ui.framework.runtime.IExecutionContext)
	 */
	@Override
	public IExecutionContext doProcess(IExecutionContext ec) throws IOException
	{
		try
		{
			ec.getResponse().setContentType("application/json");
			ec.getResponse().addHeader("Access-Control-Allow-Origin", "*");
			HTTPUtils.setNoCacheResponseHeaders((HttpServletRequest) ec.getRequest().getNativeRequest(), (HttpServletResponse) ec.getResponse().getNativeResponse());
			buildResponse(ec, null);
		}
		catch (Exception e)
		{
			String message = e.getMessage();
			if (message == null)
			{
				message = "";
			}
			log.error("Error in SQLController doProcess method : " + e.getClass() + " - " + message);
		}
		return ec;
	}
	
	/**
	 * @param cli_id
	 * @return
	 */
	/**
	 * @param cli_id
	 * @return
	 */
	private JSONObject chercheAdresse(String participation)
	{
		
		JSONArray dataArray = new JSONArray();
		JSONObject global = new JSONObject();
		try
		{
			
			List<String> list = new ArrayList<>();
			Map<String, List<String>> Filtres = new HashMap<>();
			
			list.add("Nb congés");
			list.add("Durée congés");
			list.add("Catégorie");
			list.add("Mois");
			
			Filtres.put("lesFiltres", list);
			global.putAll(Filtres);
			
			Collection<IWorkflowInstance> conges =  getAll();
			
			IUser responsable = null;
			
				for (IWorkflowInstance conge : conges)
				{
					responsable = (IUser)conge.getValue("client_reservoir");
					JSONObject tmp = new JSONObject();
					IStorageResource programme = (IStorageResource) conge.getValue("Programme");
					ILinkedResource objectifs = (ILinkedResource)conge.getValue("ObjectifsProjet");
					tmp.put("Programme", programme.getValue("sys_Title"));
					tmp.put("Projet", conge.getValue("Designation"));
					tmp.put("Chef de projet", responsable.getFullName());
					tmp.put("Statut", conge.getValue("DocumentState"));
					tmp.put("Objectif", objectifs.getValue("Objectif"));
					tmp.put("Direction", conge.getValue(""));
					tmp.put("Budget", conge.getValue(""));
					
					dataArray.put(tmp);
						
				}
	
			global.put("data", dataArray);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return global;
		
	}
	
	@Override
	protected IExecutionContext buildResponse(IExecutionContext ec, Result result) throws IOException
	{
		try
		{
			String cli_id = ec.getRequest().getParameter("cli_id");
			JSONObject TableauJson = chercheAdresse(cli_id);
			OutputStream output;
			
			output = null;
			output = ec.getResponse().getOutputStream();
			output.toString();
			OutputStreamWriter osw = new OutputStreamWriter(output, "UTF-8");
			TableauJson.writeJSONString(osw);
			osw.flush();
			osw.close();
			
			StreamUtils.CloseSafe(output);
			
			StreamUtils.CloseSafe(output);
			return ec;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return super.buildResponse(ec, result);
	}
	
	private Collection<IWorkflowInstance> getAll()
	{
		Collection<IWorkflowInstance> collection = null;
		try
		{
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			collection = controller.evaluate(w);
			return collection;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		return null;
	}
	
	
}
