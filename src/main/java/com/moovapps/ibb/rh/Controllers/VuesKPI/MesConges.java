package com.moovapps.ibb.rh.Controllers.VuesKPI;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

public class MesConges extends BaseController {
	
	protected static final Logger log = Logger.getLogger(MesConges.class);

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
			JSONObject tmp = new JSONObject();
			
//			tmp.put("kpi2_value", df.format(calculateJoursMoyenRetard(premiumMembers)));
			Collection<IWorkflowInstance> mesConges = getData();
			float congesEnCours = 0;
			float congesAcceptee = 0;
			float congesRefusee = 0;
			float congesAnnulee = 0;
						
			Calendar c = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			c.setTime(df.parse(df.format(c.getTime())));
		
			for (IWorkflowInstance conge : mesConges) {
				if(conge.getValue("DocumentState").equals("Clôturé")){
					congesAcceptee++;
				} else if(conge.getValue("DocumentState").equals("Refusé")){
					congesRefusee++;
				} else if(conge.getValue("DocumentState").equals("Annulé")){
					congesAnnulee++; 
				} else {
					congesEnCours ++;
				}
			}
			
			tmp.put("Svalue1", congesEnCours);
			tmp.put("Svalue2", congesAcceptee);
			tmp.put("Svalue3", congesRefusee); 
			//tmp.put("Svalue4", congesAnnulee);
			
			ResultDataArray.put(tmp);
	
			ResultObject.put("data", ResultDataArray);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return ResultObject;
	}
	
	private String fromMinutesToDaysHoursMinutes(float dureeInMinutes, IStorageResource societe) { 
		return durationToStringIncludingDays(((Number)dureeInMinutes).longValue(), societe);
	}
	
	public String durationToStringIncludingDays(long durationInMinutes, IStorageResource societe){
		double numberOfWorkingHours = 480;
		if(societe != null && societe.getValue("durationWorkHours") != null){
			numberOfWorkingHours = ((Number)societe.getValue("durationWorkHours")).doubleValue();
		}
		long days = (((Number)durationInMinutes).longValue()) / (((Number)numberOfWorkingHours).longValue());
		long hours = (durationInMinutes - (days * ((Number)numberOfWorkingHours).longValue())) / 60;
		long minutes = durationInMinutes % 60;
		return String.format("%02d", days)+"j " + String.format("%02d", hours)+"h " + String.format("%02d", minutes)+"min";
	}
	
	private Collection<IWorkflowInstance> getData()
	{
		Collection<IWorkflowInstance> collection = new ArrayList<>();
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
			controller.addNotEqualsConstraint("TypeDeConge", "Absence");
			controller.addNotEqualsConstraint("DocumentState", "En cours");
			collection = controller.evaluate(w);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		return collection;
	}
}