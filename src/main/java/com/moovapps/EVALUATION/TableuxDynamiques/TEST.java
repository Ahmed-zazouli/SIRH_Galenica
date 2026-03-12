package com.moovapps.EVALUATION.TableuxDynamiques;

import com.axemble.vdoc.sdk.workflow.extensions.BaseResourceDefinitionExtension;

public class TEST extends BaseResourceDefinitionExtension {
    @Override
    public boolean onAfterSave() {
        getResource();
        return super.onAfterSave();
    }

    @Override
    public boolean onBeforeSave() {
        getResource();
        return super.onBeforeSave();
    }


    @Override
    public String onGenerateReference(String generatedReference) {
        return super.onGenerateReference(generatedReference);
    }
}
