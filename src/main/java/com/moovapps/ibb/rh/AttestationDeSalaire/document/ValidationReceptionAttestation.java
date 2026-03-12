package com.moovapps.ibb.rh.AttestationDeSalaire.document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.moovapps.ibb.rh.AttestationDeSalaire.document.ButtonsHtml.Generator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ValidationReceptionAttestation extends BaseDocumentExtension {
	
	ILibraryModule libraryModule = null;
	IContext sysContext = null;
	
	@Override
	public boolean onAfterLoad() {
		libraryModule = Modules.getLibraryModule();
		sysContext = getWorkflowModule().getSysadminContext();

		new Generator().generer(getWorkflowModule(),getWorkflowInstance(),getResourceController());


		String pattern = "dd/MM/yyyy";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String  dateGenerationTEXT = formatter.format(new Date());
		getWorkflowInstance().setValue("DateGenerationAttestation",dateGenerationTEXT);
		getWorkflowInstance().save(getWorkflowModule().getSysadminContext());

		return super.onAfterLoad();
	}

	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("Action5")){
			// onValiderReception();
		}
		return super.onBeforeSubmit(action);
	}
	
	private void onValiderReception() {
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		ILibrary library = getLibrary("DOC CENTER");
		if(library != null){
			if(getWorkflowInstance().getValue("Demandeur2") != null){			
				IUser demandeur = (IUser)getWorkflowInstance().getValue("Demandeur2");
				if(getWorkflowInstance().getValue("DocumentGenere") != null && ((ArrayList<IAttachment>)getWorkflowInstance().getValue("DocumentGenere")).size() > 0){
					IAttachment file = ((ArrayList<IAttachment>)getWorkflowInstance().getValue("DocumentGenere")).get(0);
					String path = "Attestations administratives/";
					path += (String)getWorkflowInstance().getValue("AttestationDemande") + "/";
					path += df.format(new Date()) + "/";
					path += file.getName();
					String[] folders = path.split("/");
					try {
						IFolder folder = libraryModule.getFolder(sysContext, library, path);
						for (int i = 0; i < folders.length - 1 ; i++) {
							if(!folders[i].equals("")){					
								folder = getOrCreateFolder(libraryModule, library, folder, folders[i], demandeur);
							}
						}
						handleFile(folder, library, file, demandeur);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private ILibrary getLibrary(String libraryName) {
		try {
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			return libraryModule.getLibrary(sysContext, organization, libraryName);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private IFolder getOrCreateFolder(ILibraryModule libraryModule, ILibrary library, IFolder folder, String folderName, IUser demandeur) {
		IFolder packPaieFolder = null;
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
				addSecurityToFolder(packPaieFolder, demandeur);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return packPaieFolder;
	}
	
	private void addSecurityToFolder(IFolder folder, IUser demandeur) {
		try {
			if(folder != null){			
				ISecurityController securityController = libraryModule.getSecurityController(folder);
				if(!securityController.checkPermission(demandeur, new Object[] { null, "read" })){					
					securityController.addPermission(demandeur, new Object[] { null, "read" });
				}
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
	
	private void handleFile(IFolder folder, ILibrary library, IAttachment file, IUser demandeur) {
		SimpleDateFormat dfh = new SimpleDateFormat("HH_mm");
		IFile docFile = getFile(folder, library, file);
		String fileName = (String)demandeur.getExtendedAttributes().getValue("Matricule") + "-" + dfh.format(new Date()) + "-" + file.getName() ;
		if(docFile != null){
			docFile.delete(sysContext);
		}
		try {
			if(folder != null){				
				docFile = libraryModule.createFile(sysContext, folder, fileName, "", file.getContent());
			}else{
				docFile = libraryModule.createFile(sysContext, library, fileName, "", file.getContent());
			}		
			if (docFile != null) {
				ISecurityController securityController = libraryModule.getSecurityController(docFile);
				securityController.breakInheritance(ISecurityController.EVERYONE, new Object[] { null, "read" } );		
				securityController.addPermission(demandeur, new Object[] { null, "read" });

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private IFile getFile(IFolder folder, ILibrary library, IAttachment file) {
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
}
