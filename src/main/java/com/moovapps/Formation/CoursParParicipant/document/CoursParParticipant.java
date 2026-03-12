package com.moovapps.Formation.CoursParParicipant.document;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CoursParParticipant extends BaseDocumentExtension {
    @Override
    public boolean onAfterLoad() {
        setQuestionsReponse();
        return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action ) {
        if (action.getName().equals("Cloturer")){
            calculateNotation();
        }

        return super.onBeforeSubmit(action);
    }

    private void calculateNotation() {
        Collection<IStorageResource>  questionReponse = getQuestionsReponseByIDSeance();
        double poidsTotale = 0;
        double notationCorrecte = 0;
        for (IStorageResource question : questionReponse){
            double notation = question.getValue("Notation") != null ? ((Number)question.getValue("Notation")).doubleValue() : 0;
            double poids = question.getValue("Poids") != null ? ((Number)question.getValue("Poids")).doubleValue() : 0;
            poidsTotale += poids;
            notationCorrecte += notation;

        }
        try {
            getWorkflowInstance().setValue("Notation0100", (notationCorrecte / poidsTotale) * 100);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setQuestionsReponse() {
        Collection<IStorageResource> QuestionsReponse = getQuestionsReponseByIDSeance();
        if (QuestionsReponse.isEmpty()) {
            Collection<IStorageResource> Questions = getQuestionsByIDSeance();
            for (IStorageResource question : Questions) {
                try {
                    IContext context = getWorkflowModule().getSysadminContext();
                    IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                    IProject project = getProjectModule().getProject(context, "Formation", organization);
                    ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
                    IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions_Reponses");
                    IStorageResource questionReponse = getWorkflowModule().createStorageResource(context, definition, "","");
                    questionReponse.setValue("Question", question);
                    questionReponse.setValue("IDSeance", question.getValue("IDSeance"));
                    questionReponse.setValue("Poids", question.getValue("Poids"));
                    questionReponse.setValue("ReponseS", question.getValue("ReponseS"));
                    questionReponse.setValue("IDSeanceParticipant", getWorkflowInstance().getValue("IDSeanceParticipant"));
                    questionReponse.save(getWorkflowModule().getSysadminContext());
                    //setChoixReponse(question , questionReponse);
                    getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

  /*  private void setChoixReponse(IStorageResource question , IStorageResource questionReponse) {
        Collection<IStorageResource> choixQuestion= getChoixByQuestion(question);
        for (IStorageResource choix : choixQuestion) {
            try {
                IContext context = getWorkflowModule().getSysadminContext();
                IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                IProject project = getProjectModule().getProject(context, "Formation", organization);
                ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
                IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Choix_Reponse");
                IStorageResource choixReponse = getWorkflowModule().createStorageResource(context, definition, "Choix_Reponse");
                String titreChoix = ((String) choix.getValue("CodeChoix")) + "- " + ((String)choix.getValue("sys_Title"));
                choixReponse.setValue("sys_Title", titreChoix);
                choixReponse.setValue("Question", questionReponse);
                choixReponse.save(getWorkflowModule().getSysadminContext());
                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } }
*/


  /*  private Collection<IStorageResource> getChoixByQuestion(IStorageResource question) {
        Collection<IStorageResource> QuestionsReponse = Collections.emptyList();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Choix");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("Question",question);
            QuestionsReponse = controller.evaluate(definition);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return QuestionsReponse;
    }*/

    private Collection<IStorageResource> getQuestionsReponseByIDSeance() {
        Collection<IStorageResource> QuestionsReponse = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions_Reponses");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("IDSeance",getWorkflowInstance().getValue("IDSeance"));
            controller.addEqualsConstraint("IDSeanceParticipant" ,getWorkflowInstance().getValue("IDSeanceParticipant"));
            QuestionsReponse = controller.evaluate(definition);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return QuestionsReponse;
    }

    private Collection<IStorageResource> getQuestionsByIDSeance() {
        Collection<IStorageResource> Questions = Collections.emptyList();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Referentiels", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "Questions");
            IViewController controller = getWorkflowModule().getViewController(context, IResource.class);
            controller.addEqualsConstraint("IDSeance",getWorkflowInstance().getValue("IDSeance"));
            Questions = controller.evaluate(definition);
        }catch (Exception ex){
            ex.printStackTrace();
        }
     return Questions;
    }
}
