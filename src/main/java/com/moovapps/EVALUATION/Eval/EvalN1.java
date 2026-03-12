package com.moovapps.EVALUATION.Eval;

import com.axemble.sdk.components.sys.forms.ScreenDescriptionComponent;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdoc.storage.ui.core.providers.views.ResourceViewProvider;
import com.axemble.vdp.ui.core.document.CustomSubFormController;
import com.axemble.vdp.ui.core.providers.IViewProvider;
import com.axemble.vdp.ui.framework.composites.IDocumentComposite;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.xml.XMLView;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.parts.NavigationPart;
import com.axemble.vdp.ui.framework.widgets.blocks.BlockWidget;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EvalN1 extends BaseDocumentExtension {
    Map<String, ILinkedResource> domaineLinkedRessourceMapper = new HashMap<>();
    Map<String, ILinkedResource> familleLinkedRessourceMapper = new HashMap<>();
    Map<String, ILinkedResource> sousFamilleLinkedRessourceMapper = new HashMap<>();



    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("EnregistrerLEvaluationDuCollaborateur")){
            boolean objAne =  checkIfAllCompetencesSeted((ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("ObjAne"),"ResultatN1");
            if(objAne == false){
                //block and show message
                getResourceController().alert("Veuillez évaluer toutes les objectifs de l'année");
                return false;
            }

            boolean compMetier =  checkIfAllCompetencesSeted((ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("CompetencesGenerale"),"EvaluationRespN1");
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
                    String decisionEvaluateur = fixation.getValue("DecisionN1")!=null? (String) fixation.getValue("DecisionN1"):"";
                    if(decisionEvaluateur.equals("Validé")){
                        double importance = fixation.getValue("Importance") != null ? ((Number) fixation.getValue("Importance")).doubleValue() : 0;
                        sommeImportances += importance;
                    }


                }
                if(sommeImportances!=100){
                    getResourceController().alert("La totalité des pondérations attribuées aux objectifs validés doit équivaloir à 100");
                    return false;
                }
            }


        }
        return super.onBeforeSubmit(action);
    }



    boolean checkIfAllCompetencesSeted(ArrayList<ILinkedResource> resources, String checkedField){
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
    @Override
    public void onPropertyChanged(IProperty property) {
        if(property.getName().equals("CompetencesGenerale")){
            ArrayList<ILinkedResource> competences = (ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("CompetencesGenerale");
            if(competences!=null && !competences.isEmpty()){
                CalculateNotes(competences);
            }

        }
        /* else if(property.getName().equals("ObjAne")) {
           CalculateAtteinteObjectifAnnuel();
        }*/

        super.onPropertyChanged(property);
    }
    void calculateNoteGlobal(){
        ArrayList<ILinkedResource> notes = (ArrayList<ILinkedResource>) getWorkflowInstance().getValue("Notes");
        if(notes!=null && !notes.isEmpty()){
            float sommeAutoEval = 0;
            float sommeN1 = 0;
            for(ILinkedResource note : notes){
                float moyenneAutoEval = (Float) note.getValue("AutoEvaluation");
                float moyenneN1 = (Float) note.getValue("Evaluation");

                sommeAutoEval+=moyenneAutoEval;
                sommeN1+=moyenneN1;
            }
            float moyenneAutoEvalGlobal = sommeAutoEval / notes.size();
            float moyenneN1Global = sommeN1 / notes.size();

            getWorkflowInstance().setValue("AutoEvaluationGlobaleDeLaTenueDeFonction",moyenneAutoEvalGlobal);
            getWorkflowInstance().setValue("AutoEvaluationGlobaleDeLaTenueDeFonctionN1",moyenneN1Global);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        }
    }

    public CtlAbstractView refrechEcanPersonalisableRealisationCTD() {
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "EvalutionCollaborateur", ICatalog.IType.NORMAL,project);
            IView view =  getWorkflowModule().getView(getWorkflowModule().getSysadminContext(),catalog,"uril://vdoc/resourceDefinitionView/DefaultOrganization/EVAL/EvalutionCollaborateur:0/Notes/Notes_View");

        }catch (Exception e){

        }





        try {
            IDocumentComposite documentComposite = null;
            try {
                documentComposite = (IDocumentComposite) Navigator.getNavigator().getCurrentNavigation();
            } catch (ClassCastException e) {
                documentComposite = (IDocumentComposite) Navigator.getNavigator().getRootNavigator().getPartByName("ezs").getCurrentNavigation();
            }
            Field fieldEntries = CustomSubFormController.class.getDeclaredField("entries");
            fieldEntries.setAccessible(true);
            List entries = (List) fieldEntries.get(documentComposite.getBody());
            for (Object entry : entries) {
                if (entry instanceof BlockWidget) {
                    BlockWidget blockWidget = (BlockWidget) entry;
                    for (IWidget widget : blockWidget.getChildrenRecursively()) {
                        if (widget instanceof ScreenDescriptionComponent) {
                            Field fieldPart = ScreenDescriptionComponent.class.getDeclaredField("part");
                            fieldPart.setAccessible(true);
                            NavigationPart navigationPart = (NavigationPart) fieldPart.get(widget);
                            if (navigationPart.getCurrentNavigation() instanceof XMLView) {
                                XMLView xmlView = (XMLView) navigationPart.getCurrentNavigation();
                                // filter
                                if (xmlView.getName().equals("XMLViewProvider_uril://vdoc/resourceDefinitionView/DefaultOrganization/EVAL/EvalutionCollaborateur:0/Notes/Notes_View")) {
                                    IViewProvider provider = ((CtlAbstractView) xmlView).getProvider();
                                    if (provider instanceof ResourceViewProvider) {
                                        ((ResourceViewProvider) provider).loadModel();
                                        provider.init();
                                        provider.getColumns();
                                        ((CtlAbstractView) xmlView).refresh();
                                    }
                                    //return (CtlAbstractView)xmlView;
                                }
                            }
                        }
                    }
                }
            }
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            LOGGER.error("Can't find form embeded view!", e);
        }
        return null;
        //throw new NoSuchElementException("Unable to find view in current form with input configuration");
    }



    private void CalculateNotes(ArrayList<ILinkedResource> competences){
        Map<String, BigDecimal> globalNotes = calculateGlobalNotes(competences);
        String profilEvalue = "PROFIL"; //(String) ((IStorageResource) getWorkflowInstance().getValue("Profil2")).getValue("sys_Title");
        for (Map.Entry<String, BigDecimal> entry : globalNotes.entrySet()) {
            String key = entry.getKey();
            BigDecimal note = entry.getValue();
            note = note.setScale(2, BigDecimal.ROUND_DOWN);
            if(key.equals(profilEvalue)){
                getWorkflowInstance().setValue("EvaluationGlobaleRespN1",note);
            }else{
                ILinkedResource domaineRecupatilatifLinkedRessource = domaineLinkedRessourceMapper.get(key);
                if(domaineRecupatilatifLinkedRessource!=null){
                    domaineRecupatilatifLinkedRessource.setValue("Evaluation",note);
                    domaineRecupatilatifLinkedRessource.save(getWorkflowModule().getSysadminContext());
                }else{
                    ILinkedResource familleRecupatilatifLinkedRessource = familleLinkedRessourceMapper.get(key);
                    if(familleRecupatilatifLinkedRessource!=null){
                        familleRecupatilatifLinkedRessource.setValue("Evaluation",note);
                        familleRecupatilatifLinkedRessource.save(getWorkflowModule().getSysadminContext());
                    }else{
                        ILinkedResource sousFamilleRecupatilatifLinkedRessource = sousFamilleLinkedRessourceMapper.get(key);
                        if(sousFamilleRecupatilatifLinkedRessource!=null){
                            sousFamilleRecupatilatifLinkedRessource.setValue("Evaluation",note);
                            sousFamilleRecupatilatifLinkedRessource.save(getWorkflowModule().getSysadminContext());
                        }

                    }
                }



            }
            System.out.println("Global Note for " + entry.getKey() + ": " + entry.getValue());
        }
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
            BigDecimal valeur = competence.getValue("EvaluationRespN1")!=null?((BigDecimal) ((IStorageResource) competence.getValue("EvaluationRespN1")).getValue("Valeur")) : BigDecimal.ZERO;
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


    public void CalculateAtteinteObjectifAnnuel(IWorkflowInstance instance) {

        ArrayList<ILinkedResource> objectifsAnne = (ArrayList<ILinkedResource>) instance.getParentInstance().getLinkedResources("ObjAne");
        if(objectifsAnne!=null && !objectifsAnne.isEmpty()) {
            double sommeImportanceObjectif = 0;

            double sommeresultatCollaborateurMultiplyPoids = 0;
            double moyeneCollaborateur = 0;

            double sommeresultatN1MultiplyPoids = 0;
            double moyeneN1 = 0;

            for (ILinkedResource objectif : objectifsAnne) {
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
           // instance.getParentInstance().save(Modules.getWorkflowModule().getSysadminContext());

        }
    }

}
