package com.moovapps.EVALUATION.Reprise.Annuaire;

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

public class Utilisateurs extends BaseAgent {
    List<String> colonnes = Maquetts.users;
    HashMap<String, String> organizations = new HashMap<>();

    @Override
    protected void execute() {
        Run();
    }

    private void Run() {
        try {
            //  organizations.put("MAROC SOIR","MAROCSOIR");
            //organizations.put("GMS MEDIA","GMSMEDIAS");
            //organizations.put("LES IMPRIMERIES DU MATIN","LIDM");
          /*  IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition competenceDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Competences");*/
            File excelFile = new File("C:\\import\\EVAL\\Utilisateurs.xlsx");
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
                        Cell collaborateurLoginCell = firstSheet.getRow(rowIndex).getCell(1);
                        if (collaborateurLoginCell == null) {
                            rowIndex++;
                            LOGGER.error("Login :" + rowIndex + " Login Vide");
                            continue;
                        }
                        collaborateurLoginCell.setCellType(CellType.STRING);
                        String collaborateurLogin = collaborateurLoginCell.getStringCellValue().trim();
                        IUser collaborateur = getWorkflowModule().getUserByLogin(collaborateurLogin);
                        if (collaborateur == null) {
                            collaborateur = getDirectoryModule().createUser(getDirectoryModule().getSysadminContext(), collaborateurLogin, "D3mo@demo", getDirectoryModule().getOrganization(getDirectoryModule().getSysadminContext(), "MAROCSOIR" ));
//                            rowIndex++;
//                            LOGGER.error("Collaborateur: " + rowIndex + " " + collaborateurLogin + " COLLABORATEUR NOT FOUND");
//                            continue;
                        }

                        //IStorageResource competenceResource = getWorkflowModule().createStorageResource(sysContext, competenceDefinition, null, null);                        //ILinkedResource factureAvalider = document.createLinkedResource("FacturesControle");

                        for (int i = 0; i < colonnes.size(); i++) {
                            String colonne = colonnes.get(i);
                            Cell cell = null;
                            if (firstSheet.getRow(rowIndex) != null && firstSheet.getRow(rowIndex).getCell(i) != null) {
                                cell = firstSheet.getRow(rowIndex).getCell(i);
                            }
                            if (colonne.startsWith("_")) {
                                try {
                                    Method method = this.getClass().getMethod(colonnes.get(i), IUser.class, Cell.class, int.class);
                                    method.invoke(this, collaborateur, cell, rowIndex);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        collaborateur.save(getWorkflowModule().getSysadminContext());

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


    public void _Matricule(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                instance.getExtendedAttributes().setValue("Matricule", cellValue);
            }

        }

    }

    public void _FirstName(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                instance.setFirstName((String) cellValue);
            }

        }

    }

    public void _LastName(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                instance.setLastName((String) cellValue);
            }

        }

    }

    public void _DateIntegration(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            // cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getDateCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                instance.setEntry((Date) cellValue);
            }
        }
    }

    public void _Fonction(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                IStorageResource fonction = getFonction((String) cellValue);
                if(fonction == null){
                    LOGGER.error("Fonction : " + rowIndex + " " + cellValue + " " + "Not Found");
                }
                instance.getExtendedAttributes().setValue("Fonction2", fonction);

            }

        }

    }

    public void _Filiale(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                IStorageResource filiale = getFiliale((String) cellValue);
                if(filiale == null){
                    LOGGER.error("Filiale: " + rowIndex + " " + cellValue + " " + "Not Found");
                }else{
                    instance.getExtendedAttributes().setValue("Societe", filiale);
                    try {
                        instance.setOrganization(getDirectoryModule().getOrganization(getDirectoryModule().getSysadminContext(), cellValue.equals("MAROC SOIR") ? "MAROCSOIR" : cellValue.equals("GMS MEDIA") ? "GMSMEDIAS" : "LIDM"));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public void _Direction(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                IStorageResource filiale = (IStorageResource) instance.getExtendedAttributes().getValue("Societe");
                IStorageResource direction = getDirection(filiale, (String) cellValue);
                if(direction == null){
                    LOGGER.error("Direction: " + rowIndex + " " + cellValue + " " + "Not Found");
                }
                instance.getExtendedAttributes().setValue("Direction", direction);

            }

        }

    }

    public void _Departement(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                IStorageResource filiale = (IStorageResource) instance.getExtendedAttributes().getValue("Societe");
                IStorageResource direction = (IStorageResource) instance.getExtendedAttributes().getValue("Direction");
                IStorageResource departement = getDepartement(filiale, direction, (String) cellValue);
                if(departement == null){
                    LOGGER.error("Département: " + rowIndex + " " + cellValue + " " + "Not Found");
                }
                instance.getExtendedAttributes().setValue("Departement", departement);

            }

        }

    }

    public void _Service(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                IStorageResource filiale = (IStorageResource) instance.getExtendedAttributes().getValue("Societe");
                IStorageResource direction = (IStorageResource) instance.getExtendedAttributes().getValue("Direction");
                IStorageResource departement = (IStorageResource) instance.getExtendedAttributes().getValue("Departement");
                if (direction == null || departement == null) return;
                IStorageResource service = getService(filiale, direction, departement, (String) cellValue);
                if(service == null){
                    LOGGER.error("Service: " + rowIndex + " " + cellValue + " " + "Not Found");
                }
                instance.getExtendedAttributes().setValue("Service2", service);

            }

        }

    }

    public void _Adresse(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                instance.setAddress1((String) cellValue);
            }

        }

    }

    public void _Tel(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                String value = (String) cellValue;
                instance.setMobilePhoneNumber(value);

                /*if (value.contains("(")) {
                    String output = value.replaceAll("\\(.*?\\)", "").trim().replace(".", " ");
                    instance.setMobilePhoneNumber(output);
                } else {
                    String output = value.trim().replace(".", " ");
                    instance.setMobilePhoneNumber(output);
                }*/

            }

        }

    }

    public void _Fixe(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                instance.setPhoneNumber((String) cellValue);
                /*String value = (String) cellValue;
                if (value.contains("(")) {
                    String output = value.replaceAll("\\(.*?\\)", "").trim().replace(".", " ");
                    instance.setPhoneNumber(output);
                } else {
                    String output = value.trim().replace(".", " ");
                    instance.setPhoneNumber(output);
                }*/

            }

        }

    }

    public void _Email(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                instance.setEmail((String) cellValue);
            }else{
                instance.setEmail(null);
            }
        }else{
            instance.setEmail(null);
        }
    }

    public void _N1(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                try {
                    IUser reponsable = getWorkflowModule().getUserByLogin((String) cellValue);
                    if (reponsable == null) {
                        LOGGER.error("N1: " + rowIndex + " " + cellValue + " N1 NOT FOUND");
                    } else {
                        instance.setHierarchicalManager(reponsable);
                        instance.getExtendedAttributes().setValue("Evaluateur", reponsable);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }




    public void _Profil(IUser instance, Cell cell, int rowIndex) {
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            cell.setCellType(CellType.STRING);
            Object cellValue = null;
            if (cell.getCellType() == CellType.NUMERIC) {
                cellValue = cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                cellValue = cell.getStringCellValue().trim();
            }
            if (cellValue != null) {
                IStorageResource profil = getProfil((String) cellValue);
                if (profil == null) {
                    LOGGER.error("Profil: " + rowIndex + " " + cellValue + " Profil NOT FOUND");
                }else {
                    instance.getExtendedAttributes().setValue("ProfilEVAL", profil);
                }

            }

        }

    }

    IStorageResource getService(IStorageResource filiale,IStorageResource direction, IStorageResource departement, String name) {
        IStorageResource fonction = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "WIZEORGA", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition fonctionDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Service");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            controller.addEqualsConstraint("sys_Title", name);
            controller.addEqualsConstraint("Direction", direction);
            controller.addEqualsConstraint("Societe", filiale);
            controller.addEqualsConstraint("Departement", departement);
            ArrayList<IStorageResource> fonctions = (ArrayList<IStorageResource>) controller.evaluate(fonctionDefinition);
            if (fonctions != null && !fonctions.isEmpty()) {
                fonction = fonctions.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fonction;
    }


    IStorageResource getDepartement(IStorageResource filiale,IStorageResource direction, String name) {
        IStorageResource fonction = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "WIZEORGA", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition fonctionDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Departement");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            controller.addEqualsConstraint("sys_Title", name);
            controller.addEqualsConstraint("Societe", filiale);
            controller.addEqualsConstraint("Direction", direction);
            ArrayList<IStorageResource> fonctions = (ArrayList<IStorageResource>) controller.evaluate(fonctionDefinition);
            if (fonctions != null && !fonctions.isEmpty()) {
                fonction = fonctions.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fonction;
    }

    IStorageResource getFonction(String name) {
        IStorageResource fonction = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "WIZEORGA", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition fonctionDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Fonction2");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            controller.addEqualsConstraint("sys_Title", name);
            ArrayList<IStorageResource> fonctions = (ArrayList<IStorageResource>) controller.evaluate(fonctionDefinition);
            if (fonctions != null && !fonctions.isEmpty()) {
                fonction = fonctions.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fonction;
    }

    IStorageResource getProfil(String name) {
        IStorageResource fonction = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition fonctionDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Profil");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            controller.addEqualsConstraint("sys_Title", name);
            ArrayList<IStorageResource> fonctions = (ArrayList<IStorageResource>) controller.evaluate(fonctionDefinition);
            if (fonctions != null && !fonctions.isEmpty()) {
                fonction = fonctions.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fonction;
    }

    IStorageResource getDirection(IStorageResource filiale, String name) {
        IStorageResource fonction = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "WIZEORGA", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition fonctionDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Direction");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            controller.addEqualsConstraint("sys_Title", name);
            controller.addEqualsConstraint("Societe", filiale);
            ArrayList<IStorageResource> fonctions = (ArrayList<IStorageResource>) controller.evaluate(fonctionDefinition);
            if (fonctions != null && !fonctions.isEmpty()) {
                fonction = fonctions.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fonction;
    }

    IStorageResource getFiliale(String name) {
        IStorageResource fonction = null;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "WIZEORGA", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition fonctionDefinition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "Societe");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            controller.addEqualsConstraint("sys_Title", name);
            ArrayList<IStorageResource> fonctions = (ArrayList<IStorageResource>) controller.evaluate(fonctionDefinition);
            if (fonctions != null && !fonctions.isEmpty()) {
                fonction = fonctions.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fonction;
    }


}
