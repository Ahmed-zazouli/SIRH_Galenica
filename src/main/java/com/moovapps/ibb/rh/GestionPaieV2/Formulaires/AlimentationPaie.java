package com.moovapps.ibb.rh.GestionPaieV2.Formulaires;

import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.Workbook;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.moovapps.ibb.rh.GestionPaie.Excel.Exceptions.IncoherantDataException;
import com.moovapps.ibb.rh.GestionPaieV2.Excel.MaquetteBuilder;
import com.moovapps.ibb.rh.GestionPaieV2.Excel.NewRecruitExcelMethods;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class AlimentationPaie extends BaseDocumentExtension {

	IWorkflowInstance document = null;
	IUser createur = null;
	String maquettePath = "Maquettes/Maquette variables paie v2.xlsx";

	List<String> colonnes = VariablesPaieColonnes.colonnes;

	List<String> newRecruitsColonnes = VariablesPaieColonnes.newRecruitsColonnes;

	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		createur = (IUser)document.getValue("sys_Creator");
		setFicheSociete();
		if((boolean)document.getValue("fakeReload")){
			document.setValue("fakeReload", false);
		} else {
			if(setAnneeMois()){
				setDonneesMoovapps();
				setElementVariablesACommuniquer();
				document.setValue("firstTime", false);
			} else if(!(boolean)document.getValue("fakeReload")) {
				setDonneesMoovapps();
				ecraserLesDonneesChangesDuVariablesACommuniquer();
				validateExcel();
			}
		}
		document.save(getWorkflowModule().getLoggedOnUserContext());
		return super.onAfterLoad();
	}

	private void ecraserLesDonneesChangesDuVariablesACommuniquer2() {
		if(document.getValue("ComplementsVariablesDeLaPaie") != null){
			ArrayList<IAttachment> tmp = (ArrayList<IAttachment>)document.getValue("ComplementsVariablesDeLaPaie");
			IAttachment elementVariableAttachement = null;
			if(tmp.size() > 0){
				elementVariableAttachement = tmp.get(0);
			}
			File elementVariables = new File("C:\\TEST\\Elements Paie.xlsx");

			try {
				FileUtils.writeByteArrayToFile(elementVariables, elementVariableAttachement.getContent());
				new FileInputStream(elementVariables).close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			document.setValue("ComplementsVariablesDeLaPaie", new ArrayList<>());
			document.save(getWorkflowModule().getLoggedOnUserContext());

			ArrayList<IAttachment> tmp2 = (ArrayList<IAttachment>)document.getValue("DonneesMoovapps");
			IAttachment donneesMoovappsAttachement = null;
			if(tmp2.size() > 0){
				donneesMoovappsAttachement = tmp2.get(0);
			}
			File donneesMoovapps = new File("C:\\TEST\\" + donneesMoovappsAttachement.getName());
			File f = null;
			try {
				FileUtils.writeByteArrayToFile(donneesMoovapps, donneesMoovappsAttachement.getContent());
				f = new MaquetteBuilder(getWorkflowModule(), getDirectoryModule(), getProjectModule(),document).merge(elementVariables, donneesMoovapps);
			} catch (IncoherantDataException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			ArrayList<IAttachment> tmpComplementList = new ArrayList<IAttachment>();
			File toDeleteFile = new File("C:\\TEST\\" + (String)((IStorageResource)document.getValue("Societe")).getValue("sys_Title") + "-"  +(String)document.getText("Mois") + "-" + (String)document.getValue("Annee") + ".xlsx");

			try {
				FileUtils.copyFile(f, toDeleteFile);
				IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), toDeleteFile);
				tmpComplementList.add(tmpJournalPaieAttachment);
				document.setValue("ComplementsVariablesDeLaPaie", tmpComplementList);
				document.save(getWorkflowModule().getLoggedOnUserContext());
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				new FileInputStream(elementVariables).close();
				new FileInputStream(toDeleteFile).close();
				new FileInputStream(f).close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			toDeleteFile.delete();
			toDeleteFile.deleteOnExit();

			elementVariables.delete();
			elementVariables.deleteOnExit();

			donneesMoovapps.delete();
			donneesMoovapps.deleteOnExit();

			f.delete();
			f.deleteOnExit();
		} else {
			setElementVariablesACommuniquer();
		}
		document.save(getWorkflowModule().getLoggedOnUserContext());

//    	String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort())
//    			.concat(Turbine.getContextPath().concat("/easysite/workplace/applications/application-remontees-variables-de-la-paie-0/GestionDeProcessus/edit-document/").concat(document.getId()+""));
//
//		ExternalScreen externalScreen = new ExternalScreen(lien.toString());
//		Navigator.getNavigator().setCurrentScreen(externalScreen);
	}
	private void ecraserLesDonneesChangesDuVariablesACommuniquer() {
		if (document.getValue("ComplementsVariablesDeLaPaie") != null) {
			ArrayList<IAttachment> tmp = (ArrayList<IAttachment>) document.getValue("ComplementsVariablesDeLaPaie");
			IAttachment elementVariableAttachement = null;

			if (tmp.size() > 0) {
				elementVariableAttachement = tmp.get(0);
			}

			File elementVariables = new File("C:\\TEST\\Elements Paie.xlsx");
			FileInputStream elementVariablesStream = null;

			try {
				FileUtils.writeByteArrayToFile(elementVariables, elementVariableAttachement.getContent());
				elementVariablesStream = new FileInputStream(elementVariables);
				// Process the elementVariables file

			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				if (elementVariablesStream != null) {
					try {
						elementVariablesStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			document.setValue("ComplementsVariablesDeLaPaie", new ArrayList<>());
			document.save(getWorkflowModule().getLoggedOnUserContext());

			ArrayList<IAttachment> tmp2 = (ArrayList<IAttachment>) document.getValue("DonneesMoovapps");
			IAttachment donneesMoovappsAttachement = null;

			if (tmp2.size() > 0) {
				donneesMoovappsAttachement = tmp2.get(0);
			}

			File donneesMoovapps = new File("C:\\TEST\\" + donneesMoovappsAttachement.getName());
			FileInputStream donneesMoovappsStream = null;

			try {
				FileUtils.writeByteArrayToFile(donneesMoovapps, donneesMoovappsAttachement.getContent());
				donneesMoovappsStream = new FileInputStream(donneesMoovapps);
				// Process the donneesMoovapps file
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (donneesMoovappsStream != null) {
					try {
						donneesMoovappsStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			File f = null;
			try {
				f = new MaquetteBuilder(getWorkflowModule(), getDirectoryModule(), getProjectModule(), document).merge(elementVariables, donneesMoovapps);
			} catch (IncoherantDataException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			ArrayList<IAttachment> tmpComplementList = new ArrayList<IAttachment>();
			File toDeleteFile = new File("C:\\TEST\\" + (String) ((IStorageResource) document.getValue("Societe")).getValue("sys_Title") + "-" + (String) document.getText("Mois") + "-" + (String) document.getValue("Annee") + ".xlsx");

			try {
				FileUtils.copyFile(f, toDeleteFile);
				IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), toDeleteFile);
				tmpComplementList.add(tmpJournalPaieAttachment);
				document.setValue("ComplementsVariablesDeLaPaie", tmpComplementList);
				document.save(getWorkflowModule().getLoggedOnUserContext());
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				toDeleteFile.delete();
				toDeleteFile.deleteOnExit();
				elementVariables.delete();
				elementVariables.deleteOnExit();
				donneesMoovapps.delete();
				donneesMoovapps.deleteOnExit();
				f.delete();
				f.deleteOnExit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			setElementVariablesACommuniquer();
		}
		document.save(getWorkflowModule().getLoggedOnUserContext());
	}

	private void finalMerge() {
		if(document.getValue("ComplementsVariablesDeLaPaie") != null){
			ArrayList<IAttachment> tmp = (ArrayList<IAttachment>)document.getValue("ComplementsVariablesDeLaPaie");
			IAttachment elementVariableAttachement = null;
			if(tmp.size() > 0){
				elementVariableAttachement = tmp.get(0);
			}
			File elementVariables = new File("C:\\TEST\\Elements Paie.xlsx");

			try {
				FileUtils.writeByteArrayToFile(elementVariables, elementVariableAttachement.getContent());
				//new FileInputStream(elementVariables).close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			document.setValue("ComplementsVariablesDeLaPaie", new ArrayList<>());
			document.save(getWorkflowModule().getLoggedOnUserContext());

			ArrayList<IAttachment> tmp2 = (ArrayList<IAttachment>)document.getValue("DonneesMoovapps");
			IAttachment donneesMoovappsAttachement = null;
			if(tmp2.size() > 0){
				donneesMoovappsAttachement = tmp2.get(0);
			}
			File donneesMoovapps = new File("C:\\TEST\\" + donneesMoovappsAttachement.getName());
			File mergedFile = null;
			try {
				FileUtils.writeByteArrayToFile(donneesMoovapps, donneesMoovappsAttachement.getContent());
		/*Here*/mergedFile = new MaquetteBuilder(getWorkflowModule(), getDirectoryModule(), getProjectModule(),document).finalMerge(elementVariables, donneesMoovapps);
			} catch (IncoherantDataException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			ArrayList<IAttachment> tmpComplementList = new ArrayList<IAttachment>();
			File toDeleteFile = new File("C:\\TEST\\" + (String)((IStorageResource)document.getValue("Societe")).getValue("sys_Title") + "-"  +(String)document.getText("Mois") + "-" + (String)document.getValue("Annee") + ".xlsx");
			try {
				FileUtils.copyFile(mergedFile, toDeleteFile);
				IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), toDeleteFile);
				tmpComplementList.add(tmpJournalPaieAttachment);
				document.setValue("ComplementsVariablesDeLaPaie", tmpComplementList);

				Workbook workbook = null;
				try {

					InputStream pdfInputStream = new FileInputStream(toDeleteFile);
					workbook = new Workbook(pdfInputStream);
					PdfSaveOptions options = new PdfSaveOptions();
					options.setAllColumnsInOnePagePerSheet(true);
					workbook.save("C:\\TEST\\" + tmpJournalPaieAttachment.getName().replaceAll(".xlsx", ".pdf"), options);
					File pdfFile = new File("C:\\TEST\\" + tmpJournalPaieAttachment.getName().replaceAll(".xlsx", ".pdf"));
					tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), pdfFile);
					tmp.clear();
					tmp.add(tmpJournalPaieAttachment);
					document.setValue("PDFRemonteesVariablesDeLaPaie", tmp);
					pdfInputStream.close();
					workbook.dispose();
					pdfFile.delete();
					pdfFile.deleteOnExit();

					/*Here*/
				} catch (Exception e) {
					e.printStackTrace();
				}

				document.save(getWorkflowModule().getLoggedOnUserContext());
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(document.getValue("TableauDAmortissement") != null){
				/*Here*/	ArrayList<IAttachment> tmpArrayTableauAmortissement = (ArrayList<IAttachment>)document.getValue("TableauDAmortissement");
				IAttachment tmpAttchementToCopy = null;
				if(tmpArrayTableauAmortissement.size() > 0){
					tmpAttchementToCopy = tmpArrayTableauAmortissement.get(0);
				}
				if(tmpAttchementToCopy != null){
					Workbook workbook = null;
					try {
						File tmpFile = new File("C:\\TEST\\"+tmpAttchementToCopy.getName());
						FileUtils.writeByteArrayToFile(tmpFile, tmpAttchementToCopy.getContent());
						InputStream pdfInputStream = new FileInputStream(tmpFile);
						workbook = new Workbook(pdfInputStream);
						PdfSaveOptions options = new PdfSaveOptions();
						options.setAllColumnsInOnePagePerSheet(true);
						workbook.save("C:\\TEST\\" + tmpAttchementToCopy.getName().replaceAll(".xlsx", ".pdf"), options);
						File pdfFile = new File("C:\\TEST\\" + tmpAttchementToCopy.getName().replaceAll(".xlsx", ".pdf"));
						tmpAttchementToCopy = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), pdfFile);
						tmp.clear();
						tmp.add(tmpAttchementToCopy);
						document.setValue("PDFTableauDAmortissement", tmp);
						pdfFile.delete();
						pdfFile.deleteOnExit();
						pdfInputStream.close();
						tmpFile.delete();
						tmpFile.deleteOnExit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if((boolean)document.getValue("isThereAreNewRecruits")){
				ArrayList<IAttachment> tmpNouvellesRecruesList = (ArrayList<IAttachment>)document.getValue("NouvellesRecrues");
				IAttachment tmpNouvelleRecrueAttachement = null;
				if(tmpNouvellesRecruesList.size() > 0){
					tmpNouvelleRecrueAttachement = tmpNouvellesRecruesList.get(0);
				}
				if(tmpNouvelleRecrueAttachement != null){

					Workbook workbook = null;
					try {
						File tmpNewRecruits = new File(tmpNouvelleRecrueAttachement.getName());
						FileUtils.writeByteArrayToFile(tmpNewRecruits, tmpNouvelleRecrueAttachement.getContent());
						InputStream pdfInputStream = new FileInputStream(tmpNewRecruits);
						workbook = new Workbook(pdfInputStream);
						PdfSaveOptions options = new PdfSaveOptions();
						options.setAllColumnsInOnePagePerSheet(true);
						workbook.save("C:\\TEST\\" + tmpNouvelleRecrueAttachement.getName().replaceAll(".xlsx", ".pdf"), options);
						File pdfFile = new File("C:\\TEST\\" + tmpNouvelleRecrueAttachement.getName().replaceAll(".xlsx", ".pdf"));
						tmpNouvelleRecrueAttachement = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), pdfFile);
						tmpNouvellesRecruesList.clear();
						tmpNouvellesRecruesList.add(tmpNouvelleRecrueAttachement);
						document.setValue("PDFNouvellesRecrues", tmpNouvellesRecruesList);
						document.save(getWorkflowModule().getLoggedOnUserContext());
						pdfFile.delete();
						pdfFile.deleteOnExit();
						pdfInputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else {
				document.setValue("PDFNouvellesRecrues", new ArrayList<IAttachment>());
				document.setValue("NouvellesRecrues", new ArrayList<IAttachment>());
				document.save(getWorkflowModule().getLoggedOnUserContext());
			}



			toDeleteFile.delete();
			toDeleteFile.deleteOnExit();

			elementVariables.delete();
			elementVariables.deleteOnExit();

			donneesMoovapps.delete();
			donneesMoovapps.deleteOnExit();
		} else {
			setElementVariablesACommuniquer();
		}
		document.save(getWorkflowModule().getLoggedOnUserContext());

//    	String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort())
//    			.concat(Turbine.getContextPath().concat("/easysite/workplace/applications/application-remontees-variables-de-la-paie-0/GestionDeProcessus/edit-document/").concat(document.getId()+""));
//
//		ExternalScreen externalScreen = new ExternalScreen(lien.toString());
//		Navigator.getNavigator().setCurrentScreen(externalScreen);
	}

	@SuppressWarnings("unchecked")
	private boolean validateExcel2() {
		ArrayList<IAttachment> tmp = (ArrayList<IAttachment>)document.getValue("ComplementsVariablesDeLaPaie");
		IAttachment elementVariableAttachement = null;
		if(tmp != null && tmp.size() > 0){
			elementVariableAttachement = tmp.get(0);
		}
		if(elementVariableAttachement == null){
			return false;
		}
		File elementVariables = new File("C:\\TEST\\Elements Paie.xlsx");

		try {
			FileUtils.writeByteArrayToFile(elementVariables, elementVariableAttachement.getContent());
			new FileInputStream(elementVariables).close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ArrayList<String> errors = new ArrayList<>();

		List<String> colonnesToValidate = Arrays.asList(
				"avanceSalairePossibiliteSuspensionOuiNon",
				"pretSocietePossibiliteSuspensionOuiNon",
				"pretConsoEkdom1PossibiliteSuspensionOuiNon",
				"pretConsoEkdom2PossibiliteSuspensionOuiNon",
				"pretConsoWafalasalafPossibiliteSuspensionOuiNon",
				"pretAidPossibiliteSuspensionOuiNon", "mois13OuiNon",
				"mois14OuiNon", "demissionOuiNon",
				"licenciementOuiNon", "abandonPosteOuiNon",
				"miseSommeilContratOuiNon");

		XSSFWorkbook elementVariableCommuniquerWorkbook = null;
		try {
			elementVariableCommuniquerWorkbook = new XSSFWorkbook(new FileInputStream(elementVariables));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (elementVariableCommuniquerWorkbook != null) {
			XSSFSheet elementsVariableACommuniquerSheet = elementVariableCommuniquerWorkbook.getSheetAt(0);
			Iterator<Row> elementsVarCommuniquerFirstSheetIterator = elementsVariableACommuniquerSheet.iterator();
			int rowIndex = 0;
			while (elementsVarCommuniquerFirstSheetIterator.hasNext()) {
				Row nextRow = elementsVarCommuniquerFirstSheetIterator.next();
				if (nextRow == null) continue;

				if (rowIndex > 2) {
					boolean isErrorFoundInThisRow = false;
					for (int i = 0; i < colonnesToValidate.size(); i++) {
						Cell columnToValidate = nextRow.getCell(colonnes.indexOf(colonnesToValidate.get(i)));
						try {
							Method method = this.getClass().getMethod(colonnesToValidate.get(i)+"Validation", Cell.class, Row.class);
							ArrayList<String> errorsListe = (ArrayList<String>)method.invoke(this, columnToValidate, nextRow);
							if(!isErrorFoundInThisRow && errorsListe.size() > 0){
								isErrorFoundInThisRow = true;
							}
							errors.addAll(errorsListe);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(isErrorFoundInThisRow){
						errors.add("\n");
					}
				}
				rowIndex++;
			}
		}
		document.setValue("CommentaireExcel", errors.toString().replace(",", "\n").replace("\n\n", "\n").replace("[", "").replace("]", ""));
		try {
			new FileInputStream(elementVariables).close();
		}catch (Exception e){
			e.printStackTrace();
		}
		elementVariables.delete();
		elementVariables.deleteOnExit();
		return true;
	}
	private boolean validateExcel() {
		ArrayList<IAttachment> tmp = (ArrayList<IAttachment>) document.getValue("ComplementsVariablesDeLaPaie");
		IAttachment elementVariableAttachement = null;

		if (tmp != null && tmp.size() > 0) {
			elementVariableAttachement = tmp.get(0);
		}

		if (elementVariableAttachement == null) {
			return false;
		}

		File elementVariables = new File("C:\\TEST\\Elements Paie.xlsx");
		FileInputStream elementVariablesStream = null;

		try {
			FileUtils.writeByteArrayToFile(elementVariables, elementVariableAttachement.getContent());
			elementVariablesStream = new FileInputStream(elementVariables);
			// Process the elementVariables file

		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (elementVariablesStream != null) {
				try {
					elementVariablesStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		ArrayList<String> errors = new ArrayList<>();

		List<String> colonnesToValidate = Arrays.asList(
				"avanceSalairePossibiliteSuspensionOuiNon",
				"pretSocietePossibiliteSuspensionOuiNon",
				"pretConsoEkdom1PossibiliteSuspensionOuiNon",
				"pretConsoEkdom2PossibiliteSuspensionOuiNon",
				"pretConsoWafalasalafPossibiliteSuspensionOuiNon",
				"pretAidPossibiliteSuspensionOuiNon", "mois13OuiNon",
				"mois14OuiNon", "demissionOuiNon",
				"licenciementOuiNon", "abandonPosteOuiNon",
				"miseSommeilContratOuiNon");

		XSSFWorkbook elementVariableCommuniquerWorkbook = null;

		try {
			elementVariableCommuniquerWorkbook = new XSSFWorkbook(new FileInputStream(elementVariables));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (elementVariableCommuniquerWorkbook != null) {
			XSSFSheet elementsVariableACommuniquerSheet = elementVariableCommuniquerWorkbook.getSheetAt(0);
			Iterator<Row> elementsVarCommuniquerFirstSheetIterator = elementsVariableACommuniquerSheet.iterator();
			int rowIndex = 0;

			while (elementsVarCommuniquerFirstSheetIterator.hasNext()) {
				Row nextRow = elementsVarCommuniquerFirstSheetIterator.next();
				if (nextRow == null) continue;

				if (rowIndex > 2) {
					boolean isErrorFoundInThisRow = false;

					for (int i = 0; i < colonnesToValidate.size(); i++) {
						Cell columnToValidate = nextRow.getCell(colonnes.indexOf(colonnesToValidate.get(i)));

						try {
							Method method = this.getClass().getMethod(colonnesToValidate.get(i) + "Validation", Cell.class, Row.class);
							ArrayList<String> errorsListe = (ArrayList<String>) method.invoke(this, columnToValidate, nextRow);

							if (!isErrorFoundInThisRow && errorsListe.size() > 0) {
								isErrorFoundInThisRow = true;
							}

							errors.addAll(errorsListe);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if (isErrorFoundInThisRow) {
						errors.add("\n");
					}
				}

				rowIndex++;
			}
		}

		document.setValue("CommentaireExcel", errors.toString().replace(",", "\n").replace("\n\n", "\n").replace("[", "").replace("]", ""));

		try {
			if (elementVariablesStream != null) {
				elementVariablesStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (elementVariablesStream != null) {
				try {
					elementVariablesStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		elementVariables.delete();
		elementVariables.deleteOnExit();
		return true;
	}
	public ArrayList<String> avanceSalairePossibiliteSuspensionOuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("avanceSalaireTempsSuspension"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("avanceSalaireTempsSuspension")) + " - merci de saisir avance sur salaire temps de suspension");
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("avanceSalaireTempsSuspension"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum()+1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("avanceSalaireTempsSuspension")) + " - merci de vérifier avance sur salaire temps de suspension");
			}
		}
		return errors;
	}

	public ArrayList<String> pretSocietePossibiliteSuspensionOuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("pretSocieteTempsSuspension"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("pretSocieteTempsSuspension")) + " - merci de saisir prêt société temps de suspension");
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("pretSocieteTempsSuspension"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("pretSocieteTempsSuspension")) + " - merci de vérifier prêt société temps de suspension");
			}
		}
		return errors;
	}

	public ArrayList<String> pretConsoEkdom1PossibiliteSuspensionOuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("pretConsoEkdom1TempsSuspension"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("pretConsoEkdom1TempsSuspension")) + " - merci de saisir prêt consommation ekdom 1 temps de suspension");
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("pretConsoEkdom1TempsSuspension"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("pretConsoEkdom1TempsSuspension")) + " - merci de vérifier prêt consommation ekdom 1 temps de suspension");
			}
		}
		return errors;
	}

	public ArrayList<String> pretConsoEkdom2PossibiliteSuspensionOuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("pretConsoEkdom2TempsSuspension"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("pretConsoEkdom2TempsSuspension")) + " - merci de saisir prêt consommation ekdom 2 temps de suspension");
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("pretConsoEkdom2TempsSuspension"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("pretConsoEkdom2TempsSuspension")) + " - merci de vérifier prêt consommation ekdom 2 temps de suspension");
			}
		}
		return errors;
	}

	public ArrayList<String> pretConsoWafalasalafPossibiliteSuspensionOuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("pretConsoWafalasalafTempsSuspension"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("pretConsoWafalasalafTempsSuspension")) + " - merci de saisir prêt consommation wafasalaf temps de suspension");
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("pretConsoWafalasalafTempsSuspension"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("pretConsoWafalasalafTempsSuspension")) + " - merci de vérifier prêt consommation wafasalaf temps de suspension");
			}
		}
		return errors;
	}

	public ArrayList<String> pretAidPossibiliteSuspensionOuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("pretAidTempsSuspension"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("pretAidTempsSuspension")) + " - merci de saisir prêt aid temps de suspension");
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("pretAidTempsSuspension"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("pretAidTempsSuspension")) + " - merci de vérifier prêt aid temps de suspension");
			}
		}
		return errors;
	}

	public ArrayList<String> mois13OuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("mois13BaseCalcul"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("mois13BaseCalcul")) + " - merci de saisir la base de calcul du 13éme mois");
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("mois13BaseCalcul"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("mois13BaseCalcul")) + " - merci de vérifier la base de calcul du 13éme mois");
			}
		}
		return errors;
	}

	public ArrayList<String> mois14OuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("mois14BaseCalcul"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("mois14BaseCalcul")) + " - merci de saisir la base de calcul du 14éme mois");
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("mois14BaseCalcul"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("mois14BaseCalcul")) + " - merci de vérifier la base de calcul du 14éme mois");
			}
		}
		return errors;
	}

	public ArrayList<String> demissionOuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("demissionDateDepart"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("demissionDateDepart")) + " - merci de saisir démission date départ");
			}
			tmpCell = validationRow.getCell(colonnes.indexOf("demissionPreavisPayeOuiNon"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("demissionPreavisPayeOuiNon")) + " - merci de saisir si le préavis est payé ou pas (démission)");
			}else if(tmpCell.getStringCellValue().equals("Oui")){
				tmpCell = validationRow.getCell(colonnes.indexOf("demissionDureePreavis"));
				if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
					errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("demissionDureePreavis")) + " - merci de saisir la durée de préavis (démission)");
				}
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("demissionDateDepart"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("demissionDateDepart")) + " - merci de vérifier démission date départ");
			}
			tmpCell = validationRow.getCell(colonnes.indexOf("demissionPreavisPayeOuiNon"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("demissionPreavisPayeOuiNon")) + " - merci de vérifier si le préavis est payé ou pas (démission)");
			}
			tmpCell = validationRow.getCell(colonnes.indexOf("demissionDureePreavis"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("demissionDureePreavis")) + " - merci de vérifier la durée de préavis (démission)");
			}
		}
		return errors;
	}

	public ArrayList<String> licenciementOuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("licenciementDateDepart"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciementDateDepart")) + " - merci de saisir licenciement date départ");
			}
			tmpCell = validationRow.getCell(colonnes.indexOf("reglementaireOuAmiable"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("reglementaireOuAmiable")) + " - merci de saisir si le licenciement est réglementaire ou amiable");
			} else {
				if(tmpCell.getStringCellValue().equals("Réglementaire")){
					tmpCell = validationRow.getCell(colonnes.indexOf("indemnitesLicenciement"));
					if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
						errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("indemnitesLicenciement")) + " - merci de saisir l'indemnités de licenciement");
					}
					tmpCell = validationRow.getCell(colonnes.indexOf("dommagesInteretOuiNon"));
					if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
						errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("dommagesInteretOuiNon")) + " - merci de saisir si les dommages ou intéret sont payés ou pas");
					}
					tmpCell = validationRow.getCell(colonnes.indexOf("licenciementPreavisPayeOuiNon"));
					if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
						errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciementPreavisPayeOuiNon")) + " - merci de saisir si le préavis est payé ou pas (licenciement)");
						tmpCell = validationRow.getCell(colonnes.indexOf("licenciementDureePreavis"));
						if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
							errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciementDureePreavis")) + " - merci de vérifier la durée du préavis (licenciement)");
						}
					}else if(tmpCell.getStringCellValue().equals("Oui")){
						tmpCell = validationRow.getCell(colonnes.indexOf("licenciementDureePreavis"));
						if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
							errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciementDureePreavis")) + " - merci de saisir la durée du préavis (licenciement)");
						}
					}else {
						tmpCell = validationRow.getCell(colonnes.indexOf("licenciementDureePreavis"));
						if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
							errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciementDureePreavis")) + " - merci de vérifier la durée du préavis (licenciement)");
						}
					}
				}else{
					tmpCell = validationRow.getCell(colonnes.indexOf("licenciemebtMontant"));
					if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
						errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciemebtMontant")) + " - merci de saisir le montant de licenciement");
					}
				}
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("licenciementDateDepart"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciementDateDepart")) + " - merci de vérifier licenciement date départ");
			}
			tmpCell = validationRow.getCell(colonnes.indexOf("reglementaireOuAmiable"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("reglementaireOuAmiable")) + " - merci de vérifier le licenciement est réglementaire ou amiable");
			}
			tmpCell = validationRow.getCell(colonnes.indexOf("indemnitesLicenciement"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("indemnitesLicenciement")) + " - merci de vérifier l'indemnités de licenciement");
			}
			tmpCell = validationRow.getCell(colonnes.indexOf("dommagesInteretOuiNon"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("dommagesInteretOuiNon")) + " - merci de vérifier les dommages ou intéret sont payés ou pas");
			}
			tmpCell = validationRow.getCell(colonnes.indexOf("licenciementPreavisPayeOuiNon"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciementPreavisPayeOuiNon")) + " - merci de vérifier le préavis est payé ou pas (licenciement)");
			}
			tmpCell = validationRow.getCell(colonnes.indexOf("licenciementDureePreavis"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciementDureePreavis")) + " - merci de vérifier la durée du préavis (licenciement)");
			}
			tmpCell = validationRow.getCell(colonnes.indexOf("licenciemebtMontant"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciemebtMontant")) + " - merci de vérifier le montant de licenciement");
			}
//			tmpCell = validationRow.getCell(colonnes.indexOf("reglementaireOuAmiable"));
//			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
//				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("reglementaireOuAmiable")) + " - merci de vérifier le licenciement est réglementaire ou amiable");
//				tmpCell = validationRow.getCell(colonnes.indexOf("indemnitesLicenciement"));
//				if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
//					errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("indemnitesLicenciement")) + " - merci de vérifier l'indemnités de licenciement");
//				}
//				tmpCell = validationRow.getCell(colonnes.indexOf("dommagesInteretOuiNon"));
//				if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
//					errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("dommagesInteretOuiNon")) + " - merci de vérifier si les dommages ou intéret sont payés ou pas");
//				}
//				tmpCell = validationRow.getCell(colonnes.indexOf("licenciementPreavisPayeOuiNon"));
//				if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
//					errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciementPreavisPayeOuiNon")) + " - merci de vérifier le préavis est payé ou pas (licenciement)");
//					tmpCell = validationRow.getCell(colonnes.indexOf("licenciementDureePreavis"));
//					if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
//						errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciementDureePreavis")) + " - merci de vérifier si durée du préavis (licenciement)");
//					}
//				}
//				tmpCell = validationRow.getCell(colonnes.indexOf("licenciemebtMontant"));
//				if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
//					errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("licenciemebtMontant")) + " - merci de saisir le montant de licenciement");
//				}
//			}
		}
		return errors;
	}

	public ArrayList<String> abandonPosteOuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("abandonPosteDateSortie"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("abandonPosteDateSortie")) + " - merci de saisir abandon de poste date sortie");
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("abandonPosteDateSortie"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("abandonPosteDateSortie")) + " - merci de vérifier abandon de poste date sortie");
			}
		}
		return errors;
	}

	public ArrayList<String> miseSommeilContratOuiNonValidation(Cell validationCell, Row validationRow) {
		ArrayList<String> errors = new ArrayList<>();
		if(validationCell != null && validationCell.getCellType() != CellType.BLANK && validationCell.getStringCellValue().equals("Oui")){
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("miseSommeilContratDate"));
			if(tmpCell == null || tmpCell.getCellType() == CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("miseSommeilContratDate")) + " - merci de saisir la date mise en sommeil");
			}
		} else {
			Cell tmpCell = validationRow.getCell(colonnes.indexOf("miseSommeilContratDate"));
			if(tmpCell != null && tmpCell.getCellType() != CellType.BLANK){
				errors.add("Ligne: " + (validationRow.getRowNum() +1) + " Cellule: " + CellReference.convertNumToColString(colonnes.indexOf("miseSommeilContratDate")) + " - merci de vérifier la date mise en sommeil");
			}
		}
		return errors;
	}

	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("AlimentationRemonteesVariablesDeLaPaie")){
			if((boolean)document.getValue("RemonteesDejaGenere")){
				getResourceController().alert("Les remontées variables de la paie du mois et année sélectionné sont déja générés");
				return false;
			} else {
				finalMerge();
				if(document.getValue("CommentaireExcel") != null && !document.getValue("CommentaireExcel").equals("")){
					getResourceController().alert("Merci de résoudre les problémes Excel!");
					return false;
				}
//				document.setValue("CommentaireRetourModifDRH", null);
//				document.setValue("ElementsVariablesPaieModifieParBPO", null);
//				document.save(getWorkflowModule().getLoggedOnUserContext());
			}
		}
		return super.onBeforeSubmit(action);
	}

	private void setFicheSociete() {
		IStorageResource societe = null;
		if(createur.getExtendedAttributes().getValue("Societe") != null){
			societe = (IStorageResource)createur.getExtendedAttributes().getValue("Societe");
		}
		document.setValue("Societe", societe != null ? societe : null);
	}

	private File getMaquetteFromFicheSociete() {
		File fileToReturn = null;
		if(document.getValue("Societe") != null){
			IStorageResource societe = (IStorageResource)document.getValue("Societe");
			if(societe.getValue("MaquetteElementsVariablesPaieAvecCodesRubriquesEtConstantes") != null){
				ArrayList<IAttachment> maquettesList = (ArrayList<IAttachment>)societe.getValue("MaquetteElementsVariablesPaieAvecCodesRubriquesEtConstantes");
				IAttachment maquette = null;
				if(maquettesList.size() > 0){
					maquette = maquettesList.get(0);
				}
				if(maquette != null && societe.getValue("sys_Title") != null && document.getValue("Mois") != null && document.getValue("Annee") != null){
					try {
						String societeName = (String)societe.getValue("sys_Title");
						String mois = (String)document.getValue("Mois");
						String annee = (String)document.getValue("Annee");
						fileToReturn = new File("c://TEST//Données Moovapps-" + societeName + "-" + mois + "-" + annee+".xlsx");
						FileUtils.writeByteArrayToFile(fileToReturn, maquette.getContent());
						new FileInputStream(fileToReturn).close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return fileToReturn;
	}

	private boolean setAnneeMois() {
		Calendar c = Calendar.getInstance();
		if(document.getValue("Annee") == null || document.getValue("Mois") == null){
			if(document.getValue("Annee") == null){
				document.setValue("Annee", String.valueOf(c.get(Calendar.YEAR)));
			}
			if(document.getValue("Mois") == null){
				document.setValue("Mois", String.valueOf(c.get(Calendar.MONTH)));
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPropertyChanged(IProperty property) {
		if(property.getName().equals("Mois") || property.getName().equals("Annee") ){
			onAnneeMoisChange();
		} else if(property.getName().equals("ComplementsVariablesDeLaPaie")){
			document.save(getWorkflowModule().getSysadminContext());
			if(document.getValue("ComplementsVariablesDeLaPaie") != null && ((ArrayList<IAttachment>)document.getValue("ComplementsVariablesDeLaPaie")).size() > 0){
				if((boolean)document.getValue("lastComplementValueWasNull")){
					document.setValue("isThereAreNewRecruits", false);
					isThereAreNewRecruits();
					validateExcel();
				}
				document.setValue("lastComplementValueWasNull", true);
				document.setValue("fakeReload", true);
				document.save(getWorkflowModule().getLoggedOnUserContext());
				/*String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort())
						.concat(Turbine.getContextPath().concat("/easysite/workplace/applications/application-remontees-variables-de-la-paie-0/GestionDeProcessus/edit-document/").concat(document.getId()+""));

				ExternalScreen externalScreen = new ExternalScreen(lien.toString());
				Navigator.getNavigator().setCurrentScreen(externalScreen);*/
			}else{
				document.setValue("lastComplementValueWasNull", false);

			}
		}
		super.onPropertyChanged(property);
	}

	private boolean isThereAreNewRecruits() {
		ArrayList<IAttachment> tmpAttachementList = (ArrayList<IAttachment>) document.getValue("ComplementsVariablesDeLaPaie");
		IAttachment elementVariableAttachement = null;
		if (tmpAttachementList.size() > 0) {
			elementVariableAttachement = tmpAttachementList.get(0);
		}
		File elementVariables = new File("C:\\TEST\\Elements Paie.xlsx");

		try {
			FileUtils.writeByteArrayToFile(elementVariables, elementVariableAttachement.getContent());
			new FileInputStream(elementVariables).close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		XSSFWorkbook elementVariableCommuniquerWorkbook = null;
		try {
			elementVariableCommuniquerWorkbook = new XSSFWorkbook(new FileInputStream(elementVariables));
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Integer> newRecruitsIndexesInElementVariablesACommuniquerExcelFile = new ArrayList<>();

		if (elementVariableCommuniquerWorkbook != null) {
			XSSFSheet elementsVariableACommuniquerSheet = elementVariableCommuniquerWorkbook.getSheetAt(0);
			Iterator<Row> elementsVarCommuniquerFirstSheetIterator = elementsVariableACommuniquerSheet.iterator();
			int rowIndex = 0;
			while (elementsVarCommuniquerFirstSheetIterator.hasNext()) {
				Row nextRow = elementsVarCommuniquerFirstSheetIterator.next();
				if (nextRow == null)
					continue;

				if (rowIndex > 2) {
					Cell nouvelleRecrueCell = nextRow.getCell(colonnes.indexOf("_nouvelleRecrueOuiNon"));
					if (nouvelleRecrueCell != null&& nouvelleRecrueCell.getCellType() != CellType.BLANK && nouvelleRecrueCell.getStringCellValue().equals("Oui")) {
						if (!(boolean) document.getValue("isThereAreNewRecruits")) {
							document.setValue("isThereAreNewRecruits", true);
							setMaquetteNewRecruits();
						}
						newRecruitsIndexesInElementVariablesACommuniquerExcelFile.add(rowIndex);
					}
				}
				rowIndex++;
			}

			if (newRecruitsIndexesInElementVariablesACommuniquerExcelFile.size() > 0) {
				File tmp = mergeNewRecruits(newRecruitsIndexesInElementVariablesACommuniquerExcelFile,elementVariableCommuniquerWorkbook);
				File toDeleteFile = new File("C:\\TEST\\Nouvelles recrues " + (String) document.getText("Mois") + "-" + (String) document.getValue("Annee") + ".xlsx");
				try {
					FileUtils.copyFile(tmp, toDeleteFile);
					IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), toDeleteFile);
					ArrayList<IAttachment> tmpNouvellesRecruesList = new ArrayList<>();
					tmpNouvellesRecruesList.add(tmpJournalPaieAttachment);
					toDeleteFile.delete();
					toDeleteFile.deleteOnExit();
					tmp.delete();
					tmp.deleteOnExit();
					document.setValue("NouvellesRecrues", tmpNouvellesRecruesList);
					document.save(getWorkflowModule().getLoggedOnUserContext());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				document.setValue("NouvellesRecrues",new ArrayList<IAttachment>());
			}
		}

		elementVariables.delete();
		elementVariables.deleteOnExit();
		return false;
	}

	private File mergeNewRecruits(ArrayList<Integer> indexes, XSSFWorkbook elementVariableCommuniquerWorkbook) {
		XSSFWorkbook newRecruitsWorkbook = null;

		File newRecruitsFile = null;

		try {
			IAttachment newRecruitsAttachment = ((ArrayList<IAttachment>) document.getValue("NouvellesRecrues")).get(0);
			newRecruitsFile = new File("c://TEST//tmpRecruitsFile.xlsx");
			FileUtils.writeByteArrayToFile(newRecruitsFile, newRecruitsAttachment.getContent());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			newRecruitsWorkbook = new XSSFWorkbook(new FileInputStream(newRecruitsFile));

			if (newRecruitsWorkbook != null && elementVariableCommuniquerWorkbook != null) {
				XSSFSheet newRecruitsFirstSheet = newRecruitsWorkbook.getSheetAt(0);
				XSSFSheet elementsVariableACommuniquerSheet = elementVariableCommuniquerWorkbook.getSheetAt(0);

				int indexOfFirstRecruits = 1;
				for (int i = 0; i < indexes.size(); i++) {
					XSSFRow elementAcommuniquerRow = elementsVariableACommuniquerSheet.getRow(indexes.get(i));
					XSSFRow newRecruitsRow = newRecruitsFirstSheet.createRow(indexOfFirstRecruits + i);
					IUser currentRowUser = null;
					for (int j = 0; j < newRecruitsColonnes.size(); j++) {
						String test = newRecruitsColonnes.get(j);
						if(!test.startsWith("_")){
							continue;
						}
						int cellIndexInElementVariablesColonnes = colonnes.indexOf(test);
						XSSFCell elementVariablesACommuniquer = null;
						if (cellIndexInElementVariablesColonnes != -1) {
							elementVariablesACommuniquer = elementAcommuniquerRow.getCell(cellIndexInElementVariablesColonnes);
							if (elementVariablesACommuniquer != null) {
								if (elementVariablesACommuniquer.getCellType() == CellType.STRING) {
									newRecruitsRow.createCell(j).setCellValue(elementVariablesACommuniquer.getStringCellValue());
								} else if (elementVariablesACommuniquer.getCellType() == CellType.NUMERIC) {
									newRecruitsRow.createCell(j).setCellValue(elementVariablesACommuniquer.getNumericCellValue());
								}
							}
						}else {
							IStorageResource societe =null;
							societe = (IStorageResource)getWorkflowInstance().getValue("Societe");
							if(currentRowUser == null){
								currentRowUser = getUserByMatricule(newRecruitsRow.getCell(0).getStringCellValue(),societe);
							}
							if(currentRowUser != null){
								try {
									Method method = NewRecruitExcelMethods.class.getMethod(test, IUser.class);
									Object value = method.invoke(new NewRecruitExcelMethods(), currentRowUser);
									if(value instanceof Number){
										newRecruitsRow.createCell(j).setCellValue(((Number)value).doubleValue());
									} else if(value instanceof Date){
										newRecruitsRow.createCell(j).setCellValue((Date)value);
									} else {
										newRecruitsRow.createCell(j).setCellValue((String)value);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try (FileOutputStream outputStream = new FileOutputStream("c://TEST//tmpRecruits.xlsx")) {
			newRecruitsWorkbook.write(outputStream);
			File toDelete = new File("c://TEST//tmpRecruits.xlsx");
			FileUtils.copyFile(toDelete, newRecruitsFile);
			toDelete.delete();
			toDelete.deleteOnExit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newRecruitsFile;
	}

	private IUser getUserByMatricule(String matricule,IStorageResource Societe) {
		IStorageResource collaborateur = null;
		if(matricule != null){
			try {
				IContext context = getWorkflowModule().getSysadminContext();
				IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
				IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",getWorkflowInstance().getCatalog().getProject().getOrganization());
				ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
				IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
				controller.addEqualsConstraint("Matricule", matricule);
				controller.addEqualsConstraint("Societe", Societe);
				Collection<IStorageResource> collaborateurs = controller.evaluate(definition);
				if (!collaborateurs.isEmpty()){
					collaborateur = collaborateurs.iterator().next();
					return (IUser)collaborateur.getValue("Salarie");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void setMaquetteNewRecruits() {
		File maquetteNewRecruits = createFileFromFileCenterInLocalDrive("Maquettes/Nouvelles recrues.xlsx", "Nouvelles recrues.xlsx");
		ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
		IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(),maquetteNewRecruits);
		tmpAttachementCollection.add(tmpJournalPaieAttachment);
		document.setValue("NouvellesRecrues", tmpAttachementCollection);
		maquetteNewRecruits.delete();
		maquetteNewRecruits.deleteOnExit();
	}

	private void onAnneeMoisChange() {
		document.setValue("RemonteesVientDeGenere", false);
		document.setValue("RemonteesVariablesDeLaPaie", null);
		document.setValue("PDFRemonteesVariablesDeLaPaie", null);
		document.setValue("ComplementsVariablesDeLaPaie", null);
		document.setValue("DonneesMoovapps", null);
		document.setValue("RemonteesDejaGenere", false);

		IStorageResource societe = null;
		String annee = null;
		String mois = null;
		IStorageResource remonteesVariablesPaie = null;
		if(document.getValue("Societe") != null && document.getValue("Annee") != null && document.getValue("Mois") !=null){
			societe = (IStorageResource)document.getValue("Societe");
			annee = (String)document.getValue("Annee");
			mois = (String)document.getValue("Mois");
			remonteesVariablesPaie = getJouralPaieBySocieteAnneeMois(societe, annee, mois);
		}
		if(remonteesVariablesPaie != null && remonteesVariablesPaie.getValue("PDFRemonteesDeLaPaie") != null){
			setPDFElementVariablesACommuniquer(remonteesVariablesPaie);
		} else {
			setDonneesMoovapps();
			setElementVariablesACommuniquer();
		}
		document.setValue("fakeReload", true);
		document.save(getWorkflowModule().getLoggedOnUserContext());
    	/*String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort())
    			.concat(Turbine.getContextPath().concat("/easysite/workplace/applications/application-remontees-variables-de-la-paie-0/GestionDeProcessus/edit-document/").concat(document.getId()+""));

		ExternalScreen externalScreen = new ExternalScreen(lien.toString());
		Navigator.getNavigator().setCurrentScreen(externalScreen);*/
	}

	private void setPDFElementVariablesACommuniquer(IStorageResource remonteesVariablesPaie) {
		document.setValue("RemonteesVientDeGenere", true);
		document.setValue("RemonteesDejaGenere", true);

		duplicateFile(remonteesVariablesPaie, document, "PDFRemonteesDeLaPaie", "PDFRemonteesVariablesDeLaPaie", null);
		duplicateFile(remonteesVariablesPaie, document, "PDFNouvellesRecrues", "PDFNouvellesRecrues", null);
		duplicateFile(remonteesVariablesPaie, document, "PDFTableauDAmortissement", "PDFTableauDAmortissement", null);

	}

	private void duplicateFile(IResource docFrom, IResource docTo, String fromSysName, String toSysName, String newName) {
		ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
		ArrayList<IAttachment> tmp = (ArrayList<IAttachment>)docFrom.getValue(fromSysName);
		IAttachment tmpAttachement = null;
		if(tmp != null && tmp.size() > 0){
			tmpAttachement = tmp.get(0);
		}
		if(tmpAttachement != null){
			try
			{
				File file = null;
				if(newName != null){
					file = new File("c://TEST//" + tmpAttachement.getName().split("\\.")[tmpAttachement.getName().split("\\.").length-1]);
				}else{
					file = new File("c://TEST//" + tmpAttachement.getName());
				}
				FileUtils.writeByteArrayToFile(file, tmpAttachement.getContent());
				IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
				tmpAttachementCollection.add(tmpJournalPaieAttachment);
				docTo.setValue(toSysName, tmpAttachementCollection);
				file.delete();
				file.deleteOnExit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setDonneesMoovapps(){
		File file = getMaquetteFromFicheSociete();//createFileFromFileCenterInLocalDrive(maquettePath, "Donnees Moovapps.xlsx");
		if(file != null){
			try {
				new MaquetteBuilder(getWorkflowModule(), getDirectoryModule(), getProjectModule(),document).buildMaquette(file);
				ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
				IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
				tmpAttachementCollection.add(tmpJournalPaieAttachment);
				document.setValue("DonneesMoovapps", tmpAttachementCollection);
				new FileInputStream(file).close();
				file.delete();
				file.deleteOnExit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			getResourceController().alert("Merci d'ajouter la maquette variables de la paie dans la fiche client");
		}
	}

	private void setElementVariablesACommuniquer() {
		try {
			ArrayList<IAttachment> donneesMoovapps = (ArrayList<IAttachment>)document.getValue("DonneesMoovapps");
			IAttachment tmpJournalPaieAttachment = null;

			if(donneesMoovapps != null && donneesMoovapps.size() > 0){
				tmpJournalPaieAttachment = donneesMoovapps.get(0);
			}
			if(tmpJournalPaieAttachment == null){
				return;
			}
			File elementVpACommuniquer = new File("C:\\TEST\\" + (String)((IStorageResource)document.getValue("Societe")).getValue("sys_Title") + "-"  +(String)document.getText("Mois") + "-" + (String)document.getValue("Annee") + ".xlsx");
			//File elementVpACommuniquer = new File("C:\\TEST\\" + (String)document.getText("Societe") + "-"  +(String)document.getText("Mois") + "-" + (String)document.getValue("Annee") + ".xlsx");
			FileUtils.writeByteArrayToFile(elementVpACommuniquer, tmpJournalPaieAttachment.getContent());

			IAttachment tmpElementsVariablesPaieACommuniquer = null;
			tmpElementsVariablesPaieACommuniquer = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), elementVpACommuniquer);
			ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
			tmpAttachementCollection.add(tmpElementsVariablesPaieACommuniquer);
			document.setValue("ComplementsVariablesDeLaPaie", tmpAttachementCollection);
			document.save(getWorkflowModule().getLoggedOnUserContext());
			elementVpACommuniquer.delete();
			elementVpACommuniquer.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File createFileFromFileCenterInLocalDrive(String filePath, String fileName) {
		try {
			ILibraryModule libraryModule = Modules.getLibraryModule();
			IContext context = getWorkflowModule().getSysadminContext();
			IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
			ILibrary library = libraryModule.getLibrary(context, organization, "Paie");
			if (library != null) {
				IFile file = libraryModule.getFileByPath(context, library, filePath);
				if (file == null) {
					getResourceController().alert("Le fichier de modele n'existe pas dans l'espace documentaire...Vieullez contacter votre administrateur");
				}else{
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

	public IStorageResource getJouralPaieBySocieteAnneeMois(IStorageResource societe, String annee, String mois) {
		IStorageResource remonteesVariablesPaie = null;
		if(societe != null && annee != null && mois != null){
			try {
				IContext context = getWorkflowModule().getSysadminContext();
				IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
				IProject project = getProjectModule().getProject(context,"RemonteVariableDeLaPaie", getWorkflowInstance().getCatalog().getProject().getOrganization());
				ICatalog catalog = getWorkflowModule().getCatalog(context,"JournalDeLaPaie", 4, project);
				IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "JournalDeLaPaieParSocieteParAnneeParMois");
				controller.addEqualsConstraint("Societe", societe);
				controller.addEqualsConstraint("Annee", annee);
				controller.addEqualsConstraint("Mois", mois);
				Collection<IStorageResource> journauxPaie = controller.evaluate(definition);
				if (!journauxPaie.isEmpty()){
					remonteesVariablesPaie = journauxPaie.iterator().next();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return remonteesVariablesPaie;
	}

}
