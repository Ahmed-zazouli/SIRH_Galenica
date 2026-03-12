package com.moovapps.ibb.rh.GestionPaieV2.Excel;

import com.axemble.vdoc.sdk.utils.Logger;
import com.moovapps.ibb.rh.GestionPaie.Excel.Exceptions.IncoherantDataException;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ExcelMergerV2 {
	
	protected static final Logger log = Logger.getLogger(ExcelMergerV2.class);
	static List<String> colonnes = Arrays.asList("_matricule", "_civilite", "_nom",
			"_prenom", "_situationFamilale", "_naissance", "_adresse",
			"_absenceJ", "_absenceH", "repriseSalaire", "repriseJ",
			"rappelSalaire", "rappelJ", "_congesPris", "_congesPayes",
			"_congesSpeciaux", "_congesMaladie", "_congesSansSolde", "HS25",
			"HS50", "HS100", "_avanceSalaireMontant",
			"avanceSalaireDureeRemboursement",
			"avanceSalairePossibiliteSuspension",
			"avanceSalaireTempsSuspension", "pretSocieteMontant",
			"pretSocieteDureeRemboursement",
			"pretSocietePossibiliteSuspension", "pretSocieteTempsSuspension",
			
			"pretConsoEkdom1Montant", "pretConsoEkdom1DureeRemboursement",
			"pretConsoEkdom1PossibiliteSuspensionOuiNon",
			"pretConsoEkdom1TempsSuspension",

			"pretConsoEkdom2Montant", "pretConsoEkdom2DureeRemboursement",
			"pretConsoEkdom2PossibiliteSuspensionOuiNon",
			"pretConsoEkdom2TempsSuspension",

			"pretConsoWafalasalafMontant",
			"pretConsoWafalasalafDureeRemboursement",
			"pretConsoWafalasalafPossibiliteSuspensionOuiNon",
			"pretConsoWafalasalafTempsSuspension",
			
			"pretAidMontant", "pretAidDureeRemboursement",
			"pretAidPossibiliteSuspension", "pretAidTempsSuspension",
			"retenueExceptionnelleMontant", "commision", "exceptionnelle",
			"fonction", "ajustement", "bilan", "mission", "bureau", "chantier",
			"formation", "carburant", "Interessement", "logement", "objectif",
			"performance", "poste", "presence", "productivite", "rendement",
			"responsabilite", "rotation", "tuteur", "vacation", "voiture",
			"gratification", "13mois", "14mois", "primeScolariteMontant",
			"nombreEnfantsAAjouter", "primeMariage", "primeDeces",
			"primeNaissance", "primeAchora", "primeAid", "salaireBase",
			"salaireBrut", "salaireNet", "indemnitesTransport",
			"indemnitesRepresentation", "indemnitesCaisse", "indemnitesPanier",
			"indemnitesSalisure", "indemnitesTournee", "indemnitesLait",
			"indemnitesOutillage", "indemnitesDeplacement",
			"indemnitesKilometrique", "_retraire1Taux", "retraite1Commentaire",
			"_retraire2Taux", "retraite2Commentaire",
			"eparneRetraiteRecuranteMontant", "arretEparge",
			"epargeRetraiteExcepMontant", "demissionOuiNon",
			"demissionDateDepart", "preavisPayeOuiNon", "dureePreavis",
			"licenciementOuiNon", "licenciementDateDepart",
			"reglementaireOuAmiable", "indemnitesLicenciement",
			"licenciemebtMontant", "dommagesInteretOuiNon",
			"abandonPosteOuiNon", "abandonPosteDateSortie",
			"miseSommeilContratOuiNon", "miseSommeilContratDate",
			"nouvelleRecrue");

	public static void merge(File complementPaieFile,File donneesMoovappsFile) throws IncoherantDataException, IOException{
		XSSFWorkbook donneesMoovappsWorkbook = null;
		XSSFWorkbook complementPaieWorkbook = null;
		try {
			donneesMoovappsWorkbook = new XSSFWorkbook(new FileInputStream(donneesMoovappsFile));
			complementPaieWorkbook = new XSSFWorkbook(new FileInputStream(complementPaieFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(donneesMoovappsWorkbook != null && complementPaieWorkbook != null){
			XSSFSheet donneesMoovappsFirstSheet = donneesMoovappsWorkbook.getSheetAt(0);
			XSSFSheet elementsVariableACommuniquer = complementPaieWorkbook.getSheetAt(0);
			Iterator<Row> donneesMoovappsFirstSheetIterator = donneesMoovappsFirstSheet.iterator();
			int rowIndex = 0;
			while (donneesMoovappsFirstSheetIterator.hasNext()) {
				Row nextRow = donneesMoovappsFirstSheetIterator.next();
				if(nextRow == null) continue;
				if(rowIndex > 1){
					for (int i = 0; i < colonnes.size(); i++) {
						String colonne = colonnes.get(i);
						int complementIndex = -1;
						if(colonne.startsWith("$")) {
							complementIndex = Integer.valueOf(colonne.split("\\$")[1]); 
							
						}
						Cell donneesMoovappsCell = donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i);
						Cell complementCell = elementsVariableACommuniquer.getRow(rowIndex).getCell(complementIndex == -1 ? i : complementIndex);
						
						if(colonne.startsWith("*")){
							if(donneesMoovappsCell == null || complementCell == null){
								throw new IncoherantDataException();
							}else if(donneesMoovappsCell.getCellType() == CellType.STRING && !elementsVariableACommuniquer.getRow(rowIndex).getCell(i).getStringCellValue().equals(donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getStringCellValue())){
								throw new IncoherantDataException();
							} else if(donneesMoovappsCell.getCellType() == CellType.NUMERIC && !(elementsVariableACommuniquer.getRow(rowIndex).getCell(i).getNumericCellValue() == donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getNumericCellValue())){
								throw new IncoherantDataException();
							} 
						} else if(colonne.startsWith("$") && complementCell != null) {							
							if(complementCell.getCellType() == CellType.STRING){
								donneesMoovappsCell.setCellValue(elementsVariableACommuniquer.getRow(rowIndex).getCell(i).getStringCellValue());
							} else if(complementCell.getCellType() == CellType.NUMERIC){
								donneesMoovappsCell = donneesMoovappsFirstSheet.getRow(rowIndex).createCell(i);
								donneesMoovappsCell.setCellValue(elementsVariableACommuniquer.getRow(rowIndex).getCell(complementIndex).getNumericCellValue());
							}
						}
					}
				}
				rowIndex++;
	        }
		}
				
		try (FileOutputStream outputStream = new FileOutputStream("c://TEST//tmpRemontesPaie.xlsx")) {
			donneesMoovappsWorkbook.write(outputStream);
			File toDelete = new File("c://TEST//tmpRemontesPaie.xlsx");
			FileUtils.copyFile(toDelete, donneesMoovappsFile);
			toDelete.delete();
			toDelete.deleteOnExit();
		}catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
}
