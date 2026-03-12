package com.moovapps.ibb.rh.Cummon.Agents;


import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.exceptions.ProjectModuleException;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class UpdateAnnuaireWIthN1AndEvaluateur extends BaseAgent {

    HashMap<String,List<String>> etablissementsAlternativeForEvaluators = new HashMap<>();


    IStorageResource getEtablissementByTitle(String name) {
        IStorageResource etablisement = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", getDirectoryModule().getOrganization(context, "DefaultOrganization"));
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
            controller.addEqualsConstraint("sys_Title", name);
            Collection<IStorageResource> societes = controller.evaluate(definition);
            if (!societes.isEmpty()) {
                etablisement = societes.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return etablisement;
    }

    HashMap<MatriculeEtablissementModel, IUser> usersMap = new HashMap<>();

    public IStorageResource getFicheSalarieByMatriculeEtablissement(String mat, IStorageResource etablissement) {
        IStorageResource ficheSalarie = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("SocieteDonnee", etablissement);
            controller.addEqualsConstraint("Matricule", mat);
            Collection<IStorageResource> fiches = controller.evaluate(definition);
            if (fiches != null && !fiches.isEmpty()) {
                ficheSalarie = fiches.iterator().next();
            }
            return ficheSalarie;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ficheSalarie;
    }

    @Override
    protected void execute() {
        //Collection<IUser> users = (Collection<IUser>) getDirectoryModule().getUsers(getWorkflowModule().getSysadminContext());
//        for(IUser user :users){
//            String matricule = (String) user.getExtendedAttributes().getValue("Matricule");
//            IStorageResource etablissement = (IStorageResource) user.getExtendedAttributes().getValue("SocieteDonnee");
//            usersMap.put(new MatriculeEtablissementModel(matricule,etablissement),user);}

        etablissementsAlternativeForEvaluators.put("Lycée Français Guy de Maupassant CL",Arrays.asList("Lycée Français Guy de Maupassant MP","International Education Group"));
        etablissementsAlternativeForEvaluators.put("Lycée Français Sophie Germain CL",Arrays.asList("Lycée Français Sophie Germain MP","International Education Group"));

        etablissementsAlternativeForEvaluators.put("Lycée Français Guy de Maupassant MP",Arrays.asList("Lycée Français Guy de Maupassant CL","International Education Group"));
        etablissementsAlternativeForEvaluators.put("Lycée Français Sophie Germain MP",Arrays.asList("Lycée Français Sophie Germain CL","International Education Group"));

        etablissementsAlternativeForEvaluators.put("International French School d’Amsterdam",Arrays.asList("International Education Group"));
        etablissementsAlternativeForEvaluators.put("Ecole Française Guy de Maupassant",Arrays.asList("International Education Group","Lycée Français Sophie Germain MP"));
        etablissementsAlternativeForEvaluators.put("Ecole Trilingue Internationale",Arrays.asList("International Education Group"));


        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            File excelFile = new File("D:\\import\\IEG\\UpdateEvaluateurs.xlsx");
            XSSFWorkbook workbook;

            int rowIndex = 0;
            try {
                workbook = new XSSFWorkbook(new FileInputStream(excelFile));
                XSSFSheet firstSheet = workbook.getSheetAt(0);
                Iterator<Row> annuaireIterator = firstSheet.iterator();
                rowIndex = 0;

                while (annuaireIterator.hasNext()) {
                    Row nextRow = annuaireIterator.next();
                    if (rowIndex == 0) {

                    }
                    // rowIndex++;
                    if (nextRow == null) continue;
                    if (rowIndex > 0) {
                        if (firstSheet.getRow(rowIndex) == null) {
                            break;
                        }

                   /* String collaborateurLogin = firstSheet.getRow(rowIndex).getCell(3)!=null? firstSheet.getRow(rowIndex).getCell(3).getStringCellValue().trim().toLowerCase():"";
                    if(collaborateurLogin.equals("")){
                        System.out.println("login est vide a la ligne "+rowIndex);
                        rowIndex++;
                        continue;
                    }*/
                        // here
                        if (firstSheet.getRow(rowIndex).getCell(0) != null) {
                            firstSheet.getRow(rowIndex).getCell(0).setCellType(CellType.STRING);
                        }
                        String collaborateurMat = firstSheet.getRow(rowIndex).getCell(0) != null ? firstSheet.getRow(rowIndex).getCell(0).getStringCellValue() : "";

                        if (firstSheet.getRow(rowIndex).getCell(1) != null) {
                            firstSheet.getRow(rowIndex).getCell(1).setCellType(CellType.STRING);
                        }
                        String collaborateurEtablissement = firstSheet.getRow(rowIndex).getCell(1) != null ? firstSheet.getRow(rowIndex).getCell(1).getStringCellValue() : "";

                        if (collaborateurMat == null || collaborateurEtablissement == null) {
                            System.out.println(rowIndex + " : MAT: " + collaborateurMat + " - Etablissement: " + collaborateurEtablissement);
                            rowIndex++;
                            continue;
                        } else {
                            IStorageResource etablissement = getEtablissementByTitle(collaborateurEtablissement);
                            IStorageResource ficheSalarie = null;
                            try {
                                ficheSalarie = getFicheSalarieByMatriculeEtablissement(collaborateurMat, etablissement);
                                if(ficheSalarie==null){
                                    List<String> alternativeEtablissements = etablissementsAlternativeForEvaluators.get(collaborateurEtablissement);
                                    for(String alternativeEtablissementName : alternativeEtablissements){
                                        IStorageResource alternativeEtablissement = getEtablissementByTitle(alternativeEtablissementName);
                                        ficheSalarie = getFicheSalarieByMatriculeEtablissement(collaborateurMat, alternativeEtablissement);
                                        if(ficheSalarie!=null){
                                            break;
                                        }
                                    }
                                }
                                if (ficheSalarie != null) {
                                    IUser salarie = (IUser) ficheSalarie.getValue("Salarie");
                                    if (true || salarie.getExtendedAttributes().getValue("Evaluateur") == null) {
                                        if (firstSheet.getRow(rowIndex).getCell(2) != null) {
                                            firstSheet.getRow(rowIndex).getCell(2).setCellType(CellType.STRING);
                                        }
                                        String evaluateurMatricule = firstSheet.getRow(rowIndex).getCell(2) != null ? firstSheet.getRow(rowIndex).getCell(2).getStringCellValue() : "";
                                        IStorageResource ficheEvaluateur = getFicheSalarieByMatriculeEtablissement(evaluateurMatricule, etablissement);
                                        if(ficheEvaluateur==null){
                                            List<String> alternativeEtablissements = etablissementsAlternativeForEvaluators.get(collaborateurEtablissement);
                                            for(String alternativeEtablissementName : alternativeEtablissements){
                                                IStorageResource alternativeEtablissement = getEtablissementByTitle(alternativeEtablissementName);
                                                ficheEvaluateur = getFicheSalarieByMatriculeEtablissement(evaluateurMatricule, alternativeEtablissement);
                                                if(ficheEvaluateur!=null){
                                                    break;
                                                }
                                            }

                                        }
                                        if (ficheEvaluateur != null) {
                                            salarie.getExtendedAttributes().setValue("Evaluateur", ficheEvaluateur.getValue("Salarie"));
                                            salarie.save(getWorkflowModule().getSysadminContext());
                                            ficheSalarie.setValue("Evaluateur",ficheEvaluateur.getValue("Salarie"));
                                            ficheSalarie.save(getWorkflowModule().getSysadminContext());
                                            System.out.println(rowIndex + " : GOOD");
                                        } else {
                                            System.out.print(rowIndex + " : Evaluateur not found : ");
                                            System.out.println("MAT: " + evaluateurMatricule + " - Etablissement: " + collaborateurEtablissement);
                                        }
                                    }
                                } else {
                                    System.out.print(rowIndex + " : Collaborateur not found : ");
                                    System.out.println("MAT: " + collaborateurMat + " - Etablissement: " + collaborateurEtablissement);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }


//                    if(collaborateurEtablissement==null){
//                        System.out.println("matricule est vide a la ligne "+rowIndex);
//                        rowIndex++;
//                        continue;
//                    }


//                    MatriculeEtablissementModel collaborateurModel = new MatriculeEtablissementModel(collaborateurMat,etablissement);
//                    IUser collaborateur = usersMap.get(collaborateurModel);
//                    if(collaborateur ==null){
//                        System.out.println("collaborateur with matricule and login not found "+collaborateurModel.matricule + " "+ collaborateurModel.etablissement.getValue("sys_Title"));
//                        rowIndex++;
//                        continue;
//                    }
//                    if(collaborateur.getExtendedAttributes().getValue("Evaluateur")!=null){
//                        //deja l evaluateur msete l had sat
//                        rowIndex++;
//                        continue;
//                    }

//                    if(firstSheet.getRow(rowIndex).getCell(4)!=null){
//                        firstSheet.getRow(rowIndex).getCell(4).setCellType(CellType.STRING);
//                    }
//
//                    String evaluateurMatricule = firstSheet.getRow(rowIndex).getCell(4)!=null? firstSheet.getRow(rowIndex).getCell(4).getStringCellValue():"";
//                    if(evaluateurMatricule.equals("")){
//                        System.out.println("evaluateur matricule est vide a la ligne "+rowIndex);
//                        rowIndex++;
//                        continue;
//                    }

                        //  String evaluateurLogin = firstSheet.getRow(rowIndex).getCell(7)!=null? firstSheet.getRow(rowIndex).getCell(7).getStringCellValue().trim().toLowerCase():"";
                        //  if(evaluateurLogin.equals("")){
                        //    System.out.println("evaluateur login est vide a la ligne "+rowIndex);
                        //  rowIndex++;
                        // continue;
                        // }

//                    MatriculeEtablissementModel evaluateurModel = new MatriculeEtablissementModel(evaluateurMatricule,etablissement);
//                    IUser evaluateur = usersMap.get(evaluateurModel);
//                    if(evaluateur==null){
//                        System.out.println("evaluateur  with matricule and login not found "+evaluateurModel.matricule + " "+evaluateurModel.etablissement.getValue("sys_Title"));
//                        rowIndex++;
//                        continue;
//                    }
//                    collaborateur.getExtendedAttributes().setValue("Evaluateur",evaluateur);
//                    collaborateur.save(getWorkflowModule().getSysadminContext());
//                    IOrganization organization = getDirectoryModule().getOrganization(getWorkflowModule().getSysadminContext(), "DefaultOrganization");
//                    IStorageResource updatedFiche = new UserFicheAnnuaireConvert().fronUserToFiche(collaborateur, getWorkflowModule(),getProjectModule(),organization);
//                    if(updatedFiche != null){
//                        updatedFiche.save(getWorkflowModule().getSysadminContext());
//                    }else {
//                        System.out.println(collaborateur.getFullName() + " : " + collaborateur.getLogin());
//                    }
//
//                    System.out.println("row current is "+rowIndex);
                        //  String collaborateurMatricule = firstSheet.getRow(rowIndex).getCell(0).getStringCellValue();
                        // String evaluateurMatricule = firstSheet.getRow(rowIndex).getCell(1).getStringCellValue();

                        //IUser collaborateur = usersMap.get(collaborateurMatricule);
                        //IUser evaluateur = usersMap.get(evaluateurMatricule);
                        //collaborateur.getExtendedAttributes().setValue("Evaluateur", evaluateur);
                        //collaborateur.save(getWorkflowModule().getSysadminContext());

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
}