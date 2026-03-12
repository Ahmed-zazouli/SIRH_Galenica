package com.moovapps.Sanction.HTMLButtons;

import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IResourceController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.foundation.screens.ExternalScreen;
import org.apache.turbine.Turbine;

public class VisiterSanctionSource {


    public void VisiterSource(IResource getWorkflowInstance, IWorkflowModule getWorkflowModule, IResourceController controller) {

        IWorkflowInstance sanctionSource  = (IWorkflowInstance) getWorkflowInstance.getValue("SanctionSource");
        if(sanctionSource!=null){
            String lienSanction = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort()).concat(Turbine.getContextPath().concat(sanctionSource.getURI()));
            ExternalScreen externalScreen = new ExternalScreen(lienSanction);
            Navigator.getNavigator().pushScreen(externalScreen);
        }

    }

    public void VisiterEvolution(IResource getWorkflowInstance, IWorkflowModule getWorkflowModule, IResourceController controller) {

        IWorkflowInstance sanctionEvoluer  = (IWorkflowInstance) getWorkflowInstance.getValue("SanctionEvolution");
        if(sanctionEvoluer!=null){
            String lienSanction = Turbine.getServerScheme().concat("://").concat(Turbine.getServerName()).concat(":").concat(Turbine.getServerPort()).concat(Turbine.getContextPath().concat(sanctionEvoluer.getURI()));
            ExternalScreen externalScreen = new ExternalScreen(lienSanction);
            Navigator.getNavigator().pushScreen(externalScreen);
        }

    }
}
