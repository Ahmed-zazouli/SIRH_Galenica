

package com.moovapps.Reprise.Referentiels;

        import com.axemble.vdoc.sdk.agent.base.BaseAgent;
        import com.axemble.vdoc.sdk.interfaces.*;

        import java.math.BigDecimal;
        import java.math.MathContext;
        import java.math.RoundingMode;
        import java.util.ArrayList;

public class CompetenceFonctionNotFromExcel extends BaseAgent {
    @Override
    protected void execute() {
        ArrayList<IStorageResource> fonctions = getFonctions();
        ArrayList<IStorageResource> competences = getCompetences();
        if(fonctions!=null && !fonctions.isEmpty() && competences!=null && !competences.isEmpty()){
            BigDecimal importance = new BigDecimal("100").divide(new BigDecimal(competences.size()+""),2, RoundingMode.DOWN);
            importance = importance.setScale(2);
            for(IStorageResource fonction : fonctions){
                for(IStorageResource comp : competences){
                    IStorageResource compFonction = createStorage("DefaultOrganization","REFERENTIELCOMMUN","REFERENTIEL","CompetenceFonctionPoste");
                    compFonction.setValue("Fonction",fonction);
                    compFonction.setValue("Domaine",comp.getValue("Domaine"));
                    compFonction.setValue("FamilleCompetence",comp.getValue("FamilleCompetence"));
                    compFonction.setValue("SousFamilleCompetence",comp.getValue("SousFamilleCompetence"));
                    compFonction.setValue("Competence",comp);
                    compFonction.setValue("Poids", comp.getValue("Poids"));
                  //  compFonction.setValue("Importance",importance);
                    compFonction.setValue("NiveauDeCompetence", getNivoByComp(comp));
                    compFonction.save(getWorkflowModule().getSysadminContext());

                }
            }
        }
    }

    private IStorageResource getNivoByComp(IStorageResource comp) {

        ArrayList<IStorageResource> data = null;
        IStorageResource item = null;
        try{

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"NiveauDeCompetence");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("Competence",comp);
            //  controller.addEqualsConstraint("sys_Title","Niveau 1");
            data =   (ArrayList<IStorageResource>) controller.evaluate(definition);
            if(data!=null && !data.isEmpty()){
                item = data.iterator().next();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return item;
    }

    ArrayList<IStorageResource> getFonctions(){
        ArrayList<IStorageResource> fonctions = null;
        try{

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"fonctions");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            fonctions =   (ArrayList<IStorageResource>) controller.evaluate(definition);


        }catch (Exception e){
            e.printStackTrace();
        }
        return fonctions;
    }

    ArrayList<IStorageResource> getCompetences(){
        ArrayList<IStorageResource> data = null;
        try{

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"Competence");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addNotEqualsConstraint("sys_Title","Compétence TEST");
            data =   (ArrayList<IStorageResource>) controller.evaluate(definition);


        }catch (Exception e){
            e.printStackTrace();
        }
        return data;
    }

    private IStorageResource createStorage(String organizationName ,  String projetName, String catalogName, String definitionName) {
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context,organizationName);
            IProject projet = getProjectModule().getProject(context,projetName,organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,catalogName,ICatalog.IType.STORAGE,projet);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context,catalog,definitionName);
            return getWorkflowModule().createStorageResource(context,definition,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

