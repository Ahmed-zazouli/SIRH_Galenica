package com.moovapps.ibb.rh.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;



public class ResetMail extends BaseAgent {
    //WHY THIS CLASS EXIST
    //WELL WHEN I EXPORT EXCEL I GOT 170 ROW BUT THE USERS ARE 655

    @Override
    protected void execute() {
        ArrayList<IStorageResource> fiches = getFiches();

        String mail = "azazouli@capone.ma";
        if(fiches!=null && !fiches.isEmpty()){
            int index = 1;
            for(IStorageResource fiche : fiches){
                fiche.setValue("Email",mail);
                fiche.setValue("Salaire",1000);
                fiche.setValue("SalaireBrutDH",1000);
                fiche.setValue("SalaireBrutImposableDH",1000);
                fiche.setValue("SalaireDeBase",1000);
                fiche.setValue("SalaireNETDH",1000);
                fiche.save(getWorkflowModule().getSysadminContext());

                IUser salarie = (IUser) fiche.getValue("Salarie");
                if(salarie!=null){
                    salarie.setEmail(mail);
                    salarie.setPassword("D3mo@demo@DEMO");
                    salarie.getExtendedAttributes().setValue("Salaire",1000);
                    salarie.getExtendedAttributes().setValue("SalaireBrut",1000);
                    salarie.getExtendedAttributes().setValue("SalaireBrutImposableDH",1000);
                    salarie.getExtendedAttributes().setValue("SalaireDeBase",1000);
                    salarie.save(getWorkflowModule().getSysadminContext());
                }

                System.out.println(index++);

            }
        }
    }

    ArrayList<IStorageResource> getFiches(){
        ArrayList<IStorageResource> fiches = null;
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4,project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"FicheCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            fiches =(ArrayList<IStorageResource>) controller.evaluate(definition);
        }catch (Exception e){
            e.printStackTrace();
        }

        return fiches;
    }
}
