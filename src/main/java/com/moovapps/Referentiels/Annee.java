package com.moovapps.Referentiels;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;

public class Annee extends BaseDocumentExtension {
    @Override
    public boolean onBeforeSave() {
        try{
            String string = getWorkflowInstance().getValue("sys_Title")!=null?(String)getWorkflowInstance().getValue("sys_Title"):"";
            int valeur = Integer.parseInt(string);
            getWorkflowInstance().setValue("Valeur",valeur);
        }catch (Exception e){
            e.printStackTrace();
            getResourceController().alert("Veuillez saisir un nombre");
            return false;
        }
        return super.onBeforeSave();
    }
}
