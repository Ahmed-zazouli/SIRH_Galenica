package com.moovapps.capone.rh.GestionDeConge.Vue;

import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.views.query.Definition;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class VueRelaisRH extends  BaseViewExtension  {

        @Override
        public void onPrepareItem(ViewItem item) {}

        @Override
        public boolean onPrepareView(Definition viewDefinition) {
            //viewDefinition.getView().setItemsPerPage(BigInteger.valueOf(99999999));
            return super.onPrepareView(viewDefinition);
        }

    @Override
    public String onPrepareSQL(String sqlQuery) {

        sqlQuery = sqlQuery.replace("FROM r_stofichecolla r JOIN vdp_resource_table rt ON r.id = rt.id LEFT JOIN vdp_resource_table_sec security ON rt.securityId = security.securityId WHERE ((((( ? = ? ) ) ) ) )", "FROM r_stofichecolla r LEFT JOIN r_stodepartemen t0 ON t0.id = r.Departement  JOIN vdp_resource_table rt ON r.id = rt.id LEFT JOIN vdp_resource_table_sec security ON rt.securityId = security.securityId WHERE  ?=5 and ? in (select link_id from r_stodepartemen_RelaisRH rsr where rsr.resource_id =t0.id )");

        sqlQuery = sqlQuery.replace("FROM r_stofichecolla r JOIN vdp_resource_table rt ON r.id = rt.id LEFT JOIN vdp_resource_table_sec security ON rt.securityId = security.securityId WHERE ((((( ? = ? ) ) ) ) )", "FROM r_stofichecolla r LEFT JOIN r_stodepartemen t0 ON t0.id = r.Departement  JOIN vdp_resource_table rt ON r.id = rt.id LEFT JOIN vdp_resource_table_sec security ON rt.securityId = security.securityId WHERE  ?=5 and ? in (select link_id from r_stodepartemen_RelaisRH rsr where rsr.resource_id =t0.id )");
       //sqlQuery = sqlQuery.replace("((((t0.r_stodepartemen_RelaisRH = ?  ) ) ) )", "? in (select link_id from r_stodepartemen_RelaisRH rsr where rsr.resource_id = t0.id) ");
       //sqlQuery = sqlQuery.replace("AND permissionLevel >= ? AND  (subjectUri IN ('uri://vdoc/user/22794','ALL','uri://vdoc/group/10113','uri://vdoc/group/1','uri://vdoc/group/10114','uri://vdoc/group/10112','uri://vdoc/group/10117','uri://vdoc/group/10115','uri://vdoc/group/10116','uri://vdoc/group/10122','uri://vdoc/group/10119','uri://vdoc/group/10120','uri://vdoc/group/10123','uri://vdoc/group/21','uri://vdoc/group/85','uri://vdoc/group/90','uri://vdoc/group/10103','uri://vdoc/group/93','uri://vdoc/group/91','uri://vdoc/group/92','uri://vdoc/profile/519','uri://vdoc/profile/512','uri://vdoc/profile/1529','uri://vdoc/profile/1401','uri://vdoc/profile/504','uri://vdoc/profile/368','uri://vdoc/profile/496','uri://vdoc/profile/1132','uri://vdoc/profile/1131','uri://vdoc/profile/11557','uri://vdoc/profile/546','uri://vdoc/profile/548','uri://vdoc/profile/291','uri://vdoc/profile/542','uri://vdoc/profile/11545','uri://vdoc/profile/281','uri://vdoc/profile/537','uri://vdoc/profile/11542','uri://vdoc/profile/534','uri://vdoc/profile/280','uri://vdoc/profile/275','uri://vdoc/profile/11536','uri://vdoc/profile/272','uri://vdoc/profile/1423','uri://vdoc/profile/198','uri://vdoc/profile/200','uri://vdoc/profile/1095','uri://vdoc/profile/186','uri://vdoc/profile/1081','uri://vdoc/profile/1083','uri://vdoc/profile/59','uri://vdoc/profile/182','uri://vdoc/profile/181','uri://vdoc/profile/1077','uri://vdoc/profile/952','uri://vdoc/profile/1080','uri://vdoc/profile/183','uri://vdoc/profile/50','uri://vdoc/profile/560','uri://vdoc/profile/1125','uri://vdoc/profile/868','uri://vdoc/profile/1124','uri://vdoc/profile/1123','uri://vdoc/profile/1117','uri://vdoc/profile/1115','uri://vdoc/profile/1362','uri://vdoc/profile/206','uri://vdoc/profile/461','uri://vdoc/profile/1360','uri://vdoc/profile/1359','uri://vdoc/profile/202','uri://vdoc/profile/1099')) AND (security.organizationPath IS NULL OR 'Organization:1/Organization:2/Organization:43/' LIKE security.organizationPath OR security.organizationPath LIKE 'Organization:1/Organization:2/Organization:43/%') AND (security.localizationPath IS NULL OR 'Localization:1/' LIKE security.localizationPath OR security.localizationPath LIKE 'Localization:1/%') AND (security.dynamicFilter = 0 OR rt.securityPath LIKE 'Organization:1/%' )", " ");
        return super.onPrepareSQL(sqlQuery);
    }

}
