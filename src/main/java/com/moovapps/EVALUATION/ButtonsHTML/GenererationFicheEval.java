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

public class GenererationFicheEval {
    IWorkflowModule workflowModule = Modules.getWorkflowModule();
    IDirectoryModule directoryModule = Modules.getDirectoryModule();
    public void generer(IWorkflowInstance workflowInstance, IResourceController resourceController){
        try{
            String etatEvaluation = (String) workflowInstance.getValue("EtatDEvaluation");
            IUser N1 = (IUser) workflowInstance.getValue("ResponsableHierarchique");
            IUser connectedUser = Modules.getWorkflowModule().getLoggedOnUser();
            if(!N1.getId().toString().equals(connectedUser.getId().toString()) || (!etatEvaluation.equals("Auto-évaluation réalisée") && !etatEvaluation.equals("Entretien programmé")) ){
                return;
            }
            workflowInstance.setValue("DateGenerationFiche",new Date());
            workflowInstance.save("DateGenerationFiche");
            GenerateWordFile generateWordFile = new GenerateWordFile();
            IContext context = workflowModule.getSysadminContext();
          //  HashMap<String,String> groupeFiles = new HashMap<>();
           // groupeFiles.put("Collaborateurs_Eval","Fiche_Evaluation Personnel administratif_VF.docx");
           // groupeFiles.put("GroupeEnseignants","Fiche_Evaluation Enseignant_CL_VF.docx");
           // groupeFiles.put("GroupePersonnelDeDirection","Profil de direction.docx");
           // groupeFiles.put("Documentaliste","Fiche_Evaluation Documentaliste CL_VF.DOCX");
           // groupeFiles.put("SupportPedagogique","Fiche_Evaluation Conséiller pédagogique.docx");
           // groupeFiles.put("CPE","Fiche_Evaluation_CPE_VF.docx");
           // String groupe = workflowInstance.getValue("GroupeHiearchique2")!=null? (String) workflowInstance.getValue("GroupeHiearchique2"):"";
            String fileName ="Fiches/Evaluation annuelle - fiche à générer.docx";
            String evalCode = (String) workflowInstance.getValue("sys_Reference");
            String collabName =workflowInstance.getValue("CollaborateurEval")!=null? ((IUser) workflowInstance.getValue("CollaborateurEval")).getFullName():"";
            File file =  createFileFromFileCenterInLocalDrive(fileName,evalCode+"_"+collabName,workflowModule,directoryModule,resourceController);
            ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
            IAttachment attachement =  directoryModule.createAttachment(context, file);
            tmpAttachementCollection.add(attachement);

            workflowInstance.setValue("FicheDEvaluation", tmpAttachementCollection);

            InputStream inputStream = generateWordFile.valorization(workflowModule, workflowInstance, attachement);

            Document document = new Document(inputStream);
            document.save("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf", SaveFormat.PDF);
            workflowInstance.setValue("FicheDEvaluation", null);
            workflowModule.addAttachment( workflowInstance, "FicheDEvaluation", new File("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf"));
            workflowInstance.save("FicheDEvaluation");
            File MyFile = new File("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf");
            MyFile.delete();
        }catch(Exception e){
            e.getCause();
            return ;

        }


    }
    public void genererCollaborateur(IWorkflowInstance workflowInstance, IResourceController resourceController){
        try{
            String etatEvaluation = (String) workflowInstance.getValue("EtatDEvaluation");
            IUser collaborateur = (IUser) workflowInstance.getValue("CollaborateurEval");
            IUser connectedUser = Modules.getWorkflowModule().getLoggedOnUser();
            if(!collaborateur.getId().toString().equals(connectedUser.getId().toString()) || !etatEvaluation.equals("A débuter")){
                return;
            }
            workflowInstance.setValue("DateGenerationFiche",new Date());
            workflowInstance.save("DateGenerationFiche");
            GenerateWordFile generateWordFile = new GenerateWordFile();
            IContext context = workflowModule.getSysadminContext();
            //  HashMap<String,String> groupeFiles = new HashMap<>();
            // groupeFiles.put("Collaborateurs_Eval","Fiche_Evaluation Personnel administratif_VF.docx");
            // groupeFiles.put("GroupeEnseignants","Fiche_Evaluation Enseignant_CL_VF.docx");
            // groupeFiles.put("GroupePersonnelDeDirection","Profil de direction.docx");
            // groupeFiles.put("Documentaliste","Fiche_Evaluation Documentaliste CL_VF.DOCX");
            // groupeFiles.put("SupportPedagogique","Fiche_Evaluation Conséiller pédagogique.docx");
            // groupeFiles.put("CPE","Fiche_Evaluation_CPE_VF.docx");
            // String groupe = workflowInstance.getValue("GroupeHiearchique2")!=null? (String) workflowInstance.getValue("GroupeHiearchique2"):"";
            String fileName ="Fiches/Evaluation annuelle - fiche à générer.docx";
            String evalCode = (String) workflowInstance.getValue("sys_Reference");
            String collabName =workflowInstance.getValue("CollaborateurEval")!=null? ((IUser) workflowInstance.getValue("CollaborateurEval")).getFullName():"";
            File file =  createFileFromFileCenterInLocalDrive(fileName,evalCode+"_"+collabName,workflowModule,directoryModule,resourceController);
            ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
            IAttachment attachement =  directoryModule.createAttachment(context, file);
            tmpAttachementCollection.add(attachement);

            workflowInstance.setValue("FicheEvaluationCollaborateur", tmpAttachementCollection);

            InputStream inputStream = generateWordFile.valorization(workflowModule, workflowInstance, attachement);

            Document document = new Document(inputStream);
            document.save("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf", SaveFormat.PDF);
            workflowInstance.setValue("FicheEvaluationCollaborateur", null);
            workflowModule.addAttachment( workflowInstance, "FicheEvaluationCollaborateur", new File("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf"));
            workflowInstance.save("FicheEvaluationCollaborateur");

            File MyFile = new File("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf");
            MyFile.delete();
        }catch(Exception e){
            e.getCause();
            return ;

        }


    }



    public void genererN2(IWorkflowInstance workflowInstance, IResourceController resourceController){
        try{

            IUser N2 = (IUser) workflowInstance.getValue("NPlus2");
            IUser connectedUser = Modules.getWorkflowModule().getLoggedOnUser();
            if(!N2.getId().toString().equals(connectedUser.getId().toString())){
                return;
            }
            workflowInstance.setValue("DateGenerationFiche",new Date());
            workflowInstance.save("DateGenerationFiche");
            GenerateWordFile generateWordFile = new GenerateWordFile();
            IContext context = workflowModule.getSysadminContext();
            //  HashMap<String,String> groupeFiles = new HashMap<>();
            // groupeFiles.put("Collaborateurs_Eval","Fiche_Evaluation Personnel administratif_VF.docx");
            // groupeFiles.put("GroupeEnseignants","Fiche_Evaluation Enseignant_CL_VF.docx");
            // groupeFiles.put("GroupePersonnelDeDirection","Profil de direction.docx");
            // groupeFiles.put("Documentaliste","Fiche_Evaluation Documentaliste CL_VF.DOCX");
            // groupeFiles.put("SupportPedagogique","Fiche_Evaluation Conséiller pédagogique.docx");
            // groupeFiles.put("CPE","Fiche_Evaluation_CPE_VF.docx");
            // String groupe = workflowInstance.getValue("GroupeHiearchique2")!=null? (String) workflowInstance.getValue("GroupeHiearchique2"):"";
            String fileName ="Fiches/Evaluation annuelle - fiche à générer.docx";
            String evalCode = (String) workflowInstance.getValue("sys_Reference");
            String collabName =workflowInstance.getValue("CollaborateurEval")!=null? ((IUser) workflowInstance.getValue("CollaborateurEval")).getFullName():"";
            File file =  createFileFromFileCenterInLocalDrive(fileName,evalCode+"_"+collabName,workflowModule,directoryModule,resourceController);
            ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
            IAttachment attachement =  directoryModule.createAttachment(context, file);
            tmpAttachementCollection.add(attachement);
            workflowInstance.setValue("FicheDEvaluationRespN2", tmpAttachementCollection);
            InputStream inputStream = generateWordFile.valorization(workflowModule, workflowInstance, attachement);
            Document document = new Document(inputStream);
            document.save("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf", SaveFormat.PDF);
            workflowInstance.setValue("FicheDEvaluationRespN2", null);
            workflowModule.addAttachment( workflowInstance, "FicheDEvaluationRespN2", new File("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf"));
            workflowInstance.save("FicheDEvaluationRespN2");
            ArrayList<IAttachment> fiche = (ArrayList<IAttachment>) workflowInstance.getValue("FicheDEvaluationRespN2");
            //workflowModule.addAttachment( workflowInstance, "FicheEvaluationCollaborateur", new File("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf"));
            //workflowModule.addAttachment( workflowInstance, "FicheDEvaluation", new File("C:\\import\\EVAL"+ "\\" + attachement.getName() + ".pdf"));

            workflowInstance.setValue("FicheEvaluationCollaborateur",duplicatePieceJointe(fiche));
            workflowInstance.setValue("FicheDEvaluation",duplicatePieceJointe(fiche));
            workflowInstance.save("FicheDEvaluation");
            workflowInstance.save("FicheEvaluationCollaborateur");
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
