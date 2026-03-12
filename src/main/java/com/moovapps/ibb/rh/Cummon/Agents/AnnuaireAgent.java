package com.moovapps.ibb.rh.Cummon.Agents;

import com.axemble.vdoc.sdk.agent.base.BaseAgent;
import com.axemble.vdoc.sdk.interfaces.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class AnnuaireAgent extends BaseAgent {

    public String getAncienneteInDetail(Date dateEmbauche) {
        if (dateEmbauche == null) {
            return null;
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
    @Override
    protected void execute() {
        try{
            IContext context = getWorkflowModule().getSysadminContext();
            IProject project = getProjectModule().getProject(context,"REFERENTIELCOMMUN",getDirectoryModule().getOrganization(context, "DefaultOrganization"));
            ICatalog catalog = getWorkflowModule().getCatalog(context,"REFERENTIEL", 4, project);
            IResourceDefinition resourceDefinition = getWorkflowModule().getResourceDefinition(context, catalog, "FicheCollaborateur");
            Collection<IUser> users = (Collection<IUser>) getDirectoryModule().getUsers(context);
            for(IUser user:users){
               // addUserToGroupeSalarieAndAdmin(user);
                if( user.getExtendedAttributes().getValue("justImported") == null ||  user.getExtendedAttributes().getValue("justImported").equals(false)){
                    continue;
                }
                IStorageResource ficheCollaborateur = getWorkflowModule().createStorageResource(context,resourceDefinition,null,null);
                ficheCollaborateur.setValue("Organisation",user.getOrganization());
                ficheCollaborateur.setValue("Actif",user.isEnable());
                ficheCollaborateur.setValue("Matricule",user.getExtendedAttributes().getValue("Matricule"));
                //civilité
                ficheCollaborateur.setValue("Title",user.getTitle());
                ficheCollaborateur.setValue("LastName",user.getLastName());
                ficheCollaborateur.setValue("FirstName",user.getFirstName());
                ficheCollaborateur.setValue("MobilePhoneNumber",user.getMobilePhoneNumber());
                ficheCollaborateur.setValue("Groupe",user.getExtendedAttributes().getValue("Groupe"));

                ficheCollaborateur.setValue("Birthday",user.getBirthday());
                ficheCollaborateur.setValue("CIN",user.getExtendedAttributes().getValue("CIN"));
                ficheCollaborateur.setValue("NCNSS",user.getExtendedAttributes().getValue("NCNSS"));
                ficheCollaborateur.setValue("Address1",user.getAddress1());
                ficheCollaborateur.setValue("EtatCivil",user.getExtendedAttributes().getValue("EtatCivil"));
                //ficheCollaborateur.setValue("Avatar",user.getTitle());
                ficheCollaborateur.setValue("NombreEnfants",user.getExtendedAttributes().getValue("NombreEnfants"));
                ficheCollaborateur.setValue("Email",user.getEmail());
                ficheCollaborateur.setValue("Identifiant",user.getLogin());
                ficheCollaborateur.setValue("MotDePasse","D3mo@demo");
                //ficheCollaborateur.setValue("ConfirmerMotDePasse",user.getTitle());
                ficheCollaborateur.setValue("NCompteBancaire",user.getExtendedAttributes().getValue("NCompteBancaire"));
                ficheCollaborateur.setValue("Banque",user.getExtendedAttributes().getValue("Banque"));
                ficheCollaborateur.setValue("AgenceBancaire",user.getExtendedAttributes().getValue("AgenceBancaire"));
                ficheCollaborateur.setValue("DateDEmbauche",user.getExtendedAttributes().getValue("DateDEmbauche"));
                ficheCollaborateur.setValue("AncienneteInDetail",getAncienneteInDetail((Date)user.getExtendedAttributes().getValue("DateDEmbauche")));
                ficheCollaborateur.setValue("TypeDuContrat",user.getExtendedAttributes().getValue("TypeDuContrat"));
                //Etablisement
                ficheCollaborateur.setValue("SocieteDonnee",user.getExtendedAttributes().getValue("SocieteDonnee"));
                ficheCollaborateur.setValue("Groupes",user.getExtendedAttributes().getValue("Groupes"));
                ArrayList<IGroup> groupes = (ArrayList<IGroup>) user.getExtendedAttributes().getValue("Groupes");
                if(groupes!=null && groupes.size()>0){
                    for(IGroup g:groupes){
                        g.addMember(user);
                        g.save(context);
                    }
                }
                ficheCollaborateur.setValue("HierarchicalManager",user.getHierarchicalManager());
                ficheCollaborateur.setValue("Exit",user.getExit());
                ficheCollaborateur.setValue("MotifSortie",user.getExtendedAttributes().getValue("MotifSortie"));
                ficheCollaborateur.setValue("TauxHoraire",user.getExtendedAttributes().getValue("TauxHoraire"));
                ficheCollaborateur.setValue("SalaireDeBase",user.getExtendedAttributes().getValue("SalaireDeBase"));
                ficheCollaborateur.setValue("SalaireBrutDH",user.getExtendedAttributes().getValue("SalaireBrut"));
                ficheCollaborateur.setValue("SalaireNETDH",user.getExtendedAttributes().getValue("Salaire"));
                ficheCollaborateur.setValue("Salaire",user.getExtendedAttributes().getValue("Salaire"));
                ficheCollaborateur.setValue("AbsenceAnneeEnCours",0);
                ficheCollaborateur.setValue("AbsenceAnterieure",0);

                ficheCollaborateur.setValue("DroitMensuelle",0);
                ficheCollaborateur.setValue("SoldeAnneeEnCours",0);
                ficheCollaborateur.setValue("SoldeAnterieur",0);
                ficheCollaborateur.setValue("SoldeConges",0);
                ficheCollaborateur.setValue("CongesPayesEnCoursDeValidation",0);
                ficheCollaborateur.setValue("CongesPayesEnCoursDeConsommation",0);
                ficheCollaborateur.setValue("CongesSpeciauxEnCoursDeValidation",0);
                ficheCollaborateur.setValue("CongesSpeciauxEnCoursDeConsommation",0);
                ficheCollaborateur.setValue("CongesMaladieEnCoursDeValidation",0);
                ficheCollaborateur.setValue("CongesMaladieEnCoursDeConsommation",0);
                ficheCollaborateur.setValue("CongesSansSoldeEnCoursDeValidation",0);
                ficheCollaborateur.setValue("CongesSansSoldeEnCoursDeConsommation",0);
                ficheCollaborateur.setValue("JourSEnCoursDeValidation",0);
                ficheCollaborateur.setValue("joursEnCoursConsommation",0);
                ficheCollaborateur.setValue("CongesPayesAnneeEnCours",0);
                ficheCollaborateur.setValue("CongesPayesN1",0);
                ficheCollaborateur.setValue("CongesPayesPris",0);
                ficheCollaborateur.setValue("CongesSpeciauxAnneeEnCours",0);
                ficheCollaborateur.setValue("CongesSpeciauxN1",0);
                ficheCollaborateur.setValue("CongesSpeciauxPris",0);
                ficheCollaborateur.setValue("CongesMaladieAnneeEnCours",0);
                ficheCollaborateur.setValue("CongesMaladieN1",0);
                ficheCollaborateur.setValue("CongesMaladiePris",0);
                ficheCollaborateur.setValue("CongesSansSoldeAnneeEnCours",0);
                ficheCollaborateur.setValue("CongesSansSoldeN1",0);
                ficheCollaborateur.setValue("CongesSansSoldePris",0);
                ficheCollaborateur.setValue("TotalJoursPris",0);

                ficheCollaborateur.setValue("Fonction",user.getExtendedAttributes().getValue("Fonction"));
                ficheCollaborateur.setValue("Fonction2",user.getExtendedAttributes().getValue("Fonction2"));
                ficheCollaborateur.setValue("DisciplineEnseignee",user.getExtendedAttributes().getValue("DisciplineEnseignee"));

                ficheCollaborateur.setValue("ConfirmerMotDePasse","D3mo@demo");
                ficheCollaborateur.setValue("Salarie",user);




               /* Collection<IGroup> userGroupes =(Collection<IGroup>)user.getParents();
                for(IGroup userGroupe : userGroupes){
                    userGroupe.addMember(user);
                    userGroupe.save(context);
                }*/

                ficheCollaborateur.save(context);



            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void addUserToGroupeSalarieAndAdmin(IUser user){
        try{
            IContext sysContext = getWorkflowModule().getSysadminContext();
            String OrganizationName = "IEGGroupes";
            IOrganization org = getDirectoryModule().getOrganization(sysContext,OrganizationName);
            IGroup salarie  = getDirectoryModule().getGroup(sysContext,org,"Salarie");
            IGroup admin = getDirectoryModule().getGroup(sysContext,org,"ADMIN");
            salarie.addMember(user);
            salarie.save(sysContext);
            admin.addMember(user);
            admin.save(sysContext);
        }catch (Exception e){
            e.printStackTrace();
        }

}
}
