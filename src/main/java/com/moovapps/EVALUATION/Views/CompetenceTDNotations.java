package com.moovapps.EVALUATION.Views;

import com.axemble.vdoc.sdk.exceptions.ModuleException;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdp.ui.core.document.CoreDocument;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.axemble.vdp.ui.framework.composites.xml.XMLChildDocument;
import com.axemble.vdp.ui.framework.foundation.Navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CompetenceTDNotations extends BaseCollectionViewProvider<IStorageResource>{
    public CompetenceTDNotations(INavigateContext context, CtlAbstractView view) {
        super(context, view);
    }

    @Override
    public Collection<IStorageResource> getObjects() {
        List<IStorageResource> details = new ArrayList<>();
        try {
            CoreDocument coreDocument = (CoreDocument) ((XMLChildDocument) Navigator.getNavigator().getParentNavigator().getCurrentScreen().getRootWidget()).getContext().getParameterMap().get("document");
            if (coreDocument != null) {
                IResource resource = this.getWorkflowModule().getResource(coreDocument);
                if (resource instanceof ILinkedResource) {
                 //   IWorkflowInstance parentInstance = ((ILinkedResource)resource).getParentInstance();
                    IStorageResource competence = (IStorageResource) resource.getValue("Competence");
                    if (competence != null) {
                        Collection<IStorageResource> notationCompetences = getNotationsByCompetence(competence);
                        details.addAll(notationCompetences);
                    }
                }
            }
        } catch (ModuleException moduleException) {
            System.err.println(moduleException);
        }
        return details;
    }

    @Override
    public ViewModelItem fetchLine(IStorageResource resource) {
        ViewModelItem viewModelItem = new ViewModelItem(resource);
        viewModelItem.setValue("Notation", resource.getValue("Titre"));
        viewModelItem.setValue("Definition", resource.getValue("Definition"));
        viewModelItem.setValue("Valeur", resource.getValue("Valeur"));

        return viewModelItem;
    }

    private ArrayList<IStorageResource> getNotationsByCompetence(IStorageResource competence) {
        Collection<IStorageResource> notations = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", 4, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "ResultatEvaluation");
            controller.addEqualsConstraint("Competence", competence);
            controller.setOrderBy("Valeur",Float.class,true);

            notations = controller.evaluate(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (ArrayList<IStorageResource>) notations;
    }

}
