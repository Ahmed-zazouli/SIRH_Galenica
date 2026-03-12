package com.moovapps.EVALUATION.Eval;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;

import java.util.Calendar;
import java.util.Date;

public class FirstEtape extends BaseDocumentExtension {
    @Override
    public boolean onBeforeSubmit(IAction action) {
        if(action.getName().equals("Envoyer3")){
            IStorageResource ficheCollaborateur = (IStorageResource) getWorkflowInstance().getValue("FicheCollaborateur");
            if(ficheCollaborateur!=null){
                IUser user = (IUser) ficheCollaborateur.getValue("Salarie");
               // getWorkflowInstance().setValue("CollaborateurEval", user);

                IWorkflowInstance campagne = (IWorkflowInstance) getWorkflowInstance().getValue("Parent");
                getWorkflowInstance().setValue("CodeCampagne", campagne.getValue("sys_Reference"));
                getWorkflowInstance().setValue("DateCreationCampagne", campagne.getValue("sys_CreationDate"));
                getWorkflowInstance().setValue("ExerciceEvaluation", campagne.getValue("ExerciceEvaluation"));
                getWorkflowInstance().setValue("DateLancementEvaluation", campagne.getValue("DateLancementEvaluation"));
                getWorkflowInstance().setValue("DateDeCloturePrevisionnelle", campagne.getValue("DateDeCloturePrevisionnelle"));
                getWorkflowInstance().setValue("Societe", campagne.getValue("Societe"));
                getWorkflowInstance().setValue("CollaborateurEval", user);
                getWorkflowInstance().setValue("FicheCollaborateur", ficheCollaborateur);
                getWorkflowInstance().setValue("Fonction", user.getExtendedAttributes().getValue("Fonction"));
                getWorkflowInstance().setValue("Service", user.getExtendedAttributes().getValue("Service"));
                getWorkflowInstance().setValue("Profil2", user.getExtendedAttributes().getValue("ProfilEVAL"));
                getWorkflowInstance().setValue("Departement", user.getExtendedAttributes().getValue("Departement"));
                getWorkflowInstance().setValue("ResponsableHierarchique", user != null ? user.getHierarchicalManager() : null);
                getWorkflowInstance().setValue("DateDIntegration", user.getExtendedAttributes().getValue("DateDEmbauche"));
                getWorkflowInstance().setValue("Anciennete", getAncienneteInDetail((Date) user.getExtendedAttributes().getValue("DateDEmbauche")));
                getWorkflowInstance().setValue("DateDEmbaucheGroupe", user.getExtendedAttributes().getValue("DateDEmbaucheGroupe"));
                getWorkflowInstance().setValue("AncienneteGroupe", getAncienneteInDetail((Date) user.getExtendedAttributes().getValue("DateDEmbaucheGroupe")));
                getWorkflowInstance().setValue("Matricule", user.getExtendedAttributes().getValue("Matricule"));
                getWorkflowInstance().setValue("NPlus2", user != null ? user.getHierarchicalManager() != null ? user.getHierarchicalManager().getHierarchicalManager() : null : null);
                getWorkflowInstance().setValue("Evaluateur", user.getExtendedAttributes().getValue("Evaluateur"));


                getWorkflowInstance().setValue("Pole", user.getExtendedAttributes().getValue("Pole"));
                getWorkflowInstance().setValue("Activite", user.getExtendedAttributes().getValue("Activite"));
                getWorkflowInstance().setValue("Metier", user.getExtendedAttributes().getValue("Metier"));
                getWorkflowInstance().setValue("Grade", user.getExtendedAttributes().getValue("Grade"));
                getWorkflowInstance().setValue("Categorie", user.getExtendedAttributes().getValue("Categorie"));


                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());


                campagne.addLinkedWorkflowInstance("Evaluations",getWorkflowInstance());
                campagne.save(getWorkflowModule().getSysadminContext());
            }
           

                //here
               // passToAutoEvalEtape(getWorkflowInstance());


        }
        return super.onBeforeSubmit(action);
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
