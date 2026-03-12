package com.moovapps.ibb.rh.ImporterPie.Document;

import com.axemble.vdoc.directory.exceptions.InvalidPasswordException;
import com.moovapps.ibb.rh.Script.Referentiel.NatureCommentaireExcel;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ImportationDonneePieFromExcel {

    public String matricule = null;
    public String nom = null ;
    public String prenom = null ;
    public String typeDeSalaire = null;
    private List<String> typeDeSalaireList = Arrays.asList("Horaire", "Mensuel");
    public BigDecimal tauxHoraire = null;
    public BigDecimal salaireDeBase = null;
    public BigDecimal salaireBrut = null;
    public BigDecimal salaireNET = null;
    public Double droitMensuel = null;
    public BigDecimal soldeAnneeEnCours = null;
    public BigDecimal soldeAnterieur = null;
    public BigDecimal soldeConges = null ;



    public boolean importer(Row row, Cell cell, String methodName, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) throws NoSuchMethodException {
        try {
            Method method = this.getClass().getMethod(methodName, Row.class, Cell.class, ArrayList.class, HashMap.class);
            System.out.println(methodName);
            return (boolean) method.invoke(this, row, cell, excelCommentaireBloquant, valueToSysNme);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof InvalidPasswordException) {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Mot de passe");
                tmp.put(NatureCommentaireExcel.ANOMALIE.label, "La valeur saisie doit comporter: au moins 8 caractères, au moins 1 caractère en majuscule, au moins 1 chiffre, au moins 1 caractère non alphanumérique (par exemple $ # %)");
                excelCommentaireBloquant.add(tmp);
                return false;
            } else {
                ex.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public boolean _Matricule(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                matricule = String.valueOf(((Number)excelCell.getNumericCellValue()).intValue());
            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                matricule = excelCell.getStringCellValue().trim();
            }
            if (matricule == null || matricule.equals("")) {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Matricule");
                tmp.put(NatureCommentaireExcel.ANOMALIE.label, "Merci d'ajouter le Matricule");
                excelCommentaireBloquant.add(tmp);
                return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Matricule");
            tmp.put(NatureCommentaireExcel.ANOMALIE.label, "Merci d'ajouter le Matricule");
            excelCommentaireBloquant.add(tmp);
            return false;
        }
        valueToSysNme.put("Matricule", matricule);

        return true;
    }

    public boolean _Nom(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                nom = excelCell.getStringCellValue().trim();
            }
            if (nom.equals("")) {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Nom");
                tmp.put(NatureCommentaireExcel.ANOMALIE.label, "Merci d'ajouter le Nom");
                excelCommentaireBloquant.add(tmp);
                return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Nom");
            tmp.put(NatureCommentaireExcel.ANOMALIE.label, "Merci d'ajouter le Nom");
            excelCommentaireBloquant.add(tmp);
            return false;
        }
      //  valueToSysNme.put("LastName", nom);
        //valueToSysNme.put("NomPrenom", (prenom != null && !prenom.equals("")) ? prenom + " " + nom: "");
        return true;
    }

    public boolean _Prenom(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                prenom = excelCell.getStringCellValue().trim();
            }
            if (prenom.equals("")) {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Prénom");
                tmp.put(NatureCommentaireExcel.ANOMALIE.label, "Merci d'ajouter le Prénom ");
                excelCommentaireBloquant.add(tmp);
                return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Prénom");
            tmp.put(NatureCommentaireExcel.ANOMALIE.label, "Merci d'ajouter le Prénom ");
            excelCommentaireBloquant.add(tmp);
            return false;
        }
     //   valueToSysNme.put("FirstName", prenom);
       // valueToSysNme.put("NomPrenom", (nom != null && !nom.equals("")) ? prenom + " " + nom: "");
        return true;
    }

    public boolean _TypeDeSalaire(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                typeDeSalaire = excelCell.getStringCellValue().trim();
            }
            if (!typeDeSalaireList.contains(typeDeSalaire.trim())) {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Type de salaire");
                tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci de saisir un des types suivants (Horaire, Mensuel) ");
                excelCommentaireBloquant.add(tmp);
                //return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Type de salaire");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le type de salaire");
             excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("TypeDeSalaire", typeDeSalaire);

        return true;
    }

    public boolean _TauxHoraire(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                tauxHoraire = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Taux horaire (DH)");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le Taux horaire (DH)");
             excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("TauxHoraire", tauxHoraire);

        return true;
    }

    public boolean _SalaireDeBase(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                salaireDeBase = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Salaire de base (DH)");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Salaire de base (DH)");
             excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SalaireDeBase", salaireDeBase);

        return true;
    }

    public boolean _SalaireBrut(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                salaireBrut = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Salaire Brut (DH)");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Salaire Brut (DH)");
            excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SalaireBrutDH", salaireBrut);

        return true;
    }

    public boolean _SalaireNET(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                salaireNET = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Salaire NET (DH)");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Salaire NET (DH)");
             excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SalaireNETDH", salaireNET);

        return true;
    }

    public boolean _DroitMensuel(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                droitMensuel = Double.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Droit mensuel");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Droit mensuel");
            excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("DroitMensuelle", droitMensuel);
        return true;
    }

    public boolean _SoldeAnneeEnCours(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                soldeAnneeEnCours = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Solde année en cours");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Solde année en cours");
             excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SoldeAnneeEnCours", soldeAnneeEnCours);
        return true;
    }

    public boolean _SoldeAnterieur(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                soldeAnterieur = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Solde antérieur");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Solde antérieur");
            excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SoldeAnterieur", soldeAnterieur);
        return true;
    }
    public boolean _SoldeConges(Row row,Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String,Object> valueToSysNme ){
        if (excelCell != null && excelCell.getCellType() != CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() == CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                soldeConges = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() == CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Solde Conges");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter  Solde Conges");
            excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SoldeConges", soldeConges);
        return true ;
    }

}
