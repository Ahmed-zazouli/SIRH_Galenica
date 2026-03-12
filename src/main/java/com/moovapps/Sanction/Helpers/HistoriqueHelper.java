package com.moovapps.Sanction.Helpers;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HistoriqueHelper {

    public void UpdateRessource(IResource historique, IResource instance, HashMap<String,String> fields){
        if(historique ==null || instance ==null) return;
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if(key.startsWith("_"))
            {
                try {
                    String newKey = key.replaceFirst("_", "");
                    Method method = this.getClass().getMethod(key,IResource.class , String.class);
                    Object obj = method.invoke(this,instance, value); //
                    historique.setValue(newKey, obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                historique.setValue(key, instance.getValue(value));
            }
        }
        historique.save(Modules.getWorkflowModule().getLoggedOnUserContext());

    }

    public String _ID(IResource instance , String value){
        return instance.getId().toString();
    }
    public IWorkflowInstance _SanctionSource(IResource instance , String value){
        return (IWorkflowInstance) instance;
    }

}
