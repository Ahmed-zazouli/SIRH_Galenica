package com.moovapps.EVALUATION.Views;

import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IStorageKey;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.workflow.extensions.BaseResourceDefinitionExtension;

public class FirstColumnTDClickableV3 extends BaseResourceDefinitionExtension {
    public FirstColumnTDClickableV3() {
        super();
    }

    @Override
    public String onGenerateReference(String generatedReference) {
        return super.onGenerateReference(generatedReference);
    }

    @Override
    public boolean onBeforeCreate() {
        return super.onBeforeCreate();
    }

    @Override
    public boolean onBeforeSave() {
        return super.onBeforeSave();
    }

    @Override
    public boolean onAfterSave() {
        return super.onAfterSave();
    }

    @Override
    public void onRemove(IStorageKey key) {
        super.onRemove(key);
    }

    @Override
    public boolean onStartRemoving() {
        return super.onStartRemoving();
    }

    @Override
    public void onPropertyChanged(IProperty property, IUser user) {
        super.onPropertyChanged(property, user);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
