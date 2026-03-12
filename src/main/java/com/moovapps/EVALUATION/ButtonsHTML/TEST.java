package com.moovapps.EVALUATION.ButtonsHTML;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.moovapps.EVALUATION.ButtonsHTML.CompetenceModel;
import com.moovapps.EVALUATION.Helpers.ExcelHelper;
import com.moovapps.EVALUATION.Helpers.ObjectifModel;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

import static com.axemble.vdoc.sdk.Modules.getDirectoryModule;
import static com.axemble.vdoc.sdk.Modules.getWorkflowModule;

public class TEST {
    HashMap<Float,Integer> positionHelper = new HashMap<>();

            //ArrayList of a model (id,parent id,notes , name childs of type model)

    public void generer(IWorkflowInstance workflowInstance, IResourceController resourceController) {
        String documentState = "";
        positionHelper.put(1f,1);
        positionHelper.put(2f,2);
        positionHelper.put(3f,3);
        positionHelper.put(4f,4);
        positionHelper.put(5f,5);
        try{
            ArrayList<CompetenceModel> data = buildDataForCompetencesExel((ArrayList<ILinkedResource>)workflowInstance.getLinkedResources("CompetencesGenerale"));
            data = updateAverage(data);
            System.out.println(data.toString());
            File excelFile = createFileFromFileCenterInLocalDrive("Fiches/fichier.xlsx","EVALUATION.xlsx",workflowInstance);
            Workbook workbook;

            if (excelFile.exists()) {
                // FileInputStream fis = new FileInputStream(excelFile);
                try (FileInputStream fis = new FileInputStream(excelFile)) {
                    workbook = new XSSFWorkbook(fis);
                    fis.close();
                }
            } else {
                workbook = new XSSFWorkbook();
            }

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null) {
                sheet = workbook.createSheet("Sheet1");
            }
            sheet.setDisplayGridlines(false);

            int rowIndex = 0; // Starting row index
            rowIndex = FillHeader(workbook, sheet, rowIndex); // Assuming FillTitle returns the updated rowIndex
            rowIndex = FillTitle(workbook, sheet, rowIndex,workflowInstance);
            HashMap<String,Object> infosCollab = new HashMap<>();
            infosCollab.put("Date de l’entretien : ",(Date) workflowInstance.getValue("DateEntretien"));
            infosCollab.put("Nom et prénom du collaborateur: ",((IUser) workflowInstance.getValue("CollaborateurEval")).getFullName());
            infosCollab.put("Service/ Activité :",workflowInstance.getValue("Service")!=null?((IStorageResource) workflowInstance.getValue("Service")).getValue("sys_Title"):"");
            infosCollab.put("Nom du Responsable hiérarchique direct : ",workflowInstance.getValue("ResponsableHierarchique")!=null?((IUser) workflowInstance.getValue("ResponsableHierarchique")).getFullName():"");
            infosCollab.put("Fonction : ",workflowInstance.getValue("Fonction")!=null?((IStorageResource) workflowInstance.getValue("Fonction")).getValue("sys_Title"):"");
            rowIndex = FillInfosCollaborateur(workbook,sheet,rowIndex,infosCollab);
            rowIndex = FillSimpleText(workbook,sheet,rowIndex,1,5,"Evenements","Caibri","BLACK",HorizontalAlignment.LEFT,VerticalAlignment.CENTER,12,false);
            rowIndex ++;
            LinkedHashMap<String,Object> evenements = new LinkedHashMap<>();
            evenements.put("Evenement","Détails");
            ArrayList<ILinkedResource> events = ( ArrayList<ILinkedResource> ) workflowInstance.getLinkedResources("Evenements");
            if(events!=null && !events.isEmpty()){
                for(ILinkedResource event : events){
                    evenements.put((String) event.getValue("Titre"),((String)event.getValue("Details")));

                }
            }

            if(evenements!=null && !evenements.isEmpty()){
                rowIndex = FillTable(workbook,sheet,rowIndex,evenements);
            }

            LinkedHashMap<String,Object> missionHashmap = new LinkedHashMap<>();
            missionHashmap.put("Mission","Détails");
            ArrayList<ILinkedResource> missions = ( ArrayList<ILinkedResource> ) workflowInstance.getLinkedResources("Missions");
            if(missions!=null && !missions.isEmpty()){
                for(ILinkedResource mission : missions){
                    missionHashmap.put((String) mission.getValue("Titre"),((String)mission.getValue("Details")));

                }
            }
            rowIndex = FillSimpleText(workbook,sheet,rowIndex,1,5,"Missions","Caibri","BLACK",HorizontalAlignment.LEFT,VerticalAlignment.CENTER,12,false);
            rowIndex++;
            if(missionHashmap!=null && !missionHashmap.isEmpty()){
                rowIndex = FillTable(workbook,sheet,rowIndex,missionHashmap);
            }
            rowIndex +=2;
            rowIndex = FillSimpleTextAndNotIncreaseIndex(workbook,sheet,rowIndex,1,5,"I – BILAN ANNUEL","Caibri","DARK_RED",HorizontalAlignment.LEFT,VerticalAlignment.CENTER,14,false);
            rowIndex++;
            rowIndex = FillSimpleTextAndNotIncreaseIndex(workbook,sheet,rowIndex,1,5,"      a) Réalisation des objectifs","Caibri","DARK_RED",HorizontalAlignment.LEFT,VerticalAlignment.CENTER,12,false);
            rowIndex ++;
            ArrayList<ObjectifModel> objectifs = new ArrayList<>();
            ArrayList<ILinkedResource> objectifsLR = ( ArrayList<ILinkedResource> ) workflowInstance.getLinkedResources("ObjAne");
            if(objectifsLR!=null && !objectifsLR.isEmpty()){
                for(ILinkedResource objectif : objectifsLR){
                    ObjectifModel objectifModel = new ObjectifModel(
                            (String) objectif.getValue("Objectif"),
                            objectif.getValue("ResultatN1")!=null?((Number) objectif.getValue("ResultatN1")).floatValue()+"":"",
                            (String) objectif.getValue("CommentaireRespHierarchique")
                    );
                    objectifs.add(objectifModel);

                }
            }

            rowIndex= FillDynamicTable(workbook,sheet,rowIndex,3,new ArrayList<>(
                    Arrays.asList(1, 6, 7,8,9,11)),new ArrayList<>(
                    Arrays.asList("Liste des objectifs définis", "Résultat (Note ou %)", "Explication du résultat")),objectifs); // Objectifs
            rowIndex = FillSimpleText(workbook,sheet,rowIndex,1,11,"Commentaires du Responsable hiérarchique sur la réalisation des objectifs","Caibri","BLACK",HorizontalAlignment.LEFT,VerticalAlignment.CENTER,12,false);
            rowIndex++;
            rowIndex = AddDynamicCell(workbook,sheet,rowIndex,rowIndex+3,1,11,(String) workflowInstance.getValue("CommentairesDuResponsableHierarchiqueSurLaRealisationDesObjectifs"),"Caibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,false,false);
            rowIndex = rowIndex+5;
            rowIndex = AddDynamicCell(workbook,sheet,rowIndex,rowIndex,1,11,"GRILLE D'EVALUATION","Caibri",16,"BLACK","GREY_25_PERCENT",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.CENTER,true,false);
            rowIndex++;
            rowIndex = AddDynamicCell(workbook,sheet,rowIndex,rowIndex+2,1,5,"","",16,"","",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.CENTER,true,true);
            rowIndex = AddDynamicCell(workbook,sheet,rowIndex,rowIndex+2,6,6,"Moyenne pondérée/5","Caibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.CENTER,true,true);
            rowIndex = AddDynamicCell(workbook,sheet,rowIndex,rowIndex+2,7,7,"Point d'amélioration prioritaire","Caibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.CENTER,true,true);
            rowIndex = AddDynamicCell(workbook,sheet,rowIndex,rowIndex+2,8,8,"En-dessous du niveau attendu","Caibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.CENTER,true,true);
            rowIndex = AddDynamicCell(workbook,sheet,rowIndex,rowIndex+2,9,9,"Atteinte du niveau attendu","Caibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.CENTER,true,true);
            rowIndex = AddDynamicCell(workbook,sheet,rowIndex,rowIndex+2,10,10,"Au-dessus du niveau attendu","Caibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.CENTER,true,true);
            rowIndex = AddDynamicCell(workbook,sheet,rowIndex,rowIndex+2,11,11,"Excellence","Caibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.CENTER,true,true);
            rowIndex = rowIndex+3;


            rowIndex = fillCompetenceCHATGPT(data,workbook,sheet,rowIndex,workflowInstance);
            rowIndex++;
            rowIndex++;
            rowIndex = FillSimpleTextAndNotIncreaseIndex(workbook,sheet,rowIndex,1,5,"II – ACTION À METTRE EN ŒUVRE","Caibri","DARK_RED",HorizontalAlignment.LEFT,VerticalAlignment.CENTER,14,false);
            rowIndex++;
            rowIndex = FillSimpleTextAndNotIncreaseIndex(workbook,sheet,rowIndex,1,5,"      a)  Nouveaux objectifs annuels","Caibri","DARK_RED",HorizontalAlignment.LEFT,VerticalAlignment.CENTER,12,false);
            rowIndex ++;
            rowIndex ++;
            ArrayList<ObjectifModel> fixationObjectifs = new ArrayList<>();
            ArrayList<ILinkedResource> fixationObjectifsLR = ( ArrayList<ILinkedResource> ) workflowInstance.getLinkedResources("ObjFix");
            if(fixationObjectifsLR!=null && !fixationObjectifsLR.isEmpty()){
                for(ILinkedResource objectif : fixationObjectifsLR){
                    fixationObjectifs.add(new ObjectifModel((String) objectif.getValue("Objectif"), objectif.getValue("Tage")+"",(String) objectif.getValue("Delais")));

                }
            }

            rowIndex= FillDynamicTable(workbook,sheet,rowIndex,3,new ArrayList<>(
                    Arrays.asList(1, 6, 7,8,9,11)),new ArrayList<>(
                    Arrays.asList("Liste des objectifs définis pour l'année suivante", "%Tage", "Dèlais")),fixationObjectifs); // Objectifs
            rowIndex = FillSimpleTextAndNotIncreaseIndex(workbook,sheet,rowIndex,1,5,"      b)  Plan de développement personnel","Caibri","DARK_RED",HorizontalAlignment.LEFT,VerticalAlignment.CENTER,12,false);
            rowIndex++;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex,1,11,"Fixation du Plan de Développement pour la période à venir :","Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.CENTER,true,false);
            rowIndex++;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex,1,11,"Le Responsable Hiérarchique identifie et valide avec le salarié la (ou les) principale(s) Compétence(s) que le salarié doit développer (point de progrès et/ou  point fort à optimiser, acté(s) au cours de la partie  « Analyse des compétences »).","Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,false,false);
            rowIndex ++;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+2,1,11,(String)workflowInstance.getValue("Accompagnement"),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,false,false);
            rowIndex += 3;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex,1,5,"Plan de développement professionnel","Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex,6,11,"Note","Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
            rowIndex++;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+4,1,5,buildMobilite(workflowInstance),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,false,false);
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+4,6,11,(String) workflowInstance.getValue("NoteMobilite"),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,false,false);
            rowIndex+=5;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+4,1,5,buildChangementPoste(workflowInstance),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,false,false);
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+4,6,11,(String) workflowInstance.getValue("NoteChangementPoste"),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,false,false);
            rowIndex+=5;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+4,1,5,buildEvolutionFonction(workflowInstance),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,false,false);
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+4,6,11,(String) workflowInstance.getValue("NoteEvolutionFonction"),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,false,false);
            rowIndex+=5;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+5,1,5,"Si oui, le Responsable Hiérarchique note le souhait exprimé par le salarié et le commente (rappeler les compétences nécessaires pour occuper la fonction, compétences indiquées dans la Définition de Fonction,  et valider si le  salarié possède ces compétences.","Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,false,false);
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+5,6,11," ","Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,false,false);
            rowIndex+=7;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+5,1,11,"Commentaires du Salarié : "+workflowInstance.getValue("CommentairesDuSalarie")!=null?(String) workflowInstance.getValue("CommentairesDuSalarie"):"","Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
            rowIndex+=7;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+5,1,11,"Commentaires du Résponsable hierarchique : "+workflowInstance.getValue("CommentairesDuResponsableHierarchique")!=null?(String) workflowInstance.getValue("CommentairesDuResponsableHierarchique"):"","Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
            rowIndex+=7;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+5,1,11,"Commentaires du Manager N+2 : "+workflowInstance.getValue("CommentairesDuManagerN2")!=null?(String) workflowInstance.getValue("CommentairesDuManagerN2"):"","Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
            // rowIndex=+7;
            try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                workbook.write(fileOut);
                fileOut.close();
                workbook.close();
            }




            File file = new File("c://TEST//EVALUATION.xlsx");
            ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
            IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
            tmpAttachementCollection.add(tmpJournalPaieAttachment);
            workflowInstance.setValue("FicheEvaluationCollaborateur", tmpAttachementCollection);
            workflowInstance.save(getWorkflowModule().getSysadminContext());
            File fileToCopyTo = new File("c://TEST//EVALUATION2.xlsx");
          //  MakeCopy(file,fileToCopyTo);
            file.delete();

        }catch (Exception e){
            File file = new File("c://TEST//EVALUATION.xlsx");
            file.delete();
            e.printStackTrace();
        }

    }

    private ArrayList<CompetenceModel> updateAverage(ArrayList<CompetenceModel> data) {
        ArrayList<String> familleTraited = new ArrayList<>();
        ArrayList<String> sousFamilleTraited = new ArrayList<>();

        for(CompetenceModel item : data){
            String categorie = item.getCategorie();
            if(categorie.equals("FAMILLE")){
                if(!familleTraited.contains(item.getId())){
                    ArrayList<Float> familleNotes = item.getNotes();
                    item.setNotes(updateFamilleAverage(item.getId(),familleNotes));
                    familleTraited.add(item.getId());
                }

            }else if(categorie.equals("SOUSFAMILLE")){
                if(!sousFamilleTraited.contains(item.getId())){
                    ArrayList<Float> familleNotes = item.getNotes();
                    item.setNotes(updateSousFamilleAverage(familleNotes));
                    sousFamilleTraited.add(item.getId());
                }

            }
        }

        return data;
    }

    private ArrayList<Float> updateSousFamilleAverage(ArrayList<Float> familleNotes) {
        Float total = familleNotes.get(0);
        Float somme = 0f;
        for(int i=1 ; i<familleNotes.size();i++){
            somme+= familleNotes.get(i);
        }
        familleNotes.set(0,total/somme);
        return familleNotes;
    }

    private ArrayList<Float> updateFamilleAverage(String familleID, ArrayList<Float> familleNotes) {
        int familleRepititionTime = objectRepetition.get(familleID);
        Float somme = 0f;
        for(Float note : familleNotes){
            somme+=note;

        }
        for (int i = 0; i < familleNotes.size(); i++) {
            familleNotes.set(i, somme/familleRepititionTime);

        }

        return familleNotes;
    }

    private ArrayList<CompetenceModel> buildDataForCompetencesExel(ArrayList<ILinkedResource> competences){
        ArrayList<CompetenceModel> data = new ArrayList<>();
            ArrayList<String> familleTraited = new ArrayList<>();
            ArrayList<String> sousFamilleTraited = new ArrayList<>();
        for(ILinkedResource competenceTD : competences){

            IStorageResource familleCompetence = (IStorageResource) competenceTD.getValue("FamilleCompetence");
            IStorageResource sousFamilleCompetence = (IStorageResource) competenceTD.getValue("SousFamilleCompetence");
            IStorageResource competence = (IStorageResource) competenceTD.getValue("Competence");
            IStorageResource notation = (IStorageResource) competenceTD.getValue("AutoEvaluation");
            Float valeur = notation!=null? (Float)notation.getValue("Valeur"):0f;
            if(!familleTraited.contains(familleCompetence.getId().toString())){
                fillFamilleCompetence(familleCompetence,data);
                familleTraited.add(familleCompetence.getId().toString());
            }
            if(!sousFamilleTraited.contains(sousFamilleCompetence.getId().toString())){
                fillSousFamilleCompetence(familleCompetence,sousFamilleCompetence,data);
                sousFamilleTraited.add(sousFamilleCompetence.getId().toString());
            }
            fillCompetence(familleCompetence,sousFamilleCompetence,competence,valeur,data);

        }

        return data;
    }

    private void fillCompetence(IStorageResource familleCompetence, IStorageResource sousFamilleCompetence, IStorageResource competence, Float valeur, ArrayList<CompetenceModel> data) {


        String id = competence.getId().toString();
        String parentID = sousFamilleCompetence.getId().toString();
        String name = (String) competence.getValue("sys_Title");
        ArrayList<Float> notes = new ArrayList<Float>(
                Arrays.asList(0f, 0f, 0f,0f,0f,0f));
        notes.set(positionHelper.get(valeur),valeur);
    //    ArrayList<CompetenceModel> childs = new ArrayList<>();

        CompetenceModel competenceModel = new CompetenceModel(id,parentID,name,notes,null,"COMPETENCE");
        data.add(competenceModel);
        objectRepetition.put(familleCompetence.getId().toString(),objectRepetition.get(familleCompetence.getId().toString())!=null?objectRepetition.get(familleCompetence.getId().toString())+1:1);

        updateSousFamilleCompetenceAndAddChilds(parentID,valeur,competenceModel);
        updateFamilleNotes(familleCompetence,valeur);
        // save repitition
    }

    private void updateSousFamilleCompetenceAndAddChilds(String parentID , Float valeur , CompetenceModel competenceModel){
        CompetenceModel sousFamille = idToSousFamilleCompetenceMap.get(parentID);
        ArrayList<Float> sousFamilleNotes = sousFamille.getNotes();
        sousFamilleNotes.set(0,sousFamilleNotes.get(0)+valeur);
        sousFamilleNotes.set(positionHelper.get(valeur),sousFamilleNotes.get(positionHelper.get(valeur))+1);
        ArrayList<CompetenceModel> sousFamilleChilds = sousFamille.getChilds();
        sousFamilleChilds.add(competenceModel);
    }

    private void updateFamilleNotes(IStorageResource familleCompetence,Float valeur){
        CompetenceModel famille = idToFamilleCompetenceMap.get(familleCompetence.getId().toString());
        ArrayList<Float> familleNotes = famille.getNotes();
        familleNotes.set(positionHelper.get(valeur), familleNotes.get(positionHelper.get(valeur))+valeur);
    }
    Map<String, CompetenceModel> idToSousFamilleCompetenceMap = new HashMap<>(); // New map
    Map<String, Integer> objectRepetition = new HashMap<>(); // New map
    Map<String, CompetenceModel> idToFamilleCompetenceMap = new HashMap<>(); // New map

    private void fillSousFamilleCompetence(IStorageResource familleCompetence, IStorageResource sousFamilleCompetence, ArrayList<CompetenceModel> data) {
        String id = sousFamilleCompetence.getId().toString();
        String parentID = familleCompetence.getId().toString();
        String name = (String) sousFamilleCompetence.getValue("sys_Title");
        String categorie = "SOUSFAMILLE";
        ArrayList<Float> notes = new ArrayList<Float>(
                Arrays.asList(0f, 0f, 0f,0f,0f,0f));
        ArrayList<CompetenceModel> childs = new ArrayList<>();

        CompetenceModel sousFamilleCompetenceModel = new CompetenceModel(id,parentID,name,notes,childs,categorie);
        data.add(sousFamilleCompetenceModel);

        idToSousFamilleCompetenceMap.put(sousFamilleCompetenceModel.getId(), sousFamilleCompetenceModel);

        CompetenceModel famille = idToFamilleCompetenceMap.get(familleCompetence.getId().toString());
        ArrayList<CompetenceModel> familleChilds =  famille.getChilds();
        familleChilds.add(sousFamilleCompetenceModel);
        famille.setChilds(familleChilds);

    }

    private void fillFamilleCompetence(IStorageResource familleCompetence, ArrayList<CompetenceModel> data) {
        String id = familleCompetence.getId().toString();
        String parentID = "";
        String name = (String) familleCompetence.getValue("sys_Title");
        String categorie = "FAMILLE";
        ArrayList<Float> notes = new ArrayList<Float>(
                Arrays.asList(0f, 0f, 0f,0f,0f,0f));
        ArrayList<CompetenceModel> childs = new ArrayList<>();

        CompetenceModel familleCompetenceModel = new CompetenceModel(id,parentID,name,notes,childs,categorie);
        data.add(familleCompetenceModel);
        idToFamilleCompetenceMap.put(familleCompetence.getId().toString(),familleCompetenceModel);



    }

    int fillCompetenceCHATGPT(ArrayList<CompetenceModel> data, Workbook workbook, Sheet sheet, int rowIndex, IWorkflowInstance instance) {
        ArrayList<CompetenceModel> familles = new ArrayList<>();
        ArrayList<String> familleTraited = new ArrayList<>();
        for (CompetenceModel item : data) {
            if ((item.getParentID() == null || item.getParentID().equals("")) && !familleTraited.contains(item.getId())) {
                rowIndex = processFamille(workbook, sheet, rowIndex, item);
                ArrayList<CompetenceModel> sousFamilles = item.getChilds();
                for (CompetenceModel sousFamille : sousFamilles) {
                        rowIndex = processSousFamille(workbook, sheet, rowIndex, sousFamille);
                         ArrayList<CompetenceModel> competences = sousFamille.getChilds();
                        for(CompetenceModel competence : competences){
                            rowIndex = processCompetence(workbook, sheet, rowIndex, competence);
                        }

                }
                familles.add(item);
                familleTraited.add(item.getId());
            }
        }
        Float totalFamille = 0f;
        for(CompetenceModel famille : familles){
            totalFamille += (famille.getNotes().get(0)!=null?famille.getNotes().get(0):0f);
            rowIndex = processApperciation(workbook, sheet, rowIndex, famille);
        }
        Float moyenne = totalFamille/familles.size();
        rowIndex = processEvaluationGlobal(workbook, sheet, rowIndex, moyenne);

        // Calculate the total for all families and process "Appréciation" for each family
        /*for (String familleId : familleAppreciationData.keySet()) {
            float total = getTotal(competenceData.get(familleId));

            rowIndex++; // Move to the next row
            NotesModel familleObject = familleAppreciationData.get(familleId);
            rowIndex = processFamilleTotal(workbook, sheet, rowIndex, familleObject, total);
        }*/

        return rowIndex + 2;
    }

    private int processFamille(Workbook workbook, Sheet sheet, int rowIndex, CompetenceModel famille) {
       // rowIndex++;
        AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 1, 5, famille.getName(), "Arial", 16, "BLACK", "GREY_25_PERCENT", true, true, true, true, HorizontalAlignment.CENTER, VerticalAlignment.TOP, true, false);
        AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 6, 11, famille.getNotes().get(0).toString(), "Arial", 16, "BLACK", "GREY_25_PERCENT", true, true, true, true, HorizontalAlignment.CENTER, VerticalAlignment.TOP, true, false);
        return rowIndex + 2;
    }

    private int processSousFamille(Workbook workbook, Sheet sheet, int rowIndex, CompetenceModel sousFamille) {
       // rowIndex++;
        AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 1, 5, sousFamille.getName(), "Calibri", 12, "WHITE", "RED", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        for (int i = 0; i < 6; i++) {
            AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 6 + i, 6 + i, sousFamille.getNotes().get(i).toString(), "Calibri", 12, "WHITE", "RED", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        }
        return rowIndex + 2;
    }
    private String XorNull(Float number){
        if(number==null)return "".replace("\"", "");
        if(number>0){
            return "X".replace("\"", "");
        }else{
            return "".replace("\"", "");
        }
    }
    private int processCompetence(Workbook workbook, Sheet sheet, int rowIndex, CompetenceModel competence) {
      //  rowIndex++;
        AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 1, 5, competence.getName(), "Calibri", 12, "BLACK", "", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        for (int i = 0; i < 6; i++) {
            AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 6 + i, 6 + i, XorNull(competence.getNotes().get(i)), "Calibri", 12, "BLACK", "", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        }
        return rowIndex + 2;
    }

    private int processApperciation(Workbook workbook, Sheet sheet, int rowIndex, CompetenceModel famille) {
       // rowIndex++;
        AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 1, 5, "Appréciation des "+famille.getName(), "Calibri", 12, "BLACK", "BRIGHT_GREEN", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 6, 11, famille.getNotes().get(0).toString(), "Calibri", 12, "BLACK", "BRIGHT_GREEN", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        return rowIndex + 2;
    }

    private int processEvaluationGlobal(Workbook workbook, Sheet sheet, int rowIndex, Float moyenne) {
       // rowIndex++;
        AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 1, 5, "Evaluation globale", "Calibri", 12, "WHITE", "RED", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 6, 11, moyenne.toString(), "Calibri", 12, "WHITE", "BLACK", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        return rowIndex + 2;
    }

    int AddDynamicCell(Workbook workbook, Sheet sheet, int rowStart ,int rowEnd ,int columnStart,int columnEnd,String value , String fontName , int fontSize , String fontColor , String backGroundColor , boolean leftBorder , boolean rightBorder , boolean topBorder , boolean bottomBorder , HorizontalAlignment h , VerticalAlignment v , boolean isBold , boolean isItalic){
        ExcelHelper.createDynamicCell(sheet,rowStart,rowEnd,columnStart,columnEnd,value,fontName,fontSize,fontColor,backGroundColor,leftBorder,rightBorder,topBorder,bottomBorder,h,v,isBold,isItalic);
        return rowStart;
    }

    private File createFileFromFileCenterInLocalDrive(String filePath, String fileName , IWorkflowInstance instance) {
        try {
            ILibraryModule libraryModule = Modules.getLibraryModule();
            IContext context = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            ILibrary library = libraryModule.getLibrary(context, organization, "Eval");
            if (library != null) {
                IFile file = libraryModule.getFileByPath(context, library, filePath);
                if (file == null) {
                    Modules.getWorkflowModule().getResourceController(instance).alert("Le fichier de modele n'existe pas dans l'espace documentaire...Vieullez contacter votre administrateur");
                }else{
                    String[] filePathDivided = filePath.split("/");
                    IAttachment attachment = libraryModule.getAttachment(file, filePathDivided[filePathDivided.length - 1]);
                    File tmpFile = new File("c://TEST//" + fileName);
                    FileUtils.writeByteArrayToFile(tmpFile, attachment.getContent());
                    return tmpFile;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    int FillSimpleText(Workbook workbook,Sheet sheet,int rowIndex , int firstColumn , int lastColumn,String Text,String fontName , String fontColor , HorizontalAlignment h ,VerticalAlignment v , int fontSize ,boolean addBorder){
        List<Cell>  cells =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex,firstColumn,lastColumn,Text);
        CellUtil.setAlignment(cells.get(0), h);
        CellUtil.setVerticalAlignment(cells.get(0), v);
        if(addBorder){
            for(Cell cell : cells){
                ExcelHelper.addBorder(workbook,cell);
            }
        }

        ExcelHelper.setFontSizeAndColor(workbook,cells.get(0),fontSize,fontName,fontColor,true,false);
        return rowIndex;
    }
    int FillSimpleTextAndNotIncreaseIndex(Workbook workbook,Sheet sheet,int rowIndex , int firstColumn , int lastColumn,String Text,String fontName , String fontColor , HorizontalAlignment h ,VerticalAlignment v , int fontSize ,boolean addBorder){
        List<Cell>  cells =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex,firstColumn,lastColumn,Text);
        CellUtil.setAlignment(cells.get(0), h);
        CellUtil.setVerticalAlignment(cells.get(0), v);
        if(addBorder){
            for(Cell cell : cells){
                ExcelHelper.addBorder(workbook,cell);
            }
        }

        ExcelHelper.setFontSizeAndColor(workbook,cells.get(0),fontSize,fontName,fontColor,true,false);
        return rowIndex;
    }


    int FillDynamicTable(Workbook workbook, Sheet sheet, int rowIndex ,int columnNumbers,ArrayList<Integer>  fromTo,ArrayList<String> headerTitles,ArrayList<ObjectifModel> objectifs){
        rowIndex =  FillHeaderDynamiclly(workbook,  sheet,  rowIndex , columnNumbers,fromTo,headerTitles);
        if(objectifs!=null && !objectifs.isEmpty()){
            for(ObjectifModel objectif : objectifs){
                ArrayList<String> columnValues = new ArrayList<>();
                columnValues.add(objectif.getObjectif());
                columnValues.add(objectif.getResultat().toString());
                columnValues.add(objectif.getExplication());
                rowIndex = FillRowDynamiclly(workbook,sheet,rowIndex,columnNumbers,fromTo,columnValues);
                //  rowIndex++;
            }
        }


        return rowIndex+1;
    }

    int FillHeaderDynamiclly(Workbook workbook, Sheet sheet, int rowIndex ,int columnNumbers,ArrayList<Integer>  fromTo,ArrayList<String> headerTitles){
        int splitFactor = fromTo.size() / columnNumbers;
        List<ArrayList<Integer>> ranges = splitArrayList(fromTo,splitFactor);
        for(ArrayList<Integer> range : ranges){
            List<Cell>  cellsLibele =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex,range.get(0),range.get(1),headerTitles.get(ranges.indexOf(range)));
            for(Cell cell : cellsLibele){
                ExcelHelper.addBorder(workbook,cell);
            }
            CellUtil.setAlignment(cellsLibele.get(0), HorizontalAlignment.CENTER);
            CellUtil.setVerticalAlignment(cellsLibele.get(0), VerticalAlignment.CENTER);
            ExcelHelper.setFontSizeAndColor(workbook,cellsLibele.get(0),12,"Calibri","BLACK",true,false);
        }

        return rowIndex+1;
    }

    int FillRowDynamiclly(Workbook workbook, Sheet sheet, int rowIndex ,int columnNumbers,ArrayList<Integer>  fromTo,ArrayList<String> columnsValues){
        int splitFactor = fromTo.size() / columnNumbers;
        List<ArrayList<Integer>> ranges = splitArrayList(fromTo,splitFactor);
        for(ArrayList<Integer> range : ranges){
            List<Cell>  cellsLibele =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex,range.get(0),range.get(1),columnsValues.get(ranges.indexOf(range)));
            for(Cell cell : cellsLibele){
                ExcelHelper.addBorder(workbook,cell);
            }
            CellUtil.setAlignment(cellsLibele.get(0), HorizontalAlignment.LEFT);
            CellUtil.setVerticalAlignment(cellsLibele.get(0), VerticalAlignment.CENTER);
            ExcelHelper.setFontSizeAndColor(workbook,cellsLibele.get(0),12,"Calibri","BLACK",false,false);
        }

        return rowIndex+1;
    }

    public static List<ArrayList<Integer>> splitArrayList(ArrayList<Integer> originalList, int size) {
        List<ArrayList<Integer>> result = new ArrayList<>();

        for (int i = 0; i < originalList.size(); i += size) {
            int toIndex = Math.min(i + size, originalList.size());
            List<Integer> sublist = originalList.subList(i, toIndex);
            result.add(new ArrayList<>(sublist));
        }
        return result;
    }

    int FillTable(Workbook workbook, Sheet sheet, int rowIndex , LinkedHashMap<String,Object> data){
        boolean isFirstRow = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {

            String libelle = entry.getKey();
            Object value = entry.getValue();
            List<Cell>  cellsLibele =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex,1,5,libelle);
            for(Cell cell : cellsLibele){
                ExcelHelper.addBorder(workbook,cell);
            }
            if(isFirstRow){
                ExcelHelper.setFontSizeAndColor(workbook,cellsLibele.get(0),12,"Calibri","BLACK",true,false);
                CellUtil.setAlignment(cellsLibele.get(0), HorizontalAlignment.CENTER);
                CellUtil.setVerticalAlignment(cellsLibele.get(0), VerticalAlignment.CENTER);
                List<Cell>  cellsValue =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex,6,11,value.toString());
                for(Cell cell : cellsValue){
                    ExcelHelper.addBorder(workbook,cell);
                }
                CellUtil.setAlignment(cellsValue.get(0), HorizontalAlignment.CENTER);
                CellUtil.setVerticalAlignment(cellsValue.get(0), VerticalAlignment.CENTER);
                ExcelHelper.setFontSizeAndColor(workbook,cellsValue.get(0),12,"Calibri","BLACK",true,false);
            }else{
                ExcelHelper.setFontSizeAndColor(workbook,cellsLibele.get(0),12,"Calibri","BLACK",false,false);
                CellUtil.setAlignment(cellsLibele.get(0), HorizontalAlignment.LEFT);
                CellUtil.setVerticalAlignment(cellsLibele.get(0), VerticalAlignment.CENTER);
                List<Cell>  cellsValue =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex,6,11,value.toString());
                for(Cell cell : cellsValue){
                    ExcelHelper.addBorder(workbook,cell);
                }
                CellUtil.setAlignment(cellsValue.get(0), HorizontalAlignment.RIGHT);
                CellUtil.setVerticalAlignment(cellsValue.get(0), VerticalAlignment.CENTER);
                ExcelHelper.setFontSizeAndColor(workbook,cellsValue.get(0),12,"Calibri","BLACK",false,false);
            }
            isFirstRow = false;
            rowIndex ++;
        }


        return rowIndex+2;
    }

    String buildMobilite(IWorkflowInstance instance){
        String mobilite = instance.getValue("MobiliteGeographique")!=null?(String) instance.getValue("MobiliteGeographique"):"";
        if(mobilite.trim().toLowerCase().equals("oui")){
            IStorageResource mobiliteStorage = (IStorageResource) instance.getValue("Mobilite2");
            if(mobiliteStorage!=null){
                mobilite ="Oui : "+ (String) mobiliteStorage.getValue("sys_Title");
                if(mobilite.trim().toLowerCase().equals("autre")){
                    mobilite ="Oui : "+ (String) instance.getValue("AutreMobilite");
                }
            }
        }else{
            mobilite = "Mobilité géographique : Non";
        }
        return mobilite;
    }

    String buildChangementPoste(IWorkflowInstance instance){
        String souhaitDeChangementDePoste = instance.getValue("SouhaitDeChangementDePoste")!=null?(String) instance.getValue("SouhaitDeChangementDePoste"):"";
        if(souhaitDeChangementDePoste.trim().toLowerCase().equals("oui")){
            souhaitDeChangementDePoste ="Souhait de changement de poste  : Oui";

        }else{
            souhaitDeChangementDePoste ="Souhait de changement de poste  : Non";
        }
        return souhaitDeChangementDePoste;
    }

    String buildEvolutionFonction(IWorkflowInstance instance){
        String souhaitDEvolutionDeFonction = instance.getValue("SouhaitDEvolutionDeFonction")!=null?(String) instance.getValue("SouhaitDEvolutionDeFonction"):"";
        if(souhaitDEvolutionDeFonction.trim().toLowerCase().equals("oui")){
            souhaitDEvolutionDeFonction ="Souhait d’évolution de fonction  : Oui";

        }else{
            souhaitDEvolutionDeFonction ="Souhait d’évolution de fonction  : Non";
        }
        return souhaitDEvolutionDeFonction;
    }

    int FillHeader(Workbook workbook,Sheet sheet,int rowIndex){
        List<Cell>  cells =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex+2,1,5,"TEST");
        for(Cell cell : cells){
            ExcelHelper.addBorder(workbook,cell);
        }

        CellUtil.setAlignment(cells.get(0), HorizontalAlignment.LEFT);
        CellUtil.setVerticalAlignment(cells.get(0), VerticalAlignment.CENTER);

        List<Cell>  cells2 =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex+2,6,10,"Support d'appréciation Annuelle");
        for(Cell cell : cells2){
            ExcelHelper.addBorder(workbook,cell);
        }
        ExcelHelper.setFontSizeAndColor(workbook,cells2.get(0),18,"Century Gothic","BLACK",true,false);
        CellUtil.setAlignment(cells2.get(0), HorizontalAlignment.CENTER);
        CellUtil.setVerticalAlignment(cells2.get(0), VerticalAlignment.CENTER);



        Cell cell = ExcelHelper.createCell(sheet,rowIndex,11);
        cell.setCellValue("PC_R04_PR07");
        ExcelHelper.addBorder(workbook,cell);
        Cell cell2 = ExcelHelper.createCell(sheet,rowIndex+1,11);
        cell2.setCellValue("Version 2");
        ExcelHelper.addBorder(workbook,cell2);
        Cell cell3 = ExcelHelper.createCell(sheet,rowIndex+2,11);
        cell3.setCellValue("Page 1 sur 1");
        ExcelHelper.addBorder(workbook,cell3);


        return rowIndex+5;
    }

    int FillTitle(Workbook workbook,Sheet sheet,int rowIndex , IWorkflowInstance workflowInstance){
        List<Cell>  cells =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex,1,11,"ENTRETIEN ANNUEL D’ÉVALUATION ANNÉE "+((Number) workflowInstance.getValue("AnneeDEvaluation")).intValue());
        for(Cell cell : cells){
            ExcelHelper.addBorder(workbook,cell);
        }
        ExcelHelper.setFontSizeAndColor(workbook,cells.get(0),16,"Calibri","BLACK",true,false);
        CellUtil.setAlignment(cells.get(0), HorizontalAlignment.CENTER);
        CellUtil.setVerticalAlignment(cells.get(0), VerticalAlignment.CENTER);

        return rowIndex+2;
    }

    int FillInfosCollaborateur(Workbook workbook, Sheet sheet, int rowIndex , HashMap<String,Object> infos){

        for (Map.Entry<String, Object> entry : infos.entrySet()) {
            String libelle = entry.getKey();
            Object value = entry.getValue();
            List<Cell>  cellsLibele =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex,1,5,libelle!=null?libelle:"");
            for(Cell cell : cellsLibele){
                ExcelHelper.addBorder(workbook,cell);
            }
            ExcelHelper.setFontSizeAndColor(workbook,cellsLibele.get(0),12,"Calibri","BLACK",true,false);
            CellUtil.setAlignment(cellsLibele.get(0), HorizontalAlignment.LEFT);
            CellUtil.setVerticalAlignment(cellsLibele.get(0), VerticalAlignment.CENTER);
            List<Cell>  cellsValue =  ExcelHelper.mergeCellsAndSetValues(sheet,rowIndex,rowIndex,6,11,value!=null?value.toString():"");
            for(Cell cell : cellsValue){
                ExcelHelper.addBorder(workbook,cell);
            }
            CellUtil.setAlignment(cellsValue.get(0), HorizontalAlignment.RIGHT);
            CellUtil.setVerticalAlignment(cellsValue.get(0), VerticalAlignment.CENTER);
            ExcelHelper.setFontSizeAndColor(workbook,cellsValue.get(0),12,"Calibri","BLACK",false,false);
            rowIndex ++;
        }


        return rowIndex+2;
    }
}
