package recrutement.candidature;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class PasserLesEntretiens extends BaseDocumentExtension {
    ArrayList<Date> entretiensDates = new ArrayList<Date>();
    ArrayList<String> entretiensName = new ArrayList<String>();
    ArrayList<IUser> myEvaluateurs = new ArrayList<IUser>();
    ArrayList<String> myOrders = new ArrayList<String>();
    int currentOrdre = 0;
    IUser evaluateurActuele = null;
    boolean dejaFait = false;

    int totalEntretien = 0;
    //IUser evalActuel = null;
    //ArrayList<IUser> u = null;

    @Override
    public boolean onBeforeLoad() {
        // TODO Auto-generated method stub
        getWorkflowInstance().setValue("DecisionDEntretien", null);
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());

        return super.onAfterLoad();
    }

    @Override
    public boolean onAfterSubmit(IAction action) {
        getWorkflowInstance().setValue("DecisionDEntretien", null);
        getWorkflowInstance().save(getWorkflowModule().getLoggedOnUserContext());
        return super.onAfterSubmit(action);
    }

    @Override
    public boolean onBeforeSubmit(IAction action) {

        if (action.getName().equals("Passer")) {
            //
            try {
                //getWorkflowInstance().setValue("EvaluateurActuel", entretien.getValue("Evaluateur"));
                //						getWorkflowInstance().setValue("OrdreActuel"
                currentOrdre = ((Number) getWorkflowInstance().getValue("OrdreActuel")).intValue();
                ArrayList<ILinkedResource> EvaluationRH = (ArrayList<ILinkedResource>) getWorkflowInstance().getLinkedResources("EvaluationRH");
                for (ILinkedResource eval : EvaluationRH) {
                    if (((Number) eval.getValue("Ordre")).intValue() == currentOrdre) {
                        eval.setValue("NoteGlobale", getWorkflowInstance().getValue("NoteGlobale"));

                        eval.setValue("DateDEntretien", getWorkflowInstance().getValue("DateDEntretienReel"));

                        eval.setValue("DecisionDEntretien", getWorkflowInstance().getValue("DecisionDEntretien"));
						ArrayList<IAttachment> p = _PieceJointe(getWorkflowInstance(),"FicheDEvaluation");
						eval.setValue("FicheDEvaluation",p);
                        eval.save(getWorkflowModule().getSysadminContext());
                        break;
                    }
                }
                getWorkflowInstance().setValue("EvaluateurActuel", null);
                getWorkflowInstance().setValue("OrdreActuel", null);
                getWorkflowInstance().setValue("DateDEntretien2", null);
                getWorkflowInstance().setValue("Entretien", null);
                getWorkflowInstance().setValue("NoteGlobale", null);
                getWorkflowInstance().setValue("DateDEntretienReel", null);

                //getWorkflowInstance().setValue("DecisionDEntretien", null);
                getWorkflowInstance().setValue("FicheDEvaluation", new ArrayList<>());
                if (EvaluationRH.size() > currentOrdre) {
                    ILinkedResource nextEntretien = EvaluationRH.get(currentOrdre);
                    getWorkflowInstance().setValue("EvaluateurActuel", nextEntretien.getValue("Evaluateur"));
                    getWorkflowInstance().setValue("OrdreActuel", nextEntretien.getValue("Ordre"));
                    getWorkflowInstance().setValue("DateDEntretien2", nextEntretien.getValue("DateDEntretien"));
                    getWorkflowInstance().setValue("Entretien", nextEntretien.getValue("Entretien"));
                    getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                }
                getWorkflowInstance().save(getWorkflowModule().getSysadminContext());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onBeforeSubmit(action);
    }


	public ArrayList<IAttachment> _PieceJointe (IWorkflowInstance instance , String attachementName)
	{
		ArrayList<IAttachment> attachments = (ArrayList<IAttachment>)instance.getValue(attachementName);
		File file = null;
		ArrayList<IAttachment> files = new ArrayList<>();
		if(attachments != null && attachments.size() > 0){
			for (IAttachment iAttachment : attachments) {
				if(iAttachment!=null)
				{
					try
					{
						file = new File("c://TEST//" + iAttachment.getName());
						FileUtils.writeByteArrayToFile(file, iAttachment.getContent());
						IAttachment attachment = getDirectoryModule().createAttachment(getWorkflowModule().getSysadminContext(), file);
						files.add(attachment);
						file.delete();
						file.deleteOnExit();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}

		}
		return files;
	}
}


