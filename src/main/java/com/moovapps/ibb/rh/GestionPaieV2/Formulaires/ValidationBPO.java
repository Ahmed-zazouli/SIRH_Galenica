package com.moovapps.ibb.rh.GestionPaieV2.Formulaires;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ValidationBPO extends BaseDocumentExtension {
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("ImportationSAGE")){
			onImporterSageClick();
		}
		//return false;
	return super.onBeforeSubmit(action);
	}
	
	private void onImporterSageClick() {
		insertRemonteesPaie();
	}
	
	public boolean insertRemonteesPaie() {
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
			IProject project = getProjectModule().getProject(context,"RemonteVariableDeLaPaie", organization);
			ICatalog catalog = getWorkflowModule().getCatalog(context,"JournalDeLaPaie", ICatalog.IType.STORAGE, project);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "JournalDeLaPaieParSocieteParAnneeParMois");
			IStorageResource storageRessource = getWorkflowModule().createStorageResource(getWorkflowModule().getSysadminContext(), definition, "");
			
			storageRessource.setValue("Societe", getWorkflowInstance().getValue("Societe"));
			storageRessource.setValue("GenerateurDuJournal", getWorkflowInstance().getValue("sys_Creator"));
			storageRessource.setValue("Annee", getWorkflowInstance().getValue("Annee"));
			storageRessource.setValue("Mois", Integer.valueOf((String)getWorkflowInstance().getValue("Mois")));
			
//			duplicateFile(getWorkflowInstance(), storageRessource, "RemonteesVariablesDeLaPaie", "RemonteesVariablesDeLaPaie", null);
			duplicateFile(getWorkflowInstance(), storageRessource, "ComplementsVariablesDeLaPaie", "RemonteesVariablesDeLaPaie", null);
			duplicateFile(getWorkflowInstance(), storageRessource, "NouvellesRecrues", "NouvellesRecrues", null);
			duplicateFile(getWorkflowInstance(), storageRessource, "TableauDAmortissement", "TableauDAmortissement", null);
			
			duplicateFile(getWorkflowInstance(), storageRessource, "PDFRemonteesVariablesDeLaPaie", "PDFRemonteesDeLaPaie", null);
			duplicateFile(getWorkflowInstance(), storageRessource, "PDFNouvellesRecrues", "PDFNouvellesRecrues", null);
			duplicateFile(getWorkflowInstance(), storageRessource, "PDFTableauDAmortissement", "PDFTableauDAmortissement", null);
			
			saveAllFilesInDocCenter();
			
			storageRessource.save(context);
//			return true;
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void saveAllFilesInDocCenter() {
		ILibraryModule libraryModule = Modules.getLibraryModule();
		IContext sysContext = getWorkflowModule().getSysadminContext();
		ILibrary library = getLibrary("DOSSIER DRH");
		IGroup group = null;
		IOrganization org = (IOrganization)((IStorageResource)getWorkflowInstance().getValue("Societe")).getValue("Organisation");
		if(org != null){	
			try {
				group = getDirectoryModule().getGroup(sysContext, org, org.getName()+"RH");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String[] filesFields = {"ComplementsVariablesDeLaPaie","NouvellesRecrues","TableauDAmortissement", "PDFRemonteesVariablesDeLaPaie","PDFNouvellesRecrues","PDFTableauDAmortissement"};
		for (String fileField : filesFields) {		
			if(getWorkflowInstance().getValue(fileField) != null && ((ArrayList<IAttachment>)getWorkflowInstance().getValue(fileField)).size() > 0){
				IAttachment file = ((ArrayList<IAttachment>)getWorkflowInstance().getValue(fileField)).get(0);
				String path = "Eléments variables de la paie/";
				path += (String)((IStorageResource)getWorkflowInstance().getValue("Societe")).getValue("sys_Title") + "/";
				path += (String)getWorkflowInstance().getText("Mois") + "-" + (String)getWorkflowInstance().getValue("Annee") + "/";
				path += file.getName();
				String[] folders = path.split("/");
				
				try {
					IFolder folder = libraryModule.getFolder(sysContext, library, path);
					for (int i = 0; i < folders.length - 1 ; i++) {
						if(!folders[i].equals("")){					
							folder = getOrCreateFolder(libraryModule, library, folder, folders[i], group);
						}
					}
					
					handleFile(folder, library, file, group);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void handleFile(IFolder folder, ILibrary library, IAttachment file, IGroup group) {
		SimpleDateFormat dfh = new SimpleDateFormat("HH_mm");
		IContext sysContext = getWorkflowModule().getSysadminContext();
		ILibraryModule libraryModule = Modules.getLibraryModule();
		IFile docFile = getFile(folder, library, file);
		if(docFile != null){
			docFile.delete(sysContext);
		}
		try {
			if(folder != null){				
				docFile = libraryModule.createFile(sysContext, folder, file.getName(), "", file.getContent());
			}else{
				docFile = libraryModule.createFile(sysContext, library, file.getName(), "", file.getContent());
			}		
			if (docFile != null) {
				ISecurityController securityController = libraryModule.getSecurityController(docFile);
				securityController.breakInheritance(ISecurityController.EVERYONE, new Object[] { null, "read" } );		
				securityController.addPermission(group, new Object[] { null, "read" });

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private IFile getFile(IFolder folder, ILibrary library, IAttachment file) {
		ILibraryModule libraryModule = Modules.getLibraryModule();
		IContext sysContext = getWorkflowModule().getSysadminContext();
		try {
			IFile insertedFile = null;
			if(folder == null){
				insertedFile = libraryModule.getFile(sysContext, library, file.getName());
			}else{				
				insertedFile = libraryModule.getFile(sysContext, folder, file.getName());
			}			
			return insertedFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private IFolder getOrCreateFolder(ILibraryModule libraryModule, ILibrary library, IFolder folder, String folderName, IGroup group) {
		IFolder packPaieFolder = null;
		IContext sysContext = getWorkflowModule().getSysadminContext();
		try {
			if(folder != null){				
				packPaieFolder = libraryModule.getFolder(sysContext, folder, folderName);
				if (packPaieFolder == null) {
					packPaieFolder = libraryModule.createFolder(sysContext, folder, folderName);
					breakInheritanceOfNewFolder(libraryModule, packPaieFolder);
				}
			} else {
				packPaieFolder = libraryModule.getFolder(sysContext, library, folderName);
				if (packPaieFolder == null) {
					packPaieFolder = libraryModule.createFolder(sysContext, library, folderName);
					breakInheritanceOfNewFolder(libraryModule, packPaieFolder);
				}
			}
			if(packPaieFolder != null){
				addSecurityToFolder(packPaieFolder, group);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return packPaieFolder;
	}
	
	private void addSecurityToFolder(IFolder folder, IGroup group) {
		ILibraryModule libraryModule = Modules.getLibraryModule();
		try {
			if(folder != null){			
				ISecurityController securityController = libraryModule.getSecurityController(folder);					
				securityController.addPermission(group, new Object[] { null, "read" });
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void breakInheritanceOfNewFolder(ILibraryModule libraryModule, IFolder folder) {
		if (folder != null) {
			try {
				ISecurityController securityController = libraryModule.getSecurityController(folder); 
				securityController.breakInheritance(ISecurityController.EVERYONE, new Object[] { libraryModule.findNativeClass(IFolder.class), "read" } );
				securityController.breakInheritance(ISecurityController.EVERYONE, new Object[] { libraryModule.findNativeClass(IFile.class), "read" } );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private ILibrary getLibrary(String libraryName) {
		ILibraryModule libraryModule = Modules.getLibraryModule();
		IContext sysContext = getWorkflowModule().getSysadminContext();
		try {
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			return libraryModule.getLibrary(sysContext, organization, libraryName);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
	
}

