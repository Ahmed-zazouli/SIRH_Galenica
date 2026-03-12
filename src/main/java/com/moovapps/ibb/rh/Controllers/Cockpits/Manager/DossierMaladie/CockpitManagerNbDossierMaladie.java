package com.moovapps.ibb.rh.Controllers.Cockpits.Manager.DossierMaladie;

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

public class CockpitManagerNbDossierMaladie extends BaseController {
	
	protected static final Logger log = Logger.getLogger(CockpitManagerNbDossierMaladie.class);

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
			
			listFiltres.add("Entité");
			listFiltres.add("Bénéficiare");
			listFiltres.add("Direction");
			listFiltres.add("Lien parenté");
			listFiltres.add("Nature médecin");
			listFiltres.add("Statut");
			
			FiltreObject.put("lesFiltres", listFiltres);
			ResultObject.putAll(FiltreObject);
			
			HashMap<String, IStorageResource> societies = new HashMap<String,IStorageResource>();
			
			Collection<IWorkflowInstance> dossiersMaladies =  getData();
			
				for (IWorkflowInstance dossierMaladie : dossiersMaladies)
				{
					String societeName = null;
					String departement = null ;
					IUser Demandeur = (IUser)dossierMaladie.getValue("Demandeur");

					if(dossierMaladie.getValue("Societe") != null){
						//societeName = (String)dossierMaladie.getValue("Societe");
						societeName = (String)((IStorageResource) Demandeur.getExtendedAttributes().getValue("Societe")).getValue("sys_Title");
						departement = (String)((IStorageResource) Demandeur.getExtendedAttributes().getValue("Departement")).getValue("sys_Title");
					}
					if(!societies.containsKey(societeName)){
						//societies.put(societeName, getSociete(societeName));
						IStorageResource societe = (IStorageResource) Demandeur.getExtendedAttributes().getValue("Societe");
						societies.put(societeName, societe);
					}
					Demandeur = (IUser)dossierMaladie.getValue("Demandeur");
					String direction = "N/D";
					if(Demandeur.getExtendedAttributes().getValue("Direction") != null){
						direction = (String)((IStorageResource)Demandeur.getExtendedAttributes().getValue("Direction")).getValue("sys_Title");
					}
					JSONObject tmp = new JSONObject();
					tmp.put("Entité", departement);
					tmp.put("Bénéficiare", Demandeur.getFullName());
					tmp.put("Direction", direction);
					tmp.put("Lien parenté",  dossierMaladie.getValue("LienDeParente"));
					tmp.put("Nature médecin", dossierMaladie.getValue("NatureMedecin"));
					tmp.put("Statut", dossierMaladie.getText("DocumentState"));
					
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
			IProject project = getProjectModule().getProject(sysContext, "DossierMaladie", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "DossiersMaladie", project);
			//IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "DossiersMaladie_1.0");
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "DossiersMaladie");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("SuperieurHierarchique", connectedUser);
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
