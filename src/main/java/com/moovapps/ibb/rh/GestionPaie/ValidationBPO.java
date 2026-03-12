package com.moovapps.ibb.rh.GestionPaie;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class ValidationBPO extends BaseDocumentExtension {
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("ImportationSAGE")){
			onImporterSageClick();
		}
		return super.onBeforeSubmit(action);
	}
	
	private void onImporterSageClick() {
		insertRemonteesPaie();
	}
	
	public boolean insertRemonteesPaie() {
		try {
			IContext context = getWorkflowModule().getSysadminContext();
			IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
			IProject project = getProjectModule().getProject(context,"RemonteVariableDeLaPaie", organization);
			ICatalog catalog = getWorkflowModule().getCatalog(context,"JournalDeLaPaie", ICatalog.IType.STORAGE, project);
			IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "JournalDeLaPaieParSocieteParAnneeParMois");
			IStorageResource storageRessource = getWorkflowModule().createStorageResource(getWorkflowModule().getSysadminContext(), definition, "");
			storageRessource.setValue("Societe", getWorkflowInstance().getValue("FicheSociete"));
			storageRessource.setValue("GenerateurDuJournal", getWorkflowInstance().getValue("sys_Creator"));
			storageRessource.setValue("Annee", getWorkflowInstance().getValue("Annee"));
			storageRessource.setValue("Mois", Integer.valueOf((String)getWorkflowInstance().getValue("Mois")));
			IAttachment remonteesPaie = null;
			ArrayList<IAttachment> tmp = (ArrayList<IAttachment>)getWorkflowInstance().getValue("RemonteesVariablesDeLaPaie");
			if(tmp != null && tmp.size() > 0){
				remonteesPaie = tmp.iterator().next();
			}
			if (remonteesPaie != null) {
				try {
					File tmpFile = new File("c://TEST//"+remonteesPaie.getName());
					FileUtils.writeByteArrayToFile(tmpFile,remonteesPaie.getContent());
					remonteesPaie = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), tmpFile);
					tmp.clear();
					tmp.add(remonteesPaie); 
					tmpFile.delete();
					tmpFile.deleteOnExit();
				} catch (Exception e) {
				}
			}
			storageRessource.setValue("RemonteesVariablesDeLaPaie", tmp);

			
			IAttachment remonteesPaiePDF = null;
			tmp = (ArrayList<IAttachment>)getWorkflowInstance().getValue("PDFRemonteesVariablesDeLaPaie");
			if(tmp != null && tmp.size() > 0){
				remonteesPaiePDF = tmp.iterator().next();
			}
			if (remonteesPaiePDF != null) {
				try {
					File tmpFile = new File("c://TEST//"+remonteesPaiePDF.getName());
					FileUtils.writeByteArrayToFile(tmpFile,remonteesPaiePDF.getContent());
					remonteesPaiePDF = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), tmpFile);
					tmp.clear();
					tmp.add(remonteesPaiePDF); 
					tmpFile.delete();
					tmpFile.deleteOnExit();
				} catch (Exception e) {
				}
			}
			
			storageRessource.setValue("PDFRemonteesDeLaPaie", tmp);
			storageRessource.save(context);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
