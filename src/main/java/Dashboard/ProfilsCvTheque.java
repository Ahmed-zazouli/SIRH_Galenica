package Dashboard;

import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.utils.StreamUtils;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ProfilsCvTheque extends BaseController{
    protected static final Logger log = Logger.getLogger(ProfilsCvTheque.class);
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
    protected IExecutionContext buildResponse(IExecutionContext ec, BaseController.Result result) throws IOException {
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

            listFiltres.add("NiveauEtude");


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
                //  String statut = " ";

                if(Candidature.getValue("NiveauDEtude")!=null){
                    tmp.put("NiveauEtude", ((IStorageResource) Candidature.getValue("NiveauDEtude")).getValue("sys_Title"));
                }
                if(tmp.get("NiveauEtude")!=null){
                    ResultDataArray.put(tmp);
                }


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
