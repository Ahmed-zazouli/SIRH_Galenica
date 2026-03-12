package com.moovapps.capone.rh.GestionDeConge.document;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;
import com.axemble.vdp.ui.framework.foundation.Navigator;
import com.moovapps.capone.rh.GestionDeConge.document.action.DemandeModification;
import com.vdoc.sanaEducation.candidatures.aspose.helper.GenerateWordFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ValidationDemandeModificationDateConge extends BaseDocumentExtension {
    @Override
    public boolean onBeforeSubmit(IAction action) {
        if (action.getName().equals("AccepterLaModificationDesDatesDeConge")){
            overrideDateConge();
            setDemandeurFieldsForSignet();
            generer();
        }

        return super.onBeforeSubmit(action);
    }

    private void overrideDateConge() {
        getWorkflowInstance().setValue("DateDeDebut",getWorkflowInstance().getValue("NouvelleDateDebut"));
        getWorkflowInstance().setValue("DateDeFin",getWorkflowInstance().getValue("NouvelleDateFin"));
        getWorkflowInstance().setValue("DateFinReel",getWorkflowInstance().getValue("NouvelleDateFinReelle"));
        getWorkflowInstance().setValue("NombreDeJoursDemandes",getWorkflowInstance().getValue("nouveauNombreDeJoursDeConges"));
        getWorkflowInstance().setValue("totalJoursFerie",getWorkflowInstance().getValue("nouveauTotalJursFeries"));
        getWorkflowInstance().setValue("TotalAbsence",getWorkflowInstance().getValue("NouveauTotalAbsence"));
        getWorkflowInstance().setValue("DebutConge",getWorkflowInstance().getValue("NouvelleTrancheDebut"));
        getWorkflowInstance().setValue("FinConge",getWorkflowInstance().getValue("NouvelleTrancheFin"));
        getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
    }

    private void setDemandeurFieldsForSignet() {
        IUser demandeur = (IUser) getWorkflowInstance().getValue("Demandeur");
        if(demandeur!=null){
            String pattern = "dd/MM/yyyy";

            // Create a DateTimeFormatter with the specified pattern
            SimpleDateFormat formatter = new SimpleDateFormat(pattern);

            String dateDebutTEXT = "";
            String dateFinTEXT = "";
            String nombreDeJourDemande = "";
            String typeConge = (String) getWorkflowInstance().getValue("TypeDeConge");
            if(typeConge.equals("CN")){
                Date dateDeDebut = (Date) getWorkflowInstance().getValue("DateDeDebut");
                Date dateDeFin =(Date) getWorkflowInstance().getValue("DateFinReel");
                dateDebutTEXT = formatter.format(dateDeDebut);
                dateFinTEXT = formatter.format(dateDeFin);
                nombreDeJourDemande = ((Number) getWorkflowInstance().getValue("NombreDeJoursDemandes")).doubleValue()+"";
            }else if(typeConge.equals("CE")){
                Date dateDeDebut = (Date) getWorkflowInstance().getValue("DateDeDebut");
                Date dateDeFin =(Date) getWorkflowInstance().getValue("DateFinReel");
                dateDebutTEXT = formatter.format(dateDeDebut);
                dateFinTEXT = formatter.format(dateDeFin);
                nombreDeJourDemande = ((Number) getWorkflowInstance().getValue("NombreDeJoursExceptionnelle")).doubleValue()+"";

            }else if(typeConge.equals("CM")){
                Date dateDeDebut = (Date) getWorkflowInstance().getValue("DateDeDebut");
                Date dateDeFin =(Date) getWorkflowInstance().getValue("DateFinReel");
                dateDebutTEXT = formatter.format(dateDeDebut);
                dateFinTEXT = formatter.format(dateDeFin);
                nombreDeJourDemande = ((Number) getWorkflowInstance().getValue("NombreDeJoursDemandes")).doubleValue()+"";
            }else if(typeConge.equals("SS")){
                Date dateDeDebut = (Date) getWorkflowInstance().getValue("DateDeDebut");
                Date dateDeFin =(Date) getWorkflowInstance().getValue("DateFinReel");
                dateDebutTEXT = formatter.format(dateDeDebut);
                dateFinTEXT = formatter.format(dateDeFin);
                nombreDeJourDemande = ((Number) getWorkflowInstance().getValue("NombreDeJoursDemandes")).doubleValue()+"";
            }
            getWorkflowInstance().setValue("DateDeDepartConge",dateDebutTEXT);
            getWorkflowInstance().setValue("DateDeRetourConge",dateFinTEXT);
            getWorkflowInstance().setValue("NombreDeJoursDemandesTEXT",nombreDeJourDemande);
            getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
        }
    }

    public void generer(){
        getWorkflowInstance().setValue("DocumentDeConge", new ArrayList());
        try {

            IStorageResource societe = (IStorageResource) getWorkflowInstance().getValue("Societe");
            if(societe ==null)return;
            ArrayList<IAttachment> documentConeg = (ArrayList<IAttachment>) societe.getValue("DocumentConge") ;

            if (documentConeg != null) {
                IAttachment iAttachment = documentConeg.get(0);
                File file = null;
                try {
                    iAttachment.getName();
                    file = new File("C:\\TEST"+ "\\"  + iAttachment.getName());
                    FileUtils.writeByteArrayToFile(file, iAttachment.getContent());
                    GenerateWordFile generateWordFile = new GenerateWordFile();
                    InputStream inputStream = generateWordFile.valorization(getWorkflowModule(), getWorkflowInstance(), iAttachment);
                    Document document = new Document(inputStream);
                    IUser demandeur = null;
                    //if (demandeurObject != null) {
                    demandeur = (IUser) getWorkflowInstance().getValue("Demandeur");
                    //File MyFile = new File("C:\\TEMP" + "\\" + iAttachment.getName() + ".pdf");
                    String congeDocumentTitre = societe.getValue("sys_Title")+"-"+demandeur.getFullName();
                    document.save("C:\\TEMP" + "\\" + congeDocumentTitre + ".pdf", SaveFormat.PDF);
                    getWorkflowModule().addAttachment(getWorkflowInstance(), "DocumentDeConge", new File("C:\\TEMP" + "\\" + congeDocumentTitre + ".pdf"));
                    getWorkflowInstance().save(getWorkflowModule().getSysadminContext());
                    file.delete();
                    file.deleteOnExit();
                    File MyFile = new File("C:\\TEMP" + "\\" + congeDocumentTitre + ".pdf");
                    MyFile.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                getWorkflowInstance().setValue("DocumentDeConge", null);
                Navigator.getNavigator().getRootNavigator().showAlertBox("le fichier est introuvable");
            }
        } catch (Exception e) {
            Navigator.getNavigator().getRootNavigator().showAlertBox("Un problème est survenu, veuillez contacter votre administrateur");
            e.printStackTrace();
        }
    }
}
