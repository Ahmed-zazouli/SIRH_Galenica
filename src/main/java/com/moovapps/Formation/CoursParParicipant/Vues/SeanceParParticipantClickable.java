package com.moovapps.Formation.CoursParParicipant.Vues;

import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.widgets.CtlHyperLink;
import com.axemble.vdp.ui.framework.widgets.CtlText;
import org.apache.turbine.Turbine;

public class SeanceParParticipantClickable extends BaseViewExtension {
    @Override
    public void onPrepareItem(ViewItem item) {
        setParticipantClickable(item);
    }

    private void setParticipantClickable(ViewItem item) {
        try
        {
            ViewModelItem viewModelItem = item.getViewModelItem();
            String link = "/moovapps/easysite/workplace/applications/application-referentiels-0/gp/edit-document/" + item.getResource().getId().toInt();
            CtlHyperLink ctlHyperLink = new CtlHyperLink("link", new CtlText((String) viewModelItem.getValue("Participant2.fullName")));
            ctlHyperLink.setUrl(link);
//            String lien = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort());
            viewModelItem.setValue("Participant2.fullName", ctlHyperLink);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
