package com.moovapps.ibb.rh.Controllers.ModulesDashBoard.AvanceSurSalaire;

import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext.IRequest;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.utils.StreamUtils;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class ModuleAvanceMontantAvanceSalaireEvolution extends BaseController {
	
	String[] mois = { "Jan", "Fév", "Mars", "Avr", "Mai", "Juin", "Juill", "Aou", "Sep", "Oct", "Nov", "Déc" };
	
	private class GroupModel{
		String societe = null;
		IUser manager = null;
        String statut = null;
		String mois = null;
		
		public GroupModel(String societe, IUser manager, String statut, String mois){
			this.societe = societe;
			this.manager = manager;
			this.statut = statut;
			this.mois = mois;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof GroupModel))
	            return false;
	        if (obj == this)
	            return true;

	        GroupModel otherGroup = (GroupModel) obj;
			
			return (societe.equals(otherGroup.societe) &&
					manager == otherGroup.manager &&
					statut.equals(otherGroup.statut) &&
					mois.equals(otherGroup.mois));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(societe, manager, statut, mois);
		}
	}
	
	protected static final Logger log = Logger.getLogger(ModuleAvanceMontantAvanceSalaireEvolution.class);

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
		JSONObject ResultObject = new JSONObject();
		try
		{	
			List<String> listFiltres = new ArrayList<>();
			JSONObject FiltreObject = new JSONObject();
			
			listFiltres.add("Société");
			listFiltres.add("Manager");
			listFiltres.add("Statut");
			listFiltres.add("Mois");
			
			FiltreObject.put("lesFiltres", listFiltres);
			ResultObject.putAll(FiltreObject);
			
			HashMap<String, IStorageResource> societies = new HashMap<String,IStorageResource>();
			
			Collection<IWorkflowInstance> demandesAvance =  getData();
			HashMap<GroupModel, JSONObject> evolutionMap = new LinkedHashMap<GroupModel, JSONObject>();
			
			for (IWorkflowInstance avance : demandesAvance)
			{
				String societeName = null;
				if(avance.getValue("Societe") != null){
					//societeName = (String)avance.getValue("Societe");
					IUser connectedUser = getWorkflowModule().getLoggedOnUser();
					societeName = (String)((IStorageResource) connectedUser.getExtendedAttributes().getValue("Societe")).getValue("sys_Title");
				}
				if(!societies.containsKey(societeName)){
					IUser connectedUser = getWorkflowModule().getLoggedOnUser();
					IStorageResource societe = (IStorageResource) connectedUser.getExtendedAttributes().getValue("Societe");
					societies.put(societeName, societe);
				}
				
				Date moisDate = (Date)avance.getValue("sys_CreationDate");
				Calendar x = Calendar.getInstance();
				x.setTime(moisDate);
				int moisIndex = x.get(Calendar.MONTH);
				GroupModel tmpGroupModel = new GroupModel(societeName, 
						(IUser)avance.getValue("SuperieurHierarchique"),
						(String)avance.getText("DocumentState"), 
						mois[moisIndex]);
				if(!evolutionMap.containsKey(tmpGroupModel)){					
					for (int i = 0; i < 12; i++) {
						JSONObject tmp = new JSONObject();
						tmp.put("Société", societeName);
						tmp.put("Manager", avance.getValue("SuperieurHierarchique")!= null ? ((IUser)avance.getValue("SuperieurHierarchique")).getFullName(): "N/D");
						tmp.put("Statut", avance.getText("DocumentState"));
						tmp.put("Mois", mois[i]);
						tmp.put("Nb", 0);
						tmp.put("Montant", 0);
						GroupModel tmpG = new GroupModel(societeName, 
								(IUser)avance.getValue("SuperieurHierarchique"),
								(String)avance.getText("DocumentState"), 
								mois[i]);
						evolutionMap.put(tmpG, tmp);
					}
				}
				
//				tmpGroupModel.mois = mois[moisIndex];
				JSONObject mapObject = evolutionMap.get(tmpGroupModel);
				mapObject.put("Nb", (int)mapObject.get("Nb") + 1);
				mapObject.put("Montant", ((Number)mapObject.get("Montant")).floatValue() + (Float)avance.getValue("Montant"));
				evolutionMap.put(tmpGroupModel, mapObject);
			}
		
			ResultObject.put("data", evolutionMap.values());
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
			controller.addEqualsConstraint("Beneficiaire", connectedUser);
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
