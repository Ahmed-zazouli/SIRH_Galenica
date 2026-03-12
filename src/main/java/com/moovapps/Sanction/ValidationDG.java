package com.moovapps.Sanction;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.Sanction.Helpers.HistoriqueHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class ValidationDG extends BaseDocumentExtension {
    HistoriqueHelper helper = new HistoriqueHelper();

    @Override
    public boolean onAfterSubmit(IAction action) {
        if(action.getName().equals("Valider")){
            if(getWorkflowInstance().getValue("SanctionToUpdate")!=null){
                IWorkflowInstance sanctionToUpdate = (IWorkflowInstance) getWorkflowInstance().getValue("SanctionToUpdate");
                IStorageResource historiqueSanction = historiserSanctionHistorique(sanctionToUpdate);
                historiqueSanction.save(getWorkflowModule().getSysadminContext());

                HashMap<String,String> fields = getSanctionReferentielFieldsWithoutDate1Declaration();
                helper.UpdateRessource(sanctionToUpdate,getWorkflowInstance(),fields);
                int nombreEvolution =sanctionToUpdate.getValue("NombreDEvolution")!=null? ((Number) sanctionToUpdate.getValue("NombreDEvolution")).intValue():0;
                nombreEvolution ++;
                sanctionToUpdate.setValue("NombreDEvolution",nombreEvolution);

                ArrayList<IUser> declarants = (ArrayList<IUser>) sanctionToUpdate.getValue("Declarants");
                if(declarants!=null){
                    declarants.add(getWorkflowInstance().getCreatedBy());
                }
                sanctionToUpdate.setValue("Declarants",declarants);
                sanctionToUpdate.save(getWorkflowModule().getSysadminContext());

            }
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());







        }
        return super.onBeforeSubmit(action);
    }


    public IStorageResource getSanction(String id){
        IStorageResource sanction = null;
        try
        {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Sanction", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Sanction");
            controller.addEqualsConstraint("ID", id);
            Collection<IStorageResource> sanctions = controller.evaluate(definition);
            if(sanctions!=null && !sanctions.isEmpty())
            {
                sanction = sanctions.iterator().next();

            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        return sanction;
    }

    public void UpdateSanction(){

    }

    public IStorageResource historiserSanctionNew(){
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject project = getProjectModule().getProject(context,"Sanction",organization);
            ICatalog catalog  = getWorkflowModule().getCatalog(context,"Referentiels",ICatalog.IType.STORAGE,project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"Sanction");
            IStorageResource Sanction = getWorkflowModule().createStorageResource(context,definition,"");
            HashMap<String,String> fields = getSanctionReferentielFields();
            helper.UpdateRessource(Sanction,getWorkflowInstance(),fields);
            return Sanction;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public IStorageResource historiserSanctionHistorique(IWorkflowInstance sanction){
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject project = getProjectModule().getProject(context,"Sanction",organization);
            ICatalog catalog  = getWorkflowModule().getCatalog(context,"Referentiels",ICatalog.IType.STORAGE,project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,"HistoriqueSanction");
            IStorageResource historiqueSanction = getWorkflowModule().createStorageResource(context,definition,"");
            HashMap<String,String> fields = getSanctionHistoriqueReferentielFields();
            helper.UpdateRessource(historiqueSanction,sanction,fields);
            return historiqueSanction;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public HashMap<String,String> getSanctionReferentielFields(){
        HashMap<String,String> fields = new HashMap<>();
        //key for historique , value for instance
        fields.put("PersonneConcernee","PersonneConcernee");
        fields.put("Motif","Motif");
        fields.put("AutreMotif","AutreMotif");
        fields.put("NatureLegale","NatureLegale");
        fields.put("Justification","Justification");
        fields.put("GraviteDeLaFaute","GraviteDeLaFaute");
        fields.put("Graduel","Graduel");
        fields.put("Code","sys_Reference");
        fields.put("Demandeur","Demandeur");
        fields.put("DateDeCreation","DateDeCreation");
        fields.put("Date1EreDeclaration","Date1EreDeclaration");
        fields.put("CommentaireRH","CommentaireRH");
        fields.put("CommentaireDG","CommentaireDG");
        fields.put("_ID","ID");
        fields.put("isNew","isNew");
       // fields.put("SanctionSource","SanctionSource");
        // fields.put("SanctionEvolution","SanctionEvolution");
        fields.put("NombreDEvolution","NombreDEvolution");
        return fields;
    }

    public HashMap<String,String> getSanctionReferentielFieldsWithoutDate1Declaration(){
        HashMap<String,String> fields = new HashMap<>();
        //key for historique , value for instance
        fields.put("PersonneConcernee","PersonneConcernee");
        fields.put("Motif","Motif");
        fields.put("AutreMotif","AutreMotif");
        fields.put("NatureLegale","NatureLegale");
        fields.put("Justification","Justification");
        fields.put("GraviteDeLaFaute","GraviteDeLaFaute");
        fields.put("Graduel","Graduel");
        //fields.put("Code","sys_Reference");
       // fields.put("Demandeur","Demandeur");
        fields.put("DateDeclaration","DateDeclaration");
       // fields.put("Date1EreDeclaration","Date1EreDeclaration");
        fields.put("CommentaireRH","CommentaireRH");
        fields.put("CommentaireDG","CommentaireDG");
        //fields.put("_ID","ID");
       // fields.put("isNew","isNew");
        // fields.put("SanctionSource","SanctionSource");
        // fields.put("SanctionEvolution","SanctionEvolution");
        //fields.put("NombreDEvolution","NombreDEvolution");
        return fields;
    }

    public HashMap<String,String> getSanctionlFields(){
        HashMap<String,String> fields = new HashMap<>();
        //key for historique , value for instance
        fields.put("PersonneConcernee","PersonneConcernee");
        fields.put("Motif","Motif");
        fields.put("AutreMotif","AutreMotif");
        fields.put("NatureLegale","NatureLegale");
        fields.put("Justification","Justification");
        fields.put("GraviteDeLaFaute","GraviteDeLaFaute");
        fields.put("Graduel","Graduel");
        fields.put("Code","sys_Reference");
        fields.put("Demandeur","Demandeur");
        fields.put("DateDeCreation","DateDeCreation");
        fields.put("Date1EreDeclaration","Date1EreDeclaration");
        fields.put("CommentaireRH","CommentaireRH");
        fields.put("CommentaireDG","CommentaireDG");
        fields.put("_ID","ID");
        //fields.put("isNew","isNew");
        fields.put("SanctionSource","SanctionSource");
        fields.put("SanctionEvolution","SanctionEvolution");
        fields.put("NombreDEvolution","NombreDEvolution");
        return fields;
    }



    public HashMap<String,String> getSanctionHistoriqueReferentielFields(){
        HashMap<String,String> fields = new HashMap<>();
        //key for historique , value for instance
        fields.put("PersonneConcernee","PersonneConcernee");
        fields.put("Motif","Motif");
        fields.put("AutreMotif","AutreMotif");
        fields.put("NatureLegale","NatureLegale");
        fields.put("Justification","Justification");
        fields.put("GraviteDeLaFaute","GraviteDeLaFaute");
        fields.put("Graduel","Graduel");
        fields.put("Code","sys_Reference");
        fields.put("Demandeur","Demandeur");
        fields.put("DateDeclaration","DateDeclaration");
        fields.put("Date1EreDeclaration","Date1EreDeclaration");
        fields.put("CommentaireRH","CommentaireRH");
        fields.put("CommentaireDG","CommentaireDG");
         fields.put("ID","ID");
       // fields.put("isNew","isNew");
        //fields.put("SanctionSource","SanctionSource");


        return fields;
    }
}
