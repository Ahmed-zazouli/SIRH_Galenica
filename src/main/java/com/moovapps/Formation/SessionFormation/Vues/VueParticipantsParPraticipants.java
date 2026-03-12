package com.moovapps.Formation.SessionFormation.Vues;

import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;

public class VueParticipantsParPraticipants extends BaseViewExtension {

    @Override
    public String onPrepareSQL(String sqlQuery) {

        //sqlQuery = sqlQuery.replace("( ? =", "( ?=5 and r.Profil in");
        sqlQuery = sqlQuery.replace("( ? =", "( ?=5 and r.Salarie in");


        return super.onPrepareSQL(sqlQuery);
    }

    @Override
    public void onPrepareItem(ViewItem item) {


    }
}
