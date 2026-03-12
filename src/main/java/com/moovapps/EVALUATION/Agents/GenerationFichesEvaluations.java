package com.moovapps.EVALUATION.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class GenerationFichesEvaluations extends BaseAgent {

    @Override
    protected void execute() {
        Run();
    }

    void Run() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        Date now = new Date();
        //  Collection<IUser> users = (Collection<IUser>) getDirectoryModule().getUsers(getWorkflowModule().getSysadminContext());
        ArrayList<IWorkflowInstance> campagnesEnCours = getCampagnesEnCours();
        if (campagnesEnCours == null || campagnesEnCours.isEmpty()) {
            return;
        }
        for (IWorkflowInstance campagne : campagnesEnCours) {
            String codeCampagne =(String) campagne.getValue("sys_Reference");
            int evalsGeneratedForCampagneNumber = getEvalsNumber(codeCampagne);
            if(evalsGeneratedForCampagneNumber>0){
                continue;
            }


            Date dateGenerationEval = (Date) campagne.getValue("DateLancementEvaluation");
            if (sdf.format(dateGenerationEval).equals(sdf.format(now))) {
                try {

                    IStorageResource societe = (IStorageResource) campagne.getValue("Societe");
                    ArrayList<IStorageResource> profils = (ArrayList<IStorageResource>) campagne.getValue("Profil");
                    if (profils == null || profils.isEmpty()) {
                        continue;
                    }

                    ArrayList<IStorageResource> usersConcerne = getUsersConcerne(societe, profils);

                    if (usersConcerne.isEmpty()) {
                        continue;
                    }

                    IContext context = getWorkflowModule().getSysadminContext();
                    IOrganization organization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
                    IProject projet = getProjectModule().getProject(context, "EVAL", organization);
                    ICatalog catalog = getWorkflowModule().getCatalog(context, "EvalutionCollaborateur", 0, projet);
                    IWorkflow evalWorkflow = getWorkflowModule().getWorkflow(context, catalog, "ApplicationEntretienDEvaluation_1.0");
                    for (IStorageResource ficheCollaborateur : usersConcerne) {
                        IUser user = (IUser) ficheCollaborateur.getValue("Salarie");
                        if (user == null) continue;
                        IWorkflowInstance evalInstance = getWorkflowModule().createWorkflowInstance(getWorkflowModule().getSysadminContext(), evalWorkflow, null);
                        //IStorageResource ficheCollaborateur = getFicheCollaborateur(user);
                        //Infos campagne
                        evalInstance.setValue("CodeCampagne", campagne.getValue("sys_Reference"));
                        evalInstance.setValue("DateCreationCampagne", campagne.getValue("sys_CreationDate"));
                        evalInstance.setValue("ExerciceEvaluation", campagne.getValue("ExerciceEvaluation"));
                        evalInstance.setValue("DateLancementEvaluation", campagne.getValue("DateLancementEvaluation"));
                        evalInstance.setValue("DateDeCloturePrevisionnelle", campagne.getValue("DateDeCloturePrevisionnelle"));
                        evalInstance.setValue("Societe", campagne.getValue("Societe"));
                        evalInstance.setValue("CollaborateurEval", user);
                        evalInstance.setValue("FicheCollaborateur", ficheCollaborateur);

                        evalInstance.setValue("Fonction", user.getExtendedAttributes().getValue("Fonction"));
                        //evalInstance.setValue("Direction", user.getExtendedAttributes().getValue("Direction"));
                        evalInstance.setValue("Service", user.getExtendedAttributes().getValue("Service"));
                        evalInstance.setValue("Profil2", user.getExtendedAttributes().getValue("ProfilEVAL"));
                        evalInstance.setValue("Departement", user.getExtendedAttributes().getValue("Departement"));
                        evalInstance.setValue("ResponsableHierarchique", user != null ? user.getHierarchicalManager() : null);
                        evalInstance.setValue("DateDIntegration", user.getExtendedAttributes().getValue("DateDEmbauche"));
                        evalInstance.setValue("Anciennete", getAncienneteInDetail((Date) user.getExtendedAttributes().getValue("DateDEmbauche")));
                        evalInstance.setValue("DateDEmbaucheGroupe", user.getExtendedAttributes().getValue("DateDEmbaucheGroupe"));
                        evalInstance.setValue("AncienneteGroupe", getAncienneteInDetail((Date) user.getExtendedAttributes().getValue("DateDEmbaucheGroupe")));
                        evalInstance.setValue("Matricule", user.getExtendedAttributes().getValue("Matricule"));
                        evalInstance.setValue("NPlus2", user != null ? user.getHierarchicalManager() != null ? user.getHierarchicalManager().getHierarchicalManager() : null : null);
                        evalInstance.setValue("Evaluateur", user.getExtendedAttributes().getValue("Evaluateur"));


                        evalInstance.setValue("Pole", user.getExtendedAttributes().getValue("Pole"));
                        evalInstance.setValue("Activite", user.getExtendedAttributes().getValue("Activite"));
                        evalInstance.setValue("Metier", user.getExtendedAttributes().getValue("Metier"));
                        evalInstance.setValue("Grade", user.getExtendedAttributes().getValue("Grade"));
                        evalInstance.setValue("Categorie", user.getExtendedAttributes().getValue("Categorie"));
                        evalInstance.save(getWorkflowModule().getSysadminContext());
                        campagne.addLinkedWorkflowInstance("Evaluations", evalInstance);
                        campagne.save(context);

                        passToAutoEvalEtape(evalInstance);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getEvalsNumber(String codeCampagne) {
        int i = -1;
        try {
            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "EVAL", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "EvalutionCollaborateur", ICatalog.IType.NORMAL, project);
            IWorkflowContainer container = getWorkflowModule().getWorkflowContainer(sysContext,catalog,"ApplicationEntretienDEvaluation");
            IViewController controller = getWorkflowModule().getViewController(sysContext);
            controller.addEqualsConstraint("CodeCampagne", codeCampagne);
            i = controller.evaluateSize(container);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    IStorageResource getFicheCollaborateur(IUser user) {
        IStorageResource fiche = null;
        try {

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext, catalog, "FicheCollaborateur");
            IViewController controller = getWorkflowModule().getViewController(sysContext, IResource.class);
            controller.addEqualsConstraint("Salarie", user);
            ArrayList<IStorageResource> data = (ArrayList<IStorageResource>) controller.evaluate(definition);
            if (data != null && !data.isEmpty()) {
                fiche = data.iterator().next();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return fiche;
    }

    private void passToAutoEvalEtape(IWorkflowInstance evalInstance) {
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            ITaskInstance taskInstance = evalInstance.getCurrentTaskInstance(context);
            if (taskInstance != null) {
                ITask task = taskInstance.getTask();
                if (task != null) {
                    IAction iAction1 = task.getAction("Envoyer3");
                    if (iAction1 != null) {
                        getWorkflowModule().end(context, taskInstance, iAction1, "");
                    }
                    evalInstance.save(context);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ArrayList<IUser> getUsersConcerneOLD(IStorageResource societe, ArrayList<IStorageResource> profils) {
        if (societe == null || profils == null) {
            return null;
        }
        ArrayList<IUser> users = new ArrayList<>();
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization defaultOrganization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", defaultOrganization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            IViewController iViewController = getWorkflowModule().getViewController(context, IResource.class);
            iViewController.addEqualsConstraint("Societe", societe);

            List<String> profilsIds = profils.stream().map(f -> f.getId().toString()).collect(Collectors.toList());
            iViewController.addInConstraint("ProfilEvaluation", profilsIds);

            //  iViewController.addInConstraint("ProfilEvaluation",profils);
            ArrayList<IStorageResource> usersStorage = (ArrayList<IStorageResource>) iViewController.evaluate(definition);
            if (usersStorage != null && !usersStorage.isEmpty()) {
                for (IStorageResource storageResource : usersStorage) {
                    users.add((IUser) storageResource.getValue("Salarie"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    ArrayList<IStorageResource> getUsersConcerne(IStorageResource societe, ArrayList<IStorageResource> profils) {
        if (societe == null || profils == null) {
            return null;
        }
        ArrayList<IStorageResource> usersStorage = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization defaultOrganization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "REFERENTIELCOMMUN", defaultOrganization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            IViewController iViewController = getWorkflowModule().getViewController(context, IResource.class);
            iViewController.addEqualsConstraint("Societe", societe);

            List<String> profilsIds = profils.stream().map(f -> f.getId().toString()).collect(Collectors.toList());
            iViewController.addInConstraint("ProfilEvaluation", profilsIds);

            //  iViewController.addInConstraint("ProfilEvaluation",profils);
            usersStorage = (ArrayList<IStorageResource>) iViewController.evaluate(definition);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return usersStorage;
    }


    public String getAncienneteInDetail(Date dateEmbauche) {
        if (dateEmbauche == null) {
            return "";
        }
        long difference_In_Milliseconds = ((new Date().getTime() - dateEmbauche.getTime()));
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(difference_In_Milliseconds);
        int years = c.get(Calendar.YEAR) - 1970;
        int months = c.get(Calendar.MONTH);
        int days = c.get(Calendar.DAY_OF_MONTH) - 1;
        return years + " ans " + months + " mois "/* + days + " jours"*/;
//		return (int)difference_In_Years;
    }

    private ArrayList<IWorkflowInstance> getCampagnesEnCours() {
        ArrayList<IWorkflowInstance> campagnes = null;
        try {
            IContext context = getWorkflowModule().getSysadminContext();
            IOrganization defaultOrganization = getDirectoryModule().getOrganization(context, "DefaultOrganization");
            IProject project = getProjectModule().getProject(context, "EVAL", defaultOrganization);
            ICatalog catalog = getWorkflowModule().getCatalog(context, "EvalutionCollaborateur", ICatalog.IType.NORMAL, project);
            IWorkflow campagneWorkflow = getWorkflowModule().getWorkflow(context, catalog, "ApplicationCompagne_1.0");
            IViewController iViewController = getWorkflowModule().getViewController(context);
            iViewController.addEqualsConstraint("EtatDeLaCampagne", "Campagne lancée");
            campagnes = (ArrayList<IWorkflowInstance>) iViewController.evaluate(campagneWorkflow);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return campagnes;
    }
}
