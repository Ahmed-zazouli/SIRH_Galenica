package com.moovapps.ibb.rh.GestionPaieV2.Excel;

import com.axemble.vdoc.sdk.interfaces.IStorageResource;
import com.axemble.vdoc.sdk.interfaces.IUser;

import java.text.SimpleDateFormat;

public class NewRecruitExcelMethods {


	public String _cin(IUser user){
		return user.getExtendedAttributes().getValue("CIN") != null ? (String)user.getExtendedAttributes().getValue("CIN") : null;
	}
	
	public String _ncsss(IUser user){
		return user.getExtendedAttributes().getValue("NCNSS") != null ? (String)user.getExtendedAttributes().getValue("NCNSS") : null;
	}
	public String _civilitee(IUser user){
		String civilite = null;
		if(user.getTitle() == null) return null;
		switch (user.getTitle()) {
		case "Mr":
			civilite = "0";
			break;
		case "Mlle":
			civilite = "1";
			break;
		case "Mme":
			civilite = "2";
			break;
		}
		return civilite;
	}
	public String _etatCivil(IUser user){
		String etat = null;
		if(user.getExtendedAttributes().getText("EtatCivil") == null) return null;
		switch ((String)user.getExtendedAttributes().getText("EtatCivil")) {
		case "Célibataire":
			etat = "0";
			break;
		case "Marié(e)":
			etat = "1";
			break;
		case "Divorcé(e)":
			etat = "2";
			break;
		case "Veuf(ve)":
			etat = "3";
			break;
		}
		return etat;
	}
	public String _TypeDeSalaire(IUser user){
		String type = null;
		if(user.getExtendedAttributes().getText("TypeDeSalaire") == null) return null;
		switch ((String)user.getExtendedAttributes().getText("TypeDeSalaire")) {
		case "Horaire":
			type = "0";
			break;
		case "Mensuelle":
			type = "1";
			break;
		}
		return type;
	}
	public int _nEnfantACharge(IUser user){
		return user.getExtendedAttributes().getValue("NombreEnfants") != null ? ((Number)user.getExtendedAttributes().getValue("NombreEnfants")).intValue(): null;
	}
	public String _HorairesSociete(IUser user){
		return "HoraiSoc";
	}
	public String _rib(IUser user){
		return user.getExtendedAttributes().getValue("NCompteBancaire") != null ? (String)user.getExtendedAttributes().getValue("NCompteBancaire"): null;
	}
	
	public String _nomBanque(IUser user){
		return user.getExtendedAttributes().getValue("Banque") != null ? (String)user.getExtendedAttributes().getValue("Banque") : null;
	}
	
	public String _nomAgenceBanque(IUser user){
		return user.getExtendedAttributes().getValue("AgenceBancaire") != null ? (String)user.getExtendedAttributes().getValue("AgenceBancaire") : null;
	}
	
	public String _dateAumbauche(IUser user){
		SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
		String dateAumbauche = null;
		if(user.getExtendedAttributes().getValue("DateDEmbauche") != null){			
			dateAumbauche = simpleFormat.format(user.getExtendedAttributes().getValue("DateDEmbauche"));
		}
		return dateAumbauche;
	}
	public String _DateDAnciennete(IUser user){
		SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
		String DateDAnciennete = null;
		if(user.getExtendedAttributes().getValue("DateDEmbauche") != null){			
			DateDAnciennete = simpleFormat.format(user.getExtendedAttributes().getValue("DateDEmbauche"));
		}
		return DateDAnciennete;
		}
	//=======
	public String _DateDeDepartSociete(IUser user){
		SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
		String DateDeDepartSociete= null;
		if(user.getExit() != null){			
			DateDeDepartSociete = simpleFormat.format(user.getExit());
		}
		return DateDeDepartSociete;
		
	}
	public String _DateDEntreeEtablissement(IUser user){
		SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
		String DateDEntreeEtablissement = null;
		if(user.getExtendedAttributes().getValue("DateDEmbauche") != null){			
			DateDEntreeEtablissement = simpleFormat.format(user.getExtendedAttributes().getValue("DateDEmbauche"));
		}
		return DateDEntreeEtablissement;
		}
	public String _DateDeSortieEtablissement(IUser user){
		SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
		String DateDeSortieEtablissement = null;
		if(user.getExit() != null){			
			DateDeSortieEtablissement = simpleFormat.format(user.getExit());
		}
		return DateDeSortieEtablissement;	
		}
	//====
	public Float _SalaireHoraireDuSalarie(IUser user){
		float type = 0;
		if(user.getExtendedAttributes().getText("TypeDeSalaire") == null) return null;
		switch ((String)user.getExtendedAttributes().getText("TypeDeSalaire")) {
		case "Horaire":
			type = ((Number)user.getExtendedAttributes().getValue("TauxHoraire")).floatValue();
			break;
		case "Mensuelle":
			type = ((Number)user.getExtendedAttributes().getValue("Salaire")).floatValue();
			break;
		}
		return type;
		//return user.getExtendedAttributes().getValue("TauxHoraire") != null ? ((Number)user.getExtendedAttributes().getValue("TauxHoraire")).floatValue() : null;
	}
	public String _typeContrat(IUser user){
		return user.getContractType() != null ? (String)user.getContractType() : null;
	}
	
	public String _categorie(IUser user){
		return (String)(user.getExtendedAttributes().getValue("Categorie") != null ? ((IStorageResource)user.getExtendedAttributes().getValue("Categorie")).getValue("sys_Title") : "");
	}
	
	public String _direction(IUser user){
		return (String)(user.getExtendedAttributes().getValue("Direction") != null ? ((IStorageResource)user.getExtendedAttributes().getValue("Direction")).getValue("sys_Title") : "");
	}
	
	public String _departement(IUser user){
		return (String)(user.getExtendedAttributes().getValue("Departement") != null ? ((IStorageResource)user.getExtendedAttributes().getValue("Departement")).getValue("sys_Title") : "");
	}
	
	/*public String _service(IUser user){
		return null;
	}*/
	
	public String _fonction(IUser user){
		return (String)(user.getExtendedAttributes().getValue("Fonction") != null ? ((IStorageResource)user.getExtendedAttributes().getValue("Fonction")).getValue("sys_Title") : "");
	}
	
	/*public String _nomMutuelle(IUser user){
		return user.getExtendedAttributes().getValue("NomDeLaMutuelle") != null ? (String)user.getExtendedAttributes().getValue("NomDeLaMutuelle") : null;
	}*/
	
	/*public double _tauxMutuelle(IUser user){
		return user.getExtendedAttributes().getValue("TauxDeLaMutuelle") != null ? ((Number)user.getExtendedAttributes().getValue("TauxDeLaMutuelle")).doubleValue() : null;
	}*/
	
	/*public String _nomRetraire1(IUser user){
		return user.getExtendedAttributes().getValue("NomRetraite") != null ? (String)user.getExtendedAttributes().getValue("NomRetraite") : null;
	}*/
	
	/*public int _retraire1NAttribue(IUser user){
		return user.getExtendedAttributes().getValue("NumeroAttribue") != null ? ((Number)user.getExtendedAttributes().getValue("NumeroAttribue")).intValue() : null;
	}*/
	
	/*public String _nomRetraire2(IUser user){
		return  user.getExtendedAttributes().getValue("NomDeLaRetraiteComplementaire") != null ? (String)user.getExtendedAttributes().getValue("NomDeLaRetraiteComplementaire") : null;
	}*/
	
	/*public double _retraire2MontantEpargne(IUser user){
		return user.getExtendedAttributes().getValue("MontantEpargneRetraite") != null ? ((Number)user.getExtendedAttributes().getValue("MontantEpargneRetraite")).doubleValue() : null;
	}*/
	
	/*public double _droitAuConges(IUser user){
		return user.getExtendedAttributes().getValue("DroitMensuelle") != null ? ((Number)user.getExtendedAttributes().getValue("DroitMensuelle")).doubleValue(): null;
	}*/
	
}
