package com.moovapps.Formation.PlanFormation.Vues;

import com.axemble.studio.providers.workflowInstances.views.WorkflowInstanceViewProvider;
import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.XMLViewModel;
import com.axemble.vdp.view.transformer.IViewTransformer;
import com.axemble.vdp.view.transformer.ProcessViewTransformer;
import com.axemble.vdp.views.query.Column;
import com.axemble.vdp.views.query.Definition;

import java.util.List;

public class VueDivisionParDepartement extends BaseViewExtension {


    @Override
    public String onPrepareSQL(String sqlQuery){
        sqlQuery = sqlQuery.replace("( ? =", "( ?=5 and r.Departement in");


        return super.onPrepareSQL(sqlQuery);
    }

    @Override
    public void onPrepareItem(ViewItem item) {

    }


}
