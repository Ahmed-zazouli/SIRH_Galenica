package com.moovapps.Formation.PlanFormation.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;

public class PlanFormation extends BaseDocumentExtension {

    @Override
    public boolean onBeforeLoad() {

        return super.onBeforeLoad();
    }

    @Override
    public boolean onAfterLoad() {
        if(getWorkflowInstance().getValue("Societe")==null){
            getWorkflowInstance().setValue("Societe",getWorkflowInstance().getCreatedBy().getExtendedAttributes().getValue("Societe"));

        }
        return super.onAfterLoad();
    }


    /*
    @Override
	public void onPrepareItem(ViewItem viewItem) {
		// Recovery of ViewModelItem

		try {
			ViewModelItem viewModelItem = viewItem.getViewModelItem();

			IResource iWorkflowInstance =  viewItem.getResource();
			// iWorkflowInstance.getValue("CandidatureInteressante");



			CtlCheckBox ctlCheckBox = new CtlCheckBox((Boolean) iWorkflowInstance.getValue("CandidatureInteressante"));
			CtlCheckBox ctlCheckBox2 = new CtlCheckBox((Boolean) iWorkflowInstance.getValue("CandidatInteresse"));

			ctlCheckBox.setThrowEvents(true);
			ctlCheckBox.setParam(viewItem);
			ctlCheckBox.setEditable(true);

			ctlCheckBox2.setThrowEvents(true);
			ctlCheckBox2.setParam(viewItem);
			ctlCheckBox2.setEditable(true);


			ctlCheckBox2.addChangeListener((new ChangeListener() {
				@Override
				public void onChange(ChangeEvent event) {
					try {
						getDirectoryModule().beginTransaction();

						CtlCheckBox ctlCheckBox1 = (CtlCheckBox) event.getSource();
						//	ViewItem viewItem = (ViewItem) ((CtlCheckBox) event.getSource()).getParam();
						ViewItem iWorkflowInstance1 =  (ViewItem) ctlCheckBox1.getParam();
						IResource r = iWorkflowInstance1.getResource();
						String ref = (String) r.getValue("sys_Reference");
						IWorkflowInstance w = getWorkflowInstancebyReference(ref);
						//w.getValue("CandidatInteresse");
						//w.getDefinition();
						//w.getId();
						w.setValue("CandidatInteresse", ctlCheckBox1.getValidationObject());
						w.save(getWorkflowModule().getLoggedOnUserContext());
						getDirectoryModule().commitTransaction();


					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}));



			ctlCheckBox.addChangeListener((new ChangeListener() {
				@Override
				public void onChange(ChangeEvent event) {
					try {

						getDirectoryModule().beginTransaction();

						CtlCheckBox ctlCheckBox1 = (CtlCheckBox) event.getSource();
						//	ViewItem viewItem = (ViewItem) ((CtlCheckBox) event.getSource()).getParam();
						ViewItem iWorkflowInstance1 =  (ViewItem) ctlCheckBox1.getParam();
						IResource r = iWorkflowInstance1.getResource();
						String ref = (String) r.getValue("sys_Reference");
						IWorkflowInstance w = getWorkflowInstancebyReference(ref);
						//	w.getValue("CandidatureInteressante");
						//	w.getDefinition();
						//	w.getId();
						w.setValue("CandidatureInteressante", ctlCheckBox1.getValidationObject());
						w.save(getWorkflowModule().getLoggedOnUserContext());
						getDirectoryModule().commitTransaction();


					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}));





			viewModelItem.setValue("CandidatureInteressante", ctlCheckBox);
			viewModelItem.setValue("CandidatInteresse", ctlCheckBox2);



			//Navigator.getNavigator().refresh();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	IWorkflowInstance getWorkflowInstancebyReference(String reference){

		try
		{
			IContext sysContext = getWorkflowModule().getSysadminContext();
			IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
			IProject project = getProjectModule().getProject(sysContext, "Recrutement", organization);
			ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Recrutement", project);
			IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "Candidature_1.0");
			IViewController controller = getWorkflowModule().getViewController(sysContext);
			controller.addEqualsConstraint("sys_Reference", reference);
			Collection<IWorkflowInstance> i = controller.evaluate(w);
			return  i.iterator().next();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

	}

    *\
     */
}
