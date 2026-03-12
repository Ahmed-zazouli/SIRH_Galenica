package com.moovapps.ibb.rh.Controllers.Cockpits.Manager.Absence;

import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext.IRequest;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.activity.domain.ActionTaskInstance;
import com.axemble.vdp.activity.domain.DecisionTaskInstance;
import com.axemble.vdp.utils.StreamUtils;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public class CockpitManagerKpiAbsence extends BaseController {

	protected static final Logger log = Logger
			.getLogger(CockpitManagerKpiAbsence.class);

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
		try {
			ArrayList<IWorkflowInstance> todos = getTodo();
			Collection<IWorkflowInstance> demandesCeMois = getAllDemandesAbsencesCemMois();
			Collection<IWorkflowInstance> demandeDeclares= getAllDemandeDeclares();
			Collection<IWorkflowInstance> demandeClotures= getAllDemandeClotures();

			JSONObject tmp = new JSONObject();
			tmp.put("kpi1_value", todos.size());
			tmp.put("kpi2_value", demandesCeMois.size());
			tmp.put("kpi3_value", demandeDeclares.size());
			tmp.put("kpi4_value", demandeClotures.size());
			ResultDataArray.put(tmp);

			ResultObject.put("data", ResultDataArray);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResultObject;

	}


	private Collection<IWorkflowInstance> getAllDemandeDeclares() {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try {
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
			// IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			//IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "GestionDeConges_3.0");
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addEqualsConstraint("SupHierarchique", connectedUser);
			controller.addEqualsConstraint("DocumentState", "En cours");
			controller.addEqualsConstraint("TypeDeConge", "Absence");

			collection = controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collection;
	}

	private Collection<IWorkflowInstance> getAllDemandesAbsencesCemMois() {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try {
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
			// IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			//IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "GestionDeConges_3.0");
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addEqualsConstraint("SupHierarchique", connectedUser);
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			controller.addEqualsConstraint("TypeDeConge", "Absence");
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, 1);
			controller.addGreaterConstraint("DateDeDebut", c.getTime());
			c.add(Calendar.MONTH, 1);
			controller.addLessConstraint("DateDeDebut", c.getTime());
			collection = controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collection;
	}

	private Collection<IWorkflowInstance> getAllDemandeClotures() {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try {
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
			// IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			//IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "GestionDeConges_3.0");
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addEqualsConstraint("SupHierarchique", connectedUser);
			controller.addEqualsConstraint("DocumentState", "Clôturé");
			controller.addEqualsConstraint("TypeDeConge", "Absence");

			collection = controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collection;
	}

	public IStorageResource getSociete(String societeName) {
		IStorageResource societe = null;
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IViewController controller = getWorkflowModule().getViewController(
					context, IResource.class);
			IProject project = getProjectModule().getProject(
					context,
					"REFERENTIELCOMMUN",
					getDirectoryModule().getOrganization(context,
							"DefaultOrganization"));
			ICatalog catalog = getWorkflowModule().getCatalog(context,
					"REFERENTIEL", 4, project);
			IResourceDefinition definition = getWorkflowModule()
					.getResourceDefinition(context, catalog, "Societe");
			controller.addEqualsConstraint("sys_Title", societeName);
			Collection<IStorageResource> societes = controller
					.evaluate(definition);
			if (!societes.isEmpty()) {
				societe = societes.iterator().next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return societe;
	}

	private ArrayList<IWorkflowInstance> getTodo() {
		final ArrayList<IWorkflowInstance> arrayList = new ArrayList<IWorkflowInstance>();
		final String[] lien = { "uril://vdoc/workflowContainerView/DefaultOrganization/Capone/RH:0/GestionDeConges/LesDemandesDAbsanceATraiterPourKPI" };
		try {
			String[] array;
			for (int length = (array = lien).length, i = 0; i < length; ++i) {
				final String string = array[i];
				final IView view = (IView) this.getWorkflowModule().getElementByProtocolURI(string);
				final ByteArrayInputStream bais = new ByteArrayInputStream(view.getXmlDefinition());
				final IContext sysContext = this.getWorkflowModule().getSysadminContext();
				final IOrganization organization = this.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
				final IProject project = this.getProjectModule().getProject(sysContext, "Capone", organization);
				final IViewController viewController = this.getWorkflowModule().getViewController(this.getWorkflowModule().getLoggedOnUserContext(), project, (InputStream) bais);
				final Collection<Object> resources = (Collection<Object>) viewController.evaluate();
				for (final Object iResource : resources) {
					IWorkflowInstance instance = null;
					if (iResource instanceof ActionTaskInstance) {
						instance = (IWorkflowInstance) ((ActionTaskInstance) iResource).getWorkflowInstance();
					} else {
						instance = (IWorkflowInstance) ((DecisionTaskInstance) iResource).getWorkflowInstance();
					}
					arrayList.add(instance);
				}
			}
		} catch (ModuleException e) {
			e.printStackTrace();
		}
		return arrayList;
	}
}
