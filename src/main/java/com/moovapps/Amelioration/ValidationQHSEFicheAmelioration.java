package com.moovapps.Amelioration;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;
import java.util.HashMap;
//import com.moovapps.Orga.Helpers.AddFilesToDoCenter;

public class ValidationQHSEFicheAmelioration extends BaseDocumentExtension{
	
	@Override
	public boolean onBeforeSubmit(IAction action) {
		if(action.getName().equals("Valider")){
		/*	HashMap<String, ArrayList<String>> fieldsPaths = new HashMap<String,  ArrayList<String> >();
			ArrayList<String> paths = new ArrayList<>();
			
			ArrayList<IStorageResource> processusConcerne = (ArrayList<IStorageResource>) getWorkflowInstance().getValue("ProcessussConcernes");
			if(processusConcerne==null || processusConcerne.isEmpty()) return true;
			
			for(IStorageResource proc : processusConcerne){
				String procName = (String) proc.getValue("sys_Title");
				IStorageResource departement = (IStorageResource) proc.getValue("Departement");
				String depName = departement!=null?(String) departement.getValue("sys_Title"):"NA";
				paths.add("_Societe/Processus/"+depName+"/"+procName+"/"+"Fiches d'amélioration/_Reference");
			}
			fieldsPaths.put("PieceJoint", paths);*/

			//AddFilesToDoCenter FilesToDoCenter = new AddFilesToDoCenter();
			//FilesToDoCenter.AddFileToDocCenterMultiplePaths("Documents SMQ", getWorkflowModule(), getDirectoryModule(), getWorkflowInstance(), fieldsPaths);
			
			ArrayList<IWorkflowInstance> actionsImmediates = (ArrayList<IWorkflowInstance>) getWorkflowInstance().getLinkedWorkflowInstances("ActionsImmediates2");
			for(IWorkflowInstance actionImmediate : actionsImmediates){
				if(!actionImmediate.getValue("DocumentState").equals("créée")){
					continue;
				}
				PassToTheRealsiationEtape(actionImmediate);
				
			}
		}
		return super.onBeforeSubmit(action);
	}
	
	
	void PassToTheRealsiationEtape(IWorkflowInstance instance){
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            ITaskInstance taskInstance = instance.getCurrentTaskInstance(context);
            if(taskInstance!=null){
                ITask task = taskInstance.getTask();
                if(task!=null){
                    IAction iAction1 = task.getAction("PassezVersLaRealisation");
                    if(iAction1!=null){
                        getWorkflowModule().end(context, taskInstance, iAction1, "");
                       // instance.setValue("DocumentState","En transmission");
                        //clear etape 2 formualaire
                    }
                    instance.save(context);

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
	
	
	@Override
	public boolean onAfterSubmit(IAction action) {
		// TODO Auto-generated method stub
		if(action.getName().equals("DysfonctionnementNonFonde")){
			getWorkflowInstance().setValue("DocumentState", "Fiche non fondé");
			getWorkflowInstance().save("DocumentState");
		}
		return super.onAfterSubmit(action);
	}

}
