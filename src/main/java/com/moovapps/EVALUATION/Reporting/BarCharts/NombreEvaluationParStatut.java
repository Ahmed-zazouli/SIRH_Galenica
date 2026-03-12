package com.moovapps.EVALUATION.Reporting.BarCharts;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NombreEvaluationParStatut extends BaseController {
    protected static final Logger log = Logger.getLogger(NombreEvaluationParStatut.class);

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
            //  String cli_id = ec.getRequest().getParameter("cli_id");
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

    @Override
    public void parseRequest(IExecutionContext.IRequest request) throws IOException {

    }

    //get data
    //for item : data
    //get date de creation systéme
    //get mois as an integer
    //set data


    ArrayList<IWorkflowInstance> getEvaluations(){
        ArrayList<IWorkflowInstance> evaluations = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");

            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "EvalutionCollaborateur", ICatalog.IType.NORMAL,project);
            IWorkflow sanctionWorkflow = getWorkflowModule().getWorkflow(sysContext,catalog,"ApplicationEntretienDEvaluation_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            evaluations =   (ArrayList<IWorkflowInstance>) controller.evaluate(sanctionWorkflow);

        } catch (Exception e) {
            e.printStackTrace();

        }
        return evaluations;

    }


    private JSONObject BuildData() {

        JSONArray ResultDataArray = new JSONArray();
        JSONObject ResultObject = new JSONObject();
        ArrayList<IWorkflowInstance> evaluations = getEvaluations();
        try {
            List<String> listFiltres = new ArrayList<>();
            JSONObject FiltreObject = new JSONObject();

            listFiltres.add("Etat évaluation");
            listFiltres.add("Exercice évaluation");
           // listFiltres.add("Etablissement");
            listFiltres.add("Evaluateur");
            listFiltres.add("Collaborateur");
            listFiltres.add("Etape");


            FiltreObject.put("lesFiltres", listFiltres);
            ResultObject.putAll(FiltreObject);
          /*  HashMap<Integer,String> moisMap = new HashMap<>();
            moisMap.put(0,"Jan");
            moisMap.put(1,"Fév");
            moisMap.put(2,"Mars");
            moisMap.put(3,"Avr");
            moisMap.put(4,"Mai");
            moisMap.put(5,"Juin");
            moisMap.put(6,"Juill");
            moisMap.put(7,"Aou");
            moisMap.put(8,"Sep");
            moisMap.put(9,"Oct");
            moisMap.put(10,"Nov");
            moisMap.put(11,"Déc");*/

            //  HashMap<String,Integer> identique = new HashMap<>();

            // { "Jan", "Fév", "Mars", "Avr", "Mai", "Juin", "Juill", "Aou", "Sep", "Oct", "Nov", "Déc" };
            //  Calendar c = Calendar.getInstance();
            for (IWorkflowInstance evaluation : evaluations) {
                String EtatDEvaluation = evaluation.getValue("EtatDEvaluation")!=null? (String) evaluation.getValue("EtatDEvaluation"):"A débuter";
                int ExerciceDEvaluation =  evaluation.getValue("ExerciceEvaluation")!=null?((Number) ((IStorageResource) evaluation.getValue("ExerciceEvaluation")).getValue("Annee")).intValue():-1;
                String Evaluateur = evaluation.getValue("Evaluateur")!=null?  ((IUser) evaluation.getValue("Evaluateur")).getFullName():"N/A";
                String Collaborateur2 = evaluation.getValue("CollaborateurEval")!=null?   ((IUser) evaluation.getValue("CollaborateurEval")).getFullName():"N/A";
                String Etape =evaluation.getValue("sys_CurrentSteps")!=null? (String) evaluation.getValue("sys_CurrentSteps"):"Clôturée";

                JSONObject tmp = new JSONObject();
                tmp.put("Etat évaluation", EtatDEvaluation);
                tmp.put("Exercice évaluation", ExerciceDEvaluation);
                //tmp.put("Etablissement", Etablissement);
                tmp.put("Evaluateur", Evaluateur);
                tmp.put("Collaborateur", Collaborateur2);
                tmp.put("Etape", Etape);
                //tmp.put("Nbr", 1);
                ResultDataArray.put(tmp);

            }






            ResultObject.put("data", ResultDataArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResultObject;
    }


    private ArrayList<IWorkflowInstance> getActions(int year,int mois){
        try{
//CreationActionRecouvrement_1.0
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "WIZEDSO", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,"WIZEDSO", ICatalog.IType.NORMAL, project);
            IWorkflow definition = getWorkflowModule().getWorkflow(context, catalog, "CreationActionRecouvrement_1.0");
            IViewController controller = getWorkflowModule().getViewController(context);
            controller.addEqualsConstraint("mois", mois);
            controller.addEqualsConstraint("year", year);

            if(controller.evaluate(definition)!=null&&!controller.evaluate(definition).isEmpty()){
                return (ArrayList<IWorkflowInstance>) controller.evaluate(definition);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }


    private ArrayList<IWorkflowInstance> getInterventionClientCloture(){
        ArrayList<IWorkflowInstance> data = null;
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IUser connectedUser = getWorkflowModule().getLoggedOnUser();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "WIZEDSO", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "WIZEDSO", ICatalog.IType.NORMAL,project);
            IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "CreationActionRecouvrement_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            IOrganization parametrageOrganisation = getDirectoryModule().getOrganization(sysContext, "OrganisationDeParametrage");
            IGroup agentGroupe = getDirectoryModule().getGroup(sysContext,parametrageOrganisation,"AgentsDeRecouvrement");
            IGroup ResponsableAgenceGroupe = getDirectoryModule().getGroup(sysContext,parametrageOrganisation,"ResponsableAgence");
            if(connectedUser.isMemberOf(agentGroupe,true)){
                controller.addEqualsConstraint("Client.AgentDeRecouvrement", connectedUser);

            }
            // responsable agence
            else if(connectedUser.isMemberOf(ResponsableAgenceGroupe,true)){
                controller.addEqualsConstraint("Client.Agence.ResponsableAgence", connectedUser);

            }
            // controller.addEqualsConstraint("ResponsableAction2", connectedUser);
            controller.addEqualsConstraint("NatureAction.sys_Title", "Intervention client");
            controller.addEqualsConstraint("DocumentState", "Action clôturée");

            Calendar monthStart = Calendar.getInstance();
            monthStart.setTime(new Date());
            monthStart.set(Calendar.MONTH, monthStart.getActualMinimum(Calendar.MONTH));
            monthStart.set(Calendar.DATE, monthStart.getActualMinimum(Calendar.DATE));


            Calendar monthEnd = Calendar.getInstance();
            monthEnd.setTime(new Date());
            monthEnd.set(Calendar.MONTH, Calendar.MONTH - 1);
            monthEnd.set(Calendar.DATE, monthStart.getActualMaximum(Calendar.DATE));

            controller.addGreaterOrEqualConstraint("sys_CreationDate", monthStart.getTime());
            controller.addLessOrEqualsConstraint("sys_CreationDate", monthEnd.getTime());

            data = (ArrayList<IWorkflowInstance>) controller.evaluate(w);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return data;
    }

    private ArrayList<IWorkflowInstance> getInterventionClientCree(){
        ArrayList<IWorkflowInstance> data = null;
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IUser connectedUser = getWorkflowModule().getLoggedOnUser();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "WIZEDSO", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "WIZEDSO", ICatalog.IType.NORMAL,project);
            IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "CreationActionRecouvrement_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            IOrganization parametrageOrganisation = getDirectoryModule().getOrganization(sysContext, "OrganisationDeParametrage");
            IGroup agentGroupe = getDirectoryModule().getGroup(sysContext,parametrageOrganisation,"AgentsDeRecouvrement");
            IGroup ResponsableAgenceGroupe = getDirectoryModule().getGroup(sysContext,parametrageOrganisation,"ResponsableAgence");
            if(connectedUser.isMemberOf(agentGroupe,true)){
                controller.addEqualsConstraint("Client.AgentDeRecouvrement", connectedUser);

            }
            // responsable agence
            else if(connectedUser.isMemberOf(ResponsableAgenceGroupe,true)){
                controller.addEqualsConstraint("Client.Agence.ResponsableAgence", connectedUser);

            }
            //controller.addEqualsConstraint("ResponsableAction2", connectedUser);
            controller.addEqualsConstraint("NatureAction.sys_Title", "Intervention client");
            controller.addNotEqualsConstraint("DocumentState", "À soumettre");

            Calendar monthStart = Calendar.getInstance();
            monthStart.setTime(new Date());
            monthStart.set(Calendar.MONTH, monthStart.getActualMinimum(Calendar.MONTH));
            monthStart.set(Calendar.DATE, monthStart.getActualMinimum(Calendar.DATE));


            Calendar monthEnd = Calendar.getInstance();
            monthEnd.setTime(new Date());
            monthEnd.set(Calendar.MONTH, Calendar.MONTH - 1);
            monthEnd.set(Calendar.DATE, monthStart.getActualMaximum(Calendar.DATE));

            controller.addGreaterOrEqualConstraint("sys_CreationDate", monthStart.getTime());
            controller.addLessOrEqualsConstraint("sys_CreationDate", monthEnd.getTime());

            data = (ArrayList<IWorkflowInstance>) controller.evaluate(w);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return data;
    }

    private ArrayList<IStorageResource>  getObjectifs() {
        ArrayList<IStorageResource> objectif = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IUser connectedUser = getWorkflowModule().getLoggedOnUser();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IOrganization parametrageOrganisation = getDirectoryModule().getOrganization(sysContext, "OrganisationDeParametrage");

            IProject project = getProjectModule().getProject(sysContext, "WIZEDSO", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE,project);
            IResourceDefinition objectifDefinition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"ObjectifsRecouvrement");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            IGroup agentGroupe = getDirectoryModule().getGroup(sysContext,parametrageOrganisation,"AgentsDeRecouvrement");
            IGroup ResponsableAgenceGroupe = getDirectoryModule().getGroup(sysContext,parametrageOrganisation,"ResponsableAgence");
            // if agent and responsabel ??
            //agentGroupe
            if(connectedUser.isMemberOf(agentGroupe,true)){
                controller.addEqualsConstraint("AgentDeRecouvrement", connectedUser);

            }
            // responsable agence
            else if(connectedUser.isMemberOf(ResponsableAgenceGroupe,true)){
                controller.addEqualsConstraint("Agence.ResponsableAgence", connectedUser);

            }
            if(controller.evaluate(objectifDefinition)!=null && !controller.evaluate(objectifDefinition).isEmpty()){
                objectif = (ArrayList<IStorageResource>) controller.evaluate(objectifDefinition);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return objectif;
    }
}
