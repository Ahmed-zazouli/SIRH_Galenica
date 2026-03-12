package com.moovapps.Referentiels;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;

public class CompetencePoste extends BaseDocumentExtension {

    @Override
    public boolean onBeforeLoad() {
        IStorageResource fonction = (IStorageResource) getWorkflowInstance().getValue("Fonction");
        getWorkflowInstance().setValue("Pole",fonction!=null?fonction.getValue("Pole"):null);
        getWorkflowInstance().setValue("Societe",fonction!=null?fonction.getValue("Societe"):null);

        return super.onBeforeLoad();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if(property.getName().equals("Fonction")){
            IStorageResource fonction = (IStorageResource) getWorkflowInstance().getValue("Fonction");
            getWorkflowInstance().setValue("Pole",fonction!=null?fonction.getValue("Pole"):null);
            getWorkflowInstance().setValue("Societe",fonction!=null?fonction.getValue("Societe"):null);

        }
        super.onPropertyChanged(property);
    }
}
