package com.moovapps.Amelioration.PlanAction;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;

import java.util.ArrayList;
import java.util.HashMap;
//import com.moovapps.Orga.Helpers.AddFilesToDoCenter;

public class RealisationAction extends BaseDocumentExtension{
	@Override
	public boolean onBeforeSubmit(IAction action) {
		// TODO Auto-generated method stub
		if(action.getName().equals("Evaluer")){
		//	AddFilesToDoCenter FilesToDoCenter = new AddFilesToDoCenter();

			
			if(getWorkflowInstance().getParentInstance()!=null){
				HashMap<String, ArrayList<String>> fieldsPaths1 = new HashMap<String,  ArrayList<String> >();
				HashMap<String, ArrayList<String>> fieldsPaths2 = new HashMap<String,  ArrayList<String> >();
				ArrayList<String> paths1 = new ArrayList<>();
				ArrayList<String> paths2 = new ArrayList<>();
				ArrayList<IStorageResource> processusConcerne = (ArrayList<IStorageResource>) getWorkflowInstance().getParentInstance().getValue("ProcessussConcernes");
				if(processusConcerne==null || processusConcerne.isEmpty()) return true;
				String reference = (String) getWorkflowInstance().getParentInstance().getValue("sys_Reference");
				for(IStorageResource proc : processusConcerne){
					String procName = (String) proc.getValue("sys_Title");
					IStorageResource departement = (IStorageResource) proc.getValue("Departement");
					String depName = departement!=null?(String) departement.getValue("sys_Title"):"NA"; 
					paths1.add("_Societe/Processus/"+depName+"/"+procName+"/"+"Fiches d'amélioration/"+reference+"/Plans d'action amélioration/_Origine2/_TitreAction/Piéce jointe 1/_Reference");
					paths2.add("_Societe/Processus/"+depName+"/"+procName+"/"+"Fiches d'amélioration/"+reference+"/Plans d'action amélioration/_Origine2/_TitreAction/Piéce jointe 2/_Reference");

				}
				fieldsPaths1.put("PieceJoint2", paths1);
				fieldsPaths2.put("PieceJointe8", paths2);
				//FilesToDoCenter.AddFileToDocCenterMultiplePaths("Documents SMQ", getWorkflowModule(), getDirectoryModule(), getWorkflowInstance(), fieldsPaths1);
				//FilesToDoCenter.AddFileToDocCenterMultiplePaths("Documents SMQ", getWorkflowModule(), getDirectoryModule(), getWorkflowInstance(), fieldsPaths2);
			}else{
				HashMap<String, String> fieldsPaths = new HashMap<String,  String >();
				HashMap<String, String> fieldsPaths2 = new HashMap<String,  String >();

				fieldsPaths.put("PieceJoint2", "_Societe/_year/Plans d'action amélioration/_Origine2/_TitreAction/Piéce jointe 1/_Reference");
				//FilesToDoCenter.AddFileToDocCenter("Documents SMQ", getWorkflowModule(), getDirectoryModule(), getWorkflowInstance(), fieldsPaths);
				
				fieldsPaths2.put("PieceJointe8", "_Societe/_year/Plans d'action amélioration/_Origine2/_TitreAction/Piéce jointe 2/_Reference");
			//	FilesToDoCenter.AddFileToDocCenter("Documents SMQ", getWorkflowModule(), getDirectoryModule(), getWorkflowInstance(), fieldsPaths2);
			}
		}
		return super.onBeforeSubmit(action);
	}
}
