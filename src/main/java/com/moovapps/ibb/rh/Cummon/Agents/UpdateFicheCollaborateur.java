package com.moovapps.ibb.rh.Cummon.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.capone.rh.cummonHelpers.UserFicheAnnuaireConvert;

import java.util.Collection;
import java.util.HashMap;

public class UpdateFicheCollaborateur extends BaseAgent {

    @Override
    protected void execute() {

        Collection<IUser> users = (Collection<IUser>) getDirectoryModule().getUsers(getWorkflowModule().getSysadminContext());
        HashMap<String, IUser> usersMap = new HashMap<>();
        try {
            IOrganization organization = getDirectoryModule().getOrganization(getWorkflowModule().getSysadminContext(), "DefaultOrganization");
            int i = 0;
            for (IUser user : users) {
                IStorageResource updatedFiche = new UserFicheAnnuaireConvert().fronUserToFiche(user, getWorkflowModule(),getProjectModule(),organization);
                if(updatedFiche != null){
                    updatedFiche.save(getWorkflowModule().getSysadminContext());
                }else {
                    System.out.println(user.getFullName() + " : " + user.getLogin());
                }
                i++;
                System.out.println(i);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


//        try {
//            IContext sysContext = getWorkflowModule().getSysadminContext();
//            File excelFile = new File("D:\\IEG Maquette\\test annuaire.xlsx");
//            XSSFWorkbook workbook;
//            int rowIndex = 0;
//            try {
//                workbook = new XSSFWorkbook(new FileInputStream(excelFile));
//                XSSFSheet firstSheet = workbook.getSheetAt(2);
//                Iterator<Row> annuaireIterator = firstSheet.iterator();
//                rowIndex = 0;
//
//                while (annuaireIterator.hasNext()) {
//                    Row nextRow = annuaireIterator.next();
//                    if (rowIndex == 0) {
//
//                    }
//                    // rowIndex++;
//                    if (nextRow == null) continue;
//                    if (rowIndex > 0) {
//                        if (firstSheet.getRow(rowIndex) == null) {
//                            break;
//                        }
//                        String collaborateurMatricule = firstSheet.getRow(rowIndex).getCell(0).getStringCellValue();
//                        String evaluateurMatricule = firstSheet.getRow(rowIndex).getCell(1).getStringCellValue();
//
//                        IUser collaborateur = usersMap.get(collaborateurMatricule);
//                        IUser evaluateur = usersMap.get(evaluateurMatricule);
//                        collaborateur.getExtendedAttributes().setValue("Evaluateur", evaluateur);
//                        collaborateur.save(getWorkflowModule().getSysadminContext());
//                    }
//
//                    rowIndex++;
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }
}
