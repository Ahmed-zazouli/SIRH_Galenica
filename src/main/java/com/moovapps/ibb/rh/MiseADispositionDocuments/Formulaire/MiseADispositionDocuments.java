package com.moovapps.ibb.rh.MiseADispositionDocuments.Formulaire;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MiseADispositionDocuments extends BaseDocumentExtension {

    SimpleDateFormat yearDF = new SimpleDateFormat("yyyy");
    SimpleDateFormat dayMonthDF = new SimpleDateFormat("MM");

    @Override
    public boolean onAfterLoad() {
        return super.onAfterLoad();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        super.onPropertyChanged(property);
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        extractDocuments();
        AddFilesToDocCenter();
        deleteDirectory(new File("C:\\Temp_Moovapps\\MDDP"));

//        return false;
        return super.onBeforeSubmit(action);
    }

    private void extractZipFile(String zipFilePath, String destDirectory) throws IOException {

        try {
            try (ZipArchiveInputStream zipFile2 = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFilePath)), "UTF-8", true)) {
                ArchiveEntry entry;
                while ((entry = zipFile2.getNextEntry()) != null) {
                    try {
                        String entryName = entry.getName();
                        System.out.println(entryName);
                        File entryDestination = new File(destDirectory, entryName);
                        if (entry.isDirectory()) {
                            entryDestination.mkdirs();
                        } else {
                            entryDestination.getParentFile().mkdirs();
                            try (OutputStream out = new FileOutputStream(entryDestination)) {
                                IOUtils.copy(zipFile2, out);
                                out.close();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            new File(zipFilePath).delete();
            new File(zipFilePath).deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }






        /*byte[] buffer = new byte[1024];

        // Create output directory if it doesn't exist
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        // Open zip file for reading
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));

        // Iterate over each entry in the zip file
        ZipEntry zipEntry = zipIn.getNextEntry();
        while (zipEntry != null) {
            String filePath = destDirectory + File.separator + zipEntry.getName();
            if (!zipEntry.isDirectory()) {
                // Create parent directories for the file if they don't exist
                new File(filePath).getParentFile().mkdirs();

                // Extract the file
                FileOutputStream fos = new FileOutputStream(filePath);
                int len;
                while ((len = zipIn.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            } else {
                // Create directory if it doesn't exist
                new File(filePath).mkdirs();
            }
            zipEntry = zipIn.getNextEntry();
        }

        zipIn.closeEntry();
        zipIn.close();*/
    }

    private void extractDocuments() {
        ArrayList<IAttachment> zipFile = (ArrayList<IAttachment>) getWorkflowInstance().getValue("DocumentsZip");

        if (zipFile.size() < 0 || zipFile == null) {
            return;// false;
        }
        for (IAttachment attachment : zipFile) {

            try {
                String fileName = attachment.getName();

                File path = new File("C:\\Temp_Moovapps\\MDDP\\" + fileName);
                FileUtils.writeByteArrayToFile(path, attachment.getContent());
                File theTargetFolder = new File("C:\\Temp_Moovapps\\MDDP\\Documents");
                extractZipFile(path.getAbsolutePath(), theTargetFolder.getAbsolutePath());
                /*try {
                    try (java.util.zip.ZipFile zipFile2 = new ZipFile(path)) {
                        Enumeration<? extends ZipEntry> entries = zipFile2.entries();
                        while (entries.hasMoreElements()) {

                            try {
                                ZipEntry entry = entries.nextElement();
                                System.out.println(entry.getName());
                                File entryDestination = new File(theTargetFolder, entry.getName());
                                if (entry.isDirectory()) {
                                    entryDestination.mkdirs();
                                } else {
                                    entryDestination.getParentFile().mkdirs();
                                    try (InputStream in = zipFile2.getInputStream(entry);
                                         OutputStream out = new FileOutputStream(entryDestination)) {
                                        IOUtils.copy(in, out);
                                        in.close();
                                        out.close();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    path.delete();
                    path.deleteOnExit();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void processFiles(File[] files) {
        if(files==null)return;
        for (File file : files) {
            if (file.isFile()) {
                addPackPaieFileToDocCenter(file);
            } else if (file.isDirectory()) {
                // If it's a directory, recursively process the files within it
                File[] subFiles = file.listFiles();
                if (subFiles != null) {
                    processFiles(subFiles);
                }
            }
        }
    }

    private void AddFilesToDocCenter() {
        File directory = new File("C:\\Temp_Moovapps\\MDDP\\Documents");

        File[] files = directory.listFiles();

        processFiles(files);
    }

    private void addPackPaieFileToDocCenter(File attachement) {
        try {
            if (attachement.getName().split("_").length > 0) {
                String mat = attachement.getName().split("_")[0];
                String obj = (String) ((IStorageResource) getWorkflowInstance().getValue("TypeDesDocuments")).getValue("sys_Title");
                IContext context = getWorkflowModule().getSysadminContext();
                IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
                IOrganization org = (IOrganization) societe.getValue("Organisation");
                // String path = societe.getValue("sys_Title") + "/" + yearDF.format(new Date()) + "/" + dayMonthDF.format(new Date()) + "/" + obj;
                String path = societe.getValue("sys_Title") + "/" + getWorkflowInstance().getValue("Annee") + "/" + getWorkflowInstance().getValue("Mois") + "/" + obj;
                if (org != null) {
                    ILibraryModule libraryModule = Modules.getLibraryModule();
                    IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                    ILibrary library = libraryModule.getLibrary(context, organization, "DOC CENTER");
                    String[] folders = path.split("/");
                    IFolder packPaieFolder = libraryModule.getFolder(context, library, path);

                    for (int i = 0; i < folders.length; i++) {
                        if (!folders[i].equals("")) {
                            packPaieFolder = getOrCreateFolder(libraryModule, context, library, packPaieFolder, folders[i]);
                        }
                    }

                    FileInputStream file = new FileInputStream(attachement);
                    IFile insertedFile = null;
                    if (packPaieFolder == null) {
                        insertedFile = libraryModule.getFile(context, library, attachement.getName());
                    } else {
                        insertedFile = libraryModule.getFile(context, packPaieFolder, attachement.getName());
                    }
                    if (insertedFile == null) {

                    } else {
                        insertedFile.delete(context);
                    }
                    if (packPaieFolder != null) {
                        insertedFile = libraryModule.createFile(context, packPaieFolder, attachement.getName(), "", file);
                    } else {
                        insertedFile = libraryModule.createFile(context, library, attachement.getName(), "", file);
                    }

                    if (insertedFile != null) {
                        ISecurityController securityController = libraryModule.getSecurityController(insertedFile);
                        securityController.breakInheritance(ISecurityController.EVERYONE, new Object[]{null, "read"});
                        IGroup group = getDirectoryModule().getGroup(context, org, org.getName() + "RH");
                        securityController.addPermission(group, new Object[]{null, "read"});
                        IFolder tmpFolder = libraryModule.getFolder(context, library, path);
                        addGroupPermissionToFolders(libraryModule, context, library, tmpFolder, folders, group);

                        IUser user = getUserByMatAndOrganisation(mat, org);
                        if (user != null) {
                            securityController.addPermission(user, new Object[]{null, "read"});
                            tmpFolder = libraryModule.getFolder(context, library, path);
                            addUserPermissionToFolders(libraryModule, context, library, tmpFolder, folders, user);
                        }
                        org = getDirectoryModule().getOrganization(context, "BPOGroupes");
                        group = getDirectoryModule().getGroup(context, org, "BPO");
                        securityController.addPermission(group, new Object[]{null, "read"});
                        securityController.addPermission(group, new Object[]{insertedFile.getNativeObject().getClass(), "read,write,grant"});

                        tmpFolder = libraryModule.getFolder(context, library, path);
                        addPermissionToBPOGroup(libraryModule, context, library, tmpFolder, folders, group);
                        securityController.addPermission(group, new Object[]{insertedFile.getNativeObject().getClass(), "read,write,grant"});
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IUser getUserByMatAndOrganisation(String mat, IOrganization org) {
        IStorageResource user = getUsersRefByMatriculeAndOrganisation(mat, org);
        return user != null && user.getValue("Salarie") != null ? (IUser) user.getValue("Salarie") : null;
    }
//

    public IStorageResource getUsersRefByMatriculeAndOrganisation(String matricule, IOrganization org) {
        IStorageResource user = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", 4, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            controller.addEqualsConstraint("Matricule", matricule);
            controller.addEqualsConstraint("Organisation", org);
            Collection<IStorageResource> users = controller.evaluate(definition);
            if (!users.isEmpty()) {
                user = users.iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    private IFolder getFolder(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String folderName) {
        IFolder packPaieFolder = null;
        try {
            if (folder != null) {
                packPaieFolder = libraryModule.getFolder(context, folder, folderName);
            } else {
                packPaieFolder = libraryModule.getFolder(context, library, folderName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packPaieFolder;
    }

    private void addPermissionToBPOGroup(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String[] folders, IGroup group) {
        for (int i = 0; i < folders.length; i++) {
            try {
//			folderPath += (i > 1 ? "/" : "") + folders[i];
                if (!folders[i].equals("")) {
                    IFolder packPaieFolder = getFolder(libraryModule, context, library, folder, folders[i]);
                    if (packPaieFolder != null) {
                        ISecurityController securityController = libraryModule.getSecurityController(packPaieFolder);
                        securityController.addPermission(group, new Object[]{null, "read"});
                        securityController.addPermission(group, new Object[]{packPaieFolder.getNativeObject().getClass(), "read,write,grant"});
                    }
                    folder = packPaieFolder;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addGroupPermissionToFolders(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String[] folders, IGroup group) {
        for (int i = 0; i < folders.length; i++) {
            try {
//			folderPath += (i > 1 ? "/" : "") + folders[i];
                if (!folders[i].equals("")) {
                    IFolder packPaieFolder = getFolder(libraryModule, context, library, folder, folders[i]);
                    if (packPaieFolder != null) {
                        ISecurityController securityController = libraryModule.getSecurityController(packPaieFolder);
                        securityController.addPermission(group, new Object[]{null, "read"});
                    }
                    folder = packPaieFolder;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addUserPermissionToFolders(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String[] folders, IUser user) {
        for (int i = 0; i < folders.length; i++) {
            try {
//			folderPath += (i > 1 ? "/" : "") + folders[i];
                if (!folders[i].equals("")) {
                    IFolder packPaieFolder = getFolder(libraryModule, context, library, folder, folders[i]);
                    if (packPaieFolder != null) {
                        ISecurityController securityController = libraryModule.getSecurityController(packPaieFolder);
                        securityController.addPermission(user, new Object[]{null, "read"});
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
        if (name != null) {
            name.trim();
            name = org.apache.commons.lang3.StringUtils.stripAccents(name);
            name = name.replaceAll("[^a-zA-Z0-9]", " ");
            String[] splitedName = name.split(" ");
            for (String string : splitedName) {
                if (string != null && !string.equals("")) {
                    string = string.trim();
                    sysName += string.substring(0, 1).toUpperCase() + (string.length() > 1 ? string.substring(1).toLowerCase() : "");
                }
            }
        }
        return sysName;
    }

    private IFolder getOrCreateFolder(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String folderName) {
        IFolder packPaieFolder = null;
        try {
            if (folder != null) {
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
                securityController.breakInheritance(ISecurityController.EVERYONE, new Object[]{libraryModule.findNativeClass(IFolder.class), "read"});
                securityController.breakInheritance(ISecurityController.EVERYONE, new Object[]{libraryModule.findNativeClass(IFile.class), "read"});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*private void archivePackPaieFile(String path) {
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
    }*/

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

}
