package com.moovapps.EVALUATION.Reprise.Referentiels;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.EVALUATION.Reprise.Helpers.Maquetts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.*;


public class NotationCompetences extends BaseAgent {
    List<String> colonnes = Maquetts.NotationCompetence;

    @Override
    protected void execute() {
        Run();
    }

    private void Run() {
        try {


            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition NotationCompetenceDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "ResultatEvaluation");
            File excelFile = new File("C:\\import\\EVAL\\Competences.xlsx");
            XSSFWorkbook workbook;
            try {
                workbook = new XSSFWorkbook(new FileInputStream(excelFile));
                XSSFSheet firstSheet = workbook.getSheetAt(1);
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

                       // IStorageResource competenceResource = getWorkflowModule().createStorageResource(sysContext, competenceDefinition, null, null);                        //ILinkedResource factureAvalider = document.createLinkedResource("FacturesControle");
                        Cell profilCell  = firstSheet.getRow(rowIndex).getCell(0);
                        Cell familleCompetenceCell  = firstSheet.getRow(rowIndex).getCell(1);
                        Cell competenceCell  = firstSheet.getRow(rowIndex).getCell(2);
                        if(profilCell==null || familleCompetenceCell==null || competenceCell==null){
                            continue;
                        }
                        String profilValue = profilCell.getStringCellValue().trim();
                        String familleCompetenceValue = familleCompetenceCell.getStringCellValue().trim();
                        String competenceValue = competenceCell.getStringCellValue().trim();
                        if(profilValue==null || profilValue.equals("") || familleCompetenceValue==null || familleCompetenceValue.equals("") || competenceValue==null || competenceValue.equals("")){
                            continue;
                        }

                        IStorageResource profil = getObject("Profil","sys_Title",profilValue);

                        HashMap<Integer, ArrayList<Object>> filters = new HashMap<>();
                        ArrayList<Object> controll1 = new ArrayList<>();
                        controll1.add("Profil");
                        controll1.add(profil);
                        controll1.add(0);
                        filters.put(0,controll1);

                        ArrayList<Object> controll2 = new ArrayList<>();
                        controll2.add("FamilleProfil");
                        controll2.add(familleCompetenceValue);
                        controll2.add(0);
                        filters.put(1,controll2);
                        IStorageResource familleCompetence = getObjectByMultipleFilter("FamilleProfile",filters);

                        HashMap<Integer, ArrayList<Object>> filters2 = new HashMap<>();
                        ArrayList<Object> controll3 = new ArrayList<>();
                        controll3.add("Profil");
                        controll3.add(profil);
                        controll3.add(0);
                        filters2.put(0,controll3);

                        ArrayList<Object> controll4 = new ArrayList<>();
                        controll4.add("FamilleProfil");
                        controll4.add(familleCompetence);
                        controll4.add(0);
                        filters2.put(1,controll4);

                        ArrayList<Object> controll5 = new ArrayList<>();
                        controll5.add("sys_Title");
                        controll5.add(competenceValue);
                        controll5.add(0);

                        filters2.put(3,controll5);
                        IStorageResource competence = getObjectByMultipleFilter("Competences",filters2);
                        if(competence==null){
                            continue;
                        }

                        for (int i = 0; i < colonnes.size(); i++) {
                            String colonne = colonnes.get(i);
                            Cell cell = null;
                            if (firstSheet.getRow(rowIndex) != null && firstSheet.getRow(rowIndex).getCell(i) != null) {
                                cell = firstSheet.getRow(rowIndex).getCell(i);
                            }
                            if (colonne.startsWith("_")) {
                                try {
                                    Method method = this.getClass().getMethod(colonnes.get(i), IStorageResource.class, Cell.class, int.class);
                                    method.invoke(this, competence, cell, rowIndex);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        //competenceResource.save(sysContext);

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


    public void _NIV1(IStorageResource competence, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                //create storage
                CreateNotation(competence,(String) cellValue,1);

            }

        }

    }

    public void _NIV2(IStorageResource competence, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                //create storage
                CreateNotation(competence,(String) cellValue,2);

            }

        }

    }

    public void _NIV3(IStorageResource competence, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                //create storage
                CreateNotation(competence,(String) cellValue,3);

            }

        }

    }

    public void _NIV4(IStorageResource competence, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                //create storage
                CreateNotation(competence,(String) cellValue,4);

            }

        }

    }

    private void CreateNotation(IStorageResource competence,String notation,int lvl){
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition notationCompetenceDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "ResultatEvaluation");
            if(competence==null) return;
            IStorageResource notationResource = getWorkflowModule().createStorageResource(sysContext, notationCompetenceDefinition, null, null);
            notationResource.setValue("Competence",competence);
            if(lvl==1){
                notationResource.setValue("sys_Title","Non maitrisée");
                notationResource.setValue("Valeur",1);

            }else if(lvl==2){
                notationResource.setValue("sys_Title","Partiellement matrisée ou en cours d'acquisition");
                notationResource.setValue("Valeur",2);

            }else if(lvl==3){
                notationResource.setValue("sys_Title","Bonne maîtrise, autonomie dans la réalisation d'une compétence donnée");
                notationResource.setValue("Valeur",3);

            }else if(lvl==4){
                notationResource.setValue("sys_Title","Excellence maîtrisée");
                notationResource.setValue("Valeur",4);

            }
            notationResource.setValue("Definition",notation);
            notationResource.save(getWorkflowModule().getSysadminContext());
            //ILinkedResource factureAvalider = document.createLinkedResource("FacturesControle");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void _Profil(IStorageResource instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                IStorageResource profil = getObject("Profil","sys_Title",(String) cellValue);
                if (profil != null) {
                    instance.setValue("Profil", profil);

                }

            }

        }

    }
    public void _FamilleCompetence(IStorageResource instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                IStorageResource profil = (IStorageResource) instance.getValue("Profil");
                HashMap<Integer, ArrayList<Object>> filters = new HashMap<>();
                ArrayList<Object> controll1 = new ArrayList<>();
                controll1.add("Profil");
                controll1.add(profil);
                filters.put(0,controll1);

                ArrayList<Object> controll2 = new ArrayList<>();
                controll1.add("FamilleProfil");
                controll1.add(cellValue);
                filters.put(0,controll2);
                IStorageResource familleCompetence = getObjectByMultipleFilter("FamilleProfile",filters);
                if (familleCompetence != null) {
                    instance.setValue("FamilleProfil", familleCompetence);

                }

            }

        }

    }


    private IStorageResource getObject(String objectSysName,String objectNameSysName,String objectName) {
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,"Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, objectSysName);
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint(objectNameSysName, objectName);
            ArrayList<IStorageResource> objects = (ArrayList<IStorageResource>) controller.evaluate(definition);
            if(objects!=null && !objects.isEmpty()){
                IStorageResource object = (IStorageResource) controller.evaluate(definition).iterator().next();
                return object;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private IStorageResource getObjectByMultipleFilter(String objectSysName, HashMap<Integer,ArrayList<Object>> filters) {
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context,"Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, objectSysName);
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            for(Map.Entry<Integer, ArrayList<Object>> entry : filters.entrySet()) {
                int index = entry.getKey();
                ArrayList<Object> value = entry.getValue();
                String nomSysteme = (String) value.get(0);
                Object valeurRechercher = value.get(1);
                int operation = (int) value.get(2);
                if(valeurRechercher==null)continue;
                if(operation==0){
                    controller.addEqualsConstraint(nomSysteme, valeurRechercher);
                }
            }
            ArrayList<IStorageResource> objects = (ArrayList<IStorageResource>) controller.evaluate(definition);
            if(objects!=null && !objects.isEmpty()){
                IStorageResource object = (IStorageResource) controller.evaluate(definition).iterator().next();
                return object;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }





    public void _Competence(IStorageResource instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                instance.setValue("sys_Title", cellValue);
            }

        }
    }

    public void _Definition(IStorageResource instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                instance.setValue("Definition", cellValue);
            }

        }
    }


}


