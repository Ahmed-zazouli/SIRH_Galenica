package com.moovapps.ibb.rh.Script.Referentiel.Collaborateurs;

import com.axemble.vdoc.core.helpers.PasswordHelper;
import com.axemble.vdoc.directory.exceptions.InvalidPasswordException;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.moovapps.ibb.rh.Script.Referentiel.GetDataFromReferentielAndAdministration;
import com.moovapps.ibb.rh.Script.Referentiel.NatureCommentaireExcel;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.poi.ss.usermodel.Row ;
import org.apache.poi.ss.usermodel.CellType ;
import org.apache.poi.ss.usermodel.Cell ;



import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.axemble.easysite.services.EasySiteServiceAccess.getSiteByName;

public class ImportationCollaborateursFromExcel {


    public String matricule = null;
    public String actif = null;
    public String civilite = null;
    private List<String> civiliteList = Arrays.asList("Mr", "Mlle", "Mme");
    public String nom = null;
    public String prenom = null;
    public String nomPrenom =null ;
    public String numTelephone = null;
    public Date dateNaissance = null;
    public String cin = null;
    public String numImmatriculationCNSS = null;
    public String ville = null;
    public String adresse = null;
    public String etatCivil = null;
    private List<String> etatCivilList = Arrays.asList("Célibataire", "Marié(e)", "Divorcé(e)", "Veuf(ve)");
    public int numEnfants = 0;
    public String email = null;
    public String identifiant = null;
    public String motDePasse = null;
    public String confirmerMotDePasse = null;
    public String rib = null;
    public String banque = null;
    public String agenceBancaire = null;
    public Date dateDEmbauche = null;
    public String typeDeContrat = null;
    private List<String> typeDeContratList = Arrays.asList("CDI", "CDD", "ANAPEC", "STG", "CSTG");
    public IStorageResource societe = null;
    public ArrayList<IGroup> groupes = new ArrayList<>();
    public ArrayList<IGroup> secretgroupes = new ArrayList<>();
    public IStorageResource direction = null;
    public IStorageResource departement = null;
    public IStorageResource division = null;
    public IStorageResource fonction = null;
    public IStorageResource profil = null;
    public ArrayList<IStorageResource> competences = new ArrayList<>();
    public IStorageResource site = null;
    public IUser superieurHierarchique = null;
    public ArrayList<IStorageResource> suppleants = new ArrayList<>();
    public Date dateSortieDeLEntreprise = null;
    public String motifSortieDeLEntreprise = null;
    public String typeDeSalaire = null;
    private List<String> typeDeSalaireList = Arrays.asList("Horaire", "Mensuel");
    public BigDecimal tauxHoraire = null;
    public BigDecimal salaireDeBase = null;
    public BigDecimal salaireBrut = null;
    public BigDecimal salaireNET = null;
    public String nMutuelle = null;
    public String nomMutuelle = null;
    public Double tauxMutuelle = null;
    public String nCIMR = null;
    public String nomRetraite = null;
    public Double tauxRetraire = null;
    public Integer numeroAttribue = null;
    public String nomRetraiteComplementaire = null;
    public Double tauxRetraiteComplementaire = null;
    public BigDecimal montantEpargneRetraite = null;
    public Double droitMensuel = null;
    public BigDecimal soldeAnneeEnCours = null;
    public BigDecimal soldeAnterieur = null;
    public BigDecimal SoldeConges = null;


    public boolean importer(Row row, Cell cell, String methodName, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) throws NoSuchMethodException {
        try {
            Method method = this.getClass().getMethod(methodName, Row.class,Cell.class, ArrayList.class, HashMap.class);
           // System.out.println(methodName);
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
        if (excelCell != null &&  excelCell.getCellType() != CellType.BLANK) {
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
            // excelCommentaireBloquant.add(tmp);
            return false;
        }
        valueToSysNme.put("Matricule", matricule);

        return true;
    }

    public boolean _Actif(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        Boolean actif = true;
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                this.actif = excelCell.getStringCellValue().trim();
            }
            if (this.actif.trim().equalsIgnoreCase("non")) {
                actif = false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Actif");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "La cellule est obligatoire (oui/non)");
            // excelCommentaireBloquant.add(tmp);
            //return false;
        }
        valueToSysNme.put("Actif", actif);
     //   valueToSysNme.put("Actif" , this.actif == null ? true : actif);

        return true;
    }

    public boolean _Civilite(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                civilite = excelCell.getStringCellValue().trim();
                if(civilite.equalsIgnoreCase("M") || civilite.equalsIgnoreCase("Mr")){
                    civilite = "Mr";
                } else if(civilite.equalsIgnoreCase("F") || civilite.equalsIgnoreCase("Mme")){
                    civilite = "Mme";
                } else if(civilite.equalsIgnoreCase("Mlle")){
                    civilite = "Mlle";
                }
            }
            if (!civiliteList.contains(civilite.trim())) {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Civilite");
                tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter une des civilité suivantes (Monsieur, Madame, Mademoiselle)");
                // excelCommentaireBloquant.add(tmp);
                //return false;
            }

        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Civilite");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter une des civilité suivantes (M., Mlle, Mme)");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("Title", civilite);
        return true;
    }

    public boolean _Nom(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
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
   //     valueToSysNme.put("LastName", nom);
   //     valueToSysNme.put("NomPrenom", (prenom != null && !prenom.equals("")) ? prenom + " " + nom: "");
        return true;
    }

    public boolean _Prenom(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
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
    //    valueToSysNme.put("FirstName", prenom);
        nomPrenom = prenom + " " + nom ;
    //    valueToSysNme.put("NomPrenom", (nom != null && !nom.equals("")) ? prenom + " " + nom: "");
        return true;
    }

    public boolean _NumTelephone(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                numTelephone = excelCell.getStringCellValue().trim();
                if (numTelephone.equals("")) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                    tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Numéro de téléphone");
                    tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le Numéro de téléphone ");
                    // excelCommentaireBloquant.add(tmp);
                    //return false;
                }
            }else {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Numéro de téléphone");
                tmp.put(NatureCommentaireExcel.ANOMALIE.label, "Merci d'ajouter le type de la cellule");
                // excelCommentaireBloquant.add(tmp);
                //return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Numéro de téléphone");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le Numéro de téléphone ");
            // excelCommentaireBloquant.add(tmp);
            //return false;
        }
        valueToSysNme.put("MobilePhoneNumber", numTelephone);
        return true;
    }

    public boolean _DateNaisssance(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                dateNaissance = excelCell.getDateCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING && !excelCell.getStringCellValue().trim().equals("")) {
                cellValue = excelCell.getStringCellValue().trim();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    dateNaissance = sdf.parse((String)cellValue);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(dateNaissance != null){
                Calendar c1 = Calendar.getInstance();
                Date dateActuelle = c1.getTime();
                Calendar c2 = Calendar.getInstance();
                c2.add(Calendar.YEAR, 18);
                Date dateMoin18 = c2.getTime();
                if (dateActuelle.before(dateNaissance) || dateActuelle.equals(dateNaissance) || dateNaissance.equals("")) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                    tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Date de naissance");
                    tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci de vérifier la date de naissance");
                    // excelCommentaireBloquant.add(tmp);
                } else if (dateActuelle.getYear() - dateNaissance.getYear() <= 17) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                    tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Date de naissance");
                    tmp.put(NatureCommentaireExcel.ALERTE.label, "Attention , la date de naissance indique que le collaborateur a moins de 18 ans.");
                    // excelCommentaireBloquant.add(tmp);
                }
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Date de naissance");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter la Date de naissance");
            // excelCommentaireBloquant.add(tmp);
            //return false;
        }
        valueToSysNme.put("Birthday", dateNaissance);
        return true;
    }

    public boolean _CIN(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                cin = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "CIN");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le CIN");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("CIN", cin);
        return true;
    }

    public boolean _NImmatriculationCNSS(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            excelCell.setCellType(CellType.STRING);
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                numImmatriculationCNSS = String.valueOf(excelCell.getNumericCellValue());
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                numImmatriculationCNSS = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "N°Immatriculation CNSS");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le N°Immatriculation CNSS");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("NCNSS", numImmatriculationCNSS);
        return true;
    }

    public boolean _Ville(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                ville = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Ville");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter la Ville");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("Ville", ville);

        return true;
    }

    public boolean _Adresse(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                adresse = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Adresse");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter l'Adresse");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("Address1", adresse);

        return true;
    }

    public boolean _EtatCivil(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                etatCivil = excelCell.getStringCellValue().trim();
                if (!etatCivilList.contains(etatCivil.trim())) {
                    etatCivil = null;
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                    tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Etat Civil");
                    tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci de saisir une des etat civil suivantes (Célibataire, Marié(e), Divorcé(e), Veuf(ve))");
                    excelCommentaireBloquant.add(tmp);
                }
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Etat Civil");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter l'Etat Civil ");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("EtatCivil", etatCivil);

        return true;
    }

    public boolean _NombreEnfants(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                numEnfants = (int) excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        }
       /* else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Nombre d'enfants");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le Nombre d'enfants ");
            excelCommentaireBloquant.add(tmp);
            //return false;
        }*/
        valueToSysNme.put("NombreEnfants", numEnfants);
        return true;
    }

    public boolean _Email(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                email = excelCell.getStringCellValue().trim();
            }
            if (!email.contains("@")) {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Email");
                tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci de vérifier le format d'email ");
                // excelCommentaireBloquant.add(tmp);
                //return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Email");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter l'Email");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("Email", email);

        return true;
    }


    public boolean _Identifiant(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                identifiant = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Identifiant");
            tmp.put(NatureCommentaireExcel.ANOMALIE.label, "Merci d'ajouter l'identifiant ");
            excelCommentaireBloquant.add(tmp);
            return false;
        }
        valueToSysNme.put("Identifiant", identifiant);

        return true;
    }

    public boolean _MotDePasse(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                motDePasse = excelCell.getStringCellValue().trim();
                PasswordHelper.checkPasswordStrength(motDePasse, "");
            }
        }
        valueToSysNme.put("MotDePasse", motDePasse);

        return true;
    }

    public boolean _ConfirmerMotDePasse(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                confirmerMotDePasse = excelCell.getStringCellValue().trim();
            }
            if (!confirmerMotDePasse.equals(motDePasse)) {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Confirmer mot de passe");
                tmp.put(NatureCommentaireExcel.ANOMALIE.label, "Attention , Le mot de passe ne ressemble pas a la confirmation");
                excelCommentaireBloquant.add(tmp);
                return false;
            }

        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Confirmer mot de passe");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter la confirmation du mot de passe ");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        //valueToSysNme.put("ConfirmerMotDePasse", confirmerMotDePasse);

        return true;
    }

    public boolean _RIB(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                rib = excelCell.getStringCellValue().trim();
                rib.replaceAll(" ","");
                if (rib.length() != 24) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                    tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "RIB");
                    tmp.put(NatureCommentaireExcel.ALERTE.label, "Attention , Merci de verifier le RIB (24 chiffres)");
                    // excelCommentaireBloquant.add(tmp);
                    //return false;
                }
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "RIB");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le RIB");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("NCompteBancaire", rib);

        return true;
    }

    public boolean _Banque(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                banque = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Banque");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter la Banque");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("Banque", banque);

        return true;
    }

    public boolean _AgenceBancaire(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                agenceBancaire = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Agence bancaire");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter l' agence bancaire");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("AgenceBancaire", agenceBancaire);

        return true;
    }

    public boolean _DateDEmbauche(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                dateDEmbauche = excelCell.getDateCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                cellValue = excelCell.getStringCellValue().trim();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    dateDEmbauche = sdf.parse((String)cellValue);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Date d'embauche");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter la Date d'embauche");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("DateDEmbauche", dateDEmbauche);

        return true;
    }

    public boolean _TypeDeContrat(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                typeDeContrat = excelCell.getStringCellValue().trim();
                if (!typeDeContratList.contains(typeDeContrat.trim())) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                    tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Type De Contrat");
                    tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci de vérifier le type contrat ");
                    // excelCommentaireBloquant.add(tmp);
                    //return false;
                }
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Type De Contrat");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le type contrat");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("ContractType", typeDeContrat);

        return true;
    }

    public boolean _Societe(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            String societeName = "";
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                societeName = excelCell.getStringCellValue().trim();
            }
            societe = getSocieteByName(societeName);
            if (societe == null) {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Société");
                tmp.put(NatureCommentaireExcel.ANOMALIE.label, "la Société n'existe pas");
                excelCommentaireBloquant.add(tmp);
                return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Société");
            tmp.put(NatureCommentaireExcel.ANOMALIE.label, "Merci d'ajouter la Société");
            excelCommentaireBloquant.add(tmp);
            return false;
        }
        valueToSysNme.put("Societe", societe);
        valueToSysNme.put("Organisation", societe);

        return true;
    }

    public boolean _Groupes(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            String groupesName = "";
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                groupesName = excelCell.getStringCellValue().trim();
            }
            if (groupesName.contains(";")) {
                String[] groupesNames = groupesName.split(";");
                for (String groupeName : groupesNames) {
                    IGroup group = getGroupByName(groupeName, getOrganization("BPOGroupes"));
                    if (group == null) {
                        JSONObject tmp = new JSONObject();
                        tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                        tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Groupes");
                        tmp.put(NatureCommentaireExcel.ANOMALIE.label, "le groupe " + groupeName + " n'existe pas");
                        excelCommentaireBloquant.add(tmp);
                        //return false;
                        return true;
                    } else {
                        groupes.add(group);
                        IGroup sercretGroup = getGroupByName(groupeName, (IOrganization) societe.getValue("Organisation"));
                        if(sercretGroup != null){
                            secretgroupes.add(sercretGroup);
                        }
                    }
                }
            } else {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Groupes");
                tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci de séparer entre les groupes par ;");
                // excelCommentaireBloquant.add(tmp);
                //return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Groupes");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Groupe(s)");
            // excelCommentaireBloquant.add(tmp);
            //return false;
        }
        valueToSysNme.put("Groupes", groupes);
        valueToSysNme.put("SecretGroupes", secretgroupes);
        return true;
    }

    public boolean _Direction(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            String directionName = "";
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                directionName = excelCell.getStringCellValue().trim();
            }
            direction = getDirectionByName(directionName);
            if (direction == null) {
                try {
                    IContext sysContext = Modules.getDirectoryModule().getSysadminContext();
                    IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                    IProject project = Modules.getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
                    ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4, project);
                    IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(sysContext, catalog, "Direction");
                    direction = Modules.getWorkflowModule().createStorageResource(sysContext, definition, "", directionName);
                    direction.setValue("Societe", societe);
                    direction.save(sysContext);
                }catch (Exception e){
                    e.printStackTrace();
                }
//                JSONObject tmp = new JSONObject();
//                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
//                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Direction");
//                tmp.put(NatureCommentaireExcel.ALERTE.label, "la Direction n'existe pas");
//                excelCommentaireBloquant.add(tmp);
                //return false;
            }
//            else{
//                try {
//                    IContext sysContext = Modules.getDirectoryModule().getSysadminContext();
//                    IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
//                    IProject project = Modules.getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
//                    ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4, project);
//                    IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(sysContext, catalog, "Direction");
//                    direction = Modules.getWorkflowModule().createStorageResource(sysContext, definition, "", directionName);
//                    direction.setValue("Societe", societe);
//                    direction.save(sysContext);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Direction");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter la Direction");
            // excelCommentaireBloquant.add(tmp);
            //return false;
        }
        valueToSysNme.put("Direction", direction);

        return true;
    }

    public boolean _Departement(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            String departementName = "";
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                departementName = excelCell.getStringCellValue().trim();
            }
            departement = getDepartementByName(departementName);
            if (departement == null) {
                try {
                    IContext sysContext = Modules.getDirectoryModule().getSysadminContext();
                    IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                    IProject project = Modules.getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
                    ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4, project);
                    IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(sysContext, catalog, "departementcommun");
                    departement = Modules.getWorkflowModule().createStorageResource(sysContext, definition, "", departementName);
                    departement.setValue("Societe", societe);
                    departement.save(sysContext);
                }catch (Exception e){
                    e.printStackTrace();
                }
                /*JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "departement");
                tmp.put(NatureCommentaireExcel.ALERTE.label, "le departement n'existe pas");
                excelCommentaireBloquant.add(tmp);*/
                // return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "département");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le département");
            // excelCommentaireBloquant.add(tmp);
            // return false;
        }
        valueToSysNme.put("Departement", departement);

        return true;
    }

    public boolean _Division(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            String divisionName = "";
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                divisionName = excelCell.getStringCellValue().trim();
            }
            division = getDivisionByName(divisionName);
            if (division == null) {
                try {
                    IContext sysContext = Modules.getDirectoryModule().getSysadminContext();
                    IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                    IProject project = Modules.getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
                    ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4, project);
                    IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(sysContext, catalog, "Division");
                    division = Modules.getWorkflowModule().createStorageResource(sysContext, definition, "", divisionName);
                    division.setValue("Societe", societe);
                    division.save(sysContext);
                }catch (Exception e){
                    e.printStackTrace();
                }
                /*JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Division");
                tmp.put(NatureCommentaireExcel.ALERTE.label, "la Division n'existe pas");
                excelCommentaireBloquant.add(tmp);*/
                //return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Division");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter la Division");
            // excelCommentaireBloquant.add(tmp);
            //return false;
        }
        valueToSysNme.put("Division", division);

        return true;
    }

    public boolean _Fonction(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            String fonctionName = "";
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                fonctionName = excelCell.getStringCellValue().trim();
            }
            fonction = getFontionByName(fonctionName);
            if (fonction == null) {
                try {
                    IContext sysContext = Modules.getDirectoryModule().getSysadminContext();
                    IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                    IProject project = Modules.getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
                    ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4, project);
                    IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(sysContext, catalog, "fonctions");
                    fonction = Modules.getWorkflowModule().createStorageResource(sysContext, definition, "", fonctionName);
                    fonction.setValue("Societe", societe);
                    fonction.save(sysContext);
                }catch (Exception e){
                    e.printStackTrace();
                }
                /*JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "fonction");
                tmp.put(NatureCommentaireExcel.ALERTE.label, "la fonction n'existe pas");
                excelCommentaireBloquant.add(tmp);*/
                // return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "fonction");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter la fonction");
            // excelCommentaireBloquant.add(tmp);
            // return false;
        }
        valueToSysNme.put("Fonction", fonction);

        return true;
    }

    public boolean _Profil(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            String profilName = "";
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                profilName = excelCell.getStringCellValue().trim();
            }
            profil = getProfilByName(profilName);
            if (profil == null) {
                try {
                    IContext sysContext = Modules.getDirectoryModule().getSysadminContext();
                    IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                    IProject project = Modules.getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
                    ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4, project);
                    IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(sysContext, catalog, "Profil");
                    profil = Modules.getWorkflowModule().createStorageResource(sysContext, definition, "", profilName);
                    profil.setValue("Societe", societe);
                    profil.save(sysContext);
                }catch (Exception e){
                    e.printStackTrace();
                }
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Profil");
                tmp.put(NatureCommentaireExcel.ALERTE.label, "le profil n'existe pas");
                excelCommentaireBloquant.add(tmp);
                // return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Profil");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le Profil");
            // excelCommentaireBloquant.add(tmp);
            // return false;
        }
        valueToSysNme.put("Profil", profil);

        return true;
    }

    public boolean _Site(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            String siteName = "";
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                siteName = excelCell.getStringCellValue().trim();
            }
            site = getSiteByName(siteName);
            if (site == null) {
                try {
                    IContext sysContext = Modules.getDirectoryModule().getSysadminContext();
                    IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                    IProject project = Modules.getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
                    ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4, project);
                    IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(sysContext, catalog, "Site");
                    site = Modules.getWorkflowModule().createStorageResource(sysContext, definition, "", siteName);
                    site.setValue("Societe", societe);
                    site.save(sysContext);
                }catch (Exception e){
                    e.printStackTrace();
                }
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Site");
                tmp.put(NatureCommentaireExcel.ALERTE.label, "le site n'existe pas");
                excelCommentaireBloquant.add(tmp);
                // return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Site");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le site");
            // excelCommentaireBloquant.add(tmp);
            // return false;
        }
        valueToSysNme.put("Site", site);

        return true;
    }

    public boolean _SuperieurHierarchique(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;

            String superieurHierarchiqueCIN = "";
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                superieurHierarchiqueCIN = excelCell.getStringCellValue().trim();
            }

            IStorageResource refSuperieurHierarchique = getCollaborateurByFullName(superieurHierarchiqueCIN);
            if (refSuperieurHierarchique != null){
                superieurHierarchique = (IUser) refSuperieurHierarchique.getValue("Salarie");
            }
            if (superieurHierarchique == null) {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Supérieur hiérarchique");
                tmp.put(NatureCommentaireExcel.ALERTE.label, "Supérieur hiérarchique n'existe pas");
                // excelCommentaireBloquant.add(tmp);
                 return true;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Supérieur hiérarchique");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Supérieur hiérarchique");
            // excelCommentaireBloquant.add(tmp);
            // return false;
        }
        valueToSysNme.put("HierarchicalManager", superieurHierarchique);

        return true;
    }

    public boolean _Suppleant(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;

            String suppleantsLogin = "";
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                suppleantsLogin = excelCell.getStringCellValue().trim();
            }

            String[] suppleantsLogins = suppleantsLogin.split(";");
            for (String suppleantLogin : suppleantsLogins) {
                IStorageResource suppeantFiche = getCollaborateurByFullName(suppleantLogin);
                if (suppeantFiche != null){
                    suppleants.add(suppeantFiche);
                }
                if (suppeantFiche == null) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                    tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Suppléant");
                    tmp.put(NatureCommentaireExcel.ALERTE.label, "Suppléant n'existe pas");
                    excelCommentaireBloquant.add(tmp);
//                    return false;
                }
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Suppléant");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le suppléant");
            // excelCommentaireBloquant.add(tmp);
            // return false;
        }
        valueToSysNme.put("Suppleant", suppleants);

        return true;
    }

    public boolean _Competence(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;

            String competencesExcel = "";
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                competencesExcel = excelCell.getStringCellValue().trim();
            }

            String[] competencesNames = competencesExcel.split(";");
            for (String competenceName : competencesNames) {
                IStorageResource competence = getCompetenceByName(competenceName);
                if (competence == null) {
                    try {
                        IContext sysContext = Modules.getDirectoryModule().getSysadminContext();
                        IOrganization organization = Modules.getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
                        IProject project = Modules.getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
                        ICatalog catalog = Modules.getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", 4, project);
                        IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(sysContext, catalog, "Competence");
                        competence = Modules.getWorkflowModule().createStorageResource(sysContext, definition, "", competenceName);
                        competence.setValue("Societe", societe);
                        competence.save(sysContext);
                        competences.add(competence);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    JSONObject tmp = new JSONObject();
                    tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                    tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Compétence");
                    tmp.put(NatureCommentaireExcel.ANOMALIE.label, "la Compétence n'existe pas");
                    excelCommentaireBloquant.add(tmp);
                    //return false;
                }else{
                    competences.add(competence);
                }
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Compétence");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter la Compétence");
            // excelCommentaireBloquant.add(tmp);
            //return false;
        }
        valueToSysNme.put("Competence", competences);

        return true;
    }

    public boolean _DateSortieDeLEntreprise(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                dateSortieDeLEntreprise = excelCell.getDateCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Date sortie de l’entreprise");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter la Date sortie de l’entreprise");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("DateDeSortie", dateSortieDeLEntreprise);

        return true;
    }

    public boolean _MotifSortieDeLEntreprise(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                motifSortieDeLEntreprise = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Motif sortie de l’entreprise");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Motif sortie de l’entreprise ");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("MotifSortie", motifSortieDeLEntreprise);
        return true;
    }

    public boolean _TypeDeSalaire(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                typeDeSalaire = excelCell.getStringCellValue().trim();
            }
            if (!typeDeSalaireList.contains(typeDeSalaire.trim())) {
                JSONObject tmp = new JSONObject();
                tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
                tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Type de salaire");
                tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci de saisir un des types suivants (Horaire, Mensuel) ");
                // excelCommentaireBloquant.add(tmp);
                //return false;
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Type de salaire");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le type de salaire");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("TypeDeSalaire", typeDeSalaire);

        return true;
    }

    public boolean _TauxHoraire(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                tauxHoraire = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Taux horaire (DH)");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter le Taux horaire (DH)");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("TauxHoraire", tauxHoraire);

        return true;
    }

    public boolean _SalaireDeBase(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                salaireDeBase = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Salaire de base (DH)");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Salaire de base (DH)");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SalaireDeBase", salaireDeBase);

        return true;
    }

    public boolean _SalaireBrut(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                salaireBrut = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Salaire Brut (DH)");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Salaire Brut (DH)");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SalaireBrutDH", salaireBrut);

        return true;
    }

    public boolean _SalaireNET(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                salaireNET = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Salaire NET (DH)");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Salaire NET (DH)");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SalaireNETDH", salaireNET);

        return true;
    }

    public boolean _NMutuelle(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                nMutuelle = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "N° Mutuelle");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter N° Mutuelle");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("NMutuelle", nMutuelle);
        return true;
    }

    public boolean _NomMutuelle(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                nomMutuelle = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Nom mutuelle");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Nom mutuelle");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("NomDeLaMutuelle", nomMutuelle);
        return true;
    }

    public boolean _TauxMutuelle(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                tauxMutuelle = Double.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Taux mutuelle (%)");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Taux mutuelle (%)");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("TauxDeLaMutuelle", tauxMutuelle);
        return true;
    }

    public boolean _NCIMR(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                nCIMR = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "N° CIMR");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter N° CIMR");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("NCIMR", nCIMR);
        return true;
    }

    public boolean _NomRetraite(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                nomRetraite = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Nom retraite");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Nom retraite");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("NomRetraite", nomRetraite);
        return true;
    }

    public boolean _TauxRetraire(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                tauxRetraire = Double.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Taux retraire (%)");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Taux retraire (%)");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("TauxDeLaRetraire", tauxRetraire);
        return true;
    }

    public boolean _NumeroAttribue(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                numeroAttribue = (int) excelCell.getNumericCellValue();

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Numéro attribué");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Numéro attribué");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("NumeroAttribue", numeroAttribue);
        return true;
    }

    public boolean _NomRetraiteComplementaire(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
                nomRetraiteComplementaire = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Nom retraite complémentaire");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Nom retraite complémentaire");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("NomDeLaRetraiteComplementaire", nomRetraiteComplementaire);
        return true;
    }

    public boolean _TauxRetraiteComplementaire(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                tauxRetraiteComplementaire = Double.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Taux retraite complémentaire (%)");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Taux retraite complémentaire (%)");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("TauxDeLaRetraiteComplementaire", tauxRetraiteComplementaire);
        return true;
    }

    public boolean _MontantEpargneRetraite(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                montantEpargneRetraite = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Montant épargne retraite");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Montant épargne retraite");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("MontantEpargneRetraite", montantEpargneRetraite);
        return true;
    }

    public boolean _DroitMensuel(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                droitMensuel = Double.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Droit mensuel");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Droit mensuel");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("DroitMensuelle", droitMensuel);
        return true;
    }

    public boolean _SoldeAnneeEnCours(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                soldeAnneeEnCours = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Solde année en cours");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Solde année en cours");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SoldeAnneeEnCours", soldeAnneeEnCours);
        return true;
    }

    public boolean _SoldeAnterieur(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                soldeAnterieur = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Solde antérieur");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Solde antérieur");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SoldeAnterieur", soldeAnterieur);
        return true;
    }

    public boolean _SoldeConges(Row row, Cell excelCell, ArrayList<JSONObject> excelCommentaireBloquant, HashMap<String, Object> valueToSysNme) {
        if (excelCell != null && excelCell.getCellType() !=CellType.BLANK) {
            Object cellValue = null;
            if (excelCell.getCellType() ==CellType.NUMERIC) {
                cellValue = excelCell.getNumericCellValue();
                SoldeConges = BigDecimal.valueOf(excelCell.getNumericCellValue());

            } else if (excelCell.getCellType() ==CellType.STRING) {
                cellValue = excelCell.getStringCellValue().trim();
            }
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("Ligne", "Ligne: " + (row.getRowNum() + 1));
            tmp.put("Cordonnee", "Sheet: " + (row.getSheet().getSheetName()) + " / Cellule: " + "Solde Conges");
            tmp.put(NatureCommentaireExcel.ALERTE.label, "Merci d'ajouter Solde Conges");
            // excelCommentaireBloquant.add(tmp);
            ///return false;
        }
        valueToSysNme.put("SoldeConges", SoldeConges);
        return true;
    }

    protected IStorageResource getSocieteByName(String societeName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
            controller.addEqualsConstraint("sys_Title", societeName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getDirectionByName(String DirectionName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "Direction");
            controller.addEqualsConstraint("Societe", societe);
            controller.addEqualsConstraint("sys_Title", DirectionName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getCompetenceByName(String competenceName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "Competence");
            controller.addEqualsConstraint("Societe", societe);
            controller.addEqualsConstraint("sys_Title", competenceName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getDepartementByName(String departementName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "departementcommun");
            controller.addEqualsConstraint("Societe", societe);
            controller.addEqualsConstraint("sys_Title", departementName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getDivisionByName(String divisionName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "Division");
            controller.addEqualsConstraint("Societe", societe);
            controller.addEqualsConstraint("sys_Title", divisionName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getFontionByName(String fonctionName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "fonctions");
            controller.addEqualsConstraint("Societe", societe);
            controller.addEqualsConstraint("sys_Title", fonctionName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getProfilByName(String profilName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "Profil");
            controller.addEqualsConstraint("Societe", societe);
            controller.addEqualsConstraint("sys_Title", profilName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    protected IStorageResource getSiteByName(String siteName) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "Site");
            controller.addEqualsConstraint("Societe", societe);
            controller.addEqualsConstraint("sys_Title", siteName);
            Collection<IStorageResource> societe = controller.evaluate(definition);
            if (!societe.isEmpty()) {
                return societe.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getCollaborateurByMatricule(String matricule, IStorageResource societe) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("Matricule", matricule);
            controller.addEqualsConstraint("Societe", societe);
            Collection<IStorageResource> collaberateurs = controller.evaluate(definition);
            if (!collaberateurs.isEmpty()) {
                return (IStorageResource) collaberateurs.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IStorageResource getCollaborateurByFullName(String identifiant) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = Modules.getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = Modules.getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IViewController controller = Modules.getWorkflowModule().getViewController(context, IResource.class);
            IResourceDefinition definition = Modules.getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("CIN", identifiant);

            Collection<IStorageResource> collaberateurs = controller.evaluate(definition);
            if (!collaberateurs.isEmpty()) {
                return (IStorageResource) collaberateurs.iterator().next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IGroup getGroupByName(String groupeName, IOrganization organization) {
        try {
            if(groupeName.equals("News readers")){
                groupeName = "NEWS_ANIMATION";
            }else if(groupeName.equals("Workplace FAQ administrators")){
                groupeName = "WP_ADMIN_MIDDLE_OFFICE_FAQ";
            }else if(groupeName.equals("Salarié")){
                groupeName = "Salarie";
            }else if(groupeName.equals("RH")){
                groupeName = "RH";
            }else if(groupeName.equals("Manager")){
                groupeName = "Manager";
            }else if(groupeName.equals("BPO")){
                groupeName = "BPO";
            }
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IGroup group = Modules.getDirectoryModule().getGroup(context, organization, organization.getName() + groupeName);
            if (group == null) {
                group = Modules.getDirectoryModule().getGroup(context, organization, groupeName);
            }
            return group;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IOrganization getOrganization(String nameOrganization) {
        try {
            IContext context = Modules.getDirectoryModule().getSysadminContext();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, nameOrganization);
            return organization;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected IUser getUserByNameAndOrganization(String nameUser, IOrganization organization) {
        //getUserTest(nameUser);
        IContext context = Modules.getDirectoryModule().getSysadminContext();
        Collection<IUser> users = (Collection<IUser>) Modules.getDirectoryModule().getUsers(null, organization);
        for (IUser user : users) {
            if (user.getFullName().trim().equals(nameUser.trim())) {
                return user;
            }
        }
        return null;
    }


}
