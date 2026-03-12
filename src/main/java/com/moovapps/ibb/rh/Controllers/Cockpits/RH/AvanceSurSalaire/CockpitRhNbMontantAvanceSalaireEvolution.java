package com.moovapps.ibb.rh.Controllers.Cockpits.RH.AvanceSurSalaire;

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

public class CockpitRhNbMontantAvanceSalaireEvolution extends BaseController {
	
	private class GroupModel{
		String societe = null;
		IUser manager = null;
        IUser beneficiaire = null;
        String statut = null;
		String mois = null;
		
		public GroupModel(String societe, IUser manager, IUser beneficiaire, String statut, String mois){
			this.societe = societe;
			this.manager = manager;
			this.beneficiaire = beneficiaire;
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
					beneficiaire == otherGroup.beneficiaire &&
					statut.equals(otherGroup.statut) &&
					mois.equals(otherGroup.mois));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(societe, manager, beneficiaire, statut, mois);
		}
	}
	
	protected static final Logger log = Logger.getLogger(CockpitRhNbMontantAvanceSalaireEvolution.class);

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
			listFiltres.add("Bénéficiaire");
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
				IUser beneficiaire = (IUser)avance.getValue("Beneficiaire");
				if(avance.getValue("Societe") != null){
					societeName = (String)((IStorageResource) beneficiaire.getExtendedAttributes().getValue("Societe")).getValue("sys_Title");
				}
				if(!societies.containsKey(societeName)){
					IStorageResource societe = (IStorageResource) beneficiaire.getExtendedAttributes().getValue("Societe");
					societies.put(societeName, societe);
					}
				String departement = null ;
				if (beneficiaire.getExtendedAttributes().getValue("Departement") != null){
					departement = (String)((IStorageResource) beneficiaire.getExtendedAttributes().getValue("Departement")).getValue("sys_Title");

				}
				
				Date moisDate = (Date)avance.getValue("sys_CreationDate");
				Calendar x = Calendar.getInstance();
				x.setTime(moisDate);
				String mois = String.valueOf(x.get(Calendar.MONTH) + 1);
				GroupModel tmpGroupModel = new GroupModel(societeName, 
						(IUser)avance.getValue("SuperieurHierarchique"), 
						(IUser)avance.getValue("Beneficiaire"), 
						(String)avance.getText("DocumentState"), 
						mois);
				
				if(!evolutionMap.containsKey(tmpGroupModel)){					
					for (int i = 1; i < 13; i++) {
						JSONObject tmp = new JSONObject();
						tmp.put("Société", departement);
						tmp.put("Manager", avance.getValue("SuperieurHierarchique")!= null ? ((IUser)avance.getValue("SuperieurHierarchique")).getFullName(): "N/D");
						tmp.put("Bénéficiaire", ((IUser)avance.getValue("Beneficiaire")).getFullName());
						tmp.put("Statut", avance.getText("DocumentState"));
						tmp.put("Mois", String.valueOf(i));
						tmp.put("Nb", 0);
						tmp.put("Montant", 0);
						GroupModel tmpG = new GroupModel(societeName, 
								(IUser)avance.getValue("SuperieurHierarchique"), 
								(IUser)avance.getValue("Beneficiaire"), 
								(String)avance.getText("DocumentState"), 
								String.valueOf(i));
//						tmpGroupModel.mois = String.valueOf(i);
						evolutionMap.put(tmpG, tmp);
					}
				}
				
//				tmpGroupModel.mois = String.valueOf(x.get(Calendar.MONTH) + 1);
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
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDesAvances");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			controller.addNotEqualsConstraint("DocumentState", "Refusée");

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
