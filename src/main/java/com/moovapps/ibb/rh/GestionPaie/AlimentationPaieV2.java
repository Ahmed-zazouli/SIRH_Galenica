package com.moovapps.ibb.rh.GestionPaie;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.screens.ExternalScreen;
import com.moovapps.ibb.rh.Cummon.ExcelWriterTest;
import org.apache.commons.io.FileUtils;
import org.apache.turbine.Turbine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

public class AlimentationPaieV2 extends BaseDocumentExtension {
	
	IWorkflowInstance document = null;
	IUser createur = null;
	private boolean IAttachment;
	String fileName = "Paie test.xlsx";

	@Override
	public boolean onAfterLoad() {
		document = getWorkflowInstance();
		createur = (IUser)document.getValue("sys_Creator");
//		setMaquette();
		setSocieteJournalpaie();
		setAnneeMois();
		return super.onAfterLoad();
	}
	
	private void setMaquette() {
		if(document.getValue("Maquette") == null){			
			try 
			{
				ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
				File file = createFileFromFileCenterInLocalDrive("Maquettes/"+fileName);
				IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
				tmpAttachementCollection.add(tmpJournalPaieAttachment);
				document.setValue("Maquette", tmpAttachementCollection);
				file.delete();
				file.deleteOnExit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setSocieteJournalpaie() {
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
		}else if(property.getName().equals("ComplementsVariablesDeLaPaie2")){
			onComplementUpload();
		}
		super.onPropertyChanged(property);
	}
	
	private void onComplementUpload() {
		ArrayList<IAttachment> tmp = (ArrayList<IAttachment>)document.getValue("ComplementsVariablesDeLaPaie2");
		IAttachment complementVariablePaie = null;
		if(tmp != null && tmp.size() > 0){
			complementVariablePaie = tmp.iterator().next();
		}
		if(complementVariablePaie != null){
			try {
				File tmpFile = new File("c://TEST//tmpComplementVariablePaie.xlsx");
				FileUtils.writeByteArrayToFile(tmpFile, complementVariablePaie.getContent());
				mergePaieData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void mergePaieData() {
		File file = createFileFromFileCenterInLocalDrive("Maquettes/Paie test.xlsx");
		if(file != null){
			try {
				new ExcelWriterTest(getWorkflowModule(), getDirectoryModule(), getProjectModule(),document).main(file, fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}				
		}
	}
	
	private void onAnneeMoisChange() {
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
		if(remonteesVariablesPaie != null && remonteesVariablesPaie.getValue("RemonteesVariablesDeLaPaie") != null){
			ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
			document.setValue("JournalGenere", true);
			ArrayList<IAttachment> tmp = (ArrayList<IAttachment>)remonteesVariablesPaie.getValue("RemonteesVariablesDeLaPaie");
			IAttachment journalPaieFile = null;
			if(tmp.size() > 0){
				journalPaieFile = tmp.get(0);
			}
			if(journalPaieFile != null){				
				try 
				{
					File file = null;
					file = new File("c://TEST//" + journalPaieFile.getName());
					FileUtils.writeByteArrayToFile(file, journalPaieFile.getContent());
					IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
					tmpAttachementCollection.add(tmpJournalPaieAttachment);
					document.setValue("RemonteesVariablesDeLaPaie", tmpAttachementCollection);
					file.delete();
					file.deleteOnExit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {		
			document.setValue("JournalGenere", false);
			document.setValue("RemonteesVariablesDeLaPaie", null);
			File file = createFileFromFileCenterInLocalDrive("Maquettes/"+ fileName);
			if(file != null){
				try {
					new ExcelWriterTest(getWorkflowModule(), getDirectoryModule(), getProjectModule(),document).main(file, fileName);
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		}
		document.save(getWorkflowModule().getLoggedOnUserContext());
    	String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort())
    			.concat(Turbine.getContextPath().concat("/easysite/workplace/applications/application-gestion-de-la-paie-0/GestionDeProcessus/edit-document/").concat(document.getId()+""));    

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
	
	private File createFileFromFileCenterInLocalDrive(String filePath) {
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
					File tmpFile = new File("c://TEST//" + filePathDivided[filePathDivided.length - 1]);
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
		IStorageResource journalPaie = null;
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
					journalPaie = journauxPaie.iterator().next();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return journalPaie;
	}
	
}
