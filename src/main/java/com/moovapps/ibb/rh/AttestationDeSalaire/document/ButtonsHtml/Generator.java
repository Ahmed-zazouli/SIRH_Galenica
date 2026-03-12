package com.moovapps.ibb.rh.AttestationDeSalaire.document.ButtonsHtml;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.modules.IProjectModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.vdoc.sanaEducation.candidatures.aspose.helper.GenerateWordFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class Generator {

    public void generer(IWorkflowModule iWorkflowModule,   IWorkflowInstance iWorkflowInstance , IResourceController controller){
        iWorkflowInstance.setValue("DocumentGenere", new ArrayList());
        try {

            IStorageResource attestationDemande = (IStorageResource) iWorkflowInstance.getValue("AttestationAdministrative");
            if(attestationDemande==null)return;
            ArrayList<IAttachment> attestation = (ArrayList<IAttachment>) attestationDemande.getValue("AttestationAdministrative") ;

            if (attestation != null) {
                IAttachment iAttachment = attestation.get(0);
                File file = null;
                    try {
                        iAttachment.getName();
                        file = new File("C:\\TEST"+ "\\"  + iAttachment.getName());
                        FileUtils.writeByteArrayToFile(file, iAttachment.getContent());
                        GenerateWordFile generateWordFile = new GenerateWordFile();
                        InputStream inputStream = generateWordFile.valorization(iWorkflowModule, iWorkflowInstance, iAttachment);
                        Document document = new Document(inputStream);
                        IUser demandeur = null;
                        //if (demandeurObject != null) {
                        demandeur = (IUser) iWorkflowInstance.getValue("Demandeur2");
                        //File MyFile = new File("C:\\TEMP" + "\\" + iAttachment.getName() + ".pdf");
                        String attestationTitre = attestationDemande.getValue("sys_Title")+"-"+demandeur.getFullName();
                        document.save("C:\\TEMP" + "\\" + attestationTitre+ ".pdf", SaveFormat.PDF);
                        iWorkflowModule.addAttachment(iWorkflowInstance, "DocumentGenere", new File("C:\\TEMP" + "\\" + attestationTitre + ".pdf"));
                        iWorkflowInstance.save(iWorkflowModule.getSysadminContext());
                        file.delete();
                        file.deleteOnExit();
                        File MyFile = new File("C:\\TEMP" + "\\" + attestationTitre + ".pdf");
                        MyFile.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            } else {
                iWorkflowInstance.setValue("DocumentGenere", null);
                Navigator.getNavigator().getRootNavigator().showAlertBox("le fichier est introuvable");
            }
        } catch (Exception e) {
            Navigator.getNavigator().getRootNavigator().showAlertBox("Un problème est survenu, veuillez contacter votre administrateur");
            e.printStackTrace();
        }
    }
}
