package com.moovapps.EVALUATION.TableuxDynamiques;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IUser;

public class Notes extends BaseDocumentExtension {

    @Override
    public boolean onBeforeLoad() {
        if(getWorkflowInstance().getParentInstance()!=null){
            IUser collaborateur = (IUser) getWorkflowInstance().getParentInstance().getValue("CollaborateurEval");
            IUser N1 = (IUser) getWorkflowInstance().getParentInstance().getValue("ResponsableHierarchique");
            if(collaborateur!=null){
                if(collaborateur.equals(getWorkflowModule().getLoggedOnUser())){
                    getWorkflowInstance().setValue("isCollaborateur",true);
                    getWorkflowInstance().setValue("isN1",false);

                }
            }
            if(N1!=null){
                if(N1.equals(getWorkflowModule().getLoggedOnUser())){
                    getWorkflowInstance().setValue("isN1",true);
                    getWorkflowInstance().setValue("isCollaborateur",false);

                }
            }
        }
        return super.onBeforeLoad();
    }


}
