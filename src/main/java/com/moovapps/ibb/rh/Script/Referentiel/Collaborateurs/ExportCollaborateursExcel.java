package com.moovapps.ibb.rh.Script.Referentiel.Collaborateurs;

import com.axemble.vdoc.sdk.interfaces.IGroup;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ExportCollaborateursExcel {

    /*

    Arrays.asList("_Pole","_Societe","_Matricule", "_Actif", "_Civilite", "_Nom", "_Prenom", "_NumTelephone", "_CodeMobile",
    "_Extension","_DateNaisssance", "_CIN", "_NImmatriculationCNSS","_Ville",
                        "_Adresse","_Adresse2", "_EtatCivil", "_Maladie","_Email",
                        "_Identifiant","_MotDePasse","_RIB","_Banque","_AgenceBancaire",
                        "_NumeroFax","_NumeroAgence","_Domiciliation","_MainLever",
                        "_DateDEmbauche","_DateDEmbaucheGroupe","_TypeDeContrat","_Groupes",
                        "_SuperieurHierarchique","_Evaluateur","_Suppleant",
                        "Direction","_Departement","_Activite","_Service","_Metier",
                        "Division","_Site","_Fonction","_Grade","_Categorie","_ProfilEvaluation",
                        "_TypeDeSalaire","_TauxHoraire","_SalaireDeBase","_SalaireBrut",
                        "_SalaireBrutImposable","_Remuneration","_SalaireNET",
                        "_NMutuelle",
                        "_NomMutuelle","_TauxMutuelle","_NCIMR","_NomRetraite",
                        "_TauxRetraire","_NumeroAttribue",
                        "_NomRetraiteComplementaire",
                        "_TauxRetraiteComplementaire","_MontantEpargneRetraite",
                        "_DroitMensuel","_SoldeAnneeEnCours","_SoldeAnterieur","_CongesPris")


     */

    String getSysTitle(Object ref){
        if(ref instanceof IStorageResource){
            return (String)((IStorageResource) ref).getValue("sys_Title");
        }else{
            return "";
        }
    }

    public boolean _Fonction(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Fonction") != null){
                excelCell.setCellValue(getSysTitle(ficheUser.getValue("Fonction")));
            }
        }

        return true;
    }

    public boolean _Grade(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Grade") != null){
                excelCell.setCellValue(getSysTitle(ficheUser.getValue("Grade")));
            }
        }

        return true;
    }



    public boolean _Categorie(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Categorie") != null){
                excelCell.setCellValue(getSysTitle(ficheUser.getValue("Categorie")));
            }
        }

        return true;
    }



    public boolean _ProfilEvaluation(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("ProfilEvaluation") != null){
                excelCell.setCellValue(getSysTitle(ficheUser.getValue("ProfilEvaluation")));
            }
        }

        return true;
    }

    public boolean _Site(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Site") != null){
                excelCell.setCellValue(getSysTitle( ficheUser.getValue("Site")) );
            }
        }

        return true;
    }

    public boolean _SuperieurHierarchique(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("HierarchicalManager") != null){
                excelCell.setCellValue((String) ((IUser) ficheUser.getValue("HierarchicalManager")).getExtendedAttributes().getValue("CIN"));
            }
        }

        return true;
    }

    public boolean _Evaluateur(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Evaluateur") != null){
                excelCell.setCellValue((String) ((IUser) ficheUser.getValue("Evaluateur")).getExtendedAttributes().getValue("CIN"));
            }
        }

        return true;
    }

    public boolean _Suppleant(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Suppleant") != null){
                List<String> groupesLabels = ((ArrayList<IStorageResource>)ficheUser.getValue("Suppleant")).stream().map(g->(String) g.getValue("Identifiant")).collect(Collectors.toList());
                if (groupesLabels!=null && !groupesLabels.isEmpty()) {
                    excelCell.setCellValue(String.join(";", groupesLabels));
                }
            }
        }

        return true;
    }

    public boolean _DateSortie(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();
            CreationHelper createHelper = row.getSheet().getWorkbook().getCreationHelper();
            cellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
            excelCell.setCellValue(ficheUser.getValue("DateDeSortie") != null ? (Date) ficheUser.getValue("DateDeSortie") : null);
            excelCell.setCellStyle(cellStyle);
        }

        return true;
    }

    public boolean _MotifSortie(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("MotifSortie") != null){
                excelCell.setCellValue((String) ficheUser.getValue("MotifSortie") );
            }
        }

        return true;
    }

    public boolean _TypeDeSalaire(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("TypeDeSalaire") != null){
                excelCell.setCellValue((String) ficheUser.getValue("TypeDeSalaire") );
            }
        }

        return true;
    }

    public boolean _TauxHoraire(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("TauxHoraire") != null){
                excelCell.setCellValue((Number) ficheUser.getValue("TauxHoraire") + "");
            }
        }

        return true;
    }

    public boolean _Matricule(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Matricule") != null){
                excelCell.setCellValue((String) ficheUser.getValue("Matricule") );
            }
        }

        return true;
    }

    public boolean _Actif(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Actif") != null){
                excelCell.setCellValue(((boolean) ficheUser.getValue("Actif"))? "Oui" : "Non");
            }
        }

        return true;
    }

    public boolean _Civilite(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Title") != null){
                excelCell.setCellValue((String)ficheUser.getValue("Title"));
            }
        }

        return true;
    }

    public boolean _Nom(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("LastName") != null){
                excelCell.setCellValue((String) ficheUser.getValue("LastName") );
            }
        }

        return true;
    }

    public boolean _Prenom(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("FirstName") != null){
                excelCell.setCellValue((String) ficheUser.getValue("FirstName") );
            }
        }

        return true;
    }

    public boolean _NumTelephone(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("CodeMobile") != null){
                excelCell.setCellValue((String) ficheUser.getValue("CodeMobile") );
            }
        }

        return true;
    }
    public boolean _CodeMobile(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("MobilePhoneNumber") != null){
                excelCell.setCellValue((String) ficheUser.getValue("MobilePhoneNumber") );
            }
        }

        return true;
    }



    public boolean _Extension(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Extension") != null){
                excelCell.setCellValue((String) ficheUser.getValue("Extension") );
            }
        }

        return true;
    }

    public boolean _DateNaisssance(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();
           CreationHelper createHelper = row.getSheet().getWorkbook().getCreationHelper();
            cellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
            excelCell.setCellValue(ficheUser.getValue("Birthday") != null ? (Date) ficheUser.getValue("Birthday") : null);
            excelCell.setCellStyle(cellStyle);
        }

        return true;
    }

    public boolean _CIN(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("CIN") != null){
                excelCell.setCellValue((String) ficheUser.getValue("CIN") );
            }
        }

        return true;
    }

    public boolean _NImmatriculationCNSS(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("NCNSS") != null){
                excelCell.setCellValue((String) ficheUser.getValue("NCNSS") );
            }
        }

        return true;
    }

    public boolean _Ville(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Ville") != null){
                excelCell.setCellValue((String) ficheUser.getValue("Ville") );
            }
        }

        return true;
    }

    public boolean _Adresse(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Address1") != null){
                excelCell.setCellValue((String) ficheUser.getValue("Address1") );
            }
        }

        return true;
    }
    public boolean _Adresse2(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Adresse2") != null){
                excelCell.setCellValue((String) ficheUser.getValue("Adresse2") );
            }
        }

        return true;
    }

    public boolean _EtatCivil(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("EtatCivil") != null){
                excelCell.setCellValue((String) ficheUser.getValue("EtatCivil") );
            }
        }

        return true;
    }

    public boolean _Maladie(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Maladie") != null){
                excelCell.setCellValue((String) ficheUser.getValue("Maladie") );
            }
        }

        return true;
    }



    public boolean _NombreEnfant(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("NombreEnfants") != null){
                excelCell.setCellValue((Float) ficheUser.getValue("NombreEnfants"));
            }
        }

        return true;
    }

    public boolean _Email(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Email") != null){
                excelCell.setCellValue((String) ficheUser.getValue("Email") );
            }
        }

        return true;
    }


    public boolean _Identifiant(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Identifiant") != null){
                excelCell.setCellValue((String) ficheUser.getValue("Identifiant") );
            }
        }

        return true;
    }

    public boolean _MotDePasse(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {

        }

        return true;
    }

    public boolean _ConfirmerMotDePasse(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {

        }

        return true;
    }

    public boolean _RIB(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("NCompteBancaire") != null){
                excelCell.setCellValue((String) ficheUser.getValue("NCompteBancaire") );
            }
        }

        return true;
    }

    public boolean _Banque(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Banque") != null){
                excelCell.setCellValue((String) ficheUser.getValue("Banque") );
            }
        }

        return true;
    }

    public boolean _AgenceBancaire(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("AgenceBancaire") != null){
                excelCell.setCellValue((String) ficheUser.getValue("AgenceBancaire") );
            }
        }

        return true;
    }

    public boolean _NumeroFax(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("NumeroFax") != null){
                excelCell.setCellValue((String) ficheUser.getValue("NumeroFax") );
            }
        }

        return true;
    }

    public boolean _NumeroAgence(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("NumeroDAgence") != null){
                excelCell.setCellValue((String) ficheUser.getValue("NumeroDAgence") );
            }
        }

        return true;
    }

    public boolean _Domiciliation(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Domiciliation") != null &&ficheUser.getValue("Domiciliation").equals(true) ){
                excelCell.setCellValue("Oui" );
            }else if(ficheUser.getValue("Domiciliation") != null &&ficheUser.getValue("Domiciliation").equals(false) ){
                excelCell.setCellValue("Non" );
            }
        }

        return true;
    }

    public boolean _MainLever(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("MainLever") != null &&ficheUser.getValue("MainLever").equals(true) ){
                excelCell.setCellValue("Oui" );
            }else if(ficheUser.getValue("MainLever") != null &&ficheUser.getValue("MainLever").equals(false) ){
                excelCell.setCellValue("Non" );
            }
        }

        return true;
    }

    public boolean _DateDEmbauche(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();
            CreationHelper createHelper = row.getSheet().getWorkbook().getCreationHelper();
            cellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
            excelCell.setCellValue(ficheUser.getValue("DateDEmbauche") != null ? (Date) ficheUser.getValue("DateDEmbauche") : null);
            excelCell.setCellStyle(cellStyle);
        }

        return true;
    }

    public boolean _DateDEmbaucheGroupe(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            CellStyle cellStyle = row.getSheet().getWorkbook().createCellStyle();
            CreationHelper createHelper = row.getSheet().getWorkbook().getCreationHelper();
            cellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
            excelCell.setCellValue(ficheUser.getValue("DateDEmbaucheGroupe") != null ? (Date) ficheUser.getValue("DateDEmbaucheGroupe") : null);
            excelCell.setCellStyle(cellStyle);
        }

        return true;
    }

    public boolean _TypeDeContrat(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("ContractType") != null){
                excelCell.setCellValue((String) ficheUser.getValue("ContractType") );
            }
        }

        return true;
    }
    public boolean _Pole(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Pole") != null){
                excelCell.setCellValue((String)((IStorageResource) ficheUser.getValue("Pole")).getValue("sys_Title") );
            }
        }

        return true;
    }

    public boolean _Societe(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Societe") != null){
                excelCell.setCellValue((String)((IStorageResource) ficheUser.getValue("Societe")).getValue("sys_Title") );
            }
        }

        return true;
    }

    public boolean _Groupes(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Groupes") != null){
                ArrayList<IGroup> groupes = (ArrayList<IGroup>) ficheUser.getValue("Groupes");
                List<String> groupesLabels = new ArrayList<>();
                for(IGroup groupe : groupes){
                    //if(groupe.getLabel().equals("CODIR") || groupe.getLabel().equals("Manager")||groupe.getLabel().equals("RH")||groupe.getLabel().equals("ADMIN") || groupe.getLabel().equals("Salarié") || groupe.getLabel().equals("Responsable(s) DEV")){
                        groupesLabels.add(groupe.getLabel());
                    //}
                }
                //List<String> groupesLabels = ((ArrayList<IGroup>)ficheUser.getValue("Groupes")).stream().map(g->g.getLabel()).collect(Collectors.toList());
                if(groupesLabels!=null && !groupesLabels.isEmpty()){
                    excelCell.setCellValue(String.join(";", groupesLabels));

                }

            }
        }

        return true;
    }

    public boolean _Direction(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Direction") != null){
                excelCell.setCellValue(getSysTitle(ficheUser.getValue("Direction")));
            }
        }

        return true;
    }

    public boolean _Division(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Division") != null){
                excelCell.setCellValue(getSysTitle(ficheUser.getValue("Division")) );
            }
        }

        return true;
    }

    public boolean _Departement(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Departement") != null){
                excelCell.setCellValue(getSysTitle(ficheUser.getValue("Departement")) );
            }
        }

        return true;
    }

    public boolean _Activite(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Activite") != null){
                excelCell.setCellValue(getSysTitle(ficheUser.getValue("Activite")) );
            }
        }

        return true;
    }

    public boolean _Service(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Service") != null){
                excelCell.setCellValue(getSysTitle(ficheUser.getValue("Service")) );
            }
        }

        return true;
    }

    public boolean _Metier(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("Metier") != null){
                excelCell.setCellValue(getSysTitle(ficheUser.getValue("Metier")) );
            }
        }

        return true;
    }





    public boolean _SalaireDeBase(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("SalaireDeBase") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("SalaireDeBase")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue() );
            }
        }

        return true;
    }

    public boolean _SalaireBrut(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("SalaireBrutDH") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("SalaireBrutDH")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue() );
            }
        }

        return true;
    }



    public boolean _SalaireBrutImposable(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("SalaireBrutImposableDH") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("SalaireBrutImposableDH")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue() );
            }
        }

        return true;
    }



    public boolean _Remuneration(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("RemunerationDH") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("RemunerationDH")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue() );
            }
        }

        return true;
    }

    public boolean _SalaireNET(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("SalaireNETDH") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("SalaireNETDH")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue() );
            }
        }

        return true;
    }

    public boolean _MontantMensuelNoteFrais(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("MontantMensuelDeNoteDeFrais") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("MontantMensuelDeNoteDeFrais")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue() );
            }
        }

        return true;
    }

    public boolean _NMutuelle(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("NMutuelle") != null){
                excelCell.setCellValue((String) ficheUser.getValue("NMutuelle") );
            }
        }

        return true;
    }

    public boolean _NomMutuelle(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("NomDeLaMutuelle") != null){
                excelCell.setCellValue((String) ficheUser.getValue("NomDeLaMutuelle") );
            }
        }

        return true;
    }

    public boolean _TauxMutuelle(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("TauxDeLaMutuelle") != null){
                // CellStyle style = row.getSheet().getWorkbook().createCellStyle();
                //style.setDataFormat(row.getSheet().getWorkbook().createDataFormat().getFormat("0.00%"));
                // excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("TauxDeLaMutuelle")).doubleValue() / 100).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue());
                //excelCell.setCellStyle(style);
                excelCell.setCellValue((Number) ficheUser.getValue("TauxDeLaMutuelle") + "");

            }
        }

        return true;
    }

    public boolean _NCIMR(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("NCIMR") != null){
                excelCell.setCellValue((String) ficheUser.getValue("NCIMR") );
            }
        }

        return true;
    }

    public boolean _NomRetraite(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("NomRetraite") != null){
                excelCell.setCellValue((String) ficheUser.getValue("NomRetraite") );
            }
        }

        return true;
    }

    public boolean _TauxRetraire(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("TauxDeLaRetraire") != null){
                // CellStyle style = row.getSheet().getWorkbook().createCellStyle();
                //style.setDataFormat(row.getSheet().getWorkbook().createDataFormat().getFormat("0.00%"));
                excelCell.setCellValue((Number) ficheUser.getValue("TauxDeLaRetraire") + "");
                // excelCell.setCellStyle(style);
            }
        }

        return true;
    }

    public boolean _NumeroAttribue(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("NumeroAttribue") != null){
                excelCell.setCellValue(String.valueOf(ficheUser.getValue("NumeroAttribue")) );
            }
        }

        return true;
    }

    public boolean _NomRetraiteComplementaire(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("NomDeLaRetraiteComplementaire") != null){
                excelCell.setCellValue((String) ficheUser.getValue("NomDeLaRetraiteComplementaire") );
            }
        }

        return true;
    }

    public boolean _TauxRetraiteComplementaire(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("TauxDeLaRetraiteComplementaire") != null){
                //CellStyle style = row.getSheet().getWorkbook().createCellStyle();
                //style.setDataFormat(row.getSheet().getWorkbook().createDataFormat().getFormat("0.00%"));
                excelCell.setCellValue((Number) ficheUser.getValue("TauxDeLaRetraiteComplementaire") + "");
                // excelCell.setCellStyle(style);
            }
        }

        return true;
    }

    public boolean _MontantEpargneRetraite(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("MontantEpargneRetraite") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("MontantEpargneRetraite")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue());
            }
        }

        return true;
    }

    public boolean _DroitMensuelle(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("DroitMensuelle") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("DroitMensuelle")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue() );
            }
        }

        return true;
    }

    public boolean _DroitMensuel(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("DroitMensuelle") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("DroitMensuelle")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue() );
            }
        }

        return true;
    }

    public boolean _SoldeAnneeEnCours(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("SoldeAnneeEnCours") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("SoldeAnneeEnCours")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue()  );
            }
        }

        return true;
    }

    public boolean _SoldeAnterieur(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("SoldeAnterieur") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("SoldeAnterieur")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue() );
            }
        }

        return true;
    }

    public boolean _SoldeConges(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("SoldeConges") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("SoldeConges")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue() );
            }
        }

        return true;
    }

    public boolean _CongesPris(IStorageResource ficheUser, Row row, Cell excelCell) {
        if (ficheUser != null) {
            if(ficheUser.getValue("TotalJoursPris") != null){
                excelCell.setCellValue(new BigDecimal(((Number) ficheUser.getValue("TotalJoursPris")).doubleValue()).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue() );
            }
        }

        return true;
    }

}
