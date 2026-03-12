package com.moovapps.ibb.rh.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class UpdateSoldeConge extends BaseAgent {

    @Override
    protected void execute() {
        try {
            File excelFile = new File("C:\\import\\GALENICA\\SoldeConges.xlsx");
            List<String> columns = Arrays.asList("CIN","_DroitMensuelle", "_SoldeAnneeEnCours","_SoldeAnterieur","_SoldeConges"


            );
            XSSFWorkbook workbook;
            int rowIndex = 0;
            workbook = new XSSFWorkbook(new FileInputStream(excelFile));
            XSSFSheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> annuaireIterator = firstSheet.iterator();
            rowIndex = 0;

            while (annuaireIterator.hasNext()) {
                Row nextRow = annuaireIterator.next();
                if (nextRow == null) continue;
                if (rowIndex > 0) {
                    if (firstSheet.getRow(rowIndex) == null) {
                        break;
                    }
                    String cin = firstSheet.getRow(rowIndex).getCell(0)!=null?firstSheet.getRow(rowIndex).getCell(0).getStringCellValue().trim():"";
                    if(!cin.equals("")){
                        IStorageResource ficheCollaborateur = getCollaborateurByCIN(cin);
                        if(ficheCollaborateur!=null){
                            for(String colonne : columns){
                                if(colonne.startsWith("_")){
                                    Method method = this.getClass().getMethod(colonne,IStorageResource.class, Row.class, Cell.class);
                                    method.invoke(this, ficheCollaborateur,firstSheet.getRow(rowIndex),firstSheet.getRow(rowIndex).getCell(columns.indexOf(colonne)));
                                }
                            }
                            ficheCollaborateur.save(getWorkflowModule().getSysadminContext());
                            IUser user = (IUser) ficheCollaborateur.getValue("Salarie");
                            if(user!=null){
                                user.getExtendedAttributes().setValue("DroitMensuelle",ficheCollaborateur.getValue("DroitMensuelle"));
                                user.getExtendedAttributes().setValue("SoldeAnneeEnCours",ficheCollaborateur.getValue("SoldeAnneeEnCours"));
                                user.getExtendedAttributes().setValue("SoldeAnterieur",ficheCollaborateur.getValue("SoldeAnterieur"));
                                user.getExtendedAttributes().setValue("SoldeConges",ficheCollaborateur.getValue("SoldeConges"));
                                user.save(getWorkflowModule().getSysadminContext());
                            }
                        }else{
                            LOGGER.error("fiche not found at cin "+cin);
                        }
                    }else{
                        LOGGER.error("cin not found at "+rowIndex);
                    }
                }

                rowIndex++;
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public IStorageResource getCollaborateurByCIN(String cin){
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",getDirectoryModule().getOrganization(context, "DefaultOrganization"));
            ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
            IResourceDefinition resourceDefinition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("CIN",cin);
            ArrayList<IStorageResource> fiches = (ArrayList<IStorageResource>) controller.evaluate(resourceDefinition);
            if(fiches!=null && !fiches.isEmpty()){
                return fiches.iterator().next();
            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return null;
    }


    public void _DroitMensuelle(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("DroitMensuelle",cellValue);
            }else{
                instance.setValue("DroitMensuelle",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("DroitMensuelle",null);
            // isAnomalieFound=true;
        }
    }
    public void _SoldeAnneeEnCours(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("SoldeAnneeEnCours",cellValue);
            }else{
                instance.setValue("SoldeAnneeEnCours",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("SoldeAnneeEnCours",null);
            // isAnomalieFound=true;
        }
    }
    public void _SoldeAnterieur(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("SoldeAnterieur",cellValue);
            }else{
                instance.setValue("SoldeAnterieur",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("SoldeAnterieur",null);
            // isAnomalieFound=true;
        }
    }

    public void _SoldeConges(IStorageResource instance, Row row, Cell cell){
        if(cell!=null && !cell.getCellType().equals(CellType.BLANK)){
            Object cellValue = null;
            if(cell.getCellType().equals(CellType.STRING)){
                cellValue = cell.getStringCellValue().trim();
                try {
                    // cellValue = Double.parseDouble((String) cellValue);
                    cellValue =  NumberFormat.getInstance().parse((String) cellValue);

                } catch (Exception e) {
                    System.out.println("Cannot convert the string to a numeric value.");
                }
            }else if(cell.getCellType().equals(CellType.NUMERIC)){
                cellValue = cell.getNumericCellValue();
            }
            if(cellValue!=null){
                instance.setValue("SoldeConges",cellValue);
            }else{
                instance.setValue("SoldeConges",null);
                // isAnomalieFound = true;
            }
        }else{
            instance.setValue("SoldeConges",null);
            // isAnomalieFound=true;
        }
    }
}
