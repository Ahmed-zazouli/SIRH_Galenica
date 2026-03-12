package com.moovapps.ibb.rh.Controllers.ModulesDashBoard.Sortie;

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
import java.util.Collections;

public class ModuleSortieKpi extends BaseController {

	protected static final Logger log = Logger.getLogger(ModuleSortieKpi.class);

	@Override
	public void parseRequest(IRequest arg0) throws IOException {
	}

	@Override
	public IExecutionContext doProcess(IExecutionContext ec) throws IOException {
		try {
			ec.getResponse().setContentType("application/json");
			ec.getResponse().addHeader("Access-Control-Allow-Origin", "*");
			HTTPUtils.setNoCacheResponseHeaders((HttpServletRequest) ec.getRequest().getNativeRequest(), (HttpServletResponse) ec.getResponse().getNativeResponse());
			buildResponse(ec, null);
		} catch (Exception e) {
			String message = e.getMessage();
			if (message == null) {
				message = "";
			}
			log.error("Error in SQLController doProcess method : " + e.getClass() + " - " + message);
		}
		return ec;
	}

	@Override
	protected IExecutionContext buildResponse(IExecutionContext ec, Result result) throws IOException {
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.buildResponse(ec, result);
	}

	private JSONObject BuildData() {

		IUser connectedUser = getWorkflowModule().getLoggedOnUser();
		JSONArray ResultDataArray = new JSONArray();
		JSONObject ResultObject = new JSONObject();
		try {
			
			Collection<IWorkflowInstance> allMySorties = getAllMySorties();
			int nbSortiesEnCours = 0;
			long dureeTotale = 0;
			
			for (IWorkflowInstance sortie : allMySorties) {
				String statut = (String)sortie.getValue("DocumentState");
				if(!statut.equals("Clôturée") && !statut.equals("Refusé")){	
					nbSortiesEnCours += 1;
				}
				dureeTotale += ((Number)sortie.getValue("dureeSortieMinutesReelle")).longValue();
			}
			
			JSONObject tmp = new JSONObject();
			tmp.put("kpi1_value", nbSortiesEnCours);
			tmp.put("kpi2_value", String.format("%.2f", dureeTotale/(float)60)+"h");
			tmp.put("kpi3_value", getAllSortiesACloturer().size());
			ResultDataArray.put(tmp);

			ResultObject.put("data", ResultDataArray);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResultObject;

	}
	
	private Collection<IWorkflowInstance> getAllMySorties()
	{
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try
		{
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			//IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "GestionDeSortie_1.0");
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeSortie");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("Demandeur", connectedUser);
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			collection = controller.evaluate(w);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		return collection;
	}
	
	private Collection<IWorkflowInstance> getAllSortiesACloturer()
	{
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try
		{
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "GestionDeSortie_1.0");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("DestinataireSortie", connectedUser);
			//controller.addNotEqualsConstraint("DocumentState", "En cours");
			controller.addEqualsConstraint("DocumentState", "Clôturée");
			//controller.addNotEqualsConstraint("DocumentState", "Refusé");
			collection = controller.evaluate(w);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		return collection;
	}
	
}
