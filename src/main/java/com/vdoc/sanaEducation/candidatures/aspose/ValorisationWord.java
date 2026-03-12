package com.vdoc.sanaEducation.candidatures.aspose;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.LibraryModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.modules.IProjectModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.vdoc.sanaEducation.candidatures.aspose.helper.GenerateNumber;
import com.vdoc.sanaEducation.candidatures.aspose.helper.GenerateWordFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public class ValorisationWord extends BaseDocumentExtension {
    /**
     *
     */
    private static final long serialVersionUID = -3305905377213332702L;
    private static final Logger LOG = Logger.getLogger(ValorisationWord.class);


    @Override
    public boolean onAfterLoad() {

        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
//		if(action.getName().equals("Envoyer"))
//		{
//			try
//			{
//				ILibraryModule libraryModule = Modules.getLibraryModule();
//				IContext context = libraryModule.getContextByLogin("sysadmin");
//				ICatalog catalog = iWorkflowInstance.getCatalog();
//				//----------------------------------------- Paramètres ------------------------------------
//
//				String libraryName = catalog.getConfiguration().getStringUserProperty("NOM.BIBLIOTHEQUE.ESPACE.DOCUMENTAIRE").trim();
//				String fileName = catalog.getConfiguration().getStringUserProperty("CHEMIN.MODELEWORD").trim();
//				String pjOutput = catalog.getConfiguration().getStringUserProperty("NOM.CHAMP_PJ").trim();
//				String cheminTempFolder = catalog.getConfiguration().getStringUserProperty("CHEMIN.DOSSIER.TEMP").trim();
//				String nomPJOutput = catalog.getConfiguration().getStringUserProperty("NOM.PJ.GENERE").trim();
//				String nameOrganization = iWorkflowInstance.getCatalog().getConfiguration().getStringUserProperty("ORGANIZATION").trim();
//				IOrganization organization = iDirectoryModule.getOrganization(context, nameOrganization);
//				ILibrary library = libraryModule.getLibrary(context, organization, libraryName);
//
//				IFile file = libraryModule.getFileByPath(context, library, fileName);
//				if (file == null)
//				{
//					throw new LibraryModuleException("le fichier de modele n'existe pas dans l'espace documentaire");
//				}
//
//				GenerateWordFile generateWordFile = new GenerateWordFile();
//				IAttachment attachement = libraryModule.getAttachment(file, file.getName());
//				InputStream inputStream = generateWordFile.valorization(iWorkflowModule, iWorkflowInstance, attachement);
//
//
//				Document document = new Document(inputStream);
//				document.save(cheminTempFolder+ "\\" + nomPJOutput + ".pdf", SaveFormat.PDF);
//				iWorkflowInstance.setValue(pjOutput, null);
//				iWorkflowModule.addAttachment(iWorkflowInstance, pjOutput, new File(cheminTempFolder+ "\\" + nomPJOutput + ".pdf"));
//				iWorkflowInstance.save(pjOutput);
//
//				File MyFile = new File(cheminTempFolder+ "\\" + nomPJOutput + ".pdf");
//				MyFile.delete();
//
//			}
//			catch (LibraryModuleException libraryModuleException)
//			{
//				Navigator.getNavigator().getRootNavigator().showAlertBox("Nom de l'espace documentaire est incorrect, ou le fichier de modèle n'existe pas dans l'espace documentaire!");
//				LOG.error(libraryModuleException.getMessage());
//			}
//			catch (Exception e)
//			{
//				Navigator.getNavigator().getRootNavigator().showAlertBox("Un problème est survenu, veuillez contacter votre administrateur");
//				LOG.error(e.getMessage());
//			}
//		}
        return super.onBeforeSubmit(action);
    }


    public void generer(IWorkflowInstance iWorkflowInstance, IWorkflowModule iWorkflowModule, IResourceController iResourceController) {
        iWorkflowInstance.setValue("GenerationContratDuTravail", new ArrayList());
        try {

            ILibraryModule libraryModule = Modules.getLibraryModule();
            IContext context = libraryModule.getContextByLogin("sysadmin");
            IStorageResource ecole = (IStorageResource) iWorkflowInstance.getValue("EcoleClient");
            ArrayList<IAttachment> attestation = null;
            Collection<IStorageResource> ficheAttestation = null;

            IViewController controller = iWorkflowModule.getViewController(
                    context, IResource.class);
            IProjectModule p = Modules.getProjectModule();
            p.getSysadminContext();
            IProject project = p.getProject(context, "REFERENTIELCOMMUN", iWorkflowInstance.getCatalog().getProject().getOrganization());
            ICatalog catalog = iWorkflowModule.getCatalog(context, "REFERENTIEL", 4, project);
            IResourceDefinition definition = iWorkflowModule.getResourceDefinition(context, catalog, "FicheAttestationsAdministratives");
            controller.addEqualsConstraint("Societe", ecole);
            ficheAttestation = controller.evaluate(definition);
            IStorageResource contratChoisie = (IStorageResource) iWorkflowInstance.getValue("TypeContratTravail");
            if (contratChoisie != null) {
                if (!ficheAttestation.isEmpty()) {
                    for (IStorageResource attestation1 : ficheAttestation) {
                        if (attestation1.getValue("sys_Title").equals(contratChoisie.getValue("sys_Title"))) {
                            attestation = (ArrayList) attestation1.getValue("AttestationAdministrative");
                            break;
                        }
                    }
                }
            }


            if (attestation != null) {
                File file = null;
                for (IAttachment iAttachment : attestation) {
                    try {
                        iAttachment.getName();
                        file = new File("c://TEST//" + iAttachment.getName());
                        FileUtils.writeByteArrayToFile(file, iAttachment.getContent());
                        GenerateWordFile generateWordFile = new GenerateWordFile();
                        InputStream inputStream = generateWordFile.valorization(iWorkflowModule, iWorkflowInstance, iAttachment);
                        // FileUtils.copyInputStreamToFile(inputStream, file);
                        //document.setValue("ModeleAttestationAdministrative", null);
                        Document document = new Document(inputStream);
                        ICatalog Recrutement = iWorkflowInstance.getCatalog();
                        String cheminTempFolder = Recrutement.getConfiguration().getStringUserProperty("CHEMIN.DOSSIER.TEMP").trim();
                        document.save(cheminTempFolder + "\\" + iAttachment.getName() + ".pdf", SaveFormat.PDF);
                        iWorkflowModule.addAttachment(iWorkflowInstance, "GenerationContratDuTravail", new File(cheminTempFolder + "\\" + iAttachment.getName() + ".pdf"));
                        //document.refresh("ModeleAttestationAdministrative");
                        file.delete();
                        file.deleteOnExit();
                        File MyFile = new File(cheminTempFolder + "\\" + iAttachment.getName() + ".pdf");
                        MyFile.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                iWorkflowInstance.setValue("GenerationContratDuTravail", null);
                Navigator.getNavigator().getRootNavigator().showAlertBox("le fichier est introuvable");
            }
        } catch (Exception e) {
            Navigator.getNavigator().getRootNavigator().showAlertBox("Un problème est survenu, veuillez contacter votre administrateur");
            e.printStackTrace();
        }
    }

    //lettre d'engagement
    public void genererLettreEngagement(IWorkflowInstance iWorkflowInstance, IWorkflowModule iWorkflowModule, IResourceController iResourceController) {
        iWorkflowInstance.setValue("GenerationDeLaLettreDEngagement", new ArrayList());
        String Salaire = " ";
        if (iWorkflowInstance.getValue("SalaireBrutEnPlusDuSalaireNet") != null) {
            Salaire = ((Number) iWorkflowInstance.getValue("SalaireBrutEnPlusDuSalaireNet")).toString();

        }
        iWorkflowInstance.setValue("SalaireTXT", Salaire);

        Number SalaireBrut = ((Number) iWorkflowInstance.getValue("SalaireBrutMAD"));
        Number PeriodeDessai = ((Number) iWorkflowInstance.getValue("PeriodeDEssai"));
        if (SalaireBrut != null) {
            iWorkflowInstance.setValue("SalaireBrutTXT", GenerateNumber.ConvertNumber(SalaireBrut));
            iWorkflowInstance.setValue("SalaireBrutTXTNORMAL", SalaireBrut.toString());

        }
        if (PeriodeDessai != null && GenerateNumber.ConvertNumbePeriodeEssai(PeriodeDessai.floatValue()) != null) {
            iWorkflowInstance.setValue("PeriodeDEssaiTXT", GenerateNumber.ConvertNumbePeriodeEssai(PeriodeDessai.floatValue()));
        } else {
            iResourceController.alert("La période d'essai doit être un multiplicateur de 0,5");
            return;
        }

        try {

            ILibraryModule libraryModule = Modules.getLibraryModule();
            IContext context = libraryModule.getContextByLogin("sysadmin");
            IStorageResource ecole = (IStorageResource) iWorkflowInstance.getValue("EcoleClient");
            ArrayList<IAttachment> attestation = null;
            Collection<IStorageResource> ficheAttestation = null;

            IViewController controller = iWorkflowModule.getViewController(
                    context, IResource.class);
            IProjectModule p = Modules.getProjectModule();
            p.getSysadminContext();
            IProject project = p.getProject(context, "REFERENTIELCOMMUN", iWorkflowInstance.getCatalog().getProject().getOrganization());
            ICatalog catalog = iWorkflowModule.getCatalog(context, "REFERENTIEL", 4, project);
            IResourceDefinition definition = iWorkflowModule.getResourceDefinition(context, catalog, "FicheAttestationsAdministratives");
            controller.addEqualsConstraint("Societe", ecole);
            ficheAttestation = controller.evaluate(definition);
            IStorageResource lettreChoisie = (IStorageResource) iWorkflowInstance.getValue("TypeLettreEngagement");
            if (lettreChoisie != null) {
                if (!ficheAttestation.isEmpty()) {
                    for (IStorageResource attestation1 : ficheAttestation) {
                        if (attestation1.getValue("sys_Title").equals(lettreChoisie.getValue("sys_Title"))) {
                            attestation = (ArrayList) attestation1.getValue("AttestationAdministrative");
                            break;
                        }
                    }
                }
            }


            if (attestation != null) {
                File file = null;
                for (IAttachment iAttachment : attestation) {
                    try {
                        file = new File("c://TEST//" + iAttachment.getName());
                        FileUtils.writeByteArrayToFile(file, iAttachment.getContent());
                        GenerateWordFile generateWordFile = new GenerateWordFile();
                        InputStream inputStream = generateWordFile.valorization(iWorkflowModule, iWorkflowInstance, iAttachment);
                        //FileUtils.copyInputStreamToFile(inputStream, file);
                        Document document = new Document(inputStream);
                        ICatalog Recrutement = iWorkflowInstance.getCatalog();
                        String cheminTempFolder = Recrutement.getConfiguration().getStringUserProperty("CHEMIN.DOSSIER.TEMP").trim();
                        document.save(cheminTempFolder + "\\" + iAttachment.getName() + ".pdf", SaveFormat.PDF);
                        iWorkflowModule.addAttachment(iWorkflowInstance, "GenerationDeLaLettreDEngagement", new File(cheminTempFolder + "\\" + iAttachment.getName() + ".pdf"));
                        file.delete();
                        file.deleteOnExit();
                        File MyFile = new File(cheminTempFolder + "\\" + iAttachment.getName() + ".pdf");
                        MyFile.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                iWorkflowInstance.setValue("GenerationDeLaLettreDEngagement", null);
                Navigator.getNavigator().getRootNavigator().showAlertBox("le fichier est introuvable");
            }
        } catch (Exception e) {
            Navigator.getNavigator().getRootNavigator().showAlertBox("Un problème est survenu, veuillez contacter votre administrateur");
            e.printStackTrace();
        }


    }


    public void generer2(IWorkflowInstance iWorkflowInstance, IWorkflowModule iWorkflowModule, IResourceController iResourceController) {

        try {

            ILibraryModule libraryModule = Modules.getLibraryModule();
            IContext context = libraryModule.getContextByLogin("sysadmin");
            ICatalog catalog = iWorkflowInstance.getCatalog();
            //----------------------------------------- Paramètres ------------------------------------
            //String homologation = (String) iWorkflowInstance.getValue("Homologation");
            String profil = (String) iWorkflowInstance.getValue("Profil");
            String ecoleName = ((IStorageResource) iWorkflowInstance.getValue("EcoleClient")).getValue("sys_Title").toString();
            String fileName = "";
            String folderName = "";

            if (ecoleName.equals("Lycée Français Guy de Maupassant")) {
                fileName = "LFGM - ";
                folderName = "LFGM";
            } else if (ecoleName.equals("Lycée Français Sophie Germain")) {
                fileName = "LFSG - ";
                folderName = "LFSG";
            } else if (ecoleName.equals("Ecole Trilingue Internationale")) {
                fileName = "ETI - ";
                folderName = "ETI";
            } else if (ecoleName.equals("Ecole Française Guy de Maupassant")) {
                fileName = "EFGM - ";
                folderName = "EFGM";
            } else {
                iResourceController.alert("Ecole introuvable");
            }
            if (profil.equals("Enseignants")) {
                fileName += "Template Contrat CDI - PROFESSEURS.docx";
            } else if (profil.equals("Personnel de Direction")) {
                fileName += "Template Contrat CDI - Non profs.docx";
            } else if (profil.equals("Personnel administratif")) {
                fileName += "Template Contrat CDI - Non Cadres (Animateur Surveillant).docx";
            } else {
                iResourceController.alert("Model introuvable");
            }


            //String ecole = (String) iWorkflowInstance.getValue("Ecole_ASPOSE");

            //String ecole = ((IStorageResource) iWorkflowInstance.getValue("EcoleClient")).getValue("sys_Title").toString();
            //fileName = ecole+"_"+fileName;
            //fileName = "Lycée Français Guy de Maupassant CL"+"_"+fileName;


            String libraryName = catalog.getConfiguration().getStringUserProperty("NOM.BIBLIOTHEQUE.ESPACE.DOCUMENTAIRE").trim();

            String pjOutput = catalog.getConfiguration().getStringUserProperty("NOM.CHAMP_PJ").trim();
            String cheminTempFolder = catalog.getConfiguration().getStringUserProperty("CHEMIN.DOSSIER.TEMP").trim();
            String nomPJOutput = catalog.getConfiguration().getStringUserProperty("NOM.PJ.GENERE").trim();
            String nameOrganization = iWorkflowInstance.getCatalog().getConfiguration().getStringUserProperty("ORGANIZATION").trim();
            IOrganization organization = Modules.getDirectoryModule().getOrganization(context, nameOrganization);
            ILibrary library = libraryModule.getLibrary(context, organization, libraryName);

            IFile file = libraryModule.getFileByPath(context, library, "Modèles/" + folderName + "/" + fileName);
            if (file == null) {
                throw new LibraryModuleException("le fichier de modele n'existe pas dans l'espace documentaire");
            }

            GenerateWordFile generateWordFile = new GenerateWordFile();
            IAttachment attachement = libraryModule.getAttachment(file, file.getName());
            InputStream inputStream = generateWordFile.valorization(iWorkflowModule, iWorkflowInstance, attachement);


            Document document = new Document(inputStream);
            //nomPJOutput , hada 3ad zedtor
            nomPJOutput = fileName;
            document.save(cheminTempFolder + "\\" + nomPJOutput + ".pdf", SaveFormat.PDF);
            iWorkflowInstance.setValue(pjOutput, null);
            iWorkflowModule.addAttachment(iWorkflowInstance, pjOutput, new File(cheminTempFolder + "\\" + nomPJOutput + ".pdf"));
            iWorkflowInstance.save(pjOutput);

            File MyFile = new File(cheminTempFolder + "\\" + nomPJOutput + ".pdf");
            MyFile.delete();

        } catch (LibraryModuleException libraryModuleException) {
            Navigator.getNavigator().getRootNavigator().showAlertBox("Nom de l'espace documentaire est incorrect, ou le fichier de modèle n'existe pas dans l'espace documentaire!");
            LOG.error(libraryModuleException.getMessage());
        } catch (Exception e) {
            Navigator.getNavigator().getRootNavigator().showAlertBox("Un problème est survenu, veuillez contacter votre administrateur");
            e.printStackTrace();
        }
    }


}





