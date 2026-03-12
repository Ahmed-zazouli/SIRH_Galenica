package com.moovapps.ibb.rh.OnBoarding.Processus.Referenciel;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;

public class ParcoursDIntegration extends BaseDocumentExtension {
    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("collaborateur")){
            IStorageResource collaborateur = (IStorageResource) getWorkflowInstance().getValue("collaborateur");

            getWorkflowInstance().setValue("Interlocuteur",collaborateur.getValue("Salarie"));
            getWorkflowInstance().setValue("Fonction",collaborateur.getValue("Fonction"));
            getWorkflowInstance().setValue("Departement",collaborateur.getValue("Departement"));
        }
        super.onPropertyChanged(property);
    }
}
