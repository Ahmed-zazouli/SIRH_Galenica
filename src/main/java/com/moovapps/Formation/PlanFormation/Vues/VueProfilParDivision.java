package com.moovapps.Formation.PlanFormation.Vues;

import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;

public class VueProfilParDivision extends BaseViewExtension {
    @Override
    public void onPrepareItem(ViewItem item) {

    }

    @Override
    public String onPrepareSQL(String sqlQuery) {
        sqlQuery = sqlQuery.replace("( ? =", "( ?=5 and r.Division in");

        return super.onPrepareSQL(sqlQuery);
    }
}
