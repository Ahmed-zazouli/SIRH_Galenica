package com.moovapps.capone.rh.GestionDeConge.Vue;

import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.IResource;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.ui.core.document.CoreDocument;
import com.axemble.vdp.ui.framework.composites.xml.XMLChildDocument;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.views.query.Definition;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class VueDemandeCongeByJourFerie extends  BaseViewExtension  {

        @Override
        public void onPrepareItem(ViewItem item) {}

        @Override
        public boolean onPrepareView(Definition viewDefinition) {
            viewDefinition.getView().setItemsPerPage(BigInteger.valueOf(99999999));
            return super.onPrepareView(viewDefinition);
        }

        // Filter Document(WI) by
        @Override
        public String onPrepareSQL(String sqlQuery) {

            Calendar calendar = Calendar.getInstance();

            Date satartDateJ = (Date) getResource().getValue("DateDebutJourFerie");
            calendar.setTime(satartDateJ);

            int NJoursFeries =((Number) getResource().getValue("NJoursFeries")).intValue();
            calendar.add(Calendar.DAY_OF_MONTH , NJoursFeries - 1);
            Date endDateJ = calendar.getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String startDateJFormatted = formatter.format(satartDateJ);
            String endDateJFormatted = formatter.format(endDateJ);

//          SELECT wi.id, r.id, wi.workflow_id, wi.createdDate,r.sysReference,r.sysTitle,r.DateDeDebut,r.DateFinReel
//          FROM vdp_workflow_instance wi inner join r_worrh r on wi.resourceTable_id = r.id  inner join vdp_workflow w on wi.workflow_id = w.id
//          WHERE ((w.workflowContainer_id = ?  ) )

            sqlQuery = sqlQuery.replace("? ", "? AND " +
                    " ( '"+startDateJFormatted+"' BETWEEN r.DateDeDebut AND  r.DateFinReel ) OR\n" +
                    "    ( '"+endDateJFormatted+"' BETWEEN r.DateDeDebut AND  r.DateFinReel )");


            return super.onPrepareSQL(sqlQuery);
}

}
