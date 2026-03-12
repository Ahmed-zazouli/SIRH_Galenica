package recrutement.candidature;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;


public class AddFilesToDoCenter {


	HashMap<String, String> fieldsPaths = new HashMap<String, String>() {{
		put("AncienCTE", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Ancien CTE");
		put("CasierJudiciaireDuPays", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Casier judiciaire du pays");
		put("CertificatMedicalDAptitudePhysique", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Certificat médical d'aptitude physique");
		put("CertificatsDeTravailDesPrecedentsEmployeurs", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Certificats de travail des postes précédents");
		put("CIN_PJ", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/CIN");
		put("ContratDeBailOriginal", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Contrat de bail");
		put("CopieDeLaCarteDeSejour", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/La carte de séjour");
		put("CopiesCertifieesDesDiplomes", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Copies certifiées des diplômes");
		put("CV", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/CV");
		put("CVCandidat", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/CV Candidat");
		put("_CVCandidat", "Recrutement/_Annee/BASE CV/_NomPrenom");
		put("DernierBulletinDePaie2", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Dernier bulletin de paie");
		put("DernierBulletinDePaie", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Dernier bulletin de paie");
		put("Document1", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Document 1");
		put("Document2", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Document 2");
		put("Document3", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Document 3");
		put("FactureLYDEC", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Facture LYDEC");
		put("FicheAnthropometrique", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Fiche anthropométrique");
		put("FicheAnthropometrique2", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Fiche anthropométrique");
		put("FicheEvaluation", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Fiche Evaluation");
		put("FicheEvaluation2", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Fiche Evaluation");
		put("LettreDEngagementSigne", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Lettre d'engagement signée");
		put("LettreMotivation", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Lettre motivation");
		put("Passeport2", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Passeport");
		put("PhotocopieActeDeMariage", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Photocopie Acte de Mariage");
		put("PhotocopieCarteCNSS", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Photocopie Carte CNSS");
		put("PhotocopieDuLivretDeFamille", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Photocopie du livret de famille");
		put("PhotosDIdentite", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/Photos d’identité");
		put("RIB", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Dossier administratif/RIB");
		put("GenerationContratDuTravail", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Contrat/Génération contrat du travail");

		put("CVCandidat", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/CV candidat");
		put("FicheEvaluation2", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Fiche évaluation");
		put("LettreMotivation", "Recrutement/_Annee/_EcoleClient/_IntitulePosteAPourvoir/_NomPrenom/Lettre motivation");



	}};

	public boolean AddFileToDocCenter(IWorkflowModule workflowModule,IDirectoryModule directoryModule, IWorkflowInstance workflowInstance) {
		try {

			//String appPath = workflowModule.getConfiguration().getStringProperty("com.moovapps.app.path");

			ILibraryModule libraryModule = Modules.getLibraryModule();
			IContext context = workflowModule.getSysadminContext();
			IOrganization organization = directoryModule.getOrganization(context, "DefaultOrganization");
			ILibrary library = libraryModule.getLibrary(context, organization, "CVTHEQUE");

			for (Entry<String, String> fieldPath : fieldsPaths.entrySet()) {
				String field = fieldPath.getKey();
				String path = fieldPath.getValue();
				if(field.startsWith("_")){
					Method method = this.getClass().getMethod(field, String.class, IWorkflowInstance.class);
					field = (String)method.invoke(this, field, workflowInstance);
				}
				if(workflowInstance.getValue(field) != null){
					ArrayList<IAttachment> attachements = (ArrayList<IAttachment>) workflowInstance.getValue(field);
					if(attachements.size() > 0){
						for (IAttachment attachment : attachements) {
							try {
								path = rebuildPath(path, workflowInstance);
								IFolder packPaieFolder = libraryModule.getFolder(context, library, path);
								String[] folders = path.split("/");

								for (String folder : folders) {
									if (!folder.equals("")) {
										packPaieFolder = getOrCreateFolder(libraryModule, context, library, packPaieFolder, folder);
									}
								}
								File tmpFile = new File("wildfly\\standalone\\deployments\\vdoc.ear\\vdoc.war\\external-tools\\PJ\\" + attachment.getName());
								FileUtils.writeByteArrayToFile(tmpFile, attachment.getContent());

								FileInputStream file = new FileInputStream(tmpFile);
								IFile insertedFile = null;
								if(packPaieFolder == null){
									insertedFile = libraryModule.getFile(context, library, attachment.getName());
								}else{
									insertedFile = libraryModule.getFile(context, packPaieFolder, attachment.getName());
								}
								if (insertedFile != null) {
									insertedFile.delete(context);
								}
								if(packPaieFolder != null){
									insertedFile = libraryModule.createFile(context, packPaieFolder, attachment.getName(), "", file);
									ISecurityController securityController = libraryModule.getSecurityController(insertedFile);
									securityController.breakInheritance(ISecurityController.EVERYONE, new Object[]{null, "read"});
									//securityController.addPermission(user, new Object[]{null, "read"});
								}else{
									insertedFile = libraryModule.createFile(context, library, attachment.getName(), "", file);
									ISecurityController securityController = libraryModule.getSecurityController(insertedFile);
									securityController.breakInheritance(ISecurityController.EVERYONE, new Object[]{null, "read"});
									//securityController.addPermission(user, new Object[] { null, "read" });

								}
								deleteDirectory(tmpFile);
								//tmpFile.delete();
								//tmpFile.deleteOnExit();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		} catch (Exception f) {
			f.printStackTrace();
		}

		return false;
	}



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
	private String rebuildPath(String path, IWorkflowInstance workflowInstance) {
		String newPath = "";
		try {
			String[] folders = path.split("/");
			for (String folder : folders) {
				if(folder.startsWith("_")){
					Method method = this.getClass().getMethod(folder, IWorkflowInstance.class);
					newPath += method.invoke(this, workflowInstance);
				}else{
					newPath+= folder+"/";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newPath;
	}

	public String _CVCandidat(String field, IWorkflowInstance workflowInstance) {
		return "CVCandidat";
	}

	public String _Annee(IWorkflowInstance workflowInstance) {
		String thisYear = new SimpleDateFormat("yyyy").format(new Date());
		return thisYear+"/";
	}

	public String _EcoleClient(IWorkflowInstance workflowInstance) {
		String folder = "N/A";
		if(workflowInstance.getValue("EcoleClient") != null){
			folder = (String) ((IStorageResource) workflowInstance.getValue("EcoleClient")).getValue("sys_Title");
		}
		return folder+"/";
	}

	public String _IntitulePosteAPourvoir(IWorkflowInstance workflowInstance) {
		String folder = "N/A";
		if(workflowInstance.getValue("IntitulePosteAPourvoir") != null){
			folder = (String) workflowInstance.getValue("IntitulePosteAPourvoir");
		}
		return folder+"/";
	}

	public String _NomPrenom(IWorkflowInstance workflowInstance) {
		String folder = "N/A";
		if(workflowInstance.getValue("Nom") != null && workflowInstance.getValue("Prenom") != null){
			folder = (String) workflowInstance.getValue("Nom") +" " + (String) workflowInstance.getValue("Prenom")  ;
		}else if (workflowInstance.getValue("Nom") != null ){
			folder = (String) workflowInstance.getValue("Nom");
		}else if(workflowInstance.getValue("Prenom") != null){
			folder = (String) workflowInstance.getValue("Prenom");
		}
		return folder+"/";
	}

	public String _Categorie(IWorkflowInstance workflowInstance) throws InvocationTargetException {
		String folder = "N/A";

		if(workflowInstance.getValue("Categorie") != null){
			folder = (String) ((IStorageResource) workflowInstance.getValue("Categorie")).getValue("sys_Title");
		}

		return folder+"/";
	}



	public String _CodeDemande(IWorkflowInstance workflowInstance) {
		String folder = "N/A";
		if(workflowInstance.getValue("sys_Reference") != null){
			folder = (String) workflowInstance.getValue("sys_Reference");
		}
		return folder+"/";
	}

	public String _RaisonSociale(IWorkflowInstance workflowInstance) {
		String folder = "N/A";
		if(workflowInstance.getValue("RaisonSocialeFonct") != null){
			folder = (String) ((IStorageResource)workflowInstance.getValue("RaisonSocialeFonct")).getValue("sys_Title");
		}
		return folder+"/";
	}

	private IFolder getOrCreateFolder(ILibraryModule libraryModule, IContext context, ILibrary library, IFolder folder, String folderName) {
		IFolder packPaieFolder = null;
		try {
			if(folder != null){
				packPaieFolder = libraryModule.getFolder(context, folder, folderName);
				if (packPaieFolder == null) {
					packPaieFolder = libraryModule.createFolder(context, folder, folderName);
				}
			} else {
				packPaieFolder = libraryModule.getFolder(context, library, folderName);
				if (packPaieFolder == null) {
					packPaieFolder = libraryModule.createFolder(context, library, folderName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return packPaieFolder;
	}

}
