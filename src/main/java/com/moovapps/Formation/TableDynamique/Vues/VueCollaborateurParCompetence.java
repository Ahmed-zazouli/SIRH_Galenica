package com.moovapps.Formation.TableDynamique.Vues;

import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;

public class VueCollaborateurParCompetence extends BaseViewExtension {

    @Override
    public void onPrepareItem(ViewItem item) {

    }

    @Override
    public String onPrepareSQL(String sqlQuery) {


        if(!sqlQuery.contains("COUNT")){
            sqlQuery = sqlQuery.replace("SELECT ", "SELECT DISTINCT ");
        }
        sqlQuery = sqlQuery.replace("FROM r_stofichecolla r WHERE", "FROM r_stofichecolla r LEFT JOIN r_stocompetence2 cc on ( r.id = cc.Collaborateur and 1 = ? and cc.Competence = ? )  WHERE");

        sqlQuery = sqlQuery.replace("WHERE ((((( ? = ? ) AND (( ? = ? ) OR (( ? = ? ) AND ( ? = ? ) ) ) ) ) ) )", "WHERE ((((( ? = 5 AND  'true' = ? and cc.Competence is null ) OR (( 4= ? and cc.Competence = ? ) AND (3 = ? AND cc.NiveauDeCompetence = ? ) ) ) ) ) )");
        return super.onPrepareSQL(sqlQuery);
    }
}
