package com.moovapps.ibb.rh.Controllers.ModulesDashBoard.DossierMaladie;

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
import java.util.Collection;

public class ModuleDossierMaladieKpi extends BaseController {

	protected static final Logger log = Logger.getLogger(ModuleDossierMaladieKpi.class);

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

		IUser connectedUser = getWorkflowModule().getLoggedOnUser();
		JSONArray ResultDataArray = new JSONArray();
		IStorageResource societe = (IStorageResource) connectedUser.getExtendedAttributes().getValue("Societe");
		float delaiTraitementDossierMaladieSociete = ((Number)societe.getValue("DelaiTraitementDossierMaladie")).floatValue();
		JSONObject ResultObject = new JSONObject();
		try {

			Collection<IWorkflowInstance> dossiersMaladies = getData();
			
//			int demandeEnCours = 0;
//			int demandeEnRetard = 0;
//			int montantRembourse = 0;
//			int ageTotal = 0;
//			int ageCount = 0;
//			int ageMoyen = 0;
//			
//			for (IWorkflowInstance dossier : dossiersMaladies) {
//				Date dateRemboursementPrevu = new Date();
//				if(dossier.getValue("DateDeRemboursement") != null){
//					dateRemboursementPrevu = (Date)dossier.getValue("DateDeRemboursement");
//				}
//				DateHelper dateHelper = new DateHelper();
//				double delaiInstruction = dateHelper.getDurationBetweenTwoDatesInMin(
//						dateHelper.formatDateAsDate((Date)dossier.getValue("sys_CreationDate"), "dd-MM-yyyy") ,
//						dateHelper.formatDateAsDate(dateRemboursementPrevu, "dd-MM-yyyy")
//						)/(24*60);
//				
//				if(!dossier.getValue("DocumentState").equals("Rejeté") && !dossier.getValue("DocumentState").equals("Remboursé clôturé")){
//					demandeEnCours += 1;
//					if(delaiTraitementDossierMaladieSociete <= delaiInstruction && !dossier.getValue("DocumentState").equals("Remboursé")){
//						demandeEnRetard += 1;
//					}
//				}
//				if(dossier.getValue("DocumentState").equals("Remboursé clôturé") || dossier.getValue("DocumentState").equals("Remboursé")){
//					montantRembourse += ((Number)dossier.getValue("MontantRembourse")).floatValue();
//					ageTotal += delaiInstruction;
//					ageCount += 1;
//				}
//			}
//			if(ageCount != 0){
//				ageMoyen = ageTotal / ageCount;
//			}
			
			int dossiersRembourses = 0;
			int dossiersEnCours = 0;
			int montantSoins = 0;
			int montantRembourse = 0;
			
			for (IWorkflowInstance dossier : dossiersMaladies) {
//				Date dateRemboursementPrevu = new Date();
//				if(dossier.getValue("DateDeRemboursement") != null){
//					dateRemboursementPrevu = (Date)dossier.getValue("DateDeRemboursement");
//				}
//				DateHelper dateHelper = new DateHelper();
//				double delaiInstruction = dateHelper.getDurationBetweenTwoDatesInMin(
//						dateHelper.formatDateAsDate((Date)dossier.getValue("sys_CreationDate"), "dd-MM-yyyy") ,
//						dateHelper.formatDateAsDate(dateRemboursementPrevu, "dd-MM-yyyy")
//						)/(24*60);
				montantSoins += ((Number)dossier.getValue("MontantDesSoins")).floatValue();
				if(!dossier.getValue("DocumentState").equals("Rejeté par RH") && !dossier.getValue("DocumentState").equals("Rejeté par assurance") && !dossier.getValue("DocumentState").equals("Remboursé clôturé")){
					dossiersEnCours += 1;
				}
				if(dossier.getValue("DocumentState").equals("Remboursé clôturé") || dossier.getValue("DocumentState").equals("Remboursé")){
					montantRembourse += ((Number)dossier.getValue("MontantRembourse")).floatValue();
					dossiersRembourses += 1;
				}
			}
			
			JSONObject tmp = new JSONObject();
			tmp.put("kpi1_value", dossiersRembourses);
			tmp.put("kpi2_value", dossiersEnCours);
			tmp.put("kpi3_value", montantSoins);
			tmp.put("kpi4_value", montantRembourse);
			ResultDataArray.put(tmp);

			ResultObject.put("data", ResultDataArray);
		} catch (Exception e) {
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
			controller.addEqualsConstraint("Demandeur", connectedUser);
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
