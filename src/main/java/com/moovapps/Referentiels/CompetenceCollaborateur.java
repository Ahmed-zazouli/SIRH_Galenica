package com.moovapps.Referentiels;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CompetenceCollaborateur extends BaseDocumentExtension {

    @Override
    public boolean onBeforeLoad() {
        if(getWorkflowInstance().getValue("Societe")==null){
            IStorageResource collaborateur = (IStorageResource) getWorkflowInstance().getValue("Collaborateur");
            if(collaborateur!=null){
               // getWorkflowInstance().setValue("Societe",collaborateur.getValue("Societe"));

            }
        }
        return super.onBeforeLoad();
    }

    @Override
    public boolean onBeforeSave() {
        IStorageResource collaborateur = (IStorageResource) getWorkflowInstance().getValue("Collaborateur");
        ArrayList<IStorageResource> competencesCollaborateur = getCompetencesCollaboarteurs(collaborateur);
        if(competencesCollaborateur!=null && !competencesCollaborateur.isEmpty()){
            HashMap<IStorageResource, BigDecimal> competencesPoids = new HashMap<>();
            BigDecimal totalPoids = BigDecimal.ZERO;
            for(IStorageResource competence : competencesCollaborateur){
                BigDecimal poids =competence.getValue("Poids")!=null? (BigDecimal) competence.getValue("Poids"):BigDecimal.ONE;
                competencesPoids.put(competence,poids);
                totalPoids = totalPoids.add(poids);
            }
            if(getWorkflowInstance().getValue("sys_Reference")==null){
                BigDecimal currentCompetencepoids =getWorkflowInstance().getValue("Poids")!=null? (BigDecimal) getWorkflowInstance().getValue("Poids"):BigDecimal.ONE;
                competencesPoids.put(null,currentCompetencepoids);
                totalPoids = totalPoids.add(currentCompetencepoids);
            }


            for(Map.Entry<IStorageResource, BigDecimal> entry : competencesPoids.entrySet()) {
                IStorageResource competence = entry.getKey();
                BigDecimal competencePoids = entry.getValue();

                BigDecimal importance = competencePoids.multiply(new BigDecimal(100)).divide(totalPoids,MathContext.DECIMAL128).setScale(2, RoundingMode.DOWN);;
                if(competence!=null){
                    competence.setValue("Importance",importance);
                    competence.save("Importance");
                }else{
                    getWorkflowInstance().setValue("Importance",importance);
                }

            }
        }
        return super.onBeforeSave();
    }



    ArrayList<IStorageResource> getCompetencesCollaboarteurs(IStorageResource collaborateur){
        if(collaborateur==null)return null;
        try{

            IContext sysContext = getWorkflowModule().getSysadminContext();
            IOrganization organization = getDirectoryModule().getOrganization(sysContext, "DefaultOrganization");
            IProject project = getProjectModule().getProject(sysContext, "REFERENTIELCOMMUN", organization);
            ICatalog catalog = getWorkflowModule().getCatalog(sysContext, "REFERENTIEL", ICatalog.IType.STORAGE, project);
            IResourceDefinition definition = getWorkflowModule().getResourceDefinition(sysContext,catalog,"CompetenceSalarie");
            IViewController controller = getWorkflowModule().getViewController(sysContext,IResource.class);
            controller.addEqualsConstraint("Collaborateur",collaborateur);
            return  (ArrayList<IStorageResource>) controller.evaluate(definition);

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
