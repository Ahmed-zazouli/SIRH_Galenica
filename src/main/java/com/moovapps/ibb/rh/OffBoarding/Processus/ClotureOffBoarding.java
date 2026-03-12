package com.moovapps.ibb.rh.OffBoarding.Processus;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IOrganization;
import com.axemble.vdoc.sdk.interfaces.IResourceController;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;

import java.util.List;

public class ClotureOffBoarding extends BaseDocumentExtension {

    @Override
    public boolean onAfterLoad() {
        try {
            IUser connectedUser = getWorkflowModule().getLoggedOnUser();
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "IEGGroupes");
            if(!getDirectoryModule().isMemberOf(connectedUser, getDirectoryModule().getGroup(context,organization,"RH" ), true)){
                IResourceController iResourceController = getResourceController();
                NamedContainer namedContainer = iResourceController.getButtonContainer(2);
                List<IWidget> widgets = namedContainer.getWidgets();
                for (IWidget iWidget : widgets) {
                    CtlButton button = (CtlButton) iWidget;
                    button.setHidden(true);
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return super.onAfterLoad();
    }
}
