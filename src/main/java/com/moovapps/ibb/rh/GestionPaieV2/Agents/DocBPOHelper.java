package com.moovapps.ibb.rh.GestionPaieV2.Agents;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.Logger;

import java.io.File;
import java.io.FileInputStream;

public class DocBPOHelper {
	
	private IWorkflowModule workflowModule = null;
	private IDirectoryModule directoryModule = null;
	private Logger logger = null;
	
	public DocBPOHelper(IWorkflowModule workflowModule, IDirectoryModule directoryModule, Logger LOGGER) {
		 this.workflowModule = workflowModule;
		 this.directoryModule  = directoryModule;
		 this.logger  = LOGGER;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public boolean AddFileToBPODoc(File attachement,String path) {
		try {
			ILibraryModule libraryModule = Modules.getLibraryModule();
			IContext context = workflowModule.getSysadminContext();
			IOrganization organization = directoryModule.getOrganization(context, "DefaultOrganization");
			ILibrary library = libraryModule.getLibrary(context, organization, "Doc BPO");
			
			String[] folders = path.split("/");
//			String folderPath = "";
			IFolder packPaieFolder = libraryModule.getFolder(context, library, path);
			
			for (int i = 0; i < folders.length - 1 ; i++) {
//				folderPath += (i > 1 ? "/" : "") + folders[i];
				if(!folders[i].equals("")){					
					packPaieFolder = getOrCreateFolder(libraryModule, context, library, packPaieFolder, folders[i]);
				}
			}
			
			FileInputStream file = new FileInputStream(attachement);
			IFile insertedFile = null;
			if(packPaieFolder == null){
				insertedFile = libraryModule.getFile(context, library, attachement.getName());
			}else{				
				insertedFile = libraryModule.getFile(context, packPaieFolder, attachement.getName());
			}
			if (insertedFile == null) {
			} else {
				insertedFile.delete(context);
				logger.error("DELETE: "+insertedFile.getPath());
			}
			if(packPaieFolder != null){				
				insertedFile = libraryModule.createFile(context, packPaieFolder, attachement.getName(), "", file);
			}else{
				insertedFile = libraryModule.createFile(context, library, attachement.getName(), "", file);
			}
			
			//VDocManagers
			if(insertedFile != null){				
				ISecurityController securityController = libraryModule.getSecurityController(insertedFile);
				securityController.breakInheritance(ISecurityController.EVERYONE, new Object[] { null, "read" } );
				IOrganization org = directoryModule.getOrganization(context, "BPOGroupes");
				if(org != null){					
					IGroup group = directoryModule.getGroup(context, org, "BPO");
					securityController.addPermission(group, new Object[] { null, "read" });
					IFolder tmpFolder = libraryModule.getFolder(context, library, path);
					addPermissionToFolders(libraryModule, context, library, tmpFolder, folders, group);
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private IFolder getFolder(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String folderName) {
		IFolder packPaieFolder = null;
		try {
			if(folder != null){				
				packPaieFolder = libraryModule.getFolder(context, folder, folderName);
			} else {
				packPaieFolder = libraryModule.getFolder(context, library, folderName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return packPaieFolder;
	}
	
	private void addPermissionToFolders(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String[] folders, IGroup group) {
		for (int i = 0; i < folders.length - 1 ; i++) {
			try {
//			folderPath += (i > 1 ? "/" : "") + folders[i];
				if(!folders[i].equals("")){					
					IFolder packPaieFolder = getFolder(libraryModule, context, library, folder, folders[i]);
					if(packPaieFolder != null){						
						ISecurityController securityController = libraryModule.getSecurityController(packPaieFolder);
						securityController.addPermission(group, new Object[] { null, "read" });
					}
					folder = packPaieFolder;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private IFolder getOrCreateFolder(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String folderName) {
		IFolder packPaieFolder = null;
		try {
			if(folder != null){				
				packPaieFolder = libraryModule.getFolder(context, folder, folderName);
				if (packPaieFolder == null) {
					packPaieFolder = libraryModule.createFolder(context, folder, folderName);
					breakInheritanceOfNewFolder(libraryModule, packPaieFolder);
				}
			} else {
				packPaieFolder = libraryModule.getFolder(context, library, folderName);
				if (packPaieFolder == null) {
					packPaieFolder = libraryModule.createFolder(context, library, folderName);
					breakInheritanceOfNewFolder(libraryModule, packPaieFolder);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return packPaieFolder;
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
	
}
