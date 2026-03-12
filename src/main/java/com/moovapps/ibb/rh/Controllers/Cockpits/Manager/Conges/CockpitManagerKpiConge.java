package com.moovapps.ibb.rh.Controllers.Cockpits.Manager.Conges;

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

public class CockpitManagerKpiConge extends BaseController {

	protected static final Logger log = Logger
			.getLogger(CockpitManagerKpiConge.class);

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
			Collection<IWorkflowInstance> demandesCeMois = getAllDemandesCongesCemMois();
			Collection<IWorkflowInstance> demandeAccepteesCeMois = getAllDemandesCongesAccepteesCemMois();
			Collection<IWorkflowInstance> demandeRefuseesCeMois = getAllDemandesCongesRefuseesCemMois();

			JSONObject tmp = new JSONObject();
			tmp.put("kpi1_value", todos.size());
			tmp.put("kpi2_value", demandesCeMois.size());
			tmp.put("kpi3_value", demandeAccepteesCeMois.size());
			tmp.put("kpi4_value", demandeRefuseesCeMois.size());
			ResultDataArray.put(tmp);

			ResultObject.put("data", ResultDataArray);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResultObject;

	}

	private Collection<IWorkflowInstance> getAllDemandesCongesRefuseesCemMois() {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try {
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
			// IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("ValidateurConge", connectedUser);
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, 1);
			controller.addGreaterConstraint("DateDeRefus", c.getTime());
			c.add(Calendar.MONTH, 1);
			controller.addLessConstraint("DateDeRefus", c.getTime());
			collection = controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();

		}
		return collection;
	}

	private Collection<IWorkflowInstance> getAllDemandesCongesCemMois() {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try {
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
			// IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("ValidateurConge", connectedUser);
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			controller.addNotEqualsConstraint("TypeDeConge", "Absence");
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, 1);
			controller.addGreaterConstraint("sys_CreationDate", c.getTime());
			c.add(Calendar.MONTH, 1);
			controller.addLessConstraint("sys_CreationDate", c.getTime());
			collection = controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();

		}
		return collection;
	}

	private Collection<IWorkflowInstance> getAllDemandesCongesAccepteesCemMois() {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try {
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Capone", organization);
			// IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("ValidateurConge", connectedUser);
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			controller.addNotEqualsConstraint("TypeDeConge", "Absence");
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_MONTH, 1);
			controller.addGreaterConstraint("DateDAcceptation", c.getTime());
			c.add(Calendar.MONTH, 1);
			controller.addLessConstraint("DateDAcceptation", c.getTime());
			collection = controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();

		}
		return collection;
	}

	private ArrayList<IWorkflowInstance> getTodo() {
		final ArrayList<IWorkflowInstance> arrayList = new ArrayList<IWorkflowInstance>();
		final String[] lien = { "uril://vdoc/workflowContainerView/DefaultOrganization/Capone/RH:0/GestionDeConges/LesDemandesDeCongesATraiterPourKPI" };
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
