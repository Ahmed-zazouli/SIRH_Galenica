package com.moovapps.Formation.Cours.document;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdp.ui.framework.composites.IDocumentComposite;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.axemble.vdp.ui.framework.widgets.blocks.sys.layouts.FormSectionBlock;
import org.apache.commons.io.FileUtils;

import java.io.DataInput;
import java.io.File;
import java.util.*;

public class CreationCours extends BaseDocumentExtension {

    @Override
    public boolean onAfterLoad() {
        IWorkflowInstance sessionDeFormation = getWorkflowInstance().getParentInstance();
        if (getWorkflowInstance().getValue("IDSeance") == null) {
            getWorkflowInstance().setValue("IDSeance", UUID.randomUUID().toString());
        }

        getWorkflowInstance().setValue("Societe", sessionDeFormation.getValue("Societe"));
        getWorkflowInstance().setValue("PlanFormationProcessus", sessionDeFormation.getValue("PlanFormationProcessus"));
        getWorkflowInstance().setValue("ProgrammeFormationProcessus", sessionDeFormation.getValue("ProgrammeFormationProcessus"));
        getWorkflowInstance().setValue("FormationProcessus", sessionDeFormation.getValue("FormationProcessus"));
        getWorkflowInstance().setValue("SessionDeFormationProcessus", sessionDeFormation);
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());

        refreshSection("Questions");

        return super.onAfterLoad();
    }

    private void refreshSection(String sectionName) {
        IDocumentComposite documentComposite = null;
        try {
            documentComposite = (IDocumentComposite) Navigator.getNavigator().getCurrentNavigation();
        } catch (ClassCastException e) {
            documentComposite = (IDocumentComposite) Navigator.getNavigator().getRootNavigator().getPartByName("ezs").getCurrentNavigation();
        }
        //documentComposite.getBody().getViews().iterator().next().refresh();

        List<FormSectionBlock> Sections = documentComposite.getBody().getSections();
        Sections.forEach(section -> {
            if (section.getTitle() != null && section.getTitle().equals(sectionName)) {
                section.refresh();
            }
        });
    }

    @Override
    public void onPropertyChanged(IProperty property) {
        if (property.getName().equals("SessionDeFormation")) {
            onSessionFormationChange();
        }
        /*if (property.getName().equals("DateDebut")){
            if (getWorkflowInstance().getValue("DateDebut") != null){
                getWorkflowInstance().setValue("DateFinPrevisionnelle",getWorkflowInstance().getValue("DateDebut"));
            }
        }*/
        super.onPropertyChanged(property);
    }

    private void onSessionFormationChange() {
        if (getWorkflowInstance().getValue("SessionDeFormation") != null) {
            IStorageResource sessionFormation = (IStorageResource) getWorkflowInstance().getValue("SessionDeFormation");
            getWorkflowInstance().setValue("Participants", sessionFormation.getValue("Participants"));
        } else {
            getWorkflowInstance().setValue("Participants", null);
        }
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("CreerLeCours")) {
            onCreerCoursClick();
        }
        return super.onBeforeSubmit(action);
    }

    private void onCreerCoursClick() {
        try {

            // Collection<IStorageResource> participants = (Collection<IStorageResource>) getWorkflowInstance().getValue("Participants");

            ArrayList<IStorageResource> participants = (ArrayList<IStorageResource>) getWorkflowInstance().getParentInstance().getValue("Participants");
            for (IStorageResource participant : participants) {
                boolean isNewCoursParParticipant = true;
                IWorkflowInstance coursParParticipant = getSeanceParticipantsByIDSeance((IUser) participant.getValue("Salarie"));
                if (coursParParticipant == null) {
                    IWorkflow coursParParticipantWorkflow = getWorkflowModule().getWorkflow(getWorkflowModule().getSysadminContext(), getWorkflowInstance().getCatalog(), "CoursParParticipant_1.0");
                    coursParParticipant = getWorkflowModule().createWorkflowInstance(getDirectoryModule().getSysadminContext(), coursParParticipantWorkflow, "");
                }else{
                    isNewCoursParParticipant = false;
                }
                coursParParticipant.setValue("Societe", getWorkflowInstance().getValue("Societe"));
                coursParParticipant.setValue("IDSession", getWorkflowInstance().getParentInstance().getValue("IDSession"));
                String IDSeancePartcicipant = UUID.randomUUID().toString();
                coursParParticipant.setValue("IDSeanceParticipant", IDSeancePartcicipant);
                coursParParticipant.setValue("IDSeance", getWorkflowInstance().getValue("IDSeance"));
                coursParParticipant.setValue("PlanFormationProcessus", getWorkflowInstance().getValue("PlanFormationProcessus"));
                coursParParticipant.setValue("ProgrammeFormationProcessus", getWorkflowInstance().getValue("ProgrammeFormationProcessus"));
                coursParParticipant.setValue("FormationProcessus", getWorkflowInstance().getValue("FormationProcessus"));
                coursParParticipant.setValue("SessionDeFormationProcessus", getWorkflowInstance().getValue("SessionDeFormationProcessus"));
                coursParParticipant.setValue("SeanceDeFormationProcessus", getWorkflowInstance());//getSeanceDeFormationParReference((String) getWorkflowInstance().getValue("sys_Reference")));
                coursParParticipant.setValue("DateDeDebut", getWorkflowInstance().getValue("DateDebut"));//getSeanceDeFormationParReference((String) getWorkflowInstance().getValue("sys_Reference")));
                coursParParticipant.setValue("HeureMinuteDebut", getWorkflowInstance().getValue("HeureMinuteDebut"));
                coursParParticipant.setValue("HeureMinuteFin", getWorkflowInstance().getValue("HeureMinuteFin"));

               /*coursParParticipant.setValue("PlanDeFormation2", getWorkflowInstance().getParentInstance().getValue("PlanDeFormation2"));
               coursParParticipant.setValue("Formation2", getWorkflowInstance().getParentInstance().getValue("Formation2"));
               coursParParticipant.setValue("SessionDeFormation2", getWorkflowInstance().getParentInstance().getValue("SessionDeFormation2"));*/

//                coursParParticipant.setValue("Competence", getWorkflowInstance().getValue("Competence2"));
                coursParParticipant.setValue("EstCeQueCeCoursComprendUnQuiz", getWorkflowInstance().getValue("EstCeQueCeCoursComprendUnQuiz"));
                coursParParticipant.setValue("QuizPJ", getWorkflowInstance().getValue("QuizPJ"));
                coursParParticipant.setValue("RessourcesDuCours", resourceDuCours(getWorkflowInstance(), "RessourcesDuCours"));
                coursParParticipant.setValue("Commentaire", getWorkflowInstance().getValue("Commentaire"));
                coursParParticipant.setValue("Participant2", participant.getValue("Salarie"));
                //coursParParticipant.setValue("Active",true);
                // Participant2.extendedAttributes.Values.Societe.Values.ResponsableRH
                IUser validateur = null;
                if (getWorkflowInstance().getParentInstance().getValue("NatureDeFormateur").equals("Interne")) {
                    validateur = (IUser) getWorkflowInstance().getParentInstance().getValue("FormateurInterne");
                } else {
                    validateur = (IUser) getWorkflowInstance().getParentInstance().getValue("RepresentantFormateur");
                }
                coursParParticipant.setValue("Validateur2", validateur);
                coursParParticipant.save(getWorkflowModule().getSysadminContext());
                if(isNewCoursParParticipant){
                    getWorkflowInstance().addLinkedWorkflowInstance("CoursParParticipant", coursParParticipant);
                }
                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());

                ISecurityController securityController = getSecuriteController(coursParParticipant);
                breakInheritance(securityController);
                addManagementGroupes(securityController);
                addSpecificPermissions(coursParParticipant,securityController);
            }
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private IWorkflowInstance getSeanceParticipantsByIDSeance(IUser participant) {
        IWorkflowInstance seanceParticipants = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "Formation", project);
            IWorkflow w = getWorkflowModule().getWorkflow(context, catalog, "CoursParParticipant_1.0");
            IViewController controller = getWorkflowModule().getViewController(context);
            controller.addEqualsConstraint("IDSeance", getWorkflowInstance().getValue("IDSeance"));
            controller.addEqualsConstraint("Participant2", participant);
            if (!controller.evaluate(w).isEmpty()) {
                seanceParticipants = (IWorkflowInstance) controller.evaluate(w).iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return seanceParticipants;
    }

    public ArrayList<IAttachment> resourceDuCours(IWorkflowInstance instance, String key) {
        ArrayList<IAttachment> attachments = (ArrayList<IAttachment>) instance.getValue(key);
        File file = null;
        ArrayList<IAttachment> files = new ArrayList<>();
        if (attachments != null && attachments.size() > 0) {
            for (IAttachment iAttachment : attachments) {
                if (iAttachment != null) {
                    try {
                        file = new File("C://Temp_Moovapps//" + iAttachment.getName());
                        FileUtils.writeByteArrayToFile(file, iAttachment.getContent());
                        IAttachment attachment = Modules.getDirectoryModule().createAttachment(Modules.getWorkflowModule().getSysadminContext(), file);
                        files.add(attachment);
                        file.delete();
                        file.deleteOnExit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return files;
    }

    private IWorkflowInstance getSeanceDeFormationParReference(String reference) {
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "Formation", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "Formation", project);
            // IWorkflow w = getWorkflowModule().getWorkflow(sysContext, catalog, "Cours_1.0");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addInConstraint("sys_WorkflowContainer", Arrays.asList("Cours"));
            controller.addNotEqualsConstraint("sys_Reference", reference);
            return (IWorkflowInstance) controller.evaluate(catalog).iterator().next();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    public ISecurityController getSecuriteController(IWorkflowInstance instance){
        try {
            return getWorkflowModule().getSecurityController(instance);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void breakInheritance(ISecurityController securityController){
        securityController.breakInheritance(1, new Object[] { null, "write" });
    }

    public void addManagementGroupes(ISecurityController securityController){
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization BPOGroupesOrganization = getDirectoryModule().getOrganization(context, "BPOGroupes");
            IGroup adminGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"BPO");
            IGroup DRHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"DRH");
            IGroup RHGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"RH");
            // IGroup ResponsableDevGroupe = getDirectoryModule().getGroup(context, BPOGroupesOrganization,"ResponsableSDEV");

            addPermission(securityController, adminGroupe, "read");
            addPermission(securityController, DRHGroupe, "read");
            addPermission(securityController, RHGroupe, "read");
            //addPermission(securityController, ResponsableDevGroupe, "read");

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void addPermission(ISecurityController securityController, IGroup group, String readWriteGrant){
        securityController.addPermission(group, new Object[] { null, readWriteGrant });
    }

    public void addPermission(ISecurityController securityController, IUser user, String readWriteGrant){
        securityController.addPermission(user, new Object[] { null, readWriteGrant });
    }


    public void addSpecificPermissions(IResource instance, ISecurityController securityController) {
        if(instance == null || securityController == null){
            return;
        }
        try {

            IUser salarie = (IUser) instance.getValue("sys_Creator");
            if (salarie == null) {
                return;
            }
            addPermission(securityController, salarie, "read");



            IUser participant = (IUser) instance.getValue("Participant2");
            if(participant!=null){
                addPermission(securityController, participant, "read");
                if(participant.getHierarchicalManager()!=null){

                    addPermission(securityController, participant.getHierarchicalManager(), "read");
                }

            }

            IUser validateur2 = (IUser) instance.getValue("Validateur2");
            if(validateur2!=null){
                addPermission(securityController, validateur2, "read");

            }

            //IStorageResource societe = (IStorageResource) instance.getValue("Societe");
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization organization = null;
            //  if (societe != null) {
            organization = participant.getOrganization();
            //}

            if (organization == null) return;
            IGroup RHClientGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "RH");
            if (RHClientGroupe != null) {
                addPermission(securityController, RHClientGroupe, "read");
            }

            IGroup ResponsableDevClientGroupe = getDirectoryModule().getGroup(context, organization,"ResponsableSDEV");
            if(ResponsableDevClientGroupe!=null){
                addPermission(securityController, ResponsableDevClientGroupe, "read");
            }

            IGroup RHSocieteGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "RHSociete");
            if (RHSocieteGroupe != null) {
                addPermission(securityController, RHSocieteGroupe, "read");
            }

            IGroup ResponsableDevSocieteGroupe = getDirectoryModule().getGroup(context, organization, organization.getName() + "ResponsablesDevSociete");
            if (ResponsableDevSocieteGroupe != null) {
                addPermission(securityController, ResponsableDevSocieteGroupe, "read");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
