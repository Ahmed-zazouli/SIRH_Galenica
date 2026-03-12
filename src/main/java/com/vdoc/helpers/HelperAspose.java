package com.vdoc.helpers;

import com.axemble.vdoc.sdk.interfaces.IConfiguration;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResourceController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
public class HelperAspose {
	public static void genererFicheEval(final IWorkflowModule workflowModule, final IWorkflowInstance workflowInstance, final IResourceController resourceController) {
	        try {
	            String cheminModeleWord = "";
	            if (workflowInstance.getValue("GroupeHiearchique2") != null) {

					if (workflowInstance.getValue("GroupeHiearchique2").equals("Enseignants")) {
						cheminModeleWord = "Modèles/Fiche_Evaluation Enseignant_CL_VF.docx";

	                }else if (workflowInstance.getValue("GroupeHiearchique2").equals("Personnel de administratif")) {
						cheminModeleWord = "Modèles/Fiche_Evaluation Personnel administratif_VF.docx";

	                }else  if (workflowInstance.getValue("GroupeHiearchique2").equals("Personnel de direction")){
						cheminModeleWord = "Modèles/Fiche_Evaluation_Personnel direction_CPE_VF.docx";
					}

	            }
	            workflowInstance.save(workflowModule.getLoggedOnUserContext());
	            final IConfiguration configuration = workflowModule.getConfiguration();
	            final String champPJName = "PjLancement";
				String codeFiche = (String) workflowInstance.getValue(IProperty.System.REFERENCE);
	            final String titrePJOutput = "ApplicationEntretienDEvaluation_" + codeFiche;
	            final Aspose aspose = new Aspose();
	            aspose.GenerationDuDocumentWord(workflowModule, workflowInstance, resourceController, cheminModeleWord, champPJName, titrePJOutput);
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

}
