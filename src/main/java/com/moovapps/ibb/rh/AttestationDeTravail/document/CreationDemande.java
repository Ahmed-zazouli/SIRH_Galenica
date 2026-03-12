package com.moovapps.ibb.rh.AttestationDeTravail.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

public class CreationDemande extends BaseDocumentExtension {
  IWorkflowInstance document = null;
  
  public boolean onAfterLoad() {
    this.document = getWorkflowInstance();
    return super.onAfterLoad();
  }
  
  public void onPropertyChanged(IProperty property) {
    if (property.getName().equals("Societe")) {
     // String societeName = (String)this.document.getValue("Societe2");
      IStorageResource societe = (IStorageResource) this.document.getValue("Societe");
      if (societe != null) {
        this.document.setValue("AdressSociete", societe.getValue("Adresse"));
        this.document.setValue("Place", societe.getValue("Ville"));
      } 
    } 
    super.onPropertyChanged(property);
  }
  
}
