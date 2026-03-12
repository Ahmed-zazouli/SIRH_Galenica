package com.moovapps.EVALUATION.Eval;

import com.axemble.sdk.components.sys.forms.ScreenDescriptionComponent;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdoc.storage.ui.core.providers.views.ResourceViewProvider;
import com.axemble.vdp.ui.core.providers.IViewProvider;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.xml.XMLView;
import com.axemble.vdp.ui.framework.foundation.parts.NavigationPart;
import org.apache.ecs.storage.Hash;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;



public class AutoEval extends BaseDocumentExtension {
    Map<String, ILinkedResource> domaineLinkedRessourceMapper = new HashMap<>();
    Map<String, ILinkedResource> familleLinkedRessourceMapper = new HashMap<>();
    Map<String, ILinkedResource> sousFamilleLinkedRessourceMapper = new HashMap<>();
    ArrayList<IStorageResource> domaineCreated = new ArrayList<>();
    ArrayList<IStorageResource> familleCreated = new ArrayList<>();
    ArrayList<IStorageResource> sousFamilleCreated = new ArrayList<>();

    @Override
    public boolean onBeforeLoad() {
         //   SetCollaboarteurData(getWorkflowInstance());
            SetObjectifs();
            SetCompetences();
            SetMissions();
        return super.onBeforeLoad();
    }

    private void SetCompetences() {
        if(getWorkflowInstance().getLinkedResources("CompetencesGenerale")==null || getWorkflowInstance().getLinkedResources("CompetencesGenerale").isEmpty()){
            IStorageResource ficheCollaborateur = (IStorageResource) getWorkflowInstance().getValue("FicheCollaborateur");
            ArrayList<IStorageResource> competences =  getCompetencesCollaborateur(ficheCollaborateur);
            if(competences!=null && !competences.isEmpty()) {
                for (IStorageResource competence : competences) {

                        ILinkedResource linkedResource = getWorkflowInstance().createLinkedResource("CompetencesGenerale");
                      //  linkedResource.setValue("Societe", competence.getValue("Societe"));
                       // linkedResource.setValue("Profil", getWorkflowInstance().getValue("Profil2"));
                        IStorageResource competencee= (IStorageResource)competence.getValue("Competence");
                        linkedResource.setValue("Domaine", competence.getValue("Domaine"));
                        linkedResource.setValue("FamilleCompetence", competence.getValue("FamilleCompetence"));
                        linkedResource.setValue("SousFamilleCompetence", competence.getValue("SousFamilleCompetence"));
                        linkedResource.setValue("Competence", competencee);
                        linkedResource.setValue("Definition",competencee!=null?competencee.getValue("Definition"):"");
                        linkedResource.setValue("NiveauDeCompetence", competence.getValue("NiveauDeCompetence"));
                        linkedResource.setValue("Poids", competence.getValue("Poids"));
                        linkedResource.setValue("Importance", competence.getValue("Importance"));
                        linkedResource.save(getWorkflowModule().getSysadminContext());
                        getWorkflowInstance().addLinkedResource(linkedResource);
                        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());

                        CreateNotesDomaine(competence);
                        CreateNotesFamille(competence);
                        CreateNotesSousFamille(competence);


                }

            }

        }

    }

    private void SetObjectifs() {
        IUser user =  (IUser) getWorkflowInstance().getValue("CollaborateurEval");
        IStorageResource exerciceEvaluation = (IStorageResource) getWorkflowInstance().getValue("ExerciceEvaluation");
        int exercice = exerciceEvaluation!=null?((Number) exerciceEvaluation.getValue("Annee")).intValue():-1;
        if(user ==null ||exercice==-1)return;
        if(getWorkflowInstance().getLinkedResources("ObjAne")==null || getWorkflowInstance().getLinkedResources("ObjAne").isEmpty()){
            ArrayList<IStorageResource> objectifs = getObjectifs(user,exercice);
            if(objectifs!=null && !objectifs.isEmpty()){
                for(IStorageResource objectif : objectifs){
                    ILinkedResource linkedResource = getWorkflowInstance().createLinkedResource("ObjAne");
                    linkedResource.setValue("Objectif", objectif.getValue("Objectifs"));
                    linkedResource.setValue("Importance", objectif.getValue("Importance"));
                    linkedResource.setValue("EcheancePrevue", objectif.getValue("EcheancePrevue"));
                    linkedResource.save(getWorkflowModule().getSysadminContext());
                    getWorkflowInstance().addLinkedResource(linkedResource);
                    getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                }
            }

        }
    }

    private void SetMissions(){
        IStorageResource ficheCollaborateur = (IStorageResource) getWorkflowInstance().getValue("FicheCollaborateur");
        if(ficheCollaborateur==null)return;
        if(getWorkflowInstance().getLinkedResources("Missions2")==null || getWorkflowInstance().getLinkedResources("Missions2").isEmpty()){
            ArrayList<IStorageResource> missions = getMissions(ficheCollaborateur);
            if(missions!=null && !missions.isEmpty()){
                for(IStorageResource mission : missions){
                    ILinkedResource linkedResource = getWorkflowInstance().createLinkedResource("Missions2");
                    linkedResource.setValue("MissionRessourceID", mission.getId().toString());
                    linkedResource.setValue("Mission", mission.getValue("sys_Title"));
                    linkedResource.setValue("Details", mission.getValue("Details"));
                    linkedResource.setValue("DateAffectation", mission.getValue("DateAffectation"));
                    linkedResource.setValue("StatutActifInactif", true);
                    linkedResource.save(getWorkflowModule().getSysadminContext());
                    getWorkflowInstance().addLinkedResource(linkedResource);
                    getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                }
            }
        }

    }

    private void SetCollaboarteurData(IWorkflowInstance evalInstance) {
        IUser user =  (IUser) getWorkflowInstance().getValue("CollaborateurEval");
        if(user==null)return;
        String etatEvaluation = (String) getWorkflowInstance().getValue("EtatDEvaluation");
       // if(etatEvaluation.equals("A débuter")){
           // evalInstance.setValue("NPlus2",user.getHierarchicalManager()!=null?user.getHierarchicalManager().getHierarchicalManager():null);
            //evalInstance.setValue("Evaluateur",user.getExtendedAttributes().getValue("Evaluateur"));
            evalInstance.setValue("Fonction",user.getExtendedAttributes().getValue("Fonction"));
            evalInstance.setValue("Direction",user.getExtendedAttributes().getValue("Direction"));
            evalInstance.setValue("Service",user.getExtendedAttributes().getValue("Service"));
            evalInstance.setValue("Profil2",user.getExtendedAttributes().getValue("ProfilEVAL"));
            evalInstance.setValue("Departement",user.getExtendedAttributes().getValue("Departement"));
            evalInstance.setValue("ResponsableHierarchique",user.getHierarchicalManager());
            evalInstance.setValue("DateDIntegration",user.getExtendedAttributes().getValue("DateDEmbauche"));
            evalInstance.setValue("Anciennete",getAncienneteInDetail((Date)user.getExtendedAttributes().getValue("DateDEmbauche")));
            evalInstance.setValue("DateDEmbaucheGroupe",user.getExtendedAttributes().getValue("DateDEmbaucheGroupe"));
            evalInstance.setValue("AncienneteGroupe",getAncienneteInDetail((Date)user.getExtendedAttributes().getValue("DateDEmbaucheGroupe")));
            evalInstance.setValue("Matricule",user.getExtendedAttributes().getValue("Matricule"));

       /* }else if(!etatEvaluation.equals("Evaluation réceptionnée") && !etatEvaluation.equals("Evaluation clôturée")){
            evalInstance.setValue("NPlus2",user.getHierarchicalManager()!=null?user.getHierarchicalManager().getHierarchicalManager():null);
            evalInstance.setValue("Fonction",user.getExtendedAttributes().getValue("Fonction"));
            evalInstance.setValue("Direction",user.getExtendedAttributes().getValue("Direction"));
            evalInstance.setValue("Service",user.getExtendedAttributes().getValue("Service"));
            evalInstance.setValue("Profil2",user.getExtendedAttributes().getValue("ProfilEVAL"));
            evalInstance.setValue("Departement",user.getExtendedAttributes().getValue("Departement"));
            evalInstance.setValue("ResponsableHierarchique",user.getHierarchicalManager());
            evalInstance.setValue("DateDIntegration",user.getExtendedAttributes().getValue("DateDEmbauche"));
            evalInstance.setValue("Anciennete",getAncienneteInDetail((Date)user.getExtendedAttributes().getValue("DateDEmbauche")));
            evalInstance.setValue("DateDEmbaucheGroupe",user.getExtendedAttributes().getValue("DateDEmbaucheGroupe"));
            evalInstance.setValue("AncienneteGroupe",getAncienneteInDetail((Date)user.getExtendedAttributes().getValue("DateDEmbaucheGroupe")));
            evalInstance.setValue("Matricule",user.getExtendedAttributes().getValue("Matricule"));

        }*/
    }
    public String getAncienneteInDetail(Date dateEmbauche) {
        if (dateEmbauche == null) {
            return "";
        }
        long difference_In_Milliseconds = ((new Date().getTime() - dateEmbauche.getTime()));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(difference_In_Milliseconds);
        int years = c.get(Calendar.YEAR) - 1970;
        int months = c.get(Calendar.MONTH);
        int days = c.get(Calendar.DAY_OF_MONTH) - 1;
        return years + " ans " + months + " mois "/* + days + " jours"*/;
//		return (int)difference_In_Years;
    }


    private void CreateNotesDomaine(IStorageResource competence){
        IStorageResource domaineCompetence = (IStorageResource)competence.getValue("Domaine");
        if(!domaineCreated.contains(domaineCompetence)){
            // create
            ILinkedResource note = getWorkflowInstance().createLinkedResource("DomaineNotes");
            note.setValue("Domaine",domaineCompetence);
            note.save(getWorkflowModule().getSysadminContext());
            getWorkflowInstance().addLinkedResource(note);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
            // add to hashmap
            domaineCreated.add(domaineCompetence);
        }
    }

    private void CreateNotesFamille(IStorageResource competence){
       IStorageResource familleCompetence = (IStorageResource)competence.getValue("FamilleCompetence");
       if(!familleCreated.contains(familleCompetence)){
           // create
           ILinkedResource note = getWorkflowInstance().createLinkedResource("Notes");
           note.setValue("FamilleCompetence",familleCompetence);
           note.save(getWorkflowModule().getSysadminContext());
           getWorkflowInstance().addLinkedResource(note);
           getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
           // add to hashmap
           familleCreated.add(familleCompetence);
       }
    }



    private void CreateNotesSousFamille(IStorageResource competence){
        IStorageResource sousFamilleCompetence = (IStorageResource)competence.getValue("SousFamilleCompetence");
        if(!sousFamilleCreated.contains(sousFamilleCompetence)){
            // create
            ILinkedResource note = getWorkflowInstance().createLinkedResource("NotesSousFamille");
            note.setValue("SousFamilleCompetence",sousFamilleCompetence);
            note.save(getWorkflowModule().getSysadminContext());
            getWorkflowInstance().addLinkedResource(note);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
            // add to hashmap
            sousFamilleCreated.add(sousFamilleCompetence);
        }
    }



    ArrayList<IStorageResource> getCompetencesByUserAndFonction(IStorageResource ficheCollaborateur,IStorageResource fonction , int type){

        ArrayList<IStorageResource> competences = new ArrayList<>();
        if(type==1){
            //IStorageResource ficheCollaborateur = getFicheCollaborateur(collaborateur);
            if(ficheCollaborateur==null){
                return null;
            }
            ArrayList<IStorageResource> competencesCollaborateur = getCompetencesCollaborateur(ficheCollaborateur);
            if(competencesCollaborateur==null || competencesCollaborateur.isEmpty()){
                return null;
            }
            for(IStorageResource competenceCollaborateur : competencesCollaborateur){
                IStorageResource competence = (IStorageResource) competenceCollaborateur.getValue("Competence");
                if(competence==null){
                    continue;
                }
                competences.add(competence);
            }

        }else if(type==2){
            if(fonction==null){
                return null;
            }
            ArrayList<IStorageResource> competencesFonction = getCompetencesFonction(fonction);
            if(competencesFonction==null || competencesFonction.isEmpty()){
                return null;
            }
            for(IStorageResource competenceFonction : competencesFonction){
                IStorageResource competence = (IStorageResource) competenceFonction.getValue("Competence");
                if(competence==null){
                    continue;
                }
                competences.add(competence);
            }
        }else{
            if(ficheCollaborateur!=null && fonction !=null){
                ArrayList<IStorageResource> competencesCollaborateur = getCompetencesCollaborateur(ficheCollaborateur);
                if(competencesCollaborateur==null || competencesCollaborateur.isEmpty()){
                    return null;
                }
                for(IStorageResource competenceCollaborateur : competencesCollaborateur){
                    IStorageResource competence = (IStorageResource) competenceCollaborateur.getValue("Competence");
                    if(competence==null){
                        continue;
                    }
                    competences.add(competence);
                }

                ArrayList<IStorageResource> competencesFonction = getCompetencesFonction(fonction);
                if(competencesFonction==null || competencesFonction.isEmpty()){
                    return competences;
                }
                for(IStorageResource competenceFonction : competencesFonction){
                    IStorageResource competence = (IStorageResource) competenceFonction.getValue("Competence");
                    if(competence==null){
                        continue;
                    }
                    competences.add(competence);
                }
            }else if(ficheCollaborateur!=null){
                ArrayList<IStorageResource> competencesCollaborateur = getCompetencesCollaborateur(ficheCollaborateur);
                if(competencesCollaborateur==null || competencesCollaborateur.isEmpty()){
                    return null;
                }
                for(IStorageResource competenceCollaborateur : competencesCollaborateur){
                    IStorageResource competence = (IStorageResource) competenceCollaborateur.getValue("Competence");
                    if(competence==null){
                        continue;
                    }
                    competences.add(competence);
                }
            }else if(fonction !=null){
                ArrayList<IStorageResource> competencesFonction = getCompetencesFonction(fonction);
                if(competencesFonction==null || competencesFonction.isEmpty()){
                    return null;
                }
                for(IStorageResource competenceFonction : competencesFonction){
                    IStorageResource competence = (IStorageResource) competenceFonction.getValue("Competence");
                    if(competence==null){
                        continue;
                    }
                    competences.add(competence);
                }
            }
        }
        return competences;
    }

    ArrayList<IStorageResource> getCompetencesCollaborateur(IStorageResource collaborateur){
        ArrayList<IStorageResource> competencesCollaborateur = null;
        try{

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"CompetenceSalarie");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("Collaborateur",collaborateur);
            competencesCollaborateur =  (ArrayList<IStorageResource>) controller.evaluate(definition);




        }catch (Exception e){
            e.printStackTrace();
        }
        return competencesCollaborateur;
    }

    ArrayList<IStorageResource> getCompetencesFonction(IStorageResource fonction){
        ArrayList<IStorageResource> competences = null;
        try{

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"CompetenceFonctionPoste");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("Fonction",fonction);
            competences =  (ArrayList<IStorageResource>) controller.evaluate(definition);



        }catch (Exception e){
            e.printStackTrace();
        }
        return competences;
    }

    @Override
    public void onPropertyChanged(IProperty property) {
       if(property.getName().equals("CompetencesGenerale")){
            ArrayList<ILinkedResource> competences = (ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("CompetencesGenerale");
            if(competences!=null && !competences.isEmpty()){
                CalculateNotes(competences);
            }

       }

        /*else if(property.getName().equals("ObjAne")){
            CalculateAtteinteObjectifAnnuel();
        }*/

        super.onPropertyChanged(property);
    }

    public void CalculateAtteinteObjectifAnnuel(IWorkflowInstance instance) {

        ArrayList<ILinkedResource> objectifsAnne = (ArrayList<ILinkedResource>) instance.getParentInstance().getLinkedResources("ObjAne");
        if(objectifsAnne!=null && !objectifsAnne.isEmpty()) {
            double sommeImportanceObjectif = 0;

            double sommeresultatCollaborateurMultiplyPoids = 0;
            double moyeneCollaborateur = 0;

            double sommeresultatN1MultiplyPoids = 0;
            double moyeneN1 = 0;

            for (ILinkedResource objectif : objectifsAnne) {
                //objectif.save(getWorkflowModule().getSysadminContext());
                double importance = objectif.getValue("Importance") != null ? ((Number) objectif.getValue("Importance")).doubleValue() : 0;

                double resultatCollaborateur = objectif.getValue("ResultatCollaborateur") != null ? ((Number) objectif.getValue("ResultatCollaborateur")).doubleValue() : 0;
                double resultatCollaborateurMultiplyPoids = importance * resultatCollaborateur;
                sommeresultatCollaborateurMultiplyPoids += resultatCollaborateurMultiplyPoids;

                double resultatN1 = objectif.getValue("ResultatN1") != null ? ((Number) objectif.getValue("ResultatN1")).doubleValue() : 0;
                double resultatN1MultiplyPoids = importance * resultatN1;
                sommeresultatN1MultiplyPoids += resultatN1MultiplyPoids;

                sommeImportanceObjectif += importance;


            }
            if (sommeImportanceObjectif != 0) {
                moyeneCollaborateur = sommeresultatCollaborateurMultiplyPoids / sommeImportanceObjectif;
                moyeneN1 = sommeresultatN1MultiplyPoids / sommeImportanceObjectif;
            }

            // % Atteintes objectif annuelle
            instance.getParentInstance().setValue("Total", moyeneCollaborateur);
            instance.getParentInstance().setValue("TotalN1", moyeneN1);
          //  instance.getParentInstance().save(Modules.getWorkflowModule().getSysadminContext());

        }
    }


    @Override
    public boolean onBeforeSubmit(IAction action) {

        if(action.getName().equals("SoumettreMonAutoEvaluation")){
            IUser evaluateur = (IUser) getWorkflowInstance().getValue("Evaluateur");
            if(evaluateur==null){
                getResourceController().alert("Merci de choisir un évaluateur pour ce collaborateur");
                return false;
            }

            boolean objAne =  checkIfAllCompetencesSeted((ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("ObjAne"),"ResultatCollaborateur");
            if(objAne == false){
                //block and show message
                getResourceController().alert("Veuillez évaluer toutes les objectifs de l'année");
                return false;
            }

            boolean compMetier =  checkIfAllCompetencesSeted((ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("CompetencesGenerale"),"AutoEvaluation");
            if(compMetier == false){
                //block and show message
                getResourceController().alert("Veuillez évaluer toutes les compétences");
                return false;
            }



            ArrayList<ILinkedResource> objectifs = (ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("ObjAne");
            if (objectifs != null && !objectifs.isEmpty()) {
                double sommeImportances = 0;
                for (ILinkedResource objectif : objectifs) {
                    double importance = objectif.getValue("Importance") != null ? ((Number) objectif.getValue("Importance")).doubleValue() : 0;
                    sommeImportances += importance;

                }
                if(sommeImportances!=100){
                    getResourceController().alert("La totalité des pondérations attribuées aux objectifs doit équivaloir à 100");
                    return false;
                }
            }



            ArrayList<ILinkedResource> fixations = (ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("ObjFix");
            if (fixations != null && !fixations.isEmpty()) {
                double sommeImportances = 0;
                for (ILinkedResource fixation : fixations) {
                    double importance = fixation.getValue("Importance") != null ? ((Number) fixation.getValue("Importance")).doubleValue() : 0;
                    sommeImportances += importance;

                }
                if(sommeImportances!=100){
                    getResourceController().alert("La totalité des pondérations attribuées aux objectifs fixés doit équivaloir à 100");
                    return false;
                }
            }

        }
        return super.onBeforeSubmit(action);
    }


    @Override
    public boolean onAfterSubmit(IAction action) {
        if (action.getName().equals("SoumettreMonAutoEvaluation")) {
           /* try {
                IContext sysContext = getWorkflowModule().getSysadminContext();
                IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
                ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
                IResourceDefinition objectifAnneDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "ObjectifsDeLAnnee");
                ArrayList<ILinkedResource> fixations = (ArrayList<ILinkedResource>) getWorkflowInstance().getValue("ObjFix");
                int nextYear = (Calendar.getInstance().get(Calendar.YEAR)) + 1;
                IUser collaborateur = (IUser) getWorkflowInstance().getValue("Collaborateur");
                for (ILinkedResource fixation : fixations) {
                    IStorageResource objectif = getWorkflowModule().createStorageResource(sysContext, objectifAnneDefinition, "");
                    objectif.setValue("Annee", nextYear);
                    objectif.setValue("Collaborateur", collaborateur);
                    objectif.setValue("Objectifs", fixation.getValue("Objectif"));
                    objectif.setValue("IndicateurDePerformance", fixation.getValue("IndicateurDePerformance"));
                    objectif.setValue("Poids", fixation.getValue("Poids"));
                    objectif.save(getWorkflowModule().getSysadminContext());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }*/

        }
        return super.onAfterSubmit(action);
    }

    ArrayList<IStorageResource> getObjectifs(IUser collaborateur  , int year){
        if(collaborateur ==null) return null;
        ArrayList<IStorageResource> objectifs = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE,project);
            IResourceDefinition filialeDefinition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"ObjectifsDeLAnnee");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("Collaborateur",collaborateur);
            //controller.addEqualsConstraint("Profil",profil);
            controller.addEqualsConstraint("Annee",year);
            objectifs = (ArrayList<IStorageResource>) controller.evaluate(filialeDefinition);
        } catch (Exception e) {
            e.printStackTrace();

        }


        return objectifs;
    }

    ArrayList<IStorageResource> getMissions(IStorageResource collaborateur){
        ArrayList<IStorageResource> missions = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE,project);
            IResourceDefinition filialeDefinition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"MissionCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("Collaborateur",collaborateur);

            missions = (ArrayList<IStorageResource>) controller.evaluate(filialeDefinition);
        } catch (Exception e) {
            e.printStackTrace();

        }


        return missions;
    }

    boolean checkIfAllCompetencesSeted(ArrayList<ILinkedResource> resources,String checkedField){
        if(resources.isEmpty()){
            return true;
        }
        for(ILinkedResource resource : resources){
            if(resource.getValue(checkedField)==null){
                return false;
            }
        }

        return true;
    }






    ArrayList<IStorageResource> getCompetencesBySocieteByProfile(IStorageResource societe , IStorageResource profil){
        if(profil ==null) return null;
        ArrayList<IStorageResource> competences = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE,project);
            IResourceDefinition filialeDefinition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"Competences");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("Profil",profil);
            controller.addEqualsConstraint("Societe",societe);

            competences = (ArrayList<IStorageResource>) controller.evaluate(filialeDefinition);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return competences;
    }















        public  Map<String, BigDecimal> calculateGlobalNotes(ArrayList<ILinkedResource> competences) {
            Map<String, BigDecimal> globalNotes = new HashMap<>();
            Map<String, BigDecimal> totalWeightMAP  = new HashMap<>();
            for (ILinkedResource competence : competences) {
                String profilKey = "PROFIL";//competence.getValue("Profil")!=null?(String) ((IStorageResource) competence.getValue("Profil")).getValue("sys_Title"):"";
                String domaineKey =competence.getValue("Domaine")!=null? (String) ((IStorageResource) competence.getValue("Domaine")).getValue("sys_Title"):"";
                String familleKey =competence.getValue("FamilleCompetence")!=null? (String) ((IStorageResource) competence.getValue("FamilleCompetence")).getValue("sys_Title"):"";
                String sousFamilleKey =competence.getValue("SousFamilleCompetence")!=null?(String) ((IStorageResource) competence.getValue("SousFamilleCompetence")).getValue("sys_Title"):"";
                //IStorageResource competenceStorage = (IStorageResource) competence.getValue("Competence");
                if(!domaineLinkedRessourceMapper.containsKey(domaineKey)){
                    domaineLinkedRessourceMapper.put(domaineKey,getDomaineByTitle(domaineKey));
                }

                if(!familleLinkedRessourceMapper.containsKey(familleKey)){
                    familleLinkedRessourceMapper.put(familleKey,getFamilleByTitle(familleKey));
                }
                if(!sousFamilleLinkedRessourceMapper.containsKey(sousFamilleKey)){
                    sousFamilleLinkedRessourceMapper.put(sousFamilleKey,getSousFamilleByTitle(sousFamilleKey));
                }
                // Calculate the contribution of the competence to the global note
                BigDecimal valeur = competence.getValue("AutoEvaluation")!=null?((BigDecimal) ((IStorageResource) competence.getValue("AutoEvaluation")).getValue("Valeur")) : BigDecimal.ZERO;
                BigDecimal importance = competence.getValue("Importance")!=null?(BigDecimal) competence.getValue("Importance"):BigDecimal.ZERO;
                if (totalWeightMAP.containsKey(profilKey)) {
                    totalWeightMAP.put(profilKey, totalWeightMAP.get(profilKey).add(importance));
                } else {
                    totalWeightMAP.put(profilKey, importance);
                }
                if (totalWeightMAP.containsKey(domaineKey)) {
                    totalWeightMAP.put(domaineKey, totalWeightMAP.get(domaineKey).add(importance));
                } else {
                    totalWeightMAP.put(domaineKey, importance);
                }

                if (totalWeightMAP.containsKey(familleKey)) {
                    totalWeightMAP.put(familleKey, totalWeightMAP.get(familleKey).add(importance));
                } else {
                    totalWeightMAP.put(familleKey, importance);
                }

                if (totalWeightMAP.containsKey(sousFamilleKey)) {
                    totalWeightMAP.put(sousFamilleKey, totalWeightMAP.get(sousFamilleKey).add(importance));
                } else {
                    totalWeightMAP.put(sousFamilleKey, importance);
                }
                BigDecimal competenceContributionAutoEvaluation = valeur.multiply(importance);

                // Update the global note for the profile
                globalNotes.put(profilKey, globalNotes.getOrDefault(profilKey, BigDecimal.ZERO).add(competenceContributionAutoEvaluation));
                globalNotes.put(domaineKey, globalNotes.getOrDefault(domaineKey, BigDecimal.ZERO).add(competenceContributionAutoEvaluation));

                // Update the global note for the family
                globalNotes.put(familleKey, globalNotes.getOrDefault(familleKey, BigDecimal.ZERO).add(competenceContributionAutoEvaluation));

                // Update the global note for the sub-family
                globalNotes.put(sousFamilleKey, globalNotes.getOrDefault(sousFamilleKey, BigDecimal.ZERO).add(competenceContributionAutoEvaluation));
            }


            for (Map.Entry<String, BigDecimal> entry : globalNotes.entrySet()) {
                String key = entry.getKey();
                BigDecimal totalWeight = totalWeightMAP.get(key);


               /* BigDecimal totalWeight = competences.stream()
                        .filter(comp -> {
                            Object profilValue = comp.getValue("Profil");
                            Object familleValue = comp.getValue("FamilleCompetence");
                            Object sousFamilleValue = comp.getValue("SousFamilleCompetence");

                            return ( key.equals("PROFIL") ||
                                    (familleValue != null && key.equals((String) ((IStorageResource) familleValue).getValue("sys_Title"))) ||
                                    (sousFamilleValue != null && key.equals((String) ((IStorageResource) sousFamilleValue).getValue("sys_Title"))));
                        })
                        .map(comp ->comp.getValue("Importance")!=null? (BigDecimal) comp.getValue("Importance"):BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);*/
                BigDecimal result = totalWeight.compareTo(BigDecimal.ZERO) != 0 ? entry.getValue().divide(totalWeight,MathContext.DECIMAL128) : BigDecimal.ZERO;

                globalNotes.put(key, result);
            }


            return globalNotes;
        }


private ILinkedResource getFamilleByTitle(String name){
       ArrayList<ILinkedResource> familleNotes = (ArrayList<ILinkedResource> ) getWorkflowInstance().getLinkedResources("Notes");
       if(familleNotes==null ||familleNotes.isEmpty()){
           return null;
       }
       for(ILinkedResource note : familleNotes){
           IStorageResource familleCompetence = (IStorageResource) note.getValue("FamilleCompetence");
           if(familleCompetence.getValue("sys_Title").equals(name)){
               return note;
           }
       }

       return null;
}

    private ILinkedResource getDomaineByTitle(String name){
        ArrayList<ILinkedResource> domaineNotes = (ArrayList<ILinkedResource> ) getWorkflowInstance().getLinkedResources("DomaineNotes");
        if(domaineNotes==null ||domaineNotes.isEmpty()){
            return null;
        }
        for(ILinkedResource note : domaineNotes){
            IStorageResource domaine = (IStorageResource) note.getValue("Domaine");
            if(domaine.getValue("sys_Title").equals(name)){
                return note;
            }
        }

        return null;
    }

    private ILinkedResource getSousFamilleByTitle(String name){
        ArrayList<ILinkedResource> familleNotes = (ArrayList<ILinkedResource> ) getWorkflowInstance().getLinkedResources("NotesSousFamille");
        if(familleNotes==null ||familleNotes.isEmpty()){
            return null;
        }
        for(ILinkedResource note : familleNotes){
            IStorageResource sousFamilleCompetence = (IStorageResource) note.getValue("SousFamilleCompetence");
            if(sousFamilleCompetence.getValue("sys_Title").equals(name)){
                return note;
            }
        }

        return null;
    }


    private void CalculateNotes(ArrayList<ILinkedResource> competences){
        Map<String, BigDecimal> globalNotes = calculateGlobalNotes(competences);
        String profilEvalue = "PROFIL"; //(String) ((IStorageResource) getWorkflowInstance().getValue("Profil2")).getValue("sys_Title");
        for (Map.Entry<String, BigDecimal> entry : globalNotes.entrySet()) {
            String key = entry.getKey();
             BigDecimal note = entry.getValue();
             note = note.setScale(2, BigDecimal.ROUND_DOWN);
            if(key.equals(profilEvalue)){
                getWorkflowInstance().setValue("EvaluationGlobale",note);
            }else{
                ILinkedResource domaineRecupatilatifLinkedRessource = domaineLinkedRessourceMapper.get(key);
                if(domaineRecupatilatifLinkedRessource!=null){
                    domaineRecupatilatifLinkedRessource.setValue("AutoEvaluation",note);
                    domaineRecupatilatifLinkedRessource.save(getWorkflowModule().getSysadminContext());
                }else{
                    ILinkedResource familleRecupatilatifLinkedRessource = familleLinkedRessourceMapper.get(key);
                    if(familleRecupatilatifLinkedRessource!=null){
                        familleRecupatilatifLinkedRessource.setValue("AutoEvaluation",note);
                        familleRecupatilatifLinkedRessource.save(getWorkflowModule().getSysadminContext());
                    }else{
                        ILinkedResource sousFamilleRecupatilatifLinkedRessource = sousFamilleLinkedRessourceMapper.get(key);
                        if(sousFamilleRecupatilatifLinkedRessource!=null){
                            sousFamilleRecupatilatifLinkedRessource.setValue("AutoEvaluation",note);
                            sousFamilleRecupatilatifLinkedRessource.save(getWorkflowModule().getSysadminContext());
                        }

                    }
                }



            }
            System.out.println("Global Note for " + entry.getKey() + ": " + entry.getValue());
        }
    }

}
