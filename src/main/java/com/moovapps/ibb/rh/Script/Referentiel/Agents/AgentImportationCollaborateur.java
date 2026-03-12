package com.moovapps.ibb.rh.Script.Referentiel.Agents;

import com.axemble.fc.file.domain.DBFile;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.moovapps.Reprise.Helpers.ClassListHelper;
import com.moovapps.Reprise.Helpers.ExcelWriter;
import com.moovapps.Reprise.Helpers.LienParente;
import com.moovapps.ibb.rh.Script.Referentiel.Collaborateurs.ExportCollaborateursExcel;
import com.moovapps.ibb.rh.Script.Referentiel.VariablesColonnes;
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

public class AgentImportationCollaborateur extends BaseAgent {
    boolean isAnomalieFound = false;

    List<String> sheets = Arrays.asList("Collaborateur");
    //"LienParente","Etudes","CompetenceCollaborateur","MissionCollaborateur","ObjectifCollaborateurs"
    ArrayList<JSONObject>  excelAnomaliesWorkFlowImportRefs = new ArrayList();

    HashMap<IStorageResource,String> SuppleantMap = new HashMap<IStorageResource,String>();
    HashMap<IStorageResource,String> EvaluateurMap = new HashMap<IStorageResource,String>();
    HashMap<IStorageResource,String> N1Map = new HashMap<IStorageResource,String>();
    ArrayList<JSONObject> excelAnomaliesWorkFlowImportUser = new ArrayList<>();


    @Override
    protected void execute() {

        IWorkflowInstance instance = createInstance();
        if (instance != null) {




            File file = createFileFromFileCenterInLocalDrive("Maquette/Importation_CollaborateursV1.xlsx", "Importation_CollaborateursV1.xlsx");
            if (file != null) {
                ArrayList<IAttachment> tmpImportationCollaborateurs = new ArrayList<>();
                IAttachment tmpImportationCollaborateursAttachment = Modules.getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
                tmpImportationCollaborateurs.add(tmpImportationCollaborateursAttachment);
                instance.setValue("FichierAExporter", tmpImportationCollaborateurs);
                file.delete();
                file.deleteOnExit();
            }
            instance.setValue("Societe", getSociete());



//            ImportationMain importation = new ImportationMain(getWorkflowModule(), getWorkflowInstance(), getDirectoryModule().getLoggedOnUserContext());
//            importation.importFromExcelToRef(true);
         //   instance.deleteLinkedResources(instance.getLinkedResources("RapportDImport"));
            instance.save(getWorkflowModule().getSysadminContext());
        excelAnomaliesWorkFlowImportRefs = new ArrayList();
        ImporterV2(instance);

    }



    }

    public IWorkflowInstance createInstance(){

        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "ReferentielsTuile", project);
            IWorkflow workflow = getWorkflowModule().getWorkflow(sysContext,catalog,"ImportationCollaborateurs_1.0");

            return getWorkflowModule().createWorkflowInstance(sysContext,workflow,"","");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void ImporterV2(IWorkflowInstance workflowInstanceinstance){
        try{

//            ArrayList<IAttachment> tmp = (ArrayList<IAttachment>) workflowInstanceinstance.getValue("FichierAImporter");
//            IAttachment tmpAttachement = null;
//            if (tmp != null && tmp.size() > 0) {
//                tmpAttachement = tmp.get(0);
//            }
            File file = new File("C://Users//CAP ONE//Desktop//App Moovapps//Galenica//Maquette-visiativ-02-2026.xlsx");;
//            if (tmpAttachement != null) {
//                try {
//                    file = new File("C://Users//CAP ONE//Desktop//App Moovapps//Galenica//Maquette-visiativ-02-2026.xlsx");
//
//                    FileUtils.writeByteArrayToFile(file, tmpAttachement.getContent());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
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




           // workflowInstanceinstance.setValue("FichierAImporter",file);
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
                    createVerificationImportWorkflowInstance(workflowInstanceinstance, filePath);
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
                if (file != null) {
                    String[] filePathDivided = filePath.split("/");
                    IAttachment attachment = libraryModule.getAttachment(file, filePathDivided[filePathDivided.length - 1]);
                    File tmpFile = new File("c://TEST//SIRH_V2//TmpImportation//" + fileName);
                    FileUtils.writeByteArrayToFile(tmpFile, attachment.getContent());
                    return tmpFile;                }
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

    protected IStorageResource getSociete() {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
            if (!controller.evaluate(definition).isEmpty()){
                return (IStorageResource) controller.evaluate(definition).iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


}
