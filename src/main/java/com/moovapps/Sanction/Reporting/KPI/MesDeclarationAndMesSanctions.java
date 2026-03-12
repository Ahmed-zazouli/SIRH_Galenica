package com.moovapps.Sanction.Reporting.KPI;

import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.utils.StreamUtils;
import net.sf.ehcache.util.SetAsList;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MesDeclarationAndMesSanctions extends BaseController {

    protected static final Logger log = Logger.getLogger(MesDeclarationAndMesSanctions.class);
    @Override
    public void parseRequest(IExecutionContext.IRequest request) throws IOException {

    }

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
            Collection<IWorkflowInstance> mesDeclarationsEnCours = getMesDeclarationsEncours();
            Collection<IWorkflowInstance> mesDeclarationsConfirme = getMesDeclarationsConfirme();
            Collection<IWorkflowInstance> mesDeclarationsRefuse = getMesDeclarationsRefuse();
            Collection<IWorkflowInstance> mesSanctions = getMesSanctions();


            tmp.put("kpi1_value", mesDeclarationsEnCours.size());
            tmp.put("kpi2_value", mesDeclarationsConfirme.size());
            tmp.put("kpi3_value", mesDeclarationsRefuse.size());
            tmp.put("kpi4_value", mesSanctions.size());

            ResultDataArray.put(tmp);

            ResultObject.put("data", ResultDataArray);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ResultObject;
    }





    private Collection<IWorkflowInstance> getMesDeclarationsEncours()
    {
        Collection<IWorkflowInstance> collection = new ArrayList<>();
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Sanction", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "GestionDesSanctions", project);
            //IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "CreationSanction_1.1");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addInConstraint("sys_WorkflowContainer",Arrays.asList("CreationSanction"));
            controller.addEqualsConstraint("sys_Creator",getWorkflowModule().getLoggedOnUser());
            controller.addNotInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("Sanction confirmée","Demande refusée")));
            controller.addNotEqualsConstraint("DocumentState" , "En cours");
            collection = controller.evaluate(catalog);
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return collection;
    }

    private Collection<IWorkflowInstance> getMesDeclarationsConfirme()
    {
        Collection<IWorkflowInstance> collection = new ArrayList<>();
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Sanction", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "GestionDesSanctions", project);
            //IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "CreationSanction_1.1");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("sys_Creator",getWorkflowModule().getLoggedOnUser());
            controller.addInConstraint("sys_WorkflowContainer",Arrays.asList("CreationSanction"));
            //controller.addInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("Sanction confirmée","Demande refusée")));
            controller.addEqualsConstraint("DocumentState" , "Sanction confirmée");
            collection = controller.evaluate(catalog);
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return collection;
    }
    private Collection<IWorkflowInstance> getMesDeclarationsRefuse()
    {
        Collection<IWorkflowInstance> collection = new ArrayList<>();
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Sanction", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "GestionDesSanctions", project);
            //IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "CreationSanction_1.1");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addInConstraint("sys_WorkflowContainer",Arrays.asList("CreationSanction"));
            controller.addEqualsConstraint("sys_Creator",getWorkflowModule().getLoggedOnUser());
            //controller.addInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("Sanction confirmée","Demande refusée")));
            controller.addEqualsConstraint("DocumentState" , "Demande refusée");
            collection = controller.evaluate(catalog);
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return collection;
    }

    private Collection<IWorkflowInstance> getMesSanctions()
    {
        Collection<IWorkflowInstance> collection = new ArrayList<>();
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Sanction", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "GestionDesSanctions", project);
            //IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "CreationSanction_1.1");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addInConstraint("sys_WorkflowContainer",Arrays.asList("CreationSanction"));
            controller.addEqualsConstraint("PersonneConcernee",getWorkflowModule().getLoggedOnUser());
            controller.addNotInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("Sanction confirmée","Demande refusée")));

            //controller.addEqualsConstraint("isNew", true);
            collection = controller.evaluate(catalog);
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return collection;
    }




}
