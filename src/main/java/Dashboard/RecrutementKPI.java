package Dashboard;

import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.activity.domain.ActionTaskInstance;
import com.axemble.vdp.activity.domain.DecisionTaskInstance;
import com.axemble.vdp.utils.StreamUtils;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.codehaus.jettison.json.JSONArray;

public class RecrutementKPI extends BaseController {

    protected static final Logger log = Logger.getLogger(RecrutementKPI.class);
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
            Collection<IWorkflowInstance> mesPostes = getPostes();
            Collection<IWorkflowInstance> mesCandidaturesRetenue = getCandidaturesRetenues();
            Collection<IWorkflowInstance> mesCandidaturesEnEvaluation = getCandidaturesEnEvaluation();
            Collection<IWorkflowInstance> maTODOCandidatures = getTodoCandidature();

            int PostesEnCours = 0;
            int CandidaturesRetenues = 0;
            int CandidaturesEnEvaluation = 0;
            int TodoCandiddatures = 0;
            if(!mesPostes.isEmpty()&&mesPostes.size()>0){
                PostesEnCours = mesPostes.size();
            }
            if(!mesCandidaturesRetenue.isEmpty()&&mesCandidaturesRetenue.size()>0){
                CandidaturesRetenues = mesCandidaturesRetenue.size();
            }
            if(!mesCandidaturesEnEvaluation.isEmpty()&&mesCandidaturesEnEvaluation.size()>0){
                CandidaturesEnEvaluation = mesCandidaturesEnEvaluation.size();
            }
            if(!maTODOCandidatures.isEmpty()&&maTODOCandidatures.size()>0){
                TodoCandiddatures = maTODOCandidatures.size();
            }

           // float congesAcceptee = 0;
            //float congesRefusee = 0;
            //float congesAnnulee = 0;

           // Calendar c = Calendar.getInstance();
            //SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            //c.setTime(df.parse(df.format(c.getTime())));

           /* for (IWorkflowInstance conge : mesConges) {
                if(conge.getValue("DocumentState").equals("Cl�tur�")){
                    congesAcceptee++;
                } else if(conge.getValue("DocumentState").equals("Refus�")){
                    congesRefusee++;
                } else if(conge.getValue("DocumentState").equals("Annul�")){
                    congesAnnulee++;
                } else {
                    congesEnCours ++;
                }
            }*/

            tmp.put("kpi_Postes", PostesEnCours);
            tmp.put("kpi_CandidaturesRetenues", CandidaturesRetenues);
            tmp.put("kpi_CandidaturesEnEvaluation", CandidaturesEnEvaluation);
            tmp.put("kpi_TodoCandidatures", TodoCandiddatures);


            ResultDataArray.put(tmp);

            ResultObject.put("data", ResultDataArray);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ResultObject;
    }





    private Collection<IWorkflowInstance> getPostes()
    {
        Collection<IWorkflowInstance> collection = new ArrayList<>();
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IUser connectedUser = getWorkflowModule().getLoggedOnUser();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Recrutement", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Recrutement", project);
            IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "CreationDUnNouveauPosteAPourvoir_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("DocumentState", "Poste créé");
            collection = controller.evaluate(w);
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return collection;
    }

    private Collection<IWorkflowInstance> getCandidaturesRetenues()
    {
        Collection<IWorkflowInstance> collection = new ArrayList<>();
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
            controller.addEqualsConstraint("EtatDeCvTheque", "Candidature Accepter");
            collection = controller.evaluate(w);
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return collection;
    }

    private Collection<IWorkflowInstance> getCandidaturesEnEvaluation()
    {
        Collection<IWorkflowInstance> collection = new ArrayList<>();
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
             controller.addInConstraint("DocumentState", Arrays.asList("Candidature en évaluation","Evaluation RH"));

            // controller.addInConstraint("DocumentState", "Candidature en évaluation");
            collection = controller.evaluate(w);
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return collection;
    }

    private ArrayList<IWorkflowInstance> getTodoCandidature() {
        final ArrayList<IWorkflowInstance> arrayList = new ArrayList<IWorkflowInstance>();
        final String[] lien = { "uril://vdoc/workflowContainerView/DefaultOrganization/Recrutement/Recrutement:0/Candidature/MaTodo" };
        try {
            String[] array;
            for (int length = (array = lien).length, i = 0; i < length; ++i) {
                final String string = array[i];
                final IView view = (IView) this.getWorkflowModule().getElementByProtocolURI(string);
                final ByteArrayInputStream bais = new ByteArrayInputStream(view.getXmlDefinition());
                final IContext sysContext = this.getWorkflowModule().getSysadminContext();
                final IOrganization organization = this.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                final IProject project = this.getProjectModule().getProject(sysContext, "Recrutement", organization);
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
