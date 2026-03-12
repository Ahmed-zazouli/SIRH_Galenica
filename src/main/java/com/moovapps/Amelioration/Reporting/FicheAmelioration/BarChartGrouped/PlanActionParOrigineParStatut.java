package com.moovapps.Amelioration.Reporting.FicheAmelioration.BarChartGrouped;

import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext.IRequest;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.activity.domain.ActionTaskInstance;
import com.axemble.vdp.utils.StreamUtils;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;


public class PlanActionParOrigineParStatut extends BaseController{
protected static final Logger log = Logger.getLogger(PlanActionParOrigineParStatut.class);
	
	/**
	 * @see com.axemble.vdp.ui.framework.foundation.controllers.CustomController#parseRequest(com.axemble.vdp.ui.framework.runtime.IExecutionContext.IRequest)
	 */
	public void parseRequest(IRequest iRequest) throws IOException
	{
		
	}
	
	/**
	 * @see com.axemble.vdoc.sdk.controllers.BaseController#doProcess(com.axemble.vdp.ui.framework.runtime.IExecutionContext)
	 */
	@Override
	public IExecutionContext doProcess(IExecutionContext ec) throws IOException
	{
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
	
	/**
	 * @param cli_id
	 * @return
	 */
	/**
	 * @param cli_id
	 * @return
	 */
	private JSONObject chercheAdresse(String participation)
	{
		
		JSONArray dataArray = new JSONArray();
		JSONObject global = new JSONObject();
		JSONObject data = new JSONObject();
		try
		{
			
			List<String> list = new ArrayList<>();
			Map<String, List<String>> Filtres = new HashMap<>();
			
			list.add("Origine");
			//list.add("Type");
			list.add("Responsable réalisation");
			list.add("Statut");
			list.add("Année");
			list.add("Société");
			Filtres.put("lesFiltres", list);
			global.putAll(Filtres);
			
			
			Collection<IWorkflowInstance> planActions =  getActionsFiltrer();
			
			if(planActions!=null && !planActions.isEmpty()){
				for (IWorkflowInstance iWorkflowInstance : planActions)
				{
				data = new JSONObject();

				//data.put("Type", iWorkflowInstance.getValue("TypeAction") != null ? iWorkflowInstance.getValue("TypeAction") : "N/A");
				data.put("Responsable réalisation", iWorkflowInstance.getValue("ResponsableRealisation") != null ? ((IUser)iWorkflowInstance.getValue("ResponsableRealisation")).getFullName() : "N/A");
				Calendar c = Calendar.getInstance();
				Date dateCreationDocument = (Date) iWorkflowInstance.getValue("sys_CreationDate");
				c.setTime(dateCreationDocument);
				data.put("Année", c.get(java.util.Calendar.YEAR));
				if(iWorkflowInstance.getValue("Origine2")!=null){
					data.put("Origine", ((IStorageResource)iWorkflowInstance.getValue("Origine2")).getValue("sys_Title"));

				}else if(iWorkflowInstance.getValue("Origine3")!=null){
					data.put("Origine", (String)iWorkflowInstance.getValue("Origine3"));

				}else{
					data.put("Origine", "N/A");

				}
				
				if(iWorkflowInstance.getValue("Societe")!=null){
					data.put("Société", ((IStorageResource)iWorkflowInstance.getValue("Societe")).getValue("sys_Title"));
				}else{
					data.put("Société", "N/A");
				}
				//en evaluation c'est a dire deja realiser
				if(iWorkflowInstance.getValue("DocumentState").equals("En evaluation")||iWorkflowInstance.getValue("DocumentState").equals("Clôturée")){
					data.put("Statut", "Réalisé");
				}else{
					data.put("Statut", "En cours");
				}
				
					

					dataArray.put(data);	
			}

		global.put("data", dataArray);
			}
			
				
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return global;
		
	}
	
	@Override
	protected IExecutionContext buildResponse(IExecutionContext ec, Result result) throws IOException
	{
		try
		{
			String cli_id = ec.getRequest().getParameter("cli_id");
			JSONObject TableauJson = chercheAdresse(cli_id);
			OutputStream output;
			
			Document responseDocument;
			output = null;
			output = ec.getResponse().getOutputStream();
			output.toString();
			OutputStreamWriter osw = new OutputStreamWriter(output, "UTF-8");
			TableauJson.writeJSONString(osw);
			osw.flush();
			osw.close();
			// DomWriter.stdWrite(responseDocument, output, true);
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
	
	private Collection<IWorkflowInstance> getPlanActionAllOrPilote(String catalogName, String workflowName)
	{
		Collection<IWorkflowInstance> collection = null;
		try
		{
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "WIZEORGA", organization);
			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, catalogName, project);
			IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, workflowName);
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			if(!isMembreOfQualiteDirection()){
            	controller.addEqualsConstraint("ResponsableRealisation",connectedUser );
            }
			collection = controller.evaluate(w);
			return collection;
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		
		return null;
		// TODO Auto-generated method stub
	}
	Collection<IWorkflowInstance> getActions(){
		Collection<IWorkflowInstance> instances = null;
		IUser connectedUser = getWorkflowModule().getLoggedOnUser();
		//IStorageResource departement = (IStorageResource) connectedUser.getExtendedAttributes().getValue("Departement");
		//if(departement==null) return null;
		//ArrayList<IUser> departementUsers = getUsers(departement);
	  try
      {
		 // IUser connectedUser = getWorkflowModule().getLoggedOnUser();
          IContext sysContext = getWorkflowModule().getSysadminContext();
          IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
          IProject project = getProjectModule().getProject(sysContext, "WIZEORGA", organization);
          ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Processus", project);
          //IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "PlanDActions_1.0");
          IViewController controller = getWorkflowModule().getViewController(sysContext);
          controller.addInConstraint("sys_WorkflowContainer", Arrays.asList("PlanDAction"));
          controller.addEqualsConstraint("DocumentState", "En cours");
          controller.addInConstraint("ReportingViewers", connectedUser);
       
          instances = controller.evaluate(catalog);
      }
      catch (Exception e)
      {
          e.printStackTrace();
      }

	return instances;
	}
	
	private ArrayList<IUser> getUsers(IStorageResource departement) {
		Collection<IUser> users = (Collection<IUser>) getDirectoryModule().getUsers(getWorkflowModule().getSysadminContext());
		ArrayList<IUser> usersDepartement = new ArrayList<>();
		for(IUser user : users){
			if(user.getExtendedAttributes().getValue("Departement")!=null &&user.getExtendedAttributes().getValue("Departement").equals(departement) ){
				usersDepartement.add(user);
			}
		}
		return usersDepartement;
	}
	private Collection<IWorkflowInstance> getPlanActionAllOrCreator(String catalogName, String workflowName)
	{
		Collection<IWorkflowInstance> collection = null;
		try
		{
			IUser connectedUser = getWorkflowModule().getLoggedOnUser();
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "WIZEORGA", organization);
			IContext context = getWorkflowModule().getLoggedOnUserContext();
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, catalogName, project);
			IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, workflowName);
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			if(!isMembreOfQualiteDirection()){
            	controller.addEqualsConstraint("sys_Creator",connectedUser );
            }
			collection = controller.evaluate(w);
			return collection;
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		
		return null;
		// TODO Auto-generated method stub
	}
	
	private boolean isMembreOfQualiteDirection(){
		try{
			  IContext sysContext = getWorkflowModule().getSysadminContext();
		        IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
				IGroup Qualite = getDirectoryModule().getGroup(sysContext, organization, "ResponsableQHSE");
				IGroup Direction = getDirectoryModule().getGroup(sysContext, organization, "Direction");
				if(getWorkflowModule().getLoggedOnUser().isMemberOf(Qualite, true)||getWorkflowModule().getLoggedOnUser().isMemberOf(Direction, true)){
					return true;
				}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return false;
      
	}
	
	
	private Collection<IWorkflowInstance> getActionsFiltrer(){
		final ArrayList<IWorkflowInstance> arrayList = new ArrayList<IWorkflowInstance>();
		final String[] lien = { "uril://vdoc/workflowContainerView/DefaultOrganization/WIZEORGA/Processus:0/PlanDAction/BarChartPlanParStatut" };
		try {
			String[] array;
			for (int length = (array = lien).length, i = 0; i < length; ++i) {
				final String string = array[i];
				final IView view = (IView) this.getWorkflowModule().getElementByProtocolURI(string);
				final ByteArrayInputStream bais = new ByteArrayInputStream(view.getXmlDefinition());
				final IContext sysContext = this.getWorkflowModule().getSysadminContext();
				final IOrganization organization = this.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
				final IProject project = this.getProjectModule().getProject(sysContext, "WIZEORGA", organization);
				final IViewController viewController = this.getWorkflowModule().getViewController(this.getWorkflowModule().getLoggedOnUserContext(), project, (InputStream) bais);
				final Collection<Object> resources = (Collection<Object>) viewController.evaluate();
				for (final Object iResource : resources) {
					IWorkflowInstance instance = null;
					if (iResource instanceof ActionTaskInstance) {
						instance = (IWorkflowInstance) ((ActionTaskInstance) iResource).getWorkflowInstance();
					} else {
						instance = (IWorkflowInstance) ((com.axemble.vdp.workflow.domain.ProcessWorkflowInstance) iResource);
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
