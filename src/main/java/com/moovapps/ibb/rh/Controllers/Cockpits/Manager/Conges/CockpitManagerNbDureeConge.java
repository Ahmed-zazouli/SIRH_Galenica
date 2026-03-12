package com.moovapps.ibb.rh.Controllers.Cockpits.Manager.Conges;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CockpitManagerNbDureeConge extends BaseController {
	
	protected static final Logger log = Logger.getLogger(CockpitManagerNbDureeConge.class);
	
	private enum TYPECONGES{
		CN("C.Payés"),
		CE("C.Spéciaux"),
		CM("C.Maladie"),
		SS("C.Sans solde");
		
		public final String label;

	    private TYPECONGES(String label) {
	        this.label = label;
	    }
	}

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
			
			listFiltres.add("Société");
			listFiltres.add("Salarié");
			//listFiltres.add("Direction");
			listFiltres.add("Catégorie");
			listFiltres.add("Statut");
			
			FiltreObject.put("lesFiltres", listFiltres);
			ResultObject.putAll(FiltreObject);
			
			HashMap<String, IStorageResource> societies = new HashMap<String,IStorageResource>();
			
			Collection<IWorkflowInstance> demandesConges =  getData();
			
				for (IWorkflowInstance conge : demandesConges)
				{
					String societeName = "";
					IStorageResource societe = (IStorageResource) conge.getValue("Societe");
					if(conge.getValue("Societe") != null){
						societeName =societe!=null?(String) societe.getValue("sys_Title"):"";
						if(!societies.containsKey(societeName)){
							societies.put(societeName, societe);
						}
					}


					IUser demandeur = (IUser)conge.getValue("Demandeur");
					JSONObject tmp = new JSONObject();
					tmp.put("Société", societeName);
					tmp.put("Salarié", demandeur.getFullName());
					tmp.put("Catégorie", TYPECONGES.valueOf((String)conge.getValue("TypeDeConge")).label);
					tmp.put("Statut", conge.getText("DocumentState"));
					
					if(conge.getValue("TypeDeConge") != null){
						if(conge.getValue("TypeDeConge").equals("CE")){
							tmp.put("Durée congés", (Float)conge.getValue("NombreDeJoursExceptionnelle"));
						}else{
							tmp.put("Durée congés", (Float)conge.getValue("NombreDeJoursDemandes"));
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
		Collection<IWorkflowInstance> collection = null;
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
			//controller.addEqualsConstraint("SupHierarchique", connectedUser);
			controller.addEqualsConstraint("ValidateurConge",connectedUser);
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			controller.addNotEqualsConstraint("DocumentState", "Refusé");
			controller.addNotEqualsConstraint("DocumentState", "Annulé");
			controller.addNotEqualsConstraint("DocumentState", "Annulée");
			controller.addNotEqualsConstraint("DocumentState", "Refusée");
			controller.addNotEqualsConstraint("TypeDeConge", "Absence");
			collection = controller.evaluate(w);
			return collection;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		return null;
	}

	public IStorageResource getSociete(String societeName) {
		IStorageResource societe = null;
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
			IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",getDirectoryModule().getOrganization(context, "DefaultOrganization"));
			ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
			controller.addEqualsConstraint("sys_Title", societeName);
			Collection<IStorageResource> societes = controller.evaluate(definition);
			if (!societes.isEmpty()){				
				societe = societes.iterator().next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return societe;
	}
}
