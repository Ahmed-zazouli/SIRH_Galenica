package com.moovapps.ibb.rh.Controllers.Cockpits.RH.AvanceSurSalaire;

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
import java.util.Collection;
import java.util.HashMap;

public class CockpitRHKpiAvanceSalaire extends BaseController {
	
	protected static final Logger log = Logger.getLogger(CockpitRHKpiAvanceSalaire.class);

	@Override
	public void parseRequest(IRequest arg0) throws IOException {}
	
	@Override
	public IExecutionContext doProcess(IExecutionContext ec) throws IOException {
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
	
	@Override
	protected IExecutionContext buildResponse(IExecutionContext ec, Result result) throws IOException {
		try
		{
			// String cli_id = ec.getRequest().getParameter("cli_id");
			JSONObject TableauJson = BuildData();
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

	private JSONObject BuildData() {
		
		JSONArray ResultDataArray = new JSONArray();
		JSONObject ResultObject = new JSONObject();
		try
		{
			HashMap<IUser, Float> userSalary = new HashMap<IUser, Float>();
			float totalAvanceDemande = 0; 
			float totalUsersSalary = 0;
			Collection<IWorkflowInstance> demandesAvances =  getData();
			
			for (IWorkflowInstance demandesAvance : demandesAvances)
			{
				IUser beneficiaire = (IUser)demandesAvance.getValue("Beneficiaire");
				if(!userSalary.containsKey(beneficiaire)){
					if(beneficiaire.getExtendedAttributes().getValue("Salaire")!=null){
						userSalary.put(beneficiaire, (Float)beneficiaire.getExtendedAttributes().getValue("Salaire"));
						totalUsersSalary += (Float)beneficiaire.getExtendedAttributes().getValue("Salaire");
					}

				}
				if(demandesAvance.getValue("Montant")!=null){
					totalAvanceDemande += (Float)demandesAvance.getValue("Montant");

				}
			}
			
			JSONObject tmp = new JSONObject();
			tmp.put("kpi1_value", String.format("%.0f", ((totalAvanceDemande / totalUsersSalary)*100)) + "%");
			ResultDataArray.put(tmp);
			ResultObject.put("data", ResultDataArray);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return ResultObject;
		
	}
	
	private Collection<IWorkflowInstance> getData()
	{
		Collection<IWorkflowInstance> collection = null;
		try
		{
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "GestionDesAvances", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "GestionDesAvances", project);
			//IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "GestionDesAvances_1.0");
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDesAvances");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addNotEqualsConstraint("DocumentState", "En cours");
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
