package com.moovapps.Formation.Formation.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdp.ui.framework.composites.IDocumentComposite;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.widgets.blocks.sys.layouts.FormSectionBlock;

import java.util.List;
import java.util.UUID;

public class Formation extends BaseDocumentExtension {
    @Override
    public boolean onAfterSubmit(IAction action) {


        return super.onAfterSubmit(action);
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
//        if (action.getName().equals("ValiderLAchevementDuCours")) {
//            //     IWorkflow particiant = (IWorkflow) getWorkflowInstance();
//            //      String parent = (String) getWorkflowInstance().getParentInstance().getValue("Participants");
//            setCompetence();
//            return false;
//        }


        return super.onBeforeSubmit(action);
    }

    @Override
    public boolean onAfterLoad() {
        // getWorkflowInstance().setValue("Societe", getWorkflowInstance().getParentInstance().getValue("Societe"));

        // IStorageResource planDeFormation = (IStorageResource) getWorkflowInstance().getValue("PlanDeFormation");
        // if(planDeFormation!=null){
        //   getWorkflowInstance().setValue("PlanDeFormation",planDeFormation);
        // IStorageResource planDeFormation = (IStorageResource) getWorkflowInstance().getValue("PlanDeFormation");
        getWorkflowInstance().setValue("Societe", getWorkflowInstance().getParentInstance().getValue("Societe"));
        getWorkflowInstance().setValue("Departement",getWorkflowInstance().getParentInstance().getValue("Departement"));
       // getWorkflowInstance().setValue("Activite", getWorkflowInstance().getParentInstance().getValue("Activite"));
       // getWorkflowInstance().setValue("Pole", getWorkflowInstance().getParentInstance().getValue("Pole"));
        getWorkflowInstance().setValue("Division",getWorkflowInstance().getParentInstance().getValue("Division"));
        getWorkflowInstance().setValue("ProfilS",getWorkflowInstance().getParentInstance().getValue("ProfilS"));
        if (getWorkflowInstance().getParentInstance().getValue("IDProgramme")==null){

            getWorkflowInstance().setValue("PlanFormationProcessus", getWorkflowInstance().getParentInstance());
        }else {
            getWorkflowInstance().setValue("PlanFormationProcessus", getWorkflowInstance().getParentInstance().getParentInstance());
            getWorkflowInstance().setValue("ProgrammeFormationProcessus", getWorkflowInstance().getParentInstance());
        }

        if(getWorkflowInstance().getValue("IDFormation") == null){
            getWorkflowInstance().setValue("IDFormation", UUID.randomUUID().toString());
            String gestionGestionaireAuNiveauDeFormation = "Oui";
            if(getWorkflowInstance().getParentInstance().getValue("QuizEtape").equals("Programme") || getWorkflowInstance().getParentInstance().getValue("QuizEtape").equals("Sans") ){
                gestionGestionaireAuNiveauDeFormation = "Non";
            }
            getWorkflowInstance().setValue("SouhaitezVousGererLesQuestionnaireAChaux", gestionGestionaireAuNiveauDeFormation);
            getWorkflowInstance().setValue("SouhaitezVousGererLesQuestionnaireAFroid", gestionGestionaireAuNiveauDeFormation);

        }
        if(getWorkflowInstance().getValue("IDFormationQuestionFroid") == null){
            getWorkflowInstance().setValue("IDFormationQuestionFroid", UUID.randomUUID().toString());
        }
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        refreshSection("Quizz à chaud");
        refreshSection("Questionnaire à froid");

        return super.onAfterLoad();
    }

    @Override
    public boolean onAfterSave() {

        return super.onAfterSave();
    }
    private void refreshSection(String sectionName){
        IDocumentComposite documentComposite = null;
        try {
            documentComposite = (IDocumentComposite) Navigator.getNavigator().getCurrentNavigation();
        } catch (ClassCastException e) {
            documentComposite = (IDocumentComposite)Navigator.getNavigator().getRootNavigator().getPartByName("ezs").getCurrentNavigation();
        }
        //documentComposite.getBody().getViews().iterator().next().refresh();

        List<FormSectionBlock> Sections =  documentComposite.getBody().getSections();
        Sections.forEach(section ->{
            if(section.getTitle() != null && section.getTitle().equals(sectionName)){
                section.refresh();
            }
        });
    }
//
//    public void setListParticipants() {
//        String Formation = (String) getWorkflowInstance().getValue("sys_Title");
//        Collection<IStorageResource> Participants = getParticipants(Formation);
//        for (IStorageResource resource : Participants) {
//            getWorkflowInstance().setValue("Participants", resource.getValue("Participants"));
//        }
//
//    }
//
//    public Collection<IStorageResource> getParticipants(String formation) {
//        Collection<IStorageResource> Participants = null;
//        try {
//            IContext context = getWorkflowModule().getSysadminContext();
//            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
//            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE);
//            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "SessionDeFormation");
//            controller.addEqualsConstraint("Formation", formation);
//            Participants = controller.evaluate(definition);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return Participants;
//    }


//    public void setListParticipants() {
//        ArrayList<ILinkedResource> participants = (ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("Participants2");
//        ArrayList<IOptionList.IOption> options = (ArrayList<IOptionList.IOption>) getWorkflowInstance().getParentInstance().getList("Participants");
//
//        for ( ILinkedResource resource : participants) {
//            resource.getValue("Participants");
//            IOptionList.IOption optionOne = getWorkflowModule().createListOption(resource.getValue("Participants"), String.valueOf((IUser) resource.getValue("Participants")));
//           options.add(optionOne);
//           getWorkflowInstance().getParentInstance().setList("Participants",options);
//
//          //  getWorkflowInstance().getParentInstance().setValue("Participants", resource.getValue("Participants"));
//
//
//        }
//    }
}
