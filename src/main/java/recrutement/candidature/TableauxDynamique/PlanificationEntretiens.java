package recrutement.candidature.TableauxDynamique;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;

public class PlanificationEntretiens extends BaseDocumentExtension {
    @Override
    public void onPropertyChanged(IProperty property) {
        if(property.getName().equals("ordre")){
            //if(property.)
        }
        super.onPropertyChanged(property);
    }
}
