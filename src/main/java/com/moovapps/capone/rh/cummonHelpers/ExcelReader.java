package com.moovapps.capone.rh.cummonHelpers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

class IncorrectValueException extends Exception {
	public IncorrectValueException(String errorMessage, IWorkflowModule module, IWorkflowInstance instance, IResourceController resourceController) {
		super(errorMessage);
		resourceController.alert(errorMessage);
		instance.setValue("Tache", null);
		module.rollbackTransaction();
		printStackTrace();
	}
}

public class ExcelReader {
	
	Comparator<ILinkedResource> byDateDebut = new Comparator<ILinkedResource>() {
		public int compare(ILinkedResource c1, ILinkedResource c2) {
			return Long.valueOf(((Date) c1.getValue("DateDebut")).getTime()).compareTo(((Date) c2.getValue("DateDebut")).getTime());
		}
	};

	Comparator<ILinkedResource> byDatefin = new Comparator<ILinkedResource>() {
		public int compare(ILinkedResource c1, ILinkedResource c2) {
			return Long.valueOf(((Date) c1.getValue("DateFin")).getTime()).compareTo(((Date) c2.getValue("DateFin")).getTime());
		}
	};

	private IStorageResource getNatureByName(String name, IWorkflowModule workflowModule, IWorkflowInstance instance) {
		IStorageResource auditeur = null;
		try {
			IContext context = workflowModule.getSysadminContext();
			ICatalog StorageCatalog = workflowModule.getCatalog(context, "Donnees", ICatalog.IType.STORAGE, instance.getCatalog().getProject());
			IResourceDefinition resourceDefinition = workflowModule.getResourceDefinition(context, StorageCatalog, "NatureTache");
			IViewController controller = workflowModule.getViewController(context, IResource.class);
			controller.addEqualsConstraint(IProperty.System.TITLE, name);
			Collection<IStorageResource> collection = controller.evaluate(resourceDefinition);
			if (collection != null) {
				if (!collection.isEmpty()) {
					auditeur = collection.iterator().next();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return auditeur;
	}

	public void generateTache(IWorkflowInstance instance,
			IWorkflowModule workflowModule) {
		try {
			IContext context = workflowModule.getLoggedOnUserContext();
			instance.save(context);
			workflowModule.beginTransaction();

			IContext sysContext = workflowModule.getSysadminContext();

			IDirectoryModule directoryModule = Modules.getDirectoryModule();
			IGroup group = directoryModule.getGroup(sysContext, instance.getCatalog().getProject().getOrganization(), "MembreProjet");
			ICatalog StorageCatalog = workflowModule.getCatalog(sysContext, "Donnees", ICatalog.IType.STORAGE, instance.getCatalog().getProject());
			Collection<IAttachment> attachments = (Collection<IAttachment>) workflowModule.getAttachments(instance, "PieceJointe");
			File file = null;
			for (IAttachment iAttachment : attachments) {
				file = new File("c://TEST//test.xls");
				iAttachment.getContent();
				FileUtils.writeByteArrayToFile(file, iAttachment.getContent());
			}

			int i = 0, ligne = 1, colonne = 0;
			double nbrJours = 0, budget = 0;
			Date debut = null, fin = null;
			Number iD = null;
			String phase = "", sousPhase = "", criticitetxt = "", systemtxt = "", tache = "", entite = "", code = "", natureTxt = "", commentaire = "";
			IUser entiteuser = null;
			IStorageResource nature = null, systeme = null, criticite = null;
			POIFSFileSystem MyFile = new POIFSFileSystem(new FileInputStream(file));
			HSSFWorkbook workbook = new HSSFWorkbook(MyFile);
			HSSFSheet sheet = workbook.getSheetAt(0);
			ligne = 1;
			colonne = 0;
			HSSFRow row = sheet.getRow(ligne);
			ILinkedResource iLinkedResource = null;
			while (row != null && row.getCell(0) != null) {
				phase = null;
				sousPhase = null;
				tache = null;
				entite = null;
				nbrJours = 0;
				budget = 0;
				natureTxt = null;
				entiteuser = null;
				nature = null;
				debut = null;
				fin = null;
				if (row.getCell(colonne) != null) {
					iD = row.getCell(colonne).getNumericCellValue();
				}
				colonne++;
				if (row.getCell(colonne) != null) {
					phase = row.getCell(colonne).getStringCellValue();
				}
				colonne++;
				if (row.getCell(colonne) != null) {
					sousPhase = row.getCell(colonne).getStringCellValue();
				}
				colonne++;
				if (row.getCell(colonne) != null) {
					tache = row.getCell(colonne).getStringCellValue();
				}
				colonne++;
				// if(row.getCell(colonne)!=null)
				// {
				// entite = row.getCell(colonne).getStringCellValue();
				// }
				// colonne ++ ;
				// if(row.getCell(colonne)!=null)
				// {
				// nbrJours = row.getCell(colonne).getNumericCellValue();
				// }

				if (row.getCell(colonne) != null) {
					natureTxt = row.getCell(colonne).getStringCellValue();
					nature = getNatureByName(natureTxt, workflowModule, instance);
				}
				colonne++;
				if (row.getCell(colonne) != null) {
					nbrJours = row.getCell(colonne).getNumericCellValue();
				}
				colonne++;
				if (row.getCell(colonne) != null) {
					budget = row.getCell(colonne).getNumericCellValue();
				}
				colonne++;
				if (row.getCell(colonne) != null) {
					String resp = row.getCell(colonne).getStringCellValue();
					if (workflowModule.getUserByLogin(resp) != null) {
						entiteuser = workflowModule.getUserByLogin(resp);
						if (entiteuser.isMemberOf(group, false)) {

						} else {
							// workflowModule.getResourceController(instance).alert("L'utilisateur "+entiteuser.getFullName()+" n'est pas membre du groupeeeee équipe projet");
							// instance.setValue("Tache", null);
							throw new IncorrectValueException("L'utilisateur " + entiteuser.getFullName() + " n'est pas membre du groupe équipe projet", workflowModule, instance, workflowModule.getResourceController(instance));
						}
					} else {
						// workflowModule.getResourceController(instance).alert("L'utilisateur "+resp+" n'existe pas dans l'annuaire ");
						// instance.setValue("Tache", null);
						throw new IncorrectValueException("L'utilisateur " + resp + " n'existe pas dans l'annuaire ", workflowModule, instance, workflowModule.getResourceController(instance));

					}
				}
				colonne++;
				if (row.getCell(colonne) != null) {
					debut = row.getCell(colonne).getDateCellValue();
				}
				colonne++;
				if (row.getCell(colonne) != null) {
					fin = row.getCell(colonne).getDateCellValue();
				}
				ligne++;
				colonne = 0;
				row = sheet.getRow(ligne);
				iLinkedResource = instance.createLinkedResource("Tache");
				iLinkedResource.setValue("id", iD);
				iLinkedResource.setValue("Projet", instance.getValue("Designation"));
				iLinkedResource.setValue("Phase", phase);
				iLinkedResource.setValue("SousPhase", sousPhase);
				iLinkedResource.setValue("Tache", tache);
				iLinkedResource.setValue("ChargeJ", nbrJours);
				iLinkedResource.setValue("NatureTache", nature);
				iLinkedResource.setValue("DateDebut", debut);
				iLinkedResource.setValue("DateFin", fin);
				iLinkedResource.setValue("Responsable", entiteuser);
				iLinkedResource.setValue("Budget", budget);
				iLinkedResource.save(context);
				instance.addLinkedResource(iLinkedResource);
			}
			// workflowModule.getResourceController(instance).alert("Modèle importé avec succés");
			// instance.setValue("Taches", null);
			instance.save(context);
			List<ILinkedResource> taches = (List<ILinkedResource>) instance.getLinkedResources("Tache");
			Collections.sort(taches, byDateDebut);
			instance.setValue("DateDebut2", taches.get(0).getValue("DateDebut"));
			Collections.sort(taches, byDatefin);
			instance.setValue("DateFin2", taches.get(taches.size() - 1).getValue("DateFin"));
			workflowModule.commitTransaction();
			// String lien =
			// Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort())
			// .concat(Turbine.getContextPath().concat("/easysite/workplace/reservoir-de-donnees/edit-document/").concat(instance.getId()+""));
			//
			// ExternalScreen externalScreen = new
			// ExternalScreen(lien.toString());
			// Navigator.getNavigator().setCurrentScreen(externalScreen);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
