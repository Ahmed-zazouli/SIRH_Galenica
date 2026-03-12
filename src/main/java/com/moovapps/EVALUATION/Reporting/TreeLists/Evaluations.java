package com.moovapps.EVALUATION.Reporting.TreeLists;

import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.impl.ProcessViewController;
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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class Evaluations extends BaseController {
    protected static final Logger log = Logger.getLogger(Evaluations.class);

    @Override
    public void parseRequest(IExecutionContext.IRequest arg0) throws IOException {
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
            OutputStreamWriter osw = new OutputStreamWriter(output, StandardCharsets.UTF_8);
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
        Collection<IWorkflowInstance>  evals = evals();
        try{
            if(evals!=null && !evals.isEmpty()){
                for(IWorkflowInstance eval : evals){
                    JSONObject evalObject = new JSONObject();
                    evalObject.put("exercice",   eval.getValue("ExerciceEvaluation")!=null?((Number) ((IStorageResource) eval.getValue("ExerciceEvaluation")).getValue("Annee")).intValue():"N/A");
                    evalObject.put("profil", eval.getValue("Profil2")!=null?((IStorageResource) eval.getValue("Profil2")).getValue("sys_Title"):"N/A");
                    evalObject.put("responsable",eval.getValue("Evaluateur")!=null?((IUser) eval.getValue("Evaluateur")).getFullName():"N/A");
                    evalObject.put("etape", eval.getValue("sys_CurrentSteps")!=null?eval.getValue("sys_CurrentSteps"):"Cloturé");
                    evalObject.put("collaborateur", eval.getValue("CollaborateurEval")!=null?((IUser) eval.getValue("CollaborateurEval")).getFullName():"N/A");
                    evalObject.put("dateCreation", eval.getValue("sys_CreationDate"));
                    evalObject.put("etatDEvaluation", eval.getValue("EtatDEvaluation")!=null?eval.getValue("EtatDEvaluation"):"N/A");
                    evalObject.put("atraiterPar", eval.getValue("sys_CurrentActors")!=null?eval.getValue("sys_CurrentActors"):"N/A");
                    ResultDataArray.put(evalObject);
                }
            }
            ResultObject.put("data", ResultDataArray);
        }catch (Exception e){
            e.printStackTrace();
        }



        return ResultObject;
    }

    Collection<IWorkflowInstance> getEvaluatorEvals(Collection<IWorkflowInstance> evals , IUser evaluator){
        Collection<IWorkflowInstance> evalautorEvals = new ArrayList<>();
        for(IWorkflowInstance eval  : evals){
            if(eval.getValue("Evaluateur")==evaluator){
                evalautorEvals.add(eval);
            }
        }
        return evalautorEvals;
    }

    private void fillHelperMap(JSONObject helperMap, IWorkflowInstance document, IWorkflowInstance processus, IWorkflowInstance sousProcessus) {
//        IStorageResource processus = document.getValue("Processus3") != null ? (IStorageResource) document.getValue("Processus3") : null;
//        IStorageResource sousProcessus = document.getValue("Processus2") != null ? (IStorageResource) document.getValue("Processus2") : null;
        processus = processus == null && document != null ? (IWorkflowInstance) document.getValue("Processus5") : processus;
        sousProcessus = sousProcessus == null && document != null ? (IWorkflowInstance) document.getValue("SousProcessus2") : sousProcessus;
        JSONObject tmpHelper = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (processus != null && !helperMap.containsKey(processus.getId().toString())) {
            tmpHelper.put("Statut", processus.getValue("DocumentState"));
            tmpHelper.put("DateCreation", processus.getValue("sys_CreationDate") != null ? sdf.format(processus.getValue("sys_CreationDate")) : null);
            tmpHelper.put("DateAutorisation", processus.getValue("DateDAutorisation") != null ? sdf.format(processus.getValue("DateDAutorisation")) : null);
            tmpHelper.put("DateTraitement", processus.getValue("DateDeTraitement") != null ? sdf.format(processus.getValue("DateDeTraitement")) : null);
            tmpHelper.put("DateValidation", processus.getValue("DateDeValidation") != null ? sdf.format(processus.getValue("DateDeValidation")) : null);
            tmpHelper.put("DateDernierMiseAJour", processus.getValue("Date_Ajour") != null ? sdf.format(processus.getValue("Date_Ajour")) : null);
            tmpHelper.put("Avancement", 100);
            if (processus.getValue("Pilote") != null) {
                ArrayList<JSONObject> acteur = new ArrayList<>();
                IUser pilote = (IUser) processus.getValue("Pilote");
                JSONObject o = new JSONObject();
                o.put("name", pilote.getFullName());
                o.put("img", pilote.getAvatar() != null ? "/moovapps" + pilote.getAvatar().getURI() : "/moovapps/easysite-resources/skins/workplace/img/default-avatar.svg");
                acteur.add(o);
                tmpHelper.put("Acteur", acteur);
            }
            helperMap.put(processus.getId().toString(), tmpHelper);
        }

        if (sousProcessus != null && !helperMap.containsKey(sousProcessus.getId().toString())) {
            tmpHelper = new JSONObject();
            tmpHelper.put("Statut", sousProcessus.getValue("DocumentState"));
            tmpHelper.put("DateCreation", sousProcessus.getValue("sys_CreationDate") != null ? sdf.format(sousProcessus.getValue("sys_CreationDate")) : null);
            tmpHelper.put("DateAutorisation", sousProcessus.getValue("DateDAutorisation") != null ? sdf.format(sousProcessus.getValue("DateDAutorisation")) : null);
            tmpHelper.put("DateTraitement", sousProcessus.getValue("DateDeTraitement") != null ? sdf.format(sousProcessus.getValue("DateDeTraitement")) : null);
            tmpHelper.put("DateValidation", sousProcessus.getValue("DateDeValidation") != null ? sdf.format(sousProcessus.getValue("DateDeValidation")) : null);
            tmpHelper.put("DateDernierMiseAJour", sousProcessus.getValue("Date_Ajour") != null ? sdf.format(sousProcessus.getValue("Date_Ajour")) : null);
            tmpHelper.put("Avancement", 100);
            if (sousProcessus.getValue("Pilote") != null) {
                ArrayList<JSONObject> acteur = new ArrayList<>();
                IUser pilote = (IUser) sousProcessus.getValue("Pilote");
                JSONObject o = new JSONObject();
                o.put("name", pilote.getFullName());
                o.put("img", pilote.getAvatar() != null ? "/moovapps" + pilote.getAvatar().getURI() : "/moovapps/easysite-resources/skins/workplace/img/default-avatar.svg");
                acteur.add(o);
                tmpHelper.put("Acteur", acteur);
            }
            helperMap.put(sousProcessus.getId().toString(), tmpHelper);
        }

    }

    private ArrayList<IWorkflowInstance> getInstances(String module, String reference) {

        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "WIZECONTROLE", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RisquesOperationnelles", project);
            IWorkflow w = null;
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            if (module.equalsIgnoreCase("Contrôle")) {
                w = getWorkflowModule().getWorkflow(sysContext, catalog, "EXECUTIONDESCONTROLES_1.0");
            } else if (module.equalsIgnoreCase("Remontée périodique")) {
                w = getWorkflowModule().getWorkflow(sysContext, catalog, "INSTANCESREMONTEESCONFORMITE_1.0");
                controller.addEqualsConstraint("DocumentState", "Validation");
            }
            controller.addEqualsConstraint("ParentReference", reference);
//			controller.addInConstraint("EXERCICEMOIS", Arrays.asList(periode,periodeMinus1));
            return (ArrayList<IWorkflowInstance>) controller.evaluate(w);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Collection<IWorkflowInstance> getCampagnesClotures() {
        Collection<IWorkflowInstance> collection = Collections.emptyList();
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "EvalutionCollaborateur", project);
            IWorkflow workflow = getWorkflowModule().getWorkflow(sysContext,catalog,"ApplicationCompagne_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("DocumentState", "Compagne clôturée");
            collection = controller.evaluate(workflow);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collection;
    }

    private Collection<IWorkflowInstance> getDocumentsBySousProcessus(IWorkflowInstance sousProcessus) {
//        IStorageResource procStorage = getProcStorage(proc);
        Collection<IWorkflowInstance> collection = Collections.emptyList();
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "WIZEORGA", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Processus", project);
//			IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "DOCUMENTSADMINISTRATIFSINTERNES_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addInConstraint("sys_WorkflowContainer", Arrays.asList("Cr_ProceduresAssimilees"));

            controller.addEqualsConstraint("SousProcessus2", sousProcessus);
//            controller.addEqualsConstraint("Processus5", sousProcessus.getValue("Processus5"));
            //controller.addEqualsConstraint("StatutDeroulement", "Clôturé");
            // controller.addEqualsConstraint("StatutDeroulement", "En cours");
            // controller.addInConstraint("DocumentState", Arrays.asList("Crée"));
            collection = controller.evaluate(catalog);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collection;
    }

    private Collection<IWorkflowInstance> getProcessusWithNoSousProcessus(HashSet<String> idProcessus) {
        Collection<IWorkflowInstance> collection = Collections.emptyList();
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "WIZEORGA", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Processus", project);
//			IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "DOCUMENTSADMINISTRATIFSINTERNES_1.0");
            ProcessViewController controller = (ProcessViewController) getWorkflowModule().getViewController(sysContext);
            controller.addInConstraint("sys_WorkflowContainer", Arrays.asList("Cr_Processus"));
            controller.addEqualsConstraint("Categorie2.sys_Title", "Processus");
            controller.addNotInConstraint("sys_Reference", idProcessus);
            controller.addEqualsConstraint("StatutDeroulement", "Clôturé");

            // controller.addEqualsConstraint("StatutDeroulement", "En cours");
            // controller.addInConstraint("DocumentState", Arrays.asList("Crée"));
            collection = controller.evaluate(catalog);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return collection;
    }

    IStorageResource getProcStorage(IWorkflowInstance proc) {
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "WIZEORGA", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition processusDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Processus");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            controller.addEqualsConstraint("ID", proc.getId().toString());
            if (controller.evaluate(processusDefinition) != null && !controller.evaluate(processusDefinition).isEmpty()) {
                return (IStorageResource) controller.evaluate(processusDefinition).iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    Collection<IWorkflowInstance> evals (){
        Collection<IWorkflowInstance> evals = new ArrayList<>();
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "EvalutionCollaborateur", project);
            IWorkflow workflow = getWorkflowModule().getWorkflow(sysContext,catalog,"ApplicationEntretienDEvaluation_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
           // controller.addNotEqualsConstraint("EtatDEvaluation", "Evaluation clôturée");
            evals = controller.evaluate(workflow);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return evals;
    }

}
