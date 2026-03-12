package com.moovapps.ibb.rh.GestionPaieV2.Excel;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.modules.IProjectModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.moovapps.ibb.rh.Cummon.DateHelper;
import com.moovapps.ibb.rh.Cummon.WorkingDaysNumberCalculator;
import com.moovapps.ibb.rh.GestionPaie.Excel.Exceptions.IncoherantDataException;
import com.moovapps.ibb.rh.GestionPaieV2.Formulaires.VariablesPaieColonnes;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class MaquetteBuilder {

	JSONObject userDataModel = new JSONObject();

	List<String> colonnes = VariablesPaieColonnes.colonnes;
	List<String> newRecruitsColonnes = Arrays.asList("_matricule", "_civilite", "_nom",
			"_prenom", "cin", "_naissance", "_adresse");
	
	IWorkflowModule workflowModule = null;
	IDirectoryModule directoryModule = null;
	IProjectModule projectModule = null;
	IWorkflowInstance currentDoc = null;
	IContext sysContext = null;
	IUser currentUser = null;
	IStorageResource societe = null;
	int periodeCloture = 1;
	int selectedMonthPaie = -1;
	Date paieFrom = null;
	Date paieTo = null;

	HashMap<IUser, JSONObject> userMap = new HashMap<>();

	public MaquetteBuilder(IWorkflowModule workflowModule, IDirectoryModule directoryModule, IProjectModule projectModule, IWorkflowInstance currentDoc) {
		userDataModel.put("Absence", null);
		userDataModel.put("CN", null);
		userDataModel.put("CE", null);
		userDataModel.put("CM", null);
		userDataModel.put("SS", null);
		userDataModel.put("Avance", null);
		userDataModel.put("DureeRemboursement", null);
		userDataModel.put("FraixMedicaux", null);
		userDataModel.put("Sortie", null);
		
		this.workflowModule = workflowModule;
		this.directoryModule = directoryModule;
		this.projectModule = projectModule;
		this.currentDoc = currentDoc;
		this.sysContext = workflowModule.getSysadminContext();
		this.currentUser = workflowModule.getLoggedOnUser();
		this.selectedMonthPaie = Float.valueOf((String) currentDoc.getValue("Mois")).intValue();

		if (currentUser.getExtendedAttributes().getValue("Societe") != null) {
			this.societe = (IStorageResource) currentUser.getExtendedAttributes().getValue("Societe");
			if (this.societe != null) {
				if (!societe.getValue("ClotureSociete").equals("Mois")) {
					periodeCloture = ((Number) societe.getValue("PeriodeDeCloture")).intValue();
				}
			}
		}
		Calendar paieDateFrom = Calendar.getInstance();
		paieDateFrom.set(Calendar.DATE, 1);
		paieDateFrom.set(Calendar.MONTH, selectedMonthPaie);
		if (periodeCloture != 1) {
			paieDateFrom.add(Calendar.MONTH, -1);
		}
		paieDateFrom.set(Calendar.DATE, periodeCloture);
		Calendar paieDateTo = Calendar.getInstance();
		paieDateTo.setTime(paieDateFrom.getTime());
		paieDateTo.add(Calendar.MONTH, 1);
		paieDateTo.add(Calendar.DATE, -1);
		paieFrom = paieDateFrom.getTime();
		paieTo = paieDateTo.getTime();
	}

	public File finalMerge(File elementVariablesAcommuniquer, File donneesMoovappsFile) throws IncoherantDataException, IOException {
		XSSFWorkbook donneesMoovappsWorkbook = null;
		XSSFWorkbook elementVariableCommuniquerWorkbook = null;

		FileInputStream donneesMoovappsStream = null;
		FileInputStream elementVariableCommuniquerStream = null;

		try {
			donneesMoovappsStream = new FileInputStream(donneesMoovappsFile);
			elementVariableCommuniquerStream = new FileInputStream(elementVariablesAcommuniquer);

			donneesMoovappsWorkbook = new XSSFWorkbook(donneesMoovappsStream);
			elementVariableCommuniquerWorkbook = new XSSFWorkbook(elementVariableCommuniquerStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (donneesMoovappsStream != null) {
				donneesMoovappsStream.close();
			}
			if (elementVariableCommuniquerStream != null) {
				elementVariableCommuniquerStream.close();
			}
		}

		XSSFCellStyle style = elementVariableCommuniquerWorkbook.createCellStyle();
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());

		if (donneesMoovappsWorkbook != null && elementVariableCommuniquerWorkbook != null) {
			XSSFSheet donneesMoovappsFirstSheet = donneesMoovappsWorkbook.getSheetAt(0);
			XSSFSheet elementsVariableACommuniquerSheet = elementVariableCommuniquerWorkbook.getSheetAt(0);

			Iterator<Row> donneesMoovappsFirstSheetIterator = donneesMoovappsFirstSheet.iterator();
			Iterator<Row> elementsVarCommuniquerFirstSheetIterator = elementsVariableACommuniquerSheet.iterator();
			int rowIndex = 0;

			while (elementsVarCommuniquerFirstSheetIterator.hasNext()) {
				Row nextRow = elementsVarCommuniquerFirstSheetIterator.next();
				if (nextRow == null) continue;

				if (rowIndex > 2) {
					for (int i = 0; i < colonnes.size(); i++) {
						String colonne = colonnes.get(i);
						Cell donneesMoovappsCell = null;

						if (donneesMoovappsFirstSheet.getRow(rowIndex) != null && donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i) != null) {
							donneesMoovappsCell = donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i);
						}

						Cell complementCell = elementsVariableACommuniquerSheet.getRow(rowIndex).getCell(i);

						if (colonne.startsWith("_")) {
							try {
								if ((donneesMoovappsCell == null || donneesMoovappsCell.getCellType() == CellType.BLANK) ^
										(complementCell == null || complementCell.getCellType() == CellType.BLANK)) {
									if (complementCell == null) {
										complementCell = elementsVariableACommuniquerSheet.getRow(rowIndex).createCell(i);
									}
									complementCell.setCellStyle(style);

									if (complementCell.getCellComment() == null) {
										addComment(elementVariableCommuniquerWorkbook, complementCell, elementsVariableACommuniquerSheet, donneesMoovappsCell);
									}
								} else if (donneesMoovappsCell != null && complementCell != null) {
									if (donneesMoovappsCell.getCellType() != complementCell.getCellType()) {
										complementCell.setCellStyle(style);

										if (complementCell.getCellComment() == null) {
											addComment(elementVariableCommuniquerWorkbook, complementCell, elementsVariableACommuniquerSheet, donneesMoovappsCell);
										}
									} else if (donneesMoovappsCell.getCellType() == CellType.STRING &&
											!complementCell.getStringCellValue().equals(donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getStringCellValue())) {
										complementCell.setCellStyle(style);

										if (complementCell.getCellComment() == null) {
											addComment(elementVariableCommuniquerWorkbook, complementCell, elementsVariableACommuniquerSheet, donneesMoovappsCell);
										}
									} else if (donneesMoovappsCell.getCellType() == CellType.NUMERIC &&
											(complementCell.getNumericCellValue() != donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getNumericCellValue())) {
										complementCell.setCellStyle(style);

										if (complementCell.getCellComment() == null) {
											addComment(elementVariableCommuniquerWorkbook, complementCell, elementsVariableACommuniquerSheet, donneesMoovappsCell);
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				rowIndex++;
			}
		}

		FileOutputStream outputStream = null;

		try {
			outputStream = new FileOutputStream("c://TEST//tmpRemontesPaie.xlsx");
			elementVariableCommuniquerWorkbook.write(outputStream);
		} catch (Exception e2) {
			e2.printStackTrace();
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}

		FileInputStream tmpInputStream = null;
		FileOutputStream elementVariablesAcommuniquerOutputStream = null;

		try {
			tmpInputStream = new FileInputStream("c://TEST//tmpRemontesPaie.xlsx");
			elementVariablesAcommuniquerOutputStream = new FileOutputStream(elementVariablesAcommuniquer);
			byte[] buffer = new byte[1024];
			int length;

			while ((length = tmpInputStream.read(buffer)) > 0) {
				elementVariablesAcommuniquerOutputStream.write(buffer, 0, length);
			}
		} catch (Exception e3) {
			e3.printStackTrace();
		} finally {
			if (tmpInputStream != null) {
				tmpInputStream.close();
			}
			if (elementVariablesAcommuniquerOutputStream != null) {
				elementVariablesAcommuniquerOutputStream.close();
			}
		}

		return elementVariablesAcommuniquer;
	}
	public File finalMerge2(File elementVariablesAcommuniquer, File donneesMoovappsFile) throws IncoherantDataException, IOException{
		XSSFWorkbook donneesMoovappsWorkbook = null;
		XSSFWorkbook elementVariableCommuniquerWorkbook = null;
		try {
			donneesMoovappsWorkbook = new XSSFWorkbook(new FileInputStream(donneesMoovappsFile));
			elementVariableCommuniquerWorkbook = new XSSFWorkbook(new FileInputStream(elementVariablesAcommuniquer));
		} catch (Exception e) {
			e.printStackTrace();
		}
		CellStyle cs = elementVariableCommuniquerWorkbook.createCellStyle();
		cs.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		
//		ArrayList<Integer> newRecruitsIndexesInElementVariablesACommuniquerExcelFile = new ArrayList<>();
		
		if(donneesMoovappsWorkbook != null && elementVariableCommuniquerWorkbook != null){
			XSSFSheet donneesMoovappsFirstSheet = donneesMoovappsWorkbook.getSheetAt(0);
			XSSFSheet elementsVariableACommuniquerSheet = elementVariableCommuniquerWorkbook.getSheetAt(0);
	        
	        XSSFCellStyle style = elementVariableCommuniquerWorkbook.createCellStyle();
	        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());

			Iterator<Row> donneesMoovappsFirstSheetIterator = donneesMoovappsFirstSheet.iterator();
			Iterator<Row> elementsVarCommuniquerFirstSheetIterator = elementsVariableACommuniquerSheet.iterator();
			int rowIndex = 0;
			while (elementsVarCommuniquerFirstSheetIterator.hasNext()) {
				Row nextRow = elementsVarCommuniquerFirstSheetIterator.next();
				if(nextRow == null) continue;
				if(rowIndex > 2){
					for (int i = 0; i < colonnes.size(); i++) {
						String colonne = colonnes.get(i);
						Cell donneesMoovappsCell = null;
						if(donneesMoovappsFirstSheet.getRow(rowIndex) != null && donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i) != null){
							donneesMoovappsCell = donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i);
						}
						
						Cell complementCell = elementsVariableACommuniquerSheet.getRow(rowIndex).getCell(i);
						if(colonne.startsWith("_")){
							try {
								if((donneesMoovappsCell == null || donneesMoovappsCell.getCellType() == CellType.BLANK) ^ (complementCell == null || complementCell.getCellType() == CellType.BLANK)){
									if(complementCell == null){
										complementCell = elementsVariableACommuniquerSheet.getRow(rowIndex).createCell(i);
									}
									complementCell.setCellStyle(style);
									if(complementCell.getCellComment() == null){										
										addComment(elementVariableCommuniquerWorkbook, complementCell, elementsVariableACommuniquerSheet, donneesMoovappsCell);
									}
								} else if(donneesMoovappsCell != null && complementCell != null){
									 if(donneesMoovappsCell.getCellType() != complementCell.getCellType()){
											complementCell.setCellStyle(style);
											if(complementCell.getCellComment() == null){										
												addComment(elementVariableCommuniquerWorkbook, complementCell, elementsVariableACommuniquerSheet, donneesMoovappsCell);
											}
										} else if (donneesMoovappsCell.getCellType() == CellType.STRING && !complementCell.getStringCellValue().equals(donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getStringCellValue())) {
											complementCell.setCellStyle(style);
											if(complementCell.getCellComment() == null){										
												addComment(elementVariableCommuniquerWorkbook, complementCell, elementsVariableACommuniquerSheet, donneesMoovappsCell);
											}
										} else if (donneesMoovappsCell.getCellType() == CellType.NUMERIC && (complementCell.getNumericCellValue() != donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getNumericCellValue())) {
											complementCell.setCellStyle(style);
											if(complementCell.getCellComment() == null){										
												addComment(elementVariableCommuniquerWorkbook, complementCell, elementsVariableACommuniquerSheet, donneesMoovappsCell);
											}
										}	
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
//						if(colonne.equals("_nouvelleRecrueOuiNon")){
//							if(complementCell != null && complementCell.getCellType() != CellType.BLANK && complementCell.getStringCellValue().equals("Oui")){
//								if(!(boolean)currentDoc.getValue("isThereAreNewRecruits")){									
//									currentDoc.setValue("isThereAreNewRecruits", true);
//									setMaquetteNewRecruits();
//								}
//								newRecruitsIndexesInElementVariablesACommuniquerExcelFile.add(rowIndex);
//							}
//						}
					}
				}
				rowIndex++;
	        }
			
//			if(newRecruitsIndexesInElementVariablesACommuniquerExcelFile.size() > 0 ){
//				File tmp = mergeNewRecruits(newRecruitsIndexesInElementVariablesACommuniquerExcelFile,elementVariableCommuniquerWorkbook);
//				File toDeleteFile = new File("C:\\TEST\\Nouvelles recrues "+(String)currentDoc.getText("Mois") + "-" + (String)currentDoc.getValue("Annee") + ".xlsx");
//				try {
//					FileUtils.copyFile(tmp, toDeleteFile);
//					IAttachment tmpJournalPaieAttachment = directoryModule.createAttachment(workflowModule.getSysadminContext(), toDeleteFile);
//					ArrayList<IAttachment> tmpNouvellesRecruesList = new ArrayList<>();
//					tmpNouvellesRecruesList.add(tmpJournalPaieAttachment);
//					toDeleteFile.delete();
//					toDeleteFile.deleteOnExit();
//					tmp.delete();
//					tmp.deleteOnExit();
//					currentDoc.setValue("NouvellesRecrues", tmpNouvellesRecruesList);
//					currentDoc.save(workflowModule.getLoggedOnUserContext());
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}else{
//				currentDoc.setValue("NouvellesRecrues", new ArrayList<IAttachment>());
//			}
		}
				
		try (FileOutputStream outputStream = new FileOutputStream("c://TEST//tmpRemontesPaie.xlsx")) {
			elementVariableCommuniquerWorkbook.write(outputStream);

			File toDelete = new File("c://TEST//tmpRemontesPaie.xlsx");
			FileUtils.copyFile(toDelete, elementVariablesAcommuniquer);
			toDelete.delete();
			toDelete.deleteOnExit();
		}catch (Exception e2) {
			e2.printStackTrace();
		}
		return elementVariablesAcommuniquer;
	}
	
	private void setMaquetteNewRecruits() {
		File maquetteNewRecruits = createFileFromFileCenterInLocalDrive("Maquettes/Nouvelles recrues.xlsx", "Nouvelles recrues.xlsx");
		ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
		IAttachment tmpJournalPaieAttachment = directoryModule.createAttachment(workflowModule.getSysadminContext(), maquetteNewRecruits);
		tmpAttachementCollection.add(tmpJournalPaieAttachment);
		currentDoc.setValue("NouvellesRecrues", tmpAttachementCollection);
		maquetteNewRecruits.delete();
		maquetteNewRecruits.deleteOnExit();
	}
	
	private File createFileFromFileCenterInLocalDrive(String filePath, String fileName) {
		try {
			ILibraryModule libraryModule = Modules.getLibraryModule();
			IContext context = workflowModule.getSysadminContext();
			IOrganization organization = directoryModule.getOrganization(context, "DefaultOrganization");
			ILibrary library = libraryModule.getLibrary(context, organization, "Paie");
			if (library != null) {
				IFile file = libraryModule.getFileByPath(context, library, filePath);
				if (file != null) {
					String[] filePathDivided = filePath.split("/");
					IAttachment attachment = libraryModule.getAttachment(file, filePathDivided[filePathDivided.length - 1]);
					File tmpFile = new File("c://TEST//" + fileName);
					FileUtils.writeByteArrayToFile(tmpFile, attachment.getContent());
					return tmpFile;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void addComment(Workbook workbook, Cell cell, XSSFSheet sheet, Cell donneeMoovappsCell) {
        CreationHelper factory = workbook.getCreationHelper();
        //get an existing cell or create it otherwise:

        ClientAnchor anchor = factory.createClientAnchor();
        //i found it useful to show the comment box at the bottom right corner
        anchor.setCol1(cell.getColumnIndex() + 1); //the box of the comment starts at this given column...
        anchor.setCol2(cell.getColumnIndex() + 3); //...and ends at that given column
        anchor.setRow1(cell.getRowIndex() + 1); //one row below the cell...
        anchor.setRow2(cell.getRowIndex() + 5); //...and 4 rows high

        Drawing drawing = sheet.createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        //set the comment text and author
        String commentText = "La valeur Moovapps est: ";
        if(donneeMoovappsCell != null && donneeMoovappsCell.getCellType() != CellType.BLANK){
        	if(donneeMoovappsCell.getCellType() == CellType.NUMERIC){
        		commentText += String.valueOf(donneeMoovappsCell.getNumericCellValue()) ;
        	}else if(donneeMoovappsCell.getCellType() == CellType.STRING){
        		commentText += donneeMoovappsCell.getStringCellValue();
        	}
        } else {
        	commentText += "N/D";
        }
        
        comment.setString(factory.createRichTextString(commentText));
        comment.setAuthor("Moovapps");
        
        cell.removeCellComment();
        cell.setCellComment(comment);
    }
	
	public File merge2(File elementVariablesAcommuniquer, File donneesMoovappsFile) throws IncoherantDataException, IOException{
		XSSFWorkbook donneesMoovappsWorkbook = null;
		XSSFWorkbook elementVariableCommuniquerWorkbook = null;
		try {
			donneesMoovappsWorkbook = new XSSFWorkbook(new FileInputStream(donneesMoovappsFile));
			elementVariableCommuniquerWorkbook = new XSSFWorkbook(new FileInputStream(elementVariablesAcommuniquer));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(donneesMoovappsWorkbook != null && elementVariableCommuniquerWorkbook != null){
			XSSFSheet donneesMoovappsFirstSheet = donneesMoovappsWorkbook.getSheetAt(0);
			XSSFSheet elementsVariableACommuniquerSheet = elementVariableCommuniquerWorkbook.getSheetAt(0);
			Iterator<Row> donneesMoovappsFirstSheetIterator = donneesMoovappsFirstSheet.iterator();
			Iterator<Row> elementsVarCommuniquerFirstSheetIterator = elementsVariableACommuniquerSheet.iterator();
			int rowIndex = 0;
			while (elementsVarCommuniquerFirstSheetIterator.hasNext()) {
				Row nextRow = elementsVarCommuniquerFirstSheetIterator.next();
				if(nextRow == null) continue;
				if(rowIndex > 2){
					for (int i = 0; i < colonnes.size(); i++) {
						String colonne = colonnes.get(i);
						Cell donneesMoovappsCell = null;
						if(donneesMoovappsFirstSheet.getRow(rowIndex) != null && donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i) != null){
							donneesMoovappsCell = donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i);
						}
						Cell complementCell = elementsVariableACommuniquerSheet.getRow(rowIndex).getCell(i);
						
						if(colonne.startsWith("_")){
							if(complementCell != null && complementCell.getCellComment() != null){
								complementCell.removeCellComment();
								complementCell.setCellComment(null);
							}
							if(donneesMoovappsCell != null){								
								if(donneesMoovappsCell.getCellType()==CellType.BLANK){
									elementsVariableACommuniquerSheet.getRow(rowIndex).createCell(i);
								}else{								
									if(donneesMoovappsCell.getCellType() == CellType.STRING){
										complementCell.setCellValue(donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getStringCellValue());
										complementCell.setCellStyle(null);
									} else if(donneesMoovappsCell.getCellType() == CellType.NUMERIC){
										complementCell.setCellValue(donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getNumericCellValue());
										complementCell.setCellStyle(null);
									}
								}
							}
						}
					}
				}
				rowIndex++;
	        }
		}
				
		try (FileOutputStream outputStream = new FileOutputStream("c://TEST//tmpRemontesPaie.xlsx")) {
			elementVariableCommuniquerWorkbook.write(outputStream);
			File toDelete = new File("c://TEST//tmpRemontesPaie.xlsx");
			FileUtils.copyFile(toDelete, elementVariablesAcommuniquer);
			toDelete.delete();
			toDelete.deleteOnExit();
		}catch (Exception e2) {
			e2.printStackTrace();
		}
		return elementVariablesAcommuniquer;
	}
	public File merge(File elementVariablesAcommuniquer, File donneesMoovappsFile) throws IncoherantDataException, IOException {
		XSSFWorkbook donneesMoovappsWorkbook = null;
		XSSFWorkbook elementVariableCommuniquerWorkbook = null;
		FileInputStream donneesMoovappsStream = null;
		FileInputStream elementVariableCommuniquerStream = null;

		try {
			donneesMoovappsStream = new FileInputStream(donneesMoovappsFile);
			elementVariableCommuniquerStream = new FileInputStream(elementVariablesAcommuniquer);

			donneesMoovappsWorkbook = new XSSFWorkbook(donneesMoovappsStream);
			elementVariableCommuniquerWorkbook = new XSSFWorkbook(elementVariableCommuniquerStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (donneesMoovappsStream != null) {
				donneesMoovappsStream.close();
			}
			if (elementVariableCommuniquerStream != null) {
				elementVariableCommuniquerStream.close();
			}
		}

		if (donneesMoovappsWorkbook != null && elementVariableCommuniquerWorkbook != null) {
			XSSFSheet donneesMoovappsFirstSheet = donneesMoovappsWorkbook.getSheetAt(0);
			XSSFSheet elementsVariableACommuniquerSheet = elementVariableCommuniquerWorkbook.getSheetAt(0);

			Iterator<Row> donneesMoovappsFirstSheetIterator = donneesMoovappsFirstSheet.iterator();
			Iterator<Row> elementsVarCommuniquerFirstSheetIterator = elementsVariableACommuniquerSheet.iterator();
			int rowIndex = 0;

			while (elementsVarCommuniquerFirstSheetIterator.hasNext()) {
				Row nextRow = elementsVarCommuniquerFirstSheetIterator.next();
				if (nextRow == null) continue;

				if (rowIndex > 2) {
					for (int i = 0; i < colonnes.size(); i++) {
						String colonne = colonnes.get(i);
						Cell donneesMoovappsCell = null;

						if (donneesMoovappsFirstSheet.getRow(rowIndex) != null && donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i) != null) {
							donneesMoovappsCell = donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i);
						}

						Cell complementCell = elementsVariableACommuniquerSheet.getRow(rowIndex).getCell(i);

						if (colonne.startsWith("_")) {
							if (complementCell != null && complementCell.getCellComment() != null) {
								complementCell.removeCellComment();
								complementCell.setCellComment(null);
							}
							if (donneesMoovappsCell != null) {
								if (donneesMoovappsCell.getCellType() == CellType.BLANK) {
									elementsVariableACommuniquerSheet.getRow(rowIndex).createCell(i);
								} else {
									if (donneesMoovappsCell.getCellType() == CellType.STRING) {
										complementCell.setCellValue(donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getStringCellValue());
										complementCell.setCellStyle(null);
									} else if (donneesMoovappsCell.getCellType() == CellType.NUMERIC) {
										complementCell.setCellValue(donneesMoovappsFirstSheet.getRow(rowIndex).getCell(i).getNumericCellValue());
										complementCell.setCellStyle(null);
									}
								}
							}
						}
					}
				}
				rowIndex++;
			}
		}

		FileOutputStream outputStream = null;

		try {
			outputStream = new FileOutputStream("c://TEST//tmpRemontesPaie.xlsx");
			elementVariableCommuniquerWorkbook.write(outputStream);
			File toDelete = new File("c://TEST//tmpRemontesPaie.xlsx");
			FileUtils.copyFile(toDelete, elementVariablesAcommuniquer);
			toDelete.delete();
			toDelete.deleteOnExit();
		} catch (Exception e2) {
			e2.printStackTrace();
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}

		return elementVariablesAcommuniquer;
	}



	public void buildMaquette(File maquetteFile) throws IOException {
		try (FileInputStream fileInputStream = new FileInputStream(maquetteFile)) {
			XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
			XSSFSheet firstSheet = workbook.getSheetAt(0);

			Collection<IUser> myUsers = getUsersWithTheSameSociete(sysContext, directoryModule, currentUser);

			BuildCongesByUser();
			BuildCongesAnnuelsByUser();
			buildAvanceByUser();
			buildSortieByUser();

			int line = 3;
			for (IUser user : myUsers) {
				XSSFRow row = firstSheet.createRow(line);
				for (int j = 0; j < colonnes.size(); j++) {
					if (colonnes.get(j).startsWith("_")) {
						colonnes.get(j);
						try {
							Method method = this.getClass().getMethod(colonnes.get(j), IUser.class);
							Object value = method.invoke(this, user);
							if (value instanceof Number) {
								row.createCell(j).setCellValue(((Number) value).doubleValue());
							} else {
								row.createCell(j).setCellValue((String)value);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				line++;
			}

			try (FileOutputStream outputStream = new FileOutputStream("c://TEST//tmpMaquette.xlsx")) {
				workbook.write(outputStream);
			}
			workbook.close();
			fileInputStream.close();
			// Copy the generated file to the original file location
			File toDelete = new File("c://TEST//tmpMaquette.xlsx");

			FileUtils.copyFile(toDelete, maquetteFile);
			toDelete.delete();
			toDelete.deleteOnExit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	{
//	private void setReglementaireOuAmiable(XSSFSheet sheet, int rowIndex, int cellIndex) {
//		DataValidationHelper helper = sheet.getDataValidationHelper();
//		DataValidationConstraint constraint = helper.createExplicitListConstraint(new String[] { "Réglementaire","Amiable" });
//		CellRangeAddressList regions = new CellRangeAddressList(rowIndex,rowIndex, cellIndex, cellIndex);
//		DataValidation dataValidation = helper.createValidation(constraint,regions);
//		if (dataValidation instanceof XSSFDataValidation) {
//			dataValidation.setSuppressDropDownArrow(true);
//			dataValidation.setShowErrorBox(true);
//		} else {
//			dataValidation.setSuppressDropDownArrow(false);
//		}
//
//		sheet.addValidationData(dataValidation);
//	}
//	
//	private void setOuiNonCell(XSSFSheet sheet, int rowIndex, int cellIndex) {
//		DataValidationHelper helper = sheet.getDataValidationHelper();
//		DataValidationConstraint constraint = helper.createExplicitListConstraint(new String[] { "Non", "Oui" });
//		CellRangeAddressList regions = new CellRangeAddressList(rowIndex,rowIndex, cellIndex, cellIndex);
//		DataValidation dataValidation = helper.createValidation(constraint,regions);
//		if (dataValidation instanceof XSSFDataValidation) {
//			dataValidation.setSuppressDropDownArrow(true);
//			dataValidation.setShowErrorBox(true);
//		} else {
//			dataValidation.setSuppressDropDownArrow(false);
//		}
//
//		sheet.addValidationData(dataValidation);
//	}
	}

	private File mergeNewRecruits(ArrayList<Integer> indexes, XSSFWorkbook elementVariableCommuniquerWorkbook) {
		XSSFWorkbook newRecruitsWorkbook = null;
		
		File newRecruitsFile = null;
		
		try {
			IAttachment newRecruitsAttachment = ((ArrayList<IAttachment>)currentDoc.getValue("NouvellesRecrues")).get(0);
			newRecruitsFile  = new File("c://TEST//tmpRecruitsFile.xlsx");
			FileUtils.writeByteArrayToFile(newRecruitsFile, newRecruitsAttachment.getContent());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			newRecruitsWorkbook = new XSSFWorkbook(new FileInputStream(newRecruitsFile));
			
			if(newRecruitsWorkbook != null && elementVariableCommuniquerWorkbook != null){
				XSSFSheet newRecruitsFirstSheet = newRecruitsWorkbook.getSheetAt(0);
				XSSFSheet elementsVariableACommuniquerSheet = elementVariableCommuniquerWorkbook.getSheetAt(0);
				
				int indexOfFirstRecruits = 3;
				for (int i = 0; i < indexes.size(); i++) {
					XSSFRow elementAcommuniquerRow = elementsVariableACommuniquerSheet.getRow(indexes.get(i));
					XSSFRow newRecruitsRow = newRecruitsFirstSheet.createRow(indexOfFirstRecruits + i);
					for (int j = 0; j < newRecruitsColonnes.size(); j++) {
						String test = newRecruitsColonnes.get(j);
						int cellIndexInElementVariablesColonnes = colonnes.indexOf(test);
						XSSFCell elementVariablesACommuniquer = null;
						if(cellIndexInElementVariablesColonnes != -1){
							elementVariablesACommuniquer = elementAcommuniquerRow.getCell(cellIndexInElementVariablesColonnes);
						}
						if(elementVariablesACommuniquer != null){
							if(elementVariablesACommuniquer.getCellType() == CellType.STRING){
								newRecruitsRow.createCell(j).setCellValue(elementVariablesACommuniquer.getStringCellValue());
							} else if(elementVariablesACommuniquer.getCellType() == CellType.NUMERIC){
								newRecruitsRow.createCell(j).setCellValue(elementVariablesACommuniquer.getNumericCellValue());
							}
							
						}
					}
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try (FileOutputStream outputStream = new FileOutputStream("c://TEST//tmpRecruits.xlsx")) {
		newRecruitsWorkbook.write(outputStream);
			File toDelete = new File("c://TEST//tmpRecruits.xlsx");
			FileUtils.copyFile(toDelete, newRecruitsFile);
			toDelete.delete();
			toDelete.deleteOnExit();
		}catch (Exception e) {
			e.printStackTrace();		
		}
		return newRecruitsFile;
	}
	
	public void buildDonneesMoovapps(File donneesMoovappsFile) throws FileNotFoundException, IOException {

		Collection<IUser> myUsers = getUsersWithTheSameSociete(sysContext, directoryModule, currentUser);
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(donneesMoovappsFile));
		XSSFSheet firstSheet = workbook.getSheetAt(0);

		BuildCongesByUser();
		BuildCongesAnnuelsByUser();
		buildAvanceByUser();
		buildFraisMedicauxbyUser();

		int line = 3;
		for (IUser user : myUsers) {
			XSSFRow row = firstSheet.createRow(line);
			for (int j = 0; j < colonnes.size(); j++) {
				Object value = null;
				String methodName = colonnes.get(j);
				
				if (colonnes.get(j).startsWith("$")) {
					continue;
				} else if (colonnes.get(j).startsWith("_")) {
					try {
						Method method = this.getClass().getMethod(methodName, IUser.class);
						value = method.invoke(this, user);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (colonnes.get(j).endsWith("()")) {
					try {
						Method method = user.getClass().getMethod(methodName);
						value = method.invoke(user);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					value = user.getExtendedAttributes().getValue(methodName);
				}

				try {
					row.createCell(j).setCellValue(((Number) value).doubleValue());
				} catch (Exception e) {
					row.createCell(j).setCellValue((String) value);
				}

			}
			line++;
		}
		try (FileOutputStream outputStream = new FileOutputStream("c://TEST//tmpDonneesMoovapps.xlsx")) {
			workbook.write(outputStream);
			File toDelete = new File("c://TEST//tmpDonneesMoovapps.xlsx");
			FileUtils.copyFile(toDelete, donneesMoovappsFile);
			toDelete.delete();
			toDelete.deleteOnExit();
		}
	}



	public String _matricule(IUser user){
		return user.getExtendedAttributes().getValue("Matricule")!=null? (String)user.getExtendedAttributes().getValue("Matricule"):"";
	}
	
	public String _civilite(IUser user){
		String civilite = "";
		if(user.getTitle() == null) return "";
		switch (user.getTitle()) {
		case "Mr":
			civilite = "M";
			break;
		case "Mlle":
			civilite = "Mlle";
			break;
		case "Mme":
			civilite = "Mme";
			break;
		}
		return civilite;
	}
	
	public String _nom(IUser user){
		return user.getLastName();
	}
	
	public String _prenom(IUser user){
		return user.getFirstName();
	}
	
	public String _situationFamilale(IUser user){
		return user.getExtendedAttributes().getText("EtatCivil")!=null? (String)user.getExtendedAttributes().getText("EtatCivil"):"";
	}
	
	public String _naissance(IUser user){
		SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
		String birthday = "";
		if(user.getBirthday() != null){			
			birthday = simpleFormat.format(user.getBirthday());
		}
		return birthday;
	}
	
	public String _adresse(IUser user){
		return user.getAddress1()!=null?user.getAddress1():"";
	}
	
	public Float _absenceJ(IUser user){
		if (userMap.get(user) != null) {
			return userMap.get(user).get("Absence") != null ? ((Number)userMap.get(user).get("Absence")).floatValue() : null;
		}
		return null;
	}

	public Float _sortiesH(IUser user){
		if (userMap.get(user) != null) {
			return userMap.get(user).get("Sortie") != null ? (((Number)userMap.get(user).get("Sortie")).floatValue()-(((Number)userMap.get(user).get("Sortie")).floatValue()%15))/60 : null;
		}
		return null;
	}
	
	public Float _absenceH(IUser user){
		if (userMap.get(user) != null && userMap.get(user).get("Absence") != null) {
			return ((Number) userMap.get(user).get("Absence")).floatValue()
					* (((Number) societe.getValue("durationWorkHours"))
							.floatValue() / 60);
		}
		return null;
	}
	public Float _congeAnnuel(IUser user){
		return  userMap.get(user).get("Conge Annuel") != null ?(Float) userMap.get(user).get("Conge Annuel") : null;
	}
	public Float _congesPris(IUser user){
		if (userMap.get(user) != null) {
			if (userMap.get(user).get("CN") == null && userMap.get(user).get("Conge Annuel") == null  ){
				return null ;
			}
			float totalCongeris = 0;
			if (userMap.get(user).get("CN") != null ){
                totalCongeris = (Float) userMap.get(user).get("CN") ;
			}
			if (userMap.get(user).get("Conge Annuel") != null ){
               totalCongeris += (Float) userMap.get(user).get("Conge Annuel") ;
			}
			return totalCongeris;
		}
		return null;
		
	}

	
	public Float _congesPayes(IUser user){
		if (userMap.get(user) != null) {
			return userMap.get(user).get("CN") != null ? (Float)userMap.get(user).get("CN") : null;
		}
		return null;
	}
	
	public Float _congesSpeciaux(IUser user){
		if (userMap.get(user) != null) {
			return userMap.get(user).get("CE") != null ? (Float)userMap.get(user).get("CE") : null;
		}
		return null;
	}
	
	public Float _congesMaladie(IUser user){
		if (userMap.get(user) != null) {
			return userMap.get(user).get("CM") != null ? (Float)userMap.get(user).get("CM") : null;
		}
		return null;
	}
	
	public Float _congesSansSolde(IUser user){
		if (userMap.get(user) != null) {
			return userMap.get(user).get("SS") != null ? (Float)userMap.get(user).get("SS") : null;
		}
		return null;
	}
	private Collection<ILinkedResource> getAllAvancesOfTheMonthInTheSameSociete(IUser user) {
		Collection<ILinkedResource> collection = Collections.emptyList();
		try
		{
			SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
			IContext sysContext = workflowModule.getSysadminContext();
			IUser connectedUser = workflowModule.getLoggedOnUser();
			IViewController controller = workflowModule.getViewController(sysContext, IResource.class);
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(sysContext, "GestionDesAvances", organization);
			ICatalog catalog = workflowModule.getCatalog(sysContext, "GestionDesAvances", project);
			IResourceDefinition definition = workflowModule.getResourceDefinition(sysContext, catalog, "GestionAvances");
			controller.addEqualsConstraint("Salarie", user);
			controller.addEqualsConstraint("Statut", "Validé");
			Calendar c = Calendar.getInstance();
			c.setTime(new Date(paieFrom.getTime()));
			c.set(Calendar.DATE, 1);

			controller.addGreaterOrEqualConstraint("Date", simpleFormat.parse(simpleFormat.format(c.getTime())));
			collection = controller.evaluate(definition);

		} catch (Exception e) {
			e.printStackTrace();

		}
		return collection;
	}
	public Float _avanceSalaireMontant(IUser user) {
		SimpleDateFormat simpleFormat = new SimpleDateFormat("MM/yyyy");
		Collection<ILinkedResource> avances = getAllAvancesOfTheMonthInTheSameSociete(user);
		Float montantARembourserCeMois = 0f;
		Float resteARembourser = 0f;
		Float moisRestant = 0f;
		Set<String> months = new HashSet<>();
		for (ILinkedResource avance: avances) {
			Date dateRemboursement = (Date) avance.getValue("Date");
			months.add(simpleFormat.format(dateRemboursement));
			if(simpleFormat.format(dateRemboursement).equals(simpleFormat.format(paieFrom))){
				montantARembourserCeMois += ((Number)avance.getValue("Montant")).floatValue();
			} else {
				resteARembourser += ((Number)avance.getValue("Montant")).floatValue();
			}
		}
		JSONObject tmp = (JSONObject) userDataModel.clone();
		if (userMap.containsKey(user)) {
			tmp = userMap.get(user);
		}
		tmp.put("Avance", montantARembourserCeMois);
		tmp.put("ResteARembourser", resteARembourser);
		tmp.put("DureeRemboursement", months.size());
		userMap.put(user, tmp);
		return montantARembourserCeMois;
	}

	public Integer _avanceSalaireDureeRemboursement(IUser user) {
		if (userMap.get(user) != null) {
			int dureeAvanceSalaire = userMap.get(user).get("DureeRemboursement") != null ? ((Number) userMap.get(user).get("DureeRemboursement")).intValue() : 0;
			userMap.get(user).put("DureeRemboursement", dureeAvanceSalaire);
			return dureeAvanceSalaire;
		}
		return null;
	}

	public Integer _remboursementChaqueMois(IUser user) {
		if (userMap.get(user) != null) {
			return ((Number)userMap.get(user).get("ResteARembourser")).intValue();
		}
		return null;
	}
	
	public Float _retraire1Taux(IUser user) {
		return user.getExtendedAttributes().getValue("TauxDeLaRetraire") != null ? ((Number) user
				.getExtendedAttributes().getValue("TauxDeLaRetraire"))
				.floatValue() : null;
	}
	
	public Float _retraire2Taux(IUser user) {
		return user.getExtendedAttributes().getValue("TauxDeLaRetraiteComplementaire") != null ? ((Number) user
				.getExtendedAttributes().getValue("TauxDeLaRetraiteComplementaire"))
				.floatValue() : null;
	}
	
	public String _nouvelleRecrueOuiNon(IUser user) {
		if(user.getExtendedAttributes().getValue("DateDEmbauche") != null){
			Date userDateEmbauche = (Date)user.getExtendedAttributes().getValue("DateDEmbauche");
			if(DateHelper.isDateEqualsDate(userDateEmbauche, paieFrom) || DateHelper.isDateEqualsDate(userDateEmbauche, paieTo) || (DateHelper.isDateAfterDate(userDateEmbauche, paieFrom) && DateHelper.isDateBeforeDate(userDateEmbauche, paieTo))){
				return "Oui";
			}
		}
		return "Non";
	}
	
	
	
	
	
	public Object getUserCongesPayesFromUserCongesMap(IUser user) {
		if (userMap.get(user) != null) {
			return userMap.get(user).get("CN");
		}
		return null;
	}
	
	public Object getUserAbsenceFromUserCongesMap(IUser user) {
		if (userMap.get(user) != null) {
			return userMap.get(user).get("Absence");
		}
		return null;
	}

	public Object getUserAbsenceFromUserCongesMapByHour(IUser user) {
		if (userMap.get(user) != null
				&& userMap.get(user).get("Absence") != null) {
			return ((Number) userMap.get(user).get("Absence")).floatValue()
					* (((Number) societe.getValue("durationWorkHours"))
							.floatValue() / 60);
		}
		return null;
	}

	public Object getUserAvanceFromUserMap(IUser user) {
		if (userMap.get(user) != null) {
			return userMap.get(user).get("Avance");
		}
		return null;
	}

	public Object getFraisMedicauxFromUserMap(IUser user) {
		if (userMap.get(user) != null) {
			return userMap.get(user).get("FraisMedicaux");
		}
		return null;
	}

	public Object getUserTauxHoraire(IUser user) {
		return user.getExtendedAttributes().getValue("TauxHoraire");
	}

	private Collection<IUser> getUsersWithTheSameSociete(IContext context,
			IDirectoryModule directoryModule, IUser currentUser) {
		Collection<IUser> usersWithTheSameSociete = Collections.emptyList();
		usersWithTheSameSociete = (Collection<IUser>)directoryModule.getUsers(context, currentUser.getOrganization());
		return usersWithTheSameSociete;
	}

	private Collection<IWorkflowInstance> getAllDossierMaladieByUser() {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try {
			IContext sysContext = workflowModule.getSysadminContext();
			IUser connectedUser = workflowModule.getLoggedOnUser();
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(sysContext, "DossierMaladie", organization);
			ICatalog catalog = workflowModule.getCatalog(sysContext, "DossiersMaladie", project);
			IWorkflow w = workflowModule.getWorkflow(sysContext, catalog, "DossiersMaladie_1.0");
			IViewController controller = workflowModule.getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addInConstraint("DocumentState", new ArrayList<String>(Arrays.asList("Remboursé clôturé")));
			controller.addGreaterConstraint("DateRemboursementCloture", paieFrom);
			controller.addLessConstraint("DateRemboursementCloture", paieTo);
			collection = controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return collection;
	}

	private void BuildCongesByUser() {
		Collection<IWorkflowInstance> allConges = getAllCongesOfUsersInTheSameSociete();
		for (IWorkflowInstance conge : allConges) {
			IUser demandeur2 = (IUser) conge.getValue("Demandeur");
			String hisName = demandeur2.getFullName();
			Date dateDebutConge = (Date) conge.getValue("DateDeDebut");
			Date dateFinConge = (Date) conge.getValue("DateDeFin");
			if (DateHelper.isTwoRangesOverlapped(dateDebutConge, dateFinConge, paieFrom, paieTo)) {
				Date calculStartDate = dateDebutConge;
				Date calculFinDate = dateFinConge;
				boolean isStartDateChanged = false;
				boolean isEndDateChanged = false;
				if (DateHelper.isDateBeforeDate(calculStartDate, paieFrom)) {
					calculStartDate = paieFrom;
					isStartDateChanged = true;
				}
				if (DateHelper.isDateAfterDate(calculFinDate, paieTo)) {
					calculFinDate = paieTo;
					isEndDateChanged = true;
				}

				float nombreJours = (Float) new WorkingDaysNumberCalculator(
						workflowModule, getJoursOuvrables(societe),
						getJoursOuvres(societe)).calculateV2(societe,calculStartDate,
						calculFinDate).get("nbrJoursDemande");
				
				nombreJours -= calculateTrancheMinusValue(calculStartDate, calculFinDate, conge, isStartDateChanged, isEndDateChanged);

				IUser demandeur = (IUser) conge.getValue("Demandeur");
				String categorieConges = (String) conge.getValue("TypeDeConge");
				JSONObject tmp = (JSONObject) userDataModel.clone();
//				for (String key : userDataModel.keySet()) {
//					Object value = userDataModel.get(key);
//					tmp.put(key, value);
//				}
				if (userMap.containsKey(demandeur)) {
					tmp = userMap.get(demandeur);
					if (tmp.get(categorieConges) != null) {
						tmp.put(categorieConges,
								(Float) tmp.get(categorieConges) + nombreJours);
					} else {
						tmp.put(categorieConges, nombreJours);
					}
				} else {
					tmp.put(categorieConges, nombreJours);
				}
				userMap.put(demandeur, tmp);
			}
		}
	}
	private void BuildCongesAnnuelsByUser() {
		ArrayList<ILinkedResource> allConges = getAllCongesAnnuelsOfUsersInTheSameSociete();
		for (ILinkedResource conge : allConges) {
			Date dateDebutConge = (Date) conge.getValue("DateDeDebut");
			Date dateFinConge = (Date) conge.getValue("DateDeFin");
			if (DateHelper.isTwoRangesOverlapped(dateDebutConge, dateFinConge, paieFrom, paieTo)) {
				Date calculStartDate = dateDebutConge;
				Date calculFinDate = dateFinConge;
				boolean isStartDateChanged = false;
				boolean isEndDateChanged = false;
				if (DateHelper.isDateBeforeDate(calculStartDate, paieFrom)) {
					calculStartDate = paieFrom;
					isStartDateChanged = true;
				}
				if (DateHelper.isDateAfterDate(calculFinDate, paieTo)) {
					calculFinDate = paieTo;
					isEndDateChanged = true;
				}

				float nombreJours = (Float) new WorkingDaysNumberCalculator(
						workflowModule, getJoursOuvrables(societe),
						getJoursOuvres(societe)).calculateV2(societe,calculStartDate,
						calculFinDate).get("nbrJoursDemande");

				nombreJours -= calculateTrancheMinusValue(calculStartDate, calculFinDate, conge, isStartDateChanged, isEndDateChanged);

				IUser demandeur = (IUser) conge.getValue("Collaborateur");
				String categorieConges = "Conge Annuel";
				JSONObject tmp = (JSONObject) userDataModel.clone();
				if (userMap.containsKey(demandeur)) {
					tmp = userMap.get(demandeur);
					if (tmp.get(categorieConges) != null) {
						tmp.put(categorieConges,
								(Float) tmp.get(categorieConges) + nombreJours);
					} else {
						tmp.put(categorieConges, nombreJours);
					}
				} else {
					tmp.put(categorieConges, nombreJours);
				}
				userMap.put(demandeur, tmp);
			}
		}
	}

	private float calculateTrancheMinusValue(Date dateDebutConge, Date dateFinConge, IResource conge, boolean isStartDateChanged, boolean isEndDateChanged) {
		float heureDebutFinMinus = 0;
		ArrayList<Integer>  joursOuvrables=getJoursOuvrables(societe);
		ArrayList<Integer>  joursOuvres=getJoursOuvres(societe);
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(dateFinConge);

		String debutConges = conge.getValue("DebutConge") != null ? (String) conge.getValue("DebutConge") : "TJ";
		String finConges = conge.getValue("FinConge") != null ? (String) conge.getValue("FinConge") : "TJ";
		if (dateDebutConge.equals(dateFinConge)) {
			if (!isStartDateChanged && debutConges.equals("DJ")) {
				heureDebutFinMinus += .5;
			}
		} else {
			if (!isStartDateChanged && debutConges.equals("DJ")) {
				heureDebutFinMinus += .5;
			}
			if (!isEndDateChanged && finConges.equals("DJ")) {
				heureDebutFinMinus += .5;
			}
		}
			if (isEndDateChanged && joursOuvres.indexOf(endCalendar.get(Calendar.DAY_OF_WEEK)) == joursOuvres.size() - 1) {
				heureDebutFinMinus += Math.abs(joursOuvrables.size() - joursOuvres.size());

			}


		return heureDebutFinMinus;
	}

	public ArrayList<Integer> getJoursOuvrables(IStorageResource societe) {
		ArrayList<Integer> joursOuvrables = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6, 7));
		int joursOuvrablesDebut = -1;
		int joursOuvrablesFin = -1;

		if (societe.getValue("JoursOuvrablesDebut") != null) {
			joursOuvrablesDebut = Integer.parseInt(((String) societe.getValue("JoursOuvrablesDebut")));
		}
		if (societe.getValue("JoursOuvrablesFin") != null) {
			joursOuvrablesFin = Integer.parseInt(((String) societe.getValue("JoursOuvrablesFin")));
		}
		if (joursOuvrablesDebut != -1 && joursOuvrablesFin != -1) {
			joursOuvrables.clear();
			int i = joursOuvrablesDebut;
			joursOuvrables.add(i);
			do {
				i = ((i + 1) / 8) != 0 ? 1 : (i + 1);
				joursOuvrables.add(i);
			} while (i != joursOuvrablesFin);
		}

		return joursOuvrables;
	}

	public ArrayList<Integer> getJoursOuvres(IStorageResource societe) {
		ArrayList<Integer> joursOuvres = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5, 6));
		int joursOuvresDebut = -1;
		int joursOuvresFin = -1;

		if (societe.getValue("JoursOuvresDebut") != null) {
			joursOuvresDebut = Integer.parseInt(((String) societe.getValue("JoursOuvresDebut")));
		}
		if (societe.getValue("JoursOuvresFin") != null) {
			joursOuvresFin = Integer.parseInt(((String) societe.getValue("JoursOuvresFin")));
		}
		if (joursOuvresDebut != -1 && joursOuvresFin != -1) {
			joursOuvres.clear();
			int i = joursOuvresDebut;
			joursOuvres.add(joursOuvresDebut);
			do {
				i = ((i + 1) / 8) != 0 ? 1 : (i + 1);
				joursOuvres.add(i);
			} while (i != joursOuvresFin);
		}

		return joursOuvres;
	}

	private Collection<IWorkflowInstance> getAllCongesOfUsersInTheSameSociete() {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try {
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(sysContext, "Capone", organization);
			ICatalog catalog = workflowModule.getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = workflowModule.getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = workflowModule.getViewController(sysContext);
			controller.addEqualsConstraint("Societe", currentUser.getExtendedAttributes().getValue("Societe"));
			controller.addEqualsConstraint("DocumentState", "Clôturé");
			collection = controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();

		}
		return collection;
	}

	private ArrayList<ILinkedResource> getAllCongesAnnuelsOfUsersInTheSameSociete() {
		Collection<IWorkflowInstance> Conges = new ArrayList<>();
		try {
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(sysContext, "Capone", organization);
			ICatalog catalog = workflowModule.getCatalog(sysContext, "RH", project);
			IWorkflow w = workflowModule.getWorkflow(sysContext, catalog, "LancementDeCongeAnnuel_1.0");
			IViewController controller = workflowModule.getViewController(sysContext);
			controller.addEqualsConstraint("Societe", currentUser.getExtendedAttributes().getValue("Societe"));
			controller.addEqualsConstraint("DocumentState", "Lancé congé annuelle");
			Conges =  controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();

		}
		ArrayList<ILinkedResource> getSelectedUsers = new ArrayList<>();
     for (IWorkflowInstance conge : Conges ){

		ArrayList<ILinkedResource> Users = (ArrayList<ILinkedResource>) conge.getLinkedResources("UsersDeDepartement");
		ArrayList<ILinkedResource> exceptionUsers = (ArrayList<ILinkedResource>) conge.getLinkedResources("DiviserCongeAnnuel");

		for (ILinkedResource user : Users) {

			if (user.getValue("cocher").equals(true) && user.getValue("Statut").equals("OK")) {
				getSelectedUsers.add(user);
			} else if (user.getValue("cocher").equals(false) && user.getValue("Statut").equals("Conflit")) {
				for (ILinkedResource exceptionUser : exceptionUsers) {
					if (user.getValue("Collaborateur").equals(exceptionUser.getValue("Collaborateur"))) {
						getSelectedUsers.add(exceptionUser);
					}
				}
			}
		}

	 }
		return getSelectedUsers;
	}

	private void buildAvanceByUser() {
		Collection<IWorkflowInstance> allAvances = getAllAvancesOfUsersInTheSameSociete();
		for (IWorkflowInstance avance : allAvances) {
			IUser beneficiaire = (IUser) avance.getValue("Beneficiaire");
			JSONObject tmp = (JSONObject) userDataModel.clone();
//			for (String key : userDataModel.keySet()) {
//				Object value = userDataModel.get(key);
//				tmp.put(key, value);
//			}
			if (userMap.containsKey(beneficiaire)) {
				tmp = userMap.get(beneficiaire);
				if (tmp.get("Avance") != null) {
					tmp.put("Avance", ((Number) tmp.get("Avance")).floatValue() + ((Number) avance.getValue("Montant")).floatValue());
					if(((Number)tmp.get("DureeRemboursement")).intValue() < Integer.valueOf((String)avance.getValue("NombreDeMensualite"))){
						tmp.put("DureeRemboursement", Integer.valueOf((String)avance.getValue("NombreDeMensualite")));
					}
				} else {
					tmp.put("Avance", avance.getValue("Montant"));
					tmp.put("DureeRemboursement", Integer.valueOf((String)avance.getValue("NombreDeMensualite")));
				}
			} else {
				tmp.put("Avance", avance.getValue("Montant"));
				tmp.put("DureeRemboursement", Integer.valueOf((String)avance.getValue("NombreDeMensualite")));
			}
			userMap.put(beneficiaire, tmp);
		}
	}
	//buildSortieByUser
	private void buildSortieByUser() {
		Collection<IWorkflowInstance> allSorties = getAllSortiesOfUsersInTheSameSociete();
		for (IWorkflowInstance Sortie : allSorties) {
			IUser beneficiaire = (IUser) Sortie.getValue("Demandeur");
			JSONObject tmp = (JSONObject) userDataModel.clone();
			
			if (userMap.containsKey(beneficiaire)) {
				tmp = userMap.get(beneficiaire);
				if (tmp.get("Sortie") != null) {
					tmp.put("Sortie", ((Number) tmp.get("Sortie")).floatValue() + ((Number) Sortie.getValue("dureeSortieMinutesReelle")).floatValue());
				} else {
					tmp.put("Sortie", Sortie.getValue("dureeSortieMinutesReelle"));
				}
			} else {
				tmp.put("Sortie", Sortie.getValue("dureeSortieMinutesReelle"));
			}
			userMap.put(beneficiaire, tmp);
		}
	}
	//getAllSortiesOfUsersInTheSameSociete
	private Collection<IWorkflowInstance> getAllSortiesOfUsersInTheSameSociete() {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try {
			IContext sysContext = workflowModule.getSysadminContext();
			IUser connectedUser = workflowModule.getLoggedOnUser();
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(sysContext, "Capone", organization);
			ICatalog catalog = workflowModule.getCatalog(sysContext, "RH", project);
			IWorkflow w = workflowModule.getWorkflow(sysContext, catalog, "GestionDeSortie_1.0");
			IViewController controller = workflowModule.getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addEqualsConstraint("DocumentState", "Clôturée");
			controller.addGreaterConstraint("sys_CreationDate", paieFrom);
			controller.addLessConstraint("sys_CreationDate", paieTo);
			collection = controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();

		}
		return collection;
	}
	private void buildFraisMedicauxbyUser() {
		Collection<IWorkflowInstance> allDossiersMaladies = getAllDossierMaladieByUser();
		for (IWorkflowInstance dossierMaladie : allDossiersMaladies) {
			IUser beneficiaire = (IUser) dossierMaladie.getValue("Demandeur");
			JSONObject tmp = (JSONObject) userDataModel.clone();
			if (userMap.containsKey(beneficiaire)) {
				tmp = userMap.get(beneficiaire);
				if (tmp.get("FraisMedicaux") != null) {
					tmp.put("FraisMedicaux", ((Number) tmp.get("FraisMedicaux")).floatValue() + ((Number) dossierMaladie.getValue("MontantDesSoins")).floatValue());
				} else {
					tmp.put("FraisMedicaux", dossierMaladie.getValue("MontantDesSoins"));
				}
			} else {
				tmp.put("FraisMedicaux", dossierMaladie.getValue("MontantDesSoins"));
			}
			userMap.put(beneficiaire, tmp);
		}

	}

	private Collection<IWorkflowInstance> getAllAvancesOfUsersInTheSameSociete() {
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try {
			IContext sysContext = workflowModule.getSysadminContext();
			IUser connectedUser = workflowModule.getLoggedOnUser();
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(sysContext, "GestionDesAvances", organization);
			ICatalog catalog = workflowModule.getCatalog(sysContext, "GestionDesAvances", project);
			IWorkflow w = workflowModule.getWorkflow(sysContext, catalog, "GestionDesAvances_1.0");
			IViewController controller = workflowModule.getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addEqualsConstraint("DocumentState", "Acceptée");
			controller.addGreaterConstraint("sys_CreationDate", paieFrom);
			controller.addLessConstraint("sys_CreationDate", paieTo);
			collection = controller.evaluate(w);
		} catch (Exception e) {
			e.printStackTrace();

		}
		return collection;
	}

}
