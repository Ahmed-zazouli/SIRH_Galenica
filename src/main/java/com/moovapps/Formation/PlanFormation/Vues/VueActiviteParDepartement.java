package com.moovapps.Formation.PlanFormation.Vues;

import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;

public class VueActiviteParDepartement extends BaseViewExtension {


    @Override
    public String onPrepareSQL(String sqlQuery){
        sqlQuery = sqlQuery.replace("( ? =", "( ?=5 and r.Departement in");


        return super.onPrepareSQL(sqlQuery);
    }

    @Override
    public void onPrepareItem(ViewItem item) {

    }


}
