package com.moovapps.ibb.rh.Test;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

public class AgentRIBCNSS extends BaseAgent {
    @Override
    protected void execute() {
        generateReleveFacturation();
    }


    private void generateReleveFacturation(){

        try {
            File maquetteFile = createFileFromFileCenterInLocalDrive("Modèles/RIBCNSS.xlsx","RIBCNSS.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(maquetteFile));
            XSSFSheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = firstSheet.iterator();
            int rowIndex = 0;
            while (iterator.hasNext()) {
                Row row = iterator.next();
                if(row == null){
                    break;
                }
                if(rowIndex > 0) {
                    String login = row.getCell(48).getStringCellValue();
                    IUser user = getDirectoryModule().getUserByLogin(login);
                    user.getExtendedAttributes().setValue("NCNSS",row.getCell(49).getStringCellValue());
                    user.getExtendedAttributes().setValue("NCompteBancaire",row.getCell(50).getStringCellValue());
                    user.save(getWorkflowModule().getSysadminContext());
                }
                rowIndex++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private File createFileFromFileCenterInLocalDrive(String filePath, String fileName) {
        try {
            ILibraryModule libraryModule = Modules.getLibraryModule();
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            ILibrary library = libraryModule.getLibrary(context, organization, "Paie");
            if (library != null) {
                IFile file = libraryModule.getFileByPath(context, library, filePath);
                if (file == null) {
                    LOGGER.error("Le fichier de modele n'existe pas dans l'espace documentaire...Vieullez contacter votre administrateur");
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
}
