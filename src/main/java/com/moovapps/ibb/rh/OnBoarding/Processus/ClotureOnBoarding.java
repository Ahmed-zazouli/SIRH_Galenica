package com.moovapps.ibb.rh.OnBoarding.Processus;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.ui.IWidget;
import com.axemble.vdp.ui.framework.runtime.NamedContainer;
import com.axemble.vdp.ui.framework.widgets.CtlButton;
import com.moovapps.Reprise.Referentiels.Collaborateur;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClotureOnBoarding extends BaseDocumentExtension {

    @Override
    public boolean onAfterLoad() {
        try {
            IUser connectedUser = getWorkflowModule().getLoggedOnUser();
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "IEGGroupes");
            if (false && !getDirectoryModule().isMemberOf(connectedUser, getDirectoryModule().getGroup(context, organization, "DRH"), true)) {
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
