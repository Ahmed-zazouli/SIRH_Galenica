package com.moovapps.ibb.rh.Controllers.ModulesDashBoard.Conges;

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
import java.text.SimpleDateFormat;
import java.util.*;

public class ModuleCongesDashParStatut extends BaseController {
	
	private enum TYPECONGES{
		CN("C.Payés"),
		CE("C.Spéciaux"),
		CM("C.Maladie"),
		SS("C.Sans solde"),
		Absence("Absence");
		
		public final String label;

	    private TYPECONGES(String label) {
	        this.label = label;
	    }
	}
	
	protected static final Logger log = Logger.getLogger(ModuleCongesDashParStatut.class);

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
			
			List<String> listFiltres = new ArrayList<>();
			JSONObject FiltreObject = new JSONObject();
			
			listFiltres.add("Manager");
			listFiltres.add("Demandeur");
			listFiltres.add("Catégorie");
			listFiltres.add("Date congé");
			listFiltres.add("Statut");
			
			FiltreObject.put("lesFiltres", listFiltres);
			ResultObject.putAll(FiltreObject);
			
			Collection<IWorkflowInstance> conges =  getData();
			SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM-yy");
				for (IWorkflowInstance conge : conges)
				{

					JSONObject tmp = new JSONObject();
					tmp.put("Manager", conge.getValue("ValidateurConge") != null ? ((IUser)conge.getValue("ValidateurConge")).getFullName() : "N/D");
					tmp.put("Demandeur", ((IUser)conge.getValue("Demandeur")).getFullName());
					tmp.put("Catégorie", TYPECONGES.valueOf((String)conge.getValue("TypeDeConge")).label);
					tmp.put("Date congé", monthYearFormat.format((Date)conge.getValue("DateDeDebut")));
					tmp.put("Statut", conge.getText("DocumentState"));
					if(conge.getValue("TypeDeConge") != null){
						if(conge.getValue("TypeDeConge").equals("CE")){
							tmp.put("Durée", (Float)conge.getValue("NombreDeJoursExceptionnelle"));
						}else{
							tmp.put("Durée", (Float)conge.getValue("NombreDeJoursDemandes"));
						}
					}
					ResultDataArray.put(tmp);	
				}
	
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
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try
		{
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
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

}
