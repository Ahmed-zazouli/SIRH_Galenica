package com.moovapps.ibb.rh.Controllers.Cockpits.Manager.DossierMaladie;

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
import java.util.*;

public class CockpitManagerNbMontanDelaiDossierMaladieRembourseEvolution extends BaseController {
	
	private class GroupModel{
		String societe = null;
        IUser salarie = null;
        String direction = null;
        String statut = null;
		String mois = null;
		
		public GroupModel(String societe, IUser salarie, String direction, String statut, String mois){
			this.societe = societe;
			this.salarie = salarie;
			this.direction = direction;
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
					salarie.equals(otherGroup.salarie) &&
					direction.equals(otherGroup.direction) &&
					statut.equals(otherGroup.statut) &&
					mois.equals(otherGroup.mois));
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(societe, salarie, direction, statut, mois);
		}
	}
	
	protected static final Logger log = Logger.getLogger(CockpitManagerNbMontanDelaiDossierMaladieRembourseEvolution.class);

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
			listFiltres.add("Statut");
			listFiltres.add("Mois");
			
			FiltreObject.put("lesFiltres", listFiltres);
			ResultObject.putAll(FiltreObject);
			
			HashMap<String, IStorageResource> societies = new HashMap<String,IStorageResource>();
			
			Collection<IWorkflowInstance> dossiersMaladie =  getData();
			HashMap<GroupModel, JSONObject> evolutionMap = new LinkedHashMap<GroupModel, JSONObject>();
			
			for (IWorkflowInstance dossierMaladie : dossiersMaladie)
			{
				String societeName = null;
				IUser Demandeur = (IUser)dossierMaladie.getValue("Demandeur");
				if(dossierMaladie.getValue("Societe") != null){
					//societeName = (String)dossierMaladie.getValue("Societe");
					societeName = (String)((IStorageResource) Demandeur.getExtendedAttributes().getValue("Societe")).getValue("sys_Title");
				}
				if(!societies.containsKey(societeName)){
					//societies.put(societeName, getSociete(societeName));
					IStorageResource societe = (IStorageResource) Demandeur.getExtendedAttributes().getValue("Societe");
					societies.put(societeName, societe);
					}
				
				Date moisDate = (Date)dossierMaladie.getValue("sys_CreationDate");
				Calendar x = Calendar.getInstance();
				x.setTime(moisDate);
				
				IUser beneficiaire = (IUser)dossierMaladie.getValue("Demandeur");
				String direction = "N/D";
				if(beneficiaire.getExtendedAttributes().getValue("Direction") != null){
					direction = (String)((IStorageResource)beneficiaire.getExtendedAttributes().getValue("Direction")).getValue("sys_Title");
				}
				
				GroupModel tmpGroupModel = new GroupModel(societeName, 
						(IUser)dossierMaladie.getValue("Demandeur"), 
						direction,
						(String)dossierMaladie.getText("DocumentState"), 
						String.valueOf(x.get(Calendar.MONTH) + 1));
				
				Date dateRemboursementPrevu = new Date();
				if(dossierMaladie.getValue("DateDeRemboursement") != null){
					dateRemboursementPrevu = (Date)dossierMaladie.getValue("DateDeRemboursement");
				}
				
				if(!evolutionMap.containsKey(tmpGroupModel)){					
					for (int i = 1; i < 13; i++) {
						JSONObject tmp = new JSONObject();
						tmp.put("Société", societeName);
						tmp.put("Salarié", ((IUser)dossierMaladie.getValue("Demandeur")).getFullName());
						tmp.put("Direction", direction);
						tmp.put("Statut", dossierMaladie.getText("DocumentState"));
						tmp.put("Mois", String.valueOf(i));
						tmp.put("Nb", 0);
						tmp.put("Montant remboursé", 0);
						tmp.put("Délai instruction", 0);
						GroupModel tmpG = new GroupModel(societeName, 
								(IUser)dossierMaladie.getValue("Demandeur"), 
								direction,
								(String)dossierMaladie.getText("DocumentState"), 
								String.valueOf(i));
//						tmpGroupModel.mois = String.valueOf(i);
						evolutionMap.put(tmpG, tmp);
					}
				}
				
//				Date moisDate = (Date)dossierMaladie.getValue("sys_CreationDate");
//				Calendar x = Calendar.getInstance();
//				x.setTime(moisDate);
				
//				tmpGroupModel.mois = String.valueOf(x.get(Calendar.MONTH) + 1);
				
				JSONObject mapObject = evolutionMap.get(tmpGroupModel);
				
				mapObject.put("Nb", (int)mapObject.get("Nb") + 1);
				mapObject.put("Montant remboursé", ((Number)mapObject.get("Montant remboursé")).floatValue() + ((Number)dossierMaladie.getValue("MontantRembourse")).floatValue());
				
				DateHelper dateHelper = new DateHelper();
				double delaiInstruction = dateHelper.getDurationBetweenTwoDatesInMin(
						dateHelper.formatDateAsDate((Date)dossierMaladie.getValue("DateDepotDuDossierALAssurance"), "dd-MM-yyyy") ,
						dateHelper.formatDateAsDate(dateRemboursementPrevu, "dd-MM-yyyy")
						)/(24*60);
				mapObject.put("Délai instruction", ((Number)mapObject.get("Délai instruction")).doubleValue() + delaiInstruction);
				
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
			IProject project = getProjectModule().getProject(sysContext, "DossierMaladie", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "DossiersMaladie", project);
			//IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "DossiersMaladie_1.0");
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "DossiersMaladie");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("SuperieurHierarchique", connectedUser);
			controller.addInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("Remboursé", "Remboursé clôturé")));
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
