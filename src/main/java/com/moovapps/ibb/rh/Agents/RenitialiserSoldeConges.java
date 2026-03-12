package com.moovapps.ibb.rh.Agents;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;

public class RenitialiserSoldeConges extends BaseAgent {

    @Override
    protected void execute() {
        ArrayList<IStorageResource> fiches = getFiches();
        if(fiches!=null && !fiches.isEmpty()){
            for(IStorageResource ficheCollaborateur : fiches){
                ficheCollaborateur.setValue("TypeDeSalaire","Mensuel");
                ficheCollaborateur.setValue("AbsenceAnneeEnCours",0);
                ficheCollaborateur.setValue("AbsenceAnterieure",0);
                //ficheCollaborateur.setValue("DroitMensuelle",0);
               // ficheCollaborateur.setValue("SoldeAnneeEnCours",0);
              //  ficheCollaborateur.setValue("SoldeAnterieur",0);
               // ficheCollaborateur.setValue("SoldeConges",0);
                double soldeAnneEncours = ficheCollaborateur.getValue("SoldeAnneeEnCours")!=null? ((Number) ficheCollaborateur.getValue("SoldeAnneeEnCours")).doubleValue():0;
                double soldeAnterieur = ficheCollaborateur.getValue("SoldeAnterieur")!=null? ((Number) ficheCollaborateur.getValue("SoldeAnterieur")).doubleValue():0;
                double soldeConge = soldeAnneEncours + soldeAnterieur;
                ficheCollaborateur.setValue("SoldeConges",soldeConge);
                ficheCollaborateur.setValue("CongesPayesEnCoursDeValidation",0);
                ficheCollaborateur.setValue("CongesPayesEnCoursDeConsommation",0);
                ficheCollaborateur.setValue("CongesSpeciauxEnCoursDeValidation",0);
                ficheCollaborateur.setValue("CongesSpeciauxEnCoursDeConsommation",0);
                ficheCollaborateur.setValue("CongesMaladieEnCoursDeValidation",0);
                ficheCollaborateur.setValue("CongesMaladieEnCoursDeConsommation",0);
                ficheCollaborateur.setValue("CongesSansSoldeEnCoursDeValidation",0);
                ficheCollaborateur.setValue("CongesSansSoldeEnCoursDeConsommation",0);
                ficheCollaborateur.setValue("JourSEnCoursDeValidation",0);
                ficheCollaborateur.setValue("joursEnCoursConsommation",0);
                ficheCollaborateur.setValue("CongesPayesAnneeEnCours",0);
                ficheCollaborateur.setValue("CongesPayesN1",0);
                ficheCollaborateur.setValue("CongesPayesPris",0);
                ficheCollaborateur.setValue("CongesSpeciauxAnneeEnCours",0);
                ficheCollaborateur.setValue("CongesSpeciauxN1",0);
                ficheCollaborateur.setValue("CongesSpeciauxPris",0);
                ficheCollaborateur.setValue("CongesMaladieAnneeEnCours",0);
                ficheCollaborateur.setValue("CongesMaladieN1",0);
                ficheCollaborateur.setValue("CongesMaladiePris",0);
                ficheCollaborateur.setValue("CongesSansSoldeAnneeEnCours",0);
                ficheCollaborateur.setValue("CongesSansSoldeN1",0);
                ficheCollaborateur.setValue("CongesSansSoldePris",0);
                ficheCollaborateur.setValue("TotalJoursPris",0);
                ficheCollaborateur.save(getWorkflowModule().getSysadminContext());

                IUser salarie = (IUser) ficheCollaborateur.getValue("Salarie");
                if(salarie!=null){
                    salarie.getExtendedAttributes().setValue("TypeDeSalaire","Mensuel");

                    salarie.getExtendedAttributes().setValue("AbsenceAnneeEnCours",0);
                    salarie.getExtendedAttributes().setValue("AbsenceAnterieure",0);
                    //salarie.getExtendedAttributes().setValue("DroitMensuelle",0);
                    // salarie.getExtendedAttributes().setValue("SoldeAnneeEnCours",0);
                    //  salarie.getExtendedAttributes().setValue("SoldeAnterieur",0);
                     salarie.getExtendedAttributes().setValue("SoldeConges",soldeConge);
                    salarie.getExtendedAttributes().setValue("CongesPayesEnCoursDeValidation",0);
                    salarie.getExtendedAttributes().setValue("CongesPayesEnCoursDeConsommation",0);
                    salarie.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeValidation",0);
                    salarie.getExtendedAttributes().setValue("CongesSpeciauxEnCoursDeConsommation",0);
                    salarie.getExtendedAttributes().setValue("CongesMaladieEnCoursDeValidation",0);
                    salarie.getExtendedAttributes().setValue("CongesMaladieEnCoursDeConsommation",0);
                    salarie.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeValidation",0);
                    salarie.getExtendedAttributes().setValue("CongesSansSoldeEnCoursDeConsommation",0);
                    salarie.getExtendedAttributes().setValue("JourSEnCoursDeValidation",0);
                    salarie.getExtendedAttributes().setValue("joursEnCoursConsommation",0);
                    salarie.getExtendedAttributes().setValue("CongesPayesAnneeEnCours",0);
                    salarie.getExtendedAttributes().setValue("CongesPayesN1",0);
                    salarie.getExtendedAttributes().setValue("CongesPayesPris",0);
                    salarie.getExtendedAttributes().setValue("CongesSpeciauxAnneeEnCours",0);
                    salarie.getExtendedAttributes().setValue("CongesSpeciauxN1",0);
                    salarie.getExtendedAttributes().setValue("CongesSpeciauxPris",0);
                    salarie.getExtendedAttributes().setValue("CongesMaladieAnneeEnCours",0);
                    salarie.getExtendedAttributes().setValue("CongesMaladieN1",0);
                    salarie.getExtendedAttributes().setValue("CongesMaladiePris",0);
                    salarie.getExtendedAttributes().setValue("CongesSansSoldeAnneeEnCours",0);
                    salarie.getExtendedAttributes().setValue("CongesSansSoldeN1",0);
                    salarie.getExtendedAttributes().setValue("CongesSansSoldePris",0);
                    salarie.getExtendedAttributes().setValue("TotalJoursPris",0);
                    salarie.save(getWorkflowModule().getSysadminContext());
                }
            }
        }


    }

    public ArrayList<IStorageResource> getFiches() {
        try {
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context,"DefaultOrganization");
            IProject projet = Modules.getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context,"REFERENTIEL",ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context,catalog,"FicheCollaborateur");
            IViewController controller = Modules.getWorkflowModule().getViewController(context,IResource.class);
            return ( ArrayList<IStorageResource>) controller.evaluate(definition);


        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
