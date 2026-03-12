package com.moovapps.Sanction;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;

import java.util.ArrayList;
import java.util.Date;

public class CreationSanction extends BaseDocumentExtension {
    @Override
    public boolean onBeforeLoad() {
        if(getWorkflowInstance().getValue("ID")==null){
            getWorkflowInstance().setValue("ID",getWorkflowInstance().getId().toString());
        }
        if(getWorkflowInstance().getValue("Declarants")==null){
            ArrayList<IUser> declarants = new ArrayList<>();
            declarants.add(getWorkflowInstance().getCreatedBy());
           // getWorkflowInstance().setValue("Declarants",declarants);
        }
        if(getWorkflowInstance().getValue("sys_Title")==null){
            getWorkflowInstance().setValue("sys_Title",getWorkflowInstance().getValue("sys_Reference"));
        }
        if(getWorkflowInstance().getValue("Date1EreDeclaration")==null){
            getWorkflowInstance().setValue("Date1EreDeclaration",new Date());
        }
        if(getWorkflowInstance().getValue("DateDeclaration")==null){
          //  getWorkflowInstance().setValue("DateDeclaration",new Date());
        }
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        return super.onBeforeLoad();
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("PersonneConcerneeFicheCollaborateur")){
            if (getWorkflowInstance().getValue("PersonneConcerneeFicheCollaborateur") != null){
                IUser personneConcernee = (IUser) ((IStorageResource) getWorkflowInstance().getValue("PersonneConcerneeFicheCollaborateur")).getValue("Salarie");
                getWorkflowInstance().setValue("PersonneConcernee" ,personneConcernee);
            }
        }
        super.onPropertyChanged(property);
    }
}
