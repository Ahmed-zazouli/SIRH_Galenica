package com.moovapps.Formation;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

import java.util.ArrayList;

public class ClearCompetenceFromSuiviProgramme extends BaseAgent {
    @Override
    protected void execute() {
        ArrayList<IWorkflowInstance> data = getData();
        if(data !=null && !data.isEmpty()){
            for(IWorkflowInstance item : data){
                item.setValue("Competence",null);
                item.save("Competence");
            }
        }
    }

    private ArrayList<IWorkflowInstance> getData() {
        return null;
    }
}
