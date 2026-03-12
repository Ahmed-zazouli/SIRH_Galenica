package com.moovapps.ibb.rh.OnBoarding.Processus.TableauxDynamique;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;

public class Parcours extends BaseDocumentExtension {

    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("Interlocuteur")){
            IUser Interlocuteur = (IUser) getWorkflowInstance().getValue("Interlocuteur");
            getWorkflowInstance().setValue("Fonction",Interlocuteur.getExtendedAttributes().getValue("Fonction"));
            getWorkflowInstance().setValue("Departement",Interlocuteur.getExtendedAttributes().getValue("Departement"));
        }
        super.onPropertyChanged(property);
    }
}
