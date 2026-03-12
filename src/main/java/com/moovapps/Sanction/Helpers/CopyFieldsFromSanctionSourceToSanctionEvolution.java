package com.moovapps.Sanction.Helpers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.IResource;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CopyFieldsFromSanctionSourceToSanctionEvolution {

    public void Copy(IResource sanctionEvolution, IResource sanctionSource, HashMap<String,String> fields){
        if(sanctionEvolution ==null || sanctionSource ==null) return;
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if(key.startsWith("_"))
            {
                try {
                    String newKey = key.replaceFirst("_", "");
                    Method method = this.getClass().getMethod(key,IResource.class , String.class);
                    Object obj = method.invoke(this,sanctionSource, value); //
                    sanctionEvolution.setValue(newKey, obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                sanctionEvolution.setValue(key, sanctionSource.getValue(value));
            }
        }

        sanctionEvolution.save(Modules.getWorkflowModule().getLoggedOnUserContext());

    }
}
