package com.moovapps.EVALUATION.CampagneEval;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Creation extends BaseDocumentExtension {

    @Override
    public boolean onBeforeLoad() {
        if(getWorkflowInstance().getValue("FonctionInitiateur")==null){
            IUser creator = getWorkflowInstance().getCreatedBy();
            IStorageResource fonctionCreateur = (IStorageResource) creator.getExtendedAttributes().getValue("Fonction");
            getWorkflowInstance().setValue("FonctionInitiateur",fonctionCreateur);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        }
      /* try{
         //  IList exerciceEval = getWorkflowModule().getList(context,catalog,"ExerciceEvaluationList");
           int currentYear = Calendar.getInstance().get(Calendar.YEAR);
           int currentYearMinusFiveYears = currentYear - 2;
           int currentYearPlusFiveYears = currentYear + 2;
           int j = currentYearPlusFiveYears - currentYearMinusFiveYears;
           ArrayList<IOptionList.IOption> options = new ArrayList<>();
           for(int i = 0;i<=j;i++){
              // IOptionList.IOption option = getWorkflowModule().createListOption(currentYearMinusFiveYears+"-"+(currentYearMinusFiveYears+1),currentYearMinusFiveYears+"-"+(currentYearMinusFiveYears+1));
               IOptionList.IOption option = getWorkflowModule().createListOption(currentYearMinusFiveYears+"",currentYearMinusFiveYears+"");
               currentYearMinusFiveYears++;
               options.add(option);
           }
           getWorkflowInstance().setList("ExerciceEvaluationList",options);


       }catch (Exception e){
           e.printStackTrace();
       }*/

        return super.onBeforeLoad();
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {
      /*  String exerciceEval = (String) getWorkflowInstance().getValue("ExerciceEvaluationList");
      //  int exercice = Integer.parseInt(exerciceEval.split("-")[0].trim()) ;
        int exercice = Integer.parseInt(exerciceEval) ;

        getWorkflowInstance().setValue("AnneeDEvaluation",exercice);
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
      /*  if(action.getName().equals("LancerLaCampagneDEvaluation")){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
            Date dateGenerationEval = (Date) getWorkflowInstance().getValue("DateLancementEvaluation");
            Date now = new Date();
            if(sdf.format(dateGenerationEval).equals(sdf.format(now))){
                try {

                    // IStorageResource filiale = (IStorageResource) getWorkflowInstance().getValue("Filiale");
                    ArrayList<IStorageResource> profils = (ArrayList<IStorageResource>) getWorkflowInstance().getValue("Profil");
                    if( profils==null || profils.isEmpty()){
                        getResourceController().alert("Merci de choisir le profil à évaluer");
                        return false;
                    }
                    Collection<IUser> users = (Collection<IUser>) getDirectoryModule().getUsers(getWorkflowModule().getSysadminContext());
                    ArrayList<IUser> usersConcerne = new ArrayList<>();
                    for(IUser user : users){
                        IStorageResource userProfil =(IStorageResource) user.getExtendedAttributes().getValue("Profil");
                        if(profils.contains(userProfil)){
                            usersConcerne.add(user);
                        }
                    }
                    if(usersConcerne.isEmpty()){
                        return true;
                    }

                    for(IUser user : usersConcerne){
                        IContext context = getWorkflowModule().getSysadminContext();
                        IOrganization organization = getDirectoryModule().getOrganization(context,"DefaultOrganization");
                        IProject projet = getProjectModule().getProject(context,"EVAL",organization);
                        ICatalog catalog = getWorkflowModule().getCatalog(context,"EvalutionCollaborateur",0,projet);
                        IWorkflow evalWorkflow = getWorkflowModule().getWorkflow(context,catalog,"ApplicationEntretienDEvaluation_1.0");
                        IWorkflowInstance evalInstance = getWorkflowModule().createWorkflowInstance(getWorkflowModule().getSysadminContext(), evalWorkflow, null);

                        evalInstance.setValue("CodeCampagne",getWorkflowInstance().getValue("sys_Reference"));
                        evalInstance.setValue("DateCreationCampagne",getWorkflowInstance().getValue("sys_CreationDate"));
                        evalInstance.setValue("AnneeDEvaluation",getWorkflowInstance().getValue("AnneeDEvaluation"));
                        evalInstance.setValue("DateLancementEvaluation",getWorkflowInstance().getValue("DateLancementEvaluation"));
                        evalInstance.setValue("DateDeCloturePrevisionnelle",getWorkflowInstance().getValue("DateDeCloturePrevisionnelle"));
                      //  evalInstance.setValue("Filiale",getWorkflowInstance().getValue("Filiale"));
                        evalInstance.setValue("CollaborateurEval",user);
                       // evalInstance.setValue("ResponsableHierarchique",user.getHierarchicalManager());
                        //evalInstance.setValue("NPlus2",user.getHierarchicalManager()!=null?user.getHierarchicalManager().getHierarchicalManager():null);
                        evalInstance.setValue("FonctionInitiateur",user.getExtendedAttributes().getValue("Fonction2"));
                        evalInstance.setValue("Direction",user.getExtendedAttributes().getValue("Direction"));
                        evalInstance.setValue("Profil2",user.getExtendedAttributes().getValue("ProfilEVAL"));
                        evalInstance.setValue("Service",user.getExtendedAttributes().getValue("Service2"));
                        evalInstance.setValue("ResponsableHierarchique",user.getExtendedAttributes().getValue("Evaluateur"));
                        evalInstance.setValue("DateDIntegration",user.getEntry());
                        evalInstance.setValue("Anciennete",getAncienneteInDetail(user.getEntry()));
                        evalInstance.setValue("Matricule",user.getExtendedAttributes().getValue("Matricule"));

                        evalInstance.save(getWorkflowModule().getSysadminContext());
                        getWorkflowInstance().addLinkedWorkflowInstance("Evaluations",evalInstance);
                        getWorkflowInstance().save(context);

                        //here
                        passToAutoEvalEtape(evalInstance);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }*/
        return super.onBeforeSubmit(action);
    }

    private void passToAutoEvalEtape(IWorkflowInstance evalInstance) {
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            ITaskInstance taskInstance = evalInstance.getCurrentTaskInstance(context);
            if(taskInstance!=null){
                ITask task = taskInstance.getTask();
                if(task!=null){
                    IAction iAction1 = task.getAction("Envoyer3");
                    if(iAction1!=null){
                        getWorkflowModule().end(context, taskInstance, iAction1, "");
                    }
                    evalInstance.save(context);

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
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
}
