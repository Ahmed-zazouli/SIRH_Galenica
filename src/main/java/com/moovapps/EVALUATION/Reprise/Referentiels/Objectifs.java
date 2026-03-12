package com.moovapps.EVALUATION.Reprise.Referentiels;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Objectifs extends BaseAgent {
    @Override
    protected void execute() {
            Run();
    }

    void Run(){
        try {
            Collection<IUser> users = (Collection<IUser>) getDirectoryModule().getUsers(getWorkflowModule().getSysadminContext());
            ArrayList<String> logins = new ArrayList<>();
            for(IUser user : users){
                logins.add(user.getLogin().toLowerCase());
            }

            File excelFile = new File("C:\\import\\EVAL\\Obj 2022 MSO.xlsx");
            XSSFWorkbook workbook;
            try {
                workbook = new XSSFWorkbook(new FileInputStream(excelFile));
                XSSFSheet firstSheet = workbook.getSheetAt(0);
                Iterator<Row> journalVenteIterator = firstSheet.iterator();
                int rowIndex = 0;

                while (journalVenteIterator.hasNext()) {
                    Row nextRow = journalVenteIterator.next();
                    if (rowIndex == 0) {
                        //Verify maquette
                    }
                    if (nextRow == null) continue;
                    if (rowIndex > 0) {
                        if (firstSheet.getRow(rowIndex) == null) {
                            break;
                        }
                        String login ="";
                        String objectif1 ="";
                        String objectif1Poids = "";

                        String objectif2 ="";
                        String objectif2Poids = "";

                        String objectif3 ="";
                        String objectif3Poids = "";

                        String objectif4 ="";
                        String objectif4Poids = "";

                        String objectif5 ="";
                        String objectif5Poids = "";

                        String objectif6 ="";
                        String objectif6Poids = "";

                        if(firstSheet.getRow(rowIndex).getCell(0)!=null){
                            firstSheet.getRow(rowIndex).getCell(0).setCellType(CellType.STRING);
                            Cell loginCell = firstSheet.getRow(rowIndex).getCell(0);
                             login = firstSheet.getRow(rowIndex).getCell(0).getStringCellValue();
                        }
                        if(firstSheet.getRow(rowIndex).getCell(1)!=null){
                            firstSheet.getRow(rowIndex).getCell(1).setCellType(CellType.STRING);
                            Cell objectif1Cell = firstSheet.getRow(rowIndex).getCell(1);
                            objectif1 = firstSheet.getRow(rowIndex).getCell(1).getStringCellValue();
                        }

                        if(firstSheet.getRow(rowIndex).getCell(2)!=null){
                            firstSheet.getRow(rowIndex).getCell(2).setCellType(CellType.STRING);
                            Cell objectif1PoidsCell = firstSheet.getRow(rowIndex).getCell(2);
                            objectif1Poids = firstSheet.getRow(rowIndex).getCell(2).getStringCellValue();
                        }

                        if(firstSheet.getRow(rowIndex).getCell(3)!=null){
                            firstSheet.getRow(rowIndex).getCell(3).setCellType(CellType.STRING);
                            Cell objectif2Cell = firstSheet.getRow(rowIndex).getCell(3);
                            objectif2 = firstSheet.getRow(rowIndex).getCell(3).getStringCellValue();
                        }

                        if(firstSheet.getRow(rowIndex).getCell(4)!=null){
                            firstSheet.getRow(rowIndex).getCell(4).setCellType(CellType.STRING);
                            Cell objectif2PoidsCell = firstSheet.getRow(rowIndex).getCell(4);
                            objectif2Poids = firstSheet.getRow(rowIndex).getCell(4).getStringCellValue();
                        }

                        if(firstSheet.getRow(rowIndex).getCell(5)!=null){
                            firstSheet.getRow(rowIndex).getCell(5).setCellType(CellType.STRING);
                            Cell objectif3Cell = firstSheet.getRow(rowIndex).getCell(5);
                            objectif3 = firstSheet.getRow(rowIndex).getCell(5).getStringCellValue();
                        }

                        if(firstSheet.getRow(rowIndex).getCell(6)!=null){
                            firstSheet.getRow(rowIndex).getCell(6).setCellType(CellType.STRING);
                            Cell objectif3PoidsCell = firstSheet.getRow(rowIndex).getCell(6);
                            objectif3Poids = firstSheet.getRow(rowIndex).getCell(6).getStringCellValue();
                        }

                        if(firstSheet.getRow(rowIndex).getCell(7)!=null){
                            firstSheet.getRow(rowIndex).getCell(7).setCellType(CellType.STRING);
                            Cell objectif4Cell = firstSheet.getRow(rowIndex).getCell(7);
                            objectif4 = firstSheet.getRow(rowIndex).getCell(7).getStringCellValue();
                        }

                        if(firstSheet.getRow(rowIndex).getCell(8)!=null){
                            firstSheet.getRow(rowIndex).getCell(8).setCellType(CellType.STRING);
                            Cell objectif4PoidsCell = firstSheet.getRow(rowIndex).getCell(8);
                            objectif4Poids = firstSheet.getRow(rowIndex).getCell(8).getStringCellValue();
                        }

                        if(firstSheet.getRow(rowIndex).getCell(9)!=null){
                            firstSheet.getRow(rowIndex).getCell(9).setCellType(CellType.STRING);
                            Cell objectif5Cell = firstSheet.getRow(rowIndex).getCell(9);
                            objectif5 = firstSheet.getRow(rowIndex).getCell(9).getStringCellValue();
                        }

                        if(firstSheet.getRow(rowIndex).getCell(10)!=null){
                            firstSheet.getRow(rowIndex).getCell(10).setCellType(CellType.STRING);
                            Cell objectif5PoidsCell = firstSheet.getRow(rowIndex).getCell(10);
                            objectif5Poids = firstSheet.getRow(rowIndex).getCell(10).getStringCellValue();
                        }

                        if(firstSheet.getRow(rowIndex).getCell(11)!=null){
                            firstSheet.getRow(rowIndex).getCell(11).setCellType(CellType.STRING);
                            Cell objectif6Cell = firstSheet.getRow(rowIndex).getCell(11);
                            objectif6 = firstSheet.getRow(rowIndex).getCell(11).getStringCellValue();
                        }

                        if(firstSheet.getRow(rowIndex).getCell(12)!=null){
                            firstSheet.getRow(rowIndex).getCell(12).setCellType(CellType.STRING);
                            Cell objectif6PoidsCell = firstSheet.getRow(rowIndex).getCell(12);
                            objectif6Poids = firstSheet.getRow(rowIndex).getCell(12).getStringCellValue();
                        }
                        IUser collaborateur = null;
                        if(logins.contains(login.toLowerCase())){
                             collaborateur = getWorkflowModule().getUserByLogin(login);
                        }
                        if(collaborateur==null){
                            LOGGER.error(login +" NOT FOUND");
                            LOGGER.error("---------------------");
                            rowIndex++;
                            continue;
                        }
                        if(!objectif1.equals("")){
                            CreateObjectif(collaborateur,objectif1,objectif1Poids);
                        }
                        if(!objectif2.equals("")){
                            CreateObjectif(collaborateur,objectif2,objectif2Poids);
                        }
                        if(!objectif3.equals("")){
                            CreateObjectif(collaborateur,objectif3,objectif3Poids);
                        }
                        if(!objectif4.equals("")){
                            CreateObjectif(collaborateur,objectif4,objectif4Poids);
                        }
                        if(!objectif5.equals("")){
                            CreateObjectif(collaborateur,objectif5,objectif5Poids);
                        }
                        if(!objectif6.equals("")){
                            CreateObjectif(collaborateur,objectif6,objectif6Poids);
                        }


                    }

                    rowIndex++;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void CreateObjectif(IUser user,String objectif,String poids){
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition objectifsDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "ObjectifsDeLAnnee");
            IStorageResource objectifResource = getWorkflowModule().createStorageResource(sysContext,objectifsDefinition,"");
            objectifResource.setValue("Annee",2022);
            objectifResource.setValue("Collaborateur",user);
            objectifResource.setValue("Objectifs",objectif);
            //String poidsString = poids.replace("%", "");
            if(!poids.equals("")){
                float poidsInteger = Float.parseFloat(poids);
                int value = ((Number) (poidsInteger * 100)).intValue();
                objectifResource.setValue("Poids",value);
            }

            objectifResource.save(getWorkflowModule().getSysadminContext());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
