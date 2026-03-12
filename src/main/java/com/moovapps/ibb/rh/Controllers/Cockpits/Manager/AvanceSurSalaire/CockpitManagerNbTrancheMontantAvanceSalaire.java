package com.moovapps.ibb.rh.Controllers.Cockpits.Manager.AvanceSurSalaire;

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
import java.util.List;

public class CockpitManagerNbTrancheMontantAvanceSalaire extends BaseController{

    protected static final Logger log = Logger.getLogger(CockpitManagerNbTrancheMontantAvanceSalaire.class);

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

            listFiltres.add("Société");
            listFiltres.add("Bénéficiaire");
            listFiltres.add("Departement");
            listFiltres.add("Statut");

            FiltreObject.put("lesFiltres", listFiltres);
            ResultObject.putAll(FiltreObject);

            Collection<IWorkflowInstance> demandesAvances =  getData();

            for (IWorkflowInstance demandesAvance : demandesAvances)
            {
                String societeName = null;
                IUser beneficiaire = (IUser)demandesAvance.getValue("Beneficiaire");
                if(demandesAvance.getValue("Societe") != null){
                    societeName = (String)((IStorageResource) beneficiaire.getExtendedAttributes().getValue("Societe")).getValue("sys_Title");
                }
                beneficiaire = (IUser)demandesAvance.getValue("Beneficiaire");
                String departement = "N/D";
                if(beneficiaire.getExtendedAttributes().getValue("Departement") != null){
                    departement = (String)((IStorageResource)beneficiaire.getExtendedAttributes().getValue("Departement")).getValue("sys_Title");
                }
                JSONObject tmp = new JSONObject();
                tmp.put("Société", societeName);
                tmp.put("Bénéficiaire", ((IUser)demandesAvance.getValue("Beneficiaire")).getFullName());
                tmp.put("Departement", departement);
                tmp.put("Statut", demandesAvance.getText("DocumentState"));
                tmp.put("Montant", demandesAvance.getValue("Montant"));
                //tmp.put("Tranche salaire", demandesAvance.getValue("TrancheSalaire"));

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
        Collection<IWorkflowInstance> collection = null;
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IUser connectedUser = getWorkflowModule().getLoggedOnUser();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "GestionDesAvances", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "GestionDesAvances", project);
            IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "GestionDesAvances");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("SuperieurHierarchique", connectedUser);
            controller.addNotEqualsConstraint("DocumentState", "En cours");
            controller.addNotEqualsConstraint("DocumentState", "Refusée");

            //controller.addNotEqualsConstraint("DocumentState", "Refusée");

            collection = controller.evaluate(w);
            return collection;
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
        return null;
    }

}
