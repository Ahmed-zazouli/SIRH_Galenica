package com.moovapps.EVALUATION.Views;

import com.axemble.vdoc.sdk.view.extensions.BaseViewExtension;
import com.axemble.vdoc.sdk.view.extensions.ViewItem;
import com.axemble.vdp.views.query.Definition;

import java.util.List;

public class FirstColumnTDClickable extends BaseViewExtension {
    public FirstColumnTDClickable() {
        super();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public boolean onPrepareView(Definition viewDefinition) {
        getResource();
        return super.onPrepareView(viewDefinition);
    }

    @Override
    public void onPrepareColumns(List viewModelColumns) {
        //getView().get
        super.onPrepareColumns(viewModelColumns);
    }

    @Override
    public String onPrepareSQL(String sqlQuery) {
        return super.onPrepareSQL(sqlQuery);
    }

    @Override
    public boolean onReady() {
        return super.onReady();
    }

    @Override
    public void onPrepareItem(ViewItem item) {
        try{
            item.getResource();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
