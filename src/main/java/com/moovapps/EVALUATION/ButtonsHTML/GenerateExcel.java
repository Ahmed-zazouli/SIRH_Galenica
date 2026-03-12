package com.moovapps.EVALUATION.ButtonsHTML;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moovapps.EVALUATION.Helpers.ExcelHelper;
import com.moovapps.EVALUATION.Helpers.ObjectifModel;
import com.moovapps.EVALUATION.Models.NotesModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.util.*;

import static com.axemble.vdoc.sdk.Modules.getDirectoryModule;
import static com.axemble.vdoc.sdk.Modules.getWorkflowModule;

public class GenerateExcel {
    ExcelHelper excelHelper = new ExcelHelper();

    public void generer(IWorkflowInstance workflowInstance, IResourceController resourceController){
        try {
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


            ArrayList<ILinkedResource> competences = (ArrayList<ILinkedResource>) workflowInstance.getLinkedResources("CompetencesGenerale");
            ArrayList<NotesModel> data =  buildFamilleArrayCHATGPT(competences);
            addSousFamilleToArrayCHATGPT(competences,data);
            addCompetenceToArrayCHATGPT(competences,data);





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
           /* fixationObjectifs.add(new ObjectifModel("Objectif 1",3,"Explication 1"));
            fixationObjectifs.add(new ObjectifModel("Objectif 2",4,"Explication 2"));
            fixationObjectifs.add(new ObjectifModel("Objectif 3",1,"Explication 3"));*/
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
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+5,1,11,"Commentaires du Salarié : "+(String) workflowInstance.getValue("CommentairesDuSalarie"),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
            rowIndex+=7;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+5,1,11,"Commentaires du Résponsable hierarchique : "+(String) workflowInstance.getValue("CommentairesDuResponsableHierarchique"),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
            rowIndex+=7;
            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+5,1,11,"Commentaires du Manager N+2 : "+(String) workflowInstance.getValue("CommentairesDuManagerN2"),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
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
            MakeCopy(file,fileToCopyTo);
            file.delete();
            //fileToCopyTo.delete();




        } catch (IOException e) {
            File file = new File("c://TEST//EVALUATION.xlsx");
            file.delete();
            e.printStackTrace();
        } catch (Exception e) {

            File file = new File("c://TEST//EVALUATION.xlsx");
            file.delete();
            e.printStackTrace();
        }


    }

    public static void MakeCopy(File file1, File file2) {
        FileInputStream sourceFile = null;
        FileInputStream targetFile = null;
        FileOutputStream outputStream = null;
        Workbook sourceWorkbook = null;
        Workbook targetWorkbook = null;

        try {
            sourceFile = new FileInputStream(file1);
            targetFile = new FileInputStream(file2);

            sourceWorkbook = new XSSFWorkbook(sourceFile);
            targetWorkbook = new XSSFWorkbook(targetFile);


            Sheet sourceSheet = sourceWorkbook.getSheetAt(0);
            Sheet targetSheet = targetWorkbook.createSheet();

            for (int rowIndex = 0; rowIndex <= sourceSheet.getLastRowNum(); rowIndex++) {
                Row sourceRow = sourceSheet.getRow(rowIndex);
                Row targetRow = targetSheet.createRow(rowIndex);

                if (sourceRow != null) {
                    for (int columnIndex = 0; columnIndex < sourceRow.getLastCellNum(); columnIndex++) {
                        Cell sourceCell = sourceRow.getCell(columnIndex);
                        if (sourceCell != null) {
                            Cell targetCell = targetRow.createCell(columnIndex, sourceCell.getCellType());

                            // Copy the cell value
                            switch (sourceCell.getCellType()) {
                                case NUMERIC:
                                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                                    break;
                                case STRING:
                                    targetCell.setCellValue(sourceCell.getStringCellValue());
                                    break;
                                // Handle other cell types as needed
                                default:
                                    break;
                            }

                            // Copy cell style
                            CellStyle sourceCellStyle = sourceCell.getCellStyle();
                            CellStyle targetCellStyle = targetWorkbook.createCellStyle();
                            targetCellStyle.cloneStyleFrom(sourceCellStyle);
                            targetCell.setCellStyle(targetCellStyle);

                            // Handle merged cells
                            for (CellRangeAddress mergedRegion : sourceSheet.getMergedRegions()) {
                                if (mergedRegion.isInRange(rowIndex, columnIndex)) {
                                    // Check if the merged region already exists in the targetSheet
                                    boolean mergedRegionExists = false;
                                    for (CellRangeAddress existingMergedRegion : targetSheet.getMergedRegions()) {
                                        if (existingMergedRegion.isInRange(rowIndex, columnIndex)) {
                                            mergedRegionExists = true;
                                            break;
                                        }
                                    }

                                    if (!mergedRegionExists) {
                                        targetSheet.addMergedRegion(mergedRegion);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            outputStream = new FileOutputStream(file2);
            targetWorkbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (sourceFile != null) sourceFile.close();
                if (targetFile != null) targetFile.close();
                if (outputStream != null) outputStream.close();
                if (sourceWorkbook != null) sourceWorkbook.close();
                if (targetWorkbook != null) targetWorkbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void copyCellValue(XSSFCell sourceCell, XSSFCell destCell) {
        switch (sourceCell.getCellType()) {
            case STRING:
                destCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    destCell.setCellValue(sourceCell.getDateCellValue());
                } else {
                    destCell.setCellValue(sourceCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                destCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            default:
                break;
        }
    }

    private static CellRangeAddress getMergedRegion(XSSFSheet sheet, int row, int col) {
        for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
            if (mergedRegion.isInRange(row, col)) {
                return mergedRegion;
            }
        }
        return null;
    }

    private static void adjustMergedRegion(CellRangeAddress mergedRegion, XSSFSheet sheet, int rowIndex, int cellIndex) {
        int firstRow = mergedRegion.getFirstRow() + rowIndex;
        int lastRow = mergedRegion.getLastRow() + rowIndex;
        int firstCol = mergedRegion.getFirstColumn() + cellIndex;
        int lastCol = mergedRegion.getLastColumn() + cellIndex;
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }
    private static void copyCell(Cell sourceCell, Cell destCell) {
        switch (sourceCell.getCellType()) {
            case STRING:
                destCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(sourceCell)) {
                    destCell.setCellValue(sourceCell.getDateCellValue());
                } else {
                    destCell.setCellValue(sourceCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                destCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case FORMULA:
                destCell.setCellFormula(sourceCell.getCellFormula());
                break;
            default:
                break;
        }
    }

    private static Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            default:
                return null;
        }
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

      /*  boolean isFirstRow = true;
        for (Object objectif : objectifs) {

         //   String libelle = entry.getKey();
        //    Object value = entry.getValue();
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
*/

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

    int AddDynamicCell(Workbook workbook, Sheet sheet, int rowStart ,int rowEnd ,int columnStart,int columnEnd,String value , String fontName , int fontSize , String fontColor , String backGroundColor , boolean leftBorder , boolean rightBorder , boolean topBorder , boolean bottomBorder , HorizontalAlignment h , VerticalAlignment v , boolean isBold , boolean isItalic){
        ExcelHelper.createDynamicCell(sheet,rowStart,rowEnd,columnStart,columnEnd,value,fontName,fontSize,fontColor,backGroundColor,leftBorder,rightBorder,topBorder,bottomBorder,h,v,isBold,isItalic);
        return rowStart;
    }


    int fillCompetence( JsonArray jsonArray , Workbook workbook, Sheet sheet, int rowIndex ){
        JsonArray FamilleArray = new JsonArray();
        for(JsonElement jsonElement : jsonArray){
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String parentId = jsonObject.get("parentID").getAsString();
                String id = jsonObject.get("id").getAsString();
                String name = jsonObject.get("name").getAsString();
                JsonArray notes = jsonObject.get("Notes").getAsJsonArray();
                if(parentId.equals("")){
                    FamilleArray.add(jsonObject);
                    rowIndex ++;
                    AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,1,5,name,"Arial",16,"BLACK","",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.TOP,true,false);
                    AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,6,11,notes.get(0).toString(),"Arial",16,"BLACK","",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.TOP,true,false);
                    rowIndex = rowIndex+2;
                    JsonArray sousFamilleArray = new JsonArray();

                    // Iterate through the original array and filter objects with "id" equal to "2"
                    for (JsonElement element : jsonArray) {
                        if (element.isJsonObject()) {
                            JsonObject object = element.getAsJsonObject();
                            if (object.has("parentID") && object.get("parentID").getAsString().equals(id)) {
                                sousFamilleArray.add(object);
                            }
                        }
                    }

                    if(sousFamilleArray.size()>0) {
                        for (JsonElement jsonElement2 : sousFamilleArray) {
                            if (jsonElement2.isJsonObject()) {
                                JsonObject object2 = jsonElement2.getAsJsonObject();
                                String sousFamilleParentId = object2.get("parentID").getAsString();
                                String sousFamilleId = object2.get("id").getAsString();
                                String sousFamilleName = object2.get("name").getAsString();
                                JsonArray sousFamilleNotes = object2.get("Notes").getAsJsonArray();
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,1,5,sousFamilleName,"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,6,6,sousFamilleNotes.get(0).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,7,7,sousFamilleNotes.get(1).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,8,8,sousFamilleNotes.get(2).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,9,9,sousFamilleNotes.get(3).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,10,10,sousFamilleNotes.get(4).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,11,11,sousFamilleNotes.get(5).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                rowIndex +=2;

                                JsonArray competenceArray = new JsonArray();

                                // Iterate through the original array and filter objects with "id" equal to "2"
                                for (JsonElement element : jsonArray) {
                                    if (element.isJsonObject()) {
                                        JsonObject object = element.getAsJsonObject();
                                        if (object.has("parentID") && object.get("parentID").getAsString().equals(sousFamilleId)) {
                                            competenceArray.add(object);
                                        }
                                    }
                                }

                                if(competenceArray.size()>0){
                                    for (JsonElement jsonElement3 : competenceArray) {
                                        if (jsonElement3.isJsonObject()) {
                                            JsonObject object3 = jsonElement3.getAsJsonObject();
                                            String competenceParentId = object3.get("parentID").getAsString();
                                            String competenceId = object3.get("id").getAsString();
                                            String competenceName = object3.get("name").getAsString();
                                            JsonArray competenceNotes = object3.get("Notes").getAsJsonArray();
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,1,5,competenceName,"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,6,6,competenceNotes.get(0).toString().replace("\"", ""),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,7,7,competenceNotes.get(1).toString().replace("\"", ""),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,8,8,competenceNotes.get(2).toString().replace("\"", ""),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,9,9,competenceNotes.get(3).toString().replace("\"", ""),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,10,10,competenceNotes.get(4).toString().replace("\"", ""),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,11,11,competenceNotes.get(5).toString().replace("\"", ""),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            rowIndex+=2;
                                        }
                                    }
                                }







                            }
                        }
                    }


                }

            }
        }
        if(FamilleArray.size()>0){
            for (JsonElement familleElement : FamilleArray) {
                if (familleElement.isJsonObject()) {
                    JsonObject familleObject = familleElement.getAsJsonObject();
                    rowIndex = AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 2, 1, 8,"Appréciation des " + familleObject.get("name").getAsString(), "Calibri", 12, "BLACK", "BRIGHT_GREEN", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
                    rowIndex = AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 2, 9, 11, familleObject.get("Notes").getAsJsonArray().get(0).toString(), "Arial", 16, "BLACK", "BRIGHT_GREEN", true, true, true, true, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true, false);
                    rowIndex += 3;
                }
            }
        }
        rowIndex = AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 2, 1, 8,"Evaluation globale", "Calibri", 12, "WHITE", "RED", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        rowIndex = AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 2, 9, 11, "3.84", "Arial", 16, "WHITE", "BLACK", true, true, true, true, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true, false);
        return rowIndex + 2;
    }


    public ArrayList<NotesModel> buildFamilleArray(ArrayList<ILinkedResource> competences){
        if(competences==null || competences.isEmpty()){
            return null;
        }

        ArrayList<NotesModel> data = new ArrayList<>();
        ArrayList<String> familleTraited = new ArrayList<>();
        for(ILinkedResource competence : competences){
            IStorageResource familleCompetence = (IStorageResource) competence.getValue("FamilleCompetence");
            if(!familleTraited.contains(familleCompetence.getId().toString())){
                NotesModel notesModel = new NotesModel();
                notesModel.setId(familleCompetence.getId().toString());
                notesModel.setName((String) familleCompetence.getValue("sys_Title"));
                notesModel.setParentID("");
                notesModel.setNotes(new ArrayList<Float>() {{
                    add(0f);
                    add(0f);
                    add(0f);
                    add(0f);
                    add(0f);
                    add(0f);
                }});
                data.add(notesModel);
                familleTraited.add(familleCompetence.getId().toString());
            }
        }
        return data;


    }

    public ArrayList<NotesModel> addSousFamilleToArray(ArrayList<ILinkedResource> competences , ArrayList<NotesModel> data){
        //ArrayList<NotesModel> data = new ArrayList<>();
        ArrayList<String> sousFamilleTraited = new ArrayList<>();
        for(ILinkedResource competence : competences){
            IStorageResource familleCompetence =(IStorageResource) competence.getValue("FamilleCompetence");
            IStorageResource sousFamille = (IStorageResource) competence.getValue("SousFamilleCompetence");
            if(!sousFamilleTraited.contains(sousFamille.getId().toString())){
                NotesModel notesModel = new NotesModel();
                notesModel.setId(sousFamille.getId().toString());
                notesModel.setName((String) sousFamille.getValue("sys_Title"));
                notesModel.setParentID(familleCompetence.getId().toString());
                notesModel.setNotes(new ArrayList<Float>() {{
                    add(0f);
                    add(0f);
                    add(0f);
                    add(0f);
                    add(0f);
                    add(0f);
                }});
                data.add(notesModel);
                sousFamilleTraited.add(sousFamille.getId().toString());
            }
        }
        return data;


    }

    public ArrayList<NotesModel> addCompetenceToArray(ArrayList<ILinkedResource> competences , ArrayList<NotesModel> data){
        //ArrayList<NotesModel> data = new ArrayList<>();
        HashMap<Float,Integer> positionHelper = new HashMap<>();
        //positionHelper.put(0f,0);
        positionHelper.put(1f,1);
        positionHelper.put(2f,2);
        positionHelper.put(3f,3);
        positionHelper.put(4f,4);
        positionHelper.put(5f,5);
        HashMap<String,Integer> idRepetition = new HashMap<>();
       // int i = 1;
     ArrayList<NotesModel> sousFamilleParents = new ArrayList();

        ArrayList<NotesModel> competenceParents = new ArrayList();
        for(ILinkedResource competenceLR : competences){
            IStorageResource competence = (IStorageResource) competenceLR.getValue("Competence");
            IStorageResource familleCompetence =(IStorageResource) competenceLR.getValue("FamilleCompetence");
            IStorageResource sousFamille = (IStorageResource) competenceLR.getValue("SousFamilleCompetence");
            IStorageResource notationN1 = (IStorageResource) competenceLR.getValue("EvaluationRespN1");
            Float valeur =notationN1!=null? ((Number) notationN1.getValue("Valeur")).floatValue():0f;
                NotesModel notesModel = new NotesModel();
                notesModel.setId(competence.getId().toString());
                notesModel.setName((String) competence.getValue("sys_Title"));
                notesModel.setParentID(sousFamille.getId().toString());
                ArrayList<Float> competenceNotes = new ArrayList<Float>() {{
                    add(0f);
                    add(0f);
                    add(0f);
                    add(0f);
                    add(0f);
                    add(0f);
                }};
                competenceNotes.set(positionHelper.get(valeur),valeur);
                notesModel.setNotes(competenceNotes);
                data.add(notesModel);

            NotesModel competenceParent = data.stream()
                    .filter(obj -> obj.getId().equals(notesModel.getParentID()) )
                    .findFirst()
                    .orElse(null);
            if(!competenceParents.contains(competenceParent)){
                competenceParents.add(competenceParent);
            }
            ArrayList<Float> competenceParentNotes = competenceParent.getNotes();
            competenceParentNotes.set(positionHelper.get(valeur),(competenceParentNotes.get(positionHelper.get(valeur)))+1);
            competenceParentNotes.set(0,competenceParentNotes.get(0)+valeur);
            // here update Moyenne
            NotesModel sousFamilleParent = data.stream()
                    .filter(obj -> obj.getId().equals(familleCompetence.getId().toString()) )
                    .findFirst()
                    .orElse(null);

            if(!sousFamilleParents.contains(sousFamilleParent)){
                sousFamilleParents.add(sousFamilleParent);
            }

            ArrayList<Float>   sousFamilleParentNotes = sousFamilleParent.getNotes();
           // sousFamilleParentNotes.set(positionHelper.get(valeur),(sousFamilleParentNotes.get(positionHelper.get(valeur)))+valeur);
            sousFamilleParentNotes.set(0,(sousFamilleParentNotes.get(0))+valeur);
            sousFamilleParentNotes.set(1,(sousFamilleParentNotes.get(1))+valeur);
            sousFamilleParentNotes.set(2,(sousFamilleParentNotes.get(2))+valeur);
            sousFamilleParentNotes.set(3,(sousFamilleParentNotes.get(3))+valeur);
            sousFamilleParentNotes.set(4,(sousFamilleParentNotes.get(4))+valeur);
            sousFamilleParentNotes.set(5,(sousFamilleParentNotes.get(5))+valeur);
            idRepetition.put(familleCompetence.getId().toString(),
                    idRepetition.get(familleCompetence.getId().toString())!=null?
                            (idRepetition.get(familleCompetence.getId().toString()))+1
                            :1);

            idRepetition.put(competence.getId().toString(),
                    idRepetition.get(competence.getId().toString())!=null?
                            (idRepetition.get(competence.getId().toString()))+1
                            :1);

            idRepetition.put(sousFamille.getId().toString(),
                    idRepetition.get(sousFamille.getId().toString())!=null?
                            (idRepetition.get(sousFamille.getId().toString()))+1
                            :1);

        }
      /*  for(NotesModel noteModel :sousFamilleParents ){
            String id = noteModel.getId();
           ArrayList<Float> notes = noteModel.getNotes();
           for(Float note : notes){
               note = note / (idRepetition.get(id));
           }
        }*/

        for (NotesModel noteModel : sousFamilleParents) {
            String id = noteModel.getId();
            ArrayList<Float> notes = noteModel.getNotes();

            for (int j = 0; j < notes.size(); j++) {
                float note = notes.get(j);
                notes.set(j, note / idRepetition.get(id));
            }
        }
        for (NotesModel noteModel : competenceParents) {
            String id = noteModel.getId();
            ArrayList<Float> notes = noteModel.getNotes();

           // for (int j = 0; j < notes.size(); j++) {
                float note = notes.get(0);
                notes.set(0, note / idRepetition.get(id));
           // }
        }
        return data;


    }


    int fillCompetence2( ArrayList<NotesModel> jsonArray , Workbook workbook, Sheet sheet, int rowIndex ,IWorkflowInstance instance){
        ArrayList<NotesModel> FamilleArray = new ArrayList<NotesModel>();
        for(NotesModel notesModel : jsonArray){
                String parentId = notesModel.getParentID();
                String id = notesModel.getId();
                String name = notesModel.getName();
            ArrayList<Float> notes = notesModel.getNotes();
                if(parentId.equals("")){
                    FamilleArray.add(notesModel);
                    rowIndex ++;
                    AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,1,5,name,"Arial",16,"BLACK","",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.TOP,true,false);
                    AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,6,11,notes.get(0).toString(),"Arial",16,"BLACK","",true,true,true,true,HorizontalAlignment.CENTER,VerticalAlignment.TOP,true,false);
                    rowIndex = rowIndex+2;
                    ArrayList<NotesModel> sousFamilleArray = new  ArrayList<NotesModel>();

                    for (NotesModel element : jsonArray) {
                            if (element.getParentID()!=null && element.getParentID().equals(id)) {
                                sousFamilleArray.add(element);
                            }

                    }

                    if(sousFamilleArray.size()>0) {
                        for (NotesModel jsonElement2 : sousFamilleArray) {
                                String sousFamilleParentId = jsonElement2.getParentID();
                                String sousFamilleId = jsonElement2.getId();
                                String sousFamilleName = jsonElement2.getName();
                                ArrayList<Float> sousFamilleNotes = jsonElement2.getNotes();
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,1,5,sousFamilleName,"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,6,6,sousFamilleNotes.get(0).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,7,7,sousFamilleNotes.get(1).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,8,8,sousFamilleNotes.get(2).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,9,9,sousFamilleNotes.get(3).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,10,10,sousFamilleNotes.get(4).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,11,11,sousFamilleNotes.get(5).toString(),"Calibri",12,"WHITE","RED",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                rowIndex +=2;

                                 ArrayList<NotesModel>  competenceArray = new ArrayList<NotesModel>();

                                for (NotesModel element : jsonArray) {
                                        if (element.getParentID()!=null && element.getParentID().equals(sousFamilleId)) {
                                            competenceArray.add(element);
                                        }

                                }

                                if(competenceArray.size()>0){
                                    for (NotesModel jsonElement3 : competenceArray) {
                                            String competenceParentId = jsonElement3.getParentID();
                                            String competenceId = jsonElement3.getId();
                                            String competenceName = jsonElement3.getName();
                                            ArrayList<Float> competenceNotes = jsonElement3.getNotes();
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,1,5,competenceName,"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,6,6, XorNull(competenceNotes.get(0)),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,7,7,XorNull(competenceNotes.get(1)),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,8,8,XorNull(competenceNotes.get(2)),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,9,9,XorNull(competenceNotes.get(3)),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,10,10,XorNull(competenceNotes.get(4)),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            AddDynamicCell(workbook,sheet,rowIndex,rowIndex+1,11,11,XorNull(competenceNotes.get(5)),"Calibri",12,"BLACK","",true,true,true,true,HorizontalAlignment.LEFT,VerticalAlignment.TOP,true,false);
                                            rowIndex+=2;

                                    }
                                }
                        }
                    }


                }


        }
        ArrayList<Float> toCalculatTotal = new ArrayList<>();
        if(FamilleArray.size()>0){
            for (NotesModel familleObject : FamilleArray) {
                    rowIndex = AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 2, 1, 8,"Appréciation des " + familleObject.getName(), "Calibri", 12, "BLACK", "BRIGHT_GREEN", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
                    rowIndex = AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 2, 9, 11, familleObject.getNotes().get(0).toString(), "Arial", 16, "BLACK", "BRIGHT_GREEN", true, true, true, true, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true, false);
                    rowIndex += 3;
                toCalculatTotal.add(familleObject.getNotes().get(0));

            }
        }
        Float total = getTotal(toCalculatTotal);
        rowIndex = AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 2, 1, 8,"Evaluation globale", "Calibri", 12, "WHITE", "RED", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        rowIndex = AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 2, 9, 11, total!=null?total.toString():"", "Arial", 16, "WHITE", "BLACK", true, true, true, true, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true, false);
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
    private Float getTotal(ArrayList<Float> data){
        Float sum = 0f;
        if(data!=null && !data.isEmpty()){
            for(Float note : data){
                sum+=note;
            }
            return sum/data.size();
        }
       return null;
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











    int fillCompetenceCHATGPTOLD(ArrayList<NotesModel> jsonArray, Workbook workbook, Sheet sheet, int rowIndex, IWorkflowInstance instance) {
        for (NotesModel familleObject : jsonArray) {
            if (familleObject.getParentID()==null || familleObject.getParentID().isEmpty()) {
                rowIndex = processFamille(workbook, sheet, rowIndex, familleObject);

                ArrayList<Float> toCalculateTotal = new ArrayList<>();
                for (NotesModel sousFamilleObject : jsonArray) {
                    if (sousFamilleObject.getParentID()!=null && sousFamilleObject.getParentID().equals(familleObject.getId())) {
                        rowIndex = processSousFamille(workbook, sheet, rowIndex, sousFamilleObject);
                      /*NSSA*/  Pair<ArrayList<Float>, Integer> result = processCompetence(workbook, sheet, rowIndex, jsonArray, sousFamilleObject.getId());

                        ArrayList<Float> competenceNotes = result.getLeft();
                        toCalculateTotal.addAll(competenceNotes);
                        rowIndex=result.getRight();
                    }
                }
                float total = getTotal(toCalculateTotal);
               // rowIndex+=50;
                rowIndex++;
                rowIndex = processFamilleTotal(workbook, sheet, rowIndex, familleObject, total);
            }
        }
        return rowIndex + 2;
    }

    int fillCompetenceCHATGPT(ArrayList<NotesModel> jsonArray, Workbook workbook, Sheet sheet, int rowIndex, IWorkflowInstance instance) {
        Map<String, ArrayList<Float>> competenceData = new HashMap<>();
        Map<String, NotesModel> familleAppreciationData = new HashMap<>();

        for (NotesModel familleObject : jsonArray) {
            if (familleObject.getParentID() == null || familleObject.getParentID().isEmpty()) {
                rowIndex = processFamille(workbook, sheet, rowIndex, familleObject);

                for (NotesModel sousFamilleObject : jsonArray) {
                    if (sousFamilleObject.getParentID() != null && sousFamilleObject.getParentID().equals(familleObject.getId())) {
                        rowIndex = processSousFamille(workbook, sheet, rowIndex, sousFamilleObject);

                        Pair<ArrayList<Float>, Integer> result = processCompetence(workbook, sheet, rowIndex, jsonArray, sousFamilleObject.getId());

                        ArrayList<Float> competenceNotes = result.getLeft();
                        competenceData.put(familleObject.getId(), competenceData.getOrDefault(familleObject.getId(), new ArrayList<>()));
                        competenceData.get(familleObject.getId()).addAll(competenceNotes);

                        rowIndex = result.getRight();
                    }
                }

                // Store the "Appréciation" data for each family
                familleAppreciationData.put(familleObject.getId(), familleObject);
            }
        }

        // Calculate the total for all families and process "Appréciation" for each family
        for (String familleId : familleAppreciationData.keySet()) {
            float total = getTotal(competenceData.get(familleId));

            rowIndex++; // Move to the next row
            NotesModel familleObject = familleAppreciationData.get(familleId);
            rowIndex = processFamilleTotal(workbook, sheet, rowIndex, familleObject, total);
        }

        return rowIndex + 2;
    }


    private int processFamille(Workbook workbook, Sheet sheet, int rowIndex, NotesModel familleObject) {
        rowIndex++;
        AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 1, 5, familleObject.getName(), "Arial", 16, "BLACK", "", true, true, true, true, HorizontalAlignment.CENTER, VerticalAlignment.TOP, true, false);
        AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 6, 11, familleObject.getNotes().get(0).toString(), "Arial", 16, "BLACK", "", true, true, true, true, HorizontalAlignment.CENTER, VerticalAlignment.TOP, true, false);
        return rowIndex + 2;
    }

    private int processSousFamille(Workbook workbook, Sheet sheet, int rowIndex, NotesModel sousFamilleObject) {
        rowIndex++;
        AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 1, 5, sousFamilleObject.getName(), "Calibri", 12, "WHITE", "RED", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        for (int i = 0; i < 6; i++) {
            AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 6 + i, 6 + i, sousFamilleObject.getNotes().get(i).toString(), "Calibri", 12, "WHITE", "RED", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        }
        return rowIndex + 2;
    }

    private Pair<ArrayList<Float>, Integer> processCompetence(Workbook workbook, Sheet sheet, int rowIndex, ArrayList<NotesModel> jsonArray, String sousFamilleId) {
        rowIndex++;
        ArrayList<Float> competenceNotes = new ArrayList<>();
        for (NotesModel competenceObject : jsonArray) {
            if (competenceObject.getParentID() != null && competenceObject.getParentID().equals(sousFamilleId)) {
                AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 1, 5, competenceObject.getName(), "Calibri", 12, "BLACK", "", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
                for (int i = 0; i < 6; i++) {
                    AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 1, 6 + i, 6 + i, XorNull(competenceObject.getNotes().get(i)), "Calibri", 12, "BLACK", "", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
                }
                competenceNotes.addAll(competenceObject.getNotes());
                rowIndex += 2;
            }
        }
        return Pair.of(competenceNotes, rowIndex);
    }

    private int processFamilleTotal(Workbook workbook, Sheet sheet, int rowIndex, NotesModel familleObject, Float total) {
        rowIndex = AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 2, 1, 8, "Appréciation des " + familleObject.getName(), "Calibri", 12, "BLACK", "BRIGHT_GREEN", true, true, true, true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, true, false);
        rowIndex = AddDynamicCell(workbook, sheet, rowIndex, rowIndex + 2, 9, 11, total != null ? total.toString() : "", "Arial", 16, "BLACK", "BRIGHT_GREEN", true, true, true, true, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, true, false);
        return rowIndex + 3;
    }






////////////
    public ArrayList<NotesModel> buildFamilleArrayCHATGPT(ArrayList<ILinkedResource> competences) {
        if (competences == null || competences.isEmpty()) {
            return null;
        }

        ArrayList<NotesModel> data = new ArrayList<>();
        HashSet<String> familleTraited = new HashSet<>();

        for (ILinkedResource competence : competences) {
            IStorageResource familleCompetence = (IStorageResource) competence.getValue("FamilleCompetence");
            String familleId = familleCompetence.getId().toString();

            if (!familleTraited.contains(familleId)) {
                NotesModel notesModel = createNotesModel(familleCompetence);
                data.add(notesModel);
                familleTraited.add(familleId);
            }
        }

        return data;
    }

    public ArrayList<NotesModel> addSousFamilleToArrayCHATGPT(ArrayList<ILinkedResource> competences, ArrayList<NotesModel> data) {
        HashSet<String> sousFamilleTraited = new HashSet<>();

        for (ILinkedResource competence : competences) {
            IStorageResource familleCompetence = (IStorageResource) competence.getValue("FamilleCompetence");
            IStorageResource sousFamille = (IStorageResource) competence.getValue("SousFamilleCompetence");
            String sousFamilleId = sousFamille.getId().toString();

            if (!sousFamilleTraited.contains(sousFamilleId)) {
                NotesModel notesModel = createNotesModel(sousFamille);
                notesModel.setParentID(familleCompetence.getId().toString());
                data.add(notesModel);
                sousFamilleTraited.add(sousFamilleId);
            }
        }

        return data;
    }

    public ArrayList<NotesModel> addCompetenceToArrayCHATGPT(ArrayList<ILinkedResource> competences, ArrayList<NotesModel> data) {
        HashMap<Float, Integer> positionHelper = new HashMap<>();
        //positionHelper.put(0f, 0);
        positionHelper.put(1f, 1);
        positionHelper.put(2f, 2);
        positionHelper.put(3f, 3);
        positionHelper.put(4f, 4);
        positionHelper.put(5f, 5);

        HashMap<String, Integer> idRepetition = new HashMap<>();

        for (ILinkedResource competenceLR : competences) {
            IStorageResource competence = (IStorageResource) competenceLR.getValue("Competence");
            IStorageResource familleCompetence = (IStorageResource) competenceLR.getValue("FamilleCompetence");
            IStorageResource sousFamille = (IStorageResource) competenceLR.getValue("SousFamilleCompetence");
            IStorageResource notationN1 = (IStorageResource) competenceLR.getValue("AutoEvaluation");
            float valeur =notationN1!=null? ((Number) notationN1.getValue("Valeur")).floatValue():0;

            NotesModel notesModel = createNotesModel(competence);
            notesModel.setParentID(sousFamille.getId().toString());
            ArrayList<Float> competenceNotes = notesModel.getNotes();
            Integer position = positionHelper.get(valeur);
            if(position!=null){
                competenceNotes.set(position, valeur);
            }

            data.add(notesModel);

            updateNotesModel(data, familleCompetence.getId().toString(), sousFamille.getId().toString(), competence.getId().toString(), valeur, idRepetition);
        }

        updateAverages(data, idRepetition);
        return data;
    }

    private NotesModel createNotesModel(IStorageResource resource) {
        NotesModel notesModel = new NotesModel();
        notesModel.setId(resource.getId().toString());
        notesModel.setName((String) resource.getValue("sys_Title"));
        notesModel.setNotes(new ArrayList<>(Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f)));
        return notesModel;
    }

    private void updateNotesModel(ArrayList<NotesModel> data, String familleId, String sousFamilleId, String competenceId, float valeur, HashMap<String, Integer> idRepetition) {
        HashMap<Float, Integer> positionHelper = new HashMap<>();
        positionHelper.put(1f, 1);
        positionHelper.put(2f, 2);
        positionHelper.put(3f, 3);
        positionHelper.put(4f, 4);
        positionHelper.put(5f, 5);

        NotesModel competenceParent = findNotesModelById(data, sousFamilleId);
        ArrayList<Float> competenceParentNotes = competenceParent.getNotes();

        Integer position = positionHelper.get(valeur);

        if (position != null) {
            competenceParentNotes.set(position, competenceParentNotes.get(position) + 1);
        }

        competenceParentNotes.set(0, competenceParentNotes.get(0) + valeur);

        NotesModel sousFamilleParent = findNotesModelById(data, familleId);
        ArrayList<Float> sousFamilleParentNotes = sousFamilleParent.getNotes();
        sousFamilleParentNotes.set(0, sousFamilleParentNotes.get(0) + valeur);

        for (int i = 1; i < 6; i++) {
            sousFamilleParentNotes.set(i, sousFamilleParentNotes.get(i) + valeur);
        }

        incrementIdRepetition(idRepetition, familleId);
        incrementIdRepetition(idRepetition, competenceId);
        incrementIdRepetition(idRepetition, sousFamilleId);

    }

    private void updateAverages(ArrayList<NotesModel> data, HashMap<String, Integer> idRepetition) {
        for (NotesModel noteModel : data) {
            String id = noteModel.getId();
            ArrayList<Float> notes = noteModel.getNotes();

            for (int j = 0; j < notes.size(); j++) {
                float note = notes.get(j);
                notes.set(j, note / idRepetition.get(id));
            }
        }
    }

    private NotesModel findNotesModelById(ArrayList<NotesModel> data, String id) {
        return data.stream()
                .filter(obj -> obj.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void incrementIdRepetition(HashMap<String, Integer> idRepetition, String id) {
        idRepetition.put(id, idRepetition.getOrDefault(id, 0) + 1);
    }



}



