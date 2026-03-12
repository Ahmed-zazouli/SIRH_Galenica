package com.moovapps.Referentiels;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;

public class LienParente extends BaseDocumentExtension {

    @Override
    public boolean onBeforeLoad() {
        return super.onBeforeLoad();
    }

    @Override
    public boolean onBeforeSave() {
        if(getWorkflowInstance().getValue("sys_Reference")==null){
            IStorageResource collaborateur =(IStorageResource) getWorkflowInstance().getValue("FicheSalarie");
            int nombreEnfant = collaborateur!=null?((Number) collaborateur.getValue("NombreEnfants")).intValue():0;
            if(collaborateur!=null){
                collaborateur.setValue("NombreEnfants",nombreEnfant+1);
                collaborateur.save("NombreEnfants");
            }
        }
        return super.onBeforeSave();
    }
}
