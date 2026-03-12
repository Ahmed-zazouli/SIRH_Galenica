package com.moovapps.ibb.rh.Controllers.Cockpits.RH.Conges;

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

public class CockpitRhNbDureeCongesEvolution extends BaseController {
	
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
	
	private class GroupModel{
		String categorie = null;
		String mois = null;
		
		public GroupModel(String categorie, String mois){
			this.categorie = categorie;
			this.mois = mois;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof GroupModel))
	            return false;
	        if (obj == this)
	            return true;

	        GroupModel otherGroup = (GroupModel) obj;
			
			return (categorie.equals(otherGroup.categorie) && mois.equals(otherGroup.mois));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(categorie, mois);
		}
	}
	
	protected static final Logger log = Logger.getLogger(CockpitRhNbDureeCongesEvolution.class);

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
			
			listFiltres.add("Nb congés");
			listFiltres.add("Durée congés");
			listFiltres.add("Catégorie");
			listFiltres.add("Mois");
			
			FiltreObject.put("lesFiltres", listFiltres);
			ResultObject.putAll(FiltreObject);
			
			HashMap<String, IStorageResource> societies = new HashMap<String,IStorageResource>();
			
			Collection<IWorkflowInstance> demandesConges =  getData();
			HashMap<GroupModel, JSONObject> evolutionMap = new LinkedHashMap<GroupModel, JSONObject>();
			
			for (IWorkflowInstance conge : demandesConges)
			{
				String societeName = null;
				IUser connectedUser = getWorkflowModule().getLoggedOnUser();
				if(conge.getValue("Societe") != null){
					societeName = (String)((IStorageResource) connectedUser.getExtendedAttributes().getValue("Societe")).getValue("sys_Title");
				}
				if(!societies.containsKey(societeName)){
					IStorageResource societe = (IStorageResource) connectedUser.getExtendedAttributes().getValue("Societe");
					societies.put(societeName, societe);
					}
				
				GroupModel testObject = new GroupModel(TYPECONGES.valueOf((String)conge.getValue("TypeDeConge")).label, "1");
				if(!evolutionMap.containsKey(testObject)){					
					for (int i = 1; i < 13; i++) {
						JSONObject tmp = new JSONObject();
						tmp.put("Nb congés", 0);
						tmp.put("Durée congés", 0);
						tmp.put("Catégorie", TYPECONGES.valueOf((String)conge.getValue("TypeDeConge")).label);
						tmp.put("Mois", String.valueOf(i));
						GroupModel tmpGroupModel = new GroupModel(TYPECONGES.valueOf((String)conge.getValue("TypeDeConge")).label, String.valueOf(i));
						evolutionMap.put(tmpGroupModel, tmp);
					}
				}
				
				Date moisDate = (Date)conge.getValue("DateDeDebut");
				Calendar x = Calendar.getInstance();
				x.setTime(moisDate);
				String mois = String.valueOf(x.get(Calendar.MONTH) + 1);
				GroupModel tmpGroupModel = new GroupModel(TYPECONGES.valueOf((String)conge.getValue("TypeDeConge")).label, mois);
				JSONObject mapObject = evolutionMap.get(tmpGroupModel);
				mapObject.put("Nb congés", (int)mapObject.get("Nb congés") + 1);
				float dureeConge = 0;
				if(conge.getValue("TypeDeConge") != null){
					if(conge.getValue("TypeDeConge").equals("CE")){
						dureeConge =  (Float)conge.getValue("NombreDeJoursExceptionnelle");
					}else{
						dureeConge =  (Float)conge.getValue("NombreDeJoursDemandes");
					}
				}
				mapObject.put("Durée congés", ((Number)mapObject.get("Durée congés")).floatValue() + dureeConge);
				evolutionMap.put(tmpGroupModel, mapObject);
			}
			
//			Iterator<JSONObject> it = evolutionMap.values().iterator();
//			while (it.hasNext()){
//				JSONObject next = it.next();
//				next.put("Durée congés", new DateHelper().durationToDays(((Number)next.get("Durée congés")).longValue(), societies.get(next.get("Société"))));
//			}
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
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			controller.addNotEqualsConstraint("DocumentState", "Refusé");
			controller.addNotEqualsConstraint("DocumentState", "Annulé");
			controller.addNotEqualsConstraint("TypeDeConge", "Absence");
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
