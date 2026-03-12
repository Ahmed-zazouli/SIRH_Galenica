package com.moovapps.Helpers;

import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

public class Utils {

    private void setTargetNullOnSourceChange(IWorkflowInstance instance, IProperty property, String source, String target){
        if(property.getName().equals(source)){
            instance.setValue(target,null);
        }
    }
}
