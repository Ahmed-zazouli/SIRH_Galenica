package com.moovapps.capone.rh.GestionDeConge.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;

public class RHValidation extends BaseDocumentExtension {
    IWorkflowInstance document = null;
    IUser demandeur = null;

    @Override
    public boolean onAfterLoad() {
        document = getWorkflowInstance();
        demandeur = (IUser)document.getValue("Demandeur");
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("ValiderCongeDuCollaborateur2") ){
           onValiderDemandeConge();
        }

        if(action.getName().equals("RefuserCongeOuAbsenceDuCollaborateur2")){
//			reinitializeDemandeurFields();
            onRefuserConge();
        }
        return super.onBeforeSubmit(action);
    }

    private void onRefuserConge() {
        String categorieConges = (String)document.getValue("TypeDeConge");
        float nbrJoursDemandes = (Float)document.getValue("NombreDeJoursDemandes");
        if(categorieConges.equals("CN")){
            onRefuserCongesPayes(nbrJoursDemandes);
        } else if(categorieConges.equals("CE")){
            float nbrJoursExceptionnelle = (Float)document.getValue("NombreDeJoursExceptionnelle");
            onRefuserCongesSpeciaux(nbrJoursExceptionnelle);
        } else if(categorieConges.equals("SS")){
            onRefuserCongesSansSolde(nbrJoursDemandes);
        } else if(categorieConges.equals("CM")){
            onRefuserCongesMaladie(nbrJoursDemandes);
        }
        document.setValue("demandeurCongesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation"));

        demandeur.save(getWorkflowModule().getSysadminContext());
        IStorageResource userFiche = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(demandeur, getWorkflowModule(), getProjectModule(), getWorkflowInstance().getCatalog().getProject().getOrganization());
        userFiche.save(getWorkflowModule().getSysadminContext());

        document.save(getWorkflowModule().getSysadminContext());
    }

    private void onRefuserCongesPayes(float nbrJoursDemandes) {
        demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
        document.setValue("CongesPayesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));

        //document.setValue("demandeurCongesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));

    }

    private void onRefuserCongesSpeciaux(float nbrJoursDemandes) {
        demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
        document.setValue("demandeurCongesSpeciauxEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation"));

    }

    private void onRefuserCongesSansSolde(float nbrJoursDemandes) {
        demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation",(Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
        document.setValue("demandeurCongesSansSoldeEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation"));
    }

    private void onRefuserCongesMaladie(float nbrJoursDemandes) {
        demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") - nbrJoursDemandes);
        document.setValue("demandeurCongesMaladieEnCoursDeConsommation", demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
    }

	/*public void reinitialize(){
		reinitializeDemandeurFields();
		reinitializeDocument();
	}*/

	/*public void reinitializeDocument(){
		document.setValue("DateDeFin", null);
		document.setValue("DateDeDebut", null);
		document.setValue("NombreDeJoursDemandes", 0);
		document.save(getWorkflowModule().getSysadminContext());
	}*/

    public void reinitializeDemandeurFields(){
        float nbrJoursDemandes = (Float)document.getValue("NombreDeJoursDemandes");
        float demandeurNbrJoursDeConges = (Float)demandeur.getExtendedAttributes().getValue("SoldeConges") + nbrJoursDemandes;
        float demandeurNbrJoursEnCoursValidation = (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes;
        float demandeurNbrJoursEnCoursTraitement = (Float) demandeur.getExtendedAttributes().getValue("JourSEnCoursDeTraitement");
        demandeur.getExtendedAttributes().setValue("SoldeConges", demandeurNbrJoursDeConges);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", demandeurNbrJoursEnCoursValidation);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeTraitement", demandeurNbrJoursEnCoursTraitement);
        demandeur.save(getWorkflowModule().getSysadminContext());
        document.setValue("DemandeurSoldeConge", demandeurNbrJoursDeConges);
        document.setValue("DemandeurTotalJourEnCoursValidation", demandeurNbrJoursEnCoursValidation);



    }

    public void onValiderDemandeConge(){
        String categorieConges = (String)document.getValue("TypeDeConge");
        float nbrJoursDemandes = (Float)document.getValue("NombreDeJoursDemandes");
        if(categorieConges.equals("CN")){
            setDemandeurCongesPayesInfo(nbrJoursDemandes);
        } else if(categorieConges.equals("CE")){
            float nbrJoursExceptionnelle = (Float)document.getValue("NombreDeJoursExceptionnelle");
            setDemandeurCongesSpeciauxInfo(nbrJoursExceptionnelle);
        } else if(categorieConges.equals("SS")){
            setDemandeurCongesSansSoldeInfo(nbrJoursDemandes);
        }else if(categorieConges.equals("CM")){
            setDemandeurCongesMaladieInfo(nbrJoursDemandes);
        }
        document.setValue("demandeurCongesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation")-nbrJoursDemandes);
        document.setValue("demandeurCongesValidesAConsommer", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation")+nbrJoursDemandes);
        //document.setValue("DemandeurCongesEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("joursEnCoursTraitement"));

        demandeur.save(getWorkflowModule().getSysadminContext());
        IStorageResource userFiche = new UserFicheAnnuaireConvert().fromUserToFicheOnlyConges(demandeur, getWorkflowModule(), getProjectModule(), getWorkflowInstance().getCatalog().getProject().getOrganization());
        userFiche.save(getWorkflowModule().getSysadminContext());

        document.save(getWorkflowModule().getSysadminContext());
    }

    private void setDemandeurCongesPayesInfo(float nbrJoursDemandes) {
        demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation") + nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") + nbrJoursDemandes);

        document.setValue("CongesPayesEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeValidation"));
        document.setValue("CongesPayesEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeConsommation"));
        document.setValue("CongesPayesEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesPayesEnCoursDeTraitement"));
    }
    private void setDemandeurCongesSpeciauxInfo(float nbrJoursDemandes) {
        demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation") + nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") + nbrJoursDemandes);
        document.setValue("demandeurCongesSpeciauxEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeValidation"));
        document.setValue("demandeurCongesSpeciauxEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeConsommation"));
        document.setValue("CongesSpeciauxEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesSpeciauxEnCoursDeTraitement"));
    }
    private void setDemandeurCongesSansSoldeInfo(float nbrJoursDemandes) {
        demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation") + nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") + nbrJoursDemandes);
        document.setValue("demandeurCongesSansSoldeEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeValidation"));
        document.setValue("demandeurCongesSansSoldeEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeConsommation"));
        document.setValue("CongesSansSoldeEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesSansSoldeEnCoursDeTraitement"));
    }

    private void setDemandeurCongesMaladieInfo(float nbrJoursDemandes) {
        demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("JourSEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("JourSEnCoursDeValidation") - nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation",(Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation") + nbrJoursDemandes);
        demandeur.getExtendedAttributes().setValue("joursEnCoursConsommation",(Float)demandeur.getExtendedAttributes().getValue("joursEnCoursConsommation") + nbrJoursDemandes);
        document.setValue("demandeurCongesMaladieEnCoursDeValidation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeValidation"));
        document.setValue("demandeurCongesMaladieEnCoursDeConsommation", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
        document.setValue("CongesMaladieEnCoursDeTraitement", (Float)demandeur.getExtendedAttributes().getValue("CongesMaladieEnCoursDeConsommation"));
    }
}

