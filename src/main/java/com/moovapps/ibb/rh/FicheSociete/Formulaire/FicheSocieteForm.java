package com.moovapps.ibb.rh.FicheSociete.Formulaire;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.DirectoryModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.ILibraryModule;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class FicheSocieteForm extends BaseDocumentExtension {
	
	@Override
	public boolean onAfterLoad() {
		if(getWorkflowInstance().getValue("Organisation") == null){
			try {
				getWorkflowInstance().setValue("Organisation", getDirectoryModule().getOrganization(getWorkflowModule().getSysadminContext(), "EmptyOrg"));
			} catch (DirectoryModuleException e) {
				e.printStackTrace();
			}
		}
		handleElementVariablesPaieFile();
		return super.onAfterLoad();
	}
	
	@Override
	public boolean onBeforeSave() {
		upsertOrganization();
		calculateWorkHours();
		return super.onBeforeSave();
	}
	
	private void calculateWorkHours() {
		float HeureDebutPremiereShift = Integer.valueOf(((String)getWorkflowInstance().getValue("HeureDebutPremiereShift")));
		float MinuteDebutPremiereShift = Integer.valueOf(((String)getWorkflowInstance().getValue("MinuteDebutPremiereShift")));
		float HeureFinPremiereShift = Integer.valueOf(((String)getWorkflowInstance().getValue("HeureFinPremiereShift")));
		float MinuteFinPremiereShift = (Integer.valueOf((String)getWorkflowInstance().getValue("MinuteFinPremiereShift")));
		float HeureDebutDeuxiemeShift = Integer.valueOf(((String)getWorkflowInstance().getValue("HeureDebutDeuxiemeShift")));
		float MinuteDebutDeuxiemeShift = Integer.valueOf(((String)getWorkflowInstance().getValue("MinuteDebutDeuxiemeShift")));
		float HeureFinDeuxiemeShift = Integer.valueOf(((String)getWorkflowInstance().getValue("HeureFinDeuxiemeShift")));
		float MinuteFinDeuxiemeShift = Integer.valueOf(((String)getWorkflowInstance().getValue("MinuteFinDeuxiemeShift")));
		
		float durationPremiereShift = (HeureFinPremiereShift - HeureDebutPremiereShift) * 60 +( MinuteFinPremiereShift - MinuteDebutPremiereShift);
		float durationDeuxiemeShift = (HeureFinDeuxiemeShift - HeureDebutDeuxiemeShift) * 60 +( MinuteFinDeuxiemeShift - MinuteDebutDeuxiemeShift);
		float durationWorkHours = durationPremiereShift + durationDeuxiemeShift;
		
		getWorkflowInstance().setValue("durationPremiereShift", durationPremiereShift);
		getWorkflowInstance().setValue("durationDeuxiemeShift", durationDeuxiemeShift);
		getWorkflowInstance().setValue("durationWorkHours", durationWorkHours);
	}
	
	private void upsertOrganization() {
		IContext sysContext = getWorkflowModule().getSysadminContext();
		String sysNameTmp = "";
		String sysName = "";
		if(getWorkflowInstance().getValue("sys_Title") != null){
			sysNameTmp = (String)getWorkflowInstance().getValue("sys_Title");
			sysNameTmp.trim();
			sysNameTmp= org.apache.commons.lang3.StringUtils.stripAccents(sysNameTmp);
			sysNameTmp= sysNameTmp.replaceAll("[^a-zA-Z0-9]", " ");
			String[] splitedName = sysNameTmp.split(" ");
			for (String string : splitedName) {
				if(string != null && !string.equals("") ){
					string = string.trim();
					sysName += string.substring(0,1).toUpperCase() + (string.length() > 1 ? string.substring(1).toLowerCase() : "");
				}
			}
		}
		IOrganization org = null;
		IOrganization emptyOrg = null;
		try {
			emptyOrg = getDirectoryModule().getOrganization(getWorkflowModule().getSysadminContext(), "EmptyOrg");
		} catch (DirectoryModuleException e1) {
			e1.printStackTrace();
		}
		
		if(getWorkflowInstance().getValue("Organisation") != null &&
				!((IOrganization)getWorkflowInstance().getValue("Organisation")).getId().equals(emptyOrg.getId())){
			org = (IOrganization)getWorkflowInstance().getValue("Organisation");
			
		}
		if(org == null){
			try {
				getDirectoryModule().beginTransaction();
				IOrganization organization = getDirectoryModule().getOrganization(sysContext, "Internal");
				IOrganization newOrg =  getDirectoryModule().createOrganization(sysContext, organization, sysName, (String)getWorkflowInstance().getValue("sys_Title"));		
				newOrg.save(sysContext);
				IGroup clientRh = getDirectoryModule().createGroup(sysContext, newOrg, sysName+"RH", "RH" );
				clientRh.save(sysContext);
				IGroup clientManager = getDirectoryModule().createGroup(sysContext, newOrg, sysName+"Manager", "Manager" );
				clientManager.save(sysContext);
				getDirectoryModule().commitTransaction();
				getWorkflowInstance().setValue("Organisation",newOrg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			org.setLabel((String)getWorkflowInstance().getValue("sys_Title"));
			org.save(sysContext);
		}
	}
	
	private void handleElementVariablesPaieFile() {
		if(getWorkflowInstance().getValue("MaquetteElementsVariablesPaie") == null){
			File file = createFileFromFileCenterInLocalDrive("Maquettes/Maquette variables paie v2.xlsx", "Canevas éléments variables paie.xlsx");
			if(file == null){
				getResourceController().alert("Merci d'ajouter le canevas éléments variables de la paie dans la fiche client");
				return;
			}
			ArrayList<IAttachment> tmpAttachementCollection = new ArrayList<IAttachment>();
			IAttachment tmpJournalPaieAttachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
			tmpAttachementCollection.add(tmpJournalPaieAttachment);
			getWorkflowInstance().setValue("MaquetteElementsVariablesPaie", tmpAttachementCollection);
			file.delete();
			file.deleteOnExit();
		}
	}
	
	private File createFileFromFileCenterInLocalDrive(String filePath, String fileName) {
		try {
			ILibraryModule libraryModule = Modules.getLibraryModule();
			IContext context = getWorkflowModule().getSysadminContext();
			IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
			ILibrary library = libraryModule.getLibrary(context, organization, "Paie");
			if (library != null) {
				IFile file = libraryModule.getFileByPath(context, library, filePath);
				if (file == null) {
					getResourceController().alert("Le fichier de modele n'existe pas dans l'espace documentaire...Vieullez contacter votre administrateur");
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
