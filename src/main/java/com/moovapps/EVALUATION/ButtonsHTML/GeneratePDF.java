package com.moovapps.EVALUATION.ButtonsHTML;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.vdoc.helpers.GenerateWordFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GeneratePDF {
    IWorkflowModule workflowModule = Modules.getWorkflowModule();
    IDirectoryModule directoryModule = Modules.getDirectoryModule();
    HashMap<String , String> documentStateChampCible = new HashMap<>();
    public void genererCollaborateur(IWorkflowInstance workflowInstance, IResourceController resourceController){
        try{
            documentStateChampCible.put("A débuter","FicheEvaluationCollaborateur");
            documentStateChampCible.put("Auto-évaluation réalisée","FicheDEvaluation");
            documentStateChampCible.put("Entretien programmé","FicheDEvaluation");
            documentStateChampCible.put("Evaluation réceptionnée","FicheDEvaluationRespN2");
            String etatEvaluation = (String) workflowInstance.getValue("EtatDEvaluation");
            String champCible = documentStateChampCible.get(etatEvaluation)!=null?documentStateChampCible.get(etatEvaluation):"";
            if(champCible.equals("")){
                return;
            }
            workflowInstance.setValue("DateGenerationFiche",new Date());
            workflowInstance.save("DateGenerationFiche");
            GenerateWordFile generateWordFile = new GenerateWordFile();
            IContext context = workflowModule.getSysadminContext();

            String fileName ="Fiches/Evaluation annuelle.docx";
            String evalCode = (String) workflowInstance.getValue("sys_Reference");
            String collabName =workflowInstance.getValue("CollaborateurEval")!=null? ((IUser) workflowInstance.getValue("CollaborateurEval")).getFullName():"";
            File file =  createFileFromFileCenterInLocalDrive(fileName,evalCode+"_"+collabName,workflowModule,directoryModule,resourceController);
            ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
            IAttachment attachement =  directoryModule.createAttachment(context, file);
            tmpAttachementCollection.add(attachement);

            workflowInstance.setValue(champCible, tmpAttachementCollection);

            InputStream inputStream = generateWordFile.valorization(workflowModule, workflowInstance, attachement);

            Document document = new Document(inputStream);
            document.save("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf", SaveFormat.PDF);
            workflowInstance.setValue(champCible, null);
            workflowModule.addAttachment( workflowInstance, champCible, new File("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf"));
            workflowInstance.save(champCible);

            File MyFile = new File("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf");
            MyFile.delete();
        }catch(Exception e){
            e.getCause();
            return ;

        }


    }



    public ArrayList<IAttachment> duplicatePieceJointe(ArrayList<IAttachment> attachments) {
        //ArrayList<IAttachment> attachments = (ArrayList<IAttachment>) instance.getValue(key);
        File file = null;
        ArrayList<IAttachment> files = new ArrayList<>();
        if (attachments != null && attachments.size() > 0) {
            for (IAttachment iAttachment : attachments) {
                if (iAttachment != null) {
                    try {
                        file = new File("c://TEST//" + iAttachment.getName());
                        FileUtils.writeByteArrayToFile(file, iAttachment.getContent());
                        IAttachment attachment = Modules.getDirectoryModule().createAttachment(Modules.getWorkflowModule().getSysadminContext(), file);
                        files.add(attachment);
                        file.delete();
                        file.deleteOnExit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return files;
    }

    private File createFileFromFileCenterInLocalDrive(String filePath, String fileName,IWorkflowModule workflowModule,IDirectoryModule directoryModule,IResourceController resourceController) {
        try {
            ILibraryModule libraryModule = Modules.getLibraryModule();
            IContext context = workflowModule.getSysadminContext();
            IOrganization organization = directoryModule.getOrganization(context, "DefaultOrganization");
            ILibrary library = libraryModule.getLibrary(context, organization, "Eval");
            if (library != null) {
//                IFolder models = libraryModule.getFolder(context,library,"Fiches");
                IFile file = libraryModule.getFileByPath(context, library, filePath);//.getFile(context,models,fileName);

                if (file == null) {
                    resourceController.alert("Le fichier de modele n'existe pas dans l'espace documentaire...Vieullez contacter votre administrateur");
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


}
