package Dashboard;

import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext;
import com.axemble.vdp.activity.domain.ActionTaskInstance;
import com.axemble.vdp.activity.domain.DecisionTaskInstance;
import com.axemble.vdp.utils.StreamUtils;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

import org.codehaus.jettison.json.JSONArray;

public class CandidaturesParEtape extends BaseController {
    protected static final Logger log = Logger.getLogger(CandidaturesParEtape.class);
    @Override
    public void parseRequest(IExecutionContext.IRequest arg0) throws IOException {}

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

            listFiltres.add("Etape");
           // listFiltres.add("Demandeur");
            //listFiltres.add("Cat�gorie");
            //listFiltres.add("Date cong�");
            //listFiltres.add("Statut");

            FiltreObject.put("lesFiltres", listFiltres);
            ResultObject.putAll(FiltreObject);

            Collection<IWorkflowInstance> Candidatures =  getData();
           // SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM-yy");
            for (IWorkflowInstance Candidature : Candidatures)
            {

                JSONObject tmp = new JSONObject();
                tmp.put("Etape", (String)Candidature.getValue("sys_CurrentSteps"));
                ResultDataArray.put(tmp);
            }

            ResultObject.put("data", ResultDataArray);
        }
        catch (Exception e)
        {
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
            IProject project = getProjectModule().getProject(sysContext, "Recrutement", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Recrutement", project);
            IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "Candidature_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
           // controller.addEqualsConstraint("Demandeur", connectedUser);
            controller.addNotInConstraint("DocumentState", Arrays.asList("En cours","Terminé","Clôturée"));
            collection = controller.evaluate(w);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return collection;
    }
}
