package com.moovapps.ibb.rh.GestionPaie;

import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.Workbook;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.screens.ExternalScreen;
import com.moovapps.ibb.rh.Cummon.SimpleExcelWriter;
import com.moovapps.ibb.rh.GestionPaie.Excel.ExcelMerger;
import com.moovapps.ibb.rh.GestionPaie.Excel.Exceptions.IncoherantDataException;
import org.apache.commons.io.FileUtils;
import org.apache.turbine.Turbine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

public class AlimentationPaie extends BaseDocumentExtension {
	
	IWorkflowInstance document = null;
	IUser createur = null;

	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		createur = (IUser)document.getValue("sys_Creator");
		setAnneeMois();
		setMaquette();
		setFicheSociete();
		return super.onAfterLoad();
	}
	
	private void setMaquette() {
		if(document.getValue("Maquette") == null){
			try 
			{
				ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
				File maquetteFile = createFileFromFileCenterInLocalDrive("Maquettes/Maquette remontées variables paie.xlsx","Maquette remontées variables paie.xlsx");
				new SimpleExcelWriter(getWorkflowModule(), getDirectoryModule(), getProjectModule(),document).buildMaquette(maquetteFile);
				IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), maquetteFile);
				tmpAttachementCollection.add(tmpJournalPaieAttachment);
				document.setValue("Maquette", tmpAttachementCollection);
				maquetteFile.delete();
				maquetteFile.deleteOnExit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("AlimentationRemonteesVariablesDeLaPaie")){
			if((boolean)document.getValue("RemonteesDejaGenere")){
				getResourceController().alert("Les remontées variables de la paie du mois et année sélectionné sont déja générés");
				return false;
			}else{
				mergePaieData();
			}
		}
		return super.onBeforeSubmit(action);
	}
	
	private void setFicheSociete() {
		String societeName = null;
		if(createur.getExtendedAttributes().getValue("Societe") != null){	
			societeName = (String)createur.getExtendedAttributes().getValue("Societe");
		}
		document.setValue("Societe", societeName);
		document.setValue("FicheSociete", societeName != null ? getSociete(societeName) : null);
	}
	
	private void setAnneeMois() {
		Calendar c = Calendar.getInstance();
		if(document.getValue("Annee") == null){
			document.setValue("Annee", c.get(Calendar.YEAR));
		}
		if(document.getValue("Mois") == null){
			document.setValue("Mois", String.valueOf(c.get(Calendar.MONTH)));
		}
	}
	
	@Override
	public void onPropertyChanged(IProperty property) {
		if(property.getName().equals("Mois") || property.getName().equals("Annee") ){
			onAnneeMoisChange();
		}
//		else if(property.getName().equals("ComplementsVariablesDeLaPaie2")){
//			onComplementUpload();
//		}
		super.onPropertyChanged(property);
	}
	
	private void mergePaieData() {
		File tmpComplementPaieFile = null;
		File tmpDonneesMoovappsFile = null;

		ArrayList<IAttachment> tmp = (ArrayList<IAttachment>)document.getValue("ComplementsVariablesDeLaPaie");
		IAttachment complementVariablePaieAttachement = null;
		if(tmp != null && tmp.size() > 0){
			complementVariablePaieAttachement = tmp.iterator().next();
		}
		if(complementVariablePaieAttachement != null){
			try {
				tmpComplementPaieFile = new File("c://TEST//tmpComplementsVariablesPaie.xlsx");
				FileUtils.writeByteArrayToFile(tmpComplementPaieFile, complementVariablePaieAttachement.getContent());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		tmp = (ArrayList<IAttachment>)document.getValue("DonneesMoovapps");
		IAttachment donnesMoovappsAttachement = null;
		if(tmp != null && tmp.size() > 0){
			donnesMoovappsAttachement = tmp.iterator().next();
		}
		if(donnesMoovappsAttachement != null){
			try {
				tmpDonneesMoovappsFile = new File("c://TEST//tmpDonneesMoovappsFile.xlsx");
				FileUtils.writeByteArrayToFile(tmpDonneesMoovappsFile, donnesMoovappsAttachement.getContent());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			ExcelMerger.merge(tmpComplementPaieFile, tmpDonneesMoovappsFile);
		} catch (IncoherantDataException e) {
			getResourceController().alert(e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		tmp.clear();
		File toDeleteFile = new File("C:\\TEST\\Remontées variables de la paie "+(String)document.getText("Mois") + "-" + ((Number)document.getValue("Annee")).intValue() + ".xlsx");
		try {
			FileUtils.copyFile(tmpDonneesMoovappsFile, toDeleteFile);
			IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), toDeleteFile);
			tmp.add(tmpJournalPaieAttachment);
			document.setValue("RemonteesVariablesDeLaPaie", tmp);
			
			// Create Workbook to load Excel file
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
				pdfFile.delete();
				pdfFile.deleteOnExit();
				pdfInputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Save the document in PDF format
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		tmpComplementPaieFile.delete();
		tmpComplementPaieFile.deleteOnExit();
		tmpDonneesMoovappsFile.delete();
		tmpDonneesMoovappsFile.deleteOnExit();
		toDeleteFile.delete();
		toDeleteFile.deleteOnExit();
		
		
		document.save(getWorkflowModule().getLoggedOnUserContext());
    	String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort())
    			.concat(Turbine.getContextPath().concat("/easysite/workplace/applications/application-remontees-variables-de-la-paie-0/GestionDeProcessus/edit-document/").concat(document.getId()+""));    

		ExternalScreen externalScreen = new ExternalScreen(lien.toString());
		Navigator.getNavigator().setCurrentScreen(externalScreen);
		
	}
	
	private void onAnneeMoisChange() {
		document.setValue("RemonteesVientDeGenere", false);
		document.setValue("RemonteesVariablesDeLaPaie", null);
		document.setValue("PDFRemonteesVariablesDeLaPaie", null);
//		document.setValue("ComplementsVariablesDeLaPaie", null);
		document.setValue("DonneesMoovapps", null);
		document.setValue("RemonteesDejaGenere", false);

		IStorageResource societe = null;
		float annee = -1;
		String mois = null;
		IStorageResource remonteesVariablesPaie = null;
		if(document.getValue("Societe") != null && document.getValue("Annee") != null && document.getValue("Mois") !=null){
			societe = (IStorageResource)document.getValue("FicheSociete");
			annee = ((Number)document.getValue("Annee")).floatValue();
			mois = (String)document.getValue("Mois");
			remonteesVariablesPaie = getJouralPaieBySocieteAnneeMois(societe, annee, mois);
		}
		if(remonteesVariablesPaie != null && remonteesVariablesPaie.getValue("PDFRemonteesDeLaPaie") != null){
			document.setValue("RemonteesVientDeGenere", true);
			document.setValue("RemonteesDejaGenere", true);

			ArrayList<IAttachment> tmp = (ArrayList<IAttachment>)remonteesVariablesPaie.getValue("PDFRemonteesDeLaPaie");
			IAttachment journalPaieFile = null;
			if(tmp.size() > 0){
				journalPaieFile = tmp.get(0);
			}
			if(journalPaieFile != null){				
				try 
				{
					ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
					File file = null;
					file = new File("c://TEST//" + journalPaieFile.getName());
					FileUtils.writeByteArrayToFile(file, journalPaieFile.getContent());
					IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
					tmpAttachementCollection.add(tmpJournalPaieAttachment);
					document.setValue("PDFRemonteesVariablesDeLaPaie", tmpAttachementCollection);
					file.delete();
					file.deleteOnExit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			File file = createFileFromFileCenterInLocalDrive("Maquettes/Modele Canevas éléments variables mensuels.xlsx","Donnees Moovapps.xlsx");
			if(file != null){
				try {
					new SimpleExcelWriter(getWorkflowModule(), getDirectoryModule(), getProjectModule(),document).buildDonneesMoovapps(file);
					ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
					IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
					tmpAttachementCollection.add(tmpJournalPaieAttachment);
					document.setValue("DonneesMoovapps", tmpAttachementCollection);
					file.delete();
					file.deleteOnExit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		document.save(getWorkflowModule().getLoggedOnUserContext());
    	String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort())
    			.concat(Turbine.getContextPath().concat("/easysite/workplace/applications/application-remontees-variables-de-la-paie-0/GestionDeProcessus/edit-document/").concat(document.getId()+""));    

		ExternalScreen externalScreen = new ExternalScreen(lien.toString());
		Navigator.getNavigator().setCurrentScreen(externalScreen);
	}
	
	public IStorageResource getSociete(String societeName) {
		IStorageResource societe = null;
		if(societeName != null){			
			try {
				IContext context = getWorkflowModule().getSysadminContext();
				IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
				IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",getWorkflowInstance().getCatalog().getProject().getOrganization());
				ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
				IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Societe");
				controller.addEqualsConstraint("sys_Title", societeName);
				Collection<IStorageResource> societes = controller.evaluate(definition);
				if (!societes.isEmpty()){				
					societe = societes.iterator().next();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return societe;
	}
	
	private File createFileFromFileCenterInLocalDrive(String filePath,String fileName) {
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
					IAttachment attachment = libraryModule.getAttachment( file, filePathDivided[filePathDivided.length - 1] );
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
	
	public IStorageResource getJouralPaieBySocieteAnneeMois(IStorageResource societe, float annee, String mois) {
		IStorageResource remonteesVariablesPaie = null;
		if(societe != null && annee != -1 && mois != null){
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
