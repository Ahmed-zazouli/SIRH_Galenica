package com.moovapps.ibb.rh.Controllers.ModulesDashBoard.DossierMaladie;

import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext.IRequest;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.utils.StreamUtils;
import com.moovapps.ibb.rh.Controllers.Helpers.DateHelper;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ModuleDossierMaladieClotureesDash extends BaseController {
	
	private class GroupModel {
		String dateRefusOuRemboursement = null;
//        String statut = null;
//        String age = null;
		
		public GroupModel(String dateRefusOuRemboursement){
			this.dateRefusOuRemboursement = dateRefusOuRemboursement;
//			this.statut = statut;
//			this.age = age;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof GroupModel))
	            return false;
	        if (obj == this)
	            return true;

	        GroupModel otherGroup = (GroupModel) obj;
			
			return (dateRefusOuRemboursement.equals(otherGroup.dateRefusOuRemboursement)/* &&
					statut.equals(otherGroup.statut) &&
					age.equals(otherGroup.age)*/);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(dateRefusOuRemboursement/*, statut, age*/);
		}
	}
	
	protected static final Logger log = Logger.getLogger(ModuleDossierMaladieClotureesDash.class);

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
			
			listFiltres.add("Date rejet ou remboursement");
			listFiltres.add("Age");
			listFiltres.add("Statut");
			
			FiltreObject.put("lesFiltres", listFiltres);
			
			ResultObject.putAll(FiltreObject);
			
			SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM-yy");
			
			Collection<IWorkflowInstance> dossiersMaladie =  getData();
			HashMap<GroupModel, JSONObject> evolutionMap = new LinkedHashMap<GroupModel, JSONObject>();
				for (IWorkflowInstance dossier : dossiersMaladie)
				{
					Date date = null;
					if(dossier.getValue("DateDeRemboursement") != null){
						date = (Date)dossier.getValue("DateDeRemboursement");
					} else if(dossier.getValue("DateDeRejetDuDossier") != null){
						date = (Date)dossier.getValue("DateDeRejetDuDossier");
					}
					
					String dateRefusOuRemboursement = date != null ? monthYearFormat.format(date) : "N/D";
					String statut = (String)dossier.getText("DocumentState");
					String age = dateRefusOuRemboursement != null ? getAge(new DateHelper().monthsBetweenTwoDates((Date)dossier.getValue("sys_CreationDate"), date)) : "N/D";
					GroupModel tmpGroupModel = new GroupModel(dateRefusOuRemboursement);
					
					if(!evolutionMap.containsKey(tmpGroupModel)){
						JSONObject tmp = new JSONObject();
						tmp.put("Date rejet ou remboursement", dateRefusOuRemboursement);
						tmp.put("Age", age);
						tmp.put("Statut", statut);
						tmp.put("Montant Remboursé total", 0);
						tmp.put("Pourcentage remboursé", 0);
						tmp.put("Nbr", 0);
						evolutionMap.put(tmpGroupModel, tmp);
					}
					JSONObject mapObject = evolutionMap.get(tmpGroupModel);
					mapObject.put("Nbr", ((Number)mapObject.get("Nbr")).intValue() + 1);
					if(statut.equals("Remboursé clôturé") || statut.equals("Remboursé")){
						mapObject.put("Montant Remboursé total", ((Number)mapObject.get("Montant Remboursé total")).floatValue() + ((Number)dossier.getValue("MontantRembourse")).floatValue());

					}
					evolutionMap.put(tmpGroupModel, mapObject);
				}
				
				Iterator<JSONObject> it = evolutionMap.values().iterator();
				
				NumberFormat nf = NumberFormat.getNumberInstance();
				nf.setMaximumFractionDigits(1);
				
//				while (it.hasNext()) {
//					JSONObject next = it.next();
//					if(((Number)next.get("Nbr")).floatValue() > 0){
//					next.put("Pourcentage remboursé", Float.valueOf(
//							nf.format(((Number) next.get("Montant Remboursé total")) .floatValue() / ((Number) next.get("Nbr")).floatValue()).replace(',', '.')));
//					}
//				}

				ResultObject.put("data", evolutionMap.values());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return ResultObject;
		
	}
	
	private String getAge(int age){
//		if(age >= 0 && age < 1){
//			return "0-1";
//		}else if(age >= 1 && age < 2){
//			return "1-2";
//		}else if(age >= 2 && age < 3){
//			return "2-3";
//		}else if(age >= 3 && age < 4){
//			return "3-4";
//		}
		return age + "-" + (age+1);
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
			controller.addEqualsConstraint("Demandeur", connectedUser);
			controller.addInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("Rejeté par assurance", "Remboursé clôturé", "Remboursé")));
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
