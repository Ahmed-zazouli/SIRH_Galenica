package com.moovapps.ibb.rh.Securite.Vues;

import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;

public class FilterVuesRHGeneral extends BaseViewExtension {

    @Override
    public String onPrepareSQL(String sqlQuery) {

        sqlQuery = sqlQuery.replace("r.Societe = ?","r.Societe in (?)");
        sqlQuery = sqlQuery.replace("r.Societe =( ? , ? )","r.Societe in( ? , ? )");


        return super.onPrepareSQL(sqlQuery);
    }

    @Override
    public void onPrepareItem(ViewItem item) {

    }
}
