package com.moovapps.ibb.rh.Controllers.Cockpits.Manager.AttestationAdministrative;

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

public class CockpitManagerNbAttAdministrativeEvolution extends BaseController {
	
	private class GroupModel{
		String societe = null;
        IUser salarie = null;
        String direction = null;
        Object motif = null;
        String statut = null;
		String mois = null;
		
		public GroupModel(String societe, IUser salarie, String direction, Object motif, String statut, String mois){
			this.societe = societe;
			this.salarie = salarie;
			this.direction = direction;
			this.statut = statut;
			this.motif = motif;
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
					salarie == otherGroup.salarie &&
					motif.equals(otherGroup.motif) &&
					statut.equals(otherGroup.statut) &&
					direction.equals(otherGroup.direction) &&
					mois.equals(otherGroup.mois));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(societe, salarie, direction, motif, statut, mois);
		}
	}
	
	protected static final Logger log = Logger.getLogger(CockpitManagerNbAttAdministrativeEvolution.class);

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
			listFiltres.add("Salarié");
			listFiltres.add("Direction");
			listFiltres.add("Motif");
			listFiltres.add("Statut");
			listFiltres.add("Mois");
			
			FiltreObject.put("lesFiltres", listFiltres);
			ResultObject.putAll(FiltreObject);
			
			HashMap<String, IStorageResource> societies = new HashMap<String,IStorageResource>();
			
			Collection<IWorkflowInstance> demandesAttestation =  getData();
			HashMap<GroupModel, JSONObject> evolutionMap = new LinkedHashMap<GroupModel, JSONObject>();
			
			for (IWorkflowInstance attestation : demandesAttestation)
			{
				String societeName = null;
				if(attestation.getValue("Societe") != null){
					societeName = (String)attestation.getValue("Societe");
				}
				if(!societies.containsKey(societeName)){
					societies.put(societeName, getSociete(societeName));
				}
				
				IUser demandeur = (IUser)attestation.getValue("Demandeur2");
				String direction = "N/D";
				if(demandeur.getExtendedAttributes().getValue("Direction") != null){
					direction = (String)((IStorageResource)demandeur.getExtendedAttributes().getValue("Direction")).getValue("sys_Title");
				}
				
				Date moisDate = (Date)attestation.getValue("sys_CreationDate");
				Calendar x = Calendar.getInstance();
				x.setTime(moisDate);
				
				GroupModel tmpGroupModel = new GroupModel(societeName, 
						(IUser)attestation.getValue("Demandeur2"), 
						direction,
						attestation.getValue("MotifDemandeAttestation"), 
						(String)attestation.getText("DocumentState"), 
						String.valueOf(x.get(Calendar.MONTH) + 1));
				
				if(!evolutionMap.containsKey(tmpGroupModel)){					
					for (int i = 1; i < 13; i++) {
						JSONObject tmp = new JSONObject();
						tmp.put("Société", societeName);
						tmp.put("Salarié", demandeur.getFullName());
						tmp.put("Direction", direction);
						tmp.put("Motif", attestation.getValue("MotifDemandeAttestation") != null ? attestation.getValue("MotifDemandeAttestation") : "N/D");
						tmp.put("Statut", attestation.getText("DocumentState"));
						tmp.put("Mois", String.valueOf(i));
						tmp.put("Nb", 0);
						GroupModel tmpG = new GroupModel(societeName, 
								(IUser)attestation.getValue("Demandeur2"), 
								direction,
								attestation.getValue("MotifDemandeAttestation"), 
								(String)attestation.getText("DocumentState"), 
								String.valueOf(i));
//						tmpGroupModel.mois = String.valueOf(i);
						evolutionMap.put(tmpG, tmp);
					}
				}
				
//				tmpGroupModel.mois = String.valueOf(x.get(Calendar.MONTH) + 1);
				
				JSONObject mapObject = evolutionMap.get(tmpGroupModel);
				
				mapObject.put("Nb", (int)mapObject.get("Nb") + 1);
				
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
