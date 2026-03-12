package recrutement.candidature;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

public class GenerationContratTravaille extends BaseDocumentExtension {

    @Override
    public boolean onAfterSubmit(IAction action) {
        if (action.getName().equals("ValiderLeCandidat")) {
            IWorkflowInstance poste = getWorkflowInstance().getParentInstance();
            if (poste.getValue("NombreRecrue") != null) {
                int NombreRecrue = ((Number) poste.getValue("NombreRecrue")).intValue();
                if (NombreRecrue >= 0) {
                    NombreRecrue= NombreRecrue+1;
                    poste.setValue("NombreRecrue", NombreRecrue);
                    poste.save(getWorkflowModule().getLoggedOnUserContext());
                }

            }




        }
        return super.onAfterSubmit(action);
    }
}







