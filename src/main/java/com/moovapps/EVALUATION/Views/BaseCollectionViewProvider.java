package com.moovapps.EVALUATION.Views;

import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdoc.sdk.providers.BaseViewProvider;
import com.axemble.vdp.ui.core.providers.ICollectionViewProvider;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;

public abstract class BaseCollectionViewProvider<T> extends BaseViewProvider implements ICollectionViewProvider<T> {
    public BaseCollectionViewProvider(INavigateContext context, CtlAbstractView view) {
        super(context, view);
    }
}