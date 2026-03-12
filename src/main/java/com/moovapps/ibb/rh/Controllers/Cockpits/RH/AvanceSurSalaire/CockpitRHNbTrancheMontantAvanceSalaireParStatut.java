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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CockpitRHNbTrancheMontantAvanceSalaireParStatut extends BaseController {
	
	protected static final Logger log = Logger.getLogger(CockpitRHNbTrancheMontantAvanceSalaireParStatut.class);

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
			listFiltres.add("Manager");
			listFiltres.add("Bénéficiaire");
			listFiltres.add("Statut");
			
			FiltreObject.put("lesFiltres", listFiltres);
			ResultObject.putAll(FiltreObject);
						
			Collection<IWorkflowInstance> demandesAvances =  getData();
			
				for (IWorkflowInstance demandesAvance : demandesAvances)
				{
					String societeName = null;
					IUser connectedUser = getWorkflowModule().getLoggedOnUser();
					if(demandesAvance.getValue("Societe") != null){
						//societeName = (String)demandesAvance.getValue("Societe");
						societeName = (String)((IStorageResource) connectedUser.getExtendedAttributes().getValue("Societe")).getValue("sys_Title");

					}
					String departement = null ;
					if (connectedUser.getExtendedAttributes().getValue("Departement") != null){
						departement = (String)((IStorageResource) connectedUser.getExtendedAttributes().getValue("Departement")).getValue("sys_Title");

					}
				
					JSONObject tmp = new JSONObject();
					tmp.put("Société", departement);
					tmp.put("Manager", demandesAvance.getValue("SuperieurHierarchique")!= null ? ((IUser)demandesAvance.getValue("SuperieurHierarchique")).getFullName(): "N/D");
					tmp.put("Bénéficiaire", ((IUser)demandesAvance.getValue("Beneficiaire")).getFullName());
					tmp.put("Statut", demandesAvance.getText("DocumentState"));
					tmp.put("Montant", demandesAvance.getValue("Montant"));
				//	tmp.put("Tranche salaire", demandesAvance.getValue("TrancheSalaire"));
					
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
			IProject project = getProjectModule().getProject(sysContext, "GestionDesAvances", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "GestionDesAvances", project);
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDesAvances");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			//controller.addNotEqualsConstraint("DocumentState", "Refusée");
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
