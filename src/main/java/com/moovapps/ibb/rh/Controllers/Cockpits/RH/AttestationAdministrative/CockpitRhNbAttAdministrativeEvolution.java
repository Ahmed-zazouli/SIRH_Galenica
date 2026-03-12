package com.moovapps.ibb.rh.Controllers.Cockpits.RH.AttestationAdministrative;

import com.axemble.commons.utils.HTTPUtils;
import com.axemble.vdoc.sdk.controllers.BaseController;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext;
import com.axemble.vdoc.sdk.interfaces.runtime.IExecutionContext.IRequest;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.utils.StreamUtils;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class CockpitRhNbAttAdministrativeEvolution extends BaseController {

    private class GroupModel{
        String societe = null;
        Object manager = null;
        IUser salarie = null;
        Object motif = null;
        String statut = null;
        String mois = null;

        public GroupModel(String societe, Object manager, IUser beneficiaire, Object motif, String statut, String mois){
            this.societe = societe;
            this.manager = manager;
            this.salarie = beneficiaire;
            this.statut = statut;
            this.motif = motif;
            this.mois = mois;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof GroupModel))
                return false;
            if (obj == this)
                return true;

            GroupModel otherGroup = (GroupModel) obj;

            return (societe.equals(otherGroup.societe) &&
                    manager == otherGroup.manager &&
                    salarie == otherGroup.salarie &&
                    motif.equals(otherGroup.motif) &&
                    statut.equals(otherGroup.statut) &&
                    mois.equals(otherGroup.mois));
        }

        @Override
        public int hashCode() {
            return Objects.hash(societe, manager, salarie, motif, statut, mois);
        }
    }

    protected static final Logger log = Logger.getLogger(CockpitRhNbAttAdministrativeEvolutionParStatut.class);

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
        JSONObject ResultObject = new JSONObject();
        try
        {
            List<String> listFiltres = new ArrayList<>();
            JSONObject FiltreObject = new JSONObject();

            listFiltres.add("Société");
            listFiltres.add("Manager");
            listFiltres.add("Salarié");
            listFiltres.add("Motif");
            listFiltres.add("Statut");
            listFiltres.add("Mois");

            FiltreObject.put("lesFiltres", listFiltres);
            ResultObject.putAll(FiltreObject);

            HashMap<String, IStorageResource> societies = new HashMap<String,IStorageResource>();

            Collection<IWorkflowInstance> demandesAttestation =  getData();
            HashMap<GroupModel, JSONObject> evolutionMap = new LinkedHashMap<GroupModel, JSONObject>();

            for (IWorkflowInstance attestation : demandesAttestation)
            {
                String societeName = null;
                IUser connectedUser = getWorkflowModule().getLoggedOnUser();
                if(attestation.getValue("Societe") != null){
                    //societeName = (String)attestation.getValue("Societe");
                    societeName = (String)((IStorageResource) connectedUser.getExtendedAttributes().getValue("Societe")).getValue("sys_Title");
                }
                if(!societies.containsKey(societeName)){
                    IStorageResource societe = (IStorageResource) connectedUser.getExtendedAttributes().getValue("Societe");
                    societies.put(societeName, societe);
                    //societies.put(societeName, getSociete(societeName));
                }
                String departement = null ;
                if (connectedUser.getExtendedAttributes().getValue("Departement") != null){
                    departement = (String)((IStorageResource) connectedUser.getExtendedAttributes().getValue("Departement")).getValue("sys_Title");

                }
                Date moisDate = (Date)attestation.getValue("sys_CreationDate");
                Calendar x = Calendar.getInstance();
                x.setTime(moisDate);

                GroupModel tmpGroupModel = new GroupModel(societeName,
                        attestation.getValue("SuperieurHierarchique"),
                        (IUser)attestation.getValue("Demandeur2"),
                        attestation.getValue("MotifDemandeAttestation"),
                        (String)attestation.getText("DocumentState"),
                        String.valueOf(x.get(Calendar.MONTH) + 1));

                if(!evolutionMap.containsKey(tmpGroupModel)){
                    for (int i = 1; i < 13; i++) {
                        JSONObject tmp = new JSONObject();
                        tmp.put("Société", departement);
                        tmp.put("Manager", attestation.getValue("SuperieurHierarchique")!= null ? ((IUser)attestation.getValue("SuperieurHierarchique")).getFullName(): "N/D");
                        tmp.put("Salarié", ((IUser)attestation.getValue("Demandeur2")).getFullName());
                        tmp.put("Motif", attestation.getValue("MotifDemandeAttestation") != null ? attestation.getValue("MotifDemandeAttestation") : "N/D");
                        tmp.put("Statut", attestation.getText("DocumentState"));
                        tmp.put("Mois", String.valueOf(i));
                        tmp.put("Nb", 0);
                        GroupModel tmpG = new GroupModel(societeName,
                                attestation.getValue("SuperieurHierarchique"),
                                (IUser)attestation.getValue("Demandeur2"),
                                attestation.getValue("MotifDemandeAttestation"),
                                (String)attestation.getText("DocumentState"),
                                String.valueOf(i));
//						tmpGroupModel.mois = String.valueOf(i);
                        evolutionMap.put(tmpG, tmp);
                    }
                }



//				tmpGroupModel.mois = String.valueOf(x.get(Calendar.MONTH) + 1);

                JSONObject mapObject = evolutionMap.get(tmpGroupModel);

                mapObject.put("Nb", (int)mapObject.get("Nb") + 1);

                evolutionMap.put(tmpGroupModel, mapObject);
            }

            ResultObject.put("data", evolutionMap.values());
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
            IProject project = getProjectModule().getProject(sysContext, "AttestationDeSalaire", organization);
//			IContext context = getWorkflowModule().getLoggedOnUserContext();
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "RH", project);
            IWorkflowContainer w = getWorkflowModule().getWorkflowContainer(sysContext, catalog, "DOCUMENTSADMINISTRATIFSINTERNES");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
            controller.addNotEqualsConstraint("DocumentState", "En cours");
            controller.addNotEqualsConstraint("DocumentState", "Refusée");

            collection = controller.evaluate(w);
            return collection;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public IStorageResource getSociete(String societeName) {
        IStorageResource societe = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",getDirectoryModule().getOrganization(context, "DefaultOrganization"));
            ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
            controller.addEqualsConstraint("sys_Title", societeName);
            Collection<IStorageResource> societes = controller.evaluate(definition);
            if (!societes.isEmpty()){
                societe = societes.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return societe;
    }
}
