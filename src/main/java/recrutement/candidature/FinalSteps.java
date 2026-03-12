package recrutement.candidature;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IUser;

import java.util.Arrays;
import java.util.List;

public class FinalSteps extends BaseDocumentExtension {
    //all actions that genere les docs
    List<String> actions = Arrays.asList("ValiderLeCandidat","Envoyer");

    @Override
    public boolean onAfterSubmit(IAction action) {
        if (actions.contains(action.getName())) {
            new AddFilesToDoCenter().AddFileToDocCenter(getWorkflowModule(), getDirectoryModule(), getWorkflowInstance());
            getWorkflowInstance().save(getWorkflowModule().getLoggedOnUserContext());
        }
        return super.onAfterSubmit(action);
    }

}
