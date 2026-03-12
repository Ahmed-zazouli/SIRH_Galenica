package com.moovapps.ibb.rh.GestionPaie.Excel;

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

public class ExcelMerger {
	
	protected static final Logger log = Logger.getLogger(ExcelMerger.class);
	static List<String> colonnes = Arrays.asList("Matricule", "getLastName()",
			"getFirstName()", "$3$M/H", "$4$TauxHoraire",
			"_getUserAbsenceFromUserCongesMap", "_getUserAbsenceFromUserCongesMapByHour", "$5$Reprise",
			"$6$Rappel", "_getUserCongesPayesFromUserCongesMap", "$7$HS25", "$8$HS50",
			"$9$HS100", "_getUserAvanceFromUserMap", "$10$AvanceConges",
			"_getFraisMedicauxFromUserMap", "$11$PrimeBrut", "$12$SalaireNet",
			"$13$Observation", "$14$STC", "$15$STCConges", "$16$STCJTravailles",
			"$17$SituationSalarie", "$18$NouveauSalarie");

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
			XSSFSheet complementPaieFirstSheet = complementPaieWorkbook.getSheetAt(0);
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
						Cell complementCell = complementPaieFirstSheet.getRow(rowIndex).getCell(complementIndex == -1 ? i : complementIndex);
						
						if(colonne.startsWith("*")){
							if(donneesMoovappsCell == null || complementCell == null){
								throw new IncoherantDataException();
							}else if(donneesMoovappsCell.getCellType() == CellType.STRING && !complementPaieFirstSheet.getRow(rowIndex).getCell(i).getStringCellValue().equals(donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getStringCellValue())){
								throw new IncoherantDataException();
							} else if(donneesMoovappsCell.getCellType() == CellType.NUMERIC && !(complementPaieFirstSheet.getRow(rowIndex).getCell(i).getNumericCellValue() == donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getNumericCellValue())){
								throw new IncoherantDataException();
							} 
						} else if(colonne.startsWith("$") && complementCell != null) {							
							if(complementCell.getCellType() == CellType.STRING){
								donneesMoovappsCell.setCellValue(complementPaieFirstSheet.getRow(rowIndex).getCell(i).getStringCellValue());
							} else if(complementCell.getCellType() == CellType.NUMERIC){
								donneesMoovappsCell = donneesMoovappsFirstSheet.getRow(rowIndex).createCell(i);
								donneesMoovappsCell.setCellValue(complementPaieFirstSheet.getRow(rowIndex).getCell(complementIndex).getNumericCellValue());
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
