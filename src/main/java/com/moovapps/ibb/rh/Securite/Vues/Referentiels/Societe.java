package com.moovapps.ibb.rh.Securite.Vues.Referentiels;

import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;

public class Societe extends BaseViewExtension {
    @Override
    public String onPrepareSQL(String sqlQuery) {

        sqlQuery = sqlQuery.replace("r.sysTitle = ?","r.sysTitle in (?)");
        sqlQuery = sqlQuery.replace("r.sysTitle =( ? , ? )","r.sysTitle in( ? , ? )");


        return super.onPrepareSQL(sqlQuery);
    }

    @Override
    public void onPrepareItem(ViewItem item) {

    }
}
