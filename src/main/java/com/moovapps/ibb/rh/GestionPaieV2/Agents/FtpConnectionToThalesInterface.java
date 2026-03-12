package com.moovapps.ibb.rh.GestionPaieV2.Agents;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.Logger;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;



public class FtpConnectionToThalesInterface extends BaseAgent {
	
	private IWorkflowModule workflowModule = null;
	private IDirectoryModule directoryModule = null;
	private Logger logger = null;
	public int filesAddedToFileCenter = 0;
	
	List<String> fileToRetreive = Arrays.asList("BP","LP","RC","CNSS","IR","AMO","Mutuelle");
	
	public FtpConnectionToThalesInterface(IWorkflowModule workflowModule, IDirectoryModule directoryModule, Logger LOGGER) {
		 this.workflowModule = workflowModule;
		 this.directoryModule  = directoryModule;
		 this.logger  = LOGGER;
	}
	
	public FtpConnectionToThalesInterface() {
		 this.workflowModule = Modules.getWorkflowModule();
		 this.directoryModule  = Modules.getDirectoryModule();
		 this.logger = LOGGER;
	}

	FTPClient ftpClient = new FTPClient();
	Date dateExecution = new Date();
	SimpleDateFormat simpleFormat = new SimpleDateFormat("dd-MM-yyyy");
	
	private void showServerReply(String path, FTPClient ftpClient) {
	    String[] replies = ftpClient.getReplyStrings();
	    if (replies != null && replies.length > 0) {
	        for (String aReply : replies) {
	        	logger.error(path +" : SERVER: " + aReply);
	        }
	    }
	}
	
	@Override
	public void execute() {
		dateExecution = new Date();
//		//Local
//		String server = "192.168.10.157";
//		int port = 21;
//		String user = "userftp";
//		String pass = "123$$";
		
		//IBB Prod
		String server = "10.1.0.5";
		int port = 21;
		String user = "FtpUser";
		String pass = "M@ster0312";
		
		/*String server = "192.168.100.120";
		int port = 21;
		String user = "ftp-user";
		String pass = "1998";*/
		
		//LOCAL FTP
	/*	String server = "localhost";
		int port = 21;
		String user = "Dell";
		String pass = "Capone2019";*/
		
		ftpClient.enterLocalPassiveMode();
		ftpClient.setStrictReplyParsing(false);
//		ftpClient.setBufferSize(1024 * 1024);
		ftpClient.addProtocolCommandListener(new PrintCommandListener(System.out));
//        ftpClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        try {
            ftpClient.connect(server, port);
            showServerReply("execute 1: ", ftpClient);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                LOGGER.error("Operation failed. Server reply code: " + replyCode);
                return;
            }
    		ftpClient.enterLocalPassiveMode();
    		boolean success = ftpClient.login(user, pass);
            showServerReply("execute 2: ", ftpClient);
            if (!success) {
            	LOGGER.error("Could not login to the server");
                return;
            } else {
//            	getVariablesPaieFromFtp();
            	getPackPaieFromFtp();
            	removeAllFoldersInFtp();
            	ftpClient.logout();
            	ftpClient.disconnect();
            	
            	LOGGER.error("DONE");
            }
        } catch (IOException ex) {
        	LOGGER.error("Oops! Something wrong happened");
            ex.printStackTrace();
        }
	}
	HashMap<String,IUser> usersMap = new HashMap<>();
	//
	
	private IUser getUserByMatAndOrganisation(String mat, IOrganization org){
        if(!usersMap.containsKey(mat)){
            IStorageResource user = getUsersRefByMatriculeAndOrganisation(mat, org);
            if(user != null){            	
            	usersMap.put((String)user.getValue("Matricule"),(IUser)user.getValue("Salarie"));
            }
        }
        return usersMap.get(mat);
    }
//
	
	public IStorageResource getUsersRefByMatriculeAndOrganisation(String matricule, IOrganization org) {
		IStorageResource user = null;
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
			IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
			IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",organization);
			ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
			controller.addEqualsConstraint("Matricule", matricule);
			controller.addEqualsConstraint("Organisation", org);
			Collection<IStorageResource> users = controller.evaluate(definition);
			if (!users.isEmpty()){				
				user = users.iterator().next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}
	
//	private IStorageResource getUsersRefByMatricule(String matricule) {
//        try {
//            IContext context = getWorkflowModule().getSysadminContext();
//            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIELCOMMUN", ICatalog.IType.STORAGE);
//            IResourceDefinition resourceDefinition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
//            IViewController viewController = getWorkflowModule().getViewController(context, IResource.class);
////            viewController.addEqualsConstraint("Matricule", matricule);
//            ArrayList<IStorageResource> users = (ArrayList<IStorageResource>) viewController.evaluate(resourceDefinition);
//            if (users != null && !users.isEmpty()) {
//                return users.iterator().next();
//            }
//
//        } catch (WorkflowModuleException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

	private void removeAllFoldersInFtp() {
		FTPFile[] files;
		try {
			files = ftpClient.listFiles("/"/*Paie" + c.get(Calendar.YEAR)*/);
			for (FTPFile ftpFile : files) {
				if(!ftpFile.getName().contains("HISTORIQUE")){
					if(ftpFile.isDirectory()){						
						ftpClient.removeDirectory("/"+ftpFile.getName());
						showServerReply("removeAllFoldersInFtp 1: "+ftpFile.getName(), ftpClient);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	private void getVariablesPaieFromFtp() {
//		OutputStream outputStream = null;
//		try {
//			FTPFile[] files = ftpClient.listFiles("/VariablePaie");
//			for (FTPFile ftpFile : files) {
//				String remoteFile = "/VariablePaie/" + ftpFile.getName();
//				File downloadFile = new File("C:/TEST/" + ftpFile.getName());
//				outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
//				boolean yesNo = ftpClient.retrieveFile(remoteFile, outputStream);
//				if(addElementVariablePaieFileToDocCenter(downloadFile)){
//					outputStream.close();
//					archiveElementVariablePaieFile(ftpFile);
//				}
//				downloadFile.delete();
//				downloadFile.deleteOnExit();
//			}
//		} catch (Exception e) {
//			try {
//				outputStream.close();
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//		}
//	}
	
//	private boolean addElementVariablePaieFileToDocCenter(File attachement) {
//		try {
//			ILibraryModule libraryModule = Modules.getLibraryModule();
//			IContext context = getWorkflowModule().getSysadminContext();
//			IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
//			ILibrary library = libraryModule.getLibrary(context, organization, "DOSSIER DRH");
//			IFolder elementsVariablesPaieFolder = libraryModule.getFolder(context, library, "Elements variables de la paie");
//			if (elementsVariablesPaieFolder == null) {
//				elementsVariablesPaieFolder = libraryModule.createFolder(context, library, "Elements variables de la paie");
//			}
//			FileInputStream file = new FileInputStream(attachement);
//
//			IFile insertedFile = libraryModule.getFile(context, elementsVariablesPaieFolder, attachement.getName());
//			if (insertedFile == null) {
//				insertedFile = libraryModule.createFile(context, elementsVariablesPaieFolder, attachement.getName(), "", file);
//				libraryModule.beginTransaction();
//				IResourceDefinition elementsVariablesFileDefinition = libraryModule.getResourceDefinition(context, library, "elementsvariablespaie");
//				insertedFile.setDefinition(elementsVariablesFileDefinition);
//				try {					
//					insertedFile.setValue("client", attachement.getName().split("_")[1].split("-")[0]);
//				} catch (Exception e) {
////					insertedFile.setValue("client", attachement.getName());
//				}
//				insertedFile.save(context);
//				libraryModule.commitTransaction();
//			}
//			return true;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return false;
//	}
//	
//	private void archiveElementVariablePaieFile(FTPFile ftpFile) {
//		try {
//			String date = simpleFormat.format(dateExecution);
//
//			if(!ftpClient.changeWorkingDirectory("/HISTORIQUE/VariablesPaie"+date)){				
//				String dirToCreate = "HISTORIQUE";  
//				boolean success = ftpClient.makeDirectory(dirToCreate);
//				showServerReply(ftpClient, LOGGER);
//				dirToCreate = "HISTORIQUE/VariablesPaie"+date;
//				success = ftpClient.makeDirectory(dirToCreate);
//				showServerReply(ftpClient, LOGGER);
//				ftpClient.rename("/VariablePaie/" + ftpFile.getName(), "/HISTORIQUE/VariablesPaie" + date + "/" + ftpFile.getName());
//				showServerReply(ftpClient, LOGGER);
//			} else {
//				ftpClient.rename("/VariablePaie/" + ftpFile.getName(), "/HISTORIQUE/VariablesPaie" + date + "/" + ftpFile.getName());
//				showServerReply(ftpClient, LOGGER);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	private void getPackPaieFromFtp() {
		try {
			Calendar c = Calendar.getInstance();
			LOGGER.error("Gathering files");
			FTPFile[] files = ftpClient.listFiles("/"/*Paie" + c.get(Calendar.YEAR)*/);
			LOGGER.error("DONE Gathering files");

			for (FTPFile ftpFile : files) {
				filesAddedToFileCenter = 0;
				recursivelyAddFilesToDocCenter(ftpFile, ""/*Paie" + c.get(Calendar.YEAR)*/);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean recursivelyAddFilesToDocCenter(FTPFile ftpFile, String path) {
		if(!path.contains("HISTORIQUE")){			
			OutputStream outputStream = null;
			try {
				String checkPath = path +"/"+ ftpFile.getName();
				FTPFile[] files = ftpClient.listFiles(checkPath);
				if(files.length > 0){				
					for (FTPFile file : files) {
						recursivelyAddFilesToDocCenter(file, checkPath);
						if(!checkPath.contains("HISTORIQUE")){
							if(file.isDirectory() && file.getSize() == 0){
								ftpClient.removeDirectory(checkPath+"/"+file.getName());
								showServerReply("recursivelyAddFilesToDocCenter 1: "+checkPath+"/"+file.getName(), ftpClient);
							}
						}		
					}			
				} else {
					if(ftpFile.isFile()){
						String remoteFile = path;
						String fileName = ftpFile.getName();
						if(fileName.startsWith("BP") || fileName.startsWith("LP")|| fileName.startsWith("RC") || fileName.startsWith("CNSS")
								|| fileName.startsWith("IR") || fileName.startsWith("AMO") || fileName.startsWith("Mutuelle") || fileName.startsWith("Retraite")){
							File downloadFile = new File("C:/TEST/"+ ftpFile.getName());
							downloadFile.getParentFile().mkdirs();
							outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
							boolean yesNo = ftpClient.retrieveFile(remoteFile, outputStream);
							filesAddedToFileCenter++;
//							archivePackPaieFile(ftpFile, path);
							outputStream.close();
							addPackPaieFileToDocCenter(downloadFile, path);
							downloadFile.delete();
							downloadFile.deleteOnExit();
						}else {
							ftpClient.deleteFile(path);
							return false;
						}
						
						return true;
					}
				}
			} catch (Exception e) {
				try {
					if(outputStream != null){					
						outputStream.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
		return false;
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	private boolean addPackPaieFileToDocCenter(File attachement,String path) {
		try {
			
		//	 new DocBPOHelper(this.workflowModule,this.directoryModule,this.LOGGER).AddFileToBPODoc(attachement, path);
			if(attachement.getName().split("-").length > 0){
				String clientName = attachement.getName().split("-")[2];
				String mat = attachement.getName().split("-")[1];
				String obj = attachement.getName().split("-")[0];
				IContext context = workflowModule.getSysadminContext();

				IOrganization org = getDirectoryModule().getOrganization(context, fromNameToSysName(clientName));
				if(org!=null){
					ILibraryModule libraryModule = Modules.getLibraryModule();
					IOrganization organization = directoryModule.getOrganization(context, "DefaultOrganization");
					//ILibrary library = libraryModule.getLibrary(context, organization, "DOSSIER DRH");
					ILibrary library = libraryModule.getLibrary(context, organization, "DOC CENTER");
					//
					String[] folders = path.split("/");
					IFolder packPaieFolder = libraryModule.getFolder(context, library, path);
					
					for (int i = 0; i < folders.length - 1 ; i++) {
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
//				insertedFile = libraryModule.createFile(context, packPaieFolder, attachement.getName(), "", file);
//				ISecurityController securityController = libraryModule.getSecurityController(insertedFile);
//				securityController.breakInheritance(ISecurityController.EVERYONE, new Object[] { null, "read" } );
//				if(attachement.getName().split("-").length > 0){
//					IOrganization org = getDirectoryModule().getOrganization(context, attachement.getName().split("-")[0]);
//					IGroup group = directoryModule.getGroup(context, getDirectoryModule().getOrganization(context, "BPOGroupes"), "RH");
////					Collection<IUser> users = (Collection<IUser>) getDirectoryModule().getUsers(context, org);
////					securityController.addPermission(users, new Object[] { null, "read" });
//					securityController.addPermission(group, IPermissionLevels.READ, IScopeFilters.CHILDREN, org, IScopeFilters.NONE, getDirectoryModule().getLocalization(context, "DefaultLocalization"));
////					securityController.addPermission(group, IPermissionLevels.READ, IPermissionFlags.CommonPermissionFlags.CREATE, org, null,);
//				}
//				archivePackPaieFile(path);
					} else {
						insertedFile.delete(context);
//						insertedFile.deleteFromTrash();
					}
					if(packPaieFolder != null){				
						insertedFile = libraryModule.createFile(context, packPaieFolder, attachement.getName(), "", file);
					}else{
						insertedFile = libraryModule.createFile(context, library, attachement.getName(), "", file);
					}
					
					//VDocManagers
					if(insertedFile != null){
						/*
                */
						ISecurityController securityController = libraryModule.getSecurityController(insertedFile);
						securityController.breakInheritance(ISecurityController.EVERYONE, new Object[] { null, "read" } );
						IGroup group = directoryModule.getGroup(context, org, org.getName()+"RH");
						securityController.addPermission(group, new Object[] { null, "read" });
						IFolder tmpFolder = libraryModule.getFolder(context, library, path);
						addGroupPermissionToFolders(libraryModule, context, library, tmpFolder, folders, group);
						
						
						//String mat=null;
						IUser user = getUserByMatAndOrganisation(mat, org);
		                if(user!=null){
		                    securityController.addPermission(user, new Object[]{null, "read"});
		                    tmpFolder = libraryModule.getFolder(context, library, path);
		                    addUserPermissionToFolders(libraryModule, context, library, tmpFolder, folders, user);
		                }
						org = directoryModule.getOrganization(context, "BPOGroupes");
		                group = directoryModule.getGroup(context, org, "BPO");
						securityController.addPermission(group, new Object[] { null, "read" });
						tmpFolder = libraryModule.getFolder(context, library, path);
						addPermissionToBPOGroup(libraryModule, context, library, tmpFolder, folders, group);
							
					
//						return true;
					}
				}
			}
			archivePackPaieFile(path);
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
	private void addPermissionToBPOGroup(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String[] folders, IGroup group){
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
	private void addGroupPermissionToFolders(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String[] folders, IGroup group) {
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
	private void addUserPermissionToFolders(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String[] folders, IUser user) {
		for (int i = 0; i < folders.length - 1 ; i++) {
			try {
//			folderPath += (i > 1 ? "/" : "") + folders[i];
				if(!folders[i].equals("")){					
					IFolder packPaieFolder = getFolder(libraryModule, context, library, folder, folders[i]);
					if(packPaieFolder != null){						
						ISecurityController securityController = libraryModule.getSecurityController(packPaieFolder);
						securityController.addPermission(user, new Object[] { null, "read" });
					}
					folder = packPaieFolder;

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String fromNameToSysName(String name) {
		String sysName = "";
		if(name != null){
			name.trim();
			name= org.apache.commons.lang3.StringUtils.stripAccents(name);
			name= name.replaceAll("[^a-zA-Z0-9]", " ");
			String[] splitedName = name.split(" ");
			for (String string : splitedName) {
				if(string != null && !string.equals("") ){					
					string = string.trim();
					sysName += string.substring(0,1).toUpperCase() + (string.length() > 1 ? string.substring(1).toLowerCase() : "");
				}
			}
		}
		return sysName;
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
	
	@SuppressWarnings("unused")
	private void archivePackPaieFile(String path) {
		try {
			String date = simpleFormat.format(dateExecution);
			String pathToCreate = "/HISTORIQUE/" + date + path;
//			String fileName = path.split("/")[path.split("/").length-1];
			if(ftpClient.listFiles(pathToCreate) != null && ftpClient.listFiles(pathToCreate).length > 0){
				ftpClient.deleteFile(pathToCreate);
				showServerReply("archivePackPaieFile 1: "+path, ftpClient);

				ftpClient.rename(path, pathToCreate);
				showServerReply("archivePackPaieFile 2: "+path, ftpClient);
			} else {	
				String[] foldersToCreate = pathToCreate.split("/");
				String folderpath = "";
				for (int i = 1; i < foldersToCreate.length - 1; i++) {
					folderpath += "/" + foldersToCreate[i];
					if(!ftpClient.changeWorkingDirectory(folderpath)){	
						boolean success = ftpClient.makeDirectory(folderpath);
						showServerReply("archivePackPaieFile 3: "+path, ftpClient);
					}
				}
				boolean success = ftpClient.rename(path, pathToCreate);
				showServerReply("archivePackPaieFile 4: "+path, ftpClient);
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

}
