package com.moovapps.ibb.rh.Controllers.ModulesDashBoard.Conges;

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
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;

public class ModuleCongeKpi extends BaseController {

	protected static final Logger log = Logger.getLogger(ModuleCongeKpi.class);

	@Override
	public void parseRequest(IRequest arg0) throws IOException {
	}

	@Override
	public IExecutionContext doProcess(IExecutionContext ec) throws IOException {
		try {
			ec.getResponse().setContentType("application/json");
			ec.getResponse().addHeader("Access-Control-Allow-Origin", "*");
			HTTPUtils.setNoCacheResponseHeaders((HttpServletRequest) ec.getRequest().getNativeRequest(), (HttpServletResponse) ec.getResponse().getNativeResponse());
			buildResponse(ec, null);
		} catch (Exception e) {
			String message = e.getMessage();
			if (message == null) {
				message = "";
			}
			log.error("Error in SQLController doProcess method : " + e.getClass() + " - " + message);
		}
		return ec;
	}

	@Override
	protected IExecutionContext buildResponse(IExecutionContext ec, Result result) throws IOException {
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.buildResponse(ec, result);
	}

	private JSONObject BuildData() {

		JSONArray ResultDataArray = new JSONArray();
		JSONObject ResultObject = new JSONObject();
		IUser connectedUser = getWorkflowModule().getLoggedOnUser();

		try {
//			Collection<IWorkflowInstance> allMyConges = getData();
//			int nbCongesPayes = 0;
//			float dureeCongesPayes = 0;
//			
//			int nbCongesSpeciaux = 0;
//			float dureeCongesSpeciaux = 0;
//			
//			int nbCongesMaladie = 0;
//			float dureeCongesMaladie = 0;
//			
//			int nbCongesSansSolde = 0;
//			float dureeCongesSansSolde = 0;
//			
//			for (IWorkflowInstance conge : allMyConges) {
//				if(conge.getValue("TypeDeConge").equals("CN")){
//					nbCongesPayes += 1;
//					dureeCongesPayes += ((Number)conge.getValue("NombreDeJoursDemandes")).floatValue();
//				} else if(conge.getValue("TypeDeConge").equals("CE")){
//					nbCongesSpeciaux += 1;
//					dureeCongesSpeciaux += ((Number)conge.getValue("NombreDeJoursExceptionnelle")).floatValue();
//				} else if(conge.getValue("TypeDeConge").equals("CM")){
//					nbCongesMaladie += 1;
//					dureeCongesMaladie += ((Number)conge.getValue("NombreDeJoursDemandes")).floatValue();
//				} else if(conge.getValue("TypeDeConge").equals("SS")){
//					nbCongesSansSolde += 1;
//					dureeCongesSansSolde += ((Number)conge.getValue("NombreDeJoursDemandes")).floatValue();
//				}
//			}
			
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(1);
			Float droitMensuelleConge = connectedUser.getExtendedAttributes().getValue("DroitMensuelle") != null ? (Float)connectedUser.getExtendedAttributes().getValue("DroitMensuelle") : 0;
			Float soldeCongesRestant = connectedUser.getExtendedAttributes().getValue("SoldeConges") != null ? (Float)connectedUser.getExtendedAttributes().getValue("SoldeConges") : 0;
			Float congesPayesConsommes = connectedUser.getExtendedAttributes().getValue("CongesPayesPris") != null ? (Float)connectedUser.getExtendedAttributes().getValue("CongesPayesPris") : 0;

			Float congesSpeciauxConsommes = connectedUser.getExtendedAttributes().getValue("CongesSpeciauxPris") != null ? (Float)connectedUser.getExtendedAttributes().getValue("CongesSpeciauxPris") : 0;
			Float congesMaladieConsommes = connectedUser.getExtendedAttributes().getValue("CongesMaladiePris") != null ? (Float)connectedUser.getExtendedAttributes().getValue("CongesMaladiePris") : 0;
			Float congesSansSoldeConsommes = connectedUser.getExtendedAttributes().getValue("CongesSansSoldePris") != null ? (Float)connectedUser.getExtendedAttributes().getValue("CongesSansSoldePris") : 0;;

			Float autreCongesConsommes = congesSpeciauxConsommes + congesMaladieConsommes + congesSansSoldeConsommes;
			double shift = Math.pow(10,2);
			JSONObject tmp = new JSONObject();
			tmp.put("kpi1_value", Math.round(droitMensuelleConge*shift)/shift);
			tmp.put("kpi2_value", Math.round(soldeCongesRestant*shift)/shift);
			tmp.put("kpi3_value", Math.round(congesPayesConsommes*shift)/shift);
			tmp.put("kpi4_value", Math.round(autreCongesConsommes*shift)/shift);
			ResultDataArray.put(tmp);

			ResultObject.put("data", ResultDataArray);
		} catch (Exception e) {
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
			controller.addEqualsConstraint("Demandeur", connectedUser);
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
			IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", getDirectoryModule().getOrganization(context, "DefaultOrganization"));
			ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
			controller.addEqualsConstraint("sys_Title", societeName);
			Collection<IStorageResource> societes = controller.evaluate(definition);
			if (!societes.isEmpty()) {
				societe = societes.iterator().next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return societe;
	}
}
