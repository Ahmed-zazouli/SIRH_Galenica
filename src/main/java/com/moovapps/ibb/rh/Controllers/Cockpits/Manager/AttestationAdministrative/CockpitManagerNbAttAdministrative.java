package com.moovapps.ibb.rh.Controllers.Cockpits.Manager.AttestationAdministrative;

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
import java.util.Collections;
import java.util.List;

public class CockpitManagerNbAttAdministrative extends BaseController {
	
	protected static final Logger log = Logger.getLogger(CockpitManagerNbAttAdministrative.class);

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
			listFiltres.add("Direction");
			listFiltres.add("Demandeur");
			listFiltres.add("Motif");
			listFiltres.add("Statut");
			
			FiltreObject.put("lesFiltres", listFiltres);
			ResultObject.putAll(FiltreObject);
			
//			HashMap<String, IStorageResource> societies = new HashMap<String,IStorageResource>();
			
			Collection<IWorkflowInstance> demandesAttestations =  getData();
			
				for (IWorkflowInstance demandeAttestation : demandesAttestations)
				{
					String societeName = null;
					if(demandeAttestation.getValue("Societe") != null){
						societeName = (String)demandeAttestation.getValue("Societe");
					}
//					if(!societies.containsKey(societeName)){
//						societies.put(societeName, getSociete(societeName));
//					}
					IUser Demandeur = (IUser)demandeAttestation.getValue("Demandeur2");
					String direction = "N/D";
					if(Demandeur.getExtendedAttributes().getValue("Direction") != null){
						direction = (String)((IStorageResource)Demandeur.getExtendedAttributes().getValue("Direction")).getValue("sys_Title");
					}
					JSONObject tmp = new JSONObject();
					tmp.put("Société", societeName);
					tmp.put("Direction", direction);
					tmp.put("Demandeur", Demandeur.getFullName());
					tmp.put("Motif",  demandeAttestation.getValue("MotifDemandeAttestation") != null ? demandeAttestation.getValue("MotifDemandeAttestation") : "N/D");
					tmp.put("Statut", demandeAttestation.getText("DocumentState"));
					
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
			IProject project = getProjectModule().getProject(sysContext, "AttestationDeSalaire", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			//IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "DOCUMENTSADMINISTRATIFSINTERNES_1.0");
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "DOCUMENTSADMINISTRATIFSINTERNES");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("SuperieurHierarchique", connectedUser);
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			collection = controller.evaluate(w);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return collection;
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
