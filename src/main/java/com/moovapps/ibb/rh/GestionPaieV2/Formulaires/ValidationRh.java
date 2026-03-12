package com.moovapps.ibb.rh.GestionPaieV2.Formulaires;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

public class ValidationRh extends BaseDocumentExtension {
	
	IWorkflowInstance document = null;
	
	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		return super.onAfterLoad();
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("RetourneePourModification")){
//			document.setValue("isThereAreNewRecruits", false);
//			document.save(getWorkflowModule().getSysadminContext());
		}else if(action.getName().equals("ValidationDesElementsVariablesDeLaPaie")){
			document.setValue("CommentaireRetourModifDRH", null);
			document.setValue("ElementsVariablesPaieModifieParBPO", null);
			document.save(getWorkflowModule().getLoggedOnUserContext());
			getResourceController().alert("La saisie des éléments variables de la paie a été validée");
//			if((boolean)document.getValue("isThereAreNewRecruits")){
//				ArrayList<IAttachment> tmpNouvellesRecruesList = (ArrayList<IAttachment>)document.getValue("NouvellesRecrues");
//				IAttachment tmpNouvelleRecrueAttachement = null;
//				if(tmpNouvellesRecruesList.size() > 0){
//					tmpNouvelleRecrueAttachement = tmpNouvellesRecruesList.get(0);
//				}
//				if(tmpNouvelleRecrueAttachement != null){					
//					
//					Workbook workbook = null;
//					try {
//						File tmpNewRecruits = new File(tmpNouvelleRecrueAttachement.getName());
//						FileUtils.writeByteArrayToFile(tmpNewRecruits, tmpNouvelleRecrueAttachement.getContent());
//						InputStream pdfInputStream = new FileInputStream(tmpNewRecruits);
//						workbook = new Workbook(pdfInputStream);
//						PdfSaveOptions options = new PdfSaveOptions();
//						options.setAllColumnsInOnePagePerSheet(true);
//						workbook.save("C:\\TEST\\" + tmpNouvelleRecrueAttachement.getName().replaceAll(".xlsx", ".pdf"), options);
//						File pdfFile = new File("C:\\TEST\\" + tmpNouvelleRecrueAttachement.getName().replaceAll(".xlsx", ".pdf"));
//						tmpNouvelleRecrueAttachement = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), pdfFile);
//						tmpNouvellesRecruesList.clear();
//						tmpNouvellesRecruesList.add(tmpNouvelleRecrueAttachement);
//						document.setValue("PDFNouvellesRecrues", tmpNouvellesRecruesList);
//						document.save(getWorkflowModule().getLoggedOnUserContext());
//						pdfFile.delete();
//						pdfFile.deleteOnExit();
//						pdfInputStream.close();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
		}
		return super.onBeforeSubmit(action);
	}

}
