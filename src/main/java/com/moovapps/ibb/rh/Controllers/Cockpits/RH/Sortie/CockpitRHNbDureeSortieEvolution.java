package com.moovapps.ibb.rh.Controllers.Cockpits.RH.Sortie;

import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext.IRequest;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.utils.StreamUtils;
import com.moovapps.ibb.rh.Controllers.Helpers.DateHelper;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.*;

public class CockpitRHNbDureeSortieEvolution extends BaseController {
	
	private class GroupModel{
		String societe = null;
		String mois = null;
		
		public GroupModel(String societe, String mois){
			this.societe = societe;
			this.mois = mois;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof GroupModel))
	            return false;
	        if (obj == this)
	            return true;

	        GroupModel otherGroup = (GroupModel) obj;
			
			return (societe.equals(otherGroup.societe) && mois.equals(otherGroup.mois));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(societe,mois);
		}
	}
	
	protected static final Logger log = Logger.getLogger(CockpitRHNbDureeSortieEvolution.class);

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
		DecimalFormat twodigits = new DecimalFormat("00");
		JSONObject ResultObject = new JSONObject();
		try
		{	
			List<String> listFiltres = new ArrayList<>();
			JSONObject FiltreObject = new JSONObject();
			
			listFiltres.add("Nb sortie");
			listFiltres.add("Durée sortie");
			listFiltres.add("Société");
			listFiltres.add("Mois");
			
			FiltreObject.put("lesFiltres", listFiltres);
			ResultObject.putAll(FiltreObject);
			
			HashMap<String, IStorageResource> societies = new HashMap<String,IStorageResource>();
			
			Collection<IWorkflowInstance> declarationsRetard =  getData();
			HashMap<GroupModel, JSONObject> evolutionMap = new LinkedHashMap<GroupModel, JSONObject>();
			
			for (IWorkflowInstance retard : declarationsRetard)
			{
				String societeName = null;
				IUser connectedUser = getWorkflowModule().getLoggedOnUser();

				if(retard.getValue("Societe") != null){
					//societeName = (String)retard.getValue("Societe");
					societeName = (String)((IStorageResource) connectedUser.getExtendedAttributes().getValue("Societe")).getValue("sys_Title");

				}
				String departement = null ;
				if (connectedUser.getExtendedAttributes().getValue("Departement") != null){
					departement = (String)((IStorageResource) connectedUser.getExtendedAttributes().getValue("Departement")).getValue("sys_Title");

				}
				if(!societies.containsKey(societeName)){
					societies.put(societeName, getSociete(societeName));
					for (int i = 1; i < 13; i++) {
						JSONObject tmp = new JSONObject();
						tmp.put("Nb sortie", 0);
						tmp.put("Durée sortie", 0);
						tmp.put("Société", departement);
						tmp.put("Mois", String.format("%2d", i));
						GroupModel tmpGroupModel = new GroupModel(societeName, twodigits.format(i));
						evolutionMap.put(tmpGroupModel, tmp);
					}
				}
				
				Date moisDate = (Date)retard.getValue("dateSortie");
				Calendar x = Calendar.getInstance();
				x.setTime(moisDate);
				String mois = twodigits.format(x.get(Calendar.MONTH) + 1);
				GroupModel tmpGroupModel = new GroupModel(societeName, mois);
				JSONObject mapObject = evolutionMap.get(tmpGroupModel);
				mapObject.put("Nb sortie", (int)mapObject.get("Nb sortie") + 1);
				mapObject.put("Durée sortie", (int)mapObject.get("Durée sortie") + ((Number)retard.getValue("dureeSortieMinutesReelle")).intValue());
				evolutionMap.put(tmpGroupModel, mapObject);
			}
			
			Iterator<JSONObject> it = evolutionMap.values().iterator();
			while (it.hasNext()){
				JSONObject next = it.next();
				next.put("Durée sortie", new DateHelper().durationToDays(((Number)next.get("Durée sortie")).longValue(), societies.get(next.get("Société"))));
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
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			//IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "GestionDeSortie_1.0");
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeSortie");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
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
