package com.moovapps.ibb.rh.Script.Referentiel;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.moovapps.Reprise.Helpers.ClassListHelper;
import com.moovapps.Reprise.Helpers.Collaborateur;
import com.moovapps.Reprise.Helpers.ExcelWriter;
import com.moovapps.Reprise.Helpers.LienParente;
import com.moovapps.ibb.rh.Script.Referentiel.Collaborateurs.ExportCollaborateursExcel;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class DemanedImportation extends BaseDocumentExtension {
    boolean isAnomalieFound = false;

    List<String> sheets = Arrays.asList("Collaborateur");
    //"LienParente","Etudes","CompetenceCollaborateur","MissionCollaborateur","ObjectifCollaborateurs"
    ArrayList<JSONObject>  excelAnomaliesWorkFlowImportRefs = new ArrayList();

    HashMap<IStorageResource,String> SuppleantMap = new HashMap<IStorageResource,String>();
    HashMap<IStorageResource,String> EvaluateurMap = new HashMap<IStorageResource,String>();
    HashMap<IStorageResource,String> N1Map = new HashMap<IStorageResource,String>();
    ArrayList<JSONObject> excelAnomaliesWorkFlowImportUser = new ArrayList<>();


    @Override
    public boolean onAfterLoad() {
        //getWorkflowInstance().deleteLinkedResources(getWorkflowInstance().getLinkedResources("RapportDImport"));
        //getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        if (getWorkflowInstance().getValue("FichierAExporter") == null) {
            File file = createFileFromFileCenterInLocalDrive("Maquette/Importation_CollaborateursV1.xlsx", "Importation_CollaborateursV1.xlsx");
            if (file == null) {
                getResourceController().alert("Maquette introuvable !!");
            } else {
                ArrayList<IAttachment> tmpImportationCollaborateurs = new ArrayList<>();
                IAttachment tmpImportationCollaborateursAttachment = Modules.getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
                tmpImportationCollaborateurs.add(tmpImportationCollaborateursAttachment);
                getWorkflowInstance().setValue("FichierAExporter", tmpImportationCollaborateurs);
                file.delete();
                file.deleteOnExit();
            }
        }
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("ImporterLesCollaborateurs")){
//            ImportationMain importation = new ImportationMain(getWorkflowModule(), getWorkflowInstance(), getDirectoryModule().getLoggedOnUserContext());
//            importation.importFromExcelToRef(true);
            getWorkflowInstance().deleteLinkedResources(getWorkflowInstance().getLinkedResources("RapportDImport"));
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
            excelAnomaliesWorkFlowImportRefs = new ArrayList();
            ImporterV2();
            if(isAnomalieFound){

                return false;
            }
        }
        //return  false;
        return super.onBeforeSubmit(action);
    }

    public void Importer(){
        try {
            ClassListHelper helper = new ClassListHelper();
            // Specify the path to your Excel file
            ArrayList<IAttachment> tmp = (ArrayList<IAttachment>) getWorkflowInstance().getValue("FichierAImporter");
            IAttachment tmpAttachement = null;
            if (tmp != null && tmp.size() > 0) {
                tmpAttachement = tmp.get(0);
            }
            File file = null;
            if (tmpAttachement != null) {
                try {
                    file = new File("c://Temp_Moovapps//" + tmpAttachement.getName());

                    FileUtils.writeByteArrayToFile(file, tmpAttachement.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(file==null){
                return;
            }

            // Create a FileInputStream to read the Excel file
            FileInputStream fis = new FileInputStream(file);

            // Create a workbook object to represent the Excel file
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            if(!verifySheetsNames(workbook)){

                return;
            }
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) { // Loop Over sheets
                if(helper.classList==null  || helper.classList.size()<=i || helper.classList.get(i)==null){
                    continue;
                }
                Method getStringListMethod = helper.classList.get(i)!=null? helper.classList.get(i).getMethod("getColumns")!=null? helper.classList.get(i).getMethod("getColumns"):null:null;
                if(getStringListMethod==null)continue;
                List<String> columns = (List<String>) getStringListMethod.invoke(helper.classList.get(i).newInstance());
                Sheet sheet = workbook.getSheetAt(i);
                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) { // Loop OverRows
                    isAnomalieFound = false;
                    Row row = sheet.getRow(rowIndex);
                    if(rowIndex==0){
                        //Verify maquette
                        //if false break;
                        continue;
                    }


                    //Create pole storage
                    Method createStorageMethode =  helper.classList.get(i).getMethod("createStorage");
                    IStorageResource item = (IStorageResource) createStorageMethode.invoke(helper.classList.get(i).newInstance());


                    for(String colonne : columns){
                        if(colonne.startsWith("_")){
                            Method method = helper.classList.get(i).getMethod(colonne,IWorkflowInstance.class,IStorageResource.class, Row.class, Cell.class);
                            Object isAnomalieFoundObject =  method.invoke(helper.classList.get(i).newInstance(),getWorkflowInstance(), item,row,row.getCell(columns.indexOf(colonne)));
                            //if(isAnomalieFound==false){
                            isAnomalieFound = (boolean) isAnomalieFoundObject;
                            //}
                        }
                    }
                    // Method saveMethod = helper.classList.get(i).getMethod("save",IStorageResource.class);
                    // saveMethod.invoke(helper.classList.get(i).newInstance(),item);
                    //isNew(item,columns) // THE columns should be the sysNames || declare isNew on the helper class for each
                    //if(!isNew) DONT SAVE
                    if(!isAnomalieFound){

                        if(helper.classList.get(i) == LienParente.class){
                            Method method = helper.classList.get(i).getMethod("IncreaseNombreEnfant",IStorageResource.class);
                            method.invoke(helper.classList.get(i).newInstance(), item);
                        }

                        item.save(getWorkflowModule().getSysadminContext());
                    }
                    System.out.println();
                }
            }


            workbook.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void ImporterV2(){
        try{
            ArrayList<IAttachment> tmp = (ArrayList<IAttachment>) getWorkflowInstance().getValue("FichierAImporter");
            IAttachment tmpAttachement = null;
            if (tmp != null && tmp.size() > 0) {
                tmpAttachement = tmp.get(0);
            }
            File file = null;
            if (tmpAttachement != null) {
                try {
                    file = new File("c://Temp_Moovapps//" + tmpAttachement.getName());

                    FileUtils.writeByteArrayToFile(file, tmpAttachement.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(file==null){
                return;
            }
            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            if(!verifySheetsNames(workbook)){
                //  return;
            }
            //int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < sheets.size(); i++){
                Sheet sheet = workbook.getSheetAt(i);
                Class<?> sheetClass = Class.forName("com.moovapps.Reprise.Helpers."+sheets.get(i));
                Constructor<?> constructor = sheetClass.getDeclaredConstructor();
                Object instance = constructor.newInstance();
                Method method = instance.getClass().getMethod("CreateOrUpdateStorages",Sheet.class,ArrayList.class);
                method.invoke(instance, sheet,excelAnomaliesWorkFlowImportRefs);
            }
            if(excelAnomaliesWorkFlowImportRefs!=null && !excelAnomaliesWorkFlowImportRefs.isEmpty()){
//                for (JSONObject comment : excelAnomaliesWorkFlowImportRefs) {
//                    ILinkedResource commentTD = getWorkflowInstance().createLinkedResource("RapportDImport");
//                    commentTD.setValue("Ligne", comment.get("Ligne"));
//                    commentTD.setValue("Cordonnee", comment.get("Cordonnee"));
//                    commentTD.setValue("Alerte", comment.get("Alerte"));
//                    commentTD.setValue("Anomalie", comment.get("Anomalie"));
//                    getWorkflowInstance().addLinkedResource(commentTD);
//                    commentTD.save(getDirectoryModule().getSysadminContext());
//                }
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yy");
                String formattedDate = formatter.format(date);
                String filePath =   new ExcelWriter().GenerateRefExcel(excelAnomaliesWorkFlowImportRefs,"C:\\Export\\Galenica\\Anomalies\\Anomalies_"+formattedDate+".xlsx");
                if(!filePath.equals("")){
                    createVerificationImportWorkflowInstance(getWorkflowInstance(), filePath);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void createVerificationImportWorkflowInstance(IWorkflowInstance instance,String anomalieFile){
        try {
            IContext context = getWorkflowModule().getSysadminContext();

            IAttachment attachement = getDirectoryModule().createAttachment(context,new File(anomalieFile));
            ArrayList<IAttachment>  attachements = new ArrayList();
            attachements.add(attachement);
            instance.setValue("Anomalies",attachements);
            instance.save(getWorkflowModule().getSysadminContext());
            //fillAnomaliesLinkedResources(verificationImportInstance);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public boolean verifySheetsNames(XSSFWorkbook workbook){
        if(workbook.getSheetName(0).equals("Pôle")){
            return true;
        }
        return false;
    }

    private File createFileFromFileCenterInLocalDrive(String filePath, String fileName) {
        try {
            ILibraryModule libraryModule = Modules.getLibraryModule();
            IContext sysContext = Modules.getWorkflowModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            ILibrary library = libraryModule.getLibrary(sysContext, organization, "Espace System");
            if (library != null) {
                IFile file = libraryModule.getFileByPath(sysContext, library, filePath);
                if (file == null) {
                    getResourceController().alert("Le fichier de modele n'existe pas dans l'espace documentaire...Veuillez contacter votre administrateur !");
                } else {
                    String[] filePathDivided = filePath.split("/");
                    IAttachment attachment = libraryModule.getAttachment(file, filePathDivided[filePathDivided.length - 1]);
                    File tmpFile = new File("c://TEST//SIRH_V2//TmpImportation//" + fileName);
                    FileUtils.writeByteArrayToFile(tmpFile, attachment.getContent());
                    return tmpFile;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void exportCollaborateurs(IWorkflowInstance workflowInstance, IResourceController resourceController){
        try {
            File file = createFileFromFileCenterInLocalDrive("Maquette/Importation_CollaborateursV1.xlsx", "Export_Collaborateurs.xlsx");

            try (XSSFWorkbook exportWorkbook = new XSSFWorkbook(new FileInputStream(file));
                 FileOutputStream outputStream = new FileOutputStream("c://TEST//SIRH_V2//TmpImportation//tmpExport.xlsx")) {

                XSSFSheet exportWorkbookFirstSheet = exportWorkbook.getSheetAt(0);
                int rowIndex = 2;
                Collection<IStorageResource> collaborateurs = getCollaborateurs((IStorageResource) workflowInstance.getValue("Societe"));

                for (IStorageResource collaborateur : collaborateurs) {
                    XSSFRow row = exportWorkbookFirstSheet.createRow(rowIndex);
                    for (int i = 0; i < VariablesColonnes.sheetFields.get("_ImportationCollaborateur").size(); i++) {
                        if(VariablesColonnes.sheetFields.get("_ImportationCollaborateur").get(i).startsWith("_")){
                            XSSFCell cell = row.createCell(i);
                            String methodName = VariablesColonnes.sheetFields.get("_ImportationCollaborateur").get(i);
                            Method method = ExportCollaborateursExcel.class.getMethod(methodName, IStorageResource.class, Row.class, Cell.class);
                            Object value = method.invoke(new ExportCollaborateursExcel(), collaborateur, row, cell);
                        }

                    }
                    rowIndex++;
                }

                exportWorkbook.write(outputStream);
                exportWorkbook.close();
                outputStream.close();
                File toDelete = new File("c://TEST//SIRH_V2//TmpImportation//tmpExport.xlsx");
                FileUtils.copyFile(toDelete, file);
                toDelete.delete();
                toDelete.deleteOnExit();
            }

            File toDeleteFile = new File("C:\\TEST\\Collaborateurs.xlsx");
            FileUtils.copyFile(file, toDeleteFile);
            IAttachment tmpJournalPaieAttachment = Modules.getDirectoryModule().createAttachment(Modules.getWorkflowModule().getSysadminContext(), toDeleteFile);
            ArrayList<IAttachment> tmpNouvellesRecruesList = new ArrayList<>();
            tmpNouvellesRecruesList.add(tmpJournalPaieAttachment);
            toDeleteFile.delete();
            toDeleteFile.deleteOnExit();
            file.delete();
            file.deleteOnExit();
            workflowInstance.setValue("FichierDExport", tmpNouvellesRecruesList);
            workflowInstance.save(Modules.getWorkflowModule().getLoggedOnUserContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    protected Collection<IStorageResource> getCollaborateurs(IStorageResource societe) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("Societe", societe);
            return controller.evaluate(definition);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


}
