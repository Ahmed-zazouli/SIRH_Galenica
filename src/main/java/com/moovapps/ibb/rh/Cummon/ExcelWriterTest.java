package com.moovapps.ibb.rh.Cummon;

import com.axemble.vdoc.sdk.interfaces.*;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.modules.IProjectModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class ExcelWriterTest {
	
//	public class UserCongeModel{
//		IUser user = null;
//		String categorieConges = null;
//		
//		public UserCongeModel(IUser user, String categorieConges){
//			this.user = user;
//			this.categorieConges = categorieConges;
//		}
//		
//		@Override
//		public boolean equals(Object obj) {
//			if (!(obj instanceof UserCongeModel))
//	            return false;
//	        if (obj == this)
//	            return true;
//
//	        UserCongeModel otherModel = (UserCongeModel)obj;
//			return user == otherModel.user && categorieConges.equals(otherModel.categorieConges);
//		}
//		
//		@Override
//		public int hashCode() {
//			return Objects.hash(user, categorieConges);
//		}
//	}
	
////////////  LEGEND ///////////
	
//	{
//		annuaireField: "xxxx()",
//		extendedField: "xxxx",
//		methodInThisClass: "_xxxx",
//		FormuleField: "$xxxxx"
//	}
	
//	List<String> colonnes = Arrays.asList("Matricule", "getLastName()",
//			"getFirstName()", "_getUserAnciennete",
//			"$setUserTauxAncienneteFormule", "$setUserPrimeAncienneteFormule",
//			"M/H", "$setFormuleTauxHoraire", "AbsenceJ", "AbsenceH",
//			"Opposition", "Reprise", "Rappel",
//			"_getUserCongesPayesFromUserCongesMap", "HS25", "HS50", "HS100",
//			"_getUserAvanceFromUserMap", "AvanceConges", "FraisMedicaux",
//			"PrimeBrut", "$setSalaireNetFormule", "Observation", "STC",
//			"STCConges", "STCJTravailles", "SituationSalarie",
//			"NouveauSalarie", "$setJoursTravaillesFormule", "SalaireDeBase",
//			"$SBplusHSFormule", "$setFormuleSBG", "Avantages",
//			"$setFormuleIndemnites", "_calculateUserIndemnitesPlafond","_calculateUserIndemnitesExoneres",
//			"elementExoneres", "$setSBIFormule", "$setFraisProFormule",
//			"$setRetenueCnss", "$setRetenueAMO", "AutreCotisations",
//			"$setNetImposableFormule", "$setIRBrutFormule",
//			"_getPersonnesACharge", "$setIRNetFormule");
	
	
	ArrayList<String> colonnes = new ArrayList<String>(Arrays.asList(
			"_Matricule", "_Civilite", "_Nom", "_Prenom", "_DateNaissance",
			"_CIN", "_Adresse", "_SituationFamiliale", "_DateEmbauche",
			"_TypeContrat", "_Categorie", "_Fonction", "_CentreCout",
			"_SalaireDeBase", "$indemnites", "_NombreJoursTravailles",
			"_NombreHeuresTravailles", "_HS25", "_HS50", "_HS100",
			"_NomMutuelle", "_TauxMutuelle", "_NomRetraite", "_TauxRetraite",
			"_NomRetraireComp", "_TauxRetraiteComp", "_DroitConges", "_NCnss"));
	
	IWorkflowModule workflowModule = null;
	IDirectoryModule directoryModule = null;
	IProjectModule projectModule = null;
	IWorkflowInstance currentDoc = null;
	IContext sysContext = null;
	IUser currentUser = null;
	IStorageResource societe = null;
	int periodeCloture = 1;
	int selectedMonthPaie = -1;
	Date paieFrom = null;
	Date paieTo = null;
	boolean indemnitesCharged = false;
	
//	HashMap<UserCongeModel, JSONObject> userCongesMap = new HashMap<>();
	HashMap<IUser, JSONObject> userMap = new HashMap<>();
	JSONObject userDataModel = new JSONObject();
	
	public ExcelWriterTest(IWorkflowModule workflowModule, IDirectoryModule directoryModule, IProjectModule projectModule,IWorkflowInstance currentDoc){
		userDataModel.put("Absence", null);
		userDataModel.put("CN", null);
		userDataModel.put("CE", null);
		userDataModel.put("CM", null);
		userDataModel.put("SS", null);
		userDataModel.put("Avance", null);
		userDataModel.put("FraixMedicaux", null);
		
		this.workflowModule = workflowModule;
		this.directoryModule = directoryModule;
		this.projectModule = projectModule;
		this.currentDoc = currentDoc;
		this.sysContext = workflowModule.getSysadminContext();
		this.currentUser = workflowModule.getLoggedOnUser();
		this.selectedMonthPaie = Float.valueOf((String)currentDoc.getValue("Mois")).intValue();
		
		String societeName = null;
		if(currentUser.getExtendedAttributes().getValue("Societe") != null){
			societeName = (String)currentUser.getExtendedAttributes().getValue("Societe");
			this.societe = getSociete(societeName);
			if(this.societe != null){
				if(!societe.getValue("ClotureSociete").equals("Mois")){
					periodeCloture = ((Number)societe.getValue("PeriodeDeCloture")).intValue();
				}
			}
		}
		Calendar paieDateFrom = Calendar.getInstance();
		paieDateFrom.set(Calendar.DATE, 1);
		paieDateFrom.set(Calendar.MONTH, selectedMonthPaie);
		if(periodeCloture != 1){
			paieDateFrom.add(Calendar.MONTH, -1);
		}
		paieDateFrom.set(Calendar.DATE, periodeCloture);
		Calendar paieDateTo = Calendar.getInstance();
		paieDateTo.setTime(paieDateFrom.getTime());
		paieDateTo.add(Calendar.MONTH, 1);
		paieDateTo.add(Calendar.DATE, -1);
		paieFrom = paieDateFrom.getTime();
		paieTo = paieDateTo.getTime();
	}
	
	public void main(File file, String filePath) throws IOException {
 		Collection<IUser> myUsers = getUsersWithTheSameSociete(sysContext, directoryModule, currentUser);
		String[] filePathDivided = filePath.split("/");
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
		XSSFSheet firstSheet = workbook.getSheetAt(0);
		BuildCongesByUser();
//		buildAvanceByUser();
		int line = 3;
		for (IUser user : myUsers) {
			XSSFRow row = firstSheet.createRow(line);
			for (int j = 0; j < colonnes.size(); j++) {
				Object value = null;
				String methodName = colonnes.get(j).replace("(", "").replace(")", "").replace("_", "").replace("-", "").replace("$", "");
				if(colonnes.get(j).startsWith("$")){
					Method method;
					try {
						method = this.getClass().getMethod(methodName, IUser.class, XSSFWorkbook.class, Integer.class);
						value = method.invoke(this, user, workbook, j);
						continue;
					} catch (Exception e) {
						e.printStackTrace();
					} 
					
					
//					Method method;
//					try {
//						method = this.getClass().getMethod(methodName, IUser.class, XSSFCell.class);
//						value = method.invoke(this, user, row.createCell(j));;
//						continue;
//					} catch (Exception e) {
//						e.printStackTrace();
//					} 
				}else if(colonnes.get(j).startsWith("-")){
					value = Float.valueOf(methodName);
				} else if(colonnes.get(j).startsWith("_")){
					try {
						Method method = this.getClass().getMethod(methodName, IUser.class);
						value = method.invoke(this, user);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if(colonnes.get(j).endsWith("()")){
					try {
						Method method = user.getClass().getMethod(methodName);
						value = method.invoke(user);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					value = user.getExtendedAttributes().getValue(methodName);
				}
				
				try {
					row.createCell(j).setCellValue(((Number)value).doubleValue());
				} catch (Exception e) {
					try {
						row.createCell(j).setCellValue((Date)value);
					} catch (Exception e2) {
						row.createCell(j).setCellValue((String)value);

					}
				}
				
			}
			line++;
		}
		try (FileOutputStream outputStream = new FileOutputStream(
				"c://TEST//" + filePathDivided[filePathDivided.length - 1])) {
			workbook.write(outputStream);
		}
		
	}
	
	public Object Matricule(IUser user){
		return user.getExtendedAttributes().getValue("Matricule");
	}
	
	public Object Civilite(IUser user){
		return user.getTitle();
	}
	
	public Object Nom(IUser user){
		return user.getLastName();
	}
	
	public Object Prenom(IUser user){
		return user.getFirstName();
	}
	
	public Object DateNaissance(IUser user){
		return user.getBirthday();
	}
	
	public Object CIN(IUser user){
		return user.getExtendedAttributes().getValue("CIN");
	}
	
	public Object Adresse(IUser user){
		return user.getAddress1();
	}
	
	public Object SituationFamiliale(IUser user){
		return null;
	}
	
	public Object DateEmbauche(IUser user){
		return user.getExtendedAttributes().getValue("DateDEmbauche");
	}
	
	public Object TypeContrat(IUser user){
		return user.getContractType();
	}
	
	public Object Categorie(IUser user){
		return user.getExtendedAttributes().getValue("Categorie") != null ? ((IStorageResource)user.getExtendedAttributes().getValue("Categorie")).getValue("sys_Title") : null;
	}
	
	public Object Fonction(IUser user){
		return user.getExtendedAttributes().getValue("Fonction");
	}
	
	public Object CentreCout(IUser user){
		return null;
	}
	
	public Object SalaireDeBase(IUser user){
		return user.getExtendedAttributes().getValue("SalaireDeBase");
	}
	
	public Object indemnites(IUser user, XSSFWorkbook workbook, Integer index){
		if(!indemnitesCharged){			
			IStorageResource ficheUser = getFicheUser(user);
			colonnes.remove(index);
			index++;
			Collection<IStorageResource> indemnites = getAllIndemnites();
			int i = 0;
			for (int j = colonnes.size(); j >= index+1; j--) {
				XSSFCell cell = workbook.getSheetAt(0).getRow(2).getCell(j-1);
				XSSFCell tmpCell = 	workbook.getSheetAt(0).getRow(j).createCell(j);
			}
			XSSFCellStyle style = workbook.getSheetAt(0).getRow(2).getCell(15).getCellStyle();
			for (IStorageResource indemnite : indemnites) {
				colonnes.add(index+i, (String)indemnite.getValue("sys_Title"));
				XSSFCell cell = null;
				for (int j = 0; j < 3; j++) {
					cell = workbook.getSheetAt(0).getRow(j).createCell(index+i);
				}
				
				cell.setCellStyle(style);
				cell.setCellValue((String)indemnite.getValue("sys_Title"));
				i++;
			}
			indemnitesCharged = true;
		}
		return null;
	}
	
	public Object NombreJoursTravailles(IUser user){
		return null;
	}
	
	public Object NombreHeuresTravailles(IUser user){
		return null;
	}
	
	public Object HS25(IUser user){
		return null;
	}
	
	public Object HS50(IUser user){
		return null;
	}
	
	public Object HS100(IUser user){
		return null;
	}
	
	public Object NomMutuelle(IUser user){
		return user.getExtendedAttributes().getValue("SalaireDeBase");
	}
	
	public Object TauxMutuelle(IUser user){
		return user.getExtendedAttributes().getValue("TauxDeLaMutuelle");
	}
	
	public Object NomRetraite(IUser user){
		return user.getExtendedAttributes().getValue("NomRetraite");
	}
	
	public Object TauxRetraite(IUser user){
		return user.getExtendedAttributes().getValue("TauxDeLaRetraire");
	}
	
	public Object NomRetraireComp(IUser user){
		return user.getExtendedAttributes().getValue("NomDeLaRetraiteComplementaire");
	}
	
	public Object TauxRetraiteComp(IUser user){
		return user.getExtendedAttributes().getValue("TauxDeLaRetraiteComplementaire");
	}
	
	public Object DroitConges(IUser user){
		return user.getExtendedAttributes().getValue("DroitMensuelle");
	}
	
	public Object NCnss(IUser user){
		return user.getExtendedAttributes().getValue("NCNSS");
	}
	
	
	
	
	
	
	
	
	
	
	
	private Collection<IStorageResource> getAllIndemnites() {
		Collection<IStorageResource> indemnites = Collections.emptyList();
		try {
			IContext context = workflowModule.getSysadminContext();
			IViewController controller = workflowModule.getViewController(context, IResource.class);
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(context, "REFERENTIELCOMMUN", organization);
			ICatalog catalog = workflowModule.getCatalog(context, "REFERENTIEL",ICatalog.IType.STORAGE, project);
			IResourceDefinition definition = workflowModule.getResourceDefinition(context, catalog, "Indemnites");
			indemnites = controller.evaluate(definition);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return indemnites;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public Object getUserCongesPayesFromUserCongesMap(IUser user){
		if(userMap.get(user) != null){
			return userMap.get(user).get("CN");
		}
		return null;
	}
	
	public int getUserAnciennete(IUser user){
		int anciennete = 0;
		anciennete = DateHelper.getDurationFromADateToNowInYears((Date)user.getExtendedAttributes().getValue("DateDEmbauche"));
		return anciennete;
	}
	
	private String ColumnName(int index)
	{
		char[] chars = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

	    int quotient = index / 26;
	    if (quotient > 0)
	        return ColumnName(quotient-1) + chars[index % 26];
	    else
	        return String.valueOf(chars[index % 26]);
	}
	
	public Object getUserAvanceFromUserMap(IUser user){
		if(userMap.get(user) != null){
			return userMap.get(user).get("Avance");
		}
		return null;
	}

	public XSSFCell setUserTauxAncienneteFormule(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "IF(AND("
				+ ColumnName(colonnes.indexOf("_getUserAnciennete")) + rowIndex
				+ ">=2," + ColumnName(colonnes.indexOf("_getUserAnciennete"))
				+ rowIndex + "<5),5,IF(AND("
				+ ColumnName(colonnes.indexOf("_getUserAnciennete")) + rowIndex
				+ ">=5," + ColumnName(colonnes.indexOf("_getUserAnciennete"))
				+ rowIndex + "<12),10,IF(AND("
				+ ColumnName(colonnes.indexOf("_getUserAnciennete")) + rowIndex
				+ ">=12," + ColumnName(colonnes.indexOf("_getUserAnciennete"))
				+ rowIndex + "<20),15,IF(AND("
				+ ColumnName(colonnes.indexOf("_getUserAnciennete")) + rowIndex
				+ ">=20," + ColumnName(colonnes.indexOf("_getUserAnciennete"))
				+ rowIndex + "<25),20,IF(AND("
				+ ColumnName(colonnes.indexOf("_getUserAnciennete")) + rowIndex
				+ ">=25),25,\"\")))))";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell setUserPrimeAncienneteFormule(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "IFERROR(ROUND("
				+ ColumnName(colonnes.indexOf("$SBplusHSFormule")) + rowIndex
				+ "*"
				+ ColumnName(colonnes.indexOf("$setUserTauxAncienneteFormule"))
				+ rowIndex + "%,2),\"\")";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}

	public XSSFCell setFormuleTauxHoraire(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "IFERROR(ROUND("
				+ ColumnName(colonnes.indexOf("SalaireDeBase"))
				+ rowIndex
				+ "/26/"
				+ (((Number) societe.getValue("durationWorkHours"))
						.floatValue() / 60) + ",2),\"\")";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell setSalaireNetFormule(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "ROUND("
				+ ColumnName(colonnes.indexOf("$setFormuleSBG")) + rowIndex
				+ "-" + ColumnName(colonnes.indexOf("$setIRNetFormule"))
				+ rowIndex + "-"
				+ ColumnName(colonnes.indexOf("$setRetenueCnss")) + rowIndex
				+ "-" + ColumnName(colonnes.indexOf("$setRetenueAMO"))
				+ rowIndex + "-"
				+ ColumnName(colonnes.indexOf("AutreCotisations")) + rowIndex
				+ "-"
				+ ColumnName(colonnes.indexOf("_getUserAvanceFromUserMap"))
				+ rowIndex + "-"
				+ ColumnName(colonnes.indexOf("Opposition"))
				+ rowIndex + ",2)";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell setJoursTravaillesFormule(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "IFERROR(26-" + ColumnName(colonnes.indexOf("AbsenceJ"))  + rowIndex + ",\"\")";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell SBplusHSFormule(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "IFERROR(ROUND("
				+ ColumnName(colonnes.indexOf("SalaireDeBase")) + rowIndex
				+ "/26*"
				+ ColumnName(colonnes.indexOf("$setJoursTravaillesFormule"))
				+ rowIndex + "+(" + ColumnName(colonnes.indexOf("HS25"))
				+ rowIndex + "*"
				+ ColumnName(colonnes.indexOf("$setFormuleTauxHoraire"))
				+ rowIndex + "*25/100)+("
				+ ColumnName(colonnes.indexOf("HS50")) + rowIndex + "*"
				+ ColumnName(colonnes.indexOf("$setFormuleTauxHoraire"))
				+ rowIndex + "*50/100)+("
				+ ColumnName(colonnes.indexOf("HS100")) + rowIndex + "*"
				+ ColumnName(colonnes.indexOf("$setFormuleTauxHoraire"))
				+ rowIndex + "),2),\"\")";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell setFormuleSBG(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "IFERROR(ROUND("
				+ ColumnName(colonnes.indexOf("$SBplusHSFormule")) + rowIndex
				+ "+" + ColumnName(colonnes.indexOf("PrimeBrut")) + rowIndex
				+ "+" + ColumnName(colonnes.indexOf("$setFormuleIndemnites"))
				+ rowIndex + "+" + ColumnName(colonnes.indexOf("Avantages"))
				+ rowIndex + ",2),\"\")";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell setFormuleIndemnites(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "IFERROR(ROUND("
				+ ColumnName(colonnes
						.indexOf("_calculateUserIndemnitesPlafond")) + rowIndex
				+ "/26" + "*"
				+ ColumnName(colonnes.indexOf("$setJoursTravaillesFormule"))
				+ rowIndex + ",2),\"\")";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell setSBIFormule(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "ROUND("
				+ ColumnName(colonnes.indexOf("$setFormuleSBG"))
				+ rowIndex
				+ "-"
				+ ColumnName(colonnes.indexOf("elementExoneres"))
				+ rowIndex
				+ "-"
				+ ColumnName(colonnes
						.indexOf("_calculateUserIndemnitesExoneres"))
				+ rowIndex + ",2)";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell setFraisProFormule(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "IF(ROUND(("
				+ ColumnName(colonnes.indexOf("$setSBIFormule")) + rowIndex
				+ "-" + ColumnName(colonnes.indexOf("Avantages")) + rowIndex
				+ ")*20%,2)>2500,2500,ROUND(("
				+ ColumnName(colonnes.indexOf("$setSBIFormule")) + rowIndex
				+ "-" + ColumnName(colonnes.indexOf("Avantages")) + rowIndex
				+ ")*20%,2))";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell setRetenueCnss(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "ROUND(IF("
				+ ColumnName(colonnes.indexOf("$setSBIFormule")) + rowIndex
				+ ">6000,6000," + ColumnName(colonnes.indexOf("$setSBIFormule"))
				+ rowIndex + ")*4.48%,2)";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell setRetenueAMO(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "ROUND("
				+ ColumnName(colonnes.indexOf("$setSBIFormule")) + rowIndex
				+ "*2.26%,2)";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell setNetImposableFormule(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "ROUND("
				+ ColumnName(colonnes.indexOf("$setSBIFormule")) + rowIndex
				+ "-" + ColumnName(colonnes.indexOf("$setFraisProFormule")) + rowIndex 
				+ "-" + ColumnName(colonnes.indexOf("$setRetenueCnss")) + rowIndex
				+ "-" + ColumnName(colonnes.indexOf("$setRetenueAMO")) + rowIndex 
				+ "-" + ColumnName(colonnes.indexOf("AutreCotisations")) + rowIndex
				+ ",2)";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}

	public XSSFCell setIRBrutFormule(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "ROUND(IF(AND("
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + ">=0,"
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + "<=2500),0,IF(AND("
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + ">=2501,"
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + "<=4166),"
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + "*10%-250,IF(AND("
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + ">=4167,"
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + "<=5000),"
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + "*20%-666.67,IF(AND("
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + ">=5001,"
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + "<=6666),"
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + "*30%-1166.67,IF(AND("
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + ">=6667,"
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + "<=15000),"
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + "*34%-1433.33,"
				+ ColumnName(colonnes.indexOf("$setNetImposableFormule"))
				+ rowIndex + "*38%-2033.33))))),2)";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	public XSSFCell setIRNetFormule(IUser user, XSSFCell cell) {
		int rowIndex = cell.getRowIndex() + 1;
		String strFormula = "IF(ROUND("
				+ ColumnName(colonnes.indexOf("$setIRBrutFormule")) + rowIndex
				+ "-IF(" + ColumnName(colonnes.indexOf("_getPersonnesACharge"))
				+ rowIndex + ">6,6,"
				+ ColumnName(colonnes.indexOf("_getPersonnesACharge"))
				+ rowIndex + ")*30,2)<0,0,ROUND("
				+ ColumnName(colonnes.indexOf("$setIRBrutFormule")) + rowIndex
				+ "-IF(" + ColumnName(colonnes.indexOf("_getPersonnesACharge"))
				+ rowIndex + ">6,6,"
				+ ColumnName(colonnes.indexOf("_getPersonnesACharge"))
				+ rowIndex + ")*30,2))";
		cell.setCellType(CellType.FORMULA);
		cell.setCellFormula(strFormula);
		return cell;
	}
	
	private void BuildCongesByUser() {
		Collection<IWorkflowInstance> allConges = getAllCongesOfUsersInTheSameSociete();
		for (IWorkflowInstance conge : allConges) {
			Date dateDebutConge = (Date)conge.getValue("DateDeDebut");
			Date dateFinConge = (Date)conge.getValue("DateDeFin");
			if (DateHelper.isTwoRangesOverlapped(dateDebutConge, dateFinConge, paieFrom, paieTo)) {
				Date calculStartDate = dateDebutConge;
				Date calculFinDate = dateFinConge;
				boolean isStartDateChanged = false;
				boolean isEndDateChanged = false;
				if(DateHelper.isDateBeforeDate(calculStartDate, paieFrom)){
					calculStartDate = paieFrom;
					isStartDateChanged = true;
				}
				if(DateHelper.isDateAfterDate(calculFinDate, paieTo)){
					calculFinDate = paieTo;
					isEndDateChanged = true;
				}
				
				float nombreJours = (Float) new WorkingDaysNumberCalculator(
						workflowModule, getJoursOuvrables(societe),
						getJoursOuvres(societe)).calculateV2(societe,calculStartDate,
						calculFinDate).get("nbrJoursDemande");
				nombreJours -= calculateTrancheMinusValue(dateDebutConge, dateFinConge, conge, isStartDateChanged, isEndDateChanged);
				
//				UserCongeModel userModel = new UserCongeModel((IUser)conge.getValue("Demandeur"), (String)conge.getValue("TypeDeConge"));
				IUser demandeur = (IUser)conge.getValue("Demandeur");
				String categorieConges = (String)conge.getValue("TypeDeConge");
				JSONObject tmp = new JSONObject();
				tmp = userDataModel;
				if(userMap.containsKey(demandeur)){
					tmp = userMap.get(demandeur);
					if(tmp.get(categorieConges) != null){						
						tmp.put(categorieConges, (Float)tmp.get(categorieConges) + nombreJours);
					}else{
						tmp.put(categorieConges, nombreJours);
					}
				} else {
					tmp.put(categorieConges, nombreJours);
				}
				userMap.put(demandeur, tmp);
			}
		}
	}
	
	private void buildAvanceByUser() {
		Collection<IWorkflowInstance> allAvances = getAllAvancesOfUsersInTheSameSociete();
		for (IWorkflowInstance avance : allAvances) {
			IUser beneficiaire= (IUser)avance.getValue("Beneficiaire");
			JSONObject tmp = new JSONObject();
			if(userMap.containsKey(beneficiaire)){
				tmp = userMap.get(beneficiaire);
				tmp.put("Avance", ((Number)tmp.get("Avance")).floatValue() + ((Number)avance.getValue("Montant")).floatValue());
			}else{
				tmp.put("Avance", avance.getValue("Montant"));
			}
			userMap.put(beneficiaire, tmp);
		}

	}
	
	public double calculateUserIndemnitesPlafond(IUser user) {
		IStorageResource ficheUser = getFicheUser(user);
		Collection<IStorageResource> indemnites = getUserIndemnites(ficheUser);
		float totalIndemnites = 0;
		for (IStorageResource indemnite : indemnites) {
			totalIndemnites+= ((Number)indemnite.getValue("Plafond")).floatValue();
		}
		return totalIndemnites;
	}
	
	public double calculateUserIndemnitesExoneres(IUser user) {
		IStorageResource ficheUser = getFicheUser(user);
		Collection<IStorageResource> indemnites = getUserIndemnites(ficheUser);
		float totalIndemnitesExoneres = 0;
		for (IStorageResource indemnite : indemnites) {
			if((boolean)((IStorageResource)indemnite.getValue("Indemnite")).getValue("Exonere") == true){				
				totalIndemnitesExoneres+= ((Number)indemnite.getValue("Plafond")).floatValue();
			}
		}
		return totalIndemnitesExoneres;
	}
	
	public int getPersonnesACharge(IUser user) {
		IStorageResource ficheUser = getFicheUser(user);
		Collection<IStorageResource> personnesACharge = getUserLienParente(ficheUser);
		return personnesACharge.size();
	}
	
	public double buildAvanceByUser(IUser user) {
		return 0;
	}
	
	public ArrayList<Integer> getJoursOuvrables(IStorageResource societe) {
		ArrayList<Integer> joursOuvrables = new ArrayList<Integer>(Arrays.asList(2,3,4,5,6,7));
		int joursOuvrablesDebut = -1;
		int joursOuvrablesFin = -1;
		
		if(societe.getValue("JoursOuvrablesDebut") != null){
			joursOuvrablesDebut = Integer.parseInt(((String)societe.getValue("JoursOuvrablesDebut")));
		}
		if(societe.getValue("JoursOuvrablesFin") != null){
			joursOuvrablesFin = Integer.parseInt(((String)societe.getValue("JoursOuvrablesFin")));
		}
		if(joursOuvrablesDebut != -1 && joursOuvrablesFin != -1){	
			joursOuvrables.clear();
			int i = joursOuvrablesDebut;
			joursOuvrables.add(i);
			do {
				i = ((i+1)/8) != 0 ? 1 : (i+1);
				joursOuvrables.add(i);
			} while(i != joursOuvrablesFin);
		}
		
		return joursOuvrables;
	}
	
	public ArrayList<Integer> getJoursOuvres(IStorageResource societe) {
		ArrayList<Integer> joursOuvres = new ArrayList<Integer>(Arrays.asList(2,3,4,5,6));
		int joursOuvresDebut = -1;
		int joursOuvresFin = -1;
		
		if(societe.getValue("JoursOuvresDebut") != null){
			joursOuvresDebut = Integer.parseInt(((String)societe.getValue("JoursOuvresDebut")));
		}
		if(societe.getValue("JoursOuvresFin") != null){
			joursOuvresFin = Integer.parseInt(((String)societe.getValue("JoursOuvresFin")));
		}
		if(joursOuvresDebut != -1 && joursOuvresFin != -1){
			joursOuvres.clear();
			int i = joursOuvresDebut;
			joursOuvres.add(joursOuvresDebut);
			do {
				i = ((i+1)/8) != 0 ? 1 : (i+1);
				joursOuvres.add(i);
			} while(i != joursOuvresFin);			
		}
		
		return joursOuvres;
	}
	
	private float calculateTrancheMinusValue(Date dateDebutConge, Date dateFinConge, IWorkflowInstance conge, boolean isStartDateChanged, boolean isEndDateChanged) {
		float heureDebutFinMinus = 0;
		if(dateDebutConge.equals(dateFinConge)){
			if(!isStartDateChanged && conge.getValue("DebutConge").equals("DJ")){				
				heureDebutFinMinus += .5;
			}
		} else{
			if(!isStartDateChanged && conge.getValue("DebutConge").equals("DJ")){
				heureDebutFinMinus += .5;
			}
			if(!isEndDateChanged && conge.getValue("FinConge").equals("DJ")){				
				heureDebutFinMinus += .5;
			}
		}
		return heureDebutFinMinus;
	}
	
	private IStorageResource getSociete(String societeName) {
		IStorageResource societe = null;
		if(societeName != null){			
			try {
				IViewController controller = workflowModule.getViewController(sysContext, IResource.class);
				IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
				IProject project = projectModule.getProject(sysContext,"REFERENTIELCOMMUN",organization);
				ICatalog catalog = workflowModule.getCatalog(sysContext,"REFERENTIEL", 4, project);
				IResourceDefinition definition = workflowModule.getResourceDefinition(sysContext, catalog, "Societe");
				controller.addEqualsConstraint("sys_Title", societeName);
				Collection<IStorageResource> societes = controller.evaluate(definition);
				if (!societes.isEmpty()){				
					societe = societes.iterator().next();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return societe;
	}
	
	private Collection<IWorkflowInstance> getAllCongesOfUsersInTheSameSociete()
	{
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try
		{
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(sysContext, "Capone", organization);
			ICatalog catalog = workflowModule.getCatalog(sysContext, "RH", project);
			IWorkflowContainer w = workflowModule.getWorkflowContainer(sysContext, catalog, "GestionDeConges");
			IViewController controller = workflowModule.getViewController(sysContext);
			controller.addEqualsConstraint("Societe", currentUser.getExtendedAttributes().getValue("Societe"));
			controller.addEqualsConstraint("DocumentState", "Clôturé");
			collection = controller.evaluate(w);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		return collection;
	}
	
	private Collection<IWorkflowInstance> getAllAvancesOfUsersInTheSameSociete()
	{
		Collection<IWorkflowInstance> collection = Collections.emptyList();
		try
		{
			IContext sysContext = workflowModule.getSysadminContext();
			IUser connectedUser = workflowModule.getLoggedOnUser();
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(sysContext, "GestionDesAvances", organization);
			ICatalog catalog = workflowModule.getCatalog(sysContext, "GestionDesAvances", project);
			IWorkflow w = workflowModule.getWorkflow(sysContext, catalog, "GestionDesAvances_1.0");
			IViewController controller = workflowModule.getViewController(sysContext);
			controller.addEqualsConstraint("Societe", connectedUser.getExtendedAttributes().getValue("Societe"));
			controller.addNotEqualsConstraint("DocumentState", "Accordée");
			controller.addGreaterConstraint("sys_CreationDate", paieFrom);
			controller.addLessConstraint("sys_CreationDate", paieTo);
			collection = controller.evaluate(w);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		return collection;
	}
	
	private Collection<IUser> getUsersWithTheSameSociete(IContext context, IDirectoryModule directoryModule, IUser currentUser) {
		Collection<IUser> usersWithTheSameSociete = Collections.emptyList();
		usersWithTheSameSociete = (Collection<IUser>) directoryModule.getUsers(context, currentUser.getOrganization());
		return usersWithTheSameSociete;
	}

	private IStorageResource getFicheUser(IUser user) {
		IStorageResource ficheUser = null;
		try {
			IContext context = workflowModule.getSysadminContext();
			IViewController controller = workflowModule.getViewController(context, IResource.class);
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(context, "REFERENTIELCOMMUN", organization);
			ICatalog catalog = workflowModule.getCatalog(context, "REFERENTIEL",ICatalog.IType.STORAGE, project);
			IResourceDefinition definition = workflowModule.getResourceDefinition(context, catalog, "FicheCollaborateur");
			controller.addEqualsConstraint("Salarie", user);
			Collection<IStorageResource> demandeurFiches = controller.evaluate(definition);
			if (!demandeurFiches.isEmpty()){				
				ficheUser = demandeurFiches.iterator().next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ficheUser;
	}
	
	private Collection<IStorageResource> getUserIndemnites(IStorageResource ficheUser) {
		Collection<IStorageResource> indemnites = Collections.emptyList();
		try {
			IContext context = workflowModule.getSysadminContext();
			IViewController controller = workflowModule.getViewController(context, IResource.class);
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(context, "REFERENTIELCOMMUN", organization);
			ICatalog catalog = workflowModule.getCatalog(context, "REFERENTIEL",ICatalog.IType.STORAGE, project);
			IResourceDefinition definition = workflowModule.getResourceDefinition(context, catalog, "IndemnitesSalarie");
			controller.addEqualsConstraint("SalarieFiche", ficheUser);
			indemnites = controller.evaluate(definition);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return indemnites;
	}
	
	private Collection<IStorageResource> getUserLienParente(IStorageResource ficheUser) {
		Collection<IStorageResource> liensParente = Collections.emptyList();
		try {
			IContext context = workflowModule.getSysadminContext();
			IViewController controller = workflowModule.getViewController(context, IResource.class);
			IOrganization organization = directoryModule.getOrganization(sysContext, "DefaultOrganization");
			IProject project = projectModule.getProject(context, "REFERENTIELCOMMUN", organization);
			ICatalog catalog = workflowModule.getCatalog(context, "REFERENTIEL",ICatalog.IType.STORAGE, project);
			IResourceDefinition definition = workflowModule.getResourceDefinition(context, catalog, "LienParente");
			controller.addEqualsConstraint("FicheSalarie", ficheUser);
			liensParente = controller.evaluate(definition);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return liensParente;
	}
}
